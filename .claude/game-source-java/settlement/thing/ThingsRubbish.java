package settlement.thing;

import static settlement.main.SETT.THINGS;

import java.io.IOException;

import game.time.TIME;
import init.constant.C;
import settlement.main.SETT;
import settlement.thing.THINGS.Thing;
import settlement.thing.THINGS.ThingFactory;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.RecFacade.RecFacadePoint;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import util.rendering.ShadowBatch;
import util.updating.IUpdater;

public class ThingsRubbish{

	
	final RubbishHolder rubbish;
	final RubbishMovingHolder rubbishMoving;
	
	
	public ThingsRubbish(LISTE<ThingFactory<?>> all){
		
		rubbish = new RubbishHolder(all);
		rubbishMoving = new RubbishMovingHolder(all);
		
	}
	
	void update(float ds) {
		rubbish.update(ds);
		rubbishMoving.update(ds);
		
	}
	
	public void throww(int sx, int sy, int destx, int desty) {
		rubbishMoving.make(sx, sy, destx, desty);
	}
	
	static class ThingRubbish extends Thing {

		private final Rec body = new Rec(C.SCALE*8,C.SCALE*8);
		private byte hour;
		private byte ran;
		
		ThingRubbish(int index){
			super(index);
		}
		
		
		protected void init(int cx, int cy, byte ran) {
			body.moveC(cx, cy);
			this.ran = ran;
			hour = (byte) ((TIME.hours().bitsSinceStart()-RND.rInt(TIME.hoursPerDay()/4)) & 0b01111111);
			add();
		}



		@Override
		public RECTANGLE body() {
			return body;
		}


		@Override
		public void render(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY) {
			
			double t = age();
			t /= TIME.hoursPerDay()*0.25;
			t = CLAMP.d(t, 0, 1);
			ColorImp.TMP.interpolate(COLOR.WHITE100, COLOR.DARK_BROWN, t);
			ColorImp.TMP.bind();
			THINGS().sprites.rubbish.render(r, ran&0x0F, body().x1()+offsetX, body().y1()+offsetY);
			COLOR.unbind();
			shadows.setHeight(1).setDistance2Ground(0);
			THINGS().sprites.rubbish.render(shadows, ran&0x0F, body().x1()+offsetX, body().y1()+offsetY);
		}
		
		int age() {
			int h = TIME.hours().bitsSinceStart() & 0b01111111;
			if (h < hour) {
				return hour-h;
				
			}
			h = h-hour;
			return h;
		}

		@Override
		protected int z() {
			return 0;
		}

		@Override
		protected void save(FilePutter f) {
			body.save(f);
			f.b(hour);
			f.b(ran);
		}


		@Override
		protected void load(FileGetter f) throws IOException {
			body.load(f);
			hour = f.b();
			ran = f.b();
		}


		@Override
		public ThingFactory<?> factory() {
			return SETT.THINGS().rubbish.rubbish;
		}
	
	}
	
	final static class RubbishHolder extends ThingFactory<ThingRubbish>{
		
		private final ThingRubbish[] gore = new ThingRubbish[5000];
		private final IUpdater up = new IUpdater(5000, TIME.secondsPerDay()) {
			
			@Override
			protected void update(int i, double timeSinceLast) {
				ThingRubbish r = gore[i];
				
				if (!r.isRemoved()) {
					int hour = r.age();
					if (hour >= TIME.hoursPerDay())
						r.remove();
				}
			}
		};
		
		RubbishHolder(LISTE<ThingFactory<?>> all){
			super(all, 5000);
			for (int i = 0; i < gore.length; i++) {
				gore[i] = new ThingRubbish(i);
			}
		}
		
		public void make(int cx, int cy){
			ThingRubbish f = nextInLine();
			f.init(cx, cy, (byte) (RND.rInt() & 0xFF));
			f.add();
		}
		
		public void make(int cx, int cy, byte ran){
			ThingRubbish f = nextInLine();
			f.init(cx, cy, ran);
		}
		
		@Override
		void update(double ds) {
			up.update(ds);
		}
		
		@Override
		protected void save(FilePutter file) {
			up.save(file);
			super.save(file);
		}
		
