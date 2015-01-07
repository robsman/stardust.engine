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
import java.awt.Point;
import java.io.Serializable;

abstract class Segment implements Serializable
{
    public static final int MIN_LENGTH = 20;

    Point point;
    SegmentPath path;
    Segment Prev;
    Segment Next;

    public Segment(SegmentPath NewPath, int NewXPos, int NewYPos)
    {
        point = new Point();

        path = NewPath;
        point.x = NewXPos;
        point.y = NewYPos;
        Next = null;
        Prev = null;
    }

    public String toString()
    {
        return "(" + point.x + "," + point.y + ")";
    }

    /** */
    public abstract void checkIntegrity();

    public void checkMinLength()
    {
    }

    /** */
    public abstract void checkRedundancy();

    public void computeSize(IntegerWrapper MinX, IntegerWrapper MinY,
                            IntegerWrapper MaxX, IntegerWrapper MaxY)
    {
        if (point.x > MaxX.intValue())
            MaxX.setValue(point.x);
        if (point.y > MaxY.intValue())
            MaxY.setValue(point.y);
        if (point.x < MinX.intValue())
            MinX.setValue(point.x);
        if (point.y < MinY.intValue())
            MinY.setValue(point.y);

        if (!isLast())
            getNext().computeSize(MinX, MinY, MaxX, MaxY);
    }

    /** */
    public abstract void draw(Graphics graphics);

    /** */
    public abstract int getDimension();

    public Segment getMaxSeg()
    {
        return getNext();
    }

    public int getMaxXPos()
    {
        return point.x;
    }

    public int getMaxYPos()
    {
        return point.y;
    }

    public Segment getMinSeg()
    {
        return getNext();
    }

    public int getMinXPos()
    {
        return point.x;
    }

    public int getMinYPos()
    {
        return point.y;
    }

    public Segment getNext()
    {
        return Next;
    }

    public Segment getPrevious()
    {
        return Prev;
    }

    public int getXPos()
    {
        return point.x;
    }

    public int getYPos()
    {
        return point.y;
    }

    public boolean isFirst()
    {
        return (Prev == null ? true : false);
    }

    public boolean isHitBy(int x, int y)
    {
        return false;
    }

    public boolean isLast()
    {
        return (Next == null ? true : false);
    }

    /** */
    public abstract void moveRel(int xShift, int yShift);

    /**
     *
     * @return 1 if the segment has positive direction,
     *         -1 if the segment has negative direction
     *         0 if the segment has not direction (end segment)
     */
    public abstract int getDirection();

    /**
     *
     * @return Length of the segment.
     */
    public abstract int getLength();

    public void moveTo(int NewXPos, int NewYPos)
    {
        point.x = NewXPos;
        point.y = NewYPos;
    }

    public void setNext(Segment NewNext)
    {
        if (Next != NewNext)
        {
            Next = NewNext;

            if (Next != null)
            {
                Next.setPrevious(this);
            }
        }
    }

    public void setPrevious(Segment NewPrev)
    {
        if (Prev != NewPrev)
        {
            Prev = NewPrev;

            if (Prev != null)
            {
                Prev.setNext(this);
            }
        }
    }

    public void setXPos(int NewXPos)
    {
        point.x = NewXPos;
    }

    public void setYPos(int NewYPos)
    {
        point.y = NewYPos;
    }

    public void translate(int xShift, int yShift)
    {
        point.x += xShift;
        point.y += yShift;
    }
}
