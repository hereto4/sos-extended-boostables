package world.map.terrain;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.TAREA;
import static world.WORLD.TWIDTH;

import java.io.IOException;

import game.time.TIME;
import init.constant.Config.ConfigWorld;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.info.GFORMAT;
import util.rendering.RenderData.RenderIterator;
import util.text.Dic;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import view.tool.ToolManager;
import world.WORLD;
import world.map.regions.Region;
import world.map.terrain.WorldTerrain.WorldTerrainResource;
import world.region.RD;

public class WorldGround extends WorldTerrainResource {

	private final Bitsmap1D rotData = new Bitsmap1D(0, 4, TAREA());
	private final Bitsmap1D ids = new Bitsmap1D(0, 4, TAREA());

	private final LIST<WGROUND> all;
	private final COLOR[] seasonColors = new COLOR[64];
	
	public final WorldGroundSprites sprites = new WorldGroundSprites();

	
	public final SPRITE icon = new SPRITE.Imp(Icon.L) {
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			COLOR.WHITE100.render(r, X1-1, X2, Y1-1, Y2);
			COLOR.BLACK.render(r, X1, X2+1, Y1, Y2+1);
			all.get(0).col.bind();
			sprites.sheets[0].render(r, 0, X1, X2, Y1, Y2);
			COLOR.unbind();
		}
	};

	private final WGROUND PATCHED_GRASS;

	

	WorldGround() throws IOException{

		
		final int am = 9;
		final int pg = am/2;
		
		ArrayList<WGROUND> all = new ArrayList<>(16);
		
		Json json = ConfigWorld.json("Ground");
		
		COLOR[] cols = new COLOR[am];
		LIST<ColorImp> source = ColorImp.cols(json, "COLOR");
		

			
		
		for (int i = 0; i < cols.length; i++) {
			double d = i;
			d /= cols.length-1;
			d *= source.size()-1;
			int fi = (int) d;
			double inter = d-fi;
			if (fi < source.size()-1)
				cols[i] = new ColorImp().interpolate(source.get(fi), source.get(fi+1), inter);
			else
				cols[i] = source.get(fi);
			
		}
		
		//COLOR[] cols = COLOR.interpolate(new ColorImp(json, "COLOR_WET"), new ColorImp(json, "COLOR_DRY"), am);
		COLOR[] colsD = COLOR.interpolate(new ColorImp(json, "COLOR_DEVASTATED_WET"), new ColorImp(json, "COLOR_DEVASTATED_DRY"), am);
		for (int i = 0; i < am; i++) {
			float f = (float) (1.0-i/(am-1.0));
			f = (float) Math.pow(f, 2);
			int bg = i < pg ? i+1 : i-1;
			if (i == pg)
				bg = pg;
			TILE_SHEET sh = sprites.sheets[(sprites.sheets.length-1)*i/(am-1)];
			new WGROUND(all, "ground: " + i, f, bg, cols[i], sh, colsD[i]);
		}

		PATCHED_GRASS = all.get(pg);
		
		
		this.all = all;


		ColorImp winter = new ColorImp(90, 105, 127);
		for (double i = 0; i < seasonColors.length; i++) {
			ColorImp p = new ColorImp();
			double d = i / (seasonColors.length - 1);
			if (d < 0.5)
				d = d * 2;
			else
				d = 1.0 - (d - 0.5) * 2;
			
			p.interpolate(COLOR.WHITE100, winter, CLAMP.c(d+0.75+0.125, 1));
			seasonColors[(int) i] = p;

		}
		ids.setAll(all.size()/2);
	}

	public LIST<WGROUND> all() {
		return all;
	}

	@Override
	protected void save(FilePutter f) {
		rotData.save(f);
		ids.save(f);
	}

	@Override
	protected void load(FileGetter f) throws IOException {
		rotData.load(f);
		ids.load(f);
	}
	
	@Override
	protected void clear() {
		ids.setAll(all.size()/2);
		rotData.setAll(0);
	}

	public void renderInit(double season) {
		
		int i = (int) (TIME.years().bitPartOf() * seasonColors.length);
		i %= seasonColors.length;
		
		ColorImp.TMP.interpolate(COLOR.WHITE100, seasonColors[i], season).bind();
		for (WGROUND g : all) {
			g.colImp.set(g.col);
			g.colImp.multiply(ColorImp.TMP);
		}
	}
	
	public void render(Renderer r, RenderIterator it) {

		
		WGROUND g = all.get(ids.get(it.tile()));
		int d = rotData.get(it.tile());
		Region reg = WORLD.REGIONS().map.get(it.tile());
		if (reg != null) {
			
			double ii = RD.DEVASTATION().current.getD(reg);
			if (ii > 0.25) {
				g.cdeva[(int) ((ii-0.1)*g.cdeva.length)].bind();
				int i = (int) ((ii-0.25)*2);
				sprites.renderNormal(sprites.cracked[i], r, it.x(), it.y(), sprites.ran(it.tx(), it.ty()));
				return;
			}
		}
		
		TILE_SHEET over = g.over;
		
		
		
		if (d == 0x0F) {
			g.colImp.bind();
			sprites.renderNormal(over, r, it.x(), it.y(), sprites.ran(it.tx(), it.ty()));
		}else {
			
			WGROUND bg = all.get(g.bg);
			bg.colImp.bind();
			sprites.renderNormal(bg.over, r, it.x(), it.y(), sprites.ran(it.tx(), it.ty()));
			if (g != bg) {
				g.colImp.bind();
				sprites.renderStenciled(over, r, it.x(), it.y(), d, sprites.ran(it.tx(), it.ty()), it.ran());
			}
		}

	}
	
	public final MAP_DOUBLE moisture = new MAP_DOUBLE() {
		
		@Override
		public double get(int tx, int ty) {
			if (!WORLD.IN_BOUNDS(tx, ty))
				return 0.5;
			return getter.get(tx, ty).moisture();
		}
		
		@Override
		public double get(int tile) {
			return getter.get(tile).moisture();
		}
	};
	
