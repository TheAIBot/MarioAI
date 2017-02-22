package MarioAI;

import java.util.*; 

import ch.idsia.mario.environments.Environment;

public  class Grapher {
	private static final float JUMP_HEIGHT = 4;
	private static final float MAX_JUMP_RANGE = 6;
	private static final short GRID_SIZE = 22;
	private static Node[][] observationGraph = new Node[GRID_SIZE][GRID_SIZE];
	private static boolean[][] inRecursion = new boolean[GRID_SIZE][GRID_SIZE];
	private static final short MARIO_JUMP_LENGHT = 5;
	private static Node marioNode;
	static int testPrintCounter = 24; // Rand value

	public static void printView() {
		testPrintCounter = testPrintCounter % 240;
		if (true) {
			System.out.printf("    ");
			for (int j = 0; j < observationGraph[0].length; j++) {
				System.out.printf("%03d ", j);
			}
			System.out.println();
			
			for (int i = 0; i < observationGraph.length; i++) {
				System.out.printf("%03d ", i);				
				for (int j = 0; j < observationGraph[i].length; j++) {
					if (j == marioNode.y && i == GRID_SIZE/2) 
						System.out.printf("MMM ");
					else {
						if (observationGraph[i][j] == null) {
							System.out.printf("%03d ", 0);									
						} else System.out.printf("%03d ", observationGraph[i][j].type);						
					}
				}
				System.out.println();
			}	
		}
		testPrintCounter++;

	}
	
	public static void graph(Node[][] levelMatrix, Node mario) {
		observationGraph = levelMatrix;
		inRecursion= new boolean[GRID_SIZE][GRID_SIZE];
		//inRecursion[GRID_SIZE/2][mario.y]  = true; Skal ikke goeres, da Mario er en seperat node fra banen.
		marioNode = mario;
		printView();
		connectNode(mario, (short) (GRID_SIZE/2));
		System.out.println("The edges are ready!");
	}
	
	private static void connectNode(Node node, short coloumn) {
		// For efficiency, it is possible to make this into an iterative
		// function, instead of a recursive method,
		// either by using a stack (depth first),or an queue (breadth first).

		// Maybe detect enemy.
		// Remember to use mario's speed in calculations.

		// Find the reachable nodes:
		List<Node> reachableNodes = getReachableNodes(node, coloumn);
		for (Node foundNode : reachableNodes) {
			if (canMarioStandThere(getColoumnRelativeToMario(foundNode, marioNode), foundNode.y)) { // FIX
				node.neighbors.add(foundNode); //TODO Fix the fact that there are no guarantee that there aren't duplicates.
			}
		}
		// Recursion over the reachable nodes:
		for (Node neighbor : node.neighbors) {
			short neighborColoumn = getColoumnRelativeToMario(neighbor, marioNode);
			if (!inRecursion[neighborColoumn][neighbor.y]) {
				inRecursion[neighborColoumn][neighbor.y] = true; //Infinite recursion not allowed!.
				connectNode(neighbor,neighborColoumn ); // Check which nodes it can reach!
				//Because of the structure, it is a depth first search.
			}
		}

	}
	
	private static short getColoumnRelativeToMario(Node node, Node marioNode) {
		return (short) ((node.x - marioNode.x) + 11);
	}

	private static List<Node> getReachableNodes(Node startingNode, short nodeColoumn) {
		ArrayList<Node> listOfNodes = new ArrayList<Node>();
		//Three different ways to find the reachable nodes from a given position:
		//getRunningReachableNodes(startingNode, listOfNodes, nodeColoumn); //TODO Obs. no need to return a list of nodes
		//getBadJumpReachableNodes(startingNode, listOfNodes, nodeColoumn);
		getPolynomialReachableNodes(startingNode,nodeColoumn, listOfNodes);
		return listOfNodes;
	}
	
