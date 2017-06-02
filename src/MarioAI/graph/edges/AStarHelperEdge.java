package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.nodes.Node;

public class AStarHelperEdge extends DirectedEdge {

	public AStarHelperEdge(Node source, Node target) {
		super(source, target);
		hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
	}

	@Override
	public float getMaxY() {
		return 0;
	}

	@Override
	public float getWeight() {
		return 0;
	}

	@Override
	protected int getExtraEdgeHashcode() {
		return 0;
	}

}
