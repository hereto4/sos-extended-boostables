package view.interrupter;

import java.util.Iterator;

import init.constant.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayList;
import util.gui.misc.GBox;
import view.tool.ToolManager;
import view.tool.ToolPlacer;

public class InterManager {

	private final Collection inters = new Collection();
	private Interrupter hovered = null;
	private Rec viewPort = new Rec();

	
	public InterManager() {

	}

	public void add(Interrupter i) {
		
		if (i.addManager != null)
			throw new RuntimeException("" + i);
		
		for (Interrupter in : inters)
			in.otherAdd(i);
		
		if (i.desturbingfuck) {
			for (Interrupter in : inters) {
				if ((!in.isPersistent() && !in.pinned()) && in != i)
					in.hide();
			}
		}
		if (i.last()) {
			inters.add(i);
		}else {
			inters.addFirst(i);
		}
		
		i.addManager = this;
	}
	
	public void disturb() {
		for (Interrupter in : inters) {
			if (!in.pinned() && !in.isPersistent())
				in.hide();
		}
	}

	public void remove(Interrupter interrupter) {
		inters.remove(interrupter);
		interrupter.deactivateAction();
		if (hovered == interrupter) {
			hovered = null;
		}
		interrupter.addManager = null;
	}

	/**
	 * 
	 * @param mouseStillTime
	 * @return false if should hover next
	 */
	public boolean hoverTimer(double mouseStillTime, GBox text) {

		if (hovered != null) {
			hovered.hoverTimer(text);
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param ds
	 * @return false if there should be no more rendering
	 */
	public boolean render(Renderer r, float ds) {
		r.newLayer(true, 0);
		for (Interrupter i : inters) {
			if (!i.render(r, ds)) {
				r.newLayer(true, 0);
				return false;
			}
			r.newLayer(true, 0);
		}
		return true;
	}

	/**
	 * 
	 * @param button
	 * @return false if nothing more should be clicked
	 */
	public boolean click(MButt button) {
		
		for (Interrupter i : inters) {
			if (i == hovered) {
				hovered.mouseClick(button);
				return false;
			}
				
			if (i.otherClick(button))
				return false;
		}
		
		return true;
	}

	/**
	 * 
	 * @return if the next should update
	 */
	public boolean update(float ds) {
		boolean ret = true;
		for (Interrupter i : inters) {
			if (!i.update(ds))
				ret = false;
		}
		for (Interrupter i : inters) {
			if (!i.DoWhateverAndallowOthersToDoWhatever())
				break;
		}
		
		return ret;
		
	}
	
	public void afterTick() {
		for (Interrupter i : inters) {
			i.afterTick();
		}
		viewPort.set(C.DIM());
	}

	/**
	 * 
	 * @param mCoo
	 * @param mouseHasMoved
	 * @return false if shouldn't hover
	 */
	public boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		hovered = null;
		for (Interrupter i : inters) {
			if (i.hover(mCoo, mouseHasMoved)) {
				hovered = i;
				return false;
			}
		}
		return true;
	}

	public void clear() {
		for (Interrupter i : inters) {
			if (!i.pinned()) {
				i.hide();
				if (hovered == i)
					hovered = null;
			}
		}
	}

	public boolean isHovered() {
		return hovered != null;
	}

	public Rec viewPort() {
		return viewPort;
	}

	private static class Collection implements Iterable<Interrupter>, Iterator<Interrupter>{

		private int i;
		private final ArrayList<Interrupter> all = new ArrayList<>(64);
		
		@Override
		public boolean hasNext() {
			if (i < 0)
				i = 0;
			return i < all.size();
		}

		@Override
		public Interrupter next() {
			if (i < 0)
				i = 0;
			return all.get(i++);
		}

		@Override
		public Iterator<Interrupter> iterator() {
			i = 0;
			return this;
		}
		
		void add(Interrupter i) {
			all.add(i);
			
		}
		
		void addFirst(Interrupter i) {
			all.insert(0, i);
			if (this.i > 0)
				this.i--;
			
		}
		
		void remove(Interrupter i) {
			int index = all.removeOrdered(i);
			if (index < 0)
				throw new RuntimeException();
			if (index <= this.i)
				this.i--;
		}
		
	}

	public boolean isGoodTimeToSave() {
		for (Interrupter i : inters) {
			if (i instanceof ToolManager) {
				ToolManager t = (ToolManager) i;
				if (t.current() instanceof ToolPlacer)
					return false;
			}
		}
		return true;
	}

	public boolean canSave() {
		for (Interrupter i : inters) {
			if (!i.canSave())
				return false;
		}
		return true;
	}
	
}
