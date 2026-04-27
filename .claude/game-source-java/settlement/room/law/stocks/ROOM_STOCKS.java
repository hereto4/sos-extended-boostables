package settlement.room.law.stocks;

import static settlement.main.SETT.ROOMS;

import java.io.IOException;

import game.GAME;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.path.finders.SFinderRoomService;
import settlement.room.law.PUNISHMENT_SERVICE;
import settlement.room.law.stocks.Tile.STATE;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.ROOM_ACTIVITY;
import settlement.room.service.module.ROOM_ACTIVITY.ROOM_ACTIVITY_HASER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayCooShort;
import snake2d.util.sets.LISTE;
import util.GUTIL;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.Dic;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_STOCKS extends RoomBlueprintImp implements PUNISHMENT_SERVICE, ROOM_ACTIVITY_HASER{

	final MConstructor constructor;
	final Instance instance;
	int used;
	private ArrayCooShort spots = new ArrayCooShort(128);
	private int spotI = 0;
	
	final Tile tile = new Tile(this);
	
	public final SFinderRoomService finder = new SFinderRoomService("Stock") {
		
		@Override
		public FSERVICE get(int tx, int ty) {
			Tile t = tile.get(tx, ty);
			if (t != null) {
				return t.service;
			}
			return null;
		}
	};
	
	public ROOM_STOCKS(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(init, 0, "_STOCKS", cat);
		this.constructor = new MConstructor(this, init);
		this.instance = new Instance(init.m, this);
	}
	
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new UIRoomModule() {
			
			@Override
			public void hover(GBox box, Room i, int rx, int ry) {
				
				AREA a = SETT.ROOMS().map.rooma.get(rx, ry);
				int am = 0;
				int aa = 0;
				for (COORDINATE c : a.body()) {
					if (a.is(c) && tile.get(c.x(), c.y()) != null) {
						am ++;
						if (tile.get(c.x(), c.y()).state() == STATE.available)
							aa++;
					}
				}
				
				box.textLL(Dic.¤¤Available);
				box.add(GFORMAT.iofk(box.text(), aa, am));
				
				super.hover(box, i, rx, ry);
			}
		});
	}
	
	@Override
	protected void save(FilePutter f) {
		f.i(used);
		f.i(spots.size());
		spots.save(f);
	}

	@Override
	protected void load(FileGetter f) throws IOException {
		used = f.i();
		spots = new ArrayCooShort(f.i());
		spots.load(f);
	}

	@Override
	protected void clear() {
		used = 0;
		spots.set(0);
	}
	
	@Override
	public Room get(int tx, int ty) {
		if (ROOMS().map.get(tx, ty) == instance)
			return instance;
		return null;
	}

	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return finder;
	}
	
	@Override
	public MConstructor constructor() {
		return constructor;
	}
	



	@Override
	public int punishTotal() {
		return spots.getI();
	}


	@Override
	public int punishUsed() {
		return used;
	}

	private final ROOM_ACTIVITY activity = new ROOM_ACTIVITY() {
		
		@Override
		public SFinderRoomService service() {
			return finder;
		}
		
		@Override
		public boolean shouldBoo(int sx, int sy) {
			Tile t = tile.get(sx, sy);
			if (t != null && t.state() == STATE.used)
				return  true;
			return false;
		};
		
		@Override
		public boolean shouldCheer(int sx, int sy) {
			return false;
		};
	};

	@Override
	public ROOM_ACTIVITY spec() {
		return activity;
	}
	
	void add(int tx, int ty) {
		
		tile.get(tx, ty).availableSet(100);
		tile.get(tx, ty).stateSet(STATE.available);
		int ii = spots.getI();
		if (spots.getI() >= spots.size()-1) {
			ArrayCooShort nn = new ArrayCooShort(spots.size()+128);
			for (int i = 0; i < ii; i++) {
				nn.set(i).set(spots.set(i));
			}
			nn.set(ii);
			spots = nn;
			
		}
		
		spots.get().set(tx, ty);
		
		spots.inc();
		
	}
	
	void remove(int tx, int ty) {
		
		tile.get(tx, ty).availableSet(0);
		tile.get(tx, ty).stateSet(STATE.none);
		int ii = spots.getI();
		spots.set(ii-1);
		int ox = spots.get().x();
		int oy = spots.get().y();
		for (int i = 0; i < ii; i++) {
			if (spots.set(i).isSameAs(tx, ty)) {
				spots.get().set(ox, oy);
				spots.set(ii-1);
				return;
			}
		}
		throw new RuntimeException();
	}
	
	public DIR stockDir(int tx, int ty, DIR d) {
		FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
		if (it == null)
			return d;
		if ((GUTIL.ran2().get(tx, ty) & 1) == 0)
			return DIR.ORTHO.getC(it.rotation-1);
		return DIR.ORTHO.getC(it.rotation+1);
	}
	
	public boolean stockIsReserved(int tx, int ty) {
		Tile t = tile.get(tx, ty);
		if (t != null)
			return t.state() == STATE.reserved || t.state() == STATE.used;
		return constructor.service(tx, ty);
	}
	
	private Coo tmp = new Coo();
	
	public COORDINATE stockReserve() {
		int ii = spots.getI();
		if (used > ii) {
			recount();
		}
		if (used >= ii)
			return null;
		
		for (int i = 0; i< ii; i++) {
			if (spotI >= ii) {
				spotI = 0;
			}
			spots.set(spotI);
			
			Tile t = tile.get(spots.get().x(), spots.get().y());
			if (t.state() == STATE.available) {
				t.stateSet(STATE.reserved);
				tmp.set(spots.get());
				spots.set(ii);
				return tmp;
			}
			spotI ++;
		}
		spots.set(ii);
		
		recount();
		
		return null;
	}
	
	private void recount() {
		
		int ii = spots.getI();
		GAME.Warn("nay " + ii + " " + used);
		used = 0;
		for (int i = 0; i< ii; i++) {
			if (spotI >= ii) {
				spotI = 0;
			}
			spots.set(spotI);
			
			Tile t = tile.get(spots.get().x(), spots.get().y());
			if (t.used())
				used++;
		}
		spots.set(ii);
	}
	
	public void stockUse(int tx, int ty) {
		Tile t = tile.get(tx, ty);
		if (t != null && t.state() == STATE.reserved) {
			t.stateSet(STATE.used);
		}
	}
	
	public void stockCancel(int tx, int ty) {
		Tile t = tile.get(tx, ty);
		if (t != null) {
			t.stateSet(STATE.available);
		}
	}
}
