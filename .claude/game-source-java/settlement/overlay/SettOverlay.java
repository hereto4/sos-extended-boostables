package settlement.overlay;

import game.GAME;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.environment.Foundation;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.environment.SettEnvShape;
import settlement.main.SETT;
import settlement.path.components.SComponentChecker;
import settlement.path.finders.SFinderFindable;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentIns;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.service.module.RoomFinderHaser;
import settlement.thing.THINGS.Thing;
import settlement.thing.halfEntity.HalfEntity;
import settlement.tilemap.ground.Ground;
import settlement.tilemap.terrain.TGrowable;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayCooShort;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimapSettConfig;

public final class SettOverlay {

	
	private final LIST<Env> envs;
	private final SComponentChecker cCheck = new SComponentChecker(SETT.PATH().comps.zero);
	{
		D.gInit(this);
	}
	
	public boolean added;
	
	public Addable RESOURCES = new Addable(false, true) {
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			
			if (SETT.MINERALS().getter.is(it.tile())) {
				double v = 1.0; //SETT.MINERALS().amountD.get(it.tile());
				if (!SETT.TERRAIN().CAVE.is(it.tile()))
					renderUnder(v, r, it, false);
				
				if (SETT.ROOMS().map.is(it.tile()))
					return false;
				if (SETT.MINERALS().getter.is(it.tile())) {
					COLOR.unbind();
					double am = 1.0; //0.25 + 0.75*SETT.MINERALS().amountD.get(it.tile());
					ColorImp.TMP.interpolate(COLOR.WHITE20, COLOR.WHITE100, am).bind();;
					int size = (int) (C.TILE_SIZE*(am));
					int off = (C.TILE_SIZE-size)/2;
					renderAbove(am, r, it, true);
					COLOR.unbind();
					SETT.MINERALS().getter.get(it.tile()).resource.icon().render(r, it.x()+off, it.x()+off+size, it.y()+off, it.y()+off+size);
					return true;
				}
			}
			if (SETT.TERRAIN().get(it.tile()) instanceof TGrowable) {
				EDIBLES.renderBelow(r, it);
				EDIBLES.render(r, it);
				return true;
			}
			return false;
		}
		
	};	

	public Addable EDIBLES = new Addable(true, true) {
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			if (SETT.JOBS().getter.get(it.tile()) == null && SETT.TERRAIN().get(it.tile()) instanceof TGrowable) {
				
				if (SETT.TERRAIN().get(it.tile()) instanceof TGrowable) {
					COLOR.unbind();
					double am =1.0;
					ColorImp.TMP.interpolate(COLOR.WHITE20, COLOR.WHITE100, am).bind();;
					int size = (int) (C.TILE_SIZE*(am));
					int off = (C.TILE_SIZE-size)/2;
					COLOR.unbind();
					((TGrowable)SETT.TERRAIN().get(it.tile())).growable.resource.icon().render(r, it.x()+off, it.x()+off+size, it.y()+off, it.y()+off+size);
					
					return true;
				}
			}
			return false;
		}
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			
			double v = 0;
			if (SETT.TERRAIN().get(it.tile()) instanceof TGrowable) {
				TGrowable b= (TGrowable) SETT.TERRAIN().get(it.tile());
				v = 0.5 + 0.5*(double)b.size.DM.get(it.tile());
			}
			renderUnder(v, r, it, false);
		};
	};
	
	public Addable MOISTURE = new Addable(UI.icons().m.fertility.twin(UI.icons().s.drop, DIR.NE, 1), "MOISTURE", Ground.¤¤moisture, D.g("MoistureD", "Highlights moisture of the ground."), true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			double d = SETT.GROUND().MOISTURE_TOT.get(it.tile());
			d = CLAMP.d(d, 0, 1);
			d*=d;
			renderUnder(d, r, it, false);
			if (d > 0.75) {
				d = (d-0.75)*4;
				renderPluses(d, r, it);
			}
		};

		
		
	};
	
	public Addable FERTILITY_BASE = new Addable(UI.icons().m.fertility, "FERTILITY_base", D.g("Soil"), D.g("SoilD", "Type of soil."), true, true) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			double d = SETT.GROUND().MAP.get(it.tile()).farm/SETT.GROUND().types.NORMAL.farm;
			d*= d;
			renderUnder(d, r, it, false);
			if (d > 0.75) {
				d = (d-0.75)*4;
				renderPluses(d, r, it);
			}
		};
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			double d = SETT.GROUND().MAP.get(it.tile()).farm/SETT.GROUND().types.NORMAL.farm;
			d*=d;
			if (renderAbove(d, r, it, false)) {
				if (d > 0.75) {
					d = (d-0.75)*4;
					renderPluses(d, r, it);
				}
				return true;
			}
			return false;
		};
		
	};
	
	public Addable SHAPE = new Addable(UI.icons().m.place_rec, "SHAPE_base", SettEnvShape.¤¤name, "shaped", true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			
			COLOR c = COLOR.WHITE05;
			boolean base = false;
			if (SETT.ROOMS().map.is(it.tile()) && !SETT.ROOMS().map.get(it.tile()).blueprint().registersEnvironment()) {
				
			}else {
				if (SETT.ENV().map.SHAPE.round.is(it.tile())) {
					c = GCOLOR.MAP().OVERLAY_GOOD;
				}
				else if (SETT.ENV().map.SHAPE.square.is(it.tile()))
					c = GCOLOR.MAP().OVERLAY_BAD;
				base = VIEW.s().getWindow().zoomout() <= 1 && SETT.ENV().map.SHAPE.isBase(it.tx(), it.ty());
			}

			renderUnder(c, r, it);
			if (base) {
				renderPluses(1.0, r, it);
			}
			
//			DIR d = SETT.ENV().environment.SHAPE.getWallDIR(it.tx(), it.ty());
//			if (d != null) {
//				COLOR.WHITE100.bind();
//				UI.FONT().S.render(r, d.name(), it.x(), it.y());
//			}
		};

		
	};
	
	public Addable FOUNDATION = new Addable(UI.icons().m.foundation, "FOUNDATION", Foundation.¤¤name, Foundation.¤¤desc, true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			
			renderUnder(SETT.ENV().foundation.get(it.tx(), it.ty()), r, it);
		};

		
	};
	
	
	public OverlayMaintenance MAINTENANCE = new OverlayMaintenance();
	
	public final Addable ROOM_PROBLEM = new RoomProblem();
	
	public Addable ROADING = new Addable(SETT.FLOOR().defaultRoad.getIcon(), "ROADING", D.g("Path-Usage"), D.g("Path-UsageD", "Highlights the tiles your subjects use when moving."), true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			if (SETT.ROOMS().map.is(it.tile()))
				return;
			if (SETT.JOBS().getter.get(it.tile()) != null)
				return;
			
			double p = SETT.PATH().huristics.getter.get(it.tile())*16;
			p = CLAMP.d(p, 0, 1);

//			if (JobBuildRoad.problem(it.tx(), it.ty()) != null) {
//				return;
//			}

			
			double d = 0.25 + SETT.PATH().huristics.getter.get(it.tile())*8;
			d = CLAMP.d(d, 0, 1);
			renderUnder(d, r, it, false);
			ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLOR.MAP().OVERLAY_GOOD, p).bind();;
			renderPluses(p, r, it);
			
			if (SETT.FLOOR().getter.is(it.tile()) || SETT.JOBS().jobGetter.is(it.tile())) {
				ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLOR.MAP().OVERLAY_GOOD, p).bind();;
				renderPluses(p, r, it);
				return;
			}
		};
	};
	
	public Addable RODIFY = new Addable(null, null, null, null, true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			renderUnder(SETT.FLOOR().floorundernot.is(it.tile()) ? 0 : 1, r, it, false);
		};
	};
	
	public Addable PAINTER = new Addable(UI.icons().m.place_brush, "MAP_PAINTED", D.g("Paint-Tool"), D.g("Paint-ToolD", "shows your manual paintings on the map."), true, true) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			int ci = SETT.JOBS().paintmap.get(it.tile());
			COLOR c = ci == 0 ? COLOR.WHITE10 : COLOR.UNIQUE.get(ci);
			renderUnder(c, r, it);
		};
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			int ci = SETT.JOBS().paintmap.get(it.tile());
			COLOR c = ci == 0 ? COLOR.WHITE10 : COLOR.UNIQUE.get(ci);
			return renderAbove(c, r, it);
		};
	};
	
	public Addable WORKLOAD = new Addable(UI.icons().m.workshop, "WORKLOAD", RoomEmploymentIns.¤¤Workload, RoomEmploymentIns.¤¤WorkloadD, true, false) {
		
		private final ColorImp c = new ColorImp();
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			c.set(COLOR.WHITE05);
			Room ro = SETT.ROOMS().map.get(it.tx(), it.ty());
			if (ro != null && ro instanceof RoomInstance) {
				RoomInstance i = (RoomInstance) ro;
				if (i.blueprintI().employment() != null) {
					c.interpolate(GCOLOR.MAP().OVERLAY_BAD, GCOLOR.MAP().OVERLAY_GOOD, i.employees().efficiency());
				}
				
			}
			renderUnder(c, r, it);
		}
	};
	public final OverlayPull PULL = new OverlayPull();
	
	private final ServiceRadius service = new ServiceRadius();
	private final RadiusInter radius = new RadiusInter();
	private final RoomRadius roomRadius = new RoomRadius(cCheck);
	
	private Mon mon = new Mon();
	
	public Addable monument(ROOM_MONUMENT m) {
		mon.set(m);
		mon.add();
		return mon;
	}
	
	public void monument(ROOM_MONUMENT m, FurnisherItem it, int x1, int y1, int radius) {
		mon.set(m, it, x1, y1, radius);
	}
	
	public void service(RoomFinderHaser blue) {
		service.add(blue);
	}

	public void RadiusInter(RoomBlueprintIns<? extends RADIUS_INTER> blue, SFinderFindable fin) {
		radius.add(blue, fin);
	}

	public void RadiusInter(RoomBlueprintIns<? extends RADIUS_INTER> blue, SFinderFindable fin, int tx, int ty, double ra) {
		radius.add(blue, fin, tx, ty, ra);
	}

	public void roomRadius(RoomInstance ins, int radius) {
		roomRadius.add(ins, radius);
	}
	
	
	public Addable HOMELESS = new Homeless("HOMELESS", D.g("Homeless"), D.g("HomelessD", "Highlights homeless workplaces and homeless oddjobbers."));
	
	public LIST<Addable> all(){
		return Addable.ALL;
	}
	
	private ArrayList<Addable> tmp;
	
	private final ArrayList<ON_TOP_TILE> tiles = new ArrayList<>(100);
	private final ArrayList<BODY_HOLDER> objects = new ArrayList<>(100);
	private final ArrayCooShort rooms = new ArrayCooShort(5);
	private final COLOR[] colors = new COLOR[100];
	

	
	public boolean renderOnGround(Renderer r, RenderData data, int zoomout) {
		added = false;
		Addable aa = getUnder();
		for (Addable a : Addable.ALL) {
			a.added = false;
		}
		
		if (aa == null)
			return false;
		
		
		
		r.newLayer(true, zoomout);
		
		RenderIterator it = data.onScreenTiles();
		aa.initBelow(data);
		while(it.has()) {
			aa.renderBelow(r, it);
			it.next();
		}
		aa.finishBelow();
		COLOR.unbind();
		return true;
	}
	
	private Addable getUnder() {
		Addable aa = null;
		for (Addable a : Addable.ALL) {
			if (a.added && a.under) {
				aa = a;
			}
		}
		return aa;
	}
	
	private void prune() {
		Addable aa = null;
		for (Addable a : Addable.ALL) {
			if (a.added && a.exclusive) {
				if (aa != null)
					a.added = false;
				aa = a;
			}
		}
	}
	
	public void renderAbove(Renderer r, RenderData data, int zoomout) {
		
		if (tmp == null || tmp.size() != Addable.ALL.size()) {
			tmp = new ArrayList<Addable>(Addable.ALL.size());
		}
		
		tmp.clear();
		prune();
		for (Addable a : Addable.ALL) {
			if (a.added && a.above) {
				a.initAbove(data);
				tmp.add(a);
			}
		}
		r.newLayer(true, zoomout);
		RenderIterator it = data.onScreenTiles();
		
		
		while(it.has()) {
			
			for (Addable a : tmp) {
				if (a.render(r, it))
					break;
			}
			
			it.next();
		}
		
		for (Addable a : tmp) {
			a.finishAbove();
		}
		ents(r, data);
	}
	
	private void ents(Renderer r, RenderData data) {
		if (objects.size() == 0 && rooms.getI() == 0 && tiles.size() == 0)
			return;
		
		for (int i = 0; i < objects.size(); i++) {
			colors[i].bind();
			BODY_HOLDER e = objects.get(i);
			SPRITES.cons().BIG.outline.renderBox(r, e.body().x1() - data.offX1(), e.body().y1() - data.offY1(), e.body().width(), e.body().height());
		}
		
		
		objects.clear();
		
		int rI = rooms.getI();
		for (int i = 0; i < rI; i++) {
			COORDINATE c = rooms.set(i);
			Room room = SETT.ROOMS().map.get(c);
			if (room != null) {
				

				if (GAME.ARMIES().map.army.get(room.mX(c.x(), c.y()), room.mY(c.x(), c.y())) == GAME.ARMIES().enemy())
					COLOR.RED2RED.bind();
				else
					GCOLOR.MAP().OK_2_BETTER.bind();
				int x1 = room.x1(c.x(), c.y());
				int x2 = x1 + room.width(c.x(), c.y());
				int y1 = room.y1(c.x(), c.y());
				int y2 = y1 + room.height(c.x(), c.y());
				int mx = room.mX(c.x(), c.y());
				int my = room.mY(c.x(), c.y());
				for (int ty =  y1-1; ty <= y2; ty++) {
					for (int tx =  x1-1; tx <= x2; tx++) {
						if (room.isSame(mx, my, tx, ty))
							continue;
						
						int m = 0;
						for (int di = 0; di < DIR.ORTHO.size(); di++) {
							DIR d = DIR.ORTHO.get(di);
							if (!room.isSame(mx, my, tx+d.x(), ty+d.y()))
								m |= d.mask();
						}
						if (m != 0x0F) {
							int x = tx*C.TILE_SIZE - data.offX1();
							int y = ty*C.TILE_SIZE - data.offY1();
							SPRITES.cons().BIG.outline.render(r, m, x, y);
						}
					}
						
				}
			}
		}
		rooms.set(0);
		
		COLOR.unbind();
		
		for (ON_TOP_TILE t : tiles) {
			t.render(r, null, data);
		}
		
		tiles.clearSloppy();
		
	}
	
	public Addable envThing(SettEnv t) {
		return envs.get(t.index());
	}
	
	public void add(int rx, int ry) {
		if (rooms.getI() < rooms.size()-1) {
			rooms.set(rooms.getI()).set(rx, ry);
			rooms.set(rooms.getI()+1);
		}
	}
	
	public void add(ENTITY e) {
		if (e instanceof Humanoid) {
			Humanoid a = (Humanoid) e;
			if (a.indu().hostile())
				add(e, UIMinimapSettConfig.colHostile);
			else
				add(e, UIMinimapSettConfig.colNormal);
		}else
			add(e, UIMinimapSettConfig.colAnimal);
	}
	
	public void add(Thing t) {
		add(t, COLOR.WHITE2WHITE);
	}
	
	public void add(HalfEntity t) {
		add(t, COLOR.WHITE2WHITE);
	}
	
	public void add(ON_TOP_TILE t) {
		if (tiles.contains(t))
			return;
		tiles.add(t);
	}
	
	public void add(BODY_HOLDER object, COLOR c) {
		if (!objects.hasRoom())
			return;
		objects.add(object);
		int i = objects.size()-1;
		colors[i] = c;
	}

	
	public SettOverlay(){
		LinkedList<Env> ee = new LinkedList<>();
		for (SettEnv s : SETT.ENV().map.all()) {
//			if (s == SETT.ENV().environment.ROUNDNESS) {
//				ee.add(new Env(adders, s, true) {
//					@Override
//					public boolean render(Renderer r, RenderIterator it) {
//						if (envThing.getBaseValue(it.tx(), it.ty()) > 0) {
//							GCOLOR.MAP().OVERLAY_GOOD.bind();
//							UI.icons().m.expand.render(r, it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE);
//						}
//						return false;
//					}
//				});
//				
//				
//			}else {
//				ee.add(new Env(adders, s, false));
//			}
			ee.add(new Env(s, false));
			
		}
		this.envs = new ArrayList<Env>(ee);
	}
	
	

	
}
