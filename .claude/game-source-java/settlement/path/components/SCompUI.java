package settlement.path.components;

import static settlement.main.SETT.PATH;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HGROUP;
import settlement.main.ON_TOP_RENDERABLE;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.IDebugPanelSett;

final class SCompUI extends ISidePanel{

	private SComponentLevel l;
	
	
	SCompUI(SCOMPONENTS comps) {
		
		l = comps.zero;
		
		titleSet("Components");
		
		IDebugPanelSett.add("Path Comps show", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.s().panels.add(SCompUI.this, true);
			}
		});
		
		int i = 0;
		for (SComponentLevel l : comps.all) {
			
			
			GButt b = new GButt.Panel(UI.FONT().M.getText(""+i)) {
				
				@Override
				protected void clickA() {
					SCompUI.this.l = l;
				}
				
				@Override
				protected void renAction() {
					selectedSet(SCompUI.this.l == l);
				}
			};
			section.addRightC(2, b);
			i++;
		}
		
		section.body().setWidth(600);
		
		
		
		section.add(new view(comps), section.body().x1()+10, section.body().y2()+4);
		
	}
	
	private class view extends GuiSection {
		

		private SComponent comp = null;
		ON_TOP_RENDERABLE rr = new ON_TOP_RENDERABLE() {
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				RenderIterator it = data.onScreenTiles();
				SComponentLevel prev = l;
				SComponentLevel current = l;
				
				if (l.level() > 0) {
					prev = PATH().comps.all.get(l.level()-1);
				}
				
				while(it.has()) {
					SComponent c = prev.get(it.tile());
					if (c != null) {
						int m = 0;
						for (DIR d : DIR.ORTHO) {
							if (c.is(it.tx(), it.ty(), d))
								m |= d.mask();
						}
						SComponent cc = current.get(it.tile());
						if (cc != null) {
							if (cc == comp)
								COLOR.WHITE100.bind();
							else
								COLOR.UNIQUE.getC(cc.index()).bind();
							if (m != 0xF)
								SPRITES.cons().BIG.dashed_hollow.render(r, m, it.x(), it.y());
							if (it.tx() == cc.centreX() && it.ty() == cc.centreY()) {
								SPRITES.cons().ICO.crosshair.render(r, it.x(), it.y());
							}
						}
						
						
					}
					
					it.next();
				}
				COLOR.unbind();
				remove();
			}
		};
		
		view(SCOMPONENTS comps){

			
			addRelBody(8, DIR.S, new GStat() {
				
				@Override
				public void update(GText text) {
					text.lablify();
					text.add(comp.index());
					text.s();
					
					if (comp.hasEdge()) {
						text.s();
						text.add('b');
					}
					if (comp.hasEntry()) {
						text.s();
						text.add('e');
					}
					
					text.s().s().add('s');
					if (comp.superComp() != null)
						text.s().add(comp.superComp().index());
					
					text.s().s().s();
					text.add('(').add(VIEW.s().getWindow().tile().x()).s().add(VIEW.s().getWindow().tile().y()).add(')');
					text.s().add(comp.retired());
				}
			}.increase());
			
			add(new RENDEROBJ.RenderImp(600, 48) {
				
				private final GText t = new GText(UI.FONT().S, "");
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					int i = 0;
					COLOR.WHITE100.render(r, body());
					SComponentEdge e = comp.edgefirst();
					while(e != null) {
						t.clear();
						t.color(COLOR.UNIQUE.getC(e.to().index()));
						t.add(e.to().index());
						t.add('-').add('>');
						t.add((int)e.distance());
						t.add('|');
						t.add((int)e.cost2());
						t.s().s();
						t.render(r, body().x1()+(i%5)*120, body().y1()+(i/5)*16);
						e = e.next();
						i++;
					}
				}
			}, 0, body().y2()+4);
			
			{
				add(new GText(UI.FONT().H2, ""), 0, getLastY2()+32);
				int tab = 90;
				for (FindableDataRes r : FindableDataRes.all) {
					addRightCAbs(tab, new GText(UI.FONT().H2, r.title));
				}
				RENDEROBJ[] rows = new RENDEROBJ[RESOURCES.ALL().size()];
				for (RESOURCE res : RESOURCES.ALL()) {
					GuiSection section = new GuiSection();
					section.hoverInfoSet(res.name);
					section.add(res.icon().small, 0, section.getLastY2());
					for (FindableDataRes r : FindableDataRes.all) {
						GStat s = new GStat() {
							
							@Override
							public void update(GText text) {
								text.add(r.get(comp, res));
								text.add(r.bits(comp).has(res) ? '*' : ' ');
								text.normalify();
								if (r.get(comp, res) > 0)
									text.lablify();
								if (r.overflow(comp, res)) {
									text.add('!');
								}
									
							}
						};
						section.addRightCAbs(tab, s);
					}
					section.body().incrW(80);
					rows[res.index()] = section;
				}
				add(new GScrollRows(rows, HEIGHT/3, 0).view(), 0, getLastY2()+2);
				
				
			}
			
			{
				RENDEROBJ[] rows = new RENDEROBJ[FindableDataSingle.all.size()];
				
				int i = 0;
				for (FindableData d : FindableDataSingle.all) {
					rows[i++] = new GStat() {
						
						@Override
						public void update(GText text) {
							if (comp != null) {
								text.add(d.get(comp));
								if (d.overflow(comp)) {
									text.add('!');
								}
							}
						}
					}.hh(d.name, 200);
				}
				add(new GScrollRows(rows, HEIGHT-getLastY2()-64, 0).view(), 0, getLastY2()+2);
			}
			
			{
				RENDEROBJ[] rows = new RENDEROBJ[HGROUP.all().size()];
				
				int i = 0;
				for (HGROUP t : HGROUP.all()) {
					rows[i++] = new GStat() {
						FindableData d = comps.data.home.get(t);
						@Override
						public void update(GText text) {
							if (comp != null) {
								text.add(d.get(comp));
								if (d.overflow(comp)) {
									text.add('!');
								}
							}
						}
					}.hh(comps.data.home.get(t).name, 250);
				}
				add(new GScrollRows(rows, HEIGHT-getLastY1()-64, 0).view(), getLastX2()+10, getLastY1());
			}
			
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			rr.add();
			if (VIEW.mouse().x() > 600) {
				comp = l.get(VIEW.s().getWindow().tile());
			}
			if (comp == null)
				return;
			
			super.render(r, ds);
		}
		
	}
	
	
}
