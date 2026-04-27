package snake2d;

import snake2d.util.misc.OS;
import snake2d.util.process.Proccesser;

public final class PreLoader {

	private static Process preloader;
	
	public static void load(String version, String pathToImage, String pathToIcon) {
		

		exit();
		try {
			if (OS.get() != OS.MAC)
				preloader = Proccesser.exec(PreLoaderSwing.class, new String[] {}, new String[] {version, pathToImage, pathToIcon}, new String[] {});
		}catch(Exception e) {
			PreLoader.exit();
			e.printStackTrace();
			return;
		}
		
		
	}
	
	public static void exit() {
		
		
		
		Process p = preloader;
		preloader = null;
		if (p == null)
			return;
		
		
		if (p.isAlive()) {
			try {
				p.getOutputStream().write('s');
				p.getOutputStream().write('s');
				p.getOutputStream().write('s');
				p.getOutputStream().write('s');
				p.getOutputStream().flush();
				//p.destroy();
				//p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		preloader = null;
		
		
	}

}
