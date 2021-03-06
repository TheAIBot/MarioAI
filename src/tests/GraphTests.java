package tests;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.environments.Environment;
/**
 * @author Andreas
 *
 */
public class GraphTests {
	
	@Test
	public void testLevelMatrixCreation()
	{
		UnitTestAgent agent = new UnitTestAgent();
		Environment observation = TestTools.loadLevel("flat.lvl", agent);
		
		World graph = new World();
		graph.initialize(observation);
		
		final byte[][] levelMap = TestTools.getLevelMap(observation);
		final Node[][] nodeMap = graph.getLevelMatrix();
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int nodeMapStartX = 11 -marioXPos;
		if (!isLevelMapAndNodeMapTheSame(levelMap, 0, nodeMap, nodeMapStartX)) {
			Assert.fail("Maps were not the same");
		}
	}
	/*
	@Test
	public void testLevelMatrixMovement()
	{
		Agent agent = new ForwardAgent();
		Environment observation = TestTools.loadLevel("src/tests/testLevels/testGraphMovement.lvl", agent);
		
		do {
			TestTools.runOneTick(observation);
			Graph graph = new Graph();
			graph.createStartGraph(observation);
			
			final byte[][] levelMap = TestTools.getLevelMap(observation);
			final Node[][] nodeMap = graph.getLevelMatrix();
			final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
			final int nodeMapStartX = 11 - marioXPos;
			if (!isLevelMapAndNodeMapTheSame(levelMap, Math.max(0, marioXPos - 11), nodeMap, nodeMapStartX)) {
				Assert.fail("Maps were not the same");
			}
		} while (observation.getMarioMode() == Mario.STATUS_RUNNING);
	}
	*/
	
	private boolean isLevelMapAndNodeMapTheSame(final byte[][] levelMap, final int levelMapStartX, final Node[][] nodeMap, final int nodeMapStartX)
	{
		for (int x = 0; x < nodeMap.length - nodeMapStartX; x++) {
			for (int y = 0; y < nodeMap[0].length - levelMapStartX; y++) {
				final byte levelByte = levelMap[x + levelMapStartX][y];
				final Node nodeMapNode = nodeMap[x + nodeMapStartX][y];
				
				if (nodeMapNode == null) {
					if (levelByte != 0) {
						return false;
					}
				}
				else {
					if (levelByte == 0) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
