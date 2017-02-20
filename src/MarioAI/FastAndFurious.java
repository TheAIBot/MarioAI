package MarioAI;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 * Main program agent. Is to run A* algorithm.
 */
public class FastAndFurious implements Agent {

	private static final String name = "THE ULTIME AND SUPREME OVERLORD THAT BRINGS DEATH AND DESTRUCTION";
	private final Graph graph = new Graph();
	private int tickCount = 0;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {

        byte[][] level = observation.getCompleteObservation();
        for (int x = 0; x < level.length; x++) {
			for (int y = 0; y < level[0].length; y++) {
				System.out.print((char)(level[x][y] + 50));
			}
			System.out.println("");
		}
        System.out.println();
		
		if (tickCount == 30) {
			graph.createStartGraph(observation);
			//do edge creation first and then astar
			//AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
		} else if (tickCount > 30) {
			if (graph.updateMatrix(observation)) {
				//do edge creation first and then astar
				//AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
			}
		}
		tickCount++;
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
