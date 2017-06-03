package MarioAI.path;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

import MarioAI.Hasher;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.AStarHelperEdge;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;

public class AStar {
	private final HashMap<Long, SpeedNode> speedNodes = new HashMap<Long, SpeedNode>();
	
	// Set of nodes already explored
	private final HashSet<Integer> closedSet = new HashSet<Integer>();
	// Set of nodes yet to be explored
	private final PriorityQueue<SpeedNode> openSet = new PriorityQueue<SpeedNode>();
	private final Map<Integer, SpeedNode> openSetMap = new HashMap<Integer, SpeedNode>();
	
	private SpeedNode currentBestPathEnd = null;
	public final int hashGranularity; // number of different speed values a speednode can have
	private boolean keepRunning = false;
	private boolean foundBestPath = false;
	private final Object lockBestSpeedNode = new Object();
	
	private static final int PENALTY_SCORE = 9000; // arbitrary high value;
	
	public AStar(int hashGranularity) {
		this.hashGranularity = hashGranularity;
	}
	
	/**
	 * @param start
	 * @param goal
	 * @param enemyPredictor
	 * @param marioHeight
	 * @param world
	 */
	public void initAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor, int marioHeight, World world) {
		closedSet.clear();
		openSet.clear();
		openSetMap.clear();
		currentBestPathEnd = null;
		
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
	 * Main part of the A* search algorithm. Adapted to fit project and problem specification.
	 * @param start
	 * @param goal
	 * @return
	 */
	private void runAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor, int marioHeight, World world) {		
		while (!openSet.isEmpty() && keepRunning) {
			//System.out.println("Current open set:");
			//System.out.println(openSet);
			
			synchronized (lockBestSpeedNode) {
				
				final SpeedNode current = openSet.remove();
				openSetMap.remove(current.hash);
				
				// If goal is reached return solution path.
				if (current.node.equals(goal.node)) {
					currentBestPathEnd = current;
					foundBestPath = true;
					return;
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
					final SpeedNode sn = getSpeedNode(neighborEdge, current, world);
					
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
					
					if (!sn.isSpeedNodeUseable()) {
						continue;
					}
					
					// collision detection and invincibility handling 
					int penalty = 0;
					if (!(sn.ancestorEdge instanceof AStarHelperEdge)) {
						if (sn.ticksOfInvincibility == 0) {
							if (sn.doesMovementCollideWithEnemy(current.gScore, enemyPredictor, marioHeight)) {
								if (sn.lives == 1) {
									continue; // if Mario would die if he hits an enemy this node can under no circumstances be used on a path
								}
								penalty = PENALTY_SCORE;
							}
						}
					}					
					
					//Update the edges position in the priority queue
					//by updating the scores and taking it in and out of the queue.
					if (openSetMap.containsKey(sn.hash)) openSet.remove(sn);
					sn.gScore = tentativeGScore;
					sn.fScore = sn.gScore + heuristicFunction(sn, goal) + neighborEdge.getWeight() + penalty;
					sn.parent = current;
					openSet.add(sn);
					openSetMap.put(snEndHash, sn);
				}	
			}
		}
		
		currentBestPathEnd = null;
		foundBestPath = false;
	}
	
	/**
	 * @param neighborEdge
	 * @param current
	 * @param world
	 * @return speedNode
	 */
	private SpeedNode getSpeedNode(DirectedEdge neighborEdge, SpeedNode current, World world) {
		final long hash = Hasher.hashSpeedNode(current.vx, neighborEdge, hashGranularity);
		
		final SpeedNode speedNode = speedNodes.get(hash);
		if (speedNode != null) {
			return speedNode; //TODO temp for testing purposes
		}
		
		final SpeedNode newSpeedNode = new SpeedNode(neighborEdge.target, current, neighborEdge, hash, world);
		speedNodes.put(hash, newSpeedNode);
		return newSpeedNode;
	}
	
	/**
	 * @param current
	 * @param goal
	 * @return an estimate of the ticks away from the goal
	 */
	private int heuristicFunction(final SpeedNode current, final SpeedNode goal) {
		return MarioControls.getTicksToTarget(goal.node.x - current.xPos, current.vx);
	}
	
	public AStarPath getCurrentBestPath() {
		//lock out here because the lock has to surround foundBestPath aswell
		//because that can also change
		synchronized (lockBestSpeedNode) {
			if (foundBestPath) {
				return new AStarPath(currentBestPathEnd, true, hashGranularity);
			}
			else {
				return new AStarPath(openSet.peek(), false, hashGranularity);
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