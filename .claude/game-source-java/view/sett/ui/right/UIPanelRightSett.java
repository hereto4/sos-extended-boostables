package view.sett.ui.right;

import java.io.IOException;

import init.constant.C;
import init.sprite.SPRITES;
import init.type.HCLASSES;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimapSett;
import view.subview.GameWindow;

public class UIPanelRightSett extends Interrupter implements SAVABLE{

	private static CharSequence ¤¤hotspots = "Hot-spots";
	private static CharSequence ¤¤minipanels = "Mini Panels";
	static {
		D.ts(UIPanelRightSett.class);
	}

	final UIMiniResources resources;
	final UIMiniHotSpots hs;
	final UIMiniRaces species;	
	private final Expansion[] all;
	private boolean[] visable;
	private int[] widths = new int[4];
	
	public UIPanelRightSett(UIMinimapSett top, InterManager i, GameWindow w) {
		desturberSet().persistantSet().pin();
		
		int y1 = top.y2();
		
		{
			
			this.resources = new UIMiniResources(1,y1);
			this.hs = new UIMiniHotSpots(2, y1, w);
			this.species = new UIMiniRaces(3, y1);
		}
		
		all = new Expansion[] {
			this.species,
			this.resources,
			this.hs,
		};
		visable = new boolean[] {
			false,
			false,
			false,
			false,
		};
		
		this.hs.visableSet(true);
		this.resources.visableSet(true);
		this.species.visableSet(true);
		
		
		
		update(0);
		show(i);
		
		top.panel().padd(makeButt());
	}

	private CLICKABLE makeButt() {
		GuiSection s = new GuiSection();
		s.addDownC(0, exp(species, HCLASSES.CITIZEN().names));
		s.addDownC(0, exp(resources, Dic.¤¤Resource));
		s.addDownC(0, exp(hs, ¤¤hotspots));
		
		
		CLICKABLE c = new UIMinimapSett.Butt(SPRITES.icons().s.menu) {
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(s, this);
			}
		}.hoverInfoSet(¤¤minipanels);
		return c;
	}
	
	@Override
	protected void hoverTimer(GBox text) {
		for (Expansion e : all) {
			e.hoverInfoGet(text);
		}
	}

	@Override
	protected boolean render(Renderer r, float ds) {

		int w = 0;
		for (Expansion e : all) {
			if (e.visableIs()) {
				w+= e.body().width();
				e.render(r, ds);
			}
			
		}
	
		if (w > 0)
			manager().viewPort().incrW(-w);
		
		return true;
	}




	@Override
	protected void mouseClick(MButt button) {
		hs.click();
		if (button == MButt.LEFT) {
			for (Expansion e : all) {
				e.click();
			}
		}
		
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {

		boolean h = false;
		for (Expansion e : all) {
			
			if (e.hover(mCoo))
				h = true;
		}
		
		return h;
	}

	@Override
	protected boolean update(float ds) {	
		boolean changed = false;
		int i = 0;
		for (RENDEROBJ r : all) {
			if (r.visableIs() != visable[i]) {
				changed = true;
				visable[i] = r.visableIs();
			}
			if (widths[i] != r.body().width()) {
				widths[i] = r.body().width();
				changed = true;
			}
			i++;
		}
		
		if (changed) {
			int x2 = C.WIDTH();
			for (i = all.length-1; i >= 0; i--) {
				if (all[i].visableIs()) {
					all[i].body().moveX2(x2);
					x2 = all[i].body().x1();
				}
			}
		}
		return true;
	}	

	@Override
	public void save(FilePutter file) {
		int i = 0;
		for (RENDEROBJ r : all) {
			i |= r.visableIs() ? 1 :0;
			i = i<<1;
		}
		file.i(i);
	}


	@Override
	public void load(FileGetter file) throws IOException {
		int i = file.i();
		int k = all.length;
		for (RENDEROBJ r : all) {
			r.visableSet(((i>>k)&1) == 1);
			k--;
		}
		
	}


	@Override
	public void clear() {
		for (int i = 0; i < visable.length; i++)
			visable[i] = true;
		hs.clear();
	}

	static abstract class Expansion extends GuiSection{

		
		Expansion(int index){
			
		}
		
	}

	private RENDEROBJ exp(Expansion s, CharSequence name) {
		if (s == null)
			throw new RuntimeException();
		CLICKABLE b = new GButt.ButtPanel(name) {
			@Override
			protected void clickA() {
				s.visableSet(!s.visableIs());
			};
			
			@Override
			protected void renAction() {
				selectedSet(s.visableIs());
			}
		}.setDim(140, 32);
		return b;
	}
	
}
