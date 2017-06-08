package tests;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.EnemyType;
import MarioAI.enemySimuation.simulators.EnemySimulator;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;

public class TestEnemyPredictor {
	
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
	
	@Test
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
	}
	
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
			
			int enemiesLeftToFind = currentEnemyCount;
			for (int q = 0; q < predictedPositions.size(); q++) {
				final Point2D.Float[] simulatedEnemyPositions = predictedPositions.get(q);
				
				if (enemiesLeftToFind == 0) {
					break;
				}
				
				for (int j = 0; j < enemyArray.length; j += EnemyPredictor.FLOATS_PER_ENEMY) {
					final float deltaX = Math.abs(simulatedEnemyPositions[i].x - enemyArray[j + EnemyPredictor.X_OFFSET]);
					final float deltaY = Math.abs(simulatedEnemyPositions[i].y - enemyArray[j + EnemyPredictor.Y_OFFSET]);
					
					if (deltaX < EnemyPredictor.ACCEPTED_POSITION_DEVIATION && 
						deltaY < EnemyPredictor.ACCEPTED_POSITION_DEVIATION) {
						enemiesLeftToFind--;
						break;
					}
				}
			}
			
			if (enemiesLeftToFind > 0) {
				Assert.fail("Enemy simulator position didn't match any enemy position");
			}
		}
		
		enemies.forEach(x -> TestTools.removeEnemy(observation, x));
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
	
	@Test
	public void asd() {
		fisk(1, 2, false, EnemyType.GOOMBA);
	}
	
	private void fisk(int xDistance, int jumpHeight, boolean useSuperSpeed, EnemyType enemyType) {
		final UnitTestAgent agent = new UnitTestAgent();
		final Environment observation = TestTools.loadLevel("testEnemyPredictor/flat.lvl", agent, true);
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final EnemyPredictor enemyPredictor = new EnemyPredictor();
		world.initialize(observation);
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		

		final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, xDistance, jumpHeight, -jumpHeight, 1, world, useSuperSpeed);
		
		testJumpRight(observation, agent, world, marioControls, enemyPredictor, path, enemyType, xDistance);
	}
	
	private void testJumpRight(Environment observation, UnitTestAgent agent, World world, MarioControls marioControls, EnemyPredictor enemyPredictor, ArrayList<DirectedEdge> path, EnemyType enemyType, int xDistance) {
		
		final int startXPixel = (4 - xDistance) * World.PIXELS_PER_BLOCK;
		final int endXPixel = 7 * World.PIXELS_PER_BLOCK;
		
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		for (int i = startXPixel; i <= endXPixel; i++) {
			TestTools.setMarioPixelPosition(observation, i, Math.round(startMarioYPos * World.PIXELS_PER_BLOCK));
			TestTools.resetMarioSpeed(observation);
			final Sprite enemy = TestTools.spawnEnemy(observation, (startXPixel / 16) + 4, (int)startMarioYPos, -1, enemyType);
			for (int j = 0; j < 3; j++) {
				TestTools.runOneTick(observation);
				enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
			}
			
			TestTools.runOneTick(observation);
			TestTools.runOneTick(observation);
			world.update(observation);
			marioControls.reset();
			
			final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			final ArrayList<DirectedEdge> pathCopy = new ArrayList<DirectedEdge>();
			pathCopy.add(path.get(0));
			
			boolean expectedToHitSomething = false;
			for (int j = 0; j < pathCopy.get(0).getMoveInfo().getMoveTime(); j++) {
				final Point2D.Float currentOffset = pathCopy.get(0).getMoveInfo().getPositions()[j];
				expectedToHitSomething = enemyPredictor.hasEnemy(marioXPos + currentOffset.x, marioYPos + currentOffset.y, MarioMethods.MARIO_WIDTH, 2, j + 1);
				
				if (expectedToHitSomething) {
					break;
				}
			}
			
			final boolean actualHitSomething = isHittingEnemyOnRoad(observation, pathCopy, agent, marioControls);
			
			assertEquals(expectedToHitSomething, actualHitSomething);
			
			TestTools.removeEnemy(observation, enemy);
		}
	}
	
	private boolean isHittingEnemyOnRoad(Environment observation, ArrayList<DirectedEdge> path, UnitTestAgent agent, MarioControls marioControls) {
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		agent.action = marioControls.getActions();
		TestTools.runOneTick(observation);
	
		final MovementInformation moveInfo = path.get(0).getMoveInfo();
		for (int i = 0; i < moveInfo.getPositions().length; i++) {	
			marioControls.getNextAction(observation, path);
			TestTools.runOneTick(observation);
			
			final Point2D.Float position = moveInfo.getPositions()[i];
			
			final float expectedMarioXPos = startMarioXPos + position.x;
			final float expectedMarioYPos = startMarioYPos - position.y;
			
			final float actualMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			final float actualMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			if (!withinAcceptableError(expectedMarioXPos, actualMarioXPos, actualMarioYPos, expectedMarioYPos) ||
				TestTools.getMarioInvulnerableTime(observation) > 0) {
				Arrays.fill(agent.action, false);
				return false;
			}
		}
		Arrays.fill(agent.action, false);
		return true;
	}
	
	private boolean withinAcceptableError(float a1, float b1, float a2, float b2) {
		return 	withinAcceptableError(a1, b1) && 
				withinAcceptableError(a2, b2);
	}
	
	private boolean withinAcceptableError(float a, float b) {
		return 	Math.abs(a - b) <= MarioControls.ACCEPTED_DEVIATION;
	}
}
