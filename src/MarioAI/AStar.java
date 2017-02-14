package MarioAI;

import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayList;

public class AStar {
	
	// Set of nodes already explored
	private static List<Node> closedSet = new ArrayList<Node>();
	// Set of nodes yet to be explored
	private static PriorityQueue<Node> openSet = new PriorityQueue<Node>();
	
	public static List<Node> MultiNodeAStar(Node start, Node[] nodes) {
		Node goal = new Node((short) 100, (short) 11);
		for (Node node : nodes) {
			node.neighbors.add(goal);
		}
		
		return AStar(start, goal);
	}
	
	public static List<Node> AStar(Node start, Node goal) {
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
			
			openSet.remove(current);
			closedSet.add(current);
			
			//
			List<Node> neighbors = current.getNeighbors();
			for (Node neighbor : neighbors) {
				if (closedSet.contains(neighbor)) continue;
				
				// Distance from start to neighbor of current node
				int tentativeGScore = current.gScore + distanceBetween(current, neighbor);
				if (!openSet.contains(neighbor)) {
					openSet.add(neighbor);
				} else if (tentativeGScore >= neighbor.gScore) {
					continue;
				}
				
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
	public static int heuristicFunction(Node node, Node goal) {
		//temp use distance (later should use time)
		int dist = (int) Math.sqrt(Math.pow((goal.x - node.x),2) + Math.pow((goal.y - node.y),2));
		return dist;
	}
	
	private static List<Node> reconstructPath(Node current) {
		List<Node> path = new ArrayList<Node>();
		while (current.parent != null) {
			path.add(current);
			current = current.parent;
		}
		return path;
	}
	
	/**
	 * Distance between two nodes.
	 * We hardcode this to 1 for the moement.
	 * @param current
	 * @param neighbor
	 * @return
	 */
	private static int distanceBetween(Node current, Node neighbor) {
		return 1;
	}
	
}
