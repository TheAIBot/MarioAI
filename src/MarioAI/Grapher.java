package MarioAI;

import java.util.*; 

import ch.idsia.mario.environments.Environment;

public class Grapher {
	Environment observation;
	private byte[][] currentObservation;
	private final float JUMP_HEIGHT = 4;
	private final float MAX_JUMP_RANGE = 6;
	private final short GRID_SIZE = 22;
	private Node[][] observationGraph = new Node[GRID_SIZE][GRID_SIZE];
	private boolean[][] inRecursion = new boolean[GRID_SIZE][GRID_SIZE];
	private final short MARIO_JUMP_LENGHT = 5;
	private Node marioNode;
	int testPrintCounter = 24; // Rand value

	public Grapher(Environment observation) {
		this.observation = observation;
		currentObservation = observation.getCompleteObservation();
	}

	public void updateLevelGraph() {
		currentObservation = this.observation.getCompleteObservation();
	}

	public void printView() {
		testPrintCounter = testPrintCounter % 240;
		if (testPrintCounter == 0) {
			System.out.printf("    ");
			for (int j = 0; j < GRID_SIZE; j++) {
				System.out.printf("%03d ", j);
			}
			System.out.println();
			
			for (int i = 0; i < GRID_SIZE; i++) {
				System.out.printf("%03d ", i);				
				for (int j = 0; j < GRID_SIZE; j++) {
					if (i == 11 && j == 11) 
						System.out.printf("MMM ", currentObservation[i][j]);
					else
						System.out.printf("%03d ", currentObservation[i][j]);
				}
				System.out.println();
			}			
			observationGraph = new Node[GRID_SIZE][GRID_SIZE]; // Test
			marioNode = new Node((short) 0, (short) 0, (short) (GRID_SIZE / 2), (short) (GRID_SIZE / 2),
					currentObservation[GRID_SIZE / 2][GRID_SIZE / 2]);
			observationGraph[GRID_SIZE / 2][GRID_SIZE / 2] = marioNode; // Mario
																		// node
			inRecursion[GRID_SIZE / 2][GRID_SIZE / 2] = true;
			if (isSolid(currentObservation[marioNode.row + 1][marioNode.coloumn]))
				connectNode(marioNode);
			System.out.println("The observation.");
		}
		testPrintCounter++;

	}
	
	private void connectNode(Node node) {
		// For efficiency, it is possible to make this into an iterative
		// function, instead of a recursive method,
		// either by using a stack (depth first),or an queue (breadth first).

		// Maybe detect enemy.
		// Remember to use mario's speed in calculations.

		// Find the reachable nodes:
		List<Node> reachableNodes = getReachableNodes(node);
		for (Node foundNode : reachableNodes) {
			if (canMarioStandThere(foundNode)) { // FIX
				node.edges.add(foundNode); //TODO Fix the fact that there are no guarantee that there aren't duplicates.
			}
		}
		// Recursion over the reachable nodes:
		for (Node edge : node.edges) {
			if (!inRecursion[edge.row][edge.coloumn]) {
				inRecursion[edge.row][edge.coloumn] = true; //Infinite recursion not allowed!.
				connectNode(edge); // Check which nodes it can reach!
				//Because of the structure, it is a depth first search.
			}
		}

	}

	private List<Node> getReachableNodes(Node startingNode) {
		ArrayList<Node> listOfNodes = new ArrayList<Node>();
		//Three different ways to find the reachable nodes from a given position:
		//getRunningReachableNodes(startingNode, listOfNodes); //TODO Obs. no need to return a list of nodes
		//getBadJumpReachableNodes(startingNode, listOfNodes);
		getPolynomialReachableNodes(startingNode, listOfNodes);
		return listOfNodes;
	}

