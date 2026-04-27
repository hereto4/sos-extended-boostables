package init.race.bio;

import java.io.IOException;

import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import settlement.stats.muls.StatsMultipliers.StatMultiplier;
import settlement.stats.stat.STAT;
import settlement.stats.util.StatsJson;
import snake2d.util.MATH;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import util.text.Dic;

final class BioOpinionData {

	private final CharSequence[] funnies;
	private final CharSequence[] full;
	private final Opinion[] all = new Opinion[STATS.all().size()];
	private final Str stmp = new Str(256);
	
	private final CharSequence[][] titles;
	
	BioOpinionData(Json org, Json spe) throws IOException{
		
		titles = new CharSequence[][] {
			tt("HAPPY", org, spe),
			tt("HAPPY_SOSO", org, spe),
			tt("HAPPY_NO", org, spe)
		};
		
		Opinion def = new Opinion();
		for (int i = 0; i < all.length; i++)
			if (all[i] == null)
				all[i] = def;
		for (STAT s : STATS.all()) {
			all[s.index()] = new Opinion().setMore(s.info().defOpinion.more).setLess(s.info().defOpinion.less);
		}
		
		
		funnies = tt("FUNNY", org, spe);
		full = tt("NOTHING", org, spe);
		
		
		ArrayList<Json> js = new ArrayList<Json>(2);
		js.add(org);
		if (spe != null)
			js.add(spe);
		
		for (Json jj : js) {
			Json j = jj.json("STATS_MORE");
			new StatsJson(j) {
				
				@Override
				public void doWithTheJson(STAT s, Json j, String key) {
					CharSequence[] tt = BioLine.insert.check(j.texts(key));
					if (tt.length == 0)
						all[s.index()].setMore(BioLine.insert.check(j.texts(key)));
					all[s.index()].setMore(BioLine.insert.check(j.texts(key)));
				}
				
				@Override
				public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					
				}
			};
			j = jj.json("STATS_LESS");
			new StatsJson(j) {
				
				@Override
				public void doWithTheJson(STAT s, Json j, String key) {
					all[s.index()].setLess(BioLine.insert.check(j.texts(key)));
				}
				
				@Override
				public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					
				}
			};
		}
		
	}
	
	private CharSequence[] tt(String key, Json org, Json spe) {
		if (spe != null && spe.has(key))
			return BioLine.insert.check(spe.texts(key));
		return BioLine.insert.check(org.textsTry(key));
	}
	
	public CharSequence get(STAT s, Humanoid a, long ran) {

		if (s.standing().definition(a.race()).get(a.indu().clas()).from > s.standing().definition(a.race()).get(a.indu().clas()).to) {
			return less(s, ran, a);
		}else {
			return more(s, ran, a);
		}
	}
	
	public CharSequence title(Humanoid h, double value) {
		if (value > 0.95)
			return get(h, titles[0], STATS.RAN().getL(h.indu(), 0));
		if (value > 0.8)
			return get(h, titles[1], STATS.RAN().getL(h.indu(), 0));
		return get(h, titles[2], STATS.RAN().getL(h.indu(), 0));
	}
	
	public CharSequence funny(long ran) {
		return funnies[MATH.mod((int) ran, funnies.length)];
	}
	
	public CharSequence full(long ran) {
		return full[MATH.mod((int) ran, full.length)];
	}
	
	private CharSequence get(Humanoid i, CharSequence[] r, long ran) {
		if (r.length == 0)
			return Dic.empty;
		stmp.clear().add(r[MATH.mod((int) ran, r.length)]);
		BioLine.insert.set(stmp, i);
		return stmp;
	}
	
	private CharSequence more(STAT stat, long ran, Humanoid a) {
		Opinion i = all[stat.index()];
		if (i.more.length == 0)
			return Dic.empty;
		CharSequence s = i.more[MATH.mod((int)ran, i.more.length)];
		stmp.clear().add(s);
		i.insert(stmp, stat, a);
		return stmp;
	}
	
	private CharSequence less(STAT stat, long ran, Humanoid a) {
		
		
		
		Opinion i = all[stat.index()];
		if (i.less.length == 0)
			return Dic.empty;
		CharSequence s = i.less[MATH.mod((int)ran, i.less.length)];
		stmp.clear().add(s);
		i.insert(stmp, stat, a);
		return stmp;
	}

	
}
