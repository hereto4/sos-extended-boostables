package view.sett.ui.minimap;

import init.constant.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.subview.GameWindow;

public class UIMinimapSett extends Interrupter {

	final UIMiniMapSettView view;
	final UIMinimapPanel map;
	private final UIMinimapPanelButts buttons;
	public final UIMinimapSettConfig config;
	
	public UIMinimapSett(InterManager i, int y1, GameWindow w, UIMinimapSettConfig config) {
		if (config == null)
			config = UIMinimapSettConfig.NORMAL;
		this.config = config;
		desturberSet().persistantSet().pin();
		view = new UIMiniMapSettView(this, i, w, config);

		map = new UIMinimapPanel(w, config);
		map.body().moveX2(C.WIDTH());
		map.body().moveY1(y1);
		
		buttons = new UIMinimapPanelButts(view, map, w);
		buttons.section.body().moveX2(C.WIDTH());
		buttons.section.body().moveY1(map.body().y2());
		
		
		
		
		
		
		update(0);
		show(i);
	}

	
	public int y2() {
		return buttons.section.body().y2();
	}
	
	public void add() {
		
	}
	
	public void open() {
		view.showMin();
	}
	
	public boolean openIs() {
		return view.isActivated();
	}

	
	@Override
	protected void hoverTimer(GBox text) {
		buttons.section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {

		map.render(r, ds);
		buttons.section.render(r, ds);
		
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			map.click();
			buttons.section.click();
		}
		
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {

		
		if (map.hover(mCoo) || buttons.section.hover(mCoo))
			return true;
		return false;
	}

	@Override
	protected boolean update(float ds) {	

		if (KEYS.MAIN().MINIMAP.consumeClick()) {
			view.show();
		}
		return true;
	}
	
	public UIMinimapPanelButts panel() {
		return buttons;
	}
	
	public static class Butt extends GButt.ButtPanel {

		public Butt(SPRITE icon) {
			super(icon);
			setDim(30, 26);
		}
		
	}

}
