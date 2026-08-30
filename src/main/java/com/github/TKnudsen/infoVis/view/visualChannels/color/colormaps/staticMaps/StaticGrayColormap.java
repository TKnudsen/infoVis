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
public class StaticGrayColormap
{



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
      if (value>= 1) {
    	  value= 1;
      }
      if (value< 0) {
        value = 0;
      }
      return new Color(value,value,value);
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
