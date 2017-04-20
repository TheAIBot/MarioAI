package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.nodes.Node;

public class RunningEdge extends DirectedEdge{

	public RunningEdge(Node source, Node target) {
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
