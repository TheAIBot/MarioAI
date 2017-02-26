package MarioAI.debugGraphics;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import MarioAI.Graph;
import MarioAI.MarioMethods;
import MarioAI.Node;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;

public class DebugDraw {
	
	public static final int LEVEL_HEIGHT = 15;
	public static final int LEVEL_WIDTH = 22;
	private static final int BLOCK_PIXEL_SIZE = 16;
	
	public static void resetGraphics(final Environment observation) {
		((MarioComponent)observation).resetDebugGraphics();
	}
	
	public static void drawPath(final Environment observation, final List<Node> path)
	{
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final ArrayList<Point> pathLines = new ArrayList<Point>();
		final ArrayList<Point> pathCircles = new ArrayList<Point>(); 
		
		final Point marioPoint = new Point(marioXPos, marioYPos);
		convertLevelPointToOnScreenPoint(observation, marioPoint);
		pathLines.add(marioPoint);
		
		for (int i = 0; i < path.size(); i++) {
			final Node node = path.get(i);
			final Point point = new Point(node.x, node.y);
    		
			convertLevelPointToOnScreenPoint(observation, point);
			pathLines.add(point);
			pathCircles.add(point);
		}
		
		((MarioComponent)observation).addDebugLines(new debugLines(Color.RED, pathLines));
		((MarioComponent)observation).addDebugPoints(new debugPoints(Color.RED, pathCircles));
	}
	
	public static void drawBlockBeneathMarioNeighbors(final Environment observation, Graph graph) {
		int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos()) - Math.max(0, graph.getMaxMarioXPos() - LEVEL_WIDTH);
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		final Node[][] levelMatrix =  graph.getLevelMatrix();
		
		int groundYLevel = marioYPos;
		while(groundYLevel < LEVEL_HEIGHT && levelMatrix[marioXPos][groundYLevel] == null) {
			groundYLevel++;
		}
		
		if (groundYLevel < LEVEL_HEIGHT) {
			final Node groundNode = levelMatrix[marioXPos][groundYLevel];
			if (groundNode != null) {
				ArrayList<Point>neighbors = new ArrayList<Point>();
				
				for (Node neighbor : groundNode.getNeighbors()) {
					final Point neighborPoint = new Point(neighbor.x, neighbor.y);
					convertLevelPointToOnScreenPoint(observation, neighborPoint);
					neighbors.add(neighborPoint);
				}
				
				((MarioComponent)observation).addDebugPoints(new debugPoints(Color.BLACK, neighbors));
			}	
		}
	}
	
	private static void convertLevelPointToOnScreenPoint(final Environment observation, final Point point) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		point.x = (int)((point.x - Math.max(marioXPos - (LEVEL_WIDTH / 2), 0)) * BLOCK_PIXEL_SIZE) - (BLOCK_PIXEL_SIZE / 2);
		point.y = (int)((marioYPos * BLOCK_PIXEL_SIZE) + ((point.y - marioYPos) * BLOCK_PIXEL_SIZE)) - (BLOCK_PIXEL_SIZE / 2);
	}
}
