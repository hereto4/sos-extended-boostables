package game.faction.player;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.boosting.BoosterValue;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCES;
import init.resources.STOCKPILE.StockpileImp;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.GUTIL;
import util.text.D;

public final class PBonusSetting implements SAVABLE{

	private static CharSequence ¤¤name = "¤Setting";
	private final ArrayListGrower<AAA> boosts = new ArrayListGrower<>();
	
	public final StockpileImp startResources = new StockpileImp();
	public int startLevel = 0;
	
	static {
		D.ts(PBonusSetting.class);
	}

	protected PBonusSetting() {

		
	}
	
	public void add(String key, double am, boolean isMul) {
		
		if (isMul && am == 1 || am == 0)
			return;
		
		AAA a = new AAA(key, am, isMul);
		if (add(a)) {
			boosts.add(a);
		}
		
		
	}

	private boolean add(AAA a) {
		String key = a.key;
		double am = a.am;
		boolean isMul = a.isMul;
		LIST<Boostable> bb = BOOSTING.MAP().get(key);
		if (bb == null) {
			LOG.err(key);
			return false;
		}
		for (Boostable b : bb) {
			
			BValue v = new BValue.BValuePlayerOnly() {
				
				@Override
				public double vGet(FactionNPC f) {
					// TODO Auto-generated method stub
					return 0;
				}
				
				@Override
				public double vGet(Player f) {
					return 1.0;
				}
				
				@Override
				public double vGet(PopTime t) {
					if (b == BOOSTABLES.BEHAVIOUR().HAPPI) {
						if (t.pop.cl == HCLASSES.CITIZEN() && STATS.POP().POP.data(HCLASSES.CITIZEN()).get(t.pop.race) == 0)
							return 0;
					}
					return 1;
				}
			};
			

			
			Booster bo = new BoosterValue(v, new BSourceInfo(¤¤name, UI.icons().s.alert), am, isMul);
			
			bo.add(b);
			
		}
		return true;
	}
	
	
	@Override
	public void save(FilePutter file) {
		file.i(boosts.size());
		for (AAA a : boosts) {
			file.chars(a.key);
			file.d(a.am);
			file.bool(a.isMul);
		}
		startResources.save(file);
		file.i(startLevel);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		boosts.clear();
		int am = file.i();
		for (int i = 0; i < am; i++) {
			AAA a = new AAA(file.chars(), file.d(), file.bool());
			if (add(a))
				boosts.add(a);
		}
		startResources.load(file);
		startLevel = file.i();
	}

	public void copy(PBonusSetting old) {
		boosts.clear();
		for (AAA a : old.boosts)
			add(a);
		for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
			startResources.set(RESOURCES.ALL().get(ri), old.startResources.get(ri));
		}
		startLevel = old.startLevel;
	}
	
	@Override
	public void clear() {
		boosts.clear();
	}

	private static class AAA {
		
		public final String key;
		public final double am;
		public final boolean isMul;
		
		AAA(String key, double am, boolean isMul){
			this.key = key;
			this.am = am;
			this.isMul = isMul;
		}
		
	}

	public void apply() {
		if (startLevel > 0)
			FACTIONS.player().level().set(startLevel);
		
		Flooder f = GUTIL.flooder();
		f.init(this);
		int ri = 0;
		
		
		f.pushSloppy(THRONE.coo(), 0);
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			if (!SETT.PATH().solidity.is(t) && SETT.THINGS().resources.get(t.x(), t.y()) == null) {
				
				while(ri < RESOURCES.ALL().size()) {
					int am = startResources.get(ri);
					if (am > 0) {
						
						if (am > 100)
							am = 100;
						startResources.inc(RESOURCES.ALL().get(ri), -am);
						SETT.THINGS().resources.create(t, RESOURCES.ALL().get(ri), am);
						break;
					}else {
						ri++;
					}
				}
				
				if (ri >= RESOURCES.ALL().size())
					break;
				
				for (DIR d : DIR.ORTHO) {
					if (!SETT.PATH().solidity.is(t, d))
						f.pushSmaller(t, d, t.getValue()+1);
				}
				
			}
			
		}
		f.done();
		
	}
	
}
