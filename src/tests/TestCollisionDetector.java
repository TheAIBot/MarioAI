package tests;

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
import MarioAI.graph.nodes.StateNode;
import MarioAI.marioMovement.MarioControls;
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
		world = TestGrapher.totalFlatland(world, marioNode);//Adds a wall later, to force it to run into the wall.
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode); //Again, edge before the wall.
		Node[][] level = world.getLevelMatrix();
		//adding the walls:
		final int WALL_HEIGHT = 8; //Testing up to size 8 wall. Should all collide there.
		//TODO change size back to 1
		for (int size = 1; size <= WALL_HEIGHT; size++) {			
			short i = 1; //TODO change back to 1
			for (; i < GRID_WIDTH/2; i++) {
				TestGrapher.addWall(size, 11 - i - 1, marioNode.y, level, marioNode); //Left wall
				TestGrapher.addWall(size, 11 + i, marioNode.y, level, marioNode); //Right wall
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
				StateNode currentRight = new StateNode(rightWalkingPath.get(0).source, marioRightSpeed, Long.MAX_VALUE);
				StateNode currentLeft = new StateNode(leftWalkingPath.get(0).source, marioLeftSpeed, Long.MAX_VALUE);
				for (int j = 0; j < rightWalkingPath.size(); j++) { //Going through the paths.		
					DirectedEdge currentRightEdge = rightWalkingPath.get(j);		
					DirectedEdge currentLeftEdge 	= leftWalkingPath.get(j);
					
					final StateNode snRight = new StateNode(currentRightEdge.target, currentRight	,
							                                  currentRightEdge, 0, world); //Don't care about the hash.
					final StateNode snLeft 	= new StateNode(currentLeftEdge.target	, currentLeft	,
							                                  currentLeftEdge	, 0, world); //Don't care about the hash.
					if (!snRight.isSpeedNodeUseable() ||
						 !snLeft .isSpeedNodeUseable()) {
						fail();
					}	
					String errorMessage = "Size = " + size + ", i = " + i + ", j =" + j;
					System.out.println(errorMessage);
					boolean shouldHaveCollisionRight = false;
					boolean shouldHaveCollisionLeft = false;
					
					for (int k = 0; k < snRight.getMoveInfo().getPositions().length; k++) {
						Point2D.Float currentPosition = snRight.getMoveInfo().getPositions()[k];
						//Starting at the middle of the block + offset.
						double rightMarioPlace = snRight.parentXPos + currentPosition.getX(); 
						if ((11+i <= rightMarioPlace + CollisionDetection.MARIO_WIDTH/(16) && 
							          rightMarioPlace + CollisionDetection.MARIO_WIDTH/(16) <= 11+i+1 
							  && 
							  ((Math.floor(snRight.parentXPos) != Math.floor(rightMarioPlace + 0.25) && size == 1) || size != 1)
							  )  ) {
							shouldHaveCollisionRight = true;
							break;
						}
					}
					
					for (int k = 0; k < snRight.getMoveInfo().getPositions().length; k++) {
						Point2D.Float currentPosition = snLeft.getMoveInfo().getPositions()[k];
						//Starting at the middle of the block + offset.
						double leftMarioPlace = snLeft.parentXPos + currentPosition.getX(); 
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
				TestGrapher.removeWall(size, 11 - i - 1, marioNode.y, level);
				TestGrapher.removeWall(size, 11 + i, marioNode.y, level);
			}
		}	
	}
	
	@Test
	public void testJumpIntoCeiling(){
		//TODO go trough the test step by step, and check it is correct.
		World world = TestGrapher.totalFlatland(flatlandWorld(),marioNode);
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode); //Edges are made before the ceiling, to ensure that the movements are possible.
		Node[][] level = world.getLevelMatrix();
		
		for (int height = 3; height <= 5; height++) { //Tested for different height of the ceilings
			//Makes the ceiling
			for (int i = 0; i < level.length; i++) {
				level[i][marioNode.y - height] = new Node(marioNode.x + i - 11, marioNode.y - height , (byte) -10);
			}
			final int currentHeight = height;
			StateNode startNode = new StateNode(marioNode, 0, Long.MAX_VALUE); //Starts at the normal speed
			for (DirectedEdge edge : marioNode.getEdges()) {
				final StateNode sn = new StateNode(edge.target, startNode	, edge, 0, world); //Don't care about the hash.
				
				if (sn.isSpeedNodeUseable()) { //Does not take the ceiling into account
					if (Arrays.asList(sn.getMoveInfo().getPositions()).stream().anyMatch(position -> position.y + 2 > currentHeight - 1)) { //If it collides with the ceiling
						assertTrue(sn.getMoveInfo().hasCollisions(startNode, world));
					} else {
						assertFalse(sn.getMoveInfo().hasCollisions(startNode, world));
					}
				} 
			}
			//Removes it again.
			for (int i = 0; i < level.length; i++) {
				level[i][marioNode.y - height] = null;
			}
		}
		
			
	}

	@Test
	public void testSmallMarioJumpIntoCeiling(){
		fail("Make the test");		
	}
	
	@Test	
	public void testStandardMovementsAtDifferentSpeeds(){
		World world = TestGrapher.totalFlatland(flatlandWorld(),marioNode);
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(world, marioNode);
		
		//For all movements, given an arbitrary starting speed, since he starts on a flat world,
		//there shouldn't be any collisions:
		for (float speed = (float) -0.35; speed < 0.35; speed += 0.01) {
			StateNode startNode = new StateNode(marioNode, speed, Long.MAX_VALUE);
			for (DirectedEdge edge : marioNode.getEdges()) {
				final StateNode sn = new StateNode(edge.target, startNode	, edge, 0, world); //Don't care about the hash.
				
				if (sn.isSpeedNodeUseable()) {
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
		world = TestGrapher.totalFlatland(world, marioNode);
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
				StateNode startNode = new StateNode(fakeNode, 0, Long.MAX_VALUE); 
				float lastYPosition = 14; //Lets just say that the jump ends at y=0.
				boolean hasCollision = world.isColliding(futureOffset, currentOffset, startNode,lastYPosition);
				String errorMessage = "Error at height = " + j + ", at i = " + i;
				//TODO discuss if this is fine, and the desired result. (*)
				// 1.0/16 needs to be added, as this is subtracted in the method, and not added later.
				if (14 - j - CollisionDetection.MARIO_HEIGHT/16 - 1.0/16 <= 10 ) { //TODO check should it also hold true with j=9?
					//If he is raised so that he collides with the floor, there should be a collision
					//hasCollision = CollisionDetection.isColliding(futureOffset, currentOffset, startNode);
					assertTrue(errorMessage, 	hasCollision);
				} else{
					if (hasCollision) {
						hasCollision = world.isColliding(futureOffset, currentOffset, startNode,lastYPosition);
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
		world = TestGrapher.totalFlatland(world, marioNode);
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
				StateNode startNode = new StateNode(fakeNode, 0, Long.MAX_VALUE); 
				float lastYPosition = 14; //Lets just say that the fall ends at y=14.
				boolean hasCollision = world.isColliding(futureOffset, currentOffset, startNode, lastYPosition);
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
		world = TestGrapher.totalFlatland(world, marioNode);
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
	public void testCompareWithMariosCollisionEngine(){
		fail("Make the test");
	}
	
	@Test
	public void testBox() {
		final UnitTestAgent agent = new UnitTestAgent();
		final Environment observation = TestTools.loadLevel("testCollisionDetector/collisionBox.lvl", agent, true);
		TestTools.setMarioXPosition(observation, 4);
		//TestTools.renderLevel(observation);
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
		
		ArrayList<DirectedEdge> path = PathHelper.createPath((int)marioX, (int)marioY, -1, 0, 0, 1, world, false);
		assertTrue(path.get(0).getMoveInfo().hasCollisions(marioX, marioY, world));
		
		
		
		
		agent.action[Mario.KEY_LEFT] = false;
		agent.action[Mario.KEY_RIGHT] = true;
		for (int i = 0; i < 100; i++) {
			TestTools.runOneTick(observation);
		}
		
		marioX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		marioY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		path = PathHelper.createPath((int)marioX, (int)marioY, 1, 0, 0, 1, world, false);
		assertTrue(path.get(0).getMoveInfo().hasCollisions(marioX, marioY, world));
	}
	
}
