package util.gui.common;

import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListShort;
import util.data.GETTER;
import util.data.INT.INTE;
import util.data.INT_O.INT_OE;
import util.gui.slider.GSliderIntInput;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;

public class UIPickerRaceAm extends GuiSection{

	private INT_OE<Race> g;
	private ArrayListShort all = new ArrayListShort(RACES.all().size());
	
	public UIPickerRaceAm(INT_OE<Race> g, int rows) {
		
		this.g = g;
		
		GTableBuilder builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return all.size();
			}
		};
		
		
		
		Button b = new Button(new GETTER.GETTER_IMP<Integer>(0));
		G gg = new G(new GETTER.GETTER_IMP<Integer>(0));
		addToRow(b, gg);
		
		builder.column("", b.body().width(), new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				Button b = new Button(ier);
				G gg = new G(ier);
				addToRow(b, gg);
				return b;
			}
		});
		
		add(builder.create(rows, false));
		
		
	}
	
	protected void addToRow(GuiSection row, GETTER<Race> g) {
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		all.clear();
		for (Race res : RACES.all())
			if (g.max(res) > 0)
				all.add(res.index());
		super.render(r, ds);
	}
	
	private  class G implements GETTER<Race> {
		
		private final GETTER<Integer> ier;
		
		G(GETTER<Integer> ier){
			this.ier = ier;
		}
		
		@Override
		public Race get() {
			return RACES.all().get(all.get(ier.get()));
		}
		
	}
	
	private class Button extends GuiSection{

		private final GETTER<Integer> ier;
		
		public Button(GETTER<Integer> ier) {
			this.ier = ier;
			
			add(new HoverableAbs(Icon.M) {

				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					r().appearance().icon.render(r, body);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(r().info.names);
				}
			});
			
			INTE in = new INTE() {
				
				@Override
				public int min() {
					return g.min(r());
				}
				
				@Override
				public int max() {
					return g.max(r());
				}
				
				@Override
				public int get() {
					return g.get(r());
				}
				
				@Override
				public void set(int t) {
					g.set(r(), t);
				}
			};
			
			addRightC(4, new GSliderIntInput(in));
			
		}
		
		private Race r() {
			return RACES.all().get(all.get(ier.get()));
		}
		
	}
	
}
