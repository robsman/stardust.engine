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

import java.awt.Graphics;

class EndSegment extends Segment
{
    public EndSegment(SegmentPath NewPath, int NewXPos, int NewYPos)
    {
        super(NewPath, NewXPos, NewYPos);
    }

    public String toString()
    {
        return "E" + super.toString();
    }

    public void checkIntegrity()
    {
        if (isFirst())
        {
            getNext().checkIntegrity();
        }
        else if (isLast())
        {
            getPrevious().checkIntegrity();
        }
    }

    public void checkRedundancy()
    {
        if (isFirst())
        {
            getNext().checkRedundancy();
        }
        else if (isLast())
        {
            getPrevious().checkRedundancy();
        }
    }

    public void draw(Graphics graphics)
    {
        if (isLast())
        {
            int width = 10;
            int length = 10;

            graphics.fillPolygon(new int[]{point.x, point.x - length, point.x - length},
                                 new int[]{point.y, point.y + width / 2, point.y - width / 2},
                                 3);
        }
    }

    public int getDimension()
    {
        return SpatialDimension.UNKNOWN;
    }

    public void moveRel(int xShift, int yShift)
    {
        translate(xShift, yShift);

        // Validate current position

        IntegerWrapper tempX = new IntegerWrapper(point.x);
        IntegerWrapper tempY = new IntegerWrapper(point.y);

        if (isFirst())
        {
            path.validLeftDrag(tempX, tempY);
        }
        else if (isLast())
        {
            path.validRightDrag(tempX, tempY);
        }

        // Move to newly computed point

        setXPos(tempX.intValue());
        setYPos(tempY.intValue());

        // Adjust other segments

        checkIntegrity();
    }

    /**
     *
     * @return 1 if the segment has positive direction,
     *         -1 if the segment has negative direction
     *         0 if the segment has not direction (end segment)
     */
    public int getDirection()
    {
        return 0;
    }

    /**
     *
     * @return Length of the segment.
     */
    public int getLength()
    {
        return 0;
    }
}
