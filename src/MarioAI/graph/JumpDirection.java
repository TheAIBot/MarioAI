package MarioAI.graph;

public enum JumpDirection {
	LEFT, RIGHT, LEFT_UPWARDS,LEFT_DOWNWARDS,RIGHT_UPWARDS,RIGHT_DOWNWARDS;

	public boolean isUpwardsType() {
		if (this == LEFT_UPWARDS || this == RIGHT_UPWARDS) {
			return true;
		} else return false;
	}
	
	public JumpDirection getIsLeftOrRightType() {
		if (isLeftType()) {
			return LEFT;
		} else return RIGHT;
	}
	public boolean isLeftType() {
		if (this == LEFT || this == LEFT_UPWARDS || this == LEFT_DOWNWARDS) {
			return true;
		} else return false;
	}
}
