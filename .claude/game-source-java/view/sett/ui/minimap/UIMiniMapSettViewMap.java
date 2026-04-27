package view.sett.ui.minimap;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.HALFENTS;
import static settlement.main.SETT.TILE_MAP;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DivFormationImp;
import game.battle.formation.DivPositionCopyable;
import game.battle.thread.order.BattleOrderPath;
import game.battle.thread.order.BattleOrderTask;
import game.time.TIME;
import init.constant.C;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.animal.spawning.AnimalSpawnSpot;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.thing.halfEntity.HalfEntity;
import settlement.tilemap.SettMarks.SettMark;
import settlement.tilemap.TerrainHotspots.TerrainHotSpot;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.ColorShifting;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GText;
import util.gui.panel.GFrame;
import view.main.VIEW;
import view.subview.GameWindow;

class UIMiniMapSettViewMap {

	private final Rec zoomWindow = new Rec(200);
	private final UIMinimapSettConfig config;
	
	UIMiniMapSettViewMap(UIMinimapSettConfig config) {
		this.config = config;
	}
	
	
	void render(Renderer r, float ds, GameWindow window, RECTANGLE absBounds, COORDINATE hoverPixel, boolean hovered) {
		
		r.newLayer(false, 0);
		
		//AmbientLight.full.register(0, C.WIDTH<<zoomout, 0, C.HEIGHT<<zoomout);
		TIME.light().applyGuiLight(0, 0, C.WIDTH(), 0, C.HEIGHT());
		
		int scale = C.TILE_SIZE>>window.zoomout();
		
		hovered &= VIEW.mouse().isWithinRec(absBounds);
		
		renderHotspots(window, absBounds, hovered);
		
		
		if (hovered) {
			
			renderBlack(window.zoomout(), window.pixels(), true);
			renderFrame();
			
			renderDivs(window, absBounds, true);
			r.newLayer(true, 0);
			renderSquares(window, absBounds, true);
			renderRooms(window, absBounds, true);
			
			r.newLayer(true, 0);
			renderEnts(r, window, absBounds, true);
			
			r.newLayer(true, 0);
			
			
			renderFractured(r, window, absBounds);
			GAME.s().render(r, ds, 2, hoverPixel.x(), hoverPixel.y(), zoomWindow, config);
			
		}else {
			zoomWindow.moveC(-500, -500);
			renderBlack(window.zoomout(), window.pixels(), false);
			renderDivs(window, absBounds, false);
			r.newLayer(true, 0);
			renderSquares(window, absBounds, true);
			renderRooms(window, absBounds, true);
			r.newLayer(true, 0);
			renderEnts(r, window, absBounds, true);
			r.newLayer(true, 0);
			
			SETT.MINIMAP().render(r, 
					window.pixels().x1()/(double)C.TILE_SIZE, window.pixels().y1()/(double)C.TILE_SIZE,
					absBounds.x1(), absBounds.y1(), 
					absBounds.width(), absBounds.height(), 
					scale
					);
			config.shade().bind();
			COLOR.BLACK.render(r, absBounds);
			OPACITY.unbind();
			
			
			
			
			
		}
		
		
	}
	
	private void renderFrame() {
		GCOLOR.UI().border().render(CORE.renderer(), zoomWindow.x1(), zoomWindow.x2(), zoomWindow.y1(), zoomWindow.y1()+8);
		GCOLOR.UI().border().render(CORE.renderer(), zoomWindow.x1(), zoomWindow.x2(), zoomWindow.y2()-8, zoomWindow.y2());
		GCOLOR.UI().border().render(CORE.renderer(), zoomWindow.x1(), zoomWindow.x1()+8, zoomWindow.y1(), zoomWindow.y2());
		GCOLOR.UI().border().render(CORE.renderer(), zoomWindow.x2()-8, zoomWindow.x2(), zoomWindow.y1(), zoomWindow.y2());
		GFrame.render(CORE.renderer(), 0, zoomWindow);
	}
	
	private final Rec tmp = new Rec();
	
