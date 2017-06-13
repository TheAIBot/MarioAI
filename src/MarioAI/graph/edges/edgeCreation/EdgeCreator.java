package MarioAI.graph.edges.edgeCreation;

import java.util.ArrayList;
import java.util.List;

import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.FallEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;

public class EdgeCreator {
	public static final float MAX_JUMP_HEIGHT = 4;
	public static final float MAX_JUMP_RANGE = 4;
	public static final float MAX_FALL_RANGE = 2;
	public static final int GRID_HEIGHT = 15;
	public static final int GRID_WIDTH = 22;
	//He actually only have a height of 1.5, but for overestimation purposes, 
	//it is taken as 1.8.
	public static final float MARIO_HEIGHT = (float) 1.8; 
	//The width of Mario is 1, but 1 will be used instead of a defined constant.
	public static final boolean ALLOW_RUNNING = true;
	public static final boolean ALLOW_JUMPING = true;
	public static final boolean ALLOW_SPEED_KEY = true;
	private Node[][] observationGraph;

	/** Given a world and a node representing Mario, connects all the edges of possible movements,
	 * for all the nodes in the worlds level matrix, and for the node representing Mario.
	 * @param world The given world.
	 * @param marioNode The node representing Mario.
	 */
	public void setMovementEdges(World world, Node marioNode) {
		observationGraph = world.getLevelMatrix();
		// First connects all the edges for Mario:
		setMovementEdgesForMario(world, marioNode, marioNode.x);
		
		// Then for the rest of the level matrix:
		for (int i = 0; i < observationGraph.length; i++) { 
			for (int j = 0; j < observationGraph[i].length; j++) {
				if (isOnLevelMatrix(i, j) && canMarioStandThere(i, j) && observationGraph[i][j] != null
						&& !observationGraph[i][j].isAllEdgesMade()) {
					connectNode(observationGraph[i][j], i, marioNode);
				}
			}
		}
	}
	
	/** Creates all the possible movement edges, for the given node representing Mario,
	 *  to the places in the level matrix in the world given.
	 * @param world The world object, to which the edges will be connected.
	 * @param marioNode The node for Mario.
	 * @param marioXPos Mario's actual position at the time the method is called. 
	 * 		 Sometimes necessary to use to correct for a possible rounding, 
	 * 		 which can happen to the x position of the Mario node.
	 * 
	 */
	public void setMovementEdgesForMario(World world, Node marioNode, float marioXPos) {
		if (isOnLevelMatrix(GRID_WIDTH / 2, marioNode.y) && canMarioStandThere(GRID_WIDTH / 2, marioNode.y)) {
			int extraXValue = (marioNode.x - ((int) marioXPos)); //Necessary if the update is not made with mario's x position,
			//corresponding to the middle of the level matrix
			connectNode(marioNode, GRID_WIDTH / 2 + extraXValue, marioNode);
		}
	}

