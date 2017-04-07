package MarioAI;

import java.util.List;

import MarioAI.debugGraphics.DebugDraw;
import MarioAI.graph.Graph;
import MarioAI.graph.Grapher;
import MarioAI.graph.edges.DirectedEdge;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 * Main program agent.
 */
public class FastAndFurious implements Agent {

	private static final String name = "The painkiller";
	private final Graph graph = new Graph();
	private final Grapher grapher = new Grapher();
	private final MarioControls marioController = new MarioControls();
	private int tickCount = 0;
	private List<DirectedEdge> newestPath = null;
	
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
			
		} else if (tickCount > 30) {
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
			}
			
			if (newestPath != null && newestPath.size() > 0) { //TODO Must also allowed to be 1, but adding this gives an error
				if (MarioControls.reachedNextNode(observation, newestPath) && graph.goalNodesChanged() || 
					 MarioControls.isPathInvalid(observation, newestPath)) {
					newestPath = getPath(observation);
					graph.setGoalNodesChanged(false);
				}
				
				marioController.getNextAction(observation, newestPath, action);
				
				if (DEBUG) {
					DebugDraw.drawPath(observation, newestPath);
					DebugDraw.drawPathEdgeTypes(observation, newestPath);
					DebugDraw.drawAction(observation, action);
				}
			}
		}
		tickCount++;
		
		return action;
	}
	
	private List<DirectedEdge> getPath(Environment observation) {
		long startTime = System.currentTimeMillis();
		List<DirectedEdge> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), marioController.getXVelocity());
		
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
