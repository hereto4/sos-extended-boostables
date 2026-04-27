package snake2d;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Files;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.openal.AL10;

class Data{

	final ByteBuffer data;
	final int sizeInBytes;
	final int alFormat;
	final int channels;
	final int samplerate;
	final float length;
	
	Data(java.nio.file.Path path){
		
		if (!((""+path.toAbsolutePath()).endsWith(".wav")))
			throw new Errors.DataError("only wav and aiff formats are supported", path);
		
		AudioInputStream ais;
		
		try {
			ais = AudioSystem.getAudioInputStream(
					new BufferedInputStream(Files.newInputStream(path)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Problem creating sound: " + path);
		}
		
		AudioFormat audioformat = ais.getFormat();

		length = (float) ((ais.getFrameLength() + 0.0)/audioformat.getFrameRate());
		if (length > 10){
			throw new Errors.DataError("Sound effects more than 10s should not be used as sound effects!", path);
		}
		
		// get channels
		if (audioformat.getChannels() == 1) {
			channels = 1;
			if (audioformat.getSampleSizeInBits() == 8) {
				alFormat = AL10.AL_FORMAT_MONO8;
			} else if (audioformat.getSampleSizeInBits() == 16) {
				alFormat = AL10.AL_FORMAT_MONO16;
			} else {
				throw new Errors.DataError("Illegal sample size", path);
			}
		} else if (audioformat.getChannels() == 2) {
			throw new Errors.DataError("stereo sounds can't be sound effects", path);
		} else {
			throw new Errors.DataError("Only mono or stereo is supported", path);
		}

		//read data into buffer
		byte[] buf =
			new byte[audioformat.getChannels()
				* (int) ais.getFrameLength()
				* audioformat.getSampleSizeInBits()
				/ 8];
		int read = 0, total = 0;
		try {
			while ((read = ais.read(buf, total, buf.length - total)) != -1
				&& total < buf.length) {
				total += read;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		data = convertAudioBytes(audioformat, buf, audioformat.getSampleSizeInBits() == 16);

		try {
			ais.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new RuntimeException("Couldn't close stream!");
		}
		
		samplerate = (int) audioformat.getSampleRate();
		sizeInBytes = data.capacity();
		
	}

	/**
	 * Disposes the wavedata
	 */
	public void dispose() {
		data.clear();
	}	

	/**
	 * Convert the audio bytes into the stream
	 * 
	 * @param audio_bytes The audio byts
	 * @param two_bytes_data True if we using double byte data
	 * @return The byte bufer of data
	 */
	private static ByteBuffer convertAudioBytes(AudioFormat format, byte[] audio_bytes, boolean two_bytes_data) {
		ByteBuffer dest = ByteBuffer.allocateDirect(audio_bytes.length);
		dest.order(ByteOrder.nativeOrder());
		ByteBuffer src = ByteBuffer.wrap(audio_bytes);
		
		if (format.isBigEndian())
			src.order(ByteOrder.BIG_ENDIAN);
		else
			src.order(ByteOrder.LITTLE_ENDIAN);
		if (two_bytes_data) {
			ShortBuffer dest_short = dest.asShortBuffer();
			ShortBuffer src_short = src.asShortBuffer();
			while (src_short.hasRemaining())
				dest_short.put(src_short.get());
		} else {
			while (src.hasRemaining()){
				byte b = src.get();
				if (format.getEncoding() == Encoding.PCM_SIGNED) {
					b = (byte) (b + 127);
				}
				dest.put(b);
			}
		}
		dest.rewind();
		return dest;
	}
}
