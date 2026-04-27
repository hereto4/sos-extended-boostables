package view.ui.goods;

import game.faction.Faction;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GInput;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.manage.IFullView;

public final class UIGoods extends IFullView {

	private static final int COLS = 2;
	private final ArrayList<Object> all = new ArrayList<Object>(RESOURCES.ALL().size()*2+1);
	
	public final Icon icon = SPRITES.icons().s.storage;
	private static CharSequence ¤¤Name = "¤Goods";
	
	static {
		D.ts(UIGoods.class);
	}
	
	private final GTableBuilder bu;
	private RESOURCE flashRes;
	private double flashTime;
	private final GInput filter = new GInput(new StringInputSprite(24, UI.FONT().S).placeHolder(Dic.¤¤Search));
	
	

	public UIGoods() {
		super(¤¤Name, UI.icons().l.crate);
		
		section = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER ren, float ds) {
				update();
				super.render(ren, ds);
			}
			
		};
		
		section.body().setWidth(WIDTH).setHeight(1);
		section.addRelBody(8, DIR.S, filter);
		
		bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return (int) Math.ceil((double)all.size()/COLS);
			}
		};
		
		
		Pop pop = new Pop();
		int wi = new Entry(null, 0, pop).body().width();
		bu.column(null, wi, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Entry(ier, 0, pop);
			}
		});
		bu.column(null, wi, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Entry(ier, 1,pop);
			}
		});

		section.addRelBody(8, DIR.S, bu.createHeight(HEIGHT-section.body().height()-8, false));
		
		
		
	}

	private void update() {
		all.clearSloppy();
		
		CharSequence f = filter.text();
		
		if (f.length() == 0 || Str.containsText(Dic.¤¤Total, f))
			all.add((Object)null);
		
		int cat = RESOURCES.ALL().get(0).category;
		for (RESOURCE r : RESOURCES.ALL()) {
			if (f.length() == 0 || Str.containsText(r.name, f) || Str.containsText(r.names, f)) {
				if (r.category != cat) {
					if ((all.size() & 1) == 1)
						all.add(icon);
					cat = r.category;
					
				}
				all.add(r);
			}
				
			
		}
	}
	
	public void detail(RESOURCE res, Faction f) {
		
		activate();
		int ta = all.indexOf(res);
		if (ta == -1)
			ta= 0;
		
		bu.set(ta/2);
		flashRes = res;
		flashTime = VIEW.renderSecond();
	}
	
	@Override
	public void activate() {
		flashRes = null;
		filter.text().clear();
		filter.focus();
		update();
		super.activate();
	}
	
	
	private class Entry extends GuiSection{
		
		private final GETTER<Integer> ier;
		private final GETTER<RESOURCE> res;
		private final int off;
		
		Entry(GETTER<Integer> ier, int off, Pop pop){
			this.off = off;
			this.ier = ier;
			this.res = new GETTER<RESOURCE>() {

				@Override
				public RESOURCE get() {
					int i = ier.get()*COLS + off;
					if (i >= all.size() || all.get(i) == null || all.get(i) == icon)
						return null;
					return (RESOURCE) all.get(i);
				}
				
			};
			

			addRightCAbs(56, new Row(res, pop));
			
			
			
			addRelBody(2, DIR.S, new RENDEROBJ.RenderImp(body().width(), 6) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().cY(), body().cY()+1);
					
				}
			});
			
			
			
			pad(6, 0);
			
		}
		
		
		@Override
		public void render(SPRITE_RENDERER rr, float ds) {
			int i = ier.get()*COLS + off;
			if (i >= all.size() || all.get(i) == icon)
				return;
			
			if (res.get() != null && flashRes == res.get() && VIEW.renderSecond()-flashTime < 3) {
				COLOR.WHITE2WHITE.render(rr, body(), -1);
				
				
			}else if (hoveredIs()) {
				OPACITY.O012.bind();
				COLOR.WHITE100.render(rr, body(), -1);
				OPACITY.unbind();
			} 
			super.render(rr, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(res.get() == null ? Dic.¤¤Total :  res.get().name);
			super.hoverInfoGet(text);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			int i = ier.get()*COLS + off;
			if (i >= all.size() || all.get(i) == icon)
				return false;
			return super.hover(mCoo);
		}
		
	}


}
