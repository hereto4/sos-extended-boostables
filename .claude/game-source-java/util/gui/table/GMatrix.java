package util.gui.table;

import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.table.GTableBuilder.GRowBuilder;

public abstract class GMatrix extends GuiSection{

	private final int columns;
	
	private final ArrayListGrower<Wrap> wraps = new ArrayListGrower<GMatrix.Wrap>();
	private Wrap toMove = null;
	private Wrap toMoveTo = null;
	
	public GMatrix(int rows, int columns, int entryWidth, int entryheight){
		if (columns <= 0)
			throw new RuntimeException();
		this.columns = columns;
		GTableBuilder b = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return (int) Math.ceil((double)GMatrix.this.nrOFEntries()/columns);
			}
		};
		
		b.column(null, entryWidth*columns, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Row(ier, entryheight);
			}
		});
		
		add(b.create(rows, false));
		
	}
	
	public abstract RENDEROBJ get(int i, int columnI);
	
	public abstract int nrOFEntries();
	
	public void multiSelect(int i) {
		
	}
	
	public abstract void move(int oldI, int newI);
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (toMove != null) {
			if (!MButt.LEFT.isDown()) {
				if (toMoveTo != null && toMove != toMoveTo) {
					move(toMove.i, toMoveTo.i);
				}
				toMove = null;
				toMoveTo = null;
			}
		}
		super.render(r, ds);
	}
	
	private class Row extends GuiSection {
		
		private final GETTER<Integer> ier;
		
		Row(GETTER<Integer> ier, int height){
			this.ier = ier;
			body().setHeight(height);
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			int ox = body().x1();
			int oy = body().y1();
			clear();
			
			int s = ier.get()*columns;
			int m = nrOFEntries();
			for (int i = 0; i < columns && s < m; i++) {
				while (s >= wraps.size())
					wraps.add(new Wrap());
				Wrap w = wraps.get(s);
				w.init(i, ier.get()*columns, s++);
				addRight(0, w);
			}
			body().moveX1Y1(ox, oy);
			super.render(r, ds);
		}
		
	}
	
	
	private class Wrap extends CLICKABLE.ClickWrap2 {

		private int col;
		private int row;
		private int i;
		private RENDEROBJ rr;
		
		public Wrap() {

		}

		private void init(int col, int row, int i) {
			this.col = col;
			this.row = row;
			this.i = i;
			rr = GMatrix.this.get(i, col);
		}
		
		@Override
		protected RENDEROBJ get() {
			return rr;
		}
		
		@Override
		public boolean click() {
			toMove = this;
			return super.click();
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			boolean hov = hoveredIs();
			
			super.render(r, ds);
			if (toMove == this) {
				COLOR.WHITE85.render(r, body().x1(), body().x1()+2,  body().y1(), body().y2());
			}else if (toMove != null && hov) {
				GCOLOR.UI().GOOD.hovered.render(r, body().x1(), body().x1()+2,  body().y1(), body().y2());
				toMoveTo = this;
			}
		}
		
		
	}
	
}
