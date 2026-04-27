package view.interrupter;

import init.constant.C;
import snake2d.util.gui.GuiSection;
import view.ui.top.UIPanelTop;

public class ISidePanel{

	protected GuiSection section;
	protected CharSequence title;
	public static final int M = 8*C.SG;	
	static final int Y1 = UIPanelTop.HEIGHT;
	static final int Y2 = Y1+C.SG*32+M;
	public static final int HEIGHT = C.HEIGHT()-Y2-M;
	ISidePanels last;
	
	
	public ISidePanel(GuiSection section){
		this.section = section;
	}
	
	public ISidePanel(){
		section = new GuiSection();
	}
	
	public GuiSection section() {
		return section;
	}
	
	public void titleSet(CharSequence title) {
		this.title = title;
	}
	
	public CharSequence title() {
		return this.title;
	}
	
	protected void update(float ds) {
		
	}
	
	public ISidePanels last() {
		return last;
	}
	
	protected void addAction() {
		
	}
	
	protected boolean back() {
		return false;
	}
	
}
