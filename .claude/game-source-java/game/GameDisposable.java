package game;

import snake2d.util.sets.ArrayList;

/**
 * Come and add yourself to the init phase of the game. If you've added yourself, you are
 * guaranteed to be disposed the next time the game is inited,
 * @author mail__000
 *
 */
public abstract class GameDisposable {

	private static ArrayList<GameDisposable> initers = new ArrayList<GameDisposable>(180);
	
	
	public GameDisposable() {
		initers.add(this);
	}
	
	protected abstract void dispose();
	
	static void disposeAll() {
		for (GameDisposable i : initers)
			i.dispose();
	}
	
//	public static class Counter extends GameDisposable{
//		
//		private int i;
//		
//		public int getNext() {
//			int j = i;
//			i++;
//			return j;
//		}
//		
//
//		@Override
//		protected void dispose() {
//			i = 0;
//		}
//		
//	}
	
}
