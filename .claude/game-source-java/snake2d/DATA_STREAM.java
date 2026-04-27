package snake2d;


interface DATA_STREAM {
	
	static DATA_STREAM getStream(java.nio.file.Path path){
		String p = (""+path.toAbsolutePath());
		
		if (p.endsWith(".wav") || p.endsWith(".aiff")){
			return new DataStream(path);
		}else if(p.endsWith(".ogg")){
			return new DataStreamOgg(path);
		}else
			throw new RuntimeException("only .wav, .aiff and .ogg formats are supported for streaming audio");
	}
	
	public boolean hasMoreBuffers();
	
	public void setNext(int alBuffHandle);
	
	public double getProgress();
	
	public float getLengthInSeconds();
	
	public void dispose();

	public void rewind();
	
}
