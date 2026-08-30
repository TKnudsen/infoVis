package com.github.TKnudsen.infoVis.view.gpu;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * <p>
 * Indexed multi-primitive GPU renderer: batches heterogeneous
 * {@link DrawCommand draw commands} (points, lines, triangles) into a single
 * fixed-size, pre-allocated vertex/index buffer pair ({@code MAX_VERTICES}
 * capacity, allocated once) and issues them via {@code glDrawElements}.
 * Suited to geometry whose upper bound is known ahead of time and where
 * indexed (shared-vertex) primitives are worthwhile.
 * </p>
 *
 * <p>
 * Contrast with {@link GPURendererJOGLSprite}, which renders points only from
 * a growable buffer via {@code glDrawArrays} -- no indexing, no fixed upper
 * bound.
 * </p>
 *
 * @author Juergen Bernard (with AI assistance)
 * @version 1.01
 * @since 2026
 */
public class GPURendererJOGLIndexed {

	// ==================== PRIMITIVE TYPES ====================

	public enum PrimitiveType {
		POINTS(GL3.GL_POINTS), LINES(GL3.GL_LINES), TRIANGLES(GL3.GL_TRIANGLES), LINE_STRIP(GL3.GL_LINE_STRIP);

		final int glType;

		PrimitiveType(int glType) {
			this.glType = glType;
		}
	}

	// ==================== CONFIGURATION ====================

	private static final int MAX_VERTICES = 10_000_000;
	private static final int MAX_INDICES = 30_000_000;

	private static final int POSITION_SIZE = 2;
	private static final int COLOR_SIZE = 4;
	private static final int VERTEX_SIZE = POSITION_SIZE + COLOR_SIZE;

	// ==================== OPENGL RESOURCES ====================

	private int vaoId;
	private int vboId;
	private int iboId;
	private ShaderProgramIndexed shaderProgram;

	private boolean initialized = false;
	private GL3 gl; // Cached GL context

	// ==================== CPU-SIDE BUFFERS ====================

	private FloatBuffer vertexBuffer;
	private IntBuffer indexBuffer;

	private int vertexCount;
	private int indexCount;

	// ==================== BATCH STATE ====================

	private final List<DrawCommand> drawCommands = new ArrayList<DrawCommand>();
	private DrawCommand currentCommand;

	// ==================== COORDINATE TRANSFORMATION ====================

	private float worldMinX, worldMaxX;
	private float worldMinY, worldMaxY;

	//private int viewportX, viewportY;
	private int viewportWidth, viewportHeight;

	private float[] projectionMatrix;

	// ==================== STATISTICS ====================

	private int frameDrawCalls;
	private int frameTotalVertices;

	// ==================== INITIALIZATION ====================

	public GPURendererJOGLIndexed() {
		// Allocate CPU-side buffers
		vertexBuffer = Buffers.newDirectFloatBuffer(MAX_VERTICES * VERTEX_SIZE);
		indexBuffer = Buffers.newDirectIntBuffer(MAX_INDICES);
	}

	/**
	 * Initialize OpenGL resources. Called from GLCanvas.init()
	 */
	/**
	 * Initialize OpenGL resources. Called from GLCanvas.init()
	 */
	public void init(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();


		// Set bright blue background for testing
		gl.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);

		// Create VAO
		int[] vaos = new int[1];
		gl.glGenVertexArrays(1, vaos, 0);
		vaoId = vaos[0];
		gl.glBindVertexArray(vaoId);

