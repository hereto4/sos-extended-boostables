package snake2d;

import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.IntBuffer;
import java.util.Arrays;

//import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryUtil;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LinkedList;


final class SoundCore extends SOUND_CORE{
	
	private volatile boolean lock;
	private volatile boolean yippieKayey = false;
	private long context = -1;
	private long device = -1;
	private Thread t;
	
	private final static int MONO_SOURCES = 10;
	private final Source[] monoSources = new Source[MONO_SOURCES];
	private volatile int lastMon = 0;
	private volatile double largestDistance;
	private final int max;
	
	private final static int STEREO_SOURCES = 6;
	private final Source[] stereoSources = new Source[STEREO_SOURCES];
	private volatile int lastStereo = 0;
	private volatile int stereoCount = 0;
	private volatile boolean muteOnFocus;
	
	private static double[] gains = new double[AUDIO_GAIN_TYPE.values().length];
	private static double masterGain = 1.0;
	
	
	SoundCore(String d, SETTINGS settings){
		
		Printer.ln("SOUND");
		
		Arrays.fill(gains, 1.0);
		masterGain = 1.0;
		device = alcOpenDevice(d);
		if (device == NULL) {
			throw new IllegalStateException("Failed to open an OpenAL device.");
		}
		
		max = (settings.getNativeWidth() > settings.getNativeHeight() ?
				settings.getNativeWidth() : settings.getNativeHeight())*4;
		if (device == MemoryUtil.NULL){
			throw new Errors.GameError("Problems with sound device: " + d + ". Could not be opened. Try a different device.");
		}
		ALCCapabilities caps;
		try {
			caps = ALC.createCapabilities(device);
			Source.posTransX = 1f/settings.getNativeWidth();
			Source.posTransY = 1f/settings.getNativeHeight();
			
			if (!caps.OpenALC10){
				throw new Errors.GameError("No OpenALC 10 support found for: " + d + ". Make sure your computer has got this support. Try enabling sound and or / plug in/out speakers/earphones or restart your computer.");
			}
			context = ALC10.alcCreateContext(device, (IntBuffer)null);

			ALC10.alcMakeContextCurrent(context);
			AL.createCapabilities(caps);
			
		}catch(Throwable e) {
			e.printStackTrace();
			throw new Errors.GameError("No OpenALC 10 support found for: " + d + ". Make sure your computer has got this support. Try enabling sound and or / plug in/out speakers/earphones or restart your computer. If you're on linux you might have to adjust settings in pulseaudio");
		}
		
		AL10.alListener3f(AL10.AL_VELOCITY, 0f, 0f, 0f);
		AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);
		AL10.alListener3f(AL10.AL_POSITION, 0f, 0f, 0.5f);
		
		for (int i = 0; i < monoSources.length; i++){
			monoSources[i] = new Source(false);
		}
		
		for (int i = 0; i < STEREO_SOURCES; i++){
			stereoSources[i] = new Source(true);
		}
		
		Printer.ln("---AL version : " + AL10.alGetString(AL10.AL_VERSION));
		Printer.ln("---AL vendor : " + AL10.alGetString(AL10.AL_VENDOR));
		Printer.ln("---AL renderer : " + AL10.alGetString(AL10.AL_RENDERER));
		
		Printer.ln("---OpenALC10: " + caps.OpenALC10);
		Printer.ln("---OpenALC11: " + caps.OpenALC11);
		
		Printer.ln("---ALC_FREQUENCY: " + ALC10.alcGetInteger(device, ALC10.ALC_FREQUENCY) + "Hz");
		Printer.ln("---ALC_REFRESH: " + ALC10.alcGetInteger(device, ALC10.ALC_REFRESH) + "Hz");
		Printer.ln("---ALC_SYNC: " + (ALC10.alcGetInteger(device, ALC10.ALC_SYNC) == ALC10.ALC_TRUE));

		Printer.ln("---Created Mono Sources : " + MONO_SOURCES);
		Printer.ln("---Created Stereo Sources : " + STEREO_SOURCES);
		Printer.fin();
		checkErrors();
		
