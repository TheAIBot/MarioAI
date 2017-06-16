package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.nodes.Node;

/**A directed edge for running.
 * @author jesper
 *
 */
public class RunningEdge extends DirectedEdge{
	/** Initializer for a RunningEdge
	 * @param source The source of the RunningEdge
	 * @param target The target of the RunningEdge
	 * @param useSuperSpeed Whether the edge should use superspeed or not.
	 */
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
}
