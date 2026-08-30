package com.github.TKnudsen.infoVis.view.gpu;

import com.jogamp.opengl.GL3;

/**
 * <p>
 * Compiles and links the fixed vertex/fragment GLSL shader pair used for
 * anti-aliased point rendering in
 * {@link com.github.TKnudsen.infoVis.view.gpu.scatterplot.ScatterPlotGLJPanel
 * ScatterPlotGLJPanel}, and exposes helpers for setting uniforms.
 * </p>
 *
 * @version 1.0
 */
public class ShaderProgramGLJPanel {
	private GL3 gl;
	private final int programId;
	private final int vertexShaderId;
	private final int fragmentShaderId;

	public ShaderProgramGLJPanel(GL3 gl) {
		this.gl = gl;

		programId = gl.glCreateProgram();

		System.out.println("Creating shader program...");

		// REAL pipeline: aPosition/aColor + uProjection + uPointSize
		String vertexSource = "#version 330 core\n" + "layout(location = 0) in vec2 aPosition;\n"
				+ "layout(location = 1) in vec4 aColor;\n" + "uniform mat4 uProjection;\n"
				+ "uniform float uPointSize;\n" + "out vec4 vColor;\n" + "void main() {\n"
				+ "    gl_Position = uProjection * vec4(aPosition, 0.0, 1.0);\n" + "    gl_PointSize = uPointSize;\n"
				+ "    vColor = aColor;\n" + "}\n";

		String fragmentSource =
			    "#version 330 core\n" +
			    "in vec4 vColor;\n" +
			    "out vec4 FragColor;\n" +
			    "void main() {\n" +
			    "    vec2 coord = gl_PointCoord - vec2(0.5);\n" +
			    "    float dist = length(coord);\n" +
			    "    if (dist > 0.5) discard;\n" +
			    "    float alpha = 1.0 - smoothstep(0.45, 0.5, dist);\n" +
			    "    FragColor = vec4(vColor.rgb, vColor.a * alpha);\n" +
			    "}\n";

		vertexShaderId = compileShader(vertexSource, GL3.GL_VERTEX_SHADER);
		fragmentShaderId = compileShader(fragmentSource, GL3.GL_FRAGMENT_SHADER);

		gl.glAttachShader(programId, vertexShaderId);
		gl.glAttachShader(programId, fragmentShaderId);
		gl.glLinkProgram(programId);

		int[] linkStatus = new int[1];
		gl.glGetProgramiv(programId, GL3.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] == GL3.GL_FALSE) {
			int[] logLength = new int[1];
			gl.glGetProgramiv(programId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
			byte[] log = new byte[Math.max(1, logLength[0])];
			gl.glGetProgramInfoLog(programId, log.length, null, 0, log, 0);
			throw new RuntimeException("Shader link failed: " + new String(log));
		}

		System.out.println("Shader compiled+linked. programId=" + programId);
	}

	private int compileShader(String source, int type) {
		int shaderId = gl.glCreateShader(type);
		gl.glShaderSource(shaderId, 1, new String[] { source }, null);
		gl.glCompileShader(shaderId);

		int[] compileStatus = new int[1];
		gl.glGetShaderiv(shaderId, GL3.GL_COMPILE_STATUS, compileStatus, 0);
		if (compileStatus[0] == GL3.GL_FALSE) {
			int[] logLength = new int[1];
			gl.glGetShaderiv(shaderId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
			byte[] log = new byte[Math.max(1, logLength[0])];
			gl.glGetShaderInfoLog(shaderId, log.length, null, 0, log, 0);
			throw new RuntimeException("Shader compile failed: " + new String(log));
		}
		return shaderId;
	}

	public int id() {
		return programId;
	}

	public void setGL(GL3 gl) {
		this.gl = gl;
	}

	public void use() {
		gl.glUseProgram(programId);
	}

	public void unuse() {
		gl.glUseProgram(0);
	}

	public void setUniformMatrix4fv(String name, float[] matrix16) {
		int loc = gl.glGetUniformLocation(programId, name);
		if (loc < 0) {
			System.err.println("WARNING: uniform '" + name + "' not found (loc=" + loc + ")");
			return;
		}
		gl.glUniformMatrix4fv(loc, 1, false, matrix16, 0);
	}

	public void setUniform1f(String name, float value) {
		int loc = gl.glGetUniformLocation(programId, name);
		if (loc < 0) {
			System.err.println("WARNING: uniform '" + name + "' not found (loc=" + loc + ")");
			return;
		}
		gl.glUniform1f(loc, value);
	}

	public void dispose() {
		unuse();
		gl.glDetachShader(programId, vertexShaderId);
		gl.glDetachShader(programId, fragmentShaderId);
		gl.glDeleteShader(vertexShaderId);
		gl.glDeleteShader(fragmentShaderId);
		gl.glDeleteProgram(programId);
	}
}