package game.events.disaster;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.type.DISEASE;
import init.type.DISEASES;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.message.MessageText;

public final class EventDiseaseMild extends EventResource{

	private static CharSequence ¤¤titleF = "Disease at our doorstep!";
	private static CharSequence ¤¤descF = "There is rumour that the lands to the east have been ravaged by a deadly disease, leaving cities in ruins and emptying villages of the living. Let us hope it does not reach our lands.";
	
	private static CharSequence ¤¤title = "Epidemic!";
	private static CharSequence ¤¤desc = "It has come to us. The dreaded {0}. Nothing could have been done, it was the will of the gods. Lets hope our hospitals are well staffed. A curfew can be issued to contain it.";
	
	static {
		D.ts(EventDiseaseMild.class);
	}
	
	private final double maxTime = 16*16*TIME.secondsPerDay();
	private double timer = 0;
	private double ran = RND.rFloat();
	
	public EventDiseaseMild() {
		super("DISEASE_MILD");
		reset();
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.d(ran);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		ran = file.d();
	}
	
	@Override
	protected void clear() {
		reset();
	}
	
	@Override
	protected void update(double ds) {
		
		if (STATS.POP().POP.data().get(null) < 1000)
			return;
		
		double t = timer;
		
		timer += ds;
		
		if (t < maxTime - TIME.secondsPerDay()*4 && timer > maxTime - TIME.secondsPerDay()*4) {
			
			new MessageText(¤¤titleF).paragraph(¤¤descF).send();
		}
		
		if (timer < maxTime) {
			return;
		}
		
		
		
		DISEASE de = DISEASES.randomEpidemic(ran);
		reset();
		
		if (de == null || SETT.INVADOR().invading()) {
			timer -= TIME.secondsPerDay();
			return;
		}
		
		double eff = 0.25 + 0.75*STATS.POP().POP.data().get(null)/20000.0;
		
		
		
		if (STATS.DISEASE().outbreak(de.infectRate*eff, de)) {
			MessageText te = new MessageText(¤¤title);
			Str.TMP.clear().add(¤¤desc).insert(0, de.info.name);
			te.paragraph(Str.TMP);
			Str.TMP.clear().add(de.info.name).NL().add(de.info.desc);
			te.paragraph(Str.TMP);
			te.send();
		}
		
	}
	
	private void reset() {
		timer = 0;
		timer -= (1 + RND.rFloat()*2)*TIME.secondsPerDay();
		ran = RND.rFloat();
	}

	
}
