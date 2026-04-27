package view.battle;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DivFormationImp;
import game.battle.formation.DivRenderer;
import game.battle.formation.FormationBody;
import game.battle.thread.order.BattleOrderPath;
import game.battle.thread.order.BattleOrderTask;
import game.battle.thread.order.BattleOrderTask.DIVTASK;
import init.constant.C;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.light.AmbientLight;
import snake2d.util.sprite.SPRITE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.keyboard.KEYS;
import view.main.VIEW;

public final class BattleRenderer extends ON_TOP_RENDERABLE{

	private final DivSelection s;
	private final COLOR cHover = new ColorImp(0, 127, 0);
	private final COLOR cHoverEnemy = new ColorImp(127, 0, 0);
	private final BattleOrderTask task = new BattleOrderTask();
	
	public static ColorImp colAttack = new ColorImp(127, 40, 40);
	
//	private final DivStatus status = new DivStatus();
	
	public BattleRenderer(DivSelection s) {
		this.s = s;
	}
	
	@Override
	public void render(ShadowBatch shadowBatch, RenderData data, int zoomout, double ds) {
		CORE.renderer().newLayer(false, 0);
		AmbientLight.full.register(0, C.WIDTH(), 0, C.HEIGHT());
		remove();
		Renderer r = CORE.renderer();
		
		if (r.getZoomout() < 4) {
			ENTITY[] es = SETT.ENTITIES().getAllEnts();
			int m = SETT.ENTITIES().Imax();
			for (int ei = 0; ei < m; ei++){
				ENTITY e = es[ei];
				if (e == null || !data.gBounds().holdsPoint(e.body().cX(), e.body().cY()))
					continue;
				if (e instanceof Humanoid) {
					Div d = ((Humanoid) e).division();
					if (d != null && (s.hovered(d) || s.selected(d))) {
						if (d.army() == GAME.ARMIES().enemy())
							cHoverEnemy.bind();
						else
							cHover.bind();
						int rx = (data.absBounds().x1() + (e.body().cX() - data.gBounds().x1())>>zoomout);
						int ry = (data.absBounds().y1() + (e.body().cY() - data.gBounds().y1())>>zoomout);
						DIR dir = d.position().dir(d.reporter.positionSpot((Humanoid) e));
						if (dir == null)
							dir = e.speed.dir();
						
						if (zoomout == 3) {
							SPRITE s = SPRITES.cons().TINY.dots.get(0);
							int dd = 8;
							s.render(r, rx-dd/2, rx+dd/2, ry-dd/2, ry+dd/2);
						}else {
							SPRITE s = SPRITES.cons().ICO.arrows2.get(dir.id());
							
							int dd = s.width()>>Math.min(zoomout+1, 2);
							int x1 = rx-dd/2;
							int y1 = ry-dd/2;
							s.render(r, x1, x1+dd, y1, y1+dd);
						}
						
						
					}
				}
			}
		}

		
		
		if (KEYS.BATTLE().SHOW_DIVISIONS.isPressed()) {
			for (int di = 0; di < GAME.ARMIES().divisions().size(); di++) {
				Div d = GAME.ARMIES().divisions().getC(di);
				
				if (s.hovered(d) || s.selected(d))
					continue;
				
				int x = d.centre().cX();
				int y = d.centre().cY();
				
				if (data.gBounds().holdsPoint(x, y)) {
					x = (data.absBounds().x1() + (x - data.gBounds().x1())>>zoomout);
					y = (data.absBounds().y1() + (y - data.gBounds().y1())>>zoomout);
					SPRITE icon = VIEW.UI().div.battle.miniDiv(d, false, false);
					icon.renderC(r, x, y);
				}
			}
			
			for (int di = 0; di < GAME.ARMIES().divisions().size(); di++) {
				
				Div d = GAME.ARMIES().divisions().getC(di);
				if (s.hovered(d) || s.selected(d)) {
					SPRITE icon = VIEW.UI().div.battle.miniDiv(d, s.hovered(d),s.selected(d));
					int x = d.centre().cX();
					int y = d.centre().cY();
					
					if (data.gBounds().holdsPoint(x, y)) {
						x = (data.absBounds().x1() + (x - data.gBounds().x1())>>zoomout);
						y = (data.absBounds().y1() + (y - data.gBounds().y1())>>zoomout);
						icon.renderC(r, x, y);
					}
				}
					
				
				
			}
		}

		
		//target
		for (int di = 0; di < GAME.ARMIES().divisions().size(); di++) {
			Div d = GAME.ARMIES().divisions().getC(di);
			if (d.menNrOf() <= 0)
				continue;
			
			

			
			if (s.hovered(d) || s.selected(d) || KEYS.BATTLE().SHOW_DIVISIONS.isPressed()) {
				
				d.order().task.get(task);
				Div t = task.targetDiv();
				SPRITE sp = task.task() == DIVTASK.ATTACK_RANGED ? UI.icons().l.crossheir : UI.icons().l.swords;
				
				if (t != null && t.reporter.body().touches(data.gBounds())) {
					int x = t.centre().cX();
					int y = t.centre().cY();
					if (data.gBounds().holdsPoint(x, y)) {
						x = (data.absBounds().x1() + (x - data.gBounds().x1())>>zoomout);
						y = (data.absBounds().y1() + (y - data.gBounds().y1())>>zoomout);
						COLOR.RED2RED.bind();
						sp.renderCScaled(r, x, y, 8>>zoomout);
					}
					
					if (body.init(t.current())){
						
						int x1 = (data.absBounds().x1() + (body.x1() - data.gBounds().x1())>>zoomout);
						int y1 = (data.absBounds().y1() + (body.y1() - data.gBounds().y1())>>zoomout);
						int y2 = (data.absBounds().y1() + (body.y2() - data.gBounds().y1())>>zoomout);
						int x2 = (data.absBounds().x1() + (body.x2() - data.gBounds().x1())>>zoomout);
						
						cHoverEnemy.renderFrame(r, x1, x2, y1, y2, 1, 8>>zoomout);
					}
					
				}
				
				
				if (task.targetTileX() !=-1 && data.tBounds().touches(task.targetTileX(), task.targetTileY())) {
					int x = task.targetTileX()*C.TILE_SIZE;
					int y = task.targetTileY()*C.TILE_SIZE;
					x = (data.absBounds().x1() + (x - data.gBounds().x1())>>zoomout);
					y = (data.absBounds().y1() + (y - data.gBounds().y1())>>zoomout);
					COLOR.RED2RED.bind();
					SPRITES.cons().BIG.dots.render(r, 0, x, y);
				}
			}
		}
		
		for (ArtilleryInstance ins : s.artillery.all()) {
			
			
			
			if (ins.hovered) {
				if (ins.army() != GAME.ARMIES().player()) {
					cHoverEnemy.bind();
				}else {
					cHover.bind();
				}
			}else if (ins.selected) {
				cHover.bind();
			}else {
				continue;
			}
			if (data.tBounds().touches(ins.body())) {
				int x1 = ins.body().x1()*C.TILE_SIZE;
				int y1 = ins.body().y1()*C.TILE_SIZE;
				x1 = (data.absBounds().x1() + (x1 - data.gBounds().x1())>>zoomout);
				y1 = (data.absBounds().y1() + (y1- data.gBounds().y1())>>zoomout);
				int w = ins.body().width()*C.TILE_SIZE>>zoomout;
				int h = ins.body().height()*C.TILE_SIZE>>zoomout;
				SPRITES.cons().BIG.outline.renderBox(r, x1, y1, w, h);
			}
			Div t = ins.targetDivGet();
			if (t != null && t.reporter.body().touches(data.gBounds())) {
				int x = t.centre().cX();
				int y = t.centre().cY();
				if (data.gBounds().holdsPoint(x, y)) {
					x = (data.absBounds().x1() + (x - data.gBounds().x1())>>zoomout);
					y = (data.absBounds().y1() + (y - data.gBounds().y1())>>zoomout);
					colAttack.bind();
					UI.icons().l.crossheir.renderCScaled(r, x, y, 2);
				}
			}
			COORDINATE coo = ins.targetCooGet();
			if (coo != null) {
				int x = coo.x();
				int y = coo.y();
				if (data.gBounds().holdsPoint(x, y)) {
					x = (data.absBounds().x1() + (x - data.gBounds().x1())>>zoomout);
					y = (data.absBounds().y1() + (y - data.gBounds().y1())>>zoomout);
					colAttack.bind();
					UI.icons().l.crossheir.renderCScaled(r, x, y, 2);
				}
			}
			
			
			
			
		}
		
		
		
		COLOR.unbind();
		
	}
	
