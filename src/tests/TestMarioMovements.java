package tests;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.MarioControls;
import MarioAI.MarioMethods;
import MarioAI.MovementInformation;
import MarioAI.graph.edges.SecondOrderPolynomial;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestMarioMovements {
	
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
		float actualSpeed = 0;
		for (int i = 1; i <= speed; i++) {
			actualSpeed = MarioControls.getNextTickSpeed(actualSpeed);
			distanceMoved += actualSpeed;
		}
		
		float expectedXPos = startMarioXPos + distanceMoved;
		float expectedYPos = startMarioYPos;
		
		if (!withinAcceptableError(endMarioXPos, endMarioYPos, expectedXPos, expectedYPos)) {
			Assert.fail("Mario Wasn't close enough to the expected position\nspeed: " + speed + 
					"\nx: " + Math.abs(endMarioXPos - expectedXPos) + 
					"\ny: " + Math.abs(endMarioYPos - expectedYPos));
		}
		if (MarioControls.getXMovementTime(endMarioXPos - startMarioXPos, 0, 0).ticks != speed) {
			Assert.fail("Expected steps didn't match correct steps." + 
					"\nExpected: " + speed + 
					"\nReceived: " + MarioControls.getXMovementTime(endMarioXPos - startMarioXPos, 0, 0));
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
		float actualSpeed = 0;
		for (int i = 1; i <= speed; i++) {
			actualSpeed = -MarioControls.getNextTickSpeed(-actualSpeed);
			distanceMoved += actualSpeed;
		}
		
		float expectedXPos = startMarioXPos + distanceMoved;
		float expectedYPos = startMarioYPos;
		
		if (!withinAcceptableError(endMarioXPos, endMarioYPos, expectedXPos, expectedYPos)) {
			Assert.fail("Mario Wasn't close enough to the expected position\nspeed: " + speed + 
					"\nx: " + Math.abs(endMarioXPos - expectedXPos) + 
					"\ny: " + Math.abs(endMarioYPos - expectedYPos));
		}
		if (MarioControls.getXMovementTime(endMarioXPos - startMarioXPos, 0, 0).ticks != speed) {
			Assert.fail("Expected steps didn't match correct steps." + 
					"\nExpected: " + speed + 
					"\nReceived: " + MarioControls.getXMovementTime(endMarioXPos - startMarioXPos, 0, 0));
		}
	}
	
	@Test
	public void testDrifting() {
		int[] speeds = new int[] {1, 2, 5, 10, 20, 30, 40, 50};
		
		for (int i = 0; i < speeds.length; i++) {
			testDriftSpeed(speeds[i], 1);
			testDriftSpeed(speeds[i], 2);
			testDriftSpeed(speeds[i], 3);
			testDriftSpeed(speeds[i], 5);
			testDriftSpeed(speeds[i], 10);
			testDriftSpeed(speeds[i], 20);
			testDriftSpeed(speeds[i], 30);
			testDriftSpeed(speeds[i], 40);
			testDriftSpeed(speeds[i], 50);
		}
	}
	private void testDriftSpeed(int speed, int driftTime) {
		final UnitTestAgent agent = new UnitTestAgent();		
		Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		agent.action[Mario.KEY_RIGHT] = true;
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		for (int i = 0; i < speed; i++) {
			TestTools.runOneTick(observation);
		}
		agent.action[Mario.KEY_RIGHT] = false;
		for (int i = 0; i < driftTime; i++) {
			TestTools.runOneTick(observation);
		}
		final float endMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float endMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		float distanceMoved = 0;
		float actualSpeed = 0;
		for (int i = 1; i <= speed; i++) {
			actualSpeed = MarioControls.getNextTickSpeed(actualSpeed);
			distanceMoved += actualSpeed;
		}
		distanceMoved += MarioControls.getDriftingDistance(actualSpeed, driftTime).key.floatValue();
		
		float expectedXPos = startMarioXPos + distanceMoved;
		float expectedYPos = startMarioYPos;
		
		if (!withinAcceptableError(endMarioXPos, endMarioYPos, expectedXPos, expectedYPos)) {
			Assert.fail("Mario Wasn't close enough to the expected position." + 
					"\nspeed: " + speed + 
					"\ndrift: " + driftTime + 
					"\nxDiff: " + Math.abs(endMarioXPos - expectedXPos) + 
					"\nyDiff: " + Math.abs(endMarioYPos - expectedYPos));
		}
	}
	
	@Test
	public void testJumps() {
		for (int i = 6; i >= 0; i--) {
			testJumpTime(1     , i);
			testJumpTime(1.5f  , i);
			testJumpTime(1.645f, i);
			testJumpTime(3.4f  , i);
			testJumpTime(4     , i);
			testJumpTime(5.6f  , i);
		}
		
		testJumpTime(1.5f, -1);
		testJumpTime(3.4f, -1);
		testJumpTime(5.6f, -1);
		
		testJumpTime(3.4f, -2);
		testJumpTime(5.6f, -2);
		
		testJumpTime(3.4f, -3);
		testJumpTime(5.6f, -3);
		
		testJumpTime(4.0f, -4);
		testJumpTime(5.6f, -4);
	}
	private void testJumpTime(float jumpHeight, int heightDifference) {
		final UnitTestAgent agent = new UnitTestAgent();		
		String levelPath = "jumpLevels/jumpDownLevels/jumpDown" + heightDifference + ".lvl";
		Environment observation = TestTools.loadLevel(levelPath, agent, false);
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		boolean upTime = true;
		int expectedJumpTime = 0;
		int expectedTicksHeldUp = 0;
		agent.action[Mario.KEY_RIGHT] = true;
		while (true) {
			final float currentMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			if (startMarioYPos - jumpHeight < currentMarioYPos && upTime) {
				agent.action[Mario.KEY_JUMP] = true;
				expectedTicksHeldUp++;
			}
			else {
				agent.action[Mario.KEY_JUMP] = false;
				upTime = false;
			}
			TestTools.runOneTick(observation);
			if (observation.isMarioOnGround()) {
				break;
			}
			expectedJumpTime++;
		}
		//can't hold jump for more than 8 ticks
		expectedTicksHeldUp = Math.min(expectedTicksHeldUp, 8);
		
		final float endMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float endMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final Node startNode = new Node((short)startMarioXPos, (short)Math.round(startMarioYPos), (byte)0);
		final Node endNode   = new Node((short)endMarioXPos  , (short)Math.round(endMarioYPos)  , (byte)0);
		SecondOrderPolynomial edge = new SecondOrderPolynomial(startNode, endNode);
		
		edge.setTopPoint(0, Math.round(startMarioYPos) + jumpHeight);
		MovementInformation moveInfo = MarioControls.getStepsAndSpeedAfterJump(edge, 0);
		
		final int receivedJumpTime = moveInfo.getTotalTicksJumped();
		final int receivedHoldJump = moveInfo.getTicksHoldingJump();
		if (receivedJumpTime != expectedJumpTime ||
			receivedHoldJump != expectedTicksHeldUp) {
			Assert.fail("Expected jump time wasn't the same as the received one." + 
						"\nExpected jump time: " + expectedJumpTime + 
						"\nReceived jump time: " + receivedJumpTime + 
						"\nExpected hold jump: " + expectedTicksHeldUp + 
						"\nReceived hold jump: " + receivedHoldJump + 
						"\nJump height: " + jumpHeight + 
						"\npath: " + levelPath);
		}
	}
	
	private boolean withinAcceptableError(float x1, float y1, float x2, float y2) {
		return 	Math.abs(x1 - x2) <= MarioControls.ACCEPTED_DEVIATION && 
				Math.abs(y1 - y2) <= MarioControls.ACCEPTED_DEVIATION;
	}
}