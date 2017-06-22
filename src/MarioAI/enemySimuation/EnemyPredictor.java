package MarioAI.enemySimuation;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import MarioAI.World;
import MarioAI.enemySimuation.simulators.BulletBillSimulator;
import MarioAI.enemySimuation.simulators.BulletBillTower;
import MarioAI.enemySimuation.simulators.EnemySimulator;
import MarioAI.enemySimuation.simulators.FlowerEnemy;
import MarioAI.enemySimuation.simulators.ShellSimulator;
import MarioAI.enemySimuation.simulators.WalkingEnemySimulator;
import MarioAI.path.PathCreator;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.engine.sprites.Sprite;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Can keep track of enemies by creating and removing simulations of 
 * those enemies.
 * @author Andreas Gramstrup
 *
 */
public class EnemyPredictor {
	
	public static final int FLOATS_PER_ENEMY = 3;
	public static final int TYPE_OFFSET = 0;
	public static final int X_OFFSET = 1;
	public static final int Y_OFFSET = 2;
	public static final int ACCEPTED_POSITION_DEVIATION = 1;
	
	private LevelScene levelScene;
	//Stores the position of the enemies last time the method update was called
	private Int2ObjectOpenHashMap<ArrayList<Point2D.Float>> oldEnemyInfo;
	private final ArrayList<EnemySimulator> potentialCorrectSimulations = new ArrayList<EnemySimulator>();
	private ArrayList<EnemySimulator> verifiedEnemySimulations = new ArrayList<EnemySimulator>();
	private ArrayList<BulletBillTower> bulletTowers = new ArrayList<BulletBillTower>();
	//is true if a new enemy has spawned since last time this variable was set to false
	private boolean newEnemySpawned = false;
	
	public void intialize(LevelScene levelScene) {
		this.levelScene = levelScene;
		FlowerEnemy.createStateTable(levelScene);
	}
	
	/**
	 * Returns true if mario collides with an enemy or false if not
	 * @param marioX marios x position
	 * @param marioY marios y position
	 * @param marioHeight marios current height
	 * @param time time in ticks into the future from this current tick that the 
	 * collision detection should be checked in
	 * @return True if mario hits an enemy
	 */
	public boolean hasEnemy(float marioX, final float marioY, final float marioHeight, final int time) {
		//The collide check only takes positions in pixels and marios position and height is in blocks
		final float marioXInPixels      = marioX      * World.PIXELS_PER_BLOCK;
		final float marioYInPixels      = marioY      * World.PIXELS_PER_BLOCK;
		final float marioHeightInPixels = marioHeight * World.PIXELS_PER_BLOCK;

		for (EnemySimulator enemySimulation : verifiedEnemySimulations) {			
			//+1 to time because magic
			if (enemySimulation.collideCheck(marioXInPixels, marioYInPixels, marioHeightInPixels, time + enemySimulation.timeOffset())) {
				return true;
			}
		}
		
		for (BulletBillTower bulletBillTower : bulletTowers) {
			if (bulletBillTower.collideCheck(marioXInPixels, marioYInPixels, marioHeightInPixels, time + 1)) {
				return true;
			}
		}
		return false;	
	}
	
	/**
	 * Updates all the simulations, adds new ones and removes those that don't exist in the game anymore
	 * @param enemyInfo an array of the enemy info given by the game
	 */
	public void updateEnemies(final float[] enemyInfo, ArrayList<Point> towerPositions, int tick) {
		//Sorted version of the enemyInfo which is easier to access
		final Int2ObjectOpenHashMap<ArrayList<Point2D.Float>> sortedEnemyInfo = sortEnemiesByType(enemyInfo);
		
		//move all valid simulations forward
		updateSimulations();
		removeDeadEnemies(sortedEnemyInfo);
		addCorrectSimulations(sortedEnemyInfo);
		addPotentialCorrectSimulations(sortedEnemyInfo);
		oldEnemyInfo = sortedEnemyInfo;
		
		updateTowers(towerPositions, tick);
	}
	
