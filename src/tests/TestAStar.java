package tests;

import org.junit.Assert.*;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import org.junit.*;
import MarioAI.AStar;
import MarioAI.FastAndFurious;
import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemy.EnemyPredictor;
import MarioAI.enemy.EnemyType;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.EdgeCreator;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.NodeCreator;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;

public class TestAStar {
	Agent agent;
	Environment observation;
	NodeCreator graph;
	final float delta = 0.05f;

	public void setup(String levelName) {
		setup(levelName, false);
	}
	
	public void setup(String levelName, boolean showLevel) {
		agent = new UnitTestAgent();
		observation = TestTools.loadLevel("" + levelName + ".lvl", agent, showLevel);
		
		TestTools.runOneTick(observation);
		graph = new NodeCreator();
		graph.createStartGraph(observation);
		new EdgeCreator().setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
	}
	
	/**
	 * Test if A Star returns path with only Running Directed Edges on a flat level
	 */
	@Test
	public void testAStarRunning() {
		setup("flat");
		
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);
		assertTrue(path != null);
		
//		float c = 1.0f;
		for (DirectedEdge directedEdge : path) {
			//assertEquals(directedEdge.target.gScore, c, delta);
			//assertEquals(directedEdge.target.fScore == 1000 - c, delta);
			assertTrue(directedEdge instanceof RunningEdge);
//			try {
//				Running test = (Running) directedEdge;
//			} catch (ClassCastException e) {
//				Assert.fail();
//			}
//			c++;
		}
		// goal node has been removed from path returned
		//assertNotEquals(path.get(path.size()-1).target.x, 1000, delta);
	}
	
	/**
	 * Test if A Star returns a path where Mario is required to jump in the beginning on multiple platforms
	 */
	@Test
	public void testAStarJumping() {
		setup("TestAStarJump", true);
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);

		//TestTools.runOneTick(observation);
		assertTrue(path != null);
		
		DebugDraw.drawGoalNodes(observation, graph.getGoalNodes(0));
		DebugDraw.drawPath(observation, path);
		TestTools.renderLevel(observation);
		assertEquals(10, path.size());
		
		
		for (int i = 0; i < 3; i++) { //Jumping edges
			assertTrue(path.get(i) instanceof JumpingEdge);
		}
		
		for (int i = 4; i < path.size(); i++) { //Running
			assertTrue(path.get(i) instanceof RunningEdge);
			
		}		
		
		// TODO Bug: Mario thinks he can jump through one layer wall
		// TODO Bug: Mario not finding path at first A* call (in the next call, however, he finds the solution path)
	}
	
	// === Tests with enemies ===
	
	@Test
	public void testJumpOverEnemy() {
		setup("testAStarEnemyJumpOver");
		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);
		
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		AStar aStar = new AStar();
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);
		assertTrue(path != null);
		
		for (DirectedEdge edge : path) {
			assertFalse(edge.target.x == 6);
		}
	}
}




