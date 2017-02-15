package MarioAI;

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
					AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
				}
			}

		}

		return new boolean[Environment.numberOfButtons];
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
