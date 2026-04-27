package settlement.thing.projectiles;

import java.io.IOException;
import java.util.Arrays;

import settlement.entity.ENTITY;
import settlement.main.SETT;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

final class PData {

	private static final int CHUNK = 2*16384;
	private int activeLast;
	
	private float[] space;
	private int[] next;
	private short[] type;
	private float[] ref;
	private int[] shooter;
	
	private final Data data = new Data();
	private final Data dd = new Data();
	private final Map map;
	
	private static int SPACESIZE = 7;
	
	private final VectorImp vec = new VectorImp();
	
	PData(Map map){
		this.map = map;
		saver.clear();
	}
	
	public int create(int x, int y, int height, double dx, double dy, double dz, Projectile t, double reff, ENTITY entity) {
		if (!SETT.PIXEL_IN_BOUNDS(x, y))
			return -1;
		if (activeLast >= next.length) {
			int nz = next.length + CHUNK;
			
			float[] space = new float[nz*SPACESIZE];
			for (int i = 0; i < this.space.length; i++)
				space[i] = this.space[i];
			this.space = space;
			int[] next = new int[nz];
			short[] type = new short[nz];
			float[] ref = new float[nz];
			int[] shooter = new int[nz];
			Arrays.fill(shooter, -1);
			for (int i = 0; i < this.next.length; i++) {
				next[i] = this.next[i];
				type[i] = this.type[i];
				ref[i] = this.ref[i];
				shooter[i] = this.shooter[i];
			}
			this.next = next;
			this.type = type;
			this.ref = ref;
			this.shooter = shooter;
		}
		dd.sI = activeLast*SPACESIZE;
		
		
		dd.xSet(x);
		dd.ySet(y);
		dd.zSet(height);
		
		
		double m = vec.set(dx, dy);
		dd.nxSet(vec.nX());
		dd.nySet(vec.nY());
		dd.dzSet(dz);
		dd.magnitudeSet(m);

		shooterSet(activeLast, entity);
		type(activeLast, t.index);
		ref(activeLast, reff);
		map.add(activeLast);
		t.soundRelease().rnd(x, y, 1);
		activeLast++;
		return activeLast-1;
	}
	
	public void remove(final int index) {
		map.remove(index);
		activeLast--;
		if (index != activeLast) {
			map.remove(activeLast);
			copy(activeLast, index);
			map.add(index);
		}
		
		
		
	}
	
	public boolean move(final int i, double nx, double ny) {
		
		float x = (float) nx;
		float y = (float) ny;
		
		if (!SETT.PIXEL_IN_BOUNDS((int)x, (int)y)) {
			remove(i);
			return false;
		}
		
		dd.sI = i*SPACESIZE;
		if (((int)x)>>Map.gridScroll != dd.qx() || ((int)y)>>Map.gridScroll != dd.qy()) {
			map.remove(i);
			dd.xSet(x);
			dd.ySet(y);
			map.add(i);
		}else {
			dd.xSet(x);
			dd.ySet(y);
		}
		return true;
	}
	
	
	private void copy(int fromI, int toI) {
		
		type[toI] = type[fromI];
		next[toI] = next[fromI];
		ref[toI] = ref[fromI];
		toI *=SPACESIZE;
		fromI *= SPACESIZE;
		
		for (int i = 0; i < SPACESIZE; i++) {
			space[toI+i] = space[fromI+i];
		}
		
	}
	
	
	final class Data {
		
		private int sI;
		
		private Data() {
			
		}
		
		public float x() {
			return space[sI+0];
		}
		private void xSet(float x) {
			space[sI+0] = x;
		}
		public float y() {
			return space[sI+1];
		}
		private void ySet(float x) {
			space[sI+1] = x;
		}
		public float z() {
			return space[sI+2];
		}
		public void zSet(double x) {
			space[sI+2] = (float) x;
		}
		
		public float nx() {
			return space[sI+3];
		}
		public void nxSet(double x) {
			space[sI+3] = (float) x;
		}
		public float ny() {
			return space[sI+4];
		}
		public void nySet(double x) {
			space[sI+4] = (float) x;
		}
		public float dz() {
			return space[sI+5];
		}
		public void dzSet(double x) {
			space[sI+5] = (float) x;
		}
		void magnitudeSet(double x) {
			space[sI+6] = (float) x;
		}
		public double dMagnitude() {
			return space[sI+6];
		}
		
		public double speedX() {
			return nx()*dMagnitude();
		}
		
		public double speedY() {
			return ny()*dMagnitude();
		}
		
		public int qx() {
			return ((int)x())>>Map.gridScroll;
		}
		
		public int qy() {
			return ((int)y())>>Map.gridScroll;
		}
		

		
	}
	
	public Data data(int index) {
		data.sI = index*SPACESIZE;
		return data;
	}
	
	public int next(int index) {
		return next[index];
	}
	
	public Projectile type(int index) {
		return Projectile.ALL.get(type[index]&0b01111111);
	}
	
	
	public double ref(int index) {
		return ref[index];
	}
	
	public void ref(int index, double ref) {
		this.ref[index] = (float) ref;
	}
	
	public void nextSet(int index, int n) {
		next[index] = n;
	}
	
	public void type(int index, short t) {
		type[index] &= t & 0b1000_0000_0000_0000;
		type[index] |= t;
	}
	
	public ENTITY shooter(int index) {
		return SETT.ENTITIES().getByID(shooter[index]);
	}
	
	public void shooterSet(int index, ENTITY e) {
		int i = e == null ? -1 : e.id();
		shooter[index] = i;
	}
	
	public void live(int index, boolean live) {
		if (!live) {
			type[index] |= 0b01000_0000_0000_0000;
		}else {
			type[index] &= ~0b01000_0000_0000_0000;
		}
	}
	
	public boolean live(int index) {
		return (type[index] & 0b01000_0000_0000_0000) == 0;
	}
	
	public final int last() {
		return activeLast;
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(activeLast);
			for (int i = 0; i < activeLast; i++) {
				file.s(type[i]);
				file.f(ref[i]);
				file.i(shooter[i]);
			}
			int am = activeLast*SPACESIZE;
			for (int i = 0; i < am; i++) {
				file.f(space[i]);
			}
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			activeLast = file.i();
			int l = (int) Math.ceil((double)(activeLast+1)/CHUNK);
			space = new float[l*CHUNK*SPACESIZE];
			next = new int[l*CHUNK];
			type = new short[l*CHUNK];
			ref = new float[l*CHUNK];
			shooter = new int[l*CHUNK];
			int MZ = Projectile.ALL.size();
			for (int i = 0; i < activeLast; i++) {
				type[i] = file.s();
				if (type[i] >= MZ)
					type[i] = (short) (MZ-1);
				ref[i] = file.f();
				shooter[i] = file.i();
			}
			int am = activeLast*SPACESIZE;
			for (int i = 0; i < am; i++) {
				space[i] = file.f();
			}
			Arrays.fill(next, -1);
			for (int i = 0; i < last(); i++) {
				map.add(i);
			}
		}
		
		@Override
		public void clear() {
			space = new float[CHUNK*SPACESIZE];
			next = new int[CHUNK];
			Arrays.fill(next, -1);
			type = new short[CHUNK];
			ref = new float[CHUNK];
			shooter = new int[CHUNK];
			Arrays.fill(shooter, -1);
			activeLast = 0;
			
		}
	};
	
	
	
}
