package MarioAI;

public class MovementInformation {
	private final int xMovementTicks;
	private final int jumpTicks;
	private final float endSpeed;
	
	public MovementInformation(int xMovementTicks, int jumpTicks, float endSpeed) {
		this.xMovementTicks = xMovementTicks;
		this.jumpTicks = jumpTicks;
		this.endSpeed = endSpeed;
	}
	
	public int getXMovementTime() {
		return xMovementTicks;
	}
	
	public int getJumpTime() {
		return jumpTicks;
	}
	
	public float getEndSpeed() {
		return endSpeed;
	}
	
	public int getMoveTime() {
		return Math.max(xMovementTicks, jumpTicks);
	}
}
