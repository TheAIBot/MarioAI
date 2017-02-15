package MarioAI;

import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 * Main program agent. Is to run A* algorithm.
 */
public class FastAndFurious implements Agent {

	private static final String name = "THE ULTIME AND SUPREME OVERLORD THAT BRINGS DEATH AND DESTRUCTION";
	private final Graph graph = new Graph();
	private boolean firstTick = true;
	private boolean secondTick = false;
	private boolean[] action = new boolean[Environment.numberOfButtons]; 

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {

		if (firstTick) {
			firstTick = false;
			secondTick = true;
		} else {
			if (secondTick) {
				graph.createStartGraph(observation);
				secondTick = false;
			} else {
				if (graph.updateMatrix(observation)) {
					List<Node> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
					action = AStar.getNextMove(graph, path);
				}
			}

		}

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
