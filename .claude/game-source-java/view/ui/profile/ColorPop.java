package view.ui.profile;

import game.faction.FACTIONS;
import game.faction.FactionProfileFlusher;
import game.faction.player.PlayerColors;
import game.faction.player.PlayerColors.PlayerColor;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Text;
import util.gui.misc.GButt;
import util.gui.misc.GColorPicker;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.gui.table.GScrollRows;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;

class ColorPop extends ISidePanel{

	static CharSequence ¤¤name = "Color Masks";
	static {
		D.ts(ColorPop.class);
	}
	private PlayerColor color;
	
	ColorPop(){
		
		titleSet(¤¤name);
		
		
		section.add(new GColorPicker(true) {
			
			@Override
			public ColorImp color() {
				return color.color;
			}
		});
		
		{
			GButt bb = new GButt.ButtPanel(Dic.¤¤save){
				
				@Override
				protected void clickA() {
					FactionProfileFlusher.flush(FACTIONS.player());
				}
			};
			bb.body.setWidth(200);
			section.addRelBody(16, DIR.S, bb);
			
			bb = new GButt.ButtPanel(Dic.¤¤Reset){
				
				@Override
				protected void clickA() {
					PlayerColors.saver.clear();
				}
			};
			bb.body.setWidth(200);
			section.addRelBody(0, DIR.S, bb);
		}
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		for (String cat : PlayerColors.cats().keysSorted()) {
			rows.add(new GTextR(new GText(UI.FONT().S, cat).lablify()));
			for (PlayerColor c : PlayerColors.cats().get(cat)) {
				Text t = new Text(UI.FONT().S, c.name);
				t.setMaxChars(16);
				if (color == null)
					color = c;
				GButt bb = new GButt.ButtPanel((SPRITE) t){
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.text(c.name);
					}
					
					@Override
					protected void clickA() {
						color = c;
					}
					
					@Override
					protected void renAction() {
						selectedSet(color == c);
					}
				};
				
				bb.body.setWidth(220);
				rows.add(bb);
			}
		}

		section.addRelBody(4, DIR.N, new GScrollRows(rows, HEIGHT-section.body().height()-8).view());
		
	}
	
	public CLICKABLE butt() {
		
		SPRITE s = new SPRITE() {
			
			@Override
			public int width() {
				return 32;
			}
			
			@Override
			public int height() {
				return 32+16*3+4;
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int x1 = X1 + 4;
				UI.icons().m.place_brush.render(r, x1, Y1+4);
				COLOR.RED100.render(r, x1, X2-4, Y1+32, Y1+32+16);
				COLOR.GREEN100.render(r, x1, X2-4, Y1+32+16, Y1+32+16+16);
				COLOR.BLUE100.render(r, x1, X2-4, Y1+32+16+16, Y1+32+16+16+16);
			}
		};
		
		return new GButt.ButtPanel(s) {
			
			@Override
			protected void clickA() {
				VIEW.s().activate();
				VIEW.s().panels.add(ColorPop.this, true);
				VIEW.inters().manager.clear();
			};
			
		}.hoverInfoSet(¤¤name);
		
	}


	
}
