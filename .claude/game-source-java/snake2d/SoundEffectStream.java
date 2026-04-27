package snake2d;

import snake2d.SoundCore.Source;
import snake2d.util.misc.CLAMP;

public interface SoundEffectStream extends SoundStream{
	
	public void setCoos(int x, int y);

	static class Dummy implements SoundEffectStream {

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
			return 5;
		}

		@Override
		public void playOnce() {
			play();
		}
		

		@Override
		public void setCoos(int x, int y) {
			// TODO Auto-generated method stub
			
		}

		
		
	}
	
	public class SoundEffectStreamImp extends SoundStream.SoundStreamImp implements SoundEffectStream{
		
		private int x = 0;
		private int y = 0;
		private boolean posChanged = false;
		
		SoundEffectStreamImp(java.nio.file.Path path, boolean music) {
			super(path, music);
		}

		@Override
		public void setCoos(int x, int y){
			this.x = x;
			this.y = y;
			posChanged = true;
			if (isPlaying()){
				
			}
		}
		
		@Override
		protected void set(Source source) {
			source.setPosition(x, y);
		}
		
//		public void setPitch(float pitch){
//			this.pitch = pitch;
//			if (isPlaying()){
//				source.setPitch(pitch);
//			}
//		}
		
		@Override
		boolean refillBuffers(Source source) {
			if (super.refillBuffers(source)) {
				if (posChanged) {
					posChanged = false;
					source.setPosition(x, y);
				}
				return true;
			}
			return false;
		}

	}
	
}
