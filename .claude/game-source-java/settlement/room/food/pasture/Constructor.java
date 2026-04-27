package settlement.room.food.pasture;

import static settlement.main.SETT.FLOOR;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TERRAIN;

import java.io.IOException;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.overlay.Addable;
import settlement.path.AVAILABILITY;
import settlement.room.industry.module.IndustryResource;
import settlement.room.main.ROOMA;
import settlement.room.main.Room;
import settlement.room.main.TmpArea;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.furnisher.FurnisherItemTile;
import settlement.room.main.furnisher.FurnisherStat;
import settlement.room.main.placement.UtilWallPlacability;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSprite;
import settlement.room.sprite.RoomSprite1x1;
import settlement.room.sprite.RoomSprite1xN;
import settlement.room.sprite.RoomSpriteCombo;
import settlement.room.sprite.RoomSpriteImp;
import settlement.room.sprite.RoomSpriteXxX;
import settlement.tilemap.terrain.TerrainDiagonal.Diagonalizer;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.CLAMP;
import util.GUTIL;
import util.colors.GCOLOR;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import util.text.Dic;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	private static CharSequence ¤¤Problem = "Must be facing the edge of the room.";
	private static CharSequence ¤¤Problem2 = "Must not be placed in a corner.";
	private static CharSequence ¤¤Problem3 = "Will be blocked by walls";
	static {
		D.ts(Constructor.class);
	}
	
	private ROOM_PASTURE blue;
	public final boolean isIndoors;
	final FurnisherStat workers = new FurnisherStat(this, 1) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return ROOM_PASTURE.WORKERS_PER_TILE*ferarea.get(area, fromItems);
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.f(t, value, 1);
		}
	};
	
	final FurnisherStat ferarea;
	
	final FurnisherStat efficiency;
	
	final FurnisherStat irri;
	
	private final FurnisherItemTile gc;
	final FurnisherItemTile s1;
	final FurnisherItemTile s2;
	final FurnisherItemTile s3;
	
	private final RoomSpriteCombo fence;
	private final RoomSpriteCombo fenceDia;
	
	protected Constructor(ROOM_PASTURE blue, RoomInitData init)
			throws IOException {
		super(init, 2, 4, 88, 44);
		this.blue = blue;
		isIndoors = init.data().bool("INDOORS", false);
		irri = new FurnisherStat.FurnisherStatIrrigation(this, blue);
		efficiency = new FurnisherStat.FurnisherStatEfficiency(this, workers);
		
		ferarea = new FurnisherStat(this, 1) {
			
			@Override
			public double get(AREA area, double fromItems) {
				
				double f = 0;
				outer: for (COORDINATE c : area.body()) {
					if (!area.is(c))
						continue;
					for (DIR d : DIR.ALL) {
						if (!area.is(c, d)) {
							continue outer;
						}
					}
					f += fertility(c.x(), c.y());
				}
				return f;
			}
			
			@Override
			public GText format(GText t, double value) {
				double am = 0;
				
				for (IndustryResource o : blue.industries().get(0).outs())
					am += o.rate;
				//am *= blue.bonus().get(POP_CL.clP(null, HCLASSES.CITIZEN()));
				return GFORMAT.f(t, (ROOM_PASTURE.WORKERS_PER_TILE*value*am), 1);
			}
		};
		
		Json js = init.data().json("SPRITES");
		
		RoomSprite sbelow = new RoomSprite1x1(js, "STORAGE_1X1");
		
		RoomSprite sprite = new RoomSpriteXxX(js, "GATE_TOP_3X3", 3) {
			@Override
			public void renderAbove(snake2d.SPRITE_RENDERER r, util.rendering.ShadowBatch s, int data, util.rendering.RenderData.RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			};
			@Override
			public boolean render(snake2d.SPRITE_RENDERER r, util.rendering.ShadowBatch s, int data, util.rendering.RenderData.RenderIterator it, double degrade, boolean isCandle) {
				
				if (SETT.ROOMS().fData.tile.get(it.tile()).data() == 1) {
					return sbelow.render(r, s, getData2(it), it, degrade, isCandle);
				}
				
				return false;
				
			};
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sbelow.getData(tx, ty, rx, ry, item, itemRan);
			}
		};
		
		fence = new RoomSpriteCombo(js, "FENCE_COMBO");
		fenceDia = new RoomSpriteCombo(js, "FENCE_D_COMBO");
		
		final RoomSpriteImp auxEdge = new RoomSprite1xN(js, "AUX_EDGE_1X1", false) {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return false;
			}
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, isIndoors);
			}
		};
		final RoomSpriteImp auxMid = new RoomSprite1xN(js, "AUX_MID_1X1", true) {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return false;
			}
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, isIndoors);
			}
		};
		final RoomSpriteXxX auxBig = new RoomSpriteXxX(js, "AUX_BIG_2X2", 2) {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return false;
			}
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, isIndoors);
			}
		};
		
		FurnisherItemTile gl = new FurnisherItemTile(this, sprite, AVAILABILITY.ROOM_SOLID, false).setData(5);
		gc = new FurnisherItemTile(this, sprite, AVAILABILITY.ROOM, false) {
			
			@Override
			public CharSequence isPlacable(int tx, int ty, MAP_BOOLEAN roomIs, FurnisherItem it, int rx, int ry) {
				if (SETT.ROOMS().placement.embryo.is(tx, ty)) {
					if (SETT.ROOMS().placement.placer.autoWalls.is()) {
						for (DIR d : DIR.ORTHO) {
							if (it.get(rx, ry, d) == null && UtilWallPlacability.wallCanBe.is(tx, ty, d) && SETT.ROOMS().placement.placer.placerDoor.isPlacable(tx+d.x(), ty+d.y(), null, null) == null) {
								return ¤¤Problem3;
							}
						}
					}
					
				}
				
				for (DIR d : DIR.ORTHO) {
					if (!roomIs.is(tx, ty, d))
						return null;
					
				}
				return ¤¤Problem;
				
//				for (DIR d : DIR.ORTHO) {
//					if (!roomIs.is(tx, ty, d) && !SETT.PATH().solidity.is(tx, ty, d) && SETT.ROOMS().placement.placer.placerDoor.isPlacable(tx+d.x(), ty+d.y(), null, null) == null)
//						return null;
//					
//				}
//				return ¤¤Problem;
			};
		}.setData(5);
		FurnisherItemTile du = new FurnisherItemTile(this, sprite, AVAILABILITY.ROOM, false) {
			@Override
			public CharSequence isPlacable(int tx, int ty, MAP_BOOLEAN roomIs, FurnisherItem it, int rx, int ry) {
				
				
				for (DIR d : DIR.ORTHO) {
					if (it.get(rx, ry, d) != null) {
						continue;
					}
					if (!p(tx+d.x(), ty+d.y(), roomIs))
						return ¤¤Problem2;
					
				}
				
				
				
				return super.isPlacable(tx, ty, roomIs, it, rx, ry);
				
			};
			
			private boolean p(int tx, int ty, MAP_BOOLEAN roomIs) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					if (!roomIs.is(tx, ty, DIR.ORTHO.get(i)))
						return false;
				}
				return true;
			}
		};
		s1 = new FurnisherItemTile(this, sprite, AVAILABILITY.ROOM_SOLID, false).setData(1);
		s2 = new FurnisherItemTile(this, sprite, AVAILABILITY.ROOM_SOLID, false).setData(1);
		s3 = new FurnisherItemTile(this, sprite, AVAILABILITY.ROOM_SOLID, false).setData(1);
		
		
		
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{gl,gc,gl},
			{du,du,du},
			{s1,s2,s3},
		}, 1, 1);
		
		flush(1, 1, 3);
		
		final FurnisherItemTile m1 = new FurnisherItemTile(this, false, auxEdge, AVAILABILITY.ROOM, false) {
			@Override
			public CharSequence isPlacable(int tx, int ty, MAP_BOOLEAN roomIs, FurnisherItem it, int rx, int ry) {
				for (int i = 0; i < DIR.ALL.size(); i++) {
					if (!roomIs.is(tx, ty, DIR.ALL.get(i)))
						return Dic.empty;
				}
				return super.isPlacable(tx, ty, roomIs, it, rx, ry);
				
			};
		};
		final FurnisherItemTile m2 = new FurnisherItemTile(this, false, auxMid, AVAILABILITY.ROOM, false) {
			@Override
			public CharSequence isPlacable(int tx, int ty, MAP_BOOLEAN roomIs, FurnisherItem it, int rx, int ry) {
				for (int i = 0; i < DIR.ALL.size(); i++) {
					if (!roomIs.is(tx, ty, DIR.ALL.get(i)))
						return Dic.empty;
				}
				return super.isPlacable(tx, ty, roomIs, it, rx, ry);
				
			};
		};
		final FurnisherItemTile ml = new FurnisherItemTile(this, false, auxBig, AVAILABILITY.ROOM, false) {
			@Override
			public CharSequence isPlacable(int tx, int ty, MAP_BOOLEAN roomIs, FurnisherItem it, int rx, int ry) {
				for (int i = 0; i < DIR.ALL.size(); i++) {
					if (!roomIs.is(tx, ty, DIR.ALL.get(i)))
						return Dic.empty;
				}
				return super.isPlacable(tx, ty, roomIs, it, rx, ry);
				
			};
		};
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{m1,m1},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{m1,m2,m1},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{m1,m2,m2,m1},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ml,ml},
			{ml,ml},
		}, 4);
		
		
		flush(3);
		
	}


	@Override
	public boolean usesArea() {
		return true;
	}

	@Override
	public boolean mustBeIndoors() {
		return isIndoors;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return !isIndoors;
	}

	@Override
	public ROOM_PASTURE blue() {
		return blue;
	}

	private double fertility(int tx, int ty) {
		if (mustBeIndoors()) {
			if (SETT.TERRAIN().MOUNTAIN.isMountain(tx, ty))
				return 1.0;
			return 0.5;
		}
		double f = (SETT.GROUND().MAP.get(tx, ty).farm-0.1);
		f *= f;
		return 0.4 + 0.6*f;
	}
	
	private final Addable overlay = new Addable(true, true) {
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			double d = fertility(it.tx(), it.ty());
			d*=d;
			renderUnder(d, r, it, false);
			if (!SETT.ROOMS().placement.embryo.is(it.tile()) && TERRAIN().get(it.tile()).clearing().can()) {
				double w = SETT.GROUND().MOISTURE_TOT.get(it.tile())*2.0;
				w = CLAMP.d(w, 0, 1);
				if (w > 0) {
					ColorImp.TMP.interpolate(GCOLOR.MAP().OVERLAY_BAD, GCOLOR.MAP().OVERLAY_GOOD, w).bind();;
					int s = (int) (C.TILE_SIZE/4 + w*3*C.TILE_SIZE/4);
					int x1 = it.x() + (C.TILE_SIZE-s)/2;
					int y1 = it.y() + (C.TILE_SIZE-s)/2;
					
					UI.icons().s.drop.render(r, x1, x1 +s, y1, y1+s);
				}
			}
		};
		
		private double fertility(int tx, int ty) {
			if (mustBeIndoors()) {
				if (SETT.TERRAIN().MOUNTAIN.isMountain(tx, ty))
					return 1.0;
				return 0.5;
			}
			double f = (SETT.GROUND().MAP.get(tx, ty).farm-0.1);
			f *= f;
			return 0.4 + 0.6*f;
		}
		
