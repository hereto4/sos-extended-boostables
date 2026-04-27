package game.raiding;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.raiding.RaidingMap.RaidEntryPoint;
import game.raiding.RaidingMap.RaidRegion;
import game.time.TIME;
import init.race.Race;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import util.updating.IUpdater;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import view.ui.message.MessageSection;
import world.WORLD;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;
import world.region.pop.RDRace;

final class UpdaterRegions extends IUpdater implements SAVABLE{

	private static CharSequence ¤¤nameRace = "¤{0} Raiders";
	private static CharSequence ¤¤nameRegion = "¤Raiders of {0}";
	
	private static CharSequence ¤¤title = "Raiders!";
	private static CharSequence ¤¤desc = "Mi lord, bandits have been spotted raiding our border settlements!";

	static {
		D.ts(UpdaterRegions.class);
	}
	
	private final double[] counts = new double[WREGIONS.MAX];
	private final double II = 1.0/(TIME.secondsPerDay()*16.0);

	public UpdaterRegions() {
		super(WREGIONS.MAX, TIME.secondsPerDay());
		
		IDebugPanel.add("Raider region spawn", new ACTION() {
			
			@Override
			public void exe() {
				if (GAME.raiders().entry.entryRegions().size() > 0) {
					raid(GAME.raiders().entry.entryRegions().rnd().r());
				}
				
			}
		});
	}


	@Override
	public void save(FilePutter file) {
		file.dsE(counts);
		super.save(file);
	}


	@Override
	public void load(FileGetter file) throws IOException {
		file.dsE(counts);
		super.load(file);
	}


	@Override
	public void clear() {
		Arrays.fill(counts, 0);
		super.clear();
	}


	@Override
	protected void update(int i, double timeSinceLast) {
		RaidRegion r = GAME.raiders().entry.get(WORLD.REGIONS().all().get(i));
		double d = counts[i];
		if (r.r().faction() != FACTIONS.player() || r.r().capitol()) {
			d -= timeSinceLast*II;
		}else {
			d += timeSinceLast*II*r.probability();
			if (d >= 1) {
				raid(r.r());
				d = 0;
			}
		}
		d = CLAMP.d(d, 0, 1);
		
	}
	
	private final ArrayList<RaidEntryPoint> tmp = new ArrayList<>(16);
	
	private void raid(Region reg) {
		if (reg.besieged())
			return;
		for (WArmy a : WORLD.ENTITIES().armies.fill(reg)) {
			if (a.faction() == null || (a.faction() != FACTIONS.player() && DIP.get(a.faction(), FACTIONS.player()) == DIP.WAR()))
				return;
		}
		
		tmp.clearSloppy();
		for (RaidEntryPoint c : GAME.raiders().entry.entrySpots()) {
			if (reg.is(c.c()) && tmp.hasRoom()) {
				tmp.add(c);
			}
		}
		
		if (tmp.size() <= 0)
			return;
		
		RaidEntryPoint e = tmp.rnd();
		
		double power = RD.MILITARY().power.getD(reg) + 20;
		power += GAME.raiders().entry.get(reg).army();
		power *= 1.25 + RND.rExpo()*2.0;
		Race race = race(e);
		
		RaiderArmy a = new RaiderArmy(race, power, 0.2 + RND.rFloat()*0.5);
		Str.TMP.clear();
		if (e.from() != null) {
			Str.TMP.add(¤¤nameRegion).insert(0, e.from().info.name());
		}else {
			Str.TMP.add(¤¤nameRace).insert(0, race.info.namePosessives);
		}
		a.spawn(e.c().x(), e.c().y(), Str.TMP);
		new M(e.c().x(), e.c().y()).send();
		
	}
	
	private Race race(RaidEntryPoint e) {
		
		if (e.from() != null) {
			double tot = 0;
			for (RDRace r : RD.RACES().all) {
				tot += r.pop.get(e.from())*r.race.physics.raiding; 
			}
			
			tot *= RND.rFloat();
			for (RDRace r : RD.RACES().all) {
				tot -= r.pop.get(e.from())*r.race.physics.raiding;
				if (tot <= 0)
					return r.race;
			}
			
		}
		
		double tot = 0;
		for (RDRace r : RD.RACES().all) {
			tot += r.race.physics.raiding; 
		}
		
		tot *= RND.rFloat();
		for (RDRace r : RD.RACES().all) {
			tot -= r.race.physics.raiding;
			if (tot <= 0)
				return r.race;
		}
		
		return RD.RACES().all.rnd().race;
		
	}
	
	private static class M extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int x,y;
		
		public M(int x, int y) {
			super(¤¤title);
			this.x = x;
			this.y = y;
		}

		@Override
		protected void make(GuiSection section) {
			paragraph(¤¤desc);
			
			GButt b = new GButt.ButtPanel(UI.icons().m.crossair) {
				
				@Override
				protected void clickA() {
					VIEW.world().activate();
					VIEW.world().window.centererTile.set(x, y);
				}
				
			};
			
			section.addRelBody(16, DIR.S, b);
			
		}
		
		
	}
	
}
