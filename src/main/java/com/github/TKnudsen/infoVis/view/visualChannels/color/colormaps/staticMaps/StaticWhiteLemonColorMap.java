package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.staticMaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * @version 1.0
 * @since 2010
 */
public class StaticWhiteLemonColorMap
{

    private static final Color colors[] = {
    	
		new Color(255,255,255),
		new Color(255,255,254),
		new Color(255,255,254),
		new Color(255,255,253),
		new Color(254,255,252),
		new Color(254,255,251),
		new Color(254,255,251),
		new Color(254,255,250),
		new Color(254,255,249),
		new Color(254,255,248),
		new Color(254,255,248),
		new Color(253,255,247),
		new Color(253,255,246),
		new Color(253,255,245),
		new Color(253,255,245),
		new Color(253,255,244),
		new Color(253,255,243),
		new Color(252,255,242),
		new Color(252,255,242),
		new Color(252,255,241),
		new Color(252,255,240),
		new Color(252,255,239),
		new Color(252,255,239),
		new Color(252,255,238),
		new Color(251,255,237),
		new Color(251,255,236),
		new Color(251,255,236),
		new Color(251,255,235),
		new Color(251,255,234),
		new Color(251,255,233),
		new Color(251,255,233),
		new Color(250,255,232),
		new Color(250,255,231),
		new Color(250,255,230),
		new Color(250,255,230),
		new Color(250,255,229),
		new Color(250,255,228),
		new Color(249,255,227),
		new Color(249,255,227),
		new Color(249,255,226),
		new Color(249,255,225),
		new Color(249,255,224),
		new Color(249,255,224),
		new Color(249,255,223),
		new Color(248,255,222),
		new Color(248,255,221),
		new Color(248,255,221),
		new Color(248,255,220),
		new Color(248,255,219),
		new Color(248,255,218),
		new Color(248,255,218),
		new Color(247,255,217),
		new Color(247,255,216),
		new Color(247,255,215),
		new Color(247,255,215),
		new Color(247,255,214),
		new Color(247,255,213),
		new Color(246,255,212),
		new Color(246,255,211),
		new Color(246,255,211),
		new Color(246,255,210),
		new Color(246,255,209),
		new Color(246,255,209),
		new Color(246,255,208),
		new Color(245,255,207),
		new Color(245,255,206),
		new Color(245,255,206),
		new Color(245,255,205),
		new Color(245,255,204),
		new Color(245,255,203),
		new Color(245,255,203),
		new Color(244,255,202),
		new Color(244,255,201),
		new Color(244,255,200),
		new Color(244,255,200),
		new Color(244,255,199),
		new Color(244,255,198),
		new Color(243,255,197),
		new Color(243,255,196),
		new Color(243,255,196),
		new Color(243,255,195),
		new Color(243,255,194),
		new Color(243,255,194),
		new Color(243,255,193),
		new Color(242,255,192),
		new Color(242,255,191),
		new Color(242,255,191),
		new Color(242,255,190),
		new Color(242,255,189),
		new Color(242,255,188),
		new Color(242,255,188),
		new Color(241,255,187),
		new Color(241,255,186),
		new Color(241,255,185),
		new Color(241,255,185),
		new Color(241,255,184),
		new Color(241,255,183),
		new Color(240,255,182),
		new Color(240,255,181),
		new Color(240,255,181),
		new Color(240,255,180),
		new Color(240,255,179),
		new Color(240,255,179),
		new Color(240,255,178),
		new Color(239,255,177),
		new Color(239,255,176),
		new Color(239,255,176),
		new Color(239,255,175),
		new Color(239,255,174),
		new Color(239,255,173),
		new Color(239,255,173),
		new Color(238,255,172),
		new Color(238,255,171),
		new Color(238,255,170),
		new Color(238,255,170),
		new Color(238,255,169),
		new Color(238,255,168),
		new Color(237,255,167),
		new Color(237,255,167),
		new Color(237,255,166),
		new Color(237,255,165),
		new Color(237,255,164),
		new Color(237,255,163),
		new Color(237,255,163),
		new Color(236,255,162),
		new Color(236,255,161),
		new Color(236,255,161),
		new Color(236,255,160),
		new Color(236,255,159),
		new Color(236,255,158),
		new Color(236,255,158),
		new Color(235,255,157),
		new Color(235,255,156),
		new Color(235,255,155),
		new Color(235,255,155),
		new Color(235,255,154),
		new Color(235,255,153),
		new Color(234,255,152),
		new Color(234,255,152),
		new Color(234,255,151),
		new Color(234,255,150),
		new Color(234,255,149),
		new Color(234,255,148),
		new Color(234,255,148),
		new Color(233,255,147),
		new Color(233,255,146),
		new Color(233,255,146),
		new Color(233,255,145),
		new Color(233,255,144),
		new Color(233,255,143),
		new Color(233,255,143),
		new Color(232,255,142),
		new Color(232,255,141),
		new Color(232,255,140),
		new Color(232,255,140),
		new Color(232,255,139),
		new Color(232,255,138),
		new Color(231,255,137),
		new Color(231,255,137),
		new Color(231,255,136),
		new Color(231,255,135),
		new Color(231,255,134),
		new Color(231,255,134),
		new Color(231,255,133),
		new Color(230,255,132),
		new Color(230,255,131),
		new Color(230,255,131),
		new Color(230,255,130),
		new Color(230,255,129),
		new Color(230,255,128),
		new Color(230,255,128),
		new Color(229,255,127),
		new Color(229,255,126),
		new Color(229,255,125),
		new Color(229,255,125),
		new Color(229,255,124),
		new Color(229,255,123),
		new Color(228,255,122),
		new Color(228,255,122),
		new Color(228,255,121),
		new Color(228,255,120),
		new Color(228,255,119),
		new Color(228,255,119),
		new Color(228,255,118),
		new Color(227,255,117),
		new Color(227,255,116),
		new Color(227,255,116),
		new Color(227,255,115),
		new Color(227,255,114),
		new Color(227,255,113),
		new Color(227,255,113),
		new Color(226,255,112),
		new Color(226,255,111),
		new Color(226,255,110),
		new Color(226,255,110),
		new Color(226,255,109),
		new Color(226,255,108),
		new Color(225,255,107),
		new Color(225,255,106),
		new Color(225,255,106),
		new Color(225,255,105),
		new Color(225,255,104),
		new Color(225,255,104),
		new Color(225,255,103),
		new Color(224,255,102),
		new Color(224,255,101),
		new Color(224,255,100),
		new Color(224,255,100),
		new Color(224,255,99),
		new Color(224,255,98),
		new Color(224,255,98),
		new Color(223,255,97),
		new Color(223,255,96),
		new Color(223,255,95),
		new Color(223,255,95),
		new Color(223,255,94),
		new Color(223,255,93),
		new Color(222,255,92),
		new Color(222,255,92),
		new Color(222,255,91),
		new Color(222,255,90),
		new Color(222,255,89),
		new Color(222,255,89),
		new Color(222,255,88),
		new Color(221,255,87),
		new Color(221,255,86),
		new Color(221,255,86),
		new Color(221,255,85),
		new Color(221,255,84),
		new Color(221,255,83),
		new Color(221,255,82),
		new Color(220,255,82),
		new Color(220,255,81),
		new Color(220,255,80),
		new Color(220,255,80),
		new Color(220,255,79),
		new Color(220,255,78),
		new Color(219,255,77),
		new Color(219,255,77),
		new Color(219,255,76),
		new Color(219,255,75),
		new Color(219,255,74),
		new Color(219,255,74),
		new Color(219,255,73),
		new Color(218,255,72),
		new Color(218,255,71),
		new Color(218,255,71),
		new Color(218,255,70),
		new Color(218,255,69),
		new Color(218,255,68),
		new Color(218,255,68),
		new Color(217,255,67),
		new Color(217,255,66),
		new Color(217,255,65),
		new Color(217,255,65),
		new Color(217,255,64)};
    
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
      int index = Math.round( value * colors.length );
      if (index >= colors.length) {
        index = colors.length-1;
      }
      if (index < 0) {
        index = 0;
      }
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
