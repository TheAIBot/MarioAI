package tests;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.edgeCreation.EdgeCreator;
import MarioAI.graph.nodes.Node;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.environments.Environment;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestJumpOnEnemies {
	EdgeCreator grapher = new EdgeCreator();
	Environment observation;
	private static final int GRID_WIDTH = 22;
	public Node marioNode;
	public DirectedEdge runningEdgeType = new RunningEdge(null, null, false);
	// TODO add more tests for collisions.

	private short getColoumnRelativeToMario(int xPosition) {
		// Assumes that node!=null.
		return (short) ((xPosition - marioNode.x) + GRID_WIDTH / 2);
	}

	public static short getXPositionFromColoumn(Node marioNode, int coloumn) {
		// Assumes that node!=null.
		return (short) (coloumn + marioNode.x - GRID_WIDTH / 2);
	}

	public World getStartLevelWorld(String level) {
		BasicAIAgent agent = new BasicAIAgent("");
		observation = TestTools.loadLevel(level, agent);
		TestTools.runOneTick(observation);
		World graph = new World();
		graph.initialize(observation);
		this.marioNode = graph.getMarioNode(observation);
		return graph;
	}

	public World flatlandWorld() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flat.lvl");
		//grapher.setMovementEdges(graph, marioNode);
		return graph;
	}

	@Test
	public void testCorrectMarioStartPosition() {
		EdgeCreator grapher = new EdgeCreator();
		World graph = getStartLevelWorld("flat.lvl");
		grapher.setMovementEdges(graph, marioNode);
		assertEquals(2, marioNode.x);
		assertEquals(9, marioNode.y);
	}
}
