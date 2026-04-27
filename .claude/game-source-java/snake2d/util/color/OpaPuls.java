package snake2d.util.color;

import snake2d.CORE;

public class OpaPuls extends OpacityImp{
	
	private int base;
	private int delta;
	
	public OpaPuls(int base, int max){
		super(base);
		this.base = base;
		delta = (max - base);

	}
	
	@Override
	public void set(int op){
		base = op;
	}
	
	@Override
	public void increase(float factor){
		base *= factor;
	}
	
	@Override
	public void increase(int amount){
		base += amount;
	}
	
	public void increaseMax(float factor){
		delta*= factor;
	}
	
	public void increaseMax(int amount){
		delta += amount;
	}
	
	@Override
	public void bind() {
		super.set((int) (base + delta*CORE.getUpdateInfo().getPendulum0To1s1()));
		super.bind();
	}
	
}
