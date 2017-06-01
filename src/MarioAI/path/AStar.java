package MarioAI.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

import MarioAI.Hasher;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;


public class AStar {
	private final HashMap<Long, SpeedNode> speedNodes = new HashMap<Long, SpeedNode>();
	
	// Set of nodes already explored
	private final HashSet<Integer> closedSet = new HashSet<Integer>();
	// Set of nodes yet to be explored
	private final PriorityQueue<SpeedNode> openSet = new PriorityQueue<SpeedNode>();
	private final Map<Integer, SpeedNode> openSetMap = new HashMap<Integer, SpeedNode>();
	
	private ArrayList<DirectedEdge> currentBestPath = null;
	private final int hashGranularity;
	private boolean keepRunning = false;
	private boolean foundBestPath = false;
	private final Object lockBestSpeedNode = new Object();
	
	public AStar(int hashGranularity) {
		this.hashGranularity = hashGranularity;
	}
	
	/**
	 * A* algorithm for multiple goal nodes (tries to find path to just one of them). Method to be used with the right most column of the screen
	 * 
	 * @param start
	 * @param rightmostNodes
	 * @return optimal path
	 */
	public void runMultiNodeAStar(final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight, World world) {
		
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
		
		final Node goal = new Node((short) goalX, (short) 2, (byte) 3);
		final DirectedEdge[] addedEdges = new DirectedEdge[rightmostNodes.length];
		for (int i = 0; i < rightmostNodes.length; i++) {
			final Node node = rightmostNodes[i];
			if (node != null) {
				RunningEdge edge = new RunningEdge(node, goal);
				node.addEdge(edge);
				addedEdges[i] = edge;
			}
		}
		
		// Remove auxiliary goal node and update nodes having it as a neighbor accordingly
		initAStar(new SpeedNode(start, marioSpeed, Long.MAX_VALUE), new SpeedNode(goal, 0, Long.MIN_VALUE), enemyPredictor, marioHeight, world);
		//speedNodes.remove(Long.MAX_VALUE);
		//speedNodes.remove(Long.MIN_VALUE);
		
		for (int i = 0; i < rightmostNodes.length; i++) {
			final Node node = rightmostNodes[i];
			if (node != null) {
				node.removeEdge(addedEdges[i]);
			}
		}
	}
	
	private void initAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor, int marioHeight, World world) {
		closedSet.clear();
		openSet.clear();
		openSetMap.clear();
		currentBestPath = null;
		
		keepRunning = true;
		foundBestPath = false;
		
		// Initialization
		openSet.add(start);
		openSetMap.put(Integer.MAX_VALUE, start);
		start.gScore = 0;
		start.fScore = heuristicFunction(start, goal);
		
		runAStar(start, goal, enemyPredictor, marioHeight, world);
	}

	/**
	 * Basic A* search algorithm
	 * 
	 * @param start
	 * @param goal
	 * @return
	 */
	public void runAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor, int marioHeight, World world) {		
		while (!openSet.isEmpty() && keepRunning) {
			//System.out.println("Current open set:");
			//System.out.println(openSet);
			
			synchronized (lockBestSpeedNode) {
				final SpeedNode current = openSet.remove();
				openSetMap.remove(current.hash);
				
				// If goal is reached return solution path.
				if (current.node.equals(goal.node)) {
					currentBestPath = reconstructPath(current);
					foundBestPath = true;
					break;
				}
				//System.out.println("Current node:");
				//System.out.println(current.node + "\nSpeed: " + current.vx + "\nFrom: " + current.ancestorEdge);
				//System.out.println("Current node edges:");
				//System.out.println(current.node.edges + "\n");
				// Current node has been explored.
				final int endHash = Hasher.hashEndSpeedNode(current, hashGranularity);
				closedSet.add(endHash);
				//System.out.println(openSet.size()); //Used to check how AStar performs.
				
				// Explore each neighbor of current node
				for (DirectedEdge neighborEdge : current.node.getEdges()) {			
					final SpeedNode sn = getSpeedNode(neighborEdge, current);
					
					if (!sn.isSpeedNodeUseable()) {
						continue;
					}
					
					if (sn.getMoveInfo().hasCollisions(current, world)) {
						continue;
					}
					
					if (sn.doesMovementCollideWithEnemy(current.gScore, enemyPredictor, marioHeight)) {
						continue;
					}
					
					//If a similar enough node has already been run through
					//no need to add this one at that point
					final int snEndHash = Hasher.hashEndSpeedNode(sn, hashGranularity);
					if (closedSet.contains(snEndHash)) {
						continue;
					}
					
					// Distance from start to neighbor of current node
					final int tentativeGScore = current.gScore + sn.getMoveTime();
					
					//If a similar enough node exists and that has a better g score
					//then there is no need to add this edge as it's worse than the
					//current one
					if (openSetMap.containsKey(snEndHash) &&
						tentativeGScore >= openSetMap.get(snEndHash).gScore) {
						continue;
					}  
					
					//Update the edges position in the priority queue
					//by updating the scores and taking it in and out of the queue.
					openSet.remove(sn);
					sn.gScore = tentativeGScore;
					sn.fScore = sn.gScore + heuristicFunction(sn, goal) + neighborEdge.getWeight();
					sn.parent = current;
					openSet.add(sn);
					openSetMap.put(snEndHash, sn);
				}	
			}
		}
		currentBestPath = null;
		foundBestPath = false;
	}
	
	public SpeedNode getSpeedNode(DirectedEdge neighborEdge, SpeedNode current) {
		final long hash = Hasher.hashSpeedNode(current.vx, neighborEdge, hashGranularity);
		
		final SpeedNode speedNode = speedNodes.get(hash);
		if (speedNode != null) {
			//return speedNode; //TODO temp for testing purposes
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
		if (currentSpeedNode != null) {
			while (currentSpeedNode.parent != null) {
				currentSpeedNode.use();
				path.add(currentSpeedNode.ancestorEdge);
				//if (currentSpeedNode.parent.parent == null) {
				//	System.out.println("First transition speed: " + currentSpeedNode.vx);				
				//}
				currentSpeedNode = currentSpeedNode.parent;
			}
			Collections.reverse(path);
			
			if (path.size() > 0) {
				path.remove((path.size() - 1));
			}
			return path;	
		} 
		else {
			return path;
		}
	}
	
	public AStarPath getCurrentBestPath() {
		//lock out here because the lock has to surround foundBestPath aswell
		//because that can also change
		synchronized (lockBestSpeedNode) {
			if (foundBestPath) {
				return new AStarPath(currentBestPath, true, hashGranularity);
			}
			else {
			return new AStarPath(reconstructPath(openSet.peek()), false, hashGranularity);
			}
		}
	}
	
	public HashMap<Long, SpeedNode> getSpeedNodes() {
		return speedNodes;
	}
	
	public void stop() {
		keepRunning = false;
	}
}