package game.events.disaster;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.constant.C;
import init.type.CAUSE_LEAVES;
import init.type.POP_CL;
import settlement.entity.ECollision;
import settlement.entity.ENTITY;
import settlement.entity.EPHYSICS;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.MATH;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimple;
import view.ui.message.MessageSection;

public class EventAccident extends EventResource{
	
	private final double RADIUS = C.TILE_SIZE*20;
	private final Rec bounds = new Rec(RADIUS*2);
	private final VectorImp tVec = new VectorImp();
	
	private final ECollision coll = new ECollision();
	
	private static CharSequence ¤¤Accident = "¤Accident!";
	private static CharSequence ¤¤AccidentD = "¤An accident has occurred. {0} subjects were injured and will seek out a hospital. There were {1} deaths.";
	private static CharSequence ¤¤Go = "¤Go to Site";
	
	
	private double[] timers = new double[SETT.ROOMS().all().size()];
	private double timer = 0;
	private final double acI = (timers.length/(TIME.secondsPerDay()*16.0));
	
	static {
		D.ts(EventAccident.class);
	}
	
	public EventAccident() {
		super("ACCIDENTS");
		IDebugPanelSett.add(new PlacableSimple("event: accident") {
			
			@Override
			public void place(int x, int y) {
				ENTITY e = SETT.ENTITIES().getAtPoint(x, y);
				if (e != null && e instanceof Humanoid) {
					create((Humanoid) e);
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				ENTITY e = SETT.ENTITIES().getAtPoint(x, y);
				if (e != null && e instanceof Humanoid) {
					return null;
				}
				return E;

			}
		});
		clear();
	}
	
	@Override
	protected void update(double ds) {
		int i = (int) timer;
		timer += ds;
		if (i != (int) timer) {
			RoomBlueprint b = SETT.ROOMS().all().get(i);
			
			if ((b instanceof RoomBlueprintIns<?>)) {
				RoomBlueprintIns<?> ins = (RoomBlueprintIns<?>) b;
				if (b != null && b.employment() != null) {
					
					
					
					double c = acI*b.employment().accidentsPerYear*b.employment().employed();
					c /= BOOSTABLES.CIVICS().ACCIDENT.get(POP_CL.clP());
					c = CLAMP.d(c, 0, 1);
					
					
					timers[i] -= c;
					

					
					if (timers[i] < -10) {
						create(ins);
					}
				}
			}
				
			
		}
		
		if (timer >= timers.length)
			timer -= timers.length;
		
	}

	@Override
	protected void save(FilePutter file) {
		SETT.ROOMS().collection.saver().save(timers, file);
		file.d(timer);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		SETT.ROOMS().collection.loader().load(timers, file, 0);
		timer = file.d();
		
	}

	@Override
	protected void clear() {
		Arrays.fill(timers, 0);
		timer = 0;
	}
	
	public boolean create(RoomBlueprintIns<?> b){
		
		if (b.employment().employed() <= 0) {
			timers[b.index()] = 0;
			return false;
		}
		
		if (!MATH.isWithin(TIME.days().bitPartOf(), b.employment().getShiftStart()+0.1, b.employment().getShiftStart()+0.6)) {
			return false;
		}
		
		int emp = RND.rInt(b.employment().employed());
		for (int i = 0; i < b.instancesSize(); i++) {	
			RoomInstance ins = b.getInstance((i));
			if (ins.employees().employed() > 0) {
				emp-= ins.employees().employed();
				
				if (emp <= 0) {
					return create(ins);
				}
			}
			
		}
		
		return false;
		
		
	}
	
	public boolean create(RoomInstance ins){
		
		for (Humanoid h : ins.employees().employees()) {
			Room r = STATS.WORK().EMPLOYED.get(h);
			if (r != null && r.blueprint().employment() != null && r == SETT.ROOMS().map.get(h.tc())) {
				int am = create(h);
				timers[ins.blueprint().index()] += am;
				return true;
			}
		}
		return false;
		
	}
	

	
	public int create(Humanoid h) {
		
		Room r = STATS.WORK().EMPLOYED.get(h);
		double cx = h.body().cX()+RND.rSign();
		double cy = h.body().cY()+RND.rSign();
		
		double mom = EPHYSICS.MOM_TRESHOLDI + RND.rFloat()*2*EPHYSICS.MOM_TRESHOLDI;

		
		h.inflictDamage(1.0, CAUSE_LEAVES.getAccident());
		int death = 1;
		int inj = 0;
		
		
		bounds.moveC(cx, cy);
		SETT.THINGS().gore.debris((int)cx, (int)cy, 0, 0);
		
		
		
		
		
		for (ENTITY e : SETT.ENTITIES().fill(bounds)) {
			
			if (SETT.ROOMS().map.get(e.tc()) != r)
				continue;
			
			double l = tVec.set(cx, cy, e.body().cX(), e.body().cY());
			if (l > RADIUS)
				continue;
			l = 1.0 - (l / RADIUS);
			e.speed.setRaw(e.speed.x()+tVec.nX()*C.TILE_SIZE*3*l, e.speed.y()+tVec.nY()*C.TILE_SIZE*3*l);
			
			coll.dirDot = 1.0;
			coll.tileMomentum = mom*e.physics.getMass();
			coll.damagetileStrength = 0;
			coll.norX = tVec.nX();
			coll.norY = tVec.nY();
			coll.leave = CAUSE_LEAVES.getAccident();
			coll.other = null;
			if (e instanceof Humanoid) {
				Humanoid h2 = (Humanoid) e;
				h2.inflictDamage(l*RND.rFloat()*2.0,  CAUSE_LEAVES.getAccident());
				if (e.isRemoved()) {
					death++;
					continue;
				}else if (!STATS.NEEDS().INJURIES.inDanger(h2.indu())) {
					HEvent.Handler.alertDanger(h2);
				}else {
					inj ++;
				}
			}
			e.collide(coll);
			
		}
		
//		death -= STATS.POP().POP.data().get(null);
//		
		GAME.count().ACCIDENTS.inc(1);
		new M(¤¤Accident, inj, death, h).send();
		return inj + death;
		
	}
	
	private static class M extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int inj;
		private final int deaths;
		private int cx,cy;
		
		public M(CharSequence title, int inj, int deaths, Humanoid h) {
			super(title);
			this.inj = inj;
			this.deaths = deaths;
			cx = h.tc().x();
			cy = h.tc().y();
		}

		@Override
		protected void make(GuiSection section) {
			
			Str s = Str.TMP;
			s.clear();
			s.add(¤¤AccidentD);
			s.insert(0, inj);
			s.insert(1, deaths);
			paragraph(s);
			
			GButt.ButtPanel p = new GButt.ButtPanel(¤¤Go) {
				
				@Override
				protected void clickA() {
					VIEW.s().activate();
					VIEW.s().getWindow().centererTile.set(cx, cy);
					VIEW.messages().hide();
				}
				
			};
			
			section.addRelBody(8, DIR.S, p);
			
		}
		
		
	}

}
