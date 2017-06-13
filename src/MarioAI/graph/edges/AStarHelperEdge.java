package MarioAI.graph.edges;

import MarioAI.graph.nodes.Node;

public class AStarHelperEdge extends RunningEdge {

	public AStarHelperEdge(Node source, Node target) {
		super(source, target, true);
	}
	
	@Override
	protected byte getExtraEdgeHashcode() {
		return 0b0000_0001;
	}
}
