package view.world.ui.region;

import game.faction.FACTIONS;
import game.faction.FBanner;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import init.settings.S;
import init.sprite.UI.UI;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.text.Str;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

final class OtherHov {

	private final GETTER_IMP<Region> g = new GETTER_IMP<>();
	
//	private final SPRITE info = MiscBasics.info(g).asSprite();
//	private final SPRITE gar = MiscMore.garrison(g).asSprite();
//	
	
	private static CharSequence ¤¤Besieged = "¤Besieged!";
	
	static {
		D.ts(OtherHov.class);
	}
	
	void hover(Region reg, GUI_BOX box) {
		g.set(reg);
		Str.TMP.clear().add(reg.info.name());
		box.title(Str.TMP);
		GBox b = (GBox) box;
		
		if (reg.faction() == null) {
			b.add(FBanner.rebel.BIG);
			b.text(Dic.¤¤NoRuler);
			
		}else {
			
			FactionNPC f = (FactionNPC) reg.faction();
			b.add(f.banner().BIG);
			b.textLL(f.name);
			
		}
		
		b.tab(9);
		b.add(RD.RACES().visuals.cRace(reg).appearance().icon);
		b.add(GFORMAT.i(b.text(), RD.RACES().population.get(reg)));
		
		b.NL();
		b.add(UI.icons().s.sword);
		if (RD.OWNER().affiliation.get(g.get()) >= 0.5)
			b.add(GFORMAT.i(b.text(),RD.MILITARY().garrison.get(reg)));
		else
			b.add(b.text().add('?'));
		b.tab(3);
		b.add(UI.icons().s.flag);
		b.add(GFORMAT.perc(b.text(), RD.OWNER().affiliation.getD(reg)));
		b.tab(6);
		b.add(UI.icons().s.flags);
		b.add(GFORMAT.i(b.text(), FACTIONS.player().emissaries.assimilate.get(reg)));
		
		if (reg.faction() != null) {
			b.tab(9);
			FactionNPC f = (FactionNPC) reg.faction();
			if (DIP.WAR().is(FACTIONS.player(), reg.faction()))
				b.error(Dic.¤¤AtWar);
			else {
				b.add(UI.icons().s.heart);
				b.add(GFORMAT.perc(b.text(), ROPINIONS.peaceValue(f.court().king().roy())));
			}
		}
		
		hovSiege(reg, box);
		
		
		if (S.get().developer) {
			box.NL();
			b.add(b.text().add(1).s().add(FACTIONS.player().realm().regions()));
			b.NL();
			b.add(b.text().add(2).s().add(RD.DIST().reachable(reg)));
			b.NL();
			if (reg.faction() != null) {
				b.add(b.text().add(3).s().add(((FactionNPC)reg.faction()).sanctified));
			}	
		}
		
		
	}
	
	public static void hovSiege(Region reg, GUI_BOX box) {
		GBox b = (GBox) box;
		if (reg.besieged()) {
			b.NL(8);
			b.error(¤¤Besieged);
			b.NL();
			GText t = b.text();
			DicTime.setYearDay(t, WORLD.BATTLES().besigedTime(reg));
			b.add(t);
			b.NL();
			b.textL(Dic.¤¤Defences);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), RD.MILITARY().defensePower(reg)));
			b.NL();
			
			
		}
	}
	
}