		@Override
		protected void load(FileGetter file) throws IOException {
			up.load(file);
			super.load(file);
		}
		
		@Override
		protected void clear() {
			up.clear();
			super.clear();
		}

		@Override
		protected ThingRubbish[] all() {
			return gore;
		}
		
	}
	
	static class ThingRubbishMoving extends Thing {

		private static final VectorImp vec = new VectorImp();
		private byte ran;
		private double z;
		private double dx;
		private double dy;
		
		private double x;
		private double y;
		
		ThingRubbishMoving(int index){
			super(index);
		}
		
		
		protected void init(int cx, int cy, int destx, int desty) {
			ran = (byte) RND.rInt();
			x = cx;
			y = cy;
			
			destx += RND.rInt0(C.TILE_SIZE);
			desty += RND.rInt0(C.TILE_SIZE);
			if (cx == destx && cy == desty)
				return;
			
			z = vec.set(cx, cy, destx, desty);
			
			dx = vec.nX();
			dy = vec.nY();
			double speed = 10*C.TILE_SIZE + RND.rFloat(4*C.TILE_SIZE);
			dx *= speed;
			dy *= speed;
			z = z/speed;
			
			
			
			add();
		}
		
		boolean update(double ds) {
			z -= ds;
			
			if (z <= 0) {
				remove();
				THINGS().rubbish.rubbish.make(body().cX(), body().cY(), ran);
				return false;
			}
			x += ds*dx;
			y += ds*dy;
			
			if (z < 50) {
				if (SETT.ENTITIES().getAtPoint((int)x, (int)y) != null) {
					remove();
					THINGS().rubbish.rubbish.make(body().cX(), body().cY(), ran);
					return false;
				}
			}
			return true;
		}
		
		@Override
		public RECTANGLE body() {
			return rec;
		}


		@Override
		public void render(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY) {
			THINGS().sprites.rubbish.render(r, ran&0x0F, body().x1()+offsetX, body().y1()+offsetY);
			shadows.setHeight(1).setDistance2Ground(z/25);
			THINGS().sprites.rubbish.render(shadows, ran&0x0F, body().x1()+offsetX, body().y1()+offsetY);
		}

		@Override
		protected int z() {
			return (int) z;
		}

		@Override
		protected void save(FilePutter f) {
			f.d(x);
			f.d(y);
			f.d(z);
			f.d(dx);
			f.d(dy);
			f.b(ran);
		}


		@Override
		protected void load(FileGetter f) throws IOException {
			x = f.d();
			y = f.d();
			z = f.d();
			dx = f.d();
			dy = f.d();
			ran = f.b();
		}


		@Override
		public ThingFactory<?> factory() {
			return SETT.THINGS().rubbish.rubbishMoving;
		}
		
		private final RecFacadePoint rec = new RecFacadePoint() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public int x1() {
				return (int) x - 4*C.SCALE;
			}

			@Override
			public int y1() {
				return (int) y - 4*C.SCALE;
			}

			@Override
			public int width() {
				return 8*C.SCALE;
			}

			@Override
			public int height() {
				return 8*C.SCALE;
			}
			
			
		}; 
	
	}
	
	final static class RubbishMovingHolder extends ThingFactory<ThingRubbishMoving>{
		
		private final ThingRubbishMoving[] gore;
		
		RubbishMovingHolder(LISTE<ThingFactory<?>> all){
			super(all, 1024);
			this.gore = new ThingRubbishMoving[1024];
			for (int i = 0; i < gore.length; i++) {
				gore[i] = new ThingRubbishMoving(i);
			}
		}
		
		public void make(int cx, int cy, int sx, int sy){
			ThingRubbishMoving f = nextInLine();
			f.init(cx, cy, sx, sy);
		}
		
		@Override
		void update(double ds) {
			
			ThingRubbishMoving g = first();
			ThingRubbishMoving drop = null;
			while (g != null) {
				if (drop == g)
					break;
				ThingRubbishMoving next = next(g);
				if (g.update(ds)) {
					if (drop != null)
						drop = g;
				}else {
					
				}
				g = next;
			}
		}

		@Override
		protected ThingRubbishMoving[] all() {
			return gore;
		}
		
	}
	
}
