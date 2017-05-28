package MarioAI.graph.edges;

import java.util.*;

import MarioAI.graph.Collision;
import MarioAI.graph.JumpDirection;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.World;

public  class EdgeCreator {
	private static final float MAX_JUMP_HEIGHT = 4;
	private static final float MAX_JUMP_RANGE = 5;
	public  static final int GRID_HEIGHT = 15;
	public  static final int GRID_WIDTH = 22;
	private static final int MARIO_HEIGHT = 2;
	
	private Node[][] observationGraph = new Node[GRID_WIDTH][GRID_WIDTH];
	private int testPrintCounter = 24; // Rand value

	public void setMovementEdges(World world, Node marioNode) {
		observationGraph = world.getLevelMatrix();
		marioNode.deleteAllEdges();
		
		//First connects all the edges for Mario:
		if(isOnLevelMatrix(GRID_WIDTH / 2, marioNode.y) &&
		   canMarioStandThere(GRID_WIDTH / 2, marioNode.y)) {
			connectNode(marioNode, GRID_WIDTH / 2, marioNode); 
		}
		
		//Then for the rest of the level matrix:
		for (int i = 0; i < observationGraph.length; i++) {
			for (int j = 0; j < observationGraph[i].length; j++) {
				if (isOnLevelMatrix(i, j) &&
				    canMarioStandThere(i,j) && 
				    observationGraph[i][j] != null &&
				    !observationGraph[i][j].isAllEdgesMade()) {
				   connectNode(observationGraph[i][j], i, marioNode); 
				}
			}
		}
		
	}
	
	private void connectNode(Node node, int coloumn, Node marioNode) {
		// Find the reachable nodes:
		for (DirectedEdge connectingEdge : getConnectingEdges(node, coloumn)) {
			if (connectingEdge.target != null && //Must go to actual block, not just air.
				isOnLevelMatrix(connectingEdge.target, marioNode) && //It must be on the current level matrix.
				canMarioStandThere(connectingEdge.target, marioNode) && //The edge must not go into for example a wall.
				connectingEdge.source != connectingEdge.target) { // No movement to the same node. Notice that no equals method are needed.
				//TODO (*) Maybe allow above.
				node.addEdge(connectingEdge); 
			}
		}		
	}
	
	private boolean isOnLevelMatrix(Node position, Node marioNode) {
		return isOnLevelMatrix(getColoumnRelativeToMario(position, marioNode), position.y);
	}

	private boolean isOnLevelMatrix(int coloumn, int row) {
		return (0 <= coloumn && coloumn 	< GRID_WIDTH &&
				  0 <= row 	 	&& row		< GRID_HEIGHT); 
	}
	
	private int getColoumnRelativeToMario(Node node, Node marioNode) {
		//Assumes that node!=null.
		return (node.x - marioNode.x) + (GRID_WIDTH / 2);
	}

	private List<DirectedEdge> getConnectingEdges(Node startingNode, int nodeColoumn) {
		ArrayList<DirectedEdge> listOfEdges = new ArrayList<DirectedEdge>();
		boolean foundAllEdges = true;
		//Two different ways to find the reachable nodes from a given position:
		foundAllEdges = getRunningReachableEdges(startingNode, nodeColoumn, listOfEdges) && foundAllEdges; 
		foundAllEdges = getPolynomialReachingEdges(startingNode,nodeColoumn, listOfEdges) && foundAllEdges;
		
		if (foundAllEdges) startingNode.setIsAllEdgesMade(true);
		
		return listOfEdges;
	}

	
	public boolean getRunningReachableEdges(Node startingNode, int nodeColoumn, List<DirectedEdge> listOfEdges) {
		boolean foundAllEdges = true;

		//Run to the right:
		if (nodeColoumn + 1 < GRID_WIDTH) { //Not at the rightmost block in the view.
			listOfEdges.add(new RunningEdge(startingNode, observationGraph[nodeColoumn + 1][startingNode.y]));
		} else foundAllEdges = false;
		
		//Run to the left:
		if (nodeColoumn > 0) { //Not at the leftmost block in the view.
			listOfEdges.add(new RunningEdge(startingNode, observationGraph[nodeColoumn -1][startingNode.y]));
		}	else foundAllEdges = false;
		
		return foundAllEdges;
	}

