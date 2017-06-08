package MarioAI.marioMovement;

import java.util.ArrayList;

class XMovementInformation {
	public final float xMovedDistance;
	public final float endSpeed;
	public final int totalTicksXMoved;
	public final ArrayList<Float> xPositions; 
	public final ArrayList<Boolean> pressXButton;
	public final boolean useSuperSpeed;
	private int useNumberOfPosition;
	
	public XMovementInformation(float xMovedDistance, 
										 float endSpeed, 
										 int totalTicksXMoved,  
										 ArrayList<Float> xPositions,
										 ArrayList<Boolean> pressXButton,
										 boolean useSuperSpeed) {
		this.xMovedDistance = xMovedDistance;
		this.endSpeed = endSpeed;
		this.totalTicksXMoved = totalTicksXMoved;
		this.xPositions = xPositions;
		this.pressXButton = pressXButton;
		this.useSuperSpeed = useSuperSpeed;
		
	}
}