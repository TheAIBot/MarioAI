package MarioAI.path;

import java.util.ArrayList;
import java.util.Collections;

import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.SpeedNode;

/**
 * 
 * @author Andreas Gramstrup
 *
 */
class AStarPath {
	public final SpeedNode pathEnd;
	public final ArrayList<DirectedEdge> path;
	public final boolean isBestPath;
	public final int granularity;
	
	public AStarPath(SpeedNode pathEnd, boolean isBestPath, int granularity) {
		this.pathEnd = pathEnd;
		this.path = reconstructPath(pathEnd);
		this.isBestPath = isBestPath;
		this.granularity = granularity;
	}
	
	private ArrayList<DirectedEdge> reconstructPath(SpeedNode currentSpeedNode) {
		if (currentSpeedNode != null) {
			final ArrayList<DirectedEdge> path = new ArrayList<DirectedEdge>();
			while (currentSpeedNode.parent != null) {
				path.add(currentSpeedNode.ancestorEdge);
				
				currentSpeedNode = currentSpeedNode.parent;
			}
			Collections.reverse(path);
			
			if (path.size() > 0) {
				path.remove((path.size() - 1));
			}
			if (path.size() == 0) {
				return null;
			}
			return path;	
		} 
		else {
			return null;
		}
	}
	
	public void usePath() {
		if (pathEnd != null) {
			SpeedNode currentSpeedNode = pathEnd.parent;
			while (currentSpeedNode != null && 
				   currentSpeedNode.parent != null) {
				currentSpeedNode.use();
				currentSpeedNode = currentSpeedNode.parent;
			}
		}
	}
}
