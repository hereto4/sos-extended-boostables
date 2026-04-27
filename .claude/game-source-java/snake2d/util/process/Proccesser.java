package snake2d.util.process;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;

import snake2d.LOG;
import snake2d.util.misc.OS;

public final class Proccesser {

	private Proccesser() {
	}

	public static Process executeLwjgl(Class<?> clazz, String[] jvmArgs, String[] args, String[] classPaths) {
		if (OS.get() == OS.MAC) {
			String[] ja = new String[jvmArgs.length+1];
			for (int i = 0; i < jvmArgs.length; i++)
				ja[i] = jvmArgs[i];
			ja[ja.length-1] = "-XstartOnFirstThread";
			jvmArgs = ja;
		}
		return exec(clazz, jvmArgs, args, classPaths, true);
	}
	
	public static Process exec(Class<?> clazz, String[] jvmArgs, String[] args, String[] classPaths) {
		
		return exec(clazz, jvmArgs, args, classPaths, false);
	}
	
	public static Process exec(Class<?> clazz, String[] jvmArgs, String[] args, String[] classPaths, boolean addGameTweaks) {
		
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		if (classpath == null || classpath.length() == 0) {
			LOG.ln("java.class.path is: '" + classpath + "'");
			classpath = "SongsOfSyx.jar";
		}
		String className = clazz.getName();

		ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		for (String a : jvmArgs)
			command.add(a);
		command.add("-cp");
		String cp = "";
		
		String sep = System.getProperty("path.separator");
		
		for (String c : classPaths) {
			cp += c + sep;
		}
		
		cp += classpath;
		command.add(cp);
		command.add(className);
		for (String a : args)
			command.add(a);
		
		ProcessBuilder builder = new ProcessBuilder(command);
		
		if (addGameTweaks) {
			builder.environment().put("PULSE_PROP_application.media_role", "Game");
			builder.environment().put("DRI_PRIME", "1");
			builder.environment().put("__NV_PRIME_RENDER_OFFLOAD", "1");
			builder.environment().put("__GLX_VENDOR_LIBRARY_NAME", "nvidia");
			builder.environment().put("__VK_LAYER_NV_optimus", "NVIDIA_only");
			
		}
		
		
		Process process;
		
		try {
			process = builder.redirectOutput(Redirect.INHERIT).redirectErrorStream(true).start();
			return process;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}



}