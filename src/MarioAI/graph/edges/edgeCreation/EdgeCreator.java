package MarioAI.graph.edges.edgeCreation;

import java.util.*;

import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.FallEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;

public class EdgeCreator {
	private static final float MAX_JUMP_HEIGHT = 4;
	private static final float MAX_JUMP_RANGE = 8;
	public static final int GRID_HEIGHT = 15;
	public static final int GRID_WIDTH = 22;
	public static final float MARIO_HEIGHT = (float) 1.8;
	public static final boolean ALLOW_RUNNING = true;
	public static final boolean ALLOW_JUMPING = true;
	public static final boolean ALLOW_SPEED_KEY = false;
	private Node[][] observationGraph;

	public void setMovementEdges(World world, Node marioNode) {
		observationGraph = world.getLevelMatrix();
		marioNode.deleteAllEdges();

		// First connects all the edges for Mario:
		if (isOnLevelMatrix(GRID_WIDTH / 2, marioNode.y) && canMarioStandThere(GRID_WIDTH / 2, marioNode.y)) {
			connectNode(marioNode, GRID_WIDTH / 2, marioNode);
		}

		// Then for the rest of the level matrix:
		for (int i = 0; i < observationGraph.length; i++) {
			for (int j = 0; j < observationGraph[i].length; j++) {

				if (isOnLevelMatrix(i, j) && canMarioStandThere(i, j) && observationGraph[i][j] != null
						&& !observationGraph[i][j].isAllEdgesMade()) {
					connectNode(observationGraph[i][j], i, marioNode);
				}
			}
		}
		
		//Then for the unfinished enemy collision nodes:
		//This code needs to be placed last, see connectLoneNode.
		for (int i = world.unfinishedEnemyCollisionNodes.size() - 1; i >= 0; i--) {
			//Going backwards, depending on the implementation of the list, should be faster:
			Node currentNode = world.unfinishedEnemyCollisionNodes.get(i);
			connectLoneNode(currentNode, world);
			if (currentNode.isAllEdgesMade()) {
				world.unfinishedEnemyCollisionNodes.remove(i);
			}
		}
	}

	private void connectNode(Node node, int coloumn, Node marioNode) {
		// Find the reachable nodes:
		List<DirectedEdge> edges = getConnectingEdges(node, coloumn);
		for (DirectedEdge connectingEdge : edges) {
			if   (connectingEdge.target != null && // Must go to an actual block, not just air.
					isOnLevelMatrix(connectingEdge.target, marioNode) && // It must be on the current level matrix.
					canMarioStandThere(connectingEdge.target, marioNode) && // The edge must not go into for example a wall.
					connectingEdge.source != connectingEdge.target) { // No movement to the same node. Notice that no equals method are needed.
				// TODO (*) Maybe allow above.
				node.addEdge(connectingEdge);
			}
		}
	}
	/**Used for creating connection from a node, not placed on the level matrix.
	 * 
	 */
	public void connectLoneNode(Node loneNode, World world) {
		Node[][] loneObservationGraph = world.getLevelMatrixAt(loneNode.x);
		observationGraph = loneObservationGraph;
		connectNode(loneNode, GRID_WIDTH/2, loneNode); //Simply takes the lone node as the mario node.
	}

	private boolean isOnLevelMatrix(Node position, Node marioNode) {
		return isOnLevelMatrix(getColoumnRelativeToMario(position, marioNode), position.y);
	}

	private boolean isOnLevelMatrix(int coloumn, int row) {
		return (0 <= coloumn && coloumn < GRID_WIDTH && 
				  0 <= row 		&& row 	  < GRID_HEIGHT);
	}

	private int getColoumnRelativeToMario(Node node, Node marioNode) {
		// Assumes that node!=null.
		return (node.x - marioNode.x) + (GRID_WIDTH / 2);
	}

