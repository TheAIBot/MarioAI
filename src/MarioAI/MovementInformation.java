package MarioAI;

public class MovementInformation {
	private final XMovementInformation xMoveInfo;
	private final int ticksHoldingJump;
	private final int totalTicksJumped;
	
	public MovementInformation(XMovementInformation xMoveInfo, int ticksHoldingJump, int totalTicksJumped) {
		this.xMoveInfo = xMoveInfo;
		this.ticksHoldingJump = ticksHoldingJump;
		this.totalTicksJumped = totalTicksJumped;
	}
	
	public int getXMovementTime() {
		return xMoveInfo.ticks;
	}
	
	public float getXMovementDistance() {
		return xMoveInfo.xMovedDistance;
	}
	
	public float getEndSpeed() {
		return xMoveInfo.endSpeed;
	}
	
	public int getTicksHoldingJump() {
		return ticksHoldingJump;
	}
	
	public int getTotalTicksJumped() {
		return totalTicksJumped;
	}
	
	public int getMoveTime() {
		return Math.max(xMoveInfo.ticks, totalTicksJumped);
	}
}
