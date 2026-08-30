package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.staticMaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

// Prototype class for the implemented colormaps

/**
 * @version 1.0
 * @since 2010
 */
public class StaticReliefColorMap
{

    private static final Color colors[] = {
//    		new Color(3,91,162
//),        new Color(1,116,181
//),        new Color(29,128,193
//),        new Color(55,140,202
//),        new Color(78,158,211
//),        new Color(96,176,223
//),        new Color(117,196,243
//),        
    		new Color(78,160,90
),			new Color(112,190,102
),			new Color(140,202,106
),        	new Color(176,213,110
),        	new Color(206,196,115
),        	new Color(241,176,128
),        	new Color(218,151,108
),        	new Color(175,108,66
),        	new Color(159,91,52
),        	new Color(145,75,40
),        	new Color(93,55,37)};
       


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
//      int index = Math.round( value * colors.length );
//      if (index >= colors.length) {
//        index = colors.length-1;
//      }
//      if (index < 0) {
//        index = 0;
//      }
//      return colors[index];
      
      double index = value * colors.length;
      if (index >= colors.length) {
        index = colors.length-1;
      }
      if (index < 0) {
        index = 0;
      }
      int i_low = (int)index;
      int i_high = i_low+1;
      Color c_low = colors[i_low];
      Color c_high = colors[Math.min(i_high, colors.length-1)];
      Color ret = new Color((int)(c_low.getRed()*(1-(index-i_low))+c_high.getRed()*(1-(i_high-index))), (int)(c_low.getGreen()*(1-(index-i_low))+c_high.getGreen()*(1-(i_high-index))), (int)(c_low.getBlue()*(1-(index-i_low))+c_high.getBlue()*(1-(i_high-index))));
      return ret;
    }

    public static void drawColormap(Rectangle2D rect, Graphics2D g, boolean reverse)
    {
        drawColormap(rect, g, 1f, reverse);
    }

    // draw colormap legend into given Rectangle2D (horizontally)
    public static void drawColormap(Rectangle2D rect, Graphics2D g, float alpha, boolean reverse)
    {
        // loop columns
        int w=(int)rect.getWidth();
        int h=(int)rect.getHeight();
        int i;
        for (i=0;i<w;i++)
        {
            if (reverse) g.setColor(getColor((float)(w-i)/w,alpha));
            else g.setColor(getColor((float)i/w,alpha));
            g.drawLine((int)rect.getX()+i,(int)rect.getY(),(int)rect.getX()+i,(int)rect.getY()+h);
        }
        // draw frame
        g.setColor(Color.black);
        g.draw(rect);
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
