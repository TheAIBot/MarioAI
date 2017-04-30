package MarioAI.marioMovement;

import java.util.ArrayList;

public class XMovementInformation {
	public final float xMovedDistance;
	public final float endSpeed;
	public final int totalTicksXMoved;
	public final ArrayList<Float> xPositions; 
	public final ArrayList<Boolean> pressXButton;
	
	public XMovementInformation(float xMovedDistance, 
								float endSpeed, 
								int totalTicksXMoved,  
								ArrayList<Float> xPositions,
								ArrayList<Boolean> pressXButton) {
		this.xMovedDistance = xMovedDistance;
		this.endSpeed = endSpeed;
		this.totalTicksXMoved = totalTicksXMoved;
		this.xPositions = xPositions;
		this.pressXButton = pressXButton;
		
	}
}