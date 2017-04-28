package tests;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.crypto.interfaces.PBEKey;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.MarioMethods;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.JumpingEdge;
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
		final DirectedEdge edge = new RunningEdge(startNode, endNode);
		final SpeedNode speedNode = new SpeedNode(endNode, null, startMarioXPos, 0, edge, 0);
		speedNode.use();
		
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
		final DirectedEdge edge = new RunningEdge(startNode, endNode);
		final SpeedNode speedNode = new SpeedNode(endNode, null, startMarioXPos, 0, edge, 0);
		speedNode.use();
		
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
		
		final Node startNode = new Node((int)startMarioXPos, (int)startMarioYPos,(byte)0);
		final Node endNode = new Node((short)(startMarioXPos + distanceToMove), (short)startMarioYPos,(byte)0);
		final DirectedEdge edge1 = new RunningEdge(startNode, endNode);
		final SpeedNode speedNode1 = new SpeedNode(endNode, null, startMarioXPos, 0, edge1, 0);
		speedNode1.use();
		final MovementInformation moveInfo = speedNode1.getMoveInfo();
		
		final DirectedEdge edge2 = new RunningEdge(endNode, startNode);
		final SpeedNode speedNode2 = new SpeedNode(startNode, null, startMarioXPos + moveInfo.getXMovementDistance(), moveInfo.getEndSpeed(), edge2, 0);
		speedNode2.use();
		
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		path.add(edge1);
		path.add(edge2);
		
		testEdgeMovement(observation, edge1, speedNode1.getMoveInfo(), agent, marioControls, distanceToMove, true);
	}
	
	@Test
	public void testJumps() {
		for (int jumpHeight = 6; jumpHeight >= 0; jumpHeight--) {
			for (int jumpDistance = 1; jumpDistance < 4; jumpDistance++) {
				testJumpTime(1     , jumpHeight, jumpDistance);
				testJumpTime(1.5f  , jumpHeight, jumpDistance);
				testJumpTime(1.645f, jumpHeight, jumpDistance);
				testJumpTime(3.4f  , jumpHeight, jumpDistance);
				testJumpTime(4     , jumpHeight, jumpDistance);
				testJumpTime(5.6f  , jumpHeight, jumpDistance);	
			}
		}
		
		testJumpTime(1.5f, -1, 1);
		testJumpTime(3.4f, -1, 1);
		testJumpTime(5.6f, -1, 1);
		testJumpTime(1.5f, -1, 2);
		testJumpTime(3.4f, -1, 2);
		testJumpTime(5.6f, -1, 2);
		
		testJumpTime(3.4f, -2, 1);
		testJumpTime(5.6f, -2, 1);
		testJumpTime(3.4f, -2, 2);
		testJumpTime(5.6f, -2, 2);
		
		testJumpTime(3.4f, -3, 1);
		testJumpTime(5.6f, -3, 1);
		testJumpTime(3.4f, -3, 2);
		testJumpTime(5.6f, -3, 2);
		
		testJumpTime(4.0f, -4, 1);
		testJumpTime(5.6f, -4, 1);
	}
	private void testJumpTime(float jumpHeight, int heightDifference, int distanceToMove) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("jumpLevels/jumpDownLevels/jumpDown" + heightDifference + ".lvl", agent, false);
		
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final Node startNode = new Node((int)startMarioXPos, (int)Math.round(startMarioYPos),(byte)0);
		final Node endNode = new Node((int)(startMarioXPos + distanceToMove), (int)Math.round(startMarioYPos) + heightDifference,(byte)0);
		final JumpingEdge edge = new JumpingEdge(startNode, endNode, (int)(jumpHeight));
		edge.setTopPoint(0, Math.round(startMarioYPos) + jumpHeight);
		final SpeedNode speedNode = new SpeedNode(endNode, null, startMarioXPos, 0, edge, 0);
		speedNode.use();
		
		testEdgeMovement(observation, edge, speedNode.getMoveInfo(), agent, marioControls, distanceToMove, true);
	}
	
	private void testEdgeMovement(Environment observation, DirectedEdge edge, MovementInformation moveInfo, UnitTestAgent agent, MarioControls marioControls, int distanceToMove, boolean firstEdge) {
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		path.add(edge);
		testEdgeMovement(observation, path, moveInfo, agent, marioControls, distanceToMove, firstEdge);
	}
	
	private void testEdgeMovement(Environment observation, ArrayList<DirectedEdge> path, MovementInformation moveInfo, UnitTestAgent agent, MarioControls marioControls, int distanceToMove, boolean firstEdge) {
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		
		if (firstEdge) {
			agent.action = marioControls.getNextAction(observation, path);
			marioControls.reset();
			TestTools.runOneTick(observation);	
		}
		
		for (int i = 0; i < moveInfo.getPositions().length; i++) {
			final Point2D.Float position = moveInfo.getPositions()[i];
			
			agent.action = marioControls.getNextAction(observation, path);
			TestTools.runOneTick(observation);
			
			final float expectedMarioXPos = startMarioXPos + position.x;
			final float expectedMarioYPos = startMarioYPos - position.y;
			
			final float actualMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			final float actualMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			if (!withinAcceptableError(expectedMarioXPos, expectedMarioYPos, actualMarioXPos, actualMarioYPos)) {
				Assert.fail("Mario Wasn't close enough to the expected position\ndistance: " + distanceToMove + 
						"\nx: " + (expectedMarioXPos - actualMarioXPos) + 
						"\ny: " + (expectedMarioYPos - actualMarioYPos) +
						"\ntick: " + i);
			}
		}
	}
	
	private boolean withinAcceptableError(float x1, float y1, float x2, float y2) {
		return 	Math.abs(x1 - x2) <= MarioControls.ACCEPTED_DEVIATION && 
				Math.abs(y1 - y2) <= MarioControls.ACCEPTED_DEVIATION;
	}
}