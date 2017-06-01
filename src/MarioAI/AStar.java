package MarioAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

import com.sun.istack.internal.FinalArrayList;

import MarioAI.enemy.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.mario.environments.Environment;


public class AStar {
	private final HashMap<Long, SpeedNode> speedNodes = new HashMap<Long, SpeedNode>();
	
	// Set of nodes already explored
	private final HashSet<Integer> closedSet = new HashSet<Integer>();
	// Set of nodes yet to be explored
	private final PriorityQueue<SpeedNode> openSet = new PriorityQueue<SpeedNode>();
	private final Map<Integer, SpeedNode> openSetMap = new HashMap<Integer, SpeedNode>();
	private SpeedNode startSpeedNode = null;
	private SpeedNode goalSpeedNode = null;
	private EnemyPredictor enemyPredictor = null;
	private int marioHeight;
	private float marioSpeed;
	
	private boolean finishedARun = false; // true if AStar has found a path and false otherwise
	private boolean initAStar; // true only in the first call of multinodeastar
	
	private ArrayList<DirectedEdge> currentBestPath = null;

	
	public AStar() {
		initAStar = true;
	}
	
	/**
	 * TEMP method for running A* if no time is given
	 * (Time to run is set to max possible value)
	 * @param start
	 * @param rightmostNodes
	 * @param marioSpeed
	 * @param enemyPredictor
	 * @param marioHeight
	 * @return
	 */
	public ArrayList<DirectedEdge> runMultiNodeAStar(final Environment observation, final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight) {
		return runMultiNodeAStar(observation, start, rightmostNodes, marioSpeed, enemyPredictor, marioHeight, Integer.MAX_VALUE);
	}
	
	/**
	 * A* algorithm for multiple goal nodes (tries to find path to just one of them). Method to be used with the right most column of the screen
	 * @param start
	 * @param rightmostNodes
	 * @param marioSpeed
	 * @param enemyPredictor
	 * @param marioHeight
	 * @param timeToRun
	 * @return
	 */
	public ArrayList<DirectedEdge> runMultiNodeAStar(final Environment observation, final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight, int timeToRun) {
		
		final DirectedEdge[] addedEdges = new DirectedEdge[rightmostNodes.length];
		if (finishedARun || initAStar) {
			initAStar = false;
			
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

			final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			startSpeedNode = new SpeedNode(start, marioXPos, marioSpeed, Long.MAX_VALUE);
			goalSpeedNode = new SpeedNode(goal, 0, Long.MIN_VALUE);
			
			for (int i = 0; i < rightmostNodes.length; i++) {
				final Node node = rightmostNodes[i];
				if (node != null) {
					RunningEdge edge = new RunningEdge(node, goal);
					node.addEdge(edge);
					addedEdges[i] = edge;
				}
			}
			
			// Remove auxiliary goal node and update nodes having it as a neighbor accordingly
			//final ArrayList<DirectedEdge> path = initAStar(timeToRun);
			reset(startSpeedNode, 
							goalSpeedNode,
							marioSpeed,
							enemyPredictor, 
							marioHeight);
			finishedARun = false;
		}
		final ArrayList<DirectedEdge> path = runAStar(startSpeedNode, 
																	 goalSpeedNode,
																	 marioSpeed,
																	 enemyPredictor, 
																	 marioHeight, timeToRun);
		//speedNodes.remove(Long.MAX_VALUE);
		//speedNodes.remove(Long.MIN_VALUE);
		if (path != null && path.size() > 0) { //TODO remove when error is fixed
			path.remove((path.size() - 1));
		}
		if (finishedARun) {
			for (int i = 0; i < rightmostNodes.length; i++) {
				final Node node = rightmostNodes[i];
				if (node != null) {
					node.removeEdge(addedEdges[i]);
				}
			}
		}
		
		return path;
	}
	
	private void reset(final SpeedNode start, final SpeedNode goal, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight) {
		startSpeedNode = start;
		goalSpeedNode = goal;
		this.marioHeight = marioHeight;
		this.marioSpeed  = marioSpeed;
		this.enemyPredictor = enemyPredictor;
		
		closedSet.clear();
		openSet.clear();
		openSetMap.clear();
		
		// Initialization
		openSet.add(startSpeedNode);
		openSetMap.put(Integer.MAX_VALUE, startSpeedNode);
		startSpeedNode.gScore = 0;
		startSpeedNode.fScore = heuristicFunction(startSpeedNode, goalSpeedNode);
	}

