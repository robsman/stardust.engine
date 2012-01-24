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

class XSegment extends Segment
{
    public XSegment(SegmentPath NewPath, int NewPos)
    {
        super(NewPath, NewPos, 0);
    }

    public String toString()
    {
        return "X" + super.toString();
    }

    // todo: (fh) nowhere used
    public void checkIntegrity()
    {
        if (getPrevious().isFirst())
        {
           point.x = getPrevious().getXPos();
        }

        if (getNext().isLast())
        {
           point.x = getNext().getXPos();
        }
    }

    // todo: (fh) nowhere used !
    public void checkMinLength()
    {
        if (getPrevious().isFirst())
        {
            int Y1 = getPrevious().getYPos();
            int Y2 = getNext().getYPos();

            if (Math.abs(Y1 - Y2) < 20)
            {
                YSegment NewYLine = new YSegment(path, Y2);
                XSegment NewXLine = new XSegment(path, (point.x + getNext().getNext().getXPos()) / 2);

                NewXLine.setNext(Next);
                NewYLine.setNext(NewXLine);
                setNext(NewYLine);
            }
        }

        if (getNext().isLast())
        {
            int Y1 = getPrevious().getYPos();
            int Y2 = getNext().getYPos();

            if (Math.abs(Y1 - Y2) < 20)
            {
                YSegment NewYLine = new YSegment(path,
                                                 Y1);
                XSegment NewXLine = new XSegment(path,
                                                 (point.x + getPrevious().getPrevious().getXPos()) / 2);

                NewXLine.setPrevious(Prev);
                NewYLine.setPrevious(NewXLine);
                setPrevious(NewYLine);
            }
        }
    }

    public void checkRedundancy()
    {
        Segment NewSeg;

        if (!getNext().isLast())
        {
            if (getNext().getNext().isLast())
            {
                getNext().getNext().moveTo(point.x, getNext().getNext().getYPos());
            }

            int X = getNext().getNext().getXPos();
            int dX = (X > point.x ? X - point.x : point.x - X);

            if (dX < SegmentPath.EPS)
            {
                if (!getNext().getNext().isLast())
                {
                    NewSeg = getNext().getNext().getNext();
                }
                else // Next.Next is last == Next.Next is Point
                {
                    NewSeg = getNext().getNext();
                }

                setNext(NewSeg);

                if (getNext().isLast())
                {
                    getNext().moveTo(getNext().getXPos(), getPrevious().getYPos());
                }
            }
        }

        if (!getPrevious().isFirst())
        {
            if (getPrevious().getPrevious().isFirst())
            {
                getPrevious().getPrevious().moveTo(point.x, getPrevious().getPrevious().getYPos());
            }

            int X = getPrevious().getPrevious().getXPos();
            int dX = (X > point.x ? X - point.x : point.x - X);

            if (dX < SegmentPath.EPS)
            {
                if (!getPrevious().getPrevious().isFirst())
                {
                    NewSeg = getPrevious().getPrevious().getPrevious();
                }
                else // Prev.Prev is first == Prev.Prev is Point
                {
                    NewSeg = getPrevious().getPrevious();
                }

                setPrevious(NewSeg);

                if (getPrevious().isFirst())
                {
                    getPrevious().moveTo(getPrevious().getXPos(), getNext().getYPos());
                }
            }
        }
    }

    public void computeSize(IntegerWrapper MinX, IntegerWrapper MinY,
                            IntegerWrapper MaxX, IntegerWrapper MaxY)
    {
        if (point.x > MaxX.intValue())
            MaxX.setValue(point.x);
        if (point.x < MinX.intValue())
            MinX.setValue(point.x);
    }

