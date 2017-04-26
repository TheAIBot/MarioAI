package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import MarioAI.AStar;
import MarioAI.Hasher;
import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemy.EnemyPredictor;
import MarioAI.enemy.EnemyType;
import MarioAI.graph.JumpDirection;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.EdgeCreator;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.NodeCreator;
import MarioAI.graph.nodes.SpeedNode;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAStar {
	UnitTestAgent agent = new UnitTestAgent();
	Environment observation;
	NodeCreator graph;
	EdgeCreator edgeCreator;
	final float delta = 0.05f;
	Node marioNode;

	public void setup(String levelName) {
		setup(levelName, false);
	}
	
	public void setup(String levelName, boolean showLevel) {
		observation = TestTools.loadLevel("" + levelName + ".lvl", agent, showLevel);
		DebugDraw.resetGraphics(observation);
		TestTools.runOneTick(observation);
		graph = new NodeCreator();
		edgeCreator = new EdgeCreator();
		graph.createStartGraph(observation);
		edgeCreator.setMovementEdges(graph.getLevelMatrix(), graph.getMarioNode(observation));
		marioNode = graph.getMarioNode(observation);
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
			assertTrue(directedEdge instanceof RunningEdge);
		}
		assertEquals(12, path.get(path.size() - 1).target.x); //Correct x end destination
		assertEquals(marioNode.y, path.get(path.size() - 1).target.y); //Correct y end destination
	}
	
	@Test
	public void testTakeFastestJump(){
		setup("flatWithJump", false);
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);
		assertTrue(path != null);
		assertEquals(1, path.stream().filter(edge -> edge instanceof JumpingEdge).count());
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
		DebugDraw.drawPathMovement(observation, path);
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
	
	@Test
	public void testNumberOfSpeedNodes() {
		setup("TestAStarJump", false);
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		
		Map<Long, SpeedNode> speedNodes = aStar.getSpeedNodes();
		Map<Integer, Integer> numberOfNodesMap = new HashMap<Integer, Integer>();
		final int MAX_NUMBER_OF_SPEED_NODES = Hasher.FACTOR_NUMBER_OF_SPEED_NODES * 2 + 1;
		final int NUMBER_OF_TEST_TICKS = 100;
		final HashSet<Long>searchedNodes = new HashSet<Long>(); 
		
		for (int i=0; i<NUMBER_OF_TEST_TICKS; i++) {
			TestTools.runOneTick(observation);
			agent.action[Mario.KEY_RIGHT] = true;
			aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);
			
			for (SpeedNode speedNode : speedNodes.values()) {
				if (!searchedNodes.contains(speedNode.hash)) {
					int hashCode = speedNode.node.hashCode();
					if (numberOfNodesMap.containsKey(hashCode)) {
						int number = numberOfNodesMap.get(hashCode);
						numberOfNodesMap.put(hashCode, number+1);
						//assertTrue(number <= MAX_NUMBER_OF_SPEED_NODES);
					}
					else {
						numberOfNodesMap.put(hashCode, 1);
					}
					searchedNodes.add(speedNode.hash);
				}
			}
		}
		assertTrue("Maximum number " + numberOfNodesMap.values().stream().max(Integer::compare).get() + 
				   "instead of " + MAX_NUMBER_OF_SPEED_NODES,
				   numberOfNodesMap.values().stream().allMatch(x -> x <= MAX_NUMBER_OF_SPEED_NODES));
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
		
		for (DirectedEdge edge : path) {
			assertFalse(edge.target.x == 6);
		}
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
		JumpingEdge polynomial = new JumpingEdge(null, null); 
		polynomial.setToJumpPolynomial(source, columnStart, 2, 4);
		edgeCreator.jumpAlongPolynomial(source, columnStart, polynomial, JumpDirection.RIGHT_UPWARDS, edges); 
		
		assertEquals(1, edges.size());
		JumpingEdge polynomialEdge = (JumpingEdge) edges.get(0);		
		assertEquals(target.x, polynomialEdge.target.x);
		assertEquals(target.y, polynomialEdge.target.y);
		
		SpeedNode start = new SpeedNode(source, 0, Long.MAX_VALUE);
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
		JumpingEdge polynomial = new JumpingEdge(null, null); 
		polynomial.setToJumpPolynomial(source, columnStart, 2, 4);
		edgeCreator.jumpAlongPolynomial(source, columnStart, polynomial, JumpDirection.RIGHT_UPWARDS, edges); 
		
		assertEquals(1, edges.size());
		JumpingEdge polynomialEdge = (JumpingEdge) edges.get(0);		
		assertEquals(target.x, polynomialEdge.target.x);
		assertEquals(target.y, polynomialEdge.target.y);
		
		SpeedNode start = new SpeedNode(source, 0, Long.MAX_VALUE);
		start.gScore = 0;
		start.fScore = 0;
		AStar aStar = new AStar();
		
		SpeedNode end = aStar.getSpeedNode(polynomialEdge, start);
				
		assertTrue(end.isSpeedNodeUseable());
		assertFalse(end.doesMovementCollideWithEnemy(start.gScore, enemyPredictor, 2));		
	}


}




