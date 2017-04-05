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
	private int tickCount = 0;
	private List<DirectedEdge> newestPath = null;
	
	public boolean DEBUG = true;

	public void reset() {
		MarioControls.reset();
	}

	public boolean[] getAction(Environment observation) {
		boolean[] action = new boolean[Environment.numberOfButtons];

		if (tickCount == 30) {
			graph.createStartGraph(observation);
			Grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
			newestPath = getPath(observation);
			
		} else if (tickCount > 30) {
			if (graph.updateMatrix(observation)) {
				//graph.printMatrix(observation);
				Grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
			}
			
			if (DEBUG) {
				DebugDraw.resetGraphics(observation);
				DebugDraw.drawEndNodes(observation, graph.getGoalNodes(0));
				DebugDraw.drawBlockBeneathMarioNeighbors(observation, graph);
				DebugDraw.drawNeighborPaths(observation, graph);
				DebugDraw.drawReachableNodes(observation, graph);
				DebugDraw.drawPathOptionNodes(observation, graph);
			}
		}
		
		if (newestPath != null && newestPath.size() > 0) { //TODO Must also allowed to be 1, but adding this gives an error
			if (MarioControls.reachedNextNode(observation, newestPath) || 
				MarioControls.isPathInvalid(observation, newestPath)) {
				newestPath = getPath(observation);
			}
			MarioControls.getNextAction(observation, newestPath, action);

			if (DEBUG) {
				DebugDraw.drawPath(observation, newestPath);
				DebugDraw.drawPathEdgeTypes(observation, newestPath);
				DebugDraw.drawAction(observation, action);
			}
		}
		tickCount++;
		
		return action;
	}
	
	private List<DirectedEdge> getPath(Environment observation) {
		for (int i = 0; i < 11; i++) {
			List<DirectedEdge> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(i));
			if (path != null) {
				return  path;
			}					
		}
		return newestPath;
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
