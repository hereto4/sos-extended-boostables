package game.boosting.tmp;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.boosting.BOOSTING;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.Bitmap1D;

final class Data implements SAVABLE{

	private final Bitmap1D specActive;
	private final float[] add;
	private final float[] mul;

	Data(int specs){
		specActive = new Bitmap1D(specs+2, false);
		add = new float[BOOSTING.ALL().size()]; 
		mul = new float[BOOSTING.ALL().size()];
		Arrays.fill(mul, 1f);
	}

	private void cache() {
		if (specActive.get(specActive.size()-2)) {
			
			Arrays.fill(add, (float)0);
			Arrays.fill(mul, (float)1);
			boolean any = false;
			for (TmpBoostSpec s : GAME.BOOST().specs()) {
				if (specActive.get(s.index)) {
					any = true;
					for (BoostSpec ss : s.spec.all()) {
						if (ss.booster.isMul) {
							mul[ss.boostable.index()] *= ss.booster.to();
						}else {
							add[ss.boostable.index()] += ss.booster.to();
						}
					}
					
				}
			}
			specActive.set(specActive.size()-1, any);
			specActive.set(specActive.size()-2, false);
		}
	}
	
	public double add(Boostable bo) {
		cache();
		return add[bo.index()];
	}
	
	public double mul(Boostable bo) {
		cache();
		return mul[bo.index()];
	}

	public void set(TmpBoostSpec s, boolean set) {
		if (set == specActive.get(s.index))
			return;
		specActive.set(s.index, set);
		setDirty();
	}
	
	public void setDirty() {
		specActive.set(specActive.size()-2, true);
	}
	
	public boolean is(TmpBoostSpec spec) {
		cache();
		return specActive.get(spec.index);
	}
	
	public boolean hasAny() {
		cache();
		return specActive.get(specActive.size()-1);
	}

	@Override
	public void clear() {
		specActive.clear();
		setDirty();
	}

	@Override
	public void save(FilePutter file) {
		specActive.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		specActive.load(file);
		setDirty();
	}

	public void load(FileGetter file, int[] oldOrder) throws IOException {
		specActive.clear();
		Bitmap1D old = new Bitmap1D(specActive.size(), false);
		old.load(file);
		
		for (int i = 0; i < old.size(); i++) {
			if (old.get(i) && i < oldOrder.length && oldOrder[i] != -1) {
				specActive.set(oldOrder[i], true);
			}
		}
		setDirty();
	}
	
}