	/**Connects all the movement edges for a given Node node.
	 * @param node The Node which movement edges needs to be set.
	 * @param coloumn The column in the level matrix for node.
	 * @param marioNode Node representing the position of Mario (plus more), at the instance of connection.
	 */
	private void connectNode(Node node, int coloumn, Node marioNode) {
		// Find the reachable nodes:
		List<DirectedEdge> edges = getConnectingEdges(node, coloumn);
		for (DirectedEdge connectingEdge : edges) {
			if   (connectingEdge.target != null && // Must go to an actual block, not just air.
					isOnLevelMatrix(connectingEdge.target, marioNode) && // It must be on the current level matrix.
					canMarioStandThere(connectingEdge.target, marioNode) && // The edge must not go into for example a wall.
					connectingEdge.source.hashCode() != connectingEdge.target.hashCode()) { // No movement to the same node. Notice that no equals method are needed.
				if (connectingEdge.source.x == connectingEdge.target.x && //Motion straight up is not currently allowed with this version of MarioController.
					 connectingEdge.source.y != connectingEdge.target.y) {
					continue;
				}
				node.addEdge(connectingEdge); //addEdge handles duplicates, if necessary.
			}  
		}
	}
	/** Returns whether the given Node position, is on the current level matrix.
	 *  Uses marioNode as a reference, taking it's x value to correspond to column GRID_WIDTH/2
	 * @param position The position to be checked.
	 * @param marioNode The reference (mario) Node.
	 * @return True if position is on the level matrix, else false.
	 */
	private boolean isOnLevelMatrix(Node position, Node marioNode) {
		return isOnLevelMatrix(getColoumnRelativeToMario(position, marioNode), position.y);
	}
	/** Returns whether or not the given position (in? the level matrix) is on the level matrix.
	 * @param coloumn The column of the level matrix.
	 * @param row The row of the level matrix.
	 * @return True if the position is on the level matrix, else false.
	 */
	private boolean isOnLevelMatrix(int coloumn, int row) {
		return (0 <= coloumn && coloumn < GRID_WIDTH && 
				  0 <= row 		&& row 	  < GRID_HEIGHT);
	}
	/** Gets the corresponding column of the Node node given.
	 * @param node The Node which's column is to be found.
	 * @param marioNode The reference Node. Assume that it is the mario Node,
	 *                  and as such, is placed at column (GRID_WIDTH / 2)
	 * @return The column the node is placed at.
	 */
	private int getColoumnRelativeToMario(Node node, Node marioNode) {
		// Assumes that node!=null.
		return (node.x - marioNode.x) + (GRID_WIDTH / 2);
	}
	/** Gets a list of all the possible movement edges/DirectedEdge's for the given Node startingNode.
	 * Needs its column placement in the level matrix, as this is used to find nodes to connect to.
	 * @param startingNode The Node to find edges for.
	 * @param nodeColoumn Its place in the level matrix.
	 * @return A list of all the possible movement edges/DirectedEdge's for startingNode.
	 */
	private List<DirectedEdge> getConnectingEdges(Node startingNode, int nodeColoumn) {
		ArrayList<DirectedEdge> listOfEdges = new ArrayList<DirectedEdge>();
		//When all the edges have been found, there is no need to look again.
		//This might take multiple rounds, if for example, 
		//some edges wants to connect to something out of the level.
		boolean foundAllEdges = true; 
		// Two generally different ways to find the reachable nodes from a given
		// position:
		
		//The booleans are simply for ease of testing.
		if (ALLOW_RUNNING) {
			foundAllEdges = getRunningReachableEdges(startingNode, nodeColoumn, listOfEdges)	&& foundAllEdges;
		}
		if (ALLOW_JUMPING) {
			foundAllEdges = getPolynomialReachingEdges(startingNode, nodeColoumn, listOfEdges) && foundAllEdges;
			foundAllEdges = getJumpStraightUpEdges(startingNode, nodeColoumn, listOfEdges) && foundAllEdges;
			foundAllEdges = getFallingDownEdges(startingNode, nodeColoumn, JumpDirection.RIGHT_DOWNWARDS, listOfEdges) && foundAllEdges;
			foundAllEdges = getFallingDownEdges(startingNode, nodeColoumn, JumpDirection.LEFT_DOWNWARDS,	listOfEdges) && foundAllEdges;
		}

		if (foundAllEdges) {
			startingNode.setIsAllEdgesMade(true);
		}

		return listOfEdges;
	}
	
