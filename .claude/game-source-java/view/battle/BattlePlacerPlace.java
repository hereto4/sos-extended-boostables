package view.battle;

import game.GAME;
import game.audio.AUDIO;
import game.audio.SoundRace;
import init.constant.C;
import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.battle.BattlePlacer.Action;
import view.battle.BattlePlacer.Mode;
import view.main.VIEW;
import view.subview.GameWindow;

public final class BattlePlacerPlace extends Mode{

	private final GameWindow w;
	final DivSelection s;
	private final Action a;
	private boolean sounded = false;
	private final SoundRace sound = AUDIO.race("UP_PLACE_DIV");
	
	public BattlePlacerPlace(GameWindow w, DivSelection s, Action a) {
		this.w = w;
		this.s = s;
		this.a = a;
	}

	
	@Override
	void update(boolean hovered) {
		if (!hovered)
			return;

		if (a.clicked) {
			
		}else {
			sounded = false;
		}
		
		if (a.clickReleased) {
			
			
			GAME.ARMIES().placer.deploy(s.selection(), a.start.x(), w.pixel().x(), a.start.y(), w.pixel().y());
			if (VIEW.b().state() != null && VIEW.b().state().deploying()) {
			
				GAME.ARMIES().initAndTeleport(s.selection());
				

			}
			
		}
	}

	@Override
	void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
		
		if (!a.clicked) {
			int px = w.pixel().x();
			int py = w.pixel().y();
			if ( s.selection().size() == 0 || GAME.ARMIES().placer.isBlocked(px, py, C.TILE_SIZE, s.selection().get(0).army()))
				GCOLOR.MAP().BAD.bind();
			else {
				
//				Flooder f = GUTIL.flooder();
//				f.init(this);
//				int dx = w.pixel().x()&C.T_MASK;
//				int dy = w.pixel().y()&C.T_MASK;
//				int si = 0;
//				
//				f.pushSmaller(w.tile().x(), w.tile().y(), 0);
//				f.setValue2(w.tile().x(), w.tile().y(), 0);
//				
//				Div div = s.selection().get(0);
//				div.order().dest.get(form);
//				
//				while(f.hasMore()) {
//					if (si >= s.selection().size()) {
//						break;
//					}
//					
//					PathTile t = f.pollSmallest();
//				
//					int cx = t.x()*C.TILE_SIZE + dx;
//					int cy = t.y()*C.TILE_SIZE + dy;
//					
//					if (test(cx, cy, form, div, r, data)){
//						if (si >= s.selection().size())
//							break;
//						div = s.selection().get(si);
//						div.order().dest.get(form);
//						si++;
//					}
//					
//					for (DIR d : DIR.ORTHO) {
//						if (SETT.IN_BOUNDS(t, d) && !SETT.PATH().solidity.is(t, d))
//							f.pushSmaller(t, d, t.getValue()+1);
//						
//					}
//					
//					
//				}
//				
//				f.done();
				
				GCOLOR.MAP().BATTLE_OK.bind();
			}
				
				SPRITES.cons().BIG.dots.renderCentered(r, 0, px -data.offX1(), py- data.offY1());
			VIEW.mouse().setReplacement(SPRITES.icons().m.place_line);
			
			COLOR.unbind();
			return;
		}else {
			if (GAME.ARMIES().placer.render(r, s.selection(), a.start.x(), w.pixel().x(), a.start.y(), w.pixel().y(), data)) {
				if (!sounded) {
					sounded = true;
					sound.play(true);
				}
			}
		}
		
		
		
		
		
	
	}

//	private final DivFormationImp form = new DivFormationImp();
//	
//	private boolean test(int cx, int cy, DivFormation form, Div div, Renderer r, RenderData data) {
//	
//		int dx = cx-form.body().cX(); 
//		int dy = cy-form.body().cY();
//		Flooder f = GUTIL.flooder();
//		
//		for (int i = 0; i < form.deployed(); i++) {
//			
//			int x = form.px(i)+dx;
//			int y = form.py(i)+dy;
//			
//			for (DIR d : DIR.ORTHO) {
//				int tx = (x + d.x()*C.TILE_SIZE)/C.TILE_SIZE;
//				int ty = (y + d.y()*C.TILE_SIZE)/C.TILE_SIZE;
//
//				if (!SETT.IN_BOUNDS(tx,ty))
//					return false;
//				if (f.hasBeenPushed(tx, ty) && f.getValue2(tx, ty) != 0)
//					return false;
//			}
//			
//		}
//		
//		
//		
//		if (GAME.ARMIES().placer.deployer.canDeploy(form.start().x()+dx, form.start().y()+dy, form.dx(), form.dy(), form.width(), form.formation().size(div), div.army())) {
//			DivRenderer.render(CORE.renderer(), form, data, -dx, -dy);
//			
//			for (int i = 0; i < form.deployed(); i++) {
//				
//				int x = form.px(i)+dx;
//				int y = form.py(i)+dy;
//				
//				for (DIR d : DIR.ORTHO) {
//					int tx = (x + d.x()*C.TILE_SIZE)/C.TILE_SIZE;
//					int ty = (y + d.y()*C.TILE_SIZE)/C.TILE_SIZE;
//					
//					f.pushSmaller(tx, ty, 100000);
//					f.setValue(tx, ty, 1);
//				}
//				
//			}
//			
//			
//			return true;
//		}
//		return false;
//	}
	
	@Override
	void hoverTimer(GBox text) {
		
	}



	
}
