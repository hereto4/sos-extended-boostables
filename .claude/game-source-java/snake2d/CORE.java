package snake2d;

import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.List;

import snake2d.util.misc.ERROR_HANDLER;
import snake2d.util.misc.OS;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class CORE {

	private CORE() {

	}

	public static final float UPDATE_SECONDS_MAX = 1f / 32f;
	public static final float UPDATE_SECONDS_MIN = 1f / 1024f;

	private static boolean created;
	private static GraphicContext graphics;
	private static Input input;
	private static SOUND_CORE soundCore;
	private static volatile boolean running = true;
	private static Updater updater;
	private static volatile CORE_STATE.Constructor newState;
	private static volatile Throwable updateException;
	private static volatile GlJob glJob;
	private static Thread glThread;
	private static int FPS;
	private static volatile boolean swapping = false;
	private static volatile boolean debug;

	public static void init(ERROR_HANDLER error) {
		
		glThread = Thread.currentThread();
		Errors.init(error);
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				t.setUncaughtExceptionHandler(null);
				if (updateException != null) {
					Errors.handle(updateException);
					e.printStackTrace();
				}else
					Errors.handle(e);
				running = false;
				try {
					dispose();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		});
		//
	}

	public static void create(SETTINGS settings) {

		if (Thread.currentThread() != glThread)
			throw new RuntimeException();

		int mb = 1014 * 1024;
		final String platform = System.getProperty("os.name") + ", " + System.getProperty("os.arch") + " Platform.";
		final int nrOfPross = Runtime.getRuntime().availableProcessors();
		final String jre = System.getProperty("java.version");

		List<String> JREargs = ManagementFactory.getRuntimeMXBean().getInputArguments();
		Printer.ln("SYSTEM INFO");
		Printer.ln("---Running on a: " + platform + " " + OS.get());
		String bits = System.getProperty("sun.arch.data.model");
		Printer.ln("---jre: " + jre + " bits: " + bits);
		Printer.ln("---charset: " + Charset.defaultCharset());
		Printer.ln("---Processors avalible: " + nrOfPross);
		Printer.ln("---JRE Memory");

		Runtime run = Runtime.getRuntime();
		// available memory
		Printer.ln("      Total: " + run.totalMemory() / mb);
		Printer.ln("      Free: " + run.freeMemory() / mb);
		Printer.ln("      Used: " + (run.totalMemory() - run.freeMemory()) / mb);
		Printer.ln("      Max: " + run.maxMemory() / mb);
		Printer.ln("---JRE Input Arguments : ", JREargs);
		Printer.ln("---JRE cp : ", System.getProperty("java.class.path"));
		
		
		
		Printer.fin();

		if (created) {
			throw new RuntimeException("Core already created!");
		}

		created = true;
		swapping = false;

		graphics = new GraphicContext(settings);
		input = new Input(graphics, settings);

		soundCore = SOUND_CORE.create(settings);

		FPS = graphics.refreshRate;
		debug = settings.debugMode();
	}

	private static void setUpdater(CORE_STATE.Constructor state) {

		updater = new Updater(state);
		updater.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				running = false;
				Printer.ln("ERROR IN UPDATER DETECTED");
				updateException = e;
			}
		});
		updater.setDaemon(true);
		updater.start();
	}

	public static void performWork(Runnable r, String name) {
		Thread t = new Thread(r);
		t.setName(name);
		t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				running = false;
				Printer.ln("ERROR IN THREAD: " + name + "DETECTED");
				updateException = e;
			}
		});
		t.setDaemon(true);
		t.start();
	}

	public static void start(CORE_STATE.Constructor state) {

		running = true;
		swapping = false;

		// soundCore.start();

		long nowTemp;
		long last = 0;
		long lastSwap = System.nanoTime();

		setUpdater(state);
		newState = null;
		while (running && !swapping) {
			if (glJob != null) {
				glJob.doJob();
				glJob = null;
			}
			if (newState != null) {
				dispose();
				return;
			}
			sleep(1);
		}
		graphics.makeVisable();
		long killSwitch = System.currentTimeMillis();
		int kills = 0;
		while (running) {

			if (newState != null) {

				updater.dieHard();
				while (updater.isAlive())
					sleep(1);

				renderer().clear();
				soundCore.stopAllSounds();
				soundCore.disposeSounds();
				for (CORE_RESOURCE d : clientDisposables) {
					GlHelper.checkErrors();
					d.dis();
					GlHelper.checkErrors();
				}
				clientDisposables.clear();

				input.poll(System.nanoTime(), true);
				input.clearAllInput();

				System.gc();
				GlHelper.checkErrors();
				setUpdater(newState);
				GlHelper.checkErrors();
				killSwitch = System.currentTimeMillis();
				kills = 0;
				newState = null;

			}

			if (glJob != null) {
				glJob.doJob();
				glJob = null;
				killSwitch = System.currentTimeMillis();
				kills = 0;
			}

			if (swapping) {
				killSwitch = System.currentTimeMillis();
				kills = 0;
				/*
				 * SYNCHRONOUS BLOCK
				 */

				nowTemp = System.nanoTime();

				long total = nowTemp;
				graphics.flushRenderer();
				CoreStats.coreFlush.set(System.nanoTime() - nowTemp);

				nowTemp = System.nanoTime();
				input.poll(nowTemp, graphics.isFocused());
				long t = System.nanoTime() - nowTemp;
				CoreStats.corePoll.set(t);

				swapping = false;

				/*
				 * ASSYNCHRONOUS BLOCK
				 */

				// makes things smoother / causes stutter
				// nowTemp = System.nanoTime();
				// GlHelper.finsih();
				// CoreStats.coreFinish.set(System.nanoTime()-nowTemp);

				nowTemp = System.nanoTime();

				/*
				 * vsync fail safe, keeps computers from burning up
				 */

				Sleeper.sync(FPS);

				last = System.nanoTime();
				CoreStats.coreSleep.set(last - nowTemp);
				CoreStats.coreTotal.set(last - total);

				nowTemp = System.nanoTime();
				if (!graphics.swapAndCheckClose())
					running = false;

				lastSwap = System.nanoTime();
				CoreStats.swapPercentage.set(lastSwap - nowTemp);

			} else {
				sleep(1);
				if (System.currentTimeMillis() - killSwitch > 5000) {
					kills ++;
					if (kills > 8) {
						running = false;
						String err = "The game has taken too long to do what it's supposed to do. This can indicate that there is something wrong with the game's engine. It can also be a legit bug. Please report to the dev if your game is choppy or suffers from low FPS. info@songsofsyx.com";
						Printer.ln(err);
						StackTraceElement[] elements = updater.getStackTrace();
						for (int i = 0; i < elements.length; i++) {
							StackTraceElement s = elements[i];
							Printer.err("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":"
									+ s.getLineNumber() + ")");
						}
					}
					StackTraceElement[] elements = updater.getStackTrace();
					for (int i = 0; i < elements.length; i++) {
						StackTraceElement s = elements[i];
						Printer.ln("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":"
								+ s.getLineNumber() + ")");
					}
					Printer.ln();
					killSwitch = System.currentTimeMillis();
				}
			}

		}

		swapping = false;

		long now = System.currentTimeMillis();

		while (updater.isAlive() && updateException == null) {
			swapping = false;
			if (System.currentTimeMillis() - now > 10000) {
				Printer.err("updater refuses to die!");
				StackTraceElement[] elements = updater.getStackTrace();
				for (int i = 1; i < elements.length; i++) {
					StackTraceElement s = elements[i];
					Printer.err("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":"
							+ s.getLineNumber() + ")");
				}
				String err = "The game has taken too long to do what it's supposed to do. This can indicate that there is something wrong with the game's engine. It can also be a legit bug. Please report to the dev if your game is choppy or suffers from low FPS. info@songsofsyx.com";
				updateException = new RuntimeException(err);
				break;
			}
			sleep(1);
		}

		Errors.handle(updateException);
		updateException = null;

		dispose();

	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace(System.out);
		}
	}

	private static ArrayList<CORE_RESOURCE> clientDisposables = new ArrayList<CORE_RESOURCE>(20);

	public static void addDisposable(CORE_RESOURCE dis) {
		if (Thread.currentThread() != glThread)
			throw new RuntimeException("gl resource must be created using a gl job :(. Threading sucks!");
		if (dis instanceof TextureHolder)
			getGraphics().setTexture((TextureHolder) dis);
		clientDisposables.add(dis);
	}
	
	public static LIST<CORE_RESOURCE> disposables() {
		return clientDisposables;
	}

	public static abstract class GlJob {
		
		protected abstract void doJob();

		private volatile boolean ru = true;
		
		public final void gc() {
			System.gc();
		}

		public final void perform() {
			
			Thread t = Thread.currentThread();
			
			if (debug) {
				Thread ss = new Thread(new Runnable() {
					
					@Override
					public void run() {
						long now = System.currentTimeMillis();
						while (ru) {
							if (System.currentTimeMillis() - now < 20000)
								sleep(1);
							else {
								System.err.println("gl Thread is stuck!!!");
								for (StackTraceElement e : glThread.getStackTrace())
									System.err.println(e);
								
								System.err.println(t.getName() +  " is stuck!!!");
								for (StackTraceElement e : t.getStackTrace())
									System.err.println(e);
								
								now = System.currentTimeMillis();
							}
						}
					}
				});
				ss.setName("gljob");
				ss.start();
				
			}
			
			if (Thread.currentThread() == glThread && !swapping)
				doJob();
			else {
				glJob = this;
				while (glJob != null && running)
					sleep(1);
			}
			
			ru = false;
		}

	}

	static boolean isRunning() {
		return running;
	}

	public static void annihilate(Throwable e) {
		updateException = e;
		running = false;
		updater.dieHard();
	}

	public static void annihilate() {

		running = false;
		updater.dieHard();
	}

	public static Input getInput() {
		return input;
	}

	public static GraphicContext getGraphics() {
		return graphics;
	}

	public static Renderer renderer() {
		return graphics.renderer;
	}

	public static SOUND_CORE getSoundCore() {
		return soundCore;
	}

	public static CoreTime getUpdateInfo() {
		return updater.getCoreInfo();
	}

	public static void swapAndPoll() {

		if (Thread.currentThread() == glThread && !swapping) {
			graphics.flushRenderer();
			graphics.pollEvents();
			running = graphics.swapAndCheckClose();

			return;
		}

		swapping = true;
		while (swapping)
			sleep(1);

	}

	public static void checkIn() {

		new GlJob() {

			@Override
			protected void doJob() {
				input.poll(System.nanoTime(), graphics.isFocused());
				input.clearAllInput();
			}
		}.perform();
	}

	public static void setCurrentState(CORE_STATE.Constructor stateMaker) {
		newState = stateMaker;
		updater.dieHard();
	}

	private static void dispose() {

		if (!created)
			return;

		Printer.ln();
		Printer.ln("DISPOSING");

		disposeClient();

		clientDisposables.clear();
		if (soundCore != null) {
			soundCore.dis();
			soundCore = null;
		}
		if (input != null) {
			input.dis();
			input = null;
		}

		if (graphics != null) {
			GraphicContext c = graphics;
			graphics = null;
			c.dis();
		}

		created = false;
		swapping = false;
		GraphicContext.terminate();
		Printer.ln("---Core was sucessfully disposed");
		if (GlHelper.debug)
			Errors.check();

	}

	public static boolean isGLThread() {
		return glThread == Thread.currentThread();
	}

	public static Thread GLThread() {
		return glThread;
	}
	
	public static void disposeClient() {
		
		new GlJob() {
			
			@Override
			protected void doJob() {
				for (CORE_RESOURCE d : clientDisposables) {
					String s = GlHelper.getErrors();
					if (s != null) {
						new RuntimeException(s + " " + d).printStackTrace();
					}
					Printer.ln("---" + d);
					d.dis();
					s = GlHelper.getErrors();
					if (s != null) {
						System.err.println(s);
						new RuntimeException(s).printStackTrace();
					}
				}
				clientDisposables.clear();
			}
		}.perform();;


	}

}
