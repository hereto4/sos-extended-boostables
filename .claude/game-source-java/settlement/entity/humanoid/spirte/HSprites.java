package settlement.entity.humanoid.spirte;

import static settlement.entity.humanoid.spirte.HSpriteConst.ITOOL;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_CARRY;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_LEFT;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_LEFT2;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_LEFT3;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_OUT;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_OUT2;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_RIGHT;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_RIGHT2;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_RIGHT3;
import static settlement.entity.humanoid.spirte.HSpriteConst.ITORSO_STILL;

import game.GAME;
import init.race.appearence.RAddon;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.spirte.HSpriteConst.CLAY;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsAppearance;
import settlement.stats.equip.EquipRange;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;

public final class HSprites {

	private HSprites() {
		
	}
	
	public static final HSprite STAND = new HSprite.Standing(0, true, 
			ITORSO_STILL
	);
	
	public static final HSprite CARRY = new HSprite.Standing(0, true, 
			ITORSO_CARRY
	);
	
	public static final HSprite ARMS_OUT = new HSprite.Standing(0, true, 
			ITORSO_OUT2
	);
	
	public static final HSprite MOVE = new HSprite.Standing(0, true,
			ITORSO_STILL,
			ITORSO_RIGHT,
			ITORSO_RIGHT2,
			ITORSO_RIGHT,
			ITORSO_STILL,
			ITORSO_LEFT,
			ITORSO_LEFT2,
			ITORSO_LEFT 
			) {
		
		private final double[] ex = new double[] {
				-0.5,
				0.5,
				1,
				0.5,
				-0.5,
				0,
				0,
				0
			};
		
		@Override
		public void tick(Humanoid a, double ds) {
			a.spriteTimer += ds * 20.0* a.speed.magnitudeRelative();
		};
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			double i = ex[(int) timer];
			
			STATS.EQUIP().renderExtra(a, dir, r, s, i, x, y);
			
//			a.race().appearance().extra.weapon.render(r,
//					dir.id() + IWEAPON[qual][i], x, y);
		};
	};
	
	public static final HSprite THROW = new HSprite.Standing(0, true,
			ITORSO_STILL,
			ITORSO_RIGHT,
			ITORSO_RIGHT2
			) {
		
		@Override
		public void tick(Humanoid a, double ds) {
			a.spriteTimer += ds * 5;
		};
	};
	
	public static final HSprite DRAG = new HSprite.Standing(0, true,
			ITORSO_RIGHT2
			) {
		
		@Override
		public void tick(Humanoid a, double ds) {
			a.spriteTimer += ds * 20.0* a.speed.magnitudeRelative();
		};
		@Override
		protected int getDir(Humanoid a, float timer) {
			return a.speed.dir().perpendicular().id();
		};
		
		
	};
	
	public static final HSprite MOVE_SWORD = new HSprite.Standing(0, true,
			ITORSO_STILL,
			ITORSO_LEFT,
			ITORSO_LEFT2,
			ITORSO_LEFT
			) {
		
		private final int[] ex = new int[] {
			1,
			2,
			2,
			1,
		};
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			int i = ex[(int) timer];
			
			STATS.EQUIP().renderExtra(a, dir, r, s, ex[i], x, y);
			
//			a.race().appearance().extra.weapon.render(r,
//					dir.id() + IWEAPON[qual][i], x, y);
		};
		
		@Override
		public void tick(Humanoid a, double ds) {
			a.spriteTimer += ds * 20.0* a.speed.magnitudeRelative();
		};
	};
	
	public static final HSprite MOVE_SWORD_BACK = new HSprite.Standing(0, true,
			ITORSO_STILL,
			ITORSO_LEFT,
			ITORSO_LEFT2,
			ITORSO_LEFT
			) {
		
		private final int[] ex = new int[] {
			1,
			2,
			2,
			1,
		};
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			int i = ex[(int) timer];
			
			STATS.EQUIP().renderExtra(a, dir, r, s, ex[i], x, y);
			
//			a.race().appearance().extra.weapon.render(r,
//					dir.perpendicular().id() + IWEAPON[qual][i], x, y);
		};
		
		@Override
		public void tick(Humanoid a, double ds) {
			a.spriteTimer += ds * 20.0* a.speed.magnitudeRelative();
		};
		
		@Override
		protected int getDir(Humanoid a, float timer) {
			return a.speed.dir().perpendicular().id();
		};
	};
	
	public static final HSprite TOOL_BACK = new HSprite.Standing(8, false,
			ITORSO_RIGHT2,
			ITORSO_RIGHT2,
			ITORSO_RIGHT2, 
			ITORSO_RIGHT2,
			ITORSO_RIGHT2, 
			ITORSO_RIGHT, 
			ITORSO_RIGHT, 
			ITORSO_STILL, 
			ITORSO_STILL
			) {
		
		private final int[] ex = new int[] {
			0,
			0,
			0,
			0,
			0,
			1,
			1,
			2,
			2,
		};
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			int qual = 0;
			int i = ex[(int) timer];
			
			a.race().appearance().extra.tool.render(r,
					dir.id() + ITOOL[qual][i], x, y);
		};
	};
	
	public static final HSprite TOOL_HIT = new HSprite.Standing(32, false,
			ITORSO_STILL, 
			ITORSO_STILL,
			ITORSO_STILL, 
			ITORSO_STILL, 
			ITORSO_STILL,
			ITORSO_STILL,
			ITORSO_STILL,
			ITORSO_STILL,
			ITORSO_STILL,
			ITORSO_STILL,
			ITORSO_STILL,
			ITORSO_STILL,
			ITORSO_STILL,
			ITORSO_RIGHT, 
			ITORSO_RIGHT2
			) {
		
		private final int[] ex = new int[] {
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			1,
			0,
		};
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			int qual = 0;
			a.race().appearance().extra.tool.render(r,
					dir.id() + ITOOL[qual][ex[(int) timer]], x, y);
		};
	};
	
	public static final HSprite BOX = new HSprite.Standing(5, false, 
			ITORSO_LEFT,
			ITORSO_LEFT2,
			ITORSO_LEFT3,
			ITORSO_LEFT2,
			ITORSO_LEFT,
			ITORSO_STILL,
			ITORSO_RIGHT,
			ITORSO_RIGHT2,
			ITORSO_RIGHT3,
			ITORSO_RIGHT2,
			ITORSO_RIGHT
	);
	
	public static final HSprite GRAB = new HSprite.Standing(10, false, 
			ITORSO_LEFT2,
			ITORSO_RIGHT2,
			ITORSO_LEFT2,
			ITORSO_RIGHT2,
			ITORSO_LEFT2,
			ITORSO_RIGHT2,
			ITORSO_LEFT2,
			ITORSO_RIGHT2,
			ITORSO_LEFT2,
			ITORSO_RIGHT2
	);
	
	public static final HSprite FIST = new HSprite.Standing(10, false, 
			ITORSO_STILL,
			ITORSO_RIGHT,
			ITORSO_RIGHT,
			ITORSO_RIGHT,
			ITORSO_RIGHT,
			ITORSO_RIGHT2,
			ITORSO_RIGHT3,
			ITORSO_RIGHT2,
			ITORSO_RIGHT3,
			ITORSO_RIGHT2,
			ITORSO_RIGHT3,
			ITORSO_RIGHT2,
			ITORSO_RIGHT,
			ITORSO_RIGHT,
			ITORSO_STILL
	);
	
	public static final HSprite ARM_RIGHT = new HSprite.Standing(10, false, 
			ITORSO_RIGHT2
	);
	
	public static final HSprite ARM_LEFT = new HSprite.Standing(10, false, 
			ITORSO_LEFT
	);
	
	public static final HSprite ARM_LEFT2 = new HSprite.Standing(10, false, 
			ITORSO_LEFT2
	);
	
	public static final HSprite ARM_LEFT3 = new HSprite.Standing(10, false, 
			ITORSO_LEFT3
	);
	
	public static final HSprite ARM_RIGHT2 = new HSprite.Standing(10, false, 
			ITORSO_RIGHT3
	);
	

	
	public static final HSprite DANCE = new HSprite.Standing(10, false, 
			ITORSO_STILL,
			ITORSO_RIGHT,
			ITORSO_RIGHT2,
			ITORSO_RIGHT,
			ITORSO_STILL,
			ITORSO_LEFT,
			ITORSO_LEFT2,
			ITORSO_LEFT 
	);
	
	public static final HSprite DANCE_EXTRA = new HSprite.Standing(10, false, 
			ITORSO_STILL,
			ITORSO_RIGHT,
			ITORSO_RIGHT2,
			ITORSO_RIGHT3,
			ITORSO_RIGHT2,
			ITORSO_RIGHT,
			ITORSO_STILL,
			ITORSO_LEFT,
			ITORSO_LEFT2,
			ITORSO_LEFT3,
			ITORSO_LEFT2,
			ITORSO_LEFT 
	);
	
	public static final HSprite WAVE = new HSprite.Standing(5, false,
			ITORSO_STILL, 
			ITORSO_OUT, 
			ITORSO_OUT2, 
			ITORSO_OUT
	);
	
	public static final HSprite SWORD_STAND = new HSprite.Standing(0, false, ITORSO_RIGHT) {
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			
			STATS.EQUIP().renderExtra(a, dir, r, s, 1, x, y);
			
			//a.race().appearance().extra.weapon.render(r,dir.id() + SWORD2, x, y);
		};
		
		@Override
		public void render(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {
			super.render(a, d, r, s, ds, x, y);
		};
	};
	
	public static final HSprite SWORD_STAND_SWAY = new HSprite.Standing(0, false, ITORSO_RIGHT) {
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			
			STATS.EQUIP().renderExtra(a, dir, r, s, 1, x, y);
			
			//a.race().appearance().extra.weapon.render(r, dir.id() + SWORD2, x, y);
		};
		
		@Override
		public void render(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {
			int ox = (a.spriteoff & 0x0b01111);
			int oy = ((a.spriteoff >> 4) & 0x0b01111);
			x += (GAME.intervals().get02()&0b011111) > ox ? 1 : 0;
			y += (GAME.intervals().get02()&0b011111) > oy ? 1 : 0;
			
			
			super.render(a, d, r, s, ds, x, y);
		};
	};
	
	public static final HSprite SWORD_STAB = new HSprite.Standing(10, false, 
			ITORSO_STILL, 
			ITORSO_RIGHT, 
			ITORSO_RIGHT2, 
			ITORSO_RIGHT2,
			ITORSO_RIGHT2,
			ITORSO_RIGHT,
			ITORSO_STILL,
			ITORSO_STILL
			) {
		
		private final int[] ex = new int[] {
			0,
			1,
			2,
			2,
			2,
			1,
			0,
			0,
		};
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			
			STATS.EQUIP().renderExtra(a, dir, r, s, ex[(int) timer], x, y);
			
//			a.race().appearance().extra.weapon.render(r,
//					dir.id() + IWEAPON[qual][ex[(int) timer]], x, y);
		};
	};
	
	public static final HSprite SWORD_OUT = new HSprite.Standing(10, false, 
			ITORSO_STILL, 
			ITORSO_RIGHT, 
			ITORSO_RIGHT2, 
			ITORSO_RIGHT2
			) {
		
		private final int[] ex = new int[] {
			0,
			1,
			2,
			2,
		};
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			
			STATS.EQUIP().renderExtra(a, dir, r, s, ex[(int) timer], x, y);
			
//			a.race().appearance().extra.weapon.render(r,
//					dir.id() + IWEAPON[qual][ex[(int) timer]], x, y);
		};
	};
	
	public static final HSprite SWORD_IN = new HSprite.Standing(10, false, 
			ITORSO_RIGHT2,
			ITORSO_RIGHT,
			ITORSO_STILL,
			ITORSO_STILL
			) {
		
		private final int[] ex = new int[] {
			2,
			1,
			0,
			0,
		};
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			
			STATS.EQUIP().renderExtra(a, dir, r, s, ex[(int) timer], x, y);
			
//			a.race().appearance().extra.weapon.render(r,
//					dir.id() + IWEAPON[qual][ex[(int) timer]], x, y);
		};
	};
	
	
	private static class Archer extends HSprite.Standing {
		
		private final double ff;
		
		Archer(double ff, int... torsos) {
			super(0, false, torsos);
			this.ff = ff;
		}

		private EquipRange bow;
		
		@Override
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			
			if (bow != null) {
				bow.sprite.render(a, r, dir, ff, x, y, s);
			}
		};
		
		@Override
		public void render(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {
			int ox = (a.spriteoff & 0x0b01111);
			int oy = ((a.spriteoff >> 4) & 0x0b01111);
			x += (GAME.intervals().get02()&0b011111) > ox ? 1 : 0;
			y += (GAME.intervals().get02()&0b011111) > oy ? 1 : 0;
			bow = null;
			if (a.division() != null) {
				bow = a.division().settings().ammo();
			}
			
			super.render(a, d, r, s, ds, x, y);
		};
		
	}
	
	public static final HSprite ARCHER1 = new Archer(0, ITORSO_STILL);
	public static final HSprite ARCHER2 = new Archer(0.25, ITORSO_LEFT);
	public static final HSprite ARCHER3 = new Archer(0.5, ITORSO_LEFT2);
	public static final HSprite ARCHER4 = new Archer(0.75, ITORSO_LEFT3);
	
	
	public static final HSprite SLEEP = new HSprite(10f, 0.1) {
		

		@Override
		public void render(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {
			


			int n = STATS.POP().NAKED.get(a.indu());
			STATS.POP().NAKED.set(a.indu(), 1);
			LAY.render(a, d, r, s, ds, x, y);
			x += CLAY.off;
			y += CLAY.off;
			STATS.POP().NAKED.set(a.indu(), n);
			int dir = a.speed.dir().id();
			int k = dir+(a.spriteoff&1)*8;
			a.race().appearance().sleep.render(r, k, x, y);
			
			

		}

		@Override
		public void tick(Humanoid a, double ds) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void renderSimple(Humanoid a, AIManager ai, Renderer r, ShadowBatch shadows, float ds, int x, int y) {
			int n = STATS.POP().NAKED.get(a.indu());
			STATS.POP().NAKED.set(a.indu(), 1);
			LAY.renderSimple(a, ai, r, shadows, ds, x, y);
			x += CLAY.off;
			y += CLAY.off;
			STATS.POP().NAKED.set(a.indu(), n);
			int dir = a.speed.dir().id();
			int k = dir+(a.spriteoff&1)*8;
			a.race().appearance().sleep.render(r, k, x, y);
		}

	};

	public static final HSprite LAY = new HSprite(10f, 0.1) {

		@Override
		public void render(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {
			
			Induvidual in2 = a.indu();
			StatsAppearance ap = STATS.APPEARANCE();
			
			x += CLAY.off;
			y += CLAY.off;

			TILE_SHEET sheet = a.race().appearance().sheet(in2).sheet.lay;
			int dir = a.speed.dir().id();

			if (a.physics.getZ() != 0) {
				int t = (int) (a.spriteTimer);
				if (t > 8) {
					a.spriteTimer = 0;
					t = 0;
				}
				dir += t;
				dir &= 7;
				s.setHeight(0).setDistance2Ground(a.physics.getZ());
				sheet.render(s, CLAY.SHADOW + dir, x, y);
			} else if (!a.inWater) {
				s.setHeight(5).setDistance2Ground(0);
				sheet.render(s, CLAY.SHADOW + dir, x, y);
				SETT.PARTICLES().renderDust(x + CLAY.offC, y + CLAY.offC, a.speed.magnitude());
			}

			for (RAddon add : in2.race().appearance().types.getC(ap.gender.get(in2)).addonsBelow) {
				add.renderLaying(r, dir, x, y, in2, false);
			}
			
			boolean naked =  STATS.POP().NAKED.get(in2) == 1;
			if (!naked)
				ap.colorLegs(in2).bind();
			else
				ap.colorSkin(in2).bind();
			sheet.render(r, CLAY.PANTS + dir, x, y);
			ap.colorSkin(in2).bind();
			sheet.render(r, CLAY.ARMS + dir, x, y);
			sheet.render(r, CLAY.HEAD + dir, x, y);
			
			if (!naked)
				ap.colorClothes(in2).bind();
			else
				ap.colorSkin(in2).bind();
			sheet.render(r, CLAY.TORSO + dir, x, y);
			
			for (RAddon add : in2.race().appearance().types.getC(ap.gender.get(in2)).addonsAbove) {
				add.renderLaying(r, dir, x, y, in2, false);
			}
			COLOR.unbind();
			
//			int i = STATS.EQUIP().BATTLEGEAR.stat().indu().get(in2);
//			if (i > 0) {
//				ap.colorArmour(in2).bind();
//				sheet.render(r, CLAY.ARMOR + dir, x, y);
//			}
//			COLOR.unbind();

			OPACITY.O75.bind();
			
			if (a.inWater) {
				CLAY.water(in2, dir, x, y);
			}else {
				CLAY.blood(in2, dir, x, y);
				CLAY.filth(in2, dir, x, y);
			}
			OPACITY.unbind();
		}

		@Override
		public void tick(Humanoid a, double ds) {
			if (a.physics.getZ() != 0) {
				a.spriteTimer += a.speed.magnitudeRelative()*(ds * 15);
			}
			
		}

		@Override
		public void renderSimple(Humanoid a, AIManager ai, Renderer r, ShadowBatch s, float ds, int x, int y) {
			Induvidual in2 = a.indu();
			StatsAppearance ap = STATS.APPEARANCE();
			
			x += CLAY.off;
			y += CLAY.off;

			TILE_SHEET sheet = a.race().appearance().sheet(in2).sheet.lay;
			int dir = a.speed.dir().id();

			if (a.physics.getZ() != 0) {
				int t = (int) (a.spriteTimer);
				if (t > 8) {
					a.spriteTimer = 0;
					t = 0;
				}
				dir += t;
				dir &= 7;
				s.setHeight(0).setDistance2Ground(a.physics.getZ());
				sheet.render(s, CLAY.SHADOW + dir, x, y);
			} else if (!a.inWater) {
				s.setHeight(5).setDistance2Ground(0);
				sheet.render(s, CLAY.SHADOW + dir, x, y);
				SETT.PARTICLES().renderDust(x + CLAY.offC, y + CLAY.offC, a.speed.magnitude());
			}

			boolean naked =  STATS.POP().NAKED.get(in2) == 1;
			if (!naked)
				ap.colorLegs(in2).bind();
			else
				ap.colorSkin(in2).bind();
			sheet.render(r, CLAY.PANTS + dir, x, y);
			ap.colorSkin(in2).bind();
			sheet.render(r, CLAY.ARMS + dir, x, y);
			sheet.render(r, CLAY.HEAD + dir, x, y);
			if (!naked)
				ap.colorClothes(in2).bind();
			else
				ap.colorSkin(in2).bind();
			sheet.render(r, CLAY.TORSO + dir, x, y);
			
			for (RAddon add : in2.race().appearance().types.getC(ap.gender.get(in2)).addonsAbove) {
				add.renderLaying(r, dir, x, y, in2, false);
			}
			COLOR.unbind();
//			
//			int i = STATS.EQUIP().BATTLEGEAR.stat().indu().get(in2);
//			if (i > 0) {
//				ap.colorArmour(in2).bind();
//				sheet.render(r, CLAY.ARMOR + dir, x, y);
//			}
			COLOR.unbind();
			
		}

	};
	
	public static HSprite LAYOFF = new HSprite(10f, 0.1) {

		@Override
		public void render(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {

			x += a.speed.dir().x()*CLAY.off;
			y += a.speed.dir().y()*CLAY.off;
			LAY.render(a, d, r, s, ds, x, y);
		}

		@Override
		public void tick(Humanoid a, double ds) {
			LAY.tick(a, ds);
		}

		@Override
		public void renderSimple(Humanoid a, AIManager ai, Renderer r, ShadowBatch shadows, float ds, int x, int y) {
			x += a.speed.dir().x()*CLAY.off;
			y += a.speed.dir().y()*CLAY.off;
			LAY.renderSimple(a, ai, r, shadows, ds, x, y);
		}

	};
	

	
	
}
