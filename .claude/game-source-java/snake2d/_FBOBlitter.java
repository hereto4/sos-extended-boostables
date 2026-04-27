package snake2d;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL13.GL_SAMPLES;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBlitFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;


final class _FBOBlitter {

	private final boolean mul;
	private int ID;
	private int textureID;
	private int blitFilter;
	private boolean scaling;
	private int width;
	private int height;
	private boolean debug;
	
	public _FBOBlitter(SETTINGS sett) {
		
		mul = 
				(sett.getNativeWidth() != sett.display().width || sett.getNativeHeight() != sett.display().height) 
				&& glGetInteger(GL_SAMPLES) > 1;
		this.width = sett.getNativeWidth();
		this.height = sett.getNativeHeight();
		
		if (mul) {
			Printer.ln("This machine has forced multiple sampling enabled. "
					+ "Some performance penalties will ensue. Disable overriding sampling (MSAA, antialiasing) for "
					+ "better performance.");
			ID = glGenFramebuffers();
			glBindFramebuffer(GL_FRAMEBUFFER, ID);
			
			textureID = GlHelper.getFBTexture(sett.display().width, sett.display().height);
			glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
			

			if (GL_FRAMEBUFFER_COMPLETE != glCheckFramebufferStatus(GL_FRAMEBUFFER))
				throw new RuntimeException("Could not create fbo");
			
			glBindFramebuffer(GL_FRAMEBUFFER, 0);

		}
		blitFilter = sett.getLinearFiltering() ? GL_LINEAR : GL_NEAREST;
		scaling = sett.getFitToScreen();
		GlHelper.checkErrors();
		// TODO Auto-generated constructor stub
		debug = sett.debugMode();
	}
	
	private int blitI = 0;
	
	public void blit(int fbID) {
		
		int blitX;
		int blitY;
		int blitW;
		int blitH;
		int dw = CORE.getGraphics().blitArea.x();
		int dy = CORE.getGraphics().blitArea.y();
		
		GlHelper.ViewPort.set(dw, dy);
		
		if (scaling){
			blitX = 0;
			blitY = 0;
			blitW = dw;
			blitH = dy;
		}else{
			double d = (double)dw/width;
			if (height*d > dy)
				d = (double)1.0;
			blitW = (int) (width*d);
			blitH = (int) (height*d);
			if (blitW > dw)
				blitW = dw;
			if (blitH > dy)
				blitH = dy;
			blitX = (dw - blitW)/2;
			blitY = (dy - blitH)/2;
		}
		
		
		
		if (debug && blitI-- < 0) {
			blitI = 1000;
			//Printer.ln("BLITTING " + scaling + " " + blitX + " " + blitW + " " + blitY + " " + blitH + " " + mul);
		}
		
		glBindFramebuffer(GL_READ_FRAMEBUFFER, fbID);
		if (mul) {
			
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER,ID);
			glBlitFramebuffer(0, 0, width, height, blitX, blitY, blitX + blitW, blitY + blitH, GL_COLOR_BUFFER_BIT, blitFilter);
			
			glBindFramebuffer(GL_READ_FRAMEBUFFER,ID);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
			
			glBlitFramebuffer(0, 0, dw, dy, 0, 0, dw, dy, GL_COLOR_BUFFER_BIT, GL_NEAREST);

			
		}else {
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
			glBlitFramebuffer(0, 0, width, height, blitX, blitY, blitX + blitW, blitY + blitH, GL_COLOR_BUFFER_BIT, blitFilter);
		}
		GlHelper.ViewPort.setDefault();
		
	}
	
	
	public void dis() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		glDeleteTextures(textureID);
		GlHelper.checkErrors();
		
	}
	
}
