package MarioAI.enemy;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import MarioAI.enemy.simulators.BulletBillSimulator;
import MarioAI.enemy.simulators.EnemySimulator;
import MarioAI.enemy.simulators.FlowerEnemy;
import MarioAI.enemy.simulators.ShellSimulator;
import MarioAI.enemy.simulators.WalkingEnemySimulator;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.engine.sprites.Sprite;

public class EnemyPredictor {
	
	private static final int FLOATS_PER_ENEMY = 3;
	private static final int TYPE_OFFSET = 0;
	private static final int X_OFFSET = 1;
	private static final int Y_OFFSET = 2;
	private static final int BLOCK_PIXEL_SIZE = 16;
	private static final int ACCEPTED_POSITION_DEVIATION = 1;
	
	private LevelScene levelScene;
	private HashMap<Integer, ArrayList<Point2D.Float>> oldEnemyInfo;
	private ArrayList<EnemySimulator> potentialCorrectSimulations = new ArrayList<EnemySimulator>();
	private ArrayList<EnemySimulator> verifiedEnemySimulations = new ArrayList<EnemySimulator>();
	
	public void intialize(LevelScene levelScene) {
		this.levelScene = levelScene;
	}
	
	public boolean hasEnemy(int x, int y, int time) {
		for (EnemySimulator enemySimulation : verifiedEnemySimulations) {
			final Point2D.Float enemyPosition = enemySimulation.getPositionAtTime(time);
			final int enemyX = (int) (enemyPosition.x / BLOCK_PIXEL_SIZE);
			final int enemyY = (int) (enemyPosition.y / BLOCK_PIXEL_SIZE);
			
			if (enemyX == x && 
				enemyY == y) {
				return true;
			}
		}
		return false;
	}
	
	public void updateEnemies(float[] enemyInfo) {
		final HashMap<Integer, ArrayList<Point2D.Float>> sortedEnemyInfo = sortEnemiesByType(enemyInfo);
		
		removeDeadEnemies(sortedEnemyInfo);
		
		updateSimulations();
		
		addCorrectSimulations(sortedEnemyInfo);
		
		addPotentialCorrectSimulations(sortedEnemyInfo);
		
		oldEnemyInfo = sortedEnemyInfo;
	}
	
	private HashMap<Integer, ArrayList<Point2D.Float>> sortEnemiesByType(float[] enemyInfo) {
		HashMap<Integer, ArrayList<Point2D.Float>> byType = new HashMap<Integer, ArrayList<Point2D.Float>>();
		for (int i = 0; i < enemyInfo.length; i += FLOATS_PER_ENEMY) {
			final int kind = (int) enemyInfo[i + TYPE_OFFSET];
			final float x = enemyInfo[i + X_OFFSET];
			final float y = enemyInfo[i + Y_OFFSET];
			
			final Point2D.Float enemyPos = new Point2D.Float(x, y);
			
			if (byType.get(kind) == null) {
				byType.put(kind, new ArrayList<Point2D.Float>());
			}
			
			byType.get(kind).add(enemyPos);
		}
		return byType;
	}
	
	private void updateSimulations() {
		for (EnemySimulator enemySimulation : verifiedEnemySimulations) {
			enemySimulation.moveTime();
		}
	}
	
	private void removeDeadEnemies(HashMap<Integer, ArrayList<Point2D.Float>> enemyInfo) {
		ArrayList<EnemySimulator> notDeletedSimulations = new ArrayList<EnemySimulator>();
		for (EnemySimulator enemySimulation : potentialCorrectSimulations) {
			final int kind = enemySimulation.getKind();
			final float simulationX = enemySimulation.getX();
			final float simulationY = enemySimulation.getY();
			
			final ArrayList<Point2D.Float> enemyPositions = enemyInfo.get(kind);
			
			if (enemyPositions != null) {
				Point2D.Float simulatedPosition = null;
				
				for (Point2D.Float enemyPosition : enemyPositions) {
					final float deltaX = Math.abs(enemyPosition.x - simulationX);
					final float deltaY = Math.abs(enemyPosition.y - simulationY);
					
					if (deltaX <= ACCEPTED_POSITION_DEVIATION && 
						deltaY <= ACCEPTED_POSITION_DEVIATION) {
						simulatedPosition = enemyPosition;
						break;
					}
				}
				
				if (simulatedPosition != null) {
					enemyPositions.remove(simulatedPosition);
					
					notDeletedSimulations.add(enemySimulation);
				}	
			}
		}
		
		verifiedEnemySimulations = notDeletedSimulations;
	}
	
