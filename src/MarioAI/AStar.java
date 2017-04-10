package MarioAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import MarioAI.enemy.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
//public boolean hasEnemy(int x, int y, int time);
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;


//Fix bug moving left by maintaining Mario velocity. Problem starting a star velocity 0 meaning polynomial get 9000 score (!).  

public final class AStar {
	/**
	 * A* algorithm for multiple goal nodes (tries to find path to just one of them). Method to be used with the right most column of the screen
	 * 
	 * @param start
	 * @param rightmostNodes
	 * @return optimal path
	 */
	public static List<DirectedEdge> runMultiNodeAStar(final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor) {
		// Add singleton goal node far to the right. This will ensure each
		// vertical distance is minimal and all nodes in rightmost column will be
		// pretty good goal positions to end up in after A* search 
		int goalX = 0;
		for (int i = 0; i < rightmostNodes.length; i++) {
			if (rightmostNodes[i] != null) {
				goalX = rightmostNodes[i].x;
				break;
			}
		}
		
		
		Node goal = new Node((short) goalX, (short) 2, (byte) 3);
		for (Node node : rightmostNodes) {
			if (node != null) {
				node.addEdge(new Running(node, goal));
			}
		}

		// Remove auxiliary goal node and update nodes having it as a neighbor accordingly
		List<DirectedEdge> path = runAStar(new SpeedNode(start, marioSpeed, null, null, start.x), 
										   new SpeedNode(goal, 0, null, null, goal.x), enemyPredictor);
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
	public static List<DirectedEdge> runAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor) {
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
				
				
				final MovementInformation movementInformation = MarioControls.getMovementInformationFromEdge(current.correctXPos, current.node.y, neighborEdge.target, neighborEdge, current.vx);
				
				final float correctXPos = current.correctXPos + movementInformation.getXMovementDistance();
				
				//in a jump it's possible to jump too far
				//and there is nothing that mario can do about it
				//this should maybe be removed in the future
				if (!MarioControls.canMarioUseJumpEdge(neighborEdge, correctXPos)) {
					continue;
				}
				
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
		return MarioControls.getXMovementTime(goal.node.x - current.correctXPos, current.vx, 0).ticks;
	}

	/**
	 * @param current
	 * @return path
	 */
	private static List<DirectedEdge> reconstructPath(SpeedNode currentSpeedNode) {
		final List<DirectedEdge> path = new ArrayList<DirectedEdge>();
		while (currentSpeedNode.parent != null) {
			path.add(currentSpeedNode.ancestorEdge);
			currentSpeedNode = currentSpeedNode.parent;
		}
		Collections.reverse(path);
		return path;
	}
}