		yippieKayey = true;
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				while(yippieKayey) {
					
					while(!lock()) {
						if (!yippieKayey)
							return;
						Thread.yield();
					}
					
					double mg = masterGain;
					if (CORE.getGraphics().focused() || !muteOnFocus)
						mg += 0.25;
					else if (muteOnFocus)
						mg -= 0.1;
					mg = CLAMP.d(mg, 0, gains[AUDIO_GAIN_TYPE.MASTER.ordinal()]);
					if (mg != masterGain) {
						masterGain = mg;
						for (int i = 0; i < lastMon; i++){
							monoSources[i].setGain(monoSources[i].gain);
						}
						for (int i = 0; i < lastStereo; i++){
							stereoSources[i].setGain(stereoSources[i].gain);
						}
					}
					
					
					checkErrors();
					largestDistance = 0;
					
					for (int i = 0; i < lastMon; i++){
						if (!monoSources[i].update()){
							lastMon --;
							if (lastMon > 0){
								Source fin = monoSources[i];
								monoSources[i] = monoSources[lastMon];
								monoSources[lastMon] = fin;
								i--;
							}
						}else if(monoSources[i].distance > largestDistance){
							largestDistance = monoSources[i].distance;
						}
					}
					for (int i = 0; i < lastStereo; i++){
						if (!stereoSources[i].update()){
							lastStereo --;
							if (lastStereo > 0){
								Source fin = stereoSources[i];
								stereoSources[i] = stereoSources[lastStereo];
								stereoSources[lastStereo] = fin;
								i--;
							}
						}
					}
					
					checkErrors();
					
					unlock();
//					long now = System.currentTimeMillis();
					sleep();
//					Printer.ln(System.currentTimeMillis()-now);
					
				}
				
				for (int i = 0; i < monoSources.length; i++){
					monoSources[i].dispose();
				}
				for (int i = 0; i < STEREO_SOURCES; i++){
					stereoSources[i].dispose();
				}

				checkErrors();
				disposeSounds();
				checkErrors();
				
				checkErrors();
				ALC10.alcMakeContextCurrent(MemoryUtil.NULL);
				if (ALC10.alcGetError(device) != 0){
					throw new RuntimeException("context not destroyed");
				}
				if (!ALC10.alcCloseDevice(device))
					throw new RuntimeException("device not destroyed");
				
