package MarioAI;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.CollisionDetection;
import MarioAI.graph.edges.EdgeCreator;
import MarioAI.marioMovement.MarioControls;
import MarioAI.path.PathCreator;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;


public class FastAndFurious extends KeyAdapter implements Agent {
	public final World world = new World();
	private final EdgeCreator grapher = new EdgeCreator();
	private final PathCreator pathCreator = new PathCreator(Runtime.getRuntime().availableProcessors() - 2);
	private final MarioControls marioController = new MarioControls();
	private final EnemyPredictor enemyPredictor = new EnemyPredictor();
	private int tickCount = 0;
	public boolean DEBUG = true;
	
	private boolean pauseGame = false;
	private boolean unpauseForOneTick = false;
	private boolean savePlace = false;
	private boolean deletePlace = false;
	private boolean runToTick = false;
	private int tickToRunTo = -1;
	public static String saveStateFileName = "levelState.lvlst";
	private Object keyLock = new Object();

	public void reset() {
		marioController.reset();
	}
	
	public boolean[] getAction(Environment observation) {
		executeKeyCommands(observation);
		
		boolean[] action = new boolean[Environment.numberOfButtons];

		if (tickCount == 30) {
			//Create the initial world and all its edges
			world.initialize(observation);
			grapher.setMovementEdges(world, world.getMarioNode(observation));
			
			CollisionDetection.loadTileBehaviors();
			
			enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
			
			
			pathCreator.initialize(observation);
			pathCreator.syncWithRealWorld(world, enemyPredictor);
			findPath(observation);
			
		} else if (tickCount > 30) {
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
			marioController.update(observation);
			world.update(observation);
			
			if (world.hasWorldChanged()) {
				grapher.setMovementEdges(world, world.getMarioNode(observation));
				world.resetHasWorldChanged();
			}
			
			if ((world.hasGoalNodesChanged() || 
				 MarioControls.isPathInvalid(observation, pathCreator.getBestPath()) ||
				 enemyPredictor.hasNewEnemySpawned() ||
				 pathCreator.getBestPath() == null) && 
				marioController.canUpdatePath || 
				!pathCreator.isRunning) 
			{
				/*
				pathCreator.syncWithRealWorld(world, enemyPredictor);
				findPath(observation);
				*/
				/*
				if (world.hasGoalNodesChanged()) {
					System.out.println("Reason: World");
				}
				if (MarioControls.isPathInvalid(observation, pathCreator.getBestPath())) {
					System.out.println("Reason: Path invalid");
				}
				if (enemyPredictor.hasNewEnemySpawned()) {
					System.out.println("Reason: New enemies");
				}
				if (marioController.canUpdatePath) {
					System.out.println("Reason: Edge finished");
				}
				if (pathCreator.getBestPath() == null) {
					System.out.println("Reason: No path");
				}
				*/
				
				if (pathCreator.isRunning) {
					pathCreator.stop();
					pathCreator.updateBestPath();
					//System.out.println("Tick: " + tickCount + " Stopped");
				}
				if (!pathCreator.isRunning && 
					 pathCreator.getBestPath() != null && 
					 pathCreator.getBestPath().size() > 0) {
					pathCreator.syncWithRealWorld(world, enemyPredictor);
					startFindingPathFromPreviousPath(observation);
					//System.out.println("Tick: " + tickCount + " Started\n");
				}
				if (!pathCreator.isRunning && 
					(pathCreator.getBestPath() == null || 
					 pathCreator.getBestPath().size() == 0)) {
					
					pathCreator.syncWithRealWorld(world, enemyPredictor);
					findPath(observation);
					//System.out.println("Failed to find path. Restarting.");
				}
				
				
				
				world.resetGoalNodesChanged();
				enemyPredictor.resetNewEnemySpawned();
			}
			else if (marioController.canUpdatePath) {
				pathCreator.stop();
				//System.out.println("Tick: " + tickCount + " Path ignored");
			}
			
			action = marioController.getNextAction(observation, pathCreator.getBestPath());
			
			if (DEBUG) {
				DebugDraw.resetGraphics(observation);
				DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
				DebugDraw.drawBlockBeneathMarioNeighbors(observation, world);
				DebugDraw.drawEdges(observation, world.getLevelMatrix());
				DebugDraw.drawMarioReachableNodes(observation, world);
				DebugDraw.drawNodeEdgeTypes(observation, world.getLevelMatrix());
				//DebugDraw.drawEnemies(observation, enemyPredictor);
				DebugDraw.drawMarioNode(observation, world.getMarioNode(observation));
				DebugDraw.drawPathEdgeTypes(observation, pathCreator.getBestPath());
				DebugDraw.drawPathMovement(observation, pathCreator.getBestPath());
				DebugDraw.drawAction(observation, action);
				//TestTools.renderLevel(observation);
				//System.out.println();
			}
		}
		tickCount++;
		
		return action;
	}
	
