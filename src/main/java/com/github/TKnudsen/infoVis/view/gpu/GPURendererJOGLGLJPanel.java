package com.github.TKnudsen.infoVis.view.gpu;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * <p>
 * InfoVis GPU Renderer
 *
 * High-performance GPU-accelerated renderer designed for Swing integration.
 * Uses JOGL for proper AWT/Swing compatibility.
 * </p>
 *
 * @author Juergen Bernard (with AI assistance)
 * @version 1.02
 * @since 2026
 */
public class GPURendererJOGLGLJPanel {

	private static final int FLOATS_PER_VERTEX = 6;
	private static final int BYTES_PER_FLOAT = 4;

	private int vaoId = 0;
	private int vboId = 0;
	private Object contextOwner = null; // Track which context owns our VAO

	private ShaderProgramGLJPanel shader;
	private FloatBuffer vertexBuffer;
	private int pointCount = 0;
	private final float[] projection = new float[16];
	private boolean initialized = false;
	private boolean debug = false;
	private boolean printedContextInfo = false;
	private int maxPoints = 200_000;
	/** capacity (in points) the GL-side VBO was last allocated for; grown lazily
	 * in {@link #render(GLAutoDrawable, float)} whenever it falls behind
	 * {@link #maxPoints} (which {@link #addPointSprite} grows on demand). */
	private int glBufferCapacityPoints = 0;

	/**
	 * Contiguous runs of vertices sharing the same point size, in submission
	 * order, so points added via different {@link #addPointSprite} calls (e.g. a
	 * selection halo followed by its point, at different sizes) each render at
	 * their own size instead of a single size for the whole frame.
	 */
	private static final class PointBatch {
		final int startVertex;
		int vertexCount;
		final float pointSize;

		PointBatch(int startVertex, float pointSize) {
			this.startVertex = startVertex;
			this.pointSize = pointSize;
		}
	}

	private final List<PointBatch> batches = new ArrayList<>();
	private PointBatch currentBatch;

	public void init(GLAutoDrawable drawable) {
		final GL3 gl = drawable.getGL().getGL3();

		if (!printedContextInfo) {
			if (debug)
				printContextInfo(gl);
			printedContextInfo = true;
		}

		if (vertexBuffer == null) {
			vertexBuffer = ByteBuffer.allocateDirect(maxPoints * FLOATS_PER_VERTEX * BYTES_PER_FLOAT)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}

		// Store context
		contextOwner = gl.getContext();

		// Create shader in this context
		if (shader != null) {
			shader.dispose();
		}
		shader = new ShaderProgramGLJPanel(gl);

		// Create VAO/VBO in this context
		createOrRecreateVaoVbo(gl);

		initialized = true;

		if (debug)
			System.out.println("GPURendererJOGL.init: context=" + System.identityHashCode(contextOwner) + " vaoId="
					+ vaoId + " vboId=" + vboId + " program=" + shader.id());
	}

	public void render(GLAutoDrawable drawable, float defaultPointSize) {
		if (!initialized || pointCount <= 0)
			return;

		final GL3 gl = drawable.getGL().getGL3();

		// Refresh shader's GL reference -- GLJPanel may rebuild its surface between frames
		if (shader != null) {
			shader.setGL(gl);
		}

		// Clear errors
		while (gl.glGetError() != GL3.GL_NO_ERROR)
			;

		vertexBuffer.flip();

		// GL state
		gl.glDisable(GL3.GL_DEPTH_TEST);
		gl.glEnable(GL3.GL_BLEND);
		gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);

		// GLJPanel may rebuild its backing surface (and thus its GL context) between
		// frames, invalidating a previously-created VAO/VBO -- recreate lazily, only
		// when that has actually happened, instead of unconditionally every frame.
		// Also recreate (at the new, larger size) if addPointSprite grew maxPoints
		// beyond what the GL-side VBO currently holds.
		if (vaoId == 0 || !gl.glIsVertexArray(vaoId) || glBufferCapacityPoints < maxPoints) {
			createOrRecreateVaoVbo(gl);
		}

		shader.use();
		shader.setUniformMatrix4fv("uProjection", projection);

