package MarioAI.enemySimuation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import MarioAI.World;
import MarioAI.enemySimuation.simulators.BulletBillSimulator;
import MarioAI.enemySimuation.simulators.EnemySimulator;
import MarioAI.enemySimuation.simulators.FlowerEnemy;
import MarioAI.enemySimuation.simulators.ShellSimulator;
import MarioAI.enemySimuation.simulators.WalkingEnemySimulator;
import MarioAI.graph.nodes.StateNode;
import MarioAI.path.PathCreator;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.engine.sprites.Sprite;

public class EnemyPredictor {
	
	public static final int FLOATS_PER_ENEMY = 3;
	public static final int TYPE_OFFSET = 0;
	public static final int X_OFFSET = 1;
	public static final int Y_OFFSET = 2;
	public static final int ACCEPTED_POSITION_DEVIATION = 1;
	
	private LevelScene levelScene;
	private HashMap<Integer, ArrayList<Point2D.Float>> oldEnemyInfo;
	private final ArrayList<EnemySimulator> potentialCorrectSimulations = new ArrayList<EnemySimulator>();
	private ArrayList<EnemySimulator> verifiedEnemySimulations = new ArrayList<EnemySimulator>();
	private boolean newEnemySpawned = false;
	
	public void intialize(LevelScene levelScene) {
		this.levelScene = levelScene;
		FlowerEnemy.createStateTable(levelScene);
	}
	
	public boolean hasEnemy(float marioX, final float marioY, final float marioWidth, final int marioHeight, final int time, boolean movingDownwards, boolean isOrWasNotOnGround, EnemyCollision firstCollision, long livingEnemies) {
		
		for (int i = 0; i < verifiedEnemySimulations.size(); i++) {
			if (!isEnemyAlive(livingEnemies, i)) {
				continue;
			}
			EnemySimulator enemySimulation = verifiedEnemySimulations.get(i);
			
			final Point2D.Float enemyPositionInPixels = enemySimulation.getPositionAtTime(time + 1);
			
			final float marioXInPixels = marioX * World.PIXELS_PER_BLOCK;
			final float marioYInPixels = marioY * World.PIXELS_PER_BLOCK;
			final float marioHeightInPixels = marioHeight * World.PIXELS_PER_BLOCK;
			
			if (enemySimulation.collideCheck(enemyPositionInPixels.x, enemyPositionInPixels.y, marioXInPixels, marioYInPixels, marioHeightInPixels)) {
				//Now to check if he also stomps the enemy:
				if(enemySimulation.stomp(time, marioHeight, marioXInPixels, marioYInPixels, movingDownwards, isOrWasNotOnGround)){ //Discerns if it is a stomp type collision or not.
					firstCollision.isStompType = true;
				}				
				return true;
			}
		}
		return false;	
	}
	
	public void updateEnemies(final float[] enemyInfo) {
		final HashMap<Integer, ArrayList<Point2D.Float>> sortedEnemyInfo = sortEnemiesByType(enemyInfo);
		
		updateSimulations();
		
		removeDeadEnemies(sortedEnemyInfo);
		
		//System.out.println(verifiedEnemySimulations.size());
		addCorrectSimulations(sortedEnemyInfo);
		
		addPotentialCorrectSimulations(sortedEnemyInfo);
		
		oldEnemyInfo = sortedEnemyInfo;
	}
	
	private HashMap<Integer, ArrayList<Point2D.Float>> sortEnemiesByType(final float[] enemyInfo) {
		final HashMap<Integer, ArrayList<Point2D.Float>> byType = new HashMap<Integer, ArrayList<Point2D.Float>>();
		for (int i = 0; i < enemyInfo.length; i += FLOATS_PER_ENEMY) {
			final int kind = (int) enemyInfo[i + TYPE_OFFSET];
			final float x  =       enemyInfo[i + X_OFFSET];
			final float y  =       enemyInfo[i + Y_OFFSET];
			
			final Point2D.Float enemyPos = new Point2D.Float(x, y);
			
			if (!byType.containsKey(kind)) {
				byType.put(kind, new ArrayList<Point2D.Float>());
			}
			
			byType.get(kind).add(enemyPos);
		}
		return byType;
	}
	
	private void updateSimulations() {
		for (EnemySimulator enemySimulation : verifiedEnemySimulations) {
			enemySimulation.moveTimeForward();
		}
	}
	
