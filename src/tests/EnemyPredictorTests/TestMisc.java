package tests.EnemyPredictorTests;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.EnemyType;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;
import tests.TestTools;
import tests.UnitTestAgent;
/**
 * 
 * @author Andreas
 *
 */
public class TestMisc {
	@Test
	public void testMultithreadedEnemyAccess() {
		final UnitTestAgent agent = new UnitTestAgent();
		final Environment observation = TestTools.loadLevel("testEnemyPredictor/flat.lvl", agent, false);
		final EnemyPredictor enemyPredictor = new EnemyPredictor();
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		ExecutorService exe = Executors.newFixedThreadPool(16);
		TestTools.spawnEnemy(observation, 1, 1, 1, EnemyType.BULLET_BILL);
		final EnemyPredictor copy = TestEnemyHelper.findEnemies(observation, enemyPredictor, true);
		
		assertEquals(1, copy.getEnemies().size());
		
		for (int i = 0; i < 100000; i++) {
			final int finalI = i;
			exe.submit(() -> copy.getEnemies().get(0).getPositionAtTime(finalI));
		}
		
		for (int i = 0; i < 100000; i++) {
			enemyPredictor.getEnemies().get(0).getPositionAtTime(i);
		}
		
		for (int i = 0; i < 100000; i++) {
			Point2D.Float singleThreadedResult = enemyPredictor.getEnemies().get(0).getPositionAtTime(i);
			Point2D.Float multiThreadedResult = copy.getEnemies().get(0).getPositionAtTime(i);
			
			assertEquals("x was not the same", singleThreadedResult.x, multiThreadedResult.x, MarioControls.ACCEPTED_DEVIATION);
			assertEquals("y was not the same", singleThreadedResult.y, multiThreadedResult.y, MarioControls.ACCEPTED_DEVIATION);
		}
	}
}
