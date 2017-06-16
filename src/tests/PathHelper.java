package tests;

import java.util.ArrayList;
import java.util.Collections;

import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
/**
 * @author Andreas
 *
 */
public class PathHelper {
	private static int hashNumber = 0;
	
	public static ArrayList<DirectedEdge> createPath(int startX, int startY, int distanceX, int jumpHeight, int heightDifference, int pathlength, World world, boolean useSuperSpeed) {
		final int[] distanceXArray = new int[pathlength];
		for (int i = 0; i < distanceXArray.length; i++) {
			distanceXArray[i] = distanceX;
		}
		
		final boolean[] useSuperSpeeds = new boolean[pathlength];
		for (int i = 0; i < useSuperSpeeds.length; i++) {
			useSuperSpeeds[i] = useSuperSpeed;
		}		
		
		return createPath(startX, startY, distanceXArray, jumpHeight, heightDifference, pathlength, world, useSuperSpeeds);
	}
	
	public static ArrayList<DirectedEdge> createPath(int startX, int startY, int distanceX, int jumpHeight, int heightDifference, int pathlength, World world, boolean[] useSuperSpeeds) {
		final int[] distanceXArray = new int[pathlength];
		for (int i = 0; i < distanceXArray.length; i++) {
			distanceXArray[i] = distanceX;
		}
		
		return createPath(startX, startY, distanceXArray, jumpHeight, heightDifference, pathlength, world, useSuperSpeeds);
	}
	
	public static ArrayList<DirectedEdge> createPath(int startX, int startY, int[] distanceX, int jumpHeight, int heightDifference, int pathlength, World world, boolean useSuperSpeed) {
		final boolean[] useSuperSpeeds = new boolean[pathlength];
		for (int i = 0; i < useSuperSpeeds.length; i++) {
			useSuperSpeeds[i] = useSuperSpeed;
		}
		
		return createPath(startX, startY, distanceX, jumpHeight, heightDifference, pathlength, world, useSuperSpeeds);
	}
	
	public static ArrayList<DirectedEdge> createPath(int startX, int startY, int[] distanceX, int jumpHeight, int heightDifference, int pathlength, World world, boolean[] useSuperSpeed) {
		return reconstructPath(createPathEndSpeedNode(startX, startY, distanceX, jumpHeight, heightDifference, pathlength, world, useSuperSpeed));
	}
	
	public static SpeedNode createPathEndSpeedNode(int startX, int startY, int distanceX, int jumpHeight, int heightDifference, int pathlength, World world, boolean[] useSuperSpeed) {
		int[] distanceXArray = new int[pathlength];
		for (int i = 0; i < distanceXArray.length; i++) {
			distanceXArray[i] = distanceX;
		}
		return createPathEndSpeedNode(startX, startY, distanceXArray, jumpHeight, heightDifference, pathlength, world, useSuperSpeed);
	}
	
	public static SpeedNode createPathEndSpeedNode(int startX, int startY, int[] distanceX, int jumpHeight, int heightDifference, int pathlength, World world, boolean[] useSuperSpeed) {
		
		final Node startNode = new Node(startX, startY, (byte)0);
		SpeedNode speedNode = createEdgeWithSpeedNode(startNode, null, distanceX[0], heightDifference, jumpHeight, world, useSuperSpeed[0]);
		
		for (int i = 1; i < pathlength; i++) {
			speedNode = createEdgeWithSpeedNode(speedNode.ancestorEdge.target, speedNode, distanceX[i], heightDifference, jumpHeight, world, useSuperSpeed[i]);
		}
		
		return speedNode;
	}
	
	private static SpeedNode createEdgeWithSpeedNode(Node startNode, SpeedNode startSpeedNode, int xMove, int yMove, int jumpHeight, World world, boolean useSuperSpeed) {
		final Node endNode = new Node(startNode.x + xMove, startNode.y + yMove, (byte)0);
		
		DirectedEdge edge;
		if (jumpHeight == 0) {
			edge = new RunningEdge(startNode, endNode, useSuperSpeed);
		}
		else {
			edge = new JumpingEdge(startNode, endNode, startNode.y + Math.min(jumpHeight, 4), useSuperSpeed);
		}
		
		SpeedNode speedNode;
		if (startSpeedNode != null) {
			speedNode = new SpeedNode(endNode, startSpeedNode, edge, getHash(), world);
		}
		else {
			speedNode = new SpeedNode(endNode, edge.source.x, 0, edge, getHash(), world);
		}
		speedNode.use();
		
		return speedNode;
	}
	
	public static ArrayList<DirectedEdge> reconstructPath(SpeedNode currentSpeedNode) {
		if (currentSpeedNode != null) {
			final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
			while (currentSpeedNode != null) {
				path.add(currentSpeedNode.ancestorEdge);
				currentSpeedNode.use();
				
				currentSpeedNode = currentSpeedNode.parent;
			}
			Collections.reverse(path);
			return path;	
		} 
		else {
			return null;
		}
	}
	
	private static int getHash() {
		return hashNumber++;
	}	
}
