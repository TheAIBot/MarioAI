package MarioAI;

import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayList;

public class AStar {
	
	private static List<Node> closedSet = new ArrayList<Node>();
	private static PriorityQueue<Node> openSet = new PriorityQueue<Node>();
	
	private static int LARGE_NUMBER = 1000;
	
	public static ArrayList<Node> AStar(Node start, Node goal) {
		initAStar(start, goal);
		
		while (!openSet.isEmpty()) {
			Node current = openSet.remove();
			if (current.equals(goal)) {
				return reconstructPath(current.parent, current);
			}
			openSet.remove(current);
			closedSet.add(current);
			
			List<Node> neighbors = current.getNeighbors();
			for (int i = 0; i < neighbors.size(); i++) {
				Node neighbor = neighbors.get(i);
				if (closedSet.contains(neighbor)) continue;
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

	private static void initAStar(Node start, Node goal) {
		openSet.add(start);	
		start.gScore = 0;
		start.fScore = heuristicFunction(start, goal);
	}
	
	public static int heuristicFunction(Node start, Node goal) {
		//temp use distance (later should use time)
		int dist = (int) Math.sqrt(Math.pow((goal.x - start.x),2) + Math.pow((goal.y - start.y),2));
		return dist;
	}
	
	private static ArrayList<Node> reconstructPath(Node parent, Node current) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Distance between two nodes.
	 * We hardcode this to 1 for the moement.
	 * @param current
	 * @param neighbor
	 * @return
	 */
	private static int distanceBetween(Node current, Node neighbor) {
		// TODO Auto-generated method stub
		return 5;
	}
	
}
