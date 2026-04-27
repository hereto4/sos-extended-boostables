package snake2d;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

abstract class VboAbs {

    private final int vertexArrayID;
    private final int attributeElementID;
    
    final ByteBuffer buffer;
    
    final int MAX_ELEMENTS;
    final int ELEMENT_SIZE;
    private final int BUFFER_SIZE;
    
    private final int NR_OF_ATTRIBUTES;
    

	private final int type;
	private final int indexMul;
    
	VboAbs(int type, int maxElements, VboAttribute... attributes){
		GlHelper.checkErrors();
		MAX_ELEMENTS = maxElements;
		NR_OF_ATTRIBUTES = attributes.length;
		
		this.type = type;
		int vertecies = 0;
		if (type == GL_TRIANGLES) {
			indexMul = 6;
			vertecies = 4;
		}else if (type == GL_POINTS) {
			indexMul = 1;
			vertecies = 1;
		}else {
			throw new RuntimeException("unsupported type");
		}

		int byteStride = 0;
		for (VboAttribute v : attributes) {
			byteStride += v.sizeInBytes;
		}
		
		if (byteStride % 4 != 0)
			throw new RuntimeException(byteStride + " Needs padding with " + (4 - (byteStride % 4)));

		ELEMENT_SIZE = byteStride*vertecies;
		BUFFER_SIZE = ELEMENT_SIZE * MAX_ELEMENTS;
		buffer = MemoryUtil.memAlloc(BUFFER_SIZE);

		vertexArrayID = glGenVertexArrays();
		glBindVertexArray(vertexArrayID);

		attributeElementID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, attributeElementID);

		int index = 0;
		int pointerOffset = 0;
		for (VboAttribute v : attributes) {
			if (v.isInt)
				glVertexAttribIPointer(index, v.amount, v.glType, byteStride, pointerOffset);
			else
				glVertexAttribPointer(index, v.amount, v.glType, v.normalized, byteStride, pointerOffset);
			index ++;
			pointerOffset += v.sizeInBytes;
		}

		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STREAM_DRAW);
		if (type == GL_TRIANGLES) {
			ElementArrays.quadBind();
		}else if (type == GL_POINTS) {
			ElementArrays.pointBind();
		}
		
		
		
		glBindVertexArray(0);

		GlHelper.checkErrors();

		
	}
    
    final void flush(int from, int to){

    	if (from == to)
    		return;
		GL20.glDrawElements(type, (to - from)*indexMul, GL20.GL_UNSIGNED_INT, from * 4* indexMul);
    }
    
    
    void clear(){
    	buffer.clear();
    }
    
    void dis(){
    	GlHelper.checkErrors();
    	// Disable the VBO index from the VAO attributes list
    	glBindVertexArray(vertexArrayID);

		glBindBuffer(GL_ARRAY_BUFFER, attributeElementID);

		for (int i = 0; i < NR_OF_ATTRIBUTES; i++) {
			glDisableVertexAttribArray(i);
		}

    	// Dispose the buffer object
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(attributeElementID);
    	
    	// Dispose the vertex array
        glBindVertexArray(0);
        glDeleteVertexArrays(vertexArrayID);


        // Dispose the element buffer object
        
        GlHelper.checkErrors();
        
        MemoryUtil.memFree(buffer);
    }
    
	final void bindAndUpload() {

		if (buffer.position() == 0)
			return;

		buffer.flip();

		bind();

		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);

	}
	
	final void upload() {
		buffer.flip();
		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
	}

	final void bind() {
		glBindVertexArray(vertexArrayID);

		glBindBuffer(GL_ARRAY_BUFFER, attributeElementID);

		for (int i = 0; i < NR_OF_ATTRIBUTES; i++) {
			glEnableVertexAttribArray(i);
		}
		
		
	}
	
	static class VboAttribute {

		private final boolean isInt;
		private final int amount;
		private final int glType;
		private final int sizeInBytes;
		private final boolean normalized;
		
		public VboAttribute(int amount, int glType, boolean normalized, int sizeInBytes){
			isInt = false;
			this.amount = amount;
			this.glType = glType;
			this.sizeInBytes = sizeInBytes*amount;
			this.normalized = normalized;
		}

		public VboAttribute(int amount, int glType, int sizeInBytes){
			isInt = true;
			this.amount = amount;
			this.glType = glType;
			this.sizeInBytes = sizeInBytes*amount;
			this.normalized = false;
		}
		
	}

}
