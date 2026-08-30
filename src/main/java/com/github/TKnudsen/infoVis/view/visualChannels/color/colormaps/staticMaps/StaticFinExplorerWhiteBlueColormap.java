package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.staticMaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

// Prototype class for the implemented colormaps

/**
 * @version 1.0
 * @since 2009
 */
public class StaticFinExplorerWhiteBlueColormap
{

    private static final Color colors[] = {
		new Color(255,255,255),
		new Color(254,254,255),
		new Color(253,253,255),
		new Color(252,252,255),
		new Color(251,251,255),
		new Color(250,250,255),
		new Color(249,249,255),
		new Color(248,248,255),
		new Color(247,247,255),
		new Color(246,246,255),
		new Color(246,245,255),
		new Color(245,244,255),
		new Color(244,243,255),
		new Color(243,243,255),
		new Color(242,242,255),
		new Color(241,241,255),
		new Color(240,240,255),
		new Color(239,239,255),
		new Color(238,238,255),
		new Color(237,237,255),
		new Color(236,236,255),
		new Color(235,235,255),
		new Color(234,234,255),
		new Color(233,233,255),
		new Color(232,232,255),
		new Color(231,231,255),
		new Color(230,230,255),
		new Color(229,229,255),
		new Color(228,228,255),
		new Color(228,227,255),
		new Color(227,226,255),
		new Color(226,225,255),
		new Color(225,224,255),
		new Color(224,223,255),
		new Color(223,222,255),
		new Color(222,221,255),
		new Color(221,220,255),
		new Color(220,219,255),
		new Color(219,219,255),
		new Color(218,218,255),
		new Color(217,217,255),
		new Color(216,216,255),
		new Color(215,215,255),
		new Color(214,214,255),
		new Color(213,213,255),
		new Color(212,212,255),
		new Color(211,211,255),
		new Color(210,210,255),
		new Color(209,209,255),
		new Color(209,208,255),
		new Color(208,207,255),
		new Color(207,206,255),
		new Color(206,205,255),
		new Color(205,204,255),
		new Color(204,203,255),
		new Color(203,202,255),
		new Color(202,201,255),
		new Color(201,200,255),
		new Color(200,199,255),
		new Color(199,198,255),
		new Color(198,197,255),
		new Color(197,196,255),
		new Color(196,195,255),
		new Color(195,195,255),
		new Color(194,194,255),
		new Color(193,193,255),
		new Color(192,192,255),
		new Color(191,191,255),
		new Color(191,190,255),
		new Color(190,189,255),
		new Color(189,188,255),
		new Color(188,187,255),
		new Color(187,186,255),
		new Color(186,185,255),
		new Color(185,184,255),
		new Color(184,183,255),
		new Color(183,182,255),
		new Color(182,181,255),
		new Color(181,180,255),
		new Color(180,179,255),
		new Color(179,178,255),
		new Color(178,177,255),
		new Color(177,176,255),
		new Color(176,175,255),
		new Color(175,174,255),
		new Color(174,173,255),
		new Color(173,172,255),
		new Color(173,171,255),
		new Color(172,171,255),
		new Color(171,170,255),
		new Color(170,169,255),
		new Color(169,168,255),
		new Color(168,167,255),
		new Color(167,166,255),
		new Color(166,165,255),
		new Color(165,164,255),
		new Color(164,163,255),
		new Color(163,162,255),
		new Color(162,161,255),
		new Color(161,160,255),
		new Color(160,159,255),
		new Color(159,158,255),
		new Color(158,157,255),
		new Color(157,156,255),
		new Color(156,155,255),
		new Color(155,154,255),
		new Color(155,153,255),
		new Color(154,152,255),
		new Color(153,151,255),
		new Color(152,150,255),
		new Color(151,149,255),
		new Color(150,148,255),
		new Color(149,147,255),
		new Color(148,147,255),
		new Color(147,146,255),
		new Color(146,145,255),
		new Color(145,144,255),
		new Color(144,143,255),
		new Color(143,142,255),
		new Color(142,141,255),
		new Color(141,140,255),
		new Color(140,139,255),
		new Color(139,138,255),
		new Color(138,137,255),
		new Color(137,136,255),
		new Color(137,135,255),
		new Color(136,134,255),
		new Color(135,133,255),
		new Color(134,132,255),
		new Color(133,131,255),
		new Color(132,130,255),
		new Color(131,129,255),
		new Color(130,128,255),
		new Color(129,127,255),
		new Color(128,126,255),
		new Color(127,125,255),
		new Color(126,124,255),
		new Color(125,123,255),
		new Color(124,123,255),
		new Color(123,122,255),
		new Color(122,121,255),
		new Color(121,120,255),
		new Color(120,119,255),
		new Color(119,118,255),
		new Color(118,117,255),
		new Color(118,116,255),
		new Color(117,115,255),
		new Color(116,114,255),
		new Color(115,113,255),
		new Color(114,112,255),
		new Color(113,111,255),
		new Color(112,110,255),
		new Color(111,109,255),
		new Color(110,108,255),
		new Color(109,107,255),
		new Color(108,106,255),
		new Color(107,105,255),
		new Color(106,104,255),
		new Color(105,103,255),
		new Color(104,102,255),
		new Color(103,101,255),
		new Color(102,100,255),
		new Color(101,99,255),
		new Color(100,99,255),
		new Color(100,98,255),
		new Color(99,97,255),
		new Color(98,96,255),
		new Color(97,95,255),
		new Color(96,94,255),
		new Color(95,93,255),
		new Color(94,92,255),
		new Color(93,91,255),
		new Color(92,90,255),
		new Color(91,89,255),
		new Color(90,88,255),
		new Color(89,87,255),
		new Color(88,86,255),
		new Color(87,85,255),
		new Color(86,84,255),
		new Color(85,83,255),
		new Color(84,82,255),
		new Color(83,81,255),
		new Color(82,80,255),
		new Color(82,79,255),
		new Color(81,78,255),
		new Color(80,77,255),
		new Color(79,76,255),
		new Color(78,75,255),
		new Color(77,75,255),
		new Color(76,74,255),
		new Color(75,73,255),
		new Color(74,72,255),
		new Color(73,71,255),
		new Color(72,70,255),
		new Color(71,69,255),
		new Color(70,68,255),
		new Color(69,67,255),
		new Color(68,66,255),
		new Color(67,65,255),
		new Color(66,64,255),
		new Color(65,63,255),
		new Color(64,62,255),
		new Color(64,61,255),
		new Color(63,60,255),
		new Color(62,59,255),
		new Color(61,58,255),
		new Color(60,57,255),
		new Color(59,56,255),
		new Color(58,55,255),
		new Color(57,54,255),
		new Color(56,53,255),
		new Color(55,52,255),
		new Color(54,51,255),
		new Color(53,51,255),
		new Color(52,50,255),
		new Color(51,49,255),
		new Color(50,48,255),
		new Color(49,47,255),
		new Color(48,46,255),
		new Color(47,45,255),
		new Color(46,44,255),
		new Color(45,43,255),
		new Color(45,42,255),
		new Color(44,41,255),
		new Color(43,40,255),
		new Color(42,39,255),
		new Color(41,38,255),
		new Color(40,37,255),
		new Color(39,36,255),
		new Color(38,35,255),
		new Color(37,34,255),
		new Color(36,33,255),
		new Color(35,32,255),
		new Color(34,31,255),
		new Color(33,30,255),
		new Color(32,29,255),
		new Color(31,28,255),
		new Color(30,27,255),
		new Color(29,27,255),
		new Color(28,26,255),
		new Color(27,25,255),
		new Color(27,24,255),
		new Color(26,23,255),
		new Color(25,22,255),
		new Color(24,21,255),
		new Color(23,20,255),
		new Color(22,19,255),
		new Color(21,18,255),
		new Color(20,17,255),
		new Color(19,16,255),
		new Color(18,15,255),
		new Color(17,14,255),
		new Color(16,13,255),
		new Color(15,12,255),
		new Color(14,11,255),
		new Color(13,10,255)};
    
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