//	public void render(Renderer r, RenderData data, double season) {
//
//		RenderIterator it = data.onScreenTiles();
//		int i = (int) (TIME.years().bitPartOf() * seasonColors.length);
//		i %= seasonColors.length;
//		
//		ColorImp.TMP.interpolate(COLOR.WHITE100, seasonColors[i], season).bind();
//		for (WGROUND g : all) {
//			g.colImp.set(g.col);
//			g.colImp.multiply(ColorImp.TMP);
//		}
//		
//		
//		;
//		//CORE.renderer().setUniLight(light);
//		while (it.has()) {
//			WGROUND g = all.get(ids.get(it.tile()));
//			int d = rotData.get(it.tile());
//			TILE_SHEET over = g.over;
//			if (over == sprites.desert)
//				over = WORLD.CLIMATE().getter.get(it.tile()) == CLIMATES.COLD() ? sprites.steppe : sprites.desert;
//			if (d == 0x0F) {
//				g.colImp.bind();
//				sprites.renderNormal(sprites.normal, r, it.x(), it.y(), sprites.ran(it.tx(), it.ty()));
//				g.op.bind();
//				sprites.renderNormal(over, r, it.x(), it.y(), sprites.ran(it.tx()+3, it.ty()+6));
//				OPACITY.unbind();
//			}else {
//				WGROUND bg = all.get(g.bg);
//				bg.colImp.bind();
//				sprites.renderNormal(sprites.normal, r, it.x(), it.y(), sprites.ran(it.tx(), it.ty()));
//				bg.op.bind();
//				sprites.renderNormal(over, r, it.x(), it.y(), sprites.ran(it.tx()+3, it.ty()+6));
//				OPACITY.unbind();
//				if (g != bg) {
//					g.colImp.bind();
//					sprites.renderStenciled(sprites.normal, r, it.x(), it.y(), d, sprites.ran(it.tx(), it.ty()), it.ran());
//					g.op.bind();
//					sprites.renderStenciled(over, r, it.x(), it.y(), d, sprites.ran(it.tx()+3, it.ty()+6), it.ran());
//					OPACITY.unbind();
//				}
//			}
//			it.next();
//		}
//		COLOR.unbind();
//
//	}

	public void renderStencil(SPRITE_RENDERER r, int x, int y, int tile, int ran) {
		sprites.stencil.render(r, tile+16*(ran&3), x, y);
	}
	


	private void set(int tx, int ty, int code, int data) {
		int t = tx + ty * TWIDTH();
		rotData.set(t, data);
		ids.set(t, code);
		WORLD.changeTile(tx, ty);
	}