	@Override
	public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {

	}
	
	private final DivFormationImp tmp = new DivFormationImp();
	private final BattleOrderPath pathDiv = new BattleOrderPath();
	private static FormationBody body = new FormationBody();
	
	public void renderBelow(SPRITE_RENDERER ren, RenderData data) {
		//OPACITY.O50.bind();
	
		
		SPRITE s = SPRITES.icons().s.circle;
		for (Div d : GAME.ARMIES().divisions()) {
			COLOR.GREEN40.bind();
			if ((d.army() == GAME.ARMIES().player() || S.get().developer) && (this.s.hovered(d) || this.s.selected(d) || KEYS.BATTLE().SHOW_DIVISIONS.isPressed())) {
				
				if ( S.get().developer)
					DivRenderer.render(ren, d.position(), data);
				
				if (body.init(d.current())){
					
					if (body.width() > d.position().width()*2 || body.height() > d.position().width()*2)
						body.init(d.position());
					
					int x1 = body.x1()-data.offX1();
					int y1 = body.y1()-data.offY1();
					int y2 = body.y2()-data.offY1();
					int x2 = body.x2()-data.offX1();
					
					(d.player() ? cHover : cHoverEnemy).renderFrame(ren, x1, x2, y1, y2, 1, 8);
				}
				
				if (this.s.hovered(d)) {
					COLOR.WHITE100.bind();
				}else
					COLOR.WHITE50.bind();
				d.order().task.get(task);
				if (task.task().showDest || S.get().developer) {
					d.order().dest.get(tmp);
					DivRenderer.render(ren, tmp, data);
				}
				if (task.task().showPath || S.get().developer) {
					d.order().path.get(pathDiv);
					if (pathDiv.length() > 0) {
						COLOR.ORANGE100.bind();
						int curr = pathDiv.currentI();
						int k = curr > 0 ? curr-1 : curr;
						for(int i = k; i < pathDiv.length(); i++) {
							pathDiv.setCurrentI(i);
							int rx = pathDiv.x()-s.width()/2;
							int ry = pathDiv.y()-s.width()/2;
							rx -= data.offX1();
							ry -= data.offY1();
							
							s.renderScaled(ren, rx, ry, C.SCALE);									
						}
						pathDiv.setCurrentI(curr);
						
					}
				}
				
				COLOR.unbind();
				
				
			}
		}
		COLOR.unbind();
	}
	

}
