package tests;

import org.junit.Test;
import org.junit.Assert;

import MarioAI.Graph;
import MarioAI.MarioMethods;
import MarioAI.Node;

import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.environments.Environment;

public class GraphTests {
	
	@Test
	public void testLevelMatrixCreation()
	{
		BasicAIAgent agent = new BasicAIAgent("");
		Environment observation = TestTools.loadLevel("src/tests/testLevels/flat.lvl", agent);
		
		TestTools.runOneTick(observation);
		Graph graph = new Graph();
		graph.createStartGraph(observation);
		
		final byte[][] levelMap = TestTools.getLevelMap(observation);
		final Node[][] nodeMap = graph.getLevelMatrix();
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int nodeMapStartX = 11 -marioXPos;
		if (!isLevelMapAndNodeMapTheSame(levelMap, 0, nodeMap, nodeMapStartX)) {
			Assert.fail("Maps were not the same");
		}
	}
	
	private boolean isLevelMapAndNodeMapTheSame(final byte[][] levelMap, final int levelMapStartX, final Node[][] nodeMap, final int nodeMapStartX)
	{
		for (int x = 0; x < nodeMap.length - nodeMapStartX; x++) {
			for (int y = 0; y < nodeMap[0].length; y++) {
				if (levelMap[x + levelMapStartX][y] != nodeMap[x + nodeMapStartX][y].type) {
					return false;
				}
			}
		}
		return true;
	}
}
