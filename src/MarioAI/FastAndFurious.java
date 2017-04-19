package MarioAI;

import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import com.sun.xml.internal.ws.api.server.AbstractServerAsyncTransport;

import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemy.EnemyPredictor;
import MarioAI.graph.Graph;
import MarioAI.graph.Grapher;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;
import sun.security.krb5.Asn1Exception;

/**
 * Main program agent.
 */
public class FastAndFurious implements Agent {

	private static final String name = "The painkiller";
	private final Graph graph = new Graph();
	private final Grapher grapher = new Grapher();
	private final AStar aStar = new AStar();
	private final MarioControls marioController = new MarioControls();
	private final EnemyPredictor enemyPredictor = new EnemyPredictor();
	private int tickCount = 0;
	private ArrayList<DirectedEdge> newestPath = null;
	
	public boolean DEBUG = true;

	public void reset() {
		marioController.reset();
	}

	public boolean[] getAction(Environment observation) {
		boolean[] action = new boolean[Environment.numberOfButtons];

		if (tickCount == 30) {
			graph.createStartGraph(observation);
			grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
			newestPath = getPath(observation);
			enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
			
		} else if (tickCount > 30) {
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
			
			if (graph.updateMatrix(observation)) {
				//graph.printMatrix(observation);
				//long startTime = System.currentTimeMillis();
				grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
				//System.out.println(System.currentTimeMillis() - startTime);
			}
			
			if (DEBUG) {
				DebugDraw.resetGraphics(observation);
				DebugDraw.drawGoalNodes(observation, graph.getGoalNodes(0));
				DebugDraw.drawBlockBeneathMarioNeighbors(observation, graph);
				DebugDraw.drawEdges(observation, graph.getLevelMatrix());
				DebugDraw.drawMarioReachableNodes(observation, graph);
				DebugDraw.drawNodeEdgeTypes(observation, graph.getLevelMatrix());
				//DebugDraw.drawEnemies(observation, enemyPredictor);
				DebugDraw.drawMarioNode(observation, graph.getMarioNode(observation));
			}
			
			if (newestPath == null || newestPath.size() == 0) {
				grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation)); // TODO probably not nessesary
				newestPath = getPath(observation);
			}
			
			if (newestPath != null && newestPath.size() > 0) { //TODO Must also allowed to be 1, but adding this gives an error
				if (MarioControls.reachedNextNode(observation, newestPath) && graph.goalNodesChanged() || 
					newestPath.size() > 0 && MarioControls.isPathInvalid(observation, newestPath) ||
					enemyPredictor.hasNewEnemySpawned()) {
					newestPath = getPath(observation);
					graph.setGoalNodesChanged(false);
				}
				
				if (newestPath != null && newestPath.size() > 0) {
					action = marioController.getNextAction(observation, newestPath);
				}
				
				if (DEBUG) {
					//DebugDraw.drawPath(observation, newestPath);
					DebugDraw.drawPathEdgeTypes(observation, newestPath);
					DebugDraw.drawPathMovement(observation, newestPath);
				}
			}
			
			if (DEBUG) {
				DebugDraw.drawAction(observation, action);
			}
		}
		tickCount++;
		
		return action;
	}
	
	private ArrayList<DirectedEdge> getPath(Environment observation) {
		final int marioHeight = MarioMethods.getMarioHeightFromMarioMode(observation.getMarioMode());
		//long startTime = System.currentTimeMillis();
		final ArrayList<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), marioController.getXVelocity(), enemyPredictor, marioHeight);
		//System.out.println(System.currentTimeMillis() - startTime);
		
		return (path == null)? newestPath : path;
	}

	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
	}
}