	private List<DirectedEdge> getConnectingEdges(Node startingNode, int nodeColoumn) {
		ArrayList<DirectedEdge> listOfEdges = new ArrayList<DirectedEdge>();
		boolean foundAllEdges = true;
		// Two different ways to find the reachable nodes from a given
		// position:
		if (ALLOW_RUNNING) {
			foundAllEdges = getRunningReachableEdges(startingNode, nodeColoumn, listOfEdges)	&& foundAllEdges;
		}
		if (ALLOW_JUMPING) {
			foundAllEdges = getPolynomialReachingEdges(startingNode, nodeColoumn, listOfEdges) && foundAllEdges;
			//foundAllEdges = getJumpStraightUpEdges(startingNode, nodeColoumn, listOfEdges) && foundAllEdges;
			//foundAllEdges = getFallingDownEdges(startingNode, nodeColoumn, JumpDirection.RIGHT_DOWNWARDS, listOfEdges) && foundAllEdges;
			//foundAllEdges = getFallingDownEdges(startingNode, nodeColoumn, JumpDirection.LEFT_DOWNWARDS,	listOfEdges) && foundAllEdges;
		}

		if (foundAllEdges)
			startingNode.setIsAllEdgesMade(true);

		return listOfEdges;
	}

	private boolean getJumpStraightUpEdges(Node startingNode, int nodeColoumn,
			ArrayList<DirectedEdge> listOfEdges) {
		// He will of course not collide with anything at his starting
		// position.
		Node currentLandingPosition = startingNode;
		for (int jumpHeight = (int) 1; jumpHeight <= MAX_JUMP_HEIGHT; jumpHeight++) {
			int currentYPosition = startingNode.y - jumpHeight;
			// All jumps will be to the greatest height where Mario
			// can stand:
			if (canMarioStandThere(nodeColoumn, currentYPosition)) {
				currentLandingPosition = observationGraph[nodeColoumn][currentYPosition];
			}
			// He can only hit something upwards, with his top
			// corners.
			// The opposite corner here actually corresponds to his
			// top part.
			if (isHittingWallOrGroundUpwards(nodeColoumn,
					(int) Math.ceil(currentYPosition - MARIO_HEIGHT))) {
				// All jumps of a greater height will end there:
				// This corresponds to hitting the ceiling,
				// which will not currently be allowed.
				break;
			} else {
				//Else will Mario be able to jump up to this height, to the current target:
				listOfEdges.add(new JumpingEdge(startingNode, currentLandingPosition, startingNode.y + jumpHeight, false));
				listOfEdges.add(new JumpingEdge(startingNode, currentLandingPosition, startingNode.y + jumpHeight, true));
			}
		}
		// TODO change so it depends on the situation.
		return true;
	}

	public boolean getFallingDownEdges(Node startingNode, int nodeColoumn, JumpDirection direction,
			ArrayList<DirectedEdge> listOfEdges) {
		// TODO two cases, left and right.
		int initialXPosition = nodeColoumn + direction.getHorizontalDirectionAsInt();
		// If the column to the left/right haven't been seen yet, then
		// no edges can be found,
		// and therefore false is returned.
		if (isOnLevelMatrix(initialXPosition, startingNode.y)
				&& isOnLevelMatrix(initialXPosition, startingNode.y - 1)) {

			// It is required that the two blocks to the right/left,
			// are blocks that mario can pass trough,
			// and the block below also can be passed trough:
			if (isSolid(observationGraph[initialXPosition][startingNode.y])
					|| isSolid(observationGraph[initialXPosition][startingNode.y - 1])
					|| !isAir(initialXPosition, startingNode.y + 1)) {
				// Can't fall down, so return = true, and no
				// edges are added.
				return true;
			}

		} else
			return false;
		// First falling straight down:
		for (int height = startingNode.y + 1; height < GRID_HEIGHT; height++) {
			if (isHittingWallOrGroundDownwards(initialXPosition, height)) {
				listOfEdges.add(new FallEdge(startingNode, observationGraph[initialXPosition][height], false));
				listOfEdges.add(new FallEdge(startingNode, observationGraph[initialXPosition][height], true));
			}
		}

		boolean foundAllEdges = true;
		JumpingEdge polynomial = new JumpingEdge(null, null);
		List<DirectedEdge> jumpDownEdges = new ArrayList<DirectedEdge>();
		// Does not include falling straight down.
		for (int fallRange = 1; fallRange <= MAX_JUMP_RANGE / 2; fallRange++) {
			// it should start from initialXPosition, as Mario first
			// needs to go of the ledge.
			int currentFallRange = fallRange * direction.getHorizontalDirectionAsInt();
			polynomial.setToFallPolynomial(startingNode, initialXPosition, currentFallRange);
			// The opposite vertical direction is used, as it is
			// inverted in the method.
			// Plus direction.getHorizontalDirectionAsInt(), as it
			// moved one back later.
			foundAllEdges = jumpAlongPolynomial(startingNode,
					initialXPosition + direction.getHorizontalDirectionAsInt(), polynomial,
					direction.getOppositeVerticalDirection(), jumpDownEdges) && foundAllEdges;
		}
		// The edges added by the method above, is polynomials.
		// They need to be converted to fallDownEdges:
		for (DirectedEdge edge : jumpDownEdges) {
			listOfEdges.add(new FallEdge(edge.source, edge.target, false));
			listOfEdges.add(new FallEdge(edge.source, edge.target, true));
		}	
		
		return foundAllEdges;
	}

