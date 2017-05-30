package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.AStar;
import MarioAI.FastAndFurious;
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
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.graph.nodes.World;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAStar {
	Agent agent = new UnitTestAgent();
	Environment observation;
	World graph;
	EdgeCreator grapher;
	EdgeCreator edgeCreator;
	final float delta = 0.05f;
	Node marioNode;

	public void setup(String levelName) {
		setup(levelName, false, false);
	}
	
	public void setup(String levelName, boolean showLevel, boolean withFastAndFurius) {
		if (withFastAndFurius) {
			agent = new FastAndFurious();
		} else agent = new UnitTestAgent();
		observation = TestTools.loadLevel("" + levelName + ".lvl", agent, showLevel);
		DebugDraw.resetGraphics(observation);
		TestTools.runOneTick(observation);
		graph = new World();
		edgeCreator = new EdgeCreator();
		graph.initialize(observation);
		grapher = new EdgeCreator();
		grapher.setMovementEdges(graph, graph.getMarioNode(observation));
		marioNode = graph.getMarioNode(observation);
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		new EdgeCreator().setMovementEdges(graph, graph.getMarioNode(observation));
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
			//assertEquals(directedEdge.target.gScore, c, delta);
			//assertEquals(directedEdge.target.fScore == 1000 - c, delta);
			assertTrue(directedEdge instanceof RunningEdge);
//			try {
//				Running test = (Running) directedEdge;
//			} catch (ClassCastException e) {
//				Assert.fail();
//			}
//			c++;
			assertTrue(directedEdge instanceof RunningEdge);
		}
		assertEquals(12, path.get(path.size() - 1).target.x); //Correct x end destination
		assertEquals(marioNode.y, path.get(path.size() - 1).target.y); //Correct y end destination
	}
	
	@Test
	public void testTakeFastestJump(){
		//TODO Remember to fix bug with different speeds after running along a path, compared to what the path describes.
		setup("flatWithJump", true, true);
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		MarioControls marioControls = new MarioControls();
		FastAndFurious fastAgent = (FastAndFurious) agent;
		List<DirectedEdge> path = fastAgent.getPath(observation);
		int numberOfActions = 1;
		int numberOfTicks = 0;
		DebugDraw.resetGraphics(observation);
		DebugDraw.drawGoalNodes(observation, graph.getGoalNodes(0));
		DebugDraw.drawPathMovement(observation, path);
		TestTools.renderLevel(observation);
		assertTrue(path != null);
		assertEquals("Fail at action: " + numberOfActions + ", at tick: " + numberOfTicks, 1, path.stream().filter(edge -> edge instanceof JumpingEdge).count()); //Should only jump ones.
		while(numberOfActions <= 5){
			if (marioControls.canUpdatePath && graph.hasGoalNodesChanged() || 
				 path.size() > 0 && MarioControls.isPathInvalid(observation, path)) {
				 numberOfActions++;
				 path = fastAgent.getPath(observation);
				 DebugDraw.resetGraphics(observation);
				 DebugDraw.drawGoalNodes(observation, graph.getGoalNodes(0));
				 DebugDraw.drawPathMovement(observation, path);
				 TestTools.renderLevel(observation);
				 assertTrue(path != null);
				 assertEquals("Fail at action: " + numberOfActions + ", at tick: " + numberOfTicks, 1, path.stream().filter(edge -> edge instanceof JumpingEdge).count()); //Should only jump ones.
			}			
			TestTools.runOneTick(observation);
			numberOfTicks++;
		}
		assertEquals(1, path.stream().filter(edge -> edge instanceof JumpingEdge).count());
		TestTools.runWholeLevel(observation);		
	}
	
	/**
	 * Test if A Star returns a path where Mario is required to jump in the beginning on multiple platforms
	 */
	@Test
	public void testAStarJumping() {
		setup("TestAStarJump", false, false);
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		ArrayList<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2);

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
		setup("TestAStarJump", false, false);
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		
		Map<Long, SpeedNode> speedNodes = aStar.getSpeedNodes();
		Map<Integer, Integer> numberOfNodesMap = new HashMap<Integer, Integer>();
		final int MAX_NUMBER_OF_SPEED_NODES = Hasher.FACTOR_NUMBER_OF_SPEED_NODES * 2 + 1;
		final int NUMBER_OF_TEST_TICKS = 100;
		final HashSet<Long>searchedNodes = new HashSet<Long>(); 
		UnitTestAgent unitTestAgent = (UnitTestAgent) agent;
		for (int i=0; i<NUMBER_OF_TEST_TICKS; i++) {
			TestTools.runOneTick(observation);
			unitTestAgent.action[Mario.KEY_RIGHT] = true;
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
		setup("testAStarEnemyJumpOver",false, false);
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
		setup("testAStarEnemyJumpOver",false, false);
		
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
		setup("testAStarEnemyJumpOver",false,false);
		
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
	
	
	/**
	 * Tests if A* is able to find a path when it is broken down into multiple ticks
	 */
	@Test
	public void testAStarCanRunMoreThanOneTick() {
		setup("flat");
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		
		// Should not find path in the given timespan
		final int TIME_ALLOWED = 20;
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2, TIME_ALLOWED);
		assertTrue(path == null);
		ArrayList<DirectedEdge> pathSegment = aStar.getCurrentBestSegmentPath();
		
		// Check pathSegment does not reach end 
		Node lastNode = null;
		for (DirectedEdge directedEdge : pathSegment) {
			lastNode = directedEdge.target;
		}
		for (Node node : graph.getGoalNodes(0)) {
			assertNotEquals(lastNode, node);
		}
		
		// Let A* run many times - enough times to make it find a solution path 
		int c = 0;
		do {
			path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2, TIME_ALLOWED);
			c++;
			if (c >= 100) Assert.fail("AStar never finishes");
		} while (path == null);
		
		// Check last node in solution path is indeed one of the goal nodes
		lastNode = null;
		for (DirectedEdge directedEdge : path) {
			lastNode = directedEdge.target;
		}
		boolean hasFoundGoal = false;
		for (Node node : graph.getGoalNodes(0)) {
			if (node.equals(lastNode)) hasFoundGoal = true;
		}
		assertTrue(hasFoundGoal);
		
	}
	
	/**
	 * Tests if A* is able to append more nodes on current best path segment
	 * Note that the test level ensures this will be the way A* should progress towards a final solution
	 */
	@Test
	public void testAStarRunMoreThanOneTickExtendPathSegment() {
		setup("flat");
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		AStar aStar = new AStar();
		
		// This time limit should be short enough to not make A* able to finish in two runs, but still be able to make progress by extending path fragments
		final int TIME_ALLOWED = 20;
		List<DirectedEdge> path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2, TIME_ALLOWED);
		
		// The path should not be a complete solution but should include some nodes
		assertTrue(path == null);
		ArrayList<DirectedEdge> pathSegment = aStar.getCurrentBestSegmentPath();
		assertTrue(pathSegment.size() > 0);
		
		// The path should be extended by next A* call but not finish
		path = aStar.runMultiNodeAStar(graph.getMarioNode(observation), graph.getGoalNodes(0), 0, enemyPredictor, 2, 1);
		assertTrue(path == null);
		ArrayList<DirectedEdge> newPathSegment = aStar.getCurrentBestSegmentPath();
		assertTrue(newPathSegment.size() > pathSegment.size());
		
		// For this level the nodes in the two pathsegments should be the same up until the end of the shortest one
		for (int i = 0; i < pathSegment.size(); i++) {
			assertEquals(pathSegment.get(i), newPathSegment.get(i));
		}
		
	}
	

}




