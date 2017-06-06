package MarioAI;

import java.awt.geom.Point2D;
import java.io.DataInputStream;

import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.mario.engine.LevelScene;

public class CollisionDetection {
	public static final float MARIO_WIDTH = 4; //TODO Change depending on mario
	public static final float MARIO_HEIGHT = 24; //(*)TODO Change depending on mario
	//Taken from Level class.
	public static final int BIT_BLOCK_UPPER = 1 << 0;
	public static final int BIT_BLOCK_ALL = 1 << 1;
    public static final int BIT_BLOCK_LOWER = 1 << 2;   

    public static final byte[] TILE_BEHAVIORS = new byte[256];
   	//Test seed: 3261372
	public static final byte[] TILE_CONVERTER 	     = new byte[]{16,21,34,  9,-74,11};
	public static final byte[] TILE_CONVERTER_MARKER = new byte[]{16,21, 0,-10,-11,20};
	
	public static boolean isColliding(Point2D.Float futureOffset, Point2D.Float currentOffset, SpeedNode sourceNode, float lastYValue, World world){
		return isColliding(futureOffset, currentOffset, sourceNode.xPos, sourceNode.yPos, lastYValue, world);
	}
	
	public static boolean isColliding(Point2D.Float futureOffset, Point2D.Float currentOffset, float startX, float startY, float lastYValue, World world){
		//TODO check correct directions.
		//One block = 16
		//Note that it will take it as Marios right corner, if he had width=16, is placed at the speed node position initially
		final float xa =  (futureOffset.x - currentOffset.x) * 16;
		final float ya = -(futureOffset.y - currentOffset.y) * 16; //yes, it is the correct placement.
		//Change below to just use current position, if one want to get the actual position after the collision.
		//Note how the y direction is handled.
		//The minus one is needed to reflect how it is done by the mario code.
		//TODO find out why,
		final Point2D.Float currentPosition = new Point2D.Float( (currentOffset.x + startX) * 16,
															     (startY - currentOffset.y) * 16 - 1);
		final Point2D.Float expectedPosition = new Point2D.Float(currentPosition.x + xa, currentPosition.y + ya);
		//System.out.println("Current position: x = " + currentPosition.x/16 + ", y = " + currentPosition.y/16);
		//System.out.println("Or: x = " + currentPosition.x + ", y = " + currentPosition.y);
		//TODO change -2 back to -1
		//TODO (*) Why -2 to the y position?. Should it be +2? test.
		if (lastYValue == futureOffset.y) {
			move(currentPosition, xa, 0, world);
			currentPosition.y += ya;
		}
		else {
			move(currentPosition, xa, 0, world);
			move(currentPosition, 0, ya, world);
		}
		
		final float diffX = Math.abs(currentPosition.x - expectedPosition.x);
		final float diffY = Math.abs(currentPosition.y - expectedPosition.y);
		
		return diffX > MarioControls.ACCEPTED_DEVIATION || diffY > MarioControls.ACCEPTED_DEVIATION;
	}
	
	/** Taken from the Mario class, with some changes. Lack of comments are due to their lack of comments.
	 * 
	 * @param xa
	 * @param ya
	 * @return
	 */
	private static boolean move(Point2D.Float currentPosition, float xa, float ya, World world) {
		while (xa > 8) {
			if (!move(currentPosition, 8, 0, world))
				return false;
			xa -= 8;
		}
		while (xa < -8) {
			if (!move(currentPosition, -8, 0, world))
				return false;
			xa += 8;
		}
		while (ya > 8) {
			if (!move(currentPosition, 0, 8, world))
				return false;
			ya -= 8;
		}
		while (ya < -8) {
			if (!move(currentPosition, 0, -8, world))
				return false;
			ya += 8;
		}

		boolean collide = false;

		//We don't care if it is just one of the blocking that is true, or multiple, so they are joiuned together
		if 			(ya > 0 && 
						(isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya, xa, 0, world) || 
						 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya, xa, 0, world) || 
						 isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya + 1, xa, ya, world) || 
						 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya + 1, xa, ya, world))) {
			collide = true;
		} 
		if 	(ya < 0 && 
				  		(isBlocking(currentPosition, currentPosition.x + xa, currentPosition.y + ya - MARIO_HEIGHT, xa, ya, world) ||
				  		 isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya, world)) ||
				  		 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya, world)) {
			collide = true;
		}
		if 	(xa > 0 && 
						(isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya, world) ||
						 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT / 2, xa, ya, world) || 
						 isBlocking(currentPosition, currentPosition.x + xa + MARIO_WIDTH, currentPosition.y + ya, xa, ya, world))) {
			collide = true;
		}
		if 	(xa < 0 &&
						(isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT, xa, ya, world) ||
						 isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya - MARIO_HEIGHT / 2, xa, ya, world) ||
						 isBlocking(currentPosition, currentPosition.x + xa - MARIO_WIDTH, currentPosition.y + ya, xa, ya, world))) {
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
	private static boolean isBlocking(Point2D.Float currentPosition, float newX, float newY, float xa, float ya, World world) {
		int x = (int) (newX / 16);
		int y = (int) (newY / 16);
		//TODO check why this is necessary.
		if (x == (int) (currentPosition.x / 16) && y == (int) (currentPosition.y / 16))
			return false;
		else {
			Node[] column = world.getColumn(x);
			if (column != null && y >= 0 && y <= 15) { //TODO (*)Check correct null check
				if (column[y] == null) {
					return false; //Can't block if it is air.
				}
				byte block = convertType(column[y].type);
				boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
				blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
				blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;
				return blocking;
			} else {
				return false;//Haven't seen the column=no collision. Corresponds to goal nodes(*) TODO check
			}
		}
	}
	
	public static byte convertType(byte type){
		for (int i = 0; i < TILE_CONVERTER.length; i++) {
			if (TILE_CONVERTER_MARKER[i] == type) {
				return TILE_CONVERTER[i];
			}
		}
		throw new Error("Missing tile converter type, for type = " + type);
	}
	
	public static void loadTileBehaviors()
	{
		//TODO check done correctly
		try 
		{
			new DataInputStream(LevelScene.class.getResourceAsStream("resources/tiles.dat")).readFully(CollisionDetection.TILE_BEHAVIORS);
		}
		catch (Exception e)
		{
		   e.printStackTrace();
		   System.exit(0);
		}
	}
}