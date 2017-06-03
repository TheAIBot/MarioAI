package tests;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;
import ch.idsia.mario.environments.Environment;

public class TestMarioMovements {
	
	@Test
	public void testRightMovement() {
		testRightSpeed(1);
		testRightSpeed(2);
		testRightSpeed(3);
		testRightSpeed(4);
	}
	private void testRightSpeed(int distanceToMove) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startMarioXPos, startMarioYPos, distanceToMove, 0, 0, 10, world);
		
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testLeftMovement() {
		testLeftSpeed(1);
		testLeftSpeed(2);
		testLeftSpeed(3);
		testLeftSpeed(4);
	}
	private void testLeftSpeed(int distanceToMove) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		TestTools.setMarioXPosition(observation, 50);
		TestTools.runOneTick(observation);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startMarioXPos, startMarioYPos, -distanceToMove, 0, 0, 10, world);
		
		testEdgeMovement(observation, path, agent, marioControls);
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
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent, false);
		
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final int REPEAT_COUNT = 10;
		int[] moveVector = new int[distanceToMove * REPEAT_COUNT * 2];
		int index = 0;
		for (int i = 0; i < REPEAT_COUNT; i++) {
			for (int x = 0; x < distanceToMove; x++) {
				moveVector[index] = 1;
				index++;
			}
			for (int x = 0; x < distanceToMove; x++) {
				moveVector[index] = -1;
				index++;
			}
		}
		
		final Node startNode = new Node((int)startMarioXPos, (int)startMarioYPos,(byte)0);
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startNode.x, startNode.y, moveVector, 0, 0, distanceToMove * REPEAT_COUNT, world);
		
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
				testJumpTime(1, jumpHeight, jumpDistance);
				testJumpTime(2, jumpHeight, jumpDistance);
				testJumpTime(3, jumpHeight, jumpDistance);
				testJumpTime(4, jumpHeight, jumpDistance);
				testJumpTime(5, jumpHeight, jumpDistance);	
			}
		}
		
		testJumpTime(2, -1, 1);
		testJumpTime(3, -1, 1);
		testJumpTime(4, -1, 1);
		testJumpTime(2, -1, 2);
		testJumpTime(3, -1, 2);
		testJumpTime(4, -1, 2);
		
		testJumpTime(3, -2, 1);
		testJumpTime(4, -2, 1);
		testJumpTime(3, -2, 2);
		testJumpTime(4, -2, 2);
		
		testJumpTime(4, -3, 1);
		testJumpTime(5, -3, 1);
		testJumpTime(4, -3, 2);
		testJumpTime(5, -3, 2);
		
		testJumpTime(4, -4, 1);
		testJumpTime(5, -4, 1);
	}
	private void testJumpTime(int jumpHeight, int heightDifference, int distanceToMove) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("jumpLevels/jumpDownLevels/jumpDown" + heightDifference + ".lvl", agent, false);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startMarioXPos, startMarioYPos, distanceToMove, jumpHeight, heightDifference, 1, world);
		
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testConsecutiveJumps() {
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
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent, false);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startMarioXPos, startMarioYPos, distanceX, jumpHeight, 0, pathLength, world);
		
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testRunningRightPathEqualities() {
		MarioControls.setupYMovements();
		final World world = new World();
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				comparePaths(PathHelper.createPath(0, 0, i, 0, 0, j, world), PathHelper.createPath(0, 0, j, 0, 0, i, world));
			}
		}	
	}
	
	@Test
	public void testRunningLeftPathEqualities() {
		MarioControls.setupYMovements();
		final World world = new World();
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				comparePaths(PathHelper.createPath(0, 0, -i, 0, 0, j, world), PathHelper.createPath(0, 0, -j, 0, 0, i, world));
			}
		}	
	}
	
	@Test
	public void testXWidthJumpNoAstar() {
		testJumpNoAstar(1, 2);
		testJumpNoAstar(1, 3);
		testJumpNoAstar(1, 4);
		
		testJumpNoAstar(2, 3);
		testJumpNoAstar(2, 4);
	}
	private void testJumpNoAstar(int distanceX, int jumpHeight) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("jumpLevels/only" + distanceX + "Width.lvl", agent, false);
		TestTools.setMarioXPosition(observation, 3);
		TestTools.runOneTick(observation);
		
		final int startY = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		ArrayList<DirectedEdge> path = PathHelper.createPath(3, startY, distanceX + 1, jumpHeight, 0, 40, world); 
		
		testEdgeMovement(observation, path, agent, marioControls);
	}

	private void comparePaths(ArrayList<DirectedEdge> path1, ArrayList<DirectedEdge> path2) {
		final ArrayList<Point2D.Float> positions1 = new ArrayList<Point2D.Float>();
		final ArrayList<Boolean> xActions1 = new ArrayList<Boolean>();
		final ArrayList<Boolean> yActions1 = new ArrayList<Boolean>();
		final ArrayList<Point2D.Float> speed1 = new ArrayList<Point2D.Float>(); 
		convertPathToLists(path1, positions1, xActions1, yActions1, speed1);
		
		final ArrayList<Point2D.Float> positions2 = new ArrayList<Point2D.Float>();
		final ArrayList<Boolean> xActions2 = new ArrayList<Boolean>();
		final ArrayList<Boolean> yActions2 = new ArrayList<Boolean>();
		final ArrayList<Point2D.Float> speed2 = new ArrayList<Point2D.Float>(); 
		convertPathToLists(path2, positions2, xActions2, yActions2, speed2);
		
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
}