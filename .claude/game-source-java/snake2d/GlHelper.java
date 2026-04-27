package snake2d;


import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_FRONT_LEFT;
import static org.lwjgl.opengl.GL11.GL_GEQUAL;
import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_KEEP;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_MAX_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_RENDERER;
import static org.lwjgl.opengl.GL11.GL_REPLACE;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_VENDOR;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glClearStencil;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glFlush;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glStencilOp;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_MAX_ELEMENTS_VERTICES;
import static org.lwjgl.opengl.GL13.GL_SAMPLES;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glGetFramebufferAttachmentParameteri;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Callback;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

class GlHelper {
	
	private final Callback callback;
	public static String renderer;
	public static String rendererV;
	private static final Coo FBSize = new Coo();
	public static boolean debug;
	
	GlHelper(int viewPortWidth, int viewPortHeight, boolean debug){
		

		GLCapabilities g = GL.createCapabilities();
		checkErrors();
		if (debug)
			callback = GlDebugger.setupDebugMessageCallback();
		else
			callback = null;
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glEnable(GL_BLEND);
		setBlendNormal();
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glEnable(GL_STENCIL_TEST);
		glDisable(GL_CULL_FACE);
		glClearStencil(~0);
		glClearDepth(0);
		GlHelper.debug = debug;
		FBSize();
		int[] dd = new int[4];
		glGetIntegerv(GL11.GL_VIEWPORT, dd);
		FBSize.set(dd[2], dd[3]);
		
		ViewPort.setDefault(viewPortWidth, viewPortHeight);
		checkErrors();
		Printer.ln("OPEN_GL");
		Printer.ln("---FB size: " + FBSize.x() + "x" + FBSize.y());
//		Printer.ln("---FB stencil Bits: " + glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_STENCIL, GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE));
//		Printer.ln("---FB depth Bits: " + glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_DEPTH, GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE));
		Printer.ln("---FB Red Bits: " + glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_FRONT_LEFT, GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE));
		Printer.ln("---FB Green Bits: " + glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_FRONT_LEFT, GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE));
		Printer.ln("---FB Blue Bits: " + glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_FRONT_LEFT, GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE));
		Printer.ln("---FB Alpha Bits: " + glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_FRONT_LEFT, GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE));
		Printer.ln("---FB Samples: " + glGetInteger(GL_SAMPLES));
		Printer.ln("---Max Texture Dim: " + glGetInteger(GL_MAX_TEXTURE_SIZE));
		Printer.ln("---Version: " + glGetString(GL_VERSION));
		Printer.ln("---SL Version: " + glGetString(GL_SHADING_LANGUAGE_VERSION));
		Printer.ln("---Max Vert: " + glGetInteger(GL_MAX_ELEMENTS_VERTICES));

		renderer = glGetString(GL_VENDOR) + ", " + glGetString(GL_RENDERER);
		rendererV = glGetString(GL_VERSION);
		Printer.ln("---glRenderer: " + renderer); 
		Printer.ln("---Forward compatible: " + g.forwardCompatible);
		
		Printer.fin();
		
		String s = getErrors();
		if (s != null) {
			Printer.ln("---error at query: " + s);
		}
	