	private List<Node> getBadJumpReachableNodes(Node startingNode, List<Node> listOfNodes) {
		// (bad) Jumping and falling.
		for (short i = -5; i < MARIO_JUMP_LENGHT; i++) { // Starting at 5 blocks
															// below, to
															// simulate falling.
			if (startingNode.row + i >= GRID_SIZE || startingNode.row + i < 0 || startingNode.row - i >= GRID_SIZE
					|| startingNode.row - i < 0)
				continue;
			if (startingNode.coloumn + 1 < GRID_SIZE) {
				if (observationGraph[startingNode.row - i][startingNode.coloumn + 1] == null) {
					observationGraph[startingNode.row - i][startingNode.coloumn + 1] = new Node(
							(short) (startingNode.x + 1), startingNode.y, (short) (startingNode.coloumn + 1),
							(short) (startingNode.row - i),
							currentObservation[startingNode.row - i][startingNode.coloumn + 1]);
				}
				listOfNodes.add(observationGraph[startingNode.row - i][startingNode.coloumn + 1]);
			}
			// Minus
			if (startingNode.coloumn - 1 >= 0) {
				if (observationGraph[startingNode.row - i][startingNode.coloumn - 1] == null) {
					observationGraph[startingNode.row - i][startingNode.coloumn - 1] = new Node(
							(short) (startingNode.x + 1), startingNode.y, (short) (startingNode.coloumn - 1),
							(short) (startingNode.row - i),
							currentObservation[startingNode.row - i][startingNode.coloumn - 1]);
				}
				listOfNodes.add(observationGraph[startingNode.row - i][startingNode.coloumn - 1]);
			}
		}
		return listOfNodes;
	}

