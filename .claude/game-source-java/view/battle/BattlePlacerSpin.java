package view.battle;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DivFormationImp;
import game.battle.thread.order.BattleOrderTask;
import init.constant.C;
import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.VectorImp;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.battle.BattlePlacer.Action;
import view.battle.BattlePlacer.Mode;
import view.main.VIEW;
import view.subview.GameWindow;

final class BattlePlacerSpin extends Mode{

	
	
	private final GameWindow w;
	private final DivSelection s;
	private final DivFormationImp form = new DivFormationImp();
	private final Action a;
	
	public BattlePlacerSpin(GameWindow w, DivSelection s, Action a) {
		this.w = w;
		this.s = s;
		this.a = a;
	}
	

	private final BattleOrderTask task = new BattleOrderTask();
	private double cx,cy;
	private final VectorImp vec = new VectorImp();
	
	@Override
	void update(boolean hovered) {
		
		cx = 0;
		cy = 0;
		for (Div d : s.selection()) {
			d.order().dest.get(form);
			cx += form.start().x();
			cy += form.start().y();
		}
		cx /= s.selection().size();
		cy /= s.selection().size();
		
		if (!hovered)
			return;
		

		if (a.clickReleased) {

			for (Div d : s.selection()) {
				DivFormationImp f = getFor(d);
				if (f != null) {
					d.order().dest.set(f);
					task.move(d);
					d.order().task.set(task);
				}
			}
			if (VIEW.b().state() != null && VIEW.b().state().deploying()) {
				
				GAME.ARMIES().initAndTeleport(s.selection());
				
			}

			
		}
	}
	
	private DivFormationImp getFor(Div d) {
		d.order().dest.get(form);
		
		double newAngle = 0;
		{
			double destDX = w.pixel().x()-a.start.x();
			destDX/= (100<<w.zoomout());
			destDX %= Math.PI*2;
			newAngle = destDX;
		}
		
		double dist = vec.set(cx, cy, form.start().x(), form.start().y());
		
		vec.rotateRad(newAngle);
		
		double x1 = a.start.x() + vec.nX()*dist;
		double y1 = a.start.y() + vec.nY()*dist;

		vec.set(form.dx(), form.dy());
		vec.rotateRad(newAngle);

		return GAME.ARMIES().placer.deployer.deploy(d.info, d.menNrOf(), d.settings().formation, (int)x1, (int)y1, vec.nX(), vec.nY(), form.width(), d.army());
	
	}

	@Override
	void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
		
		VIEW.mouse().setReplacement(SPRITES.icons().m.rotate);
		
		
		if (a.clicked) {
			int x = w.pixel().x();
			int y = w.pixel().y();
			{
				if (GAME.ARMIES().placer.isBlocked(x, y, C.TILE_SIZE, GAME.ARMIES().player()))
					GCOLOR.MAP().BAD.bind();
				else
					GCOLOR.MAP().BATTLE_OK.bind();
				SPRITES.cons().BIG.dots.renderCentered(r, 0, x -data.offX1(), y- data.offY1());
			}
			
			
			
			
			COLOR.unbind();
			for (Div d : s.selection())
				GAME.ARMIES().placer.render(r, getFor(d), data);
			return;
		}
		
		
		
	
	}

	@Override
	void hoverTimer(GBox text) {
		
	}


	
}
