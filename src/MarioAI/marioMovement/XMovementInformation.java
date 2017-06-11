package MarioAI.marioMovement;

import java.util.ArrayList;

class XMovementInformation {
	public final float xMovedDistance;
	public final float endSpeed;
	public final int totalTicksXMoved;
	public final float[] xPositions; 
	public final boolean[] pressXButton;
	public final boolean useSuperSpeed;
	
	public XMovementInformation(float xMovedDistance, 
								float endSpeed, 
								int totalTicksXMoved,  
								float[] xPositions,
								boolean[] pressXButton,
								boolean useSuperSpeed) {
		this.xMovedDistance = xMovedDistance;
		this.endSpeed = endSpeed;
		this.totalTicksXMoved = totalTicksXMoved;
		this.xPositions = xPositions;
		this.pressXButton = pressXButton;
		this.useSuperSpeed = useSuperSpeed;
		
	}
}