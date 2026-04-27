package game.debug;

import java.nio.file.Path;
import java.util.Comparator;

import game.GAME;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;

public final class CallCounter {

	private final int max;
	private int count;
	private final KeyMap<EE> map = new KeyMap<>();
	private boolean running = false;
	
	
	public CallCounter(int max){
		this.max = max;
		GAME.saver().onAfterLoad(new ACTION_O<Path>() {
			
			@Override
			public void exe(Path t) {
				running = true;
			}
		});
	}
	
	private static class EE {
		
		final StackTraceElement[] ee;
		int count = 0;
		
		EE(StackTraceElement[] ee){
			this.ee = ee;
		}
	}
	
	public void count() {
		if (!running)
			return;
		StackTraceElement[] eee = Thread.currentThread().getStackTrace();
		String ii = "";
		for (StackTraceElement e : eee) {
			ii += e.toString();
		}
		if (!map.containsKey(ii)){
			map.put(ii, new EE(eee));
		}
		map.get(ii).count ++;
		count ++;
		
		if (count > max) {
			
			
			ArrayList<EE> es = new ArrayList<EE>(map.all());
			es.sort(new Comparator<CallCounter.EE>() {
				
				@Override
				public int compare(EE o1, EE o2) {
					return o1.count-o2.count;
				}
			});
			
			for (EE e : es) {
				if (e.count > 0) {
					System.out.println(e.count);
					for (int ei = 2; ei < e.ee.length; ei++) {
						System.out.println(e.ee[ei]);
					}
					System.out.println();
					e.count = 0;
				}
			}
			
			count = 0;
		}
		
		
		
	}
	
}
