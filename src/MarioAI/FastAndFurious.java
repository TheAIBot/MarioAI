package MarioAI;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.sun.istack.internal.FinalArrayList;
import com.sun.org.apache.bcel.internal.generic.RETURN;

import MarioAI.debugGraphics.DebugDraw;
import MarioAI.graph.DirectedEdge;
import MarioAI.graph.Graph;
import MarioAI.graph.GraphMath;
import MarioAI.graph.Grapher;
import MarioAI.graph.Node;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Main program agent.
 */
public class FastAndFurious implements Agent {

	private static final String name = "THE ULTIME AND SUPREME OVERLORD THAT BRINGS DEATH AND DESTRUCTION";
	private final Graph graph = new Graph();
	private int tickCount = 0;
	private List<DirectedEdge> newestPath = null;
	
	private static final boolean DEBUG = true;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		boolean[] action = new boolean[Environment.numberOfButtons];
		if (tickCount == 30) {
			graph.createStartGraph(observation);
			Grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
			List<DirectedEdge> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
			if (path != null) {
				newestPath = path;
			}
			
		} else if (tickCount > 30) {
			if (graph.updateMatrix(observation)) {
				Grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
			}
			if (DEBUG) {
				DebugDraw.resetGraphics(observation);
				DebugDraw.drawEndNodes(observation, graph);
				DebugDraw.drawBlockBeneathMarioNeighbors(observation, graph);
				DebugDraw.drawNeighborPaths(observation, graph);
				DebugDraw.drawReachableNodes(observation, graph);
				DebugDraw.drawPathOptionNodes(observation, graph);
			}
		}
					
		if (newestPath != null && newestPath.size() > 1) {
			if (MarioControls.reachedNextNode(observation, newestPath)) {
				List<DirectedEdge> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
				if (path != null) {
					newestPath = path;
				}
			}
			MarioControls.getNextAction(observation, newestPath, action);
			if (DEBUG) DebugDraw.drawPath(observation, newestPath);
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