	/**
	 * Basic A* search algorithm
	 * @param timeToRun
	 * @return
	 */
	public ArrayList<DirectedEdge> runAStar(final SpeedNode start, final SpeedNode goal, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight, int timeToRun) {
		
		long startTime = System.currentTimeMillis();
		
		while (!openSet.isEmpty()) {
			//System.out.println("Current open set:");
			//System.out.println(openSet);
			
			long currentTime = System.currentTimeMillis();
			if (currentTime - startTime >= timeToRun) return getCurrentBestPath();
			
			final SpeedNode current = openSet.remove();
			openSetMap.remove(current.hash);
			
			// If goal is reached return solution path.
			if (current.node.equals(goalSpeedNode.node)) {
				currentBestPath = reconstructPath(current);
				finishedARun = true;
				return currentBestPath;
			}
			//System.out.println("Current node:");
			//System.out.println(current.node + "\nSpeed: " + current.vx + "\nFrom: " + current.ancestorEdge);
			//System.out.println("Current node edges:");
			//System.out.println(current.node.edges + "\n");
			// Current node has been explored.
			final int endHash = Hasher.hashEndSpeedNode(current);
			closedSet.add(endHash);
			//System.out.println(openSet.size()); //Used to check how AStar performs.
			
			// Explore each neighbor of current node
			for (DirectedEdge neighborEdge : current.node.getEdges()) {			
				final SpeedNode sn = getSpeedNode(neighborEdge, current);
				
				//System.out.println("Current edge: ");
				//System.out.println(neighborEdge + "\n");
				
				if (!sn.isSpeedNodeUseable()) {
					continue;
				}
				
				if (sn.getMoveInfo().hasCollisions(current)) {
					continue;
				}
				
				if (sn.doesMovementCollideWithEnemy(current.gScore, enemyPredictor, marioHeight)) {
					continue;
				}
				
				//If a similar enough node has already been run through
				//no need to add this one at that point
				final int snEndHash = Hasher.hashEndSpeedNode(sn);
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
				sn.fScore = sn.gScore + heuristicFunction(sn, goalSpeedNode) + neighborEdge.getWeight();
				sn.parent = current;
				openSet.add(sn);
				openSetMap.put(snEndHash, sn);
			}
		}
		// No solution was found
		return null;
	}
	
	public SpeedNode getSpeedNode(DirectedEdge neighborEdge, SpeedNode current) {
		final long hash = Hasher.hashSpeedNode(current.vx, neighborEdge);
		
		final SpeedNode speedNode = speedNodes.get(hash);
		if (speedNode != null) {
			//return speedNode; //TODO temp for testing purposes. It actually doesn't work, see test level.
		}
		
		final SpeedNode newSpeedNode = new SpeedNode(neighborEdge.target, current, neighborEdge, hash);
		speedNodes.put(hash, newSpeedNode);
		return newSpeedNode;
	}
	
	public HashMap<Long, SpeedNode> getSpeedNodes() {
		return speedNodes;
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
			//if (currentSpeedNode.parent.parent == null) {
			//	System.out.println("First transition speed: " + currentSpeedNode.vx);				
			//}
			currentSpeedNode = currentSpeedNode.parent;
		}
		Collections.reverse(path);
		return path;
	}
	
	private ArrayList<DirectedEdge> getCurrentBestPath() {
		return currentBestPath;
	}
	
	/**
	 * @param currentSpeedNode
	 * @return the path segment corresponding to the best path so far
	 */
	public ArrayList<DirectedEdge> getCurrentBestSegmentPath() {
		SpeedNode currentSpeedNode = openSet.peek();
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		while (currentSpeedNode.parent != null) {
			currentSpeedNode.use();
			path.add(currentSpeedNode.ancestorEdge);
			//if (currentSpeedNode.parent.parent == null) {
			//	System.out.println("First transition speed: " + currentSpeedNode.vx);				
			//}
			currentSpeedNode = currentSpeedNode.parent;
		}
		Collections.reverse(path);
		return path;
	}
	
}