package MarioAI.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.Node;

public class PathCreator {
	private static final int[] HASH_GRANULARITY = new int[] {2, 4, 8, 16, 24, 32, 40, 48};
	public static final int MAX_THREAD_COUNT = 8;
	private final ExecutorService threadPool;
	private final AStar[] aStars;
	private ArrayList<DirectedEdge> bestPath = null;
	
	public PathCreator(int threadCount) {
		//There can't be threads than granularities as two threads
		//would then have to share the same granularity.
		threadCount = Math.min(threadCount, HASH_GRANULARITY.length);
		threadPool = Executors.newFixedThreadPool(threadCount);
		
		//Create all the different AStar instances with each a
		//different granularity.
		aStars = new AStar[threadCount];
		for (int i = 0; i < threadCount; i++) {
			aStars[i] = new AStar(HASH_GRANULARITY[i]);
		}
	}
	
	public void start(final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight) {
		for (AStar aStar : aStars) {
			threadPool.submit(() -> aStar.runMultiNodeAStar(start, rightmostNodes, marioSpeed, enemyPredictor, marioHeight));
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
	
	public void stop() {
		for (AStar aStar : aStars) {
			aStar.stop();
		}
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			System.out.println("Threadpool in PathCreator failed to shutdown the threads in an orderly manner.\n" + e.getMessage());
		}
	}
	
	public void reset(final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight) {
		stop();
		start(start, rightmostNodes, marioSpeed, enemyPredictor, marioHeight);
	}
}
