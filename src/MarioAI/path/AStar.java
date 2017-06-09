package MarioAI.path;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

import com.sun.corba.se.spi.orbutil.fsm.State;

import MarioAI.Hasher;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyCollision;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.simulators.EnemySimulator;
import MarioAI.graph.edges.AStarHelperEdge;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.edgeCreation.EdgeCreator;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.StateNode;
import MarioAI.marioMovement.MarioControls;
import javafx.util.Pair;

class AStar {
	private final HashMap<Long, StateNode> speedNodes = new HashMap<Long, StateNode>();
	private final EdgeCreator grapher = new EdgeCreator(); //TODO shouldn give any problems there.
	
	// Set of nodes already explored
	private final HashSet<Long> closedSet = new HashSet<Long>();
	// Set of nodes yet to be explored
	private final PriorityQueue<StateNode> openSet = new PriorityQueue<StateNode>();
	private final Map<Long, StateNode> openSetMap = new HashMap<Long, StateNode>();
	final int hashGranularity;
	private StateNode currentBestPathEnd = null;
	private boolean keepRunning = false;
	private boolean foundBestPath = false;
	private final Object lockBestSpeedNode = new Object();
	
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
	public void initAStar(final StateNode start, final StateNode goal, final EnemyPredictor enemyPredictor, float marioHeight, World world) {
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
	private void runAStar(final StateNode start, final StateNode goal, final EnemyPredictor enemyPredictor, float marioHeight, World world) {		
		while (!openSet.isEmpty() && keepRunning) {
			//System.out.println("Current open set:");
			//System.out.println(openSet);
			
			synchronized (lockBestSpeedNode) {
				
				final StateNode currentState = openSet.remove();
				openSetMap.remove(currentState.hash);
				
				// If goal is reached return solution path.
				if (currentState.node.equals(goal.node)) {
					currentBestPathEnd = currentState;
					foundBestPath = true;
					return;
				}
				//System.out.println("Current node:");
				//System.out.println(current.node + "\nSpeed: " + current.vx + "\nFrom: " + current.ancestorEdge);
				//System.out.println("Current node edges:");
				//System.out.println(current.node.edges + "\n");
				// Current node has been explored.
				final long endHash = Hasher.hashEndSpeedNode(currentState, hashGranularity);
				closedSet.add(endHash);
				//System.out.println(openSet.size()); //Used to check how AStar performs.
				

				// The current best speednode is the one furthest to the right
				// (disregarding if it passes through an enemy or not).
				if (currentBestPathEnd == null || currentState.currentXPos > currentBestPathEnd.currentXPos) {
					currentBestPathEnd = currentState;
				}
				
				// Explore each neighbor of current node
				for (DirectedEdge neighborEdge : currentState.node.getEdges()) {
					final StateNode nextState = getSpeedNode(neighborEdge, currentState, world);
					
					//If a similar enough node has already been run through
					//no need to add this one at that point
					final long nextEndHash = Hasher.hashEndSpeedNode(nextState, hashGranularity);
					if (closedSet.contains(nextEndHash)) {
						continue;
					}
					
					// Distance from start to neighbor of current node
					final int tentativeGScore = currentState.gScore + nextState.getMoveTime();
					
					//If a similar enough node exists and that has a better g score
					//then there is no need to add this edge as it's worse than the
					//current one
					if (openSetMap.containsKey(nextEndHash) &&
							tentativeGScore >= openSetMap.get(nextEndHash).gScore) {
						continue;
					}
					// collision detection and invincibility handling 
					int penalty = 0;
					if (!(nextState.ancestorEdge instanceof AStarHelperEdge)) {
						nextState.currentXPos = nextState.currentXPos + nextState.getMoveInfo().getXMovementDistance();
						nextState.parentXPos = currentState.currentXPos;
						
						if (!nextState.isSpeedNodeUseable(world)) {
							continue;
						}
						/*
						if (nextState.tempDoesMovementCollideWithEnemy(currentState.gScore, enemyPredictor, marioHeight)) {
							//TODO look at this
							continue;
						}*/
						/*
						//Gives extra information about the collision with an enemy, if it happens.
						EnemyCollision firstCollision = new EnemyCollision(); 
						
						if (nextState.doesMovementCollideWithEnemy(currentState.gScore, enemyPredictor, marioHeight, firstCollision)) {
							if (firstCollision.isStompType) { //Stomping means no lost life.
								addStompState(firstCollision, world, currentState, nextState, goal);	
								continue; //The rest is handled in the method above.
							} else {//He has lost life and gets a penalty:
								if (nextState.lives <= 1) {
									// if Mario would die if he hits an enemy, with this many lives. 
									//This node can under no circumstances be used on a path
									continue; 
								}
								int livesLost = (currentState.lives - nextState.lives);
								penalty = livesLost*PENALTY_SCORE;
							}
						}
*/								
					}
					
					// Update the edges position in the priority queue
					// by updating the scores and taking it in and out of the queue.
					updateOpenSet(nextState, tentativeGScore, goal, penalty, currentState, nextEndHash);
				}
			}
		}
		
		//currentBestPathEnd = null;
		foundBestPath = false;
	}
	
	private void addStompState(EnemyCollision firstCollision, World world, StateNode currentState, StateNode wrongNextState, StateNode goal) {
		
		//TODO when done, go through and discuss.
		//Temp test to try to ensure that he lands on enemies:
		int penalty = -PENALTY_SCORE; 
		System.out.println("Temp target enemy");
		//Changing the path:
		Point2D.Float enemyPosition = firstCollision.enemy.getPositionAtTime(firstCollision.tickForCollision);
		int enemyX = (int) enemyPosition.x;
		int enemyY = (int) enemyPosition.y;
		Node[] enemyColumn = world.getColumn(enemyX);
		Node targetNode;
		
		if ((enemyColumn != null && enemyColumn[enemyY] != null) || 
			  enemyColumn == null) { //Ie. the enemy is placed in a block! Or the column with it on is not known!
			throw new Error("Logic error");	
			//Might not be a total logic error, if the enemy is at a permeable block.   
			//TODO remove, not needed, after tests
		} else {
			int enemyNodeHash = Hasher.hashNode(enemyX, enemyY);
			//Make a modified speed node to the top of this position,
	 		//from the former position data.
			if (world.hasEnemyCollisionNode(enemyNodeHash)) { //Stomped on this enemy before?
				targetNode = world.getEnemyCollisionNode(enemyX,enemyY);
				//Add the target to this speed node
			} else {
				//No collisions for this placement has been added. This is now done:
				targetNode = new Node(enemyX, enemyY, (byte)42);  //42 is arbitrary
				//!!!!!! TODO Ensure no parallel connection with EdgeCreator. Shouldn't happen:
				//TODO little mistake: might think it is able to run, after the stomp
				grapher.connectLoneNode(targetNode, world);
				world.addEnemyCollisionNode(targetNode); //To be reused later.						
			}
		}	
		//TODO change hashGranularity to the one desired	
		//TODO reuse the speed node.
		//Do it from the living enemies described above.
		
		StateNode stompState = wrongNextState.getStompVersion(firstCollision, targetNode, world);
		//Make the target equal to targetNode, and finish the creation of the speed node.
		EnemyPredictor.hitEnemy(firstCollision, stompState);
		final int tentativeGScore = currentState.gScore + stompState.getMoveTime();	
		long stompNodeHash = Hasher.hashEndSpeedNode(stompState, hashGranularity);
		
		//Needs the same A* checks as before:
		
		if (closedSet.contains(stompNodeHash) ||
			(openSetMap.containsKey(stompNodeHash) &&	tentativeGScore >= openSetMap.get(stompNodeHash).gScore)) {
			//ie, it has already been used, or we have a better version.
			return;
		}	else updateOpenSet(stompState, tentativeGScore, goal, penalty, currentState, (int) stompNodeHash);
		
		
		/*
		closedSet.add(snEndHash); //Ensures
		//There should be some small finesses? that means that this is needed. 
		//Ie. because we need to change the target of the speed node, to another node.
		 */
		
	}

	public void updateOpenSet(StateNode sn, final int tentativeGScore, final StateNode goal, 
									  final int penalty, final StateNode current, final long snEndHash){

		//Update the edges position in the priority queue
		//by updating the scores and taking it in and out of the queue.
		if (openSetMap.containsKey(sn.hash)) openSet.remove(sn);
		sn.gScore = tentativeGScore;
		sn.fScore = sn.gScore + heuristicFunction(sn, goal)  + penalty;
		sn.parent = current;
		openSet.add(sn);
		openSetMap.put(snEndHash, sn);
	}
	
	
	/**
	 * @param neighborEdge
	 * @param current
	 * @param world
	 * @return speedNode
	 */
	private StateNode getSpeedNode(DirectedEdge neighborEdge, StateNode current, World world) {
		final long hash = Hasher.hashSpeedNode(current.vx, neighborEdge, 5000);
		
		final StateNode speedNode = speedNodes.get(hash);
		if (speedNode != null) {
			return speedNode;
		}
		
		final StateNode newSpeedNode = new StateNode(neighborEdge.target, current, neighborEdge, current.livingEnemies, hash, world);
		speedNodes.put(hash, newSpeedNode);
		return newSpeedNode;
	}
	
	/**
	 * @param current
	 * @param goal
	 * @return an estimate of the ticks away from the goal
	 */
	private int heuristicFunction(final StateNode current, final StateNode goal) {
		return MarioControls.getTicksToTarget(goal.node.x - current.currentXPos, current.vx);
	}
	
	public AStarPath getCurrentBestPath() {
		//lock out here because the lock has to surround foundBestPath aswell
		//because that can also change
		synchronized (lockBestSpeedNode) {
			return new AStarPath(currentBestPathEnd, true, hashGranularity);
		}
	}
	
	public HashMap<Long, StateNode> getSpeedNodes() {
		return speedNodes;
	}
	
	public void stop() {
		keepRunning = false;
	}
}