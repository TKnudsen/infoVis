package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * <p>
 * Viewing conditions are modeled after sRGB's "typical" viewing environment
 * with 200 cd/m2. Consecutive colors have a delta E distance of at least 1.
 * Delta E distance is defined in CAM02-UCS as published in "Uniform Colour
 * Spaces Based on CIECAM02 Colour Appearance Model" (Luo et al.)
 * </p>
 *
 */
public final class CieCamColors {

	/**
	 * Luminance (in CIE-Lch) is 65 for all color tones, Chroma is at 65. The
	 * entire hue circle is sampled (non-linearly). The color plane is
	 * transformed with some clipping (mostly blue and red) into RGB.
	 */
	public static final List<Color> L65C65 = Arrays.asList(new Color(0xFF77AE), new Color(0xFF77AB), new Color(0xFF77A8), new Color(0xFF77A5), new Color(0xFF78A2), new Color(0xFF789F), new Color(0xFF789B), new Color(0xFF7898), new Color(0xFF7895), new Color(0xFF7992), new Color(0xFF798F), new Color(0xFF798C), new Color(0xFF7989), new Color(0xFF7A86), new Color(0xFF7A82), new Color(0xFF7B7F), new Color(0xFF7B7C), new Color(0xFF7B79), new Color(0xFF7C76), new Color(0xFF7D73), new Color(0xFF7D6F), new Color(0xFF7E6C), new Color(0xFF7E69), new Color(0xFF7F66), new Color(0xFF8062), new Color(0xFF815F), new Color(0xFF815C), new Color(0xFF8259),
			new Color(0xFF8356), new Color(0xFF8453), new Color(0xFF8550), new Color(0xFF864D), new Color(0xFF874A), new Color(0xFF8846), new Color(0xFF8943), new Color(0xFF8A40), new Color(0xFF8B3D), new Color(0xFF8C3A), new Color(0xFF8D37), new Color(0xFF8E34), new Color(0xFF8F31), new Color(0xFF902E), new Color(0xFF912B), new Color(0xFF9228), new Color(0xFE9425), new Color(0xFD9522), new Color(0xFB961E), new Color(0xFA971B), new Color(0xF89817), new Color(0xF79A13), new Color(0xF59B0F), new Color(0xF39C0A), new Color(0xF29D05), new Color(0xF09F01), new Color(0xEEA000), new Color(0xECA100), new Color(0xEAA200), new Color(0xE8A300), new Color(0xE6A500),
			new Color(0xE4A600), new Color(0xE2A700), new Color(0xE0A800), new Color(0xDDAA00), new Color(0xDBAB00), new Color(0xD9AC00), new Color(0xD6AD00), new Color(0xD4AF00), new Color(0xD2B000), new Color(0xCFB100), new Color(0xCCB200), new Color(0xCAB300), new Color(0xC7B500), new Color(0xC4B600), new Color(0xC2B700), new Color(0xBFB804), new Color(0xBCB90A), new Color(0xB9BA0F), new Color(0xB6BB13), new Color(0xB3BC17), new Color(0xB0BE1B), new Color(0xADBF1F), new Color(0xA9C022), new Color(0xA6C125), new Color(0xA3C229), new Color(0x9FC32C), new Color(0x9CC42F), new Color(0x98C532), new Color(0x94C535), new Color(0x91C638), new Color(0x8DC73B),
			new Color(0x89C83F), new Color(0x85C942), new Color(0x81CA45), new Color(0x7DCB48), new Color(0x78CB4B), new Color(0x74CC4E), new Color(0x6FCD51), new Color(0x6BCE54), new Color(0x66CE57), new Color(0x60CF5A), new Color(0x5BD05D), new Color(0x55D060), new Color(0x4FD163), new Color(0x49D266), new Color(0x41D269), new Color(0x39D36C), new Color(0x30D36F), new Color(0x25D472), new Color(0x15D475), new Color(0x00D578), new Color(0x00D57B), new Color(0x00D57E), new Color(0x00D681), new Color(0x00D684), new Color(0x00D787), new Color(0x00D78A), new Color(0x00D78D), new Color(0x00D790), new Color(0x00D893), new Color(0x00D896), new Color(0x00D899),
			new Color(0x00D89C), new Color(0x00D89F), new Color(0x00D8A2), new Color(0x00D8A5), new Color(0x00D9A8), new Color(0x00D9AB), new Color(0x00D9AE), new Color(0x00D9B1), new Color(0x00D8B4), new Color(0x00D8B7), new Color(0x00D8BA), new Color(0x00D8BD), new Color(0x00D8C0), new Color(0x00D8C3), new Color(0x00D8C6), new Color(0x00D7C9), new Color(0x00D7CB), new Color(0x00D7CE), new Color(0x00D7D1), new Color(0x00D6D4), new Color(0x00D6D7), new Color(0x00D5DA), new Color(0x00D5DD), new Color(0x00D5DF), new Color(0x00D4E2), new Color(0x00D4E5), new Color(0x00D3E8), new Color(0x00D2EB), new Color(0x00D2ED), new Color(0x00D1F0), new Color(0x00D0F3),
			new Color(0x00D0F5), new Color(0x00CFF8), new Color(0x00CEFB), new Color(0x00CDFD), new Color(0x00CDFF), new Color(0x00CCFF), new Color(0x00CBFF), new Color(0x00CAFF), new Color(0x00C9FF), new Color(0x00C8FF), new Color(0x00C7FF), new Color(0x12C6FF), new Color(0x25C5FF), new Color(0x32C3FF), new Color(0x3CC2FF), new Color(0x45C1FF), new Color(0x4DC0FF), new Color(0x55BFFF), new Color(0x5BBDFF), new Color(0x62BCFF), new Color(0x68BBFF), new Color(0x6EB9FF), new Color(0x73B8FF), new Color(0x78B6FF), new Color(0x7DB5FF), new Color(0x82B4FF), new Color(0x87B2FF), new Color(0x8BB1FF), new Color(0x90AFFF), new Color(0x94AEFF), new Color(0x98ACFF),
			new Color(0x9CABFF), new Color(0xA0A9FF), new Color(0xA4A8FF), new Color(0xA8A6FF), new Color(0xACA5FF), new Color(0xAFA4FF), new Color(0xB2A2FF), new Color(0xB6A1FF), new Color(0xB99FFF), new Color(0xBC9EFF), new Color(0xBF9DFF), new Color(0xC29BFF), new Color(0xC59AFF), new Color(0xC898FF), new Color(0xCB97FF), new Color(0xCD96FF), new Color(0xD094FF), new Color(0xD393FF), new Color(0xD592FF), new Color(0xD791FF), new Color(0xDA90FF), new Color(0xDC8EFF), new Color(0xDE8DFF), new Color(0xE18CFF), new Color(0xE38BFF), new Color(0xE58AFF), new Color(0xE689FF), new Color(0xE888FF), new Color(0xEA87FF), new Color(0xEC86FF), new Color(0xEE85FF),
			new Color(0xF084FF), new Color(0xF183FF), new Color(0xF383FF), new Color(0xF582FC), new Color(0xF681FA), new Color(0xF880F7), new Color(0xF980F5), new Color(0xFA7FF2), new Color(0xFC7EEF), new Color(0xFD7EED), new Color(0xFE7DEA), new Color(0xFF7DE7), new Color(0xFF7CE4), new Color(0xFF7BE1), new Color(0xFF7BDE), new Color(0xFF7BDB), new Color(0xFF7AD8), new Color(0xFF7AD5), new Color(0xFF79D2), new Color(0xFF79CF), new Color(0xFF79CC), new Color(0xFF78C9), new Color(0xFF78C6), new Color(0xFF78C2), new Color(0xFF78BF), new Color(0xFF78BC), new Color(0xFF78B9), new Color(0xFF77B5));

	private CieCamColors() {
	}
}