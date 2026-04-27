package world.map.regions;

import static world.WORLD.REGIONS;

import java.io.IOException;
import java.util.Arrays;

import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAIN;
import init.type.TERRAINS;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.GUTIL;
import world.WORLD;
import world.map.regions.centre.WCentre;
import world.map.regions.centre.WorldCentrePlacablity;

public final class RegionInfo {
	
	private static int[] countTerrain = new int[TERRAINS.ALL().size()];
	
	
	private static double bi = 1.0/0x0FF;
	
	public static final int nameSize = 24;
	private final Str name = new Str(nameSize);
	private int area;
	private final Rec bounds = new Rec();
	private short cx,cy;
	private byte fertility;
	private final byte[] climateTerrMin = new byte[1+TERRAINS.ALL().size()];
	
	private static Averages ave;
	
	public RegionInfo() {
		ave = null;
		clear();
	}
	
	void save (FilePutter f) {
		name.save(f);
		f.s(cx).s(cy);
		bounds.save(f);
		f.i(area);
		f.b(fertility);
		f.bs(climateTerrMin);
	}
	
	void load (FileGetter f) throws IOException {
		name.load(f);
		cx = (short) f.s();
		cy = (short) f.s();
		bounds.load(f);
		area = f.i();
		fertility = f.b();
		f.bs(climateTerrMin);
		ave = null;
	}
	
	void clear(){
		name.clear();
		cx = -1;
		cy = -1;
		bounds.clear();
		area = 0;
		fertility = 0;
		Arrays.fill(climateTerrMin, (byte)0);
		ave = null;
	}
	
	public Str name() {
		return name;
	}

	public int cx() {
		return cx;
	}
	
	public int cy() {
		return cy;
	}
	
	void centreSet(int tx, int ty) {
		cx = (short) tx;
		cy = (short) ty;
		WORLD.REGIONS().dirty = true;
	}
	
	public int area() {
		return area;
	}
	
	public RECTANGLE bounds() {
		return bounds;
	}
	
	public double moisture() {
		return (fertility & 0x0FF)*bi;
	}
	
	public double climate(CLIMATE c) {
		double ci = climateI();
		
		if ((int) ci == c.index()) {
			return 1 - (ci-(int)ci);
		}else if ((int) ci == c.index()-1)
			return (ci-(int)ci);
		return 0;
	}
	
	public double climateI() {
		return (CLIMATES.ALL().size()-1)*(climateTerrMin[0] & 0x0FF)*bi;
	}
	
	public double terrain(TERRAIN c) {
		return (climateTerrMin[1+ c.index()] & 0x0FF)*bi;
	}
	
