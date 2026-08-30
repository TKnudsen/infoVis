package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces.converter;

import java.awt.Color;

/**
 * @version 1.01
 * @since 2012-01-05
 */
public class ColorConverter {

	/**
	 * RGB to YCbCr
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int[] rgb2ycbcr(int r, int g, int b) {
		int y  = (int)( 0.299   * r + 0.587   * g + 0.114   * b);
		int cb = (int)(-0.16874 * r - 0.33126 * g + 0.50000 * b);
		int cr = (int)( 0.50000 * r - 0.41869 * g - 0.08131 * b);
		
		int[] ycbcr = new int[3];
		ycbcr[0] = y;
		ycbcr[1] = cb;
		ycbcr[2] = cr;
		
		return ycbcr;
	}
	
	/**
	 * RGB to YUV
	 * @param r
	 * @param g
	 * @param b
	 */
	public static int[] rgb2yuv(int r,int g, int b) {
		int y = (int)(0.299 * r + 0.587 * g + 0.114 * b);
		int u = (int)((b - y) * 0.492f); 
		int v = (int)((r - y) * 0.877f);
	
		int[] yuv = new int[3];
		yuv[0]= y;
		yuv[1]= u;
		yuv[2]= v;
		
		return yuv;
	}
	
	/**
	 * RGB to HSB
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static float[] rgb2hsb(int r, int g, int b) {
		float [] hsbvals = new float[3]; 
		Color.RGBtoHSB(r, g, b, hsbvals);
		return hsbvals;
	}
	
	/**
	 * RGB to HMMD
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int[] rgb2hmmd(int r, int g, int b) {
		
		float max = (int)Math.max(Math.max(r,g), Math.max(g,b));
		float min = (int)Math.min(Math.min(r,g), Math.min(g,b));
		float diff = (max - min);
//		float sum = (float) ((max + min)/2.);
	
		float hue = 0;
		if (diff == 0)
			hue = 0;
		else if (r == max && (g - b) > 0)
			hue = 60*(g-b)/(max-min);
		else if (r == max && (g - b) <= 0)
			hue = 60*(g-b)/(max-min) + 360;
		else if (g == max)
			hue = (float) (60*(2.+(b-r)/(max-min)));
		else if (b == max)
			hue = (float) (60*(4.+(r-g)/(max-min)));
						 
		int[] hmmd = new int[3];
		hmmd[0] = (int)(hue);
		hmmd[1] = (int)(max);
		hmmd[2] = (int)(min);
		hmmd[3] = (int)(diff);
		return hmmd;
	}
	
	/**
	 * RGB to HSL
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int[] rgb2hsl(int r, int g, int b) {
		
		float var_R = ( r / 255f );                    
		float var_G = ( g / 255f );
		float var_B = ( b / 255f );
		
		float var_Min;    //Min. value of RGB
		float var_Max;    //Max. value of RGB
		float del_Max;    //Delta RGB value
						 
		if (var_R > var_G) 
			{ var_Min = var_G; var_Max = var_R; }
		else 
			{ var_Min = var_R; var_Max = var_G; }

		if (var_B > var_Max) var_Max = var_B;
		if (var_B < var_Min) var_Min = var_B;

		del_Max = var_Max - var_Min; 
								 
		float H = 0, S, L;
		L = ( var_Max + var_Min ) / 2f;
	
		if ( del_Max == 0 ) { H = 0; S = 0; } // gray
		else {                                //Chroma
			if ( L < 0.5 ) 
				S = del_Max / ( var_Max + var_Min );
			else           
				S = del_Max / ( 2 - var_Max - var_Min );
	
			float del_R = ( ( ( var_Max - var_R ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
			float del_G = ( ( ( var_Max - var_G ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
			float del_B = ( ( ( var_Max - var_B ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
	
			if ( var_R == var_Max ) 
				H = del_B - del_G;
			else if ( var_G == var_Max ) 
				H = ( 1 / 3f ) + del_R - del_B;
			else if ( var_B == var_Max ) 
				H = ( 2 / 3f ) + del_G - del_R;
			if ( H < 0 ) H += 1;
			if ( H > 1 ) H -= 1;
		}
		
		int[] hsl = new int[3];
		hsl[0] = (int)(360*H);
		hsl[1] = (int)(S*100);
		hsl[2] = (int)(L*100);
		return hsl;
	}
	
	/**
	 * RGB to HSV
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int[] rgb2hsv(int r, int g, int b) {
		
		int min;    //Min. value of RGB
		int max;    //Max. value of RGB
		int delMax; //Delta RGB value
		
		if (r > g) { min = g; max = r; }
		else { min = r; max = g; }
		if (b > max) max = b;
		if (b < min) min = b;
								
		delMax = max - min;
	 
		float H = 0, S;
		float V = max;
		   
		if ( delMax == 0 ) { H = 0; S = 0; }
		else {                                   
			S = delMax/255f;
			if ( r == max ) 
				H = (      (g - b)/(float)delMax)*60;
			else if ( g == max ) 
				H = ( 2 +  (b - r)/(float)delMax)*60;
			else if ( b == max ) 
				H = ( 4 +  (r - g)/(float)delMax)*60;   
		}
								 
		int[] hsv = new int[3];
		hsv[0] = (int)(H);
		hsv[1] = (int)(S*100);
		hsv[2] = (int)(V*100);
		return hsv;
	}
	
	/**
	 * RGB to xyY
	 * @param R
	 * @param G
	 * @param B
	 * @return
	 */
	public static int[] rgb2xyY(int R, int G, int B) {
		//http://www.brucelindbloom.com
		
		float rf, gf, bf;
		float r, g, b, X, Y, Z;
		  
		// RGB to XYZ
		r = R/255.f; //R 0..1
		g = G/255.f; //G 0..1
		b = B/255.f; //B 0..1
		 
		if (r <= 0.04045)
			r = r/12;
		else
			r = (float) Math.pow((r+0.055)/1.055,2.4);
			  
		if (g <= 0.04045)
			g = g/12;
		else
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		
		if (b <= 0.04045)
			b = b/12;
		else
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		  
		X =  0.436052025f*r  + 0.385081593f*g  + 0.143087414f *b;
		Y =  0.222491598f*r  + 0.71688606f *g  + 0.060621486f *b;
		Z =  0.013929122f*r  + 0.097097002f*g  + 0.71418547f  *b;
		 
		float x;
		float y;
					  
		float sum = X + Y + Z;
		if (sum != 0) {
		x = X / sum;
		y = Y / sum;
		}
		else {
			float Xr = 0.964221f;  // reference white
			float Yr = 1.0f;
			float Zr = 0.825211f;
								 
		x = Xr / (Xr + Yr + Zr);
		y = Yr / (Xr + Yr + Zr);
		}
		   
		int[] xyy = new int[3];
		xyy[0] = (int) (255*x + .5);
		xyy[1] = (int) (255*y + .5);
		xyy[2] = (int) (255*Y + .5);
		return xyy;		 
	}
	
