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

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {

		if (firstTick) {
			graph.createStartGraph(observation);
			firstTick = false;
		} else {
			graph.updateMatrix(observation);
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
