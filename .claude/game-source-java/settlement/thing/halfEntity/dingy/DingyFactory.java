package settlement.thing.halfEntity.dingy;

import java.io.IOException;

import game.time.TIME;
import init.constant.C;
import init.resources.RESOURCE;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.thing.halfEntity.Factory;
import snake2d.Renderer;
import snake2d.util.MATH;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import util.rendering.ShadowBatch;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PlacableSimpleTile;

public class DingyFactory extends Factory<Dingy>{

	public final Sprite sprite = new Sprite();

	public DingyFactory(LISTE<Factory<?>> all) throws IOException {
		super(all);

		PLACABLE pp = new PlacableSimpleTile("Dingy place") {
			
			@Override
			public void place(int tx, int ty) {
				
				Humanoid h = h();
				if (h != null)
					make(h, tx, ty, SETT.ROOMS().FISHERIES.get(0).industries().get(0).outs().get(0).resource, RND.rInt(), DIR.ALL.rnd());
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return SETT.TERRAIN().WATER.DEEP.is(tx, ty) || SETT.TERRAIN().WATER.BRIDGE.is(tx, ty) ? null : E;
			}
			
			private Humanoid h() {
				for (int i = 0; i < SETT.ENTITIES().getAllEnts().length; i++) {
					ENTITY e = SETT.ENTITIES().getAllEnts()[i];
					if (e instanceof Humanoid)
						return (Humanoid) e;
				}
				return null;
			}
			
		};
		
		IDebugPanelSett.add(pp);
		
	}

	@Override
	protected void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Dingy make() {
		return new Dingy();
	}

	public boolean make(Humanoid h, int tx, int ty, RESOURCE rCatch, int up, DIR dir) {
		Dingy e = create();
		return e.init(h, tx, ty, rCatch, up, dir);
	}
	
	public void renderBoat(Renderer r, ShadowBatch s, int cx, int cy, DIR dir, int ran, int up) {
		
		int x1 = cx-16*C.SCALE;
		int y1 = cy-16*C.SCALE;
		
		double sp = 10.0/(1+(ran&0b1111));
		ran = ran >> 4;
		int f = (ran & 0b1111) + (int) (sp*TIME.currentSecond());
		ran = ran >> 4;
		int df = MATH.distanceC(8, f, 16);
		x1 += df;
		
		sp = 10.0/(1+(ran&0b1111));
		ran = ran >> 4;
		f = (ran & 0b1111) + (int) (sp*TIME.currentSecond());
		ran = ran >> 4;
		df = MATH.distanceC(8, f, 16);
		y1 += df;
		
		sprite.render(r, s, dir.id(), x1, y1, ran>>1, up);
		
	}
	
}
