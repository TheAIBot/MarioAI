package MarioAI;

import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.CollisionDetection;
import MarioAI.graph.edges.EdgeCreator;
import MarioAI.graph.nodes.Node;
import MarioAI.marioMovement.MarioControls;
import MarioAI.path.PathCreator;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;
import tests.TestTools;


public class FastAndFurious implements Agent {
	public final World world = new World();
	private final EdgeCreator grapher = new EdgeCreator();
	private final PathCreator pathCreator = new PathCreator(Runtime.getRuntime().availableProcessors() - 1);
	private final MarioControls marioController = new MarioControls();
	private final EnemyPredictor enemyPredictor = new EnemyPredictor();
	private int tickCount = 0;
	
	public boolean DEBUG = true;

	public void reset() {
		marioController.reset();
	}
	
	public boolean[] getAction(Environment observation) {
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
				if (world.hasGoalNodesChanged()) {
					System.out.println("Reason: World");
				}
				if (MarioControls.isPathInvalid(observation, pathCreator.getBestPath())) {
					System.out.println("Reason: Path invalid");
				}
				if (enemyPredictor.hasNewEnemySpawned()) {
					System.out.println("Reason: New enemies");
				}
				if (pathCreator.getBestPath() == null) {
					System.out.println("Reason: No path");
				}
				
				if (pathCreator.isRunning) {
					pathCreator.stop();
					pathCreator.updateBestPath();
					System.out.println("Tick: " + tickCount + " Stopped");
				}
				if (!pathCreator.isRunning) {
					pathCreator.syncWithRealWorld(world, enemyPredictor);
					startFindingPathFromPreviousPath(observation);
					System.out.println("Tick: " + tickCount + " Started\n");
				}
				
				
				
				world.resetGoalNodesChanged();
				enemyPredictor.resetNewEnemySpawned();
			}
			else if (marioController.canUpdatePath) {
				pathCreator.stop();
				System.out.println("Tick: " + tickCount + " Path ignored");
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

	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return "'; DROP TABLE Grades; --";
	}

	public void setName(String name) {
	}

	
}
