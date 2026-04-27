package snake2d;

import java.io.PrintStream;

public final class LOG {


	private LOG() {
		
	}
	
	private static String lasth;
	private static int li = 0;
	private static int lln = 0;
	private static String tab = "      ";
	
	public static void ln() {
		System.out.println();
	}
	
	private static void header(PrintStream o) {
		StackTraceElement[] ee = new RuntimeException().getStackTrace();
		String s = ee[2].getClassName();
		int ln = 0;
		if (s.contains(".")) {
			String ss[] = s.split("\\.");
			s = ss[ss.length-1];
			ln = ee[2].getLineNumber();
		}
		if (!s.equals(lasth) || li > 250) {
			o.println();
			o.print("[GAME]" );
			//o.print(ee[2]);
			String cl = ee[2].getClassName();
			if (cl.indexOf('$') > 0)
				cl = cl.substring(0, cl.indexOf('$'));
			o.println(" ("+cl + ".java:" +ln+ ")");
			
			li = 0;
			lln = -1;
		}
		if (lln != ln) {
			lln = ln;
			String l = "[" + ln + "]";
			o.print(l);
			for (int i = 0; i < tab.length()-l.length(); i++)
				o.print(" ");
		}else {
			o.print(tab);
		}
		
		
		li++;
		lasth = s;
	}
	
	public static void ln(Object info) {
		
		header(System.out);
		System.out.println(info);

	}
	
	public static void ln(Object a, Object b) {
		
		ln(a);
		ln(b);
	}
	
	public static void ln(Object a, Object b, Object c) {
		
		ln(a);
		ln(b);
		ln(c);
	}
	
	public static void ln(Object[] info) {
		for (Object oo : info) {
			ln(oo);
		}
	}
	
	public static void err(Object info) {
		
		header(System.err);
		System.err.println(info);

	}
	
//	public static void err(Object[] info) {
//		header(System.err);
//		for (Object oo : info) {
//			System.err.println(oo);
//		}
//	}
	
	public static String bits(long l) {
		String s = "";
		int sp = 0;
		for (int bi = 0; bi < 64; bi++) {
			if ((sp == 8)) {
				s += "_";
				sp = 0;
			}
			long m = 1l << (63-bi);
			
			if ((l & m) != 0) {
				s += "1";
			}else {
				s += "0";
			}
			sp++;
		}
		return s;
	}
	
	public static String WS(int spaces) {
		String s = "";
		for (int i = 0; i < spaces; i++)
			s += " ";
		return s;
	}
	
	public static String NL() {
		return System.lineSeparator();
	}
	

	
}