	private List<Node> getRunningReachableNodes(Node startingNode, List<Node> listOfNodes) {
		// (bad) Running:
		for (short i = 1; i <= 1; i++) {
			// Plus
			if (startingNode.coloumn + 1 < GRID_SIZE) {
				if (observationGraph[startingNode.row][startingNode.coloumn + i] == null) {
					observationGraph[startingNode.row][startingNode.coloumn + i] = new Node(
							(short) (startingNode.x + i), startingNode.y, (short) (startingNode.coloumn + i),
							startingNode.row, currentObservation[startingNode.row][startingNode.coloumn + i]);
				}
				listOfNodes.add(observationGraph[startingNode.row][startingNode.coloumn + 1]);
			}
			// Minus
			if (startingNode.coloumn - 1 >= 0) {
				if (observationGraph[startingNode.row][startingNode.coloumn - i] == null) {
					observationGraph[startingNode.row][startingNode.coloumn - i] = new Node(
							(short) (startingNode.x + i), startingNode.y, (short) (startingNode.coloumn - i),
							startingNode.row, currentObservation[startingNode.row][startingNode.coloumn - i]);
				}
				listOfNodes.add(observationGraph[startingNode.row][startingNode.coloumn - 1]);
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
	private List<Node> getPolynomialReachableNodes(Node startingNode, List<Node> listOfNodes) {
		//TODO Extra ting der kan tilføjes: polynomium hop til fjender!
		//TODO Polynomial bounding conditions.
		SecondOrderPolynomial polynomial = new SecondOrderPolynomial(); //The jump polynomial.
		//TODO All four corners of Mario!
		for (float jumpRange = 1; jumpRange <= MAX_JUMP_RANGE; jumpRange++) {
			polynomial.setToJumpPolynomial(startingNode, jumpRange, JUMP_HEIGHT);
			jumpAlongPolynomial(startingNode, polynomial, listOfNodes);
						
		}
		return listOfNodes; //No guarantee that there are no duplicate nodes.
	}
	
	private void jumpAlongPolynomial(Node startingNode, SecondOrderPolynomial polynomial, List<Node> listOfNodes) {
		
		//Starts of from Mario's initial position:
		short currentXPosition = startingNode.coloumn;
		float formerYPosition = startingNode.row;
		short formerLowerYPosition = startingNode.row;
		boolean hasMetHardGround = false;
				
		float currentYPosition;
		short currentLowerYPosition; //Automatic flooring included!
		// TODO change to take the sign into account
		short bound;
		//TODO Doesen't take falling down into a hole into account.		
			
		//Get upwards moving part:
		//Primarily collision detection.
		while (!hasMetHardGround &&
			   !polynomial.isPastTopPunkt(startingNode.coloumn, currentXPosition) &&
			   isWithinView(currentXPosition)) { 
			currentXPosition++;			
			//Has just passed the toppunkt, ie. the toppunkt was on the current "block"
			if (polynomial.isPastTopPunkt(startingNode.coloumn, currentXPosition)) { 
				//Up to the max height of the polynomial!
				currentYPosition = polynomial.getTopPunktY(); 						
			} else {//Else up to the current height of the polynomial.
				currentYPosition = polynomial.f(currentXPosition);				
			}
			//First rounded to 1/64.	
			currentLowerYPosition = (short) (Math.round(currentYPosition*64)/64); //Automatic flooring included!	
			bound = (short) (startingNode.row - (currentLowerYPosition-startingNode.row)); 
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
			bound = (short) (Math.round((startingNode.row - (currentLowerYPosition-startingNode.row))*100)/100f); //First roundet to two deciamals, then floored.		
			
			hasMetHardGround = descendingPolynomial(formerLowerYPosition, bound, currentXPosition, listOfNodes);			
			
			formerYPosition = currentYPosition;
			formerLowerYPosition = bound;
		}
	}
	
	
	private boolean ascendingPolynomial(short formerLowerYPosition, short bound, short currentXPosition, List<Node> listOfNodes) {
		for (short y = formerLowerYPosition; y >= bound; y--) {
			if (isHittingWallOrGround(currentXPosition,y)) {
				hitWallOrGround(listOfNodes, currentXPosition,y);
				return true;
			} 
			
		}	
		return false;
	}
	
	private boolean descendingPolynomial(short formerLowerYPosition, short bound, short currentXPosition, List<Node> listOfNodes) {
		for (short y = formerLowerYPosition; y <= bound; y++) {
			if (isHittingWallOrGround(currentXPosition,y)) {
				hitWallOrGround(listOfNodes, currentXPosition,y);
				return true;
			} else if (canMarioStandThere(currentXPosition, y)) { 
				//Checks if mario can stand on the current block, and if he is currently in the falling part of the polynomial.
				//This is not done by precisely checking if it hits the ground, for a reason --> Mario's can move while jumping.
				//TODO Later it would be more appropriate to use a isFalling boolean, f.eks. hvis mario glider langs en mur,
				//hvormed x positionen ikke ændrer sig.
				
				if (observationGraph[y][currentXPosition] == null) {
					observationGraph[y][currentXPosition] = new Node(currentXPosition, y, currentXPosition, y,currentObservation[currentXPosition][y]);
				}					
				listOfNodes.add(observationGraph[y][currentXPosition]);
			}		
		}
		return false;
	}
	
	/***
	 * @return
	 */
	private boolean isHittingWallOrGround(short xPosition, short yPosition) {
		return isSolid(currentObservation[yPosition][xPosition]);
	}
	
	/***
	 * 
	 */
	private void hitWallOrGround(List<Node> listOfNodes, short xPosition, short yPosition) {
		Node fallDownPosition = getFallDownPosition(xPosition, yPosition);
		if(fallDownPosition != null) {
			if (observationGraph[fallDownPosition.row][fallDownPosition.coloumn] != null) {
				observationGraph[fallDownPosition.row][fallDownPosition.coloumn] = fallDownPosition;
			}			
			listOfNodes.add(fallDownPosition);
		}
	}
		
	private boolean isWithinView(short xPosition) { //TODO Rename, curtesy of +1
		return xPosition + 1 < GRID_SIZE && xPosition > 0;
	}
	
	private Node getFallDownPosition(short row, short coloumn) {
		// Not including zero, as there needs to be a block below mario to fall
		// down and stand on.
		for (short yPosition = row; yPosition < GRID_SIZE - 1; yPosition++) {
			if (canMarioStandThere(coloumn,yPosition))
				return new Node(coloumn, row, coloumn, yPosition, currentObservation[yPosition][coloumn]);
		}

		return null;
	}
	
	private short newYPosition(short x, short topPunktX,short currentY) {
		if(x < topPunktX) return currentY--;
		else return currentY++;
	}

	private boolean canMarioStandThere(Node place) {
		return canMarioStandThere(place.coloumn,place.row);
	}

	private boolean canMarioStandThere(short coloumn,short row) {
		return isOnSolidGround(row, coloumn) && !isSolid(currentObservation[row][coloumn]);
	}

	private boolean isOnSolidGround(short row, short coloumn) {
		return isSolid(currentObservation[row + 1][coloumn]) || currentObservation[row + 1][coloumn] == -11; //TODO Fix in general.
	}

	private boolean isSolid(byte blockType) {
		return blockType != 0 && blockType != -11;// TODO(*) Fix
	}
	
}