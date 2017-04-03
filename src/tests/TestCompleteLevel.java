package tests;

import org.junit.Test;

import MarioAI.FastAndFurious;
import MarioAI.graph.Graph;
import MarioAI.graph.Grapher;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class TestCompleteLevel {
	Agent agent;
	Environment observation;
	Graph graph;
	
	public void setup(String levelName) {
		agent = new FastAndFurious();
		observation = TestTools.loadLevel(levelName + ".lvl", agent);
		
		TestTools.runOneTick(observation);
		graph = new Graph();
		graph.createStartGraph(observation);
		Grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
	}
	
	public void testLevel(String path) {
		setup(path);
		TestTools.runWholeLevel(observation);
	}
	
	@Test
	public void testFlat() {
		testLevel("flat");
	}
	
	@Test
	public void testPit() {
		testLevel("pit12345");
	}
	
	@Test
	public void testStaircase() {
		testLevel("staircase");
	}
	
	@Test
	public void testDeadend() {
		testLevel("deadend");
	}
	
}



