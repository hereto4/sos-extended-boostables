package settlement.entity.humanoid;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.type.CAUSE_ARRIVE;
import init.type.CAUSE_ARRIVES;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.ECollision;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.ai.main.AI;
import settlement.main.SETT;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GButt.Panel;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PlacableSimple;

public final class Humanoids {
	
	{AI.init();}
	public final SoundRace sound = AUDIO.race("BODY_EXPLODE");
	
	public Humanoids(){

		final LinkedList<PLACABLE> all = new LinkedList<PLACABLE>();
		HTYPE[] tt = new HTYPE[] {
			HTYPES.SUBJECT(), HTYPES.PRISONER(), HTYPES.SLAVE(), HTYPES.CHILD(), HTYPES.DERANGED(), HTYPES.ENEMY(), HTYPES.RIOTER()
		};
		for (HTYPE t : tt) {
			for (Race r : RACES.all()) {
				Placer p = new Placer(r, t);
				all.add(p);
			}
		}
		
		PlacableSimple death = new PlacableSimple("kill", "") {
			
			private CAUSE_LEAVE cause = CAUSE_LEAVES.AGE();
			ArrayList<CLICKABLE> butts = new ArrayList<CLICKABLE>(CAUSE_LEAVES.ALL().size());
			
			
			{
				for (CAUSE_LEAVE l : CAUSE_LEAVES.ALL()) {
					
					butts.add(new Panel(SPRITES.icons().s.dot) {
						@Override
						protected void clickA() {
							cause = l;
						};
						
						@Override
						protected void renAction() {
							selectedSet(cause == l);
						};
					}.hoverInfoSet(l.desc));
				}
			}
			
			
			@Override
			public PLACABLE getUndo() {
				return null;
			}
			
			@Override
			public SPRITE getIcon() {
				return SPRITES.icons().m.cancel;
			}

			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						((Humanoid) e).kill(false, cause);
						return;
					}
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						return null;
					}
				}
				return E;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
		};
		
		PlacableSimple attack = new PlacableSimple("attack", "") {
			
			private CAUSE_LEAVE cause = CAUSE_LEAVES.SLAYED();
			private final ECollision coll = new ECollision();
			
			
			
			@Override
			public PLACABLE getUndo() {
				return null;
			}
			
			@Override
			public SPRITE getIcon() {
				return SPRITES.icons().m.cancel;
			}

			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						kill((Humanoid) e, x, y);
						return;
					}
				}
			}
			
			private void kill(Humanoid e, int x, int y) {
				VectorImp vec = new VectorImp();
				double m = vec.set(x, y, e.body().cX(), e.body().cY());
				
				e.speed.setRaw(vec.nX()*m*C.TILE_SIZEH+e.speed.x(), vec.nY()*m*C.TILE_SIZEH+e.speed.y());
				
				coll.damagetileStrength = 0;
				coll.tileMomentum = 0;
				for (int i = 0; i < coll.damage.length; i++)
					coll.damage[i] = 0;
				coll.dirDot = 1;
				coll.dirDotOther = 1;
				coll.norX = 0.5;
				coll.norY = 0.5;
				coll.speedHasChanged = true;
				coll.other = null;
				e.collide(coll);
				if (!e.isRemoved())
					e.kill(true, cause);
				
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						return null;
					}
				}
				return E;
			}

		};
		all.add(attack);
		all.add(death);
		
		PlacableSimple explode = new PlacableSimple("explode", "") {
			
			private CAUSE_LEAVE cause = CAUSE_LEAVES.SLAYED();
			
			
			@Override
			public PLACABLE getUndo() {
				return null;
			}
			
			@Override
			public SPRITE getIcon() {
				return SPRITES.icons().m.cancel;
			}

			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						((Humanoid) e).inflictDamage(10, cause);
						return;
					}
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						return null;
					}
				}
				return E;
			}

		};
		
		all.add(explode);
		
		IDebugPanelSett.add("humanoids", new ArrayList<PLACABLE>(all));
		
		
		
	}
	
	public Humanoid create(Race r, int tx, int ty, HTYPE t, CAUSE_ARRIVE cause) {
		if (!SETT.IN_BOUNDS(tx, ty))
			throw new RuntimeException(tx + " " + ty);
		int x = tx*C.TILE_SIZE + C.TILE_SIZEH;
		int y = ty*C.TILE_SIZE + C.TILE_SIZEH;
		return new Humanoid(x, y, r, t, cause);
	}
	
	private class Placer extends PlacableSimple{

		private final Race r;
		private final HTYPE f;
		
		private Placer(Race r, HTYPE f) {
			super(r.info.name + " " + f.name, "");
			this.r = r;
			this.f = f;
		}
		
		@Override
		public SPRITE getIcon() {
			return r.appearance().icon;
		}

		@Override
		public void place(int x, int y) {
			if (isPlacable(x, y) == null){
				new Humanoid(x,y,r, f, CAUSE_ARRIVES.IMMIGRATED());
				return;
			}
		}

		@Override
		public CharSequence isPlacable(int x, int y) {
			int x1 = (x-r.physics.hitBoxsize()/2)/C.TILE_SIZE;
			int x2 = (x+r.physics.hitBoxsize()/2)/C.TILE_SIZE;
			int y1 = (y-r.physics.hitBoxsize()/2)/C.TILE_SIZE;
			int y2 = (y+r.physics.hitBoxsize()/2)/C.TILE_SIZE;
			if (!IN_BOUNDS(x1, y1) ||!IN_BOUNDS(x2, y2))
				return E;
			return !PATH().solidity.is(x1, y1) && !PATH().solidity.is(x2, y1) &&
					!PATH().solidity.is(x1, y2) && !PATH().solidity.is(x2, y2) &&
					ENTITIES().getAtPoint(x,y) == null ? null : E;
		}

		@Override
		public PLACABLE getUndo() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	
}