	private void renderSquares(GameWindow window, RECTANGLE absBounds, boolean hovered){
		if (config.renderGrowable() || config.renderMinable()) {
			for (TerrainHotSpot s : TILE_MAP().hotspots().ALL()) {
				if (config.renderGrowable() && s.type == 0) {
					renderSquare(s.body(), COLOR.GREEN100, window, absBounds, hovered);
					renderSprite(s.icon, s.body().cX(), s.body().cY(), window, absBounds, hovered);
				}
				else if (config.renderMinable() && s.type == 1) {
					renderSquare(s.body(), COLOR.RED100, window, absBounds, hovered);
					renderSprite(s.icon, s.body().cX(), s.body().cY(), window, absBounds, hovered);
				}
			}
		}
		
		if (config.renderPack()) {
			for (AnimalSpawnSpot s : SETT.ANIMALS().spawn.all()) {
				if (s.active()) {
					tmp.setDim(16 + s.max());
					tmp.moveC(s);
					renderSquare(tmp, COLOR.WHITE85, window, absBounds, hovered);
					renderSprite(s.species().icon, s.x(), s.y(), window, absBounds, hovered);
				}
				
			}
			
		}
		
	}
	
	private void renderSquare(RECTANGLE rec, COLOR color, GameWindow window, RECTANGLE absBounds, boolean hovered){
		
		int x1 = absBounds.x1() + ((rec.x1()*C.TILE_SIZE - window.pixels().x1())>>window.zoomout()) -10;
		int y1 = absBounds.y1() + ((rec.y1()*C.TILE_SIZE - window.pixels().y1())>>window.zoomout()) -10;
		
		int x2 = absBounds.x1() + ((rec.x2()*C.TILE_SIZE - window.pixels().x1())>>window.zoomout()) + 10;
		int y2 = absBounds.y1() + ((rec.y2()*C.TILE_SIZE - window.pixels().y1())>>window.zoomout()) + 10;
		ColorImp.TMP.set(color).shadeSelf(0.5);
		renderSquare(x1, x2, y1, y2, ColorImp.TMP, 4, hovered);
		
		renderSquare(x1+1, x2-1, y1+1, y2-1, color, 1, hovered);
	}
	
	private void renderSquare(int x1, int x2, int y1, int y2, COLOR color, int d, boolean hovered){
		
		renderSquare(x1, x2, y1, y1+d, color, hovered);
		renderSquare(x1, x2, y2-d, y2, color, hovered);
		renderSquare(x1, x1+d, y1, y2, color, hovered);
		renderSquare(x2-d, x2, y1, y2, color, hovered);
	}
	
	private void renderSquare(int x1, int x2, int y1, int y2, COLOR color, boolean hovered){
		
		if (x2-x1 <= 0 || y2-y1 <= 0)
			return;
		
		if (hovered && zoomWindow.touches(x1, x2, y1, y2)) {
			if (x1 < zoomWindow.x2())
				renderSquare(zoomWindow.x2(), x2, y1, y2, color, hovered);
			if (x2 > zoomWindow.x1())
				renderSquare(x1, zoomWindow.x1(), y1, y2, color, hovered);
			if (y1 < zoomWindow.y2())
				renderSquare(x1, x2, zoomWindow.y2(), y2, color, hovered);
			if (y2 > zoomWindow.y1())
				renderSquare(x1, x2, y1, zoomWindow.y1(), color, hovered);
			
		}else {
			color.render(CORE.renderer(), x1, x2, y1, y2);
		}
		
	}
	
	private COLOR rCol = new ColorShifting(COLOR.WHITE65, COLOR.WHITE150);
	
	private void renderRooms(GameWindow window, RECTANGLE absBounds, boolean hovered){
		for (RoomBlueprint bb : SETT.ROOMS().all()) {
			if (bb instanceof RoomBlueprintIns<?>) {
				RoomBlueprintIns<?> b = (RoomBlueprintIns<?>) bb;
				if (config.room(b)) {
					for (int i = 0; i < b.instancesSize(); i++) {
						RoomInstance ins = b.getInstance(i);
						renderSprite(b.iconBig(), ins.body().cX(), ins.body().cY(), window, absBounds, hovered);
					}
				}
				
			}
		}
		
	
		
	}
	
	private final DivFormationImp forCurrent = new DivFormationImp();
	private final BattleOrderPath path = new BattleOrderPath();
	private final BattleOrderTask task = new BattleOrderTask();
	