//	/**
//	 * 
//	 * @param x
//	 * @param y
//	 * @return value between -1 to 0.5
//	 */
//	public float getFertility(int x, int y) {
//		if (IN_BOUNDS(x, y))
//			return getter.get(x, y).fertility();
//		return 0;
//	}

	public final MAP_OBJECT<WGROUND> getter = new MAP_OBJECT<WorldGround.WGROUND>() {

		@Override
		public WGROUND get(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return null;
			return get(tx + ty * TWIDTH());
		}

		@Override
		public WGROUND get(int tile) {
			return all.get(ids.get(tile));
		}
	};

	public class WGROUND extends PlacableMulti {

		protected final double moisture;
		protected final int code;
		private final int bg;
		private final COLOR col;
		private final COLOR[] cdeva;
		private final ColorImp colImp = new ColorImp();
		private final TILE_SHEET over;
		//private final OPACITY op;
		
		protected WGROUND(LISTE<WGROUND> all, String name, double moisture, int bg, COLOR col, TILE_SHEET sheet, COLOR deva) {
			super(name);
			code = all.add(this);
			this.moisture = moisture;
			this.bg = bg;
			this.col = col;
			this.cdeva = new COLOR[8];
			for (int i = 0; i < cdeva.length; i++) {
				cdeva[i] = new ColorImp().interpolate(col, deva, (i+1.0)/cdeva.length);
			}
			

			
			over = sheet;

//			double ff = (fertility-0.1)/0.7;
//			if (ff < 0.5) {
//				over = sprites.desert;
//				op = new OpacityImp((int) (0xFF*(1.0-ff*2)));
//				
//			}else {
//				over = sprites.lush;
//				op = new OpacityImp((int) (0xFF*(Math.pow((ff-0.5)*2, 2))));
//			}
			
		}

		boolean place(int tx, int ty) {
			int res = 0;
			
			if (bg < PATCHED_GRASS.code) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if (!WORLD.IN_BOUNDS(tx, ty, d))
						continue;
					
					WGROUND neigh = getter.get(tx + d.x(), ty + d.y());
					
					if (neigh.code > bg) {
						all.get(bg).place(tx, ty);
						return true;	
					}
				}
				
			}else if (bg > PATCHED_GRASS.code) {
				
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if (!WORLD.IN_BOUNDS(tx, ty, d))
						continue;
					
					WGROUND neigh = getter.get(tx + d.x(), ty + d.y());
					
					if (neigh.code < bg) {
						all.get(bg).place(tx, ty);
						return true;	
					}
				}
				
			}
			
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!WORLD.IN_BOUNDS(tx, ty, d)) {
					res |= d.mask();
					continue;
				}
				WGROUND neigh = getter.get(tx + d.x(), ty + d.y());
				
				if (neigh.bg == code || neigh.code == code)
					res |= d.mask();
				
			}

			set(tx, ty, code, res);
			
			return false;
		}

		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			if (!IN_BOUNDS(tx, ty))
				return;
			WGROUND old = getter.get(tx, ty);
			place(tx, ty);
			if (old != getter.get(tx, ty)) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if (IN_BOUNDS(tx, ty, d)) {
						getter.get(tx + d.x(), ty + d.y()).place(tx + d.x(), ty + d.y(), area, type);
					}
				}
			}
		}

		final void placeRaw(int tx, int ty) {
			set(tx, ty, code, 0);
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {

			return null;
		}

		@Override
		public SPRITE getIcon() {
			return SPRITES.icons().m.cancel;
		}

		public boolean is(int tx, int ty) {
			return IN_BOUNDS(tx, ty) && code == ids.get(tx + ty * TWIDTH());
		}


		public double moisture() {
			return moisture;
		}

		public WGROUND fallback() {
			return PATCHED_GRASS;
		}

	}

	@Override
	public LIST<PLACABLE> placers(ToolManager tm) {
		ArrayListGrower<PLACABLE> pp = new ArrayListGrower<>();
		
		pp.add(new PlacableMulti(Dic.¤¤ground) {
			WGROUND g = all.get(0);
			final LinkedList<CLICKABLE> butts = new LinkedList<>();
			{
				for (int i = 0; i < all.size(); i++) {
					WGROUND ggg = all.get(i);
					butts.add(new GButt.ButtPanel(GFORMAT.toNumeral(new Str(4), i+1)){
						
						@Override
						protected void clickA() {
							g = ggg;
						};
						@Override
						protected void renAction() {
							selectedSet(g == ggg);
						};
					});
				}
			}
			
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				g.place(tx, ty, area, type);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
			
			@Override
			public SPRITE getIcon() {
				return icon;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
			
		});
		return pp;
	}
	


}
