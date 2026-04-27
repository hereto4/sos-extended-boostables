package init.value;

import game.GAME;
import init.sprite.SPRITES;
import init.value.Locker.LockerValue;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.gui.misc.GBox;
import util.text.D;

public class Lockable<T> {
	
	private static CharSequence ¤¤requires = "¤Requires";
	static {
		D.ts(Lockable.class);
	}
	
	final ArrayListGrower<Lock<T>> res = new ArrayListGrower<>();
	public final GValueCat<T> values;
	private static boolean hasSpewed = false;
	
	public final CharSequence name;
	public final CharSequence desc;
	public final String key;
	public final SPRITE icon;
	Lockable(String key, CharSequence name, CharSequence desc, SPRITE icon, GValueCat<T> mm){
		this.values = mm;
		this.name = name;
		this.icon = icon;
		this.desc = desc;
		this.key = key;
	}
	
	public LIST<Lock<T>> all() {
		return res;
	}
	
	public boolean passes(T t) {
		for (Lock<T> r : all()) {
			if (!r.unlocker.inUnlocked(t))
				return false;
		}
		return true;
	}
	
	public double progress(T t) {
		if (all().size() == 0)
			return 1.0;
		double d = 0;
		for (Lock<T> r : all()) {
			d += CLAMP.d(r.unlocker.progress(t), 0, 1);
		}
		
		return d/all().size();
	}
	
	public boolean hover(GUI_BOX text, T t) {
		return hover(text, t, ¤¤requires);
	}
	
	public boolean hover(GUI_BOX text, T t, CharSequence title) {
		if (all().size() == 0)
			return false;
		GBox b = (GBox) text;
		b.textLL(title);
		b.NL();
		
		for (Lock<T> r : all()) {
			r.unlocker.hover(text, t);
			b.NL();
		}
		return true;
	}
	
	public void debug(T t) {
		LOG.ln(t);
		for (Lock<T> r : all()) {
			LOG.ln(r.unlocker.name + " " + r.unlocker.inUnlocked(t) + " " + r.unlocker.progress(t));
		}
		LOG.ln();
	}
	
	public void push(String key, double value, Object path, COMPARATOR comp) {
		RPromise p = new RPromise(key, value, path.toString(), comp);
		values.inits.add(p);
	}
	
	public void push(Lock<T> r) {
		res.add(r);
	}
	
	public void push(Json json) {
		push("REQUIRES", json);
	}
	

	
	
	public void push(String key, Json json) {

		
		if (!json.has(key))
			return;
		json = json.json(key);
		pushPush(json);
	}
	
	public void pushPush(Json json) {
		for (String keyComp : json.keys()) {
			COMPARATOR comp = COMPARATOR.map.get(keyComp, json);
			if (comp != null) {
				Json j = json.json(keyComp);
				for (String k : j.keys()) {
					
					RPromise p = new RPromise(k, j.d(k), j.path() + ", line" + j.line(k), comp);
					values.inits.add(p);
				}
			}	
		}
	}
	
	class RPromise implements ACTION{
		
		final String key;
		final double value;
		final String path;
		final COMPARATOR comp;
		
		RPromise(String key, double value, String path, COMPARATOR comp){
			this.key = key;
			this.value = value;
			this.path = path;
			this.comp = comp;
		}

		@Override
		public void exe() {
			if (values.get(key) == null) {
				if (!hasSpewed)
					GAME.Warn(path + System.lineSeparator() + "no " + values.key + " named : " + key + " available: " + System.lineSeparator() + values.available());
				else {
					LOG.err(path + System.lineSeparator() + "no " + values.key + " named : " + key);
				}
				hasSpewed = true;
				
				Value<T> v = new Value<>(key, SPRITES.icons().s.cancel, "unknown", new DOUBLE_O<T>() {
					@Override
					public double getD(T t) {
						return 0;
					}
				}, false, true);
				
				Locker<T> un = new LockerValue<T>(comp, v, 1, icon);
				Lock<T> lock = new Lock<>(Lockable.this, un);
				res.add(lock);
				
			}else {
				
				Value<T> v = values.get(key);
				
				Locker<T> un = new LockerValue<T>(comp, v, value, icon);
				Lock<T> lock = new Lock<>(Lockable.this, un);
				res.add(lock);
				
			}
			
		}
		
	}

	public void clear() {
		res.clear();
	}
}