	/**
	 * Converts the enemy array of floats into a hashmap where there is an entry
	 * for each kind in the array where the value is a list of all enemy positions of that kind
	 * @param enemyInfo
	 * @return
	 */
	private Int2ObjectOpenHashMap<ArrayList<Point2D.Float>> sortEnemiesByType(final float[] enemyInfo) {
		final Int2ObjectOpenHashMap<ArrayList<Point2D.Float>> byType = new Int2ObjectOpenHashMap<ArrayList<Point2D.Float>>();
		for (int i = 0; i < enemyInfo.length; i += FLOATS_PER_ENEMY) {
			final int kind = (int) enemyInfo[i + TYPE_OFFSET];
			final float x  =       enemyInfo[i + X_OFFSET];
			final float y  =       enemyInfo[i + Y_OFFSET];
			
			//Apparently mushrooms and other powerups
			//are also considered enemies so first 
			//filter those away
			if (isKindAnEnemy(kind)) {
				final Point2D.Float enemyPos = new Point2D.Float(x, y);
				
				if (!byType.containsKey(kind)) {
					byType.put(kind, new ArrayList<Point2D.Float>());
				}
				
				byType.get(kind).add(enemyPos);	
			}
		}
		return byType;
	}
	
	/**
	 * Moves all valid enemies one tick forward
	 */
	private void updateSimulations() {
		for (EnemySimulator enemySimulation : verifiedEnemySimulations) {
			enemySimulation.moveTimeForward();
		}
	}
		
	/**
	 * If an enemy doesn't exist anymore then it's removed with this method
	 * @param enemyInfo
	 */
	private void removeDeadEnemies(final Int2ObjectOpenHashMap<ArrayList<Point2D.Float>> enemyInfo) {
		//As a foreach is used there can't be deleted from the arraylist so instead
		//a new list is created where all the not dead simulations are added
		final ArrayList<EnemySimulator> notDeletedSimulations = new ArrayList<EnemySimulator>();
		for (EnemySimulator enemySimulation : verifiedEnemySimulations) {
			final int kind = enemySimulation.getKind();
			final Point2D.Float simulationPosition = enemySimulation.getCurrentPosition();
			final float simulationX = simulationPosition.x;
			final float simulationY = simulationPosition.y;
			
			final ArrayList<Point2D.Float> enemyPositions = enemyInfo.get(kind);
			
			//Simulation can only be alive if there is a position for it in the enemyInfo
			if (enemyPositions != null) {
				Point2D.Float simulatedPosition = null;
				float leastDifference = Integer.MAX_VALUE;
				
				//foreach enemy position of the same kind as the simulator
				//if the expected position is within one of the values in the
				//enemyInfo list then the simulator is assumed to be alive
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
				//if a position matched the positon of the simulator
				//then remove the position from the arraylist so it can't
				//also be used by another enemy and add the simulation to the alive simulations list
				if (simulatedPosition != null) {
					enemyPositions.remove(simulatedPosition);					
					notDeletedSimulations.add(enemySimulation);
				}
			}
		}
		//replace old list with correct list of alive simulations
		verifiedEnemySimulations = notDeletedSimulations;	
	}
	
