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

import MarioAI.World;
import MarioAI.graph.CollisionDetection;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.EdgeCreator;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.environments.Environment;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCollisionDetector {
	MarioControls marioControler = new MarioControls();
	Environment observation;
	EdgeCreator grapher = new EdgeCreator();
	private static final int GRID_WIDTH = 22;
	public Node marioNode;
	public DirectedEdge runningEdgeType = new RunningEdge(null, null);
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
		CollisionDetection.setWorld(world);
		CollisionDetection.loadTileBehaviors();
		Node[][] level = world.getLevelMatrix();
		//adding the walls:
		final int WALL_HEIGHT = 8; //Testing up to size 8 wall. Should all collide there.
		for (int size = 1; size <= WALL_HEIGHT; size++) {			
			short i = 1;
			for (; i < GRID_WIDTH/2; i++) {
				TestGrapher.addWall(size, 11 - i, marioNode.y, level, marioNode); //Left wall
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
				SpeedNode currentRight = new SpeedNode(rightWalkingPath.get(0).source, marioRightSpeed, Long.MAX_VALUE);
				SpeedNode currentLeft = new SpeedNode(leftWalkingPath.get(0).source, marioLeftSpeed, Long.MAX_VALUE);
				for (int j = 0; j < rightWalkingPath.size(); j++) { //Going through the paths.		
					DirectedEdge currentRightEdge = rightWalkingPath.get(j);		
					DirectedEdge currentLeftEdge 	= leftWalkingPath.get(j);
					final SpeedNode snRight = new SpeedNode(currentRightEdge.target, currentRight	, currentRightEdge, 0); //Don't care about the hash.
					final SpeedNode snLeft 	= new SpeedNode(currentLeftEdge.target	, currentLeft	, currentLeftEdge	, 0); //Don't care about the hash.
					if (!snRight.isSpeedNodeUseable() ||
						 !snLeft.isSpeedNodeUseable()) {
						fail();
					}	
					String errorMessage = "Size = " + size + ", i = " + i + ", j =" + j;
					System.out.println(errorMessage);
					boolean shouldHaveCollision = false;
					for (int k = 0; k < snRight.getMoveInfo().getPositions().length; k++) {
						Point2D.Float currentPosition = snRight.getMoveInfo().getPositions()[k];
						double rightMarioPlace = snRight.parentXPos + currentPosition.getX() + 0.5;
						if ((11+i <= rightMarioPlace + CollisionDetection.MARIO_WIDTH/(2*16) && 
							  rightMarioPlace + CollisionDetection.MARIO_WIDTH/(2*16) <= 11+i+1 
							  && Math.floor(snRight.parentXPos) != Math.floor(rightMarioPlace + 0.25)) 
							     ||
							 (11+i <= rightMarioPlace - CollisionDetection.MARIO_WIDTH/(2*16) && 
							 rightMarioPlace - CollisionDetection.MARIO_WIDTH/(2*16) <= 11+i+1 && 
							 Math.floor(snRight.parentXPos) != Math.floor(rightMarioPlace - CollisionDetection.MARIO_WIDTH/(2*16)))) {
							shouldHaveCollision = true;
							break;
						}
					}
					//If he should collide with the block
					if (shouldHaveCollision) {
						assertTrue(errorMessage, snRight.getMoveInfo().hasCollisions(currentRight));
						assertTrue(errorMessage + ". There are no collision.", snLeft.getMoveInfo().hasCollisions(currentLeft));
					} else{
						assertFalse(errorMessage , snRight.getMoveInfo().hasCollisions(currentRight));
						assertFalse(errorMessage + ". There are a collision.", snLeft.getMoveInfo().hasCollisions(currentLeft));
					}
					currentRight 	= snRight;
					currentLeft 	= snLeft;					
				}				
				//removing the walls:
				TestGrapher.removeWall(size, 11 - i, marioNode.y, level);
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
		CollisionDetection.setWorld(world);
		CollisionDetection.loadTileBehaviors();
		Node[][] level = world.getLevelMatrix();
		
		for (int height = 3; height <= 5; height++) { //Tested for different height of the ceilings
			//Makes the ceiling
			for (int i = 0; i < level.length; i++) {
				level[i][marioNode.y - height] = new Node(marioNode.x + i - 11, marioNode.y - height , (byte) 11);
			}
			final int currentHeight = height;
			SpeedNode startNode = new SpeedNode(marioNode, 0, Long.MAX_VALUE); //Starts at the normal speed
			for (DirectedEdge edge : marioNode.getEdges()) {
				final SpeedNode sn = new SpeedNode(edge.target, startNode	, edge, 0); //Don't care about the hash.
				
				if (sn.isSpeedNodeUseable()) { //Does not take the ceiling into account7
					if (Arrays.asList(sn.getMoveInfo().getPositions()).stream().anyMatch(position -> position.y + 2 > currentHeight - 1)) { //If it collides with the ceiling
						assertTrue(sn.getMoveInfo().hasCollisions(startNode));
					} else {
						assertFalse(sn.getMoveInfo().hasCollisions(startNode));
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
		CollisionDetection.setWorld(world);
		CollisionDetection.loadTileBehaviors();
		
		//For all movements, given an arbitrary starting speed, since he starts on a flat world,
		//there shouldn't be any collisitions:
		for (float speed = (float) -0.35; speed < 0.35; speed += 0.01) {
			SpeedNode startNode = new SpeedNode(marioNode, speed, Long.MAX_VALUE);
			for (DirectedEdge edge : marioNode.getEdges()) {
				final SpeedNode sn = new SpeedNode(edge.target, startNode	, edge, 0); //Don't care about the hash.
				
				if (sn.isSpeedNodeUseable()) {
					assertFalse(sn.getMoveInfo().hasCollisions(startNode));
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
		CollisionDetection.setWorld(world);
		CollisionDetection.loadTileBehaviors();
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
				Node fakeNode = new Node((int) (level[(int) i][9].x) , 14, (byte) 12);
				SpeedNode startNode = new SpeedNode(fakeNode, 0, Long.MAX_VALUE); 
				boolean hasCollision = CollisionDetection.isColliding(futureOffset, currentOffset, startNode);
				String errorMessage = "Error at height = " + j + ", at i = " + i;
				//Plus 1.0/16, as 2.0/16 is added/subtracted, and then 1.0/16 is subtracted/added, inside the method.
				//TODO discuss if this is fine, and the desired result. (*)
				if (14 - j - CollisionDetection.MARIO_HEIGHT/16 <= 10 ) { //TODO check should it also hold true with j=9?
					//If he is lowered below the floor, he should have a collision.
					hasCollision = CollisionDetection.isColliding(futureOffset, currentOffset, startNode);
					assertTrue(errorMessage,hasCollision);
				} else{
					if (hasCollision) {
						hasCollision = CollisionDetection.isColliding(futureOffset, currentOffset, startNode);
					}
					assertFalse(errorMessage,hasCollision);
				}
				//currentOffset = futureOffset;
			}
		}
		fail("Make the test.");
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
		CollisionDetection.setWorld(world);
		CollisionDetection.loadTileBehaviors();
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
				Node fakeNode = new Node((int) (level[(int) i][9].x) , 0, (byte) 12);
				SpeedNode startNode = new SpeedNode(fakeNode, 0, Long.MAX_VALUE); 
				boolean hasCollision = CollisionDetection.isColliding(futureOffset, currentOffset, startNode);
				String errorMessage = "Error at height = " + j + ", at i = " + i;
				//Plus 1.0/16, as 2.0/16 is added/subtracted, and then 1.0/16 is subtracted/added, inside the method.
				//TODO discuss if this is fine, and the desired result. (*)
				if (9 + 1.0/16 <= j) { //TODO check should it also hold true with j=9?
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
	public void testStepwise(){
		fail("Make the test");
	}

	@Test
	public void testCompareWithMariosCollisionEngine(){
		fail("Make the test");
	}
	
}
