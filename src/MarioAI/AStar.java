package MarioAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;


//Fix bug moving left by maintaining Mario velocity. Problem starting a star velocity 0 meaning polynomial get 9000 score (!).  

public final class AStar {
	private static int maxStateSpaceSize = 0;
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
		Node goal = new Node((short) (start.x + 50), (short) 2, (byte) 3);
		for (Node node : rightmostNodes) {
			if (node != null) {
				node.addEdge(new Running(node, goal));
			}
		}

		// Remove auxiliary goal node and update nodes having it as a neighbor accordingly
		List<DirectedEdge> path = runAStar(new SpeedNode(start, MarioControls.getXVelocity(), null, null, start.x), 
										   new SpeedNode(goal, 0, null, null, goal.x));
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
			final SpeedNode current = openSet.remove();
			openSetMap.remove(current.hashCode());
						
			// If goal is reached return solution path
			if (current.node.equals(goal.node)) {
				return reconstructPath(current);
			}

			maxStateSpaceSize = Math.max(maxStateSpaceSize, openSet.size());
			System.out.println(openSet.size() + ", max=" + maxStateSpaceSize);
			
			// Current node has been explored
			closedSetMap.put(current.hashCode(), current);
			//System.out.println(openSet.size());
			
			// Explore each neighbor of current node
			for (DirectedEdge neighborEdge : current.node.getEdges()) {
				//make sure the edge is possible to use
				//all Running edges are possible
				//not all jumps are possible
				if (!MarioControls.canMarioUseEdge(neighborEdge, current.correctXPos, current.vx)) {
					continue;
				}
				
				final MovementInformation movementInformation = MarioControls.getStepsAndSpeedAfterJump(neighborEdge, current.vx);
				final float correctXPos = current.correctXPos + movementInformation.getXMovementDistance();
				final SpeedNode sn = new SpeedNode(neighborEdge.target, movementInformation.getEndSpeed(), current, neighborEdge, correctXPos);

				//a similar enough node has already been run through
				//no need to add this one at that point
				if (closedSetMap.containsKey(sn.hashCode())) {
					continue;
				}
				
				// Distance from start to neighbor of current node
				final float tentativeGScore = current.gScore + movementInformation.getMoveTime();
				
				//is a similar enough node exists and that has a better g score
				//then there is no need to add this edge as it's worse than the
				//current one
				if (openSetMap.containsKey(sn.hashCode()) &&
					tentativeGScore >= openSetMap.get(sn.hashCode()).gScore) {
					continue;
				}
				
				//update the edges position in the priority queue
				//by updating the scores and taking it in and out of the queue.
				openSet.remove(sn);
				sn.gScore = tentativeGScore;
				sn.fScore = sn.gScore + heuristicFunction(sn, goal) + neighborEdge.getWeight();
				openSet.add(sn);
				openSetMap.put(sn.hashCode(), sn);
			}
		}
		// No solution was found
		return null;
	}

	/**
	 * @param current
	 * @param goal
	 * @return
	 */
	public static float heuristicFunction(final SpeedNode current, final SpeedNode goal) {
		//return MarioControls.getXMovementTime(goal.node.x - start.node.x); //pending correct funtinoality
		//if (current.vx == 0) return 1000000f;
		return MarioControls.getXMovementTime(goal.node.x - current.correctXPos, current.vx, 0).ticks;
		//return timeToReachNode(goal, current);
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