		// Create VBO
		int[] vbos = new int[1];
		gl.glGenBuffers(1, vbos, 0);
		vboId = vbos[0];
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, (long) MAX_VERTICES * VERTEX_SIZE * Float.BYTES, null,
				GL3.GL_DYNAMIC_DRAW);

		// Position attribute (location = 0)
		gl.glVertexAttribPointer(0, POSITION_SIZE, GL3.GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 0);
		gl.glEnableVertexAttribArray(0);

		// Color attribute (location = 1)
		gl.glVertexAttribPointer(1, COLOR_SIZE, GL3.GL_FLOAT, false, VERTEX_SIZE * Float.BYTES,
				POSITION_SIZE * Float.BYTES);
		gl.glEnableVertexAttribArray(1);

		// Create IBO
		int[] ibos = new int[1];
		gl.glGenBuffers(1, ibos, 0);
		iboId = ibos[0];
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
		gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, (long) MAX_INDICES * Integer.BYTES, null, GL3.GL_DYNAMIC_DRAW);

		// Unbind VAO
		gl.glBindVertexArray(0);

		// Create shader AFTER VAO setup
		shaderProgram = new ShaderProgramIndexed(gl);

		// Enable blending for transparency
		gl.glEnable(GL3.GL_BLEND);
		gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

		// Enable point size in shader
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);

		// Disable depth test for 2D rendering
		gl.glDisable(GL3.GL_DEPTH_TEST);

		initialized = true;

		int error = gl.glGetError();
		if (error != GL3.GL_NO_ERROR) {
			System.err.println("ERROR during init: " + error);
			throw new RuntimeException("OpenGL initialization error: " + error);
		}

	}

	// ==================== FRAME LIFECYCLE ====================

	/**
	 * Must be called at the start of every display() callback with the current
	 * drawable. GLJPanel may rebuild its backing surface between frames, making any
	 * GL3 reference captured during init() stale. Refreshing here keeps the cached
	 * GL context and the shader's GL context in sync with the live drawable.
	 */
	public void beginFrame(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();
		if (shaderProgram != null) {
			shaderProgram.setGL(this.gl);
		}
	}

	// ==================== PROJECTION MANAGEMENT ====================

	public void updateProjectionMatrix(int viewportWidth, int viewportHeight,
			double worldMinX, double worldMaxX, double worldMinY, double worldMaxY) {

		// Store viewport SIZE only (offset is handled by Swing panel positioning)
		//this.viewportX = 0; // Always 0 in GL coordinates
		//this.viewportY = 0; // Always 0 in GL coordinates
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;

		this.worldMinX = (float) worldMinX;
		this.worldMaxX = (float) worldMaxX;
		this.worldMinY = (float) worldMinY;
		this.worldMaxY = (float) worldMaxY;

		computeProjectionMatrix();

//		System.out.println("Updated projection: viewport=" + viewportWidth + "x" + viewportHeight + ", world=["
//				+ worldMinX + "," + worldMaxX + "] x [" + worldMinY + "," + worldMaxY + "]");
	}

	private void computeProjectionMatrix() {
		projectionMatrix = new float[16];

		// Initialize to identity
		for (int i = 0; i < 16; i++) {
			projectionMatrix[i] = 0.0f;
		}
		projectionMatrix[15] = 1.0f;

		float worldWidth = worldMaxX - worldMinX;
		float worldHeight = worldMaxY - worldMinY;

		if (worldWidth == 0)
			worldWidth = 1;
		if (worldHeight == 0)
			worldHeight = 1;

		// Scale: how much to scale world units to NDC units
		float scaleX = 2.0f / worldWidth;
		float scaleY = 2.0f / worldHeight;

		// Translation: where does world origin map to in NDC
		float translateX = -1.0f - worldMinX * scaleX;
		float translateY = -1.0f - worldMinY * scaleY;

		// Orthographic projection matrix (column-major for OpenGL)
		projectionMatrix[0] = scaleX; // Scale X
		projectionMatrix[5] = scaleY; // Scale Y - POSITIVE (no flip!)
		projectionMatrix[10] = -1.0f; // Scale Z

		projectionMatrix[12] = translateX; // Translate X
		projectionMatrix[13] = translateY; // Translate Y - POSITIVE (no flip!)
		projectionMatrix[14] = 0.0f; // Translate Z
		projectionMatrix[15] = 1.0f; // W component

//		System.out.println("Projection matrix computed:");
//		System.out.println("  Scale: X=" + scaleX + ", Y=" + scaleY + " (applied as -" + scaleY + ")");
//		System.out.println("  Translate: X=" + translateX + ", Y=" + translateY + " (applied as -" + translateY + ")");
//		System.out.println("  World: [" + worldMinX + ", " + worldMaxX + "] x [" + worldMinY + ", " + worldMaxY + "]");
	}

	// ==================== GEOMETRY BATCHING ====================

	public void addCircle(float worldX, float worldY, float radius, Color color, boolean fill) {
		int segments = Math.max(8, Math.min(64, (int) (radius * 2)));
		addCircle(worldX, worldY, radius, color, fill, segments);
	}

	public void addCircle(float worldX, float worldY, float radius, Color color, boolean fill, int segments) {
		ensureCapacity(segments + 2, segments * 3);

		if (fill) {
			int centerIdx = vertexCount;
			addVertex(worldX, worldY, color);

			for (int i = 0; i <= segments; i++) {
				float angle = (float) (2.0 * Math.PI * i / segments);
				float px = worldX + radius * (float) Math.cos(angle);
				float py = worldY + radius * (float) Math.sin(angle);
				addVertex(px, py, color);

				if (i > 0) {
					addIndex(centerIdx);
					addIndex(centerIdx + i);
					addIndex(centerIdx + i + 1);
				}
			}

			flushIfNeeded(PrimitiveType.TRIANGLES, radius, 1.0f);
		} else {
			int firstIdx = vertexCount;
			for (int i = 0; i <= segments; i++) {
				float angle = (float) (2.0 * Math.PI * i / segments);
				float px = worldX + radius * (float) Math.cos(angle);
				float py = worldY + radius * (float) Math.sin(angle);
				addVertex(px, py, color);

				if (i > 0) {
					addIndex(firstIdx + i - 1);
					addIndex(firstIdx + i);
				}
			}

			flushIfNeeded(PrimitiveType.LINES, 1.0f, 1.0f);
		}
	}

	public void addPoint(float worldX, float worldY, float radius, Color color, boolean fill) {
		// System.out.println("Adding point: world=(" + worldX + "," + worldY + "),
		// size=" + radius + ", color=" + color);

		addCircle(worldX, worldY, radius, color, fill);
	}

	/**
	 * Add a single point using GL_POINTS (much faster than tessellated circles)
	 */
	public void addPointSprite(float worldX, float worldY, float size, Color color) {
		// System.out.println("Adding point: world=(" + worldX + "," + worldY + "),
		// size=" + size + ", color=" + color);

		ensureCapacity(1, 1);

		addVertex(worldX, worldY, color);
		addIndex(vertexCount - 1);

		flushIfNeeded(PrimitiveType.POINTS, size, 1.0f);
	}

	public void addLine(float worldX1, float worldY1, float worldX2, float worldY2, float width, Color color) {
		ensureCapacity(2, 2);

		int idx1 = vertexCount;
		addVertex(worldX1, worldY1, color);
		addVertex(worldX2, worldY2, color);

		addIndex(idx1);
		addIndex(idx1 + 1);

		flushIfNeeded(PrimitiveType.LINES, 1.0f, width);
	}

	public void addRectangle(float worldX, float worldY, float width, float height, Color color, boolean fill) {
		ensureCapacity(4, 6);

		int v = vertexCount;
		addVertex(worldX, worldY, color);
		addVertex(worldX + width, worldY, color);
		addVertex(worldX + width, worldY + height, color);
		addVertex(worldX, worldY + height, color);

		if (fill) {
			addIndex(v);
			addIndex(v + 1);
			addIndex(v + 2);
			addIndex(v);
			addIndex(v + 2);
			addIndex(v + 3);
			flushIfNeeded(PrimitiveType.TRIANGLES, 1.0f, 1.0f);
		} else {
			addIndex(v);
			addIndex(v + 1);
			addIndex(v + 1);
			addIndex(v + 2);
			addIndex(v + 2);
			addIndex(v + 3);
			addIndex(v + 3);
			addIndex(v);
			flushIfNeeded(PrimitiveType.LINES, 1.0f, 1.0f);
		}
	}

	public void addPath(float[] worldXPoints, float[] worldYPoints, Color color, boolean closed, boolean fill,
			float lineWidth) {
		int n = Math.min(worldXPoints.length, worldYPoints.length);
		if (n < 2)
			return;

		if (fill && n >= 3) {
			ensureCapacity(n, (n - 2) * 3);
			int firstIdx = vertexCount;
			for (int i = 0; i < n; i++) {
				addVertex(worldXPoints[i], worldYPoints[i], color);
			}
			for (int i = 1; i < n - 1; i++) {
				addIndex(firstIdx);
				addIndex(firstIdx + i);
				addIndex(firstIdx + i + 1);
			}
			flushIfNeeded(PrimitiveType.TRIANGLES, 1.0f, 1.0f);
		} else {
			int lineCount = closed ? n : n - 1;
			ensureCapacity(n, lineCount * 2);

			int firstIdx = vertexCount;
			for (int i = 0; i < n; i++) {
				addVertex(worldXPoints[i], worldYPoints[i], color);
			}
			for (int i = 0; i < n - 1; i++) {
				addIndex(firstIdx + i);
				addIndex(firstIdx + i + 1);
			}
			if (closed) {
				addIndex(firstIdx + n - 1);
				addIndex(firstIdx);
			}
			flushIfNeeded(PrimitiveType.LINES, 1.0f, lineWidth);
		}
	}

	// ==================== LOW-LEVEL OPERATIONS ====================

	private void addVertex(float worldX, float worldY, Color color) {
		vertexBuffer.put(worldX);
		vertexBuffer.put(worldY);
		vertexBuffer.put(color.getRed() / 255.0f);
		vertexBuffer.put(color.getGreen() / 255.0f);
		vertexBuffer.put(color.getBlue() / 255.0f);
		vertexBuffer.put(color.getAlpha() / 255.0f);
		vertexCount++;
	}

	private void addIndex(int index) {
		indexBuffer.put(index);
		indexCount++;
	}

	private void ensureCapacity(int vertices, int indices) {
		if (vertexCount + vertices >= MAX_VERTICES || indexCount + indices >= MAX_INDICES) {
			render();
			clear();
		}
	}

	private void flushIfNeeded(PrimitiveType type, float pointSize, float lineWidth) {
		boolean needNewCommand = (currentCommand == null || currentCommand.getType() != type
				|| Math.abs(currentCommand.getPointSize() - pointSize) > 0.01f
				|| Math.abs(currentCommand.getLineWidth() - lineWidth) > 0.01f);

		if (needNewCommand) {
			int start = (currentCommand == null) ? 0
					: (currentCommand.getStartIndex() + currentCommand.getIndexCount());
			currentCommand = new DrawCommand(type, start, 0, pointSize, lineWidth);
			drawCommands.add(currentCommand);
		}

		currentCommand.setIndexCount(indexCount - currentCommand.getStartIndex());
	}

	// ==================== RENDERING ====================

	public void render() {
		render(5.0f); // Default point size
	}

	public void render(float pointSize) {
		if (!initialized || vertexCount == 0 || drawCommands.isEmpty())
			return;

		// Clear any stale errors accumulated by the GL framework before we start
		while (gl.glGetError() != GL3.GL_NO_ERROR) {
		}

		// Verify VAO is still valid in this context (guards against surface rebuilds)
		if (!gl.glIsVertexArray(vaoId)) {
			System.err.println("GPURenderer: VAO " + vaoId + " is no longer valid -- skipping render");
			return;
		}

		// Do NOT sort by state: submission order is intentional.
		// Selected halos must render before selected colored points.
		uploadToGPU();

		// Clear any errors from the VBO/IBO upload so shader error checks are clean
		while (gl.glGetError() != GL3.GL_NO_ERROR) {
		}

		// Re-establish GL state that GLJPanel may reset between frames
		gl.glEnable(GL3.GL_BLEND);
		gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		gl.glDisable(GL3.GL_DEPTH_TEST);

		// Bind VAO first
		gl.glBindVertexArray(vaoId);

		// Use shader
		shaderProgram.use();

		// Verify the program was actually bound before setting uniforms
		int[] curProg = new int[1];
		gl.glGetIntegerv(GL3.GL_CURRENT_PROGRAM, curProg, 0);
		if (curProg[0] == 0) {
			System.err.println("GPURenderer: glUseProgram failed -- no program active after use()");
			gl.glBindVertexArray(0);
			return;
		}

		shaderProgram.setUniformMatrix4fv("uProjection", projectionMatrix);

		gl.glViewport(0, 0, viewportWidth, viewportHeight);

		frameDrawCalls = 0;
		frameTotalVertices = 0;

		for (DrawCommand cmd : drawCommands) {
			// Update point size per command so halos (larger) and regular points render
			// at their correct sizes. Using the global pointSize as fallback for non-point
			// primitives that don't carry a meaningful per-command size.
			float cmdPointSize = (cmd.getType() == PrimitiveType.POINTS) ? cmd.getPointSize() : pointSize;
			shaderProgram.setUniform1f("uPointSize", cmdPointSize);

			gl.glDrawElements(cmd.getType().glType, cmd.getIndexCount(), GL3.GL_UNSIGNED_INT,
					(long) cmd.getStartIndex() * Integer.BYTES);

			frameDrawCalls++;
			frameTotalVertices += cmd.getIndexCount();
		}

		int drawErr = gl.glGetError();
		if (drawErr != GL3.GL_NO_ERROR) {
			System.err.println("GPURenderer.render: glDrawElements error " + drawErr);
		}

		shaderProgram.unbind();
		gl.glBindVertexArray(0);
	}

	private void sortByState() {
		drawCommands.sort(Comparator.comparingInt(DrawCommand::getStateHash));
	}

	private void uploadToGPU() {
		gl.glBindVertexArray(vaoId);

		vertexBuffer.flip();
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);
		gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, 0, vertexBuffer.limit() * Float.BYTES, vertexBuffer);

		indexBuffer.flip();
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
		gl.glBufferSubData(GL3.GL_ELEMENT_ARRAY_BUFFER, 0, indexBuffer.limit() * Integer.BYTES, indexBuffer);
	}

	public void clear() {
		vertexBuffer.clear();
		indexBuffer.clear();
		vertexCount = 0;
		indexCount = 0;
		drawCommands.clear();
		currentCommand = null;
	}

	// ==================== STATISTICS ====================

	public int getDrawCallCount() {
		return frameDrawCalls;
	}

	public int getTotalVerticesRendered() {
		return frameTotalVertices;
	}

	public int getCurrentBatchSize() {
		return vertexCount;
	}

	public boolean isInitialized() {
		return initialized;
	}

	// ==================== DEBUG METHODS ====================

	/**
	 * Get vertex buffer for debugging. Returns a read-only view.
	 */
	public FloatBuffer getVertexBufferForDebug() {
		if (vertexBuffer == null) {
			return null;
		}
		// Return a duplicate so we don't mess up the original position
		FloatBuffer copy = vertexBuffer.duplicate();
		copy.flip();
		return copy;
	}

	/**
	 * Print debug info about current batch
	 */
	public void debugPrintBatch() {
		System.out.println("=== GPU Renderer Debug ===");
		System.out.println("  Vertex count: " + vertexCount);
		System.out.println("  Index count: " + indexCount);
		System.out.println("  Draw commands: " + drawCommands.size());

		if (vertexCount > 0 && vertexBuffer != null) {
			FloatBuffer vb = vertexBuffer.duplicate();
			vb.flip();

			System.out.println("  First vertex:");
			if (vb.remaining() >= VERTEX_SIZE) {
				System.out.println("    Position: (" + vb.get(0) + ", " + vb.get(1) + ")");
				System.out.println(
						"    Color: (" + vb.get(2) + ", " + vb.get(3) + ", " + vb.get(4) + ", " + vb.get(5) + ")");
			}
		}

		System.out.println("==========================");
	}

	// ==================== CLEANUP ====================

	/**
	 * Frees GL resources using a live drawable's GL context, rather than a
	 * cached reference -- must only be called from a context guaranteed to be
	 * current (e.g. JOGL's own {@code GLEventListener.dispose(GLAutoDrawable)}
	 * callback), never from ordinary application cleanup code.
	 */
	public void dispose(GLAutoDrawable drawable) {
		if (initialized) {
			GL3 gl = drawable.getGL().getGL3();
			gl.glDeleteBuffers(1, new int[] { vboId }, 0);
			gl.glDeleteBuffers(1, new int[] { iboId }, 0);
			gl.glDeleteVertexArrays(1, new int[] { vaoId }, 0);
			shaderProgram.dispose();
			initialized = false;
		}
	}

}