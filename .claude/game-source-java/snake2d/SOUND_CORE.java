package snake2d;

public abstract class SOUND_CORE {

	public enum AUDIO_GAIN_TYPE {
		MASTER,
		MUSIC,
		AMBIENCE,
		EFFECT,
	}
	
	public abstract void set(int cX, int cY);
	
	public abstract void stopAllSounds();
	
	abstract void dis();
	

	abstract boolean requestMono(AbsBuffer buff, int x, int y, boolean prio, float gain, float pitch);
	
	abstract boolean requestMono(AbsBuffer buff, boolean prio, float gain, float pitch);

	
	abstract boolean requestStereo(AbsBuffer buff);
	
	public abstract void setGain(double gain, AUDIO_GAIN_TYPE type);
	public abstract void setMuteOnFocus(boolean muteOnFocus);
	

	static SOUND_CORE create(SETTINGS s) {
		if (s.openALDevice() != null) {
			SoundDevices.refresh();
			for (String ss : SoundDevices.get())
				if (ss.equalsIgnoreCase(s.openALDevice()))
					return new SoundCore(ss, s);
			if (SoundDevices.get().size() > 0)
				return new SoundCore(SoundDevices.get().get(0), s);
		}
		return new SoundCoreDummy();
	}
	
	public abstract SoundEffect getEffect(java.nio.file.Path path);
	
	public abstract SoundStream getStream(java.nio.file.Path path, boolean music);
	
	public abstract SoundEffectStream getStreamMono(java.nio.file.Path path);
	
	public abstract void disposeSounds();
	
}

