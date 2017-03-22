package tests;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import MarioAI.MarioControls;
import MarioAI.MarioMethods;
import MarioAI.graph.Graph;
import MarioAI.graph.Grapher;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.nodes.*;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestGrapher {
	
	public Node marioNode;
	public DirectedEdge runningEdgeType = new Running(null, null);
	
	public Graph getStartLevelWorld(String level){
		BasicAIAgent agent = new BasicAIAgent("");
		Environment observation = TestTools.loadLevel(level, agent);
		TestTools.runOneTick(observation);		
		Graph graph = new Graph();
		graph.createStartGraph(observation);
		this.marioNode = graph.getMarioNode(observation);
		Grapher.setMovementEdges(graph.getLevelMatrix(), marioNode);
		return graph;		
	}
	
	public void testCorrectMarioStartPosition() {
		Graph graph = getStartLevelWorld("flat.lvl");
		
	}
	
	public Graph flatlandWorld() {
		return getStartLevelWorld("flat.lvl");
		
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
	
	
	
	
}