	public boolean getRunningReachableEdges(Node startingNode, int nodeColoumn, List<DirectedEdge> listOfEdges) {
		boolean foundAllEdges = true;

		//Run to the right:
		if (nodeColoumn + 1 < GRID_WIDTH) { //Not at the rightmost block in the view.
			listOfEdges.add(new RunningEdge(startingNode, observationGraph[nodeColoumn + 1][startingNode.y], false));
			if (ALLOW_SPEED_KEY) {
				listOfEdges.add(new RunningEdge(startingNode, observationGraph[nodeColoumn + 1][startingNode.y], true));				
			}
		} else foundAllEdges = false;
		
		//Run to the left:
		if (nodeColoumn > 0) { //Not at the leftmost block in the view.
			listOfEdges.add(new RunningEdge(startingNode, observationGraph[nodeColoumn -1][startingNode.y], false));
			if (ALLOW_SPEED_KEY) {
				listOfEdges.add(new RunningEdge(startingNode, observationGraph[nodeColoumn -1][startingNode.y], true));				
			}
		}	else foundAllEdges = false;

		return foundAllEdges;
	}

	/***
	 * Finds the possible places that mario can jump to, from the given
	 * position, and adds the to the given list of nodes. This is done by
	 * simulating the jump as a polynomial, and finding the place it
	 * collides with something.
	 * 
	 * @return
	 */
	public boolean getPolynomialReachingEdges(Node startingNode, int nodeColoumn, List<DirectedEdge> listOfEdges) {
		JumpingEdge polynomial = new JumpingEdge(null, null); // The
									// jump
									// polynomial.
		boolean foundAllEdges = true;
		for (int jumpHeight = (int) 1; jumpHeight <= MAX_JUMP_HEIGHT; jumpHeight++) {
			// TODO ensure result doesn't contain null
			for (int jumpRange = (int) 1; jumpRange <= MAX_JUMP_RANGE; jumpRange++) { // TODO
													// test
													// only
													// jumprange
													// =
													// 6,
													// no
													// running.
				polynomial.setToJumpPolynomial(startingNode, nodeColoumn, jumpRange, jumpHeight);
				foundAllEdges = jumpAlongPolynomial(startingNode, nodeColoumn, polynomial,
						JumpDirection.RIGHT_UPWARDS, listOfEdges) && foundAllEdges; // TODO
														// ERROR
														// if
														// removed
														// on
														// shortdeadend

				polynomial.setToJumpPolynomial(startingNode, nodeColoumn, -jumpRange, jumpHeight);
				foundAllEdges = jumpAlongPolynomial(startingNode, nodeColoumn, polynomial,
						JumpDirection.LEFT_UPWARDS, listOfEdges) && foundAllEdges;
			}
		}
		return foundAllEdges;
	}