	/**
	 * RGB to XYZ
	 * @param R
	 * @param G
	 * @param B
	 * @return
	 */
	public static int[] rgb2xyz(int R, int G, int B) {
		float rf, gf, bf;
		float r, g, b, X, Y, Z;
								 
		r = R/255.f; //R 0..1
		g = G/255.f; //G 0..1
		b = B/255.f; //B 0..1
		 
		if (r <= 0.04045)
			r = r/12;
		else
			r = (float) Math.pow((r+0.055)/1.055,2.4);
		
		if (g <= 0.04045)
			g = g/12;
		else
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		
		if (b <= 0.04045)
			b = b/12;
		else
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		 
		X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
							 
		int[] xyz = new int[3];
		xyz[1] = (int) (255*Y + .5);
		xyz[0] = (int) (255*X + .5); 
		xyz[2] = (int) (255*Z + .5);
		return xyz;    
	}
	
	/**
	 * RGB to LAB
	 * @param R
	 * @param G
	 * @param B
	 * @return
	 */
	public static int[] rgb2lab(int R, int G, int B) {
		//http://www.brucelindbloom.com
		  
		float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
		float Ls, as, bs;
		float eps = 216.f/24389.f;
		float k = 24389.f/27.f;
		   
		float Xr = 0.964221f;  // reference white D50
		float Yr = 1.0f;
		float Zr = 0.825211f;
		
		// RGB to XYZ
		r = R/255.f; //R 0..1
		g = G/255.f; //G 0..1
		b = B/255.f; //B 0..1
		
		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r/12;
		else
			r = (float) Math.pow((r+0.055)/1.055,2.4);
		
		if (g <= 0.04045)
			g = g/12;
		else
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		
		if (b <= 0.04045)
			b = b/12;
		else
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		
		
		X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
		
		// XYZ to Lab
		xr = X/Xr;
		yr = Y/Yr;
		zr = Z/Zr;
				
		if ( xr > eps )
			fx =  (float) Math.pow(xr, 1/3.);
		else
			fx = (float) ((k * xr + 16.) / 116.);
		 
		if ( yr > eps )
			fy =  (float) Math.pow(yr, 1/3.);
		else
		fy = (float) ((k * yr + 16.) / 116.);
		
		if ( zr > eps )
			fz =  (float) Math.pow(zr, 1/3.);
		else
			fz = (float) ((k * zr + 16.) / 116);
		
		Ls = ( 116 * fy ) - 16;
		as = 500*(fx-fy);
		bs = 200*(fy-fz);
		
		int[] lab = new int[3];
		lab[0] = (int) (2.55*Ls + .5);
		lab[1] = (int) (as + .5); 
		lab[2] = (int) (bs + .5);
		return lab;   
	} 
	
