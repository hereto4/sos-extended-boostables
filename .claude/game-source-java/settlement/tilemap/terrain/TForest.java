package settlement.tilemap.terrain;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;
import java.nio.file.Path;

import game.GAME;
import game.audio.AUDIO;
import game.audio.SoundRace;
import game.time.TIME;
import init.constant.C;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.Icons.S.IconS;
import init.sprite.game.Sheet;
import init.sprite.game.SheetData;
import init.sprite.game.SheetPair;
import init.sprite.game.SheetType;
import init.type.TERRAIN;
import init.type.TERRAINS;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.map.MAP_INTE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.TileTexture.TileTextureScroller;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerUtil;
import util.text.D;

public final class TForest {

	public final Small SMALL;
	public final Medium MEDIUM;
	public final Big BIG;
	private final Sprites sprites;
	private static CharSequence ¤¤name = "¤Tree";
	public final SPRITE icon;
	
	private double clearAm = 0;
	
	static {
		D.ts(TForest.class);
	}
	private final double[] mshades = new double[32];
	TForest(Terrain t) throws IOException {

		sprites = new Sprites();
		SMALL = new Small(t);
		MEDIUM = new Medium(t);
		BIG = new Big(t);
		
		for (int i = 0; i < mshades.length; i++)
			mshades[i] = 0.8 + RND.rFloat(0.2);
		
		icon = new SPRITE.Imp(IconS.L) {
			
			private COLOR bg = new ColorImp(102, 87, 65);
			private COLOR bg2 = bg.shade(0.6);
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				
				int[] dx = new int[] {0, IconS.L/2, 0, IconS.L/2};
				int[] dy = new int[] {0, 0, IconS.L/2, IconS.L/2};
				
				bg2.render(r, X1, X2, Y1, Y2);
				bg.render(r, X1+2, X2-2, Y1+2, Y2-2);
				
				for (int i = 0; i < 4; i++) {
					int x = X1 + dx[i];
					int y = Y1 + dy[i];
					int x2 = x+IconS.L/2;
					int y2 = y+IconS.L/2;
					
					SETT.TERRAIN().colors.tree.fertile.get(0).bind();
					
					sprites.smedium.render(r, i, x, x2, y, y2);			
				}
				COLOR.unbind();
				
				
			}
		};

	}

	public boolean isTree(int tx, int ty) {
		return TERRAIN().get(tx, ty) instanceof Tree;
	}
	
	public boolean isTree(int tile) {
		return TERRAIN().get(tile) instanceof Tree;
	}

	void update(double ds) {
		sprites.update(ds);
	}
	
	public static final int MAX_AMOUNT = 4;
	
	public final TAmount amount = new TAmount(8, "Tree") {
		
		
		
		@Override
		public int get(int tile) {
			if (isTree(tile)) {
				return CLAMP.i(1 + ((SETT.TERRAIN().data.get(tile)>>8)&0x07), 0, MAX_AMOUNT);
			}
			return 0;
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			
			if (value <= 0) {
				if (isTree(tile)) {
					TERRAIN().NADA.placeFixed(tile%TWIDTH, tile/TWIDTH);
				}
			}else {
				if (!isTree(tile)) {
					SMALL.placeFixed(tile%TWIDTH, tile/TWIDTH);
					
				}
				value = CLAMP.i(value-1, 0, MAX_AMOUNT-1);
				int d = SETT.TERRAIN().data.get(tile);
				
				d &= 0x00FF;
				d |= value << 8;
				
				SETT.TERRAIN().data.set(tile, d);
			}
			return this;
		}
	};

	public abstract class Tree extends Terrain.TerrainTile {

		private final TerrainClearing clearing = new TerrainClearing() {

			private SoundRace sound = AUDIO.race("CLEAR_TREE");
			
			@Override
			public RESOURCE clear1(int tx, int ty) {

				int a = amount.get(tx, ty);
				if (a > 0) {
					
					if (a == 1) {
						TERRAIN().DECOR_WOOD.placeFixed(tx, ty);
						return RESOURCES.WOOD();
					}else
						amount.increment(tx, ty, -1);
				} else {
					GAME.Notify("what??? " + tx+ " " + ty + " " + a);
				}
				
				clearAm += 0.25;
				if (clearAm >= 1) {
					return RESOURCES.WOOD();
				}
				return null;
			}

			@Override
			public boolean can() {
				return true;
			}

			@Override
			public int clearAll(int tx, int ty) {
				int a = amount.get(tx, ty);
				TERRAIN().NADA.placeFixed(tx, ty);
				return a;
			}

			@Override
			public SoundRace sound(int tx, int ty) {
				return sound;
			}
			
			@Override
			public double strength() {
				return super.strength()*4;
				
			};
		
		};
		
		@Override
		protected final boolean place(int tx, int ty) {
			
			if (!isTree(tx, ty)) {
				
			}else {
				
			}
			
			
			if (!shared.TREES.BIG.tryPlace(tx, ty))
				if (!shared.TREES.MEDIUM.tryPlace(tx, ty))
					shared.TREES.SMALL.tryPlace(tx, ty);
			amount.set(tx, ty, 4);
			
			return false;
		}
		
		protected Tree(Terrain shared, int i) {
			super("TREE_SIZE_" + i, shared, ¤¤name, SPRITES.icons().m.cancel, shared.colors.minimap.tree);
		}

		@Override
		public TerrainClearing clearing() {
			return clearing;
		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return null;
		}

		@Override
		void unplace(int tx, int ty) {
			//amounts.set(tx+ty*TWIDTH, 0);
		}
		
		@Override
		public void hoverInfo(GBox box, int tx, int ty) {
			box.add(RESOURCES.WOOD().icon());
			box.textLL(RESOURCES.WOOD().name);
			box.tab(6);
			box.add(GFORMAT.i(box.text(), amount.get(tx, ty)));
		}
		
		@Override
		public int miniDepth() {
			return 2;
		}
		
		@Override
		public TERRAIN terrain(int tx, int ty) {
			return TERRAINS.FOREST();
		}
		
		@Override
		public int heightStart(int tx, int ty) {
			return 2;
		}
		
		@Override
		public int heightEnd(int tx, int ty) {
			return 4;
		}
		

		
		
	}

	public class Small extends Tree {

		private Small(Terrain shared) {
			super(shared, 0);
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return IN_BOUNDS(tx, ty);
		}

		private boolean tryPlace(int tx, int ty) {
			super.placeRaw(tx, ty);
			shared.data.set(tx, ty, RND.rInt(32));
			return false;
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			return false;
		}

		@Override
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			SheetType type = SheetType.s1x1;
			data &= 0x0FF;
			SheetPair sheet = sprites.small;
			int tile = type.tile(sheet, data, i.ran(), 0);
			sheet.s.render(sheet.d, i.x(), i.y(), i, r, tile, i.ran(), 0);
			sheet.s.renderShadow(sheet.d, i.x(), i.y(), i, s, tile, i.ran());
			return false;
		}
		
		@Override
		public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
			c.shadeSelf(mshades[(x+y)&mshades.length-1]);
			return c;
		}

	}

	public class Medium extends Tree {

		private Medium(Terrain shared) {
			super(shared, 1);
		}

		private boolean tryPlace(int tx, int ty) {

			if (is(tx, ty)) {
				int var = shared.data.get(tx, ty) & 0b011;
				if (tryVar(tx, ty, var) >= 0)
					return true;
			}

			for (int var = 0; var < 4; var++) {
				int i = tryVar(tx, ty, var);
				if (i >= 0) {
					super.placeRaw(tx, ty);
					shared.data.set(tx, ty, (i));
					return true;
				}
			}
			return false;

		}

		private int tryVar(int tx, int ty, int var) {
			int rx = var % 2;
			int ry = var / 2;

			for (int ov = 0; ov < 4; ov++) {

				if (ov == var)
					continue;

				int dx = ov % 2;
				int dy = ov / 2;
				dx -= rx;
				dy -= ry;
				int x = tx + dx;
				int y = ty + dy;

				if (!IN_BOUNDS(x, y))
					return -1;
				if (!shared.TREES.SMALL.is(x, y) && !(is(x, y) && (shared.data.get(x, y) & 0b011) == ov))
					return -1;

				
			}
			return var;
		}

		@Override
		public void placeRaw(int tx, int ty) {
			if (shared.NADA.is(tx + 1, ty))
				shared.TREES.SMALL.placeRaw(tx + 1, ty);
			if (shared.NADA.is(tx, ty + 1))
				shared.TREES.SMALL.placeRaw(tx, ty + 1);
			if (shared.NADA.is(tx + 1, ty + 1))
				shared.TREES.SMALL.placeRaw(tx + 1, ty + 1);
			super.placeFixed(tx, ty);
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			
			data &= 0x0FF;
			SheetType.cXxX type = SheetType.s2x2;
			SheetPair sheet = sprites.medium;
			
			int dx = type.dx(data);
			int dy = type.dy(data);
			int ran = i.ranGet(-dx, -dy);
			
			int tile = type.tile(sheet, data, ran, 0);
			sheet.s.render(sheet.d, i.x(), i.y(), i, r, tile, ran, 0);
			s.setHeight(2).setDistance2Ground(5);
			sheet.s.renderShadow(sheet.d, i.x(), i.y(), i, s, tile, ran);
			return false;
		}

		@Override
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			TERRAIN().BUSH.render(i, r, s, i.x(), i.y(), i.ran());
			return false;
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return IN_BOUNDS(tx, ty) && IN_BOUNDS(tx + 1, ty + 1);
		}
		
		@Override
		public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
			int data = shared.data.get(x, y)&0xFF;
			SheetType.cXxX type = SheetType.s2x2;
			c.shadeSelf(mshades[(x-type.dx(data)+y-type.dy(data))&mshades.length-1]);
			if (type.dy(data) + type.dx(data) == 0)
				c.shadeSelf(1.2);
			if (type.dy(data) * type.dx(data) == 1)
				c.shadeSelf(0.8);
			return c;
		}

	}

	public class Big extends Tree {


		private Big(Terrain shared) {
			super(shared, 2);
		}

		private boolean tryPlace(int tx, int ty) {

			if (is(tx, ty)) {
				int var = shared.data.get(tx, ty) & 0b01111;
				if (tryVar(tx, ty, var) >= 0) {
					return true;
				}
			}

			for (int var = 0; var < 9; var++) {
				int i = tryVar(tx, ty, var);
				if (i >= 0) {
					super.placeRaw(tx, ty);
					shared.data.set(tx, ty, i);
					return true;
				}
			}
			return false;

		}

		@Override
		public void placeRaw(int tx, int ty) {
			shared.TREES.SMALL.placeRaw(tx + 1, ty);
			shared.TREES.SMALL.placeRaw(tx + 2, ty);
			shared.TREES.SMALL.placeRaw(tx, ty + 1);
			shared.TREES.SMALL.placeRaw(tx + 1, ty + 1);
			shared.TREES.SMALL.placeRaw(tx + 2, ty + 1);
			shared.TREES.SMALL.placeRaw(tx, ty + 2);
			shared.TREES.SMALL.placeRaw(tx + 1, ty + 2);
			shared.TREES.SMALL.placeRaw(tx + 2, ty + 2);
			super.placeFixed(tx, ty);
		}

		private int tryVar(int tx, int ty, int var) {
			int rx = var % 3;
			int ry = var / 3;

			for (int ov = 0; ov < 9; ov++) {

				if (ov == var)
					continue;

				int dx = ov % 3;
				int dy = ov / 3;
				dx -= rx;
				dy -= ry;
				int x = tx + dx;
				int y = ty + dy;

				if (!IN_BOUNDS(x, y))
					return -1;
				if (shared.TREES.SMALL.is(x, y)) {

				} else if (shared.TREES.MEDIUM.is(x, y)) {

				} else if (is(x, y) && (shared.data.get(x, y) & 0b01111) == ov) {
					
				} else {
					return -1;
				}
			}
			return var;
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return IN_BOUNDS(tx, ty) && IN_BOUNDS(tx + 2, ty + 2);
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			SheetType.cXxX type = SheetType.s3x3;
			SheetPair sheet = sprites.big;
			data &= 0x0FF;
			int dx = type.dx(data);
			int dy = type.dy(data);
			int ran = i.ranGet(-dx, -dy);
			
			int tile = type.tile(sheet.s, sheet.d, data, ran, 0);
			sheet.s.render(sheet.d, i.x(), i.y(), i, r, tile, ran, 0);
			s.setHeight(8).setDistance2Ground(20);
			sheet.s.renderShadow(sheet.d, i.x(), i.y(), i, s, tile, ran);
			return false;
		}

		@Override
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			TERRAIN().BUSH.render(i, r, s, i.x(), i.y(), i.ran());
			return false;
		}
		
		@Override
		public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
			int data = shared.data.get(x, y)&0x0FF;
			SheetType.cXxX type = SheetType.s3x3;
			c.shadeSelf(mshades[(x-type.dx(data)+y-type.dy(data))&mshades.length-1]);
			double s = 1;
			if (type.dx(data) == 0)
				s = 1.4;
			if (type.dy(data) == 2)
				s = 0.8;
			if (type.dx(data) == 2)
				s = 0.8;
			if (type.dy(data) == 0)
				s = 1.4;
			c.shadeSelf(s);
			return c;
		}

	}
	
	private static final class Sprites {
		
		private final TILE_SHEET ssmall;
		private final TILE_SHEET smedium;
		private final SheetPair small;
		private final SheetPair medium;
		private final SheetPair big;
		private final TForestTop top = new TForestTop();
		private final TForestLeafs leafs = new TForestLeafs();
		private final Swayer swayer = new Swayer();
		
		private Sprites() throws IOException {
			
			Path p = PATHS.SPRITE_SETTLEMENT_MAP().get("Tree");
			
			ssmall = new ComposerThings.ITileSheet(p, 720, 296) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					ComposerDests.Tile t = d.s16;
					s.singles.init(0, 0, 1, 1, 16, 1, t);
					s.singles.paste(1, true);
					return t.saveGame();
				}
			}.get();

			
			smedium = new ComposerThings.ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					ComposerDests.Tile t = d.s16;
					s.combo.init(0, s.singles.body().y2(), 8, 2, 2, t);
					for (int i = 0; i < 16; i++)
						s.combo.setVar(i).paste(1, true);
				
					return t.saveGame();
				}
			}.get();
			
			TILE_SHEET large = new ComposerThings.ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					ComposerDests.Tile t = d.s16;

					s.combo.init(0, s.combo.body().y2(), 6, 3, 3, t);
					for (int i = 0; i < 16; i++) {
						s.combo.setVar(i).paste(1, true);
					}
				
					return t.saveGame();
				}
			}.get();
			
			this.small = make(SheetType.s1x1, ssmall);
			this.small.d.shadowLength = 5;
			this.small.d.shadowHeight = 2;
			
			this.medium = make(SheetType.s2x2, smedium);
			this.medium.d.shadowLength = 10;
			this.medium.d.shadowHeight = 6;
			this.big = make(SheetType.s3x3, large);
			this.big.d.shadowLength = 10;
			this.big.d.shadowHeight = 6;
			
		}
		
		public void update(double ds) {
			top.update(ds);
			leafs.update(ds);
			swayer.update();
		}

		private SheetPair make(SheetType type, TILE_SHEET sh){
			Sheet s = new FSheet(type, sh);
			ArrayList<Sheet> shh = new ArrayList<Sheet>(s);
			SPRITES.GAME().add(type, shh, "_TREE");
			return new SheetPair(shh.get(0), new SheetData());
			
		}
		
		private static class FSheet extends Sheet.Imp {

			public FSheet(SheetType type, TILE_SHEET s) {
				super(type, s, false);
			}
			
			private final ColorImp col = new ColorImp();
			
			@Override
			public void render(SheetData da, int x, int y, RenderIterator it, SPRITE_RENDERER sr, int tile, int random,
					double degrade) {
				
				int ran = random;
				int colI = (ran >> 7)&31;
				int swI = (ran >> 10)&63;
				
				
				if (degrade > 0) {

					col.interpolate(SETT.TERRAIN().colors.tree.get(random), SETT.TERRAIN().colors.tree.dry(random), degrade);
				}else {
					col.set(SETT.TERRAIN().colors.tree.get(random));
				}
				col.bind();
				x += TERRAIN().TREES.sprites.swayer.dx[swI];
				y -= TERRAIN().TREES.sprites.swayer.dy[swI];
				int offX = it.oX()+TERRAIN().TREES.sprites.swayer.dx[swI];
				int offY = it.oY()-TERRAIN().TREES.sprites.swayer.dy[swI];
				
				it.setOff(offX, offY);
				sheet.render(sr, tile, x , y);
				if (S.get().graphics.get() > 0) {
					TERRAIN().TREES.sprites.top.render(x, y, sr, 0, it, colI);
					TERRAIN().TREES.sprites.leafs.render(x, y, it.ran());
				}
				
				
				
				COLOR.unbind();
				it.countVegetation();
				it.countVegetation();
			}
			
			@Override
			public void renderShadow(SheetData da, int x, int y, RenderIterator it, ShadowBatch shadow, int tile,
					int random) {
				super.renderShadow(da, x, y, it, shadow, tile, random);
			}
		}
		
		private static class TForestLeafs {
			
			private final static int AMOUNT = 32;
			private final LeafTile[] tiles;
			private double time = RND.rFloat()*10000;
			private int t = 0;
			private int am = LeafTile.amount;
			
			private TForestLeafs() {
				tiles = new LeafTile[AMOUNT];
				for (int i = 0; i < AMOUNT; i++) {
					tiles[i] = new LeafTile();
				}
				update(0);
			}
				
			public void render(int x, int y, int ran) {
				tiles[ran&0x01F].render(x, y, t, am);
			}
			
			void update(double ds) {
				
				am = LeafTile.amount;
				if (SETT.WEATHER() != null) {
					time+= ds*(1.0 + 3*SETT.WEATHER().wind.getD());
					double winter = 1.0-SETT.WEATHER().growth.getD();
					if (winter > 0.5 && !SETT.WEATHER().growth.isAutumn()) {
						am = (int) ((1.0-(winter-0.5)*2)*LeafTile.amount);
						
					}
				}
				if (time > 10000)
					time -= 10000;
				t = ((int)(time*LeafTile.ticksPerTime)) & LeafTile.tmask;
			}
			
			private static class LeafTile {
				
				private static final int ticks = 128;
				private static final int tmask = ticks-1;
				private static final int amount = 4;
				private static final double time = 10;
				private static final double ticksPerTime = ticks/time;
				//private final COLOR[] colors = new COLOR[amount];
				
				private final byte[][] xs = new byte[ticks][amount];
				private final byte[][] ys = new byte[ticks][amount];
				private final int ran = RND.rInt(AMOUNT);
				
				private LeafTile() {
					
					for (int a = 0; a < amount; a++) {
						
						double dvx = -(6*C.TILE_SIZE + RND.rFloat(6*C.TILE_SIZE));
						double dvy = (6*C.TILE_SIZE + RND.rFloat0(3*C.TILE_SIZEH));
						dvx /= (double)(ticks);
						dvy /= (double)(ticks);
						double y = RND.rInt(C.TILE_SIZE);
						double x = RND.rInt(C.TILE_SIZEH);

						int tStart = RND.rInt(ticks);
						int tStop = ticks/16 + RND.rInt(3*ticks/16);
						
						double xsin = RND.rFloat(1);
//						double dxsin = dv + RND.rFloat(4);
//						double dysin = RND.rFloat(1);
						double ysin = RND.rFloat(1);
						double dsin = RND.rFloat()/ticks;
						
						for (int t = 0; t < ticks; t++) {
						
							xs[tStart][a] = (byte) (x);
							ys[tStart][a] = (byte) (y);
//							xsin += dxsin;
//							ysin += dysin;
							tStop--;
							if (tStop >= 0) {
								x+= dvx*Math.sin(xsin);
								y+= dvy*Math.sin(ysin);
								xsin+=dsin;
								ysin+=dsin;
							}
							tStart++;
							tStart &= tmask;
						}
					}
				}
				
				private void render(int x, int y, int t, int amount) {
					
					byte[] xs = this.xs[t];
					byte[] ys = this.ys[t];
					
					for (int i = 0; i < amount; i++) {
						SETT.TERRAIN().colors.tree.get(ran).bind();
						CORE.renderer().renderParticle(x+xs[i], y+ys[i]);
					}
					COLOR.unbind();
				}
			}

			
		}
		
		private static final class TForestTop {

			private final int max = 32;
			private final double[] speeds = new double[max];
			private final double[] speedsMax = new double[max];

			private final TileTextureScroller[] dis = new TileTextureScroller[max];
				
			public TForestTop() {
				for (int i = 0; i < max; i++) {
					speedsMax[i] = 0.5 + RND.rFloat();
					speeds[i] = 0.5 + RND.rFloat();
					dis[i] = SPRITES.textures().dis_low.scroller(12*6, -12*5.5);
				}
				
				
			}
			
			void update(double ds) {
				
				for (int i = 0; i < max; i++) {
					if (speeds[i] > speedsMax[i]) {
						speeds[i] -= speedsMax[i]*ds*0.2;
						if (speeds[i] < speedsMax[i])
							speedsMax[i] = 0.5 + RND.rFloat();
					}else if(speeds[i]<= speedsMax[i]) {
						speeds[i] += speedsMax[i]*ds*0.2;
						if (speeds[i] >= speedsMax[i])
							speedsMax[i] = 0.5 + RND.rFloat();
					}
					dis[i].update(ds*(speeds[i])*SETT.WEATHER().wind.getD());
				}
				//dis.update(ds);
			}

			
			public void render(int x, int y, SPRITE_RENDERER r, int tile, RenderIterator i, int ran) {
				
				ran &= 0x01F;
				OPACITY.O50.bind();
				TextureCoords t = SPRITES.textures().dots.get(i.tx(), i.ty(), 0, 0);
				TextureCoords d = dis[ran].get(i.tx(), i.ty());
				CORE.renderer().renderDisplaced(x, x+C.TILE_SIZE, y, y+C.TILE_SIZE, d, t);
				OPACITY.unbind();
			}
			
			
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
				
				double d = (SETT.WEATHER().wind.getD()-0.4)/0.4;
				if (d < 0)
					d = 0;
				
				dd += ds*d*4;

				for (int i = 0; i < am; i++) {
					double cos = (Math.cos(dd+ran[i]));
					double a = dz[i]*4*cos;
					dx[i] = (byte) (a);
					dy[i] = (byte) (a);
				}
				
			}
			
			
		}
		
	}


	
}
