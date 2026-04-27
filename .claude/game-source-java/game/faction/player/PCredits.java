package game.faction.player;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.FCredits;
import game.faction.Faction;
import game.time.TIME;
import game.time.TIMECYCLE;
import init.resources.RESOURCE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.statistics.HistoryInt;

public final class PCredits extends FCredits{

	public static int history = 48;
	private final ArrayListGrower<CredHistory> all = new ArrayListGrower<CredHistory>();

	public PCredits() {
		super(history, TIME.days());
		
		for (CTYPE t : CTYPE.values()) {
			all.add(new CredHistory(t, 48, TIME.days()));
		}
	}

	
	@Override
	protected void update(double ds, Faction f) {
		super.update(ds, f);
	}
	
//	public double tradePenalty(double price) {
//		double b = CLAMP.d(1.0/BOOSTABLES.CIVICS().TRADE.get(HCLASS.CITIZEN, null), 0, 10000);
//		return  CLAMP.d((price*0.2 + price*0.7*b), 0, price);
//	}

	@Override
	public void inc(double amount, CTYPE t) {
		if (amount < 0)
			all.get(t.ordinal()).OUT.inc((int) -amount);
		else
			all.get(t.ordinal()).IN.inc((int) amount);
	}

	@Override
	public void inc(double amount, CTYPE t, RESOURCE res, int resAm) {
		inc(amount, t);
		if (t == CTYPE.TRADE) {
			FACTIONS.player().trade.trade(amount, res, resAm);
		}
		
		
		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(all.size());
		for (CredHistory h : all)
			h.saver.save(file);
		super.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		int l = file.i();
		if (l != all.size()) {
			for (int i = 0; i < l; i++)
				all.get(0).saver.load(file);
			clear();
		}else {
			for (CredHistory h : all)
				h.saver.load(file);
		}

		super.load(file);
	}
	
	@Override
	protected void clear() {
		for (CredHistory h : all)
			h.saver.clear();
		super.clear();
	}
	
	public LIST<CredHistory> all(){
		return all;
	}
	
	public CredHistory get(CTYPE type) {
		return all.get(type.ordinal());
	}
	
	public class CredHistory {

		public final CTYPE type;
		public final HistoryInt IN;
		public final HistoryInt OUT;
		
		public CredHistory(CTYPE type, int saved, TIMECYCLE time) {
			IN = new HistoryInt(saved, time, false) {
				@Override
				protected void change(int old, int current) {
					inccc(current-old);
				}
			};
			OUT = new HistoryInt(saved, time, false) {
				@Override
				protected void change(int old, int current) {
					
					inccc(-(current-old));
				}
			};
			this.type = type;
		}

		public final SAVABLE saver = new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				IN.save(file);
				OUT.save(file);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				IN.load(file);
				OUT.load(file);
			}
			
			@Override
			public void clear() {
				IN.clear();
				OUT.clear();
			}
		};
		
		
	}
	
}