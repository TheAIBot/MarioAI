package MarioAI.marioMovement;

import java.util.ArrayList;

public class YMovementInformation {
	public final int ticksHoldingJump;
	public final int totalTicksJumped;
	public final ArrayList<Float> yPositions;
	public final ArrayList<Boolean> pressYButton;
	
	public YMovementInformation(int ticksHoldingJump, int totalTicksJumped, ArrayList<Float> yPositions, ArrayList<Boolean> pressYButton) {
		this.ticksHoldingJump = ticksHoldingJump;
		this.totalTicksJumped = totalTicksJumped;
		this.yPositions = yPositions;
		this.pressYButton = pressYButton;
	}
}