	/** Does what it says on the tin, and it does it for a given Node, startingNode. 
	 *  getPolynomialReachingEdges cannot take those kind of edges into account, 
	 *  why it is necessary.
	 *  //TODO do i need to describe the algorithm?
	 * @param startingNode The starting Node, and the node which the algorithm will take basis in. 
	 * 						  All the edges found will have this Node as its source.
	 * @param nodeColoumn The column of the level matrix the Node startingNode is placed at.
	 * @param listOfEdges The list of edges to add the newly found edges to. When it is done, the edges are added  to the list.
	 * @return Whether it has found all jump straight up edges or not. Will always return true.
	 */
	public boolean getJumpStraightUpEdges(Node startingNode, int nodeColoumn, ArrayList<DirectedEdge> listOfEdges) {
		
		// He will of course not collide with anything at his starting position.
		Node currentLandingPosition = startingNode;
		for (int jumpHeight = (int) 1; jumpHeight <= MAX_JUMP_HEIGHT; jumpHeight++) {
			int currentYPosition = startingNode.y - jumpHeight;
			// All jumps will be to the greatest height where Mario
			// can stand, and which height is less than or equal to the jump height:
			if (canMarioStandThere(nodeColoumn, currentYPosition)) {
				currentLandingPosition = observationGraph[nodeColoumn][currentYPosition];
			}
			// He can only hit something upwards, with his top:
			if (isHittingWallOrGroundUpwards(nodeColoumn, currentYPosition - MARIO_HEIGHT)) {
				// All jumps of a greater height will end there:
				// This corresponds to hitting the ceiling, which will not currently be allowed.
				return true; //No more jumping straight up edges can be found.
			} else {
				//Else will Mario be able to jump up to this height, to the current target:
				listOfEdges.add(new JumpingEdge(startingNode, currentLandingPosition, startingNode.y + jumpHeight, false));
				if (ALLOW_SPEED_KEY) {
					listOfEdges.add(new JumpingEdge(startingNode, currentLandingPosition, startingNode.y + jumpHeight, true));
				}
				
			}
		}
		return true;
	}

	/** Gets all the movement edges taking basis in the Node startingNode, 
	 *  where the movement represents falling of an ledge, in a given direction.
	 *  They are added to the given list of directed edges.
	 *  
	 *  It does this, by two methods/two different cases.: 
	 *  Falling straight down a ledge, taking the given node as the top of a pillar (there is a check for it).
	 *  This guarantees the edges just to the side of the "pillar".
	 *  
	 *  The other is possible falls, starting from the rightmost or leftmost, depending on the direction,
	 *  is done by using polynomial falls, like those seen in the getPolynomialReachingEdges. 
	 *  This is done to get further/longer falls.
	 *  
	 * @param startingNode The starting Node, and the node which the algorithm will take basis in. 
	 * 						  All the edges found will have this Node as its source.
	 * @param nodeColoumn The column of the level matrix the Node startingNode is placed at.
	 * @param listOfEdges The list of edges to add the newly found edges to. When it is done, the edges are added  to the list.
	 * @param direction	An enum representing to which direction the edges should go/Mario should fall. Should always be the downwards type.
	 * @return Whether it has found all jump straight up edges or not. Will always return true.
	 */
	public boolean getFallingDownEdges(Node startingNode, int nodeColoumn, JumpDirection direction, ArrayList<DirectedEdge> listOfEdges) {
		// two cases, left and right.
		int initialXPosition = nodeColoumn + direction.getHorizontalDirectionAsInt();
		// If the column to the left/right haven't been seen yet, then
		// no edges can be found, and therefore false is returned.
		if (isOnLevelMatrix(initialXPosition, startingNode.y) && isOnLevelMatrix(initialXPosition, startingNode.y - 1)) {
			// It is required that the two blocks to the right/left,
			// are blocks that mario can pass trough,
			// and the block below also can be passed trough. 
			//Else it wont be possible for Mario to jump of a ledge:
			if (isSolid(observationGraph[initialXPosition][startingNode.y - 1])
			||  isSolid(observationGraph[initialXPosition][startingNode.y - 2])
			|| !isAir(initialXPosition, startingNode.y)) {
				// Can't fall down, so return = true, and no
				// edges are added.
				return true;
			}
		} else return false;
		// First falling straight down:
		for (int height = startingNode.y + 1; height < GRID_HEIGHT; height++) {
			if (isHittingWallOrGroundDownwards(initialXPosition, height)) {
				listOfEdges.add(new FallEdge(startingNode, observationGraph[initialXPosition][height], false));
				listOfEdges.add(new FallEdge(startingNode, observationGraph[initialXPosition][height], true));
				break;
			}
		}
		//Now with polynomials:
		boolean foundAllEdges = true;
		JumpingEdge polynomial = new JumpingEdge(null, null);
		List<DirectedEdge> jumpDownEdges = new ArrayList<DirectedEdge>();
		// Does not include falling straight down.
		for (int fallRange = 1; fallRange <= MAX_FALL_RANGE; fallRange++) {
			// It should start from initialXPosition, as Mario first needs to go of the ledge,
			// before he falls.			
			int currentFallRange = fallRange * direction.getHorizontalDirectionAsInt(); 
			polynomial.setToFallPolynomial(startingNode, initialXPosition, currentFallRange);
			
			// The opposite vertical direction is used, as it is inverted in the method.
			// It of course currently needs to be downwards.
			// Plus direction.getHorizontalDirectionAsInt(), as it moved one back later.
			int jumpStartPosition = initialXPosition + direction.getHorizontalDirectionAsInt();
			foundAllEdges = jumpAlongPolynomial(startingNode, jumpStartPosition, polynomial, direction.getOppositeVerticalDirection(), jumpDownEdges) && foundAllEdges;
		}
		// The edges added by the method above, is polynomials.
		// They need to be converted to FallEdge's:
		for (DirectedEdge edge : jumpDownEdges) {
			if (edge.useSuperSpeed) {
				listOfEdges.add(new FallEdge(edge.source, edge.target, true));				
			} else {
				listOfEdges.add(new FallEdge(edge.source, edge.target, false));
			}
		}	
		
		return foundAllEdges;
	}
	/**Finds all the RunningEdge's that can reached by running, from the given Node startNode, 
	 * at the given column nodeColumn in the level matrix. They are added to listOfEdges.
	 * @param startingNode The given Node that will work as the source for the running edges.
	 * @param nodeColoumn The column startingNode is placed at.
	 * @param listOfEdges The list of DirectedEdge's to add the newly found edges to.
	 * @return True if all the edges possible running edges for the Node.
	 */
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

