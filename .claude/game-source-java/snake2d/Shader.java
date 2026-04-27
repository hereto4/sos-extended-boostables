package snake2d;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORMS;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import java.io.InputStream;
import java.nio.charset.Charset;

final class Shader {

	protected int programID;
	private int vertexShaderID;
	private int fragmentShaderID;
	private int geometryShaderID = -1;
	
	public Shader(double width, double height, String vertex, String geometry, String fragment) {
		
		programID = glCreateProgram();
		vertex = getFile(vertex, "v");
		vertex = vertex.replace("SCREEN_X", "" + (2f / width));
		vertex = vertex.replace("SCREEN_Y", "" + (-2f / height));
		
		vertexShaderID = attachShader(vertex, programID, GL_VERTEX_SHADER);
		
		fragment = getFile(fragment, "f");
		
		fragmentShaderID = attachShader(fragment, programID, GL_FRAGMENT_SHADER);
		
		
		if (geometry != null) {
			geometry = getFile(geometry, "g");
			geometryShaderID = attachShader(geometry, programID, GL_GEOMETRY_SHADER);
		}
	
		link();
		bind();
		Printer.ln(" " + this.getClass() + ": " + programID + ", ");
	}

	public static String getFile(String name, String append) {
		

		name += "_" + append + ".txt";
		try {
			InputStream fis = Shader.class.getResourceAsStream(name);
			byte[] bs = new byte[100000];
			int size = 0;
			while(true) {
				int r = fis.read();
				if (r == -1)
					break;
				bs[size] = (byte) r;
				size++;
			}
			byte[] res = new byte[size];
			for (int i = 0; i < res.length; i++)
				res[i] = bs[i];
			
			return new String(res, Charset.forName("utf-8"));
		}catch(Exception e) {
			throw new RuntimeException(name);
		}
		
	}
	
	private static int attachShader(String source, int programID, int type) {

		int id = glCreateShader(type);
		if (id == 0) {
			throw new RuntimeException("shader didn't compile");
		}
		glShaderSource(id, source);
		glCompileShader(id);

		if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Error creating shader\n"
					+ glGetShaderInfoLog(id, glGetShaderi(id, GL_INFO_LOG_LENGTH)));

		glAttachShader(programID, id);
		return id;

	}

	/**
	 * Links this program in order to use.
	 */
	private void link() {

		glLinkProgram(programID);

		if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Unable to link shader program:");
	}

	/**
	 * Bind this program to use.
	 */
	public void bind() {
		glUseProgram(programID);
	}

	protected void bindAttribute(int possition, String name) {
		glBindAttribLocation(programID, possition, name);
	}

	/**
	 * Unbind the shader program.
	 */
	public void unbind() {
		glUseProgram(0);
	}


	public void dis() {

		unbind();

		glDetachShader(programID, vertexShaderID);
		glDetachShader(programID, fragmentShaderID);
		if (geometryShaderID != -1) {
			glDetachShader(programID, geometryShaderID);
		}

		glDeleteShader(vertexShaderID);
		glDeleteShader(fragmentShaderID);
		if (geometryShaderID != -1) {
			glDeleteShader(geometryShaderID);
		}
		
		glDeleteProgram(programID);
	}

	/**
	 * @return The ID of this program.
	 */
	public int getID() {
		return programID;
	}

	public int getUniformLocation(String name) throws RuntimeException {

		int id = glGetUniformLocation(programID, name);
		if (id == -1 || glGetProgrami(programID, GL_ACTIVE_UNIFORMS) == 0)
			throw new RuntimeException("not able to find shader uniform: " + name);
		return id;
	}

	public String getScreenVec(float width, float height) {
		float w =  2f / width;
		float h = -2f / height;
		return "const vec2 screen = vec2(" + w + "," + h + ");" + "\n";
	}

	protected void setUniform2f(int loc, float a, float b) {
		glUniform2f(loc, a, b);
	}
	
	protected void setUniform(int loc, float a) {
		glUniform1f(loc, a);
	}
	
	protected void setUniform(int loc, float a, float b, float c) {
		glUniform3f(loc, a, b, c);
	}
	
	public void setUniform1i(String name, int a) {
		glUniform1i(getUniformLocation(name), a);
	}
	
}
