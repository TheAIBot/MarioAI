package MarioAI;

import java.util.List;
import java.util.PriorityQueue;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import java.util.ArrayList;
import java.util.Collections;

public final class AStar {

	/**
	 * A* algorithm for multiple goal nodes (tries to find path to just one of them). Method to be used with the right most
	 * column of the screen
	 * 
	 * @param start
	 * @param nodes
	 * @return optimal path
	 */
	public static List<Node> runMultiNodeAStar(final Node start, final Node[] nodes)  {
		// Add singleton goal node far to the right. This will ensure each
		// vertical distance is minimal and all nodes in rightmost column will be
		// pretty good goal positions to end up in after A* search
		Node goal = new Node((short) 1000, (short) 11, (byte) 3);
		for (Node node : nodes) {
			if (node != null) {
				node.addEdge(new DirectedEdge(node, goal, null));
			}
		}

		// Remove auxiliary goal node and update nodes having it as a neighbor accordingly
		List<Node> path = runAStar(start, goal);
		if (path != null) {
			path.remove((path.size() - 1));
		}
		for (Node node : nodes) {
			if (node != null) {
				node.removeEdge(new DirectedEdge(node, goal, null));
			}
		}

		return path;
	}

	/**
	 * Basic A* search algorithm
	 * 
	 * @param start
	 * @param goal
	 * @return
	 */
	public static List<Node> runAStar(final Node start, final Node goal) {
		// Set of nodes already explored
		final List<Node> closedSet = new ArrayList<Node>();
		// Set of nodes yet to be explored
		final PriorityQueue<Node> openSet = new PriorityQueue<Node>();

		// Initialization
		openSet.add(start);
		start.gScore = 0;
		start.fScore = heuristicFunction(start, goal);

		while (!openSet.isEmpty()) {
			Node current = openSet.remove();

			// If goal is reached return solution path
			if (current.equals(goal)) {
				return reconstructPath(current);
			}

			// Current node has been explored
			// openSet.remove(current);
			closedSet.add(current);

			// Explore each neighbor of current node
			final List<DirectedEdge> neighborEdges = current.getEdges();
			for (DirectedEdge neighborEdge : neighborEdges) {
				if (closedSet.contains(neighborEdge.target))
					continue;
				// Distance from start to neighbor of current node
				float tentativeGScore = current.gScore + GraphMath.distanceBetween(current, neighborEdge.target);
				if (!openSet.contains(neighborEdge.target)) {
					openSet.add(neighborEdge.target);
				} else if (tentativeGScore >= neighborEdge.target.gScore) {
					continue;
				}

				// Update values
				neighborEdge.target.parent = current;
				neighborEdge.target.gScore = tentativeGScore;
				neighborEdge.target.fScore = neighborEdge.target.gScore + heuristicFunction(neighborEdge.target, goal);
			}
		}
		for (Node node : closedSet) {
			node.gScore = 0;
			node.fScore = 0;
			node.parent = null;
		}
		// No solution was found
		return null;
	}

	/**
	 * @param start
	 * @param goal
	 * @return the estimated cost of the cheapest path from current node to goal node
	 */
	public static float heuristicFunction(final Node node, final Node goal) {
		// temp use distance (later should use time)
		return GraphMath.distanceBetween(node, goal);
	}

	/**
	 * @param current
	 * @return path
	 */
	private static List<Node> reconstructPath(Node current) {
		final List<Node> path = new ArrayList<Node>();
		while (current.parent != null) {
			path.add(current);
			current = current.parent;
		}
		Collections.reverse(path);
		return path;
	}
}
