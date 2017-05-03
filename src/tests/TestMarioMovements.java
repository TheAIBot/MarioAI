package tests;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

import MarioAI.MarioMethods;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestMarioMovements {
	
	private int hashNumber = 0;
	
	@Test
	public void testRightMovement() {
		testRightSpeed(1);
		testRightSpeed(2);
		testRightSpeed(3);
		testRightSpeed(4);
	}
	private void testRightSpeed(int distanceToMove) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		
		final Node startNode = new Node((short)startMarioXPos, (short)startMarioYPos,(byte)0);
		final Node endNode = new Node((short)(startMarioXPos + distanceToMove), (short)startMarioYPos,(byte)0);
		final DirectedEdge edge = new RunningEdge(startNode, endNode);
		SpeedNode speedNode = new SpeedNode(endNode, null, startMarioXPos, 0, edge, 0);
		speedNode.use();
		path.add(speedNode.ancestorEdge);
		
		for (int i = 0; i < 10; i++) {
			speedNode = createEdgeWithSpeedNode(speedNode.ancestorEdge.target, speedNode, distanceToMove, 0, 0);
			speedNode.use();
			path.add(speedNode.ancestorEdge);
		}
		
		testEdgeMovement(observation, path, agent, marioControls);
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
		
		testEdgeMovement(observation, edge, agent, marioControls);
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
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final Node startNode = new Node((int)startMarioXPos, (int)startMarioYPos,(byte)0);
		final Node endNode = new Node((short)(startMarioXPos + distanceToMove), (short)startMarioYPos,(byte)0);
		final DirectedEdge edge1 = new RunningEdge(startNode, endNode);
		final SpeedNode speedNode1 = new SpeedNode(endNode, null, startMarioXPos, 0, edge1, 0);
		speedNode1.use();
		path.add(edge1);
		
		SpeedNode speedNode2 = createEdgeWithSpeedNode(endNode, speedNode1, -1, 0, 0);
		path.add(speedNode2.ancestorEdge);
		
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testJumps() {
		testJumpTime(1, 0, 0);
		testJumpTime(2, 0, 0);
		testJumpTime(3, 0, 0);
		testJumpTime(4, 0, 0);
		for (int jumpHeight = 6; jumpHeight >= 0; jumpHeight--) {
			for (int jumpDistance = 2; jumpDistance < 4; jumpDistance++) {
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
		edge.setTopPoint(0, Math.round(startMarioYPos) + Math.min(jumpHeight, 4));
		final SpeedNode speedNode = new SpeedNode(endNode, null, startMarioXPos, 0, edge, 0);
		speedNode.use();
		
		testEdgeMovement(observation, edge, agent, marioControls);
	}
	
	@Test
	public void testConsecutiveJumps() {
		testConsecutiveJumpMovement(1, 1, 2);
		for (int pathLength = 1; pathLength < 10; pathLength++) {
			for (int jumpHeight = 1; jumpHeight <= 4; jumpHeight++) {
				for (int distanceX = 1; distanceX <= 4; distanceX++) {
					testConsecutiveJumpMovement(distanceX, jumpHeight, pathLength);
				}
			}
		}
	}
	private void testConsecutiveJumpMovement(int distanceX, int jumpHeight, int pathLength) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = createPath(startMarioXPos, startMarioYPos, distanceX, jumpHeight, pathLength);
		
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testRunningRightPathEqualities() {
		MarioControls.setupYMovements();
		
		//comparePaths(createPath(0, 0, 3, 1), createPath(0, 0, 1, 3));
		
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				comparePaths(createPath(0, 0, i, 0, j), createPath(0, 0, j, 0, i));
			}
		}	
	}
	
	@Test
	public void testRunningLeftPathEqualities() {
		MarioControls.setupYMovements();
		
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				comparePaths(createPath(0, 0, -i, 0, j), createPath(0, 0, -j, 0, i));
			}
		}	
	}
	
	
	private ArrayList<DirectedEdge> createPath(int startX, int startY, int distanceX, int jumpHeight, int pathlength) {
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		
		final Node startNode = new Node((short)startX, (short)startY,(byte)0);
		final Node endNode = new Node((short)(startX + distanceX), (short)startY,(byte)0);
		//final DirectedEdge edge = new RunningEdge(startNode, endNode);
		final DirectedEdge edge;
		if (jumpHeight == 0) {
			edge = new RunningEdge(startNode, endNode);
		}
		else {
			edge = new JumpingEdge(startNode, endNode, startNode.y + Math.min(jumpHeight, 4));
		}
		SpeedNode speedNode = new SpeedNode(endNode, null, startX, 0, edge, getHash());
		speedNode.use();
		path.add(speedNode.ancestorEdge);
		
		for (int i = 0; i < pathlength - 1; i++) {
			speedNode = createEdgeWithSpeedNode(speedNode.ancestorEdge.target, speedNode, distanceX, 0, jumpHeight);
			speedNode.use();
			path.add(speedNode.ancestorEdge);
		}
		
		return path;
	}
	
	private SpeedNode createEdgeWithSpeedNode(Node startNode, SpeedNode startSpeedNode, int xMove, int yMove, int jumpHeight) {
		final Node endNode = new Node(startNode.x + xMove, startNode.y + yMove, (byte)0);
		final DirectedEdge edge;
		if (jumpHeight == 0) {
			edge = new RunningEdge(startNode, endNode);
		}
		else {
			edge = new JumpingEdge(startNode, endNode, startNode.y + Math.min(jumpHeight, 4));
		}
		final SpeedNode speedNode = new SpeedNode(endNode, startSpeedNode, edge, getHash());
		speedNode.use();
		
		return speedNode;
	}
	
	private void comparePaths(ArrayList<DirectedEdge> path1, ArrayList<DirectedEdge> path2) {
		final ArrayList<Point2D.Float> positions1 = new ArrayList<Point2D.Float>();
		final ArrayList<Boolean> xActions1 = new ArrayList<Boolean>();
		final ArrayList<Boolean> yActions1 = new ArrayList<Boolean>();
		final ArrayList<Point2D.Float> speed1 = new ArrayList<Point2D.Float>(); 
		convertPathToLists(path1, positions1, xActions1, yActions1, speed1);
		
		//positions1.stream().forEach(x -> System.out.println(x.x));
		
		final ArrayList<Point2D.Float> positions2 = new ArrayList<Point2D.Float>();
		final ArrayList<Boolean> xActions2 = new ArrayList<Boolean>();
		final ArrayList<Boolean> yActions2 = new ArrayList<Boolean>();
		final ArrayList<Point2D.Float> speed2 = new ArrayList<Point2D.Float>(); 
		convertPathToLists(path2, positions2, xActions2, yActions2, speed2);
		
		//positions2.stream().forEach(x -> System.out.println(x.x));
		
		assertEquals(positions1.size(), positions2.size());
		assertEquals(xActions1.size(), xActions2.size());
		assertEquals(yActions1.size(), yActions2.size());
		assertEquals(speed1.size(), speed2.size());
		
		for (int i = 0; i < positions1.size(); i++) {
			assertEquals(positions1.get(i).x, positions2.get(i).x, MarioControls.ACCEPTED_DEVIATION);
			assertEquals(positions1.get(i).y, positions2.get(i).y, MarioControls.ACCEPTED_DEVIATION);
			
			assertEquals(xActions1.get(i), xActions2.get(i));
			
			assertEquals(yActions1.get(i), yActions2.get(i));
		}
		for (int i = 0; i < speed1.size(); i++) {
			assertEquals(speed1.get(i).x, speed2.get(i).x, MarioControls.ACCEPTED_DEVIATION);
			assertEquals(speed1.get(i).y, speed2.get(i).y, MarioControls.ACCEPTED_DEVIATION);
		}
	}
	
	private void convertPathToLists(ArrayList<DirectedEdge> path, ArrayList<Point2D.Float> positions, ArrayList<Boolean> xActions, ArrayList<Boolean> yActions, ArrayList<Point2D.Float> speed) {
		float xOffset = 0;
		float yOffset = 0;
		
		Point2D.Float oldPos = new Point2D.Float(0, 0);
		
		int speedIndexOffset = 0;
		for (int i = 0; i < path.size(); i++) {
			final DirectedEdge edge = path.get(i);
			final MovementInformation moveInfo = edge.getMoveInfo();
			
			for (int z = 0; z < moveInfo.getMoveTime(); z++) {
				final Point2D.Float position = moveInfo.getPositions()[z];
				
				final float x = position.x + xOffset;
				final float y = position.y + yOffset;
				
				positions.add(new Point2D.Float(x, y));
				
				xActions.add(moveInfo.getPressXButton()[z]);
				yActions.add(moveInfo.getPressYButton()[z]);
				
				final Point2D.Float currentSpeed = new Point2D.Float(x - oldPos.x, y - oldPos.y);
				speed.add(currentSpeed);
				
				oldPos = new Point2D.Float(x, y);
			}
			
			final float lastXSpeed = speed.get(moveInfo.getMoveTime() - 1 + speedIndexOffset).x;
			assertTrue("Calculated speed " + lastXSpeed + " is not equal end speed " + moveInfo.getEndSpeed(),withinAcceptableError(lastXSpeed, moveInfo.getEndSpeed()));
			speedIndexOffset += moveInfo.getMoveTime();
			
			
			final Point2D.Float endPoint = moveInfo.getPositions()[moveInfo.getPositions().length - 1];
			xOffset += endPoint.x;
			yOffset += endPoint.y;
		}
		
	}
	
	private void testEdgeMovement(Environment observation, DirectedEdge edge, UnitTestAgent agent, MarioControls marioControls) {
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		path.add(edge);
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	private void testEdgeMovement(Environment observation, ArrayList<DirectedEdge> path, UnitTestAgent agent, MarioControls marioControls) {
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		float oldMarioXPos = startMarioXPos;
		float actualMarioSpeed = 0;
		float xOffset = 0;
		float yOffset = 0;
		for (int z = 0; z < path.size(); z++) {	
			DirectedEdge edge = path.get(0);
			MovementInformation moveInfo = edge.getMoveInfo();
			for (int i = 0; i < moveInfo.getPositions().length; i++) {				
				final boolean[] newActions = marioControls.getNextAction(observation, path);
				for (int j = 0; j < newActions.length; j++) {
					agent.action[j] = newActions[j];
				}
				TestTools.runOneTick(observation);
				
				edge = path.get(0);
				moveInfo = edge.getMoveInfo();
				final Point2D.Float position = moveInfo.getPositions()[i];
				
				final float expectedMarioXPos = startMarioXPos + position.x + xOffset;
				final float expectedMarioYPos = startMarioYPos - position.y + yOffset;
				
				final float actualMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
				final float actualMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
				
				actualMarioSpeed = actualMarioXPos - oldMarioXPos;
				oldMarioXPos = actualMarioXPos;
				
				if (!withinAcceptableError(expectedMarioXPos, actualMarioXPos, actualMarioYPos, expectedMarioYPos)) {
					final int distanceXMoved = edge.target.x - edge.source.x;
					final int distanceYMoved = edge.target.y - edge.source.y;
					Assert.fail("Mario Wasn't close enough to the expected position" + 
								"\nxdistance: " + distanceXMoved + 
								"\nydistance: " + distanceYMoved + 
								"\njump height: " + edge.getMaxY() + 
								"\nx: " + (expectedMarioXPos - actualMarioXPos) + 
								"\ny: " + (expectedMarioYPos - actualMarioYPos) +
								"\ntick: " + i + 
								"\npath: " + z);
				}
			}
			
			final float expectedMarioSpeed = moveInfo.getEndSpeed();
			if (!withinAcceptableError(expectedMarioSpeed, actualMarioSpeed)) {
				final int distanceXMoved = edge.target.x - edge.source.x;
				final int distanceYMoved = edge.target.y - edge.source.y;
				Assert.fail("Mario Wasn't close enough to the expected position" + 
							"\nxdistance: " + distanceXMoved + 
							"\nydistance: " + distanceYMoved + 
							"\njump height: " + edge.getMaxY() + 
							"\nspeed diff: " + (expectedMarioSpeed - actualMarioSpeed));
			}
			
			final Point2D.Float endPoint = moveInfo.getPositions()[moveInfo.getPositions().length - 1];
			xOffset += endPoint.x;
			yOffset += endPoint.y;
		}
	}
	
	private boolean withinAcceptableError(float a1, float b1, float a2, float b2) {
		return 	withinAcceptableError(a1, b1) && 
				withinAcceptableError(a2, b2);
	}
	
	private boolean withinAcceptableError(float a, float b) {
		return 	Math.abs(a - b) <= MarioControls.ACCEPTED_DEVIATION;
	}
	
	private int getHash() {
		return hashNumber++;
	}
}