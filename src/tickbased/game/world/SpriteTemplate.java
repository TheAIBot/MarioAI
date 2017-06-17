package tickbased.game.world;

import tickbased.game.enemies.Enemy;
import tickbased.game.enemies.FlowerEnemy;
import tickbased.game.enemies.Sprite;

public class SpriteTemplate {
	public int lastVisibleTick = -1;
	public Sprite sprite;
	public boolean isDead = false;
	private boolean winged;

	public int getType() {
		return type;
	}

	private int type;

	public SpriteTemplate(int type, boolean winged) {
		this.type = type;
		this.winged = winged;
	}

	/**
	 * Copy constructor
	 * @param st
	 */
	public SpriteTemplate(SpriteTemplate st) {
		this(st.type, st.winged);
	}

	public void spawn(LevelScene world, int x, int y, int dir) {
		if (isDead) return;

		if (type == Enemy.ENEMY_FLOWER) {
			sprite = new FlowerEnemy(world, x * 16 + 15, y * 16 + 24, x, y);
		} else {
			// sprite = new Enemy(world, x*16+8, y*16+15, dir, type, winged);
			sprite = new Enemy(world, x * 16 + 8, y * 16 + 15, dir, type, winged, x, y);
		}
		sprite.spriteTemplate = this;
		world.addSprite(sprite);
	}
}