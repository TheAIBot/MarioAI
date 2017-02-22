package MarioAI;

import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Main program agent. Is to run A* algorithm.
 */
public class FastAndFurious implements Agent {

	private static final String name = "THE ULTIME AND SUPREME OVERLORD THAT BRINGS DEATH AND DESTRUCTION";
	private final Graph graph = new Graph();
	private boolean[] action = new boolean[Environment.numberOfButtons]; 
	private int tickCount = 0;
	private List<Node> newestPath = null;

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
			Grapher.graph(graph.getLevelMatrix(), graph.getMarioNode(observation));
			//do edge creation first and then astar
			//AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
			List<Node> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
			if (path != null) {
				newestPath = path;
			}
			
		} else if (tickCount > 30) {
			if (graph.updateMatrix(observation)) {
				Grapher.graph(graph.getLevelMatrix(), graph.getMarioNode(observation));
				//do edge creation first and then astar
				List<Node> path = AStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes());
				if (path != null) {
					newestPath = path;
				}
			}
		}
		if (newestPath != null &&
			newestPath.size() >= 1 &&
			GraphMath.distanceBetween(graph.getMarioNode(observation), newestPath.get(0)) <= 1) {
			newestPath.remove(0);
		}
		if (newestPath != null && newestPath.size() > 0) {
			action = AStar.getNextMove(MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()), 
					MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()), 
					newestPath, 
					observation.mayMarioJump());
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
