package tests.EnemyPredictorTests;
import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.EnemyType;
import MarioAI.enemySimuation.simulators.EnemySimulator;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;
import tests.TestTools;
import tests.UnitTestAgent;

public class TestEnemyPrediction {
	
	@Test
	public void testPredictingRedKoopa() {
		testEnemy(EnemyType.RED_KOOPA, false);
	}
	@Test
	public void testPredictingRedKoopaFromCopy() {
		testEnemy(EnemyType.RED_KOOPA, true);
	}
	@Test
	public void testPredictingRedKoopaWinged() {
		testEnemy(EnemyType.RED_KOOPA.asWinged(), false);
	}
	@Test
	public void testPredictingRedKoopaWingedFromCopy() {
		testEnemy(EnemyType.RED_KOOPA.asWinged(), true);
	}
	
	@Test
	public void testPredictingGreenKoopa() {
		testEnemy(EnemyType.GREEN_KOOPA, false);
	}
	@Test
	public void testPredictingGreenKoopaFromCopy() {
		testEnemy(EnemyType.GREEN_KOOPA, true);
	}
	@Test
	public void testPredictingGreenKoopaWinged() {
		testEnemy(EnemyType.GREEN_KOOPA.asWinged(), false);
	}
	@Test
	public void testPredictingGreenKoopaWingedFromCopy() {
		testEnemy(EnemyType.GREEN_KOOPA.asWinged(), true);
	}
	
	@Test
	public void testPredictingGoomba() {
		testEnemy(EnemyType.GOOMBA, false);
	}
	@Test
	public void testPredictingGoombaFromCopy() {
		testEnemy(EnemyType.GOOMBA, true);
	}
	@Test
	public void testPredictingGoombaWinged() {
		testEnemy(EnemyType.GOOMBA.asWinged(), false);
	}
	@Test
	public void testPredictingGoombaWingedFromCopy() {
		testEnemy(EnemyType.GOOMBA.asWinged(), true);
	}
	
	@Test
	public void testPredictingSpiky() {
		testEnemy(EnemyType.SPIKY, false);
	}
	@Test
	public void testPredictingSpikyFromCopy() {
		testEnemy(EnemyType.SPIKY, true);
	}
	@Test
	public void testPredictingSpikyWinged() {
		testEnemy(EnemyType.SPIKY.asWinged(), false);
	}
	@Test
	public void testPredictingSpikyWingedFromCopy() {
		testEnemy(EnemyType.SPIKY.asWinged(), true);
	}
	
	@Test
	public void testPredictingFlower() {
		testEnemy(EnemyType.FLOWER, false);
	}
	@Test
	public void testPredictingFlowerFromCopy() {
		testEnemy(EnemyType.FLOWER, true);
	}
	
	/*@Test
	public void testPredictingRedShell() {
		testEnemy(EnemyType.RED_SHELL, false);
	}
	@Test
	public void testPredictingRedShellFromCopy() {
		testEnemy(EnemyType.RED_SHELL, true);
	}
	
	@Test
	public void testPredictingGreenShell() {
		testEnemy(EnemyType.GREEN_SHELL, false);
	}
	@Test
	public void testPredictingGreenShellFromCopy() {
		testEnemy(EnemyType.GREEN_SHELL, true);
	}*/
	
	@Test
	public void testPredictingBulletBill() {
		testEnemy(EnemyType.BULLET_BILL, false);
	}
	@Test
	public void testPredictingBulletBillFromCopy() {
		testEnemy(EnemyType.BULLET_BILL, true);
	}
	
	private void testEnemy(EnemyType enemyType, boolean makeCopy) {
		testEnemyOnLevel("testEnemyPredictor/plainbox.lvl", enemyType, makeCopy);
		testEnemyOnLevel("testEnemyPredictor/bumpybox.lvl", enemyType, makeCopy);
	}
	
	private void testEnemyOnLevel(String levelPath, EnemyType enemyType, boolean makeCopy) {
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		Environment observation = TestTools.loadLevel(levelPath, new UnitTestAgent(), false);
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		TestTools.setMarioInvulnerability(observation, true);
		TestTools.setMarioXPosition(observation, 11);
		TestTools.runOneTick(observation);
		
		testEnemy(observation, enemyPredictor, enemyType, makeCopy);
	}
	
