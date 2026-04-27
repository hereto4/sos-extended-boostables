package settlement.room.food.cannibal;

import java.io.IOException;

import init.constant.C;
import init.race.RACES;
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
import settlement.room.sprite.RoomSprite1x1;
import settlement.room.sprite.RoomSpriteCombo;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	private final ROOM_CANNIBAL blue;
	
	final FurnisherStat workers = new FurnisherStat(this) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return fromItems;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};
	final FurnisherStat efficiency = new FurnisherStat.FurnisherStatEfficiency(this, workers);
	
	final FurnisherItemTile ww;
	final FurnisherItemTile rm;
	final FurnisherItemTile rr;
	
	protected Constructor(ROOM_CANNIBAL blue, RoomInitData init)
			throws IOException {
		super(init, 2, 2, 88, 44);
		this.blue = blue;
		
		Json sp = init.data().json("SPRITES");
		
		RoomSprite table = new RoomSpriteCombo(sp, "TABLE_COMBO") {
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				super.render(r, s, data, it, degrade, isCandle);
				if (blue.is(it.tile())) {
					RACES.all().get(Job.race.get(SETT.ROOMS().data.get(it.tile()))).appearance().colors.blood.bind();
					long ran = it.bigRan();
					int a = Job.gore.get(SETT.ROOMS().data.get(it.tile()));
					int cx = it.x()+C.TILE_SIZEH;
					int cy = it.y()+C.TILE_SIZEH;
					for (int i = 0; i < a; i++) {
						int xx = (int) (cx + (-4 + (ran&0x07))*C.SCALE);
						ran = ran >> 3;
						int yy = (int) (cy + (-4 + (ran&0x07))*C.SCALE);
						ran = ran >> 3;
						SETT.THINGS().sprites.bloodPool.render(r, (int) (ran&0x0F), xx, yy);
						ran = ran >> 4;
					}
					COLOR.unbind();
				}
				return false;
			}
		};
		
		final RoomSprite table2 = new RoomSpriteCombo(table) {
			
			final RoomSprite1x1 top = new RoomSprite1x1(sp, "ON_TABLE_1X1") {
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return item.get(rx, ry) == null;
				}
			};
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				top.renderRandom(r, s, it, data, degrade);
			}
			
		};
		
		final RoomSprite misc = new RoomSprite1x1(sp, "MISC_1X1");
		
		ww = new FurnisherItemTile(this, true,table, AVAILABILITY.ROOM_SOLID, false);
		rm = new FurnisherItemTile(this, true,table, AVAILABILITY.ROOM_SOLID, false);
		rr = new FurnisherItemTile(this, true,table, AVAILABILITY.ROOM_SOLID, false);
		final FurnisherItemTile mm = new FurnisherItemTile(this, false,table2, AVAILABILITY.ROOM_SOLID, true);
		final FurnisherItemTile nn = new FurnisherItemTile(this, false,misc, AVAILABILITY.ROOM_SOLID, false);
		
		
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{rm,ww,rr,mm},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{rm,ww,rr,mm,rm,ww,rr,mm},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{rm,ww,rr,mm,rm,ww,rr,mm},
			{rm,ww,rr,mm,rm,ww,rr,mm},
		}, 4);
		
		flush(1, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,nn},
		}, 3);
		
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,mm,nn},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,mm,nn},
			{nn,mm,mm,nn},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,mm,mm,nn},
			{nn,mm,mm,mm,nn},
		}, 10);
		
		flush(1);
	}

	@Override
	public boolean usesArea() {
		return true;
	}

	@Override
	public boolean mustBeIndoors() {
		return true;
	}

	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new CannibalInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
	
	@Override
	public boolean isHeavy() {
		return true;
	}
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,1,1,1,0,0,0,0},
//		{0,0,1,0,1,0,0,0},
//		{0,0,1,0,1,0,0,0},
//		{0,0,1,0,1,0,0,0},
//		{0,0,1,0,1,0,0,0},
//		{0,1,1,1,0,0,0,0},
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
