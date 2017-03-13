package MarioAI.debugGraphics;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import MarioAI.MarioMethods;
import MarioAI.Running;
import MarioAI.SecondOrderPolynomial;
import MarioAI.graph.DirectedEdge;
import MarioAI.graph.Graph;
import MarioAI.graph.Node;
import ch.idsia.mario.engine.Art;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;

public class DebugDraw {

	public static final int LEVEL_HEIGHT = 15;
	public static final int LEVEL_WIDTH = 22;
	private static final int BLOCK_PIXEL_SIZE = 16;

	public static void resetGraphics(final Environment observation) {
		((MarioComponent) observation).resetDebugGraphics();
	}

	public static void drawPath(final Environment observation, final List<DirectedEdge> path) {
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());

		final ArrayList<Point> pathLines = new ArrayList<Point>();
		final ArrayList<Point> pathCirclesPolynomial = new ArrayList<Point>();
		final ArrayList<Point> pathCirclesRunning = new ArrayList<Point>();

		final Point marioPoint = new Point(marioXPos, marioYPos);
		convertLevelPointToOnScreenPoint(observation, marioPoint);
		pathLines.add(marioPoint);

		for (int i = 0; i < path.size(); i++) {
			final Node node = path.get(i).target;
			final Point point = new Point(node.x, node.y);

			convertLevelPointToOnScreenPoint(observation, point);
			pathLines.add(point);
			if (path.get(i) instanceof SecondOrderPolynomial) {
				pathCirclesPolynomial.add(point);
			} else pathCirclesRunning.add(point);
		}

