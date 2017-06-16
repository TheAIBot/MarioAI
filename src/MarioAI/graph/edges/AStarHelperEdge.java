package MarioAI.graph.edges;

import MarioAI.graph.nodes.Node;

/**
 * Should only be used in AStar
 * @author Andreas Gramstrup
 *
 */
public class AStarHelperEdge extends RunningEdge {

	public AStarHelperEdge(Node source, Node target) {
		super(source, target, true);
	}
	
	@Override
	protected byte getExtraEdgeHashcode() {
		return 0b0000_0001;
	}
}
