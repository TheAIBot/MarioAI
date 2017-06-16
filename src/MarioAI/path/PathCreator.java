package MarioAI.path;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.AStarHelperEdge;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * 
 * @author Andreas Gramstrup
 *
 */
public class PathCreator {
	private static final int MAX_HASH_GRANULARITY = 16;
	private static final int[] HASH_GRANULARITY = new int[] {2, MAX_HASH_GRANULARITY, 8, 40, 24, 16, 40, 4}; //{2, 4, 8, 16, 24, 32, 40, 48};
	public static final int MAX_THREAD_COUNT = 8;
	private final ExecutorService threadPool;
	private final AStar[] aStars;
	private final AStar singleThreadAstar; // AStar used when running path finding in the same thread
	private AStarPath bestPath = null;
	private final World world = new World();
	private final EnemyPredictor enemyPredictor = new EnemyPredictor();
	private final CompletableFuture<Void>[] runningTasks;
	private final AStarHelperEdge[] addedEdges = new AStarHelperEdge[World.LEVEL_HEIGHT];
	private final Node[] nodesWithAddedEdges = new Node[World.LEVEL_HEIGHT];
	private Point2D.Float marioFuturePosition;
	public boolean isRunning = false;
	
	@SuppressWarnings("unchecked")
	public PathCreator(int threadCount) {
		//There can't be more threads than granularities as two threads
		//would then have to share the same granularity.
		threadCount = Math.min(threadCount, MAX_THREAD_COUNT);
		threadPool = Executors.newFixedThreadPool(threadCount);
		runningTasks = new CompletableFuture[threadCount];
		
		//Create all the different AStar instances with each a
		//different granularity.
		aStars = new AStar[threadCount];
		for (int i = 0; i < threadCount; i++) {
			aStars[i] = new AStar(HASH_GRANULARITY[i]);
		}
		
		Arrays.sort(aStars, (AStar a, AStar b) -> a.hashGranularity - b.hashGranularity);
		
		singleThreadAstar = new AStar(MAX_HASH_GRANULARITY);
	}
	
	/**
	 * Initialize information used in this class.
	 * @param observation
	 */
	public void initialize(Environment observation) {
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
	}
	
	/**
	 * Starts the multithreaded AStar one edge into the future.
	 * @param observation
	 * @param path
	 * @param rightmostNodes
	 * @param marioHeight
	 */
	public void start(final Environment observation, final ArrayList<DirectedEdge> path, final Node[] rightmostNodes, float marioHeight) 
	{
		final DirectedEdge currentEdge = path.get(0);
		
		final Node futureStartNode = currentEdge.target;
		final int timeForward = currentEdge.getMoveInfo().getMoveTime();
		//Move enemypredictor into the future so when the AStars begins the 
		//enemies will be at the correct places in the future
		enemyPredictor.moveIntoFuture(timeForward);
		
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());

        final float edgeEndDistanceX = currentEdge.getMoveInfo().getXPositions()[currentEdge.getMoveInfo().getXPositions().length - 1];
        final float edgeEndDistanceY = currentEdge.getMoveInfo().getYPositions()[currentEdge.getMoveInfo().getYPositions().length - 1];
		
        //Add distance moved by edge to marios poistion to get marios position after moving over the edge
		final float futureMarioXPos = marioXPos + edgeEndDistanceX;
		final float futureMarioYPos = marioYPos - edgeEndDistanceY;
		
		marioFuturePosition = new Point2D.Float(futureMarioXPos, futureMarioYPos);
		
		final float futureMarioSpeed = currentEdge.getMoveInfo().getEndSpeed();
		
