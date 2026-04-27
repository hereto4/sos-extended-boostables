package game.boosting.superb;

import java.io.IOException;

import game.GAME;
import game.boosting.BHoverer;
import game.boosting.BUtil;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.boosting.superb.SuperSpec.SuperSpecImp;
import game.save.Savable;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.text.Dic;


public class SuperBoostable<T extends SuperBoostableObj> {

	private int[] saveOrder; 
	
	final KeyMap<SuperSpecImp<T>> map = new KeyMap<SuperSpecImp<T>>();
	final ArrayListGrower<SuperSpecImp<T>> ups = new ArrayListGrower<SuperSpecImp<T>>();
	final ArrayListGrower<SuperSpec<T>> all = new ArrayListGrower<SuperSpec<T>>();
	public final Boostable bo;
	
	public SuperBoostable(Boostable bo){	
		this.bo = bo;
		
		GAME.saver().addSpecialSaver(new Savable("BOOST_" + bo.key) {
			
			@Override
			protected void save(FilePutter file) {
				file.i(ups.size());
				for (SuperSpecImp<T> s : ups)
					file.chars(s.key);
			}
			
			@Override
			protected void load(FileGetter file) throws IOException {

				int am = file.i();
				saveOrder = new int[am];
				for (int i = 0; i < saveOrder.length; i++)
					saveOrder[i] = -1;
				for (int i = 0; i < am; i++) {
					String k =  file.chars();
					if (map.containsKey(k)) {
						saveOrder[i] = map.get(k).index;
					}
				}
				
			}
		});
		
		GAME.addOnViewInit(new ACTION() {
			SuperBoostable<T> self = SuperBoostable.this;
			@Override
			public void exe() {
				for (Booster b : bo.all()) {
					new SuperSpec.Wrap<T>(b, self, "", b.info, null);
				}
				
			}
		});
	}
	
	public void clear() {
		
	}
	
	public SuperData makeData() {
		return new SuperData(this);
	}
	
	int[] saveOrder() {
		if (saveOrder == null) {
			saveOrder = new int[ups.size()];
			for (int i = 0; i < saveOrder.length; i++)
				saveOrder[i] = i;
		}
		return saveOrder;
	}
	
	public void update(T t, double ds) {
		for (int i = 0; i < ups.size(); i++) {
			SuperSpecImp<T> spec = ups.get(i);
			spec.update(t, ds);
		}
	}
	
	public LIST<SuperSpec<T>> all() {
		return all;
	}
	
	public LIST<SuperSpecImp<T>> imps() {
		return ups;
	}
	
	public void hover(GUI_BOX box, T roy) {
		BHoverer.hover(box, all, roy, Dic.¤¤Boosts, bo.baseValue, false);
	}
	
	public void hoverDetailed(GUI_BOX box, T roy) {
		BHoverer.hoverDetailed(box, all, roy, Dic.¤¤Boosts, bo.baseValue, false);
	}
	
	public double get(T bo) {
		return BUtil.value(all, bo, this.bo.baseValue, 1, -100);
	}
}
