package MarioAI;

import java.util.List;
import java.util.PriorityQueue;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import java.util.ArrayList;
import java.util.Collections;

public final class AStar {

	/**
	 * A* algorithm for multiple goal nodes (tries to find path to just one of
	 * them). Method to be used with the right most column of the screen
	 * 
	 * @param start
	 * @param nodes
	 * @return optimal path
	 */
	public static List<Node> runMultiNodeAStar(final Node start, final Node[] nodes) {
		// Add singleton goal node far to the right. This will ensure each
		// vertical distance is minimal and all nodes in rightmost column will
		// be
		// pretty good goal position to end up in after A* search
		Node goal = new Node((short) 1000, (short) 11, (byte) 3);
		for (Node node : nodes) {
			if (node != null) {
				node.neighbors.add(goal);
			}
		}

		// Remove auxiliary goal node
		List<Node> path = runAStar(start, goal);
		if (path != null) {
			path.remove((path.size() - 1));
		}

		for (Node node : nodes) {
			if (node != null) {
				node.neighbors.remove(goal);
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
			final List<Node> neighbors = current.getNeighbors();
			for (Node neighbor : neighbors) {
				if (closedSet.contains(neighbor))
					continue;

				// Distance from start to neighbor of current node
				float tentativeGScore = current.gScore + GraphMath.distanceBetween(current, neighbor);
				if (!openSet.contains(neighbor)) {
					openSet.add(neighbor);
				} else if (tentativeGScore >= neighbor.gScore) {
					continue;
				}

				// Update values
				neighbor.parent = current;
				neighbor.gScore = tentativeGScore;
				neighbor.fScore = neighbor.gScore + heuristicFunction(neighbor, goal);
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
	 * @return the estimated cost of the cheapest path from current node to goal
	 *         node
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