		start(futureMarioXPos, futureStartNode, rightmostNodes, futureMarioSpeed, marioHeight);
	}
	
	/**
	 * Starts the multithreaded AStar implementation
	 * @param marioXPos
	 * @param start
	 * @param rightmostNodes
	 * @param marioSpeed
	 * @param marioHeight
	 */
	private void start(final float marioXPos, final Node start, final Node[] rightmostNodes, final float marioSpeed, final float marioHeight) {
		//Can't start if it's already running
		if (isRunning) {
			throw new Error("PathCreator is already running. Stop PathCreator before starting it again.");
		}
		
		isRunning = true;
		
		final SpeedNode startSpeedNode = new SpeedNode(start, marioXPos, marioSpeed, Long.MAX_VALUE);
		//Convert array of goalnodes into a single goal speednode
		final SpeedNode goalSpeedNode = createGoalSpeedNode(rightmostNodes);
		
		//Start each AStart in a new thread
		for (int i = 0; i < aStars.length; i++) {
			final int q = i; // Stupid java can only take final variables as arguments to a lambda
			runningTasks[i] = CompletableFuture.runAsync(() -> aStars[q].initAStar(startSpeedNode, goalSpeedNode, enemyPredictor, marioHeight, world), threadPool);
		}
	}
	
	/**
	 * Convert array of goalnodes into a single goal speednode
	 * @param rightmostNodes
	 * @return
	 */
	private SpeedNode createGoalSpeedNode(final Node[] rightmostNodes) {
		// Add singleton goal node far to the right. This will ensure each
		// vertical distance is minimal and all nodes in rightmost column will be
		// pretty good goal positions to end up in after A* search 
		
		//First find the x position of the nodes the in column
		int goalX = 0;
		for (int i = 0; i < rightmostNodes.length; i++) {
			if (rightmostNodes[i] != null) {
				goalX = rightmostNodes[i].x;
				break;
			}
		}
		//Set the goal to be 50 blocks ahead
		goalX += 50;
		
		//Create edges from the nodes in the column to the goal nodes and save
		//the newly created edges so they can be deleted again
		final Node goal = new Node(goalX, 2, (byte) 3);
		for (int i = 0; i < rightmostNodes.length; i++) {
			final Node node = rightmostNodes[i];
			if (node != null) {
				AStarHelperEdge edge = new AStarHelperEdge(node, goal);
				node.addEdge(edge);
				addedEdges[i] = edge;
			}
			
			nodesWithAddedEdges[i] = node;
		}
		
		return new SpeedNode(goal, 0, Long.MIN_VALUE);
	}
	
	/**
	 * Removed the edges that was created in the method 
	 * createGoalSpeedNode from the Node column to the goal node
	 */
	private void removeGoalFrame() {
		for (int i = 0; i < nodesWithAddedEdges.length; i++) {
			final Node node = nodesWithAddedEdges[i];
			if (node != null) {
				node.removeEdge(addedEdges[i]);
			}
		}
	}
	
	/**
	 * A singlethreaded implementation of path finding
	 * @param observation
	 * @param start
	 * @param rightmostNodes
	 * @param marioSpeed
	 * @param enemyPredictor
	 * @param marioHeight
	 * @param world
	 * @param newEnemiesSpawned
	 */
	public void blockingFindPath(Environment observation, final Node start, final Node[] rightmostNodes, final float marioSpeed, final EnemyPredictor enemyPredictor, final float marioHeight, final World world, final boolean newEnemiesSpawned) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		
		final SpeedNode startSpeedNode = new SpeedNode(start, marioXPos, marioSpeed, Long.MAX_VALUE);
		final SpeedNode goalSpeedNode = createGoalSpeedNode(rightmostNodes);
		
		singleThreadAstar.initAStar(startSpeedNode, goalSpeedNode, enemyPredictor, marioHeight, world);
		//Needs to call stop after the AStar is done so it doesn't fuck it up if 
		//the multithreaded implementation is run afterwards
		singleThreadAstar.stop();
		
		removeGoalFrame();
		
		final AStarPath path = singleThreadAstar.getCurrentBestPath();
		if (shouldUpdateToNewPath(path, newEnemiesSpawned)) {
			path.usePath();
			bestPath = path;	
		}
	}
	
	/**
	 * Looks at all the paths created the the different astars when the multithreaded
	 * solution was run and uses the best one
	 * @param newEnemiesSpawned
	 */
	public void updateBestPath(final boolean newEnemiesSpawned) {
		final AStarPath[] paths = new AStarPath[aStars.length];
		for (int i = 0; i < aStars.length; i++) {
			paths[i] = aStars[i].getCurrentBestPath();
		}
		
		//Of one or more paths are finished then chose
		//the path with the highest granularity.
		for (int i = aStars.length - 1; i >= 0; i--) {
			if (paths[i].isBestPath) {
				paths[i].usePath();
				bestPath = paths[i];
				//System.out.println("Updated best path");
				return;
			}
		}
		
		if (shouldUpdateToNewPath(paths[paths.length - 1], newEnemiesSpawned)) {
			//Otherwise chose the path from the astar
			//with the highest granularity which is the last one
			//as the array is sorted after granularity in the constructor
			paths[paths.length - 1].usePath();
			bestPath = paths[paths.length - 1];
		}
	}
	
	/**
	 * Determines whether this given path is better than the current path
	 * @param newPotentialPath
	 * @param newEnemiesSpawned
	 * @return
	 */
	private boolean shouldUpdateToNewPath(AStarPath newPotentialPath, final boolean newEnemiesSpawned) {
		//New path can't be better than current if there is none
		if (newPotentialPath.path == null) {
			return false;
		}
		//If current best path is null then new path must be better or equally as bad
		if (bestPath == null ||
			bestPath.path == null) {
			return true;
		}
		
		//If a new enemy spawned or if the new path isn an optimal path and the current best path isn't
		//then return true
		if (!newEnemiesSpawned &&
			!newPotentialPath.isBestPath && 
			bestPath.isBestPath &&
			bestPath.path.size() > 1) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks wheter mario was at the expected position after running the multithreaded astar method
	 * @param observation
	 * @return
	 */
	public boolean isMarioAtExpectedPosition(Environment observation) {
		if (marioFuturePosition == null) {
			throw new Error("marioFuturePosition wasn't set.");
		}
		
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final float diffX = Math.abs(marioFuturePosition.x - marioXPos);
		final float diffY = Math.abs(marioFuturePosition.y - marioYPos);
		
		return diffX < MarioControls.ACCEPTED_DEVIATION && 
			   diffY < MarioControls.ACCEPTED_DEVIATION;
	}
	
	public ArrayList<DirectedEdge> getBestPath() {
		return bestPath == null ? null : bestPath.path;
	}
	
	/**
	 * Updates the world and enemypredictor from the ones in FastAndFurious
	 * @param realWorld
	 * @param realEnemyPredictor
	 */
	public void syncWithRealWorld(World realWorld, EnemyPredictor realEnemyPredictor) {
		world.syncFrom(realWorld);
		enemyPredictor.syncFrom(realEnemyPredictor);
	}
	
	/**
	 * Stops running the multithreaded astar
	 */
	public void stop() {
		//Can't stop if it wasn't started
		if (!isRunning) {
			throw new Error("PathCreator wasn't running. Start PathCreator before stopping it again.");
		}

		for (AStar aStar : aStars) {
			aStar.stop();
		}
		//Wait for all threads to stop
		try {
			CompletableFuture.allOf(runningTasks);
		} catch (Exception e) {
			System.out.println("Threadpool in PathCreator failed to shutdown the threads in an orderly manner.\n" + e.getMessage());
		}
		//pathfinding is done so the goalframe can now be removed
		removeGoalFrame();
		isRunning = false;
	}
	
	public Long2ObjectOpenHashMap<SpeedNode> getSpeedNodesFromSingleThreadAStar() {
		return singleThreadAstar.getSpeedNodes();
	}
	
	public int getBlockingGranularitySingleThreadAStar() {
		return singleThreadAstar.hashGranularity;
	}
	
}
