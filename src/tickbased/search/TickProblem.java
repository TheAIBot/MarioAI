package tickbased.search;

import java.util.ArrayList;
import java.util.List;

import ch.idsia.mario.environments.Environment;
import tickbased.game.enemies.BulletBill;
import tickbased.game.enemies.Enemy;
import tickbased.game.enemies.FlowerEnemy;
import tickbased.game.enemies.Mario;
import tickbased.game.enemies.Sprite;
import tickbased.game.world.LevelScene;
import tickbased.main.Action;
import tickbased.main.Problem;
import tickbased.main.SearchNode;
import tickbased.main.State;

public class TickProblem extends Problem {

	private static final float MAX_RIGHT = 200 * 16;
	public static final int SCREEN_WIDTH = 22; // TODO
	private static final int SCREEN_HEIGHT = 15;
	public int maxRightSeenSoFar = 15 * 16; // TODO
	public LevelScene worldScene;
	public LevelScene tentativeScene;
	public List<Sprite> sprites = new ArrayList<Sprite>();

	@Override
	public List<Action> actions(State state) {
		Node node = (Node) state;
		
		List<Action> actions = new ArrayList<Action>();

//		// move right
//		actions.add(new MarioAction(node, createAction(false, true, false, false, true)));
//		if (node.levelScene.mario.mayJump()) actions.add(new MarioAction(node, createAction(false, true, false, true, true)));
//		actions.add(new MarioAction(node, createAction(false, true, false, false, false)));
//		if (node.levelScene.mario.mayJump()) actions.add(new MarioAction(node, createAction(false, true, false, true, false)));
//
//		// move left
//		actions.add(new MarioAction(node, createAction(true, false, false, false, false)));
//		if (node.levelScene.mario.mayJump()) actions.add(new MarioAction(node, createAction(true, false, false, true, false)));
//		actions.add(new MarioAction(node, createAction(true, false, false, false, true)));
//		if (node.levelScene.mario.mayJump()) actions.add(new MarioAction(node, createAction(true, false, false, true, true)));
//
//		// jump straight up
//		if (node.levelScene.mario.mayJump()) actions.add(new MarioAction(node, createAction(false, false, false, true, true)));
//		
//		// stand still and jump
//		if (node.levelScene.mario.mayJump()) actions.add(new MarioAction(node, createAction(false, false, false, true, false)));
		
		actions.add(new MarioAction(node, createAction(false, true, false, false, false)));
		actions.add(new MarioAction(node, createAction(true, false, false, false, false)));
		actions.add(new MarioAction(node, createAction(false, true, false, true, false)));
		actions.add(new MarioAction(node, createAction(true, false, false, true, false)));
		
		return actions;
	}

	/**
	 * Auxiliary method
	 * @param left
	 * @param right
	 * @param down
	 * @param jump
	 * @param speed
	 * @return
	 */
	private boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean speed) {
		boolean[] action = new boolean[5];
		action[Mario.KEY_DOWN] = down;
		action[Mario.KEY_JUMP] = jump;
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_SPEED] = speed;
		return action;
	}

	@Override
	public SearchNode childNode(SearchNode searchNode, Action action) {
		Node node = (Node) searchNode.state;
		MarioAction marioAction = (MarioAction) action;
		
		// TODO dont make more levelscenes. Instead manipulate the tentativeLevelscene by taking back-ups and advancing it in time.
		// Clone the levelScene and update it by executing the given action
		LevelScene levelScene = new LevelScene(node.levelScene);
		levelScene.mario.setKeys(marioAction.action);
		levelScene.tick();
		
		State newNode = new Node(levelScene);
		states.add(newNode);
		
		SearchNode sn = new SearchNode(newNode, action);
		return sn;
	}

	@Override
	public double pathCost(SearchNode n1, SearchNode n2) {
		return 1;
	}

	/**
	 * Heuristic is the distance to the right side of the screen
	 */
	@Override
	public double heuristicFunction(SearchNode searchNode, SearchNode goal) {
		float distToRightSideOfScreen = ((Node) goal.state).x - ((Node) searchNode.state).x;
		return distToRightSideOfScreen < 0 ? 0 : distToRightSideOfScreen;
	}

	@Override
	public boolean goalTest(State node) {
		float distToRightSideOfScreen = ((Node) node).x - maxRightSeenSoFar;
		if (distToRightSideOfScreen >= 0) {
			maxRightSeenSoFar += ((Node) node).levelScene.mario.x += SCREEN_WIDTH / 2;
			return true;
		}
		return false;
	}
	
	/**
	 * Updates the levelScene (world) from the latest observation by setting blocks and enemies (sprites)
	 * @param observation
	 */
	public void updateLevel(Environment observation) {
		byte[][] blockPositions = observation.getLevelSceneObservationZ(0);
    	float[] enemyPositions = observation.getEnemiesFloatPos();
		float[] marioPosition = observation.getMarioFloatPos();
		
		worldScene.mario.x = marioPosition[0];
		worldScene.mario.y = marioPosition[1];
		Mario mario = worldScene.mario;
		
		int marioXPos = (int) mario.x / 16;
		int marioYPos = (int) mario.y / 16;

		// Blocks
        for (int y = 0; y < SCREEN_HEIGHT; y++) {
        	int xStart = (marioXPos - SCREEN_WIDTH > 0) ? marioXPos - SCREEN_WIDTH : 0;
        	int xEnd = (marioXPos <= 15) ? 15 : marioXPos - SCREEN_WIDTH ; // in the beginning Mario cannot see very far for some reason. So take care of this.
        	for (int x = xStart; x < xEnd; x++) {
        		worldScene.level.setBlock(x, y, blockPositions[x][y]);
        	}
        }
        
        // Enemies
        float delta = 0.5f; // uncertainty of already seen enemy positions relative to observed now
        Sprite sprite = null;
        for (int i = 0; i < enemyPositions.length; i += 3) {
        	int kind = (int) enemyPositions[i];
        	float x = enemyPositions[i+1];
        	float y = enemyPositions[i+2];
        	
        	boolean hasFoundEnemy = false;
        	for (Sprite spr : sprites) {
        		// check if enemy has been seen previously
        		if (Math.abs(spr.x - x) < delta && Math.abs(spr.y - y) < delta && spr.kind == kind
        			&& spr.kind != Sprite.KIND_SHELL
	        		&& spr.kind != Sprite.KIND_BULLET_BILL) {
        			if (!spr.hasFacingBeenSet) { // if enemy.facing has not been set previously
        				((Enemy) spr).facing = (spr.x - x > 0) ? 1 : -1;
        			}
        			hasFoundEnemy = true;
        			break;
        		}
        	}
        	
			if (!hasFoundEnemy) {
				if (kind == Enemy.ENEMY_FLOWER) {
					int xBlockPos = (int) x / 16;
					int yBlockPos = (int) y / 16;
					sprite = new FlowerEnemy(worldScene, (int) x, (int) y, xBlockPos, yBlockPos);
				} else if (kind == Sprite.KIND_BULLET_BILL) {
					sprite = new BulletBill(worldScene, x, y, -1);
				} else {
					boolean winged = false; // TODO temporarily not taken care of
					sprite = new Enemy(worldScene, (int) x, (int) y, -1, kind, winged, (int) x / 16, (int) y / 16);
				}
				sprites.add(sprite);
        	}
			if (sprite != null) worldScene.addSprite(sprite);
        	
        }
        
        ((Node) goalState).x = ((Node) initialState).x + TickProblem.SCREEN_WIDTH / 2 * 16;
	}

}
