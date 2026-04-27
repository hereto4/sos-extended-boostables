package world.army;

import java.io.IOException;

import game.boosting.BUtil;
import game.boosting.BoosterAbs;
import game.debug.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.Race;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import util.data.INT_O.INT_OE;
import world.WORLD;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;
import world.army.ai.WArmyAI;
import world.entity.army.WArmy;

public final class AD extends WorldResource{	
	
	static AD self;
	{
		self = this;
	}
	private final ArrayListGrower<BoosterAbs<WArmy>> moraleFactors = new ArrayListGrower<>();
	private final ADInit init = new ADInit();
	private final ArrayList<ADArmies> fArmies = new ArrayList<>(FACTIONS.MAX()+1);
	private final ADConscripts conscripts = new ADConscripts(init);
	private final ADSoldiers soldiers = new ADSoldiers(init);
	private final ADPower power = new ADPower(init);
	final ADSupplies supplies = new ADSupplies(init); 
	private final INT_OE<WArmy> data = init.dataA.new DataShort("FACTION");
	private final ADStats stats = new ADStats(init);
	private final ADUpdater updater = new ADUpdater(init);
	private final ADUpdaterDiv updaterDiv = new ADUpdaterDiv(init);
	public final WArmyAI AI = new WArmyAI();
	
	private final WDivStoredAll divsCity = new WDivStoredAll();
	private final WDivMercenaries divsMerc = new WDivMercenaries();
	private final WDivRegionalAll divsReg = new WDivRegionalAll();
	

	
	private final WorldResourceManager saver = new WorldResourceManager() {
		
		@Override
		public void save(FilePutter file) {
			for (ADArmies a : fArmies)
				a.saver.save(file);
			updater.save(file);
			updaterDiv.save(file);
			AI.saver.save(file);
			divsCity.save(file);
			divsMerc.save(file);
			divsReg.save(file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			for (ADArmies a : fArmies)
				a.saver.load(file);
			updater.load(file);
			updaterDiv.load(file);
			AI.saver.load(file);
			divsCity.load(file);
			divsMerc.load(file);
			divsReg.load(file);
		}
		
		@Override
		public void clear() {
			for (ADArmies a : fArmies)
				a.saver.clear();	
			updater.clear();
			updaterDiv.clear();
			AI.saver.clear();

			divsReg.clear();
		}
		
		@Override
		public void generate(ACTION loadPrint) {
			for (Faction f : FACTIONS.all()) {
				if (f.isActive()) {
					for (ACTION_O<Faction> a : init.inits) {
						a.exe(f);
					}	
					AI.init(f);
				}
						
			}
			mercenaries().randmoize();
		};
	};
	
	
	public AD(WORLD ww){
		super("Armies", "AD");
		fArmies.add(new ADArmies(-1, 1024));
		while(fArmies.hasRoom())
			fArmies.add(new ADArmies(fArmies.size()-1, 60));
		

	}
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}
	
	@Override
	protected void update(double ds, Profiler prof) {
		prof.logStart(this);
		
		updater.update(ds);
		updaterDiv.update(ds);
		divsCity.update(ds);
		divsMerc.update(ds);
		AI.update(ds);
		prof.logEnd(this);
	}
	
	static ADInit iinit() {
		return self.init;
	}
	
	public static ADArmies army(Faction f) {
		return self.fArmies.get(f == null ? 0 : f.index()+1);
	}
	
	public static ADConscripts conscripts(){
		return self.conscripts;
	}
	
	public static Faction faction(WArmy a) {
		if (self.data.get(a) == 0)
			return null;
		return FACTIONS.getByIndex(self.data.get(a)-1);
		
	}
	
	public static void factionSet(WArmy a, Faction f) {
		
		if (faction(a) == f)
			return;
		
		removeOnlyTobeCalledFromAnArmy(a);

		
		
		addOnlyToBeCalledFromAnArmy(a, f);
		
	}
	
	public static void addOnlyToBeCalledFromAnArmy(WArmy a, Faction f) {
		self.data.set(a, f == null ? 0 : f.index() + 1);
		ADArmies aa = army(faction(a));
		aa.armies.add(a.armyIndex());
		for (ADInit.Countable cc : self.init.countable) {
			cc.count(a, 1);
		}
	}
	
	public static void removeOnlyTobeCalledFromAnArmy(WArmy a) {
		for (ADInit.Countable cc : self.init.countable) {
			cc.count(a, -1);
		}
		ADArmies aa = army(faction(a));
		aa.armies.removeShort(a.armyIndex());
	}
	
	public static ADInt men(Race race) {
		return self.soldiers.current(race);
	}
	
	public static ADInt menTarget(Race race) {
		return self.soldiers.target(race);
	}
	
	public static void updateArmy(WArmy a) {
		self.AI.update(a);
	}
	
	static void register(ADDiv div, int d) {
		if (div.army() == null)
			return;
		
		for (ADInit.Register rr : self.init.registers) {
			rr.register(div, d);
		}
	}
		
	public static ADPower power() {
		return self.power;
	}
	
	public static ADStats stats() {
		return self.stats;
	}
	
	public static ADSupplies supplies() {
		return self.supplies;
	}
	
	public static WDivStoredAll cityDivs() {
		return self.divsCity;
	}
	
	public static WDivRegionalAll regional() {
		return self.divsReg;
	}
	
	public static WDivMercenaries mercenaries() {
		return self.divsMerc;
	}
	
	public static ArrayListGrower<BoosterAbs<WArmy>> moraleFactors(){
		return self.moraleFactors;
	}
	
	public static double morale(WArmy a) {
		return BUtil.value(self.moraleFactors, a);
	}

	
}
