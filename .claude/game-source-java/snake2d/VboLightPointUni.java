
package snake2d;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL20.glUseProgram;

class VboLightPointUni extends VboAbsExt{
    
	public final static int MAX_ELEMENTS = 65536/4;
	private float[] lights = new float[255*5];
	private final float inv = 1f/255f;
	private final Shader shader;
	private boolean specialLayer;
	
	private final int uColor;
	private final int uShaded;
	private final int uFalloff;
	
	
    VboLightPointUni(SETTINGS sett){
    	
    	super( GL_POINTS,
    			MAX_ELEMENTS,
    			new VboAttribute(4, GL_SHORT, false, 2) //centre
    	);
    	shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "LightPointUni", "LightPointUni", "LightPointUni");

	      shader.setUniform1i("Tdiffuse", 2);
	      shader.setUniform1i("Tnormal", 3);
	      uColor = shader.getUniformLocation("u_color");
	      uShaded = shader.getUniformLocation("u_shaded");
	      uFalloff = shader.getUniformLocation("falloff");
    }   


    
	void upload(float r, float g, float b, float shaded, float falloff){
    	
		shader.setUniform(uShaded, shaded);
		shader.setUniform(uColor, r, g, b);
		shader.setUniform(uFalloff, falloff);
	}
    
	void setNew(){
		if (specialLayer)
			return;
		vTo[current] = count;
		current++;
		vFrom[current] = count;
	}
	
	void setNewButKeepLight(){
		if (specialLayer)
			return;
		vTo[current] = count;
		current++;
		vFrom[current] = vFrom[current-1];
		
	}
	
	void setNewFinal(){
//		if (specialLayer)
//			throw new RuntimeException("can't set final layer twice!");
		specialLayer = true;
		vTo[current] = count;
		current++;
		vFrom[current] = count;
		
	}
	
	void flush() {
		bindAndUpload();
		
		GlHelper.setBlendAdditative();
		GlHelper.enableDepthTest(true);
		GlHelper.setDepthTestLess();
		shader.bind();
		int i = 0;
		vTo[current] = count;
		while (i <= current){
			if (vFrom[i] == vTo[i]) {
				i++;
				continue;
			}
			if (specialLayer && i == current){
				GlHelper.Stencil.setLEQUALKeepOnFail(i);
			}else{
				GlHelper.Stencil.setEQUALKeepOnFail(i);
			}
			int k = i*5;
			upload(lights[k], lights[k+1], lights[k+2], lights[k+3], lights[k+4]);
			flush(vFrom[i], vTo[i]);
			i++;
		}
		glUseProgram(0);
		GlHelper.enableDepthTest(false);
		GlHelper.setBlendNormal();
		clear();

	}

    void render(short x, short y, short z, short radius){
    	
    	if (count >= MAX_ELEMENTS) {
    		return;
    	}
    	buffer.putShort(x).putShort(y).putShort(z).putShort(radius);
    	
    	count++;
    }
    
	void setLight(float radius, float red, float green, float blue, float falloff, byte depth) {
		
		int i = current*5;
		lights[i] = red;
		lights[i+1] = green;
		lights[i+2] = blue;
		lights[i+3] = (depth & 0x0FF)*inv;
		lights[i+4] = falloff;
	}
    
    @Override
    public void dis() {
    	shader.dis();
    	super.dis();
    }
    
    @Override
    public void clear() {
    	current = 0;
		specialLayer = false;
    	super.clear();
    }
    
}
