package game.events.world;

import java.io.IOException;

import game.GAME;
import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GText;
import util.text.D;
import view.ui.message.MessageText;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.army.AD;
import world.army.ADSupply;
import world.army.WDivRegional;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;
import world.region.pop.RDRace;

public class EventWorldRebellion extends EventResource{

	private static CharSequence ¤¤Rebellion = "¤Rebellion!";
	private static CharSequence ¤¤Warning = "¤Rebellion Imminent!";
	private static CharSequence ¤¤WarningD = "¤The region of {0} has very low public order, and could rebel any day now. A temporary fix would be to station troops there, deterring the troublemakers, but in the long run, we should look into fixing these problems permanently.";
	private static CharSequence ¤¤RebellionD = "¤The region of {0} has had enough of your mistreatment and have declared independency from your tyrannical rule.";
	private static CharSequence ¤¤RebellionArmy = "¤In fact they hate you so much that they have raised an army against you. You better deal with this problem before it spreads.";
	
	
	private double timer = 0;
	private int ri = 0;
	private final Bitmap1D warning = new Bitmap1D(WREGIONS.MAX, false);
	
	static {
		D.ts(EventWorldRebellion.class);
	}
	
	public EventWorldRebellion(){
		super("REBELLION");
		IDebugPanelWorld.add("Rebellion", new ACTION() {
			
			@Override
			public void exe() {
				int ri = RND.rInt(FACTIONS.player().realm().all().size());
				if (ri >= 1) {
					rebel(FACTIONS.player().realm().region(ri));
				}
			}
		});
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				if (newOwner == FACTIONS.player()) {
					timer = 0;
					warning.set(reg.index(), false);
				}
			}
		};
		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.i(ri);
		warning.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		ri = file.i();
		warning.load(file);
	}
	
	@Override
	protected void clear() {
		ri = 0;
		timer = 0;
		warning.setAll(false);
	}

	
	@Override
	protected void update(double ds) {
		if (FACTIONS.player().realm().regions() == 0)
			return;
		int t = (int) timer;
		timer += ds;
		if (t != (int)timer) {
			
		}
		if (timer > TIME.secondsPerDay()) {
			timer -= TIME.secondsPerDay();
			ri++;
			if (ri >= FACTIONS.player().realm().regions())
				ri = 0;
			Region reg = FACTIONS.player().realm().region(ri);
			if (reg.capitol())
				return;
			
			if (RD.RACES().loyaltyAll.getD(reg) <= 0) {
				if (!warning.get(ri)) {
					warning.set(ri, true);
					new MessageText(¤¤Warning).paragraph(GText.TMP.clear().add(¤¤WarningD).insert(0, reg.info.name())).send();
				}else {
					rebel(reg);
				}
				
			}else if (RD.RACES().loyaltyAll.getD(reg) > 0.5) {
				warning.set(ri, false);
			}
		}
	}

	
	public void rebel(Region reg) {
		
		if (reg.capitol())
			return;
		
		
		RD.setFaction(reg, null, true);
		
		int men = men(reg);
		WArmy a = null;
		if (WORLD.ENTITIES().armies.canCreate() && men > 10 && RND.rBoolean()) {
			a = army(reg, men);
		}
		
		MessageText m = new MessageText(¤¤Rebellion);
		m.paragraph(Str.TMP.clear().add(¤¤RebellionD).insert(0, reg.info.name()));
		
		if (a != null)
			m.paragraph(¤¤RebellionArmy);
		
		m.send();
		
	}
	
	public int men(Region reg) {
		
		int men = RD.MILITARY().garrison.get(reg);
		
		for (WArmy a2 : FACTIONS.player().armies().all()) {
			if (a2.region() == reg) {
				men += AD.men(null).get(a2);
			}else if(a2.region() != null && a2.region().faction() == FACTIONS.player())
				men += 0.25* AD.men(null).get(a2);
			else
				men += 0.125* AD.men(null).get(a2);
		}
		
		men *= 1.0 + RND.rExpo();
		
		if (men < 30)
			men = 30 + RND.rInt(20);
		
		
		
		men = CLAMP.i(men, 0, Config.battle().MEN_PER_ARMY);
		
		return men;
	}
	
	public WArmy army(Region reg, int men) {
		COORDINATE c = WORLD.PATH().rnd(reg);
		
		WArmy a = WORLD.ENTITIES().armies.create(c.x(), c.y(), null);
		if (a == null) {
			GAME.Notify(c.x() + " " + c.y());
			return null;
		}
		
		double raceTot = 0;
		Race biggest = RACES.all().get(0);
		int b = 0;
		for (RDRace r : RD.RACES().all) {
			raceTot += r.pop.get(reg);
			if (r.pop.get(reg) > b) {
				biggest = r.race;
			}
		}
		
		double menLeft = 0;
		
		for (RDRace r : RD.RACES().all) {
		
			menLeft += men*r.pop.get(reg)/raceTot;
			
			while (menLeft > 2 && a.divs().canAdd()) {
				
				
				int am = CLAMP.i((int)menLeft, 0, Config.battle().MEN_PER_DIVISION);
				
				
				WDivRegional d = AD.regional().create(r.race, (double)am/Config.battle().MEN_PER_DIVISION, a);
				d.randomize(RND.rExpo(), RND.rExpo());
				d.menSet(d.menTarget());
				menLeft -= am;
				
			}
		}
		
		a.name.clear().add(biggest.info.armyNames.rnd());
		
		for (ADSupply s : AD.supplies().all) {
			s.current().set(a, s.targetAmount(a));
		}
		
		return a;
	}



	
}
