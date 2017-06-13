package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.validator.PublicClassValidator;

import MarioAI.FastAndFurious;
import MarioAI.Hasher;
import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.debugGraphics.DebugDraw;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.EnemyType;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.edgeCreation.EdgeCreator;
import MarioAI.graph.edges.edgeCreation.JumpDirection;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class TestAStar {
	FastAndFurious agent;
	Environment observation;
	final float delta = MarioControls.ACCEPTED_DEVIATION; //0.05f;
	World world;
	EdgeCreator edgeCreator;
	Node marioNode;
	EnemyPredictor enemyPredictor;
	MarioControls marioControls;
	
	public void setup(String levelName) {
		setup(levelName, false);
	}
	
	public void setup(String levelName, boolean showLevel) {
		agent = new FastAndFurious();
		observation = TestTools.loadLevel(levelName + ".lvl", agent, showLevel);
		DebugDraw.resetGraphics(observation);
		TestTools.runOneTick(observation);
		world = agent.world;
		edgeCreator = agent.grapher;
		enemyPredictor = agent.enemyPredictor;
		marioControls = agent.marioController;
		marioNode = world.getMarioNode(observation);
	}
	
	/**
	 * Test if A Star returns path with only Running Directed Edges on a flat level
	 */
	@Test
	public void testAStarRunning() {
		setup("flat");
		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
		List<DirectedEdge> path = agent.pathCreator.getBestPath();
		assertNotNull(path);
		//He should run through the level:
		for (DirectedEdge directedEdge : path) {
			assertTrue(directedEdge instanceof RunningEdge);
		}
		assertEquals(12, path.get(path.size() - 1).target.x); //Correct x end destination
		assertEquals(marioNode.y, path.get(path.size() - 1).target.y); //Correct y end destination
	}
	
	/**
	 * Tests that Mario only jumps when nessecary
	 */
	@Test
	public void testOnlyJumpsOnce() {
		setup("flatWithJump3", false);
		TestTools.runOneTick(observation);
		
		// run until Mario finishes the level, counting the number of jumps on the way
		int numberOfJumps = 0;
		while (((MarioComponent) observation).getMarioStatus() != Mario.STATUS_WIN) {
			TestTools.runOneTick(observation);
			agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
			List<DirectedEdge> path = agent.pathCreator.getBestPath();
			numberOfJumps += path.stream().filter(edge -> edge instanceof JumpingEdge).count();
		}
		
		// There should only be one jump nessecary in this level
		assertEquals("number of jumps: " + numberOfJumps, 1, numberOfJumps);
		
		TestTools.runWholeLevel(observation);
	}
	
//	@Test
//	public void testTakeFastestJump() {
//		setup("flatWithJump2", true);
//		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world);
//		List<DirectedEdge> path = agent.pathCreator.getBestPath();
//		int numberOfActions = 1;
//		int numberOfTicks = 0;
////		DebugDraw.resetGraphics(observation);
////		DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
////		DebugDraw.drawPathMovement(observation, path);
////		TestTools.renderLevel(observation);
//		assertNotNull(path);
//		assertEquals("Fail at action: " + numberOfActions + ", at tick: " + numberOfTicks, 1, path.stream().filter(edge -> edge instanceof JumpingEdge).count()); //Should only jump once
//		//Assert.fail("Test will run forever after this line, though it works as expected");
//		while(numberOfActions <= 5){
//			if (marioControls.canUpdatePath && world.hasGoalNodesChanged() || 
//				 path.size() > 0 && MarioControls.isPathInvalid(observation, path)) {
//				 numberOfActions++;
//				 agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world);
//				 path = agent.pathCreator.getBestPath();
////				 DebugDraw.resetGraphics(observation);
////				 DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
////				 DebugDraw.drawPathMovement(observation, path);
////				 TestTools.renderLevel(observation);
//				 assertNotNull(path);
//				 assertEquals("Fail at action: " + numberOfActions + ", at tick: " + numberOfTicks, 1, path.stream().filter(edge -> edge instanceof JumpingEdge).count()); //Should only jump once
//			}
//			TestTools.runOneTick(observation);
//			numberOfTicks++;
//		}
//		assertEquals(1, path.stream().filter(edge -> edge instanceof JumpingEdge).count());
//		TestTools.runWholeLevel(observation);
//	}
	
	
	
	/**
	 * Test if A Star returns a path where Mario is required to jump in the beginning on multiple platforms
	 */
	@Test
	public void testAStarJumping() {
		setup("TestAStarJump", true);
		Node[] originalGoalNodes = world.getGoalNodes(0);
		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
		List<DirectedEdge> path = agent.pathCreator.getBestPath();

		//TestTools.runOneTick(observation);
		assertNotNull(path);
		
//		DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
//		DebugDraw.drawPathMovement(observation, path);
//		TestTools.renderLevel(observation);
		assertEquals(10, path.size());
		
		for (int i = 0; i < 3; i++) { //Jumping edges
			assertTrue(path.get(i) instanceof JumpingEdge);
		}
		
		for (int i = 4; i < path.size(); i++) { //Running
			assertTrue(path.get(i) instanceof RunningEdge);
		}
		
		verifyPath(path, originalGoalNodes);
		
	}
	
	/**
	 * Tests that number of speednodes for an edge never exceeds the granularity (i.e. the number of velocities a speednode can have)
	 */
	@Test
	public void testNumberOfSpeedNodes() {
		setup("TestAStarJump", false);
		//agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world);
		
		Map<Long, SpeedNode> speedNodes = agent.pathCreator.getSpeedNodes();
		Map<Integer, Integer> numberOfNodesMap = new HashMap<Integer, Integer>();
		final int MAX_NUMBER_OF_SPEED_NODES = agent.pathCreator.getBlockingGranularity() * 2 + 1;
		final int NUMBER_OF_TEST_TICKS = 100;
		final HashSet<Long>searchedNodes = new HashSet<Long>(); 
		for (int i=0; i<NUMBER_OF_TEST_TICKS; i++) {
			TestTools.runOneTick(observation);
			agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation), world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
//			List<DirectedEdge> path = agent.pathCreator.getBestPath();
			
			for (SpeedNode speedNode : speedNodes.values()) {
				if (!searchedNodes.contains(speedNode.hash)) {
					int hashCode = speedNode.ancestorEdge.hashCode(); // used to be: speedNode.node.hashCode();
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
	
	/**
	 * Test that Mario is not jumping on the block that holds an enemy
	 */
	@Test
	public void testJumpOverEnemy() {
		setup("testAStarEnemyJumpOver", true);
		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		
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
		
//		TestTools.renderLevel(observation);
		Node[] originalGoalNodes = world.getGoalNodes(0);
		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
		List<DirectedEdge> path = agent.pathCreator.getBestPath();
		assertNotNull(path);
		
		assertFalse(path.stream().anyMatch(x -> x.target.x == 6));
		
		verifyPath(path, originalGoalNodes);
	}
	
	
	/**
	 * Test collision with enemies using other functionality than A*
	 */
	@Test
	public void testNotCollideWithEnemy() {
		setup("testAStarEnemyJumpOver", false);
		
		TestTools.spawnEnemy(observation, 6, 10, 1, EnemyType.RED_KOOPA);		
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
//		TestTools.renderLevel(observation);
		
		final int columnStart = 11;
		final int columnEnd = 13;

		Node source = world.getLevelMatrix()[columnStart][marioNode.y];
		Node target = world.getLevelMatrix()[columnEnd][marioNode.y];
		
		List<DirectedEdge> edges = new ArrayList<DirectedEdge>();
		marioNode = world.getMarioNode(observation);
		JumpingEdge polynomial = new JumpingEdge(null, null); 
		polynomial.setToJumpPolynomial(source, columnStart, 2, 4);
		edgeCreator.jumpAlongPolynomial(source, columnStart, polynomial, JumpDirection.RIGHT_UPWARDS, edges); 
		
		assertEquals(1, edges.size());
		JumpingEdge polynomialEdge = (JumpingEdge) edges.get(0);
		assertEquals(target.x, polynomialEdge.target.x);
		assertEquals(target.y, polynomialEdge.target.y);
		
		SpeedNode start = new SpeedNode(source, source.x, 0, Long.MAX_VALUE, 3); //TODO
		start.gScore = 0;
		start.fScore = 0;
		
		Long2ObjectOpenHashMap<SpeedNode> speedNodes = agent.pathCreator.getSpeedNodes();
		SpeedNode end = speedNodes.values().stream().filter(x -> x.ancestorEdge != null && x.ancestorEdge.equals(polynomialEdge))
													.findFirst().get();
		
		assertTrue(end.isSpeedNodeUseable(world));
		assertFalse(end.doesMovementCollideWithEnemy(start.gScore, enemyPredictor, 2));		
	}
	
	/**
	 * Check that no path exists if in a narrow tunnel with an enemy when life is 1
	 */
	@Test
	public void testTunnelWithEnemyOneLife() {
		setup("straightTunnel", false);
		
		//SpeedNode.MAX_MARIO_LIFE = 1; // make sure Mario will only have one life in this test (technically he will have more, but he will act as if he only had one)
		
		TestTools.setMarioPosition(observation, 2, 12);
		TestTools.spawnEnemy(observation, 7, 12, -1, EnemyType.GREEN_KOOPA);
		world.update(observation);
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		//TestTools.renderLevel(observation);
		
		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 1);
		List<DirectedEdge> path = agent.pathCreator.getBestPath();
		//assertNull(path);
		assertNull(path); // this assumes Mario will see that there is no path not colliding with enemies and has to choose it anyway eventhough the fscore is high.
	}
	
	/**
	 * Check that Mario can go through an enemy because there is no alternative path assuming Mario has more than one life left
	 */
	@Test
	public void testTunnelWithEnemyMoreThanOneLife() {
		setup("straightTunnel", true);
		TestTools.setMarioPosition(observation, 2, 12);
		TestTools.spawnEnemy(observation, 7, 12, -1, EnemyType.GREEN_KOOPA);
		world.update(observation);
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		//TestTools.renderLevel(observation);
		
		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
		List<DirectedEdge> path = agent.pathCreator.getBestPath();
		//assertNull(path);
		assertNotNull(path); // this assumes Mario will see that there is no path not colliding with enemies and has to choose it anyway, eventhough the fscore is high.
		
		// Go through the path and check that there is a collision
		Long2ObjectOpenHashMap<SpeedNode> speedNodes = agent.pathCreator.getSpeedNodes();
		boolean hasHitEnemy = false;
		for (DirectedEdge edge : path) {
			SpeedNode sn = speedNodes.values().stream()
											   .filter(x -> x.ancestorEdge.equals(edge))
											   .findFirst().get();
			if (edge.getMoveInfo().hasCollisions(sn, world)) hasHitEnemy = true;
			//else System.out.println("NOPE");
		}
		assertTrue(hasHitEnemy);
	}
	
	/**
	 * Tests if Mario is able to jump straight up (the edges the path found should be the right type, JumpingEdge)
	 */
	@Test
	public void testJumpStraightUp() {
		setup("jumpLevels/jumpStraightUp", true);
		TestTools.setMarioPosition(observation, 3, 12);
		world.update(observation);
		
		int marioBeginningXPos = world.getMarioNode(observation).x; // should be 3
		int marioBeginningYPos = world.getMarioNode(observation).y; // should be 12
		
		TestTools.runOneTick(observation);

//		DebugDraw.drawGoalNodes(observation, world.getGoalNodes(0));
//		DebugDraw.drawBlockBeneathMarioNeighbors(observation, world);
//		DebugDraw.drawEdges(observation, world.getLevelMatrix());
//		DebugDraw.drawMarioReachableNodes(observation, world);
//		DebugDraw.drawNodeEdgeTypes(observation, world.getLevelMatrix());
		
//		Node orignalMarioNode = world.getMarioNode(observation);
//		edgeCreator.setMovementEdges(world, orignalMarioNode);		

//		TestTools.renderLevel(observation);
		
		Node[] originalGoalNodes = world.getGoalNodes(0);
		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
		List<DirectedEdge> path = agent.pathCreator.getBestPath();
		assertNotNull(path);
		
		// --- Check the first elements in the path only consist of jumps
		
		assertTrue(path.get(0) instanceof JumpingEdge);
		assertEquals(marioBeginningXPos, path.get(0).target.x);
		assertEquals(marioBeginningYPos - 3, path.get(0).target.y);
		
		assertTrue(path.get(1) instanceof JumpingEdge);
		assertEquals(marioBeginningXPos, path.get(1).target.x);
		assertEquals(marioBeginningYPos - 2 * 3, path.get(1).target.y);
		
		assertTrue(path.get(2) instanceof JumpingEdge);
		assertEquals(marioBeginningXPos, path.get(2).target.x);
		assertEquals(marioBeginningYPos - 3 * 3, path.get(2).target.y);
		
		verifyPath(path, originalGoalNodes);
	}
	
	/**
	 * Tests if Mario is making many low jumps instead of longer jumps, given that the ceiling only allows the former
	 */
	@Test
	public void testLowJumpCourse() {
		setup("jumpLevels/semiAdvancedJumpingCourse", true);
		TestTools.runOneTick(observation);
		//TestTools.runWholeLevel(observation);
		
		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
		Node[] originalGoalNodes = world.getGoalNodes(0);
		List<DirectedEdge> path = agent.pathCreator.getBestPath();
		assertNotNull(path);
		
		// check the jumps reach the expected nodes
		final int marioInitialXPos = 2;
		assertTrue(path.size() <= 4);
		for (int i = 0; i < path.size(); i++) {
			DirectedEdge edge = path.get(i);
			assertTrue(edge instanceof JumpingEdge);
			assertEquals(edge.source.x, marioInitialXPos + i * 2);
		}
		
		verifyPath(path, originalGoalNodes);
	}
	
	/**
	 * Test that Mario's ticks of invincibility and life is set correctly after hitting and enemy
	 */
	@Test
	public void testTicksOfInvincibility() {
		setup("straightTunnel", true);
		TestTools.setMarioPosition(observation, 2, 12);
		
		// spawn two enemies
		int enemyOneXStartPos = 7; 
		TestTools.spawnEnemy(observation, enemyOneXStartPos, 12, -1, EnemyType.GREEN_KOOPA);
		TestTools.spawnEnemy(observation, enemyOneXStartPos - 1, 12, -1, EnemyType.GREEN_KOOPA);
		world.update(observation);
		TestTools.runOneTick(observation);
		enemyPredictor.updateEnemies(observation.getEnemiesFloatPos());
		
		agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
		List<DirectedEdge> path = agent.pathCreator.getBestPath();
		assertNotNull(path);
		
		// Go through the path and check that a collision will occur
		Long2ObjectOpenHashMap<SpeedNode> speedNodes = agent.pathCreator.getSpeedNodes();
		boolean willHitEnemy = false;
		SpeedNode sn = null;
		SpeedNode newSn = sn;
		
		for (DirectedEdge edge : path) {
			newSn = speedNodes.values().stream()
					.filter(x -> x.ancestorEdge.equals(edge))
					.findFirst().get();
			if (willHitEnemy) break;
			if (edge.getMoveInfo().hasCollisions(sn, world)) {
				willHitEnemy = true;
			}
			sn = newSn;
		}
		assertTrue(willHitEnemy);
//		assertTrue(sn.currentXPos < enemyOneXStartPos - 0.5);
		
		// Verify ticks of invincibility
		assertNotNull(sn);
		assertEquals(SpeedNode.MAX_TICKS_OF_INVINCIBILITY, sn.ticksOfInvincibility);
		assertEquals(sn.ticksOfInvincibility - newSn.ticksOfInvincibility, newSn.ancestorEdge.getMoveInfo().getMoveTime());
		
		// Check that Mario has lost only one life when he reaches the end of the level
		// This should be the case as the second enemy is close enough for Mario's invincibility not to have worn off
		int marioStatus = TestTools.runWholeLevelWillWin(observation);
		assertEquals(Mario.STATUS_WIN, marioStatus);
		assertEquals(2, observation.getMarioMode());
		
	}
	
	/**
	 * Verify that the same path is found by A* if initiated from a position later in a previously found solution path
	 * under the condition that the environment does not experience any change.
	 * @param aStar
	 * @param path
	 * @param originalGoalNodes
	 */
	private void verifyPath(List<DirectedEdge> path, Node[] originalGoalNodes) {
		for (int i = 0; i < path.size(); i++) {
			Node nextNode = path.get(i).source;
			agent.pathCreator.blockingFindPath(observation, nextNode, originalGoalNodes, 0, enemyPredictor, 2, world, false, 3); // TODO
			List<DirectedEdge> newPath = agent.pathCreator.getBestPath();
			
			// Go through edges and check they are same and verify the movement 
			assertEquals(path.size() - i, newPath.size());
			for (int j = i; j < path.size(); j++) {
				assertEquals(path.get(j), newPath.get(j - i));
			}
		}
	}
	
//	private void verifyPath(List<DirectedEdge> path, Node[] originalGoalNodes) {
//		
//		float oldSpeed = 0;
//		for (int i = 0; i < path.size(); i++) {
////			Node nextNode = path.get(i).source;
////			agent.pathCreator.blockingFindPath(observation, world.getMarioNode(observation),  world.getGoalNodes(0), 0, enemyPredictor, 2, world, false, 3);
////			List<DirectedEdge> newPath = agent.pathCreator.getBestPath();
//			
////			assertEquals(path.size() - i, newPath.size());
//			// Go through edges and check they are same and verify the movement 
//			//iter.next();
//			
////			for (int j = 0; j < originalGoalNodes.length; j++) {
////				assertEquals(path.get(j + i), newPath.get(i));
//			final DirectedEdge edge = path.get(0);
//			verifyMoveAlongEdge(edge, oldSpeed);
//			oldSpeed = edge.getMoveInfo().getEndSpeed();
////			}
//		}
//	}

	/**
	 * Verify that Mario's actual position is in accordance with the expected position given by the speedNode within A* 
	 * @param aStar
	 * @param edge
	 */
//	private void verifyMoveAlongEdge(DirectedEdge edge, float vx) {
//		for (int i = 0; i < edge.getMoveInfo().getMoveTime(); i++) {
//			TestTools.runOneTick(observation);
//		}
//		TestTools.runOneTick(observation);
//		
//		// Compare speednode with Mario's position
//		long hashCode = Hasher.hashSpeedNode(vx, edge, 5000); //TODO
//		SpeedNode correspondingSpeedNode = agent.pathCreator.getSpeedNodes().get(hashCode);
//		assertEquals(correspondingSpeedNode.currentXPos, MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()), MarioControls.ACCEPTED_DEVIATION);
//		assertEquals(correspondingSpeedNode.yPos - (1f / World.PIXELS_PER_BLOCK), MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()), MarioControls.ACCEPTED_DEVIATION);
//	}
	
}



