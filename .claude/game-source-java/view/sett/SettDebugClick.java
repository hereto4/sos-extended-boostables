package view.sett;

import game.GameDisposable;
import snake2d.util.sets.LinkedList;

public abstract class SettDebugClick {
	
	
	
	static LinkedList<SettDebugClick> all = new LinkedList<>();
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
			}
		};
	}
	
	public abstract boolean debug(int px, int py, int tx, int ty);
	
	public void add() {
		all.add(this);
	}
	
	
}