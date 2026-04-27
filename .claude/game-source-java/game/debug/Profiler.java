package game.debug;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import snake2d.LOG;
import snake2d.util.sets.ArrayList;

public interface Profiler {

	public default void logStart(Object o) {
		logStart(o.getClass());
	}
	public void logStart(Class<?> cl);
	public void logEnd(Class<?> cl);
	public default void logEnd(Object o) {
		logEnd(o.getClass());
	}
	public void log();
	
	
	public static final Profiler DUMMY = new Profiler() {
		
		@Override
		public void logStart(Class<?> cl) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void logEnd(Class<?> cl) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void log() {
			// TODO Auto-generated method stub
			
		}
	};
	
	public static final Profiler LIVE = new Profiler() {
		
		private final ArrayList<Prof> entries = new ArrayList<Prof>(256);
		{
			while(entries.hasRoom())
				entries.add(new Prof());
		}
		
		private LinkedHashMap<Class<?>, Prof> map = new LinkedHashMap<>();
		private long ss = -1;
		private int tab = 0;
		private boolean cpu = true;
		private boolean mem = false;
		private boolean keepnops = false;
		private boolean outliners = false;
		
		private LinkedHashMap<Class<?>, Out> omap = new LinkedHashMap<>();
		
		@Override
		public void logStart(Class<?> cl) {
			if (ss == -1)
				ss = System.currentTimeMillis();
			
			if (!map.containsKey(cl)) {
				Prof e = entries.removeLast();
				e.acc = 0;
				e.tab = tab;
				e.memAcc = 0;
				map.put(cl, e);
			}
			map.get(cl).mem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			map.get(cl).start = System.nanoTime();
			
			map.get(cl).tab = tab;
			tab++;
		}
		
		@Override
		public void logEnd(Class<?> cl) {
			Prof e = map.get(cl);
			long l = System.nanoTime();
			l -= e.start;
			map.get(cl).memAcc += (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())-map.get(cl).mem;
			e.acc += l;
			tab --;
		}
		
		@Override
		public void log() {
			
			if (ss == -1)
				return;

//			
			if (System.currentTimeMillis() - ss < 2000)
				return;
			
			long tot = 0;
			for (Entry<Class<?>, Prof> k : map.entrySet()) {
				tot += k.getValue().acc;
			}
			
			LOG.ln("CPU");
			for (Entry<Class<?>, Prof> k : map.entrySet()) {
				entries.add(k.getValue());
				int v = (int)(1000.0*k.getValue().acc/tot);
				
				if (outliners) {
					if (!omap.containsKey(k.getKey())) {
						omap.put(k.getKey(), new Out());
						omap.get(k.getKey()).old = v;
					}else {
						Out o = omap.get(k.getKey());
						o.nn = v;
					}
				}
				if (cpu)
					if (keepnops || v > 0)
						LOG.ln(LOG.WS(k.getValue().tab*2) + " " +  v + " " + k.getKey() + " " + k.getValue().acc);
			}
			if (mem) {
				LOG.ln();
				LOG.ln("MEM");
				for (Entry<Class<?>, Prof> k : map.entrySet()) {
					LOG.ln(LOG.WS(k.getValue().tab*2) + " " + k.getValue().memAcc + " " + k.getKey());
				}
			}

			if (outliners) {
				LOG.ln();
				LOG.ln("CHANGE");
				for (Entry<Class<?>, Out> k : omap.entrySet()) {
					
					double v = k.getValue().nn / k.getValue().old;
					
					if (v > 1.5) {
						LOG.ln(k.getValue().nn + " <- " + k.getValue().old + " " + k.getKey());
					}
					
					k.getValue().old = k.getValue().nn;
						
				}
			}
			
			
			map.clear();
			tab = 0;
			ss = -1;
			LOG.ln();
		}
		
		class Prof {
			
			public long start;
			public int tab;
			public long acc;
			public long mem;
			public long memAcc;
		}
		
		class Out {
			
			public double old = 0;
			public double nn = 0;
		}
		
	};
	
	
}
