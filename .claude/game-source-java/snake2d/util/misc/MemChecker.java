package snake2d.util.misc;

public final class MemChecker {

	private static long memo = 0;
	
	public static boolean check() {
		Runtime rt = Runtime.getRuntime();
		long mem = rt.totalMemory()-rt.freeMemory();
		
		if (mem != memo) {
			System.out.println(mem-memo);
			memo = mem;
			return true;
		}
		return false;
	}
	
	public static boolean check(int i) {
		Runtime rt = Runtime.getRuntime();
		long mem = rt.totalMemory()-rt.freeMemory();
		
		if (mem != memo) {
			System.out.println(i + " " + (mem-memo));
			memo = mem;
			return true;
		}
		return false;
			
	}
	
	public static void clear() {
		Runtime rt = Runtime.getRuntime();
		memo = rt.totalMemory()-rt.freeMemory();
	}
	
}
