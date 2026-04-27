package settlement.stats.standing;

import java.io.IOException;

import game.GAME;
import game.debug.Profiler;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.value.GVALUES;
import settlement.main.SETT.SettResource;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;

public class STANDINGS extends SettResource{

	private static STANDINGS s;
	
	public static void create() {
		s = new STANDINGS();
	}
	
	private final StandingCitizen happiness = new StandingCitizen(); 
	private final StandingSlave submission = new StandingSlave();
	
	private STANDINGS(){
		super("STANDINGS", false);
		
		for (Race r : RACES.all()) {
			GVALUES.FACTION.push("LOYALTY_" + r.key, happiness.loyalty.info().name + ": " + r.info.names, new SPRITE.Imp(Icon.M) {

				@Override
				public void render(SPRITE_RENDERER re, int X1, int X2, int Y1, int Y2) {
					r.appearance().icon.render(re, X1, X2, Y1, Y2);
					
				}}, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction t) {
					return happiness.loyalty.getD(r);
				}
				
			});
			
			
		}
		GVALUES.FACTION.push("LOYALTY", happiness.loyalty.info().name, UI.icons().s.heart, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction t) {
				return happiness.current();
			}
			
		});
		GVALUES.FACTION.push("SUBMISSION_SLAVES", submission.info().name, UI.icons().s.slave, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction t) {
				return submission.current();
			}
			
		});
		
		GAME.addBeforeGameStarts(new ACTION() {
			
			@Override
			public void exe() {
				s.happiness.init();
				s.submission.init();
			}
		});
		
	}
	
//	public static void initAll() {
//		s.happiness.init();
//		s.submission.init();
//	}
//	
	@Override
	protected void save(FilePutter file) {
		happiness.save(file);
		submission.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		happiness.load(file);
		submission.load(file);
	}
	
	@Override
	protected void clear() {
		happiness.clear();
		submission.clear();
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		happiness.update(ds);
		submission.update(ds);
	}
	
	public static Standing get(HCLASS c) {
		if (c == HCLASSES.CITIZEN())
			return s.happiness;
		else if (c == HCLASSES.SLAVE())
			return s.submission;
		return s.happiness;
	}
	
	public static StandingCitizen CITIZEN() {
		return s.happiness;
	}
	
	public static StandingSlave SLAVE() {
		return s.submission;
	}
	
	
	
	
}
