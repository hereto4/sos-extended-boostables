package util.gui.slider;

import init.constant.C;
import init.sprite.SPRITES;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import view.keyboard.KEYS;

public class GTarget extends GuiSection{

	private static CharSequence ¤¤Prev5 = "Previous (5)";
	private static CharSequence ¤¤Prev = "Previous";
	private static CharSequence ¤¤Next5 = "Next (5)";
	private static CharSequence ¤¤Next = "Next";
	
	static {
		D.ts(GTarget.class);
	}
	
	public GTarget(int width, boolean doubleNext, boolean horizontal, GStat stat, INTE target){
		this(width, (RENDEROBJ)null, doubleNext, horizontal, stat.r(DIR.C), target);
	}
	
	public GTarget(int width, boolean doubleNext, boolean horizontal, RENDEROBJ stat, INTE target){
		this(width, (RENDEROBJ)null, doubleNext, horizontal, stat, target);
	}
	
	public GTarget(int width, boolean doubleNext, boolean horizontal, INTE target){
		this(width, (RENDEROBJ)null, doubleNext, horizontal, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, target.get());
			}
		}.r(DIR.C), target);
	}
	
	public GTarget(int width, SPRITE label, boolean doubleNext, boolean horizontal, INTE target){
		this(width, new RENDEROBJ.Sprite(label), doubleNext, horizontal, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, target.get());
			}
		}.r(DIR.C), target);
	}
	
	public GTarget(int width, SPRITE label, boolean doubleNext, boolean horizontal, GStat stat, INTE target){
		this(width, new RENDEROBJ.Sprite(label), doubleNext, horizontal, stat.r(DIR.C), target);
	}
	
	public GTarget(int width, RENDEROBJ label, boolean doubleNext, boolean horizontal, GStat stat, INTE target){
		this(width, label, doubleNext, horizontal, stat.r(DIR.C), target);
	}
	
	public GTarget(int width, RENDEROBJ label, boolean doubleNext, boolean horizontal, RENDEROBJ stat, INTE target){

		if (doubleNext) {
			CLICKABLE c = new GButt.Glow(SPRITES.icons().s.minifierBig) {
				@Override
				protected void clickA() {
					target.inc(-5);
					if (KEYS.MAIN().MOD.isPressed())
						target.set(target.min());
				}
				
				@Override
				protected void renAction() {
					activeSet(GTarget.this.activeIs() && target.get() > target.min());
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					
					super.hoverInfoGet(text);
					text.NL();
					GAllocator.hov(text);
				}
				
			}.repetativeSet(true).hoverInfoSet(¤¤Prev5);
			addRightC(0, c); 
		}
		CLICKABLE c = new GButt.Glow(SPRITES.icons().s.minifier) {
			@Override
			protected void clickA() {
				target.inc(-1);
				if (KEYS.MAIN().MOD.isPressed())
					target.set(target.min());
			}
			
			@Override
			protected void renAction() {
				activeSet(GTarget.this.activeIs() && target.get() > target.min());
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				super.hoverInfoGet(text);
				text.NL();
				GAllocator.hov(text);
			}
			
		}.repetativeSet(true).hoverInfoSet(¤¤Prev);
		addRightC(0, c); 
		
		addRightC(width/2, stat);
		
		body().incrW(width/2);
		c = new GButt.Glow(SPRITES.icons().s.magnifier) {
			@Override
			protected void clickA() {
				target.inc(1);
				if (KEYS.MAIN().MOD.isPressed())
					target.set(target.max());
			}
			
			@Override
			protected void renAction() {
				activeSet(GTarget.this.activeIs() && target.get() < target.max());
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				super.hoverInfoGet(text);
				text.NL();
				GAllocator.hov(text);
			}
			
		}.repetativeSet(true).hoverInfoSet(¤¤Next);
		c.body().moveX1(body().x2()).moveCY(body().cY());
		add(c); 
		
		if (doubleNext) {
			c = new GButt.Glow(SPRITES.icons().s.magnifierBig) {
				@Override
				protected void clickA() {
					target.inc(5);
					if (KEYS.MAIN().MOD.isPressed())
						target.set(target.max());
				}
				
				@Override
				protected void renAction() {
					activeSet(GTarget.this.activeIs() &&  target.get() < target.max());
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					
					super.hoverInfoGet(text);
					text.NL();
					GAllocator.hov(text);
				}
				
			}.repetativeSet(true).hoverInfoSet(¤¤Next5);
			addRightC(0, c); 
		}
		
		if (label != null) {
		if (horizontal) {
			label.body().moveX1(-label.body().width()-C.SG*8);
			label.body().moveCY(body().cY());
		
		}else {
			label.body().moveCX(body().cX());
			label.body().moveY2(body().y1()-C.SG*2);
			
		}
		
		add(label);
		}
		
	}

	
	
	
	
}
