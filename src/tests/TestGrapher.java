package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import MarioAI.MarioControls;
import MarioAI.MarioMethods;
import MarioAI.graph.Graph;
import MarioAI.graph.Grapher;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.edges.SecondOrderPolynomial;
import MarioAI.graph.nodes.*;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGrapher {
	
	private static final int GRID_WIDTH = 22;
	public Node marioNode;
	public DirectedEdge runningEdgeType = new Running(null, null);
	
	private short getColoumnRelativeToMario(int xPosition) {
		//Assumes that node!=null.
		return (short) ((xPosition - marioNode.x) + GRID_WIDTH/2);
	}
	
	private short getXPositionFromColoumn(int coloumn) {
		//Assumes that node!=null.
		return (short) (coloumn + marioNode.x - GRID_WIDTH/2);
	}
	
	public Graph getStartLevelWorld(String level){
		BasicAIAgent agent = new BasicAIAgent("");
		Environment observation = TestTools.loadLevel(level, agent);
		TestTools.runOneTick(observation);		
		Graph graph = new Graph();
		graph.createStartGraph(observation);
		this.marioNode = graph.getMarioNode(observation);
		return graph;		
	}
	
	@Test
	public void testCorrectMarioStartPosition() {
		Graph graph = getStartLevelWorld("flat.lvl");
		Grapher.setMovementEdges(graph.getLevelMatrix(), marioNode);
		fail("Test not added yet");
		
	}
	
	public Graph flatlandWorld() {
		Graph graph = getStartLevelWorld("flat.lvl");
		Grapher.setMovementEdges(graph.getLevelMatrix(), marioNode);
		return graph;
	}
	
	public Graph totalFlatland() {
		Graph graph = getStartLevelWorld("flat.lvl");
		Node[][] level = graph.getLevelMatrix();
		for (short i = 0; i < GRID_WIDTH; i++) {
			level[i][marioNode.y] = new Node(getXPositionFromColoumn(i), marioNode.y, (byte) 11); //TODO(*) Error: try to set it to -11
		}	
		Grapher.setMovementEdges(level, marioNode);
		return graph;
	}
	
	@Test
	public void testRunningEdgesToNeighbors() {
		Graph graph = flatlandWorld();
		Node[][] world = graph.getLevelMatrix();
		assertTrue(marioNode.containsEdgeWithTargetAndType(marioNode.x - 1, marioNode.y, runningEdgeType));
		assertTrue(marioNode.containsEdgeWithTargetAndType(marioNode.x + 1, marioNode.y, runningEdgeType));
	}
	
	@Test
	public void testRunningEdgesAlongRow() {
		Graph graph = flatlandWorld();
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
		Graph graph = getStartLevelWorld("flatWithBump.lvl");
		Node[][] world = graph.getLevelMatrix();
		Grapher.setMovementEdges(world, marioNode);
		//Mario shouldn't run to the right, only to the left:
		assertEquals(1, marioNode.getNumberOfEdgesOfType(runningEdgeType));
		//To the left:
		assertTrue(marioNode.containsEdgeWithTargetAndType(marioNode.x - 1, marioNode.y, runningEdgeType));		
	}
	
	@Test
	public void testCanJumpRight() {
		Graph graph = totalFlatland();
		Node[][] level = graph.getLevelMatrix();
		boolean[] possibleJumpLenghts = new boolean[5];//5 for the five different jump lengths
		for (DirectedEdge edge : marioNode.edges) {
			if (edge instanceof SecondOrderPolynomial) {
				SecondOrderPolynomial polynomialEdge = (SecondOrderPolynomial) edge;
				if (marioNode.x < polynomialEdge.target.x) { //Goes rightwards
					int index = (polynomialEdge.target.x - marioNode.x) - 1;
					possibleJumpLenghts[index] = true;
				}
			}		
		}
		for (int i = 0; i < possibleJumpLenghts.length; i++) {
			assertTrue(possibleJumpLenghts[i]);
		}
	}
	
	@Test
	public void testCanJumpLeft() {
		Graph graph = totalFlatland();
		Node[][] level = graph.getLevelMatrix();
		boolean[] possibleJumpLenghts = new boolean[5];//5 for the five different jump lengths
		for (DirectedEdge edge : marioNode.edges) {
			if (edge instanceof SecondOrderPolynomial) {
				SecondOrderPolynomial polynomialEdge = (SecondOrderPolynomial) edge;
				if (marioNode.x > polynomialEdge.target.x) { //Goes rightwards
					int index = (marioNode.x - polynomialEdge.target.x ) - 1;
					possibleJumpLenghts[index] = true;
				}
			}		
		}
		for (int i = 0; i < possibleJumpLenghts.length; i++) {
			assertTrue(possibleJumpLenghts[i]);
		}
	}
	
	@Test
	public void testJumpRightOverWall() { 
		//This includes correct jump heights		
		Graph graph = totalFlatland();
		Node[][] world = graph.getLevelMatrix();
		//adding the walls:
		
		//TODO change WALL_HEIGHT to be variable between 1 and 4.
		for (int WALL_HEIGHT = 2; WALL_HEIGHT <= 4; WALL_HEIGHT++) { //4 is marios max jump height
			for (short i = 2; i <= 4; i++) {
				addWall(WALL_HEIGHT, 11 + i, marioNode.y, world);
				List<DirectedEdge> newEdges = new ArrayList<DirectedEdge>();
				Grapher.getPolynomialReachingEdges(marioNode,(short) 11, newEdges);	
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
	public void testJumpUpAgainsWall() {
		Graph graph = totalFlatland();
		Node[][] world = graph.getLevelMatrix();
		//adding the walls:
		final int WALL_HEIGHT = 4;
		for (short i = -3; i <= 3; i++) {
			if (i == 0) continue;
			boolean hasJumpedAgainstWall = false;
			addWall(WALL_HEIGHT, 11 + i, marioNode.y, world);
			//Grapher.clearAllEdges(world);
			//Grapher.setMovementEdges(world, marioNode);
			List<DirectedEdge> newEdges = new ArrayList<DirectedEdge>();
			Grapher.getPolynomialReachingEdges(marioNode,(short) 11, newEdges);
			for (DirectedEdge edge : newEdges) {
				if (edge instanceof SecondOrderPolynomial) {
					SecondOrderPolynomial polynomialEdge = (SecondOrderPolynomial) edge;
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
			assertTrue("Failure at wall distance: " + i,hasJumpedAgainstWall);
			//removing the wall:
			removeWall(WALL_HEIGHT, 11 + i, marioNode.y, world);
		}
	}
	
	@Test
	public void testAbleToJumpUpThroughCertainMaterials() {
		Graph graph = totalFlatland();
		Node[][] world = graph.getLevelMatrix();
		for (int i = 0; i < world.length; i++) {
			world[i][(short)(marioNode.y - 3)] = new Node(getXPositionFromColoumn(i),(short) (marioNode.y - 3), (byte) -11);
		}
		Grapher.setMovementEdges(world, marioNode); //TODO remove -1 after adding possibility for left jump.
		for (int i = 0; i < world.length - 1; i++) {
			boolean hasEdgeToUpperLevel = false;
			for (DirectedEdge edge : world[i][marioNode.y].getEdges()) {
				if(edge.target.y == marioNode.y - 3) hasEdgeToUpperLevel = true;
			}
			assertTrue("Error at coloumn: "+i ,hasEdgeToUpperLevel);
		}		
	}
	
	@Test
	public void testNoFallingThroughPlatform() {
		Graph graph = totalFlatland();
		Node[][] world = graph.getLevelMatrix();
		for (short i = 0; i < world.length; i++) {
			world[i][marioNode.y] = new Node(i, (short) (marioNode.y + 3), (byte) -11);
		}		
		Grapher.setMovementEdges(world, marioNode);
		for (int i = 0; i < world.length ; i++) {
			for (DirectedEdge edge : world[i][marioNode.y].getEdges()) {
				assertTrue(edge.target.y == edge.source.y);
			}
		}
		
	}
		
	@Test
	public void testNoOverlapJumpHashingAndRunningHashing() {
		List<Integer> allJumpingEdgesList = getAllPossibleJumpingEdgeHashcodes();
		List<Integer> allRunningEdges = getAllPossibleRunningEdgeHashcodes();
		HashSet<Integer> allEdgesHashed = new HashSet<Integer>();
		allEdgesHashed.addAll(allJumpingEdgesList);
		allEdgesHashed.addAll(allRunningEdges);
		assertEquals(allJumpingEdgesList.size() + allRunningEdges.size(), allEdgesHashed.size());
		
	}

	@Test
	public void testProperJumpEdgeHashing() {
		//Because of the small state space, brute force over the statespace will be used to check its correctness.
		List<Integer> allJumpingEdgesHashcodes = getAllPossibleJumpingEdgeHashcodes();
		HashSet<Integer> allRuningEdgesHashed = new HashSet<Integer>();
		for (Integer edgeHash : allJumpingEdgesHashcodes) {
			allRuningEdgesHashed.add(edgeHash);
		}
		//They are all unique, so the size of the HashMap should be the same.
		assertEquals(allJumpingEdgesHashcodes.size(), allRuningEdgesHashed.size());
		//If we do it again, we should get the same list:
		allJumpingEdgesHashcodes = getAllPossibleJumpingEdgeHashcodes();
		allRuningEdgesHashed.addAll(allJumpingEdgesHashcodes);
		assertEquals(allJumpingEdgesHashcodes.size(), allRuningEdgesHashed.size());
	}
	
	private List<Integer> getAllPossibleJumpingEdgeHashcodes() {
		int limitY = 15;
		int limitX = 300;
		int limitJumpHeight = 4;
		ArrayList<Integer> allRunningEdgesHashes = new ArrayList<Integer>();
		
		//The source:
		for (short sourceY = 0; sourceY <= limitY; sourceY++) {
			for (short sourceX = 0; sourceX <= limitX; sourceX++) {			
				
				//The target:				
				for (short targetY = 0; targetY <= limitY; targetY++) {
					for (short targetX = 0; targetX <= limitX; targetX++) {		
						
						//The different possible jump heights
						for (int JumpHeight = 0; JumpHeight <= limitJumpHeight; JumpHeight++) {
							//Type does not matter
							Node source = new Node(sourceX, sourceY, (byte)10);
							Node target = new Node(targetX, targetY, (byte)10);
							SecondOrderPolynomial newPolynomial = new SecondOrderPolynomial(source,target);
							newPolynomial.ceiledTopPointY = sourceY+JumpHeight;
							newPolynomial.reHash();//important to rehash it now when its jump height has been registered.
							allRunningEdgesHashes.add(newPolynomial.hashCode());
						}						
					}
				}			
			}
		}
		assertEquals((limitY+1)*(limitY+1)*(limitX+1)*(limitX+1)*(limitJumpHeight+1), allRunningEdgesHashes.size() );
		return allRunningEdgesHashes;
	}

	@Test
	public void testProperRunningEdgeHashing() {		
		//Because of the small state space, brute force over the statespace will be used to check its correctness.
		List<Integer> allRunningEdgesList = getAllPossibleRunningEdgeHashcodes();
		HashSet<Integer> allRuningEdgesHashed = new HashSet<Integer>(allRunningEdgesList);
		//They are all unique, so the size of the HashMap should be the same.
		assertEquals(allRunningEdgesList.size(), allRuningEdgesHashed.size());
		//If we do it again, we should get the same list:
		allRunningEdgesList = getAllPossibleRunningEdgeHashcodes();
		allRuningEdgesHashed.addAll(allRunningEdgesList);
		assertEquals(allRunningEdgesList.size(), allRuningEdgesHashed.size());
	}

	private ArrayList<Integer> getAllPossibleRunningEdgeHashcodes() {
		int limitY = 15;
		int limitX = 300;
		ArrayList<Integer> allRunningEdgesHashcodes = new ArrayList<Integer>();
		//The source:
		for (short sourceY = 0; sourceY <= limitY; sourceY++) {
			for (short sourceX = 0; sourceX <= limitX; sourceX++) {			
				//The target:				
				for (short targetY = 0; targetY <= limitY; targetY++) {
					for (short targetX = 0; targetX <= limitX; targetX++) {
						//Type does not matter
						allRunningEdgesHashcodes.add(new Running(new Node(sourceX, sourceY, (byte)10), 
								                        new Node(targetX, targetY, (byte)10)).hashCode());
						
					}
				}
				
				
			}
		}
		assertEquals((limitY+1)*(limitY+1)*(limitX+1)*(limitX+1), allRunningEdgesHashcodes.size() );
		return allRunningEdgesHashcodes;
	}
	
	
}
