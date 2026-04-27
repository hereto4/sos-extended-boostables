package game.faction.diplomacy;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.IDebugPanel;
import view.main.VIEW;

class Debug {
	
	private ArrayList<Faction> team1 = new ArrayList<Faction>(FACTIONS.MAX());
	private ArrayList<Faction> team2 = new ArrayList<Faction>(FACTIONS.MAX());
	
	Debug(){
		
		ArrayListGrower<Butt> butts = new ArrayListGrower<>();
		for (Faction f : FACTIONS.all()) {
			butts.add(new Butt(f));
		}
		
		GuiSection s = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				team1.clearSloppy();
				team2.clearSloppy();
				for (Butt b : butts) {
					if (b.fa.isActive() && b.team == 1) {
						team1.add(b.fa);
					}
					if (b.fa.isActive() && b.team == 2) {
						team2.add(b.fa);
					}
				}
				super.render(r, ds);
			}
		};
		
		int ii = 0;
		for (Butt b : butts) {
			s.addGrid(b, ii++, 12, 0, 0);
		}
		
		s.addRelBody(8, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f0(text, DIP.WAR().offenseValue(team1, team2));
			}
		}.hh(UI.icons().s.sword));
		
		IDebugPanel.add("WAR VALUES", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().popup.show(s, null);
			}
		});
		
	}
	
	
	private class Butt extends CLICKABLE.ClickableAbs{

		int team = 0;
		final Faction fa;
		
		Butt(Faction f){
			super(Icon.L, Icon.L);
			this.fa = f;
			
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isActive = fa.isActive();
			if (!isActive)
				return;
			OPACITY.O50.bind();
			COLOR.UNIQUE.get(team).render(r, body);
			OPACITY.unbind();
			fa.banner().MEDIUM.render(r, body);

		}	
		
		@Override
		protected void clickA() {
			team ++;
			team %= 3;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (!fa.isActive())
				return;
			GBox b = (GBox) text;
			b.text(fa.name);
			b.NL();
			b.textL(Dic.¤¤Enemies);
			for (Faction f : DIP.WAR().all(fa))
				b.text(f.name);
			b.NL();
			b.textL(Dic.¤¤All);
			for (Faction f : DIP.ALLY().all(fa))
				b.text(f.name);
			b.NL();
			
			b.add(UI.icons().s.sword);
			b.add(b.text().add(DIP.WAR().offenseValue(fa)));
			b.NL();
			
			b.add(UI.icons().s.soso);
			b.add(b.text().add(DIP.WAR().distress(fa)));
			b.NL();
		}
		
	}
	
}
