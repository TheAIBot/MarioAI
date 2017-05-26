package MarioAI.graph;

import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.IOException;

import com.sun.javafx.scene.paint.GradientUtils.Point;
import com.sun.javafx.scene.traversal.Direction;

import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.World;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.engine.sprites.Sparkle;

public class CollisionDetection {
	private static World world;
	private static final float MARIO_WIDTH = 4; //TODO Change depending on mario
	private static final float MARIO_HEIGHT = 24; //(*)TODO Change depending on mario
	//Taken from Level class.
	public static final int BIT_BLOCK_UPPER = 1 << 0;
	public static final int BIT_BLOCK_ALL = 1 << 1;
   public static final int BIT_BLOCK_LOWER = 1 << 2;   

   public static byte[] TILE_BEHAVIORS = new byte[256];
   	//Test seed: 3261372
	
	
	public static boolean isColliding(Point2D.Float currentPosition, Point2D.Float priorPosition, Node sourceNode){
		//TODO check correct directions.
		//One block = 16
		float xa = (currentPosition.x - priorPosition.x)*16;
		float ya = (currentPosition.y - priorPosition.y)*16;
		//Change below to just use current position, if one want to get the actual position after the collision.
		//Note how the y direction is handled.
		Point2D.Float positionAfterCollisions = new Point2D.Float((currentPosition.x + sourceNode.x)*16,(sourceNode.y - currentPosition.y )*16);
		return move(positionAfterCollisions, xa, ya);
	}
	
	public static void setWorld(World newWorld){
		world = newWorld;
	}
	
	/** Taken from the Mario class, with some changes. Lack of comments are due to their lack of comments.
	 * 
	 * @param xa
	 * @param ya
	 * @return
	 */
	private static boolean move(Point2D.Float currentPosition, float xa, float ya) {
		while (xa > 8) {
			if (!move(currentPosition, 8, 0))
				return false;
			xa -= 8;
		}
		while (xa < -8) {
			if (!move(currentPosition, -8, 0))
				return false;
			xa += 8;
		}
		while (ya > 8) {
			if (!move(currentPosition, 0, 8))
				return false;
			ya -= 8;
		}
		while (ya < -8) {
			if (!move(currentPosition, 0, -8))
				return false;
			ya += 8;
		}

		boolean collide = false;

		//We don't care if it is just one of the blocking that is true, or multiple, so they are joiuned together
		if (ya > 0 && 
			(isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya, xa, 0) || 
			 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya, xa, 0) || 
			 isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya + 1, xa, ya) || 
			 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya + 1, xa, ya))) {
			collide = true;
		} else if 	(ya < 0 && 
				  		(isBlocking(currentPosition, currentPosition.x + xa, currentPosition.y + ya - MARIO_HEIGHT, xa, ya) ||
				  		(collide || isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya)) ||
				  		(collide || isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya)))) {
			collide = true;
		} else if 	(xa > 0 && 
						(isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya) ||
						 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya) || 
						 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya, xa, ya))) {
			collide = true;
		}else if (xa < 0 &&
					(isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya) ||
					 isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya) ||
					 isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT / 2, xa, ya) ||
					 isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya, xa, ya))) {
			collide = true;
		}

		if (collide) {
			if (xa < 0) {
				currentPosition.x = (int) ((currentPosition.x - MARIO_WIDTH) / 16) * 16 + MARIO_WIDTH;
				//this.xa = 0;
			}
			if (xa > 0) {
				currentPosition.x = (int) ((currentPosition.x + MARIO_WIDTH) / 16 + 1) * 16 - MARIO_WIDTH - 1;
				//this.xa = 0;
			}
			if (ya < 0) {
				currentPosition.y = (int) ((currentPosition.y - MARIO_HEIGHT) / 16) * 16 + MARIO_HEIGHT;
				//jumpTime = 0;
				//this.ya = 0;
			}
			if (ya > 0) {
				currentPosition.y = (int) ((currentPosition.y - 1) / 16 + 1) * 16 - 1;
				//onGround = true;
			}
			return false;
		} else {
			currentPosition.x += xa;
			currentPosition.y += ya;
			return true;
		}
	}

	/** Taken directly from the Mario class. Lack of comments are due to their lack of comments.
	 * 
	 * @param xa
	 * @param ya
	 * @return
	 */
	private static boolean isBlocking(Point2D.Float currentPosition, float newX, float newY, float xa, float ya) {
		int x = (int) (newX / 16);
		int y = (int) (newY / 16);
		if (x == (int) (currentPosition.x / 16) && y == (int) (currentPosition.y / 16))
			return false;
		else {
			Node[] column = world.getColumn(x);
			if (column != null && column[y] != null) { //TODO (*)Check correct null check
				boolean blocking = ((TILE_BEHAVIORS[column[y ].type & 0xff]) & BIT_BLOCK_ALL) > 0;
				blocking |= (ya > 0) && ((TILE_BEHAVIORS[column[y].type & 0xff]) & BIT_BLOCK_UPPER) > 0;
				blocking |= (ya < 0) && ((TILE_BEHAVIORS[column[y].type & 0xff]) & BIT_BLOCK_LOWER) > 0;
				return blocking;
			} else return true;//Haven't seen the column=collision.
		}
	}
	
	
	public static void loadTileBehaviors(){
		//TODO check done correctly
		try {
			new DataInputStream(LevelScene.class.getResourceAsStream("resources/tiles.dat")).readFully(Level.TILE_BEHAVIORS);
	        }
	   catch (IOException e){
		   e.printStackTrace();
	            System.exit(0);
	        }
	  }
}
