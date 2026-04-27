package world.map.pathing;

import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LinkedList;
import util.GUTIL;
import world.WORLD;
import world.map.regions.Region;
import world.map.road.WTRAV;

final class GenLand {

	private final Bitmap2D tmp = new Bitmap2D(WORLD.TBOUNDS(), false);
	private final ACTION u;

	GenLand(ACTION util) {
		this.u = util;
		
		Flooder f = GUTIL.flooder();
		f.init(this);

		LinkedList<COORDINATE> nodes = new LinkedList<>();

		for (Region r : WORLD.REGIONS().all()) {

			if (r.info.area() > 0) {
				f.pushSloppy(r.cx(), r.cy(), 0, null);
				f.setValue2(r.cx(), r.cy(), r.index());
			}
		}

		tmp.clear();
		while (f.hasMore()) {

			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());

			Region rr = WORLD.REGIONS().map.get(t);

			for (DIR d : DIR.ALL) {
				if (WTRAV.can(t.x(), t.y(), d, true)) {
					Region other = WORLD.REGIONS().map.get(t, d);
					if (other != rr) {
						continue;
					}
					f.pushSmaller(t, d, t.getValue() + d.tileDistance(), t);
				}
			}
		}
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (f.hasBeenPushed(c.x(), c.y())) {

				int from = (int) f.getValue2(c.x(), c.y());

				if (WORLD.ROADS().harbour.is(c)) {
					Gen.connect(f.get(c.x(), c.y()));
				} else if (WORLD.WATER().isBig.is(c))
					continue;
				else {
					for (DIR d : DIR.ALL) {
						if (f.hasBeenPushed(c.x(), c.y(), d) && WTRAV.canLand(c.x(), c.y(), d, true)) {
							int regTo = (int) f.getValue2(c.x(), c.y(), d);
							if (from != regTo) {
								markAdd(f.get(c.x(), c.y()), nodes);
								markAdd(f.get(c.x() + d.x(), c.y() + d.y()), nodes);

							}
						}
					}
				}
			}
		}
		u.exe();
		f.done();
		int i = 0;
		while (!nodes.isEmpty()) {
			if (i++ % 10 == 0)
				u.exe();
			f.init(this);
			Region start = WORLD.REGIONS().map.get(nodes.removeFirst());
			Region end = WORLD.REGIONS().map.get(nodes.removeFirst());
			f.pushSloppy(start.cx(), start.cy(), 0);

			while (f.hasMore()) {

				PathTile t = f.pollSmallest();
				if (t.isSameAs(end.cx(), end.cy())) {

					Gen.connect(t);
					break;
				}

				for (DIR d : DIR.ALL) {
					if (tmp.is(t.x(), t.y(), d) && WTRAV.can(t.x(), t.y(), d, true)) {
						Region other = WORLD.REGIONS().map.get(t, d);
						if (other != start && other != end) {
							continue;
						}
						double v = WORLD.PATH().map.can(t.x(), t.y(), d) ? 0.5 : 1;
						f.pushSmaller(t, d, t.getValue() + v * d.tileDistance(), t);
					}
				}
			}
			f.done();

		}
		util.exe();

	}

	private void markAdd(PathTile t, LinkedList<COORDINATE> li) {
		while (t != null) {
			tmp.set(t, true);
			if (t.getParent() == null)
				li.add(t);
			t = t.getParent();
		}
	}



}
