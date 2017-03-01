package MarioAI;

import MarioAI.graph.DirectedEdge;
import MarioAI.graph.Node;

public class Running extends DirectedEdge{
/*
	private final int motionTypeID = 1;

	public Running() {
		// TODO Auto-generated constructor stub
	}
	
	public int motionTypeID() {
		return motionTypeID;
	}

	public int getXAccelleration() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getYAccelleration() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void getActionPatern() {
		// TODO Auto-generated method stub
		
	}

	public int getTimespan() {
		// TODO Auto-generated method stub
		return 0;
	}
	*/

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
