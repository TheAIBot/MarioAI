package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.nodes.Node;

public class Running extends DirectedEdge{

	public Running(Node source, Node target) {
		super(source, target);
		hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
	}

	@Override
	public float getMaxY() {
		return 0;
	}

	public float getWeight() {
		return 0;
	}

	@Override
	protected int getExtraEdgeHashcode() {
		return 0; //0 represents it being a running edge. Nothing else needed.
	}

}
