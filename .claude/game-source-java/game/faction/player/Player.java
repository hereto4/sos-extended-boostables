package game.faction.player;

import java.io.IOException;

import game.GAME;
import game.boosting.BOOSTABLE_O;
import game.boosting.BValue;
import game.debug.Profiler;
import game.faction.FACTIONS;
import game.faction.FBanner;
import game.faction.FCredits.CTYPE;
import game.faction.FResources;
import game.faction.Faction;
import game.faction.player.emmi.Emissaries;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.FACTION_IMPORTER;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.type.HCLASSES;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import view.interrupter.IDebugPanel;
import world.army.AD;
import world.army.WDivMercenary;
import world.region.RD;
import world.region.pop.RDRace;

public final class Player extends Faction implements BOOSTABLE_O{

	public final PTech tech = new PTech();
	public final PTitles titles = new PTitles();
	public final PlayerRaces races;
	private final FResources resources = new FResources(48, TIME.days()) {

		@Override
		public int getAvailable(RESOURCE t) {
			long stocked = t.sellable();
			if (stocked < 1)
				return 0;
			return (int) Math.ceil(stocked*0.9);
		}
		
		
	};
	private final FBanner banner = new FBanner(this);
	private final PLevels level = new PLevels();
	private final PCredits credits = new PCredits();
	public final Emissaries emissaries = new Emissaries();
	public final PBonusSetting bonusesCustom;
	private int ri;
	public final PTrade trade = new PTrade();
	public final Str rulerName = new Str(24).add("bob");
	public final Str desc = new Str(24);
	private final PSlaves slaves = new PSlaves();
	
	public Player(LISTE<Faction> all) throws IOException{
		super(all);
		races = new PlayerRaces();
		ri = races.get(0).index();
		
		IDebugPanel.add("add credits", new ACTION() {
			
			@Override
			public void exe() {
				credits.inc(50, CTYPE.MISC);
			}
		});
		
		IDebugPanel.add("add credits+", new ACTION() {
			
			@Override
			public void exe() {
				credits.inc(10000000, CTYPE.MISC);
			}
		});
		
		bonusesCustom = new PBonusSetting();
	}
	
	public void setRace(Race race) {
		ri = race.index;
		races.set(race);
	}
	
