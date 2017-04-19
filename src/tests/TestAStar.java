package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import MarioAI.AStar;
import MarioAI.FastAndFurious;
import MarioAI.enemy.EnemyPredictor;
import MarioAI.graph.Graph;
import MarioAI.graph.Grapher;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.edges.SecondOrderPolynomial;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class TestAStar {
	Agent agent;
	Environment observation;
	Graph graph;
	final float delta = 0.05f;
	
	public void setup(String levelName) {
		agent = new FastAndFurious();
		observation = TestTools.loadLevel("" + levelName + ".lvl", agent);
		
		TestTools.runOneTick(observation);
		graph = new Graph();
		graph.createStartGraph(observation);
		new Grapher().setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
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
			assertTrue(directedEdge instanceof Running);
//			try {
//				Running test = (Running) directedEdge;
//			} catch (ClassCastException e) {
//				Assert.fail();
//			}
//			c++;
		}
		// goal node has been removed from path returned
		assertNotEquals(path.get(path.size()-1).target.x, 1000, delta);
	}
	
	/**
	 * Test if A Star returns a path where Mario is required to jump in the beginning on multiple platforms
	 */
	@Test
	public void testAStarJumping() {
		setup("TestAStarJump");
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);
		assertTrue(path != null);
		
		DirectedEdge e1 = path.get(1);
		DirectedEdge e2 = path.get(2);
		DirectedEdge eN = path.get(5);
		assertEquals(e1.target.gScore, e1.target.y - e1.source.y, delta);
		assertEquals(e1.target.gScore, e2.target.y - e2.source.y, delta);
		assertTrue(e1 instanceof SecondOrderPolynomial);
		assertTrue(e2 instanceof SecondOrderPolynomial);
		assertTrue(eN instanceof Running);		
		
		// TODO Bug: Mario thinks he can jump through one layer wall
		// TODO Bug: Mario not finding path at first A* call (in the next call, however, he finds the solution path)
	}

	
}