	private void testEnemy(Environment observation, EnemyPredictor enemyPredictor, EnemyType enemyType, boolean makeCopy) {
		final ArrayList<Sprite> enemies = new ArrayList<Sprite>();
		
		enemies.add(TestTools.spawnEnemy(observation, 2, 2, 1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 1, 80, enemyType.name(), makeCopy);
		enemies.clear();
		
		enemies.add(TestTools.spawnEnemy(observation, 20, 2, -1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 1, 80, enemyType.name(), makeCopy);
		enemies.clear();
		
		enemies.add(TestTools.spawnEnemy(observation, 2, 2, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 2, 3, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 3, 4, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 4, 5, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 5, 2, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 6, 3, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 7, 4, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 8, 5, 1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 8, 80, enemyType.name(), makeCopy);
		enemies.clear();
		
		enemies.add(TestTools.spawnEnemy(observation, 20, 2, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 20, 3, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 19, 4, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 18, 5, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 17, 2, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 16, 3, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 15, 4, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 14, 5, -1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 8, 80, enemyType.name(), makeCopy);
		enemies.clear();
		
		enemies.add(TestTools.spawnEnemy(observation, 2, 2, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 2, 3, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 3, 4, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 4, 5, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 5, 2, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 6, 3, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 7, 4, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 8, 5, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 20, 2, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 20, 3, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 19, 4, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 18, 5, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 17, 2, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 16, 3, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 15, 4, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 14, 5, -1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 16, 80, enemyType.name(), makeCopy);
		enemies.clear();
	}
	
	private void testNoRemovalOfEnemyAndPrediction(Environment observation, EnemyPredictor enemyPredictor, ArrayList<Sprite> enemies, int enemyCount, int testTime, String enemyName, boolean makeCopy) {
		enemyPredictor = findEnemies(observation, enemyPredictor, makeCopy);
		
		assertEquals("Didn't find " + enemyName + " after the first 3 ticks", enemyCount, enemyPredictor.getEnemies().size());
		
		final ArrayList<Point2D.Float[]> predictedPositions = new ArrayList<Point2D.Float[]>();
		for (EnemySimulator enemy : enemyPredictor.getEnemies()) {
			final Point2D.Float[] enemyPredictedPositions = new Point2D.Float[testTime];
			
			for (int j = 0; j < testTime; j++) {
				enemyPredictedPositions[j] = enemy.getPositionAtTime(j + 1);
			}
			
			predictedPositions.add(enemyPredictedPositions);
		}
		
		for (int i = 0; i < 80; i++) {
			TestTools.runOneTick(observation);
			final float[] enemyArray = observation.getEnemiesFloatPos();
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());


			final int currentEnemyCount = enemyArray.length / EnemyPredictor.FLOATS_PER_ENEMY;
			
			assertEquals("Lost " + enemyName + " after " + i + " ticks", currentEnemyCount, enemyPredictor.getEnemies().size());
			
			if (enemyArray.length == 0 && enemyPredictor.getEnemies().size() == 0) {
				break;
			}
			
			final int enemiesLeftToFind = getMissingEnemies(predictedPositions, currentEnemyCount, enemyArray, i);
			
			if (enemiesLeftToFind > 0) {
				Assert.fail("Enemy simulator position didn't match any enemy position");
			}
		}
		
		enemies.forEach(x -> TestTools.removeEnemy(observation, x));
	}
	
	private int getMissingEnemies(ArrayList<Point2D.Float[]> predictedPositions, int enemiesLeftToFind, float[] enemyArray, int time) {
		for (int q = 0; q < predictedPositions.size(); q++) {
			final Point2D.Float[] simulatedEnemyPositions = predictedPositions.get(q);
			
			if (enemiesLeftToFind == 0) {
				break;
			}
			
			for (int j = 0; j < enemyArray.length; j += EnemyPredictor.FLOATS_PER_ENEMY) {
				final float deltaX = Math.abs(simulatedEnemyPositions[time].x - enemyArray[j + EnemyPredictor.X_OFFSET]);
				final float deltaY = Math.abs(simulatedEnemyPositions[time].y - enemyArray[j + EnemyPredictor.Y_OFFSET]);
				
				if (deltaX < EnemyPredictor.ACCEPTED_POSITION_DEVIATION && 
					deltaY < EnemyPredictor.ACCEPTED_POSITION_DEVIATION) {
					enemiesLeftToFind--;
					break;
				}
			}
		}
		
		return enemiesLeftToFind;
	}
	
	private EnemyPredictor findEnemies(Environment observation, EnemyPredictor enemyPredictor, boolean makeCopy) {
		for (int i = 0; i < 3; i++) {
			TestTools.runOneTick(observation);
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		}
		
		final EnemyPredictor copy = new EnemyPredictor();
		copy.intialize(((MarioComponent)observation).getLevelScene());
		copy.syncFrom(enemyPredictor);
		
		return (makeCopy) ? copy : enemyPredictor;
	}
	
}
