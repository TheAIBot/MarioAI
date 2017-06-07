package MarioAI.marioMovement;

class YMovementInformation {
	public final int totalTicksJumped;
	public final float[] yPositions;
	public final boolean[] pressYButton;
	
	public YMovementInformation(int totalTicksJumped, float[] yPositions, boolean[] pressYButton) {
		this.totalTicksJumped = totalTicksJumped;
		this.yPositions = yPositions;
		this.pressYButton = pressYButton;
	}
}
