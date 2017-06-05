package MarioAI.path;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

public class PathCreator {
	private static final int[] HASH_GRANULARITY = new int[] {2, 4, 8, 16, 24, 32, 40, 48};
	public static final int MAX_THREAD_COUNT = 8;
	private final ExecutorService threadPool;
	private final AStar[] aStars;
	private AStarPath bestPath = null;
	private final World world = new World();
	private final EnemyPredictor enemyPredictor = new EnemyPredictor();
	private final CompletableFuture<Boolean>[] runningTasks;
	private final AStarHelperEdge[] addedEdges = new AStarHelperEdge[World.LEVEL_HEIGHT];
	private final Node[] nodesWithAddedEdges = new Node[World.LEVEL_HEIGHT];
	private Point2D.Float marioFuturePosition;
	public boolean isRunning = false;
	
	@SuppressWarnings("unchecked")
	public PathCreator(int threadCount) {
		//There can't be more threads than granularities as two threads
		//would then have to share the same granularity.
		threadCount = Math.min(threadCount, HASH_GRANULARITY.length);
		threadPool = Executors.newFixedThreadPool(threadCount);
		runningTasks = new CompletableFuture[threadCount];
		
		//Create all the different AStar instances with each a
		//different granularity.
		aStars = new AStar[threadCount];
		for (int i = 0; i < threadCount; i++) {
			aStars[i] = new AStar(HASH_GRANULARITY[i]);
		}
	}
	
	public void initialize(Environment observation) {
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
	}
	
	public void start(final Environment observation, final ArrayList<DirectedEdge> path, final Node[] rightmostNodes, int marioHeight) 
	{
		final DirectedEdge currentEdge = path.get(0);
		
		final Node futureStartNode = currentEdge.target;
		final int timeForward = currentEdge.getMoveInfo().getMoveTime();
		enemyPredictor.moveIntoFuture(timeForward);
		
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final Point2D.Float edgeEndDistance = currentEdge.getMoveInfo().getPositions()[currentEdge.getMoveInfo().getPositions().length - 1];
		
		final float futureMarioXPos = marioXPos + edgeEndDistance.x;
		final float futureMarioYPos = marioYPos - edgeEndDistance.y;
		
		marioFuturePosition = new Point2D.Float(futureMarioXPos, futureMarioYPos);
		
		final float futureMarioSpeed = currentEdge.getMoveInfo().getEndSpeed();
		
		start(futureMarioXPos, futureStartNode, rightmostNodes, futureMarioSpeed, marioHeight);
	}
	
	private void start(final float marioXPos, final Node start, final Node[] rightmostNodes, final float marioSpeed, final int marioHeight) {
		if (isRunning) {
			throw new Error("PathCreator is already running. Stop PathCreator before starting it again.");
		}
		
		isRunning = true;
		
		final SpeedNode startSpeedNode = new SpeedNode(start, marioXPos, marioSpeed, Long.MAX_VALUE);
		final SpeedNode goalSpeedNode = createGoalSpeedNode(rightmostNodes);
		
		for (int i = 0; i < aStars.length; i++) {
			final int q = i;
			runningTasks[i] = CompletableFuture.supplyAsync(() -> 
			{
				aStars[q].initAStar(startSpeedNode, goalSpeedNode, enemyPredictor, marioHeight, world);
				return true;
			}, threadPool);
		}
	}
	
	private SpeedNode createGoalSpeedNode(final Node[] rightmostNodes) {
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
		
		//added hashgranularity to the distance because otherwise two threads
		//can create the exact same node which will have the same hashcode
		//which two nodes aren't allowed to have
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
	
	private void removeGoalFrame() {
		for (int i = 0; i < nodesWithAddedEdges.length; i++) {
			final Node node = nodesWithAddedEdges[i];
			if (node != null) {
				node.removeEdge(addedEdges[i]);
			}
		}
	}
	
	public void blockingFindPath(Environment observation, final Node start, final Node[] rightmostNodes, final float marioSpeed, final EnemyPredictor enemyPredictor, final int marioHeight, final World world) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		
		final SpeedNode startSpeedNode = new SpeedNode(start, marioXPos, marioSpeed, Long.MAX_VALUE);
		final SpeedNode goalSpeedNode = createGoalSpeedNode(rightmostNodes);
		
		aStars[aStars.length - 1].initAStar(startSpeedNode, goalSpeedNode, enemyPredictor, marioHeight, world);
		aStars[aStars.length - 1].stop();
		
		removeGoalFrame();
		
		final AStarPath path = aStars[aStars.length - 1].getCurrentBestPath();
		if (shouldUpdateToNewPath(path)) {
			path.usePath();
			bestPath = path;	
		}
	}
	
	public void updateBestPath() {
		final AStarPath[] paths = new AStarPath[aStars.length];
		for (int i = 0; i < aStars.length; i++) {
			paths[i] = aStars[i].getCurrentBestPath();
		}
		
		Arrays.sort(paths, (AStarPath a, AStarPath b) -> a.granularity - b.granularity);
		
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
		
		if (shouldUpdateToNewPath(paths[paths.length - 1])) {
			//Otherwise chose the path from the astar
			//with the highest granularity
			paths[paths.length - 1].usePath();
			bestPath = paths[paths.length - 1];	
			//System.out.println("Updated best path");
		}
		else {
			bestPath.path.remove(0);
		}
	}
	
	public void discardFoundPath() {
		//bestPath.path.remove(0);
	}
	
	private boolean shouldUpdateToNewPath(AStarPath newPotentialPath) {
		if (newPotentialPath.path == null) {
			return false;
		}
		if (!newPotentialPath.isBestPath && 
			bestPath.isBestPath &&
			bestPath.path.size() > 1) {
			return false;
		}
		
		return true;
	}
	
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
	
	public void syncWithRealWorld(World realWorld, EnemyPredictor realEnemyPredictor) {
		world.syncFrom(realWorld);
		enemyPredictor.syncFrom(realEnemyPredictor);
	}
	
	public void stop() {
		if (!isRunning) {
			throw new Error("PathCreator wasn't running. Start PathCreator before stopping it again.");
		}
		
		for (AStar aStar : aStars) {
			aStar.stop();
		}
		try {
			CompletableFuture.allOf(runningTasks);
		} catch (Exception e) {
			System.out.println("Threadpool in PathCreator failed to shutdown the threads in an orderly manner.\n" + e.getMessage());
		}
		removeGoalFrame();
		isRunning = false;
	}
	
	public HashMap<Long, SpeedNode> getSpeedNodes() {
		return aStars[aStars.length-1].getSpeedNodes();
	}
	
	public int getBlockingGranularity() {
		return aStars[aStars.length-1].hashGranularity;
	}
	
}
