package init.resources;

import java.io.IOException;
import java.util.HashMap;

import game.time.TIME;
import init.constant.C;
import init.paths.PATHS;
import init.sprite.SPRITES;
import init.sprite.game.Sheet;
import init.sprite.game.SheetData;
import init.sprite.game.SheetType;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerUtil;

public final class GrowableSprite {

	private final static Swayer swayer = new Swayer();
	private final static Pollen[] pollen = new Pollen[0x04F];
	private static int SET = 16;
	
	private static final Positions[] poss = new Positions[] {new Positions(0, 0), new Positions(16, 0), new Positions(0, 16)};
	
	static {

		for (int i = 0; i < pollen.length; i++)
			pollen[i] = new Pollen();
		

		
	}
	
	private final TILE_SHEET sheet;
	private final double wind;

	private final double poll;

	private final COLOR[] cpollen = new COLOR[0x04F];
	
	public final Part trunk = new Part(SET, 0.8);
	public final Part growth = new Part(0, 1.0);
	
	public GrowableSprite(String ssheet, double wind, double pollen, KeyMap<TILE_SHEET> sheetMap) throws IOException{

		if (!sheetMap.containsKey(ssheet)) {
			TILE_SHEET sheet = new ComposerThings.ITileSheet(PATHS.SPRITE().getFolder("resource").getFolder("growable").get(ssheet), 460, 34) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 16, 2, d.s8);
					s.singles.paste(true);
					return d.s8.saveGame();
				}
			}.get();
			sheetMap.put(ssheet, sheet);
		}
		sheet = sheetMap.get(ssheet);
		
		this.wind = wind;
		this.poll = pollen;
		setPollenColor(new ColorImp(107, 107, 107));
	}
	
	public void setPollenColor(COLOR color) {
		for (int i = 0; i < cpollen.length; i++) {
			ColorImp col = new ColorImp();
			for (int c = 0; c < 3; c++)
				col.setComp(c, CLAMP.i(color.getComp(c)-RND.rInt(15), 0, color.getComp(c)));
			cpollen[i] = col;
		}
	}
	
	private static final double ri = 0.5/3.0;
	
	public void render(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, double size, double tops) {
		i.countVegetation();
		
		double m = CLAMP.d(SETT.WEATHER().moisture.getD()*4, 0, 1);
		double growth = SETT.WEATHER().growth.getD()*m*(2.0+(i.ran()&0b011));
		double ripe = SETT.WEATHER().growthRipe.getD();
		
		
		
		renderTrunk(r, s, i, growth, ripe*2.0, size);
		
		double ra = ripe*2.5-ri*((i.ran()>>2)&0b011);
		double t = CLAMP.d(tops*ra, 0, tops);
		if (t > 0) {
			if (SETT.WEATHER().growth.isAutumn())
				renderTop(r, s, i, ripe, 1.0, t);
			else
				renderTop(r, s, i, 1.0, (ripe-0.5)*2, t);
		}
		
	}

	
	public void renderTrunk(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, double growth, double ripe, double size) {
		renderTrunk(0, r, s, i, growth, ripe, size);
	}
	
	public void renderTrunk(int pos, SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, double growth, double ripe, double size) {
		
		render(poss[pos&3], r, s, i, trunk, growth, ripe, size);
	}
	
	public void renderTop(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, double growth, double ripe, double size) {
		renderTop(0, r, s, i, growth, ripe, size);
	}
	

	
	public void renderTop(int pos, SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, double growth, double ripe, double size) {
		swayer.update();
		render(poss[pos&3], r, s, i, this.growth, growth, ripe, size);
		if (poll > 0 && ripe*growth >= 1) {
			int aa = (int) (8*size*poll);
			int k = i.ran()&0x03F;
			cpollen[k].bind();
			pollen[k].render(i.x(), i.y(), aa);
		}
		COLOR.unbind();
	}
	
	private void render(Positions pos, SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, Part part, double growth, double ripe, double size) {
		swayer.update();
		part.set(growth, ripe);

		if (size <= 0)
			return;
		
		int am = (int) (size*8);
		if (CORE.renderer().getZoomout() > 1)
			am = am >> CORE.renderer().getZoomout()-1;
		am = CLAMP.i(1 + am, 0, 8);
		
		long ran = i.bigRan();
		
		final int posI = (int) (ran & pos.positionsX.length-1);
		ran = ran >> 8;
		
		int swayI = (int) (ran & 63);
		ran = ran >> 6;
		
		int cr = i.ranGet(-1, -1);

		if (CORE.renderer().getZoomout() >= 2)
			am = CLAMP.i(am, 0, 2);
		
		for (int ii = 0; ii < am; ii++) {
			
			part.color[cr&0x0F].bind();
			cr = cr >> 4;
			int dx = i.x()+ pos.x(posI, ii);
			int dy = i.y()+ pos.y(posI, ii);
			int rr = (int) (ran & 0x00F);
			ran = ran >> 4;
			
			int kk = (swayI+ii)&63;

			int xx = (int) (dx - 0.5*part.sway*wind*swayer.dx[kk]);
			int yy = (int) (dy + 0.5*part.sway*wind*swayer.dy[kk]);
			
			//sheet.render(r, rr, x- (swayer.dx[kk]>>1), y+(swayer.dy[kk]>>1));
			int tile = part.off+rr;
			sheet.render(r, tile, xx, yy);
			s.setDistance2Ground(part.sheightoverGround*swayer.dz[kk]).setHeight((int) (part.sheight*swayer.dz[kk]));
			sheet.render(s, tile, xx, yy);
		}
		
		
	
		COLOR.unbind();
		
	}

	private static class Swayer {
		private final int am = 64;
		private final byte[] dx = new byte[am];
		private final byte[] dy = new byte[am];

		private final double[] dz = new double[am]; 
		private double[] ran = new double[am];
		
		double dd;
		
		private double lastSecond = 0;
		
		Swayer(){
			for (int i = 0; i < am; i++) {
				dx[i] = (byte) RND.rInt(16);
				dy[i] = (byte) RND.rInt(16);
				ran[i] = RND.rFloat()*Math.PI*2;
				dz[i] = 0.1 + 0.9*RND.rFloat();
			}
			
		}
		
		void update() {
			
			if (TIME.currentSecond() == lastSecond)
				return;
			
			double ds = TIME.currentSecond()-lastSecond;
			lastSecond = TIME.currentSecond();
			
			dd += ds*Math.pow(SETT.WEATHER().wind.getD(), 1.5)*4;

			for (int i = 0; i < am; i++) {
				double cos = (Math.cos(dd+ran[i]));
				double a = dz[i]*6*cos;
				dx[i] = (byte) (a);
				dy[i] = (byte) (a);
			}
			
		}
		
		
	}
	
	private static final class Pollen {
		
		private static final int ticks = 128;
		private static final int tmask = ticks-1;
		private static final int amount = 8;
		private static final double time = 5;
		private static final double ticksPerTime = ticks/time;
		
		private final byte[][] xs = new byte[ticks][amount];
		private final byte[][] ys = new byte[ticks][amount];
		
		private Pollen() {

			for (int a = 0; a < amount; a++) {
				
				double dvx = -(1.5*C.TILE_SIZE + RND.rFloat(1.5*C.TILE_SIZE));
				double dvy = (1.5*C.TILE_SIZE + RND.rFloat0(0.75*C.TILE_SIZEH));
				dvx /= (double)(ticks);
				dvy /= (double)(ticks);
				double y = RND.rInt(C.TILE_SIZE);
				double x = RND.rInt(C.TILE_SIZEH);

				int tStart = RND.rInt(ticks);
				
				double xsin = RND.rFloat(1);
				double ysin = RND.rFloat(1);
				double dsin = RND.rFloat()/ticks;
				
				for (int t = 0; t < ticks; t++) {
				
					xs[tStart][a] = (byte) (x);
					ys[tStart][a] = (byte) (y);
					x+= dvx*Math.sin(xsin);
					y+= dvy*Math.sin(ysin);
					xsin+=dsin;
					ysin+=dsin;
					tStart++;
					tStart &= tmask;
				}
			}
		}
		
		private void render(int x, int y, int a) {
			int t = (int) ((SETT.WEATHER().wind.time.getD()*8 + TIME.currentSecond()*0.5) *ticksPerTime);
			t = t & tmask;
			a = CLAMP.i(a, 0, 8);
			for (int i = 0; i < a; i++) {
				CORE.renderer().renderParticle(x+xs[t][i], y+ys[t][i]);
			}
		}
	}
	
	private final static class Positions {
		private byte[][] positionsX = new byte[0b100_0000][8];
		private byte[][] positionsY = new byte[0b100_0000][8];
		private static final int dPosition = 0;
		
		public Positions(int ddx, int ddy) {
			final int D = C.TILE_SIZE/3;
			for (int i = 0; i < positionsX.length; i++) {
				int[] ss = new int[] {0,1,2,3,4,5,6,7};
				{
					for (int k = 0; k < ss.length; k++) {
						int i1 = RND.rInt(ss.length);
						int o = ss[i1];
						ss[i1] = ss[0];
						ss[0] = o;
					}
				}
				
				int k = 0;
				for (int dy = 0; dy < 3; dy++) {
					for (int dx = 0; dx < 3; dx++) {
						if (dx == 1 && dy == 1)
							continue;
						int x = ddx + (dx*(D-ddx)) - dPosition + RND.rInt(2*dPosition+1);
						int y = ddy + (dy*(D-ddy)) - dPosition + RND.rInt(2*dPosition+1);
						int s = ss[k];
						k++;
						positionsX[i][s] = (byte) x;
						positionsY[i][s] = (byte) y;
						
					}
				}
				
			}
		}
		
		private int x(int ran, int i) {
			return positionsX[ran&0b011_1111][i&0b111];
		}
		
		private int y(int ran, int i) {
			return positionsY[ran&0b011_1111][i&0b111];
		}
	}
	
	public final static class Part {
		public double sheight = 4;
		public double sheightoverGround = 4;
		private ColorImp[] color = new ColorImp[16];
		
		public final ColorImp[] cdead = new ColorImp[16];
		public final ColorImp[] clive = new ColorImp[16];
		public final ColorImp[] cripe = new ColorImp[16];

		private double lastGrowth = -1;
		private double lastRipe = -1;
		private final int off;
		public double sway;
		
		private Part(int off, double sway) {
			this.off = off;
			this.sway = sway;
			for (int i = 0; i < color.length; i++) {
				color[i] = new ColorImp();
				cdead[i] = new ColorImp(50, 30, 10);
				clive[i] = new ColorImp(127, 127, 127);
				
				
				cripe[i] = new ColorImp(127, 127, 127);
			}
		}
		
		public void setColors(COLOR dead, COLOR live, COLOR ripe) {
			for (int i = 0; i < color.length; i++) {
				if (dead != null)
					cdead[i].set(dead);
				if (live != null)
					clive[i].set(live);
				if (ripe != null)
					cripe[i].set(ripe);
			}
		}
		
		
		
		private void set(double growth, double ripe) {
			
			if (lastGrowth != growth || lastRipe != ripe) {
				growth = CLAMP.d(growth, 0, 1);
				ripe = CLAMP.d(ripe, 0, 1);
				for (int i = 0; i< color.length; i++) {
					ColorImp.TMP.interpolate(clive[i], cripe[i], ripe);
					color[i].interpolate(cdead[i], ColorImp.TMP, growth);
				}
				lastGrowth = growth;
				lastRipe = ripe;
			}
		}
		
	}
	
	public void makeSheet(String key) {
		new SSheet(SheetType.sCombo, key, this);
		new SSheet(SheetType.s1x1, key, this);
		new SSheet(SheetType.s2x2, key, this);
		new SSheet(SheetType.s3x3, key, this);
	}
	
	private static class SSheet extends Sheet {
		
		private final static HashMap<SheetData, Part> datas = new HashMap<SheetData, Part>();
		private ShadowBatch s;
		private final GrowableSprite sp;
		
		SSheet(SheetType type, String key, GrowableSprite sp){
			super(type.sizeSize*1, false, false);
			this.sp = sp;
			SPRITES.GAME().add(type, new ArrayList<Sheet>(this), key);
		}
		
		@Override
		public void renderShadow(SheetData da, int x, int y, RenderIterator it, ShadowBatch shadow, int tile, int random) {
			this.s = shadow;
		}
		
		@Override
		public void render(SheetData da, int x, int y, RenderIterator it, SPRITE_RENDERER sr, int tile, int random,
				double degrade) {
			it.countVegetation();
			
			double m = CLAMP.d(SETT.WEATHER().moisture.getD()*4, 0, 1);
			double growth = SETT.WEATHER().growth.getD()*m*(2.0+(it.ran()&0b011));
			double ripe = SETT.WEATHER().growthRipe.getD();
			
			if (s == null)
				s = ShadowBatch.DUMMY;
			
			double am = 1.0-0.8*degrade;
			
			sp.renderTrunk(sr, s, it, growth, ripe*2.0, am);
			
			double ra = ripe*2.5-ri*((it.ran()>>2)&0b011);
			double t = CLAMP.d(am*ra, 0, 1.0);
			
			if (!datas.containsKey(da)) {
				Part p = new Part(0, 1);
				for (int i = 0; i < p.cripe.length; i++) {
					p.cripe[i].set(da.colors.getC(i));
				}
				datas.put(da, p);
			}
			
			Part part = datas.get(da);
			
			if (t > 0) {
				if (SETT.WEATHER().growth.isAutumn())
					sp.render(poss[0], sr, s, it, part, am, ripe, t);
				else
					sp.render(poss[0], sr, s, it, part, am, (ripe-0.5)*2, t);
			}
			
		}

		@Override
		public TextureCoords texture(int tile) {
			return COLOR.WHITE100.texture();
		}
		

	};
	
}