	/*** Finds the possible places that mario can jump to, from the given position, 
	 *   and adds the to the given list of nodes. This is done by simulating the jump as a polynomial,
	 *   and finding the place it collides with something.
	 * 
	 * @return
	 */
	public boolean getPolynomialReachingEdges(Node startingNode, int nodeColoumn, List<DirectedEdge> listOfEdges) {
		JumpingEdge polynomial = new JumpingEdge(null, null); //The jump polynomial.
		boolean foundAllEdges = true;
		for (int jumpHeight = (int) 1; jumpHeight <= MAX_JUMP_HEIGHT; jumpHeight++) {
			for (int jumpRange = (int) 1; jumpRange <= MAX_JUMP_RANGE; jumpRange++) { //TODO test only jumprange = 6, no running.
				polynomial.setToJumpPolynomial(startingNode, nodeColoumn, jumpRange, jumpHeight);
				foundAllEdges = jumpAlongPolynomial(startingNode, nodeColoumn, polynomial, JumpDirection.RIGHT_UPWARDS, listOfEdges) && foundAllEdges; //TODO ERROR if removed on shortdeadend
				
				polynomial.setToJumpPolynomial(startingNode, nodeColoumn, -jumpRange, jumpHeight);
				foundAllEdges = jumpAlongPolynomial(startingNode, nodeColoumn, polynomial, JumpDirection.LEFT_UPWARDS, listOfEdges) && foundAllEdges;					
			}
		}
		return foundAllEdges;
	}
	
	public boolean jumpAlongPolynomial(Node startingNode, int nodeColoumn, JumpingEdge polynomial, JumpDirection direction, List<DirectedEdge> listOfEdges) {
		//Starts of from Mario's initial position:
		int currentXPosition = nodeColoumn;
		//If there is a wall collision, an offset between the height function (function of x)
		//and the actual height, might build up. This is taken care of with this variable.
		int xPositionOffsetForJump = 0;
		int formerLowerYPosition = startingNode.y;
		Collision collisionDetection = Collision.HIT_NOTHING;
		//Gives the current direction of the jump:
		JumpDirection currentJumpDirection = direction;

		//Switches modes from ascending to descending, when the top point has been reached.
		boolean hasAlreadyPassedTopPoint = false;
		boolean isPastTopPointColumn = polynomial.isPastTopPoint(nodeColoumn,  currentXPosition + xPositionOffsetForJump);;
		
		while (isWithinView(currentXPosition + xPositionOffsetForJump)) {
			currentXPosition = currentXPosition + direction.getHorizontalDirectionAsInt();
			float currentYPosition;
			
			if (isPastTopPointColumn && !hasAlreadyPassedTopPoint) {
				if ((polynomial.getTopPointX() < currentXPosition && !direction.isLeftType())) { //rightwards!
					currentXPosition--; //The toppoint was in the current block (and not ending there).
					//Therefore the downward going part of that block needs to be checked.
					//See more detailed explanation in the report.
				} else if ((polynomial.getTopPointX() > currentXPosition && direction.isLeftType())) { //Leftwards
					currentXPosition++;
				}
				currentJumpDirection = direction.getOppositeVerticalDirection();				
				hasAlreadyPassedTopPoint = true;
			}
			
			if (!isPastTopPointColumn && 
				  polynomial.isPastTopPoint(nodeColoumn,  currentXPosition + xPositionOffsetForJump)) { //TODO fix here, probably a bug.
				currentYPosition = Math.max(polynomial.getTopPointY(), polynomial.f(currentXPosition + xPositionOffsetForJump)); //TODO no max needed?
			} else {
				currentYPosition = polynomial.f(currentXPosition + xPositionOffsetForJump);	
			}
			//The bound is the bounded value for the next y position -> rounded down.
			//This converts the next y value from (high value = higher up on the level) to (high value = lower on the level)
			final int bound = getBounds(startingNode, (int)currentYPosition); 
			
			if (!isPastTopPointColumn) {
				collisionDetection = ascendingPolynomial (formerLowerYPosition, bound, currentXPosition, collisionDetection, 
														            polynomial, currentJumpDirection, startingNode, listOfEdges);	
			} else {
				collisionDetection = descendingPolynomial(formerLowerYPosition, bound, currentXPosition, collisionDetection, 
						                                  polynomial, currentJumpDirection, startingNode, listOfEdges);		
			}
			
			if (collisionDetection == Collision.HIT_WALL) {
				currentXPosition = currentXPosition + direction.getOppositeDirection().getHorizontalDirectionAsInt();
				xPositionOffsetForJump = xPositionOffsetForJump + direction.getHorizontalDirectionAsInt();
			} else if (collisionDetection == Collision.HIT_CEILING ||
					   collisionDetection == Collision.HIT_GROUND) {
				return true;
			}
			
			isPastTopPointColumn = polynomial.isPastTopPoint(nodeColoumn,  currentXPosition + xPositionOffsetForJump);
			formerLowerYPosition = bound;
		}
		return false;
	}
				
