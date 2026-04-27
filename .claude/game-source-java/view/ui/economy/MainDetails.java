package view.ui.economy;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PCredits.CredHistory;
import game.time.TIME;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.data.INT.IntImp;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;
import util.text.DicTime;

final class MainDetails extends GuiSection{

	private final GText t = new GText(UI.FONT().S, 48).lablify();
	
	MainDetails(IntImp ii){
		
		
		
		int i = 0;
		
		for (CredHistory h : GAME.player().credits().all()) {
			HOVERABLE hh = new HOVERABLE.Sprite(new SDetail(h) {
				
				@Override
				void up(GText text) {
					int i = ii.get();
					if (i < 0)
						i = GAME.player().credits().creditsH().historyRecords()-1;
					i = GAME.player().credits().creditsH().historyRecords()-i-1;
					GFORMAT.iIncr(text, h.IN.get(i)-h.OUT.get(i));
				}
			}).hoverTitleSet(h.type.name).hoverInfoSet(h.type.desc);
			
			hh.body().moveX1Y1((i%1)*(hh.body().width()+32), (i/1)*(hh.body().height()+2));
			add(hh);
			i++;
		}
		
		addDown(4, new HOVERABLE.HoverableAbs(260, 32) {
			GText t = new GText(UI.FONT().S, 48).lablify();
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				GCOLOR.T().H1.bind();
				UI.FONT().H2.renderCY(r, body().x1()+UI.FONT().S.height()*2, body().cY(), Dic.¤¤Treasury);
				t.clear();
				int i = ii.get();
				if (i < 0)
					i = GAME.player().credits().creditsH().historyRecords()-1;
				i = GAME.player().credits().creditsH().historyRecords()-i-1;
				GFORMAT.i(t, FACTIONS.player().credits().creditsH().get(i));
				t.adjustWidth();
				t.renderCY(r, body().x2()-t.width(), body().cY());
				
			}
		});
		
		addRelBody(8, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				text.lablifySub();
				int i = ii.get();
				if (i < 0)
					i = GAME.player().credits().creditsH().historyRecords()-1;
				i = GAME.player().credits().creditsH().historyRecords()-i-1;
				DicTime.setAgo(text, i*TIME.secondsPerDay());
			}
		}.r(DIR.N));
		
	}

	
	private abstract class SDetail implements SPRITE{
		
		private final GStat stat = new GStat() {
			
			@Override
			public void update(GText text) {
				up(text);;
			}
		};
		private final CredHistory cr;
		
		
		SDetail(CredHistory cr){
			this.cr = cr;
		}
		
		abstract void up(GText text);
		
		@Override
		public int width() {
			return 260;
		}



		@Override
		public int height() {
			return stat.height();
		}



		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			ColorImp.TMP.set(COLOR.UNIQUE.getC(cr.type.ordinal())).shadeSelf(0.5);
			ColorImp.TMP.render(r, X1, X1+height(), Y1, Y1+height());
			ColorImp.TMP.set(COLOR.UNIQUE.getC(cr.type.ordinal()));
			ColorImp.TMP.render(r, X1+2, X1+height()-2, Y1+2, Y1+height()-2);
			
			t.clear().add(cr.type.name);
			
			t.render(r, X1+height()*2, Y1);
			
			stat.adjust();
			
			stat.render(r, X2-stat.width(), Y1);
			
		}



		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		
		
	}
	
}
