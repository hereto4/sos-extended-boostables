package view.ui.economy;

import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.faction.trade.TradeManager;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;
import world.region.RD;

final class Factions extends GuiSection{

	private ArrayList<FactionNPC> all = new ArrayList<FactionNPC>(FACTIONS.MAX());
	private static final int width = 96;
	private final GText t = new GText(UI.FONT().S, 16);
	
	Factions(int HEIGHT){
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return all.size();
			}
		};
		
		bu.column("", width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Row(ier);
			}
		});
		
		add(bu.createHeight(48*(HEIGHT/48), false));
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		all.clearSloppy();
		for (FactionNPC f : FACTIONS.NPCs()) {
			if (RD.DIST().reachable(f)) {
				all.add(f);
			}
		}
		for (FactionNPC f : FACTIONS.NPCs()) {
			if (!RD.DIST().reachable(f)) {
				all.add(f);
			}
		}
		
		super.render(r, ds);
	}
	
	private class Row extends ClickableAbs {

		private final GETTER<Integer> ier;
		
		Row(GETTER<Integer> ier){
			this.ier = ier;
			body.setDim(width, 48);
		}
		
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			GButt.ButtPanel.renderBG(r, RD.DIST().reachable(f()), DIP.get(f()).trades, isHovered, body);
			
			f().banner().BIG.renderCY(r, body.x1()+8, body.cY());
			
			t.clear();
			GFORMAT.percInv(t, ROPINIONS.tradeCost(f()));
			
			t.render(r, body().x2()-48, body.y1()+8);
			
			t.clear();
			GFORMAT.i(t, Math.round(TradeManager.toll(f())));
			
			t.render(r, body().x2()-48, body.y1()+8+18);
			
			GButt.ButtPanel.renderFrame(r, body);
			
			if (!RD.DIST().reachable(f())) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body, -1);
				OPACITY.unbind();
			}
			
		}
		
		FactionNPC f() {
			return all.get(ier.get());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			VIEW.world().UI.factions.hover(text, f());
			GBox b = (GBox) text;
			b.sep();
			
			b.add(UI.icons().s.wheel);
			b.textLL(Dic.¤¤Toll);
			b.tab(7);
			b.add(GFORMAT.f(b.text(), TradeManager.toll(f())));
			b.NL();
			
			b.add(UI.icons().s.money);
			b.textLL(Dic.¤¤CreditScore);
			b.tab(7);
			b.add(GFORMAT.percInc(b.text(), f().stockpile.creditScore()-1.0));
			b.NL();
			
			b.add(UI.icons().s.angry);
			b.textLL(Dic.¤¤Tariff);
			b.tab(7);
			b.add(GFORMAT.percInv(b.text(), ROPINIONS.tradeCost(f())));
			b.NL();
		}
		
		@Override
		protected void clickA() {
			VIEW.UI().manager.close();
			VIEW.world().UI.factions.open(f());
		}
	}
	
	
}
