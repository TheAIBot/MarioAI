package MarioAI;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import MarioAI.debugGraphics.DebugDraw;
import MarioAI.graph.Graph;
import MarioAI.graph.GraphMath;
import MarioAI.graph.Grapher;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.Node;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Main program agent.
 */
public class FastAndFurious implements Agent {

	private static final String name = "The painkiller";
	private final Graph graph = new Graph();
	private int tickCount = 0;
	private List<DirectedEdge> newestPath = null;
	
	private static final boolean DEBUG = true;

	public void reset() {
	}

	boolean updatedLastFrame = false;
	int updateCount  = 0;
	
	public boolean[] getAction(Environment observation) {
		boolean[] action = new boolean[Environment.numberOfButtons];

		if (updatedLastFrame) {
			System.out.println();
			updateCount++;
			updatedLastFrame = false;
		}
		if (tickCount == 30) {
			graph.createStartGraph(observation);
			updatedLastFrame = true;
			Grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
			List<DirectedEdge> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
			if (path != null) {
				newestPath = path;
			}
			
		} else if (tickCount > 30) {
			if (graph.updateMatrix(observation)) {
<<<<<<< HEAD
=======
				//graph.printMatrix(observation);
>>>>>>> refs/remotes/origin/AStar-Velocity
				graph.printMatrix(observation);
				updatedLastFrame = true;
				Grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
			}
			if (DEBUG) {
				DebugDraw.resetGraphics(observation);
				DebugDraw.drawEndNodes(observation, graph.getGoalNodes());
				DebugDraw.drawBlockBeneathMarioNeighbors(observation, graph);
				DebugDraw.drawNeighborPaths(observation, graph);
				DebugDraw.drawReachableNodes(observation, graph);
				DebugDraw.drawPathOptionNodes(observation, graph);
			}
		}
					
		if (newestPath != null && newestPath.size() > 1) {
			if (MarioControls.reachedNextNode(observation, newestPath) || MarioControls.isPathInvalid(observation, newestPath)) {
				List<DirectedEdge> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
				if (path != null) {
					newestPath = path;
				}
			}
			MarioControls.getNextAction(observation, newestPath, action);
			if (DEBUG) {
				DebugDraw.drawPath(observation, newestPath);
				DebugDraw.drawAction(observation, action);
			}
		}
		tickCount++;
		
		return action;
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
