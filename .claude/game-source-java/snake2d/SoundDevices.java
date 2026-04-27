package snake2d;

import static org.lwjgl.openal.AL10.AL_EXTENSIONS;
import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.AL_RENDERER;
import static org.lwjgl.openal.AL10.AL_VENDOR;
import static org.lwjgl.openal.AL10.AL_VERSION;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.openal.AL10.alGetString;
import static org.lwjgl.openal.ALC10.ALC_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.ALC_EXTENSIONS;
import static org.lwjgl.openal.ALC10.ALC_MAJOR_VERSION;
import static org.lwjgl.openal.ALC10.ALC_MINOR_VERSION;
import static org.lwjgl.openal.ALC10.ALC_NO_ERROR;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetError;
import static org.lwjgl.openal.ALC10.alcGetInteger;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.openal.ALC11.ALC_CAPTURE_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.Objects;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALUtil;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class SoundDevices {

	private ArrayList<String> available;
	
	SoundDevices() {

		

		available = new ArrayList<String>(ALUtil.getStringList(NULL, ALC11.ALC_ALL_DEVICES_SPECIFIER));
//		Printer.ln("SOUND-INFO");
//		for (String s : available)
//			check(s);

		// byte[] bs = ss.get(1).getBytes();
		// ByteBuffer bb = ByteBuffer.allocateDirect(bs.length+8);
		// bb.put(bs);
		// bb.putLong(NULL);
		// bb.flip();
		//
		//
		// long device = ALC10.alcOpenDevice(bb);
		// ALC10.alcMakeContextCurrent(NULL);
		// if (ALC10.alcGetError(device) != 0){
		// throw new RuntimeException("context not destroyed");
		// }
		// if (!ALC10.alcCloseDevice(device))
		// throw new RuntimeException("device not destroyed");
	}

	private static SoundDevices self;
	
	public static LIST<String> get(){
		if (self == null)
			self = new SoundDevices();
		return self.available;
	}
	
	public static void refresh() {
		self = new SoundDevices();
	}
	
	public void check(String name) {

		Printer.ln(name);

		long device = alcOpenDevice(name);
		if (device == NULL) {
			throw new IllegalStateException("Failed to open an OpenAL device.");
		}

		ALCCapabilities deviceCaps = ALC.createCapabilities(device);

		long context = alcCreateContext(device, (IntBuffer) null);
		checkALCError(device);

		boolean useTLC = deviceCaps.ALC_EXT_thread_local_context && alcSetThreadContext(context);
		if (!useTLC) {
			if (!alcMakeContextCurrent(context)) {
				throw new IllegalStateException();
			}
		}
		checkALCError(device);

		AL.createCapabilities(deviceCaps);

		printALCInfo(device, deviceCaps);
		printALInfo();

		alcMakeContextCurrent(NULL);
		if (useTLC) {
			AL.setCurrentThread(null);
		} else {
			AL.setCurrentProcess(null);
		}

		alcDestroyContext(context);
		alcCloseDevice(device);

		Printer.ln();
	}

	private static void printALCInfo(long device, ALCCapabilities caps) {

		Printer.ln("Default capture device: " + alcGetString(0, ALC_CAPTURE_DEFAULT_DEVICE_SPECIFIER));

		Printer.ln("ALC device specifier: " + alcGetString(device, ALC_DEVICE_SPECIFIER));

		int majorVersion = alcGetInteger(device, ALC_MAJOR_VERSION);
		int minorVersion = alcGetInteger(device, ALC_MINOR_VERSION);
		checkALCError(device);

		Printer.ln("ALC version: " + majorVersion + "." + minorVersion);

		
		String[] extensions = Objects.requireNonNull(alcGetString(device, ALC_EXTENSIONS)).split(" ");
		Printer.ln("ALC extensions:", extensions);
		checkALCError(device);
	}

	private static void printALInfo() {
		Printer.ln("OpenAL vendor string: " + alGetString(AL_VENDOR));
		Printer.ln("OpenAL renderer string: " + alGetString(AL_RENDERER));
		Printer.ln("OpenAL version string: " + alGetString(AL_VERSION));

		String[] extensions = Objects.requireNonNull(alGetString(AL_EXTENSIONS)).split(" ");
		Printer.ln("AL extensions:", extensions);
		checkALError();
	}

	static void checkALCError(long device) {
		int err = alcGetError(device);
		if (err != ALC_NO_ERROR) {
			throw new RuntimeException(alcGetString(device, err));
		}
	}

	static void checkALError() {
		int err = alGetError();
		if (err != AL_NO_ERROR) {
			throw new RuntimeException(alGetString(err));
		}
	}

	public static void main(String[] args) {
		new SoundDevices();
	}

}
