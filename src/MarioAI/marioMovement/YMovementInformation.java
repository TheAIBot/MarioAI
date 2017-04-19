package MarioAI.marioMovement;

import java.util.ArrayList;

public class YMovementInformation {
	public final int ticksHoldingJump;
	public final int totalTicksJumped;
	public final ArrayList<Float> yPositions;
	
	public YMovementInformation(int ticksHoldingJump, int totalTicksJumped, ArrayList<Float> yPositions) {
		this.ticksHoldingJump = ticksHoldingJump;
		this.totalTicksJumped = totalTicksJumped;
		this.yPositions = yPositions;
	}
}
