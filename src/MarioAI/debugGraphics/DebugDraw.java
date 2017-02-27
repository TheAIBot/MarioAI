package MarioAI.debugGraphics;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import MarioAI.DirectedEdge;
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
<<<<<<< HEAD
		int marioXPos = Math.min(MarioMethods.getMarioXPos(observation.getMarioFloatPos()), 11);
=======
		if (MarioMethods.getMarioXPos(observation.getMarioFloatPos()) - Math.min(0, graph.getMaxMarioXPos() - LEVEL_WIDTH) == 38) {
			int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos()) - Math.max(0, graph.getMaxMarioXPos() - LEVEL_WIDTH);
		}
		
		int marioXPos = Math.min(11,MarioMethods.getMarioXPos(observation.getMarioFloatPos()) - Math.max(0, graph.getMaxMarioXPos() - LEVEL_WIDTH));
>>>>>>> refs/remotes/origin/GraphingOfJustice
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
				
				for (DirectedEdge neighborEdge : groundNode.getEdges()) {
					final Point neighborPoint = new Point(neighborEdge.target.x, neighborEdge.target.y);
					convertLevelPointToOnScreenPoint(observation, neighborPoint);
					neighbors.add(neighborPoint);
				}
				
				((MarioComponent)observation).addDebugPoints(new debugPoints(Color.BLACK, neighbors));
			}	
		}
	}
	
	public static void drawPathOptionNodes(final Environment observation, Graph graph) {
		ArrayList<Point> allPathNodes = new ArrayList<Point>();
		
		Node mario = graph.getMarioNode(observation);
		HashSet<Node> visitedNodes = new HashSet<Node>();
		Queue<Node> nodesToVisit = new LinkedList<Node>();
		nodesToVisit.addAll(mario.getNeighbors());
		
		while (nodesToVisit.size() > 0) {
			Node toCheck = nodesToVisit.poll();
			
			if (!visitedNodes.contains(toCheck)) {
				visitedNodes.add(toCheck);
				nodesToVisit.addAll(toCheck.getNeighbors());
				Point p = new Point(toCheck.x, toCheck.y);
				convertLevelPointToOnScreenPoint(observation, p);
				allPathNodes.add(p);
			}
		}
		((MarioComponent)observation).addDebugPoints(new debugPoints(Color.BLACK, allPathNodes));
	}
	
	private static void convertLevelPointToOnScreenPoint(final Environment observation, final Point point) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		point.x = (int)((point.x - Math.max(marioXPos - (LEVEL_WIDTH / 2), 0)) * BLOCK_PIXEL_SIZE) - (BLOCK_PIXEL_SIZE / 2);
		point.y = (int)((marioYPos * BLOCK_PIXEL_SIZE) + ((point.y - marioYPos) * BLOCK_PIXEL_SIZE)) - (BLOCK_PIXEL_SIZE / 2);
	}

}
