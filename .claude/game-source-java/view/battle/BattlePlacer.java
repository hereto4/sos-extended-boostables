package view.battle;

import game.GAME;
import game.battle.div.Div;
import game.battle.thread.order.BattleOrderTask;
import init.constant.C;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.keyboard.KEYS;
import view.keyboard.Key;
import view.subview.GameWindow;

public final class BattlePlacer {

	private final GameWindow w;
	final DivSelection s;
	private final BattlePlacerRenderer ren;
	
	
	Mode current;
	private final Mode selectMore;
	private final Mode position;
	private final Mode spin;
	private final BattlePlacerAttack attack;

	private final Action action = new Action();
	private final BattleOrderTask task = new BattleOrderTask();
	
	public BattlePlacer(GameWindow w, DivSelection s) {
		this.w = w;
		this.s = s;
		selectMore = new BattlePlacerSelect(w, s, action);
		position = new BattlePlacerPlace(w, s, action);
		spin = new BattlePlacerSpin(w, s, action);
		attack = new BattlePlacerAttack(w, s, action);
		current = selectMore;
		ren = new BattlePlacerRenderer(this);
	}
	
	public void click(MButt butt) {
		
		if (butt == MButt.LEFT) {
			action.clicked = true;
			action.start.set(w.pixel());
		}else if (butt == MButt.RIGHT){
			if (action.clicked) {
				action.clicked = false;
			}else {
				s.clear();
			}
		}
	}
	
	private final Key[] arrowsKeys = new Key[] {
			KEYS.BATTLE().UP,
			KEYS.BATTLE().DOWN,
			KEYS.BATTLE().LEFT,
			KEYS.BATTLE().RIGHT
	};
	
	private final DIR[] arrowsDIRS = new DIR[] {
			DIR.N,DIR.S,DIR.W,DIR.E
	};
	
	private final int[] arrowPressed = new int[4];
	
	
	void keypush() {
		
		int dx = 0;
		int dy = 0;
		
		if (KEYS.BATTLE().SELECT_ALL.consumeClick()) {
			action.clicked = false;
			s.clear();
			boolean allSelected = true;
			for (Div d : GAME.ARMIES().player().divisions()) {
				if (d.menNrOf() > 0) {
					allSelected &= s.selected(d);
					s.select(d);
				}
			}
			if (allSelected) {
				for (ArtilleryInstance ins : s.artillery.all()) {
					s.artillery.select(ins);
				}
			}
		}
		
		if (KEYS.MAIN().BACKSPACE.consumeClick()) {
			action.clicked = false;
			for (Div d : s.selection()) {
				task.stop(d);
				d.order().task.set(task);
			}
			for (ArtilleryInstance ins : s.artillery.all()) {
				ins.clearTarget();
			}
		}
		
		for (int i = 0; i < arrowsKeys.length; i++) {
			if (arrowsKeys[i].consumeClick()) {
				dx += arrowsDIRS[i].x()*C.TILE_SIZE;
				dy += arrowsDIRS[i].y()*C.TILE_SIZE;
			}else if (arrowsKeys[i].isPressed()) {
				if (arrowPressed[i]++ > 60) {
					dx += arrowsDIRS[i].x()*(arrowPressed[i]-60)*8;
					dy += arrowsDIRS[i].y()*(arrowPressed[i]-60)*8;
					arrowPressed[i] = 60;
				}
			}else {
				arrowPressed[i] = 0;
			}
			
			
		}
		
		if (KEYS.BATTLE().UP.consumeClick()) {
			dy = -C.TILE_SIZE;
		}if (KEYS.BATTLE().DOWN.consumeClick()) {
			dy = C.TILE_SIZE;
		}
		
		if (KEYS.BATTLE().LEFT.consumeClick())
			dx = -C.TILE_SIZE;
		if (KEYS.BATTLE().RIGHT.consumeClick()) {
			dx = C.TILE_SIZE;
		}
		
		if (dx == 0 && dy == 0)
			return;
		action.clicked = false;
		for (Div d : s.selection()) {
			GAME.ARMIES().placer.deploy(d, dx, dy);
		}
		
	}
	
	private Mode getState() {
		if (s.allSelected() <= 0)
			return selectMore;
		if (attack.init()) {
			return attack;
		}
		if (s.allSelected() <= 0 || KEYS.MAIN().UNDO.isPressed()) {
			return selectMore;
		}else if(KEYS.MAIN().MOD.isPressed()) {
			return spin;
		}else {
			return position;
		}
	}
	
	public void update(boolean hovered) {
		
		current = getState();
		
		keypush();
		action.clickReleased = false;
		
		if (action.clicked && !MButt.LEFT.isDown()) {
			action.clicked = false;
			action.clickReleased = true;
		}
		

		ren.add(hovered);
		current.update(hovered);
		
	}
	
	public void hoverTimer(GBox text) {
		current.hoverTimer(text);
		
	}
	
	static abstract class Mode {
		
		abstract void update(boolean hovered);
		abstract void hoverTimer(GBox text);
		abstract void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds);
		
	}
	
	static final class Action {
		
		public Coo start = new Coo();
		public boolean clicked;
		public boolean clickReleased;
		
	}
	




	
}
