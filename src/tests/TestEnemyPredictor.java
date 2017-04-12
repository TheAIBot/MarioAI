package tests;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Test;

import MarioAI.enemy.EnemyPredictor;
import MarioAI.enemy.EnemyType;
import MarioAI.enemy.simulators.EnemySimulator;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;

public class TestEnemyPredictor {
	
	@Test
	public void testPredictingRedKoopa() {
		testWalkingEnemy(EnemyType.RED_KOOPA);
	}
	
	@Test
	public void testPredictingRedKoopaWinged() {
		testWalkingEnemy(EnemyType.RED_KOOPA.asWinged());
	}
	
	@Test
	public void testPredictingGreenKoopa() {
		testWalkingEnemy(EnemyType.GREEN_KOOPA);
	}
	
	@Test
	public void testPredictingGreenKoopaWinged() {
		testWalkingEnemy(EnemyType.GREEN_KOOPA.asWinged());
	}
	
	@Test
	public void testPredictingGoomba() {
		testWalkingEnemy(EnemyType.GOOMBA);
	}
	
	@Test
	public void testPredictingGoombaWinged() {
		testWalkingEnemy(EnemyType.GOOMBA.asWinged());
	}
	
	@Test
	public void testPredictingSpiky() {
		testWalkingEnemy(EnemyType.SPIKY);
	}
	
	@Test
	public void testPredictingSpikyWinged() {
		testWalkingEnemy(EnemyType.SPIKY.asWinged());
	}
	
	private void testWalkingEnemy(EnemyType enemyType) {
		testWalkingEnemyOnLevel("plainbox.lvl", enemyType);
		testWalkingEnemyOnLevel("bumpybox.lvl", enemyType);
	}
	
	private void testWalkingEnemyOnLevel(String levelPath, EnemyType enemyType) {
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		Environment observation = TestTools.loadLevel(levelPath, new UnitTestAgent(), false);
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		TestTools.setMarioInvulnerability(observation, true);
		
		testWalkingEnemy(observation, enemyPredictor, enemyType);
	}
	
	private void testWalkingEnemy(Environment observation, EnemyPredictor enemyPredictor, EnemyType enemyType) {
		ArrayList<Sprite> enemies = new ArrayList<Sprite>();
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 2, 1, 1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 1, 80, enemyType.name());
		enemies.clear();
		
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 20, 1, -1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 1, 80, enemyType.name());
		enemies.clear();
		
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 2, 1, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 2, 2, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 3, 3, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 4, 4, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 5, 1, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 6, 2, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 7, 3, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 8, 4, 1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 8, 80, enemyType.name());
		enemies.clear();
		
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 20, 1, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 20, 2, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 19, 3, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 18, 4, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 17, 1, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 16, 2, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 15, 3, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 14, 4, -1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 8, 80, enemyType.name());
		enemies.clear();
		
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 2, 1, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 2, 2, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 3, 3, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 4, 4, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 5, 1, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 6, 2, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 7, 3, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 8, 4, 1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 20, 1, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 20, 2, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 19, 3, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 18, 4, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 17, 1, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 16, 2, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 15, 3, -1, enemyType));
		enemies.add(TestTools.SpawnWalkingEnemy(observation, 14, 4, -1, enemyType));
		testNoRemovalOfEnemyAndPrediction(observation, enemyPredictor, enemies, 16, 80, enemyType.name());
		enemies.clear();
	}
	
	@Test
	public void testPredictingBulletBill() {
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		Environment observation = TestTools.loadLevel("flat.lvl", new UnitTestAgent(), false);
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		TestTools.setMarioInvulnerability(observation, true);
		
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
		assertEquals("Didn't find " + enemyName + " after the first 3 ticks", enemyCount, enemyPredictor.getEnemies().size());
		
		/*
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
		*/
		
		for (int i = 0; i < 80; i++) {
			TestTools.runOneTick(observation);
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
			assertEquals("Lost " + enemyName + " after " + i + " ticks", enemyCount, enemyPredictor.getEnemies().size());
		}
		
		enemies.forEach(x -> TestTools.removeEnemy(observation, x));
	}
}
