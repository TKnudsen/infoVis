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
public class StaticBlueRedColorMap
{

    private static final Color colors[] = {
        new Color(180, 49, 46),
        new Color(181, 54, 48),
        new Color(183, 59, 49),
        new Color(184, 63, 51),
        new Color(186, 68, 53),
        new Color(187, 73, 55),
        new Color(188, 78, 57),
        new Color(190, 83, 60),
        new Color(191, 87, 62),
        new Color(192, 92, 64),
        new Color(193, 97, 67),
        new Color(195, 102, 70),
        new Color(196, 107, 73),
        new Color(197, 111, 76),
        new Color(198, 116, 79),
        new Color(199, 121, 82),
        new Color(200, 126, 85),
        new Color(201, 130, 88),
        new Color(203, 135, 92),
        new Color(204, 140, 96),
        new Color(205, 144, 99),
        new Color(206, 149, 103),
        new Color(207, 153, 107),
        new Color(208, 158, 111),
        new Color(209, 162, 116),
        new Color(211, 167, 120),
        new Color(212, 171, 124),
        new Color(213, 176, 129),
        new Color(215, 180, 134),
        new Color(216, 184, 139),
        new Color(217, 188, 143),
        new Color(219, 192, 148),
        new Color(220, 196, 154),
        new Color(222, 200, 159),
        new Color(223, 204, 164),
        new Color(225, 208, 170),
        new Color(226, 212, 175),
        new Color(228, 216, 181),
        new Color(230, 219, 186),
        new Color(232, 223, 192),
        new Color(234, 227, 198),
        new Color(236, 230, 204),
        new Color(238, 233, 210),
        new Color(240, 237, 216),
        new Color(242, 240, 223),
        new Color(245, 243, 229),
        new Color(247, 246, 235),
        new Color(250, 249, 242),
        new Color(252, 252, 248),
        new Color(255, 255, 255),
        new Color(250, 250, 253),
        new Color(244, 244, 252),
        new Color(239, 239, 250),
        new Color(234, 234, 249),
        new Color(229, 229, 247),
        new Color(224, 224, 246),
        new Color(219, 219, 244),
        new Color(214, 214, 243),
        new Color(209, 209, 241),
        new Color(204, 204, 240),
        new Color(199, 199, 238),
        new Color(194, 194, 237),
        new Color(189, 189, 235),
        new Color(184, 185, 234),
        new Color(180, 180, 232),
        new Color(175, 175, 231),
        new Color(171, 171, 229),
        new Color(166, 166, 227),
        new Color(161, 162, 226),
        new Color(157, 157, 224),
        new Color(153, 153, 223),
        new Color(148, 148, 221),
        new Color(144, 144, 220),
        new Color(140, 140, 218),
        new Color(135, 136, 217),
        new Color(131, 131, 215),
        new Color(127, 127, 214),
        new Color(123, 123, 212),
        new Color(119, 119, 211),
        new Color(115, 115, 209),
        new Color(111, 111, 208),
        new Color(107, 107, 206),
        new Color(103, 104, 205),
        new Color(99, 100, 203),
        new Color(96, 96, 201),
        new Color(92, 92, 200),
        new Color(88, 89, 198),
        new Color(85, 85, 197),
        new Color(81, 81, 195),
        new Color(77, 78, 194),
        new Color(74, 74, 192),
        new Color(70, 71, 191),
        new Color(67, 67, 189),
        new Color(64, 64, 188),
        new Color(60, 61, 186),
        new Color(57, 58, 185),
        new Color(54, 54, 183),
        new Color(51, 51, 182),
        new Color(48, 48, 180),
        new Color(44, 45, 178)};


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
    	int numSamples = colors.length;
      int index = Math.round( value * numSamples );
      if (index >= numSamples) {
        index = numSamples-1;
      }
      if (index < 0) {
        index = 0;
      }
      index = numSamples-1-index;
      return colors[index];
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
