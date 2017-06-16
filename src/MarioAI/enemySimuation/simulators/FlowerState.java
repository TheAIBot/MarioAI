package MarioAI.enemySimuation.simulators;

/**
 * POD class
 * @author Andreas Gramstrup
 *
 */
public class FlowerState {
	public final int jumpTime;
	public final float y;
	public final float ya;
	
	public FlowerState(int jumpTime, float y, float ya) {
		this.jumpTime = jumpTime;
		this.y = y;
		this.ya = ya;
	}
}
