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
	public static List<Node> runMultiNodeAStar(final Node start, final Node[] nodes) {
		// Add singleton goal node far to the right. This will ensure each
		// vertical distance is minimal and all nodes in rightmost column will be
		// pretty good goal position to end up in after A* search
		Node goal = new Node((short) 1000, (short) 11, (short) 0, (short) 0, (byte) 3);
		for (Node node : nodes) {
			if (node == null) continue;
			node.neighbors.add(goal);
		}

		// Remove auxiliary goal node
		List<Node> path = runAStar(start, goal);
		path.remove((path.size() - 1));

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
		
		if (goal == null) return null;

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
			//openSet.remove(current);
			closedSet.add(current);

			// Explore each neighbor of current node
			final List<Node> neighbors = current.getNeighbors();
			for (Node neighbor : neighbors) {
				if (closedSet.contains(neighbor)) continue;

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

	// TODO Pending implementation of functionality for getting info about movement between nodes in Graph.
	public static boolean[] getNextMove(final Graph graph, final List<Node> path) {
		final boolean[] action = new boolean[Environment.numberOfButtons];
		final Node start = path.get(0); //mario node
		final Node next = path.get(1);
		action[Mario.KEY_SPEED] = true;
		if (next.x > start.x) action[Mario.KEY_RIGHT] = true;
		if (next.x < start.x) action[Mario.KEY_LEFT] = true;
		if (next.y > start.y) action[Mario.KEY_JUMP] = true;
		return action;
	}
	
	/**
	 * Auxiliary method for creating actions (boolean array) in one line
	 * @param left
	 * @param right
	 * @param down
	 * @param jump
	 * @param speed
	 * @return action
	 */
	private boolean[] makeAction(boolean left, boolean right, boolean down, boolean jump, boolean speed) {
    	final boolean[] action = new boolean[5];
    	action[Mario.KEY_LEFT] = left;
    	action[Mario.KEY_RIGHT] = right;
    	action[Mario.KEY_DOWN] = down;
    	action[Mario.KEY_JUMP] = jump;
    	action[Mario.KEY_SPEED] = speed;
    	return action;
    }


}
