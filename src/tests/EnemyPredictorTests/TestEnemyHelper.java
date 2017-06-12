package tests.EnemyPredictorTests;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.EnemyType;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;
import tests.TestTools;
import tests.UnitTestAgent;

class TestEnemyHelper {
	public static EnemyPredictor findEnemies(Environment observation, EnemyPredictor enemyPredictor, boolean makeCopy) {
		for (int i = 0; i < 3; i++) {
			TestTools.runOneTick(observation);
			enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		}
		
		final EnemyPredictor copy = new EnemyPredictor();
		copy.intialize(((MarioComponent)observation).getLevelScene());
		copy.syncFrom(enemyPredictor);
		
		return (makeCopy) ? copy : enemyPredictor;
	}
	
	public static void testPathRight(Environment observation, UnitTestAgent agent, World world, MarioControls marioControls, EnemyPredictor enemyPredictor, ArrayList<DirectedEdge> path, EnemyType enemyType, int xDistance, boolean makeCopy) {
		
		final int startXPixel = (4 - xDistance) * World.PIXELS_PER_BLOCK;
		final int endXPixel = 8 * World.PIXELS_PER_BLOCK;
		
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		for (int i = startXPixel; i <= endXPixel; i++) {
			final boolean hitEnemy = initVerifySimulationToGame(observation, agent, world, 
																marioControls, enemyPredictor, path, 
																enemyType, xDistance, makeCopy, 
																i, startMarioYPos, (startXPixel / 16) + 7, -1);
		
			if (hitEnemy) {
				break;
			}
		}
	}
	
	public static void testPathLeft(Environment observation, UnitTestAgent agent, World world, MarioControls marioControls, EnemyPredictor enemyPredictor, ArrayList<DirectedEdge> path, EnemyType enemyType, int xDistance, boolean makeCopy) {
		
		final int startXPixel = (8 + xDistance) * World.PIXELS_PER_BLOCK;
		final int endXPixel = 4 * World.PIXELS_PER_BLOCK;
		
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		for (int i = startXPixel; i >= endXPixel; i--) {
			final boolean hitEnemy = initVerifySimulationToGame(observation, agent, world, 
																marioControls, enemyPredictor, path, 
																enemyType, xDistance, makeCopy, 
																i, startMarioYPos, (startXPixel / 16) - 7, 1);
		
			if (hitEnemy) {
				break;
			}
		}
	}
	
	private static boolean initVerifySimulationToGame(Environment observation, UnitTestAgent agent, World world, 
													  MarioControls marioControls, EnemyPredictor enemyPredictor, ArrayList<DirectedEdge> path, 
													  EnemyType enemyType, int xDistance, boolean makeCopy, 
													  int i, float startMarioYPos, int enemySpawnXPos, int enemyDirection) {
		TestTools.resetMarioHealth(observation);
		TestTools.setMarioPixelPosition(observation, i, Math.round(startMarioYPos * World.PIXELS_PER_BLOCK));
		TestTools.resetMarioSpeed(observation);
		final Sprite enemy = TestTools.spawnEnemy(observation, enemySpawnXPos, (int)startMarioYPos, enemyDirection, enemyType);
		final EnemyPredictor potentialEnemyPredictorCopy = TestEnemyHelper.findEnemies(observation, enemyPredictor, makeCopy);
		
		assertEquals(1, potentialEnemyPredictorCopy.getEnemies().size());
		
		world.update(observation);
		marioControls.reset();
		
		final boolean hitEnemy = verifySimulationToGame(observation, agent, world, marioControls, potentialEnemyPredictorCopy, path, i);
		
		TestTools.removeEnemy(observation, enemy);
		TestTools.setMarioPixelPosition(observation, i, Math.round(startMarioYPos * World.PIXELS_PER_BLOCK));
		TestTools.runOneTick(observation);
		
		return hitEnemy;
	}
	
	private static boolean verifySimulationToGame(Environment observation, UnitTestAgent agent, World world, MarioControls marioControls, EnemyPredictor potentialEnemyPredictorCopy, ArrayList<DirectedEdge> path, int i) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> pathCopy = new ArrayList<DirectedEdge>();
		pathCopy.add(path.get(0));
		
		if (i == 48) {
			System.out.println();
		}
		
		final ArrayList<Point2D.Float> enemyPositions = new ArrayList<Point2D.Float>();
		final boolean actualHitSomething = isHittingEnemyOnPath(observation, pathCopy, agent, marioControls, enemyPositions);
		
		if (actualHitSomething) {
			System.out.println();
		}
		
		boolean expectedToHitSomething = false;
		for (int j = 0; j < path.get(0).getMoveInfo().getMoveTime(); j++) {				
			final float expectedMarioXPos = marioXPos + path.get(0).getMoveInfo().getXPositions()[j];
			final float expectedMarioYPos = marioYPos - path.get(0).getMoveInfo().getYPositions()[j];
			expectedToHitSomething = potentialEnemyPredictorCopy.hasEnemy(expectedMarioXPos, expectedMarioYPos, MarioMethods.getMarioHeightFromMarioMode(2), j + 1);
			
			if (expectedToHitSomething) {
				break;
			}
		}
		
		if (expectedToHitSomething != actualHitSomething) {
			System.out.println();
		}
		
		assertEquals(expectedToHitSomething, actualHitSomething);
		
		return expectedToHitSomething && actualHitSomething;
	}
	
	private static boolean isHittingEnemyOnPath(Environment observation, ArrayList<DirectedEdge> path, UnitTestAgent agent, MarioControls marioControls, ArrayList<Point2D.Float> enemyPositions) {
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		agent.action = marioControls.getActions();
		TestTools.runOneTick(observation);
	
		final MovementInformation moveInfo = path.get(0).getMoveInfo();
		for (int i = 0; i < moveInfo.getMoveTime(); i++) {	
			final float[] enemyArray = observation.getEnemiesFloatPos();
			enemyPositions.add(new Point2D.Float(enemyArray[EnemyPredictor.X_OFFSET], enemyArray[EnemyPredictor.Y_OFFSET]));
			
			marioControls.getNextAction(observation, path);
			TestTools.runOneTick(observation);
			/*
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/	
			
			final float expectedMarioXPos = startMarioXPos + moveInfo.getXPositions()[i];
			final float expectedMarioYPos = startMarioYPos - moveInfo.getYPositions()[i];
			
			final float actualMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			final float actualMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			final int marioInvulnerabilityTime = TestTools.getMarioInvulnerableTime(observation);
			
			if (!withinAcceptableError(expectedMarioXPos, actualMarioXPos, actualMarioYPos, expectedMarioYPos) ||
				marioInvulnerabilityTime > 0) {
				if (marioInvulnerabilityTime == 0) {
					System.out.println();
				}
				Arrays.fill(agent.action, false);
				return true;
			}
		}
		Arrays.fill(agent.action, false);
		return false;
	}
	
	private static boolean withinAcceptableError(float a1, float b1, float a2, float b2) {
		return 	withinAcceptableError(a1, b1) && 
				withinAcceptableError(a2, b2);
	}
	
	private static boolean withinAcceptableError(float a, float b) {
		return 	Math.abs(a - b) <= MarioControls.ACCEPTED_DEVIATION;
	}

}
