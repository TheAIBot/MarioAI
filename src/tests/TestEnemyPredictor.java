package tests;

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

public class TestEnemyPredictor {
	
	@Test
	public void testPredictingRedKoopa() {
		testEnemy(EnemyType.RED_KOOPA);
	}
	
	@Test
	public void testPredictingRedKoopaWinged() {
		testEnemy(EnemyType.RED_KOOPA.asWinged());
	}
	
	@Test
	public void testPredictingGreenKoopa() {
		testEnemy(EnemyType.GREEN_KOOPA);
	}
	
	@Test
	public void testPredictingGreenKoopaWinged() {
		testEnemy(EnemyType.GREEN_KOOPA.asWinged());
	}
	
	@Test
	public void testPredictingGoomba() {
		testEnemy(EnemyType.GOOMBA);
	}
	
	@Test
	public void testPredictingGoombaWinged() {
		testEnemy(EnemyType.GOOMBA.asWinged());
	}
	
	@Test
	public void testPredictingSpiky() {
		testEnemy(EnemyType.SPIKY);
	}
	
	@Test
	public void testPredictingSpikyWinged() {
		testEnemy(EnemyType.SPIKY.asWinged());
	}
	
	@Test
	public void testPredictingFlower() {
		testEnemy(EnemyType.FLOWER);
	}
	
	@Test
	public void testPredictingRedShell() {
		testEnemy(EnemyType.RED_SHELL);
	}
	
	@Test
	public void testPredictingGreenShell() {
		testEnemy(EnemyType.GREEN_SHELL);
	}
	
	@Test
	public void testPredictingBulletBill() {
		testEnemy(EnemyType.BULLET_BILL);
	}
	
	private void testEnemy(EnemyType enemyType) {
		testEnemyOnLevel("plainbox.lvl", enemyType);
		//testEnemyOnLevel("plainboxV2.lvl", enemyType);
		//testEnemyOnLevel("plainboxV3.lvl", enemyType);
		//testEnemyOnLevel("bumpybox.lvl", enemyType);
	}
	
	private void testEnemyOnLevel(String levelPath, EnemyType enemyType) {
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		Environment observation = TestTools.loadLevel(levelPath, new UnitTestAgent(), false);
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		TestTools.setMarioInvulnerability(observation, true);
		TestTools.setMarioXPosition(observation, 11);
		TestTools.runOneTick(observation);
		
		testEnemy(observation, enemyPredictor, enemyType);
	}
	
	private void testEnemy(Environment observation, EnemyPredictor enemyPredictor, EnemyType enemyType) {
		ArrayList<Sprite> enemies = new ArrayList<Sprite>();
//		enemies.add(TestTools.spawnEnemy(observation, 2, 2, 1, enemyType));
//		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 1, 80, enemyType.name());
//		enemies.clear();
//		
//		enemies.add(TestTools.spawnEnemy(observation, 20, 2, -1, enemyType));
//		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 1, 80, enemyType.name());
//		enemies.clear();
//
//		enemies.add(TestTools.spawnEnemy(observation, 4, 4, 1, enemyType));
//		enemies.add(TestTools.spawnEnemy(observation, 8, 5, 1, enemyType));
//		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 2, 80, enemyType.name());
//		enemies.clear();
		
		enemies.add(TestTools.spawnEnemy(observation, 2, 2, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 3, 3, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 4, 4, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 5, 5, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 6, 6, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 7, 7, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 8, 8, 1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 9, 9, 1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 8, 80, enemyType.name());
		enemies.clear();
		
		enemies.add(TestTools.spawnEnemy(observation, 20, 2, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 20, 3, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 19, 4, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 18, 5, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 17, 2, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 16, 3, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 15, 4, -1, enemyType));
		enemies.add(TestTools.spawnEnemy(observation, 14, 5, -1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 8, 80, enemyType.name());
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
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 16, 80, enemyType.name());
		enemies.clear();
	}
	
	private void testNoRemovalOfEnemyAndPrediction(Environment observation, EnemyPredictor enemyPredictor, ArrayList<Sprite> enemies, int enemyCount, int testTime, String enemyName) {
		for (int i = 0; i < 3; i++) {
			TestTools.runOneTick(observation);
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		}
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
			if (i==15) {
				System.out.println("i=" + i);
			}

			TestTools.runOneTick(observation);
			final float[] enemyArray = observation.getEnemiesFloatPos();
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());


			final int currentEnemyCount = enemyArray.length / EnemyPredictor.FLOATS_PER_ENEMY;
			
			if (enemyArray.length == 0) {
				System.out.println("ENEMYARRAY HAR LÆNGDE 0");
			}
			if (currentEnemyCount != enemyPredictor.getEnemies().size()) {
				System.out.println("asd");
			}
			assertEquals("Lost " + enemyName + " after " + i + " ticks", currentEnemyCount, enemyPredictor.getEnemies().size());
			
			int removedEnemies = currentEnemyCount - enemyPredictor.getEnemies().size();
			if (enemyArray.length == 0 && removedEnemies == 0) {
				break;
			}
			int enemiesLeftToFind = enemyArray.length / 3;
			for (int q = 0; q < predictedPositions.size(); q++) {
				Point2D.Float[] simulatedEnemyPositions = predictedPositions.get(q);
				if (enemiesLeftToFind == 0) {
					break;
				}
				boolean foundEnemyPosition = false;
				for (int j = 0; j < enemyArray.length; j += EnemyPredictor.FLOATS_PER_ENEMY) {
					final float deltaX = Math.abs(simulatedEnemyPositions[i].x - enemyArray[j + EnemyPredictor.X_OFFSET]);
					final float deltaY = Math.abs(simulatedEnemyPositions[i].y - enemyArray[j + EnemyPredictor.Y_OFFSET]);
					
					if (deltaX < EnemyPredictor.ACCEPTED_POSITION_DEVIATION && 
						deltaY < EnemyPredictor.ACCEPTED_POSITION_DEVIATION) {
						foundEnemyPosition = true;
						break;
					} else {
						System.out.println();
					}
				}

				if (!foundEnemyPosition) {
					/*if (removedEnemies > 0) {
						removedEnemies--;
						enemiesLeftToFind--;
					}*/
				}
				else {
					enemiesLeftToFind--;
				}
			}
			if (enemiesLeftToFind > 0) {
				Assert.fail("Enemy simulator position didn't match any enemy position");	
				
			}
		}
		
		enemies.forEach(x -> TestTools.removeEnemy(observation, x));
	}
}
