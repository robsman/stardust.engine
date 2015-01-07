/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.compatibility.diagram;

import java.awt.Rectangle;

/** */
public class GeometryHelper
{
    private static final int PICK_SENSIVITY = 4;

    /** */
    public static double sqr(double f)
    {
        return (f * f);
    }

    /** */
    public static boolean isCloseToLine(int xClickPos, int yClickPos,
                                        int Left, int Bottom, int Right, int Top)
    {
        // Check for distance orthogonal to the line

        double VectProd;
        double ScalProd;
        double NormSqr;
        double Norm;

        VectProd = (Right - Left) * (yClickPos - Bottom) - (Top - Bottom) * (xClickPos - Left);
        ScalProd = (Right - Left) * (xClickPos - Left) + (Top - Bottom) * (yClickPos - Bottom);
        NormSqr = sqr(Right - Left) + sqr(Top - Bottom);
        Norm = Math.sqrt(NormSqr);

        if (Math.abs(VectProd / Norm) <= PICK_SENSIVITY && ScalProd >= 0 && ScalProd <= NormSqr)
        {
            return true;
        }

        // Check for the distance to the ending points

        if (Math.sqrt(sqr(xClickPos - Left) + sqr(yClickPos - Bottom)) <= PICK_SENSIVITY)
        {
            return true;
        }

        if (Math.sqrt(sqr(xClickPos - Right) + sqr(yClickPos - Top)) <= PICK_SENSIVITY)
        {
            return true;
        }

        return false;
    }

    /** */
    public static boolean isCloseToPath(int xClickPos, int yClickPos,
                                        int xPath[], int yPath[], boolean closed)
    {
        int i;

        for (i = 1; i < xPath.length; ++i)
        {
            if (isCloseToLine(xClickPos, yClickPos, xPath[i - 1], yPath[i - 1],
                              xPath[i], yPath[i]))
            {
                return true;
            }
        }

        if (closed)
        {
            return isCloseToLine(xClickPos, yClickPos, xPath[i - 1], yPath[i - 1],
                                 xPath[0], yPath[0]);
        }
        else
        {
            return false;
        }
    }

    /** */
    public static boolean isInsidePath(int xClickPos, int yClickPos,
                                       int xPath[], int yPath[])
    {
        return false;
    }

    /** */
    public static boolean isCloseToRectangle(int xClickPos, int yClickPos,
                                             int x, int y, int w, int h)
    {
        Rectangle outside = new Rectangle(x - PICK_SENSIVITY,
                                          y - PICK_SENSIVITY,
                                          w + PICK_SENSIVITY + PICK_SENSIVITY,
                                          h + PICK_SENSIVITY + PICK_SENSIVITY);

        Rectangle inside = new Rectangle(x + PICK_SENSIVITY,
                                         y + PICK_SENSIVITY,
                                         w - PICK_SENSIVITY - PICK_SENSIVITY,
                                         h - PICK_SENSIVITY - PICK_SENSIVITY);

        if (outside.contains(xClickPos,
                             yClickPos))
        {

            if (inside.contains(xClickPos,
                                yClickPos))
            {
                return false;
            }

            return true;
        }

        return false;
    }

    /** */
    public static boolean isInsideRectangle(int xClickPos, int yClickPos,
                                            int x, int y, int w, int h)
    {
        if (xClickPos >= x - PICK_SENSIVITY
                && xClickPos <= x + w + PICK_SENSIVITY
                && yClickPos >= y - PICK_SENSIVITY
                && yClickPos <= y + h + PICK_SENSIVITY)
        {

            return true;
        }
        return false;
    }

