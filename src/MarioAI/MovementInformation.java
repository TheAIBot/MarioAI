package MarioAI;

public class MovementInformation {
	private final int xMovementTicks;
	private final float endSpeed;
	private final int ticksHoldingJump;
	private final int totalTicksJumped;
	
	public MovementInformation(int xMovementTicks, float endSpeed, int ticksHoldingJump, int totalTicksJumped) {
		this.xMovementTicks = xMovementTicks;
		this.endSpeed = endSpeed;
		this.ticksHoldingJump = ticksHoldingJump;
		this.totalTicksJumped = totalTicksJumped;
	}
	
	public int getXMovementTime() {
		return xMovementTicks;
	}
	
	public float getEndSpeed() {
		return endSpeed;
	}
	
	public int getTicksHoldingJump() {
		return ticksHoldingJump;
	}
	
	public int getTotalTicksJumped() {
		return totalTicksJumped;
	}
	
	public int getMoveTime() {
		return Math.max(xMovementTicks, totalTicksJumped);
	}
}
