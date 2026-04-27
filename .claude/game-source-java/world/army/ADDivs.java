package world.army;

import java.io.IOException;
import java.util.Arrays;

import init.constant.Config;
import snake2d.util.bit.BitsLong;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sprite.text.Str;
import util.text.Dic;
import world.entity.army.WArmy;

public final class ADDivs implements SAVABLE{

	private final long[] divs = new long[Config.battle().DIVISIONS_PER_ARMY];
	final long[] data;
	private int divI;
	public final Str name = new Str(24);

	private final WArmy aa;
	static final BitsLong BType = new BitsLong(0xFF_00_00_00_00_00_00_00l);
	
	
	public ADDivs(WArmy e) {
		data = new long[AD.iinit().dataA.longCount()];
		this.aa = e;
		clear();
		
	}
	
	@Override
	public void save(FilePutter file) {
		file.ls(divs);
		file.i(divI);
		AD.iinit().dataA.saver().save(aa, file);
		name.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ls(divs);
		divI = file.i();
		AD.iinit().dataA.loader().load(aa, file);
		name.load(file);
	}

	@Override
	public void clear() {
		divI = 0;
		name.clear().add(Dic.¤¤Army);
		Arrays.fill(data, 0);
	}
	
	public boolean canAdd() {
		return divI < divs.length;
	}
	
	public int size() {
		return divI;
	}
	
	public ADDiv get(int i) {
		if (i < 0 || i >= size()) {
			return null;
		}
		
		switch(BType.get(divs[i])) {
			case WDivRegional.type: return AD.regional().get((int)divs[i] & 0x0FFFFFFFF);
			case WDivStored.type: return AD.cityDivs().get(divs[i]);
			case WDivMercenary.type: return AD.mercenaries().get(divs[i]);
			default: throw new RuntimeException();
		}
	}
	
	public void insert(int after, int insert) {
		if (after < 0 || after >= size() || insert < 0 || insert >= size())
			throw new RuntimeException(after + " " + insert);
		if (after == insert)
			return;
		long di = divs[insert];
		for (int i = insert; i < size()-1; i++) {
			divs[i] = divs[i+1];
		}
		if (after > insert)
			after--;
		
		for (int i = size()-1; i > after; i--) {
			divs[i] = divs[i-1];
		}
		divs[after] = di;
	}
	
	void add(ADDiv div) {
		int i = divI;
		long d = ADDivs.BType.set(0, div.type());
		d |= div.index;
		divs[i] = d;
		divI ++;
	}
	
	void remove(ADDiv div) {
		for (int di = 0; di < divI; di++) {
			if (get(di) == div) {
				for (int ii = di; ii < divI-1; ii++)
					divs[ii] = divs[ii+1];
				divI --;
				return;
			}
		}
		throw new RuntimeException();

	}
	
}
