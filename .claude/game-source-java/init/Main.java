package init;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import game.VERSION;
import init.paths.PATHS;
import launcher.LSettings;
import launcher.Launcher;
import snake2d.LOG;
import snake2d.util.misc.OS;
import snake2d.util.process.Proccesser;
import snake2d.util.sets.LIST;

public class Main {

	public static void main(String[] args) {

		macWarning();
		

		try {

			if (args != null && args.length > 0 && args[0].equalsIgnoreCase("launcher")) {

				LOG.ln("*************************************");
				LOG.ln("* LAUNCHER " + VERSION.VERSION_STRING);
				LOG.ln("*************************************");

				String[] jvmArgs = new String[0];

				try {
					File f = new File(System.getProperty("user.dir") + File.separator + "/jvmargs-launcher.txt");
					if (f.exists()) {
						List<String> jj = Files.readAllLines(f.toPath());
						jvmArgs = new String[jj.size()];
						int si = 0;
						for (String s : jj) {
							jvmArgs[si++] = s;
							LOG.ln("Launcher arg: " + s);
						}
					} else {
						LOG.ln("could not read launcher arguments: file does not exist");
					}

				} catch (Exception e) {
					e.printStackTrace();
					LOG.ln("could not read launcher arguments");
				}

				Process p = Proccesser.executeLwjgl(Launcher.class, new String[] {}, jvmArgs, new String[] {});

				if (p != null) {
					while (p.isAlive())
						try {
							Thread.sleep(0);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					if (p.exitValue() != 0)
						return;
				}
			}
			LOG.ln("*************************************");
			LOG.ln("* STARTING " + VERSION.VERSION_STRING);
			LOG.ln("*************************************");

			LSettings s = new LSettings();

			PATHS.init(s.mods.get(), null, false);

			LIST<String> jars = PATHS.SCRIPT().modClasspaths();
			String[] cps = new String[jars.size()];

			for (int i = 0; i < jars.size(); i++) {
				cps[i] = jars.get(i);
			}

			String[] jvmArgs = s.jvmArguments.get();

			if (s.debug.get() == 1) {
				String[] aa = new String[jvmArgs.length + 3];
				for (int i = 0; i < jvmArgs.length; i++) {
					aa[3 + i] = jvmArgs[i];
				}
				aa[0] = "-Dorg.lwjgl.util.Debug=true";
				aa[1] = "-Dorg.lwjgl.util.DebugAllocator=true";
				aa[2] = "-Dorg.lwjgl.util.DebugStack=true";
				jvmArgs = aa;

			}

			
			Proccesser.executeLwjgl(MainProcess.class, jvmArgs, new String[] {}, cps);
			


			File f = new File(System.getProperty("user.dir") + File.separator + "/hasRunOnceOnMac.txt");
			if (f.exists()) {
				return;
			} else {
				f.createNewFile();
			}
			
		} catch (Exception e) {
			e.printStackTrace();

			try {
				PrintWriter writer = new PrintWriter("SEVERE_ERROR.txt", "UTF-8");
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
				writer.println(timeStamp);
				e.printStackTrace(writer);
				writer.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	private static void macWarning() {

		try {

			if ((OS.get() == OS.MAC && PATHS.isSteam())) {

				File f = new File(System.getProperty("user.dir") + File.separator + "/hasRunOnceOnMac.txt");
				if (f.exists())
					return;

				// Using \n explicitly is safer for AppleScript strings
				String message = "Dear Mac User\n\n" + "You are playing Songs of Syx through Mac, this is good.\n"
						+ "Unfortunately, the steam overlay breaks the visuals of the game.\n"
						+ "The overlay can not be disabled by us developers, it has to be done manually by the user.\n\n"
						+ "Steam > Right click Songs of Syx > Properties > General > Uncheck 'Enable the Steam Overlay while in-game'\n\n"
						+ "If having trouble: www.reddit.com/r/songsofsyx/comments/umzi1t/deactivate_steam_overlay_to_run_game_on_mac\n\n"
						+ "Please also report this as a bug so that steam will fix this issue.\n"
						+ "https://help.steampowered.com/en/\n\n"
						+ "The game also works fine to run like a normal app from the installation directory, being completely DRM free.\n\n"
						+ "Apologies for the inconvenience, the alternative is to delist the game for mac, which would be a travesty.\n\n"
						+ "CLOSE THIS MESSAGE TO CONTINUE TO THE GAME";

				// Escape any double quotes that might be added later
				String escapedMessage = message.replace("\"", "\\\"");

				// "tell application \"System Events\"" ensures the dialog pops up on top of the
				// game
				String script = String.format(
						"tell application \"System Events\" to display dialog \"%s\" with title \"Steam Overlay Warning\" buttons {\"OK\"} default button \"OK\"",
						escapedMessage);

				ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
				Process process = pb.start();
				process.waitFor(); // This will block until the user clicks OK

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