	public  Collision ascendingNoncollidingFunction( int formerLowerYPosition, int bound, int currentXPosition, 
																	Collision collisionDetection, JumpDirection direction){
		return ascendingFunction(formerLowerYPosition, bound, currentXPosition, collisionDetection, null, direction, null, null, true, false);
	}
	
	private Collision ascendingPolynomial(int formerLowerYPosition, int bound, int currentXPosition, Collision collisionDetection,
												 JumpingEdge polynomial, JumpDirection direction, Node startingPosition, List<DirectedEdge> listOfEdges) {
		return ascendingFunction(formerLowerYPosition, bound, currentXPosition, collisionDetection, polynomial, direction, startingPosition, listOfEdges, false, true);
	}
	
	public Collision ascendingFunction(int formerLowerYPosition, int bound, int currentXPosition, 
													Collision collisionDetection, JumpingEdge polynomial, JumpDirection direction, 
													Node startingPosition, List<DirectedEdge> listOfEdges,
													boolean stopAtAnyCollision, boolean addEdges) {
		boolean isHittingWall = false;		
		for (int y = formerLowerYPosition; y >= Math.max(bound, 0); y--) {
			final Collision lowerFacingMarioCorner   = lowerFacingCornerCollision  (y, currentXPosition, collisionDetection, direction);
			final Collision upperFacingMarioCorner   = upperFacingCornerCollision  (isHittingWall, y, formerLowerYPosition, currentXPosition, direction);	
			final Collision upperOppositeMarioCorner = upperOppositeCornerCollision(y, currentXPosition, direction);	
			final Collision middleFacingMarioCorner  = middleFacingCornerCollision(y, currentXPosition, direction);
			//As it is ascending to the right, only worry about the two corners to the right/left,
			//and maybe the middle right/left part, if Mario has height 2.
			if (upperOppositeMarioCorner 	== Collision.HIT_CEILING  || 
				 upperFacingMarioCorner 	== Collision.HIT_CEILING){
				collisionDetection = Collision.HIT_CEILING;
				break;
			} else if (upperFacingMarioCorner == Collision.HIT_NOTHING && 
						  lowerFacingMarioCorner == Collision.HIT_GROUND) {
				collisionDetection = Collision.HIT_GROUND;
				if (addEdges) {
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[currentXPosition][y], polynomial));
				}
				break;
			} else if (upperFacingMarioCorner == Collision.HIT_WALL    || 
						  lowerFacingMarioCorner == Collision.HIT_WALL) {
				collisionDetection = Collision.HIT_WALL;
				isHittingWall = true;
				if (stopAtAnyCollision) {
					break;
				}
				//No break.
			} 
			//Extra check if Mario has height 2. Can only be HIT_WALL, 
			//and is always HIT_NOTHING if Mario has height less than 2.
			//(*) TODO test this works.
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
	
