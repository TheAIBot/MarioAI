package tests;

import java.util.ArrayList;

import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;

public class PathHelper {
	private static int hashNumber = 0;
	
	public static ArrayList<DirectedEdge> createPath(int startX, int startY, int distanceX, int jumpHeight, int heightDifference, int pathlength, World world) {
		int[] distanceXArray = new int[pathlength];
		for (int i = 0; i < distanceXArray.length; i++) {
			distanceXArray[i] = distanceX;
		}
		return createPath(startX, startY, distanceXArray, jumpHeight, heightDifference, pathlength, world);
	}
	
	public static ArrayList<DirectedEdge> createPath(int startX, int startY, int[] distanceX, int jumpHeight, int heightDifference, int pathlength, World world) {
		final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
		
		final Node startNode = new Node(startX, startY, (byte)0);
		SpeedNode speedNode = createEdgeWithSpeedNode(startNode, null, distanceX[0], heightDifference, jumpHeight, world);
		path.add(speedNode.ancestorEdge);
		
		for (int i = 1; i < pathlength; i++) {
			speedNode = createEdgeWithSpeedNode(speedNode.ancestorEdge.target, speedNode, distanceX[i], heightDifference, jumpHeight, world);
			speedNode.use();
			path.add(speedNode.ancestorEdge);
		}
		
		return path;
	}
	
	private static SpeedNode createEdgeWithSpeedNode(Node startNode, SpeedNode startSpeedNode, int xMove, int yMove, int jumpHeight, World world) {
		final Node endNode = new Node(startNode.x + xMove, startNode.y + yMove, (byte)0);
		
		DirectedEdge edge;
		if (jumpHeight == 0) {
			edge = new RunningEdge(startNode, endNode);
		}
		else {
			edge = new JumpingEdge(startNode, endNode, startNode.y + Math.min(jumpHeight, 4));
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
	
	private static int getHash() {
		return hashNumber++;
	}	
}
