package view.ui.profile;

import java.util.Comparator;

import game.GAME;
import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.Faction;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GInput;
import util.gui.misc.GMeter;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.Dic;

public abstract class UIBonus extends GuiSection {
	
	private final StringInputSprite in = new StringInputSprite(16, UI.FONT().M).placeHolder(Dic.¤¤Search);
	private final GETTER<BOOSTABLE_O> bbb;
	public Faction f;
	
	public UIBonus(GETTER<BOOSTABLE_O> bbb, GETTER<Faction> f, int height){
		
		this.bbb = bbb;
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		ArrayList<Boostable> all = new ArrayList<>(BOOSTING.ALL().size());
		for (Boostable b : BOOSTING.ALL()) {
			if (is(b))
				all.add(b);
		}
		
		addRelBody(0, DIR.S, new GInput(in));
		
		BoostableCat cat = null;
		
		Row rr = new Row(BOOSTING.ALL().get(0));
		
		all.sort(new Comparator<Boostable>() {
			
			@Override
			public int compare(Boostable arg0, Boostable arg1) {
				return (""+arg0.cat.name).compareTo(""+arg1.cat.name);
			}
		});
		
		for (Boostable b : all) {
			if (b.name == null || b.name.length() == 0)
				continue;
			if ( b.cat != cat) {
				cat = b.cat;
				rows.add(new RENDEROBJ.RenderImp(rr.body().width(),rr.body().height()) {
					GText h = new GText(UI.FONT().H2, b.cat.name).lablifySub();
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						h.renderCY(r, body().x1()+20, body().cY());
						GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
					}
				});
			}
			double min = b.min(null);
			double max = b.max(null);
			if (min == b.baseValue && max == b.baseValue)
				continue;
			rows.add(new Row(b));
			
		}
		
		
		
		addRelBody(8, DIR.S, new GScrollRows(rows, height-body().height()-16, 0) {
			
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				if (in.text().length() == 0)
					return true;
				if (o instanceof Row) {
					Row r = (Row) o;
					if (Str.containsText(r.bo.name, in.text()) || Str.containsText(r.bo.desc, in.text()))
						return true;
					return false;
				}else
					return false;
				
				
			};
			
		}.view());
		
		addRelBody(8, DIR.E, new Effects(f, height));
		
	}
	
	protected abstract boolean is(Boostable bo);
	

	
	private class Row extends HOVERABLE.HoverableAbs {
		
		private final Boostable bo;
		private final SPRITE ico;
		Row(Boostable bo){
			super(450, 48);
			this.bo = bo;
			ico = bo.icon.resized(32);
		}
		
		private final GText t = new GText(UI.FONT().S, 16);

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			double min = bo.min(bbb.get().getClass());
			double max = bo.max(bbb.get().getClass());
			if (min == bo.baseValue && max == bo.baseValue)
				return;
			
			double d = bo.get(bbb.get());
			
			GMeter.renderDelta(r, bo.baseValue/max, d/max, body.x1(), body.x2()-90, body.y1(), body.y2(), GMeter.C_GRAY);
			
			ico.renderCY(r, body.x1()+16, body.cY());
			
			int w = UI.FONT().M.width(bo.name);
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, body.x1()+46, body.x1()+50+w+8, body.y1()+10, body.y2()-10);
			OPACITY.unbind();
			
			UI.FONT().M.renderCY(r, body.x1()+50, body.cY(), bo.name);
			
			t.clear();
			if (min == bo.baseValue && max == bo.baseValue)
				return;
			if (bo.baseValue == 0)
				GFORMAT.percInc(t, bo.get(bbb.get()));
			else
				GFORMAT.percInc(t, bo.get(bbb.get())/bo.baseValue-1.0);
			t.renderCY(r, body.x2()-80, body.cY());
			
		}

		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(bo.name);
			text.text(bo.desc);
			text.NL(8);
			bo.hoverDetailed(text, bbb.get(), null, true);
		}

		
	}
	
	private static class Effects extends RENDEROBJ.RenderImp {
		
		private final GETTER<Faction> fa;
		
		Effects(GETTER<Faction> fa, int height){
			super(400, height);
			
			this.fa = fa;
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GBox.tmp.clear();
			GBox.tmp.maxWidth = body.width();
			GBox.tmp.maxHeight = body.height();
			GAME.BOOST().hover(GBox.tmp, fa.get());
			GBox.tmp.renderWithout(r, body.x1(), body.y1());
			
		}
		
	}
	

	
}
