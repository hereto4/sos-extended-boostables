package settlement.room.knowledge.school;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.BOOSTABLE_O;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.misc.util.FSERVICE;
import settlement.path.finders.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomService;
import settlement.room.service.module.RoomService.ROOM_SERVICE_HASER;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_SCHOOL extends RoomBlueprintIns<SchoolInstance> implements INDUSTRY_HASER, ROOM_SERVICE_HASER{

	final Industry industry;
	final SchoolConstructor constructor;
	final RoomService service;
	final SchoolStation station = new SchoolStation(this);
	public final double learningSpeed;
	private final byte[] access = new byte[RACES.all().size()]; 
	final LIST<Industry> indus;

	public ROOM_SCHOOL(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		service = new RoomService(this, init, null) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return station.service(tx, ty);
			}
		};

		constructor = new SchoolConstructor(this, init);
		

		industry = new Industry(this, init.data(), null) {
			
			@Override
			public double consumptionRate(RoomInstance ins, Humanoid h, IndustryResource oo) {
				if (ins.employees().employed() == 0)
					return 0;
				double d = oo.rate*service.load()*service.total()/ins.employees().employed();
				return d;
			}
			
		};
		learningSpeed = init.data().d("LEARNING_SPEED", 0, 1);

		indus = new ArrayList<>(industry);
		Arrays.fill(access, (byte)1);
	}
	
	@Override
	protected void update(double ds) {

	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return service.finder;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		service.saver.save(saveFile);
		industry.save(saveFile);
		saveFile.bsE(access);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		service.saver.load(saveFile);
		industry.load(saveFile);
		saveFile.bsE(access);
	}
	
	@Override
	protected void clearP() {
		service.saver.clear();
		industry.clear();
		Arrays.fill(access, (byte)1);
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	public boolean access(Race race) {
		if (race == null) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				if (access[ri] == 0)
					return false;
			}
			return true;
		}
		return access[race.index] != 0;
	}
	
	public void accessToggle(Race race) {
		byte ii = (byte) (access(race) ? 0 : 1);
		if (race == null) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				access[ri] = ii;
			}
		}else
		access[race.index] = ii;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}

	@Override
	public RoomService service() {
		return service;
	}
	
	
	public STAT stat() {
		return STATS.SERVICE().SCHOOLS.get(typeIndex());
	}
	
	public DIR childDir(int sx, int sy) {
		return station.serviceDir(sx, sy);
	}
	
	public double learningSpeed(int tx, int ty) {
		SchoolInstance ins = get(tx, ty);
		if (ins == null)
			return 0;
		return learningSpeed*(1.0-ins.getDegrade())*constructor.quality.get(ins);
	}
	
	@Override
	public double industryFormatConsumptionRate(GText text, IndustryResource i, RoomInstance ins) {
		SchoolInstance sc = (SchoolInstance) ins;
		
		double d = i.rate*sc.service().load()*sc.service().total();
		GFORMAT.f0(text, -d);
		return d;
	}
	
	public double learningSpeed(RoomInstance i, BOOSTABLE_O h) {
		SchoolInstance ins = (SchoolInstance)i;
		return learningSpeed*(1.0-ins.getDegrade())*constructor.quality.get(ins);
	}
}
