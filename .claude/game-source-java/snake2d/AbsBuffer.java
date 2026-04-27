package snake2d;

import snake2d.SOUND_CORE.AUDIO_GAIN_TYPE;
import snake2d.SoundCore.Source;

abstract class AbsBuffer{


	
	AbsBuffer(){
		
	}
	abstract void reclaimSource(Source source);
	abstract boolean refillBuffers(Source source);
	abstract void dis();
	abstract void setBuffer(Source source);
	abstract AUDIO_GAIN_TYPE type();
}