	public void findPath(Environment observation) {
		final int marioHeight = MarioMethods.getMarioHeightFromMarioMode(observation.getMarioMode());
		//long startTime = System.currentTimeMillis();
		pathCreator.blokingFindPath(observation, world.getMarioNode(observation), world.getGoalNodes(0), marioController.getXVelocity(), marioHeight);
		//System.out.println(System.currentTimeMillis() - startTime);
	}
	
	public void startFindingPathFromPreviousPath(Environment observation) {
		final int marioHeight = MarioMethods.getMarioHeightFromMarioMode(observation.getMarioMode());
		//long startTime = System.currentTimeMillis();
		pathCreator.start(observation, pathCreator.getBestPath(), world.getGoalNodes(0), marioHeight);
	}
	
	private void executeKeyCommands(Environment observation) {
		synchronized (keyLock) {
			unpauseForOneTick = false;
			
			if (savePlace) {
				save(observation);
				savePlace = false;
			}
			
			if (deletePlace) {
				delete();
				deletePlace = false;
			}
		}
		if (runToTick && tickToRunTo == tickCount) {
			pauseGame = true;
		}
		while(pauseGame && !unpauseForOneTick) {
			try {
				Thread.sleep(10);
				
				if (savePlace) {
					save(observation);
					savePlace = false;
				}
				
				if (deletePlace) {
					delete();
					deletePlace = false;
				}
			} catch (InterruptedException e) { }
		}
	}
	
	private void save(Environment observation) {
		final long seed = ((MarioComponent)observation).getLevelScene().getSeed();
		String fileContent = seed + " " + tickCount;
		
		try {
			Files.write(Paths.get(saveStateFileName), fileContent.getBytes(), StandardOpenOption.CREATE);
			System.out.println("Saved game stat to file.");
		} catch (IOException e) {
			System.out.println("Failed to save game state.");
		}
	}
	
	private void delete() {
		try {
			Files.delete(Paths.get(saveStateFileName));
			System.out.println("Deleted save state file.");
		} catch (IOException e) {
			System.out.println("Failed to delete save state.");
		}
	}
	
    public void keyPressed(KeyEvent e)
    {
        toggleKey(e.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent e)
    {
        toggleKey(e.getKeyCode(), false);
    }
    
    private void toggleKey(int keyCode, boolean pressed) {
    	switch (keyCode) {
		case KeyEvent.VK_P:
			if (pressed) {
				pauseGame = !pauseGame;	
			}
			break;
		case KeyEvent.VK_O:
			synchronized (keyLock) {
				unpauseForOneTick = pressed;	
			}
			break;
		case KeyEvent.VK_I:
			synchronized (keyLock) {
				savePlace = pressed;	
			}
			break;
		case KeyEvent.VK_L:
			synchronized (keyLock) {
				deletePlace = pressed;
			}
			break;
		}
    }
    
    public void runToTick(int tick) {
    	tickToRunTo = tick;
    	runToTick = true;
    }

	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return "'; DROP TABLE Grades; --";
	}

	public void setName(String name) {
	}

	
}
