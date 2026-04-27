package view.ui.raider;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.raiding.RaidingMap.RaidRegion;
import init.sprite.UI.UI;
import init.type.POP_CL;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.AD;
import world.entity.army.WArmy;
import world.region.RD;

final class Info extends GuiSection{

	private static CharSequence ¤¤defences = "Defence";
	private static CharSequence ¤¤defencesD = "The power of forces defending your city. This deters raiders.";
	
	private static CharSequence ¤¤armiesD = "Additional armies encamped near your city.";
	private static CharSequence ¤¤armiesD2 = "Armies encamped in your realm.";
	private static CharSequence ¤¤suprise = "Possible surprise attack";
	
	private static CharSequence ¤¤Ransom = "Potential Ransom";
	private static CharSequence ¤¤RansomD = "The potential ransom a raider sees fit to extort you with. Based on population and credits, and lowered by raid security.";
	
	private static CharSequence ¤¤entry = "Attack Route";
	private static CharSequence ¤¤entryA = "Surprise!";
	private static CharSequence ¤¤entryB = "Realm";
	private static CharSequence ¤¤entryAD = "Your capital is surrounded by regions where raiders can sneak in and surprise attack you. This makes them take more risk, and as a result, they perceive your deterrent power as low.";
	private static CharSequence ¤¤entryBD = "Raiders can't surprise attack your city directly. This is good. They must pass through your regions. This will buy you a lot of time if they choose to attack. Your regional defences and armies can deter raiders as well.";

	
	static {
		D.ts(Info.class);
	}

	
	public Info(){
		
		
		int gi = 0;
		int mx = 180;
		
		addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)GAME.raiders().util.playerPow());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤defencesD);
				
				b.NL(16);
				b.textLL(Dic.¤¤garrison);
				b.NL();
				b.add(GFORMAT.iIncr(b.text(), (int)RD.MILITARY().power.getD(FACTIONS.player().capitolRegion())));
				b.NL(8);
				
				double pow = 0;
				double pow2 = 0;
				for (WArmy a : FACTIONS.player().armies().all()) {
					if (a.region() == FACTIONS.player().capitolRegion())
						pow += AD.power().get(a);
					if (a.region() != null && a.region().faction() == FACTIONS.player())
						pow2 += AD.power().get(a);
				}
				
				b.textLL(¤¤armiesD);
				b.NL();
				b.add(GFORMAT.iIncr(b.text(), (int)pow));
				b.NL(8);
				
				b.textLL(¤¤armiesD2);
				b.NL();
				b.add(GFORMAT.iIncr(b.text(), (int)pow2));
				b.NL(8);
				
				b.textLL(¤¤suprise);
				b.NL();
				if (GAME.raiders().entry.get(FACTIONS.player().capitolRegion()).points() > 0) {
					int pp = (int) (0.75*(RD.MILITARY().power.getD(FACTIONS.player().capitolRegion()) + pow));
					pp += pow2;
					b.add(GFORMAT.iIncr(b.text(), -pp));
				}else {
					b.add(GFORMAT.iIncr(b.text(), (int)0));
				}
				b.NL(8);
				
				
			};
			
			
			
		}.hv(¤¤defences), gi++, 8, mx, 64, DIR.N);
		
		addGridD(new GStat() {
			
			@Override
			public void update(GText text) {

				if (GAME.raiders().entry.get(FACTIONS.player().capitolRegion()).points() > 0) {
					text.errorify().add(¤¤entryA);
				}else {
					text.normalify().add(¤¤entryB);
				}
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				
				if (GAME.raiders().entry.get(FACTIONS.player().capitolRegion()).points() > 0) {
					b.text(¤¤entryAD);
				}else {
					b.text(¤¤entryBD);
					b.NL(8);
					for (RaidRegion r : GAME.raiders().entry.entryRegions()) {
						b.textLL(r.r().info.name());
						b.tab(6);
						b.add(UI.icons().s.sword);
						b.add(GFORMAT.iIncr(b.text(), (int)RD.MILITARY().power.getD(r.r())));
						b.NL();
					}
					
					b.NL(8);
					
					for (WArmy a : FACTIONS.player().armies().all()) {
						if (a.region() != null && a.region().faction() == FACTIONS.player()) {
							b.textLL(a.name);
							b.tab(6);
							b.add(UI.icons().s.sword);
							b.add(GFORMAT.iIncr(b.text(), (int)AD.power().get(a)));
							b.NL();
						}
							
					}
				}
				
			};
			
		}.hv(¤¤entry), gi++, 8, mx, 64, DIR.N);

		addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				double d = BOOSTABLES.CIVICS().RAID_SECURITY.get(POP_CL.clP());
				GFORMAT.f1(text, d);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				BOOSTABLES.CIVICS().RAID_SECURITY.hover(b, POP_CL.clP(), true);
			};
			
		}.hv(BOOSTABLES.CIVICS().RAID_SECURITY.name), gi++, 8, mx, 64, DIR.N);
		
		addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)GAME.raiders().util.ransomCurrent());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤RansomD);
			};
			
		}.hv(¤¤Ransom), gi++, 8, mx, 64, DIR.N);
		


		
		
		
		
	}
	

	
	

	
}
