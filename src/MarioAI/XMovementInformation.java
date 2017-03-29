package MarioAI;

public class XMovementInformation {
	public final float xMovedDistance;
	public final float endSpeed;
	public final int ticks;
	
	public XMovementInformation(float xMovedDistance, float endSpeed, int ticks) {
		this.xMovedDistance = xMovedDistance;
		this.endSpeed = endSpeed;
		this.ticks = ticks;
	}
}