	private static List<Node> getBadJumpReachableNodes(Node startingNode, short nodeColoumn, List<Node> listOfNodes) {
		// (bad) Jumping and falling.
		for (short i = -5; i < MARIO_JUMP_LENGHT; i++) { // Starting at 5 blocks
															// below, to
															// simulate falling.
			if (startingNode.y + i >= GRID_SIZE || startingNode.y + i < 0 || startingNode.y - i >= GRID_SIZE
					|| startingNode.y - i < 0)
				continue;
			if (nodeColoumn + 1 < GRID_SIZE) {
				listOfNodes.add(observationGraph[startingNode.y - i][nodeColoumn + 1]);
			}
			// Minus
			if (nodeColoumn - 1 >= 0) {
				listOfNodes.add(observationGraph[startingNode.y - i][nodeColoumn - 1]);
			}
		}
		return listOfNodes;
	}

	private static List<Node> getRunningReachableNodes(Node startingNode, short nodeColoumn, List<Node> listOfNodes) {
		// (bad) Running:
		for (short i = 1; i <= 1; i++) {
			// Plus
			if (nodeColoumn + 1 < GRID_SIZE) {
				listOfNodes.add(observationGraph[startingNode.y][nodeColoumn + 1]);
			}
			// Minus
			if (nodeColoumn - 1 >= 0) {
				listOfNodes.add(observationGraph[startingNode.y][nodeColoumn - 1]);
			}
		}
		return listOfNodes;
	}
	
	/*** Finds the possible places that mario can jump to, from the given position, 
	 *   and adds the to the given list of nodes. This is done by simulating the jump as a polynomial,
	 *   and finding the place it collides with something.
	 * 
	 * @return
	 */
	private static List<Node> getPolynomialReachableNodes(Node startingNode, short nodeColoumn, List<Node> listOfNodes) {
		//TODO Extra ting der kan tilføjes: polynomium hop til fjender!
		//TODO Polynomial bounding conditions.
		SecondOrderPolynomial polynomial = new SecondOrderPolynomial(); //The jump polynomial.
		//TODO All four corners of Mario!
		for (float jumpRange = 6; jumpRange <= MAX_JUMP_RANGE; jumpRange++) {
			polynomial.setToJumpPolynomial(startingNode, nodeColoumn, jumpRange, JUMP_HEIGHT);
			jumpAlongPolynomial(startingNode, nodeColoumn, polynomial, listOfNodes);
						
		}
		return listOfNodes; //No guarantee that there are no duplicate nodes.
	}
	
	private static void jumpAlongPolynomial(Node startingNode, short nodeColoumn, SecondOrderPolynomial polynomial, List<Node> listOfNodes) {
		
		//Starts of from Mario's initial position:
		short currentXPosition = nodeColoumn;
		float formerYPosition = startingNode.y;
		short formerLowerYPosition = startingNode.y;
		boolean hasMetHardGround = false;
				
		float currentYPosition;
		short currentLowerYPosition; //Automatic flooring included!
		// TODO change to take the sign into account
		short bound;
		//TODO Doesen't take falling down into a hole into account.		
			
		//Get upwards moving part:
		//Primarily collision detection.
		while (!hasMetHardGround &&
			   !polynomial.isPastTopPunkt(nodeColoumn, currentXPosition) &&
			   isWithinView(currentXPosition)) { 
			currentXPosition++;			
			//Has just passed the toppunkt, ie. the toppunkt was on the current "block"
			if (polynomial.isPastTopPunkt(nodeColoumn, currentXPosition)) { 
				//Up to the max height of the polynomial!
				currentYPosition = polynomial.getTopPunktY(); 						
			} else {//Else up to the current height of the polynomial.
				currentYPosition = polynomial.f(currentXPosition);				
			}
			//First rounded to 1/64.	
			currentLowerYPosition = (short) (Math.round(currentYPosition*64)/64); //Automatic flooring included!
			bound = getBounds(startingNode, currentLowerYPosition); 
			hasMetHardGround = ascendingPolynomial(formerLowerYPosition, bound, currentXPosition, listOfNodes);	
			formerYPosition = currentYPosition;
			formerLowerYPosition = bound;
		}
		
		//Downwards:
		if (polynomial.getTopPunktX() < currentXPosition) {
			currentXPosition--; //The toppunkt was in the current block (and not ending there).
			//Therefore the downward going part of that block needs to be checked.
		}

		while (!hasMetHardGround &&
				isWithinView(currentXPosition)) { //Doesen't take falling down into a hole into account.
			
			currentXPosition++;					
			currentYPosition = polynomial.f(currentXPosition);
			//First rounded to 1/64.	
			currentLowerYPosition = (short) (Math.round(currentYPosition*64)/64); //Automatic flooring included!			
			// TODO change to take the sign into account
			bound = getBounds(startingNode, currentLowerYPosition); 	
			
			hasMetHardGround = descendingPolynomial(formerLowerYPosition, bound, currentXPosition, listOfNodes, polynomial, startingNode);			
			
			formerYPosition = currentYPosition;
			formerLowerYPosition = bound;
		}
	}
		
