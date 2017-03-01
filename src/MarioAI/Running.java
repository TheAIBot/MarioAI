package MarioAI;

import MarioAI.graph.DirectedEdge;
import MarioAI.graph.Node;

public class Running extends DirectedEdge{

	public Running(Node source, Node target) {
		super(source, target);
	}

	@Override
	public float getMaxY() {
		return (float)target.y;
	}

	public float getWeight() {
		return 0;
	}

}
