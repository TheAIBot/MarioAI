package MarioAI.debugGraphics;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import MarioAI.MarioMethods;
import MarioAI.graph.Graph;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.edges.SecondOrderPolynomial;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.engine.Art;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class DebugDraw {

	public static final int LEVEL_HEIGHT = 15;
	public static final int LEVEL_WIDTH = 22;
	private static final int BLOCK_PIXEL_SIZE = 16;

	public static void resetGraphics(final Environment observation) {
		((MarioComponent) observation).resetDebugGraphics();
	}

	public static void drawPath(final Environment observation, final List<DirectedEdge> path) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());

		final ArrayList<Point> pathLines = new ArrayList<Point>();
		final ArrayList<Point> pathCirclesPolynomial = new ArrayList<Point>();
		final ArrayList<Point> pathCirclesRunning = new ArrayList<Point>();

		final Point2D.Float marioPoint = new Point2D.Float(marioXPos, marioYPos);
		convertLevelPointToOnScreenPoint(observation, marioPoint);
		pathLines.add(new Point((int)marioPoint.x, (int)marioPoint.y));

		for (int i = 0; i < path.size(); i++) {
			final Node node = path.get(i).target;
			final Point point = new Point(node.x, node.y);

			convertLevelPointToOnScreenPoint(observation, point);
			pathLines.add(point);
			if (path.get(i) instanceof SecondOrderPolynomial) {
				pathCirclesPolynomial.add(point);
			} else {
				pathCirclesRunning.add(point);
			}
		}

		((MarioComponent) observation).addDebugDrawing(new DebugLines(Color.RED, pathLines));
		((MarioComponent) observation).addDebugDrawing(new DebugPoints(Color.DARK_GRAY, pathCirclesPolynomial));
		((MarioComponent) observation).addDebugDrawing(new DebugPoints(Color.RED, pathCirclesRunning));
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

				((MarioComponent) observation).addDebugDrawing(new DebugPoints(Color.BLACK, neighbors));
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
			DirectedEdge edge = nodesToVisit.poll();
			Node toCheck = edge.target;
			if (!visitedRunningNodes.contains(toCheck) && edge instanceof Running) {
				nodesToVisit.addAll(toCheck.getEdges());
				Point p = new Point(toCheck.x, toCheck.y);
				convertLevelPointToOnScreenPoint(observation, p);
				visitedRunningNodes.add(toCheck);
				allrunningEdges.add(p);
			} else if (!visitedJumpingNodes.contains(toCheck) && edge instanceof SecondOrderPolynomial) {
				nodesToVisit.addAll(toCheck.getEdges());
				Point p = new Point(toCheck.x, toCheck.y);
				convertLevelPointToOnScreenPoint(observation, p);
				visitedJumpingNodes.add(toCheck);
				allJumpingEdges.add(p);
			}
		}
		((MarioComponent) observation).addDebugDrawing(new DebugPoints(Color.BLACK, allrunningEdges, 12));
		((MarioComponent) observation).addDebugDrawing(new DebugPoints(Color.WHITE, allJumpingEdges, 6));
	}

	public static void drawNeighborPaths(final Environment observation, Graph graph) {
		Node mario = graph.getMarioNode(observation);
		HashSet<Node> visitedNodes = new HashSet<Node>();
		Queue<DirectedEdge> nodesToVisit = new LinkedList<DirectedEdge>();
		nodesToVisit.addAll(mario.getEdges());

		while (nodesToVisit.size() > 0) {
			DirectedEdge edge = nodesToVisit.poll();
			Node toCheck = edge.target;
			if (!visitedNodes.contains(toCheck)) {
				nodesToVisit.addAll(toCheck.getEdges());
				visitedNodes.add(toCheck);				
				
				for (DirectedEdge directedEdge : toCheck.getEdges()) {
					Point pSource = new Point(directedEdge.source.x, directedEdge.source.y);
					convertLevelPointToOnScreenPoint(observation, pSource);
					
					Point pTarget = new Point(directedEdge.target.x,directedEdge.target.y);
					convertLevelPointToOnScreenPoint(observation, pTarget);
					if (pSource.y >= pTarget.y) {
						((MarioComponent)observation).addDebugDrawing(new DebugLines(Color.GREEN, pSource,pTarget));
					}
					else {
						((MarioComponent)observation).addDebugDrawing(new DebugLines(Color.ORANGE, pSource,pTarget));
					}
				}		
			}
		}
	}

	public static void drawEndNodes(final Environment observation, Node[] endNodes) {
		ArrayList<Point> allEndPoints = new ArrayList<Point>();
		int x = 0;
		for (int i = 0; i < endNodes.length; i++) {
			if (endNodes[i] != null) {
				x = endNodes[i].x;
			}
		}
		for (int i = 0; i < endNodes.length; i++) {
			Point p = new Point(x, i);
			convertLevelPointToOnScreenPoint(observation, p);
			allEndPoints.add(p);
		}
		((MarioComponent) observation).addDebugDrawing(new DebugPoints(Color.BLUE, allEndPoints, 12));
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
			for (DirectedEdge directedEdge : toCheck.getEdges()) {
				Point pSource = new Point(directedEdge.source.x, directedEdge.source.y);
				convertLevelPointToOnScreenPoint(observation, pSource);

				Point pTarget = new Point(directedEdge.target.x, directedEdge.target.y);
				convertLevelPointToOnScreenPoint(observation, pTarget);
				if (pSource.y >= pTarget.y) {
					((MarioComponent) observation).addDebugDrawing(new DebugLines(Color.GREEN, pSource, pTarget));
				} else {
					((MarioComponent) observation).addDebugDrawing(new DebugLines(Color.ORANGE, pSource, pTarget));
				}
			}
		}
	}
	
	public static void drawAction(final Environment observation, final boolean[] actions) {
		ArrayList<Point> startsGreen = new ArrayList<Point>();
		ArrayList<Point> sizesGreen = new ArrayList<Point>();
		ArrayList<Point> startsRed = new ArrayList<Point>();
		ArrayList<Point> sizesRed = new ArrayList<Point>();
		final float marioXPos = Math.max(MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()), (LEVEL_WIDTH / 2) - 1);
		
		Point2D.Float[] keyPositions = new Point2D.Float[] {
				new Point2D.Float((marioXPos - 10), 14),
				new Point2D.Float((marioXPos -  9), 14),
				new Point2D.Float((marioXPos -  8), 14),
				new Point2D.Float((marioXPos -  9), 13)
		};
		int[] keys = new int[] {
				Mario.KEY_LEFT,
				Mario.KEY_DOWN,
				Mario.KEY_RIGHT,
				Mario.KEY_JUMP
		};
		Point size = new Point((int)(BLOCK_PIXEL_SIZE * 0.8) * Art.SIZE_MULTIPLIER, (int)(BLOCK_PIXEL_SIZE * 0.8) * Art.SIZE_MULTIPLIER);
		
		for (int i = 0; i < keys.length; i++) {
			convertLevelPointToOnScreenPoint(observation, keyPositions[i]);
			Point keyPosition = new Point((int)keyPositions[i].x, (int)keyPositions[i].y);
			
			if (actions[keys[i]]) {
				startsGreen.add(keyPosition);
				sizesGreen.add(size);
			}
			else {
				startsRed.add(keyPosition);
				sizesRed.add(size);
			}
			
			((MarioComponent) observation).addDebugDrawing(new DebugSquare(Color.GREEN, startsGreen, sizesGreen));
			((MarioComponent) observation).addDebugDrawing(new DebugSquare(Color.RED, startsRed, sizesRed));
		}
	}

	private static void convertLevelPointToOnScreenPoint(final Environment observation, final Point point) {
		Point2D.Float p = new Point2D.Float(point.x, point.y);
		convertLevelPointToOnScreenPoint(observation, p);
		point.x = (int)p.x;
		point.y = (int)p.y;
	}
	
	private static void convertLevelPointToOnScreenPoint(final Environment observation, final Point2D.Float point) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		point.x = (((point.x - (Math.max(marioXPos - (LEVEL_WIDTH / 2) + 1, 0)) + 1) * BLOCK_PIXEL_SIZE) - (BLOCK_PIXEL_SIZE / 2)) * Art.SIZE_MULTIPLIER;
		point.y = (((marioYPos * BLOCK_PIXEL_SIZE) + ((point.y - marioYPos) * BLOCK_PIXEL_SIZE)) - (BLOCK_PIXEL_SIZE / 2)) * Art.SIZE_MULTIPLIER;
	}

}