	public boolean init(int sx, int sy, RECTANGLE body) {
		ave = null;
		WORLD.REGIONS().dirty = true; 
		double climate = 0;
		Arrays.fill(countTerrain, 0);
		
		double fertility = 0;
		
		if (WORLD.REGIONS().map.get(sx, sy).info != this)
			throw new RuntimeException();
		
		Region a = REGIONS().map.get(sx, sy);
		
		bounds.moveX1Y1(sx, sy).setDim(1);
		area = 0;
		
		for (COORDINATE c : body) {
			if (!REGIONS().map.is(c, a)) {
				continue;
			}
			climate += WORLD.CLIMATE().getter.get(c).index();
			countTerrain[TERRAINS.world.get(c).index()]++;
			fertility += WORLD.MOISTURE().get(c.x(), c.y());
			area++;
			bounds.unify(c.x(), c.y());
		}
		
		Rec tmp = new Rec(bounds);
		tmp.incr(-1, -1).incrW(2).incrH(2);
		

		GUTIL.flooder().init(this);
		for (COORDINATE c : tmp) {
			if (!REGIONS().map.is(c, a)) {
				GUTIL.flooder().pushSloppy(c.x(), c.y(), 0);
			}
		}
		
		PathTile centre = null;
		PathTile backup = null;
		while (GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			
			if (REGIONS().map.is(t, a)){
				if (WorldCentrePlacablity.regionC(t.x(), t.y()) == null) {
					centre = t;
					break;
				}else if (WorldCentrePlacablity.regionMiniC(t.x(), t.y()) == null) {
					for (int y = t.y()-WCentre.TILE_DIM/2; y < t.y()+WCentre.TILE_DIM; y++)
						for (int x = t.x()-WCentre.TILE_DIM/2; x < t.x()+WCentre.TILE_DIM; x++)
					
					backup = t;
				}
			}
			
			
			
			for (DIR d : DIR.ALL) {
				if (REGIONS().map.is(t, d, a))
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		GUTIL.flooder().done();
		
		if (centre == null) {
			centre = backup;
			if (backup == null || !WORLD.REGIONS().player.is(backup))
				return false;
		}
		
		if (WORLD.REGIONS().map.get(cx, cy) != a || WorldCentrePlacablity.regionC(cx, cy) != null) {
			centreSet(centre.x(), centre.y());
		}
		
		climate /= area*(CLIMATES.ALL().size()-1);
		climate = CLAMP.d(climate, 0, 1);
		climateTerrMin[0] = (byte) (0xFF*climate);
		
		for (int i = 0; i < countTerrain.length; i++) {
			climateTerrMin[1 + i] = (byte) (0xFF*((double)countTerrain[i]/area));
		}
		this.fertility = (byte) (0x0FF*fertility/area);
		Arrays.fill(countTerrain, 0);
		return true;
	}
	
	
	public static RegValue vFer() {
		if (ave == null)
			ave = new Averages();
		return ave.fertility;
	}
	
	public static RegValue vArea() {
		if (ave == null)
			ave = new Averages();
		return ave.area;
	}
	
	public static RegValue vTerrain(TERRAIN t) {
		if (ave == null)
			ave = new Averages();
		return ave.terrains[t.index()];
	}

	
	private static final class Averages {
		
		
		public final RegValue[] terrains = new RegValue[TERRAINS.ALL().size()];
		public final RegValue fertility;
		public final RegValue area;
		private static boolean log = false;
		
		Averages(){
			ave = this;
			
			{
				fertility = new RegValue() {

					@Override
					public double rawAI(Region reg) {
						double f = 0.1 + reg.info.moisture();
						double a = reg.info.area;
						return f*f*a;
					}

					@Override
					public double raw(Region reg) {
						return reg.info.moisture();
					}
					
				};
				log(fertility, "fertility");
			}
			
			{
				area = new RegValue() {

					@Override
					public double rawAI(Region reg) {
						double f = 0.1 + reg.info.moisture();
						double a = reg.info.area;
						return f*a*a;
					}

					@Override
					public double raw(Region reg) {
						return reg.info.area;
					}
					
				};
				log(area, "area");
			}
			
			for (int i = 0; i < TERRAINS.ALL().size(); i++) {
				TERRAIN t = TERRAINS.ALL().get(i);
				terrains[i] = new RegValue() {
					
					@Override
					public double rawAI(Region reg) {
						double f = 0.25 + reg.info.moisture();
						return (0.5 + reg.info.terrain(t))*reg.info.area*f;
					}
					
					@Override
					public double raw(Region reg) {
						return reg.info.terrain(t);
					}
					
				};
				log(terrains[i], "t " + t.key);
			}
		}
		
		private void log(RegValue trans, String name) {
			if (!log)
				return;
			LOG.ln(name);
			double ave = 0;
			double ma = 0;
			double mi = 1;
			double mv = 0;
			double miv = Double.MAX_VALUE;
			
			for (int ri = 0; ri < WORLD.REGIONS().active().size(); ri++) {
				Region reg = WORLD.REGIONS().active().get(ri);
				double d = trans.getAi(reg);
				ave += d;
				ma = Math.max(d, ma);
				mi = Math.min(d, mi);
				mv = Math.max(mv, trans.rawAI(reg));
				miv = Math.min(miv, trans.rawAI(reg));
			}
			ave /= WORLD.REGIONS().active().size();
			LOG.ln("AVE: " + (int)(100*ave));
			LOG.ln("MIN: " + (int)(100*mi));
			LOG.ln("MAX: " + (int)(100*ma));
			LOG.ln("VVV: " + trans.getAi(miv) + " -> " + trans.getAi(mv));
			
			ave = 0;
			ma = 0;
			mi = 1;
			mv = 0;
			for (int ri = 0; ri < WORLD.REGIONS().active().size(); ri++) {
				Region reg = WORLD.REGIONS().active().get(ri);
				double d = trans.get(reg);
				ave += d;
				ma = Math.max(d, ma);
				mi = Math.min(d, mi);
				mv = Math.max(mv, trans.raw(reg));
			}
			ave /= WORLD.REGIONS().active().size();
			
			LOG.ln("AVE: " + (int)(100*ave));
			LOG.ln("MIN: " + (int)(100*mi));
			LOG.ln("MAX: " + (int)(100*ma));
			LOG.ln("VVV: " + trans.get(mi) + " -> " + trans.get(ma));
		}
		
	}
	
	public static abstract class RegValue {
		
		public final double weight;
		public final double ave;
		public final double aveAI;
		public final double max;
		
		public RegValue() {
			
			double ave = 0;
			double aveAI = 0;
			double ma = 0;

			for (int ri = 0; ri < WORLD.REGIONS().active().size(); ri++) {
				Region reg = WORLD.REGIONS().active().get(ri);
				double v = raw(reg);
				ave += v;
				aveAI += rawAI(reg);
				ma = Math.max(v, ma);
			}

			
			
			this.ave = ave / WORLD.REGIONS().active().size();;
			this.aveAI = aveAI / WORLD.REGIONS().active().size();;
			this.max = ma;
			if (Averages.log) {
				LOG.ln(this.ave + " " + this.max);
			}
			double w = 1 - this.ave/max;
			this.weight = 1.0/w;
			
		}
		
		public abstract double raw(Region reg);
		public abstract double rawAI(Region reg);
		
		public double getAi(double v) {
			return v / aveAI; 
		}
		
		public double getAi(Region reg) {
			return getAi(rawAI(reg));
		}
		
		
		public double get(double v) {

//			double m = weight*ave/max;
//			double d = (1-m + weight*v/max)/2.0;
//			return CLAMP.d(d, 0, 1);
			return CLAMP.d(Math.sqrt(v/max), 0, 1);
		}

		
		public double get(Region reg) {
			return get(raw(reg));
		}
		
		
		
	}

}