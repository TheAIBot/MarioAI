package MarioAI.debugGraphics;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.simulators.EnemySimulator;
import MarioAI.graph.edges.AStarHelperEdge;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.engine.Art;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Static class which draws information about an agent onto the screen
 * @author Andreas Gramstrup
 *
 */
public class DebugDraw {

	public static final int LEVEL_HEIGHT = 15;
	public static final int LEVEL_WIDTH = 22;
	private static final int BLOCK_PIXEL_SIZE = 16;

	public static void resetGraphics(final Environment observation) {
		((MarioComponent) observation).resetDebugGraphics();
	}

	public static void drawPathEdgeTypes(final Environment observation, final ArrayList<DirectedEdge> path) {
		if (path != null) {
			final float marioXPos = Math.max(MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()), (LEVEL_WIDTH / 2) - 1);
			final Point2D.Float topStringPosition = new Point2D.Float((marioXPos + 7), 1f);
			final float distanceBetweenStrings = 0.4f;
			
			for (DirectedEdge directedEdge : path) {
				final Point2D.Float stringPos = new Point2D.Float(topStringPosition.x, topStringPosition.y);
				convertLevelPointToOnScreenPoint(observation, stringPos);
				final Point correctedPos = new Point((int)stringPos.x, (int)stringPos.y);
				
				final String typeName = (directedEdge instanceof RunningEdge) ? "Running" : "Jumping";
				addDebugDrawing(observation, new DebugString(typeName, correctedPos));
				
				topStringPosition.y += distanceBetweenStrings;
			}	
		}
	}

	public static void drawBlockBeneathMarioNeighbors(final Environment observation, final World graph) {
 		final int marioXPos = Math.min(MarioMethods.getMarioXPos(observation.getMarioFloatPos()), LEVEL_WIDTH / 2);
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		final Node[][] levelMatrix = graph.getLevelMatrix();

		int groundYLevel = marioYPos;
		while (groundYLevel >= 0 && groundYLevel < LEVEL_HEIGHT && levelMatrix[marioXPos][groundYLevel] == null) {
			groundYLevel++;
		}

		if (groundYLevel >= 0 && groundYLevel < LEVEL_HEIGHT) {
			final Node groundNode = levelMatrix[marioXPos][groundYLevel];
			if (groundNode != null) {
				ArrayList<Point> neighbors = new ArrayList<Point>();

				for (DirectedEdge neighborEdge : groundNode.getEdges()) {
					final Point neighborPoint = new Point(neighborEdge.target.x, neighborEdge.target.y);
					convertLevelPointToOnScreenPoint(observation, neighborPoint);
					neighbors.add(neighborPoint);
				}

				addDebugDrawing(observation, new DebugPoints(Color.BLACK, neighbors, 4));
			}
		}
	}

	public static void drawNodeEdgeTypes(final Environment observation, final Node[][] levelMatrix) {
		final ArrayList<Point> allrunningEdges = new ArrayList<Point>();
		final ArrayList<Point> allJumpingEdges = new ArrayList<Point>();
		final ArrayList<Point> allNodesWithAllEdges = new ArrayList<Point>();
		
		for (int x = 0; x < levelMatrix.length; x++) {
			for (int y = 0; y < levelMatrix[x].length; y++) {
				if (levelMatrix[x][y] != null) {
					final Node toCheck = levelMatrix[x][y];
					
					boolean containsRunningEdge = false;
					boolean containsJumpingEdge = false;
					for (DirectedEdge edge : toCheck.getEdges()) {
						if (edge instanceof RunningEdge) {
							containsRunningEdge = true;
						}
						else if (edge instanceof JumpingEdge) {
							containsJumpingEdge = true;
						}
						if (containsRunningEdge && containsJumpingEdge) {
							break;
						}
					}
					
					final Point p = new Point(toCheck.x, toCheck.y);
					convertLevelPointToOnScreenPoint(observation, p);
					
					if (containsRunningEdge) {
						allrunningEdges.add(p);
					}
					if (containsJumpingEdge) {
						allJumpingEdges.add(p);
					}
					
					if (toCheck.isAllEdgesMade()) {
						allNodesWithAllEdges.add(p);
					}
				}
			}
		}
		addDebugDrawing(observation, new DebugPoints(Color.YELLOW, allNodesWithAllEdges, 6));
		addDebugDrawing(observation, new DebugPoints(Color.BLACK, allrunningEdges, 4));
		addDebugDrawing(observation, new DebugPoints(Color.WHITE, allJumpingEdges, 2));
	}

	public static void drawEdges(final Environment observation, final Node[][] levelMatrix) {		
		for (int x = 0; x < levelMatrix.length; x++) {
			for (int y = 0; y < levelMatrix[x].length; y++) {
				if (levelMatrix[x][y] != null) {
					final Node toCheck = levelMatrix[x][y];
					for (DirectedEdge edge : toCheck.getEdges()) {
						if (!(edge instanceof AStarHelperEdge)) {
							final Point pSource = new Point(edge.source.x, edge.source.y);
							convertLevelPointToOnScreenPoint(observation, pSource);
							
							final Point pTarget = new Point(edge.target.x,edge.target.y);
							convertLevelPointToOnScreenPoint(observation, pTarget);
							if (pSource.y >= pTarget.y) {
								addDebugDrawing(observation, new DebugLines(Color.GREEN, pSource, pTarget, 1));
							}
							else {
								addDebugDrawing(observation, new DebugLines(Color.ORANGE, pSource, pTarget, 1));
							}
						}
					}
				}
			}
		}
	}

	public static void drawGoalNodes(final Environment observation, final Node[] endNodes) {
		final ArrayList<Point> allEndPoints = new ArrayList<Point>();
		int x = 0;
		for (int i = 0; i < endNodes.length; i++) {
			if (endNodes[i] != null) {
				x = endNodes[i].x;
				break;
			}
		}
		for (int i = 0; i < endNodes.length; i++) {
			final Point p = new Point(x, i);
			convertLevelPointToOnScreenPoint(observation, p);
			allEndPoints.add(p);
		}
		addDebugDrawing(observation, new DebugPoints(Color.BLUE, allEndPoints, 4));
	}

	public static void drawMarioReachableNodes(final Environment observation, final World graph) {
		final Node mario = graph.getMarioNode(observation);
		final List<DirectedEdge> edges = mario.getEdges();

		for (DirectedEdge edge : edges) {
			final Node toCheck = edge.target;
			for (DirectedEdge directedEdge : toCheck.getEdges()) {
				final Point pSource = new Point(directedEdge.source.x, directedEdge.source.y);
				convertLevelPointToOnScreenPoint(observation, pSource);

				final Point pTarget = new Point(directedEdge.target.x, directedEdge.target.y);
				convertLevelPointToOnScreenPoint(observation, pTarget);
				if (pSource.y >= pTarget.y) {
					addDebugDrawing(observation, new DebugLines(Color.GREEN, pSource, pTarget, 1));
				} else {
					addDebugDrawing(observation, new DebugLines(Color.ORANGE, pSource, pTarget, 1));
				}
			}
		}
	}
	
	public static void drawAction(final Environment observation, final boolean[] actions) {
		final float marioXPos = Math.max(MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()), (LEVEL_WIDTH / 2) - 1);
		
		final Point2D.Float[] keyPositions = new Point2D.Float[] {
				new Point2D.Float((marioXPos - 10), 14),
				new Point2D.Float((marioXPos -  9), 14),
				new Point2D.Float((marioXPos -  8), 14),
				new Point2D.Float((marioXPos -  9), 13),
				new Point2D.Float((marioXPos -  8), 13)
		};
		final int[] keys = new int[] {
				Mario.KEY_LEFT,
				Mario.KEY_DOWN,
				Mario.KEY_RIGHT,
				Mario.KEY_JUMP,
				Mario.KEY_SPEED
		};
		
		final Point size = new Point((int)((BLOCK_PIXEL_SIZE * 0.8) * Art.SIZE_MULTIPLIER), (int)((BLOCK_PIXEL_SIZE * 0.8) * Art.SIZE_MULTIPLIER));
		
		for (int i = 0; i < keys.length; i++) {
			convertLevelPointToOnScreenPoint(observation, keyPositions[i]);
			final Point keyPosition = new Point((int)keyPositions[i].x, (int)keyPositions[i].y);
			
			if (actions[keys[i]]) {
				addDebugDrawing(observation, new DebugSquare(Color.GREEN, keyPosition, size));
			}
			else {
				addDebugDrawing(observation, new DebugSquare(Color.RED, keyPosition, size));
			}
		}
	}
	
	public static void drawEnemies(final Environment observation, EnemyPredictor enemyPredictor) {
		for (EnemySimulator enemy : enemyPredictor.getEnemies()) {
			for (int i = 0; i < 1; i++) {
				final Point2D.Float enemyPos = enemy.getPositionAtTime(i);
				final Point2D.Float startPos = new Point2D.Float(((enemyPos.x - enemy.getWidth()) / BLOCK_PIXEL_SIZE), 
																 ((enemyPos.y - enemy.getHeight()) / BLOCK_PIXEL_SIZE) + 0.5f);
				final Point2D.Float size = new Point2D.Float((float)enemy.getWidth() * 2 * Art.SIZE_MULTIPLIER, 
															 (float)enemy.getHeight() * Art.SIZE_MULTIPLIER);
				
				
				convertLevelPointToOnScreenPoint(observation, startPos);
				//convertLevelPointToOnScreenPoint(observation, size);
				
				final Point startPosAsInt = new Point((int)startPos.x, (int)startPos.y);
				final Point sizeAsInt = new Point((int)size.x, (int)size.y);
				
				addDebugDrawing(observation, new DebugSquare(new Color(255, 255, 255, 100), startPosAsInt, sizeAsInt));	
			}
		}
	}
	
	public static void drawPathMovement(final Environment observation, final List<DirectedEdge> path, final boolean pathShouldBeUpdated) {
		if (path != null) {
			final ArrayList<Point> positions = new ArrayList<Point>(); 
			for (DirectedEdge edge : path) {
                            for (int i = 0; i < edge.getMoveInfo().getMoveTime(); i++) {
                                final float posX = edge.getMoveInfo().getXPositions()[i];
                                final float posY = edge.getMoveInfo().getYPositions()[i];
                                Point2D.Float correctPos = new Point2D.Float((float)edge.source.x + posX, edge.source.y - posY);
                                convertLevelPointToOnScreenPoint(observation, correctPos);

                                positions.add(new Point((int)correctPos.x, (int)correctPos.y));
                            }
			}
			if (pathShouldBeUpdated) {
				addDebugDrawing(observation, new DebugLines(Color.BLUE, positions));
			}
			else {
				addDebugDrawing(observation, new DebugLines(Color.RED, positions));
			}
		}
	}
	
	public static void drawMarioNode(final Environment observation, final Node marioNode) {
		final Point nodePos = new Point(marioNode.x - 1, marioNode.y - 1);
		convertLevelPointToOnScreenPoint(observation, nodePos);
		final Point size = new Point(BLOCK_PIXEL_SIZE * Art.SIZE_MULTIPLIER, BLOCK_PIXEL_SIZE * Art.SIZE_MULTIPLIER);
		addDebugDrawing(observation, new DebugSquare(Color.GREEN, nodePos, size));
	}

	private static void convertLevelPointToOnScreenPoint(final Environment observation, final Point point) {
		final Point2D.Float p = new Point2D.Float(point.x, point.y);
		convertLevelPointToOnScreenPoint(observation, p);
		point.x = (int)p.x;
		point.y = (int)p.y;
	}
	
	private static void convertLevelPointToOnScreenPoint(final Environment observation, final Point2D.Float point) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		//this math is magic that i forgot how worked before i wrote a comment about it
		//sorry to all future readers
		point.x = (((point.x - (Math.max(marioXPos - (LEVEL_WIDTH / 2) + 1, 0)) + 1) * BLOCK_PIXEL_SIZE) - (BLOCK_PIXEL_SIZE / 2)) * Art.SIZE_MULTIPLIER;
		point.y = (((marioYPos * BLOCK_PIXEL_SIZE) + ((point.y - marioYPos) * BLOCK_PIXEL_SIZE)) - (BLOCK_PIXEL_SIZE / 2)) * Art.SIZE_MULTIPLIER;
	}

	private static void addDebugDrawing(final Environment observation, final DebugDrawing drawing){
		((MarioComponent) observation).addDebugDrawing(drawing);
	}
}