	public boolean jumpAlongPolynomial(Node startingNode, int nodeColoumn, JumpingEdge polynomial,
			JumpDirection direction, List<DirectedEdge> listOfEdges) {
		// Starts of from Mario's initial position:
		int currentXPosition = nodeColoumn;
		// If there is a wall collision, an offset between the height
		// function (function of x)
		// and the actual height, might build up. This is taken care of
		// with this variable.
		int xPositionOffsetForJump = 0;
		float formerLowerYPosition = startingNode.y;
		Collision collisionDetection = Collision.HIT_NOTHING;
		// Gives the current direction of the jump:
		JumpDirection currentJumpDirection = direction;

		// Switches modes from ascending to descending, when the top
		// point has been reached.
		boolean hasAlreadyPassedTopPoint = false;
		boolean isPastTopPointColumn = polynomial.isPastTopPoint(direction,  currentXPosition + xPositionOffsetForJump);
		//TODO go stepwise through it and ensure that it works.

		while (isWithinView(currentXPosition + xPositionOffsetForJump)) {
			currentXPosition = currentXPosition + direction.getHorizontalDirectionAsInt();
			float currentYPosition;

			if (isPastTopPointColumn && !hasAlreadyPassedTopPoint) {
				if ((polynomial.getTopPointX() < currentXPosition + xPositionOffsetForJump && 
					  !direction.isLeftType())
					  && collisionDetection != Collision.HIT_WALL) { //rightwards!
					currentXPosition--; //The toppoint was in the current block (and not ending there).
					//Therefore the downward going part of that block needs to be checked.
					//See more detailed explanation in the report.
					//It must not just have had a collision with a wall, as the x position has then already been decremented.
				} else if ((polynomial.getTopPointX() > currentXPosition + xPositionOffsetForJump && 
						      direction.isLeftType()) &&
								collisionDetection != Collision.HIT_WALL) { //Leftwards
					currentXPosition++;
				}
				currentJumpDirection = direction.getOppositeVerticalDirection();
				hasAlreadyPassedTopPoint = true;
			}

			if (!isPastTopPointColumn && polynomial.isPastTopPoint(direction,
					currentXPosition + xPositionOffsetForJump)) { // TODO
											// fix
											// here,
											// probably
											// a
											// bug.
				currentYPosition = Math.max(polynomial.getTopPointY(),
						polynomial.f(currentXPosition + xPositionOffsetForJump)); // TODO
														// no
														// max
														// needed?
				currentYPosition = roundWithingMargin(currentYPosition, (float) 0.02);
				// TODO error in rounding, does not always give
				// decimal number.
			} else {
				currentYPosition = polynomial.f(currentXPosition + xPositionOffsetForJump);
				// Because of the cursed limited precision:
				currentYPosition = roundWithingMargin(currentYPosition, (float) 0.02);
			}
			// The bound is the bounded value for the next y
			// position -> rounded down.
			// This converts the next y value from (high value =
			// higher up on the level) to (high value = lower on the
			// level)
			// TODO error in bound, because of rounding errors.
			// Check for similar things other places.
			final float bound = getBounds(startingNode, currentYPosition);

			if (!isPastTopPointColumn) {
				collisionDetection = ascendingPolynomial(formerLowerYPosition, bound, currentXPosition,
						collisionDetection, polynomial, currentJumpDirection, startingNode,
						listOfEdges);
			} else {
				collisionDetection = descendingPolynomial(formerLowerYPosition, bound, currentXPosition,
						collisionDetection, polynomial, currentJumpDirection, startingNode,
						listOfEdges);
			}

			if (collisionDetection == Collision.HIT_WALL) {
				currentXPosition += direction.getOppositeDirection().getHorizontalDirectionAsInt();
				xPositionOffsetForJump = xPositionOffsetForJump + direction.getHorizontalDirectionAsInt();
			} //TODO change back to both below returns true.
			else if ( collisionDetection == Collision.HIT_GROUND) {
				return true;
			} else if (collisionDetection == Collision.HIT_CEILING) {
				return false;
			}

			isPastTopPointColumn = polynomial.isPastTopPoint(direction,
					currentXPosition + xPositionOffsetForJump);
			formerLowerYPosition = bound;
		}
		return false;
	}

	public Collision ascendingNoncollidingFunction(int formerLowerYPosition, int bound, int currentXPosition,
			Collision collisionDetection, JumpDirection direction) {
		return ascendingFunction(formerLowerYPosition, bound, currentXPosition, collisionDetection, null,
				direction, null, null, true, false);
	}

	public Collision ascendingPolynomial(float formerLowerYPosition, float bound, int currentXPosition,
			Collision collisionDetection, JumpingEdge polynomial, JumpDirection direction,
			Node startingPosition, List<DirectedEdge> listOfEdges) {
		return ascendingFunction(formerLowerYPosition, bound, currentXPosition, collisionDetection, polynomial,
				direction, startingPosition, listOfEdges, false, true);
	}