	@Override
	public Race race() {
		return RACES.all().get(ri);
	}
	

	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
		file.i(ri);
		tech.saver.save(file);
		titles.saver.save(file);
		level.saver.save(file);
		races.saver.save(file);
		emissaries.saver.save(file);
		bonusesCustom.save(file);
		trade.saver.save(file);
		rulerName.save(file);
		PlayerColors.saver.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		super.load(file);
		ri = file.i();
		ri %= RACES.all().size();
		tech.saver.load(file);
		titles.saver.load(file);
		level.saver.load(file);
		races.saver.load(file);
		emissaries.saver.load(file);
		bonusesCustom.load(file);
		trade.saver.load(file);
		rulerName.load(file);
		PlayerColors.saver.load(file);
	}
	
	@Override
	protected void clear() {
		super.clear();
		tech.saver.clear();
		titles.saver.clear();
		level.saver.clear();
		races.saver.clear();
		emissaries.saver.clear();
		bonusesCustom.clear();
		trade.saver.clear();
		PlayerColors.saver.clear();
	}
	
	@Override
	public FACTION_IMPORTER buyer() {
		return SETT.ROOMS().IMPORT.tally;
	}
	
	@Override
	public FACTION_EXPORTER seller() {
		return SETT.ROOMS().EXPORT.tally;
	}
	
	@Override
	protected void update(double ds) {
		super.update(ds);
		
	}
	
	public void updateSpecial(double ds, Profiler prof) {
		if (capitolRegion() != null) {
			int ex = 0;
			for (Race r : RACES.all()) {
				if (RD.RACES().get(r) == null)
					ex += STATS.POP().POP.data(null).get(r, 0);
			}
			
			ex /= (RACES.all().size()-RD.RACES().all.size() + 1);
			
			for (RDRace rr : RD.RACES().all) {
				rr.pop.set(capitolRegion(),  STATS.POP().POP.data(null).get(rr.race, 0)+ex);
			}
		}
		
		
		prof.logStart(tech.getClass());
		tech.update(ds);
		prof.logEnd(tech.getClass());
		
		prof.logStart(titles.getClass());
		titles.update(ds);
		prof.logEnd(titles.getClass());
		
		prof.logStart(level.getClass());
		level.update(ds);
		prof.logEnd(level.getClass());
		
		prof.logStart(emissaries.getClass());
		emissaries.update(ds);
		prof.logEnd(emissaries.getClass());
		
		prof.logStart(trade.getClass());
		trade.update(ds);
		prof.logEnd(trade.getClass());
		
		
		
	}
	
	
	@Override
	public FResources res() {
		return resources;
	}

	@Override
	public FBanner banner() {
		return banner;
	}
	
	@Override
	public PCredits credits() {
		return credits;
	}
	
	public PLevels level() {
		return level;
	}
	
	public PTech tech() {
		return tech;
	}
	
	@Override
	public CharSequence rulerName() {
		return rulerName;
	}
	
	public static final class PlayerRaces{
		
		private final int[] order = new int[RACES.all().size()];
		
		final SAVABLE saver = new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.isE(order);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				if (!file.isE(order)) {
					set(FACTIONS.player().race());
				}
			}
			
			@Override
			public void clear() {
				
			}
		};
		
		
		void set(Race player){
			order[0] = player.index;
			int playable = 1;
			
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				if (r != player && r.playable) {
					playable++;
				}
			}
			int i = 1;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				if (r != player && r.playable) {
					order[i++] = r.index;
				}else if (r != player){
					order[playable++] = r.index;
				}
				
					
			}
		}
		
		PlayerRaces(){
			int playable = 0;
			for (Race r : RACES.all()) {
				if (r.playable) {
					playable++;
				}
			}
			int i = 0;
			for (Race r : RACES.all()) {
				if (r.playable) {
					order[i++] = r.index;
				}else {
					order[playable++] = r.index;
				}
				
					
			}
		}
		
		public void order(Race r, int index) {
			int i = 0;
			for (; i < order.length; i++)
				if (order[i] == r.index)
					break;
			
			
			for (; i < order.length-1; i++)
				order[i] = order[i+1];	
			
			for (i = order.length-1; i > index; i--)
				order[i] = order[i-1];
			
			order[index] = r.index;
			
		}
		
		public Race get(int index) {
			if (index < 0)
				return null;
			return RACES.all().get(order[index]);
		}

		public int size() {
			return order.length;
		}
		
	}

	@Override
	public PSlaves slaves() {
		return slaves;
	}
	
	@Override
	public double boostableValue(BValue v) {
		return v.vGet(this);
	}

	@Override
	public double offensivePower() {
		double p = AD.power().get(this);
		int creds = (int) FACTIONS.player().credits().getD();
		for (int i = 0; i < AD.mercenaries().max() && creds > 0; i++) {
			WDivMercenary d = AD.mercenaries().get(i);
			if (d.army() == null && !d.disbanded() ) {
				int c = AD.mercenaries().signingCost(i) + AD.mercenaries().upkeepCost(i)*16;
				double dd = (double)creds/c;
				dd = CLAMP.d(dd, 0, 1);
				p += GAME.battle().power.get(d);
				creds -= Math.ceil(c*dd);
			}
		}
	
		if (!SETT.INVADOR().invading())
			p += RD.MILITARY().power.getD(this.capitolRegion());
	
		return p;
	}

	@Override
	public int citizens(Race race) {
		return STATS.POP().POP.data(HCLASSES.CITIZEN()).get(race);
	}
	
}
