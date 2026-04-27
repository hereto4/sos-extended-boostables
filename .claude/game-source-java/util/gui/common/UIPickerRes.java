package util.gui.common;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.Dic;

public abstract class UIPickerRes extends GuiSection{

	
	public UIPickerRes(){
		this(RESOURCES.ALL(), false);
	}
	
	public UIPickerRes(boolean includenull){
		this(RESOURCES.ALL(), includenull);
	}
	
	public UIPickerRes(LIST<RESOURCE> list, boolean includenull){
		int i = 0;
		if (includenull) {
			add(new Resbutt(null, i++));
		}
		for (RESOURCE r : list) {
			Resbutt rb = new Resbutt(r, i);
			rb.body.moveX1Y1(rb.body.width()*(i%8), rb.body.height()*(i/8));
			add(rb);
			i++;
		}
	}
	
	protected abstract RESOURCE getResource();
	protected abstract void select(RESOURCE r, int li);
	protected void hoverResource(RESOURCE r, GBox b) {
		b.title(r.name);
		b.text(r.desc);
		b.NL();
	}
	
	private class Resbutt extends GButt.ButtPanel{
		
		private final RESOURCE res;
		private int i;
		
		Resbutt(RESOURCE res, int i){
			super(res == null ? SPRITES.icons().m.cancel : res.icon());
			this.res = res;
			this.i = 0;
			pad(4, 4);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (res == null)
				text.text(Dic.¤¤cancel);
			else
				UIPickerRes.this.hoverResource(res, (GBox)text);
		}
		
		@Override
		protected void clickA() {
			select(res, i);
		}
		
		@Override
		protected void renAction() {
			selectedSet(UIPickerRes.this.getResource() == res);
		}
		
		
	}
	
}