	private void renderDivs(GameWindow window, RECTANGLE absBounds, boolean hovered){
		
		if (!config.renderDivs())
			return;
		
		for (Div div : GAME.ARMIES().divisions()) {
			
			if (div.menNrOf() == 0)
				continue;
			DivPositionCopyable pos = div.current();
			if (pos.deployed() == 0)
				continue;
			if (div.centre().cX() < 0) {
				continue;
			}
			
			int cardX = div.centre().cX();
			int cardY = div.centre().cY();

			
			
			if (!div.settings().mustering()) {
				div.order().dest.get(forCurrent);
				COORDINATE c = forCurrent.centrePixel();
				if (c != null && forCurrent.deployed() > 0) {
					cardX = c.x();
					cardY = c.y();
				}else {
					continue;
				}
			}
			
			SPRITE icon = VIEW.UI().div.battle.miniDiv(div, false, false);
			
			cardX = absBounds.x1() + ((cardX - window.pixels().x1())>>window.zoomout());
			cardY = absBounds.y1() + ((cardY - window.pixels().y1())>>window.zoomout());
			int d = icon.width()/2;
			int x1 = cardX-d;
			int x2 = cardX +d;
			int y1 = cardY-d;
			int y2 = cardY+d;
			if (!hovered || !zoomWindow.touches(x1, x2, y1, y2)) {
				icon.renderC(CORE.renderer(), cardX, cardY);
			}
			
			if (forCurrent.deployed() > 0) {
				
				div.order().task.get(task);
				if (task.task().showDest || S.get().developer) {
					
					int men = forCurrent.deployed();
					
					if (men == 0)
						continue;
					
					if (!forCurrent.body().touches(window.pixels())) {
						continue;
					}
					
					for (int i = 0; i < men; i++) {
						int m = forCurrent.dirMaskOrtho(i);
						if (m != 0x0F) {
							COORDINATE c = forCurrent.pixel(i);
							
							int x = absBounds.x1() + ((c.x() - window.pixels().x1())>>window.zoomout());
							int y = absBounds.y1() + ((c.y() - window.pixels().y1())>>window.zoomout());

							if (!hovered || !zoomWindow.touches(x, x+1, y, y+1)) {
								CORE.renderer().renderParticle(x, y);
							}	
						}
						
						
					}
				}
				
				if (div.settings().mustering() && (task.task().showDest || S.get().developer)) {
					
					div.order().path.get(path);
					if (path.length() > 0) {
						COLOR.ORANGE100.bind();
						int curr = path.currentI();
						int k = curr > 0 ? curr-1 : curr;
						
						for(int i = k; i < path.length(); i++) {
							path.setCurrentI(i);
							
							int x = absBounds.x1() + ((path.x() - window.pixels().x1())>>window.zoomout());
							int y = absBounds.y1() + ((path.y() - window.pixels().y1())>>window.zoomout());
							
							
							if (!hovered || !zoomWindow.touches(x, x+1, y, y+1)) {
								CORE.renderer().renderParticle(x, y);
							}					
						}
					}
				}
				
				
			}
		}
		
	}
	
	private void renderSprite(SPRITE icon, int cx, int cy, GameWindow window, RECTANGLE absBounds, boolean hovered) {
		if (window.tiles().holdsPoint(cx, cy)) {
			cx = absBounds.x1() + ((cx*C.TILE_SIZE - window.pixels().x1())>>window.zoomout());
			cy = absBounds.y1() + ((cy*C.TILE_SIZE - window.pixels().y1())>>window.zoomout());
			int d = icon.width()/2;
			int x1 = cx-d;
			int x2 = cx +d;
			int y1 = cy-d;
			int y2 = cy+d;
			if (!hovered || !zoomWindow.touches(x1, x2, y1, y2)) {
				COLOR.BLACK.bind();
				icon.renderC(CORE.renderer(), cx+4, cy+4);
				
				rCol.bind();
				icon.renderC(CORE.renderer(), cx, cy);
				COLOR.unbind();
			}
			
			
		}
	}
	

	
	private void renderBlack(int zoom, RECTANGLE game, boolean hovered) {
		int x1 = Math.max(0, -game.x1())>>zoom;
		int x2 = (Math.min(game.x2(), SETT.PWIDTH));
		x2 -= game.x1();
		x2 = x2 >> zoom;
		int y1 = Math.max(0, -game.y1())>>zoom;
		int y2 =  (Math.min(game.y2(), SETT.PHEIGHT));
		y2 -= game.y1();
		y2 = y2 >> zoom;
		
		COLOR c = COLOR.WHITE20;
		
		renderColor(c, 0, x1, 0, C.DIM().height(), hovered);
		renderColor(c, x2, C.DIM().width(), 0, C.DIM().height(), hovered);
		renderColor(c, 0,C.DIM().width(), 0, y1, hovered);
		renderColor(c, 0,C.DIM().width(), y2, C.DIM().height(), hovered);
		
		c = GCOLOR.UI().border();
		renderColor(c, x1-3, x2+3, y1-3, y1, hovered);
		renderColor(c, x1-3, x2+3, y2, y2+3, hovered);
		renderColor(c, x1-3, x1, y1, y2, hovered);
		renderColor(c, x2, x2+3, y1, y2, hovered);
		
	}
	
