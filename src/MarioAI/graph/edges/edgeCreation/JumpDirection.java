package MarioAI.graph.edges.edgeCreation;

public enum JumpDirection { 
	STRAIGHT_UPWARDS{
		@Override
		public int getHorizontalDirectionAsInt() {
			return 0;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return -1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return STRAIGHT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return null; //There is no opposite.
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return STRAIGHT_DOWNWARDS;
		}
		
	},
	STRAIGHT_DOWNWARDS{
		@Override
		public int getHorizontalDirectionAsInt() {
			return 0;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return 1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return STRAIGHT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return null; //There is no opposite.
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return STRAIGHT_DOWNWARDS;
		}
		
	},
	LEFT_UPWARDS {
		@Override
		public int getHorizontalDirectionAsInt() {
			return -1;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return -1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return RIGHT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return RIGHT_UPWARDS;
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return LEFT_DOWNWARDS;
		}
	},
	LEFT_DOWNWARDS {
		@Override
		public int getHorizontalDirectionAsInt() {
			return -1;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return 1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return RIGHT_UPWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return RIGHT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return LEFT_UPWARDS;
		}
	},
	RIGHT_UPWARDS {
		@Override
		public int getHorizontalDirectionAsInt() {
			return 1;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return -1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return LEFT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return LEFT_UPWARDS;
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return RIGHT_DOWNWARDS;
		}
	},
	RIGHT_DOWNWARDS {
		@Override
		public int getHorizontalDirectionAsInt() {
			return 1;
		}

		@Override
		public int getVerticalDirectionAsInt() {
			return 1;
		}

		@Override
		public JumpDirection getOppositeDirection() {
			return LEFT_UPWARDS;
		}

		@Override
		public JumpDirection getOppositeHorizontalDirection() {
			return LEFT_DOWNWARDS;
		}

		@Override
		public JumpDirection getOppositeVerticalDirection() {
			return RIGHT_UPWARDS;
		}
	};

	public boolean isUpwardsType() {
		return (this == LEFT_UPWARDS || this == RIGHT_UPWARDS);
	}
	
	public boolean isLeftType() {
		return (this == LEFT_UPWARDS || this == LEFT_DOWNWARDS);
	}
	
	public abstract int getHorizontalDirectionAsInt();
	
	public abstract int getVerticalDirectionAsInt();
	
	public abstract JumpDirection getOppositeDirection();
	
	public abstract JumpDirection getOppositeHorizontalDirection();
	
	public abstract JumpDirection getOppositeVerticalDirection();
}
