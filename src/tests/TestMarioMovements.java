package tests;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.MarioControls;
import MarioAI.MarioMethods;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestMarioMovements {
	
	private static final float ACCEPTED_DEVIATION = 0.0001f;
	
	@Test
	public void testRightMovement() {
		testRightSpeed(1);
		testRightSpeed(2);
		testRightSpeed(5);
		testRightSpeed(10);
		testRightSpeed(20);
		testRightSpeed(30);
		testRightSpeed(40);
		testRightSpeed(50);
	}
	private void testRightSpeed(int speed) {
		final UnitTestAgent agent = new UnitTestAgent();		
		Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		agent.action[Mario.KEY_RIGHT] = true;
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		for (int i = 0; i < speed; i++) {
			TestTools.runOneTick(observation);
		}
		final float endMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float endMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		float distanceMoved = 0;
		for (int i = 1; i <= speed; i++) {
			distanceMoved += MarioControls.getDistanceFromSpeedInt(i);
		}
		
		float expectedXPos = startMarioXPos + distanceMoved;
		float expectedYPos = startMarioYPos;
		
		if (!withinAcceptableError(endMarioXPos, endMarioYPos, expectedXPos, expectedYPos)) {
			Assert.fail("Mario Wasn't close enough to the expected position\nspeed: " + speed + "x: " + Math.abs(endMarioXPos - expectedXPos) + "\ny: " + Math.abs(endMarioYPos - expectedYPos));
		}
	}
	
	@Test
	public void testLeftMovement() {
		testLeftSpeed(1);
		testLeftSpeed(2);
		testLeftSpeed(5);
		testLeftSpeed(10);
		testLeftSpeed(20);
		testLeftSpeed(30);
		testLeftSpeed(40);
		testLeftSpeed(50);
	}
	private void testLeftSpeed(int speed) {
		final UnitTestAgent agent = new UnitTestAgent();		
		Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		//first move mario right so he doesn't hit the wall when going left
		agent.action[Mario.KEY_RIGHT] = true;
		for (int i = 0; i < speed; i++) {
			TestTools.runOneTick(observation);
		}
		
		//then just wait a few ticks for mario to completely stop
		agent.action[Mario.KEY_RIGHT] = false;
		for (int i = 0; i < 50; i++) {
			TestTools.runOneTick(observation);
		}
	
		//now run the actual test
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		agent.action[Mario.KEY_LEFT] = true;
		for (int i = 0; i < speed; i++) {
			TestTools.runOneTick(observation);
		}
		final float endMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float endMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		float distanceMoved = 0;
		for (int i = 1; i <= speed; i++) {
			distanceMoved -= MarioControls.getDistanceFromSpeedInt(i);
		}
		
		float expectedXPos = startMarioXPos + distanceMoved;
		float expectedYPos = startMarioYPos;
		
		if (!withinAcceptableError(endMarioXPos, endMarioYPos, expectedXPos, expectedYPos)) {
			Assert.fail("Mario Wasn't close enough to the expected position\nspeed: " + speed + "\nx: " + Math.abs(endMarioXPos - expectedXPos) + "\ny: " + Math.abs(endMarioYPos - expectedYPos));
		}
	}
	
	private boolean withinAcceptableError(float x1, float y1, float x2, float y2) {
		return 	Math.abs(x1 - x2) <= ACCEPTED_DEVIATION && 
				Math.abs(y1 - y2) <= ACCEPTED_DEVIATION;
	}
}