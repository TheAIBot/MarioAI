package MarioAI.graph.nodes;

import java.awt.geom.Point2D;

import MarioAI.World;
import MarioAI.enemySimuation.EnemyCollision;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.enemySimuation.simulators.EnemySimulator;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.FallEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;
import javafx.util.Pair;

public class StateNode implements Comparable<StateNode> {
	public final float SCORE_MULTIPLIER = 1024;
	
	public final Node node;
	public final float vx;
	public StateNode parent;
	public final float parentXPos;
	public final float parentVx;
	public final long hash;
	public final DirectedEdge ancestorEdge;
	public float xPos;
	public final int yPos;
	public int gScore = 0;
	public float fScore = 0;
	private final MovementInformation moveInfo;
	private final boolean isSpeedNodeUseable;
	//Bit string, representing the enemies living. 
	//Must not be reused.
	public long livingEnemies; 
	
	private static final int MAX_TICKS_OF_INVINCIBILITY = 32; // source: Mario.java line 596
	public static int MAX_MARIO_LIFE = 3;
	public int ticksOfInvincibility = 0;
	public int lives = MAX_MARIO_LIFE;
	
	public StateNode(Node node, float vx, long hash, long livingEnemies) {
		this.node = node;
		this.moveInfo = null;
		this.vx = vx;
		this.parent = null;
		this.parentXPos = node.x;
		this.parentVx = 0;
		this.ancestorEdge = null;
		this.xPos = node.x;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
		this.livingEnemies = livingEnemies;
	}
	
	public StateNode(Node node, float marioX, float vx, long hash, long livingEnemies) {
		this(node, vx, hash, livingEnemies);
		this.xPos = marioX;
	}
	
	///Should only be used for testing purposes
	public StateNode(Node node, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, long livingEnemies, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.xPos = parentXPos + moveInfo.getXMovementDistance();
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
		this.livingEnemies = livingEnemies;
	}
	
	public StateNode(Node node, StateNode parent, DirectedEdge ancestorEdge, long hash, long livingEnemies, World world) {
		this(node, parent, parent.xPos, parent.vx, ancestorEdge, hash, livingEnemies, world);
	}
	
	public StateNode(Node node, StateNode parent, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash, long livingEnemies, World world) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.xPos = parentXPos + moveInfo.getXMovementDistance();
		this.parent = parent;
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.yPos = node.y;
		this.isSpeedNodeUseable = determineIfThisNodeIsUseable(world);
		this.hash = hash;
		this.livingEnemies = livingEnemies;
	}
	
	private boolean determineIfThisNodeIsUseable(World world) {
		//There are a lot of possible problems for a fall edge.
		//TODO move below, when implemented
		if (this.ancestorEdge instanceof FallEdge &&
			 !MarioControls.canMarioUseFallEdge(ancestorEdge, xPos)) {
			return false;
		}
		
		//Make sure the edge is possible to use
		//all Running edges are possible
		//not all jumps are possible
		if (!MarioControls.canMarioUseEdge(ancestorEdge, parentXPos, parentVx, moveInfo.getTotalTicksJumped())) {
			return false;
		}
		
		//In a jump it's possible to jump too far
		//and there is nothing that mario can do about it
		//TODO this should maybe be removed in the future
		if (this.ancestorEdge instanceof JumpingEdge && 
			!MarioControls.canMarioUseJumpEdge(ancestorEdge, xPos)) {
			return false;
		}
		
		//TODO remove this when changed moveinformation is implemented
		if (getMoveInfo().hasCollisions(parent, world)) {
			return false;
		}
		
		return true;
	}
	
	public boolean doesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, float marioHeight, EnemyCollision firstCollision) {
		int currentTick = startTime;
		this.ticksOfInvincibility = parent.ticksOfInvincibility;
		parent.ticksOfInvincibility = 0;
				
		boolean hasEnemyCollision = false;
		
		for (int i = 0; i < moveInfo.getPositions().length; i++) {
			Point2D.Float currentPosition = moveInfo.getPositions()[i];
			final float x = parentXPos  + currentPosition.x;
			final float y = parent.yPos - currentPosition.y;
			
			//In the beginning of a movement, Mario will always be on the ground, thus not accelerating downwards.
			//Necessary to stomp the enemies.
			boolean movingDownwards = (i == 0)? false: (moveInfo.getPositions()[i-1].y > moveInfo.getPositions()[i].y);
			//He must also land on the enemy, ie, not be on the ground, or not have been on the ground the tick before:
			boolean wasOnGround 	= (i == 0)? true: (moveInfo.getPositions()[i-1].y != moveInfo.getPositions()[i].y);
			//He is on the ground, if and only if he is a running edge,
			//or he is at the last two positions on any other type of edge
			//( the last, curtesy of mario always landing before coming to the end of a block):
			boolean isOnGround	= (this.ancestorEdge instanceof RunningEdge || moveInfo.getPositions().length - 2 <= i);
			boolean isOrWasNotOnGround = !wasOnGround || !isOnGround;
			
			//I will take the first actual collision, 
			//as though that is the one that determines the type of collision with enemies.
			if (enemyPredictor.hasEnemy(x, y, 0.5f, marioHeight, currentTick, movingDownwards, isOrWasNotOnGround, firstCollision, this.livingEnemies)) {
				if(firstCollision.isStompType){ //Stomp type collision
					ticksOfInvincibility = 1; //Gets one tick of invincibility, in case of a stomp.
					//Notice that if has more ticks of invincibility than 1, this is overwritten.
					//This is how it is done in the game code.
					//TODO check correct.
					return true; //Stops the collision here, as the path taken must be changed.
				} else if(ticksOfInvincibility == 0){ //Normal collision, where Mario is damaged
					hasEnemyCollision = true;
					lives--;
					ticksOfInvincibility = MAX_TICKS_OF_INVINCIBILITY;
				} else {
					ticksOfInvincibility--;
				}
			} else {
				ticksOfInvincibility--;
			}
			
			currentTick++;
		}
		
		return hasEnemyCollision;
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
	
	public boolean isSpeedNodeUseable() {
		return isSpeedNodeUseable;
	}
	
	@Override
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof StateNode) {
			StateNode bb = (StateNode) b;
			return bb.hash == hash;
		} else {
			return false;
		}
	}
	
	public int compareTo(StateNode o) {
		return (int) ((this.fScore * SCORE_MULTIPLIER) - (o.fScore * SCORE_MULTIPLIER));
	}
	
	@Override
	public String toString() {
		return node.toString() + (" gScore: " + gScore + ", fScore: " + fScore + "\n");
	}
}
