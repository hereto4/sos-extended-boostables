package snake2d;

import snake2d.util.misc.CLAMP;

class Updater extends Thread{

	private static final long nanoMax = (long) (1000000000*CORE.UPDATE_SECONDS_MAX);
	private static final long nanoMin = (long) (1000000000*CORE.UPDATE_SECONDS_MIN);
	private float secondsRender;
	
	private long maxAmount;
	private long minAmount;
	private long nanoAccumilator;
	
	private long nowTemp;
	
	private long lastUpdate;
	private long lastRender;

	private final CORE_STATE.Constructor constructor;
	private CORE_STATE current;
	private volatile boolean hasTheRightToLive = true;
	
	private final CoreTime info = new CoreTime();
	
	private double slowDown = 1.0;
	
	Updater(CORE_STATE.Constructor current){
		this.constructor = current;
		
	}
	
	@Override
	public void run(){
		
		Thread.currentThread().setName("updater");
		
		
		this.current = constructor.getState();
		
		constructor.doAfterSet();
		System.gc();
		//current.hover(CORE.getInput().getMouse().getCoo(), true);
		
		nanoAccumilator = 0;
		lastUpdate = System.nanoTime();
		lastRender = lastUpdate;
		
		while(CORE.isRunning() && hasTheRightToLive){
			
			long now = System.nanoTime();
			
			if (hasTheRightToLive)
				update();
			if (hasTheRightToLive)
				CORE.getInput().poll(current);
			if (hasTheRightToLive) {
				render();	
			}
			
			now = System.nanoTime()-now;
			double d = now/1000000000.0;
			double f = 1.0/40;
			
			slowDown = d / f;
			slowDown = CLAMP.d(slowDown, 0, slowDown);
			
			if (hasTheRightToLive) {
				
				CORE.swapAndPoll();
				CoreStats.endOfLoopCalc();
			}
		}
		
		current.exit();
		
	}
	
	private void render(){
		nowTemp = System.nanoTime();
		secondsRender = (nowTemp - lastRender)/1000000000f;
		lastRender = nowTemp;
		current.render(CORE.renderer(), secondsRender);
		CoreStats.renderPercentage.set(System.nanoTime()-nowTemp);
	}
	
	private void update(){
		
		nanoAccumilator += System.nanoTime() - lastUpdate;
		lastUpdate = System.nanoTime();
		maxAmount = nanoAccumilator/nanoMax;
		if (maxAmount > 0){
			nanoAccumilator = 0;
		}else{
			minAmount = nanoAccumilator/nanoMin;
			nanoAccumilator -= minAmount*nanoMin;
		}
		
		nowTemp = System.nanoTime();
		float total = CORE.UPDATE_SECONDS_MAX*maxAmount + CORE.UPDATE_SECONDS_MIN*minAmount;
		info.update(total, nowTemp/1000000, nowTemp);
		
		nowTemp = System.nanoTime();
		if (maxAmount > 0){
			maxAmount --;
			current.update(CORE.UPDATE_SECONDS_MAX, slowDown);
		}else{
			current.update(CORE.UPDATE_SECONDS_MIN*minAmount, slowDown);
		}
		CoreStats.droppedTicks.set(maxAmount);
		CoreStats.smallUpdates.set(minAmount);

		CoreStats.updatePercentage.set(System.nanoTime()-nowTemp);
		
	}
	
	void dieHard() {
		hasTheRightToLive = false;
	}
	
	CoreTime getCoreInfo(){
		return info;
	}
	
}
