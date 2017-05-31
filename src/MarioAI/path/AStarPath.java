package MarioAI.path;

import java.util.ArrayList;

import MarioAI.graph.edges.DirectedEdge;

public class AStarPath {
	public final ArrayList<DirectedEdge> path;
	public final boolean isBestPath;
	public final int granularity;
	
	public AStarPath(ArrayList<DirectedEdge> path, boolean isBestPath, int granularity) {
		this.path = path;
		this.isBestPath = isBestPath;
		this.granularity = granularity;
	}
}
