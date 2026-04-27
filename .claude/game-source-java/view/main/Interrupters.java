package view.main;

import view.interrupter.IDebugPanel;
import view.interrupter.ILoadScreen;
import view.interrupter.IPopup;
import view.interrupter.IPromtScreen;
import view.interrupter.IPromtYesNO;
import view.interrupter.ITextInput;
import view.interrupter.InterGuisection;
import view.interrupter.InterManager;
import view.menu.IMenu;
import view.ui.message.Messages;

public class Interrupters {
	
	public final InterManager manager = new InterManager();
	public final IMenu menu = new IMenu(manager);
	public final ITextInput input = new ITextInput(manager);
	public final IPromtScreen fullScreen = new IPromtScreen(manager);
	public final IPromtYesNO yesNo = new IPromtYesNO(manager);
	public final IDebugPanel debugpanel;
//	public final ITmpPanel panelTmp = new ITmpPanel(manager);
	public final IMouseMessage mouseMessage = new IMouseMessage();
	public final InterGuisection section = new InterGuisection(manager);
	public final Messages messages;
	public final IPopup popup = new IPopup(manager);
	public final IPopup popup2 = new IPopup(manager);
	public final ILoadScreen load = new ILoadScreen(manager);
	
	
	
	public Interrupters(){
		messages = new Messages(manager);

		
		debugpanel = new IDebugPanel(manager);
	}
	
//	public InterManager getManager() {
//		return manager;
//	}
}