	public Collision ascendingFunction(float formerLowerYPosition, float bound, int currentXPosition,
			Collision collisionDetection, JumpingEdge polynomial, JumpDirection direction,
			Node startingPosition, List<DirectedEdge> listOfEdges, boolean stopAtAnyCollision,
			boolean addEdges) {
		boolean isHittingWall = false;
		// TODO make the internal part into a function. Replicate for
		// descending function.
		// The Math.max(bound, 0) - 0.99 plus the internal if statement,
		// ensures the height at the end of the jump is included.
		// 0.99=margin of error.
		for (float y = formerLowerYPosition; y >= Math.max(bound, 0) - 0.99; y--) {
			// 0.05 is the margin of error.
			// This in essence sets the height to the end position
			// at the end of the column, when this is reached.
			if (y < Math.max(bound, 0)) {
				y = Math.max(bound, 0);
			}
			final Collision lowerFacingMarioCorner   = lowerFacingCornerCollision  	(y, currentXPosition, direction, collisionDetection);
			final Collision upperFacingMarioCorner   = upperFacingCornerCollision  	(y, currentXPosition, direction, isHittingWall, formerLowerYPosition);	
			final Collision upperOppositeMarioCorner = upperOppositeCornerCollision	(y, currentXPosition, direction);	
			final Collision middleFacingMarioCorner  = middleFacingCornerCollision	(y, currentXPosition, direction);
			//As it is ascending to the right, only worry about the two corners to the right/left,
			//and maybe the middle right/left part, if Mario has height 2.
			if (upperOppositeMarioCorner 	== Collision.HIT_CEILING  || 
				 upperFacingMarioCorner 	== Collision.HIT_CEILING){
				collisionDetection = Collision.HIT_CEILING;
				break;
			} else if (upperFacingMarioCorner == Collision.HIT_NOTHING
					&& lowerFacingMarioCorner == Collision.HIT_GROUND) {
				collisionDetection = Collision.HIT_GROUND;
				if (addEdges) {
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[currentXPosition][(int) y], polynomial, false));
					if (ALLOW_SPEED_KEY) {
						listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[currentXPosition][(int) y], polynomial, true));						
					}
				}
				break;
			} else if (upperFacingMarioCorner == Collision.HIT_WALL
					|| lowerFacingMarioCorner == Collision.HIT_WALL) {
				collisionDetection = Collision.HIT_WALL;
				isHittingWall = true;
				if (stopAtAnyCollision) {
					break;
				}
				// No break.
			}
			// Extra check if Mario has height 2. Can only be
			// HIT_WALL,
			// and is always HIT_NOTHING if Mario has height less
			// than 2.
			// (*) TODO test this works.
			else if (middleFacingMarioCorner == Collision.HIT_WALL) {
				collisionDetection = Collision.HIT_WALL;
				isHittingWall = true;
				if (stopAtAnyCollision) {
					break;
				}
			}
		}
		return collisionDetection;
	}

	public Collision descendingPolynomial(float formerLowerYPosition, float bound, int currentXPosition,
			Collision collisionDetection, JumpingEdge polynomial, JumpDirection direction,
			Node startingPosition, List<DirectedEdge> listOfEdges) {
		boolean isHittingWall = false;
		// TODO make the internal part into a function. Replicate for
		// ascending function.
		// The Math.min(bound, GRID_HEIGHT - 1) + 0.99 plus the internal
		// if statement, ensures the height at the end of the jump is
		// included.
		// 0.99 = margin of error.

		for (float y = formerLowerYPosition; y <= Math.min(bound, GRID_HEIGHT - 1) + 0.99; y++) {
			// 0.05 is the margin of error.
			// This in essence sets the height to the end position
			// at the end of the column, when this is reached.
			if (y > Math.min(bound, GRID_HEIGHT - 1)) {
				y = Math.max(bound, 0);
			}
			
			final Collision lowerFacingMarioCorner 	= lowerFacingCornerCollision  	(y, currentXPosition, direction, collisionDetection);
			final Collision upperFacingMarioCorner 	= upperFacingCornerCollision  	(y, currentXPosition, direction, isHittingWall, formerLowerYPosition);	
			final Collision lowerOppositeMarioCorner 	= lowerOppositeCornerCollision	(y, currentXPosition, direction);	
			if (lowerOppositeMarioCorner == Collision.HIT_WALL) {
				throw new Error("Logic error");
			}
			
			//As it is descending to the right, only worry about the two corners to the right, and the left lower one.
			//TODO change this so that walls are prioritized correctly
			//TODO discuss why it is HIT_NOTHING or HIT_WALL for upperFacingMarioCorner.
			if ((upperFacingMarioCorner == Collision.HIT_NOTHING ||
				  upperFacingMarioCorner == Collision.HIT_WALL ) 
				 && 
			    (lowerFacingMarioCorner == Collision.HIT_GROUND || 
			     lowerOppositeMarioCorner == Collision.HIT_GROUND)) {
				collisionDetection = Collision.HIT_GROUND;
				if(lowerFacingMarioCorner == Collision.HIT_GROUND)	{
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[currentXPosition][(int) y],polynomial, false));
					if (ALLOW_SPEED_KEY) {
						listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[currentXPosition][(int) y],polynomial, true));						
					}
				}										 
				else { //lands on the opposite corner.
					final int groundXPos = currentXPosition + direction.getOppositeDirection().getHorizontalDirectionAsInt();
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPos][(int) y], polynomial, false));
					if (ALLOW_SPEED_KEY) {
						listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPos][(int) y], polynomial, true));						
					}
				}
				break;
			} else if (upperFacingMarioCorner == Collision.HIT_WALL
					|| lowerFacingMarioCorner == Collision.HIT_WALL) {
				// It is purposefully made so that the hit wall
				// will never stop, until the ground is hit.

				collisionDetection = Collision.HIT_WALL;
				isHittingWall = true;
				// No break.
			}
		}
		return collisionDetection;
	}

	/**
	 * Rounds the number, if it is within a given margin of error, from an
	 * integer value (*) TODO Actually doesn't round any more, rewrite
	 * descrition The margin of error must be small.
	 * 
	 * @param number
	 * @param marginOfError
	 * @return
	 */
	private float roundWithingMargin(float number, float marginOfError) {
		// Should it be rounded up / increasing it by the margin of
		// error changes the floor value:
		if (Math.floor(number + marginOfError) > Math.floor(number)) {
			int numberRounded = Math.round(number + marginOfError);
			return numberRounded; // To get the correct floor value.
		} else if (Math.ceil(number - marginOfError) < Math.ceil(number)) { // Round
											// down/ceil
											// value.
			int numberRounded = Math.round(number - marginOfError);
			return numberRounded;
		} else
			return number; // else it is just normal.
	}

	private float getBounds(Node startingNode, float currentLowerYPosition) {
		return startingNode.y - (currentLowerYPosition - startingNode.y);
	}

	private static boolean isSolid(Node node) {
		// return node != null;
		return node != null && node.type != -11;// TODO(*) Fix
	}
	
	private boolean isHittingWallOrGroundUpwards(int xPosition, float yPosition) {
		//Being out of the level matrix does not constitute as hitting something
		
		//Floors the y position, as we are interested in the block Mario is currently in.
		//Needs yPosition-0.01, as else one will get an immediate collision with the ground, as mario starts on it.
		//Ie. takes it as though mario howers a little above the ground (which he actually does in the code.)
		return isOnLevelMatrix(xPosition, (int) (yPosition - 0.01)) && isSolid(observationGraph[xPosition][(int) (yPosition - 0.01)]);
	}

	private static boolean isJumpThroughNode(Node node) {
		return (node != null && node.type == -11);
	}
	
	private boolean isHittingWallOrGroundDownwards(int xPosition, float yPosition) {
		//Being out of the level matrix does not constitute as hitting something
		//Floors the y position, as we are interested in the block Mario is currently in.
		//Uses yPosition + 0.01, so when mario is close to the ground, it will be taken as a collision, without floating point errors.
		//Not strictly neccessary.
		if (isOnLevelMatrix(xPosition, (int) (yPosition + 0.01))) {
			final boolean isAtSolidNode = isSolid(observationGraph[xPosition][(int) (yPosition + 0.01)]);
			final boolean isAtJumpThroughNode = isJumpThroughNode(observationGraph[xPosition][(int) (yPosition + 0.01)]);
			return isAtSolidNode || isAtJumpThroughNode;
		} else {
			return false;
		}
	}

	private boolean isWithinView(int xPosition) {
		return xPosition < GRID_WIDTH && xPosition >= 0;
	}

	private boolean canMarioStandThere(Node node, Node marioNode) {
		boolean bool1 = node == null;
		boolean bool2 = node.y < 0;
		boolean bool3 = GRID_HEIGHT <= node.y;
		// if (node == null || node.y < 0 || GRID_HEIGHT <= node.y ) {
		if (bool1 || bool2 || bool3) { // Node can't stand on air, nor
						// can he stand on nothing ->
						// things that are not in the
						// array.
			return false;
		} else {
			int nodeXPosition = getColoumnRelativeToMario(node, marioNode);
			return isOnSolidGround(node.y, nodeXPosition)
					&& observationGraph[nodeXPosition][node.y - 1] == null;
		}
	}
	private boolean canMarioStandThere(int coloumn,int row) {
		//TODO changed 0 < row to 1 < row. Verify  that it makes sense
		//Reason for change: Prevents a crash where row = 1
		return 1 < row && row < GRID_HEIGHT &&
			   isOnSolidGround(row, coloumn) && 
			   !isSolid(observationGraph[coloumn][row - 1]) &&
			   !isSolid(observationGraph[coloumn][row - 2]);
	}

	private boolean canMarioStandThere(int coloumn, float yPosition) {
			return 0 < yPosition && yPosition < GRID_HEIGHT &&
				   isOnSolidGround((int) (yPosition), coloumn) && 
				   !isSolid(observationGraph[coloumn][(int) (yPosition) - 1]) &&
				   !isSolid(observationGraph[coloumn][(int) (yPosition) - 2]);
			//One could use Marios height, but this is techinacally not correct, 
			//if one only wants to use information from one corner, namely this
		}
	
	private boolean isOnSolidGround(int row, int coloumn) {
		return observationGraph[coloumn][row] != null; // TODO Fix in general.
	}

	private boolean isAir(int coloumn, int row) {
		//return node != null;
		return !(0 <= row 	  && row < GRID_HEIGHT     &&
				 0 <= coloumn && coloumn < GRID_WIDTH) ||				
				 observationGraph[coloumn][row] == null;// TODO(*) Fix
	}
				
	public Collision lowerFacingCornerCollision	(float y, int currentXPosition, JumpDirection direction, Collision collisionDetection) {
		if (direction.isLeftType()) { 
			//The x position denotes the placement of the right corner, 
			//so if one wants the left, one must be subtracted.
			currentXPosition += direction.getHorizontalDirectionAsInt();
		}
		if (direction.isUpwardsType()) { 
			//In the case one is going upwards, one should not check for any other type of collisions, 
			//than those originating from wall collisions.
			//TODO check and discuss if ceiling the result is correct.
			if (isHittingWallOrGroundUpwards(currentXPosition, y)) { //If it is hitting the ceiling, upperRight will notice.
				return Collision.HIT_WALL;
			} else {	
				if (//isAir(currentXPosition, (int) (y - MARIO_HEIGHT)) && //TODO Is this needed?
					 collisionDetection == Collision.HIT_WALL) {
					return Collision.HIT_GROUND;
				} else {
					return Collision.HIT_NOTHING;
				}
			}
		} else {
			//One must be going downwards, and there should be checked for collisions:
			//TODO check and discuss if ceiling the result is correct.
			if (isHittingWallOrGroundDownwards(currentXPosition, y)) {
				//If this corner is hitting something, then there are two possibilities: either it is the ground or a wall.
				//If it is the ground, then Mario can stand there, and if it is a wall, it is not possible.
				if (canMarioStandThere(currentXPosition, y + 0.01f)) { //+0.01, for the same reason it is done in isHittingWall...
					return Collision.HIT_GROUND;
				} else {
					return Collision.HIT_WALL;
				}
			}
			return Collision.HIT_NOTHING;
		}
	}

	public Collision upperFacingCornerCollision	(float y, int currentXPosition, JumpDirection direction, boolean isHittingWall, float formerLowerYPosition) {
		if (direction.isLeftType()) { 
			//The x position denotes the placement of the right corner, 
			//so if one wants the left, one must be subtracted.
			currentXPosition += direction.getHorizontalDirectionAsInt();
		}
		if (direction.isUpwardsType()) {
			//If mario is going upwards, one needs to check for ceiling collisions and the wall collisions,
			//and not whether he hits the ground. This will be registered by the lower part.
			//TODO check and discuss if ceiling the result is correct.
			if (isHittingWallOrGroundUpwards(currentXPosition, y - MARIO_HEIGHT)) {
				if (y == formerLowerYPosition) {
					return Collision.HIT_WALL;
				} else if (!isHittingWall){
					return Collision.HIT_CEILING;					
				} else {
					return Collision.HIT_WALL;
				}
			} else {
				return Collision.HIT_NOTHING;
			}

		} else {
			//It can only hit a wall, or nothing, so:
			//TODO check and discuss if ceiling the result is correct.
			if (isHittingWallOrGroundDownwards(currentXPosition, y - MARIO_HEIGHT)) {
				return Collision.HIT_WALL;
			} else {
				return Collision.HIT_NOTHING;
			}
		}
	}
	
	public Collision upperOppositeCornerCollision(float y, int currentXPosition, JumpDirection direction) {
		if (!direction.isLeftType()) {
			//If it is moving to the right, x=x-1 for the opposite corner. Else x=x.
			currentXPosition += direction.getOppositeDirection().getHorizontalDirectionAsInt();
		}
		
		//If Mario is going upwards, and since this is the opposite corner of the way he is going, 
		//one only needs to check for ceiling collisions.
		//TODO check and discuss if ceiling the result is correct.
		if (isHittingWallOrGroundUpwards(currentXPosition, y - MARIO_HEIGHT)) {
			return Collision.HIT_CEILING;
		} else {
			return Collision.HIT_NOTHING;
		}
	}
	
	public Collision lowerOppositeCornerCollision(float y, int currentXPosition, JumpDirection direction) {
		if (!direction.isLeftType()) {
			//If it is moving to the right, x=x-1 for the opposite corner. Else x=x.
			currentXPosition += direction.getOppositeDirection().getHorizontalDirectionAsInt();
		}
		
		//It should check if Mario hits the ground: it can't be the wall, as Mario is going in the way opposite to this corner.
		//TODO check and discuss if ceiling the result is correct.
		if (isHittingWallOrGroundDownwards(currentXPosition, y)) {
			if (canMarioStandThere(currentXPosition,  y + 0.01f)) { //+0.01, for the same reason it is done in isHittingWall
				return Collision.HIT_GROUND;
			} else {  
				throw new Error("Logic error on corner collision detection");
				//return Collision.HIT_WALL;
				//TODO i don't think this should be possible:
			}
		} else {
			return Collision.HIT_NOTHING;
		}
	}

	public Collision middleFacingCornerCollision	(float y, int currentXPosition, JumpDirection direction){
		if (direction.isLeftType()) { 
			//The x position denotes the placement of the right corner, 
			//so if one wants the left, one must be subtracted.
			currentXPosition += direction.getHorizontalDirectionAsInt();
		}
		//Mario must have height < 1, before this is relevant:
		if (MARIO_HEIGHT > 1) {
			//All blocks that can be passed downwards, can also be passed downwards, 
			//for this case:
			float marioMiddleYPosition = y - MARIO_HEIGHT/2;
			if (isOnLevelMatrix(currentXPosition, (int) marioMiddleYPosition) &&
			isSolid(observationGraph[currentXPosition][(int) marioMiddleYPosition])) { //If it is hitting the ceiling, upperRight will notice.
				return Collision.HIT_WALL;
			} else {
				return Collision.HIT_NOTHING;
			}
		} else return Collision.HIT_NOTHING; //Do not include it, which means, take it as having no collisions
		//Can only lead to a wall collision, and this happens, if and only if it is in a wall:		
	}

	public void clearAllEdges(Node[][] world) {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				if (world[i][j] != null) {
					world[i][j].deleteAllEdges();
				}
			}
		}
	}

}
