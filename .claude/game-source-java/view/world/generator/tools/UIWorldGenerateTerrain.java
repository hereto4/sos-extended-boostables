package view.world.generator.tools;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GInput;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.info.GFORMAT;
import util.text.D;
import world.WORLD;
import world.WorldGen;
import world.WorldGen.WorldGenMapType;

public class UIWorldGenerateTerrain extends GuiSection{

	public static CharSequence ¤¤MapType = "choose map type";
	private static CharSequence ¤¤Random = "Random";
	
	private static CharSequence ¤¤latitude = "¤latitude";
	private static CharSequence ¤¤nort = "¤northern";
	private static CharSequence ¤¤south = "¤southern";
	
	private static CharSequence ¤¤seed = "¤Random Seed";

	static {
		D.ts(UIWorldGenerateTerrain.class);
	}

	private int ttt;

	
	public UIWorldGenerateTerrain(WorldGen spec){

		{
			
			GuiSection s = new GuiSection();
			RMapType tt = new RMapType();
			s.add(tt);
			
			WorldGenMapType[] types = WorldGenMapType.getAll(WORLD.TWIDTH());
			
			ttt = types.length;
			spec.map = null;
			s.addRelBody(8, DIR.W, new GButt.ButtPanel(UI.icons().m.arrow_left) {
				
				@Override
				protected void clickA() {
					ttt--;
					if (ttt < 0)
						ttt = types.length;
					tt.type = ttt < types.length ? types[ttt] : null;
					spec.map = ttt < types.length ? types[ttt].name : null;
					super.clickA();
				}
				
			});
			
			s.addRelBody(8, DIR.E, new GButt.ButtPanel(UI.icons().m.arrow_right) {
				
				@Override
				protected void clickA() {
					ttt++;
					if (ttt > types.length)
						ttt = 0;
					tt.type = ttt < types.length ? types[ttt] : null;
					spec.map = ttt < types.length ? types[ttt].name : null;
					super.clickA();
				}
				
			});
			
			
			addRelBody(16, DIR.S, s);
			
		}
		
		{
			addRelBody(16, DIR.S, new GHeader(¤¤latitude));
			
			INTE lat = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 100;
				}
				
				@Override
				public int get() {
					return (int) Math.round(spec.lat*100);
				}
				
				@Override
				public void set(int t) {
					spec.lat = t/100.0;
				}
			};
			
			CLICKABLE gg =  new GSliderInt(lat, 180, true) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.add(GFORMAT.percGood(b.text(), lat.getD()));
				}
			};
			
			addRelBody(8, DIR.S, gg);
			
			addRightC(16, new GText(UI.FONT().S, ¤¤nort));
			
			GText t = new GText(UI.FONT().S, ¤¤south);
			
			int y1 = getLastY1();
			
			add(t, gg.body().x1()-16-t.width(), y1);
			
		}
		
		{
			addRelBody(16, DIR.S, new GHeader(¤¤seed));
			
			
			GInput seed = new GInput(new StringInputSprite(10, UI.FONT().M) {
				@Override
				protected void acceptChar(char c) {
					if (c >= '0' && c <= '9') {
						super.acceptChar(c);
						int se = RND.seed();
						CharSequence s = text();
						if ((s.length() > 10))
							s = (""+s).substring(0, 10);
						try {
							se = Integer.parseInt("" + s);
							spec.seed = se;
							RND.setSeed(se);
							//GAME.world().regenerate();
						}catch(Exception e) {
							text().clear().add('1');
						}
					}
				}
			}) {
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					GCOLOR.UI().bg().render(r, body);
					super.render(r, ds, isActive, isSelected, isHovered);
				}
			};
			seed.text().clear().add(spec.seed);
			addRelBody(16, DIR.S, seed);
			
			
			
		}
		
		
		
		
		
		
	}
	
	private class RMapType extends RENDEROBJ.RenderImp{

		private final GText text;
		
		private WorldGenMapType type;
		
		public RMapType() {
			body.setDim(200+10, 200+10);
			text = new GText(UI.FONT().M, ¤¤Random);
			text.setMultipleLines(true);
			text.setMaxWidth(200);
			text.adjustWidth();
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().border().render(r, body);
			GCOLOR.UI().bg(true, false, false).render(r, body, -1);
			if (type == null) {
				text.renderC(r, body);
			}else {
				type.render(r, body().x1()+5, body().y1()+5, 2);
			}
		}
	}
	
	
}
