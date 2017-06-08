package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.nodes.Node;

public class FallEdge extends DirectedEdge {

	public FallEdge(Node source, Node target, boolean useSuperSpeed) {
		super(source, target, useSuperSpeed);
		hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
	}

	@Override
	public float getMaxY() {
		return 0;
	}

	@Override
	public float getWeight() {
		return 0.1f;
	}

	@Override
	protected byte getExtraEdgeHashcode() {
		//Its fall length does not matter, only its targets position.
		//Therefore it is not included
		return 0b0000_0010;
	}

	@Override
	public DirectedEdge getStompVersion(Node targetNode) {
		return new FallEdge(source, targetNode, useSuperSpeed);
	}

}
