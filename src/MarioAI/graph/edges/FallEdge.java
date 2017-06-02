package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.nodes.Node;

public class FallEdge extends DirectedEdge {

	public FallEdge(Node source, Node target) {
		super(source, target);
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
	protected int getExtraEdgeHashcode() {
		final int fallType = 2; //it is a fall edge type
		//Its fall length does not matter, only its targets position.
		//Therefore it is not included
		return fallType;
	}

}
