package settlement.path;

import game.GameDisposable;
import snake2d.util.sets.ArrayList;

public abstract class AvailabilityListener {

	private static final ArrayList<AvailabilityListener> listeners = new ArrayList<AvailabilityListener>(20);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				listeners.clear();
			}
		};
	}
	
	private static boolean listening = true;
	
	public static void listenAll(boolean listening) {
		AvailabilityListener.listening = listening;
	}
	
	public static void notify(int tx, int ty, AVAILABILITY a, AVAILABILITY old, boolean playerChange) {
		if (!listening)
			return;
		for (AvailabilityListener l : listeners)
			l.changed(tx, ty, a, old, playerChange);
	}
	
	
	protected AvailabilityListener() {
		listeners.add(this);
	}
	
	protected abstract void changed(int tx, int ty, AVAILABILITY a, AVAILABILITY old, boolean playerChange);
	
}
