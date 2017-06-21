package MarioAI.path;

import java.util.PriorityQueue;

import MarioAI.Hasher;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.AStarHelperEdge;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * @author Emil
 * The main class for the A* search algorithm.
 */
public class AStar {
	// A hash map of speed nodes created since initialising the search
	private final Long2ObjectOpenHashMap<SpeedNode> speedNodes = new Long2ObjectOpenHashMap<SpeedNode>();
	private final LongOpenHashSet closedSet = new LongOpenHashSet();
	private final PriorityQueue<SpeedNode> openSet = new PriorityQueue<SpeedNode>();
	private final Long2ObjectOpenHashMap<SpeedNode> openSetMap = new Long2ObjectOpenHashMap<SpeedNode>();
	private SpeedNode currentBestPathEnd = null; // The current best node found by the search
	private boolean keepRunning = false;
	private boolean foundBestPath = false;
	public final int hashGranularity;
	
	//private static final int PENALTY_SCORE = 9001; // arbitrary high value; Used for penalizing hitting enemies
	private static final int PENALTY_LONG_EDGE = 15; // Long edges gives mario a tendency to jump into enemies.
	
	public static int timesAStarHasRun;
	public static int timesAStarDidNotFinish;
	public static long totalTimeUsedByAStar;
	public static int neighborsAsChildsCount;
	public static int neighborsAsParentsCount;

	public AStar(int hashGranularity) {
		this.hashGranularity = hashGranularity;
		resetStatistics();
	}
	
