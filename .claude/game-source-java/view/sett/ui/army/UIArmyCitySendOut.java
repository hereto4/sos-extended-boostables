package view.sett.ui.army;

import game.GAME;
import game.battle.div.Div;
import init.constant.Config;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.text.Dic;
import view.main.VIEW;
import view.ui.div.UIDivCardSett;
import view.ui.div.UIDivCardWorld;
import world.army.AD;
import world.entity.army.WArmy;

public class UIArmyCitySendOut extends GuiSection{


	private static int xs = 8;
	private final ArrayList<Card> cards = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private final ArrayList<Card> current = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private final ArrayList<Div> li = new ArrayList<Div>(Config.battle().DIVISIONS_PER_ARMY);
	private final UIDivCardSett card = VIEW.UI().div.settCivic;
	private WArmy army;
	
	public UIArmyCitySendOut(){
		
		for (Div d : GAME.ARMIES().player().divisions()) {
			cards.add(new Card(d));
		}
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				int am = CLAMP.i(current.size()+1, 0, Config.battle().DIVISIONS_PER_ARMY);
				return (int) Math.ceil((double)am/xs);
			}
		};
		
		bu.column(null, xs*card.width(), new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return row(ier);
			}
		});
		
		add(bu.create(4, false));
		
		GuiSection s = new GuiSection();
		GButt.ButtPanel but = new GButt.ButtPanel(Dic.¤¤confirm) {
			
			@Override
			protected void renAction() {
				activeSet(li.size() > 0 && Actions.sendProblem(li) == null);
			}
			
			@Override
			protected void clickA() {
				
				for (Card c : current) {
					if (army.divs().canAdd() && c.selectedIs() && c.div().info.men() > 0 && AD.cityDivs().attachedArmy(c.div()) == null) {
						AD.cityDivs().attach(army, c.div());
					}
				}
				
				VIEW.inters().popup.close();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Actions.hoverSendOutProblem(li, text);
				super.hoverInfoGet(text);
			}
			
		};
		s.add(but);
		but = new GButt.ButtPanel(UI.icons().m.fast_forw) {
			
			@Override
			protected void clickA() {
				SETT.BATTLE().info.sendOutWithoutTraining(!SETT.BATTLE().info.sendOutWithoutTraining());
			}

			@Override
			protected void renAction() {
				selectedSet(SETT.BATTLE().info.sendOutWithoutTraining());
			}
			

		
			
		};
		but.hoverInfoSet(Dic.¤¤SendOutArmyToggleD);
		s.addRightC(0, but);
		
		addRelBody(16, DIR.S, s);
		
		
	}
	
	public void init(WArmy a) {
		this.army = a;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		init();
		if (!VIEW.world().isActive())
			VIEW.inters().popup.close();
		super.render(r, ds);
	}
	
	private void init() {
		current.clearSloppy();
		li.clearSloppy();
		for (Card c : cards) {
			
			
			if (c.div().info.men() > 0 && AD.cityDivs().attachedArmy(c.div()) == null) {
				current.add(c);
				
				if (c.selectedIs()) {
					li.add(c.div());
				}
			}else {
				c.selectedSet(false);
			}
		}
	}
	
	private RENDEROBJ row(GETTER<Integer> ier) {
		GuiSection ss = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				int x1 = body().x1();
				int y1 = body().y1();
				clear();
				for (int i = 0; i < xs; i++) {
					int k = ier.get()*xs + i;
					if (k >= current.size()){
						break;
					}else {
						addRightC(0, current.get(k));
					}
				}
				body().moveX1Y1(x1, y1);
				body().setWidth(card.width()*xs);
				body().setHeight(card.height());
				super.render(r, ds);
			}
			
		};
		ss.body().setWidth(card.width()*xs);
		ss.body().setHeight(card.height());
		return ss;
	}
	
	private class Card extends ClickableAbs {

		private final int di;
		
		Card(Div div){
			
			super(card.width(), card.height());
			di = div.indexArmy();
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isActive =  UIDivCardWorld.supplyError(div()) == null;
			card.render(r, body.x1(), body.y1(), 1, div(), isActive, isSelected, isHovered);
		}
		
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			VIEW.UI().div.settCivic.hover(text, div());
			
		}
		
		@Override
		protected void clickA() {
			selectedSet(!selectedIs());
		}
		
		public Div div() {
			return GAME.ARMIES().player().ordered().get(di);
		}
		
		
	}
	
}
