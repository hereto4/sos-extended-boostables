package snake2d;

import org.lwjgl.openal.AL10;

import snake2d.SOUND_CORE.AUDIO_GAIN_TYPE;
import snake2d.SoundCore.Source;
import snake2d.util.misc.CLAMP;

public interface SoundStream{

	public boolean play();

	public void playOnce();
	
	public void setGain(double gain);
	

	public void stop();

	public void resume();
	
	public void setLooping(boolean yes);

	public double getProgress();

	public boolean isPlaying();
	
	public double getLengthInSeconds();
	
	static class Dummy implements SoundStream {

		long millis = 0;
		private boolean looping = false;
		
		@Override
		public boolean play() {
			millis = (long) (System.currentTimeMillis()+getLengthInSeconds()*1000);
			return true;
			
		}

		@Override
		public void setGain(double gain) {
			
		}

		@Override
		public void stop() {
			millis = 0;
		}

		@Override
		public void resume() {
			play();
		}

		@Override
		public void setLooping(boolean yes) {
			this.looping = yes;
		}

		@Override
		public double getProgress() {
			double p = 1.0-(millis-System.currentTimeMillis())/(1000.0 + getLengthInSeconds());
			if (looping && millis != 0)
				p %= 1.0;
			return CLAMP.d(p, 0, 1);
		}

		@Override
		public boolean isPlaying() {
			if (looping && millis != 0)
				return true;
			return getProgress() < 1;
		}

		@Override
		public double getLengthInSeconds() {
			return 60;
		}

		@Override
		public void playOnce() {
			play();
		}
		
		
	}
	
	static class SoundStreamImp extends snake2d.AbsBuffer implements SoundStream{

		protected final int[] bufferIDs;
		private final DATA_STREAM data;
		private boolean looping;
		private volatile boolean playing = false;
		private boolean wantsToStop = false;
		
		private boolean gainChanged = false;
		protected float gain = 1f;

		protected float bufferOffset = 0;
		private final AUDIO_GAIN_TYPE type;
		
		SoundStreamImp(java.nio.file.Path path, boolean music) {
			type = music ? AUDIO_GAIN_TYPE.MUSIC : AUDIO_GAIN_TYPE.AMBIENCE;
			this.data = DATA_STREAM.getStream(path);
			final int nrOfBuffers = 3;
			bufferIDs = new int[nrOfBuffers];
			for (int i = 0; i < bufferIDs.length; i++){
				bufferIDs[i] = AL10.alGenBuffers();
			}
			
		}
		
		/**
		 * Start playing this sound. If sound is already playing, nothing will happen.
		 */
		@Override
		public boolean play(){
			wantsToStop = false;
			if (playing)
				return true;
			return play(0);
		}
		
		private boolean play(float off){
			bufferOffset = off;
			playing = CORE.getSoundCore().requestStereo(this);
			return playing;
		}
		
		@Override
		public void playOnce() {
			if (playing) {
				wantsToStop = true;
				return;
			}
			bufferOffset = 0;
			playing = CORE.getSoundCore().requestMono(this, true, gain, 1);
		}
		
		@Override
		void setBuffer(Source source) {
			
			reset();
			for (int i = 0; i < bufferIDs.length; i++){
				source.enqueueBuffer(bufferIDs[i]);
			}
			source.setPitch(1f);
			source.setGain(gain);
			set(source);
			source.play();
			playing = true;
			gainChanged = false;
			wantsToStop = false;
			
		}
		
		protected void set(Source source){

		}
		
		@Override
		public void setGain(double gain){
			if (gain < 0 || gain > 1)
				throw new RuntimeException("" + gain);
			if (gain != this.gain) {
				this.gain = (float) gain;
				gainChanged = true;
			}
		}
		
		/**
		 * Stops this sound from playing
		 */
		@Override
		public void stop(){
			wantsToStop = true;
//			if (playing){
//				source.stop();
//			}
		}

		/**
		 * Resumes this sound from where it was last stopped.
		 */
		@Override
		public void resume(){
			if (playing)
				return;
			play(bufferOffset);
		}
		
		/**
		 * 
		 * @param yes shouldLoop
		 */
		@Override
		public void setLooping(boolean yes) {
			looping = yes;
		}
		
		/**
		 * 
		 * @return length of sound
		 */
		public double getLengthInSeconds() {
			return data.getLengthInSeconds();
		}

		/**
		 * 
		 * @return percentage of song played
		 */
		@Override
		public double getProgress() {
			if (playing){
				return data.getProgress();
			}
			return 0;
		}

		@Override
		public boolean isPlaying() {
			return playing;
		}

		private void reset(){
			data.rewind();
			for (int i = 0; i < bufferIDs.length; i++){
				data.setNext(bufferIDs[i]);
			}
		}
		
		@Override
		void reclaimSource(Source source) {
			
			bufferOffset = source.getOffset();
			playing = false;
			source = null;

		}

		@Override
		public void dis() {

			data.dispose();
			for (int i : bufferIDs){
				AL10.alDeleteBuffers(i);
			}
		}

		@Override
		boolean refillBuffers(Source source) {
			if (wantsToStop) {
				return false;
			}
			
			while(source.hasProcessedBuffer()){
				if (data.hasMoreBuffers()){
					for (int i = 0; i < bufferIDs.length-1; i++)
						bufferIDs[i] = bufferIDs[i+1];
					int buff = source.getProcessedBuffers();
					data.setNext(buff);
					source.enqueueBuffer(buff);
					bufferIDs[bufferIDs.length-1] = buff;
					if (!source.isPlaying())
						source.play();
				}else if (looping){
					data.rewind();
					refillBuffers(source);
				}else{
					source.getProcessedBuffers();
					return false;
				}
				
			}
			if (gainChanged) {
				source.setGain(gain);
				gainChanged = false;
			}
			
			return true;
		}

		@Override
		AUDIO_GAIN_TYPE type() {
			return type;
		}



	}

}
