package com.github.TKnudsen.infoVis.view.gpu;

import java.util.Objects;

import com.github.TKnudsen.infoVis.view.gpu.GPURendererJOGLWorking.PrimitiveType;

/**
 * <p>
 * Immutable descriptor of a single GPU draw call (primitive type, index
 * range, point/line size, and a state hash for change detection), used for
 * batching draw calls in the GPU rendering pipeline.
 * </p>
 *
 * @version 1.0
 */
public class DrawCommand {
	private final PrimitiveType type;
	private final int startIndex;
	private int indexCount;
	private final float pointSize;
	private final float lineWidth;
	private final int stateHash;

	DrawCommand(PrimitiveType type, int startIndex, int indexCount, float pointSize, float lineWidth) {
		this.type = type;
		this.startIndex = startIndex;
		this.setIndexCount(indexCount);
		this.pointSize = pointSize;
		this.lineWidth = lineWidth;
		this.stateHash = Objects.hash(type, Float.floatToIntBits(pointSize), Float.floatToIntBits(lineWidth));
	}

	public PrimitiveType getType() {
		return type;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getIndexCount() {
		return indexCount;
	}

	public void setIndexCount(int indexCount) {
		this.indexCount = indexCount;
	}

	public float getPointSize() {
		return pointSize;
	}

	public float getLineWidth() {
		return lineWidth;
	}

	public int getStateHash() {
		return stateHash;
	}
}