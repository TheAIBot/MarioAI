package MarioAI.enemySimuation;

import MarioAI.enemySimuation.simulators.EnemySimulator;

public class EnemyCollision {
	public EnemySimulator enemy;
	public int tickForCollision;
	public boolean isStompType = false;
	public int indexEnemy;
	
	public EnemyCollision(EnemySimulator enemy, int tick, int indexEnemy) {
		this.enemy = enemy;
		this.tickForCollision = tick;
		this.indexEnemy = indexEnemy;
	}
	
	public EnemyCollision() {
		
	}
}
