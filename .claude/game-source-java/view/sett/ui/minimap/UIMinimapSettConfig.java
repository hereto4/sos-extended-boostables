package view.sett.ui.minimap;

import init.sprite.SPRITES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.room.main.RoomBlueprintIns;
import settlement.stats.STATS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.gui.misc.GButt;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.subview.GameWindow;

public abstract class UIMinimapSettConfig{

	public final static COLOR colAnimal = new ColorImp(60,60,60);
	public final static COLOR colHostile = new ColorImp(200,10,10);
	public final static COLOR colHostileRout = new ColorImp(150,150,0);
	public final static COLOR colNormal = new ColorImp(0,50,255);
	public final static COLOR colMustered = new ColorImp(0,180,255);
	
	
	
	public abstract COLOR col(ENTITY e);
	
	public abstract boolean halfEnts();
	
	public abstract boolean room(RoomBlueprintIns<?> b);
	
	public abstract boolean renderGrowable();
	
	public abstract boolean renderMinable();
	
	public abstract boolean renderPack();
	
	public abstract OPACITY shade();
	
	public abstract boolean renderDivs();
	
	public void addButtons(GuiSection sec, GameWindow w, UIMinimapSett s) {
		CLICKABLE c;
		
		c = new GButt.ButtPanel(SPRITES.icons().m.plus) {
			@Override
			protected void clickA() {
				w.zoomInc(-1);
				if (w.zoomout() < 3) {
					VIEW.s().getWindow().centerAt(w.pixels().cX(), w.pixels().cY());
					s.view.hide();
				}
			}
		};
		sec.addRightC(0, c);
		
		
		
		c = new GButt.ButtPanel(SPRITES.icons().m.minus) {
			@Override
			protected void clickA() {
				w.zoomInc(1);
			}
			
			@Override
			protected void renAction() {
				activeSet(w.zoomout() < w.zoomoutmax());
			}
			
		};
		sec.addRightC(0, c);
	}
	
	public final static UIMinimapSettConfig NORMAL = new UIMinimapSettConfig() {
		
		@Override
		public COLOR col(ENTITY e) {
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				if (a.indu().hostile()) {
					if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 0)
						return colHostile;
					return colHostileRout;
				}else if (a.division() != null) {
					if (a.division().settings().mustering())
						return colMustered;
					return colNormal;
				}
				return colAnimal;
			}
			return null;
			
		}

		@Override
		public boolean halfEnts() {
			return false;
		}

		@Override
		public boolean room(RoomBlueprintIns<?> b) {
			return false;
		}

		@Override
		public boolean renderGrowable() {
			return false;
		}

		@Override
		public boolean renderMinable() {
			return false;
		}

		@Override
		public boolean renderPack() {
			return false;
		}

		@Override
		public OPACITY shade() {
			return OPACITY.O25;
		}

		@Override
		public boolean renderDivs() {
			return KEYS.BATTLE().SHOW_DIVISIONS.isPressed();
		}
	};
	
	public final static UIMinimapSettConfig ALL = new UIMinimapSettConfig() {
		
		@Override
		public COLOR col(ENTITY e) {
			return NORMAL.col(e);
			
		}

		@Override
		public boolean halfEnts() {
			return false;
		}

		@Override
		public boolean room(RoomBlueprintIns<?> b) {
			return false;
		}

		@Override
		public boolean renderGrowable() {
			return true;
		}

		@Override
		public boolean renderMinable() {
			return true;
		}

		@Override
		public boolean renderPack() {
			return true;
		}

		@Override
		public OPACITY shade() {
			return OPACITY.O25;
		}

		@Override
		public boolean renderDivs() {
			return KEYS.BATTLE().SHOW_DIVISIONS.isPressed();
		}
	};
	
	
}