	private static boolean ascendingPolynomial(short formerLowerYPosition, short bound, short currentXPosition, List<Node> listOfNodes) {
		for (short y = formerLowerYPosition; y >= bound; y--) {
			if (isHittingWallOrGround(currentXPosition,y)) {
				hitWallOrGround(listOfNodes, currentXPosition,y);
				return true;
			} 
			
		}	
		return false;
	}
	
	private static boolean descendingPolynomial(short formerLowerYPosition, short bound, short currentXPosition,
			                                    List<Node> listOfNodes, SecondOrderPolynomial polynomial, Node startingPosition) {
		for (short y = formerLowerYPosition; y <= bound; y++) {
			if (isHittingWallOrGround(currentXPosition,y)) {
				hitWallOrGround(listOfNodes, currentXPosition,y);
				return true;
			} else if (canMarioStandThere(currentXPosition, y)) { 
				//Checks if mario can stand on the current block, and if he is currently in the falling part of the polynomial.
				//This is not done by precisely checking if it hits the ground, for a reason --> Mario's can move while jumping.
				//TODO Later it would be more appropriate to use a isFalling boolean, f.eks. hvis mario glider langs en mur,
				//hvormed x positionen ikke ændrer sig.		
				listOfNodes.add(observationGraph[currentXPosition][y]);
				//Hvis den kun lige akkurat kommer dertil, stoppes der, så der ikke kommer en kant til næste knude.
				//TODO Fix so no multi edges
			}		
		}
		return false;
	}
	
	private static short getBounds(Node startingNode, short currentLowerYPosition) {
		return (short) (Math.round((startingNode.y - (currentLowerYPosition-startingNode.y))*64)/64); //Rounded and then floored!
	}
	
	/***
	 * @return
	 */
	private static boolean isHittingWallOrGround(short xPosition, short yPosition) {
		return isSolid(observationGraph[xPosition][yPosition-1]);
	}
	
	/***
	 * 
	 */
	private static void hitWallOrGround(List<Node> listOfNodes, short xPosition, short yPosition) {
		Node fallDownPosition = getFallDownPosition(xPosition, yPosition);
		if(fallDownPosition != null) {
			listOfNodes.add(fallDownPosition);
		}
	}
		
	private static boolean isWithinView(short xPosition) { //TODO Rename, curtesy of +1
		return xPosition + 1 < GRID_SIZE && xPosition > 0;
	}
	
	private static Node getFallDownPosition(short coloumn, short row) {
		// Not including zero, as there needs to be a block below mario to fall
		// down and stand on.
		for (short yPosition = row; yPosition < 15; yPosition++) {
			if (canMarioStandThere(coloumn,yPosition))
				return observationGraph[coloumn][row];
		}

		return null;
	}
	
	private static short newYPosition(short x, short topPunktX,short currentY) {
		if(x < topPunktX) return currentY--;
		else return currentY++;
	}

	private static boolean canMarioStandThere(short coloumn,short row) {
		return isOnSolidGround(row, coloumn) && observationGraph[coloumn][row - 1] == null;
	}

	private static boolean isOnSolidGround(short row, short coloumn) {
		return observationGraph[coloumn][row] != null && ( isSolid(observationGraph[coloumn][row]) || observationGraph[coloumn][row].type == -11); //TODO Fix in general.
	}

	private static boolean isSolid(Node node) {
		return node != null && node.type != -11;// TODO(*) Fix
	}
	
}