    public void draw(Graphics graphics)
    {
        int previousRadius = Math.min(10, Math.min(getLength(), getPrevious().getLength()) / 2);
        int nextRadius = Math.min(10, Math.min(getLength(), getNext().getLength()) / 2);

        if (getPrevious().isFirst())
        {
            graphics.drawLine(getXPos(),
                              getPrevious().getYPos(),
                              getXPos(),
                              getNext().getYPos() - getDirection() * nextRadius);
            graphics.drawArc(getXPos() + getNext().getDirection() * nextRadius - nextRadius,
                             getNext().getYPos() - getDirection() * nextRadius - nextRadius,
                             2 * nextRadius,
                             2 * nextRadius,
                             90 - 90 * getDirection() * getNext().getDirection() ,
                             45 * getDirection() * getNext().getDirection());
        }
        else if (getNext().isLast())
        {
            graphics.drawLine(getXPos(),
                              getPrevious().getYPos() + getDirection() * previousRadius,
                              getXPos(),
                              getNext().getYPos());
            graphics.drawArc(getXPos() - getPrevious().getDirection() * previousRadius - previousRadius,
                             getPrevious().getYPos() + getDirection() * previousRadius - previousRadius,
                             2 * previousRadius,
                             2 * previousRadius,
                             90 - 90 /** getDirection()*/ * getPrevious().getDirection(),
                             45 * getDirection() * getPrevious().getDirection());
        }
        else
        {
            graphics.drawLine(getXPos(),
                              getPrevious().getYPos() + getDirection() * previousRadius,
                              getXPos(),
                              getNext().getYPos() - getDirection() * nextRadius);
            graphics.drawArc(getXPos() - getPrevious().getDirection() * previousRadius - previousRadius,
                             getPrevious().getYPos() + getDirection() * previousRadius - previousRadius,
                             2 * previousRadius,
                             2 * previousRadius,
                             90 - 90 * getPrevious().getDirection(),
                             45 * getDirection() * getPrevious().getDirection());
            graphics.drawArc(getXPos() + getNext().getDirection() * nextRadius - nextRadius,
                             getNext().getYPos() - getDirection() * nextRadius - nextRadius,
                             2 * nextRadius,
                             2 * nextRadius,
                             90 + 90 * getNext().getDirection(),
                             45 * getDirection() * getNext().getDirection());
        }
    }

    public int getDimension()
    {
        return SpatialDimension.X;
    }

    public Segment getMaxSeg()
    {
        if (getNext().getYPos() > getPrevious().getYPos())
        {
            return getNext();
        }
        else
        {
            return getPrevious();
        }
    }

    public int getMaxXPos()
    {
        return getXPos();
    }

    public int getMaxYPos()
    {
        return Math.max(getNext().getYPos(), getPrevious().getYPos());
    }

    public Segment getMinSeg()
    {
        if (getNext().getYPos() < getPrevious().getYPos())
        {
            return getNext();
        }
        else
        {
            return getPrevious();
        }
    }

    public int getMinXPos()
    {
        return getXPos();
    }

    public int getMinYPos()
    {
        return Math.min(getNext().getYPos(), getPrevious().getYPos());
    }

    public int getXPos()
    {
        return point.x;
    }

    public int getYPos()
    {
        return point.y;
    }

    public boolean isHitBy(int x, int y)
    {
        if (GeometryHelper.isCloseToLine(x, y, point.x, getPrevious().getYPos(), point.x, getNext().getYPos()))
        {
            return true;
        }

        return false;
    }

    public void moveRel(int xShift, int yShift)
    {
        if (getPrevious().isFirst())
        {
            IntegerWrapper xPrevious = new IntegerWrapper(getPrevious().getXPos() + xShift);
            IntegerWrapper yPrevious = new IntegerWrapper(getPrevious().getYPos());

            // Determine adjusted drag

            if (path.validLeftDrag(xPrevious, yPrevious))
            {
                getPrevious().moveTo(xPrevious.intValue(), yPrevious.intValue());
                setXPos(xPrevious.intValue());
            }
        }
        else if (getNext().isLast())
        {
            IntegerWrapper xNext = new IntegerWrapper(getNext().getXPos() + xShift);
            IntegerWrapper yNext = new IntegerWrapper(getNext().getYPos());

            // Determine adjusted drag

            if (path.validLeftDrag(xNext, yNext))
            {
                getNext().moveTo(xNext.intValue(), yNext.intValue());
                setXPos(xNext.intValue());
            }
        }
        else
        {
            translate(xShift, 0);
        }
    }

    /**
     *
     * @return 1 if the segment has positive direction,
     *         -1 if the segment has negative direction
     *         0 if the segment has not direction (end segment)
     */
    public int getDirection()
    {
        if (getNext().getYPos() > getPrevious().getYPos())
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }

    /**
     *
     * @return Length of the segment.
     */
    public int getLength()
    {
        return Math.abs(getNext().getYPos() - getPrevious().getYPos());
    }
}
