package MarioAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import MarioAI.graph.GraphMath;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;

//TODO
//-Calculate time over edge
//-Change heuristic
//-Generate SpeedNodes based on current velocity and possible changes in velocity
//-finish hash of speed nodes - DONE


//Fix bug moving left by maintaining Mario velocity. Problem starting a star velocity 0 meaning polynomial get 9000 score (!).  

public final class AStar {

	/**
	 * A* algorithm for multiple goal nodes (tries to find path to just one of them). Method to be used with the right most column of the screen
	 * 
	 * @param start
	 * @param rightmostNodes
	 * @return optimal path
	 */
	public static List<DirectedEdge> runMultiNodeAStar(final Node start, final Node[] rightmostNodes) {
		// Add singleton goal node far to the right. This will ensure each
		// vertical distance is minimal and all nodes in rightmost column will be
		// pretty good goal positions to end up in after A* search 
		Node goal = new Node((short) (start.x + 50), (short) 11, (byte) 3);
		for (Node node : rightmostNodes) {
			if (node != null) {
				node.addEdge(new Running(node, goal));
			}
		}

		// Remove auxiliary goal node and update nodes having it as a neighbor accordingly
		List<DirectedEdge> path = runAStar(new SpeedNode(start, MarioControls.getXVelocity(), null, null), new SpeedNode(goal, 0, null, null));
		if (path != null && path.size() > 0) { //TODO remove when error is fixed
			path.remove((path.size() - 1));
		}

		for (Node node : rightmostNodes) {
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
		openSetMap.put(start.hashCode(), start);
		start.gScore = 0;
		//start.node.fScore = heuristicFunction(start.node, goal.node);
		start.fScore = heuristicFunction(start, goal);

		while (!openSet.isEmpty()) {
			SpeedNode current = openSet.remove();
			openSetMap.remove(current.hashCode());
			if (current.vx < 0) {
				System.out.println(current.fScore);
			}
			System.out.println(current.vx);

			// If goal is reached return solution path
			if (current.node.equals(goal.node)) {
				return reconstructPath(current);
			}

			// Current node has been explored
			closedSetMap.put(current.hashCode(), current);

			// Explore each neighbor of current node
			final List<DirectedEdge> neighborEdges = current.node.getEdges();
			for (DirectedEdge neighborEdge : neighborEdges) {
				MovementInformation movementInformation = MarioControls.getStepsAndSpeedAfterJump(neighborEdge, current.vx);
				SpeedNode sn = new SpeedNode(neighborEdge.target, movementInformation.getEndSpeed(), current,neighborEdge);
				if (closedSetMap.containsKey(sn.hashCode()))
					continue;
				// Distance from start to neighbor of current node
				float tentativeGScore = current.gScore + movementInformation.getMoveTime();// + neighborEdge.getWeight();
				if (!openSetMap.containsKey(sn.hashCode())) {
					sn.parent = current;
					sn.gScore = tentativeGScore;
					sn.fScore = sn.gScore + heuristicFunction(sn, goal);
					openSet.add(sn);
				} else if (tentativeGScore >= openSetMap.get(sn.hashCode()).gScore) {
					continue;
				} else {
					// Update values
					openSet.remove(sn);
					sn.parent = current;
					sn.gScore = tentativeGScore;
					sn.fScore = sn.gScore + heuristicFunction(sn, goal);
					sn.ancestorEdge = neighborEdge;
					openSet.add(sn);
				}
			}
		}

		//TODO look at this and decide if is should be changed or removed
		Iterator<SpeedNode> iter = closedSetMap.values().iterator();
		while (iter.hasNext()) {
			SpeedNode node = iter.next();
			if (node == null) continue;
			node.gScore = 0;
			node.fScore = 0;
			node.parent = null;
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
	 * @param current
	 * @param goal
	 * @return
	 */
	public static float heuristicFunction(final SpeedNode current, final SpeedNode goal) {
		//return MarioControls.getXMovementTime(goal.node.x - start.node.x); //pending correct funtinoality
		//if (current.vx == 0) return 1000000f;
		return timeToReachNode(goal, current);
	}

	/**
	 * @param current
	 * @return path
	 */
	private static List<DirectedEdge> reconstructPath(SpeedNode currentSpeedNode) {
		final List<DirectedEdge> path = new ArrayList<DirectedEdge>();
		while (currentSpeedNode.parent != null) {
			DirectedEdge fisk = null;
			for (int i = 0; i < currentSpeedNode.parent.node.edges.size(); i++) {
				if (currentSpeedNode.parent.node.edges.get(i).target.equals(currentSpeedNode.node)) {
					fisk = currentSpeedNode.parent.node.edges.get(i);
					break;
				}
			}
			path.add(fisk);
			currentSpeedNode = currentSpeedNode.parent;
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * Using derived formula for time(v0,dist) from Maple (v2.5)
	 * @param n1
	 * @param n2
	 * @return time to reach n2 from n1
	 */
	public static float timeToReachNode(SpeedNode n1, SpeedNode n2) {
		float v0 = n1.vx;
		if (v0 < 0) v0 = 0;
		
		float dist = Math.abs(n2.node.x - n1.node.x); //guard - should only be able to be positive in the first place
		float d0 = Math.min(dist,5); //float d0 = dist <= 5 ? dist : 5f;
		
		float timeToReachDistanceUnder5Blocks = (float) (0.4734168362e1 - 0.2033398373e2 * v0 + 0.3650816449e1 * d0 - 0.1899556093e0 * Math.pow(d0 - 0.2e1, 0.2e1)
				- 0.2561502720e1 * (d0 - 0.2e1) * (v0 - 0.15e0) + 0.2015956084e2 * Math.pow(v0 - 0.15e0, 0.2e1)
				+ 0.4670562450e-1 * Math.pow(d0 - 0.2e1, 0.3e1) + 0.9447200685e0 * Math.pow(d0 - 0.2e1, 0.2e1) * (v0 - 0.15e0)
				- 0.8877386747e1 * (d0 - 0.2e1) * Math.pow(v0 - 0.15e0, 0.2e1) + 0.1206780712e2 * Math.pow(v0 - 0.15e0, 0.3e1));
		
		if (dist <= 5) return timeToReachDistanceUnder5Blocks;
		return timeToReachDistanceUnder5Blocks + (dist - 5f) / MarioControls.getMaxV(); 
	}

}
