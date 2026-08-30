package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

/**
 * Test configuration for different data set sizes
 */
class TestConfig {
	final int pointCount;
	final String description;
	final boolean testCPU;
	final boolean testGPU;
	final int warmupFrames;
	final int measurementFrames;

	TestConfig(int pointCount, String description, boolean testCPU, boolean testGPU) {
		this.pointCount = pointCount;
		this.description = description;
		this.testCPU = testCPU;
		this.testGPU = testGPU;
		this.warmupFrames = 30;
		this.measurementFrames = 120;
	}

	@Override
	public String toString() {
		return String.format("%s (%,d points)", description, pointCount);
	}
}