	/**Finds the possible places that mario can jump to, from the given Node startingNode, 
	 * and adds the to the given list of nodes. 
	 * This is done by simulating the jump as a polynomial, and finding the place it collides with something.
	 * 
	 * @param startingNode The Node the jump takes basis in.
	 * @param nodeColoumn The column the Node is placed at.
	 * @param listOfEdges The list of Directed edges, to which the found edges will be added.
	 * @return
	 */
	public boolean getPolynomialReachingEdges(Node startingNode, int nodeColoumn, List<DirectedEdge> listOfEdges) {
		JumpingEdge polynomial = new JumpingEdge(null, null);
		boolean foundAllEdges = true;
		for (int jumpHeight = (int) 1; jumpHeight <= MAX_JUMP_HEIGHT; jumpHeight++) {
			for (int jumpRange = (int) 1; jumpRange <= MAX_JUMP_RANGE; jumpRange++) { 				
				polynomial.setToJumpPolynomial(startingNode, nodeColoumn, jumpRange, jumpHeight);
				// TODO ERROR if removed onshortdeadend
				foundAllEdges = jumpAlongPolynomial(startingNode, nodeColoumn, polynomial,	JumpDirection.RIGHT_UPWARDS, listOfEdges) && foundAllEdges; 
				
				polynomial.setToJumpPolynomial(startingNode, nodeColoumn, -jumpRange, jumpHeight);
				foundAllEdges = jumpAlongPolynomial(startingNode, nodeColoumn, polynomial, JumpDirection.LEFT_UPWARDS, listOfEdges) && foundAllEdges;
			}
		}
		return foundAllEdges;
	}