		gl.glBindVertexArray(vaoId);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);

		long bytes = (long) pointCount * FLOATS_PER_VERTEX * BYTES_PER_FLOAT;
		gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, 0, bytes, vertexBuffer);

		// One draw call per size-batch so halos (larger) and their points (smaller)
		// -- or any other size variation across the frame -- each render at their
		// own, correct size.
		if (batches.isEmpty()) {
			shader.setUniform1f("uPointSize", defaultPointSize);
			gl.glDrawArrays(GL3.GL_POINTS, 0, pointCount);
		} else {
			for (PointBatch batch : batches) {
				shader.setUniform1f("uPointSize", batch.pointSize);
				gl.glDrawArrays(GL3.GL_POINTS, batch.startVertex, batch.vertexCount);
			}
		}

		int err = gl.glGetError();
		if (debug)
			System.out.println("Draw result: " + (err == 0 ? "OK" : "ERROR " + err));

		// CRITICAL: Reset state at the end
		resetGLState(gl);

		vertexBuffer.clear();
		pointCount = 0;
		batches.clear();
		currentBatch = null;
	}

	private void createOrRecreateVaoVbo(GL3 gl) {
		if (debug)
			System.out.println("=== createOrRecreateVaoVbo ===");

		// Clear any stale errors
		while (gl.glGetError() != GL3.GL_NO_ERROR)
			;

		// Unbind everything first
		gl.glBindVertexArray(0);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);

		// Delete old resources
		if (vaoId != 0) {
			int[] t = { vaoId };
			gl.glDeleteVertexArrays(1, t, 0);
			vaoId = 0;
		}
		if (vboId != 0) {
			int[] t = { vboId };
			gl.glDeleteBuffers(1, t, 0);
			vboId = 0;
		}

		int[] tmp = new int[1];

		// Create VAO
		gl.glGenVertexArrays(1, tmp, 0);
		vaoId = tmp[0];

		// Create VBO
		gl.glGenBuffers(1, tmp, 0);
		vboId = tmp[0];

		// Bind VAO first (important!)
		gl.glBindVertexArray(vaoId);

		// Bind VBO
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);

		// Allocate buffer (but don't upload data yet)
		long bufferSize = (long) maxPoints * FLOATS_PER_VERTEX * BYTES_PER_FLOAT;
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, bufferSize, null, GL3.GL_DYNAMIC_DRAW);

		// Layout: [x, y, r, g, b, a]
		int stride = FLOATS_PER_VERTEX * BYTES_PER_FLOAT; // 6 * 4 = 24 bytes

		// Attribute 0: position (x, y) - offset 0
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 2, GL3.GL_FLOAT, false, stride, 0);

		// Attribute 1: color (r, g, b, a) - offset 2 floats = 8 bytes
		gl.glEnableVertexAttribArray(1);
		gl.glVertexAttribPointer(1, 4, GL3.GL_FLOAT, false, stride, 2 * BYTES_PER_FLOAT);

		// Check for errors after setup
		int err = gl.glGetError();
		if (err != GL3.GL_NO_ERROR) {
			System.err.println("ERROR during VAO setup: " + err);
		}

		// Unbind (good practice)
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		gl.glBindVertexArray(0);

		glBufferCapacityPoints = maxPoints;

		if (debug) {
			boolean vaoValid = gl.glIsVertexArray(vaoId);
			boolean vboValid = gl.glIsBuffer(vboId);
			System.out.println(
					"VAO " + vaoId + " valid: " + vaoValid + ", VBO " + vboId + " valid: " + vboValid);
		}
	}

	public void updateProjectionMatrix(int viewportX, int viewportY, int viewportW, int viewportH, double minX,
			double maxX, double minY, double maxY) {
		computeOrthoProjection(projection, minX, maxX, minY, maxY);

		if (debug) {
			System.out.println("Updated projection: viewport=" + viewportW + "x" + viewportH + " world=[" + minX + ","
					+ maxX + "] x [" + minY + "," + maxY + "]");
		}
	}

	public void clear() {
		if (vertexBuffer != null)
			vertexBuffer.clear();
		pointCount = 0;
		batches.clear();
		currentBatch = null;
	}

	public int getCurrentBatchSize() {
		return pointCount;
	}

	public void addPointSprite(float worldX, float worldY, float size, Color color) {
		if (!initialized)
			return;
		if (pointCount >= maxPoints)
			growBuffer(Math.max(maxPoints * 2, pointCount + 1));

		float r = (color != null ? color.getRed() / 255f : 1.0f);
		float g = (color != null ? color.getGreen() / 255f : 0.0f);
		float b = (color != null ? color.getBlue() / 255f : 1.0f);
		float a = (color != null ? color.getAlpha() / 255f : 1.0f);

		int vertexIndex = pointCount;
		vertexBuffer.put(worldX).put(worldY).put(r).put(g).put(b).put(a);
		pointCount++;

		if (currentBatch == null || Math.abs(currentBatch.pointSize - size) > 0.01f) {
			currentBatch = new PointBatch(vertexIndex, size);
			batches.add(currentBatch);
		}
		currentBatch.vertexCount++;
	}

	/**
	 * Grows the CPU-side vertex buffer (and, on the next {@link #render}, the
	 * GL-side VBO) to hold at least {@code newCapacityPoints}. Existing buffered
	 * vertex data (added via {@link #addPointSprite} since the last
	 * {@link #render}/{@link #clear}) is preserved.
	 */
	private void growBuffer(int newCapacityPoints) {
		FloatBuffer grown = ByteBuffer.allocateDirect(newCapacityPoints * FLOATS_PER_VERTEX * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.flip();
		grown.put(vertexBuffer);
		vertexBuffer = grown;
		maxPoints = newCapacityPoints;
	}

	/**
	 * Pre-sizes the renderer for up to {@code maxPoints} points, avoiding
	 * repeated buffer growth while ramping up to a known point count. Safe to
	 * call at any time (before or after {@link #init}).
	 */
	public void setMaxPoints(int maxPoints) {
		if (maxPoints > this.maxPoints) {
			if (vertexBuffer == null)
				this.maxPoints = maxPoints;
			else
				growBuffer(maxPoints);
		}
	}

	public int getMaxPoints() {
		return maxPoints;
	}

	public void dispose(GLAutoDrawable drawable) {
		if (!initialized)
			return;

		final GL3 gl = drawable.getGL().getGL3();

		if (shader != null) {
			shader.dispose();
			shader = null;
		}

		if (vboId != 0) {
			int[] tmp = { vboId };
			gl.glDeleteBuffers(1, tmp, 0);
			vboId = 0;
		}

		if (vaoId != 0) {
			int[] tmp = { vaoId };
			gl.glDeleteVertexArrays(1, tmp, 0);
			vaoId = 0;
		}

		initialized = false;
	}

	private void printContextInfo(GL3 gl) {
		try {
			System.out.println("=== GL CONTEXT INFO ===");
			System.out.println("CTX obj=" + gl.getContext());
			System.out.println("CTX hash=" + System.identityHashCode(gl.getContext()));
			System.out.println("GL_VERSION=" + gl.glGetString(GL3.GL_VERSION));
			System.out.println("GL_VENDOR=" + gl.glGetString(GL3.GL_VENDOR));
			System.out.println("GL_RENDERER=" + gl.glGetString(GL3.GL_RENDERER));
			System.out.println("GLSL=" + gl.glGetString(GL3.GL_SHADING_LANGUAGE_VERSION));

			int[] tmp = new int[1];
			gl.glGetIntegerv(GL3.GL_CONTEXT_PROFILE_MASK, tmp, 0);
			System.out.println("GL_CONTEXT_PROFILE_MASK=" + tmp[0]);

			gl.glGetIntegerv(GL3.GL_CONTEXT_FLAGS, tmp, 0);
			System.out.println("GL_CONTEXT_FLAGS=" + tmp[0]);

			System.out.println("=======================");
		} catch (Throwable t) {
			System.err.println("printContextInfo failed: " + t);
		}
	}

	private static void computeOrthoProjection(float[] out, double minX, double maxX, double minY, double maxY) {
		double dx = (maxX - minX);
		double dy = (maxY - minY);
		if (dx == 0.0)
			dx = 1.0;
		if (dy == 0.0)
			dy = 1.0;

		float sx = (float) (2.0 / dx);
		float tx = (float) (-(maxX + minX) / dx);

		float sy = (float) (2.0 / dy);
		float ty = (float) (-(maxY + minY) / dy);

		for (int i = 0; i < 16; i++)
			out[i] = 0f;
		out[0] = sx;
		out[5] = sy;
		out[10] = 1f;
		out[12] = tx;
		out[13] = ty;
		out[15] = 1f;
	}

	private void resetGLState(GL3 gl) {
		// Unbind everything
		gl.glBindVertexArray(0);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		gl.glUseProgram(0);

		// Disable what we enabled
		gl.glDisable(GL3.GL_BLEND);
		gl.glDisable(GL3.GL_PROGRAM_POINT_SIZE);

		// Reset to defaults that GLJPanel expects
		gl.glEnable(GL3.GL_DEPTH_TEST);
	}

	/**
	 * @param debug whether to print verbose per-frame/per-init diagnostics
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}
}
