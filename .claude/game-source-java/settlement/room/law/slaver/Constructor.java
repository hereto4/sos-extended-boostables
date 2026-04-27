package settlement.room.law.slaver;

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
import settlement.room.sprite.RoomSprite1x1;
import settlement.room.sprite.RoomSpriteCombo;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	private final ROOM_SLAVER blue;
	
	final FurnisherStat prisoners = new FurnisherStat.FurnisherStatI(this, 1);
	final static int codeService = 1;
	final static int codeHead = 2;

	
	private final RoomSprite table;
	
	
	protected Constructor(ROOM_SLAVER blue, RoomInitData init)
			throws IOException {
		super(init, 1, 1, 188, 94);
		this.blue = blue;
		
		Json sp = init.data().json("SPRITES");
	
		final RoomSprite benchA = new RoomSprite1x1(sp, "BENCH_1X1") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) == table;
			}
		};
		
		final RoomSprite benchB = new RoomSprite1x1(benchA) {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return DIR.ORTHO.get(item.rotation).perpendicular() == d;
			}
			
		};

		
		table = new RoomSpriteCombo(sp, "TABLE_COMBO") {
			
			final RoomSprite1x1 top = new RoomSprite1x1(sp, "NICKNACK_1X1");
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (!SETT.ROOMS().fData.candle.is(it.tile())) {
					top.render(r, s, data, it, degrade, false);
				}
			}
			
		};
		
		FurnisherItemTile tt = new FurnisherItemTile(this, table, AVAILABILITY.ROOM_SOLID, true);
		FurnisherItemTile ss = new FurnisherItemTile(this, true, benchB, AVAILABILITY.PENALTY4, false).setData(codeService);
		FurnisherItemTile sh = new FurnisherItemTile(this, benchA, AVAILABILITY.PENALTY4, false).setData(codeHead);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,}, 
			{sh,},
			{ss,},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt}, 
			{sh,sh,},
			{ss,ss,},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,}, 
			{sh,sh,sh,},
			{ss,ss,ss,},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,tt,}, 
			{sh,sh,sh,sh,},
			{ss,ss,ss,ss,},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,tt,tt,}, 
			{sh,sh,sh,sh,sh,},
			{ss,ss,ss,ss,ss,},
		}, 5);
		
		flush(3);
		
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
	public boolean mustBeOutdoors() {
		return false;
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new ExecutionInstance(blue, area, init);
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
//		{0,0,0,0,0,0,0,0},
//		{0,1,1,0,0,1,1,0},
//		{1,0,0,1,1,0,0,1},
//		{1,0,0,1,1,0,0,1},
//		{0,1,1,0,0,1,1,0},
//		{0,0,0,0,0,0,0,0},
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