//		@Override
//		public boolean render(Renderer r, RenderIterator it) {
//			if (!SETT.ROOMS().placement.embryo.is(it.tile()) && TERRAIN().get(it.tile()).clearing().can()) {
//				double w = SETT.GROUND().MOISTURE_TOT.get(it.tile())*2.0;
//				w = CLAMP.d(w, 0, 1);
//				if (w > 0) {
//					ColorImp.TMP.interpolate(GCOLOR.MAP().OVERLAY_BAD, GCOLOR.MAP().OVERLAY_GOOD, w).bind();;
//					int s = (int) (C.TILE_SIZE/4 + w*3*C.TILE_SIZE/4);
//					int x1 = it.x() + (C.TILE_SIZE-s)/2;
//					int y1 = it.y() + (C.TILE_SIZE-s)/2;
//					
//					UI.icons().s.drop.render(r, x1, x1 +s, y1, y1+s);
//					return true;
//				}
//			}
//			return false;
//		};
	};
	
	@Override
	public Addable overlay() {
		if (!isIndoors)
			return overlay;
		return null;
	}
	
	@Override
	public boolean needsIsolation() {
		return isIndoors;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,1,0,0,0,0,1,0},
//		{1,1,1,1,1,1,1,1},
//		{0,1,0,0,0,0,1,0},
//		{1,1,1,1,1,1,1,1},
//		{0,1,0,0,0,0,1,0},
//		{0,1,0,0,0,0,1,0},
//		{0,0,0,0,0,0,0,0},
//		},
//		miniColor
//	);
//	
//	@Override
//	public COLOR miniColor(int tx, int ty) {
//		return miniC.get(tx, ty);
//	}
	
	private final ColorImp color = new ColorImp();
	
	@Override
	public void renderEmbryo(SPRITE_RENDERER r, int mask, RenderIterator it, boolean isFloored, AREA area, boolean active) {
		double f = SETT.GROUND().MAP.get(it.tile()).farm;
		COLOR col = CORE.renderer().colorGet();
		
		if (active) {
			color.interpolate(GCOLOR.MAP().SOSO, GCOLOR.MAP().BETTER, 0.75 + 0.25*f);
			color.bind();
		}
		Room room = SETT.ROOMS().map.get(it.tile());
		
		if (isFloored) {
			COLOR.unbind();
			renderFence(r, ShadowBatch.DUMMY, it, 0);
			return;
		}
		
		if (mask != 0x0F) {
			SPRITES.cons().BIG.filled.render(r, mask, it.x(), it.y());
			return;
		}
		for (DIR d : DIR.NORTHO) {
			if (!room.isSame(it.tx(), it.ty(), it.tx()+d.x(), it.ty()+d.y())) {
				SPRITES.cons().BIG.filled.render(r, 0x0F, it.x(), it.y());
				return;
			}
		}
		col.bind();
		super.renderEmbryo(r, mask, it, isFloored, area, active);
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		FurnisherItemTile t = SETT.ROOMS().fData.tile.get(tx, ty);
		if (t != null || isIndoors) {
			super.putFloor(tx, ty, upgrade, area);
			
		}else {
			FLOOR().clearer.clear(tx, ty);
		}
		
	}
	
	@Override
	public boolean removeFertility() {
		return isIndoors;
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new PastureInstance(blue, area, init);
	}
	
	private static CharSequence ¤¤TooThin = "¤Area is too thin at places. Expand the area to at least 3x3.";
	
	static {
		D.ts(Constructor.class);
	}
	
	@Override
	public CharSequence constructionProblem(AREA area) {
		for (COORDINATE c : area.body()){
			if (area.is(c)) {
				boolean ok = false;
				for (DIR d : DIR.ALL) {
					if (isFull(c, area, d)) {
						ok = true;
						break;
					}	
				}
				if (!ok) {
					GUTIL.filler().done();
					return ¤¤TooThin;
				}
				
				
			}
			
		}
		return null;
		
	}
	
	private boolean isFull(COORDINATE c, AREA a, DIR d) {
		int tx = c.x()+d.x();
		int ty = c.y()+d.y();
		if (!a.is(tx, ty))
			return false;
		for (int i = 0; i < DIR.ALL.size(); i++) {
			DIR dd = DIR.ALL.get(i);
			if (!a.is(tx, ty, dd))
				return false;
		}
		return true;
	}
	
	@Override
	public void renderTileBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, boolean floored) {
		if (floored)
			renderFence(r, s, it, 0);
	}
	
	public boolean fenceJoin(ROOMA ii, int tx, int ty) {
		if (!ii.is(tx, ty))
			return false;
		if (SETT.ROOMS().fData.tile.get(tx, ty) == gc)
			return false;
		
		for (int di = 0; di < DIR.ALL.size(); di++) {
			DIR d = DIR.ALL.get(di);
			if (!ii.is(tx, ty, d) && !SETT.TERRAIN().get(tx, ty, d).isMassiveWall())
				return true;
		}
		return false;
	}
	
	public boolean isFence(ROOMA ii, int tx, int ty) {
		if (!ii.is(tx, ty))
			return false;
		if (SETT.ROOMS().fData.tile.get(tx, ty) != null)
			return false;
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			if (!ii.is(tx, ty, DIR.ORTHO.get(di)))
				return true;
		}
		return false;
	}
	
	public void renderFence(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, double degrade) {
		ROOMA ii = SETT.ROOMS().map.rooma.get(it.tx(), it.ty());
		if (ii == null)
			return;
		if (!fenceJoin(ii, it.tx(), it.ty()))
			return;
		
		int m = 0;
		
		for (DIR d : DIR.ORTHO) {
			if (fenceJoin(ii, it.tx()+d.x(), it.ty()+d.y()) || SETT.TERRAIN().get(it.tx()+d.x(), it.ty()+d.y()).isMassiveWall())
				m|= d.mask();			
		}
		if (m != 0x0F) {
			if (SETT.ROOMS().fData.spriteData.get(it.tile()) == 1) {
				fenceDia.render(r, s, m, it, degrade, false);
			}else {
				fence.render(r, s, m, it, degrade, false);
			}
		}
		
		
		
		
	}

	private final Diagonalizer dia = new Diagonalizer() {
		
		@Override
		public void setDia(int tx, int ty, boolean dia) {
			SETT.ROOMS().fData.spriteData.set(tx, ty, dia ? 1 :0);
		}
		
		@Override
		public boolean getDia(int tx, int ty) {
			return SETT.ROOMS().fData.spriteData.get(tx, ty) == 1;
		}
	};
	
	@Override
	public Diagonalizer dia(int tx, int ty) {
		if (blue.is(tx, ty) && fenceJoin(blue.get(tx, ty), tx, ty))
			return dia;
		return null;
	}
	
	@Override
	public boolean growsGrass(int tx, int ty) {
		return ROOMS().fData.item.get(tx, ty) == null;
	}
}
