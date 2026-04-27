package world.entity;

import static world.WORLD.CLIMATE;
import static world.WORLD.REGIONS;
import static world.WORLD.TBOUNDS;

import init.type.CLIMATE;
import init.type.TERRAIN;
import init.type.TERRAINS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;
import snake2d.util.sprite.text.Str;
import world.WORLD;
import world.entity.haven.WHavenType;
import world.entity.haven.WHavens;

final class Generator {
	
	public Generator() {
		
		WHavens cc = WORLD.ENTITIES().havens;
		
		ArrayList<WW> spots = new ArrayList<>(cc.types.size());
		for (int i = 0; i < cc.types.size(); i++)
			spots.add(new WW(cc.types.get(i)));
		
		
		for (COORDINATE c : TBOUNDS()) {
			
			if (!REGIONS().map.is(c))
				continue;
			if (WORLD.REGIONS().isCentre.is(c))
				continue;
			if (WORLD.MOUNTAIN().haser.is(c.x(), c.y()))
				continue;
			if (WORLD.WATER().has.is(c))
				continue;
			if (WORLD.FOREST().amount.get(c) > 0.25)
				continue;
			
			CLIMATE cl = CLIMATE().getter.get(c);
			for (WW w : spots) {
				w.add(c.x(), c.y(), cl);
			}
			
		}
		
		int[] ni = new int[cc.types.size()];
		
		other:
		while(!spots.isEmpty()) {
			WW w = spots.get(RND.rInt(spots.size()));
			if (w.am <= 0 || !w.spots.hasMore()) {
				spots.remove(w);
				continue;
			}
			
			Coovalue c = w.spots.pollGreatest();
			
			for (DIR d : DIR.ALLC) {
				if (WORLD.ENTITIES().havens.fillTile(c.tx+d.x(), c.ty+d.y()).size() > 0) {
					continue other;
				}
			}
			
			Str.TMP.clear().add(w.w.names.getC(ni[w.w.index()]++));
			Str.TMP.insert(0, w.w.race.appearance().lastNamesNoble.getC(RND.rInt(0x0FFFF)));
			
			cc.create(c.tx, c.ty, w.w, RND.rFloat(), Str.TMP);
			w.am--;
			
			
		}
	}
	
	private static class WW {
		
		double am = 0;
		private final Tree<Coovalue> spots = new Tree<Coovalue>(1024) {

			@Override
			protected boolean isGreaterThan(Coovalue current, Coovalue cmp) {
				return current.value > cmp.value;
			}
		};
		public final WHavenType w;
		WW(WHavenType w){
			this.w = w;
		}
		
		void add(int tx, int ty, CLIMATE cl) {
			double res = 0;
			for (TERRAIN t : TERRAINS.ALL()) {
				res += w.climates[cl.index()]*w.terrains[t.index()]*t.value(tx, ty);
			}
			am+=res;
			
			if (!spots.hasRoom()) {
				if (spots.smallest().value < res)
					spots.pollSmallest();
				else
					return;
			}
			
			
			Coovalue v = new Coovalue();
			v.value = res*RND.rFloat();
			v.tx = (short) tx;
			v.ty = (short) ty;
			spots.add(v);
		}
		
	}
	
	private static class Coovalue {
		short tx;
		short ty;
		double value;
	}
	


}