	private void renderColor(COLOR col, int x1, int x2, int y1, int y2, boolean hovered) {
		if (x1 >= x2)
			return;
		if (y1 >= y2)
			return;
		if (hovered && zoomWindow.touches(x1, x2, y1, y2)) {
			if (x1 < zoomWindow.x2())
				renderColor(col, zoomWindow.x2(), x2, y1, y2, hovered);
			if (x2 > zoomWindow.x1())
				renderColor(col, x1, zoomWindow.x1(), y1, y2, hovered);
			if (y1 < zoomWindow.y2())
				renderColor(col, x1, x2, zoomWindow.y2(), y2, hovered);
			if (y2 > zoomWindow.y1())
				renderColor(col, x1, x2, y1, zoomWindow.y1(), hovered);
		}else {
			col.render(CORE.renderer(), x1, x2, y1, y2);
		}
	}
	

	

	
	private void renderFractured(Renderer r, GameWindow window, RECTANGLE absBounds) {
		
		int scale = C.TILE_SIZE>>window.zoomout();
		final int mmx = zoomWindow.x1()-absBounds.x1();
		int mmy = zoomWindow.y1()-absBounds.y1();
		if (mmy < 0)
			mmy = 0;
		int mmy2 = zoomWindow.y2()-absBounds.y1();
		if (mmy2 < 0)
			mmy2 = 0;
		int mx1;
		int mx2;
		int my1;
		int my2;
		
		mx1 = 0;
		mx2 = absBounds.width();
		my1 = 0;
		my2 = mmy;
		if (mx2-mx1 > 0 && my2-my1 > 0) {
			double px = window.pixels().x1()/(double)C.TILE_SIZE + mx1/(double)scale;
			double py = window.pixels().y1()/(double)C.TILE_SIZE + my1/(double)scale;
			
			ren(r, 
					px, py,
					mx1+absBounds.x1(), my1+absBounds.y1(), 
					mx2-mx1+2, my2-my1+2, 
					scale);
		}
		
		mx1 = 0;
		mx2 = mmx;
		my1 = mmy;
		my2 = mmy2;
		if (mx2-mx1 > 0 && my2-my1 > 0) {
			double px = window.pixels().x1()/(double)C.TILE_SIZE + mx1/(double)scale;
			double py = window.pixels().y1()/(double)C.TILE_SIZE + my1/(double)scale;

			
			ren(r, 
					px, py,
					mx1+absBounds.x1(), my1+absBounds.y1(), 
					mx2-mx1, my2-my1+2, 
					scale);
			
		}
		
		mx1 = mmx+zoomWindow.width();
		mx2 = absBounds.width();
		my1 = mmy;
		my2 = mmy2;
		if (mx2-mx1 > 0 && my2-my1 > 0) {
			double px = window.pixels().x1()/(double)C.TILE_SIZE + mx1/(double)scale;
			double py = window.pixels().y1()/(double)C.TILE_SIZE + my1/(double)scale;

			
			ren(r, 
					px, py,
					mx1+absBounds.x1(), my1+absBounds.y1(), 
					mx2-mx1+1, my2-my1+1, 
					scale);
			
		}
		
		mx1 = 0;
		mx2 = absBounds.width();
		my1 = mmy2;
		my2 = absBounds.height();
		if (mx2-mx1 > 0 && my2-my1 > 0) {
			double px = window.pixels().x1()/(double)C.TILE_SIZE + mx1/(double)scale;
			double py = window.pixels().y1()/(double)C.TILE_SIZE + my1/(double)scale;
			
			
			ren(r, 
					px, py,
					mx1+absBounds.x1(), my1+absBounds.y1(), 
					mx2-mx1+1, my2-my1, 
					scale);
			
		}
		
		
	}
	
	private void ren(Renderer r, double px, double py, int x1, int y1, int x2, int y2, int scale) {
		SETT.MINIMAP().render(r, 
				px, py,
				x1, y1, 
				x2, y2, 
				scale);
		config.shade().bind();
		int dy = (int) ((scale)*(py-(int)py));
		COLOR.BLACK.render(r, x1, x1+x2, y1-dy, y1+y2+dy*2); 
		OPACITY.unbind();
	}
	
