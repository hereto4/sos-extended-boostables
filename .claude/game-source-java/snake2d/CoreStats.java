package snake2d;

public class CoreStats {

	CoreStats(){}
	
	private static int cycleCount = 0;
	private static long timer = System.nanoTime();
	private final static double referenceValue = 1000000000/60;
	
	
	public final static Value FPS = new Value("FPS");
	public final static Value coreTotal = new Value("Core");
	public final static Value corePoll = new Value("Poll");
	public final static Value coreFinish = new Value("Finish");
	public final static Value coreFlush = new Value("Flush");
	public final static Value coreSleep = new Value("Sleep");
	public final static Value coreSound = new Value("Sound");
	public final static Value smallUpdates= new Value("SmallUp");
	public final static Value updatePercentage = new Value("UpPer");
	public final static Value renderPercentage = new Value("Render");
	public final static Value updateThreadPercentage = new Value("Update Total");

	public final static Value swapPercentage = new Value("Swap");

	public final static Value totalPercentage = new Value("Tot");
	public final static Value droppedTicks = new Value("TicksDropped");
	public final static Value heap = new Value("heap size");
	public final static Value usedHeap = new Value("used heap");
	public final static Value heapGrowth = new Value("heap growth");
	
	private static long oldMemory = 0;
	private static long dMemory;
	private static int memoryCount = 0;
	
	public static void print2StdOut(){
		Printer.ln("--INFO--");
		Printer.ln(smallUpdates);
		Printer.ln(updatePercentage);
		Printer.ln(smallUpdates);
		Printer.ln(updatePercentage);
		Printer.ln(corePoll);
		Printer.ln(totalPercentage);
		Printer.fin();
	}
	
	static void endOfLoopCalc(){
		
		cycleCount ++;

		if (System.nanoTime() - timer >= 1000000000){
			smallUpdates.calc();
			droppedTicks.calc();
			updatePercentage.calc();
			renderPercentage.calc();
			swapPercentage.calc();
			coreTotal.calc();
			corePoll.calc();
			coreFinish.calc();
			coreFlush.calc();
			coreSleep.calc();
			coreSound.calc();
			totalPercentage.setD(updatePercentage.ave + renderPercentage.ave);
			totalPercentage.calc();
			FPS.setD((double)cycleCount);
			FPS.calc();
			Runtime r = Runtime.getRuntime();
			long kb = 1024;
			long newMemory = (r.totalMemory() - r.freeMemory())/kb;
			dMemory += newMemory - oldMemory;
			oldMemory = newMemory;
			heap.setD(r.totalMemory()/kb);
			heap.calc();
			usedHeap.setD(oldMemory);
			usedHeap.calc();
			memoryCount ++;
			if (memoryCount == 5){
				memoryCount = 0;
				heapGrowth.setD(dMemory/5);
				heapGrowth.calc();
				dMemory = 0;
			}
			
			cycleCount = 0;
			timer += 1000000000;
		}
	}
	
	public static class Value{
		
		volatile double current = 0;
		public volatile double min = 1000;
		public volatile double max = -1;
		public volatile double ave = 0;
		private volatile double acc = 0;
		private volatile int cCount = 0;
		private final String name;
		
		private Value(String name){
			this.name = name;
		}
		
		void set(long ns){
			setD(100.0*(ns/referenceValue));
		}
		
		void setD(double percentage){
			current = percentage;
			if (percentage < min)
				min = percentage;
			else if (percentage > max)
				max = percentage;
			acc += percentage;
			cCount ++;
		}
		
		private void calc(){
			ave = acc/cCount;
			cCount = 0;
			acc = 0;
		}
		
		@Override
		public String toString() {

			
			int percent = (int)ave;
			int frac = (int) ((ave - percent)*100);
			
			return name + ": " + percent + "," + frac;
		}
		
		public String toStringLong() {
			return name + ":"
					+ "\n   Current: " + Double.toString(current)
					+ "\n   Min: " + Double.toString(min)
					+ "\n   Max: " + Double.toString(max)
					+ "\n   Ave: " + Double.toString(ave);
		}
		
		public String getLabel(){
			return name;
		}
	}
	
}
