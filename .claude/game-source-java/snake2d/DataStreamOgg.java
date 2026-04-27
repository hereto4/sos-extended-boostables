package snake2d;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_sample_offset;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_seek;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_seek_start;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_seconds;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbisInfo;

/**
 * Stolen from lwjgl. This class makes a PCI stream out of an ogg file, ready to be played by
 * openal
 * @author mail__000
 *
 */
public final class DataStreamOgg implements DATA_STREAM{

    private static final int BUFFER_SIZE = 4096 * 4;

    private final ByteBuffer vorbis;

    private final long handle;
    private final int  channels;
    private final int  sampleRate;
    private  int  format;

    private final int   lengthSamples;
    private final float lengthSeconds;

    private final ShortBuffer pcm;

    private int samplesLeft;
    
    private final String file;

    DataStreamOgg(java.nio.file.Path filePath) {
        this.file = "" +filePath.getFileName().toAbsolutePath();
    	try {
            vorbis = IOUtil.ioResourceToByteBuffer(filePath, 256 * 1024);
        } catch (IOException e) {
        	throw error(filePath, e.getMessage());
        }

        IntBuffer error = BufferUtils.createIntBuffer(1);
        handle = stb_vorbis_open_memory(vorbis, error, null);
        if (handle == NULL) {
        	throw error(filePath, "Error: " + error.get(0));
           
        }

        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
        	DataStreamOgg.getInfo(handle, info);
            this.channels = info.channels();
            this.sampleRate = info.sample_rate();
        }

        this.format = getFormat(channels);

        this.lengthSamples = stb_vorbis_stream_length_in_samples(handle)*channels;
        this.lengthSeconds = stb_vorbis_stream_length_in_seconds(handle);

        this.pcm = BufferUtils.createShortBuffer(BUFFER_SIZE);

        samplesLeft = lengthSamples;
    }

    private Errors.DataError error(java.nio.file.Path path, String error) {
    	return new Errors.DataError("Could not process .ogg file. Make sure the audio file is truly encoded in ogg/vobis format" + System.lineSeparator() + error, path);
    }
    
    static void getInfo(long decoder, STBVorbisInfo info) {
        
    	stb_vorbis_get_info(decoder, info);
    	
//    	Printer.ln("stream length, samples: " + stb_vorbis_stream_length_in_samples(decoder));
//        Printer.ln("stream length, seconds: " + stb_vorbis_stream_length_in_seconds(decoder));
//
//        Printer.ln();
//
//        Printer.ln("channels = " + info.channels());
//        Printer.ln("sampleRate = " + info.sample_rate());
//        Printer.ln("maxFrameSize = " + info.max_frame_size());
//        Printer.ln("setupMemoryRequired = " + info.setup_memory_required());
//        Printer.ln("setupTempMemoryRequired() = " + info.setup_temp_memory_required());
//        Printer.ln("tempMemoryRequired = " + info.temp_memory_required());
    }

    private static int getFormat(int channels) {
        switch (channels) {
            case 1:
                return AL_FORMAT_MONO16;
            case 2:
                return AL_FORMAT_STEREO16;
            default:
                throw new UnsupportedOperationException("Unsupported number of channels: " + channels);
        }
    }

    @Override
	public boolean hasMoreBuffers(){
		return samplesLeft > 0;
	}
    
    @Override
	public void setNext(int alBuff){
		 int samples = 0;

	        while (samples < BUFFER_SIZE) {
	            pcm.position(samples);
	            int samplesPerChannel = stb_vorbis_get_samples_short_interleaved(handle, channels, pcm);
	            if (samplesPerChannel == 0) {
	                break;
	            }

	            samples += samplesPerChannel * channels;
	        }

	        if (samples == 0) {
	            throw new RuntimeException("getting nonexistant buffer " + file);
	        }
	        
	        pcm.position(0);
	        samplesLeft -= samples;
	        
	        AL10.alBufferData(alBuff, format, pcm, sampleRate);
		
	}
	
    @Override
	public double getProgress(){
		return 1.0 - samplesLeft / (double)(lengthSamples);
	}
	
    @Override
	public float getLengthInSeconds(){
		return lengthSeconds;
	}
    
    @Override
    public void rewind() {
        stb_vorbis_seek_start(handle);
        samplesLeft = lengthSamples;
    }
    
    @Override
	public void dispose() {
		stb_vorbis_close(handle);
	}

    void skip(int direction) {
        seek(min(max(0, stb_vorbis_get_sample_offset(handle) + direction * sampleRate), lengthSamples));
    }

    void skipTo(float offset0to1) {
        seek(round(lengthSamples * offset0to1));
    }

    private void seek(int sample_number) {
        stb_vorbis_seek(handle, sample_number);
        samplesLeft = lengthSamples - sample_number;
    }

}
