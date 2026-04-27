package snake2d;

import org.lwjgl.openal.AL10;

import snake2d.SOUND_CORE.AUDIO_GAIN_TYPE;
import snake2d.SoundCore.Source;
import snake2d.util.datatypes.RECTANGLE;


public interface SoundEffect extends SoundSimple{
	
	
	public boolean play(int x, int y, float pitch, float gain, boolean priority);
	
	public boolean play(RECTANGLE rec, float pitch, float gain, boolean priority);
	
	public boolean play(int x, int y, boolean priority);
	
	
	
	public boolean play(float pitch, float gain, boolean priority);
	
	public float lengthInSeconds();
	
	static class SoundEffectImp extends snake2d.AbsBuffer implements SoundEffect{
		
		final int ID;
		private final Data data;
		
		SoundEffectImp(java.nio.file.Path path){
			data = new Data(path);
			
			ID = AL10.alGenBuffers();
			
			AL10.alBufferData(ID, data.alFormat, data.data, data.samplerate);
			
			data.dispose();
		}
		
		@Override
		void dis() {
			AL10.alDeleteBuffers(ID);
		}
		
		@Override
		public boolean play(int x, int y, float pitch, float gain, boolean priority){
			
			return CORE.getSoundCore().requestMono(this, x, y, priority, gain, pitch);
		}
		
		@Override
		public final boolean play(RECTANGLE rec, float pitch, float gain, boolean priority) {
			return play(rec.cX(), rec.cY(), pitch, gain, priority);
		}
		
		@Override
		public boolean play(int x, int y, boolean priority){
			return play(x, y, 1f, 1f, priority);
		}
		
		@Override
		public boolean play(boolean priority){
			return play(1f, 1f, priority);
		}
		
		@Override
		public boolean play(float pitch, float gain, boolean priority){
			return CORE.getSoundCore().requestMono(this, priority, gain, pitch);
		}
		
		@Override
		public float lengthInSeconds(){
			return data.length;
		}
		
		@Override
		void reclaimSource(Source source) {
			// TODO Auto-generated method stub
			
		}

		@Override
		boolean refillBuffers(Source source) {
			return false;
		}

		@Override
		void setBuffer(Source source) {
			source.setBuffer(ID);
		}

		@Override
		AUDIO_GAIN_TYPE type() {
			return AUDIO_GAIN_TYPE.EFFECT;
		}
		
		
	}
	
	static class Dummy implements SoundEffect {

		@Override
		public boolean play(int x, int y, float pitch, float gain, boolean priority) {
			return true;
		}

		@Override
		public boolean play(RECTANGLE rec, float pitch, float gain, boolean priority) {
			return true;
		}

		@Override
		public boolean play(int x, int y, boolean priority) {
			return true;
		}

		@Override
		public boolean play(boolean priority) {
			return true;
		}

		@Override
		public boolean play(float pitch, float gain, boolean priority) {
			return true;
		}

		@Override
		public float lengthInSeconds() {
			return 5;
		}
		
	}
	
}
