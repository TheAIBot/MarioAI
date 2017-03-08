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

	@Override
	public float getTraversedTime(float v0) {
		return (float) (7.010101392f-16.29465006f*v0+31.36548962f*  Math.pow(v0-0.15f,2)-15.03197676f* Math.pow((v0-0.15f),3));
	}

	@Override
	public float getSpeedAfterTraversal(float v0) {
		return (float) (0.1910831330f + 0.2894875880f * v0 + 1.275251311f * Math.pow(v0 - 0.125f,2) - 0.1668494221f * Math.pow(v0 - 0.125f,3));
	}

}
