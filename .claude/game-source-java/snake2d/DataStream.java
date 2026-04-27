/* 
 * Copyright (c) 2002-2004 LWJGL Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * * Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of 
 *   its contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package snake2d;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.openal.AL10;


class DataStream implements DATA_STREAM{

	private static final int targetedBufferSize = 4096 * 20;
	private final int bufferSize;
	private final int lastBufferSize;
	private final int nrOfBuffers;
	private int currentBuffer = 0;
	private final byte[] bufferBytes;
	private final byte[] lastBuffer;
	
	private final int totalSize;
	private final int alFormat;
	private final int samplerate;
	private final float length;
	private AudioInputStream stream;
	
	private final String path;

	
	DataStream(java.nio.file.Path path) {
		
		this.path = ""+path;
		
		if (!(path.endsWith(".wav") | path.endsWith(".aiff")))
			throw new RuntimeException("only wav and aiff formats are supported");
		
		try {
			stream = AudioSystem.getAudioInputStream(path.toUri().toURL());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Problem creating sound: " + path);
		}
		
		AudioFormat format = stream.getFormat();
		
		
		length = (float) ((stream.getFrameLength() + 0.0)/format.getFrameRate());
		totalSize = (int) (stream.getFrameLength()*format.getFrameSize());
		
		
			
		bufferSize = (targetedBufferSize/format.getFrameSize())*format.getFrameSize();
		nrOfBuffers = totalSize/bufferSize + (totalSize%bufferSize == 0 ? 0 : 1);
		lastBufferSize = totalSize%bufferSize == 0 ? bufferSize : totalSize%bufferSize;
		
		samplerate = (int) format.getSampleRate();
		
		if (format.getChannels() == 1) {
			if (format.getSampleSizeInBits() == 8) {
				alFormat = AL10.AL_FORMAT_MONO8;
			} else if (format.getSampleSizeInBits() == 16) {
				alFormat = AL10.AL_FORMAT_MONO16;
			} else {
				throw new RuntimeException("Illegal sample size");
			}
		} else if (format.getChannels() == 2) {
			if (format.getSampleSizeInBits() == 8) {
				alFormat = AL10.AL_FORMAT_STEREO8;
			} else if (format.getSampleSizeInBits() == 16) {
				alFormat = AL10.AL_FORMAT_STEREO16;
			} else {
				throw new RuntimeException("Illegal sample size: " + format.getSampleSizeInBits());
			}
		} else {
			throw new RuntimeException("Only mono or stereo is supported");
		}
		
		bufferBytes = new byte[bufferSize];
		lastBuffer = new byte[lastBufferSize];
		
	}
	
	@Override
	public boolean hasMoreBuffers(){
		if (currentBuffer < nrOfBuffers)
			return true;
		return false;
	}
	
	@Override
	public void setNext(int alBuff){
		
		byte[] buf;
		
		if (currentBuffer == nrOfBuffers-1){
			buf = lastBuffer;
		}else{
			buf = bufferBytes;
		}
		
		int read = 0, total = 0;
		try {
			
			while ((read = stream.read(buf, total, buf.length - total)) != -1
				&& total < buf.length) {
				total += read;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		currentBuffer ++;
		
		AL10.alBufferData(alBuff, alFormat, convertAudioBytes(stream.getFormat(), buf, stream.getFormat().getSampleSizeInBits() == 16), samplerate);
	}
	
	@Override
	public double getProgress(){
		if (currentBuffer > 0)
			return (double) ((currentBuffer-1)*bufferSize)/totalSize;
		return 0;
	}
	
	@Override
	public float getLengthInSeconds(){
		return length;
	}
	
	@Override
	public void dispose() {
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convert the audio bytes into the stream
	 * 
	 * @param audio_bytes The audio byts
	 * @param two_bytes_data True if we using double byte data
	 * @return The byte buffer of data
	 */
	private ByteBuffer convertAudioBytes(AudioFormat format, byte[] audio_bytes, boolean two_bytes_data) {
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

	@Override
	public void rewind() {
		
		try {
			stream.close();
			stream = AudioSystem.getAudioInputStream(new File(path));
		} catch (Exception e) {
			System.err.println("Unable to rewind audioStream");
			e.printStackTrace();
			return;
		}
		currentBuffer = 0;
		
	}
}
