package MarioAI.graph.nodes;

import java.awt.geom.Point2D;

import org.hamcrest.core.IsInstanceOf;

import MarioAI.Hasher;
import MarioAI.enemy.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;

public class SpeedNode implements Comparable<SpeedNode> {
	public final float SCORE_MULTIPLIER = 1024;
	
	public final Node node;
	public final float vx;
	public SpeedNode parent;
	public final float parentXPos;
	public final float parentVx;
	public final long hash;
	public final DirectedEdge ancestorEdge;
	public final float xPos;
	public final float yPos;
	public int gScore = 0;
	public float fScore = 0;
	private final MovementInformation moveInfo;
	private final boolean isSpeedNodeUseable;
	
	public SpeedNode(Node node, long hash) {
		this.node = node;
		this.moveInfo = null;
		this.vx = 0;
		this.parent = null;
		this.parentXPos = node.x;
		this.parentVx = 0;
		this.ancestorEdge = null;
		this.xPos = node.x;
		this.yPos = node.y;
		this.isSpeedNodeUseable = true;
		this.hash = hash;
	}
	
	public SpeedNode(Node node, SpeedNode parent, DirectedEdge ancestorEdge, long hash) {
		this(node, parent, parent.xPos, parent.vx, ancestorEdge, hash);
	}
	
	public SpeedNode(Node node, SpeedNode parent, float parentXPos, float parentVx, DirectedEdge ancestorEdge, long hash) {
		this.node = node;
		this.moveInfo = MarioControls.getEdgeMovementInformation(ancestorEdge, parentVx, parentXPos);
		this.vx = moveInfo.getEndSpeed();
		this.parent = parent;
		this.parentXPos = parentXPos;
		this.parentVx = parentVx;
		this.ancestorEdge = ancestorEdge;
		this.xPos = parentXPos + moveInfo.getXMovementDistance();
		this.yPos = node.y;
		this.isSpeedNodeUseable = determineIfThisNodeIsUseable();
		this.hash = hash;
	}
	
	private boolean determineIfThisNodeIsUseable() {
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
		
		//TODO: add check for whether this edge runs into any blocks
		return true;
	}
	
	public boolean doesMovementCollideWithEnemy(int startTime, EnemyPredictor enemyPredictor, int marioHeight) {
		int currentTick = startTime;
		
		for (Point2D.Float position : moveInfo.getPositions()) {
			final float x = parentXPos + position.x;
			final float y = parent.yPos - position.y;
			
			if (enemyPredictor.hasEnemy(x, y, 1, marioHeight, currentTick)) {
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
	
	public boolean isSpeedNodeUseable() {
		return isSpeedNodeUseable;
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
		return node.toString();
	}
}