		((MarioComponent) observation).addDebugLines(new debugLines(Color.RED, pathLines));
		((MarioComponent) observation).addDebugPoints(new debugPoints(Color.DARK_GRAY, pathCirclesPolynomial));
		((MarioComponent) observation).addDebugPoints(new debugPoints(Color.RED, pathCirclesRunning));
	}

	public static void drawBlockBeneathMarioNeighbors(final Environment observation, Graph graph) {
		final int marioXPos = Math.min(MarioMethods.getMarioXPos(observation.getMarioFloatPos()), LEVEL_WIDTH / 2);
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		final Node[][] levelMatrix = graph.getLevelMatrix();

		int groundYLevel = marioYPos;
		while (groundYLevel < LEVEL_HEIGHT && levelMatrix[marioXPos][groundYLevel] == null) {
			groundYLevel++;
		}

		if (groundYLevel < LEVEL_HEIGHT) {
			final Node groundNode = levelMatrix[marioXPos][groundYLevel];
			if (groundNode != null) {
				ArrayList<Point> neighbors = new ArrayList<Point>();

				for (DirectedEdge neighborEdge : groundNode.getEdges()) {
					final Point neighborPoint = new Point(neighborEdge.target.x, neighborEdge.target.y);
					convertLevelPointToOnScreenPoint(observation, neighborPoint);
					neighbors.add(neighborPoint);
				}

				((MarioComponent) observation).addDebugPoints(new debugPoints(Color.BLACK, neighbors));
			}
		}
	}

	public static void drawPathOptionNodes(final Environment observation, Graph graph) {
		ArrayList<Point> allrunningEdges = new ArrayList<Point>();
		ArrayList<Point> allJumpingEdges = new ArrayList<Point>();

		Node mario = graph.getMarioNode(observation);
		HashSet<Node> visitedRunningNodes = new HashSet<Node>();
		HashSet<Node> visitedJumpingNodes = new HashSet<Node>();
		Queue<DirectedEdge> nodesToVisit = new LinkedList<DirectedEdge>();
		nodesToVisit.addAll(mario.getEdges());

		while (nodesToVisit.size() > 0) {
			DirectedEdge fisk = nodesToVisit.poll();
			Node toCheck = fisk.target;
			if (!visitedRunningNodes.contains(toCheck) && fisk instanceof Running) {
				nodesToVisit.addAll(toCheck.getEdges());
				Point p = new Point(toCheck.x, toCheck.y);
				convertLevelPointToOnScreenPoint(observation, p);
				visitedRunningNodes.add(toCheck);
				allrunningEdges.add(p);
			} else if (!visitedJumpingNodes.contains(toCheck) && fisk instanceof SecondOrderPolynomial) {
				nodesToVisit.addAll(toCheck.getEdges());
				Point p = new Point(toCheck.x, toCheck.y);
				convertLevelPointToOnScreenPoint(observation, p);
				visitedJumpingNodes.add(toCheck);
				allJumpingEdges.add(p);
			}
		}
		((MarioComponent) observation).addDebugPoints(new debugPoints(Color.BLACK, allrunningEdges));
		((MarioComponent) observation).addDebugPoints(new debugPoints(Color.WHITE, allJumpingEdges, 6));
	}

	public static void drawNeighborPaths(final Environment observation, Graph graph) {
		Node mario = graph.getMarioNode(observation);
		HashSet<Node> visitedNodes = new HashSet<Node>();
		Queue<DirectedEdge> nodesToVisit = new LinkedList<DirectedEdge>();
		nodesToVisit.addAll(mario.getEdges());

		while (nodesToVisit.size() > 0) {
			DirectedEdge fisk = nodesToVisit.poll();
			Node toCheck = fisk.target;
			if (!visitedNodes.contains(toCheck)) {
				nodesToVisit.addAll(toCheck.getEdges());
				visitedNodes.add(toCheck);				
				
				for (DirectedEdge directedEdge : toCheck.getEdges()) {
					Point pSource = new Point(directedEdge.source.x, directedEdge.source.y);
					convertLevelPointToOnScreenPoint(observation, pSource);
					
					Point pTarget = new Point(directedEdge.target.x,directedEdge.target.y);
					convertLevelPointToOnScreenPoint(observation, pTarget);
					if (pSource.y >= pTarget.y) {
						((MarioComponent)observation).addDebugLines(new debugLines(Color.GREEN, pSource,pTarget));
					}
					else {
						((MarioComponent)observation).addDebugLines(new debugLines(Color.ORANGE, pSource,pTarget));
					}
				}		
			}
		}
	}

	public static void drawEndNodes(final Environment observation, Graph graph) {
		ArrayList<Point> allEndPoints = new ArrayList<Point>();
		Node mario = graph.getMarioNode(observation);
		for (int i = 0; i < 15; i++) {
			Point p = new Point(mario.x + 10, i);
			convertLevelPointToOnScreenPoint(observation, p);
			allEndPoints.add(p);
		}
		((MarioComponent) observation).addDebugPoints(new debugPoints(Color.BLUE, allEndPoints, 12));
	}

	/**
	 * Draw reachable nodes from Mario's current position
	 * 
	 * @param observation
	 * @param graph
	 */
	public static void drawReachableNodes(final Environment observation, Graph graph) {
		Node mario = graph.getMarioNode(observation);
		List<DirectedEdge> edges = mario.getEdges();

		for (DirectedEdge edge : edges) {
			Node toCheck = edge.target;
			addDebugLines(observation, toCheck);
		}
	}

	/**
	 * Auxiliary method
	 * 
	 * @param observation
	 * @param toCheck
	 */
	private static void addDebugLines(final Environment observation, Node toCheck) {
		for (DirectedEdge directedEdge : toCheck.getEdges()) {
			Point pSource = new Point(directedEdge.source.x, directedEdge.source.y);
			convertLevelPointToOnScreenPoint(observation, pSource);

			Point pTarget = new Point(directedEdge.target.x, directedEdge.target.y);
			convertLevelPointToOnScreenPoint(observation, pTarget);
			if (pSource.y >= pTarget.y) {
				((MarioComponent) observation).addDebugLines(new debugLines(Color.GREEN, pSource, pTarget));
			} else {
				((MarioComponent) observation).addDebugLines(new debugLines(Color.ORANGE, pSource, pTarget));
			}
		}
	}

	private static void convertLevelPointToOnScreenPoint(final Environment observation, final Point point) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		point.x = ((int)((point.x - (Math.max(marioXPos - (LEVEL_WIDTH / 2) + 1, 0)) + 1) * BLOCK_PIXEL_SIZE) - (BLOCK_PIXEL_SIZE / 2)) * Art.SIZE_MULTIPLIER;
		point.y = ((int)((marioYPos * BLOCK_PIXEL_SIZE) + ((point.y - marioYPos) * BLOCK_PIXEL_SIZE)) - (BLOCK_PIXEL_SIZE / 2)) * Art.SIZE_MULTIPLIER;
	}

}
