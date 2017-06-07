package MarioAI.enemySimuation;

import MarioAI.enemySimuation.simulators.EnemySimulator;

public class EnemyCollision {
	public EnemySimulator enemy;
	public int tickForCollision;
	public boolean isStompType = false;
	
	public EnemyCollision(EnemySimulator enemy, int tick) {
		this.enemy = enemy;
		this.tickForCollision = tick;
	}
}
