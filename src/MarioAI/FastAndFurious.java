package MarioAI;

import java.util.ArrayList;

import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemy.EnemyPredictor;
import MarioAI.graph.CollisionDetection;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.EdgeCreator;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;


public class FastAndFurious implements Agent {
	public final World world = new World();
	public final EdgeCreator grapher = new EdgeCreator();
	public final AStar aStar = new AStar();
	public final MarioControls marioController = new MarioControls();
	public final EnemyPredictor enemyPredictor = new EnemyPredictor();
	private ArrayList<DirectedEdge> newestPath = null;
	private int tickCount = 0;
	
	public boolean DEBUG = false;

	public void reset() {
		marioController.reset();
	}

	public boolean[] getAction(Environment observation) {
		boolean[] action = new boolean[Environment.numberOfButtons];

		if (tickCount == 30) {
			world.initialize(observation);
			grapher.setMovementEdges(world, world.getMarioNode(observation));
			
			CollisionDetection.setWorld(world);
			CollisionDetection.loadTileBehaviors();
			
			newestPath = getPath(observation);
			enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
			
		} else if (tickCount > 30) {
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
			marioController.update(observation);
			world.update(observation);
			
			if (world.hasWorldChanged()) {
				grapher.setMovementEdges(world, world.getMarioNode(observation));
				world.resetHasWorldChanged();
			}
			
			boolean blue = false;
			if (world.hasGoalNodesChanged() || 
				 MarioControls.isPathInvalid(observation, newestPath) ||
				 enemyPredictor.hasNewEnemySpawned()) {
				blue = true;
			}
			
			if ((world.hasGoalNodesChanged() || 
				 MarioControls.isPathInvalid(observation, newestPath) ||
				 enemyPredictor.hasNewEnemySpawned()) && 
				marioController.canUpdatePath) 
			{
				//if path changed then blue = false
				newestPath = getPath(observation);
				world.resetGoalNodesChanged();
				enemyPredictor.resetNewEnemySpawned();
			}
			
			if (newestPath != null && newestPath.size() > 0) {
				action = marioController.getNextAction(observation, newestPath);
			}
			
			if (DEBUG) {
				DebugDraw.resetGraphics(observation);
				DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
				DebugDraw.drawBlockBeneathMarioNeighbors(observation, world);
				DebugDraw.drawEdges(observation, world.getLevelMatrix());
				DebugDraw.drawMarioReachableNodes(observation, world);
				DebugDraw.drawNodeEdgeTypes(observation, world.getLevelMatrix());
				//DebugDraw.drawEnemies(observation, enemyPredictor);
				DebugDraw.drawMarioNode(observation, world.getMarioNode(observation));
				DebugDraw.drawPathEdgeTypes(observation, newestPath);
				DebugDraw.drawPathMovement(observation, newestPath);
				DebugDraw.drawAction(observation, action);
				//TestTools.renderLevel(observation);
				//System.out.println();
			}
		}
		tickCount++;
		
		return action;
	}
	
	public ArrayList<DirectedEdge> getPath(Environment observation) {
		int timeToRun = Integer.MAX_VALUE; //TODO set proper value
		
		final int marioHeight = MarioMethods.getMarioHeightFromMarioMode(observation.getMarioMode());
		//long startTime = System.currentTimeMillis();
		final ArrayList<DirectedEdge> path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), marioController.getXVelocity(), enemyPredictor, marioHeight, timeToRun);
		//System.out.println(System.currentTimeMillis() - startTime);
		return (path == null) ? newestPath : path;
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
