package settlement.tilemap.terrain;

import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.TWIDTH;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.resources.Growable;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.bit.Bit;
import snake2d.util.bit.Bits;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.map.MAP_INTE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class TGrowable extends TerrainTile{
	
	public final Growable growable;

	private static CharSequence ¤¤Ripe = "(Ripe)";
	private static CharSequence ¤¤RipeNot = "(Not Ripe)";
	private static CharSequence ¤¤Name = "Wild {0}";
	static {
		D.ts(TGrowable.class);
	}
	public final int gIndex;
	
	private static final Bits bsize	 = new Bits	(0b0000_0000_0000_1111);
	private static final Bits bfruit = new Bits	(0b0000_0000_1111_0000);
	private static final Bit doJob = new Bit	(0b0000_0001_0000_0000);
	private final double sizeI = 1.0/0b01111;
	
	private final TerrainClearing clearing = new TerrainClearing() {
		
		private final SoundRace sound = AUDIO.race("CLEAR_BUSH");
		
		@Override
		public RESOURCE clear1(int tx, int ty) {
			
			size.increment(tx, ty, -3);
		
			if (SETT.WEATHER().growthRipe.cropsAreRipe())
				return growable.resource;
			return null;
		}
		
		@Override
		public boolean can() {
			return true;
		}

		@Override
		public int clearAll(int tx, int ty) {
			int am = (int) Math.ceil(resource.get(tx, ty)/4.0);
			shared.NADA.placeFixed(tx, ty);
			return am;
		}
		
		@Override
		public SoundRace sound(int tx, int ty) {
			return sound;
		}
		
		@Override
		public boolean isEasilyCleared() {
			return true;
		};
	};
	
	public final TAmount size = new TAmount(11, name()) {
		
		@Override
		public int get(int tile) {
			if (TERRAIN().get(tile) == TGrowable.this) {
				return CLAMP.i(1 + bsize.get(shared.data.get(tile)), 1, max);
			}
			return 0;
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			if (value <= 0) {
				if (TERRAIN().get(tile) == TGrowable.this)
					TERRAIN().NADA.placeFixed(tile%TWIDTH, tile/TWIDTH);
			}else {
				if (TERRAIN().get(tile) != TGrowable.this)
					TGrowable.this.placeFixed(tile%TWIDTH, tile/TWIDTH);
				int d = shared.data.get(tile);
				d = bsize.set(d, CLAMP.i(value-1, 0, bsize.mask));
				shared.data.set(tile, d);
			}
			return this;
		}
	};
	
	public final MAP_BOOLEANE job = new MAP_BOOLEANE() {
		
		@Override
		public boolean is(int tx, int ty) {
			return is(tx+ty*TWIDTH);
		}
		
		@Override
		public boolean is(int tile) {
			return SETT.TERRAIN().get(tile) instanceof TGrowable && doJob.is(shared.data.get(tile));
		}
		
		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			return set(tx+ty*TWIDTH, value);
		}
		
		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			shared.data.set(tile, doJob.set(shared.data.get(tile), value));
			return this;
		}
	};
	
	public final MAP_INTE resource = new MAP_INTE() {
		
		@Override
		public int get(int tx, int ty) {
			return CLAMP.i(bfruit.get(shared.data.get(tx, ty)), 0, size.get(tx, ty));
		}
		
		@Override
		public int get(int tile) {
			return CLAMP.i(bfruit.get(shared.data.get(tile)), 0, size.get(tile));
		}
		
		@Override
		public MAP_INTE set(int tx, int ty, int value) {
			return set(tx+ty*TWIDTH, value);
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			
			value = CLAMP.i(value, 0, 0b01111);
			
			int data = shared.data.get(tile);
			
			if (!TGrowable.this.is(tile)) {
				size.set(tile, value);
				shared.data.set(tile, bfruit.set(data, value));
				return this;
			}
			
			value = CLAMP.i(value, 0, size.get(tile));
			int old = bfruit.get(data);
			shared.data.set(tile, bfruit.set(data, value));
			if (old == 0 && value > 0 && doJob.is(data) && SETT.JOBS().getter.get(tile) == null) {
				boolean b = SETT.JOBS().planMode.is();
				SETT.JOBS().planMode.set(false);
				SETT.JOBS().clearss.food.placer().place(tile%TWIDTH, tile/TWIDTH, null, null);
				SETT.JOBS().planMode.set(b);
			}
			return this;
		}
	};
	
	static LIST<TGrowable> make(Terrain t){
		ArrayList<TGrowable> all = new ArrayList<>(RESOURCES.growable().all().size());
		for (Growable g : RESOURCES.growable().all()) {
			all.add(new TGrowable(t, g));
		}
		IDebugPanelSett.add(new PlacableMulti("TGrowable increase size") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (TERRAIN().get(tx, ty) instanceof TGrowable) {
					TGrowable g = (TGrowable) TERRAIN().get(tx, ty);
					g.size.increment(tx, ty, 1);
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (TERRAIN().get(tx, ty) instanceof TGrowable) {
					return null;
				}
				return E;
			}
		});
		
		IDebugPanelSett.add(new PlacableMulti("TGrowable decrease size") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (TERRAIN().get(tx, ty) instanceof TGrowable) {
					TGrowable g = (TGrowable) TERRAIN().get(tx, ty);
					g.size.increment(tx, ty, -1);
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (TERRAIN().get(tx, ty) instanceof TGrowable) {
					return null;
				}
				return E;
			}
		});
		
		IDebugPanelSett.add(new PlacableMulti("TGrowable increase res") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (TERRAIN().get(tx, ty) instanceof TGrowable) {
					TGrowable g = (TGrowable) TERRAIN().get(tx, ty);
					g.resource.increment(tx, ty, 1);
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (TERRAIN().get(tx, ty) instanceof TGrowable) {
					return null;
				}
				return E;
			}
		});
		
		IDebugPanelSett.add(new PlacableMulti("TGrowable decrease res") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (TERRAIN().get(tx, ty) instanceof TGrowable) {
					TGrowable g = (TGrowable) TERRAIN().get(tx, ty);
					g.resource.increment(tx, ty, -1);
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (TERRAIN().get(tx, ty) instanceof TGrowable) {
					return null;
				}
				return E;
			}
		});
		
		return all;
	}
	
	private TGrowable(Terrain t, Growable g) {
		
		super("GROWABLE_" + g.resource.key, t, new Str(¤¤Name).insert(0, g.resource.name), g.resource.icon(), t.colors.minimap.growable);
		this.growable = g;
		this.gIndex = g.index();
	}

	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	@Override
	protected boolean place(int tx, int ty) {
		if (!is(tx, ty)) {
			super.placeRaw(tx, ty);
			size.set(tx, ty, 1);
		}
		return false;
	}
	
	public boolean isEdible(int tx, int ty) {
		return RESOURCES.EDI().get(growable.resource) != null && SETT.WEATHER().growthRipe.cropsAreRipe();
	}
	
	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		return false;
	}
	
	@Override
	protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		i.countVegetation();
		growable.sprite.render(r, s, i, size.DM.get(i.tile()), bfruit.get(data)*sizeI);
		
//		SETT.TERRAIN().colors.tree.get(i.ran()).bind();
//		growable.render(r, s, i, bamount.get(data), isRipe());
//		COLOR.unbind();
		return false;

	}

	@Override
	public AVAILABILITY getAvailability(int x, int y) {
		return AVAILABILITY.PENALTY2;
	}
	
	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}
	
	@Override
	public void hoverInfo(GBox box, int tx, int ty) {
		
		box.add(growable.resource.icon());
		super.hoverInfo(box, tx, ty);
		box.tab(6);
		box.add(GFORMAT.iofkInv(box.text(), resource.get(tx, ty), size.get(tx, ty)));
		box.text(SETT.WEATHER().growthRipe.cropsAreRipe() ? ¤¤Ripe : ¤¤RipeNot);
			
			
		box.NL();
	}
	
	@Override
	public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
		COLOR col = SETT.GROUND().minimap.miniC(x, y);
		c.interpolate(col, miniC, 0.25+0.75*size.DM.get(x, y));
		return c;
	}

}
