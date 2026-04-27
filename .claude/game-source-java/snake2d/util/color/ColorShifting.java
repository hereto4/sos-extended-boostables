package snake2d.util.color;

import snake2d.CORE;

public class ColorShifting extends ColorImp{
	
	private static final long serialVersionUID = 1L;
	private int dRed;
	private int dGreen;
	private int dBlue;
	private int r;
	private int g;
	private int b;
	private float speed = 0.5f;
	private double old = -1;
	
	public ColorShifting(COLOR from, COLOR to) {
		super(0, 0, 0);
		r = Byte.toUnsignedInt(from.red());
		g = Byte.toUnsignedInt(from.green());
		b = Byte.toUnsignedInt(from.blue());
		
		
		dRed = Byte.toUnsignedInt(to.red()) - r;
		dGreen = Byte.toUnsignedInt(to.green()) - g;
		dBlue = Byte.toUnsignedInt(to.blue()) - b;
	}
	
	private void update() {
		if (old != CORE.getUpdateInfo().getSecondsSinceFirstUpdate()) {
			old = CORE.getUpdateInfo().getSecondsSinceFirstUpdate();
			double timer = CORE.getUpdateInfo().getSecondsSinceFirstUpdate()*speed;
			
			double d = timer-(int)timer;
			
			if (d > 0.5) {
				timer = 1.0 - (d-0.5)*2;
			}else {
				timer = d*2.0;
			}
			
			
			
			setRed((int) (r + timer*dRed));
			setGreen((int) (g + timer*dGreen));
			setBlue((int) (b + timer*dBlue));
		}
	}
	
	@Override
	public void bind() {
		
		update();
		super.bind();
	}
	
	@Override
	public byte red() {
		update();
		return super.red();
	}
	
	public ColorShifting setSpeed(double speed){
		this.speed = (float) speed;
		return this;
	}
	
}
