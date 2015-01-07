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

import java.awt.*;
import java.awt.geom.Point2D;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;


/**
 * @author mgille
 * @versin $Revision$
 */
public class GraphicsHelper
{
    public static final ArrowKey DEFAULT_ARROW = ArrowKey.EMPTY_TRIANGLE;
    public static final int DEFAULT_ARROW_LENGTH = 8;
    public static final int DEFAULT_ARROW_HEIGTH = 8;

    protected static int[] xPosArray = new int[5];
    protected static int[] yPosArray = new int[5];

    /**  */
    public static void drawArrow(Graphics2D graphics
                                 , int x1, int y1, ArrowKey arrowKey1
                                 , int x2, int y2, ArrowKey arrowKey2)
    {
        drawArrow(graphics
                  , x1, y1, arrowKey1, DEFAULT_ARROW_LENGTH, DEFAULT_ARROW_HEIGTH
                  , x2, y2, arrowKey2, DEFAULT_ARROW_LENGTH, DEFAULT_ARROW_HEIGTH);
    }

    /**  */
    public static void drawArrow(Graphics2D graphics
                                 , int x1, int y1, ArrowKey arrowKey1, int arrow1Length, int arrow1Heigth
                                 , int x2, int y2, ArrowKey arrowKey2, int arrow2Length, int arrow2Heigth)
    {
        float _lengthLine = (new Float(Point2D.distance(x1, y1, x2, y2))).floatValue();

        if (_lengthLine > 0)
        {
            float _sin = (y1 - y2) / _lengthLine;
            float _cos = (x2 - x1) / _lengthLine;

            if (arrow1Heigth > 0 && arrow1Length > 0 && arrowKey1 != ArrowKey.NO_ARROW)
            {
                // draw the first arrow
                drawArrow(graphics, x1, y1, _sin, _cos, arrowKey1, arrow1Length, arrow1Heigth);
            }
            if (arrow2Heigth > 0 && arrow2Length > 0 && arrowKey2 != ArrowKey.NO_ARROW)
            {
                // draw the second arrow
                drawArrow(graphics, x2, y2, -_sin, -_cos, arrowKey2, arrow2Length, arrow2Heigth);
            }
        }
    }

    /**  */
    public static void drawArrow(Graphics2D graphics
                                 , int x, int y
                                 , float sin
                                 , float cos
                                 , ArrowKey arrowKey
                                 , int arrowLength
                                 , int arrowHeigth)
    {
        // Hint: 	The algoritm based on a rotation, with
        //	x' =   x * cos(alpha) + y * sin(alpha)
        //	y' = - x * sin(alpha) + y * cos(alpha)
        //
        // 	(x, y) is the old point, (x', y') is the new point
        //	'alpha' is the rotation-angle
        // Attention: Be aware that the Rotation-Center is the point (0,0).

        Color _oldColor = graphics.getColor();

        Assert.isNotNull(graphics, "graphics reference is not null");
        if (arrowHeigth > 0 && arrowLength > 0)
        {
            // calculate the points for the arrow and draw the arrow
            // hint: for a effektive algorithm the polyline doesn't start
            //	  at the top of the arrow
            //       On this way the array can be used for booth (arrow and rhombus)
            xPosArray[0] = x + Math.round(arrowLength * cos - arrowHeigth / 2 * sin);
            xPosArray[1] = x;
            xPosArray[2] = x + Math.round(arrowLength * cos + arrowHeigth / 2 * sin);
            xPosArray[3] = x + Math.round(2 * arrowLength * cos);
            yPosArray[0] = y + Math.round(-arrowLength * sin - arrowHeigth / 2 * cos);
            yPosArray[1] = y;
            yPosArray[2] = y + Math.round(-arrowLength * sin + arrowHeigth / 2 * cos);
            yPosArray[3] = y + Math.round(-2 * arrowLength * sin);

            if (arrowKey.equals(ArrowKey.EMPTY_TRIANGLE))
            {
                graphics.setColor(Color.white);
                graphics.fillPolygon(xPosArray, yPosArray, 3);
                graphics.setColor(_oldColor);
                graphics.drawPolygon(xPosArray, yPosArray, 3);
            }
            else if (arrowKey.equals(ArrowKey.FILLED_TRIANGLE))
            {
                graphics.fillPolygon(xPosArray, yPosArray, 3);
            }
            else if (arrowKey.equals(ArrowKey.OPEN_TRIANGLE))
            {
                graphics.drawPolyline(xPosArray, yPosArray, 3);
            }
            else if (arrowKey.equals(ArrowKey.EMPTY_RHOMBUS))
            {
                graphics.setColor(Color.white);
                graphics.fillPolygon(xPosArray, yPosArray, 4);
                graphics.setColor(_oldColor);
                graphics.drawPolygon(xPosArray, yPosArray, 4);
            }
            else if (arrowKey.equals(ArrowKey.FILLED_RHOMBUS))
            {
                graphics.fillPolygon(xPosArray, yPosArray, 4);
            }
            else if (arrowKey.equals(ArrowKey.NO_ARROW))
            {
                // do nothing.
            }
            else
            {
            throw new PublicException(BpmRuntimeError.DIAG_UNEXPECTED_ARROW_TYPE.raise());
            }
        }
    }
}

