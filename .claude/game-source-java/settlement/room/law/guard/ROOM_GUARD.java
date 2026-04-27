package settlement.room.law.guard;

import java.io.IOException;

import init.paths.PATHS;
import settlement.entity.humanoid.Humanoid;
import settlement.misc.util.FSERVICE;
import settlement.path.finders.SFinderRoomService;
import settlement.path.path.SPath;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_GUARD extends RoomBlueprintIns<GuardInstance>{

	public final static int maxRadius = 90;

	final SFinderRoomService finder;
	
	final Constructor constructor;
	final Service service = new Service(this);
	
	private EquipBattle equip;
	private String eString;
	
	public ROOM_GUARD(RoomInitData init, RoomCategorySub block) throws IOException {
		super(0, init, "_GUARD", block);
		finder = new SFinderRoomService("Guards") {
			
			@Override
			public FSERVICE get(int tx, int ty) {
				GuardInstance ins = getter.get(tx, ty);
				if (ins != null && ins.body().cX() == tx && ins.body().cY() == ty)
					return service.get(ins);
				return null;
			}
		};
		constructor = new Constructor(this, init);
		
		eString = init.data().value("EQUIPMENT_TO_USE");
		
		if (!PATHS.STATS().folder("equip").folder("battle").init.exists(eString))
			init.data().error(eString + " does not exist in: " + PATHS.STATS().folder("equip").folder("battle").toString(), "EQUIPMENT_TO_USE");
		
	}
	
	public EquipBattle equip() {
		if (equip == null) {
			equip = STATS.EQUIP().militaryColl.get("BATTLE_" +eString, null);
		}
		return equip;
	}
	
	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		return finder;
	}
	
	public void reportCriminal(Humanoid a, boolean force) {
		
		COORDINATE c = finder.reserve(a.tc(), maxRadius);
		
		
		if (c != null) {
			GuardInstance ins = getter.get(c);
			if (force) {
				ins.reportCriminal(a);
				return;
			}
			
			double d = SPath.LAST_DISTANCE();
			double chance = 1.0 - d/ins.radius();
			chance = Math.pow(chance, 2.0);
			if (chance > RND.rFloat())
				ins.reportCriminal(a);
		}
	}


	@Override
	protected void saveP(FilePutter saveFile){
		
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		
	}
	
	@Override
	protected void clearP() {
		
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

}
