package MarioAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import MarioAI.enemy.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;


public final class AStar {
	private final HashMap<Long, SpeedNode> speedNodes = new HashMap<Long, SpeedNode>();
	
	
	/**
	 * A* algorithm for multiple goal nodes (tries to find path to just one of them). Method to be used with the right most column of the screen
	 * 
	 * @param start
	 * @param rightmostNodes
	 * @return optimal path
	 */
	public ArrayList<DirectedEdge> runMultiNodeAStar(final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight) {
		
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
		goalX += 50;
		
		Node goal = new Node((short) goalX, (short) 2, (byte) 3);
		for (Node node : rightmostNodes) {
			if (node != null) {
				node.addEdge(new Running(node, goal));
			}
		}

		// Remove auxiliary goal node and update nodes having it as a neighbor accordingly
		final ArrayList<DirectedEdge> path = runAStar(new SpeedNode(start, Long.MAX_VALUE), 
										   			  new SpeedNode(goal, Long.MIN_VALUE),
										   			  enemyPredictor, 
										   			  marioHeight);
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
	public ArrayList<DirectedEdge> runAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor, int marioHeight) {
		// Set of nodes already explored
		final Map<Long, SpeedNode> closedSetMap = new HashMap<Long, SpeedNode>();
		// Set of nodes yet to be explored
		final PriorityQueue<SpeedNode> openSet = new PriorityQueue<SpeedNode>();
		final Map<Long, SpeedNode> openSetMap = new HashMap<Long, SpeedNode>();

		// Initialization
		openSet.add(start);
		openSetMap.put(start.hash, start);
		start.gScore = 0;
		//start.node.fScore = heuristicFunction(start.node, goal.node);
		start.fScore = heuristicFunction(start, goal);
		
		while (!openSet.isEmpty()) {
			final SpeedNode current = openSet.remove();
			openSetMap.remove(current.hash);
						
			// If goal is reached return solution path.
			if (current.node.equals(goal.node)) {
				return reconstructPath(current);
			}

			System.out.println("Current node:");
			System.out.println(current.node + "\nSpeed: " + current.vx + "\nFrom: " + current.ancestorEdge);
			System.out.println("Current node edges:");
			System.out.println(current.node.edges + "\n");
			
			
			// Current node has been explored.
			closedSetMap.put(current.hash, current);
			//System.out.println(openSet.size()); //Used to check how AStar performs.
			
			// Explore each neighbor of current node
			for (DirectedEdge neighborEdge : current.node.getEdges()) {
				
				
				System.out.println("Current edge: ");
				System.out.println(neighborEdge + "\n");
				
				final SpeedNode sn = getSpeedNode(neighborEdge, current);
				
				if (!sn.isSpeedNodeUseable()) {
					continue;
				}
				
				
				if (sn.doesMovementCollideWithEnemy(current.gScore, enemyPredictor, marioHeight)) {
					continue;
				}
				
				//If a similar enough node has already been run through
				//no need to add this one at that point
				if (closedSetMap.containsKey(sn.hash)) {
					continue;
				}
				
				// Distance from start to neighbor of current node
				final int tentativeGScore = current.gScore + sn.getMoveTime();
				
				//If a similar enough node exists and that has a better g score
				//then there is no need to add this edge as it's worse than the
				//current one
				if (openSetMap.containsKey(sn.hash) &&
					tentativeGScore >= openSetMap.get(sn.hash).gScore) {
					continue;
				} else {
					//Update the edges position in the priority queue
					//by updating the scores and taking it in and out of the queue.
					openSet.remove(sn);
					sn.gScore = tentativeGScore;
					sn.fScore = sn.gScore + heuristicFunction(sn, goal) + neighborEdge.getWeight();
					openSet.add(sn);
					openSetMap.put(sn.hash, sn);
				}				
			}
		}
		// No solution was found
		return null;
	}
	
	public SpeedNode getSpeedNode(DirectedEdge neighborEdge, SpeedNode current) {
		final long hash = Hasher.hashSpeedNode(current.vx, neighborEdge);
		
		final SpeedNode speedNode = speedNodes.get(hash);
		if (speedNode != null) {
			return speedNode;
		}
		
		final SpeedNode newSpeedNode = new SpeedNode(neighborEdge.target, current, neighborEdge, hash);
		speedNodes.put(hash, newSpeedNode);
		return newSpeedNode;
	}

	/**
	 * @param current
	 * @param goal
	 * @return
	 */
	public int heuristicFunction(final SpeedNode current, final SpeedNode goal) {
		return MarioControls.getTicksToTarget(goal.node.x - current.xPos, current.vx);
	}

	/**
	 * @param current
	 * @return path
	 */
	private ArrayList<DirectedEdge> reconstructPath(SpeedNode currentSpeedNode) {
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		while (currentSpeedNode.parent != null) {
			currentSpeedNode.use();
			path.add(currentSpeedNode.ancestorEdge);
			currentSpeedNode = currentSpeedNode.parent;
		}
		Collections.reverse(path);
		return path;
	}
}