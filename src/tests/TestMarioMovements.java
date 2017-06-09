package tests;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;
import ch.idsia.mario.environments.Environment;

public class TestMarioMovements {
	
	@Test
	public void testRightMovementSlow() {
		testRightSpeed(1, false, false);
		testRightSpeed(2, false, false);
		testRightSpeed(3, false, false);
		testRightSpeed(4, false, false);
	}
	@Test
	public void testRightMovementFast() {
		testRightSpeed(1, true, false);
		testRightSpeed(2, true, false);
		testRightSpeed(3, true, false);
		testRightSpeed(4, true, false);
	}
	@Test
	public void testRightMovementMixed() {
		testRightSpeed(1, false, true);
		testRightSpeed(2, false, true);
		testRightSpeed(3, false, true);
		testRightSpeed(4, false, true);
		testRightSpeed(1, true , true);
		testRightSpeed(2, true , true);
		testRightSpeed(3, true , true);
		testRightSpeed(4, true , true);
	}
	private void testRightSpeed(int distanceToMove, boolean useSuperSpeed, boolean mixed) {
		final int pathLength = 10;
		final boolean[] useSuperSpeeds = new boolean[pathLength];
		for (int i = 0; i < useSuperSpeeds.length; i++) {
			if (mixed) {
				useSuperSpeed = !useSuperSpeed;
			}
			useSuperSpeeds[i] = useSuperSpeed;
		}
		
		testRightSpeed(distanceToMove, pathLength, useSuperSpeeds);
	}
	private void testRightSpeed(int distanceToMove, int pathLength, boolean[] useSuperSpeeds) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startMarioXPos, startMarioYPos, distanceToMove, 0, 0, 10, world, useSuperSpeeds);
		verifyIntegrityOfPath(path);
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testLeftMovementSlow() {
		testLeftSpeed(1, false, false);
		testLeftSpeed(2, false, false);
		testLeftSpeed(3, false, false);
		testLeftSpeed(4, false, false);
	}
	@Test
	public void testLeftMovementFast() {
		testLeftSpeed(1, true, false);
		testLeftSpeed(2, true, false);
		testLeftSpeed(3, true, false);
		testLeftSpeed(4, true, false);
	}
	@Test
	public void testLeftMovementMixed() {
		testLeftSpeed(1, false, true);
		testLeftSpeed(2, false, true);
		testLeftSpeed(3, false, true);
		testLeftSpeed(4, false, true);
		testLeftSpeed(1, true , true);
		testLeftSpeed(2, true , true);
		testLeftSpeed(3, true , true);
		testLeftSpeed(4, true , true);
	}
	private void testLeftSpeed(int distanceToMove, boolean useSuperSpeed, boolean mixed) {
		final int pathLength = 10;
		final boolean[] useSuperSpeeds = new boolean[pathLength];
		for (int i = 0; i < useSuperSpeeds.length; i++) {
			if (mixed) {
				useSuperSpeed = !useSuperSpeed;
			}
			useSuperSpeeds[i] = useSuperSpeed;
		}
		
		testLeftSpeed(distanceToMove, pathLength, useSuperSpeeds);
	}
	private void testLeftSpeed(int distanceToMove, int pathLength, boolean[] useSuperSpeeds) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		TestTools.setMarioXPosition(observation, 50);
		TestTools.runOneTick(observation);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startMarioXPos, startMarioYPos, -distanceToMove, 0, 0, pathLength, world, useSuperSpeeds);
		verifyIntegrityOfPath(path);
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testDeacceleratingSlow() {
		testDeaccelerating(1 , false, false);
		testDeaccelerating(2 , false, false);
		testDeaccelerating(5 , false, false);
		testDeaccelerating(8 , false, false);
		testDeaccelerating(13, false, false);
		testDeaccelerating(21, false, false);
	}
	@Test
	public void testDeacceleratingFast() {
		testDeaccelerating(1 , true, false);
		testDeaccelerating(2 , true, false);
		testDeaccelerating(5 , true, false);
		testDeaccelerating(8 , true, false);
		testDeaccelerating(13, true, false);
		testDeaccelerating(21, true, false);
	}
	@Test
	public void testDeacceleratingMixed() {
		testDeaccelerating(1 , false, true);
		testDeaccelerating(2 , false, true);
		testDeaccelerating(5 , false, true);
		testDeaccelerating(8 , false, true);
		testDeaccelerating(13, false, true);
		testDeaccelerating(21, false, true);
		testDeaccelerating(1 , true , true);
		testDeaccelerating(2 , true , true);
		testDeaccelerating(5 , true , true);
		testDeaccelerating(8 , true , true);
		testDeaccelerating(13, true , true);
		testDeaccelerating(21, true , true);
	}
	private void testDeaccelerating(int distanceToMove, boolean useSuperSpeed, boolean mixed) {
		final int pathLength = 10;
		final boolean[] useSuperSpeeds = new boolean[distanceToMove * pathLength * 2];
		for (int i = 0; i < useSuperSpeeds.length; i++) {
			if (mixed) {
				useSuperSpeed = !useSuperSpeed;
			}
			useSuperSpeeds[i] = useSuperSpeed;
		}
		
		testDeaccelerating(distanceToMove, pathLength, useSuperSpeeds);
	}
	private void testDeaccelerating(int distanceToMove, int pathLength, boolean[] useSuperSpeeds) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent, false);
		TestTools.setMarioXPosition(observation, 5);
		TestTools.runOneTick(observation);
		
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		int[] moveVector = new int[distanceToMove * pathLength * 2];
		int index = 0;
		for (int i = 0; i < pathLength; i++) {
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
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startNode.x, startNode.y, moveVector, 0, 0, distanceToMove * pathLength, world, useSuperSpeeds);
		verifyIntegrityOfPath(path);
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testJumpsSlow() {
		testJumpTime(1, 0, 0, false);
		testJumpTime(2, 0, 0, false);
		testJumpTime(3, 0, 0, false);
		testJumpTime(4, 0, 0, false);
		for (int jumpHeight = 6; jumpHeight >= 0; jumpHeight--) {
			for (int jumpDistance = 2; jumpDistance < 4; jumpDistance++) {
				testJumpTime(1, jumpHeight, jumpDistance, false);
				testJumpTime(2, jumpHeight, jumpDistance, false);
				testJumpTime(3, jumpHeight, jumpDistance, false);
				testJumpTime(4, jumpHeight, jumpDistance, false);
				testJumpTime(5, jumpHeight, jumpDistance, false);	
			}
		}
		testJumpTime(2, -1, 1, false);
		testJumpTime(3, -1, 1, false);
		testJumpTime(4, -1, 1, false);
		testJumpTime(2, -1, 2, false);
		testJumpTime(3, -1, 2, false);
		testJumpTime(4, -1, 2, false);
		testJumpTime(3, -2, 1, false);
		testJumpTime(4, -2, 1, false);
		testJumpTime(3, -2, 2, false);
		testJumpTime(4, -2, 2, false);
		testJumpTime(4, -3, 1, false);
		testJumpTime(5, -3, 1, false);
		testJumpTime(4, -3, 2, false);
		testJumpTime(5, -3, 2, false);
		testJumpTime(4, -4, 1, false);
		testJumpTime(5, -4, 1, false);
	}
	@Test
	public void testJumpsFast() {		
		testJumpTime(1, 0, 0, true);
		testJumpTime(2, 0, 0, true);
		testJumpTime(3, 0, 0, true);
		testJumpTime(4, 0, 0, true);
		for (int jumpHeight = 6; jumpHeight >= 0; jumpHeight--) {
			for (int jumpDistance = 2; jumpDistance < 4; jumpDistance++) {
				testJumpTime(1, jumpHeight, jumpDistance, true);
				testJumpTime(2, jumpHeight, jumpDistance, true);
				testJumpTime(3, jumpHeight, jumpDistance, true);
				testJumpTime(4, jumpHeight, jumpDistance, true);
				testJumpTime(5, jumpHeight, jumpDistance, true);	
			}
		}
		testJumpTime(2, -1, 1, true);
		testJumpTime(3, -1, 1, true);
		testJumpTime(4, -1, 1, true);
		testJumpTime(2, -1, 2, true);
		testJumpTime(3, -1, 2, true);
		testJumpTime(4, -1, 2, true);
		testJumpTime(3, -2, 1, true);
		testJumpTime(4, -2, 1, true);
		testJumpTime(3, -2, 2, true);
		testJumpTime(4, -2, 2, true);
		testJumpTime(4, -3, 1, true);
		testJumpTime(5, -3, 1, true);
		testJumpTime(4, -3, 2, true);
		testJumpTime(5, -3, 2, true);
		//Can't do these two as mario hits the corner of a block first
		//testJumpTime(4, -4, 1, true);
		//testJumpTime(5, -4, 1, true);
	}
	private void testJumpTime(int jumpHeight, int heightDifference, int distanceToMove, boolean useSuperSpeed) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("jumpLevels/jumpDownLevels/jumpDown" + heightDifference + ".lvl", agent, false);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startMarioXPos, startMarioYPos, distanceToMove, jumpHeight, heightDifference, 1, world, useSuperSpeed);
		verifyIntegrityOfPath(path);
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testConsecutiveJumpsSlow() {
		for (int pathLength = 1; pathLength < 10; pathLength++) {
			for (int jumpHeight = 1; jumpHeight <= 4; jumpHeight++) {
				for (int distanceX = 1; distanceX <= 4; distanceX++) {
					testConsecutiveJumpMovement(distanceX, jumpHeight, pathLength, false, false);
				}
			}
		}
	}
	@Test
	public void testConsecutiveJumpsFast() {
		for (int pathLength = 1; pathLength < 10; pathLength++) {
			for (int jumpHeight = 1; jumpHeight <= 4; jumpHeight++) {
				for (int distanceX = 1; distanceX <= 4; distanceX++) {
					testConsecutiveJumpMovement(distanceX, jumpHeight, pathLength, true, false);
				}
			}
		}
	}
	@Test
	public void testConsecutiveJumpsMixed() {
		for (int pathLength = 1; pathLength < 10; pathLength++) {
			for (int jumpHeight = 1; jumpHeight <= 4; jumpHeight++) {
				for (int distanceX = 1; distanceX <= 4; distanceX++) {
					testConsecutiveJumpMovement(distanceX, jumpHeight, pathLength, false, true);
					testConsecutiveJumpMovement(distanceX, jumpHeight, pathLength, true , true);
				}
			}
		}
	}
	private void testConsecutiveJumpMovement(int distanceX, int jumpHeight, int pathLength, boolean useSuperSpeed, boolean mixed) {
		final boolean[] useSuperSpeeds = new boolean[pathLength];
		for (int i = 0; i < useSuperSpeeds.length; i++) {
			if (mixed) {
				useSuperSpeed = !useSuperSpeed;
			}
			useSuperSpeeds[i] = useSuperSpeed;
		}
		
		testConsecutiveJumpMovement(distanceX, jumpHeight, pathLength, useSuperSpeeds);
	}
	private void testConsecutiveJumpMovement(int distanceX, int jumpHeight, int pathLength, boolean[] useSuperSpeeds) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("flat.lvl", agent, false);
		
		final int startMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int startMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(startMarioXPos, startMarioYPos, distanceX, jumpHeight, 0, pathLength, world, useSuperSpeeds);
		verifyIntegrityOfPath(path);
		testEdgeMovement(observation, path, agent, marioControls);
	}
	
	@Test
	public void testRunningRightPathEqualitiesSlow() {
		MarioControls.setupYMovements();
		final World world = new World();
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				final ArrayList<DirectedEdge> a = PathHelper.createPath(0, 0, i, 0, 0, j, world, false);
				final ArrayList<DirectedEdge> b = PathHelper.createPath(0, 0, j, 0, 0, i, world, false);
				verifyIntegrityOfPath(a);
				verifyIntegrityOfPath(b);
				comparePaths(a, b);
			}
		}	
	}
	@Test
	public void testRunningRightPathEqualitiesFast() {
		MarioControls.setupYMovements();
		final World world = new World();
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				final ArrayList<DirectedEdge> a = PathHelper.createPath(0, 0, i, 0, 0, j, world, true);
				final ArrayList<DirectedEdge> b = PathHelper.createPath(0, 0, j, 0, 0, i, world, true);
				verifyIntegrityOfPath(a);
				verifyIntegrityOfPath(b);
				comparePaths(a, b);
			}
		}	
	}
	
	@Test
	public void testRunningLeftPathEqualitiesSlow() {
		MarioControls.setupYMovements();
		final World world = new World();
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				final ArrayList<DirectedEdge> a = PathHelper.createPath(0, 0, -i, 0, 0, j, world, false);
				final ArrayList<DirectedEdge> b = PathHelper.createPath(0, 0, -j, 0, 0, i, world, false);
				verifyIntegrityOfPath(a);
				verifyIntegrityOfPath(b);
				comparePaths(a, b);
			}
		}	
	}
	@Test
	public void testRunningLeftPathEqualitiesFast() {
		MarioControls.setupYMovements();
		final World world = new World();
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				final ArrayList<DirectedEdge> a = PathHelper.createPath(0, 0, -i, 0, 0, j, world, true);
				final ArrayList<DirectedEdge> b = PathHelper.createPath(0, 0, -j, 0, 0, i, world, true);
				verifyIntegrityOfPath(a);
				verifyIntegrityOfPath(b);
				comparePaths(a, b);
			}
		}	
	}
	
	@Test
	public void testXWidthJumpNoAstarSlow() {
		testJumpNoAstar(1, 2, false, false);
		testJumpNoAstar(1, 3, false, false);
		testJumpNoAstar(1, 4, false, false);
		testJumpNoAstar(2, 3, false, false);
		testJumpNoAstar(2, 4, false, false);
	}
	@Test
	public void testXWidthJumpNoAstarFast() {
		testJumpNoAstar(1, 2, true, false);
		testJumpNoAstar(1, 3, true, false);
		testJumpNoAstar(1, 4, true, false);
		testJumpNoAstar(2, 3, true, false);
		testJumpNoAstar(2, 4, true, false);
	}
	@Test
	public void testXWidthJumpNoAstarMixed() {
		testJumpNoAstar(1, 2, false, true);
		testJumpNoAstar(1, 3, false, true);
		testJumpNoAstar(1, 4, false, true);
		testJumpNoAstar(2, 3, false, true);
		testJumpNoAstar(2, 4, false, true);
		testJumpNoAstar(1, 2, true , true);
		testJumpNoAstar(1, 3, true , true);
		testJumpNoAstar(1, 4, true , true);
		testJumpNoAstar(2, 3, true , true);
		testJumpNoAstar(2, 4, true , true);
	}
	private void testJumpNoAstar(int distanceX, int jumpHeight, boolean useSuperSpeed, boolean mixed) {
		final int pathLength = 30;
		final boolean[] useSuperSpeeds = new boolean[pathLength];
		for (int i = 0; i < useSuperSpeeds.length; i++) {
			if (mixed) {
				useSuperSpeed = !useSuperSpeed;
			}
			useSuperSpeeds[i] = useSuperSpeed;
		}
		
		testJumpNoAstar(distanceX, jumpHeight, pathLength, useSuperSpeeds);
	}
	private void testJumpNoAstar(int distanceX, int jumpHeight, int pathLength, boolean[] useSuperSpeeds) {
		final UnitTestAgent agent = new UnitTestAgent();	
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final Environment observation = TestTools.loadLevel("jumpLevels/only" + distanceX + "Width.lvl", agent, false);
		TestTools.setMarioXPosition(observation, 3);
		TestTools.runOneTick(observation);
		
		final int startY = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		ArrayList<DirectedEdge> path = PathHelper.createPath(3, startY, distanceX + 1, jumpHeight, 0, pathLength, world, useSuperSpeeds); 
		verifyIntegrityOfPath(path);
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
		}
		for (int i = 0; i < yActions1.size(); i++) {
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
				if (moveInfo.getPressYButton().length > z) {
					yActions.add(moveInfo.getPressYButton()[z]);	
				}
				
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
		agent.action = marioControls.getActions();
		TestTools.runOneTick(observation);
		for (int z = 0; z < path.size(); z++) {	
			final DirectedEdge edge = path.get(0);
			final MovementInformation moveInfo = edge.getMoveInfo();
			for (int i = 0; i < moveInfo.getPositions().length; i++) {				
				marioControls.getNextAction(observation, path);
				TestTools.runOneTick(observation);
				
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
					Assert.fail("Mario Wasn't close enough to the expected position." + 
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
	
	private void verifyIntegrityOfPath(ArrayList<DirectedEdge> path) {
		final float startX = path.get(0).source.x;
		float posX = 0;
		for (DirectedEdge edge : path) {
			posX += edge.getMoveInfo().getXMovementDistance();
			
			final float diffX = Math.abs(posX - (edge.target.x - startX));
			if (diffX > MarioControls.MAX_X_VELOCITY) {
				Assert.fail("MovementInformation wasn't close enough to the target node position." + 
							"\npathEndX: " + posX + 
							"\ntargetNodeX: " + (edge.target.x - startX));
			}
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