package MarioAI;

import java.util.*;

import ch.idsia.mario.environments.Environment;
import jdk.internal.dynalink.beans.StaticClass;

public class Grapher {
	Environment observation;
	private byte[][] currentObservation;
	private final short GRID_SIZE = 22;
	private Node[][] observationGraph = new Node[GRID_SIZE][GRID_SIZE];
	private boolean[][] inRecursion = new boolean[GRID_SIZE][GRID_SIZE];
	private final short MARIO_JUMP_LENGHT = 5;
	private Node marioNode;
	int testPrintCounter = 24; //Rand value
	
	
	
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
			for (int i = 0; i < GRID_SIZE; i++) {
				for (int j = 0; j < GRID_SIZE; j++) {
					if(i == 11 && j==11) {
						System.out.printf("  M ",currentObservation[i][j]);
					}else System.out.printf("%03d ",currentObservation[i][j]);
				}
				System.out.println();
			}			
			observationGraph = new Node[GRID_SIZE][GRID_SIZE]; //Test			
			marioNode = new Node((short) 0, (short) 0, (short) (GRID_SIZE/2), (short) (GRID_SIZE/2),  
					 currentObservation[GRID_SIZE/2][GRID_SIZE/2]);			
			observationGraph[GRID_SIZE/2][GRID_SIZE/2]= marioNode; //Mario node
			inRecursion[GRID_SIZE/2][GRID_SIZE/2] = true;
			if(isSolid(currentObservation[marioNode.row - 1][marioNode.coloumn])) connectNode(marioNode);
			System.out.println("The observation.");			
		}
		testPrintCounter++;	
		
	}
	
	private void connectNode(Node node) {
		//For efficiency, it is possible to make this into an iterative function, instead of a recursive method, 
		//either by using a stack (depth first),or an queue (breadth first).
		
		//Maybe detect enemy.		
		//Remember to use mario's speed in calculations.	
		
		//Find the reachable nodes:
		List<Node> circleNodes = getReachableNodes(node);
		for (int i = 0; i < circleNodes.size(); i++) {
			if (canMarioStandThere(circleNodes.get(i))) { //FIX
				node.neighbors.add(circleNodes.get(i));
			}
		}									
		//Recursion over the reachable nodes:
		for (Node edge : node.neighbors) {
			if (!inRecursion[edge.row][edge.coloumn]) {
				inRecursion[edge.row][edge.coloumn] = true; //No infinite recursion.
				connectNode(edge); //Thus it is depth first.
			}
		}
		
	
	}
	
	private List<Node> getReachableNodes(Node startingNode) {
		ArrayList<Node> listOfNodes = new ArrayList<Node>();	
		getRunningReachableNodes(startingNode, listOfNodes);
		getBadJumpReachableNodes(startingNode, listOfNodes);	
		getPolynomialReachableNodes(startingNode, listOfNodes);
		return listOfNodes;
	}
	
	private List<Node> getBadJumpReachableNodes(Node startingNode, List<Node> listOfNodes){
		//(bad) Jumping and falling.
		for (short i = -5; i < MARIO_JUMP_LENGHT ; i++) { //Starting at 5 blocks below, to simulate falling.
			if(startingNode.row + i >= GRID_SIZE || startingNode.row + i < 0 ||
			   startingNode.row - i >= GRID_SIZE || startingNode.row - i < 0) continue;
			if (startingNode.coloumn + 1 < GRID_SIZE) {
				if (observationGraph[startingNode.row - i][startingNode.coloumn + 1] == null) {
					observationGraph[startingNode.row - i][startingNode.coloumn + 1] = new Node((short) (startingNode.x + 1), startingNode.y, (short) (startingNode.coloumn + 1),
																									 (short) (startingNode.row - i), currentObservation[startingNode.row - i][startingNode.coloumn + 1]);
				}
				listOfNodes.add(observationGraph[startingNode.row - i][startingNode.coloumn + 1]);				
			}			
			//Minus
			if(startingNode.coloumn - 1 >= 0) {
				if (observationGraph[startingNode.row - i][startingNode.coloumn - 1] == null) {
					observationGraph[startingNode.row - i][startingNode.coloumn - 1] = new Node((short) (startingNode.x + 1), startingNode.y, (short) (startingNode.coloumn - 1),
							 																			(short) (startingNode.row - i), currentObservation[startingNode.row - i][startingNode.coloumn - 1]);
				}
				listOfNodes.add(observationGraph[startingNode.row - i][startingNode.coloumn - 1]);				
			}
		}
		return listOfNodes;
	}
	
	private List<Node> getRunningReachableNodes(Node startingNode, List<Node> listOfNodes){
		//(bad) Running:
		for(short i = 1; i<= 1; i++) {
			//Plus
			if (startingNode.coloumn + 1 < GRID_SIZE) {
				if (observationGraph[startingNode.row][startingNode.coloumn + i] == null) {
					observationGraph[startingNode.row][startingNode.coloumn + i] = new Node((short) (startingNode.x + i), startingNode.y, (short) (startingNode.coloumn + i),
																									 startingNode.row, currentObservation[startingNode.row][startingNode.coloumn + i]);
				}
				listOfNodes.add(observationGraph[startingNode.row][startingNode.coloumn + 1]);				
			}			
			//Minus
			if(startingNode.coloumn - 1 >= 0) {
				if (observationGraph[startingNode.row][startingNode.coloumn - i] == null) {
					observationGraph[startingNode.row][startingNode.coloumn - i] = new Node((short) (startingNode.x + i), startingNode.y, (short) (startingNode.coloumn - i),
							 																			startingNode.row, currentObservation[startingNode.row][startingNode.coloumn - i]);
				}
				listOfNodes.add(observationGraph[startingNode.row][startingNode.coloumn - 1]);				
			}
		}
		return listOfNodes;
	}
	
	private List<Node> getPolynomialReachableNodes(Node startingNode, List<Node> listOfNodes){		
		
		
		
		
		
		return listOfNodes;
	}
	
	private boolean canMarioStandThere(Node place) {
		return  isOnSolidGround(place) && !isSolid(place.type);
	}
	
	private boolean isOnSolidGround(Node place) {
		return isSolid(currentObservation[place.row+1][place.coloumn]);
	}
	
	private boolean isSolid(byte blockType){
		return blockType != 0;//TODO(*) Fix
	}
	
	
	
	
}
