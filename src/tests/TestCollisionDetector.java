package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import MarioAI.CollisionDetection;
import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.edgeCreation.EdgeCreator;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCollisionDetector {
	MarioControls marioControler = new MarioControls();
	Environment observation;
	EdgeCreator grapher = new EdgeCreator();
	private static final int GRID_WIDTH = 22;
	public Node marioNode;
	public DirectedEdge runningEdgeType = new RunningEdge(null, null, false);
	//TODO also add some step-by-step comparisons, of isBlocking and the likes.
	//TODO might be an error with him running to the goal nodes, going out of the level.
	//TODO test for different materials, floor and ceilings.
	public World getStartLevelWorld(String level){
		BasicAIAgent agent = new BasicAIAgent("");
		observation = TestTools.loadLevel(level, agent);
		TestTools.setMarioXPosition(observation, 12);
		TestTools.runOneTick(observation);		
		World graph = new World();
		graph.initialize(observation);
		this.marioNode = graph.getMarioNode(observation);
		return graph;		
	}
	
	public World flatlandWorld() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flat.lvl");
		grapher.setMovementEdges(graph, marioNode);
		return graph;
	}
		
	@Test
	public void testRunIntoWall(){
		World world = flatlandWorld();
		world = TestEdgeCreator.totalFlatland(world, marioNode);//Adds a wall later, to force it to run into the wall.
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode); //Again, edge before the wall.
		Node[][] level = world.getLevelMatrix();
		//adding the walls:
		final int WALL_HEIGHT = 8; //Testing up to size 8 wall. Should all collide there.
		//TODO change size back to 1
		for (int size = 1; size <= WALL_HEIGHT; size++) {			
			short i = 1; //TODO change back to 1
			for (; i < GRID_WIDTH/2; i++) {
				TestEdgeCreator.addWall(size, 11 - i - 1, marioNode.y, level, marioNode); //Left wall
				TestEdgeCreator.addWall(size, 11 + i, marioNode.y, level, marioNode); //Right wall
				//Placed at different distances. To the right:
				ArrayList<DirectedEdge> leftWalkingPath = new ArrayList<DirectedEdge>();
				ArrayList<DirectedEdge> rightWalkingPath = new ArrayList<DirectedEdge>();
				for (int j = 0; j < GRID_WIDTH/2 - 1; j++) {
					ArrayList<DirectedEdge> runningEdges = new ArrayList<DirectedEdge>();
					grapher.getRunningReachableEdges(level[j + GRID_WIDTH/2][marioNode.y], j + GRID_WIDTH/2, runningEdges);
					DirectedEdge rightRunningEdge = runningEdges.stream().filter(edge -> (edge.target.x > edge.source.x)).findAny().get();
					assertTrue(rightRunningEdge != null);
					
					runningEdges.clear();
					grapher.getRunningReachableEdges(level[GRID_WIDTH/2 - j][marioNode.y], GRID_WIDTH/2 - j, runningEdges);
					DirectedEdge leftRunningEdge = runningEdges.stream().filter(edge -> edge.target.x < edge.source.x).findFirst().get();
					assertTrue(leftRunningEdge != null);
					
					rightWalkingPath.add(rightRunningEdge);
					leftWalkingPath.add(leftRunningEdge);	
				}

				float marioRightSpeed = 0;
				float marioLeftSpeed = 0;
				SpeedNode currentRight = new SpeedNode(rightWalkingPath.get(0).source, marioRightSpeed, Long.MAX_VALUE);
				SpeedNode currentLeft = new SpeedNode(leftWalkingPath.get(0).source, marioLeftSpeed, Long.MAX_VALUE);
				for (int j = 0; j < rightWalkingPath.size(); j++) { //Going through the paths.		
					DirectedEdge currentRightEdge = rightWalkingPath.get(j);		
					DirectedEdge currentLeftEdge 	= leftWalkingPath.get(j);
					
					final SpeedNode snRight = new SpeedNode(currentRightEdge.target, currentRight	,
							                                  currentRightEdge, 0, world); //Don't care about the hash.
					final SpeedNode snLeft 	= new SpeedNode(currentLeftEdge.target	, currentLeft	,
							                                  currentLeftEdge	, 0, world); //Don't care about the hash.
					if (!snRight.isSpeedNodeUseable(world) ||
						!snLeft .isSpeedNodeUseable(world)) {
						fail();
					}	
					String errorMessage = "Size = " + size + ", i = " + i + ", j =" + j;
					System.out.println(errorMessage);
					boolean shouldHaveCollisionRight = false;
					boolean shouldHaveCollisionLeft = false;
					
					for (int k = 0; k < snRight.getMoveInfo().getMoveTime(); k++) {
						float currentPositionX = snRight.getMoveInfo().getXPositions()[k];
						//Starting at the middle of the block + offset.
						double rightMarioPlace = snRight.parentXPos + currentPositionX;
						if ((11+i <= rightMarioPlace + CollisionDetection.MARIO_WIDTH/(16) && 
							          rightMarioPlace + CollisionDetection.MARIO_WIDTH/(16) <= 11+i+1 
							  && 
							  ((Math.floor(snRight.parentXPos) != Math.floor(rightMarioPlace + 0.25) && size == 1) || size != 1)
							  )  ) {
							shouldHaveCollisionRight = true;
							break;
						}
					}
					
					for (int k = 0; k < snRight.getMoveInfo().getMoveTime(); k++) {
						float currentPositionX = snLeft.getMoveInfo().getXPositions()[k];
						//Starting at the middle of the block + offset.
						double leftMarioPlace = snLeft.parentXPos + currentPositionX; 
						if (11-i >= leftMarioPlace - CollisionDetection.MARIO_WIDTH/(16) && 
								       leftMarioPlace - CollisionDetection.MARIO_WIDTH/(16) >= 11-i-1 
							&& 
							 ((Math.floor(snLeft.parentXPos) != Math.floor(leftMarioPlace - CollisionDetection.MARIO_WIDTH/(16))&& size == 1) || size != 1)
							 ){
							shouldHaveCollisionLeft = true;
							break;
						}
					}
					
					//If he should collide with the block
					if (shouldHaveCollisionRight) {
						//errorMessage += ". There are no collision.";
						assertTrue(errorMessage, snRight.getMoveInfo().hasCollisions(currentRight, world));
					} else{
						//errorMessage += ". There are a collision.";
						assertFalse(errorMessage , snRight.getMoveInfo().hasCollisions(currentRight, world));
					}
					
					if (shouldHaveCollisionLeft) {
						//errorMessage += ". There are no collision.";
						assertTrue(errorMessage, snLeft.getMoveInfo().hasCollisions(currentLeft, world));
					} else{
						//errorMessage += ". There are a collision.";
						assertFalse(errorMessage, snLeft.getMoveInfo().hasCollisions(currentLeft, world));
					}
					currentRight 	= snRight;
					currentLeft 	= snLeft;					
				}				
				//removing the walls:
				TestEdgeCreator.removeWall(size, 11 - i - 1, marioNode.y, level);
				TestEdgeCreator.removeWall(size, 11 + i, marioNode.y, level);
			}
		}	
	}
	
	@Test
	public void testJumpIntoCeiling(){
		//TODO go trough the test step by step, and check it is correct.
		World world = TestEdgeCreator.totalFlatland(flatlandWorld(),marioNode);
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode); //Edges are made before the ceiling, to ensure that the movements are possible.
		Node[][] level = world.getLevelMatrix();
		
		for (int height = 3; height <= 5; height++) { //Tested for different height of the ceilings
			//Makes the ceiling
			for (int i = 0; i < level.length; i++) {
				level[i][marioNode.y - height] = new Node(marioNode.x + i - 11, marioNode.y - height , (byte) -10);
			}
			final int currentHeight = height;
			SpeedNode startNode = new SpeedNode(marioNode, 0, Long.MAX_VALUE); //Starts at the normal speed
			for (DirectedEdge edge : marioNode.getEdges()) {
				final SpeedNode sn = new SpeedNode(edge.target, startNode	, edge, 0, world); //Don't care about the hash.
				
				if (sn.isSpeedNodeUseable(world)) { //Does not take the ceiling into account
                                    throw new Error("Fix test by fixing below commented code");
					/*if (Arrays.asList(sn.getMoveInfo().getYPositions()).stream().anyMatch(position -> position + 2 > currentHeight - 1)) { //If it collides with the ceiling
						assertTrue(sn.getMoveInfo().hasCollisions(startNode, world));
					} else {
						assertFalse(sn.getMoveInfo().hasCollisions(startNode, world));
					}*/
				} 
			}
			//Removes it again.
			for (int i = 0; i < level.length; i++) {
				level[i][marioNode.y - height] = null;
			}
		}
		
			
	}

	
	@Test	
	public void testStandardMovementsAtDifferentSpeeds(){
		World world = TestEdgeCreator.totalFlatland(flatlandWorld(),marioNode);
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode);
		
		//For all movements, given an arbitrary starting speed, since he starts on a flat world,
		//there shouldn't be any collisions:
		for (float speed = (float) -0.35; speed < 0.35; speed += 0.01) {
			SpeedNode startNode = new SpeedNode(marioNode, speed, Long.MAX_VALUE);
			for (DirectedEdge edge : marioNode.getEdges()) {
				final SpeedNode sn = new SpeedNode(edge.target, startNode	, edge, 0, world); //Don't care about the hash.
				
				if (sn.isSpeedNodeUseable(world)) {
					assertFalse(sn.getMoveInfo().hasCollisions(startNode, world));
				} 
			}
		}
	}
	
	@Test
	public void testStepwiseJumpIntoCeiling(){
		fail("Make the test");
	}
	
	@Test
	public void testStepwiseRunIntoWall(){
		fail("Make the test");		
	}
	
	@Test
	public void testSimulatedRuns(){
		fail("Make the test");
	}

	@Test
	public void testCollisionWithAccellerationOverLimit(){
		fail("Make the test");
	}
	
	@Test
	public void testStepwiseRaiseIntoCeiling(){

		World world = flatlandWorld();
		world = TestEdgeCreator.totalFlatland(world, marioNode);
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode);
		Node[][] level = world.getLevelMatrix();
		
		
		//For all positions on the seen level:
		for (float i = 0; i < EdgeCreator.GRID_WIDTH; i += 0.05) {
			//Gradually raise into the ceiling:
			Point2D.Float currentOffset = new Point2D.Float(0, 0);
			Point2D.Float futureOffset;
			for (float j = 0; j < 14; j += 0.05) {
				//j does not need to be inverted, as we want this to be an upwards motion, and it is inverted in the method.
				futureOffset = new Point2D.Float(0, j); 
				
				//Starts at the normal speed. Doesn't have any significans.
				//Starts from the top. 
				Node fakeNode = new Node((int) (level[(int) i][9].x) , 14, (byte) -10);
				SpeedNode startNode = new SpeedNode(fakeNode, 0, Long.MAX_VALUE); 
				float lastYPosition = 14; //Lets just say that the jump ends at y=0.
				boolean hasCollision = world.isColliding(futureOffset.x, futureOffset.y, currentOffset.x, currentOffset.y, startNode, lastYPosition);
				String errorMessage = "Error at height = " + j + ", at i = " + i;
				//TODO discuss if this is fine, and the desired result. (*)
				// 1.0/16 needs to be added, as this is subtracted in the method, and not added later.
				if (14 - j - CollisionDetection.MARIO_HEIGHT/16 - 1.0/16 <= 10 ) { //TODO check should it also hold true with j=9?
					//If he is raised so that he collides with the floor, there should be a collision
					//hasCollision = CollisionDetection.isColliding(futureOffset, currentOffset, startNode);
					assertTrue(errorMessage, 	hasCollision);
				} else{
					if (hasCollision) {
						hasCollision = world.isColliding(futureOffset.x, futureOffset.y, currentOffset.x, currentOffset.y, startNode, lastYPosition);
					}
					assertFalse(errorMessage,	hasCollision);
				}
				//currentOffset = futureOffset;
			}
		}
	}
	
	@Test
	public void testCollisionDetectionOffCurentView(){
		fail("Make the test");
	}

	@Test
	public void testStepwiseLowerIntoFloor(){
		
		World world = flatlandWorld();
		world = TestEdgeCreator.totalFlatland(world, marioNode);
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode);
		Node[][] level = world.getLevelMatrix();
		
		
		//For all positions on the seen level:
		for (float i = 0; i < EdgeCreator.GRID_WIDTH; i += 0.05) {
			//Gradually lowered into the floor:
			Point2D.Float currentOffset = new Point2D.Float(0, 0);
			Point2D.Float futureOffset;
			for (float j = 0; j < 14; j += 0.05) {
				futureOffset = new Point2D.Float(0, -j); //It needs to be negative j, as it is inverted in the method.
				
				//Starts at the normal speed. Doesn't have any significans.
				//Starts from the top	. 
				Node fakeNode = new Node((int) (level[(int) i][9].x) , 0, (byte) -11);
				SpeedNode startNode = new SpeedNode(fakeNode, 0, Long.MAX_VALUE); 
				float lastYPosition = 14; //Lets just say that the fall ends at y=14.
				boolean hasCollision = world.isColliding(futureOffset.x, futureOffset.y, currentOffset.x, currentOffset.y, startNode, lastYPosition);
				String errorMessage = "Error at height = " + j + ", at i = " + i;
				
				if (9  <= j) { //TODO check should it also hold true with j=9?
					//If he is lowered below the floor, he should have a collision.
					assertTrue(errorMessage,hasCollision);
				} else{
					assertFalse(errorMessage,hasCollision);
				}
				//currentOffset = futureOffset;
			}
		}
	}
	
	@Test
	public void testIgnoreFloorIfLastPosition(){
		
		World world = flatlandWorld();
		world = TestEdgeCreator.totalFlatland(world, marioNode);
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode);
		Node[][] level = world.getLevelMatrix();
		
		fail("Make the test");
	}
	
	
	@Test
	public void testStepwise(){
		fail("Make the test");
	}

	@Test
	public void testBoxSlow() {
		testBox(false);
	}
	@Test
	public void testBoxFast() {
		testBox(true);
	}
	private void testBox(boolean useSuperSpeed) {
		final UnitTestAgent agent = new UnitTestAgent();
		final Environment observation = TestTools.loadLevel("testCollisionDetector/collisionBox.lvl", agent, false);
		TestTools.setMarioXPosition(observation, 4);
		final World world = new World();
		world.initialize(observation);
		
		agent.action[Mario.KEY_LEFT] = true;
		for (int i = 0; i < 100; i++) {
			TestTools.runOneTick(observation);
		}
		TestTools.runOneTick(observation);
		world.update(observation);
		float marioX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		float marioY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		ArrayList<DirectedEdge> path = PathHelper.createPath((int)marioX, (int)marioY, -1, 0, 0, 1, world, useSuperSpeed);
		assertTrue(path.get(0).getMoveInfo().hasCollisions(marioX, marioY, world));
		
		
		
		
		agent.action[Mario.KEY_LEFT] = false;
		agent.action[Mario.KEY_RIGHT] = true;
		for (int i = 0; i < 100; i++) {
			TestTools.runOneTick(observation);
		}
		
		marioX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		marioY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		path = PathHelper.createPath((int)marioX, (int)marioY, 1, 0, 0, 1, world, useSuperSpeed);
		assertTrue(path.get(0).getMoveInfo().hasCollisions(marioX, marioY, world));
	}
	
	@Test
	public void testJumpRightSlow() {
		for (int jumpLength = 1; jumpLength <= 4; jumpLength++) {
			for (int jumpHeight = 1; jumpHeight <= 4; jumpHeight++) {
				testJumpRight(jumpLength, jumpHeight, false);
			}
		}
	}
	@Test
	public void testJumpRightFast() {
		for (int jumpLength = 1; jumpLength <= 4; jumpLength++) {
			for (int jumpHeight = 1; jumpHeight <= 4; jumpHeight++) {
				testJumpRight(jumpLength, jumpHeight, true);
			}
		}
	}
	
	private void testJumpRight(int jumpLength, int jumpHeight, boolean useSuperSpeed) {
		final UnitTestAgent agent = new UnitTestAgent();
		final Environment observation = TestTools.loadLevel("testCollisionDetector/jumpDown-" + jumpHeight + ".lvl", agent, false);
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		world.initialize(observation);
		
		final int startXPixel = (7 - jumpLength) * World.PIXELS_PER_BLOCK;
		final int endXPixel = 7 * World.PIXELS_PER_BLOCK;
		
		final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, jumpLength, jumpHeight, -jumpHeight, 1, world, useSuperSpeed);
		
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		for (int i = startXPixel; i <= endXPixel; i++) {
			TestTools.setMarioPixelPosition(observation, i, Math.round(startMarioYPos * World.PIXELS_PER_BLOCK));
			TestTools.resetMarioSpeed(observation);
			TestTools.runOneTick(observation);
			TestTools.runOneTick(observation);
			world.update(observation);
			marioControls.reset();
			
			final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			final ArrayList<DirectedEdge> pathCopy = new ArrayList<DirectedEdge>();
			pathCopy.add(path.get(0));
			
			final boolean expectedToHitSomething = !pathCopy.get(0).getMoveInfo().hasCollisions(marioXPos, Math.round(marioYPos), world);
			final boolean actualHitSomething = isFollowingPathCorrectly(observation, pathCopy, agent, marioControls);
			
			assertEquals(expectedToHitSomething, actualHitSomething);
		}
	}

	private boolean isFollowingPathCorrectly(Environment observation, ArrayList<DirectedEdge> path, UnitTestAgent agent, MarioControls marioControls) {
		final float startMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		float xOffset = 0;
		float yOffset = 0;
		agent.action = marioControls.getActions();
		TestTools.runOneTick(observation);
		final int pathSize = path.size();
		for (int z = 0; z < pathSize; z++) {	
			final DirectedEdge edge = path.get(0);
			final MovementInformation moveInfo = edge.getMoveInfo();
			for (int i = 0; i < moveInfo.getMoveTime(); i++) {				
				marioControls.getNextAction(observation, path);
				TestTools.runOneTick(observation);
				
				//if you want to slow down the simulation
				/*try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				final float expectedMarioXPos = startMarioXPos + moveInfo.getXPositions()[i] + xOffset;
				final float expectedMarioYPos = startMarioYPos - moveInfo.getYPositions()[i] + yOffset;
				//System.out.println("Expected (" + expectedMarioXPos*16 + ", " + expectedMarioYPos*16 + ")");
				
				final float actualMarioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
				final float actualMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
				//System.out.println("Actual   (" + actualMarioXPos*16 + ", " + actualMarioYPos*16 + ")");
				
				if (!withinAcceptableError(expectedMarioXPos, actualMarioXPos, actualMarioYPos, expectedMarioYPos)) {
					Arrays.fill(agent.action, false);
					return false;
				}
			}
			xOffset += moveInfo.getXPositions()[moveInfo.getXPositions().length - 1];
			yOffset += moveInfo.getYPositions()[moveInfo.getYPositions().length - 1];
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
	
	
	@Test
	public void testCollisionWithEdgeRightSlow() {
		final World world = new World();
		for (int i = 2; i < 13; i++) {
			final int[] moveDirection = new int[i*4];
			Arrays.fill(moveDirection, i * 0, i * 1,  1);
			Arrays.fill(moveDirection, i * 1, i * 2, -1);
			Arrays.fill(moveDirection, i * 2, i * 3,  1);
			Arrays.fill(moveDirection, i * 3, i * 4, -1);
			
			final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, moveDirection, 0, 0, i * 4, world, false);
			verifyCollision("collisionWithEdge.lvl", path, world);
		}
	}
	@Test
	public void testCollisionWithEdgeRightFast() {
		final World world = new World();
		for (int i = 2; i < 13; i++) {
			final int[] moveDirection = new int[i*4];
			Arrays.fill(moveDirection, i * 0, i * 1,  1);
			Arrays.fill(moveDirection, i * 1, i * 2, -1);
			Arrays.fill(moveDirection, i * 2, i * 3,  1);
			Arrays.fill(moveDirection, i * 3, i * 4, -1);
			
			final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, moveDirection, 0, 0, i * 4, world, true);
			verifyCollision("collisionWithEdge.lvl", path, world);
		}
	}
	
	@Test
	public void testCollisionWithEdgeLeftSlow() {
		final World world = new World();
		for (int i = 2; i < 13; i++) {
			final int[] moveDirection = new int[i*4];
			Arrays.fill(moveDirection, i * 0, i * 1, -1);
			Arrays.fill(moveDirection, i * 1, i * 2,  1);
			Arrays.fill(moveDirection, i * 2, i * 3, -1);
			Arrays.fill(moveDirection, i * 3, i * 4,  1);
			
			final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, moveDirection, 0, 0, i * 4, world, false);
			verifyCollision("collisionWithEdge.lvl", path, world);
		}
	}
	@Test
	public void testCollisionWithEdgeLeftFast() {
		final World world = new World();
		for (int i = 2; i < 13; i++) {
			final int[] moveDirection = new int[i*4];
			Arrays.fill(moveDirection, i * 0, i * 1, -1);
			Arrays.fill(moveDirection, i * 1, i * 2,  1);
			Arrays.fill(moveDirection, i * 2, i * 3, -1);
			Arrays.fill(moveDirection, i * 3, i * 4,  1);
			
			final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, moveDirection, 0, 0, i * 4, world, true);
			verifyCollision("collisionWithEdge.lvl", path, world);
		}
	}
	@Test
	public void testCollisionWithWallRightSlow() {
		final World world = new World();
		for (int i = 2; i < 13; i++) {

			final int[] moveDirection = new int[i*4];
			Arrays.fill(moveDirection, i * 0, i * 1,  1);
			Arrays.fill(moveDirection, i * 1, i * 2, -1);
			Arrays.fill(moveDirection, i * 2, i * 3,  1);
			Arrays.fill(moveDirection, i * 3, i * 4, -1);
			
			final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, moveDirection, 0, 0, i * 4, world, false);
			verifyCollision("collisionWithWall.lvl", path, world);
			verifyCollision("collisionWithGrassWall.lvl", path, world);
		}
	}
	@Test
	public void testCollisionWithWallRightFast() {
		final World world = new World();
		for (int i = 2; i < 13; i++) {
			final int[] moveDirection = new int[i*4];
			Arrays.fill(moveDirection, i * 0, i * 1,  1);
			Arrays.fill(moveDirection, i * 1, i * 2, -1);
			Arrays.fill(moveDirection, i * 2, i * 3,  1);
			Arrays.fill(moveDirection, i * 3, i * 4, -1);
			
			final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, moveDirection, 0, 0, i * 4, world, true);
			verifyCollision("collisionWithWall.lvl", path, world);
			verifyCollision("collisionWithGrassWall.lvl", path, world);
		}
	}
	
	@Test
	public void testCollisionWithWallLeftSlow() {
		final World world = new World();
		for (int i = 2; i < 13; i++) {
			final int[] moveDirection = new int[i*4];
			Arrays.fill(moveDirection, i * 0, i * 1, -1);
			Arrays.fill(moveDirection, i * 1, i * 2,  1);
			Arrays.fill(moveDirection, i * 2, i * 3, -1);
			Arrays.fill(moveDirection, i * 3, i * 4,  1);
			
			final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, moveDirection, 0, 0, i * 4, world, false);
			verifyCollision("collisionWithWall.lvl", path, world);
			verifyCollision("collisionWithGrassWall.lvl", path, world);
		}
	}
	@Test
	public void testCollisionWithWallLeftFast() {
		final World world = new World();
		for (int i = 2; i < 13; i++) {
			final int[] moveDirection = new int[i * 4];
			Arrays.fill(moveDirection, i * 0, i * 1, -1);
			Arrays.fill(moveDirection, i * 1, i * 2,  1);
			Arrays.fill(moveDirection, i * 2, i * 3, -1);
			Arrays.fill(moveDirection, i * 3, i * 4,  1);
			
			final ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, moveDirection, 0, 0, i * 4, world, true);
			verifyCollision("collisionWithWall.lvl", path, world);
			verifyCollision("collisionWithGrassWall.lvl", path, world);
		}
	}
	
	
	private void verifyCollision(String levelName, ArrayList<DirectedEdge> path, World world) {
		final UnitTestAgent agent = new UnitTestAgent();
		final Environment observation = TestTools.loadLevel("testCollisionDetector/" + levelName, agent, false);
		final MarioControls marioControls = new MarioControls();
		world.initialize(observation);
		
		final int marioY = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		TestTools.setMarioPosition(observation, 9, marioY + 8);
		TestTools.runOneTick(observation);
		world.update(observation);
		
		final int centerMarioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		
		final int startXPixel = (centerMarioXPos - 5) * World.PIXELS_PER_BLOCK;
		final int endXPixel   = (centerMarioXPos + 5) * World.PIXELS_PER_BLOCK;

		final float startMarioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		
		for (int i = startXPixel; i <= endXPixel; i++) { //TODO set i back.
			TestTools.setMarioPixelPosition(observation, i, Math.round(startMarioYPos * World.PIXELS_PER_BLOCK));
			TestTools.resetMarioSpeed(observation);
			TestTools.runOneTick(observation);
			TestTools.runOneTick(observation);
			world.update(observation);
			marioControls.reset();
			
			final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			final ArrayList<DirectedEdge> pathCopy = new ArrayList<DirectedEdge>();
			path.forEach(x -> pathCopy.add(x));
			
			boolean expectedToFollowPath = false;
			float xOffset = 0;
			for (DirectedEdge directedEdge : pathCopy) {
				//System.out.println("Collision:");
				expectedToFollowPath = !directedEdge.getMoveInfo().hasCollisions(marioXPos + xOffset, Math.round(marioYPos), world);
				xOffset += directedEdge.getMoveInfo().getXMovementDistance();
				if (!expectedToFollowPath) {
					break;
				}
			}
			//System.out.println("Verification: ");
			final boolean actualFollowPath = isFollowingPathCorrectly(observation, pathCopy, agent, marioControls);
			if (expectedToFollowPath ^ actualFollowPath) { //logical xor
				System.out.println(i);
			}
			assertEquals(expectedToFollowPath, actualFollowPath);
		}
	}
}