package snake2d.util.misc;

public class Numbers {

	public static String getSuffix(int nr){
		
		nr = Math.abs(nr);
		
		if (nr == 1)
			return "1st";
		
		if (nr == 2)
			return "2nd";
		
		if (nr == 3)
			return "3rd";
		
		return Integer.toString(nr) + "th";
		
	}
	
	public static void printBits(byte b) {
		printBits(Byte.toUnsignedLong(b), 8);
	}
	
	public static void printBits(long l) {
		printBits(l, 64);
	}
	
	private static void printBits(long l, int bits) {
		
		StringBuilder s = new StringBuilder(bits);
		for (; bits > 0; bits--) {
			s.append((l & 0b001) == 0b001 ? '1' : '0');
			l = l >> 1;
		}
		s.reverse();
	}

	
}