	/**
	 * RGB to LUV
	 * @param R
	 * @param G
	 * @param B
	 * @return
	 */
	public static int[] rgb2luv(int R, int G, int B) {
		//http://www.brucelindbloom.com
		
		float rf, gf, bf;
		float r, g, b, X_, Y_, Z_, X, Y, Z, fx, fy, fz, xr, yr, zr;
		float L;
		float eps = 216.f/24389.f;
		float k = 24389.f/27.f;
		 
		float Xr = 0.964221f;  // reference white D50
		float Yr = 1.0f;
		float Zr = 0.825211f;
		 
		// RGB to XYZ
		 
		r = R/255.f; //R 0..1
		g = G/255.f; //G 0..1
		b = B/255.f; //B 0..1
		   
		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r/12;
		else
			r = (float) Math.pow((r+0.055)/1.055,2.4);
		
		if (g <= 0.04045)
			g = g/12;
		else
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		 
		if (b <= 0.04045)
			b = b/12;
		else
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		
		
		X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
		
		// XYZ to Luv
		
		float u, v, u_, v_, ur_, vr_;
						
		u_ = 4*X / (X + 15*Y + 3*Z);
		v_ = 9*Y / (X + 15*Y + 3*Z);
				 
		ur_ = 4*Xr / (Xr + 15*Yr + 3*Zr);
		vr_ = 9*Yr / (Xr + 15*Yr + 3*Zr);
		  
		yr = Y/Yr;
		
		if ( yr > eps )
			L =  (float) (116*Math.pow(yr, 1/3.) - 16);
		else
			L = k * yr;
		 
		u = 13*L*(u_ -ur_);
		v = 13*L*(v_ -vr_);
		
		int[] luv = new int[3];
		luv[0] = (int) (2.55*L + .5);
		luv[1] = (int) (u + .5); 
		luv[2] = (int) (v + .5);
		return luv;        
	}	
}
