package tests.EnemyPredictorTests;

import java.util.ArrayList;

import org.junit.Test;

import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.EnemyType;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.environments.Environment;
import tests.PathHelper;
import tests.TestTools;
import tests.UnitTestAgent;

public class TestEnemyCollisionRunningLeft {
	@Test
	public void testRedKoopaCollisionBox() {
		checkCollisionBoxForEnemy(EnemyType.RED_KOOPA, false);
	}
	@Test
	public void testRedKoopaCollisionBoxFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.RED_KOOPA, true);
	}
	@Test
	public void testRedKoopaCollisionBoxWinged() {
		checkCollisionBoxForEnemy(EnemyType.RED_KOOPA.asWinged(), false);
	}
	@Test
	public void testRedKoopaCollisionBoxWingedFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.RED_KOOPA.asWinged(), true);
	}
	
	@Test
	public void testGreenKoopaCollisionBox() {
		checkCollisionBoxForEnemy(EnemyType.GREEN_KOOPA, false);
	}
	@Test
	public void testGreenKoopaCollisionBoxFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.GREEN_KOOPA, true);
	}
	@Test
	public void testGreenKoopaCollisionBoxWinged() {
		checkCollisionBoxForEnemy(EnemyType.GREEN_KOOPA.asWinged(), false);
	}
	@Test
	public void testGreenKoopaCollisionBoxWingedFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.GREEN_KOOPA.asWinged(), true);
	}
	
	@Test
	public void testGoombaCollisionBox() {
		checkCollisionBoxForEnemy(EnemyType.GOOMBA, false);
	}
	@Test
	public void testGoombaCollisionBoxFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.GOOMBA, true);
	}
	@Test
	public void testGoombaCollisionBoxWinged() {
		checkCollisionBoxForEnemy(EnemyType.GOOMBA.asWinged(), false);
	}
	@Test
	public void testGoombaCollisionBoxWingedFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.GOOMBA.asWinged(), true);
	}
	
	@Test
	public void testSpikyCollisionBox() {
		checkCollisionBoxForEnemy(EnemyType.SPIKY, false);
	}
	@Test
	public void testSpikyCollisionBoxFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.SPIKY, true);
	}
	@Test
	public void testSpikyCollisionBoxWinged() {
		checkCollisionBoxForEnemy(EnemyType.SPIKY.asWinged(), false);
	}
	@Test
	public void testSpikyCollisionBoxWingedFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.SPIKY.asWinged(), true);
	}
	
	@Test
	public void testFlowerCollisionBox() {
		checkCollisionBoxForEnemy(EnemyType.FLOWER, false);
	}
	@Test
	public void testFlowerCollisionBoxFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.FLOWER, true);
	}
	
	@Test
	public void testRedShellCollisionBox() {
		checkCollisionBoxForEnemy(EnemyType.RED_SHELL, false);
	}
	@Test
	public void testRedShellCollisionBoxFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.RED_SHELL, true);
	}
	
	@Test
	public void testGreenShellCollisionBox() {
		checkCollisionBoxForEnemy(EnemyType.GREEN_SHELL, false);
	}
	@Test
	public void testGreenShellCollisionBoxFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.GREEN_SHELL, true);
	}
	
	@Test
	public void testBulletBillCollisionBox() {
		checkCollisionBoxForEnemy(EnemyType.BULLET_BILL, false);
	}
	@Test
	public void testBulletBillCollisionBoxFromCopy() {
		checkCollisionBoxForEnemy(EnemyType.BULLET_BILL, true);
	}
	
	private void checkCollisionBoxForEnemy(EnemyType enemyType, boolean makeCopy) {
		final UnitTestAgent agent = new UnitTestAgent();
		final Environment observation = TestTools.loadLevel("testEnemyPredictor/flat.lvl", agent, false);
		final World world = new World();
		final MarioControls marioControls = new MarioControls();
		final EnemyPredictor enemyPredictor = new EnemyPredictor();
		world.initialize(observation);
		enemyPredictor.intialize(((MarioComponent)observation).getLevelScene());
		
		for (int xDistance = 1; xDistance < 4; xDistance++) {
			final ArrayList<DirectedEdge> pathRight = PathHelper.createPath(1, 1, -xDistance, 0, 0, 1, world, false);
			TestEnemyHelper.testPathLeft(observation, agent, world, marioControls, enemyPredictor, pathRight, enemyType, xDistance, makeCopy);
		}
	}
}
