package world.army;

import java.io.IOException;
import java.util.Iterator;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.time.TIME;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.text.D;
import util.updating.IUpdater;
import view.ui.message.MessageText;
import view.world.panel.IDebugPanelWorld;
import world.entity.army.WArmy;

public final class WDivMercenaries implements LIST<WDivMercenary>{

	private ArrayList<WDivMercenary> all = new ArrayList<>(40);
	
	private static CharSequence ¤¤mWTitle = "¤Mercenaries Displeased!";
	private static CharSequence ¤¤mWBody = "¤We are running low on Denari and can't pay our mercenaries. We need at least {0} additional Denari to ensure their loyalty.";

	private static CharSequence ¤¤mTitle = "¤Mercenaries leaving!";
	private static CharSequence ¤¤mBody = "¤Since you don't have enough credits to pay them, your hired mercenaries are leaving you.";
	
	static {
		D.ts(WDivMercenaries.class);
	}
	
	private final IUpdater updater = new IUpdater(all.max(), TIME.secondsPerDay()) {
		
		@Override
		protected void update(int di, double timeSinceLast) {
			if (di == 10) {
				int missed = 0;
				int cc = 0;
				for (WDivMercenary d : all) {
					if (d.army() == null || d.army().faction() != FACTIONS.player())
						continue;
					if (d.missedPayments < 0) {
						d.missedPayments = 0;
						continue;
					}
					int cost = (int) (d.costPerMan()*d.men()*d.army().supplyAmount());
					if (cost > FACTIONS.player().credits().credits()) {
						d.missedPayments++;
						if (d.missedPayments >= 2) {
							d.reassign(null);
						}
						cc+= cost;
						missed = Math.max(missed, d.missedPayments);
					}else {
						FACTIONS.player().credits().inc(-cost, CTYPE.MERCINARIES);
						d.missedPayments --;
						if (d.missedPayments > 0 && cost <= FACTIONS.player().credits().credits()) {
							FACTIONS.player().credits().inc(-cost, CTYPE.MERCINARIES);
						}
					}
				}
				
				if (missed == 1) {
					Str.TMP.clear().add(¤¤mWBody);
					Str.TMP.insert(0, cc);
					new MessageText(¤¤mWTitle, Str.TMP).send();
				}else if (missed == 2) {
					new MessageText(¤¤mTitle, ¤¤mBody).send();
				}
				
			}
			
			
			WDivMercenary d = all.get(di);
			
			if (d.army() != null) {
				if (d.army().recruiting()) {
					d.menSet(CLAMP.i(d.men() + 1, 0, d.menTarget()));
				}
				
			}else {
				
				STATS.POP().age.DAYS.inc(d.cheif(), 1);
				if (STATS.POP().age.shouldDieOfOldAge(d.cheif())) {
					d.randomize();
				}
				
				if (d.disbandTime > 0) {
					d.disbandTime -= timeSinceLast;
				}
				
			}
			
		}
	};
	
	WDivMercenaries() {
		
		D.t(this);
		
		for (int i = 0; i < all.max(); i++) {
			WDivMercenary d = new WDivMercenary(i);
			all.add(d);
		}
		
		IDebugPanelWorld.add("mercs randomize", new ACTION() {
			
			@Override
			public void exe() {
				randmoize();
			}
		});
		
	}
	
	void randmoize() {
		for (int i = 0; i < all.max(); i++) {
			WDivMercenary d = all.get(i);
			d.randomize();
		}
	}
	
	void save(FilePutter file) {
		for (WDivMercenary d : all)
			d.save(file);
		updater.save(file);
		
	}
	
	void load(FileGetter file) throws IOException {
		for (WDivMercenary d : all)
			d.load(file);
		updater.load(file);
	}
	
	public void debug() {
		debug = true;
		update(TIME.secondsPerDay()*all.size());
	}
	
	void update(double ds) {
		
		updater.update(ds);
		
	}
	
	private boolean debug = false;
	
	public int max() {
		if (debug)
			return size();
		else {
			double d = FACTIONS.player().realm().all().size();
			for (Faction f : DIP.VASSAL().all(FACTIONS.player())) {
				d += f.realm().regions()/2.0;
			}
			for (Faction f : DIP.ALLY().all(FACTIONS.player())) {
				d += f.realm().regions()/4.0;
			}
			
			d /= 16.0;
			int m = (int) (d*size());
			m = CLAMP.i(m, 1, size());
			return m;
		}
	}
	
	public int upkeepCost(int index) {
		return all.get(index).costPerMan()*all.get(index).menTarget();
	}
	
	public int signingCost(int index) {
		return 4*all.get(index).costPerMan()*all.get(index).menTarget();
	}
	

	ADDiv get(long l) {
		return all.get((int) (l & 0x00000FFFF));
	}
	
	public void hire(WArmy a, WDivMercenary div) {
		div.reassign(a);
	}


	
	@Override
	public Iterator<WDivMercenary> iterator() {
		return all.iterator();
	}

	@Override
	public WDivMercenary get(int index) {
		return all.get(index);
	}

	@Override
	public boolean contains(int i) {
		return all.contains(i);
	}

	@Override
	public boolean contains(WDivMercenary object) {
		return all.contains(object);
	}

	@Override
	public int size() {
		return all.size();
	}

	@Override
	public boolean isEmpty() {
		return all.isEmpty();
	}


	
	
}
