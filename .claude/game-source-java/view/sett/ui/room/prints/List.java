package view.sett.ui.room.prints;

import game.faction.FACTIONS;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.copy.SavedPrints.SavedPrint;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import view.main.VIEW;

final class List {

	private final Cat[] catmap = new Cat[SETT.ROOMS().all().size()];
	private final ArrayListGrower<Cat> cats = new ArrayListGrower<>();
	private ArrayList<Entry> free = new ArrayList<Entry>();
	private ArrayList<Entry> current = new ArrayList<Entry>();
	private final StringInputSprite filter;

	List(StringInputSprite filter) {
		this.filter = filter;
		for (RoomBlueprintImp b : SETT.ROOMS().imps()) {
			if (SETT.ROOMS().copy.prints.canAdd(b)) {
				for (Cat c : cats) {
					if (c != null && c.prints.get(0).getClass() == b.getClass()
							&& c.prints.get(0).constructor().mustBeIndoors() == b.constructor().mustBeIndoors()) {
						catmap[b.index()] = c;
						catmap[b.index()].prints.add(b);
						break;
					}
				}

				if (catmap[b.index()] == null) {
					catmap[b.index()] = new Cat(b);
					cats.add(catmap[b.index()]);
				}
			}
		}
		next(0);

	}

	private int vi = -1;

	public LIST<Entry> get() {
		if (vi == VIEW.RI())
			return current;
		vi = VIEW.RI();

		int fi = 0;
		current.clearSloppy();
		for (Cat c : cats) {
			if (c == null)
				continue;

			if (filter.text().length() != 0) {
				boolean contains = false;
				for (RoomBlueprintImp b : c.prints) {
					if (Str.containsText(b.info.name, filter.text())) {
						contains = true;
						break;
					}
				}
				if (!contains)
					continue;
			}

			c.entries = 0;
			boolean hasAny = false;
			boolean locked = true;
			for (RoomBlueprintImp b : c.prints) {
				if (SETT.ROOMS().copy.prints.all(b).size() > 0) {
					hasAny = true;
					c.entries+= SETT.ROOMS().copy.prints.all(b).size();
				}
				if (b.reqs.passes(FACTIONS.player()))
					locked = false;
			}

			if (!hasAny)
				continue;
			
			Entry e = next(fi);
			fi++;
			e.cat = c;
			e.print = null;
			current.add(e);

			int nn = current.size();

			for (RoomBlueprintImp b : c.prints) {
				for (SavedPrint p : SETT.ROOMS().copy.prints.all(b)) {
					e = next(fi);
					if (c.expanded)
						current.add(e);
					fi++;
					e.cat = c;
					e.print = p;
					e.isLocked = locked;
				}
			}

			for (; nn < current.size(); nn++) {
				e.isLocked = locked;
			}

			if (c.entries == 0)
				c.expanded = false;
		}

		return current;

	}

	public void expand(SavedPrint p) {
		catmap[p.blue.index()].expanded = true;
		vi = -1;
	}

	private Entry next(int fi) {
		if (fi >= free.size()) {
			ArrayList<Entry> free = new ArrayList<Entry>(this.free.size() + 64);
			ArrayList<Entry> current = new ArrayList<Entry>(free.max());
			for (Entry e : this.free)
				free.add(e);
			for (Entry e : this.current)
				current.add(e);
			for (int i = 0; i < 64; i++)
				free.add(new Entry());
			this.free = free;
			this.current = current;

		}
		return free.get(fi);
	}

}
