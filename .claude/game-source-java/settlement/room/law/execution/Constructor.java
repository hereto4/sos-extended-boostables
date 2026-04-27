package settlement.room.law.execution;

import java.io.IOException;

import game.GAME;
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
import settlement.room.sprite.RoomSpriteBoxN;
import settlement.room.sprite.RoomSpriteSimple;
import settlement.tilemap.floor.Floors.Floor;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

final class Constructor extends Furnisher{

	private final ROOM_EXECTUTION blue;
	private final Floor floor2;
	
	final FurnisherStat prisoners = new FurnisherStat.FurnisherStatI(this, 1);
	final FurnisherStat workers = new FurnisherStat.FurnisherStatI(this);
	final FurnisherStat fear = new FurnisherStat.FurnisherStatRelative(this, workers);
	final static int codeHang = 1;
	final static int codeChopp = 2;
	final static int codeChoppTurn = 3;

	private final RoomSprite sCandle;
	
	private final RoomSprite spritePedistal;
	private final RoomSprite spriteCandle;
	private final TILE_SHEET sheet;
	
	protected Constructor(ROOM_EXECTUTION blue, RoomInitData init)
			throws IOException {
		super(init, 3, 3, 276, 104);
		this.blue = blue;
		sheet = new ITileSheet(init.sp(), 276, 84) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d)  {
				s.singles.init(0, 0, 1, 1, 6, 1, d.s16);
				s.singles.setSkip(0, 1).paste(3, true);
				s.singles.setSkip(1, 1).paste(3, true);
				s.singles.setSkip(2, 1).paste(3, true);
				s.singles.setSkip(3, 1).paste(3, true);
				s.singles.setSkip(4, 1).paste(3, true);
				s.singles.setSkip(5, 1).paste(3, true);
				
				s.singles.init(s.singles.body().x1(), s.singles.body().y2(), 1, 1, 6, 1, d.s16);
				s.singles.setSkip(0, 1).paste(3, true);
				s.singles.setSkip(1, 1).paste(3, true);
				s.singles.setSkip(2, 1).paste(3, true);
				s.singles.setSkip(3, 1).paste(3, true);
				s.singles.setSkip(4, 1).paste(3, true);
				s.singles.setSkip(5, 1).paste(3, true);
				
				s.singles.init(s.singles.body().x1(), s.singles.body().y2(), 1, 1, 6, 1, d.s16);
				s.singles.setSkip(0, 1).paste(3, true);
				s.singles.setSkip(1, 1).paste(3, true);
				s.singles.setSkip(2, 1).paste(3, true);
				s.singles.setSkip(3, 1).paste(3, true);
				s.singles.setSkip(4, 1).paste(3, true);
				s.singles.setSkip(5, 1).paste(3, true);
				
				return d.s16.saveGame();
			}
		}.get();
		
		floor2 = SETT.FLOOR().map.read("FLOOR2", init.data());
		
		Json sp = init.data().json("SPRITES");
		
		sCandle = new RoomSprite1x1(sp, "TABLE_1X1");
		spritePedistal = new Pedistall(sp);
		spriteCandle = new Pedistal(sp, sCandle);
		gallows(sp);
		chop(sp);
		decor(sp);
		
	}
	
	private void gallows(Json sp) throws IOException {
		int ii = 0;
		
		final int top_edge = ii;
		final int top_centre = ii+=4;
		final int bottom_edge = ii+=4;
		final int pillar = ii+=4;
		final int box = ii+=4;
		final int noose = ii+=4;
		
		RoomSprite spriteEdgeA = new Pedistall(sp) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				int rot = SETT.ROOMS().fData.item.get(it.tile()).rotation;
				int t = top_edge + rot;
				s.setHeight(0).setDistance2Ground(8);
				sheet.render(r, t, it.x(), it.y());
				sheet.render(s, t, it.x(), it.y());
				
				s.setHeight(8).setDistance2Ground(0);
				t = pillar+rot;
				sheet.render(s, t, it.x(), it.y());
				
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				boolean ret = super.render(r, s, data, it, degrade, isCandle);
				int rot = SETT.ROOMS().fData.item.get(it.tile()).rotation;
				int t = bottom_edge + rot;
				s.setHeight(2).setDistance2Ground(0);
				sheet.render(r, t, it.x(), it.y());
				sheet.render(s, t, it.x(), it.y());
				return ret;
			}
		};
		RoomSprite spriteEdgeB = new Pedistall(sp) {
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				int rot = (SETT.ROOMS().fData.item.get(it.tile()).rotation + 2)&0b11;
				int t = top_edge + rot;
				s.setHeight(0).setDistance2Ground(8);
				sheet.render(r, t, it.x(), it.y());
				sheet.render(s, t, it.x(), it.y());
				
				s.setHeight(8).setDistance2Ground(0);
				t = pillar+rot;
				sheet.render(s, t, it.x(), it.y());
				
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				boolean ret = super.render(r, s, data, it, degrade, isCandle);
				int rot = (SETT.ROOMS().fData.item.get(it.tile()).rotation + 2)&0b11;
				int t = bottom_edge + rot;
				s.setHeight(2).setDistance2Ground(0);
				sheet.render(r, t, it.x(), it.y());
				sheet.render(s, t, it.x(), it.y());
				return ret;
			}
		};
		RoomSprite spriteC = new Pedistall(sp) {
			final int[] animi = new int[] {0,1,2,3,4,5,6,5,4,3,2,1,0,-1,-2,-3,-4,-5,-6,-7,-8,-7,-6,-5,-4,-3,-2,-1};
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				s.setHeight(0).setDistance2Ground(8);
				int rot = SETT.ROOMS().fData.item.get(it.tile()).rotation;
				
				if (ExecutionStation.hasBox(SETT.ROOMS().data.get(it.tile()))) {
					DIR dir = DIR.ORTHO.get(rot);
					int d = animi[(it.ran()&0x0FFFF+GAME.intervals().get08())%animi.length];
					sheet.render(r, noose+rot, it.x()+dir.x()*d, it.y()+dir.y()*d);
					sheet.render(s, noose+rot, it.x()+dir.x()*d, it.y()+dir.y()*d);
				}
				
				
				int t = top_centre+rot;
				sheet.render(r, t, it.x(), it.y());
				sheet.render(s, t, it.x(), it.y());
				
				
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				boolean ret = super.render(r, s, data, it, degrade, isCandle);
				if (ExecutionStation.hasBox(SETT.ROOMS().data.get(it.tile()))) {
					s.setHeight(3).setDistance2Ground(0);
					sheet.render(r, box + (it.ran()&0b11), it.x(), it.y());
					sheet.render(s, box, it.x(), it.y());
				}
				return ret;
			}
		};
		
		final FurnisherItemTile xx = new FurnisherItemTile(
				this,
				spritePedistal, 
				AVAILABILITY.ROOM, false);
		final FurnisherItemTile ca = new FurnisherItemTile(
				this,
				spriteCandle, 
				AVAILABILITY.ROOM_SOLID, true);
		final FurnisherItemTile aa = new FurnisherItemTile(
				this,
				spriteEdgeA, 
				AVAILABILITY.PENALTY4, false);
		final FurnisherItemTile cc = new FurnisherItemTile(
				this,
				spriteC, 
				AVAILABILITY.PENALTY4, false).setData(codeHang);
		final FurnisherItemTile bb = new FurnisherItemTile(
				this,
				spriteEdgeB, 
				AVAILABILITY.PENALTY4, false);

		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,xx,xx,xx,xx,xx}, 
			{xx,ca,aa,cc,bb,xx},
			{xx,xx,xx,xx,xx,xx},
		}, 6, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,xx,xx,xx,xx,xx,xx}, 
			{xx,ca,aa,cc,cc,bb,xx},
			{xx,xx,xx,xx,xx,xx,xx},
		}, 7, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,xx,xx,xx,xx,xx,xx,xx}, 
			{xx,ca,aa,cc,cc,cc,bb,xx},
			{xx,xx,xx,xx,xx,xx,xx,xx},
		}, 8, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,xx,xx,xx,xx,xx,xx,xx,xx}, 
			{xx,ca,aa,cc,cc,cc,cc,bb,xx},
			{xx,xx,xx,xx,xx,xx,xx,xx,xx},
		}, 8, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,xx,xx,xx,xx,xx,xx,xx,xx,xx}, 
			{xx,ca,aa,cc,cc,cc,cc,cc,bb,xx},
			{xx,xx,xx,xx,xx,xx,xx,xx,xx,xx},
		}, 8, 5);
		
		flush(1);
		
	}
	
	private void chop(Json sp) throws IOException {
		
		int ii = 6*4;
		
		final int chop = ii;
		final int kneel = ii+=4;
		final int blood = ii+=4;

		
		RoomSprite spriteChop = new Pedistall(sp) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				
				
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				boolean ret = super.render(r, s, data, it, degrade, isCandle);
				int rot = SETT.ROOMS().fData.item.get(it.tile()).rotation;
				int t = chop + rot;
				s.setHeight(2).setDistance2Ground(0);
				sheet.render(r, t, it.x(), it.y());
				sheet.render(s, t, it.x(), it.y());
				OPACITY.O99.bind();
				sheet.renderTextured(sheet.getTexture(blood+(it.ran()&0x0F)), t, it.x(), it.y());
				OPACITY.unbind();
				return ret;
			}
		};
		
		RoomSprite spriteKneel = new Pedistall(sp) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				
				
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				boolean ret = super.render(r, s, data, it, degrade, isCandle);
				int rot = SETT.ROOMS().fData.item.get(it.tile()).rotation;
				int t = kneel + rot;
				s.setHeight(2).setDistance2Ground(0);
				sheet.render(r, t, it.x(), it.y());
				sheet.render(s, t, it.x(), it.y());
				OPACITY.O99.bind();
				sheet.renderTextured(sheet.getTexture(blood+(it.ran()&0x0F)), t, it.x(), it.y());
				OPACITY.unbind();
				
				return ret;
			}
		};
		
		final FurnisherItemTile ee = new FurnisherItemTile(
				this,
				true,
				spritePedistal, 
				AVAILABILITY.ROOM, false);
		final FurnisherItemTile xx = new FurnisherItemTile(
				this,
				spritePedistal, 
				AVAILABILITY.ROOM, false);
		final FurnisherItemTile ca = new FurnisherItemTile(
				this,
				spriteCandle, 
				AVAILABILITY.ROOM_SOLID, true);
		final FurnisherItemTile mm = new FurnisherItemTile(
				this,
				spriteKneel, 
				AVAILABILITY.AVOID_PASS, false).setData(codeChoppTurn);
		final FurnisherItemTile cc = new FurnisherItemTile(
				this,
				spriteChop, 
				AVAILABILITY.AVOID_PASS, false).setData(codeChopp);
		final FurnisherItemTile hh = new FurnisherItemTile(
				this,
				spritePedistal, 
				AVAILABILITY.ROOM_SOLID, false);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,ca,mm,xx,},
			{xx,xx,cc,xx,},
			{xx,ee,hh,ee,},
		}, 6, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,ca,mm,mm,xx,},
			{xx,xx,cc,cc,xx,},
			{xx,ee,hh,hh,ee,},
		}, 7, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,ca,mm,mm,xx,mm,xx},
			{xx,xx,cc,cc,xx,cc,xx},
			{xx,ee,hh,hh,ee,hh,ee},
		}, 9, 3);
		new FurnisherItem(new FurnisherItemTile[][] {
			{xx,ca,mm,mm,xx,mm,mm,xx},
			{xx,xx,cc,cc,xx,cc,cc,xx},
			{xx,ee,hh,hh,ee,hh,hh,ee},
		}, 10, 4);
		
		flush(1);
	}
	
	private void decor(Json sp) {
		

		
		RoomSprite sprite = new RoomSpriteSimple(sheet, 6*4*2, 4) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				s.setDistance2Ground(0).setHeight(14);
				sheet.render(r, tileEnd, it.x(), it.y());
				sheet.render(s, tileEnd, it.x(), it.y());
				if ((it.ran() & 1) == 1) {
					s.setDistance2Ground(14).setHeight(0);
					int ra = it.ran()>>1;
					ra &= 0x0F;
					sheet.render(r, tileEnd+4+ra, it.x(), it.y());
					sheet.render(s, tileEnd+4+ra, it.x(), it.y());
				}
				
			};
			
		};
		
		final FurnisherItemTile xx = new FurnisherItemTile(
				this,
				sprite, 
				AVAILABILITY.ROOM, false);
		final FurnisherItemTile ca = new FurnisherItemTile(
				this,
				sCandle,
				AVAILABILITY.ROOM_SOLID, true);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ca,xx,ca}, 
		}, 3, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ca,xx,xx,ca}, 
		}, 4, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ca,xx,xx,xx,ca}, 
		}, 5, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ca,xx,xx,xx,xx,ca}, 
		}, 6, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ca,xx,xx,xx,xx,xx, ca}, 
		}, 7, 5);
		
		flush(1);
		
	}

	boolean isWithinCell(int nx, int ny, int cx, int cy) {
		
		if (SETT.ROOMS().fData.item.get(nx, ny) != null && SETT.ROOMS().fData.item.get(cx, cy) != null) {
			COORDINATE c = SETT.ROOMS().fData.itemX1Y1(nx, ny, Coo.TMP);
			nx = c.x();
			ny = c.y();
			return SETT.ROOMS().fData.itemX1Y1(cx, cy, Coo.TMP).isSameAs(nx, ny);
		}
		return false;
		
		
	}

	@Override
	public boolean usesArea() {
		return true;
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
		return new ExecutionInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			if (!area.is(tx, ty, DIR.ORTHO.get(di))) {
				super.putFloor(tx, ty, upgrade, area);
				return;
			}
		}
		floor2.placeFixed(tx, ty);
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,1,1,0,0,0},
//		{0,0,0,1,1,0,0,0},
//		{0,0,1,0,0,1,0,0},
//		{0,1,0,0,0,0,1,0},
//		{0,1,0,0,0,0,1,0},
//		{0,0,1,0,0,1,0,0},
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
	
	private static class Pedistall extends RoomSpriteBoxN {
		

		
		public Pedistall(Json json) throws IOException {
			super(json, "PODIUM_BOX");
		}

		@Override
		public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
			super.render(r, s, data, it, degrade, false);
		}
		
		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			return false;
		}

		
	}
	
	private static class Pedistal extends RoomSpriteBoxN {
		
		private final RoomSprite top;
		
		public Pedistal(Json json, RoomSprite top) throws IOException {
			super(json, "PODIUM_BOX");
			this.top = top;
		}

		@Override
		public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
			super.render(r, s, data, it, degrade, false);
			top.renderBelow(r, s, data, it, degrade);
		}
		
		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			if (!isCandle) {
				top.render(r, s, getData2(it), it, degrade, false);
			}
			return false;
		}
		
		@Override
		public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
			top.renderAbove(r, s, data, it, degrade);
		}
		
		@Override
		public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			return top.getData(tx, ty, rx, ry, item, itemRan);
		}
		
	}

}
