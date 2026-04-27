package settlement.thing;

import java.io.IOException;
import java.io.Serializable;

import game.GAME;
import init.constant.C;
import init.resources.RESOURCE;
import init.settings.S;
import settlement.entity.animal.AnimalSpecies;
import settlement.main.SETT;
import settlement.thing.DRAGGABLE.DRAGGABLE_HOLDER;
import settlement.thing.THINGS.Thing;
import settlement.thing.THINGS.ThingFactory;
import snake2d.LOG;
import snake2d.Renderer;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;
import util.text.D;
import util.updating.IUpdater;
import view.sett.SETT_HOVERABLE;

public class ThingsCadavers extends ThingFactory<ThingsCadavers.Cadaver> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Cadaver[] cadavers = new Cadaver[1024];
	private final IUpdater updater = new IUpdater(cadavers.length, 5.0) {


		@Override
		protected void update(int i, double timeSinceLast) {
			if (!cadavers[i].isRemoved())
				cadavers[i].update((float) timeSinceLast);
			
		}
	};
	
	
	
	ThingsCadavers(LISTE<ThingFactory<?>> all) {
		super(all, 1024);
		for (int i = 0; i < cadavers.length; i++) {
			cadavers[i] = new Cadaver(i);
		}
	}
	
	public final DRAGGABLE_HOLDER draggable = new DRAGGABLE_HOLDER() {
		
		@Override
		public DRAGGABLE draggable(short index) {
			return cadavers[index];
		}
	};
	
	@Override
	protected Cadaver[] all() {
		return cadavers;
	}
	
	@Override
	protected void save(FilePutter file) {
		updater.save(file);
		super.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		updater.load(file);
		super.load(file);
	}
	
	
	public Cadaver gore(int cx, int cy, AnimalSpecies s) {
		Cadaver c = nextInLine();
		c.gore(cx, cy, s);
		return c;
	}
	
	public Cadaver normal(int cx, int cy, double weight, float damage, AnimalSpecies s, int rot) {
		Cadaver c = nextInLine();
		c.normal(cx, cy, (float) (1.0-damage), rot, s, weight);
		return c;
	}
	
	public Cadaver skelleton(int cx, int cy, AnimalSpecies spec, int rot) {
		Cadaver c = nextInLine();
		c.skelleton(cx, cy, rot, spec);
		return c;
	}
	
	public Cadaver rotten(int cx, int cy, AnimalSpecies spec, int rot) {
		Cadaver c = nextInLine();
		c.rotten(cx, cy, rot, spec);
		return c;
	}
	
	@Override
	void update(double ds) {
		updater.update(ds);
	}
	
	public Cadaver getByIndex(int index) {
		return cadavers[index];
	}
	
	private static String ¤¤name = "Cadaver";
	private static String ¤¤noRes = "No resources";
	
	static {
		D.ts(ThingsCadavers.class);
	}
	
	public static class Cadaver extends Thing implements SETT_HOVERABLE, DRAGGABLE{
	
		private final static byte stateGore = 0;
		private final static byte stateNormal = 1;
		private final static byte stateRotten = 2;
		private final static byte stateSkelleton = 3;
		
		private byte state;

		private static final ColorImp decayColor = new ColorImp(50,50,50);
		private short resExtracted;
		private short resAvailable;
		private short weight;
		
//		private short meat;
//		private short fur;
//		private float dPart;
		private float timer;
		private Rec body = new Rec(AnimalSpecies.SIZE);
		private byte rot;
		private byte ran;
		private float statef;
		private byte spec;
		
		Cadaver(int index){
			super(index);
		}
		
		@Override
		protected void save(FilePutter f) {
			f.b(state);
			f.s(resExtracted);
			f.s(resAvailable);
			f.s(weight);
			f.f(timer);
			body.save(f);
			f.b(rot);
			f.b(ran);
			f.f(statef);
			SETT.ANIMALS().map.saver().save(spec(), f);
		}
		
		@Override
		protected void load(FileGetter f) throws IOException {
			state = f.b();
			resExtracted = f.s();
			resAvailable = f.s();
			weight = f.s();
			timer = f.f();
			body.load(f);
			rot = f.b();
			ran = f.b();
			statef = f.f();
			spec = (byte) SETT.ANIMALS().map.loader().loadB(f, null).index();
		}
		
		@Override
		public RECTANGLE body() {
			return body;
		}
		
		void gore(int cx, int cy, AnimalSpecies spec) {
			state = stateGore;
			timer = 120;
			body.moveC(cx, cy);
			rot = (byte) RND.rInt(8);
			this.spec = (byte) spec.index();
			this.resAvailable = 0;
			add();
		}
		
		void normal(int cx, int cy, float state, int rot, AnimalSpecies spec, double weight) {
			this.state = stateNormal;
			timer = 360*8;
			body.setDim(spec.hitBoxSize());
			body.moveC(cx, cy);
			this.rot = (byte) rot;
			this.spec = (byte) spec.index();
			statef = state;
			this.resExtracted = 0;
			this.weight = (short) Math.ceil(weight);
			resAvailable = 0;
			for (int i =  0; i < spec().resources().size(); i++) {
				resAvailable += spec().resAmount(i,this.weight);
			}
			
			add();
		}
		
		public AnimalSpecies spec() {
			return SETT.ANIMALS().species.get(spec);
		}
		
		void rotten(int cx, int cy, int rot, AnimalSpecies spec) {
			state = stateRotten;
			timer = 260;
			body.moveC(cx, cy);
			rot = (byte) rot;
			this.spec = (byte) spec.index();;
			this.resAvailable = 0;
			add();
		}
		
		void skelleton(int cx, int cy, int rot, AnimalSpecies spec) {
			state = stateSkelleton;
			timer = 360;
			body.moveC(cx, cy);
			rot = (byte) rot;
			this.spec = (byte) spec.index();;
			this.resAvailable = 0;
			add();
		}
		
		@Override
		public void render(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY) {
			int x = body.cX()-AnimalSpecies.SIZE/2 + offsetX;
			int y = body.cY()-AnimalSpecies.SIZE/2 + offsetY;
			
			
			SETT.ANIMALS().renderCorpse(spec(), r, shadows, ds, x, y, state, rot, ran, statef, decayColor);
			
			
		}
		
		protected void update(float ds) {
			
			timer -= ds;
			
			if (timer > 0)
				return ;
			
			if (state == stateGore) {
				remove();
			}else if(state == stateNormal) {
				state = stateRotten;
				timer = 260;
				this.resAvailable = 0;
			}else if(state == stateRotten) {
				state = stateSkelleton;
				timer = 360;
				this.resAvailable = 0;
			}else if(state == stateSkelleton) {
				remove();
			}else {
				throw new RuntimeException();
			}
		
		}
		
		public boolean resHas() {
			return state == stateNormal && resExtracted < resAvailable;
		}
		
		public RESOURCE resRemove() {
			int am = 0;
			for (int i =  0; i < spec().resources().size(); i++) {
				am += spec().resAmount(i,weight);
				if (resExtracted < am) {
					resExtracted ++;
					if (resExtracted == resAvailable) {
						state = stateSkelleton;
						timer = 260;
					}
					double sf = (double)resExtracted/resAvailable;
					if (sf > statef)
						statef = (float) sf;
					return spec().resources().get(i);
				}
			}
			{
				GAME.Notify(resExtracted + " " + am + " " +resAvailable);
				state = stateSkelleton;
				timer = 260;
				return spec().resources().get(0);
			}
		}
		
		public void setInjuries(double inj) {
			this.statef = (float) inj;
		}
		
		void rem() {
			super.remove();
		}


		
		
		
		@Override
		public void hover(GBox text) {
			text.textL(¤¤name);
			text.NL();
			if (!S.get().developer)
				return;
			if (!resHas())
				text.text(¤¤noRes);
			else {
				int am = 0;
				for (int i =  0; i < spec().resources().size(); i++) {
					int tot = spec().resAmount(i,weight);
					am += tot;
					int a = am-resExtracted;
					a = CLAMP.i(a, 0, tot);
					text.setResource(spec().resources().get(i), a, tot);
				}
			}
			if (S.get().developer) {
				text.add(text.text().add(timer));
				text.add(text.text().add(state));
			}
		}

		@Override
		protected int z() {
			return 99;
		}
		
		@Override
		public ThingFactory<?> factory() {
			return SETT.THINGS().cadavers;
		}
		
		@Override
		public void drag(DIR d, int cx, int cy, int fromDist) {
			
			rot = (byte) d.perpendicular().id();
			body.moveC(cx-fromDist*d.xN(), cy-fromDist*d.yN());
			if (body.cX() < 0)
				body.moveCX(0);
			if (body.cX() >= SETT.PIXEL_BOUNDS.x2())
				body.moveCX( SETT.PIXEL_BOUNDS.x2()-1);
			if (body.cY() < 0)
				body.moveCY(0);
			if (body.cY() >= SETT.PIXEL_BOUNDS.y2())
				body.moveCY( SETT.PIXEL_BOUNDS.y2()-1);
			
			if (COORDINATE.tileDistance(cx, cy, body().cX(), body().cY()) > 3*C.TILE_SIZE)
				LOG.ln(cx + " " + cy + " " + fromDist);
			
			super.move();
		}

		@Override
		public void drag(DIR d, int cx, int cy) {
			drag(d, cx, cy, body.width());
			
		}

		@Override
		public boolean canBeDragged() {
			return !isRemoved();
		}

		public void makeSkelleton() {
			resExtracted = resAvailable;
			state = stateSkelleton;
			timer = 360;
		}
		
	}



	
}