	private final ColorImp col = new ColorImp();
	
	private void renderEnts(Renderer r, GameWindow window, RECTANGLE absBounds, boolean hovered) {
		
		int am = ENTITIES().Imax();
		ENTITY[] es = ENTITIES().getAllEnts();
		
		if (am > 20000) {
			for (int ei = 0; ei < am; ei++) {
				ENTITY e = es[ei];
				if (e == null)
					continue;
				COLOR c = config.col(e);
				if (c != null && e.physics.body().isWithin(window.pixels())) {
					
					int x1 = absBounds.x1() + ((e.physics.body().x1() - window.pixels().x1())>>window.zoomout());
					int y1 = absBounds.y1() + ((e.physics.body().y1() - window.pixels().y1())>>window.zoomout());
					if (hovered && zoomWindow.holdsPoint(x1, y1))
						continue;
					c.bind();
					r.renderParticle(x1, y1);
					
				}
			}
		}else {
			for (int ei = 0; ei < am; ei++) {
				ENTITY e = es[ei];
				if (e == null)
					continue;
				COLOR c = config.col(e);
				if (c != null && e.physics.body().isWithin(window.pixels())) {
					
					int x1 = absBounds.x1() + ((e.physics.body().x1() - window.pixels().x1())>>window.zoomout());
					int y1 = absBounds.y1() + ((e.physics.body().y1() - window.pixels().y1())>>window.zoomout());
					if (hovered && zoomWindow.holdsPoint(x1, y1))
						continue;
				
					col.interpolate(c, COLOR.WHITE100, 0.5).bind();;
					//COLOR.WHITE100.bind();
					r.renderParticle(x1-1, y1-1);
					col.interpolate(c, COLOR.BLACK, 0.5).bind();;
					//COLOR.BLACK.bind();
					r.renderParticle(x1+1, y1+1);
					c.bind();
					r.renderParticle(x1, y1);
					
				}
			}
		}
	
		
		if (config.halfEnts()) {
			for (HalfEntity e : HALFENTS().all()) {
				if (e == null)
					continue;
				
				if (window.tiles().holdsPoint(e.ctx(), e.cty())) {
					
					int x1 = absBounds.x1() + ((e.body().x1() - window.pixels().x1())>>window.zoomout());
					int y1 = absBounds.y1() + ((e.body().y1() - window.pixels().y1())>>window.zoomout());
					if (hovered && zoomWindow.holdsPoint(x1, y1))
						continue;
					COLOR.BROWN.bind();
					r.renderParticle(x1, y1);
					
					
				}
			}
		}
		
		
		
		COLOR.unbind();
		OPACITY.unbind();
	}

	void update(){
		COORDINATE m = VIEW.mouse();
		zoomWindow.moveCX(m.x());
		if (zoomWindow.x1() <= 2)
			zoomWindow.moveX1(2);
		if (zoomWindow.x2() > C.DIM().width())
			zoomWindow.moveX2(C.WIDTH());
		
		zoomWindow.moveY1(m.y() - Icon.M - zoomWindow.height() < 0 ? 
				m.y() + Icon.M : m.y() - Icon.M - zoomWindow.height());
	}
	
	private final GText name = new GText(UI.FONT().H1, 20);
	
	private void renderHotspots(GameWindow window, RECTANGLE absBounds, boolean hovered) {
		
		if (window.zoomout() >= window.zoomoutmax()-1) {
			name.setFont(UI.FONT().H1);
		}else {
			name.setFont(UI.FONT().H1);
		}
		
		for (SettMark d : SETT.TILE_MAP().marks.active()) {
			if (!d.active)
				continue;
			if (d.tile.isWithinRec(window.tiles())) {
				int cx = absBounds.x1() + ((d.tile.x()*C.TILE_SIZE-window.pixels().x1())>>window.zoomout());
				int cy = absBounds.y1() + ((d.tile.y()*C.TILE_SIZE-window.pixels().y1())>>window.zoomout());
				name.set(d.name).toLower();
				int x1 = cx - name.width()/2;
				int y1 = cy - name.height()/2;
				
				if (zoomWindow.touches(x1, x1+name.width()*2, y1, y1+name.height()*2))
					continue;
				
				d.color.bind();
				UI.FONT().H1.render(CORE.renderer(), d.name, x1, y1);
				
			}
			
			
		}
		COLOR.unbind();
	}
	

	

}
