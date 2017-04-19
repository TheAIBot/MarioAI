package tests;

import org.junit.Assert.*;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.*;
import MarioAI.AStar;
import MarioAI.FastAndFurious;
import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemy.EnemyPredictor;
import MarioAI.enemy.EnemyType;
import MarioAI.graph.Graph;
import MarioAI.graph.Grapher;
import MarioAI.graph.JumpDirection;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.edges.SecondOrderPolynomial;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;

public class TestAStar {
	Agent agent;
	Environment observation;
	Graph graph;
	Grapher grapher;
	final float delta = 0.05f;
	Node marioNode;

	public void setup(String levelName) {
		setup(levelName, false);
	}
	
	public void setup(String levelName, boolean showLevel) {
		agent = new UnitTestAgent();
		observation = TestTools.loadLevel("" + levelName + ".lvl", agent, showLevel);
		DebugDraw.resetGraphics(observation);
		TestTools.runOneTick(observation);
		graph = new Graph();
		graph.createStartGraph(observation);
		grapher = new Grapher();
		grapher.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
		marioNode = graph.getMarioNode(observation);
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
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
		//He should run through the level:
		for (DirectedEdge directedEdge : path) {
			assertTrue(directedEdge instanceof Running);
		}
		assertEquals(12, path.get(path.size() - 1).target.x); //Correct x end destination
		assertEquals(marioNode.y, path.get(path.size() - 1).target.y); //Correct y end destination
	}
	
	public void testTakeFastestJump(){
		setup("flatWithWall", true);
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);
		assertTrue(path != null);
		TestTools.runWholeLevel(observation);
		
	}
	
	/**
	 * Test if A Star returns a path where Mario is required to jump in the beginning on multiple platforms
	 */
	@Test
	public void testAStarJumping() {
		setup("TestAStarJump", false);
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
			assertTrue(path.get(i) instanceof SecondOrderPolynomial);
		}
		
		for (int i = 4; i < path.size(); i++) { //Running
			assertTrue(path.get(i) instanceof Running);
			
		}		
		
		// TODO Bug: Mario thinks he can jump through one layer wall
		// TODO Bug: Mario not finding path at first A* call (in the next call, however, he finds the solution path)
	}
	
	// === Tests with enemies ===
	
	@Test
	public void testJumpOverEnemy() {
		setup("testAStarEnemyJumpOver",false);
		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);
		
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.renderLevel(observation);
		AStar aStar = new AStar();
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);
		assertTrue(path != null);
		
		//for (DirectedEdge edge : path) {
			//assertFalse(edge.target.x == 6);
		//}
		
		int c = 0;
		for (Node node : graph.getGoalNodes(0)) {
			//if (node.equals(path.get(path.size()-1))) c++;
		}
		//assertEquals(1, c);
		
	}

	@Test
	public void testCollideWithEnemy(){
		setup("testAStarEnemyJumpOver",false);
		
		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);		
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.renderLevel(observation);

		final int columnStart = 13;
		final int columnEnd = 15;

		Node source = graph.getLevelMatrix()[columnStart][marioNode.y];
		Node target = graph.getLevelMatrix()[columnEnd][marioNode.y];
		
		List<DirectedEdge> edges = new ArrayList<DirectedEdge>();
		marioNode = graph.getMarioNode(observation);
		SecondOrderPolynomial polynomial = new SecondOrderPolynomial(null, null); 
		polynomial.setToJumpPolynomial(source, columnStart, 2, 4);
		grapher.jumpAlongPolynomial(source, columnStart, polynomial, JumpDirection.RIGHT_UPWARDS, edges); 
		
		assertEquals(1, edges.size());
		SecondOrderPolynomial polynomialEdge = (SecondOrderPolynomial) edges.get(0);		
		assertEquals(target.x, polynomialEdge.target.x);
		assertEquals(target.y, polynomialEdge.target.y);
		
		SpeedNode start = new SpeedNode(source, Long.MAX_VALUE);
		start.gScore = 0;
		start.fScore = 0;
		AStar aStar = new AStar();
		
		SpeedNode end = aStar.getSpeedNode(polynomialEdge, start);
		
		
		assertTrue(end.isSpeedNodeUseable());
		assertTrue(end.doesMovementCollideWithEnemy(start.gScore, enemyPredictor, 2));		
	}
	
	@Test
	public void testNotCollideWithEnemy(){
		setup("testAStarEnemyJumpOver",false);
		
		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);		
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		TestTools.renderLevel(observation);
		
		final int columnStart = 11;
		final int columnEnd = 13;

		Node source = graph.getLevelMatrix()[columnStart][marioNode.y];
		Node target = graph.getLevelMatrix()[columnEnd][marioNode.y];
		
		List<DirectedEdge> edges = new ArrayList<DirectedEdge>();
		marioNode = graph.getMarioNode(observation);
		SecondOrderPolynomial polynomial = new SecondOrderPolynomial(null, null); 
		polynomial.setToJumpPolynomial(source, columnStart, 2, 4);
		grapher.jumpAlongPolynomial(source, columnStart, polynomial, JumpDirection.RIGHT_UPWARDS, edges); 
		
		assertEquals(1, edges.size());
		SecondOrderPolynomial polynomialEdge = (SecondOrderPolynomial) edges.get(0);		
		assertEquals(target.x, polynomialEdge.target.x);
		assertEquals(target.y, polynomialEdge.target.y);
		
		SpeedNode start = new SpeedNode(source, Long.MAX_VALUE);
		start.gScore = 0;
		start.fScore = 0;
		AStar aStar = new AStar();
		
		SpeedNode end = aStar.getSpeedNode(polynomialEdge, start);
				
		assertTrue(end.isSpeedNodeUseable());
		assertFalse(end.doesMovementCollideWithEnemy(start.gScore, enemyPredictor, 2));		
	}


}




