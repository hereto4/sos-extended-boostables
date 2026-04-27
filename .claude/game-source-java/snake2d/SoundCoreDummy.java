package snake2d;

final class SoundCoreDummy extends SOUND_CORE{

	SoundCoreDummy() {

	}
	
	@Override
	public void set(int cX, int cY) {

	}

	@Override
	public void stopAllSounds() {

	}

	@Override
	void dis() {

	}

	@Override
	boolean requestMono(AbsBuffer buff, int x, int y, boolean prio, float gain, float pitch) {
		return false;
	}

	@Override
	boolean requestMono(AbsBuffer buff, boolean prio, float gain, float pitch) {
		return false;
	}

	@Override
	boolean requestStereo(AbsBuffer buff) {
		return false;
	}

	@Override
	public SoundEffect getEffect(java.nio.file.Path path) {
		return new SoundEffect.Dummy();
	}

	@Override
	public SoundStream getStream(java.nio.file.Path path, boolean music) {
		return new SoundStream.Dummy();
	}

	@Override
	public SoundEffectStream getStreamMono(java.nio.file.Path path) {
		return new SoundEffectStream.Dummy();
	}

	@Override
	public void disposeSounds() {
		
	}

	@Override
	public void setGain(double gain, AUDIO_GAIN_TYPE type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMuteOnFocus(boolean muteOnFocus) {
		// TODO Auto-generated method stub
		
	}

}
