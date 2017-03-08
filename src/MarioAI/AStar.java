package MarioAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import MarioAI.graph.DirectedEdge;
import MarioAI.graph.GraphMath;
import MarioAI.graph.Node;
import MarioAI.graph.SpeedNode;

//TODO
//-Calculate time over edge
//-Change heuristic
//-Generate SpeedNodes based on current velocity and possible changes in velocity
//-finish hash of speed nodes

public final class AStar {

	/**
	 * A* algorithm for multiple goal nodes (tries to find path to just one of them). Method to be used with the right most column of the screen
	 * 
	 * @param start
	 * @param nodes
	 * @return optimal path
	 */
	public static List<DirectedEdge> runMultiNodeAStar(final Node start, final Node[] nodes) {
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
		List<DirectedEdge> path = runAStar(new SpeedNode(start, 0, null), new SpeedNode(goal, 0, null));
		if (path != null) {
			path.remove((path.size() - 1));
		}

		for (Node node : nodes) {
			if (node != null) {
				//remove the last edge as that's the edge to the goal
				//because it was the first added edge
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
	public static List<DirectedEdge> runAStar(final SpeedNode start, final SpeedNode goal) {
		// Set of nodes already explored
		final Map<Integer, SpeedNode> closedSetMap = new HashMap<Integer, SpeedNode>();
		// Set of nodes yet to be explored
		final PriorityQueue<SpeedNode> openSet = new PriorityQueue<SpeedNode>();
		final Map<Integer, SpeedNode> openSetMap = new HashMap<Integer, SpeedNode>();

		// Initialization
		openSet.add(start);
		start.node.gScore = 0;
		//start.node.fScore = heuristicFunction(start.node, goal.node);
		start.node.fScore = heuristicFunction(start, goal);

		while (!openSet.isEmpty()) {
			SpeedNode current = openSet.remove();
			openSetMap.remove(current.hashCode());

			// If goal is reached return solution path
			if (current.node.equals(goal.node)) {
				return reconstructPath(current);
			}

			// Current node has been explored
			closedSetMap.put(current.hashCode(), current);

			// Explore each neighbor of current node
			final List<DirectedEdge> neighborEdges = current.node.getEdges();
			for (DirectedEdge neighborEdge : neighborEdges) {
				SpeedNode sn = new SpeedNode(neighborEdge.target, neighborEdge.getSpeedAfterTraversal(current.vx),
											 neighborEdge.source);
				if (closedSetMap.containsKey(sn.hashCode()))
					continue;
				// Distance from start to neighbor of current node
				float tentativeGScore = current.gScore + neighborEdge.getTraversedTime(current.vx);
				if (!openSetMap.containsKey(sn.hashCode())) {
					sn.parent = current.node;
					sn.gScore = tentativeGScore;
					sn.fScore = sn.gScore + heuristicFunction(sn, goal);
					openSet.add(sn);
				} else if (tentativeGScore >= openSetMap.get(sn.hashCode()).gScore) {
					continue;
				}
				
				// Update values
				openSet.remove(sn);
				sn.parent = current.node;
				sn.gScore = tentativeGScore;
				sn.fScore = sn.gScore + heuristicFunction(sn, goal);
				openSet.add(sn);
			}
		}

		//TODO look at this and decide if is should be changed or removed
		Iterator<SpeedNode> iter = closedSetMap.values().iterator();
		while (iter.hasNext()) {
			iter.next().node.gScore = 0;
			iter.next().node.fScore = 0;
			iter.next().node.parent = null;
		}

		// No solution was found
		return null;
	}

	/**
	 * @param start
	 * @param goal.node
	 * @return the estimated cost of the cheapest path from current node to goal node
	 */
//	public static float heuristicFunction(final Node start, final Node goal) {
//		// temp use distance (later should use time)
//		return GraphMath.distanceBetween(start, goal);
//	}

	/**
	 * TODO refactor proper integration with xvelocity
	 * 
	 * @param start
	 * @param goal
	 * @return
	 */
	public static float heuristicFunction(final SpeedNode start, final SpeedNode goal) {
		return MarioControls.getXMovementTime(goal.node.x - start.node.x);
	}

	/**
	 * @param current
	 * @return path
	 */
	private static List<DirectedEdge> reconstructPath(SpeedNode currentSpeedNode) {
		Node current = currentSpeedNode.node;
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
