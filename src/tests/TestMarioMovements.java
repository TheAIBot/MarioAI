package tests;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.crypto.interfaces.PBEKey;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.MarioMethods;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.edges.SecondOrderPolynomial;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestMarioMovements {
	
	@Test
	public void testRightMovement() {
		testRightSpeed(1);
		testRightSpeed(2);
		testRightSpeed(5);
		testRightSpeed(8);
		testRightSpeed(13);
		testRightSpeed(21);
	}
	private void testRightSpeed(int distanceToMove) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final Node startNode = new Node((short)startMarioXPos, (short)startMarioYPos,(byte)0);
		final Node endNode = new Node((short)(startMarioXPos + distanceToMove), (short)startMarioYPos,(byte)0);
		final DirectedEdge edge = new Running(startNode, endNode);
		final SpeedNode speedNode = new SpeedNode(endNode, null, startMarioXPos, 0, edge, 0);
		
		testEdgeMovement(observation, edge, speedNode.getMoveInfo(), agent, marioControls, distanceToMove, true);
	}
	
	@Test
	public void testLeftMovement() {
		testLeftSpeed(1);
		testLeftSpeed(2);
		testLeftSpeed(5);
		testLeftSpeed(8);
		testLeftSpeed(13);
		testLeftSpeed(21);
	}
	private void testLeftSpeed(int distanceToMove) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		//first move mario right so he doesn't hit the wall when going left
		agent.action[Mario.KEY_RIGHT] = true;
		for (int i = 0; i < 150; i++) {
			TestTools.runOneTick(observation);
		}
		
		//then just wait a few ticks for mario to completely stop
		agent.action[Mario.KEY_RIGHT] = false;
		for (int i = 0; i < 50; i++) {
			TestTools.runOneTick(observation);
		}
		
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final Node startNode = new Node((short)startMarioXPos, (short)startMarioYPos,(byte)0);
		final Node endNode = new Node((short)(startMarioXPos - distanceToMove), (short)startMarioYPos,(byte)0);
		final DirectedEdge edge = new Running(startNode, endNode);
		final SpeedNode speedNode = new SpeedNode(endNode, null, startMarioXPos, 0, edge, 0);
		
		testEdgeMovement(observation, edge, speedNode.getMoveInfo(), agent, marioControls, distanceToMove, true);
	}
	
	@Test
	public void testDeaccelerating() {
		testDeaccelerating(1);
		testDeaccelerating(2);
		testDeaccelerating(5);
		testDeaccelerating(8);
		testDeaccelerating(13);
		testDeaccelerating(21);
	}
	private void testDeaccelerating(int distanceToMove) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final Node startNode = new Node((short)startMarioXPos, (short)startMarioYPos,(byte)0);
		final Node endNode = new Node((short)(startMarioXPos + distanceToMove), (short)startMarioYPos,(byte)0);
		final DirectedEdge edge1 = new Running(startNode, endNode);
		final SpeedNode speedNode1 = new SpeedNode(endNode, null, startMarioXPos, 0, edge1, 0);
		final MovementInformation moveInfo = speedNode1.getMoveInfo();
		
		final DirectedEdge edge2 = new Running(endNode, startNode);
		final SpeedNode speedNode2 = new SpeedNode(startNode, null, startMarioXPos + moveInfo.getXMovementDistance(), moveInfo.getEndSpeed(), edge2, 0);
		
		testEdgeMovement(observation, edge1, speedNode1.getMoveInfo(), agent, marioControls, distanceToMove, true);
		testEdgeMovement(observation, edge2, speedNode2.getMoveInfo(), agent, marioControls, distanceToMove, false);
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
			} else {
				agent.action[Mario.KEY_JUMP] = false;
				upTime = false;
			}
			TestTools.runOneTick(observation);
			
			expectedJumpTime++;
			if (observation.isMarioOnGround()) {
				break;
			}
			
		}
		//can't hold jump for more than 8 ticks
		expectedTicksHeldUp = Math.min(expectedTicksHeldUp, 8);
		
		final float endMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float endMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final Node startNode = new Node((short)startMarioXPos, (short)Math.round(startMarioYPos), (byte)0);
		final Node endNode   = new Node((short)endMarioXPos  , (short)Math.round(endMarioYPos)  , (byte)0);
		SecondOrderPolynomial edge = new SecondOrderPolynomial(startNode, endNode);
		
		/*
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
		*/
	}
	
	private void testEdgeMovement(Environment observation, DirectedEdge edge, MovementInformation moveInfo, UnitTestAgent agent, MarioControls marioControls, int distanceToMove, boolean firstEdge) {
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		path.add(edge);
		edge.setMoveInfo(moveInfo);
		
		if (firstEdge) {
			agent.action = marioControls.getNextAction(observation, path);
			marioControls.reset();
			TestTools.runOneTick(observation);	
		}
		for (int i = 0; i < moveInfo.getPositions().length; i++) {
			final Point2D.Float position = moveInfo.getPositions()[i];
			
			agent.action = marioControls.getNextAction(observation, path);
			TestTools.runOneTick(observation);
			
			final float expectedMarioXPos = position.x + startMarioXPos;
			final float expectedMarioYPos = position.y + startMarioYPos;
			
			final float actualMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			final float actualMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			if (!withinAcceptableError(expectedMarioXPos, expectedMarioYPos, actualMarioXPos, actualMarioYPos)) {
				Assert.fail("Mario Wasn't close enough to the expected position\ndistance: " + distanceToMove + 
						"\nx: " + Math.abs(expectedMarioXPos - actualMarioXPos) + 
						"\ny: " + Math.abs(expectedMarioYPos - actualMarioYPos) +
						"\ntick: " + i);
			}
		}
	}
	
	private boolean withinAcceptableError(float x1, float y1, float x2, float y2) {
		return 	Math.abs(x1 - x2) <= MarioControls.ACCEPTED_DEVIATION && 
				Math.abs(y1 - y2) <= MarioControls.ACCEPTED_DEVIATION;
	}
}