package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import MarioAI.Hasher;
import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.JumpDirection;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.EdgeCreator;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import MarioAI.path.AStar;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAStar {
//	FastAndFurious agent;
//	Environment observation;
//	final float delta = MarioControls.ACCEPTED_DEVIATION; //0.05f;
//	World world;
//	EdgeCreator edgeCreator;
//	Node marioNode;
//	EnemyPredictor enemyPredictor;
//	AStar aStar;
//	MarioControls marioControls;
//	
//	public void setup(String levelName) {
//		setup(levelName, false);
//	}
//	
//	public void setup(String levelName, boolean showLevel) {
//		agent = new FastAndFurious();
//		observation = TestTools.loadLevel(levelName + ".lvl", agent, showLevel);
//		DebugDraw.resetGraphics(observation);
//		TestTools.runOneTick(observation);
//		world = agent.world;
//		edgeCreator = agent.grapher;
//		enemyPredictor = agent.enemyPredictor;
//		aStar = agent.aStar;
//		marioControls = agent.marioController;
//		marioNode = world.getMarioNode(observation);
//	}
//		
//	public Agent setupUnitTestAgent(String levelName) {
//		Agent agent = new UnitTestAgent();
//		observation = TestTools.loadLevel(levelName + ".lvl", agent, false);
//		DebugDraw.resetGraphics(observation);
//		TestTools.runOneTick(observation);
//		world = new World();
//		CollisionDetection.setWorld(world);
//		edgeCreator = new EdgeCreator();
//		world.initialize(observation);
//		edgeCreator.setMovementEdges(world, world.getMarioNode(observation));
//		enemyPredictor = new EnemyPredictor();
//		marioNode = world.getMarioNode(observation);
//		marioControls = new MarioControls();
//		aStar = new AStar();
//		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
//		return agent;
//	}
//	
//	/**
//	 * Test if A Star returns path with only Running Directed Edges on a flat level
//	 */
//	@Test
//	public void testAStarRunning() {
//		setup("flat");
//		List<DirectedEdge> path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2);
//		assertNotNull(path);
//		//He should run through the level:
//		for (DirectedEdge directedEdge : path) {
//			assertTrue(directedEdge instanceof RunningEdge);
//			//assertEquals(directedEdge.target.gScore, c, delta);
//			//assertEquals(directedEdge.target.fScore == 1000 - c, delta);
//		}
//		assertEquals(12, path.get(path.size() - 1).target.x); //Correct x end destination
//		assertEquals(marioNode.y, path.get(path.size() - 1).target.y); //Correct y end destination
//	}
//	
//	@Test
//	public void testTakeFastestJump() {
//		//TODO Remember to fix bug with different speeds after running along a path, compared to what the path describes.
//		//setup("flatWithJump", true, true);
//		setup("flatWithJump", false);
//		List<DirectedEdge> path = agent.getPath(observation);
//		int numberOfActions = 1;
//		int numberOfTicks = 0;
//		DebugDraw.resetGraphics(observation);
//		DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
//		DebugDraw.drawPathMovement(observation, path);
//		TestTools.renderLevel(observation);
//		assertTrue(path != null);
//		assertEquals("Fail at action: " + numberOfActions + ", at tick: " + numberOfTicks, 1, path.stream().filter(edge -> edge instanceof JumpingEdge).count()); //Should only jump ones.
//		Assert.fail("Test will run forever after this line, though it works as expected");
//		while(numberOfActions <= 5){
//			if (marioControls.canUpdatePath && world.hasGoalNodesChanged() || 
//				 path.size() > 0 && MarioControls.isPathInvalid(observation, path)) {
//				 numberOfActions++;
//				 path = agent.getPath(observation);
//				 DebugDraw.resetGraphics(observation);
//				 DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
//				 DebugDraw.drawPathMovement(observation, path);
//				 TestTools.renderLevel(observation);
//				 assertTrue(path != null);
//				 assertEquals("Fail at action: " + numberOfActions + ", at tick: " + numberOfTicks, 1, path.stream().filter(edge -> edge instanceof JumpingEdge).count()); //Should only jump ones.
//			}		
//			TestTools.runOneTick(observation);
//			numberOfTicks++;
//		}
//		assertEquals(1, path.stream().filter(edge -> edge instanceof JumpingEdge).count());
//		TestTools.runWholeLevel(observation);
//	}
//	
//	/**
//	 * Test if A Star returns a path where Mario is required to jump in the beginning on multiple platforms
//	 */
//	@Test
//	public void testAStarJumping() {
//		setup("TestAStarJump", false);
//		ArrayList<DirectedEdge> path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2);
//
//		//TestTools.runOneTick(observation);
//		assertTrue(path != null);
//		
//		DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
//		DebugDraw.drawPathMovement(observation, path);
//		TestTools.renderLevel(observation);
//		assertEquals(10, path.size());
//		
//		
//		for (int i = 0; i < 3; i++) { //Jumping edges
//			assertTrue(path.get(i) instanceof JumpingEdge);
//		}
//		
//		for (int i = 4; i < path.size(); i++) { //Running
//			assertTrue(path.get(i) instanceof RunningEdge);
//			
//		}		
//		
//	}
//	
//	@Test
//	public void testNumberOfSpeedNodes() {
//		UnitTestAgent unitTestAgent = (UnitTestAgent) setupUnitTestAgent("TestAStarJump");
//		
//		Map<Long, SpeedNode> speedNodes = aStar.getSpeedNodes();
//		Map<Integer, Integer> numberOfNodesMap = new HashMap<Integer, Integer>();
//		final int MAX_NUMBER_OF_SPEED_NODES = Hasher.FACTOR_NUMBER_OF_SPEED_NODES * 2 + 1;
//		final int NUMBER_OF_TEST_TICKS = 100;
//		final HashSet<Long>searchedNodes = new HashSet<Long>(); 
//		for (int i=0; i<NUMBER_OF_TEST_TICKS; i++) {
//			TestTools.runOneTick(observation);
//			unitTestAgent.action[Mario.KEY_RIGHT] = true;
//			aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2);
//			
//			for (SpeedNode speedNode : speedNodes.values()) {
//				if (!searchedNodes.contains(speedNode.hash)) {
//					int hashCode = speedNode.node.hashCode();
//					if (numberOfNodesMap.containsKey(hashCode)) {
//						int number = numberOfNodesMap.get(hashCode);
//						numberOfNodesMap.put(hashCode, number+1);
//						//assertTrue(number <= MAX_NUMBER_OF_SPEED_NODES);
//					}
//					else {
//						numberOfNodesMap.put(hashCode, 1);
//					}
//					searchedNodes.add(speedNode.hash);
//				}
//			}
//		}
//		assertTrue("Maximum number " + numberOfNodesMap.values().stream().max(Integer::compare).get() + 
//				   "instead of " + MAX_NUMBER_OF_SPEED_NODES,
//				   numberOfNodesMap.values().stream().allMatch(x -> x <= MAX_NUMBER_OF_SPEED_NODES));
//	}
//	
//	@Test
//	public void testJumpOverEnemy() {
//		setup("testAStarEnemyJumpOver",false);
//		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);
//		
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.renderLevel(observation);
//		List<DirectedEdge> path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2);
//		assertTrue(path != null);
//		
//		for (DirectedEdge edge : path) {
//			assertFalse(edge.target.x == 6);
//		}
//	}
//
//	@Test
//	public void testCollideWithEnemy(){
//		setup("testAStarEnemyJumpOver",false);
//		
//		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);		
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.renderLevel(observation);
//
//		final int columnStart = 13;
//		final int columnEnd = 15;
//
//		Node source = world.getLevelMatrix()[columnStart][marioNode.y];
//		Node target = world.getLevelMatrix()[columnEnd][marioNode.y];
//		
//		List<DirectedEdge> edges = new ArrayList<DirectedEdge>();
//		marioNode = world.getMarioNode(observation);
//		JumpingEdge polynomial = new JumpingEdge(null, null); 
//		polynomial.setToJumpPolynomial(source, columnStart, 2, 4);
//		edgeCreator.jumpAlongPolynomial(source, columnStart, polynomial, JumpDirection.RIGHT_UPWARDS, edges); 
//		
//		assertEquals(1, edges.size());
//		JumpingEdge polynomialEdge = (JumpingEdge) edges.get(0);		
//		assertEquals(target.x, polynomialEdge.target.x);
//		assertEquals(target.y, polynomialEdge.target.y);
//		
//		SpeedNode start = new SpeedNode(source, source.x, 0, Long.MAX_VALUE);
//		start.gScore = 0;
//		start.fScore = 0;
//		
//		SpeedNode end = aStar.getSpeedNode(polynomialEdge, start);
//		
//		assertTrue(end.isSpeedNodeUseable());
//		assertTrue(end.doesMovementCollideWithEnemy(start.gScore, enemyPredictor, 2));		
//	}
//	
//	@Test
//	public void testNotCollideWithEnemy(){
//		setup("testAStarEnemyJumpOver",true);
//		
//		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);		
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.runOneTick(observation);
//		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
//		TestTools.renderLevel(observation);
//		
//		final int columnStart = 11;
//		final int columnEnd = 13;
//
//		Node source = world.getLevelMatrix()[columnStart][marioNode.y];
//		Node target = world.getLevelMatrix()[columnEnd][marioNode.y];
//		
//		List<DirectedEdge> edges = new ArrayList<DirectedEdge>();
//		marioNode = world.getMarioNode(observation);
//		JumpingEdge polynomial = new JumpingEdge(null, null); 
//		polynomial.setToJumpPolynomial(source, columnStart, 2, 4);
//		edgeCreator.jumpAlongPolynomial(source, columnStart, polynomial, JumpDirection.RIGHT_UPWARDS, edges); 
//		
//		assertEquals(1, edges.size());
//		JumpingEdge polynomialEdge = (JumpingEdge) edges.get(0);
//		assertEquals(target.x, polynomialEdge.target.x);
//		assertEquals(target.y, polynomialEdge.target.y);
//		
//		SpeedNode start = new SpeedNode(source, source.x, 0, Long.MAX_VALUE);
//		start.gScore = 0;
//		start.fScore = 0;
//		
//		SpeedNode end = aStar.getSpeedNode(polynomialEdge, start);
//				
//		assertTrue(end.isSpeedNodeUseable());
//		assertFalse(end.doesMovementCollideWithEnemy(start.gScore, enemyPredictor, 2));		
//	}
//	
//	
//	/**
//	 * Tests if A* is able to find a path when it is broken down into multiple ticks
//	 */
//	@Test
//	public void testAStarCanRunMoreThanOneTick() {
//		setup("pit12345");
//		
//		// Should not find path in the given timespan
//		final int TIME_ALLOWED = 1;
//		Node[] goalNodesInBeginning = world.getGoalNodes(0);
//		List<DirectedEdge> path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2, TIME_ALLOWED);
//		assertTrue(path == null);
//		ArrayList<DirectedEdge> pathSegment = aStar.getCurrentBestSegmentPath();
//		
//		// Check pathSegment does not reach end 
//		Node lastNode = null;
//		for (DirectedEdge directedEdge : pathSegment) {
//			lastNode = directedEdge.target;
//		}
//		for (Node node : world.getGoalNodes(0)) {
//			assertNotEquals(lastNode, node);
//		}
//		
//		// Let A* run many times - enough times to make it find a solution path 
//		int c = 0;
//		do {
//			path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2, TIME_ALLOWED);
//			c++;
//			if (c >= 1000) {
//				Assert.fail("AStar never finishes");
//			}
//		} while (path == null);
//		
//		// Check last node in solution path is indeed one of the goal nodes
//		lastNode = null;
//		for (DirectedEdge directedEdge : path) {
//			lastNode = directedEdge.target;
//		}
//		boolean hasFoundGoal = false;
//		for (Node node : goalNodesInBeginning) {
//			if (node != null && node.equals(lastNode)) hasFoundGoal = true;
//		}
//		assertTrue(hasFoundGoal);
//		
//	}
//	
//	/**
//	 * Tests if A* is able to append more nodes on current best path segment
//	 * Note that the test level ensures this will be the way A* should progress towards a final solution
//	 */
//	@Test
//	public void testAStarRunMoreThanOneTickExtendPathSegment() {
//		setup("miniMaze", true);
//		TestTools.setMarioPosition(observation, 2, 12);
//		TestTools.runOneTick(observation);
//		world.update(observation);
//		
//		// This time limit should be short enough to not make A* able to finish in two runs, but still be able to make progress by extending path fragments
//		final int TIME_ALLOWED = 3;
//		List<DirectedEdge> path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2, TIME_ALLOWED);
//		
//		// The path should not be a complete solution but should include some nodes
//		assertNull(path);
//		ArrayList<DirectedEdge> pathSegment = aStar.getCurrentBestSegmentPath();
//		assertNotNull(pathSegment);
//		assertTrue(pathSegment.size() > 0);
//		
//		path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, TIME_ALLOWED);
//		path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, TIME_ALLOWED);
//		path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, TIME_ALLOWED);
//		path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, TIME_ALLOWED);
//		path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, TIME_ALLOWED);
//		path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, TIME_ALLOWED);
//		//assertTrue(path == null);
//		ArrayList<DirectedEdge> newPathSegment = aStar.getCurrentBestSegmentPath();
//		assertTrue(newPathSegment.size() > pathSegment.size());
//		
//		// For this level the nodes in the two pathsegments should be the same up until the end of the shortest one
//		for (int i = 0; i < pathSegment.size(); i++) {
//			assertEquals(pathSegment.get(i), newPathSegment.get(i));
//		}
//		
//	}
//	
//	/**
//	 * Check that no path exists if in a narrow tunnel with an enemy
//	 */
//	@Test
//	public void testTunnelWithEnemy() {
//		setup("straightTunnel", false);
//		TestTools.setMarioPosition(observation, 2, 12);
//		TestTools.spawnEnemy(observation, 12, 12, -1, EnemyType.GREEN_KOOPA);
//		TestTools.runOneTick(observation);
//		//TestTools.renderLevel(observation);
//		world.update(observation);
//		
//		List<DirectedEdge> path = aStar.runMultiNodeAStar(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2);
//		assertNull(path);
//	}
//	
//	@Test
//	public void testJumpStraightUp() {
//		setup("jumpLevels/jumpStraightUp", true);
//		TestTools.setMarioPosition(observation, 3, 12);
//		TestTools.runOneTick(observation);
//
//		DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
//		DebugDraw.drawBlockBeneathMarioNeighbors(observation, world);
//		DebugDraw.drawEdges(observation, world.getLevelMatrix());
//		DebugDraw.drawMarioReachableNodes(observation, world);
//		DebugDraw.drawNodeEdgeTypes(observation, world.getLevelMatrix());
//		world.update(observation);		
//		Node orignalMarioNode = world.getMarioNode(observation);
//		edgeCreator.setMovementEdges(world, orignalMarioNode);		
//		Node[][] level = world.getLevelMatrix();
//		TestTools.renderLevel(observation);
//		//edgeCreator.setMovementEdges(world, orignalMarioNode);		
//		
//		Node[] originalGoalNodes = world.getGoalNodes(0);
//		List<DirectedEdge> path = aStar.runMultiNodeAStar(observation, orignalMarioNode, originalGoalNodes, 0, enemyPredictor, 2);
//		assertNotNull(path);
//		
//		// Check the first elements in the path only consists of jumps
//		assertTrue(path.get(0) instanceof JumpingEdge);
//		assertTrue(path.get(1) instanceof JumpingEdge);
//		assertTrue(path.get(2) instanceof JumpingEdge);
//		//Check if he jumps straight up:
//		assertEquals(level[EdgeCreator.GRID_WIDTH/2][orignalMarioNode.y]		, path.get(0).source);
//		assertEquals(level[EdgeCreator.GRID_WIDTH/2][orignalMarioNode.y - 3]	, path.get(0).target);
//		assertEquals(level[EdgeCreator.GRID_WIDTH/2][orignalMarioNode.y - 3]	, path.get(1).source);
//		assertEquals(level[EdgeCreator.GRID_WIDTH/2][orignalMarioNode.y - 6]	, path.get(1).target);
//		assertEquals(level[EdgeCreator.GRID_WIDTH/2][orignalMarioNode.y - 6]	, path.get(2).source);
//		assertEquals(level[EdgeCreator.GRID_WIDTH/2][orignalMarioNode.y - 9]	, path.get(2).target);
//		
//		verifyPath(aStar, path, originalGoalNodes);
//	}
//
//	/**
//	 * Verify that the same path is found by A* if initiated from a position later in a previously found solution path
//	 * under the condition that the environment does not experience any change.
//	 * @param aStar
//	 * @param path
//	 * @param originalGoalNodes
//	 */
//	private void verifyPath(AStar aStar, List<DirectedEdge> path, Node[] originalGoalNodes) {
//		
//		for (int i = 0; i < path.size(); i++) {
//			Node nextNode = path.get(i).source;
//			List<DirectedEdge> newPath = aStar.runMultiNodeAStar(observation, nextNode, originalGoalNodes, path.get(i).getMoveInfo().getEndSpeed(), enemyPredictor, 2);
//			
//			assertEquals(path.size() - i, newPath.size());
//			// Go through edges and check they are same and verify the movement 
//			//iter.next();
//			
//			for (int j = 0; j < originalGoalNodes.length; j++) {
//				assertEquals(path.get(j + i), newPath.get(i));
//				verifyMoveAlongEdge(aStar, newPath.get(i));				
//			}
//		}
//	}
//
//	/**
//	 * Verify that Mario's actual position is in accordance with the expected position given by the speedNode within A* 
//	 * @param aStar
//	 * @param edge
//	 */
//	private void verifyMoveAlongEdge(AStar aStar, DirectedEdge edge) {
//		final int maxTicksAllowedToRun = 50;
//		int c = 0;
//		while (world.getMarioNode(observation) != edge.target) {
//			c++;
//			TestTools.runOneTick(observation);
//			TestTools.renderLevel(observation);
//			world.update(observation);
//			if (c >= maxTicksAllowedToRun) Assert.fail("Hueston we have a problem. Never reaches destination."); 
//		}
//		
//		long hashCode = Hasher.hashSpeedNode(edge.getMoveInfo().getEndSpeed(), edge);
//		SpeedNode correspondingSpeedNode = aStar.getSpeedNodes().get(hashCode);
//		assertEquals(correspondingSpeedNode.xPos, MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()), MarioControls.ACCEPTED_DEVIATION);
//		assertEquals(correspondingSpeedNode.yPos, MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()), MarioControls.ACCEPTED_DEVIATION);
//	}
}