	/**
	 * Initializes the A* search by resetting the various fields such as the closed and open set,
	 * the current best node found and setting the f and g scores for the start node.
	 * Then starting the main search method. 
	 * @param start
	 * @param goal
	 * @param enemyPredictor
	 * @param marioHeight
	 * @param world
	 */
	public void initAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor, float marioHeight, World world) {
		closedSet.clear();
		openSet.clear();
		openSetMap.clear();
		currentBestPathEnd = null;
		
		keepRunning = true;
		foundBestPath = false;
		
		// Initialization
		openSet.add(start);
		openSetMap.put(Long.MAX_VALUE, start);
		start.gScore = 0;
		start.fScore = heuristicFunction(start, goal);
		
		timesAStarHasRun++;
		
		final long startTime = System.nanoTime();
		runAStar(start, goal, enemyPredictor, marioHeight, world);
		final long timeElapsed = System.nanoTime() - startTime;
		totalTimeUsedByAStar += timeElapsed;
	}

	/**
	 * Main part of the A* search algorithm. Adapted to fit project and problem specification.
	 * @param start
	 * @param goal
	 * @param enemyPredictor
	 * @param marioHeight
	 * @param world
	 */
	private void runAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor, float marioHeight, World world) {
		final long MAX_TIME_IN_ASTAR = 30; // max time in milliseconds allowed for A* to run 
		final long startMiliseconds = System.currentTimeMillis();
		final long stopTime = startMiliseconds + MAX_TIME_IN_ASTAR;
		while (!openSet.isEmpty() && keepRunning && stopTime >= System.currentTimeMillis()) {
			
			final SpeedNode current = openSet.remove();
			openSetMap.remove(current.hash);
			neighborsAsParentsCount++;
			
			// If goal is reached return solution path.
			if (current.node.equals(goal.node)) {
				currentBestPathEnd = current;
				foundBestPath = true;
				return;
			}
			// The current best speednode is the one furthest to the right
			// (disregarding if it passes through an enemy or not).
			//if ((currentBestPathEnd == null || current.currentXPos > currentBestPathEnd.currentXPos) && current != start) {
				currentBestPathEnd = current;
			//}
			
			// Current node has been explored.
			final long endHash = Hasher.hashEndSpeedNode(current, hashGranularity);
			closedSet.add(endHash);
			
			// Explore each neighbor of current node
			for (DirectedEdge neighborEdge : current.node.getEdges()) {
				final SpeedNode sn = getSpeedNode(neighborEdge, current, world);

				//If a similar enough node has already been run through
				//no need to add this one at that point
				final long snEndHash = Hasher.hashEndSpeedNode(sn, hashGranularity);
				if (closedSet.contains(snEndHash)) {
					continue;
				}
				
				// Distance from start to neighbor of current node
				final int tentativeGScore = current.gScore + sn.getMoveTime();
				final SpeedNode contester = openSetMap.get(snEndHash);	

				//To long edges are not good, as they can hit into enemies. They are however sometimes necessary.
				final int edgeLength = Math.abs(neighborEdge.source.x - neighborEdge.target.x);
				final int pentaltyIfLongEdgeCurrent = (edgeLength > 5)? PENALTY_LONG_EDGE: 0;
				int penalty = pentaltyIfLongEdgeCurrent;
				if (contester != null) {
					final int contesterEdgeLength = Math.abs(contester.ancestorEdge.source.x - contester.ancestorEdge.target.x);
					final int pentaltyIfLongEdgeContester = (contesterEdgeLength > 5)? PENALTY_LONG_EDGE: 0;
					
					//If a similar enough node exists and that has a better g score
					//then there is no need to add this edge as it's worse than the
					//current one
					if (tentativeGScore + neighborEdge.getWeight() + pentaltyIfLongEdgeCurrent
						>= contester.gScore + contester.ancestorEdge.getWeight() + pentaltyIfLongEdgeContester) {
						continue;
					}
				}
				
				// collision detection (once invincibility handling was also made here) 
				if (!(sn.ancestorEdge instanceof AStarHelperEdge)) {
					sn.currentXPos = current.currentXPos + sn.getMoveInfo().getXMovementDistance();
					sn.parentXPos = current.currentXPos;
					
					if (!sn.isSpeedNodeUseable(world)) {
						continue;
					}
					
					if (sn.originalDoesMovementCollideWithEnemy(current.gScore, enemyPredictor, marioHeight)) {
						continue;
					}
				}
				// Update the edges position in the priority queue
				// by updating the scores and taking it in and out of the queue.
				if (openSetMap.containsKey(sn.hash)) openSet.remove(sn);
				sn.gScore = tentativeGScore;
				sn.fScore = sn.gScore + heuristicFunction(sn, goal) + neighborEdge.getWeight() + penalty;
				sn.parent = current;
				openSet.add(sn);
				openSetMap.put(snEndHash, sn);
				neighborsAsChildsCount++;
			}
		}
		
		foundBestPath = false;
		timesAStarDidNotFinish++;
	}
	
	/**
	 * Retrieves or creates the child speed node from making the action in the ancestorEdge.
	 * If it is present in the recorded hash map of speed node this will be returned, otherwise
	 * a new speed node instance is created and returned. 
	 * @param ancestorEdge
	 * @param current
	 * @param world
	 * @return speedNode
	 */
	private SpeedNode getSpeedNode(DirectedEdge ancestorEdge, SpeedNode current, World world) {
		final long hash = Hasher.hashSpeedNode(current.vx, ancestorEdge, 5000);
		
		final SpeedNode speedNode = speedNodes.get(hash);
		if (speedNode != null) {
			return speedNode;
		}
		
		final SpeedNode newSpeedNode = new SpeedNode(ancestorEdge.target, current, ancestorEdge, hash, world);
		speedNodes.put(hash, newSpeedNode);
		return newSpeedNode;
	}
	
	/**
	 * @param current
	 * @param goal
	 * @return an estimate of the ticks away from the goal
	 */
	private int heuristicFunction(final SpeedNode current, final SpeedNode goal) {
		return MarioControls.getTicksToTarget(goal.node.x - current.currentXPos, current.vx);
	}
	
	/**
	 * @return the currently best path available, which will be the path to the rightmost node found
	 */
	public AStarPath getCurrentBestPath() {
		//lock out here because the lock has to surround foundBestPath aswell
		//because that can also change
		return new AStarPath(currentBestPathEnd, foundBestPath, hashGranularity);
	}
	
	public Long2ObjectOpenHashMap<SpeedNode> getSpeedNodes() {
		return speedNodes;
	}
	
	public void stop() {
		keepRunning = false;
	}

	public static void resetStatistics() {
		timesAStarHasRun = 0;
		timesAStarDidNotFinish = 0;
		totalTimeUsedByAStar = 0;
		neighborsAsChildsCount = 0;
		neighborsAsParentsCount = 0;
	}
}