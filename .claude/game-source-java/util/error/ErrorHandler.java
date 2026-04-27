package util.error;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import game.VERSION;
import init.paths.PATHS;
import snake2d.Errors.DataError;
import snake2d.Errors.GameError;
import snake2d.LOG;
import snake2d.util.file.FileManager;
import snake2d.util.misc.ERROR_HANDLER;
import snake2d.util.process.Proccesser;

public class ErrorHandler implements ERROR_HANDLER {

	private final static String bugMail = "info@songsofsyx.com";
	private final static String pgmname = "Songs of syx";
	
	public ErrorHandler() {
		
		
	}


	@Override
	public void handle(String output, String dump) {
		String path = null;
		
		try {
			
			if (PATHS.local().LOGS.exists("UnhandledDump"))
				PATHS.local().LOGS.delete("UnhandledDump");
			String p = PATHS.local().LOGS.get() + File.separator + "UnhandledDump.txt";
			if (new File(p).createNewFile()) {
				PrintWriter out = new PrintWriter(p);
				out.println(dump);
				out.close();
				LOG.ln("saved " + p);
				path = p;
			}
		} catch (IOException e1) {
		
			e1.printStackTrace();
		}

		error(null, 2, "Unhandled error output: " + System.lineSeparator() + output, dump, path);
	}

	@Override
	public void handle(DataError e, String dump) {
		error(e, 0, e.error, dump, e.path);
		
	}

	@Override
	public void handle(GameError e, String dump) {
		error(e, 1, e.error, dump, null);
		
	}
	
	@Override
	public void handle(Throwable e, String dump) {
		
		if (isModError(e)) {
			e.printStackTrace();
			handle(new DataError("An error has occured caused by a code mod. Please inform the modders of this error."), dump);
		}else
			error(e, 3, e.getClass().getName() + ": " + e.getMessage(), dump, null);
		
	}
	
	private boolean isModError(Throwable e) {
		if (e instanceof NoSuchFieldError) {
			return true;
		}
		
		if (e instanceof NoSuchMethodError) {
			return true;
		}
		
		return false;
	}
	
	private void error(Throwable ee, int type, String message, String dump, String dataPath) {
		
		
		//save data;
		String p = new File("error.txt").getAbsolutePath();
		try {
			
			if (PATHS.local().LOGS.getFiles().length > 50) {
				int am = PATHS.local().LOGS.getFiles().length - 25;
				for (String f : PATHS.local().LOGS.getFiles()) {
					PATHS.local().LOGS.get(f).toFile().deleteOnExit();
					if (am-- < 0)
						break;
				}
			}else {
				while(PATHS.local().LOGS.getFiles().length > 25) {
					String l = null;
					long ff = Long.MAX_VALUE;
					for (String f : PATHS.local().LOGS.getFiles()) {
						long lm = PATHS.local().LOGS.get(f).toFile().lastModified();
						if (lm < ff) {
							l = f;
							ff = lm;
						}
					}
					if (l != null) {
						Files.delete(PATHS.local().LOGS.get(l));
					}
				}
			}
			

			
			p = FileManager.NAME.timeStampString(PATHS.local().LOGS.get() + File.separator + "error") + ".txt";
		}catch(Exception e) {
			
		}
		
		if (type == 3) {
			try {
				
				
				
				if (new File(p).createNewFile()) {
					PrintWriter out = new PrintWriter(p);
					out.println(dump);
					out.close();
					LOG.ln("saved " + p);
				}
			} catch (IOException e1) {
			
				e1.printStackTrace();
			}
		}
		
		
		String dumpFile = p;
		
		
		if (dataPath == null)
			dataPath = "none";
		if (message == null)
			message = "no message";
		if (dumpFile == null)
			dumpFile = "none";
		
		message = message.replaceAll("\"", "Quote");
		if (message.length() > 8000)
			message = message.substring(0, 8000);
		
		String eee = "unhandled " + System.currentTimeMillis();
		if (ee != null)
			eee = VERSION.VERSION_STRING + " " + ee.toString() + " ";
		if (ee != null && ee.getStackTrace().length > 0) {
			eee += ee.getStackTrace()[0].getClassName() + ":" + ee.getStackTrace()[0].getLineNumber();
		}
		
		String[] args = new String[] {
			pgmname,
			bugMail,
			"" + type,
			message,
			dumpFile,
			dataPath,
			eee
		};
		
		Proccesser.exec(ErrorMessage.class, new String[] {}, args, new String[] {});
		
	}

	
}
