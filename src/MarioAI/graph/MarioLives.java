package MarioAI.graph;

public enum MarioLives {
	TALL_WHITE(3), TALL_NORMAL(2), SMALL(1);
	
	public int lives;
	
	MarioLives(int lives) {
		this.lives = lives;
	}
}
