package MarioAI.enemy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import MarioAI.enemy.simulators.EnemySimulator;
import MarioAI.enemy.simulators.WalkingEnemySimulator;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Enemy;

public class EnemyPredictor {
	
	private static final int FLOATS_PER_ENEMY = 3;
	private static final int TYPE_OFFSET = 0;
	private static final int X_OFFSET = 1;
	private static final int Y_OFFSET = 2;
	private static final int BLOCK_PIXEL_SIZE = 16;
	private static final int ACCEPTED_POSITION_DEVIATION = 1;
	
	private LevelScene levelScene;
	private HashMap<Integer, ArrayList<Point>> oldEnemyInfo;
	private ArrayList<EnemySimulator> potentialCorrectSimulations = new ArrayList<EnemySimulator>();
	private ArrayList<EnemySimulator> verifiedEnemySimulations = new ArrayList<EnemySimulator>();
	
	public void intialize(LevelScene levelScene) {
		this.levelScene = levelScene;
	}
	
	public boolean hasEnemy(int x, int y, int time) {
		for (EnemySimulator enemySimulation : verifiedEnemySimulations) {
			final Point enemyPosition = enemySimulation.getPositionAtTime(time);
			final int enemyX = enemyPosition.x / BLOCK_PIXEL_SIZE;
			final int enemyY = enemyPosition.y / BLOCK_PIXEL_SIZE;
			
			if (enemyX == x && 
				enemyY == y) {
				return true;
			}
		}
		return false;
	}
	
	public void updateEnemies(float[] enemyInfo) {
		final HashMap<Integer, ArrayList<Point>> sortedEnemyInfo = sortEnemiesByType(enemyInfo);
		
		removeDeadEnemies(sortedEnemyInfo);
		
		updateSimulations();
		
		addCorrectSimulations(sortedEnemyInfo);
		
		addPotentialCorrectSimulations(sortedEnemyInfo);
		
		oldEnemyInfo = sortedEnemyInfo;
	}
	
	private HashMap<Integer, ArrayList<Point>> sortEnemiesByType(float[] enemyInfo) {
		HashMap<Integer, ArrayList<Point>> byType = new HashMap<Integer, ArrayList<Point>>();
		for (int i = 0; i < enemyInfo.length; i += FLOATS_PER_ENEMY) {
			final int kind = (int) enemyInfo[i + TYPE_OFFSET];
			final float x = enemyInfo[i + X_OFFSET];
			final float y = enemyInfo[i + Y_OFFSET];
			
			final Point enemyPos = new Point((int)(x * BLOCK_PIXEL_SIZE), (int)(y * BLOCK_PIXEL_SIZE));
			
			if (byType.get(kind) == null) {
				byType.put(kind, new ArrayList<Point>());
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
	
	private void removeDeadEnemies(HashMap<Integer, ArrayList<Point>> enemyInfo) {
		ArrayList<EnemySimulator> notDeletedSimulations = new ArrayList<EnemySimulator>();
		for (EnemySimulator enemySimulation : potentialCorrectSimulations) {
			final int kind = enemySimulation.getKind();
			final int simulationX = enemySimulation.getX();
			final int simulationY = enemySimulation.getY();
			
			final ArrayList<Point> enemyPositions = enemyInfo.get(kind);
			
			Point simulatedPosition = null;
			
			for (Point enemyPosition : enemyPositions) {
				final int deltaX = Math.abs(enemyPosition.x - simulationX);
				final int deltaY = Math.abs(enemyPosition.y - simulationY);
				
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
		
		verifiedEnemySimulations = notDeletedSimulations;
	}
	
	private void addCorrectSimulations(HashMap<Integer, ArrayList<Point>> enemyInfo) {
		for (EnemySimulator enemySimulation : potentialCorrectSimulations) {
			enemySimulation.move();
			
			final int kind = enemySimulation.getKind();
			final int x = enemySimulation.getX();
			final int y = enemySimulation.getY();
			final ArrayList<Point> enemyPositions = enemyInfo.get(kind); 
			
			if (enemyPositions != null) {
				Point foundPoint = null;
				
				for (Point point : enemyPositions) {					
					final int deltaX = Math.abs(x - point.x);
					final int deltaY = Math.abs(y - point.y);
					
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
	
	private void addPotentialCorrectSimulations(HashMap<Integer, ArrayList<Point>> enemyInfo) {
		potentialCorrectSimulations.clear();
		if (oldEnemyInfo != null) {
			for (Entry<Integer, ArrayList<Point>> keyValueSet : enemyInfo.entrySet()) {
				final int kind = keyValueSet.getKey();
				final ArrayList<Point> enemyPositions = keyValueSet.getValue();
				
				final ArrayList<Point> oldEnemyPositions = oldEnemyInfo.get(kind);
				if (oldEnemyPositions == null) {
					continue;
				}
				
				for (int i = 0; i < enemyPositions.size(); i++) {
					for (int z = 0; z < oldEnemyPositions.size(); z++) {
						final int HALF_BLOCK = BLOCK_PIXEL_SIZE / 2;
						
						final Point p1 = enemyPositions.get(i);
						final int x1 = p1.x;
						final int y1 = p1.y;
						
						final Point p2 = oldEnemyPositions.get(z);
						final int x2 = p2.x;
						final int y2 = p2.y;
						
						final int xa = x1 - x2;
						final int ya = y1 - y2;
						
						final int deltaX = Math.abs(xa);
						final int deltaY = Math.abs(ya);
						
						if (deltaX <= HALF_BLOCK && 
							deltaY <= BLOCK_PIXEL_SIZE) {
							final int type = getTypeFromKind(kind);
							final boolean winged = canKindFly(kind);
							final WalkingEnemySimulator enemySimulation = new WalkingEnemySimulator(levelScene, x1, y1, xa, ya, type, kind, winged);
							
							potentialCorrectSimulations.add(enemySimulation);
						}
					}
				}
				
				
			}
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
		case 2:
		case 3:
			return Enemy.ENEMY_GOOMBA;
		case 4:
		case 5:
			return Enemy.ENEMY_RED_KOOPA;
		case 6:
		case 7:
			return Enemy.ENEMY_GREEN_KOOPA;
		case 9:
		case 10:
			return Enemy.ENEMY_SPIKY;
		case 11:
			return Enemy.ENEMY_FLOWER;
		}
		throw new Error("Unkown kind: " + kind);
	}
}
