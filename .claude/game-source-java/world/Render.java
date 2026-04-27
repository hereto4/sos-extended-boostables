package world;

import game.GAME;
import game.time.TIME;
import init.settings.S;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.color.RGB;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.light.PointLight;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD.WorldResource;

class Render {

	private final ShadowBatch.Real shadowBatch = new ShadowBatch.Real();
	private final ShadowBatch shadowDummy = new ShadowBatch.Dummy();
	private final PointLight light = new PointLight();

	
	final WRenContext rContext;
	
	Render(int width, int height){
		rContext = new WRenContext(width, height);
		light.setGreen(1).setRed(2).setBlue(0.5);
		light.setFalloff(1);
	}
	
	public void render(Renderer r, float ds, int zoomout,
			RECTANGLE renWindow, int offX, int offY) {

		ds *= GAME.SPEED.speedTarget();
		
		ShadowBatch s = shadowDummy;
		if (S.get().shadows.get() > 0){
			shadowBatch.init(zoomout, TIME.light().shadow.sx(), TIME.light().shadow.sy());
			s = shadowBatch;
		}
		rContext.init(r, s, renWindow, offX, offY, ds);
		double seasonValue = 0;
		{
			double am = 0;
			RenderIterator it = rContext.data.onScreenTiles();
			while(it.has()) {
				am++;
				seasonValue += WORLD.CLIMATE().getter.get(it.tile()).seasonChange;
				it.next();
			}
			seasonValue/=am;
		}
		{
			double am = 0;
			RenderIterator it = rContext.data.onScreenTiles();
			while(it.has()) {
				am++;
				seasonValue += WORLD.CLIMATE().getter.get(it.tile()).seasonChange;
				it.next();
			}
			seasonValue/=am;
		}
		
		r.newLayer(false, zoomout);
		TIME.light().applyGuiLight(ds, offX, offX + renWindow.width(), offY,
				offY + renWindow.height());
		WORLD.OVERLAY().render(r, s, rContext.data, zoomout);
		
		r.newFinalLightWithShadows(zoomout, this);
		TIME.light().apply(offX, offX + renWindow.width(), offY,
				offY + renWindow.height(), RGB.WHITE);
		CORE.renderer().setUniLight(light);
		
		WORLD.FOW().render(rContext);
		r.newLayer(false, zoomout);
		
		tileRenderAboveTerrain(rContext);
		WORLD.ENTITIES().renderAboveTerrain(r, s, ds, renWindow, offX, offY);
		r.newLayer(false, zoomout);
		
		WORLD.WATER().render(r, rContext.data, seasonValue);
		r.newLayer(false, zoomout);
		
		WORLD.FOREST().render(r, s, rContext.data);
		r.newLayer(false, zoomout);
		
		COLOR.unbind();
		OPACITY.unbind();
		WORLD.MOUNTAIN().render(r, s, rContext.data);
		r.newLayer(false, zoomout);
		
		WORLD.ENTITIES().renderBelowTerrain(r, s, ds, renWindow, offX, offY);
		r.newLayer(false, zoomout);
		
		tileRenderAbove(rContext);
		r.newLayer(false, zoomout);
		
		if (!WORLD.OVERLAY().renderBelow(r, s, rContext.data, zoomout)) {
			tileRenderAboveGround(rContext, seasonValue);
		}
		r.newLayer(false, zoomout);
		
		
		
		CORE.getSoundCore().set(renWindow.cX()+offX, renWindow.cY()+offY);
		
		for (WorldResource res : WORLD.RESOURCES())
			res.afterRender();
	}
	
	private void tileRenderAboveGround(WRenContext data, double season){
		
		
		
		RenderIterator it = data.data.onScreenTiles();
		WORLD.GROUND().renderInit(season);
		
		while (it.has()) {
			
			WORLD.GROUND().render(CORE.renderer(), it);
			WORLD.BUILDINGS().renderAboveGround(data, it);
			
			WORLD.ROADS().render(data, it);
			WORLD.WATER().renderShorelines(CORE.renderer(), it);
			WORLD.REGIONS().renderBorders(CORE.renderer(), it);
			it.next();
		}
		WORLD.CENTRE().sprite.renderGround(data);
		COLOR.unbind();
	}
	
	

	private void tileRenderAbove(WRenContext data){
		
		WORLD.CENTRE().sprite.renderAbove(data);
		RenderIterator it = data.data.onScreenTiles(0,1,0,1);
		
		while(it.has()) {
			
			WORLD.BUILDINGS().renderAbove(data, it);
			it.next();
			
		}
		
	}
	
	
	private void tileRenderAboveTerrain(WRenContext data){

		WORLD.CENTRE().sprite.renderAboveTerrain(data);
		RenderIterator it = data.data.onScreenTiles(0,0,0,0);
		
		
		
		while(it.has()) {
			WORLD.ROADS().renderBridge(data, it);
			WORLD.BUILDINGS().renderAboveTerrain(data, it);
			it.next();
			
		}
		
	}
	
}
