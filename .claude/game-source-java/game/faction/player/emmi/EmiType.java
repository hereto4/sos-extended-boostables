package game.faction.player.emmi;

import java.io.IOException;
import java.util.Arrays;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.SPRITE;
import util.data.INT_O.INT_OE;
import util.gui.misc.GText;

public abstract class EmiType<T> implements INT_OE<T>{

	public final SPRITE icon;

	public final CharSequence name;
	public final CharSequence desc;
	private final int[] ams;
	private int total;
	private final int max;
	
	EmiType(SPRITE icon, CharSequence name, CharSequence desc, int size, int max) {
		this.name = name;
		this.desc = desc;
		this.icon = icon;
		ams = new int[size];
		this.max = max;
	}
	
	public int total() {
		return total;
	}
	
	@Override
	public int get(T t) {
		return ams[index(t)];
	}

	@Override
	public int min(T t) {
		return 0;
	}

	@Override
	public int max(T t) {
		return max;
	}
	
	@Override
	public double getD(T t) {
		return (double)get(t)/max;
	}

	int get(int index) {
		return ams[index];
	}
	
	void set(int index, int value) {
		count(index, -ams[index]);
		ams[index] = value;
		count(index, ams[index]);
	}
	
	void count(int index, int am) {
		total += am;
	}
	
	@Override
	public void set(T t, int i) {
		int index = index(t);
		set(index, i);
	}

	void save(FilePutter file) {
		file.isE(ams);
		
	}
	
	public abstract void formatValue(GText t, T v);

	void load(FileGetter file) throws IOException {
		clear();
		file.isE(ams);
		total = 0;
		for (int i = 0; i < ams.length; i++) {
			count(i, ams[i]);
		}
	}

	void clear() {
		Arrays.fill(ams, 0);
		total = 0;
	}
	
	abstract int index(T t);
	
}
