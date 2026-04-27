package settlement.thing;

import game.GameDisposable;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public interface DRAGGABLE extends BODY_HOLDER{

	public void drag(DIR d, int cx, int cy, int fromDist);
	public void drag(DIR d, int cx, int cy);
	public boolean canBeDragged();
	
	public abstract class DRAGGABLE_HOLDER{
		
		public final byte index;
		
		private static ArrayList<DRAGGABLE_HOLDER> all = new ArrayList<>(16);
		static {
			new GameDisposable() {
				@Override
				protected void dispose() {
					all.clear();
				}
			};
		}
		
		DRAGGABLE_HOLDER(){
			index = (byte) all.add(this);
		}
		
		public abstract DRAGGABLE draggable(short index);
		
		public static LIST<DRAGGABLE_HOLDER> all(){
			return all;
		}
		
	}
	
}
