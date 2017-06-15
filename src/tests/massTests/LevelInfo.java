package tests.massTests;

class LevelInfo {
	private final int difficulty;
	private final int ticksRun;
	private final int howLevelWasEnded;
	private final int livesLost;
	private final int timesAStarHasRun;
	private final int timesAStarDidNotFinish;
	private final long totalTimeUsedByAStar;
	private final int neighborsVisited;
	private final int neighborsAsParents;
	
	public LevelInfo(int difficulty, int ticksRun, int howLevelWasEnded, 
					 int livesLost, int timesAStarHasRun, int timesAStarDidNotFinish,
					 long totalTimeUsedByAStar, int neighborsVisited,
					 int neighborsAsParents) {
		this.difficulty = difficulty;
		this.ticksRun = ticksRun;
		this.howLevelWasEnded = howLevelWasEnded;
		this.livesLost = livesLost;
		this.timesAStarHasRun = timesAStarHasRun;
		this.timesAStarDidNotFinish = timesAStarDidNotFinish;
		this.totalTimeUsedByAStar = totalTimeUsedByAStar;
		this.neighborsVisited = neighborsVisited;
		this.neighborsAsParents = neighborsAsParents;
	}
	
	@Override
	public String toString() {
		return         difficulty + 
				", " + ticksRun + 
				", " + howLevelWasEnded + 
				", " + livesLost + 
				", " + timesAStarHasRun + 
				", " + timesAStarDidNotFinish + 
				", " + totalTimeUsedByAStar + 
				", " + neighborsVisited + 
				", " + neighborsAsParents;
	}
}
