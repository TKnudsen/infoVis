package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.staticMaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

// Prototype class for the implemented colormaps

/**
 * @author Tobias Schreck
 * @version 1.0
 * @since 2005
 */
public class StaticHeatMapColormap
{

    private static final Color colors[] = {
    	new Color(255,255,0),
    	new Color(255,254,0),
    	new Color(255,253,0),
    	new Color(255,252,0),
    	new Color(255,251,0),
    	new Color(255,250,0),
    	new Color(255,249,0),
    	new Color(255,248,0),
    	new Color(255,247,0),
    	new Color(255,246,0),
    	new Color(255,245,0),
    	new Color(255,244,0),
    	new Color(255,243,0),
    	new Color(255,242,1),
    	new Color(255,241,1),
    	new Color(255,240,1),
    	new Color(255,239,1),
    	new Color(255,238,1),
    	new Color(255,237,1),
    	new Color(255,236,1),
    	new Color(255,235,1),
    	new Color(255,234,1),
    	new Color(255,233,1),
    	new Color(255,232,1),
    	new Color(255,231,1),
    	new Color(255,230,1),
    	new Color(255,229,1),
    	new Color(255,228,1),
    	new Color(255,227,1),
    	new Color(255,226,1),
    	new Color(255,225,1),
    	new Color(255,224,1),
    	new Color(255,223,1),
    	new Color(255,222,1),
    	new Color(255,221,1),
    	new Color(255,220,1),
    	new Color(255,219,1),
    	new Color(255,218,1),
    	new Color(255,217,2),
    	new Color(255,216,2),
    	new Color(255,215,2),
    	new Color(255,214,2),
    	new Color(255,213,2),
    	new Color(255,212,2),
    	new Color(255,211,2),
    	new Color(255,210,2),
    	new Color(255,209,2),
    	new Color(255,208,2),
    	new Color(255,207,2),
    	new Color(255,206,2),
    	new Color(255,205,2),
    	new Color(255,204,2),
    	new Color(255,203,2),
    	new Color(255,202,2),
    	new Color(255,201,2),
    	new Color(255,200,2),
    	new Color(255,199,2),
    	new Color(255,198,2),
    	new Color(255,197,2),
    	new Color(255,196,2),
    	new Color(255,195,2),
    	new Color(255,194,2),
    	new Color(255,193,2),
    	new Color(255,192,3),
    	new Color(255,191,3),
    	new Color(255,190,3),
    	new Color(255,189,3),
    	new Color(255,188,3),
    	new Color(255,187,3),
    	new Color(255,186,3),
    	new Color(255,185,3),
    	new Color(255,184,3),
    	new Color(255,183,3),
    	new Color(255,182,3),
    	new Color(255,181,3),
    	new Color(255,180,3),
    	new Color(255,179,3),
    	new Color(255,178,3),
    	new Color(255,177,3),
    	new Color(255,176,3),
    	new Color(255,175,3),
    	new Color(255,174,3),
    	new Color(255,173,3),
    	new Color(255,172,3),
    	new Color(255,171,3),
    	new Color(255,170,3),
    	new Color(255,169,3),
    	new Color(255,168,3),
    	new Color(255,167,4),
    	new Color(255,166,4),
    	new Color(255,165,4),
    	new Color(255,164,4),
    	new Color(255,163,4),
    	new Color(255,162,4),
    	new Color(255,161,4),
    	new Color(255,160,4),
    	new Color(255,159,4),
    	new Color(255,158,4),
    	new Color(255,157,4),
    	new Color(255,156,4),
    	new Color(255,155,4),
    	new Color(255,154,4),
    	new Color(255,153,4),
    	new Color(255,152,4),
    	new Color(255,151,4),
    	new Color(255,150,4),
    	new Color(255,149,4),
    	new Color(255,148,4),
    	new Color(255,147,4),
    	new Color(255,146,4),
    	new Color(255,145,4),
    	new Color(255,144,4),
    	new Color(255,143,4),
    	new Color(255,142,5),
    	new Color(255,141,5),
    	new Color(255,140,5),
    	new Color(255,139,5),
    	new Color(255,138,5),
    	new Color(255,137,5),
    	new Color(255,136,5),
    	new Color(255,135,5),
    	new Color(255,134,5),
    	new Color(255,133,5),
    	new Color(255,132,5),
    	new Color(255,131,5),
    	new Color(255,130,5),
    	new Color(255,129,5),
    	new Color(255,128,5),
    	new Color(255,127,5),
    	new Color(255,126,5),
    	new Color(255,125,5),
    	new Color(255,124,5),
    	new Color(255,123,5),
    	new Color(255,122,5),
    	new Color(255,121,5),
    	new Color(255,120,5),
    	new Color(255,119,5),
    	new Color(255,118,5),
    	new Color(255,117,6),
    	new Color(255,116,6),
    	new Color(255,115,6),
    	new Color(255,114,6),
    	new Color(255,113,6),
    	new Color(255,112,6),
    	new Color(255,111,6),
    	new Color(255,110,6),
    	new Color(255,109,6),
    	new Color(255,108,6),
    	new Color(255,107,6),
    	new Color(255,106,6),
    	new Color(255,105,6),
    	new Color(255,104,6),
    	new Color(255,103,6),
    	new Color(255,102,6),
    	new Color(255,101,6),
    	new Color(255,100,6),
    	new Color(255,99,6),
    	new Color(255,98,6),
    	new Color(255,97,6),
    	new Color(255,96,6),
    	new Color(255,95,6),
    	new Color(255,94,6),
    	new Color(255,93,6),
    	new Color(255,92,7),
    	new Color(255,91,7),
    	new Color(255,90,7),
    	new Color(255,89,7),
    	new Color(255,88,7),
    	new Color(255,87,7),
    	new Color(255,86,7),
    	new Color(255,85,7),
    	new Color(255,84,7),
    	new Color(255,83,7),
    	new Color(255,82,7),
    	new Color(255,81,7),
    	new Color(255,80,7),
    	new Color(255,79,7),
    	new Color(255,78,7),
    	new Color(255,77,7),
    	new Color(255,76,7),
    	new Color(255,75,7),
    	new Color(255,74,7),
    	new Color(255,73,7),
    	new Color(255,72,7),
    	new Color(255,71,7),
    	new Color(255,70,7),
    	new Color(255,69,7),
    	new Color(255,68,7),
    	new Color(255,67,8),
    	new Color(255,66,8),
    	new Color(255,65,8),
    	new Color(255,64,8),
    	new Color(255,63,8),
    	new Color(255,62,8),
    	new Color(255,61,8),
    	new Color(255,60,8),
    	new Color(255,59,8),
    	new Color(255,58,8),
    	new Color(255,57,8),
    	new Color(255,56,8),
    	new Color(255,55,8),
    	new Color(255,54,8),
    	new Color(255,53,8),
    	new Color(255,52,8),
    	new Color(255,51,8),
    	new Color(255,50,8),
    	new Color(255,49,8),
    	new Color(255,48,8),
    	new Color(255,47,8),
    	new Color(255,46,8),
    	new Color(255,45,8),
    	new Color(255,44,8),
    	new Color(255,43,8),
    	new Color(255,42,9),
    	new Color(255,41,9),
    	new Color(255,40,9),
    	new Color(255,39,9),
    	new Color(255,38,9),
    	new Color(255,37,9),
    	new Color(255,36,9),
    	new Color(255,35,9),
    	new Color(255,34,9),
    	new Color(255,33,9),
    	new Color(255,32,9),
    	new Color(255,31,9),
    	new Color(255,30,9),
    	new Color(255,29,9),
    	new Color(255,28,9),
    	new Color(255,27,9),
    	new Color(255,26,9),
    	new Color(255,25,9),
    	new Color(255,24,9),
    	new Color(255,23,9),
    	new Color(255,22,9),
    	new Color(255,21,9),
    	new Color(255,20,9),
    	new Color(255,19,9),
    	new Color(255,18,9),
    	new Color(255,17,10),
    	new Color(255,16,10),
    	new Color(255,15,10),
    	new Color(255,14,10),
    	new Color(255,13,10),
    	new Color(255,12,10),
    	new Color(255,11,10),
    	new Color(255,10,10),
    	new Color(255,9,10),
    	new Color(255,8,10),
    	new Color(255,7,10),
    	new Color(255,6,10),
    	new Color(255,5,10),
    	new Color(255,4,10),
    	new Color(255,3,10),
    	new Color(255,2,10),
    	new Color(255,1,10),
    	new Color(255,0,10)
};


