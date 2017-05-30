package MarioAI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.nodes.Node;

public class PathMaster {
	private static final int[] HASH_GRANULARITY = new int[] {2, 4, 8, 16, 24, 32, 40, 48};
	public static final int MAX_THREAD_COUNT = 8;
	private final ExecutorService threadPool;
	private final AStar[] aStars;
	
	public PathMaster(int threadCount) {
		threadCount = Math.min(threadCount, HASH_GRANULARITY.length);
		threadPool = Executors.newFixedThreadPool(threadCount);
		
		aStars = new AStar[threadCount];
		for (int i = 0; i < threadCount; i++) {
			aStars[i] = new AStar(HASH_GRANULARITY[i]);
		}
	}
	
	public void start(final Node start, final Node[] rightmostNodes, float marioSpeed, final EnemyPredictor enemyPredictor, int marioHeight) {
		
	}
	
	public void getBestPath() {
		
	}
	
	public void stop() {
		
	}
}