	private void removeDeadEnemies(final HashMap<Integer, ArrayList<Point2D.Float>> enemyInfo) {
		final ArrayList<EnemySimulator> notDeletedSimulations = new ArrayList<EnemySimulator>();
		for (EnemySimulator enemySimulation : verifiedEnemySimulations) {
			final int kind = enemySimulation.getKind();
			final Point2D.Float simulationPosition = enemySimulation.getCurrentPosition();
			final float simulationX = simulationPosition.x;
			final float simulationY = simulationPosition.y;
			
			final ArrayList<Point2D.Float> enemyPositions = enemyInfo.get(kind);
			
			if (enemyPositions != null) {
				Point2D.Float simulatedPosition = null;
				float leastDifference = Integer.MAX_VALUE;
				
				for (Point2D.Float enemyPosition : enemyPositions) {
					final float deltaX = Math.abs(enemyPosition.x - simulationX);
					final float deltaY = Math.abs(enemyPosition.y - simulationY);
					
					final float difference = deltaX + deltaY;
					
					if (deltaX <= ACCEPTED_POSITION_DEVIATION && 
						deltaY <= ACCEPTED_POSITION_DEVIATION && 
						difference < leastDifference) {
						simulatedPosition = enemyPosition;
						leastDifference = difference;
						break;
					}
				}
				
				if (simulatedPosition != null) {
					enemyPositions.remove(simulatedPosition);
					//enemySimulation.setX(simulatedPosition.x);
					//enemySimulation.setY(simulatedPosition.y);
					
					notDeletedSimulations.add(enemySimulation);
				}
			}
		}
		
		verifiedEnemySimulations = notDeletedSimulations;	
	}
	
	private void addCorrectSimulations(final HashMap<Integer, ArrayList<Point2D.Float>> enemyInfo) {
		//keep true until set false;
		newEnemySpawned = newEnemySpawned || false;
		for (EnemySimulator enemySimulation : potentialCorrectSimulations) {			
			final int kind = enemySimulation.getKind();
			final Point2D.Float enemyPosition = enemySimulation.getCurrentPosition();
			final float x = enemyPosition.x;
			final float y = enemyPosition.y;
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
					newEnemySpawned = true;
				}
			}
		}
	}
	
	private void addPotentialCorrectSimulations(final HashMap<Integer, ArrayList<Point2D.Float>> enemyInfo) {
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
						
						if (deltaX <= World.PIXELS_PER_BLOCK && 
							deltaY <= World.PIXELS_PER_BLOCK) {
							final EnemySimulator potentialSimulation = getSimulator(x1, y1, xa, ya, kind);
							//the xa and ya are 1 tick too old so they are updated here
							potentialSimulation.moveEnemy();
							potentialSimulation.moveTimeForward();
							//but the position isn't too old so it's set here again
							potentialSimulation.setX(x1);
							potentialSimulation.setY(y1);
							
							potentialCorrectSimulations.add(potentialSimulation);
						}
					}
				}
			}
		}
	}
	
	private EnemySimulator getSimulator(final float x, final float y, final float xa , final float ya, final int kind) {
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
		
	private boolean canKindFly(final int kind) {
        switch (kind) {
        case Sprite.KIND_GOOMBA_WINGED:
        case Sprite.KIND_RED_KOOPA_WINGED:
        case Sprite.KIND_GREEN_KOOPA_WINGED:
        case Sprite.KIND_SPIKY_WINGED:
        	return true;
		default:
			return false;
		}
	} 
	
	private int getTypeFromKind(final int kind) {
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
	
	public void moveIntoFuture(final int timeToMove) {
		for (int i = 0; i < timeToMove; i++) {
			updateSimulations();
		}
	}
	
	public void moveIntoPast(final int timeToMove) {
		for (int i = 0; i < timeToMove; i++) {
			for (EnemySimulator enemySimulation : verifiedEnemySimulations) {
				enemySimulation.moveTimeBackwards();
			}
		}
	}
	
	public void syncFrom(EnemyPredictor correctPredictor) {
		verifiedEnemySimulations = new ArrayList<EnemySimulator>();
		for (EnemySimulator enemySimulator : correctPredictor.verifiedEnemySimulations) {
			verifiedEnemySimulations.add(enemySimulator.copy());
		}
	}
	
	public ArrayList<EnemySimulator> getEnemies() {
		return verifiedEnemySimulations;
	}
	
	public boolean hasNewEnemySpawned() {
		return newEnemySpawned;
	}

	public void resetNewEnemySpawned() {
		newEnemySpawned = false;
	}
	
	public boolean isEnemyAlive(long livingEnemies, int enemyIndex) {
		return ((livingEnemies >> enemyIndex) & 1) == 1;
	}
	
	public static void hitEnemy(EnemyCollision firstCollision, StateNode state) {
		// TODO Temporarily just kills the enemy.
		state.livingEnemies &= ~(1 << firstCollision.indexEnemy);
		
	}

	public long getCurrentLivingEnemies() {
		return -1L;
	}
}
