package world.map.terrain;

import static world.WORLD.FOREST;
import static world.WORLD.IN_BOUNDS;
import static world.WORLD.MOUNTAIN;
import static world.WORLD.TAREA;
import static world.WORLD.TWIDTH;

import java.io.IOException;
import java.util.Arrays;

import game.debug.Profiler;
import game.time.TIME;
import init.constant.C;
import init.constant.Config.ConfigWorld;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.TERRAINS;
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
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GButt;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import view.tool.ToolManager;
import world.WORLD;
import world.map.terrain.WorldTerrain.WorldTerrainResource;
import world.map.terrain.WorldWater.WATER;

public class WorldWater extends WorldTerrainResource implements MAP_OBJECT<WATER>{

	private final Bitsmap1D tiles = new Bitsmap1D(-1, 4, TAREA());
	private final byte[] data;
	private final ArrayListResize<WATER> all = new ArrayListResize<>(255, 255);
	private final COLOR[] seasonColors = new COLOR[64];
	private final WorldWaterSprites sprites = new WorldWaterSprites();
	
	private static CharSequence ¤¤delta = "delta";
	private static CharSequence ¤¤normal = "normal";
	private static CharSequence ¤¤deep = "deep";
	
	static {
		D.ts(WorldWater.class);
	}
	
	
	public final WATER NOTHING = new WATER("clear") {
		
		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			return false;
		}
		
		@Override
		void pplace(int tx, int ty) {
			placeRaw(tx, ty);
		}
		
		@Override
		boolean coversCompleatly(int tile) {
			return false;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return null;
		}; 
		
		@Override
		boolean isFertile() {
			return false;
		}

		@Override
		protected boolean canTravelTo(int data, DIR to) {
			return false;
		}
		
		private SPRITE icon;
		
		@Override
		public SPRITE getIcon() {
			if (icon == null)
				icon = OCEAN.icon.twin(UI.icons().m.anti, DIR.C, 0);
			return icon;
		}
		

