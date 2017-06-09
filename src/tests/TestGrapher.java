package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.edgeCreation.Collision;
import MarioAI.graph.edges.edgeCreation.EdgeCreator;
import MarioAI.graph.edges.edgeCreation.JumpDirection;
import MarioAI.graph.nodes.Node;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.environments.Environment;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGrapher {
	EdgeCreator grapher = new EdgeCreator();
	Environment observation;
	private static final int GRID_WIDTH = 22;
	public Node marioNode;
	public DirectedEdge runningEdgeType = new RunningEdge(null, null, false);
	// TODO add more tests for collisions.

	private short getColoumnRelativeToMario(int xPosition) {
		// Assumes that node!=null.
		return (short) ((xPosition - marioNode.x) + GRID_WIDTH / 2);
	}

	public static short getXPositionFromColoumn(Node marioNode, int coloumn) {
		// Assumes that node!=null.
		return (short) (coloumn + marioNode.x - GRID_WIDTH / 2);
	}

	public World getStartLevelWorld(String level) {
		BasicAIAgent agent = new BasicAIAgent("");
		observation = TestTools.loadLevel(level, agent);
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
	public void testCorrectMarioStartPosition() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flat.lvl");
		grapher.setMovementEdges(graph, marioNode);
		assertEquals(2, marioNode.x);
		assertEquals(9, marioNode.y);
	}

	public static World totalFlatland(World graph, Node marioNode) {
		Node[][] level = graph.getLevelMatrix();
		for (short i = 0; i < GRID_WIDTH; i++) {
			level[i][marioNode.y] = new Node(getXPositionFromColoumn(marioNode, i), marioNode.y, (byte) -11); // TODO(*)
																// Error: try to set it to -11
																//Check the same method in TestCollisonDetection.
		}
		return graph;
	}

	@Test
	public void testRunningEdgesToNeighbors() {
		World graph = flatlandWorld();
		Node[][] world = graph.getLevelMatrix();
		assertTrue(marioNode.containsEdgeWithTargetAndType(marioNode.x - 1, marioNode.y, runningEdgeType));
		assertTrue(marioNode.containsEdgeWithTargetAndType(marioNode.x + 1, marioNode.y, runningEdgeType));
	}

	@Test
	public void testRunningEdgesAlongRow() {
		World graph = flatlandWorld();
		Node[][] world = graph.getLevelMatrix();
		// All the reachable nodes from the mario node:
		for (int i = 10; i < world.length - 1; i++) {
			Node currentNode = world[i][marioNode.y];
			assertEquals(2*2, currentNode.getNumberOfEdgesOfType(runningEdgeType));
			assertTrue(currentNode.containsEdgeWithTargetAndType(currentNode.x - 1, currentNode.y,
					runningEdgeType));
			assertTrue(currentNode.containsEdgeWithTargetAndType(currentNode.x + 1, currentNode.y,
					runningEdgeType));
		}
		// Edge node that should only point to right, as there is
		// nothing to the left:
		Node currentNode = world[9][marioNode.y];
		assertEquals(1, currentNode.getNumberOfEdgesOfType(runningEdgeType));
		assertTrue(currentNode.containsEdgeWithTargetAndType(currentNode.x + 1, currentNode.y,
				runningEdgeType));
		// The other nodes to the left are null, and thus a running edge
		// cannot point to them.
	}

	@Test
	public void testRunningEdgesAgainstWall() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flatWithBump.lvl");
		Node[][] world = graph.getLevelMatrix();
		grapher.setMovementEdges(graph, marioNode);
		// Mario shouldn't run to the right, only to the left:
		assertEquals(2, marioNode.getNumberOfEdgesOfType(runningEdgeType)); //2 for one speed running edge
		// To the left:
		assertTrue(marioNode.containsEdgeWithTargetAndType(marioNode.x - 1, marioNode.y, runningEdgeType));
	}

	@Test
	public void testCanJumpRight() {
		World graph = totalFlatland(flatlandWorld(), marioNode);
		grapher.setMovementEdges(graph, marioNode);
		boolean[] possibleJumpLenghts = new boolean[5];// 5 for the five
								// different
								// jump lengths
		for (DirectedEdge edge : marioNode.getEdges()) {
			if (edge instanceof JumpingEdge) {
				JumpingEdge polynomialEdge = (JumpingEdge) edge;
				if (marioNode.x < polynomialEdge.target.x) { // Goes
										// rightwards
					int index = (polynomialEdge.target.x - marioNode.x) - 1;
					possibleJumpLenghts[index] = true;
				}
			}
		}
		for (int i = 0; i < possibleJumpLenghts.length; i++) {
			assertTrue("Failure at lenght " + (i + 1) + ", have array:"
					+ Arrays.toString(possibleJumpLenghts), possibleJumpLenghts[i]);
		}
	}

	@Test
	public void testCanJumpLeft() {
		World graph = totalFlatland(flatlandWorld(), marioNode);
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(graph, marioNode);
		boolean[] possibleJumpLenghts = new boolean[5];// 5 for the five
								// different
								// jump lengths
		for (DirectedEdge edge : marioNode.getEdges()) {
			if (edge instanceof JumpingEdge) {
				JumpingEdge polynomialEdge = (JumpingEdge) edge;
				if (marioNode.x > polynomialEdge.target.x) { // Goes
										// leftwards
					int index = (marioNode.x - polynomialEdge.target.x) - 1;
					possibleJumpLenghts[index] = true;
				}
			}
		}
		for (int i = 0; i < possibleJumpLenghts.length; i++) {
			assertTrue("Failure at lenght " + (i + 1), possibleJumpLenghts[i]);
		}
	}

	@Test
	public void testJumpRightOverWall() {
		EdgeCreator grapher = new EdgeCreator();
		// This includes correct jump heights
		World graph = totalFlatland(flatlandWorld(), marioNode);
		Node[][] world = graph.getLevelMatrix();
		// adding the walls:

		// TODO change WALL_HEIGHT to be variable between 1 and 4.
		for (int WALL_HEIGHT = 2; WALL_HEIGHT <= 4; WALL_HEIGHT++) { // 4
										// is
										// marios
										// max
										// jump
										// height
			for (short i = 2; i <= 4; i++) {
				addWall(WALL_HEIGHT, 11 + i, marioNode.y, world, marioNode);
				List<DirectedEdge> newEdges = new ArrayList<DirectedEdge>();
				grapher.getPolynomialReachingEdges(marioNode, (short) 11, newEdges);
				System.out.println("meh");
				for (DirectedEdge directedEdge : newEdges) {

				}
			}
		}

		fail("Finish making the test.");

	}

	@Test
	public void testJumpLeftOverWall() {
		fail("Not implemented yet");
	}

	public static void addWall(int height, int coloumn, int row, Node[][] levelMatrix, Node marioNode) {
		for (short j = 1; j <= height; j++) {
			levelMatrix[coloumn][row - j] = new Node(getXPositionFromColoumn(marioNode, coloumn),
					(short) (row - j), (byte) -10);
		}
	}

	public static void removeWall(int height, int coloumn, int row, Node[][] levelMatrix) {
		for (short j = 1; j <= height; j++) {
			levelMatrix[coloumn][row - j] = null;
		}
	}

	@Test
	public void testJumpUpAgainstWall() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = totalFlatland(flatlandWorld(), marioNode);
		Node[][] world = graph.getLevelMatrix();
		// adding the walls:
		final int WALL_HEIGHT = 4;
		for (short i = 1; i <= 3; i++) {
			if (i == 0)
				continue;
			boolean hasJumpedAgainstWall = false;
			addWall(WALL_HEIGHT, 11 + i, marioNode.y, world, marioNode);
			grapher.clearAllEdges(world);
			grapher.setMovementEdges(graph, marioNode);
			List<DirectedEdge> newEdges = new ArrayList<DirectedEdge>();
			grapher.getPolynomialReachingEdges(marioNode, (short) 11, newEdges);
			for (DirectedEdge edge : newEdges) {
				if (edge instanceof JumpingEdge) {
					JumpingEdge polynomialEdge = (JumpingEdge) edge;
					// It has only jumped along the wall, if
					// the height of the jump without
					// collision at the wall,
					// is less than the walls height.
					if (edge.target == null) {
						System.out.println("Error");
					}
					if (polynomialEdge.f(11 + i) < (marioNode.y + WALL_HEIGHT)
							&& edge.target.x == getXPositionFromColoumn(marioNode, 11 + i)
							&& edge.target.y == marioNode.y - WALL_HEIGHT) {
						// Minus or plus because of f
						// gives height according to
						// higher up=greater y.
						hasJumpedAgainstWall = true;
					}
				}
			}
			assertTrue("Failure at wall distance: " + i, hasJumpedAgainstWall);
			// removing the wall:
			removeWall(WALL_HEIGHT, 11 + i, marioNode.y, world);
		}
	}

	public void testJumpingOneBlockAbove(){
		//The block above should stop all the different jumps, thus there should only be running edges.
		fail("Make the test");
	}
	
	@Test
	public void testAbleToJumpUpThroughCertainMaterials() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = totalFlatland(flatlandWorld(), marioNode);
		Node[][] world = graph.getLevelMatrix();
		for (int i = 0; i < world.length; i++) {
			world[i][(short) (marioNode.y - 3)] = new Node(getXPositionFromColoumn(marioNode, i),
					(short) (marioNode.y - 3), (byte) -11);
		}
		grapher.setMovementEdges(graph, marioNode); // TODO remove -1
								// after adding
								// possibility
								// for left
								// jump.
		for (int i = 0; i < world.length; i++) {
			boolean hasEdgeToUpperLevel = false;
			for (DirectedEdge edge : world[i][marioNode.y].getEdges()) {
				if (edge.target.y == marioNode.y - 3)
					hasEdgeToUpperLevel = true;
			}
			assertTrue("Error at coloumn: " + i, hasEdgeToUpperLevel);
		}
	}

	public void testJumpDownLedge(){		
		World graph = totalFlatland(flatlandWorld(), marioNode);
		Node[][] levelMatrix = graph.getLevelMatrix();
		//For any given pillar, at any given height, he should be able to jump down from it:
		for (int column = 0; column < EdgeCreator.GRID_WIDTH; column++) {
			for (int pillarHeight = 1; pillarHeight <= EdgeCreator.GRID_HEIGHT - 2 - marioNode.y; pillarHeight++) {
				addWall(marioNode.y, column, marioNode.y, levelMatrix, marioNode);				
				grapher.setMovementEdges(graph, marioNode);
				
			}
		}
	}
	
	@Test
	public void testNoJumpsThroughCeiling() {
		// TODO add edge-cases
		EdgeCreator grapher = new EdgeCreator();
		World graph = totalFlatland(flatlandWorld(), marioNode);
		Node[][] world = graph.getLevelMatrix();
		//TODO also make a test where it checks if it is only those edges that will lead to a crash, that are removed.
		for (int height = 3; height <= 7; height++) {
			//Adds the ceiling:
			for (int i = 0; i < world.length; i++) {
				world[i][(short) (marioNode.y - height)] = new Node(getXPositionFromColoumn(marioNode, i),
						(short) (marioNode.y - height), (byte) 12);
			}
			grapher.setMovementEdges(graph, marioNode); 
			//TODO check why mario can't jump left at height=4.
			System.out.println();
			// TODO remove -1
									// after adding
									// possibility
									// for left
									// jump.
			//It shouldn have any edges to the upper level:
			for (int i = 0; i < world.length; i++) {
				boolean hasEdgeToUpperLevel = false;
				for (DirectedEdge edge : world[i][marioNode.y].getEdges()) {
					if (edge.target.y == marioNode.y - height)
						hasEdgeToUpperLevel = true;
				}
				assertFalse("Error at coloumn: " + i, hasEdgeToUpperLevel);
			}
			//It should have jumps of a certain height, but not any grater:
			System.out.println();
			//TODO change i back
			for (int i = 0; i < world.length; i++) {
				Node currentNode = world[i][marioNode.y];
				boolean hasEdgeToExtremeHeights = false;
				System.out.println(currentNode);
				System.out.println(currentNode.edges.toString());
				System.out.println();
				boolean hasEdgesToRequiredHeights = false;
				for (DirectedEdge edge : currentNode.getEdges()) {
					// Math.round(getMaxY()) is the height of the jump/run, rounded (will always be relativly precise, 
					//as the jumps are in integer for).
					//TODO (*)change assumption if this is changed for the jumps.
					if (Math.round(edge.getMaxY()) >= height - 2)
						hasEdgeToExtremeHeights = true;
					else if (height - 2 > Math.round(edge.getMaxY()) && Math.round(edge.getMaxY()) >= height - 3) { //required heights.
						hasEdgesToRequiredHeights = true;
					}
					else{
						System.out.println();
					}
				}
				if (hasEdgeToExtremeHeights || !hasEdgesToRequiredHeights) {
					assertFalse("Error at coloumn: " + i + ", at height = " + height, hasEdgeToExtremeHeights);
					assertTrue("Error at coloumn: " + i + ", at height = " + height, hasEdgesToRequiredHeights);
					
				}
				
			}
			//Removes the ceiling:
			for (int i = 0; i < world.length; i++) {
				world[i][(short) (marioNode.y - height)] = null;
			}
		}
	}

	@Test
	public void testNoFallingThroughPlatform() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = totalFlatland(flatlandWorld(), marioNode);
		Node[][] world = graph.getLevelMatrix();
		for (short i = 0; i < world.length; i++) {
			world[i][marioNode.y] = new Node(i, (short) (marioNode.y + 3), (byte) -11);
		}
		grapher.setMovementEdges(graph, marioNode);
		for (int i = 0; i < world.length; i++) {
			for (DirectedEdge edge : world[i][marioNode.y].getEdges()) {
				assertTrue(edge.target.y == edge.source.y);
			}
		}
	}
	
	@Test
	public void testNoJumpingThroughWall() {
		//A level has been made for this:
		World world = getStartLevelWorld("noJumpsThroughWall2.lvl");
		world.update(observation);
		TestTools.setMarioPosition(observation, 15, 8);
		world.update(observation);
		TestTools.runOneTick(observation);
		marioNode = world.getMarioNode(observation);
		grapher.setMovementEdges(world, marioNode);
		Node[][] level = world.getLevelMatrix();
		for (int i = 9; i < level.length; i++) {
			if (level[i][10] != null) {
				String errorMessage = "Error at: i=" + i; 
				boolean jumpIntoTheCeiling = level[i][10].edges.stream().anyMatch(edge -> Math.round(edge.getMaxY())  > 1);
				boolean jumpThroughWall = level[i][10].edges.stream().anyMatch(edge -> edge.target.x >= 17);
				boolean jumpAwayFromFloor = level[i][10].edges.stream().anyMatch(edge -> edge.target.y != 10);
				if (jumpIntoTheCeiling || jumpThroughWall || jumpAwayFromFloor) {
					System.out.println();
				}
				assertFalse(errorMessage, jumpIntoTheCeiling);
				assertFalse(errorMessage, jumpThroughWall);
				assertFalse(errorMessage, jumpAwayFromFloor);
			}
		}
	}
	
	@Test
	public void testAlwaysSameResultOnSetEdges() {
		EdgeCreator grapher = new EdgeCreator();
		// Should get the same result doing multiple set edges on a
		// given level matrix.
		World graph1 = totalFlatland(flatlandWorld(), marioNode);
		Node[][] world1 = graph1.getLevelMatrix();

		World graph2 = totalFlatland(flatlandWorld(), marioNode);
		Node[][] world2 = graph2.getLevelMatrix();
		grapher.setMovementEdges(graph1, marioNode);
		// And multiple times:
		grapher.setMovementEdges(graph2, marioNode);
		grapher.setMovementEdges(graph2, marioNode);

		// Asserting that the result is the same for the two matrixes:
		for (int i = 0; i < world2.length; i++) {
			for (int j = 0; j < world2[i].length; j++) {
				if (world1[i][j] == null && world2[i][j] == null) {
					continue;
				} else if (world1[i][j] != null && world2[i][j] != null){
					//Nothing, the checks below should be made.					
				} else fail();
				// The number of edges going out from a given
				// Node should be the same:
				assertEquals(world1[i][j].getEdges().size(), world2[i][j].getEdges().size());
				// All edges in one list should also be in the
				// other list:
				for (DirectedEdge edge : world2[i][j].getEdges()) {
					// A little slow, but this test doesn't
					// take that much time.
					assertTrue(world1[i][j].getEdges().contains(edge));
				}
				// And the outher way around:
				for (DirectedEdge edge : world1[i][j].getEdges()) {
					// A little slow, but this test doesn't
					// take that much time.
					assertTrue(world2[i][j].getEdges().contains(edge));
				}
			}
		}

	}
	
	@Test
	public void testCollisonDetectionLoweringIntoFloor() {
		//In essence testing when exactly it should give a collision
		World world = totalFlatland(flatlandWorld(), marioNode); 
		grapher.setMovementEdges(world, marioNode);
		Node[][] level = world.getLevelMatrix();
		//Therefore, the tests can be made:
		
		//Testing the descending function:
		
		for (int i = 0; i < level.length; i++) {
			//They are at height 9.
			
			//Does not allow only the top to hit the ground, as this is not possible in reality.
			//Does not start from 10, as this would lead to no ground collision
			//TODO does i make sense that if j=10, then there should be Collision.HIT_NOTHING?
			for (float j = (float) 0; j >= 8.01; j += 0.05) {
				//Should only collide if Mario, at height = 1.8, is within the block, or on top of it.
				
				//Simply uses a random node for the starting node:
				Node startingPosition = new Node(2, 9, (byte) 22);
				Node targetPosition = new Node(5, 9, (byte) 22);
				List<DirectedEdge> listOfEdges = new ArrayList<DirectedEdge>();
				JumpingEdge polynomial = new JumpingEdge(null, null);
				polynomial.setToJumpPolynomial(startingPosition, 11, 5, 4);
				Collision rightwardsCollision = grapher.descendingPolynomial(j, j, i, Collision.HIT_NOTHING, polynomial, JumpDirection.LEFT_DOWNWARDS, startingPosition, listOfEdges);
				Collision leftwardsCollision = grapher.descendingPolynomial(j, j, i, Collision.HIT_NOTHING, polynomial, JumpDirection.RIGHT_DOWNWARDS, startingPosition, listOfEdges);
				
				String errorMessage = "Failure at i = " + i + ", height/j = " + j;
				if (j >= 9) {					
					assertEquals(errorMessage, Collision.HIT_GROUND, rightwardsCollision);
					assertEquals(errorMessage, Collision.HIT_GROUND, leftwardsCollision);
					assertEquals(2, listOfEdges.size());
					
					double delta = 0.01;
					//Checking if one got the correct edges:
					assertEquals(level[i][(int) Math.floor(j)], listOfEdges.get(0).target);
					assertEquals(startingPosition, listOfEdges.get(0).source);
					assertEquals(polynomial.getParameterA(), ((JumpingEdge) listOfEdges.get(0)).getParameterA() ,delta );
					assertEquals(polynomial.getParameterB(), ((JumpingEdge) listOfEdges.get(0)).getParameterB() ,delta);
					assertEquals(polynomial.getParameterC(), ((JumpingEdge) listOfEdges.get(0)).getParameterC() ,delta);
					
					assertEquals(level[i][(int) Math.floor(j)], listOfEdges.get(1).target);
					assertEquals(startingPosition, listOfEdges.get(1).source);
					assertEquals(polynomial.getParameterA(), ((JumpingEdge) listOfEdges.get(1)).getParameterA() ,delta);
					assertEquals(polynomial.getParameterB(), ((JumpingEdge) listOfEdges.get(1)).getParameterB() ,delta);
					assertEquals(polynomial.getParameterC(), ((JumpingEdge) listOfEdges.get(1)).getParameterC() ,delta);
				} else {
					assertEquals(errorMessage, Collision.HIT_NOTHING, rightwardsCollision);
					assertEquals(errorMessage, Collision.HIT_NOTHING, leftwardsCollision);
					assertEquals(0, listOfEdges.size());
				}
			}
		}
	}
	
	@Test	
	public void testCollisonDetectionUpIntoCeiling() {
		//In essence testing when exactly it should give a collision,
		//by slowly increasing marios y position
		//In essence testing when exactly it should give a collision
		
		World world = totalFlatland(flatlandWorld(), marioNode); 
		grapher.setMovementEdges(world, marioNode);
		Node[][] level = world.getLevelMatrix();
		
		//Testing the ascending function:
		
		for (int i = 0; i < level.length; i++) {
			//The blocks are at height = 9
			
			//Does not allow only the bottom to hit the ground, as this is not possible in reality.
			
			for (float j = (float) 14; j - EdgeCreator.MARIO_HEIGHT >= 9; j -= 0.05) {
				//Should only collide if Mario, at height = 1.8, is within the block, or on top of it.
				
				//Simply uses a random node for the starting node:
				Node startingPosition = new Node(2, 9, (byte) 22);
				Node targetPosition = new Node(5, 9, (byte) 22);
				List<DirectedEdge> listOfEdges = new ArrayList<DirectedEdge>();
				JumpingEdge polynomial = new JumpingEdge(null, null);
				polynomial.setToJumpPolynomial(startingPosition, 11, 5, 4);
				Collision rightwardsCollision = grapher.ascendingPolynomial(j, j, i, Collision.HIT_NOTHING, polynomial, JumpDirection.RIGHT_UPWARDS, startingPosition, listOfEdges);
				Collision leftwardsCollision = grapher.ascendingPolynomial(j, j, i, Collision.HIT_NOTHING, polynomial, JumpDirection.LEFT_UPWARDS, startingPosition, listOfEdges);
				
				String errorMessage = "Failure at i = " + i + ", height/j = " + j;
				if (9 <= j - EdgeCreator.MARIO_HEIGHT && j - EdgeCreator.MARIO_HEIGHT <= 10 ) {		
					if (i == 0) { 
						//The left corner will not hit the ceiling, and it will think it is a wall collision
						assertEquals(errorMessage, Collision.HIT_WALL, rightwardsCollision);
					} else {
						assertEquals(errorMessage, Collision.HIT_CEILING, rightwardsCollision);
					}
					
					if (i == level.length - 1) {
						//The right corner will not hit the ceiling, and it will think it is a wall collision
						assertEquals(errorMessage, Collision.HIT_WALL, leftwardsCollision);
					} else {
						assertEquals(errorMessage, Collision.HIT_CEILING, leftwardsCollision);
					}
					assertEquals(0, listOfEdges.size());
				} else {
					if (rightwardsCollision != Collision.HIT_NOTHING || leftwardsCollision != Collision.HIT_NOTHING) {
						rightwardsCollision = grapher.ascendingPolynomial(j, j, i, Collision.HIT_NOTHING, polynomial, JumpDirection.RIGHT_UPWARDS, startingPosition, listOfEdges);
						leftwardsCollision = grapher.ascendingPolynomial(j, j, i, Collision.HIT_NOTHING, polynomial, JumpDirection.LEFT_UPWARDS, startingPosition, listOfEdges);
						
					}
					assertEquals(errorMessage, Collision.HIT_NOTHING, rightwardsCollision);
					assertEquals(errorMessage, Collision.HIT_NOTHING, leftwardsCollision);
					assertEquals(0, listOfEdges.size());
				}
			}
		}
	}

	public void testCornersIgnoreJumpthroughtMaterial(){
		fail("Make the test.");
	}
	
	@Test
	public void testMiddleCornerCollisionDetection(){
		World world = flatlandWorld(); //Not total flatland.
		Node[][] level = world.getLevelMatrix();
		for (int i = 9; i < level.length; i++) {
			if (level[i][9] != null) level[i][9] = new Node(level[i][9].x, level[i][9].y, (byte) -10);
		}
		grapher.setMovementEdges(world, marioNode);		
		for (int xPos = 0; xPos < level.length; xPos++) { //This is the column position.
			for (float yPos = 1; yPos < level[xPos].length; yPos += 0.005) {
				String errorMessage = "Error at xPos = " + xPos + ", yPos = " + yPos;
				//Rightwards:
				Collision currentCollisionRight = grapher.middleFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_DOWNWARDS);
				Collision mimicCurrentCollisionRight = grapher.middleFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_UPWARDS);
				//It should not matter whether the motion is upwards or downwards.
				assertEquals(errorMessage, currentCollisionRight, mimicCurrentCollisionRight); 			
				//Leftwards. The same is true as before:
				Collision currentCollisionLeft = grapher.middleFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_DOWNWARDS);
				Collision mimicCurrentCollisionLeft = grapher.middleFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_UPWARDS);
				//It should not matter whether the motion is upwards or downwards.
				assertEquals(errorMessage, currentCollisionLeft, mimicCurrentCollisionLeft); 
				
				//Case rightwards:
				if (xPos < 9) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRight);
				} else {
					//There might be a collision:
					float middleYPos = yPos - EdgeCreator.MARIO_HEIGHT/2;
					if (9 <= middleYPos && middleYPos <= 10) {
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionRight);
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRight);
					}
				}				
				
				//Case leftwards:
				//Should count as though the x position is one to the left. Therefore:
				if (xPos < 9 + 1) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeft);
				} else {
					//There might be a collision:
					float middleYPos = yPos - EdgeCreator.MARIO_HEIGHT/2;
					if (9 <= middleYPos && middleYPos <= 10) {
						assertEquals(errorMessage, Collision.HIT_WALL	, currentCollisionLeft);
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeft);
					}
				}
				
			}
		}
	}
	
	@Test
	public void testUpperFacingCornerCollisionDetection(){
		World world = flatlandWorld(); //Not total flatland.
		Node[][] level = world.getLevelMatrix();
		for (int i = 9; i < level.length; i++) {
			if (level[i][9] != null) level[i][9] = new Node(level[i][9].x, level[i][9].y, (byte) -10);
		}
		
		grapher.setMovementEdges(world, marioNode);		
		
		for (int xPos = 0; xPos < level.length; xPos++) { //This is the column position.
			for (float yPos = 1; yPos < level[xPos].length; yPos += 0.01) {
				String errorMessage = "Error at xPos = " + xPos + ", yPos = " + yPos;
				//First with no Hitting wall, and formerYPos being different from the current:
					//Four different possible directions:
					Collision currentCollisionRightUp 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_UPWARDS	 , false, -1);
					Collision currentCollisionRightDown = grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_DOWNWARDS , false, -1);
					Collision currentCollisionLeftUp 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_UPWARDS	 , false, -1);
					Collision currentCollisionLeftDown 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_DOWNWARDS  , false, -1);
				//IsHittingWall:
					//Four different possible directions:
					Collision currentCollisionHittingWallRightUp 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_UPWARDS	 , true, -1);
					Collision currentCollisionHittingWallRightDown 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_DOWNWARDS , true, -1);
					Collision currentCollisionHittingWallLeftUp 		= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_UPWARDS	 , true, -1);
					Collision currentCollisionHittingWallLeftDown 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_DOWNWARDS  , true, -1);
				//formerYPos=yPos:
					//Four different possible directions:
					Collision currentCollisionSameYRightUp 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_UPWARDS	 , false, yPos);
					Collision currentCollisionSameYRightDown 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_DOWNWARDS , false, yPos);
					Collision currentCollisionSameYLeftUp 		= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_UPWARDS	 , false, yPos);
					Collision currentCollisionSameYLeftDown 	= grapher.upperFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_DOWNWARDS  , false, yPos);
				//They should be the same, when the y-value are within a certain margin:
					
				//Case right upwards:
				if (xPos < 9) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightUp);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallRightUp);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionSameYRightUp);
				} else {
					//There might be a collision:
					float upperYPos = yPos - EdgeCreator.MARIO_HEIGHT;
					//The 0.01, is because of an addition by this in the code.
					if (9 + 0.01 <= upperYPos && upperYPos <= 10 + 0.01) { //If there should be a collision:
						assertEquals(errorMessage, Collision.HIT_CEILING, currentCollisionRightUp);
						//Thinks it is a continuation of a wall it is currently hitting:
						assertEquals(errorMessage, Collision.HIT_WALL	, currentCollisionHittingWallRightUp); 
						//Should think it has just hit a wall:
						assertEquals(errorMessage, Collision.HIT_WALL	, currentCollisionSameYRightUp); 
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightUp);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallRightUp);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionSameYRightUp);
					}
				}				
				
				//Case right downwards:
				if (xPos < 9) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightDown);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallRightDown);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionSameYRightDown);
				} else {
					//There might be a collision:
					float upperYPos = yPos - EdgeCreator.MARIO_HEIGHT;
					//The 0.01, is because of an addition by this in the code.
					if (9 - 0.01 <= upperYPos && upperYPos <= 10 - 0.01) { //If there should be a collision:
						//Can only hit a wall, or nothing:
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionRightDown);
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionHittingWallRightDown); 
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionSameYRightDown); 
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightDown);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallRightDown);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionSameYRightDown);
					}
				}				

				//Case Left upwards:
				//Should count as though the x position is one to the left. Therefore:
				if (xPos < 9 + 1) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftUp);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallLeftUp);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionSameYLeftUp);
				} else {
					//There might be a collision:
					float upperYPos = yPos - EdgeCreator.MARIO_HEIGHT;
					//The 0.01, is because of an addition by this in the code.
					if (9 + 0.01 <= upperYPos && upperYPos <= 10 + 0.01) { //If there should be a collision:
						assertEquals(errorMessage, Collision.HIT_CEILING, currentCollisionLeftUp);
						//Thinks it is a continuation of a wall it is currently hitting:
						assertEquals(errorMessage, Collision.HIT_WALL	, currentCollisionHittingWallLeftUp); 
						//Should think it has just hit a wall:
						assertEquals(errorMessage, Collision.HIT_WALL	, currentCollisionSameYLeftUp); 
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftUp);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallLeftUp);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionSameYLeftUp);
					}
				}		
				
				//Case Left downwards:
				//Should count as though the x position is one to the left. Therefore:
				if (xPos < 9 + 1) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftDown);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallLeftDown);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionSameYLeftDown);
				} else {
					//There might be a collision:
					float upperYPos = yPos - EdgeCreator.MARIO_HEIGHT;
					//The 0.01, is because of an addition by this in the code.
					if (9 - 0.01 <= upperYPos && upperYPos <= 10 - 0.01) { //If there should be a collision:
						//Can only hit a wall, or nothing:
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionLeftDown);
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionHittingWallLeftDown); 
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionSameYLeftDown); 
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftDown);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallLeftDown);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionSameYLeftDown);
					}
				}	
			}
		}
	}
	
	@Test
	public void testLowerFacingCornerCollisionDetection(){
		World world = flatlandWorld(); //Not total flatland.
		Node[][] level = world.getLevelMatrix();
		for (int i = 9; i < level.length; i++) {
			if (level[i][9] != null) level[i][9] = new Node(level[i][9].x, level[i][9].y, (byte) -10);
		}
		
		grapher.setMovementEdges(world, marioNode);		
		
		for (int xPos = 0; xPos < level.length; xPos++) { //This is the column position.
			for (float yPos = 1; yPos < level[xPos].length; yPos += 0.01) {
				String errorMessage = "Error at xPos = " + xPos + ", yPos = " + yPos;
				//First with Collision = HIT_NOTHING
					//Four different possible directions:
					Collision currentCollisionRightUp 	= grapher.lowerFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_UPWARDS	 , Collision.HIT_NOTHING);
					Collision currentCollisionRightDown = grapher.lowerFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_DOWNWARDS , Collision.HIT_NOTHING);
					Collision currentCollisionLeftUp 	= grapher.lowerFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_UPWARDS	 , Collision.HIT_NOTHING);
					Collision currentCollisionLeftDown 	= grapher.lowerFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_DOWNWARDS  , Collision.HIT_NOTHING);
				//with Collision = HIT_WALL:
					//Four different possible directions:
					Collision currentCollisionHittingWallRightUp 	= grapher.lowerFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_UPWARDS	 , Collision.HIT_WALL);
					Collision currentCollisionHittingWallRightDown 	= grapher.lowerFacingCornerCollision(yPos, xPos, JumpDirection.RIGHT_DOWNWARDS , Collision.HIT_WALL);
					Collision currentCollisionHittingWallLeftUp 		= grapher.lowerFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_UPWARDS	 , Collision.HIT_WALL);
					Collision currentCollisionHittingWallLeftDown 	= grapher.lowerFacingCornerCollision(yPos, xPos, JumpDirection.LEFT_DOWNWARDS  , Collision.HIT_WALL);
				
				final float lowerYPos = yPos;
				//Case right upwards:
				if (xPos < 9) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightUp);
					//Thinks it has reached the top of a wall:
					assertEquals(errorMessage, Collision.HIT_GROUND, currentCollisionHittingWallRightUp);
				} else {
					//There might be a collision:
					//The 0.01, is because of an addition by this in the code.
					if (9 + 0.01 <= lowerYPos && lowerYPos <= 10 + 0.01) { //If there should be a collision:
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionRightUp);
						//Thinks it is a continuation of a wall it is currently hitting:
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionHittingWallRightUp); 
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightUp);
						//Thinks it has reached the top of a wall:
						assertEquals(errorMessage, Collision.HIT_GROUND, currentCollisionHittingWallRightUp);
					}
				}				
				
				//Case right downwards:
				if (xPos < 9) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightDown);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallRightDown);
				} else {//TODO lacking case with one block above the floor.
					//There might be a collision:
					//The 0.01, is because of an addition by this in the code.
					if (9 - 0.01 <= lowerYPos && lowerYPos <= 10 - 0.01) { //If there should be a collision:	
						assertEquals(errorMessage, Collision.HIT_GROUND, currentCollisionRightDown);
						//Hits the ground, as there is empty space for Mario above.					
						assertEquals(errorMessage, Collision.HIT_GROUND , currentCollisionHittingWallRightDown); 
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightDown);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallRightDown);
					}
				}				

				//Case Left upwards:
				//Should count as though the x position is one to the left. Therefore:
				if (xPos < 9 + 1) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftUp);
					//Thinks it has reached the top of a wall:
					assertEquals(errorMessage, Collision.HIT_GROUND, currentCollisionHittingWallLeftUp);
				} else {
					//There might be a collision:
					//The 0.01, is because of an addition by this in the code.
					if (9 + 0.01 <= lowerYPos && lowerYPos <= 10 + 0.01) { //If there should be a collision:
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionLeftUp);
						//Thinks it is a continuation of a wall it is currently hitting:
						assertEquals(errorMessage, Collision.HIT_WALL, currentCollisionHittingWallLeftUp); 
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftUp);
						//Thinks it has reached the top of a wall:
						assertEquals(errorMessage, Collision.HIT_GROUND, currentCollisionHittingWallLeftUp);
					}
				}				
				
				//Case Left downwards:
				//Should count as though the x position is one to the left. Therefore:
				if (xPos < 9 + 1) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftDown);
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallLeftDown);
				} else {
					//There might be a collision:
					//The 0.01, is because of an addition by this in the code.
					if (9 - 0.01 <= lowerYPos && lowerYPos <= 10 - 0.01) { //If there should be a collision:
						assertEquals(errorMessage, Collision.HIT_GROUND, currentCollisionLeftDown);
						assertEquals(errorMessage, Collision.HIT_GROUND, currentCollisionHittingWallLeftDown); 
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftDown);
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionHittingWallLeftDown);
					}
				}				

			}
		}
	}
	
	@Test
	public void testLowerOppositeCornerCollisionDetection(){
		World world = flatlandWorld(); //Not total flatland.
		Node[][] level = world.getLevelMatrix();
		for (int i = 9; i < level.length; i++) {
			if (level[i][9] != null) level[i][9] = new Node(level[i][9].x, level[i][9].y, (byte) -10);
		}
		
		grapher.setMovementEdges(world, marioNode);		
		
		for (int xPos = 0; xPos < level.length; xPos++) { //This is the column position.
			for (float yPos = 1; yPos < level[xPos].length; yPos += 0.01) {
				String errorMessage = "Error at xPos = " + xPos + ", yPos = " + yPos;
				//Only cases for downwards directions:
				Collision currentCollisionRightDown = grapher.lowerOppositeCornerCollision(yPos, xPos, JumpDirection.RIGHT_DOWNWARDS);
				Collision currentCollisionLeftDown 	= grapher.lowerOppositeCornerCollision(yPos, xPos, JumpDirection.LEFT_DOWNWARDS);
				
				final float lowerYPos = yPos;
				//Case right downwards:
				if (xPos < 9+1) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightDown);
				} else {
					//There might be a collision:
					//The 0.01, is because of an addition by this in the code.
					if (9 - 0.01 <= lowerYPos && lowerYPos <= 10 - 0.01) { //If there should be a collision:		
						assertEquals(errorMessage, Collision.HIT_GROUND,  currentCollisionRightDown);
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightDown);
					}
				}					
				
				//Case Left downwards:
				if (xPos < 9) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftDown);
				} else {
					//There might be a collision:
					//The 0.01, is because of an addition by this in the code.
					if (9 - 0.01 <= lowerYPos && lowerYPos <= 10 - 0.01) { //If there should be a collision:
						assertEquals(errorMessage, Collision.HIT_GROUND, currentCollisionLeftDown);
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftDown);
					}
				}				

			}
		}
	}
	
	@Test
	public void testUpperOppositeCornerCollisionDetection(){
		World world = flatlandWorld(); //Not total flatland.
		Node[][] level = world.getLevelMatrix();
		for (int i = 9; i < level.length; i++) {
			if (level[i][9] != null) level[i][9] = new Node(level[i][9].x, level[i][9].y, (byte) -10);
		}
		
		grapher.setMovementEdges(world, marioNode);		
		
		for (int xPos = 0; xPos < level.length; xPos++) { //This is the column position.
			for (float yPos = 1; yPos < level[xPos].length; yPos += 0.01) {
				String errorMessage = "Error at xPos = " + xPos + ", yPos = " + yPos;
				//Only cases for upwards directions:
				Collision currentCollisionRightDown = grapher.upperOppositeCornerCollision(yPos, xPos, JumpDirection.RIGHT_UPWARDS);
				Collision currentCollisionLeftDown 	= grapher.upperOppositeCornerCollision(yPos, xPos, JumpDirection.LEFT_UPWARDS);
				
				final float upperYPos = yPos - EdgeCreator.MARIO_HEIGHT;
				//Case right downwards:
				if (xPos < 9+1) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightDown);
				} else {
					//There might be a collision:
					//The 0.01, is because of an addition by this in the code.
					if (9 + 0.01 <= upperYPos && upperYPos <= 10 + 0.01) { //If there should be a collision:	
						currentCollisionRightDown = grapher.upperOppositeCornerCollision(yPos, xPos, JumpDirection.RIGHT_UPWARDS);
						assertEquals(errorMessage, Collision.HIT_CEILING,  currentCollisionRightDown);
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionRightDown);
					}
				}					
				
				//Case Left downwards:
				if (xPos < 9) { //Beginning of the floor.
					assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftDown);
				} else {
					//There might be a collision:
					//The 0.01, is because of an addition by this in the code.
					if (9 + 0.01 <= upperYPos && upperYPos <= 10 + 0.01) { //If there should be a collision:
						currentCollisionLeftDown 	= grapher.upperOppositeCornerCollision(yPos, xPos, JumpDirection.LEFT_UPWARDS);
						assertEquals(errorMessage, Collision.HIT_CEILING, currentCollisionLeftDown);
					} else {
						assertEquals(errorMessage, Collision.HIT_NOTHING, currentCollisionLeftDown);
					}
				}				

			}
		}
	}
	
	@Test
	public void testDownwardsCollisionDetection(){
		fail("Make the test.");
	}
	
	@Test
	public void testUpwardsCollisionDetection(){
		fail("Make the test.");
	}
	
	@Test
	public void testHittingStuffUpwards(){
		fail("Make the test.");
	}
	
	@Test
	public void testHittingStuffDownwards(){
		fail("Make the test.");
	}
	
	
}
