package game.events.disaster;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.type.DISEASE;
import init.type.DISEASES;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

public final class EventDisease extends EventResource{

	private static CharSequence ¤¤title = "Outbreak!";
	private static CharSequence ¤¤desc = "Dreadful news! On account of poor health and sanitation, an epidemic of {0} has been discovered. Lets hope our hospitals are well staffed. A curfew can be issued to contain it.";
	
	static {
		D.ts(EventDisease.class);
	}
	
	private final double maxTime = 8*TIME.secondsPerDay();
	private double timer = 1;
	private double ran = RND.rFloat();
	private double spread = 0;
	private double warmup = 0.25;
	

	
	public EventDisease() {
		super("DISEASE");
		reset();
		
		IDebugPanelSett.add("EVENT DISEASE", new ACTION() {
			
			@Override
			public void exe() {
				set();
			}
		});
		
		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.d(spread);
		file.d(ran);
		file.d(warmup);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		spread = file.d();
		ran = file.d();
		warmup = file.d();
	}
	
	@Override
	protected void clear() {
		reset();
	}
	
	@Override
	protected void update(double ds) {
		
		double d = 1-BOOSTABLES.PHYSICS().HEALTH.get(POP_CL.clP());
		if (d < 0) {
			timer+= ds;
			timer = CLAMP.d(timer, 0, 16.0*TIME.secondsPerDay());
			return;
		}
		
		timer -= ds*d;
		
		if (timer < 0) {
			set();
		}
		
		
	}
	
	private void set() {
		DISEASE de = DISEASES.randomEpidemic(ran);
		
		if (de == null || SETT.INVADOR().invading()) {
			timer += 0.1;
			return;
		}
		
		warmup = CLAMP.d(warmup, 0.25, 1);
		
		double o = warmup *spread * de.infectRate;
		warmup += warmup;
		

		if (STATS.DISEASE().outbreak(o, de)) {
			MessageText te = new MessageText(¤¤title);
			Str.TMP.clear().add(¤¤desc).insert(0, de.info.name);
			te.paragraph(Str.TMP);
			Str.TMP.clear().add(de.info.name).NL().add(de.info.desc);
			te.paragraph(Str.TMP);
			te.send();
			STANDINGS.CITIZEN().buff.execute(HCLASSES.CITIZEN(), TIME.secondsPerDay()*8);
		}
		
		reset();
	}
	
	private void reset() {
		timer = maxTime;
		timer *= 1 + RND.rFloat()*2;
		ran = RND.rFloat();
		spread = 0.6 + 0.4*RND.rFloat();
	}

	
}