				Printer.ln(SoundCore.class + " sucessfully destroyed");
				
			}
		};
		
		
		t = new Thread(r);
		t.setName("Sounder");
		t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				yippieKayey = false;
				lock = false;
				e.printStackTrace();
				CORE.annihilate(e);
			}
		});
		t.start();
		
	}
	
	private void sleep() {
		try {
			Thread.sleep(128);
		} catch (InterruptedException e) {
			
		}
	}

	
	@Override
	public void set(int cX, int cY){
		while(!lock())
			if (!yippieKayey)
				return;

		if (Source.centreX != cX || Source.centreY != cY){
			Source.centreX = cX;
			Source.centreY = cY;
			for (int i = 0; i < lastMon; i++){
				if (monoSources[i].relative){
					monoSources[i].setPosition(monoSources[i].x, monoSources[i].y);
				}
			}
			
		}
		unlock();
	}
	
	@Override
	public void stopAllSounds(){
		while(!lock())
			if (!yippieKayey)
				return;
		for (int i = 0; i < monoSources.length; i++){
			monoSources[i].stop();
		}
		for (int i = 0; i < STEREO_SOURCES; i++){
			stereoSources[i].stop();
		}
		lastMon = 0;
		lastStereo = 0;
		checkErrors();
		if (ALC10.alcGetError(device) != 0){
			throw new RuntimeException("stopping error");
		}
		unlock();
	}
	
	@Override
	void dis() {
		
		yippieKayey = false;
		if (t != null && t.isAlive()) {
			
			while(t.isAlive())
				t.interrupt();
		}
		
	}
	
	private synchronized boolean lock() {
		if (lock || !yippieKayey)
			return false;
		lock = true;
		return true;
	}
	
	private void unlock() {
		lock = false;
	}

	@Override
	boolean requestMono(AbsBuffer buff, int x, int y, boolean prio, float gain, float pitch){
		return requestMono(buff, x, y, true, prio, gain, pitch);
	}
	
	@Override
	boolean requestMono(AbsBuffer buff, boolean prio, float gain, float pitch){
		return requestMono(buff, Source.centreX, Source.centreY, false, prio, gain, pitch);	
	}
	
	private boolean requestMono(AbsBuffer buff, int x, int y, boolean rel, boolean prio, float gain, float pitch){
		if (!yippieKayey)
			return false;
		if (!lock())
			return false;
		
		double d = 0;
		if (rel) {
			d = COORDINATE.tileDistance(x, y, Source.centreX, Source.centreY);
			if (d > max) {
				unlock();
				return false;
			}
		}
		
		
		if (lastMon < MONO_SOURCES-1){
			monoSources[lastMon].relative = rel;
			monoSources[lastMon].buffer = buff;
			if (d > largestDistance)
				largestDistance = d;
			Source s = monoSources[lastMon++];
			setSource(s, buff, x, y, rel, prio, gain, pitch);
			unlock();
			return true;
		}else if (d < largestDistance) {
			for (int i = 0; i < monoSources.length; i++){
				if (d < monoSources[i].distance){
					monoSources[i].stop();
					setSource(monoSources[i], buff, x, y, rel, prio, gain, pitch);
					unlock();
					return true;
				}
			}
		}else if(prio) {
			int lowest = -1;
			int dist = -1;
			for (int i = 0; i < monoSources.length; i++){
				if (!monoSources[i].prio && monoSources[i].distance > dist)
					lowest = i;
			}
			
			if (lowest != -1) {
				
				monoSources[lowest].stop();
				setSource(monoSources[lowest], buff, x, y, rel, prio, gain, pitch);
				unlock();
				return true;
			}
			
		}
		unlock();
		return false;
		
	}
	
	private void setSource(Source s, AbsBuffer buff, int x, int y, boolean rel, boolean prio, float gain, float pitch) {
		s.buffer = buff;
		s.relative = rel;
		s.prio = prio;
		buff.setBuffer(s);
		s.setGain(gain);
		s.setPitch(pitch);
		s.setPosition(x, y);
	
		s.play();
	}
	
	@Override
	boolean requestStereo(AbsBuffer buff){
		if (yippieKayey && lock()){
			
			if (lastStereo < STEREO_SOURCES){
				stereoSources[lastStereo].buffer = buff;
				stereoSources[lastStereo].distance = stereoCount ++;
				lastStereo += 1;
				buff.setBuffer(stereoSources[lastStereo-1]);
			
			}
			unlock();
			return true;
		}

		return false;
		
	}
	
	@Override
	public void setGain(double gain, AUDIO_GAIN_TYPE type) {
		
		if (gains[type.ordinal()] == gain)
			return;
		gains[type.ordinal()] = gain;
			
		while(!lock())
			if (!yippieKayey)
				return;
		
		for (int i = 0; i < lastMon; i++){
			monoSources[i].setGain(monoSources[i].gain);
		}
		for (int i = 0; i < lastStereo; i++){
			stereoSources[i].setGain(stereoSources[i].gain);
		}
		unlock();
	}

	@Override
	public void setMuteOnFocus(boolean muteOnFocus) {
		this.muteOnFocus = muteOnFocus;
	}

	
	static class Source{
		
		private static float posTransX = 1;
		private static float posTransY = 1;
		private static int centreX = 0;
		private static int centreY = 0;
		
		private final int id;
		private boolean relative;
		private int x;
		private int y;
		private double distance;
		private AbsBuffer buffer;
		private float gain;
		boolean prio;
		
		private Source(boolean stereo){
			id = AL10.alGenSources();
			if (!stereo) {
				AL10.alSourcef(id, AL10.AL_ROLLOFF_FACTOR, 5f);
				AL10.alSourcef(id, AL10.AL_REFERENCE_DISTANCE, 1f);
				AL10.alSourcef(id, AL10.AL_MAX_DISTANCE, 2f);
			}
		}
		
		void setBuffer(int buffID){
			AL10.alSourcei(id, AL10.AL_BUFFER, buffID);
		}

		void enqueueBuffer(int buffID){
			AL10.alSourceQueueBuffers(id, buffID);
		}
		
		int getProcessedBuffers(){
			return AL10.alSourceUnqueueBuffers(id);
		}
		
		boolean hasProcessedBuffer(){
			return AL10.AL_TRUE == AL10.alGetSourcei(id, AL10.AL_BUFFERS_PROCESSED);
		}
		
		void setPosition(int x, int y){
			this.x = x;
			this.y = y;
			distance = COORDINATE.tileDistance(x, y, centreX, centreY);
			AL10.alSource3f(id, AL10.AL_POSITION, (x-centreX)*posTransX, (y-centreY)*posTransY, 0f);
		}
		
		void setPitch(float pitch){
			AL10.alSourcef(id, AL10.AL_PITCH, pitch);
		}
		
		void setGain(float gain){
			this.gain = gain;
			if (gain > 1 || gain < 0) {
				new RuntimeException("" + gain).printStackTrace();
				return;
			}
			float ff = (float) (gain*masterGain*(gains[buffer.type().ordinal()]));
			if (ff < 0 || ff > 1) {
				System.out.println(ff + " " + gain + " " + masterGain + " " + gains[AUDIO_GAIN_TYPE.MASTER.ordinal()] + " " + gains[buffer.type().ordinal()]);
			}
			
			AL10.alSourcef(id, AL10.AL_GAIN, ff);
			checkErrors();
		}
		
		void play(){
			AL10.alSourcePlay(id);;
		}
		
		boolean isPlaying(){
			return AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
		}
		
		void setBufferOffset(float offset){
			AL10.alSourcef(id, AL11.AL_SAMPLE_OFFSET, offset);
		}
		
		float getOffset(){
			return AL10.alGetSourcef(id, AL11.AL_BYTE_OFFSET);
		}
		
		private boolean update(){
			if (buffer.refillBuffers(this)){
				if (!isPlaying())
					AL10.alSourcePlay(id);
					return true;
			}else if (isPlaying()){
				return true;
			}
			stop();
			return false;
		}
		
		void stop(){
			if (buffer != null){
				buffer.reclaimSource(this);
				buffer = null;
			}
			AL10.alSourceStop(id);
			AL10.alSourcei(id, AL10.AL_BUFFER, AL10.AL_NONE);
		}
		
		private void dispose(){
			AL10.alSourceStop(id);
			AL10.alSourcei(id, AL10.AL_BUFFER, AL10.AL_NONE);

			AL10.alDeleteSources(id);
		}
		
		
	}
	
	static void checkErrors(){
		
		switch (AL10.alGetError()){
        case AL10.AL_INVALID_NAME:
        	throw new RuntimeException("AL_INVALID_NAME");
        case AL10.AL_INVALID_ENUM:
        	throw new RuntimeException("AL_INVALID_ENUM");
        case AL10.AL_INVALID_VALUE:
        	throw new RuntimeException("AL_INVALID_VALUE");
        case AL10.AL_INVALID_OPERATION:
        	throw new RuntimeException("AL_INVALID_OPERATION");
        case AL10.AL_OUT_OF_MEMORY:
        	throw new RuntimeException("AL_OUT_OF_MEMORY");
		}
	}
	
	private LinkedList<AbsBuffer> sounds = new LinkedList<AbsBuffer>();
	
	@Override
	public SoundEffect getEffect(java.nio.file.Path path){
		SoundEffect.SoundEffectImp e = new SoundEffect.SoundEffectImp(path);
		sounds.add(e);
		return e;
	}
	
	@Override
	public SoundStream getStream(java.nio.file.Path path, boolean music){
		SoundStream.SoundStreamImp m = new SoundStream.SoundStreamImp(path, music);
		sounds.add(m);
		return m;
	}
	
	@Override
	public SoundEffectStream getStreamMono(java.nio.file.Path path){
		SoundEffectStream.SoundEffectStreamImp m = new SoundEffectStream.SoundEffectStreamImp(path, false);
		sounds.add(m);
		return m;
	}
	
	@Override
	public void disposeSounds() {
		if (sounds.size() > 0) {
			
			for (AbsBuffer e : sounds){
				e.dis();
			}
			sounds.clear();
		}
	}


	
}

