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

class AStar {
	private final Long2ObjectOpenHashMap<SpeedNode> speedNodes = new Long2ObjectOpenHashMap<SpeedNode>();
	
	// Set of nodes already explored
	private final LongOpenHashSet closedSet = new LongOpenHashSet();
	// Set of nodes yet to be explored
	private final PriorityQueue<SpeedNode> openSet = new PriorityQueue<SpeedNode>();
	private final Long2ObjectOpenHashMap<SpeedNode> openSetMap = new Long2ObjectOpenHashMap<SpeedNode>();
	public final int hashGranularity;
	private SpeedNode currentBestPathEnd = null;
	private boolean keepRunning = false;
	private boolean foundBestPath = false;
	
	private static final int PENALTY_SCORE = 9001; // arbitrary high value; "It's over 9000".
	
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
		
		runAStar(start, goal, enemyPredictor, marioHeight, world);
	}

	/**
	 * Main part of the A* search algorithm. Adapted to fit project and problem specification.
	 * @param start
	 * @param goal
	 * @return
	 */
	private void runAStar(final SpeedNode start, final SpeedNode goal, final EnemyPredictor enemyPredictor, float marioHeight, World world) {		
		while (!openSet.isEmpty() && keepRunning) {
			
			final SpeedNode current = openSet.remove();
			openSetMap.remove(current.hash);
			
			// If goal is reached return solution path.
			if (current.node.equals(goal.node)) {
				currentBestPathEnd = current;
				foundBestPath = true;
				return;
			}
			// The current best speednode is the one furthest to the right
			// (disregarding if it passes through an enemy or not).
			if ((currentBestPathEnd == null || current.currentXPos > currentBestPathEnd.currentXPos) && current != start) {
				currentBestPathEnd = current;
			}
			
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
				
				//If a similar enough node exists and that has a better g score
				//then there is no need to add this edge as it's worse than the
				//current one
				final SpeedNode contester = openSetMap.get(snEndHash);
				if (contester != null &&
					tentativeGScore >= contester.gScore) {
					continue;
				}
				
				// collision detection and invincibility handling 
				int penalty = 0;
				if (!(sn.ancestorEdge instanceof AStarHelperEdge)) {
					sn.currentXPos = current.currentXPos + sn.getMoveInfo().getXMovementDistance();
					sn.parentXPos = current.currentXPos;
					
					if (!sn.isSpeedNodeUseable(world)) {
						continue;
					}
					
					if (sn.tempDoesMovementCollideWithEnemy(current.gScore, enemyPredictor, marioHeight)) {
						continue;
					}
					/*
					if (sn.ticksOfInvincibility == 0) {
						if (sn.doesMovementCollideWithEnemy(current.gScore, enemyPredictor, marioHeight)) {
							continue;
							
							if (sn.lives <= 1) {
								continue; // if Mario would die if he hits an enemy this node can under no circumstances be used on a path
							}
							penalty = PENALTY_SCORE;
							
						}
					}
					*/
				}					
				
				// Update the edges position in the priority queue
				// by updating the scores and taking it in and out of the queue.
				if (openSetMap.containsKey(sn.hash)) openSet.remove(sn);
				sn.gScore = tentativeGScore;
				sn.fScore = sn.gScore + heuristicFunction(sn, goal) + neighborEdge.getWeight() + penalty;
				sn.parent = current;
				openSet.add(sn);
				openSetMap.put(snEndHash, sn);
			}
		}
		
		//currentBestPathEnd = null;
		foundBestPath = false;
	}
	
	/**
	 * @param neighborEdge
	 * @param current
	 * @param world
	 * @return speedNode
	 */
	private SpeedNode getSpeedNode(DirectedEdge neighborEdge, SpeedNode current, World world) {
		final long hash = Hasher.hashSpeedNode(current.vx, neighborEdge, 5000);
		
		final SpeedNode speedNode = speedNodes.get(hash);
		if (speedNode != null) {
			return speedNode;
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
		return MarioControls.getTicksToTarget(goal.node.x - current.currentXPos, current.vx);
	}
	
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
}