package settlement.stats.service;

import init.type.NEED;
import init.type.NEEDS;
import settlement.main.SETT;
import settlement.room.knowledge.school.ROOM_SCHOOL;
import settlement.room.service.module.RoomServiceAccess;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.StatCollection;
import settlement.stats.stat.StatInfo;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.text.D;

public final class StatsService extends StatCollection {


	public final LIST<StatServiceImp> ALL;
	public final LIST<StatServiceRoom> ROOMS;
	
	
	private final ArrayList<ArrayListGrower<StatService>> needMap = new ArrayList<>(NEEDS.ALL().size());
	private final ArrayListGrower<StatServiceImp> allNeeds = new ArrayListGrower<>();
	private double[] needTot = new double[NEEDS.ALL().size()];
	public LIST<STAT> SCHOOLS;
	public final StatServiceSimple skinnyDip;
	public final StatServiceBench bench;
	public final StatServiceHospital hospital;
	private static CharSequence ¤¤name = "Services";
	private static CharSequence ¤¤descc = "Services are provided by building service rooms and allowing access to your subjects.";
	
	static {
		D.ts(StatsService.class);
	}
	
	public StatsService(StatsInit init) {
		super(init, "SERVICE", ¤¤name, ¤¤descc);
		
		ArrayListGrower<StatServiceImp> all = new ArrayListGrower<>();

		ArrayListGrower<StatServiceRoom> rooms = new ArrayListGrower<>();
		
		for (RoomServiceAccess a : RoomServiceAccess.ALL()) {
			rooms.add(new StatServiceRoom(all, a, init));
		}
		
		skinnyDip = new StatServiceSkinny(all, init);
		bench = new StatServiceBench(all, init);
		this.ROOMS = rooms;
		
		hospital = new StatServiceHospital(all, SETT.ROOMS().HOSPITAL, init);
		
		
		ArrayListGrower<STAT> schools = new ArrayListGrower<>();
		for (ROOM_SCHOOL s : SETT.ROOMS().SCHOOLS) {
			schools.add(new STATData(null, init, init.count.new DataBit("SERVICE_SCHOOL_" + key, new StatInfo(s.info.name, s.info.desc))));
			schools.get(schools.size()-1).info().icon = s.icon;
		}
		this.SCHOOLS = schools;
		
		
		
		this.ALL = all;
		
		
		while(needMap.hasRoom())
			needMap.add(new ArrayListGrower<StatService>());
		
		for (StatServiceImp s : ALL) {
		
			if (s.need != null) {
				needTot[s.need.index()] += s.usage;
				needMap.get(s.need.index()).add(s);
				allNeeds.add(s);
			}
		}

		
		
	}
	
	private LIST<StatService> shrine;
	private LIST<StatService> temple;
	
	public LIST<StatService> perNeed(NEED n){
		if (shrine == null)
			shrine = new ArrayList<StatService>(STATS.RELIGION().SHRINE);
		if (temple == null)
			temple = new ArrayList<StatService>(STATS.RELIGION().TEMPLE);
		if (n == NEEDS.TYPES().SHRINE)
			return shrine;
		else if (n == NEEDS.TYPES().TEMPLE)
			return temple;
		return needMap.get(n.index());
	}

	public LIST<StatServiceImp> allNeeds(){
		return allNeeds;
	}
	
	public double needTot(NEED n){
		return needTot[n.index()];
	}
	
	
}
