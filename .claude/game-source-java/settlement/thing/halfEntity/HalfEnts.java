package settlement.thing.halfEntity;

import java.io.IOException;

import game.debug.Profiler;
import init.constant.C;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.thing.halfEntity.caravan.Caravans;
import settlement.thing.halfEntity.crate.TransportFactory;
import settlement.thing.halfEntity.dingy.DingyFactory;
import settlement.thing.halfEntity.halfCorpse.MovingCorpseFactory;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Tree;
import util.rendering.ShadowBatch;

public class HalfEnts extends SettResource {

	private final ArrayListResize<HalfEntity> ents;
	private final _WMap map;
	private final ArrayList<HalfEntity> tmp = new ArrayList<HalfEntity>(2056);
	private final ArrayList<Factory<?>> constructors = new ArrayList<>(20);

	public final Caravans caravans = new Caravans(constructors);
	public final TransportFactory transports = new TransportFactory(constructors);
	public final MovingCorpseFactory corpses = new MovingCorpseFactory(constructors);
	public final DingyFactory dingy = new DingyFactory(constructors);
	private final Tree<HalfEntity> renderables = new Tree<HalfEntity>(2056) {

		@Override
		protected boolean isGreaterThan(HalfEntity current, HalfEntity cmp) {
			return current.getZ() < cmp.getZ();
		}
		
	};

	public HalfEnts() throws IOException {
		super("HALF_ENTS", true);
		ents = new ArrayListResize<HalfEntity>(1024, 64000);
		map = new _WMap(SETT.PWIDTH, SETT.PHEIGHT);

	}

	@Override
	protected void load(FileGetter file) throws IOException {
		ents.clear();
		map.clear();

		int am = file.i();

		for (int i = 0; i < am; i++) {
			int ci = file.i();
			Factory<?> c = constructors.get(ci);
			HalfEntity e = c.create();
			e = e.load(file);
			e.hitBox.load(file);
			clear(e);
			e.index = ents.add(e);
			e.renderNext = null;
			map.add(e);
		}
		for (Factory<?> f : constructors)
			f.load(file);
	}

	private void clear(HalfEntity e) {
		e.index = -1;
		e.renderNext = null;
		e.gridX = -1;
		e.gridY = -1;
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(ents.size());
		for (HalfEntity e : ents) {
			file.i(e.constructor().index);
			e.save(file);
			e.hitBox.save(file);
		}
		for (Factory<?> f : constructors)
			f.save(file);
	}
	
	@Override
	protected void clear() {
		map.clear();
		for (HalfEntity e : ents)
			clear(e);
		ents.clear();
		for (Factory<?> f : constructors)
			f.clear();
	}

	void add(HalfEntity e) {
		if (e.index != -1)
			throw new RuntimeException();
		int i = ents.add(e);
		clear(e);
		e.index = i;
		map.add(e);

	}

	void remove(HalfEntity e) {
		if (e.index == -1)
			throw new RuntimeException();
		map.remove(e);

		HalfEntity e2 = ents.remove(ents.size() - 1);

		if (e2 != e) {
			ents.replace(e.index, e2);
			e2.index = e.index;
		}
		clear(e);
		e.constructor().returnT(e);
	}



	@Override
	public void update(double ds, Profiler profiler) {
		for (int i = 0; i < ents.size(); i++) {
			HalfEntity e = ents.get(i);
			e.update(ds);
			if (!e.added())
				i--;
			else
				map.move(e);
		}
	}

	public LIST<HalfEntity> all(){
		return ents;
	}

	public void fill(COORDINATE coo, LISTE<HalfEntity> result) {
		fill(coo.x(), coo.y(), result);
	}

	public void fill(int x, int y, LISTE<HalfEntity> result) {
		map.fill(x,x,y, y, result);
	}
	
	public void fill(int x1, int x2, int y1, int y2, LISTE<HalfEntity> result) {
		map.fill(x1, x2, y1, y2, result);
	}

	public HalfEntity getTallest(COORDINATE coo) {
		tmp.clear();
		fill(coo, tmp);
		HalfEntity tallest = null;
		double dist = Double.MAX_VALUE;
		for (HalfEntity e : tmp) {
			double d = coo.distance(e.body().cX(), e.body().cY());
			if (tallest == null || d < dist) {
				tallest = e;
				dist = d;
			}
		}
		return tallest;
	}

	public void renderInit( RECTANGLE renWindow) {
		renderables.clear();
		int min = C.TILE_SIZE*7;
		map.fill(renWindow.x1()-min, renWindow.x2()+min, renWindow.y1()-min, renWindow.y2()+min, renderables);
		tmp.clear();

		HalfEntity e;
		while (renderables.hasMore()) {
			e = renderables.pollGreatest();
			tmp.add(e);
		}
		
	}
	
	public void renderBelow(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {

		offX = offX - renWindow.x1();
		offY = offY - renWindow.y1();

		for (HalfEntity e : tmp) {
			e.renderBelow(r, s, ds, e.body().x1() + offX, e.body().y1() + offY);
		}

	}

	public void render(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {

		offX = offX - renWindow.x1();
		offY = offY - renWindow.y1();

		COLOR.unbind();
		
		
		for (HalfEntity e : tmp) {
			e.render(r, s, ds, e.body().x1() + offX, e.body().y1() + offY);
		}

	}
	
	public void renderAbove(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {
		
		offX = offX - renWindow.x1();
		offY = offY - renWindow.y1();

		for (HalfEntity e : tmp) {
			e.renderAbove(r, s, ds, e.body().x1() + offX, e.body().y1() + offY);
		}

	}
	
	public void renderZoomed(Renderer r, ShadowBatch shadowBatch, float ds, RECTANGLE renWindow, int offX, int offY) {
		
		
		
		for (HalfEntity e : all()) {
			if (e == null)
				continue;
			
			
			int x1 = e.body().cX();
			int y1 = e.body().cY();
			
			
			if (!renWindow.holdsPoint(x1, y1))
				continue;
			x1 -= (renWindow.x1()-offX) + C.TILE_SIZEH;
			y1 -= (renWindow.y1()-offY) + C.TILE_SIZEH;
			COLOR.BROWN.bind();
			SPRITES.cons().TINY.high.get(0).render(r, x1, y1);
		}
		COLOR.unbind();
		
	}
	

}
