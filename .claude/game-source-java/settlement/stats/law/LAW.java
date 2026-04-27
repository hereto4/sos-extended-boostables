package settlement.stats.law;

import java.io.IOException;

import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatable;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public final class LAW {

	private static LAW self;
	{
		self = this;
	}
	{PRISONER_TYPE.init();}
	private final Crimes crimes = new Crimes();
	private final Prisoners prisoners = new Prisoners();
	private final Processing processing = new Processing();
	private final LawRate law = new LawRate();
	private final Curfew curfew = new Curfew();

	LAW(StatsInit init){
		self = this;
		init.savers.put("LAW_CRAP", saver);
		init.upers.add(uper);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			crimes.saver.save(file);
			processing.saver.save(file);
			law.saver.save(file);
			curfew.saver.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			crimes.saver.load(file);
			processing.saver.load(file);
			law.saver.load(file);
			curfew.saver.load(file);
		}

		@Override
		public void clear() {
			crimes.saver.clear();
			processing.saver.clear();
			law.saver.clear();
			curfew.saver.clear();
		}
	};
	
	final StatUpdatable uper = new StatUpdatable() {
		
		@Override
		public void update(double ds) {
			processing.update(ds);
			crimes.update(ds);
			law.update(ds);
			curfew.update(ds);
		}
	};
	
	public static Prisoners prisoners() {
		return self.prisoners;
	}
	
	public static Crimes crimes() {
		return self.crimes;
	}
	
	public static Processing process() {
		return self.processing;
	}

	public static LawRate law() {
		return self.law;
	}
	
	public static Curfew curfew() {
		return self.curfew;
	}
	
	
}
