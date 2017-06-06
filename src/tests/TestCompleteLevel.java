package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import MarioAI.FastAndFurious;
import MarioAI.World;
import MarioAI.graph.edges.EdgeCreator;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class TestCompleteLevel {
	final int MARIO_WON = 1;
	final int MARIO_LOST = 0;
	
	Agent agent;
	Environment observation;
	World graph;
	
	public void setup(String levelName) {
		agent = new FastAndFurious();
		observation = TestTools.loadLevel(levelName + ".lvl", agent);
		
		TestTools.runOneTick(observation);
		graph = new World();
		graph.initialize(observation);
		new EdgeCreator().setMovementEdges(graph, graph.getMarioNode(observation));
	}
	
	public int testLevel(String path) {
		setup(path);
		return TestTools.runWholeLevelWillWin(observation);
	}
	
	@Test
	public void testFlat() {
		assertEquals(MARIO_WON, testLevel("flat"));
	}
	
	/**
	 * Mario should not be able to finish this level, because the agent cannot jump 5 blocks horizontally 
	 */
	@Test
	public void testPit() {
		assertEquals(MARIO_LOST, testLevel("pit12345"));
	}
	
	@Test
	public void testStaircase() {
		assertEquals(MARIO_WON, testLevel("staircase"));
	}
	
	@Test
	public void testDeadend() {
		assertEquals(MARIO_WON, testLevel("deadend"));
	}
	
	@Test
	public void testJumpCourse() {
		assertEquals(MARIO_WON, testLevel("jumpLevels/semiAdvancedJumpingCourse"));
	}
	
	@Test
	public void testTheMaze() {
		assertEquals(MARIO_WON, testLevel("theMaze"));
	}
	
	@Test
	public void testDropDown1() {
		assertEquals(MARIO_WON, testLevel("dropDown1"));
	}
	
}



