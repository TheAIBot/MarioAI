package MarioAI.enemy;

public enum EnemyType {
	RED_KOOPA {
		@Override
		public int getType() {
			return TYPE_RED_KOOPA;
		}

		@Override
		public int getKind() {
			return KIND_RED_KOOPA;
		}

		@Override
		public EnemyType asWinged() {
			return RED_KOOPA_WINGED;
		}

		@Override
		public boolean hasWings() {
			return false;
		}
	},
	RED_KOOPA_WINGED {
		@Override
		public int getType() {
			return TYPE_RED_KOOPA;
		}

		@Override
		public int getKind() {
			return KIND_RED_KOOPA_WINGED;
		}

		@Override
		public EnemyType asWinged() {
			return this;
		}

		@Override
		public boolean hasWings() {
			return true;
		}
	},
	GREEN_KOOPA {
		@Override
		public int getType() {
			return TYPE_GREEN_KOOPA;
		}

		@Override
		public int getKind() {
			return KIND_GREEN_KOOPA;
		}

		@Override
		public EnemyType asWinged() {
			return GREEN_KOOPA_WINGED;
		}

		@Override
		public boolean hasWings() {
			return false;
		}
	},
	GREEN_KOOPA_WINGED {
		@Override
		public int getType() {
			return TYPE_GREEN_KOOPA;
		}

		@Override
		public int getKind() {
			return KIND_GREEN_KOOPA_WINGED;
		}

		@Override
		public EnemyType asWinged() {
			return this;
		}

		@Override
		public boolean hasWings() {
			return true;
		}
	},
	GOOMBA {
		@Override
		public int getType() {
			return TYPE_GOOMBA;
		}

		@Override
		public int getKind() {
			return KIND_GOOMBA;
		}

		@Override
		public EnemyType asWinged() {
			return GOOMBA_WINGED;
		}

		@Override
		public boolean hasWings() {
			return false;
		}
	},
	GOOMBA_WINGED {
		@Override
		public int getType() {
			return TYPE_GOOMBA;
		}

		@Override
		public int getKind() {
			return KIND_GOOMBA_WINGED;
		}

		@Override
		public EnemyType asWinged() {
			return this;
		}

		@Override
		public boolean hasWings() {
			return true;
		}
	},
	SPIKY {
		@Override
		public int getType() {
			return TYPE_SPIKY;
		}

		@Override
		public int getKind() {
			return KIND_SPIKY;
		}

		@Override
		public EnemyType asWinged() {
			return SPIKY_WINGED;
		}

		@Override
		public boolean hasWings() {
			return false;
		}
	},
	SPIKY_WINGED {
		@Override
		public int getType() {
			return TYPE_SPIKY;
		}

		@Override
		public int getKind() {
			return KIND_SPIKY_WINGED;
		}

		@Override
		public EnemyType asWinged() {
			return this;
		}

		@Override
		public boolean hasWings() {
			return true;
		}
	},
	FLOWER {
		@Override
		public int getType() {
			return TYPE_FLOWER;
		}

		@Override
		public int getKind() {
			return KIND_ENEMY_FLOWER;
		}

		@Override
		public EnemyType asWinged() {
			return this;
		}

		@Override
		public boolean hasWings() {
			return false;
		}
	},
	RED_SHELL {
		@Override
		public int getType() {
			return TYPE_RED_SHELL;
		}

		@Override
		public int getKind() {
			return KIND_SHELL;
		}

		@Override
		public EnemyType asWinged() {
			return this;
		}

		@Override
		public boolean hasWings() {
			return false;
		}
	},
	GREEN_SHELL {
		@Override
		public int getType() {
			return TYPE_GREEN_SHEEL;
		}

		@Override
		public int getKind() {
			return KIND_SHELL;
		}

		@Override
		public EnemyType asWinged() {
			return this;
		}

		@Override
		public boolean hasWings() {
			return false;
		}
	},
	BULLET_BILL {
		@Override
		public int getType() {
			return TYPE_BULLET_BILL;
		}

		@Override
		public int getKind() {
			return KIND_BULLET_BILL;
		}

		@Override
		public EnemyType asWinged() {
			return this;
		}

		@Override
		public boolean hasWings() {
			return false;
		}
	};
	
    private static final int TYPE_RED_KOOPA = 0;
    private static final int TYPE_GREEN_KOOPA = 1;
    private static final int TYPE_GOOMBA = 2;
    private static final int TYPE_SPIKY = 3;
    private static final int TYPE_FLOWER = 4;
    private static final int TYPE_RED_SHELL = 0;
    private static final int TYPE_GREEN_SHEEL = 1;
    private static final int TYPE_BULLET_BILL = -1; // has none
    
    
    private static final int KIND_GOOMBA = 2;
    private static final int KIND_GOOMBA_WINGED = 3;
    private static final int KIND_RED_KOOPA = 4;
    private static final int KIND_RED_KOOPA_WINGED = 5;
    private static final int KIND_GREEN_KOOPA = 6;
    private static final int KIND_GREEN_KOOPA_WINGED = 7;
    private static final int KIND_BULLET_BILL = 8;
    private static final int KIND_SPIKY = 9;
    private static final int KIND_SPIKY_WINGED = 10;
    private static final int KIND_ENEMY_FLOWER = 12;
    private static final int KIND_SHELL = 13;  
	
	public abstract int getType();
	
	public abstract int getKind();
	
	public abstract EnemyType asWinged();
	
	public abstract boolean hasWings();
}