    /** */
    public static boolean isCloseToEllipse(int xClickPos, int yClickPos,
                                           int x, int y, int w, int h)
    {
        if (w == 0)
        {
            return ((yClickPos >= y - PICK_SENSIVITY)
                    && (yClickPos <= y + h + PICK_SENSIVITY));
        }

        if (h == 0)
        {
            return ((xClickPos >= x - PICK_SENSIVITY)
                    && (xClickPos <= x + w + PICK_SENSIVITY));
        }

        double a = w / 2.0;
        double b = h / 2.0;

        double diffX = xClickPos - x - a;
        double diffY = yClickPos - y - b;

        // Outside

        double diffXo = diffX / (a + PICK_SENSIVITY);
        double diffYo = diffY / (b + PICK_SENSIVITY);

        double ro = Math.sqrt((diffXo * diffXo) + (diffYo * diffYo));

        if (ro <= 1.0)
        {

            // Inside

            if (a <= PICK_SENSIVITY
                    || b <= PICK_SENSIVITY)
            {
                return true;
            }

            double diffXi = diffX / (a - PICK_SENSIVITY);
            double diffYi = diffY / (b - PICK_SENSIVITY);

            double ri = Math.sqrt((diffXi * diffXi) + (diffYi * diffYi));

            if (ri >= 1.0)
            {
                return true;
            }
        }

        return false;
    }

    /** */
    public static boolean isInsideEllipse(int xClickPos, int yClickPos,
                                          int x, int y,
                                          int w, int h)
    {
        if (w == 0)
        {
            return ((yClickPos >= y - PICK_SENSIVITY)
                    && (yClickPos <= y + h + PICK_SENSIVITY));
        }

        if (h == 0)
        {
            return ((xClickPos >= x - PICK_SENSIVITY)
                    && (xClickPos <= x + w + PICK_SENSIVITY));
        }

        double a = w / 2.0;
        double b = h / 2.0;

        double diffX = xClickPos - x - a;
        double diffY = yClickPos - y - b;

        double diffXo = diffX / (a + PICK_SENSIVITY);
        double diffYo = diffY / (b + PICK_SENSIVITY);

        double ro = Math.sqrt((diffXo * diffXo) + (diffYo * diffYo));

        if (ro <= 1.0)
        {
            return true;
        }

        return false;
    }