	private Collision descendingPolynomial(int formerLowerYPosition, int bound, int currentXPosition, Collision collisionDetection,
												 JumpingEdge polynomial, JumpDirection direction, Node startingPosition, List<DirectedEdge> listOfEdges) {
		boolean isHittingWall = false;		
		for (int y = formerLowerYPosition; y <= Math.min(bound, GRID_HEIGHT - 1); y++) {
			final Collision lowerFacingMarioCorner 	 = lowerFacingCornerCollision  (y, currentXPosition, collisionDetection, direction);
			final Collision upperFacingMarioCorner 	 = upperFacingCornerCollision  (isHittingWall, y, formerLowerYPosition, currentXPosition, direction);	
			final Collision lowerOppositeMarioCorner = lowerOppositeCornerCollision(y, currentXPosition, direction);	
			//As it is descending to the right, only worry about the two corners to the right, and the left lower one.
			
			if (upperFacingMarioCorner == Collision.HIT_NOTHING && 
			    (lowerFacingMarioCorner == Collision.HIT_GROUND || 
			     lowerOppositeMarioCorner == Collision.HIT_GROUND)) {
				collisionDetection = Collision.HIT_GROUND;
				if(lowerFacingMarioCorner == Collision.HIT_GROUND)	{
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[currentXPosition][y],polynomial));
				}										 
				else {
					final int groundXPos = currentXPosition + direction.getOppositeDirection().getHorizontalDirectionAsInt();
					listOfEdges.add(new JumpingEdge(startingPosition, observationGraph[groundXPos][y], polynomial));
				}
				break;
			} else if (upperFacingMarioCorner == Collision.HIT_WALL || 
					   lowerFacingMarioCorner == Collision.HIT_WALL) {
				//It is purposefully made so that the hit wall will never stop, until the ground is hit.
				
				collisionDetection = Collision.HIT_WALL;
				isHittingWall = true;
				//No break.
			}
		}
		return collisionDetection;
	}
	
	private int getBounds(Node startingNode, int currentLowerYPosition) {
		return startingNode.y - (currentLowerYPosition - startingNode.y);
	}
	
	private static boolean isSolid(Node node) {
		//return node != null;
		return node != null && node.type != -11;// TODO(*) Fix
	}
	
	private boolean isHittingWallOrGroundUpwards(int xPosition, int yPosition) {
		//Being out of the level matrix does not constitute as hitting something
		//Needs yPosition-1, as else one will get an immediate collision with the ground, as mario starts on it.
		return isOnLevelMatrix(xPosition, yPosition - 1) && isSolid(observationGraph[xPosition][yPosition - 1]);
	}
	
	private static boolean isJumpThroughNode(Node node) {
		return (node != null && node.type == -11);
	}
	
	private boolean isHittingWallOrGroundDownwards(int xPosition, int yPosition) {
		//Being out of the level matrix does not constitute as hitting something
		//TODO removed yPosition-1
		if (isOnLevelMatrix(xPosition, yPosition)) {
			final boolean isAtSolidNode = isSolid(observationGraph[xPosition][yPosition]);
			final boolean isAtJumpthroughNode = isJumpThroughNode(observationGraph[xPosition][yPosition]);
			
			return isAtSolidNode || isAtJumpthroughNode;
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
		//if (node == null || node.y < 0  || GRID_HEIGHT <= node.y  ) { 
		if (bool1|| bool2  ||bool3  ) { //Node can't stand on air, nor can he stand on nothing -> things that are not in the array.
			return false;
		} else{
			int nodeXPosition = getColoumnRelativeToMario(node, marioNode);
			return isOnSolidGround(node.y, nodeXPosition) && observationGraph[nodeXPosition][node.y - 1] == null;	
		}
	}

	private boolean canMarioStandThere(int coloumn,int row) {
		return 0 < row && row < GRID_HEIGHT &&
			   isOnSolidGround(row, coloumn) && observationGraph[coloumn][row - 1] == null;
	}

	private boolean isOnSolidGround(int row, int coloumn) {
		return observationGraph[coloumn][row] != null; //TODO Fix in general.
	}

	private boolean isAir(int coloumn, int row) {
		//return node != null;
		return !(0 <= row 	  && row < GRID_HEIGHT     &&
				 0 <= coloumn && coloumn < GRID_WIDTH) ||				
				 observationGraph[coloumn][row] == null;// TODO(*) Fix
	}
				
	private Collision lowerFacingCornerCollision(int y, int currentXPosition, Collision collisionDetection, JumpDirection direction) {
		if (direction.isUpwardsType()) { 
			//In the case one is going upwards, one should not check for any other type of collisions, 
			//than those originating from wall collisions.
			if (isHittingWallOrGroundUpwards(currentXPosition, y)) { //If it is hitting the ceiling, upperRight will notice.
				return Collision.HIT_WALL;
			} else {
				if (isAir(currentXPosition, y - MARIO_HEIGHT) && 
					collisionDetection == Collision.HIT_WALL) {
					return Collision.HIT_GROUND;
				} else {
					return Collision.HIT_NOTHING;			
				}
			}
		} else {
			//One must be going downwards, and there should be checked for collisions:
			if (isHittingWallOrGroundDownwards(currentXPosition, y)) {
				//If this corner is hitting something, then there are two possibilities: either it is the ground or a wall.
				//If it is the ground, then Mario can stand there, and if it is a wall, it is not possible.
				if (canMarioStandThere(currentXPosition, y)) {
					return Collision.HIT_GROUND;
				} else {
					return Collision.HIT_WALL;
				}
			}			
			return Collision.HIT_NOTHING;
		}
	}

	private Collision upperFacingCornerCollision(boolean isHittingWall, int y, int formerLowerYPosition, int currentXPosition, JumpDirection direction) {
		if (direction.isUpwardsType()) {
			//If mario is going upwards, one needs to check for ceiling collisions and the wall collisions,
			//and not whether he hits the ground. This will be registered by the lower part.
			if (isHittingWallOrGroundUpwards(currentXPosition, y - 1)) {
				if (y == formerLowerYPosition) {
					return Collision.HIT_WALL;
				} else if (!isHittingWall){
					//TODO make.
					//hitWallOrGround(listOfEdges, currentXPosition,y);
					return Collision.HIT_CEILING;					
				} else {
					return Collision.HIT_WALL;
				}
			} else {
				return Collision.HIT_NOTHING;
			}
			
		} else {
			//It can only hit a wall, or nothing, so:
			if (isHittingWallOrGroundDownwards(currentXPosition, y - 1)) {
				return Collision.HIT_WALL;
			} else {
				return Collision.HIT_NOTHING;
			}
		}
	}
	
	private Collision upperOppositeCornerCollision(int y, int currentXPosition, JumpDirection direction) {
		currentXPosition += direction.getOppositeDirection().getHorizontalDirectionAsInt();
		
		//If Mario is going upwards, and since this is the opposite corner of the way he is going, 
		//one only needs to check for ceiling collisions.
		if (isHittingWallOrGroundUpwards(currentXPosition, y - 1)) {
			return Collision.HIT_CEILING;
		} else {
			return Collision.HIT_NOTHING;
		}
	}
	
	private Collision lowerOppositeCornerCollision(int y, int currentXPosition, JumpDirection direction) {
		currentXPosition += direction.getOppositeDirection().getHorizontalDirectionAsInt();
		
		//It should check if Mario hits the ground: it can't be the wall, as Mario is going in the way opposite to this corner.
		if (isHittingWallOrGroundDownwards(currentXPosition, y)) {
			if (canMarioStandThere(currentXPosition, y)) {
				return Collision.HIT_GROUND;
			} else {  
				return Collision.HIT_WALL;
				//TODO i don't think this should be possible:
				//throw new Error("Logic error on corner collision detection");
			}
		}else {
			return Collision.HIT_NOTHING;
		}
	}

	private Collision middleFacingCornerCollision(int y, int currentXPosition,JumpDirection direction){
		//TODO fix this, doens't work properly
		//Mario must have height 2, before this is relevant:
		if (MARIO_HEIGHT == 2) {
			//Some blocks can be passed through going upwards, not downwards, so two cases are necessary:
			//TODO check if it is nessecary(*)
			if ((direction.isUpwardsType() 	&& isHittingWallOrGroundUpwards(currentXPosition, y)) 	||
				 (!direction.isUpwardsType()    	&& isHittingWallOrGroundDownwards(currentXPosition, y))) { //If it is hitting the ceiling, upperRight will notice.
				return Collision.HIT_WALL;
			} 
			else return Collision.HIT_NOTHING;
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


