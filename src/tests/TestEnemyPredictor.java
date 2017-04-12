package tests;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Test;

import MarioAI.enemy.EnemyPredictor;
import MarioAI.enemy.simulators.EnemySimulator;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;

public class TestEnemyPredictor {
	
	@Test
	public void testPredictingBulletBill() {
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		Environment observation = TestTools.loadLevel("flat.lvl", new UnitTestAgent(), true);
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		
		ArrayList<Sprite> enemies = new ArrayList<Sprite>();
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 1, 1));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 1, 80, "Bullet bill");
		enemies.clear();
		
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 1, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 2, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 3, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 4, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 5, 1));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 5, 80, "Bullet bill");
		enemies.clear();	
		
		
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 1, -1));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 1, 80, "Bullet bill");
		enemies.clear();
		
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 1, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 2, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 3, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 4, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 5, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 6, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 7, -1));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 7, 80, "Bullet bill");
		enemies.clear();
		
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 1, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 2, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 3, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 4, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 5, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 6, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 3, 7, 1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 1, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 2, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 3, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 4, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 5, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 6, -1));
		enemies.add(TestTools.SpawnBulletBill(observation, 19, 7, -1));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 14, 80, "Bullet bill");
		enemies.clear();
	}
	
	private void testNoRemovalOfEnemyAndPrediction(Environment observation, EnemyPredictor enemyPredictor, ArrayList<Sprite> enemies, int enemyCount, int testTime, String enemyName) {
		for (int i = 0; i < 3; i++) {
			TestTools.runOneTick(observation);
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		}
		assertEquals("didn't find " + enemyName + " after the first 3 ticks", enemyCount, enemyPredictor.getEnemies().size());
		
		final ArrayList<Point2D.Float[]> predictedPositions = new ArrayList<Point2D.Float[]>();
		for (EnemySimulator enemy : enemyPredictor.getEnemies()) {
			for (int i = 0; i < enemyCount; i++) {
				final Point2D.Float[] enemyPredictedPositions = new Point2D.Float[testTime];
				
				for (int j = 1; j <= testTime; j++) {
					enemyPredictedPositions[j - 1] = enemy.getPositionAtTime(j);
				}
				
				predictedPositions.add(enemyPredictedPositions);
			}
		}
		
		for (int i = 0; i < 80; i++) {
			TestTools.runOneTick(observation);
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
			assertEquals("lost nullet bill after " + i + " ticks", enemyCount, enemyPredictor.getEnemies().size());
		}
		
		enemies.forEach(x -> TestTools.removeEnemy(observation, x));
	}
}