	private void addCorrectSimulations(HashMap<Integer, ArrayList<Point2D.Float>> enemyInfo) {
		for (EnemySimulator enemySimulation : potentialCorrectSimulations) {
			enemySimulation.moveEnemy();
			
			final int kind = enemySimulation.getKind();
			final float x = enemySimulation.getX();
			final float y = enemySimulation.getY();
			final ArrayList<Point2D.Float> enemyPositions = enemyInfo.get(kind); 
			
			if (enemyPositions != null) {
				Point2D.Float foundPoint = null;
				
				for (Point2D.Float point : enemyPositions) {					
					final float deltaX = Math.abs(x - point.x);
					final float deltaY = Math.abs(y - point.y);
					
					if (deltaX <= ACCEPTED_POSITION_DEVIATION && 
						deltaY <= ACCEPTED_POSITION_DEVIATION) {
						foundPoint = point;
						break;
					}
				}
				
				if (foundPoint != null) {
					enemyPositions.remove(foundPoint);
					verifiedEnemySimulations.add(enemySimulation);
				}
			}
		}
	}
	
	private void addPotentialCorrectSimulations(HashMap<Integer, ArrayList<Point2D.Float>> enemyInfo) {
		potentialCorrectSimulations.clear();
		if (oldEnemyInfo != null) {
			for (Entry<Integer, ArrayList<Point2D.Float>> keyValueSet : enemyInfo.entrySet()) {
				final int kind = keyValueSet.getKey();
				final ArrayList<Point2D.Float> enemyPositions = keyValueSet.getValue();
				
				final ArrayList<Point2D.Float> oldEnemyPositions = oldEnemyInfo.get(kind);
				if (oldEnemyPositions == null) {
					continue;
				}
				
				for (int i = 0; i < enemyPositions.size(); i++) {
					for (int z = 0; z < oldEnemyPositions.size(); z++) {
						final int HALF_BLOCK = BLOCK_PIXEL_SIZE / 2;
						
						final Point2D.Float p1 = enemyPositions.get(i);
						final float x1 = p1.x;
						final float y1 = p1.y;
						
						final Point2D.Float p2 = oldEnemyPositions.get(z);
						final float x2 = p2.x;
						final float y2 = p2.y;
						
						final float xa = x1 - x2;
						final float ya = y1 - y2;
						
						final float deltaX = Math.abs(xa);
						final float deltaY = Math.abs(ya);
						
						if (deltaX <= HALF_BLOCK && 
							deltaY <= BLOCK_PIXEL_SIZE) {
							potentialCorrectSimulations.add(getSimulator(x1, y1, xa, ya, kind));
						}
					}
				}
			}
		}
	}
	
	private EnemySimulator getSimulator(float x, float y, float xa , float ya, int kind) {
		switch (kind) {
		case Sprite.KIND_BULLET_BILL:
			final int direction = (xa > 0) ? 1 : -1;
			return new BulletBillSimulator(x, y, direction, kind);
		case Sprite.KIND_ENEMY_FLOWER:
			return new FlowerEnemy(levelScene, x, y, ya);
		case Sprite.KIND_SHELL:
			return new ShellSimulator(levelScene, x, y, xa, ya);
		default:
			final int type = getTypeFromKind(kind);
			final boolean winged = canKindFly(kind);
			return new WalkingEnemySimulator(levelScene, x, y, xa, ya, type, kind, winged);
		}
	}
		
	private boolean canKindFly(int kind) {
        switch (kind) {
        case 5:
        case 7:
        case 3:
        case 10:
        	return true;
		default:
			return false;
		}
	} 
	
	private int getTypeFromKind(int kind) {
        switch (kind) {
		case Sprite.KIND_GOOMBA:
		case Sprite.KIND_GOOMBA_WINGED:
			return Enemy.ENEMY_GOOMBA;
		case Sprite.KIND_RED_KOOPA:
		case Sprite.KIND_RED_KOOPA_WINGED:
			return Enemy.ENEMY_RED_KOOPA;
		case Sprite.KIND_GREEN_KOOPA:
		case Sprite.KIND_GREEN_KOOPA_WINGED:
			return Enemy.ENEMY_GREEN_KOOPA;
		case Sprite.KIND_SPIKY:
		case Sprite.KIND_SPIKY_WINGED:
			return Enemy.ENEMY_SPIKY;
		}
		throw new Error("Unkown kind: " + kind);
	}
}
