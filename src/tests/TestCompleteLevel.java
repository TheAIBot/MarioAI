package tests;

import static org.junit.Assert.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import MarioAI.FastAndFurious;
import MarioAI.World;
import MarioAI.graph.edges.edgeCreation.EdgeCreator;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCompleteLevel {
	final int MARIO_WON = Mario.STATUS_WIN; // 1
	final int MARIO_LOST = Mario.STATUS_DEAD; // 0
	
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
	
	@Test
	public void testPit() {
		assertEquals(MARIO_WON, testLevel("pit12345"));
	}
	
	@Test
	public void testPit12345678() {
		assertEquals(MARIO_WON, testLevel("pit12345678"));
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
	public void testUltraMaze() {
		assertEquals(MARIO_WON, testLevel("UltraMaze"));
	}
	
	@Test
	public void testDropDown1() {
		assertEquals(MARIO_WON, testLevel("dropDown1"));
	}
	
	/**
	 * Level for which no solution exists
	 */
	@Test
	public void testProgramNotCrashInUnsolvableLevel() {
		assertEquals(MARIO_LOST, testLevel("bumbybox"));
	}
	
}