	/**
	 * Goes through all possible correct simulation and adds all the simulations which are correct
	 * @param enemyInfo
	 */
	private void addCorrectSimulations(final Int2ObjectOpenHashMap<ArrayList<Point2D.Float>> enemyInfo) {
		for (EnemySimulator enemySimulation : potentialCorrectSimulations) {			
			final int kind = enemySimulation.getKind();
			final Point2D.Float enemyPosition = enemySimulation.getCurrentPosition();
			final float x = enemyPosition.x;
			final float y = enemyPosition.y;
			final ArrayList<Point2D.Float> enemyPositions = enemyInfo.get(kind); 
			
			//Go through all position of the same kind and if any
			//position matches the position of the simulation then 
			//it's assumed that the simulation is correct
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
					//when a new simulation is added a new enemy is supposedly also on the screen
					newEnemySpawned = true;
				}
			}
		}
	}
	
	/**
	 * Creates simulations from the enemyInfo. As it's not known which way the
	 * enemy moves simulations are added for all possible movements. Tihis is where the old enemyInfo is used
	 * @param enemyInfo
	 */
	private void addPotentialCorrectSimulations(final Int2ObjectOpenHashMap<ArrayList<Point2D.Float>> enemyInfo) {
		//Potential simulation are only in this list for one tick. they are either moved to the
		//vlid simulations list or were invalid and are now garbage.
		potentialCorrectSimulations.clear();
		//Can onlydo this if the old enemyInfo is known as it's used to crate the speed which is required
		if (oldEnemyInfo != null) {
			for (Entry<Integer, ArrayList<Point2D.Float>> keyValueSet : enemyInfo.entrySet()) {
				final int kind = keyValueSet.getKey();
				final ArrayList<Point2D.Float> enemyPositions = keyValueSet.getValue();
				
				final ArrayList<Point2D.Float> oldEnemyPositions = oldEnemyInfo.get(kind);
				if (oldEnemyPositions == null) {
					continue;
				}
				
				//Make a simulation for any possible movement vector possible using the old and
				//new enemyInfo
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
						
						//Max speed for an enemy is way below one block per tick
						//so assume that all simulation with a speed above this are invalid
						//and thus won't be created
						if (deltaX <= World.PIXELS_PER_BLOCK && 
							deltaY <= World.PIXELS_PER_BLOCK) {
							final EnemySimulator potentialSimulation = getSimulator(x1, y1, xa, ya, kind);
							//insert first and second first position
							potentialSimulation.insertPosition(x2, y2);
							potentialSimulation.insertPosition(x1, y1);
							potentialSimulation.moveTimeForward();
							potentialSimulation.moveTimeForward();
							potentialSimulation.onlyForwardAccelerationByOne(x1, y1);
							
							
							//the xa and ya are 1 tick too old so they are updated here
							//potentialSimulation.moveEnemy();
							//potentialSimulation.moveTimeForward();
							//but the position isn't too old so it's set here again
							//potentialSimulation.setX(x1);
							//potentialSimulation.setY(y1);
							
							potentialCorrectSimulations.add(potentialSimulation);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns a new simulator given the parameters
	 * @param x
	 * @param y
	 * @param xa
	 * @param ya
	 * @param kind
	 * @return
	 */
	private EnemySimulator getSimulator(final float x, final float y, final float xa , final float ya, final int kind) {
		switch (kind) {
		case Sprite.KIND_BULLET_BILL:
			final int direction = (xa > 0) ? 1 : -1;
			return new BulletBillSimulator(x, y, direction);
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
	
	/**
	 * returns whether the kind can fly
	 * which is the same as the kind has wings
	 * @param kind
	 * @return
	 */
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
	
	/**
	 * Converts the kind into a type
	 * @param kind
	 * @return
	 */
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
	
	private boolean isKindAnEnemy(final int kind) {
        switch (kind) {
		case Sprite.KIND_GOOMBA:
		case Sprite.KIND_GOOMBA_WINGED:;
		case Sprite.KIND_RED_KOOPA:
		case Sprite.KIND_RED_KOOPA_WINGED:
		case Sprite.KIND_GREEN_KOOPA:
		case Sprite.KIND_GREEN_KOOPA_WINGED:
		case Sprite.KIND_BULLET_BILL:
		case Sprite.KIND_SPIKY:
		case Sprite.KIND_SPIKY_WINGED:
		case Sprite.KIND_ENEMY_FLOWER:
		case Sprite.KIND_SHELL:
			return true;
		default:
			return false;
		}
	}
	
	private void updateTowers(ArrayList<Point> towerPositions, int tick) {
		updateTowers();
		removeDeadTowers(towerPositions);
		addTowers(towerPositions, tick);
		
		//System.out.println(bulletTowers.size());
		//System.out.println();
	}
	
	private void updateTowers() {
		for (BulletBillTower bulletBillTower : bulletTowers) {
			bulletBillTower.update();
		}
	}
	
	private void removeDeadTowers(ArrayList<Point> towerPositions) {
		final ArrayList<BulletBillTower> aliveTowers = new ArrayList<BulletBillTower>();
		for (BulletBillTower bulletBillTower : bulletTowers) {
			for (Point point : towerPositions) {
				if (point.x == bulletBillTower.towerPos.x &&
					point.y == bulletBillTower.towerPos.y) {
					aliveTowers.add(bulletBillTower);
					break;
				}
			}
		}
		
		bulletTowers = aliveTowers;
	}
	
	private void addTowers(ArrayList<Point> towerPositions, int tick) {
		for (Point point : towerPositions) {
			boolean alreadyCreated = false;
			for (BulletBillTower bulletBillTower : bulletTowers) {
				if (point.x == bulletBillTower.towerPos.x &&
					point.y == bulletBillTower.towerPos.y) {
					alreadyCreated = true;
					break;
				}
			}
			
			if (!alreadyCreated) {
				bulletTowers.add(new BulletBillTower(point, tick + 1));
			}
		}
	}
	
	/**
	 * moves all simulations timeToMove ticks into the future
	 * @param timeToMove
	 */
	public void moveIntoFuture(final int timeToMove) {
		for (int i = 0; i < timeToMove; i++) {
			updateSimulations();
		}
	}
	
	/**
	 * Updates this EnemyPredictor's information with the one given
	 * @param correctPredictor
	 */
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
}
