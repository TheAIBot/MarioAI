package MarioAI;

import java.util.List;
import java.util.PriorityQueue;

import MarioAI.graph.DirectedEdge;
import MarioAI.graph.GraphMath;
import MarioAI.graph.Node;
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
	public static List<DirectedEdge> runMultiNodeAStar(final Node start, final Node[] nodes)  {
		// Add singleton goal node far to the right. This will ensure each
		// vertical distance is minimal and all nodes in rightmost column will be
		// pretty good goal positions to end up in after A* search
		Node goal = new Node((short) 1000, (short) 11, (byte) 3);
		for (Node node : nodes) {
			if (node != null) {
				node.addEdge(new Running(node, goal));
			}
		}

		// Remove auxiliary goal node and update nodes having it as a neighbor accordingly
		List<DirectedEdge> path = runAStar(start, goal);
		if (path != null) {
			path.remove((path.size() - 1));
		}
		for (Node node : nodes) {
			if (node != null) {
				//remove the last edge as that's the egde to the goal
				//because it was the soonest added edge
				node.removeEdge(node.edges.get(node.edges.size() - 1));
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
	public static List<DirectedEdge> runAStar(final Node start, final Node goal) {
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
				float tentativeGScore = current.gScore + neighborEdge.weight;
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
	private static List<DirectedEdge> reconstructPath(Node current) {
		final List<DirectedEdge> path = new ArrayList<DirectedEdge>();
		while (current.parent != null) {
			DirectedEdge fisk = null;
			for (int i = 0; i < current.parent.edges.size(); i++) {
				if (current.parent.edges.get(i).target.equals(current)) {
					fisk = current.parent.edges.get(i);
					break;
				}
			}
			path.add(fisk);
			current = current.parent;
		}
		Collections.reverse(path);
		return path;
	}
}