    // linear min/max scaling; assumes value \in [0.0 .. 1.0]; applies alpha value
    public static Color getColor(float value, float alpha)
    {
        Color c = getColor(value);
        float components[] = c.getComponents(null);
        return new Color(components[0], components[1], components[2], alpha);
    }

    // linear min/max scaling; assumes value \in [0.0 .. 1.0]
    public static Color getColor(float value)
    {
      int index = Math.round(value * colors.length);
      if (index >= colors.length) {
        index = colors.length-1;
      }
      if (index < 0) {
        index = 0;
      }
      return colors[index];
    }
    
    

    public static void drawColormap(Rectangle2D rect, Graphics2D g, String label1, String label2)
    {
        drawColormap(rect, g, 1f, label1, label2);
    }

    // draw colormap legend into given Rectangle2D (horizontally)
    public static void drawColormap(Rectangle2D rect, Graphics2D g, float alpha, String label1, String label2)
    {
        // loop columns
        int w=(int)rect.getWidth();
        int h=(int)rect.getHeight();
        int i;
        for (i=0;i<w;i++)
        {
            g.setColor(getColor((float)i/w,alpha));
            g.drawLine((int)rect.getX()+i,(int)rect.getY(),(int)rect.getX()+i,(int)rect.getY()+h);
        }
        // draw frame
        g.setColor(Color.black);
        g.draw(rect);
        // draw legend
        if (label1!=null) g.drawString(label1, (int)rect.getMinX()+5, (int)rect.getMinY()+12);
        if (label2!=null) g.drawString(label2, (int)rect.getMaxX()-30, (int)rect.getMinY()+12);
    }
}
