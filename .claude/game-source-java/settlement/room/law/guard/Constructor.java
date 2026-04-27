package settlement.room.law.guard;

import java.io.IOException;

import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.TmpArea;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.furnisher.FurnisherItemTile;
import settlement.room.main.furnisher.FurnisherStat;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSprite;
import settlement.room.sprite.RoomSpriteBoxN;
import settlement.room.sprite.RoomSpriteCombo;
import settlement.tilemap.terrain.TFortification;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.tool.PlacableMessages;

final class Constructor extends Furnisher{

	private final ROOM_GUARD blue;
	static final int codeLight = 3;
	static final int codeStand = 4;
	private final FurnisherItemTile xx;
	
	final FurnisherStat guards = new FurnisherStat(this) {
		
		@Override
		public double get(AREA area, double acc) {
			return Math.ceil(acc);
		}
		
		@Override
		public GText format(GText t, double value) {
			GFORMAT.i(t, (int) (value));
			return t;
		}
	};
	
	final FurnisherStat radius = new FurnisherStat(this) {
		
		@Override
		public double get(AREA area, double acc) {
			return CLAMP.d(Math.ceil((0.25 + 0.75*acc/2.0)*ROOM_GUARD.maxRadius), 0, ROOM_GUARD.maxRadius);
		}
		
		@Override
		public GText format(GText t, double value) {
			GFORMAT.i(t, (int) ((0.25 + 0.75*value/2.0)*ROOM_GUARD.maxRadius));
			return t;
		}
	};
	
	protected Constructor(ROOM_GUARD blue, RoomInitData init)
			throws IOException {
		super(init, 1, 2, 88, 44);
		this.blue = blue;
		
		Json js = init.data().json("SPRITES");
		
		RoomSprite sfloor = new RoomSpriteBoxN(js, "FLOOR_BOX") {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return false;
			}
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			}
		};
		
		RoomSprite sbraiser = new RoomSpriteBoxN(sfloor) {
			
			RoomSprite top = new RoomSpriteCombo(js, "TORCH_COMBO") {
				
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return item.sprite(rx, ry) != null && item.sprite(rx, ry).sData() == 2;
				}
				
			};
			RoomSprite bottom = new RoomSpriteCombo(js, "TORCH_BOTTOM_COMBO") {
				
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return item.sprite(rx, ry) != null && item.sprite(rx, ry).sData() == 2;
				}
				
			};
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return top.render(r, s, getData2(it), it, degrade, false);
			}
			
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
				bottom.render(r, s, getData2(it), it, degrade, false);
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return top.getData(tx, ty, rx, ry, item, itemRan);
			}

			
		}.sData(2);
		

		RoomSprite sFence = new RoomSpriteBoxN(sfloor) {
			
			RoomSprite top = new RoomSpriteCombo(js, "FENCE_COMBO") {
				
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return item.sprite(rx, ry) != null && item.sprite(rx, ry).sData() == 1;
				}
				
			};
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return top.render(r, s, getData2(it), it, degrade, false);
			}
			
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return top.getData(tx, ty, rx, ry, item, itemRan);
			}
			
		}.sData(1);
				
		
		xx = new FurnisherItemTile(
				this,
				sFence,
				AVAILABILITY.SOLID, 
				false);
		
		FurnisherItemTile __ = new FurnisherItemTile(
				this,
				sfloor,
				AVAILABILITY.ROOM, 
				false).setData(codeStand);
		
		FurnisherItemTile i1 = new FurnisherItemTile(
				this,
				sbraiser,
				AVAILABILITY.SOLID, 
				false).setData(codeLight);
		
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,i1,xx}, 
			{xx,__,xx},
			{__,__,__},
			{xx,__,xx,},
		}, 0.5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,xx,__,xx,xx}, 
			{xx,__,__,__,xx},
			{__,__,i1,__,__},
			{xx,__,__,__,xx},
			{xx,xx,__,xx,xx},
		}, 0.75);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,xx,xx,__,__,xx,xx,xx}, 
			{xx,__,__,__,__,__,__,xx},
			{xx,__,__,__,__,__,__,xx},
			{__,__,__,i1,i1,__,__,__},
			{__,__,__,i1,i1,__,__,__},
			{xx,__,__,__,__,__,__,xx},
			{xx,__,__,__,__,__,__,xx},
			{xx,xx,xx,__,__,xx,xx,xx},
		}, 2);
		
		flush(1, 3);
	}
	
	@Override
	public void renderExtra(SPRITE_RENDERER r, int x, int y, int tx, int ty, int rx, int ry, FurnisherItem item) {
		if (rx == item.width()/2 && ry == item.height()/2) {
			SETT.OVERLAY().RadiusInter(blue, blue.finder, tx, ty, radius.get(null, item.stat(radius)));
		}
	}
	
	@Override
	public boolean removeTerrain(int tx, int ty) {
		if (SETT.TERRAIN().get(tx, ty) instanceof TFortification.Normal && SETT.PATH().availability.get(tx, ty).player >= 0)
			return false;
		return super.removeTerrain(tx, ty);
	}
	
	@Override
	public CharSequence placable(int tx, int ty, FurnisherItem item, FurnisherItemTile tile) {
		if (SETT.TERRAIN().get(tx, ty) instanceof TFortification.Normal && SETT.PATH().availability.get(tx, ty).player < 0)
			return PlacableMessages.¤¤STRUCTURE_BLOCK;
		return null;
	}
	

	@Override
	public boolean usesArea() {
		return false;
	}

	@Override
	public boolean mustBeIndoors() {
		return false;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return false;
	}

	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new GuardInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		super.putFloor(tx, ty, upgrade, area);
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,1,0,0,0,0,1,0},
//		{0,1,1,1,1,1,1,0},
//		{0,1,1,1,1,1,1,0},
//		{0,1,1,1,1,1,1,0},
//		{0,0,1,1,1,1,0,0},
//		{0,0,0,1,1,0,0,0},
//		{0,0,0,0,0,0,0,0},
//		},
//		miniColor
//	);
//	
//	@Override
//	public COLOR miniColor(int tx, int ty) {
//		return miniC.get(tx, ty);
//	}
	

}
