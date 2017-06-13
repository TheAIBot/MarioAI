package MarioAI.graph.nodes;

import java.awt.geom.Point2D;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.FallEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;

public class SpeedNode implements Comparable<SpeedNode> {
	public final float SCORE_MULTIPLIER = 1024;
	
	public final Node node;
	public final float vx;
	public SpeedNode parent;
	public float parentXPos;
	public final float parentVx;
	public final long hash;
	public final DirectedEdge ancestorEdge;
	public final float creationXPos;
	public float currentXPos;
	public final int yPos;
	public int gScore = 0;
	public float fScore = 0;
	private final MovementInformation moveInfo;
	private final boolean isSpeedNodeUseable;
	
	private static final int MAX_TICKS_OF_INVINCIBILITY = 32; // source: Mario.java line 596
	public static int MAX_MARIO_LIFE = 3;
	public int ticksOfInvincibility = 0;
	public int lives = MAX_MARIO_LIFE;
	
	public SpeedNode(Node node, float vx, long hash) {
		this.node = node;
		this.moveInfo = null;
		this.vx = vx;
		this.parent = null;
		this.parentXPos = node.x;
		this.parentVx = 0;
		this.ancestorEdge = null;
		this.creationXPos = node.x;
		this.currentXPos = this.creationXPos;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
	}
	
	public SpeedNode(Node node, float marioX, float vx, long hash) {
		this.node = node;
		this.moveInfo = null;
		this.vx = vx;
		this.parent = null;
		this.parentXPos = node.x;
		this.parentVx = 0;
		this.ancestorEdge = null;
		this.creationXPos = marioX;
		this.currentXPos = this.creationXPos;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
		
	}
	
	///Should only be used for testing purposes
	public SpeedNode(Node node, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.creationXPos = parentXPos + moveInfo.getXMovementDistance();
		this.currentXPos = this.creationXPos;
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
	}
	
	public SpeedNode(Node node, SpeedNode parent, DirectedEdge ancestorEdge, long hash, World world) {
		this(node, parent, parent.creationXPos, parent.vx, ancestorEdge, hash, world);
	}
	
	public SpeedNode(Node node, SpeedNode parent, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.creationXPos = parentXPos + moveInfo.getXMovementDistance();
		this.currentXPos = this.creationXPos;
		this.parent = parent;
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isSpeedNodeUseable = determineIfThisNodeIsUseable(world);
		this.hash = hash;
	}
	
	private boolean determineIfThisNodeIsUseable(World world) {
		//Make sure the edge is possible to use
		//all Running edges are possible
		//not all jumps are possible
		if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped(), moveInfo.getXMovementDistance(), moveInfo.getXPositions())) {
			return false;
		}
		
		if (getMoveInfo().hasCollisions(parent, world)) {
			return false;
		}
		
		return true;
	}
	
	public boolean isSpeedNodeUseable(World world) {
		final float diffX = Math.abs(creationXPos - currentXPos);
		if (diffX > MarioControls.ACCEPTED_DEVIATION) {
			//Make sure the edge is possible to use
			//all Running edges are possible
			//not all jumps are possible
			if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped(), moveInfo.getXMovementDistance(), moveInfo.getXPositions())) {
				return false;
			}
			
			if (getMoveInfo().hasCollisions(parent, world)) {
				return false;
			}	
			
			return true;
		}
		else {
			return isSpeedNodeUseable;
		}
	}
	
	public boolean doesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, int marioHeight) {
		int currentTick = startTime;
		int i = parent.ticksOfInvincibility;
		this.lives = parent.lives;
		this.ticksOfInvincibility = parent.ticksOfInvincibility;
		parent.ticksOfInvincibility = 0;
		
		// If Mario is invincible longer than the time taken to get to traverse edge it does not matter
		// if an enemy is hit underway or not, so just deduct the ticks it takes from the ticks left of invincibility 
		if (i >= moveInfo.getMoveTime()) {
			this.ticksOfInvincibility -= moveInfo.getMoveTime();
			return false;
		}
		
		currentTick += i;
		boolean hasEnemyCollision = false;
		for (; i < moveInfo.getMoveTime(); i++) {
			final float x = parentXPos  + moveInfo.getXPositions()[i];
			final float y = parent.yPos - moveInfo.getYPositions()[i];
			
			if (enemyPredictor.hasEnemy(x, y - (1f / World.PIXELS_PER_BLOCK), marioHeight, currentTick)) {
				hasEnemyCollision = true;
				ticksOfInvincibility = MAX_TICKS_OF_INVINCIBILITY;
			}
			
			currentTick++;
		}
		
		if (hasEnemyCollision) {
			lives--;
		}
		
		return hasEnemyCollision;
	}
	
	/**
	 * Old collision method. Momentarily only for ease of reference.
	 * TODO remove this method
	 * @param startTime
	 * @param enemyPredictor
	 * @param marioHeight
	 * @return
	 */
	public boolean tempDoesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, float marioHeight) {
		int currentTick = startTime;
		
        for (int i = 0; i < moveInfo.getMoveTime(); i++) {
            final float x = parentXPos  + moveInfo.getXPositions()[i];
            final float y = parent.yPos - moveInfo.getYPositions()[i];

            if (enemyPredictor.hasEnemy(x, y - (1f / World.PIXELS_PER_BLOCK), marioHeight, currentTick)) {
                return true;
            }
	
            currentTick++;
        }
		return false;
	}
	
	public MovementInformation getMoveInfo() {
		return moveInfo;
	}
	
	public void use() {
		ancestorEdge.setMoveInfo(moveInfo);
	}
	
	public int getMoveTime() {
		return moveInfo.getMoveTime();
	}
	
	@Override
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof SpeedNode) {
			SpeedNode bb = (SpeedNode) b;
			return bb.hash == hash;
		} else {
			return false;
		}
	}
	
	public int compareTo(SpeedNode o) {
		return (int) ((this.fScore * SCORE_MULTIPLIER) - (o.fScore * SCORE_MULTIPLIER));
	}
	
	@Override
	public String toString() {
		return node.toString() + (" gScore: " + gScore + ", fScore: " + fScore + "\n");
	}
}
