package MarioAI.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sun.istack.internal.FinalArrayList;
import com.sun.jndi.rmi.registry.RegistryContext;

import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.Node;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;

public class PathCreator {
	private static final int[] HASH_GRANULARITY = new int[] {2, 4, 8, 16, 24, 32, 40, 48};
	public static final int MAX_THREAD_COUNT = 8;
	private final ExecutorService threadPool;
	private final AStar[] aStars;
	private ArrayList<DirectedEdge> bestPath = null;
	private final World world = new World();
	private final EnemyPredictor enemyPredictor = new EnemyPredictor();
	private final CompletableFuture<Boolean>[] runningTasks;
	
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
	
	public void start(final ArrayList<DirectedEdge>path , 
					  final Node[] rightmostNodes, 
					  float marioSpeed, 
					  final EnemyPredictor enemyPredictor, 
					  int marioHeight) 
	{
		final Node startNode = path.get(0).target;
		int timeForward = path.get(0).getMoveInfo().getMoveTime();
		enemyPredictor.moveIntoFuture(timeForward);
		
		start(startNode, rightmostNodes, marioSpeed, enemyPredictor, marioHeight);
	}
	
	public void start(final Node start, final Node[] rightmostNodes, final float marioSpeed, final EnemyPredictor enemyPredictor, final int marioHeight) {
		for (int i = 0; i < aStars.length; i++) {
			final int q = i;
			runningTasks[i] = CompletableFuture.supplyAsync(() -> 
			{
				aStars[q].runMultiNodeAStar(start, rightmostNodes, marioSpeed, enemyPredictor, marioHeight, world);
				return true;
			}, threadPool);
		}
		/*
		for (AStar aStar : aStars) {
			threadPool.submit(() -> aStar.runMultiNodeAStar(start, rightmostNodes, marioSpeed, enemyPredictor, marioHeight, world));
		}
		*/
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
				bestPath = paths[i].path;
				return;
			}
		}
		
		//Otherwise chose the path from the astar
		//with the highest granularity
		bestPath = paths[paths.length - 1].path;
	}
	
	public ArrayList<DirectedEdge> getBestPath() {
		return bestPath;
	}
	
	public void syncWithRealWorld(World realWorld, EnemyPredictor realEnemyPredictor) {
		world.syncFrom(realWorld);
		enemyPredictor.syncFrom(realEnemyPredictor);
	}
	
	public void stop() {
		for (AStar aStar : aStars) {
			aStar.stop();
		}
		try {
			CompletableFuture.allOf(runningTasks);
		} catch (Exception e) {
			System.out.println("Threadpool in PathCreator failed to shutdown the threads in an orderly manner.\n" + e.getMessage());
		}
	}
	
	public void reset(final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight) {
		stop();
		start(start, rightmostNodes, marioSpeed, enemyPredictor, marioHeight);
	}
}
