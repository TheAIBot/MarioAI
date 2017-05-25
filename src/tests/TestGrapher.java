package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import MarioAI.MarioMethods;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.EdgeCreator;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.nodes.*;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGrapher {
	EdgeCreator grapher = new EdgeCreator();
	private static final int GRID_WIDTH = 22;
	public Node marioNode;
	public DirectedEdge runningEdgeType = new RunningEdge(null, null);
	
	private short getColoumnRelativeToMario(int xPosition) {
		//Assumes that node!=null.
		return (short) ((xPosition - marioNode.x) + GRID_WIDTH/2);
	}
	
	private short getXPositionFromColoumn(int coloumn) {
		//Assumes that node!=null.
		return (short) (coloumn + marioNode.x - GRID_WIDTH/2);
	}
	
	public World getStartLevelWorld(String level){
		BasicAIAgent agent = new BasicAIAgent("");
		Environment observation = TestTools.loadLevel(level, agent);
		TestTools.runOneTick(observation);		
		World graph = new World();
		graph.initialize(observation);
		this.marioNode = graph.getMarioNode(observation);
		return graph;		
	}
	
	@Test
	public void testCorrectMarioStartPosition() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flat.lvl");
		grapher.setMovementEdges(graph.getLevelMatrix(), marioNode);
		fail("Test not added yet");
		
	}
	
	public World flatlandWorld() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flat.lvl");
		grapher.setMovementEdges(graph.getLevelMatrix(), marioNode);
		return graph;
	}
	
	public World totalFlatland() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flat.lvl");
		Node[][] level = graph.getLevelMatrix();
		for (short i = 0; i < GRID_WIDTH; i++) {
			level[i][marioNode.y] = new Node(getXPositionFromColoumn(i), marioNode.y, (byte) 11); //TODO(*) Error: try to set it to -11
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
		//All the reachable nodes from the mario node:
		for (int i = 10; i < world.length - 1; i++) {
			Node currentNode = world[i][marioNode.y];
			assertEquals(2, currentNode.getNumberOfEdgesOfType(runningEdgeType));
			assertTrue(currentNode.containsEdgeWithTargetAndType(currentNode.x - 1, currentNode.y, runningEdgeType));
			assertTrue(currentNode.containsEdgeWithTargetAndType(currentNode.x + 1, currentNode.y, runningEdgeType));
		}
		//Edge node that should only point to right, as there is nothing to the left:
		Node currentNode = world[9][marioNode.y];
		assertEquals(1, currentNode.getNumberOfEdgesOfType(runningEdgeType));
		assertTrue(currentNode.containsEdgeWithTargetAndType(currentNode.x + 1, currentNode.y, runningEdgeType));
		//The other nodes to the left are null, and thus a running edge cannot point to them.
	}
	
	@Test
	public void testRunningEdgesAgainstWall() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flatWithBump.lvl");
		Node[][] world = graph.getLevelMatrix();
		grapher.setMovementEdges(world, marioNode);
		//Mario shouldn't run to the right, only to the left:
		assertEquals(1, marioNode.getNumberOfEdgesOfType(runningEdgeType));
		//To the left:
		assertTrue(marioNode.containsEdgeWithTargetAndType(marioNode.x - 1, marioNode.y, runningEdgeType));		
	}
	
	@Test
	public void testCanJumpRight() {
		World graph = totalFlatland();
		Node[][] level = graph.getLevelMatrix();
		grapher.setMovementEdges(level, marioNode);
		boolean[] possibleJumpLenghts = new boolean[5];//5 for the five different jump lengths
		for (DirectedEdge edge : marioNode.getEdges()) {
			if (edge instanceof JumpingEdge) {
				JumpingEdge polynomialEdge = (JumpingEdge) edge;
				if (marioNode.x < polynomialEdge.target.x) { //Goes rightwards
					int index = (polynomialEdge.target.x - marioNode.x) - 1;
					possibleJumpLenghts[index] = true;
				}
			}		
		}
		for (int i = 0; i < possibleJumpLenghts.length; i++) {
			assertTrue("Failure at lenght " + (i+1) + ", have array:" + Arrays.toString(possibleJumpLenghts),possibleJumpLenghts[i]);
		}
	}
	
	@Test
	public void testCanJumpLeft() {
		World graph = totalFlatland();
		Node[][] level = graph.getLevelMatrix();
		EdgeCreator grapher = new EdgeCreator();
		grapher.setMovementEdges(level, marioNode);
		boolean[] possibleJumpLenghts = new boolean[5];//5 for the five different jump lengths
		for (DirectedEdge edge : marioNode.getEdges()) {
			if (edge instanceof JumpingEdge) {
				JumpingEdge polynomialEdge = (JumpingEdge) edge;
				if (marioNode.x > polynomialEdge.target.x) { //Goes leftwards
					int index = (marioNode.x - polynomialEdge.target.x ) - 1;
					possibleJumpLenghts[index] = true;
				}
			}		
		}
		for (int i = 0; i < possibleJumpLenghts.length; i++) {
			assertTrue("Failure at lenght " + (i+1), possibleJumpLenghts[i]);
		}
	}
	
	@Test
	public void testJumpRightOverWall() { 
		EdgeCreator grapher = new EdgeCreator();
		//This includes correct jump heights		
		World graph = totalFlatland();
		Node[][] world = graph.getLevelMatrix();
		//adding the walls:
		
		//TODO change WALL_HEIGHT to be variable between 1 and 4.
		for (int WALL_HEIGHT = 2; WALL_HEIGHT <= 4; WALL_HEIGHT++) { //4 is marios max jump height
			for (short i = 2; i <= 4; i++) {
				addWall(WALL_HEIGHT, 11 + i, marioNode.y, world);
				List<DirectedEdge> newEdges = new ArrayList<DirectedEdge>();
				grapher.getPolynomialReachingEdges(marioNode,(short) 11, newEdges);	
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
	
	private void addWall(int height, int coloumn, int row, Node[][] levelMatrix) {
		for (short j = 1; j <= height; j++) {
			levelMatrix[coloumn][row - j] = new Node(getXPositionFromColoumn(coloumn), (short) (row-j), (byte)10);
		}
	}
	
	private void removeWall(int height, int coloumn, int row, Node[][] levelMatrix) {
		for (short j = 1; j <= height; j++) {
			levelMatrix[coloumn][row - j] = null;
		}
	}
	
	@Test
	public void testJumpUpAgainstWall() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = totalFlatland();
		Node[][] world = graph.getLevelMatrix();
		//adding the walls:
		final int WALL_HEIGHT = 4;
		for (short i = 1; i <= 3; i++) {
			if (i == 0) continue;
			boolean hasJumpedAgainstWall = false;
			addWall(WALL_HEIGHT, 11 + i, marioNode.y, world);
			grapher.clearAllEdges(world);
			grapher.setMovementEdges(world, marioNode);
			List<DirectedEdge> newEdges = new ArrayList<DirectedEdge>();
			grapher.getPolynomialReachingEdges(marioNode,(short) 11, newEdges);
			for (DirectedEdge edge : newEdges) {
				if (edge instanceof JumpingEdge) {
					JumpingEdge polynomialEdge = (JumpingEdge) edge;
					//It has only jumped along the wall, if the height of the jump without collision at the wall,
					//is less than the walls height.
					if (edge.target == null) {
						System.out.println("Error");						
					}
					if (polynomialEdge.f(11 + i) < (marioNode.y + WALL_HEIGHT) && 
						edge.target.x == getXPositionFromColoumn(11+i) && 
						edge.target.y == marioNode.y - WALL_HEIGHT) {
						//Minus or plus because of f gives height according to higher up=greater y.
						hasJumpedAgainstWall = true;
					}
				}
			}
			assertTrue("Failure at wall distance: " + i, hasJumpedAgainstWall);
			//removing the wall:
			removeWall(WALL_HEIGHT, 11 + i, marioNode.y, world);
		}
	}
	
	@Test
	public void testAbleToJumpUpThroughCertainMaterials() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = totalFlatland();
		Node[][] world = graph.getLevelMatrix();
		for (int i = 0; i < world.length; i++) {
			world[i][(short)(marioNode.y - 3)] = new Node(getXPositionFromColoumn(i),(short) (marioNode.y - 3), (byte) -11);
		}
		grapher.setMovementEdges(world, marioNode); //TODO remove -1 after adding possibility for left jump.
		for (int i = 0; i < world.length - 1; i++) {
			boolean hasEdgeToUpperLevel = false;
			for (DirectedEdge edge : world[i][marioNode.y].getEdges()) {
				if(edge.target.y == marioNode.y - 3) hasEdgeToUpperLevel = true;
			}
			assertTrue("Error at coloumn: " + i ,hasEdgeToUpperLevel);
		}		
	}
	
	@Test
	public void testNoFallingThroughPlatform() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = totalFlatland();
		Node[][] world = graph.getLevelMatrix();
		for (short i = 0; i < world.length; i++) {
			world[i][marioNode.y] = new Node(i, (short) (marioNode.y + 3), (byte) -11);
		}		
		grapher.setMovementEdges(world, marioNode);
		for (int i = 0; i < world.length ; i++) {
			for (DirectedEdge edge : world[i][marioNode.y].getEdges()) {
				assertTrue(edge.target.y == edge.source.y);
			}
		}
	}
		
	@Test 
	public void testAlwaysSameResultOnSetEdges() {
		EdgeCreator grapher = new EdgeCreator();
		//Should get the same result doing multiple set edges on a given level matrix.
		World graph1 = totalFlatland();
		Node[][] world1 = graph1.getLevelMatrix();
		
		World graph2 = totalFlatland();
		Node[][] world2 = graph2.getLevelMatrix();
		grapher.setMovementEdges(world1, marioNode);
		//And multiple times:
		grapher.setMovementEdges(world2, marioNode);
		grapher.setMovementEdges(world2, marioNode);
		
		//Asserting that the result is the same for the two matrixes:
		for (int i = 0; i < world2.length; i++) {
			for (int j = 0; j < world2[i].length; j++) {
				if (world1[i][j] == null && world2[i][j] == null) {
					continue;
				} else if (world1[i][j] == null && world2[i][j] == null) fail();
				//The number of edges going out from a given Node should be the same:
				assertEquals(world1[i][j].getEdges().size(), world2[i][j].getEdges().size());
				//All edges in one list should also be in the other list:
				for (DirectedEdge edge : world2[i][j].getEdges()) { 
					//A little slow, but this test doesn't take that much time.					
					assertTrue(world1[i][j].getEdges().contains(edge));
				}
				//And the outher way around:
				for (DirectedEdge edge : world1[i][j].getEdges()) { 
					//A little slow, but this test doesn't take that much time.					
					assertTrue(world2[i][j].getEdges().contains(edge));
				}
			}
		}
		
		
	}
}
