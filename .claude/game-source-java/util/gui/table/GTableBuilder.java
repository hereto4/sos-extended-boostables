package util.gui.table;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.ColorShifting;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.Scrollable;
import snake2d.util.gui.clickable.Scrollable.ScrollRow;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.ArrayListShort;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;

public abstract class GTableBuilder {

	private final ArrayListResize<HOVERABLE> titles = new ArrayListResize<>(10, 20);
	private final ArrayListShort widths = new ArrayListShort(20);
	private final ArrayListResize<GRowBuilder> cells = new ArrayListResize<>(10, 20);
	private final ArrayListResize<DIR> dirs = new ArrayListResize<>(10, 20);
	public final static COLOR cHovered = new ColorImp(28,23,53);
	public final static COLOR cSelected = new ColorShifting(cHovered, cHovered.shade(1.5)) ;
	private GScrollable scroller;
	
	public void column(CharSequence title, int width, GRowBuilder ren) {
		column(title, width, ren, DIR.W);
	}
	
	public void column(int width, GRowBuilder ren, DIR d) {
		
		column((HOVERABLE)null, width, ren, d);
	}
	
	public void column(CharSequence title, int width, GRowBuilder ren, DIR d) {
		
		HOVERABLE t = title != null && title.length() > 0 ? new GText(UI.FONT().S, title).lablifySub().r(DIR.NW) : null;
		column(t, width, ren, d);
	}
	
	public void column(HOVERABLE t, int width, GRowBuilder ren, DIR d) {

		titles.add(t);
		widths.add(width);
		cells.add(ren);
		dirs.add(d);
	}

	public GuiSection createHeight(int heightt, boolean decorate) {
		int height = 0;
		final GETTER_IMP<Integer> inin = new GETTER_IMP<Integer>();
		for (int k = 0; k < titles.size(); k++) {
			if (cells.get(k) != null) {
				RENDEROBJ o = cells.get(k).build(inin);
				if (o.body().height() > height)
					height = o.body().height();
			}
		}
		height += (decorate ? 6 : 0);
		if (titles.size() > 0)
			for (RENDEROBJ s : titles)
				if (s != null) {
					heightt-= UI.FONT().M.height();
					break;
				}
		
		
		int rows = heightt/height;
		if (rows <= 0)
			rows = 1;
		return create(rows, decorate);
		
	}
	
	private static final int M = 3;
	
	public GuiSection create(int rows, boolean decorate) {
		if (rows < 0)
			rows = 0;
		Scrollable.ScrollRow[] rs = new Scrollable.ScrollRow[rows];
		int width = 0;
		int height = 0;
		final GETTER_IMP<Integer> inin = new GETTER_IMP<Integer>();
		for (int k = 0; k < titles.size(); k++) {
			if (cells.get(k) != null) {
				RENDEROBJ o = cells.get(k).build(inin);
				if (o.body().height() > height)
					height = o.body().height();
			}
			width += widths.get(k)+2*M;
			
		}
		width += (decorate ? 8 : 0);
		height += (decorate ? 6 : 0);
		for (int i = 0; i < rows; i++) {

			final GETTER_IMP<Integer> in = new GETTER_IMP<Integer>();
			
			ScrollRow.ScrollRowImp row = new ScrollRow.ScrollRowImp() {
				
				float clickT = 0;
				
				@Override
				public void init(int index) {
					in.set(index);
				}

				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (decorate) {
						boolean isHovered = hoveredIs();
						boolean isSelected = GTableBuilder.this.selectedIs(in.get());
						GButt.BSection.renderBG(r, body(), true, isHovered, isSelected);
					}
					if (clickT >= 0)
						clickT -= ds;
					super.render(r, ds);
					if (!GTableBuilder.this.activeIs(in.get())) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, body(), -1);
						OPACITY.unbind();
					}
					
				}
				
				@Override
				public boolean hover(COORDINATE mCoo) {
					if (super.hover(mCoo)) {
						GTableBuilder.this.hover(in.get());
						return true;
					}
					GTableBuilder.this.hover(-1);
					return false;
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					GTableBuilder.this.hoverInfo(in.get(), (GBox) text);
				}
				
