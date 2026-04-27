package snake2d;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import snake2d.CORE.GlJob;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.SnakeImage;
import snake2d.util.misc.CLAMP;

public abstract class VIDEO_MAKER {

	private final RECTANGLE start;
	private final RECTANGLE end;
	private final Rec tmp = new Rec();
	private final VectorImp vec = new VectorImp();
	private final int frames;
	private static double FPS = 60;
	private final String path;
	
	public VIDEO_MAKER(RECTANGLE start, RECTANGLE end, int duration, String path) {
		
		this.start = start;
		this.end = end;
		this.path = path;
		if (duration > 20000)
			duration = 20000;
		
		frames = (int) (FPS*duration/1000);
		
		new GlJob() {
			
			@Override
			protected void doJob() {
				for (int f = 0; f <= frames; f++) {
					make(f);
				}
				
				
				makeVideo(path);
			}
		}.perform();
		
		
	}
	
	private void make(int frame) {
	
		
		Rec bounds = frame(frame/(double)frames);
		
		int fw = CORE.getGraphics().nativeWidth;
		int fh = CORE.getGraphics().nativeHeight;
		
		int scale = bounds.width()/fw;
		scale = CLAMP.i(scale, 1, 4);
		
		SnakeImage frameBuffers = new SnakeImage(bounds.width()/scale, bounds.height()/scale);
		
		
		
		
		
		
		
		for (int dy = 0; dy < bounds.height(); dy+=fh) {
			for (int dx = 0; dx < bounds.width(); dx+=fw) {
				
				int x1 = bounds.x1()+dx;
				int y1 = bounds.y1()+dy;
				int w = fw;
				int h = fh;
				if (x1 + w > bounds.x2())
					w = bounds.x2()-x1;
				if (y1 + h > bounds.y2())
					h = bounds.y2()-y1;
				
				
				Rec r = new Rec(w, h);
				r.moveX1Y1(x1, y1);
				
				
				render(r);
				CORE.getGraphics().flushRenderer();
				CORE.getGraphics().copyFB(frameBuffers, dx/scale, dy/scale, scale);
				CORE.getGraphics().pollEvents();
			}
		}
		renderProgress(frame, frames, 1.0/FPS);
		CORE.getGraphics().pollEvents();
		
		SnakeImage result = frameBuffers.resized(fw, fh);
		frameBuffers.dispose();
		
		String id = String.format("%05d", frame);
		
		String p = path + id + ".jpg";
		result.saveJpg(p);
		System.out.println("saving " + p);
		result.dispose();
		System.gc();
		CORE.getInput().clearAllInput();
		
		
	}
	

	private static void makeVideo(String path) {
		String command = "C:\\Users\\jakob\\Desktop\\jakob\\syx\\ffmpeg-master-latest-win64-gpl-shared\\bin\\";
		command += "ffmpeg -r " + FPS + " ";
		command += "-f image2 ";
		command += "-s " + CORE.getGraphics().nativeWidth + "x" + CORE.getGraphics().nativeHeight + " ";
		command += "-i " + path + "%05d.jpg ";
		command += "-vcodec libx264 -crf 25  -pix_fmt yuv420p ";
		command += path + "video.mp4";
		
		System.out.println(command);
		try {
			Files.deleteIfExists(new File(path + "video.mp4").toPath());
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(path + "video.mp4");
		
		
		
	}
	
	private Rec frame(double dd) {
		
		tmp.setWidth(start.width()+(end.width()-start.width())*dd);
		tmp.setHeight(start.height()+(end.height()-start.height())*dd);
		
		double l = vec.set(start.cX(), start.cY(), end.cX(), end.cY());
		l*= dd;
		tmp.moveC(start.cX()+l*vec.nX(), start.cY()+l*vec.nY());
		return tmp;
	}	
	
	public abstract void render(RECTANGLE gamebounds);
	
	public abstract void renderProgress(int frame, int totFrames,  double frameTime);
	
}