//		if (!g.OpenGL33) {
//			
//			String m = "Your graphics card is reported to be: " + glGetString(GL_RENDERER) + ", " + glGetString(GL_VENDOR)  
//			+ ". It has no support for opengl 3.3 If you have several GPUs', try and select the most powerful one to run the game.";
//			throw new Errors.GameError(m);
//		}
		
		
		checkErrors();
	}
	
	public static COORDINATE FBSize() {
//		int[] dd = new int[4];
//		glGetIntegerv(GL11.GL_VIEWPORT, dd);
//		FBSize.set(dd[2], dd[3]);
		return FBSize;
	}
	
	ByteBuffer getFramePixels(int width, int height){
		
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		int bpp = 4;
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		return buffer;
	}
	
	static void checkErrors(){

		String e = getErrors();
		if (debug && e != null) {
			System.err.println(e);
			throw new RuntimeException("The game has crashed, due to opengl errors. " + e + ". You can avoid this crash in the future by unchecking the 'debug' setting in the launcher.");
		}
		
	}
	
	private static final String sInvalid = "GLerr: invalid enum";
	private static final String sValue = "GLerr: invalid value";
	private static final String sOp = "GLerr: invalid operation";
	private static final String sOver = "GLerr: stack overflow";
	private static final String sUnder = "GLerr: stack underflow";
	private static final String sMem = "GLerr: out of memory";
	private static final String sFB = "GLerr: invalid FB operation";
	private static final String sUnknown = "GLerr: unknown";
	
	static String getErrors(){

		switch(glGetError()){
		case GL_NO_ERROR: return null;
		case GL_INVALID_ENUM : return sInvalid;
		case GL_INVALID_VALUE : return sValue;
		case GL_INVALID_OPERATION : return sOp;
		case GL_STACK_OVERFLOW : return sOver;
		case GL_STACK_UNDERFLOW : return sUnder;
		case GL_OUT_OF_MEMORY : diagnozeMem(); return sMem;
		case GL_INVALID_FRAMEBUFFER_OPERATION : return sFB;
		default: return sUnknown;
		}
		
	}
	
	static int getFBTexture(int width, int height){
		
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL12.GL_BGRA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
		return id;
		
	}
	
	static void setBlendNormal(){
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	static void setBlendAdditative(){
		glBlendFunc(GL_ONE, GL_ONE);
	}
	
	static void bindNormalFrameBuffer(){
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}
	
	static void clearCurrentFrameBufferColor2(){
		glClear(GL_COLOR_BUFFER_BIT);
	}
	
	static void finsih(){
		glFinish();
	}
	
	static void flush(){
		glFlush();
	}
	
	static void enableBlend(boolean yes){
		if(yes){
			glEnable(GL_BLEND);
		}else{
			glDisable(GL_BLEND);

		}
	}
	
	static void enableDepthTest(boolean yes){
		if(yes){
			glEnable(GL_DEPTH_TEST);
		}else{
			glDisable(GL_DEPTH_TEST);

		}
		glDepthMask(true);
	}
	
	static void setDepthTestAlways(){
		glDepthMask(true);
		glDepthFunc(GL_GEQUAL);
	}
	
	static void setDepthTestLess(){
		glDepthMask(false);
		glDepthFunc(GL_GEQUAL);
	}
	
	static class Stencil {
		
		static void enable(boolean yes){
			if(yes){
				glEnable(GL_STENCIL_TEST);
			}else{
				glDisable(GL_STENCIL_TEST);
			}
		}
		
		static void setLEQUALreplaceOnPass(int value){
			glStencilFunc(GL_LEQUAL , value, ~0); 
			glStencilOp(GL_KEEP, GL_KEEP ,GL_REPLACE);
		}
		
		static void setLEQUALclear(int value){
			glStencilFunc(GL_LEQUAL , value, ~0); 
			glStencilOp(GL_KEEP, GL_KEEP , GL_ZERO);
		}
		
		static void setLESSKeepOnPass(int value){
			glStencilFunc(GL_LESS , value, ~0); 
			glStencilOp(GL_KEEP, GL_KEEP ,GL_KEEP);
		}
		
		static void setLESSreplaceOnPass(int value){
			glStencilFunc(GL_LESS , value, ~0); 
			glStencilOp(GL_KEEP, GL_KEEP ,GL_REPLACE);
		}
		
		static void setGEQUALReplaceOnPass(int value){
			glStencilFunc(GL_GEQUAL , value, ~0); 
			glStencilOp(GL_KEEP, GL_KEEP ,GL_REPLACE);
		}
		
		static void setEQUALKeepOnFail(int value){
			glStencilFunc(GL_EQUAL, value, ~0); 
			glStencilOp(GL_KEEP, GL_KEEP ,GL_KEEP);
		}
		
		static void setLEQUALKeepOnFail(int value){
			glStencilFunc(GL_LEQUAL, value, ~0); 
			glStencilOp(GL_KEEP, GL_KEEP ,GL_KEEP);
		}
		
		static void setAlways(int value){
			glStencilFunc(GL_ALWAYS, value, ~0); 
			glStencilOp(GL_REPLACE, GL_REPLACE ,GL_REPLACE);
		}
		
	}

	static class ViewPort{
		
		private static int dWidth;
		private static int dHeight;
		
		static void setDefault(int width, int height){
			dWidth = width;
			dHeight = height;
			glViewport(0,0,width,height);
		}
		
		static void setDefault() {
			glViewport(0,0,dWidth,dHeight);
		}
		
		static void set(int width, int height){
			glViewport(0,0,width,height);
		}
		
	}
	
	public void dispose() {
		GlHelper.checkErrors();
		
		if (callback != null) {
			callback.free();
		}
		GlHelper.checkErrors();
		GL.setCapabilities(null);
	}
	
	static void diagnozeMem() {
		
		int mb = 1014*1024;
		
		
		System.err.println("MEM DIAGNOSE");
		Runtime run = Runtime.getRuntime();
		// available memory
		System.err.println("--JRE Memory");
		System.err.println("--JRE Total: " + run.totalMemory() / mb);
		System.err.println("--JRE Free: " + run.freeMemory() / mb);
		System.err.println("--JRE Used: "
				+ (run.totalMemory() - run.freeMemory()) / mb);
		System.err.println("--JRE Max: " + run.maxMemory() / mb);
		System.gc();
		System.err.println("--JRE Memory After GC");
		System.err.println("--JRE Total: " + run.totalMemory() / mb);
		System.err.println("--JRE Free: " + run.freeMemory() / mb);
		System.err.println("--JRE Used: "
				+ (run.totalMemory() - run.freeMemory()) / mb);
		System.err.println("--JRE Max: " + run.maxMemory() / mb);
		int i;
		System.err.println("NVIDIA: ");
		i = glGetInteger(org.lwjgl.opengl.NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
		System.err.println("--GPU Dedicated: " + i);
		i = glGetInteger(org.lwjgl.opengl.NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
		System.err.println("--GPU Total Available: " + i);
		i = glGetInteger(org.lwjgl.opengl.NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
		System.err.println("--GPU Current Available: " + i);
		i = glGetInteger(org.lwjgl.opengl.NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTION_COUNT_NVX);
		System.err.println("--GPU Evictions: " + i);
		i = glGetInteger(org.lwjgl.opengl.NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX);
		System.err.println("--GPU Evicted: " + i);
		
		System.err.println("ATI: ");
		i = glGetInteger(org.lwjgl.opengl.ATIMeminfo.GL_RENDERBUFFER_FREE_MEMORY_ATI);
		System.err.println("--Renderbuffer Free: " + i);
		i = glGetInteger(org.lwjgl.opengl.ATIMeminfo.GL_TEXTURE_FREE_MEMORY_ATI);
		System.err.println("--Texture Free: " + i);
		i = glGetInteger(org.lwjgl.opengl.ATIMeminfo.GL_VBO_FREE_MEMORY_ATI);
		System.err.println("--Vbo Free: " + i);
		
		while (glGetError() != GL11.GL_NO_ERROR) {
			String err = getErrors();
			if (err == null)
				break;
			Printer.ln("ignored: " + err);
		}
	}

	
}
