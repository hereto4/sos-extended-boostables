package view.ui.tech;

import java.util.Comparator;

import game.boosting.BOOSTING;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FACTIONS;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class InfoBonuses extends GuiSection{

	private final int WIDTH = 300;
	private final UITechTree tree;

	public InfoBonuses(UITechTree tree, int height, int width) {
		this.tree = tree;
		int cc = (int) Math.ceil(width / (WIDTH+48.0));
		
		ArrayList<ArrayListGrower<Object>> cols = new ArrayList<>(cc);
		int max = 0;
		while(cols.hasRoom())
			cols.add(new ArrayListGrower<Object>());
		{
			
			KeyMap<BoostableCat> map = new KeyMap<>();
			for (Boostable b : BOOSTING.ALL()) {
				if (map.get(b.cat.prefix) == null)
					map.put(b.cat.prefix, b.cat);
			}
			
			ArrayList<BoostableCat> cats = new ArrayList<BoostableCat>(map.all());
			cats.sort(new Comparator<BoostableCat>() {
				
				@Override
				public int compare(BoostableCat o1, BoostableCat o2) {
					return o2.all().size()-o1.all().size();
				}
			});
			
		
			for (BoostableCat c : cats) {
				ArrayListGrower<Object> current = null;
				for (ArrayListGrower<Object> l : cols) {
					if (current == null || l.size() < current.size()) {
						current = l;
					}
				}
				if (current.size() > 0)
					current.add((Object)null);
				current.add(c);
				for (Boostable b : c.all()) {
					current.add(b);
				}
				max = Math.max(current.size(), max);
			}
		}

		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		
		for (int i = 0; i < max; i++) {
			GuiSection row = new GuiSection();
			int ri = 0;
			for (ArrayListGrower<Object> l : cols) {
				
				RENDEROBJ r = null;
				if (l.size() <= i) {
					r = new RENDEROBJ.RenderDummy(WIDTH, 18);
				}else {
					Object o = l.get(i);
					if (o == null) {
						r = new RENDEROBJ.RenderDummy(WIDTH, 18);
					}else if (o instanceof BoostableCat) {
						GTextR R = new GTextR(UI.FONT().S, ((BoostableCat)o).name);
						R.setColor(GCOLOR.T().H1);
						r = R;
					}else {
						r = new Boo((Boostable) o);
					}
				}
				row.add(r, WIDTH*ri, body().y1());
				ri++;
			}
			rens.add(row);
			
			
		}
		
		
		
		add(new GScrollRows(rens, height-8).view());

		
	}
	
	private final GText t = new GText(UI.FONT().S, 20);
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		super.render(r, ds);
	}
	
	private class Boo extends ClickableAbs{

		private final Boostable bo;
		private ArrayListGrower<TECH> techs = new ArrayListGrower<>();
		
		public Boo(Boostable bo) {
			super(WIDTH, 18);
			this.bo = bo;
			for (TECH t : TECHS.ALL()) {
				for (BoostSpec b : t.boosters.all()) {
					if (b.boostable == bo) {
						techs.add(t);
					}
				}
			}
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			if (techs.size() == 0)
				OPACITY.O50.bind();
			else if (!isHovered)
				OPACITY.O85.bind();
			
			bo.icon.render(r, body().x1(), body().y1());
			
			if (techs.size() > 0)
				GCOLOR.T().H1.bind();
			UI.FONT().S.render(r, bo.name, body().x1()+20, body().y1(), 0, bo.name.length() > 15 ? 15 : bo.name.length(), 1);
			COLOR.unbind();
			OPACITY.unbind();
			t.clear();
			double add = 0;
			double mul = 1;
			for (TECH t : techs) {
				for (BoostSpec b : t.boosters.all()) {
					if (b.boostable == bo) {
						if (b.booster.isMul)
							mul *= FACTIONS.player().tech.level(t)*(b.booster.to()-1) + 1;
						else
							add += FACTIONS.player().tech.level(t)*(b.booster.to());
						
					}
				}
			}
			
			GFORMAT.percInc(t, (add+1)*mul-1);
			t.render(r, body.x1()+220, body.y1());
		}
		
		@Override
		protected void clickA() {
			if (techs.size() == 0)
				return;
			tree.filter.set(bo.name);
			super.clickA();
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(bo.name);
			text.text(bo.desc);
			text.NL(8);
			
			GBox box = (GBox) text;
			for (TECH t : techs) {
				for (BoostSpec b : t.boosters.all()) {
					if (b.boostable == bo) {
						if (b.booster.isMul)
							b.booster.hover(box, FACTIONS.player().tech.level(t)*(b.booster.to()-1) + 1);
						else
							b.booster.hover(box, FACTIONS.player().tech.level(t)*(b.booster.to()));
						
					}
				}
				box.NL();
			}
			
		}

	}
	
}
