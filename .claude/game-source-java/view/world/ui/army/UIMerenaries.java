package view.world.ui.army;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import init.constant.Config;
import init.race.appearence.RPortrait;
import init.settings.S;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import world.army.AD;
import world.army.WDivMercenary;

class UIMerenaries {


	private static CharSequence ¤¤intro = "Captain {0}'s";
	
	static {
		D.ts(UIMerenaries.class);
	}
	
	private int max = AD.mercenaries().size();
	private final Card[] cards = new Card[AD.mercenaries().size()];
	private final ArrayList<Card> active = new ArrayList<>(cards.length);
	private GuiSection scards = new GuiSection();
	private final GuiSection section = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			arrange();
			super.render(r, ds);
			
		}
	};
	
	
	
	private static int xs = 10;
	private int width = RPortrait.P_WIDTH*2+20;
	private int height = RPortrait.P_HEIGHT*2+12+52-20;
	
	UIMerenaries() {
		
		for (int i = 0; i < AD.mercenaries().size(); i++) {
			Card c = new Card(i);
			cards[i] = c;
		}
		
		
		scards.body().setDim(xs*width, Math.ceil((double)max/xs)*height);
		
		section.add(scards);
		
		GuiSection bb = new GuiSection();
		
		bb.addRightC(8, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)FACTIONS.player().credits().credits());
			}
		}.hh(Dic.¤¤Currs));
		
		bb.addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				int co = cost();
				if (co > FACTIONS.player().credits().credits())
					text.errorify();
				else
					text.normalify();
				
				GFORMAT.iIncr(text, -co);
			}
		}.hh(Dic.¤¤Cost));
		
		bb.addRightC(64, new GButt.ButtPanel(Dic.¤¤Recruit) {
			
			@Override
			protected void renAction() {
				activeSet(Army.army.added() && Army.army.divs().canAdd() && cost() > 0 && cost() <= FACTIONS.player().credits().credits());
			}
			
			@Override
			protected void clickA() {
				for (Card c : active) {
					if (c.selectedIs() && Army.army.divs().canAdd()) {
						int cost = AD.mercenaries().signingCost(c.ii);
						if (cost < FACTIONS.player().credits().credits()) {
							c.div.reassign(Army.army);
							GAME.player().credits().inc(-AD.mercenaries().signingCost(c.ii), CTYPE.MERCINARIES);
						}
					}
				}
				VIEW.inters().popup.close();
				super.clickA();
			}
			
		});
		
		if (S.get().developer) {
			bb.addRightC(16, new GButt.ButtPanel("shuffle") {
				
				@Override
				protected void clickA() {
					AD.mercenaries().debug();
				}
				
			});
		}
		
		section.addRelBody(8, DIR.S, bb);
		
	}
	
	public void arrange() {
		active.clearSloppy();
		int max = AD.mercenaries().max();   
		for (int i = 0; i < max; i++) {
			WDivMercenary d = AD.mercenaries().get(i);
			if (d.army() != null)
				continue;
			if (d.men() == 0)
				continue;
			if (d.disbanded())
				continue;
			active.add(cards[i]);
		
			
		}

		int x1 = scards.body().x1();
		int y1 = scards.body().y1();
		scards.clear();
		for (int i = 0; i < active.size(); i++) {
			Card c = active.get(i);
			scards.add(c, (i%xs)*c.body().width(), (i/xs)*(c.body().height()));
			
		}
		scards.body().moveX1Y1(x1, y1);
		
	}
	
	private int cost() {
		int am = 0;
		for (Card c : active) {
			if (c.selectedIs()) {
				am += AD.mercenaries().signingCost(c.ii);
			}
		}
		return am;
	}

	public GuiSection get() {
		for (Card c : cards)
			c.selectedSet(false);
		arrange();
		return section;
	}
	
	private GText tmp = new GText(UI.FONT().S, 8);
	
	private class Card extends ClickableAbs{

		private final int ii;
		private final WDivMercenary div;
		
		Card(int ii){
			this.ii = ii;
			div = AD.mercenaries().get(ii);
			body.setDim(width, height);
		}
		
		private SPRITE title = new SPRITE.Imp(400, 16*2+8) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				Str.TMP.clear().add(¤¤intro);
				Str.TMP.insert(0, STATS.APPEARANCE().name(div.cheif()));
				GCOLOR.T().H1.bind();
				UI.FONT().H2.renderCX(r, X1+(X2-X1)/2, Y1, Str.TMP);
				GCOLOR.T().H2.bind();
				UI.FONT().M.renderCX(r, X1+(X2-X1)/2, Y1+18, div.name());
			}
		};

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

			isActive = AD.mercenaries().signingCost(ii) <= FACTIONS.player().credits().credits();
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			GButt.ButtPanel.renderFrame(r, body);

			STATS.APPEARANCE().portraitRender(r, div.cheif(), body.x1()+10, body.y1(), 2);
			div.cheif().race().appearance().crown.merc().getC(STATS.RAN().get(div.cheif(), 9)).renderScaled(r, body.x1()+10, body.y1()+8, 2);
			
			div.banner().renderSymbol(r, body.x1()+4, body.y1()+8, 1);
			
			//VIEW.UI().battle.cardW.render(r, body().cX()-UIDivCardW.WIDTH/2, body().y1()+6, div, isActive, false, false);
			
			int y2 = body.y1()+RPortrait.P_HEIGHT*2+4;
			GMeter.render(r, GMeter.C_GRAY, (double)div.men()/Config.battle().MEN_PER_DIVISION, body.x1()+6, body.x2()-6, y2,  y2+8);
			
			y2 += 10;
			{
				int tot = 0;
				for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
					tot += Math.ceil(div.equipI(e)/3);
				}
				
				int x1 = body.cX() - tot*10/2;
				
				for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
					int am =  (int) Math.ceil(div.equipI(e)/3);
					for (int i = 0; i < am; i++) {
						e.resource.icon().small.render(r, x1, y2);
						x1 += 10;
					}
				}
				
			}
			
			y2+= 12;
			
			tmp.clear();
			GFORMAT.i(tmp, AD.mercenaries().signingCost(ii));
			tmp.adjustWidth();
			if (cost() + AD.mercenaries().signingCost(ii) > FACTIONS.player().credits().credits())
				tmp.errorify();
			else
				tmp.normalify();
			tmp.adjustWidth();
			OPACITY.O50.bind();
			int x1 = body.cX()-tmp.width()/2;
			COLOR.BLACK.render(r, x1-1, x1 + tmp.width()+2, y2-1, y2+18);
			OPACITY.unbind();
			tmp.render(r, x1, y2);
			
			VIEW.UI().div.renderPower(body.x2()-18, body.y1()+6, r, div.provess());
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.add(title);
			text.NL();
			VIEW.UI().div.world.hover(div, text);
			text.title(null);
		}
		
		@Override
		protected void clickA() {
			selectedToggle();
		}
		
		
	}
	
}
