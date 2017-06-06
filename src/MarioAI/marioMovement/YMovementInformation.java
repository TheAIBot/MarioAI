package MarioAI.marioMovement;

import java.util.ArrayList;

class YMovementInformation {
	public final int totalTicksJumped;
	public final ArrayList<Float> yPositions;
	public final ArrayList<Boolean> pressYButton;
	
	public YMovementInformation(int totalTicksJumped, ArrayList<Float> yPositions, ArrayList<Boolean> pressYButton) {
		this.totalTicksJumped = totalTicksJumped;
		this.yPositions = yPositions;
		this.pressYButton = pressYButton;
	}
}
