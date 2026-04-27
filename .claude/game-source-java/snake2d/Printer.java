package snake2d;

public class Printer {

	private static final String pre = "[SNAKE2D] "; 
	private static final String fin = "[SNAKE2D] ------------------------"; 
	private Printer() {
		
	}
	
	public static void ln() {
		System.out.println();
	}
	
	public static void ln(Object info) {
		System.out.println(pre + info);
	}
	
	public static void pr(Object info) {
		System.out.print(info);
	}
	
	public static void err(Object info) {
		System.err.println(pre + info);
	}
	
	public static void ln(String title, String... info) {
		System.out.print(pre + title + ": ");
		for (String s : info)
			System.out.print(s + ", ");
		System.out.println();
	}
	
	public static void ln(String title, Iterable<String> info) {
		System.out.print(pre + title + ": ");
		for (String s : info)
			System.out.print(s + ", ");
		System.out.println();
	}
	
	public static void fin() {
		System.out.println(fin);
		System.out.println();
	}
	
}