				@Override
				public boolean click() {
					
					if (!super.click()) {
						if (clickT > 0) {
							doubleClick(in.get());
							clickT = 0;
						}else {
							clickT = 0.3f;
							
						}
						return true;
					}
					return false;
				}

			};
			
			row.body().setWidth(width).setHeight(height);


			int x = 0;
			for (int k = 0; k < titles.size(); k++) {
				if (cells.get(k) != null) {
					RENDEROBJ o = cells.get(k).build(in);
					DIR d = dirs.get(k);
					
					int x1 = x + M;
					
					if (d.x() < 0) {
						o.body().moveX1(x1);
					}else if (x > 0){
						o.body().moveX2(x1+widths.get(k));
					}else {
						o.body().moveCX(x1+widths.get(k)/2);
					}
					if (d.y() < 0) {
						o.body().moveY1(0);
					}else if (d.y() > 0){
						o.body().moveY2(height);
					}else {
						o.body().moveCY(height/2);
					}
					
//					int dx = (widths.get(k)-o.body().width())/2;
//					int dy = (height-o.body().height())/2;
//					int cx = x+widths.get(k)/2;
//					int cy = height/2;
//					o.body().moveCX(d.x()*dx+cx);
//					o.body().moveCY(d.y()*dy+cy);
					//o.body().moveX1Y1(x+dx*(d.x()+1), dy*(d.y()+1));
					if (decorate && k > 0) {
						row.add(new RENDEROBJ.RenderImp(2, height-4) {

							@Override
							public void render(SPRITE_RENDERER r, float ds) {
								GCOLOR.UI().border().render(r, body);
							}
							
						}, x, o.body().cY()-(height-4)/2);
					}
					row.add(o);
				}
				
				x += widths.get(k)+2*M;
				
			}

			row.clickActionSet(new ACTION() {

				@Override
				public void exe() {
					click(in.get());
				}
			});

			row.body().setWidth(x);
			rs[i] = row;

		}

		scroller = new GScrollable(rs) {

			@Override
			public int nrOFEntries() {
				return GTableBuilder.this.nrOFEntries();
			}
			
			
			
		};
		
		CLICKABLE s = scroller.getView();
		

		GuiSection res = new GuiSection();

		boolean title = false;
		RENDEROBJ last = null;

		int x = 0;
		for (int k = 0; k < titles.size(); k++) {
			if (titles.get(k) != null) {
				if (!title) {
					title = true;
					s.body().incrY(C.SG * 16);
				}
				
				int x1 = x;
				int y1 = 0;
				
				RENDEROBJ o = titles.get(k);
				
				int dx = (widths.get(k)-o.body().width())-2*M;
				o.body().moveY1(y1);
				DIR d = dirs.get(k);
				o.body().moveX1(x + M + dx*(d.x()+1)/2.0);
				
				if ((k&1) == 1 && (o.body().width() > widths.get(k)-10 ||(last != null && last.body().width() > widths.get(k-1)-10)))
					o.body().moveY2(0);
				if (decorate) {
					res.add(new RENDEROBJ.RenderImp(2, -o.body().y1()+o.body().height()) {

						@Override
						public void render(SPRITE_RENDERER r, float ds) {
							GCOLOR.UI().border().render(r, body);
						}
						
					}, x1, o.body().y1()+2);
					
					
				}
				res.add(o);
				last = o;
			}else
				last = null;
			
			x += widths.get(k) + M*2;
		}

		res.add(s);

		return res;
	}

	public abstract int nrOFEntries();

	public void click(int index) {

	}
	
	public void doubleClick(int index) {
		
	}
	
	public void hover(int index) {
		
	}

	public final void set(int index) {
		scroller.set(index);
	}
	
	public void hoverInfo(int index, GBox box) {
		
	}
	
	public boolean selectedIs(int index) {
		return false;
	}

	public boolean activeIs(int index) {
		return true;
	}

	public static abstract class GRowBuilder {

		public abstract RENDEROBJ build(GETTER<Integer> ier);

	}

	public final void pad(int width) {
		int x = 0;
		for (int k = 0; k < titles.size(); k++) {
			x += widths.get(k);
		}
		if (x < width) {
			column("", width-x, null);
		}
	}

}
