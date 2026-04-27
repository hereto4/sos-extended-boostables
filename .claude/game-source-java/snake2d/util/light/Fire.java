package snake2d.util.light;

import snake2d.util.rnd.RND;

public class Fire extends PointLight {
	
	private static final long serialVersionUID = 1L;
	private float offsetX = 0;
	private float offsetY = 0;
	private float offsetHeight = 0;
	private float intensityOffset = 0;
	private float timer = 0.05f;
	
	private float tmpRed;
	private float tmpGreen;
	private float tmpBlue;
	
	private float flickerFactor;
	
	public Fire(double intensity){
		super(1f*intensity, 0.7f*intensity, 0.3f*intensity);
		super.setZ(50);
		setRadius(100);
		flickerFactor = 20;
		timer = -1;
		flicker(0);
	}
	
	public void flicker(float ds){
		timer -= ds;
		if (timer > 0)
			return;
				
		offsetX = -flickerFactor + RND.rFloat(2*flickerFactor);
		offsetY = -flickerFactor + RND.rFloat(2*flickerFactor);
		offsetHeight = RND.rFloat(flickerFactor/4);
		intensityOffset = (float) (1f + 0.2*RND.rSign()*RND.rExpo());
		tmpRed = super.getRed()*intensityOffset;
		tmpGreen = super.getGreen()*intensityOffset;
		tmpBlue = super.getBlue()*intensityOffset;
		
		timer = 0.025f + RND.rFloat(0.05f);
		
	}
	
	@Override
	public float cx(){
		return super.x()+offsetX;
	}
	
	@Override
	public float cy(){
		return super.y()+offsetY;
	}
	
	@Override
	public float cz(){
		return super.cz()+offsetHeight;
	}
	
	@Override
	public float getRed() {
		return tmpRed;
	}
	
	@Override
	public float getGreen() {
		return tmpGreen;
	}
	
	@Override
	public float getBlue() {
		return tmpBlue;
	}

	public float getFlickerFactor() {
		return flickerFactor;
	}

	public void setFlickerFactor(float flickerFactor) {
		this.flickerFactor = flickerFactor;
	}
	
	public void setIntensity(double d){
		setRed(1*d);
		setGreen(0.7f*d);
		setBlue(0.3f*d);
	}
	
}