	public boolean jumpAlongPolynomial(Node startingNode, int nodeColoumn, JumpingEdge polynomial, JumpDirection direction, List<DirectedEdge> listOfEdges) {
		// Starts of from Mario's initial position:
		int currentXPosition = nodeColoumn;
		// If there is a wall collision, an offset between the height function (function of x)
		// and the actual height, might build up. 
		//This is taken care of with this variable:
		int xPositionOffsetForJump = 0;
		//To find which nodes to check for collisions with, on a given columÆ
		float formerLowerYPosition = startingNode.y; 
		Collision collisionDetection = Collision.HIT_NOTHING;
		// Gives the current direction of the jump:
		JumpDirection currentJumpDirection = direction;

		// Switches modes from ascending to descending, when the top point has been reached.
		boolean hasAlreadyPassedTopPoint = false;
		//Should of course only be done once:
		boolean isPastTopPointColumn = polynomial.isPastTopPoint(direction,  currentXPosition + xPositionOffsetForJump);
		
		while (isWithinView(currentXPosition + xPositionOffsetForJump, direction)) {
			currentXPosition = currentXPosition + direction.getHorizontalDirectionAsInt();
			float currentYPosition;

			if (isPastTopPointColumn && !hasAlreadyPassedTopPoint) {
				if ((polynomial.getTopPointX() < currentXPosition + xPositionOffsetForJump && !direction.isLeftType())
					     && 
					  collisionDetection != Collision.HIT_WALL) { //rightwards!
					currentXPosition--; //The toppoint was in the current block (and not ending there).
					//Therefore the downward going part of that block needs to be checked.
					//See more detailed explanation in the report.
					//It must not just have had a collision with a wall, as the x position has then already been decremented.
				} else if ((polynomial.getTopPointX() > currentXPosition + xPositionOffsetForJump && direction.isLeftType())
								&&
								collisionDetection != Collision.HIT_WALL) { //Leftwards
					currentXPosition++;
				}
				currentJumpDirection = direction.getOppositeVerticalDirection();
				hasAlreadyPassedTopPoint = true;
			}

			if (!isPastTopPointColumn && polynomial.isPastTopPoint(direction, currentXPosition + xPositionOffsetForJump)) { // TODO fix here, probably a bug.
				currentYPosition = Math.max(polynomial.getTopPointY(),
						polynomial.f(currentXPosition + xPositionOffsetForJump)); // TODO no max needed?
				currentYPosition = roundWithingMargin(currentYPosition, (float) 0.02);
			} else {
				currentYPosition = polynomial.f(currentXPosition + xPositionOffsetForJump);
				// Because of the cursed limited precision:
				currentYPosition = roundWithingMargin(currentYPosition, (float) 0.02);
			}
			// The bound is the bounded value for the next y position -> rounded down.
			// This converts the next y value from (high value = higher up on the level) to 
			//(high value = lower on the level)
			// TODO error in bound, because of rounding errors.
			final float bound = getBounds(startingNode, currentYPosition);

			if (!isPastTopPointColumn) {
				collisionDetection = ascendingPolynomial(formerLowerYPosition, bound, currentXPosition, collisionDetection, polynomial, currentJumpDirection, startingNode,listOfEdges);
			} else {
				collisionDetection = descendingPolynomial(formerLowerYPosition, bound, currentXPosition, collisionDetection, polynomial, currentJumpDirection, startingNode, listOfEdges);
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
			//If any of the upper corners hits the ceiling, the ceiling is hit:
			if (upperOppositeMarioCorner 	== Collision.HIT_CEILING  || 
				 upperFacingMarioCorner 	== Collision.HIT_CEILING){
				collisionDetection = Collision.HIT_CEILING;
				break;
			} 
			//For the case that Mario have just risen above a wall
			else if (ascendingRisenAboveWall(lowerFacingMarioCorner, upperFacingMarioCorner, middleFacingMarioCorner)) {
				collisionDetection = Collision.HIT_GROUND;
				int groundXPosition = currentXPosition;
				if (addEdges) {
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPosition][(int) Math.ceil(y)], polynomial, false));
					if (ALLOW_SPEED_KEY) {
						listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPosition][(int) Math.ceil(y)], polynomial, true));						
					}
				}
				break; //We call it quits here.
			} 
			//The former cases where more severe. If any of the corners hit a wall, 
			//without any ceiling collisions or something simililar, a wall is hit:
			else if (hitsWall(lowerFacingMarioCorner, middleFacingMarioCorner, upperFacingMarioCorner)) {
				collisionDetection = Collision.HIT_WALL;
				isHittingWall = true;
				if (stopAtAnyCollision) {
					break;
				}
				// No break.
			}
		}
		return collisionDetection;
	}

	private boolean ascendingRisenAboveWall(final Collision lowerFacingMarioCorner,
			final Collision upperFacingMarioCorner, final Collision middleFacingMarioCorner) {
		return upperFacingMarioCorner 	== Collision.HIT_NOTHING && 
				   middleFacingMarioCorner == Collision.HIT_NOTHING &&
				   lowerFacingMarioCorner  == Collision.HIT_GROUND;
	}

	public Collision descendingPolynomial(float formerLowerYPosition, float bound, int currentXPosition,
			Collision collisionDetection, JumpingEdge polynomial, JumpDirection direction,
			Node startingPosition, List<DirectedEdge> listOfEdges) {
		boolean isHittingWall = false;
		// TODO make the internal part into a function. Replicate for
		// ascending function.
		
		// The Math.min(bound, GRID_HEIGHT - 1) + 0.99 plus the internal if statement, 
		//ensures the height at the end of the jump is included. 0.99 = margin of error.
		for (float y = formerLowerYPosition; y <= Math.min(bound, GRID_HEIGHT - 1) + 0.99; y++) {
			
			// This in essence sets the height to the end position
			// at the end of the column, when this is reached.
			if (y > Math.min(bound, GRID_HEIGHT - 1)) {
				y = Math.max(bound, 0);
			}
			//One only worries about the following corners of Mario, for reasons described in the report:
			final Collision lowerFacingMarioCorner 	= lowerFacingCornerCollision  (y, currentXPosition, direction, collisionDetection);
			final Collision middleFacingMarioCorner	= middleFacingCornerCollision	(y, currentXPosition, direction);
			final Collision upperFacingMarioCorner 	= upperFacingCornerCollision  (y, currentXPosition, direction, isHittingWall, formerLowerYPosition);	
			final Collision lowerOppositeMarioCorner 	= lowerOppositeCornerCollision(y, currentXPosition, direction);
			
			if (landsFacing(lowerFacingMarioCorner, upperFacingMarioCorner, lowerOppositeMarioCorner)) {
				collisionDetection = Collision.HIT_GROUND;
				
				//Hits the facing corner.
				if(landsOpposite(lowerFacingMarioCorner) && 
					upperFacingMarioCorner 	== Collision.HIT_NOTHING &&
					middleFacingMarioCorner == Collision.HIT_NOTHING)	{
					int groundXPos = currentXPosition; 
					
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPos][(int) y],polynomial, false));
					if (ALLOW_SPEED_KEY) {
						listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPos][(int) y],polynomial, true));						
					}
				}										 
				else if (landsOpposite(lowerOppositeMarioCorner)) { //lands on the opposite corner.
					int groundXPos = currentXPosition;
					//TODO (*) I rapporten, explicit forklar hvorfor der skal vaere det paa dene case,
					//men ikke i casen ovenfor. TODO tror det er forkert.
					groundXPos += direction.getOppositeDirection().getHorizontalDirectionAsInt();
					
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPos][(int) y], polynomial, false));
					if (ALLOW_SPEED_KEY) {
						listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPos][(int) y], polynomial, true));						
					}
					
				}
				break;
			} else if (hitsWall(lowerFacingMarioCorner, middleFacingMarioCorner, upperFacingMarioCorner)) {
				// It is purposefully made so that the hit wall will never stop,
				//until the ground is hit.
				collisionDetection = Collision.HIT_WALL;
				isHittingWall = true;
				// No break.
			}
		}
		return collisionDetection;
	}

	private boolean hitsWall(final Collision lowerFacingMarioCorner,
			final Collision middleFacingMarioCorner, final Collision upperFacingMarioCorner) {
		return upperFacingMarioCorner == Collision.HIT_WALL ||
					  middleFacingMarioCorner == Collision.HIT_WALL ||
					  lowerFacingMarioCorner == Collision.HIT_WALL;
	}

	private boolean landsOpposite(final Collision lowerOppositeMarioCorner) {
		return lowerOppositeMarioCorner == Collision.HIT_GROUND;
	}

	private boolean landsFacing(final Collision lowerFacingMarioCorner, final Collision upperFacingMarioCorner,
			final Collision lowerOppositeMarioCorner) {
		return 	(upperFacingMarioCorner == Collision.HIT_NOTHING ||  upperFacingMarioCorner == Collision.HIT_WALL ) 
					&& 
					(lowerFacingMarioCorner == Collision.HIT_GROUND  ||  lowerOppositeMarioCorner == Collision.HIT_GROUND);
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
		//Floors the y position, as we are interested in the block Mario is currently in.
		//Needs yPosition-0.01, as else one will get an immediate collision with the ground, as mario starts on it.
		//Ie. takes it as though mario howers a little above the ground (which he actually does in the code.)
		return isOnLevelMatrix(xPosition, (int) (yPosition - 0.01)) && isSolid(observationGraph[xPosition][(int) (yPosition - 0.01)]);
	}

	private static boolean isJumpThroughNode(Node node) {
		return (node != null && node.type == -11);
	}
	
	private boolean isHittingWallOrGroundDownwards(int xPosition, float yPosition) {
		
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

	private boolean isWithinView(int xPosition, JumpDirection direction) {
		if (direction.isLeftType()) {
			return xPosition < GRID_WIDTH && xPosition >= 1;
		} else return xPosition < GRID_WIDTH - 1 && xPosition >= 0;
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
			//TODO it crashed to changed 0 to 1 in 1 < yPosition to fix it
			return 2 <= yPosition && yPosition < GRID_HEIGHT &&
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
		currentXPosition += direction.getOppositeDirection().getHorizontalDirectionAsInt();
		
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
		currentXPosition += direction.getOppositeDirection().getHorizontalDirectionAsInt();
		
		//It should check if Mario hits the ground: it can't be the wall, as Mario is going in the way opposite to this corner.
		//TODO check and discuss if ceiling the result is correct.
		if (isHittingWallOrGroundDownwards(currentXPosition, y)) {
			if (canMarioStandThere(currentXPosition,  y + 0.01f)) { //+0.01, for the same reason it is done in isHittingWall
				return Collision.HIT_GROUND;
			} else {  
				throw new Error("Logic error on corner collision detection");
				//return Collision.HIT_WALL;
				//TODO i don't think this should be possible:
				//TODO remove throw error.
			}
		} else {
			return Collision.HIT_NOTHING;
		}
	}

	public Collision middleFacingCornerCollision	(float y, int currentXPosition, JumpDirection direction){
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

	public void clearAllEdges() {
		resetFoundAllEdges();
		for (int i = 0; i < observationGraph.length; i++) {
			for (int j = 0; j < observationGraph[i].length; j++) {
				if (observationGraph[i][j] != null) {
					observationGraph[i][j].deleteAllEdges();
				}
			}
		}
	}

	public void resetFoundAllEdges(){
		for (int i = 0; i < observationGraph.length; i++) {
			for (int j = 0; j < observationGraph[i].length; j++) {
				if (observationGraph[i][j] != null) {
					observationGraph[i][j].setIsAllEdgesMade(false);
				}
			}
		}
	}

	
	public void setWorld(World world) {
		this.observationGraph = world.getLevelMatrix();
	}
}
