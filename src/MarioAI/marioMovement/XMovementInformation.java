package MarioAI.marioMovement;

public class XMovementInformation {
	public final float xMovedDistance;
	public final float endSpeed;
	public final int ticks;
	public final int ticksDeaccelerating;
	public final int ticksAccelerating;
	
	public XMovementInformation(float xMovedDistance, float endSpeed, int ticks, int ticksDeaccelerating, int ticksAccelerating) {
		this.xMovedDistance = xMovedDistance;
		this.endSpeed = endSpeed;
		this.ticks = ticks;
		this.ticksDeaccelerating = ticksDeaccelerating;
		this.ticksAccelerating = ticksAccelerating;
	}
}
