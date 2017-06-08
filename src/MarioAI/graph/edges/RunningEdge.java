package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.nodes.Node;

public class RunningEdge extends DirectedEdge{

	public RunningEdge(Node source, Node target, boolean useSuperSpeed) {
		super(source, target, useSuperSpeed);
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
	protected byte getExtraEdgeHashcode() {
		return 0b0000_0000; //0 represents it being a running edge. Nothing else needed.
	}

	@Override
	public DirectedEdge getStompVersion(Node targetNode) {
		return new RunningEdge(source, targetNode, useSuperSpeed);
	}
}
