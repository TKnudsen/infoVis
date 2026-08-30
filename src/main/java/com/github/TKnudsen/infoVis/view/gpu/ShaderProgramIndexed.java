package com.github.TKnudsen.infoVis.view.gpu;

import com.jogamp.opengl.GL3;

/**
 * <p>
 * Earlier working variant of the GLSL shader program wrapper for anti-aliased
 * point rendering (compile, link, uniforms, dispose). Superseded by
 * {@link ShaderProgramSprite}.
 * </p>
 *
 * @version 1.0
 */
public class ShaderProgramIndexed {
	private GL3 gl;
	private final int programId;
	private final int vertexShaderId;
	private final int fragmentShaderId;

	ShaderProgramIndexed(GL3 gl) {
		this.gl = gl;

		programId = gl.glCreateProgram();

		String vertexSource = "#version 330 core\n" + "layout(location = 0) in vec2 aPosition;\n"
				+ "layout(location = 1) in vec4 aColor;\n" + "uniform mat4 uProjection;\n"
				+ "uniform float uPointSize;\n" + // Add uniform for dynamic size
				"out vec4 vColor;\n" + "void main() {\n"
				+ "    gl_Position = uProjection * vec4(aPosition, 0.0, 1.0);\n" + "    gl_PointSize = uPointSize;\n"
				+ "    vColor = aColor;\n" + "}\n";

		String fragmentSource = "#version 330 core\n" + "in vec4 vColor;\n" + "out vec4 FragColor;\n"
				+ "void main() {\n" + "    // Make points circular with smooth edges\n"
				+ "    vec2 coord = gl_PointCoord - vec2(0.5);\n" + "    float dist = length(coord);\n" + "    \n"
				+ "    // Discard pixels outside circle\n" + "    if (dist > 0.5) discard;\n" + "    \n"
				+ "    // Anti-aliasing on the edge\n" + "    float alpha = 1.0 - smoothstep(0.45, 0.5, dist);\n"
				+ "    \n" + "    FragColor = vec4(vColor.rgb, vColor.a * alpha);\n" + "}\n";

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
			byte[] log = new byte[logLength[0]];
			gl.glGetProgramInfoLog(programId, logLength[0], null, 0, log, 0);
			throw new RuntimeException("Shader link failed: " + new String(log));
		}

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
			byte[] log = new byte[logLength[0]];
			gl.glGetShaderInfoLog(shaderId, logLength[0], null, 0, log, 0);
			throw new RuntimeException("Shader compile failed: " + new String(log));
		}

		return shaderId;
	}

	void setGL(GL3 gl) {
		this.gl = gl;
	}

	void use() {
		gl.glUseProgram(programId);
	}

	void unbind() {
		gl.glUseProgram(0);
	}

	void setUniformMatrix4fv(String name, float[] matrix) {
		int location = gl.glGetUniformLocation(programId, name);
		if (location >= 0)
			gl.glUniformMatrix4fv(location, 1, false, matrix, 0);
	}

	void setUniform1f(String name, float value) {
		int location = gl.glGetUniformLocation(programId, name);
		if (location >= 0)
			gl.glUniform1f(location, value);
	}

	void dispose() {
		unbind();
		gl.glDetachShader(programId, vertexShaderId);
		gl.glDetachShader(programId, fragmentShaderId);
		gl.glDeleteShader(vertexShaderId);
		gl.glDeleteShader(fragmentShaderId);
		gl.glDeleteProgram(programId);
	}
}