package view.ui.tech;

import java.util.Arrays;

import game.faction.FACTIONS;
import init.settings.S;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.tech.TECHS;
import init.tech.TechCost;
import init.tech.TechCurrency;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.main.VIEW;

final class Prompt {

	private static CharSequence ¤¤Unlock = "¤Do you wish to unlock the following:";
	private static CharSequence ¤¤Unlock2 = "¤For the cost of:";
	private static CharSequence ¤¤Forget = "¤Do you wish to forget the following technologies? 25% of the points deallocated will be frozen for some time.";
	
	private double renderS = VIEW.renderSecond()-60;
	private boolean[] checks = new boolean[TECHS.ALL().size()];
	private double checkFoget = VIEW.renderSecond();
	
	static {
		D.ts(Prompt.class);
	}
	
	private int[] costs = new int[TECHS.COSTS().size()];
	
	public void unlock(TECH tech){
		if (FACTIONS.player().tech().level(tech) == tech.levelMax)
			return;
		if (!FACTIONS.player().tech().canUnlockNext(tech) && !S.get().developer)
			return;
		
		for (TechCurrency c : TECHS.COSTS()) {
			costs[c.index] = FACTIONS.player().tech().costOfRequired(c, tech);
		}
		
		for (TechCost c : tech.costs) {
			costs[c.cu.index] += FACTIONS.player().tech.costLevelNext(c.amount, tech);
		}
		
		if (VIEW.renderSecond()>renderS) {
			Str s = Str.TMP;
			s.clear();
			s.add(¤¤Unlock);
			s.NL();
			Arrays.fill(checks, false);
			addUnlocks(tech, s);
			s.add(¤¤Unlock2);
			s.NL();
			for (TechCurrency c2 : TECHS.COSTS()) {
				if (costs[c2.index] > 0)
					s.add(c2.bo.name).s().add(costs[c2.index]);
				s.NL();
			}
			
			this.tech = tech;
			VIEW.inters().yesNo.activate(s, askUnlock, askNo, true);
			renderS = VIEW.renderSecond()+120;
			return;
		}
		
		
		
		pUnlock(tech, FACTIONS.player().tech().level(tech)+1);
		
			
	}
	
	private void addUnlocks(TECH tech, Str s) {
		if (checks[tech.index()])
			return;
		checks[tech.index()] = true;
		s.add(tech.name());
		s.NL();
		for (int i = 0; i < tech.requires().size(); i++) {
			TechRequirement r = tech.requires().get(i);
			if (FACTIONS.player().tech().level(r.tech)< r.level) {
				s.add(r.tech.name());
				if (r.tech.levelMax > 1)
					s.s().add(r.level);
				s.NL();
			}
		}
		
	}
	
	private void pUnlock(TECH tech, int level) {
		for (int i = 0; i < tech.requires().size(); i++) {
			TechRequirement r = tech.requires().get(i);
			if (FACTIONS.player().tech().level(r.tech)< r.level)
				pUnlock(r.tech, r.level);
			
		}
		FACTIONS.player().tech().levelSet(tech, level);
	}
	
	public void forget(TECH tech) {
		int l = FACTIONS.player().tech().level(tech);
		if (l == 0)
			return;
		
		int am = 1;
		Str s = Str.TMP;
		s.clear();
		s.add(¤¤Forget);
		s.NL();
		s.add(tech.name());
		s.NL();
		for (int ti = 0; ti < TECHS.ALL().size(); ti++) {
			TECH t = TECHS.ALL().get(ti);
			if (t != tech && t.requires(tech, l-1) && FACTIONS.player().tech().level(t) > 0) {
				
				s.add(t.name());
				s.NL();
				am++;
			}
		}
		this.tech = tech;
		
		if (am > 2 || VIEW.renderSecond()-checkFoget > 60) {
			VIEW.inters().yesNo.activate(s, askforget, askNo, true);
			checkFoget = VIEW.renderSecond();
		}else {
			askforget.exe();
		}
	}
	
	private TECH tech;
	
	private ACTION askforget = new ACTION() {
		
		@Override
		public void exe() {
			int l = FACTIONS.player().tech().level(tech);
			for (int ti = 0; ti < TECHS.ALL().size(); ti++) {
				TECH t = TECHS.ALL().get(ti);
				if (t != tech && t.requires(tech, l-1)) {
					FACTIONS.player().tech().levelSet(t, 0);
				}
				
			}
			FACTIONS.player().tech().levelSet(tech, l-1);
		}
	};
	
	private ACTION askUnlock = new ACTION() {
		
		@Override
		public void exe() {
			pUnlock(tech, FACTIONS.player().tech().level(tech)+1);
		}
	};
	
	private ACTION askNo = new ACTION() {
		
		@Override
		public void exe() {
			
		}
	};
	
}