    /** */
    public static int[] interRectLine(int FirstRectLeft, int FirstRectBottom,
                                      int FirstRectRight, int FirstRectTop,
                                      int SecondRectLeft, int SecondRectBottom,
                                      int SecondRectRight, int SecondRectTop)
    {
        int[] returnArray = new int[4];
        double XSecondLine;
        double YSecondLine;
        double XFirstLine;
        double YFirstLine;
        double FirstWidth;
        double FirstHeight;
        double SecondWidth;
        double SecondHeight;
        double LineWidth;
        double LineHeight;
        double LineGrowth;

        XFirstLine = 0.5 * (FirstRectLeft + FirstRectRight);
        YFirstLine = 0.5 * (FirstRectBottom + FirstRectTop);
        XSecondLine = 0.5 * (SecondRectLeft + SecondRectRight);
        YSecondLine = 0.5 * (SecondRectBottom + SecondRectTop);

        FirstWidth = Math.abs(FirstRectRight - FirstRectLeft);
        FirstHeight = Math.abs(FirstRectTop - FirstRectBottom);
        SecondWidth = Math.abs(SecondRectRight - SecondRectLeft);
        SecondHeight = Math.abs(SecondRectTop - SecondRectBottom);

        LineWidth = Math.abs(XSecondLine - XFirstLine);
        LineHeight = Math.abs(YSecondLine - YFirstLine);

        // Feststellen, ob die Rechtecke ueberlappen

        /***
         if (LineWidth < 0.5 * (FirstWidth + SecondWidth) &&
         LineHeight < 0.5 * (FirstHeight + SecondHeight))
         {
         // Rechtecke ueberlappen

         return 0;
         }
         ***/

        // Quadrants are
        //
        //   41
        //   32
        //

        if (FirstWidth == 0 || SecondWidth == 0)
        {
            returnArray[0] = (int) Math.round(XFirstLine);
            returnArray[1] = (int) Math.round(YFirstLine);
            returnArray[2] = (int) Math.round(XSecondLine);
            returnArray[3] = (int) Math.round(YSecondLine);

            return returnArray;
        }

        if (XFirstLine < XSecondLine)
        {
            // First rectangle left of second rectangle

            if (YFirstLine > YSecondLine)
            {
                // First rectangle below second rectangle; third quadrant

                if (FirstHeight / FirstWidth > LineHeight / LineWidth)
                {
                    returnArray[0] = Math.round(FirstRectRight);
                    returnArray[1] = (int) Math.round(YFirstLine - 0.5 * FirstWidth * LineHeight / LineWidth);
                }
                else
                {
                    returnArray[0] = (int) Math.round(XFirstLine + 0.5 * FirstHeight * LineWidth / LineHeight);
                    returnArray[1] = Math.round(FirstRectTop);
                }

                if (SecondHeight / SecondWidth > LineHeight / LineWidth)
                {
                    returnArray[2] = Math.round(SecondRectLeft);
                    returnArray[3] = (int) Math.round(YSecondLine + 0.5 * SecondWidth * LineHeight / LineWidth);
                }
                else
                {
                    returnArray[2] = (int) Math.round(XSecondLine - 0.5 * SecondHeight * LineWidth / LineHeight);
                    returnArray[3] = Math.round(SecondRectBottom);
                }
            }
            else
            {
                // First rectangle above second rectangle; fourth quadrant

                if (FirstHeight / FirstWidth > LineHeight / LineWidth)
                {
                    returnArray[0] = Math.round(FirstRectRight);
                    returnArray[1] = (int) Math.round(YFirstLine + 0.5 * FirstWidth * LineHeight / LineWidth);
                }
                else
                {
                    returnArray[0] = (int) Math.round(XFirstLine + 0.5 * FirstHeight * LineWidth / LineHeight);
                    returnArray[1] = Math.round(FirstRectBottom);
                }

                if (SecondHeight / SecondWidth > LineHeight / LineWidth)
                {
                    returnArray[2] = Math.round(SecondRectLeft);
                    returnArray[3] = (int) Math.round(YSecondLine - 0.5 * SecondWidth * LineHeight / LineWidth);
                }
                else
                {
                    returnArray[2] = (int) Math.round(XSecondLine - 0.5 * SecondHeight * LineWidth / LineHeight);
                    returnArray[3] = Math.round(SecondRectTop);
                }
            }
        }
        else
        {
            // First rectangle right of second rectangle

            if (YFirstLine > YSecondLine)
            {
                // First rectangle below second rectangle; second quadrant

                if (FirstHeight / FirstWidth > LineHeight / LineWidth)
                {
                    returnArray[0] = Math.round(FirstRectLeft);
                    returnArray[1] = (int) Math.round(YFirstLine - 0.5 * FirstWidth * LineHeight / LineWidth);
                }
                else
                {
                    returnArray[0] = (int) Math.round(XFirstLine - 0.5 * FirstHeight * LineWidth / LineHeight);
                    returnArray[1] = Math.round(FirstRectTop);
                }

                if (SecondHeight / SecondWidth > LineHeight / LineWidth)
                {
                    returnArray[2] = Math.round(SecondRectRight);
                    returnArray[3] = (int) Math.round(YSecondLine + 0.5 * SecondWidth * LineHeight / LineWidth);
                }
                else
                {
                    returnArray[2] = (int) Math.round(XSecondLine + 0.5 * SecondHeight * LineWidth / LineHeight);
                    returnArray[3] = Math.round(SecondRectBottom);
                }
            }
            else
            {
                // First rectangle above second rectangle; first quadrant

                if (FirstHeight / FirstWidth > LineHeight / LineWidth)
                {
                    returnArray[0] = Math.round(FirstRectLeft);
                    returnArray[1] = (int) Math.round(YFirstLine + 0.5 * FirstWidth * LineHeight / LineWidth);
                }
                else
                {
                    returnArray[0] = (int) Math.round(XFirstLine - 0.5 * FirstHeight * LineWidth / LineHeight);
                    returnArray[1] = Math.round(FirstRectBottom);
                }

                if (SecondHeight / SecondWidth > LineHeight / LineWidth)
                {
                    returnArray[2] = Math.round(SecondRectRight);
                    returnArray[3] = (int) Math.round(YSecondLine - 0.5 * SecondWidth * LineHeight / LineWidth);
                }
                else
                {
                    returnArray[2] = (int) Math.round(XSecondLine + 0.5 * SecondHeight * LineWidth / LineHeight);
                    returnArray[3] = Math.round(SecondRectTop);
                }
            }
        }

        return returnArray;
    }
}