		@Override
		public PLACABLE getUndo() {
			return null;
		}
		

	};
	
	public final OpenSet LAKE;
	public final OpenSet OCEAN;
	public final WATER RIVER = new River();
	public final WATER RIVER_SMALL = new RiverSmall();
	
	WorldWater() throws IOException{

		Json js = ConfigWorld.json("Water");
		
		LAKE = new OpenSet("lake", new ColorImp(js, "COLOR_LAKE_SHORE"), new ColorImp(js, "COLOR_LAKE"), true);
		OCEAN = new OpenSet("ocean", new ColorImp(js, "COLOR_OCEAN_SHORE"), new ColorImp(js, "COLOR_OCEAN"), false);
		
		data = new byte[TAREA()];
		all.trim();
		
		ColorImp winter = new ColorImp(127, 100, 127);
		for (double i = 0; i < seasonColors.length; i++) {
			ColorImp p = new ColorImp();
			double d = i/(seasonColors.length-1);
			if (d < 0.5)
				d = d*2;
			else
				d = 1.0 - (d-0.5)*2;
			
			p.interpolate(COLOR.WHITE100, winter, d);
			seasonColors[(int) i] = p;
			
		}

		
	}
	
	public LIST<WATER> all(){
		return all;
	}
	
	@Override
	protected void load(FileGetter saveFile) throws IOException {
		saveFile.bs(data);
		tiles.load(saveFile);
		
	}

	@Override
	protected void save(FilePutter saveFile){
		saveFile.bs(data);
		tiles.save(saveFile);
	}
	
	@Override
	protected void clear() {
		tiles.setAll(0);
		Arrays.fill(data, (byte)0);
	}
	
	@Override
	protected void update(double ds, Profiler prof) {
		sprites.update(ds);
	}
	
	public void renderShorelines(Renderer r, RenderData data, double season){
		
		RenderIterator it = data.onScreenTiles();

		while(it.has()) {
			ColorImp.TMP.bind();
			if (get(it.tx(), it.ty()).renderShore(r, dataGet(it.tile()), it)) {
				it.hiddenSet();
			}
			it.next();
		}
		COLOR.unbind();
	}
	
	public void renderShorelines(Renderer r, RenderIterator it){
		
		if (get(it.tx(), it.ty()).renderShore(r, dataGet(it.tile()), it)) {
			it.hiddenSet();
		}
	}
	
	public void render(Renderer r, RenderData data, double season){
		
		RenderIterator it = data.onScreenTiles();
		
		int i = (int) (TIME.years().bitPartOf()*seasonColors.length);
		i %= seasonColors.length;
		ColorImp.TMP.interpolate(COLOR.WHITE100, seasonColors[i], season).bind();
		LAKE.update(i, ColorImp.TMP);
		OCEAN.update(i, ColorImp.TMP);
		
		while(it.has()) {
			
			if (get(it.tx(), it.ty()).render(r, dataGet(it.tile()), it)) {
				it.hiddenSet();
			}
			it.next();
		}
		COLOR.unbind();
	}
	
	
	private int dataGet(int tile) {
		return data[tile] & 0xFF;
	}
	
	private void dataSet(int tx, int ty, int d) {
		dataSet(tx+ty*TWIDTH(), d);
	}
	
	private void dataSet(int tile, int d) {
		data[tile] = (byte) d;
	}
	
	@Override
	public WATER get(int tx, int ty){
		if (!IN_BOUNDS(tx, ty))
			return NOTHING;
		return all.get(tiles.get(tx+ty*TWIDTH())); 
	}
	
	@Override
	public WATER get(int tile) {
		return all.get(tiles.get(tile)); 
	}
	
	public abstract class WATER extends PlacableMulti implements MAP_BOOLEAN{
		
		protected final int code;
		
		protected WATER(String name) {
			super(name);
			this.code = all.add(this);
		}

		abstract boolean coversCompleatly(int tile);

		void placeRaw(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				tiles.set(tx + ty*TWIDTH(), code);
		}

		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			if(IN_BOUNDS(tx, ty)) {
				int old = tiles.get(tx+ty*TWIDTH());
				pplace(tx, ty);
				if (old != tiles.get(tx+ty*TWIDTH())) {
					for (int i = 0; i < DIR.ALL.size(); i++) {
						DIR d = DIR.ALL.get(i);
						get(tx+d.x(), ty+d.y()).pplace(tx+d.x(), ty+d.y());
						WORLD.changeTile(tx, ty);
					}
				}
			}
		}

		
		
		abstract void pplace (int tx, int ty);
		
		final void place (int tx, int ty, DIR d) {
			pplace(tx+d.x(), ty+d.y());
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)) == this; 
		}
		
		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;
			return is(tx+ty*TWIDTH()); 
		}
		
		abstract boolean render(SPRITE_RENDERER r, int data, RenderIterator it);
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			return false;
		}
		boolean rend2erMid(SPRITE_RENDERER r, int data, RenderIterator it) {
			return false;
		}
		abstract boolean isFertile();

		@Override
		public PLACABLE getUndo() {
			return NOTHING;
		}
		
		protected abstract boolean canTravelTo(int data, DIR to);
		
	}
	
	public final class OpenSet {

		private final COLOR cShore;
		private final COLOR cWater;
		private final ColorImp col = new ColorImp();
		public final WATER normal;
		public final WATER deep;
		public final WATER delta;
		public final PLACABLE placer;
		public final SPRITE icon;
		public final CharSequence name;
		
		private OpenSet(String name, COLOR cShore, COLOR cWater, boolean isFertile) {
			this.cShore = cShore;
			this.cWater = cWater;
			
			this.name = name;
			delta = new Delta(name + " (" + ¤¤delta + ")", this);
			normal = new Normal(name + " (" + ¤¤normal + ")", this, isFertile);
			deep = new Deep(name + " (" + ¤¤deep + ")", this, isFertile);
			
			icon = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					((Normal)normal).renderIcon(r, X1, Y1, X2-X1);
					
				}
			};
			
			placer = new PlacableMulti(name, "", icon) {
				LinkedList<CLICKABLE> butts = new LinkedList<>();
				WATER current = normal;
				{
					butts.add(new GButt.ButtPanel(¤¤normal) {
						@Override
						protected void clickA() {
							current = normal;
						}
						@Override
						protected void renAction() {
							selectedSet(current == normal);
						};
						
					});
					butts.add(new GButt.ButtPanel(¤¤deep) {
						@Override
						protected void clickA() {
							current = deep;
						}
						@Override
						protected void renAction() {
							selectedSet(current == deep);
						};
						
					});
					butts.add(new GButt.ButtPanel(¤¤delta) {
						@Override
						protected void clickA() {
							current = delta;
						}
						@Override
						protected void renAction() {
							selectedSet(current == delta);
						};
						
					});
				}
				
				@Override
				public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
					current.place(tx, ty, area, type);
					WORLD.MINIMAP().updateRegion(null);
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
					return current.isPlacable(tx, ty, area, type);
				}
				
				@Override
				public LIST<CLICKABLE> getAdditionalButt() {
					return butts;
				}
				
				
				@Override
				public PLACABLE getUndo() {
					return NOTHING;
				}
				
			};
			
		}
		
		void update(double ts, COLOR season) {
			col.set(cWater);
			col.multiply(season);
		}
		
		public MAP_BOOLEAN is = new MAP_BOOLEAN() {
			
			@Override
			public boolean is(int tx, int ty) {
				return normal.is(tx, ty) || deep.is(tx, ty) || delta.is(tx, ty);
			}
			
			@Override
			public boolean is(int tile) {
				return normal.is(tile) || deep.is(tile) || delta.is(tile);
			}
		};
		
		public MAP_BOOLEAN isOpen = new MAP_BOOLEAN() {
			
			@Override
			public boolean is(int tx, int ty) {
				return normal.is(tx, ty) || deep.is(tx, ty);
			}
			
			@Override
			public boolean is(int tile) {
				return normal.is(tile) || deep.is(tile);
			}
		};

		
	}
	
	private final class Normal extends WATER {

		private final OpenSet set;
		private final boolean fertile;
		
		private Normal(String name, OpenSet set, boolean fertile) {
			super(name);
			this.set = set;
			this.fertile = fertile;
		}

		
		@Override
		boolean coversCompleatly(int tile) {
			return dataGet(tile) == 0x0F;
		}

		@Override
		void pplace(int tx, int ty) {
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (joins(tx,ty,d)) {
					res |= d.mask();
				}
			}
			int edge = 0;
			for (int i = 0; i < DIR.NORTHO.size(); i++) {
				DIR d = DIR.NORTHO.get(i);
				if (!joins(tx, ty, d) && joins(tx, ty, d.next(-1)) && joins(tx, ty, d.next(1))) {
					edge |= d.mask();
				}
			}
			if (res == 0x0F) {
				MOUNTAIN().pClear(tx, ty);
				FOREST().amount.set(tx, ty, 0);
			}
			res |= edge << 4;
			dataSet(tx, ty, res);
		}
		
		private boolean joins(int tx, int ty, DIR d) {
			return !IN_BOUNDS(tx, ty, d) || is(tx, ty, d) || set.deep.is(tx, ty, d) || set.delta.is(tx, ty, d);
		}
		
		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			int d = data & 0x0F;
			int c = (data >> 4)&0x0F;
			set.col.bind();
			sprites.render(r, it, d, c);
			return data == 0x0F;
		}
		
		@Override
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			set.cShore.bind();
			int d = data & 0x0F;
			int c = (data >> 4)&0x0F;
			sprites.renderBackground(r, it, d, c);
			return data == 0x0F;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return null;
		}
		
		@Override
		boolean isFertile() {
			return fertile;
		}
		
		public void renderIcon(SPRITE_RENDERER r, int x, int y, int dim) {
			set.cShore.bind();
			sprites.bgSingles.render(r, 0, x, x+dim, y, y+dim);
			sprites.bgSingles.render(r, 0, x, x+dim, y, y+dim);
			set.cWater.bind();
			sprites.sheetSingles.render(r, 0, x, x+dim, y, y+dim);
			COLOR.unbind();
		}


		@Override
		protected boolean canTravelTo(int data, DIR to) {
			if (to.isOrtho())
				return (data & to.mask()) != 0;
			data = data >>>4;
			return (data & to.mask()) == 0;
		}

	}
	
	private final class Deep extends WATER {

		private final OpenSet set;
		private final boolean fertile;
		
		private Deep(String name, OpenSet set, boolean fertile) {
			super(name);
			this.set = set;
			this.fertile = fertile;
		}
		
		@Override
		boolean coversCompleatly(int tile) {
			return true;
		}

		@Override
		void pplace(int tx, int ty) {
			
			if (isPlacable(tx, ty, null, null) != null) {
				set.normal.pplace(tx, ty);
				return;
			}
			
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!IN_BOUNDS(tx, ty, d) || this.is(tx, ty, d)) {
					res |= d.mask();
				}
			}
			MOUNTAIN().pClear(tx, ty);
			FOREST().amount.set(tx, ty, 0);
			dataSet(tx, ty, res);
		}

		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			set.col.bind();
			sprites.renderDeep(r, it, data&0x0F);
			return true;
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				if (IN_BOUNDS(tx, ty, d) && !is(tx, ty, d) && !set.normal.is(tx, ty, d))
					return "";
				if (set.delta.is(tx, ty))
					return "";
			}
			return null;
		}
		
		@Override
		boolean isFertile() {
			return fertile;
		}
		
		@Override
		protected boolean canTravelTo(int data, DIR to) {
			return true;
		}

	}

	private final class River extends WATER{

		private River() {
			super("river");
		}

		@Override
		boolean coversCompleatly(int tile) {
			return dataGet(tile) == 0x0F;
		}

		@Override
		void pplace(int tx, int ty) {
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!IN_BOUNDS(tx, ty, d) || this.is(tx, ty, d)) {
					res |= d.mask();
				}else if(LAKE.delta.is(tx, ty, d) || OCEAN.delta.is(tx, ty, d)) {
					int x = tx+d.x()*2;
					int y = ty+d.y()*2;
					if (OCEAN.normal.is(x, y) || LAKE.normal.is(x, y)) {
						res |= d.mask();
					}
				}
			}
			if (res == 0x0F) {
				MOUNTAIN().pClear(tx, ty);
				FOREST().amount.set(tx, ty, 0);
			}
			dataSet(tx, ty, res);
		}

		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			LAKE.col.bind();
			sprites.riverFG.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			sprites.renderTexture(it);
			return false;
		}
		
		@Override
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			LAKE.cShore.bind();
			sprites.riverBG.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			return false;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		boolean isFertile() {
			return true;
		}

		@Override
		protected boolean canTravelTo(int data, DIR to) {
			if (to.isOrtho())
				return (data & to.mask()) != 0;
			return (data & to.mask()) != 0 || (data & to.next(1).mask()) != 0;
		}
		
		private final SPRITE iconSprite = new SPRITE.Imp(C.T_PIXELS*2) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				LAKE.cShore.bind();
				sprites.riverBG.render(r, 0, X1, X2, Y1, Y2);
				LAKE.col.bind();
				sprites.riverFG.render(r, 0, X1, X2, Y1, Y2);
				COLOR.unbind();
			}
		};
		
		@Override
		public SPRITE getIcon() {
			return iconSprite;
		}
		
	}
	
	private final class RiverSmall extends WATER{

		private RiverSmall() {
			super("river small");
		}

		@Override
		boolean coversCompleatly(int tile) {
			return false;
		}

		@Override
		void pplace(int tx, int ty) {
			placeRaw(tx, ty);
			int res = 0;
			int other = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!IN_BOUNDS(tx, ty, d) || this.is(tx, ty, d)) {
					res |= d.mask();
				}else if(get(tx, ty, d) != NOTHING) {
					res |= d.mask();
					if (!this.is(tx, ty, d))
						other |= d.mask();
				}
			}
			dataSet(tx, ty, res | (other <<4));
		}
		
		@Override
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			
			
			
			
			LAKE.cShore.bind();
			int o = data >>> 4;
			data &= 0x0F;
			sprites.riverSmallBG.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			if (o != 0) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if ((d.mask() & o) != 0) {
						int x = it.x()+d.x()*C.TILE_SIZE;
						int y = it.y()+d.y()*C.TILE_SIZE;
						int da = d.perpendicular().mask();
						sprites.riverSmallBG.render(r, da+(it.ran()&7)*16, x, y);
					}
				}
			}
			
			LAKE.col.bind();
			sprites.riverSmallFG.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			if (o != 0) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if ((d.mask() & o) != 0) {
						int x = it.x()+d.x()*C.TILE_SIZE;
						int y = it.y()+d.y()*C.TILE_SIZE;
						int da = d.perpendicular().mask();
						sprites.riverSmallFG.render(r, da+(it.ran()&7)*16, x, y);
					}
				}
			}
			
			return false;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return null;
		}
		
		@Override
		boolean isFertile() {
			return true;
		}

		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			return false;
		}

		@Override
		protected boolean canTravelTo(int data, DIR to) {
			return false;
		}
		
		private final SPRITE iconSprite = new SPRITE.Imp(C.T_PIXELS*2) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				LAKE.cShore.bind();
				sprites.riverSmallBG.render(r, 0, X1, X2, Y1, Y2);
				LAKE.col.bind();
				sprites.riverSmallFG.render(r, 0, X1, X2, Y1, Y2);
				COLOR.unbind();
			}
		};
		
		@Override
		public SPRITE getIcon() {
			return iconSprite;
		}
		
	}
	
	private final class Delta extends WATER {

		private final OpenSet set;
		
		private Delta(String name, OpenSet set) {
			super(name);
			this.set = set;
		}

		@Override
		boolean coversCompleatly(int tile) {
			return false;
		}

		@Override
		void pplace(int tx, int ty) {
			if (isPlacable(tx, ty, null, null) != null) {
				set.normal.pplace(tx, ty);
				return;
			}
			
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (set.normal.is(tx, ty, d)) {
					res = i;
					break;
				}
			}

			dataSet(tx, ty, res);
		}

		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			RIVER.render(r, DIR.ORTHO.get(data).mask()|DIR.ORTHO.get(data).perpendicular().mask(), it);
			set.col.bind();
			sprites.delta.render(r, data+(it.ran()&3)*4, it.x(), it.y());
			return false;
		}
		
		@Override
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			RIVER.renderShore(r,  DIR.ORTHO.get(data).mask()|DIR.ORTHO.get(data).perpendicular().mask(), it);
			set.cShore.bind();
			sprites.deltaShore.render(r, data+(it.ran()&3)*4, it.x(), it.y());
			return false;
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return bordersCount(tx, ty, set.normal) == 1 ? null : "";
		}
		
		@Override
		boolean isFertile() {
			return true;
		}

		@Override
		protected boolean canTravelTo(int data, DIR to) {
			int m = DIR.ORTHO.get(data).mask() | DIR.ORTHO.get(data).perpendicular().mask();
			return RIVER.canTravelTo(m, to);
		}
	}
	

	
	public MAP_BOOLEAN isRivery = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return RIVER.is(tx, ty) || LAKE.delta.is(tx, ty) || OCEAN.delta.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return RIVER.is(tile) || LAKE.delta.is(tile) || OCEAN.delta.is(tile);
		}
	};
	
	public MAP_BOOLEAN isDELTA = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return get(tx, ty) instanceof Delta;
		}
		
		@Override
		public boolean is(int tile) {
			return get(tile) instanceof Delta;
		}
	};
	
	public boolean canTravelToByBoat(int sx, int sy, DIR d) {
		if (!isBig.is(sx, sy, d))
			return false;
		WATER w = get(sx, sy);
		int dd = dataGet(sx+TWIDTH()*sy);
		return w.canTravelTo(dd, d);
	}
	
	public boolean canCrossByLand(int fromX, int fromY, int toX, int toY) {
		if (!isBig.is(toX, toY))
			return true;
		if (RIVER_SMALL.is(fromX, fromY))
			return true;
		if (RIVER.is(fromX, fromY)) {
			return (Math.abs(fromX-toX) + Math.abs(fromY-toY) <= 1) && !RIVER.is(toX, toY);
		}
		if (has.is(fromX, fromY))
			return false;
		return true;
		
	}

	
	double add(WorldTerrainInfo info, int tx, int ty) {
		if (RIVER.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.5);
			return 0.5;
		}else if (RIVER_SMALL.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.25);
			return 0.25;
		}else if (OCEAN.delta.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.25);
			info.add(TERRAINS.OCEAN(), 0.5);
			return 0.75;
		}else if (LAKE.delta.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.25);
			info.add(TERRAINS.WET(), 0.5);
			return 0.75;
		}else if (OCEAN.isOpen.is(tx, ty)) {
			double m = 0.5;
			for (DIR d : DIR.ORTHO) {
				if (OCEAN.is.is(tx, ty, d)) {
					m += 0.25/2;
				}
			}
			info.add(TERRAINS.OCEAN(), m);
			return m;
		}else if (LAKE.isOpen.is(tx, ty)) {
			double m = 0.5;
			for (DIR d : DIR.ORTHO) {
				if (LAKE.is.is(tx, ty, d)) {
					m += 0.25/2;
				}
			}
			info.add(TERRAINS.WET(), m);
			return m;
		}
		return 0;
			
	}
	
	public MAP_BOOLEAN has = new MAP_BOOLEAN() {
		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;
			return !NOTHING.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)) != NOTHING;
		}
	};
	
	public MAP_BOOLEAN isBig = new MAP_BOOLEAN() {
		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;
			return !NOTHING.is(tx, ty) && !RIVER_SMALL.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)) != NOTHING && all.get(tiles.get(tile)) != RIVER_SMALL;
		}
	};
	
	public MAP_BOOLEAN fertile = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return get(tx, ty).isFertile();
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)).isFertile();
		}
	};
	
	public MAP_BOOLEAN coversTile = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return is(tx+ty*TWIDTH());
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)).coversCompleatly(tile);
		}
	};
	
	public boolean borders(int x, int y, WATER terrain){
		
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			if (terrain.is(x, y, DIR.ORTHO.get(i)))
				return true;
		}	
		return false;
	}
	
	public int bordersCount(int x, int y, WATER tiles) {
		
		int j = 0;
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			if (tiles.is(x, y, DIR.ORTHO.get(i)))
				j++;
		}	
		return j;
	}

	@Override
	public LIST<PLACABLE> placers(ToolManager tm) {
		ArrayListGrower<PLACABLE> placers = new ArrayListGrower<>();
		
		OpenSet[] os = new OpenSet[] {
			LAKE, OCEAN
		};
		for (OpenSet o : os) {
			PlacableMulti placer = new PlacableMulti(o.name, "", o.icon) {
				LinkedList<CLICKABLE> butts = new LinkedList<>();
				WATER current = o.normal;
				{
					butts.add(new GButt.ButtPanel(¤¤normal) {
						@Override
						protected void clickA() {
							current = o.normal;
						}
						@Override
						protected void renAction() {
							selectedSet(current == o.normal);
						};
						
					});
					butts.add(new GButt.ButtPanel(¤¤deep) {
						@Override
						protected void clickA() {
							current = o.deep;
						}
						@Override
						protected void renAction() {
							selectedSet(current == o.deep);
						};
						
					});
					butts.add(new GButt.ButtPanel(¤¤delta) {
						@Override
						protected void clickA() {
							current = o.delta;
						}
						@Override
						protected void renAction() {
							selectedSet(current == o.delta);
						};
						
					});
				}
				
				@Override
				public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
					current.place(tx, ty, area, type);
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
					return current.isPlacable(tx, ty, area, type);
				}
				
				@Override
				public LIST<CLICKABLE> getAdditionalButt() {
					return butts;
				}
				
				
				@Override
				public PLACABLE getUndo() {
					return NOTHING;
				}
				
			};
			
			placers.add(placer);
		}
		
		placers.add(RIVER);
		placers.add(RIVER_SMALL);
		placers.add(NOTHING);
		return placers;
	}
	
}
