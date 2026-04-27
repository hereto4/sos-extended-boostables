package snake2d;

import java.lang.Thread.UncaughtExceptionHandler;

import snake2d.util.misc.ACTION;

public final class SlaveThread extends CORE_RESOURCE{

	private final Thread thread;
	private final long time;
	public final double ds;
	private volatile ACTION job;
	private volatile boolean working = false;
	private volatile boolean shouldWork = false;
	private volatile boolean shouldDie = false;
	private volatile boolean doOnce = false;
	public final String name;
	private volatile long sleepTime;
	
	private volatile double utilization = 0;
	private long sleepDigTimer = System.currentTimeMillis();
	private volatile long lastResponseTime = 0;
	private volatile boolean hasWarnedOfSlowResponse = false;
	
	public SlaveThread(String name, double interval){
		this.ds = interval;
		this.name = name;
		thread = new Thread(runner, name);
		thread.setDaemon(true);
		time = (long) (1000*interval);
		thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				working = false;
				shouldWork = false;
				shouldDie = true;
				e.printStackTrace();
				CORE.annihilate(e);
			}
		});
		CORE.addDisposable(this);
		thread.start();
		lastResponseTime = System.currentTimeMillis();
		hasWarnedOfSlowResponse = false;
	}
	
	public boolean working() {
		return shouldWork && !shouldDie;
	}
	
	public void start(ACTION job) {
		if (shouldWork)
			throw new RuntimeException(thread.getName() +  " is already started");
		this.job = job;
		if (shouldDie)
			throw new RuntimeException("thread is dead");
		lastResponseTime = System.currentTimeMillis();
		shouldWork = true;
	}
	
	public void doOnce(ACTION job) {
		if (shouldWork || doOnce)
			throw new RuntimeException("already started");
		this.job = job;
		lastResponseTime = System.currentTimeMillis();
		doOnce = true;
	}
	
	public void setStopFlag() {
		shouldWork = false;
		lastResponseTime = System.currentTimeMillis();
	}
	
	public void waitUntilStopped() {
		if (Thread.currentThread() == thread) {
			throw new RuntimeException("can't stop yourself");
		}
		shouldWork = false;
		while(working || doOnce) {
			checkForUnresponsiveness();
			sleep(1);
		}
	}
	
	public void kill() {
		shouldDie = true;
		if (thread != Thread.currentThread()) {
			thread.interrupt();
			long now = System.currentTimeMillis();
			while(thread.isAlive()) {
				if (System.currentTimeMillis() - now > 10000) {
					Printer.err(thread.getName() + " refuses to die");
					StackTraceElement[] ee = thread.getStackTrace();
					for (StackTraceElement e : ee)
						System.err.println(e);
					return;
				}
				sleep(0);
			}
		}
	}
	
	public boolean isDead() {
		return thread.isAlive();
	}
	
	public double getUtilization() {
		return utilization;
	}
	
	private void sleep(long milis) {
		if (milis < 0)
			milis = 0;
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			
		}
	}
	
	public void checkForUnresponsiveness() {
		if (shouldDie && !thread.isAlive())
			return;
		long now = System.currentTimeMillis();
		long tmp = now-lastResponseTime;
		if (tmp > time*1000) {
			if (hasWarnedOfSlowResponse) {
				throw new RuntimeException(thread.getName() + " is stuck and will now die!");
			}else {
				Printer.err(thread.getName() + " is stuck!");
				StackTraceElement[] ee = thread.getStackTrace();
				for (StackTraceElement e : ee)
					System.err.println(e);
				lastResponseTime = now;
				hasWarnedOfSlowResponse = true;
				thread.interrupt();
			}
		}
		
	}
	
	@Override
	void dis() {
		kill();
	}
	
	private final Runnable runner = new Runnable() {
		@Override
		public void run() {
			
			while(!shouldDie) {
				lastResponseTime = System.currentTimeMillis();
				hasWarnedOfSlowResponse = false;
				if (doOnce) {
					job.exe();
					job = null;
					doOnce = false;
					shouldWork = false;
				}
				
				if (shouldWork) {
					working = true;
					long now = System.currentTimeMillis();
					job.exe();
					
					
					long workTime = now;
					now = System.currentTimeMillis();
					workTime = now-workTime;
					
					if (workTime > time) {
						continue;
					}
					
					if (System.currentTimeMillis() -sleepDigTimer > 1000) {
						long d = now -sleepDigTimer;
						utilization = d <= 0 ? 0 : 100.0*(1.0 - ((double)sleepTime)/d);
						sleepTime = 0;
						sleepDigTimer = now;
					}
					long st = time-workTime;
					sleepTime += st;
					working = false;
					sleep(st -1);
				}else {
					working = false;
					sleep(1);
				}
			}
			Printer.ln(thread.getName() + " is dead.");
			
		}
	};

}
