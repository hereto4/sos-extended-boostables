package game.event.actions;

import game.event.actions.EventAction.CInt;
import game.event.engine.EContext;
import game.event.engine.Event;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.POP_CL;
import settlement.entity.ECollision;
import settlement.entity.ENTITY;
import settlement.entity.EPHYSICS;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GRows;
import util.info.GFORMAT;

final class _SUBJECTS_KILL extends EventActionConstructor{
	
	_SUBJECTS_KILL() {
		super("SUBJECTS_KILL");
	}
	
	private final static VectorImp tVec = new VectorImp();
	
	private final static ECollision coll = new ECollision();
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final CAUSE_LEAVE cause;
		private final boolean damage;
		private final boolean useSelection;
		private final int selectionFrom;
		private final int selectionTo;
		private final ArrayListGrower<RAmount> datas = new ArrayListGrower<>();
		
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			cause = CAUSE_LEAVES.MAP().read("DEATH_CAUSE", data);
			RACES.map().new KJson("AMOUNTS", data) {
				
				@Override
				protected void process(Race s, Json j, String key, boolean isWeak) {
					RAmount d = new RAmount(s, new CInt(s.key + "_AMOUNTS"));
					d.read(j.json(key), 0);
					datas.add(d);
				}
			};
			damage = data.bool("DAMAGE", false);
			useSelection = data.bool("USE_SELECTION", false);
			selectionFrom = data.i("SELECTION_FROM", 0, Integer.MAX_VALUE, 0);
			selectionTo = data.i("SELECTION_FROM", 0, selectionFrom, Integer.MAX_VALUE);
			data.checkUnused();
		}

		@Override
		public void setContext(Event event, EContext data) {
			for (RAmount d : datas) {
				if (useSelection) {
					
				}else {
					d.set(event, data, STATS.POP().POP.data().get(d.t));
				}
				
			}
		}
		

		@Override
		public void exe(Event event, EContext data) {

			ENTITY[] ee = SETT.ENTITIES().getAllEnts();
			
			int[] ams = new int[RACES.all().size()];
			for (RAmount a : datas) {
				ams[a.t.index()] += a.amount.get(event, data);
			}
				
			int si = 0;
			for (int ie = 0; ie < ee.length; ie++) {
				ENTITY e = ee[ie];
				if (!(e instanceof Humanoid))
					continue;
				
				Humanoid a = (Humanoid) e;
				
				if (useSelection) {
					if (STATS.EVENT().has(a.indu())) {
						if (si >= selectionFrom && si < selectionTo) {
							slap(data, e, damage ? 1 : 0, cause);
						}
						ie--;
						si++;
						
						
					}
				}else if (ams[a.race().index] > 0){
					slap(data, e, damage ? 1 : 0, cause);
					ams[a.race().index] --;
					ie--;
				}
			}
			
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext data, RECTANGLE messBody) {
			
			GRows rr = new GRows(6).setMin(100);
			if (useSelection) {
				
				for (POP_CL c : POP_CL.ALL()) {
					if (c.race != null && c.cl != null) {
						int am = STATS.EVENT().stat().data(c.cl).get(c.race);
						if (am > 0) {
							rr.add(new GStat() {
								@Override
								public void update(GText text) {
									
									GFORMAT.i(text, -am);
								}
								
								@Override
								public void hoverInfoGet(GBox b) {
									b.title(b.text().add(c.race.info.names).s().add('(').add(c.cl.names).add(')'));
									b.add(GFORMAT.i(b.text(), -am));
									b.NL();
									
								};
								
							}.hh(c.race.appearance().icon.twin(UI.icons().s.death, DIR.NE, 1)));
						}
					}
					
				}
				
			}else {
				for (RAmount d : datas) {
					
					rr.add(new GStat() {
						@Override
						public void update(GText text) {
							
							GFORMAT.i(text, -d.amount.get(event, data));
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							b.title(b.text().add(d.t.info.names));
							b.add(GFORMAT.i(b.text(), -d.amount.get(event, data)));
							b.NL();
							
						};
						
					}.hh(d.t.appearance().icon.twin(UI.icons().s.death, DIR.NE, 1)));
				}
			}
			

			rows.add(rr.rows());
		}
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
			int t = 0;
			if (useSelection) {
				for (POP_CL c : POP_CL.ALL()) {
					if (c.race != null && c.cl != null) {
						int am = STATS.EVENT().stat().data(c.cl).get(c.race);
						if (am > 0) {
							if (t > 5) {
								t = 0;
								b.NL();
							}
							b.tab(t*3);
							t++;
							b.add(c.race.appearance().icon);
							b.add(GFORMAT.i(b.text(),-am));
						}
					}
					
				}
			}else {
				for (RAmount d : datas) {
					
					if (t > 5) {
						t = 0;
						b.NL();
					}
					b.tab(t*3);
					t++;
					b.add(d.t.appearance().icon);
					b.add(GFORMAT.i(b.text(), -d.amount.get(event, context)));
				}
			}
			
			
			
		}
		
	}
	
	static void slap(EContext t, ENTITY e, double dam, CAUSE_LEAVE cause) {
		double mom = EPHYSICS.MOM_TRESHOLDI + RND.rFloat()*2*EPHYSICS.MOM_TRESHOLDI;
		if (dam > 0) {
			e.speed.setRaw(e.speed.x()+RND.rFloat0(1)*C.TILE_SIZE*3, e.speed.y()+RND.rFloat0(1)*C.TILE_SIZE*3);
			
			coll.dirDot = 1.0;
			coll.tileMomentum = mom*e.physics.getMass();
			coll.damagetileStrength = 0;
			coll.norX = tVec.nX();
			coll.norY = tVec.nY();
			coll.leave = CAUSE_LEAVES.getAccident();
			coll.other = null;
			if (e instanceof Humanoid) {
				Humanoid h2 = (Humanoid) e;
				h2.inflictDamage(dam,  cause);
				if (e.isRemoved()) {
					return;
				}else if (!STATS.NEEDS().INJURIES.inDanger(h2.indu())) {
					HEvent.Handler.alertDanger(h2);
				}
			}
			e.collide(coll);
		}
		
		if (!e.isRemoved()) {
			((Humanoid)e).kill(false, cause);
		}
	}
	
	private static class RAmount extends Amount{

		public final Race t;
		
		RAmount(Race res, CInt amount) {
			super(amount);
			this.t = res;
		}
		
		
	}
	
}
