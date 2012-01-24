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

/** */
class YSegment extends Segment
{
    public YSegment(SegmentPath NewPath, int NewPos)
    {
        super(NewPath, 0, NewPos);
    }

    public String toString()
    {
        return "Y" + super.toString();
    }

    public void checkIntegrity()
    {
        if (getPrevious().isFirst())
        {
           point.y = getPrevious().getYPos();
           checkSize(this, Heading.EAST);
        }
        if (getNext().isLast())
        {
           point.y = getNext().getYPos();
           checkSize(this, Heading.WEST);
        }
    }

   private static void checkSize(YSegment segment, int direction)
   {
      if (segment.getLength() < Segment.MIN_LENGTH)
      {
         if (direction == Heading.EAST)
         {
            if (!segment.getNext().isLast())
            {
               segment.getNext().setXPos(segment.getPrevious().getXPos() + Segment.MIN_LENGTH);
               checkSize((YSegment) segment.getNext().getNext(), direction);
            }
         }
         if (direction == Heading.WEST)
         {
            if (!segment.getPrevious().isFirst())
            {
               segment.getPrevious().setXPos(segment.getNext().getXPos() - Segment.MIN_LENGTH);
               checkSize((YSegment) segment.getPrevious().getPrevious(), direction);
            }
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
                getNext().getNext().moveTo(getNext().getNext().getXPos(), point.y);
            }

            int Y = getNext().getNext().getYPos();
            int dY = (Y > point.y ? Y - point.y : point.y - Y);

            if (dY < SegmentPath.EPS)
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
                    getNext().moveTo(getPrevious().getXPos(), getNext().getYPos());
                }
            }
        }

        if (!getPrevious().isFirst())
        {
            if (getPrevious().getPrevious().isFirst())
            {
                getPrevious().getPrevious().moveTo(getPrevious().getPrevious().getXPos(), point.y);
            }

            int Y = getPrevious().getPrevious().getYPos();
            int dY = (Y > point.y ? Y - point.y : point.y - Y);

            if (dY < SegmentPath.EPS)
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
                    getPrevious().moveTo(getNext().getXPos(),
                                         getPrevious().getYPos());
                }
            }
        }
    }

    public void computeSize(IntegerWrapper MinX, IntegerWrapper MinY,
                            IntegerWrapper MaxX, IntegerWrapper MaxY)
    {
        if (point.y > MaxY.intValue())
            MaxY.setValue(point.y);
        if (point.y < MinY.intValue())
            MinY.setValue(point.y);
    }

    public void draw(Graphics graphics)
    {
        Stylesheet.instance();

        int previousRadius = Math.min(10, Math.min(getLength(), getPrevious().getLength()) / 2);
        int nextRadius = Math.min(10, Math.min(getLength(), getNext().getLength()) / 2);

        if (getPrevious().isFirst())
        {
            graphics.drawLine(getPrevious().getXPos(),
                              getYPos(),
                              getNext().getXPos() - getDirection() * nextRadius,
                              getYPos());
            graphics.drawArc(getNext().getXPos() - getDirection() * nextRadius - nextRadius,
                             getYPos() + getNext().getDirection() * nextRadius - nextRadius,
                             2 * nextRadius,
                             2 * nextRadius,
                             90 * getNext().getDirection() ,
                             - 45 * getDirection() * getNext().getDirection());
        }
        else if (getNext().isLast())
        {
            graphics.drawLine(getPrevious().getXPos() + getDirection() * previousRadius,
                              getYPos(),
                              getNext().getXPos(),
                              getYPos());
            graphics.drawArc(getPrevious().getXPos() + getDirection() * previousRadius - previousRadius,
                             getYPos() - getPrevious().getDirection() * previousRadius - previousRadius,
                             2 * previousRadius,
                             2 * previousRadius,
                             - 90 * getPrevious().getDirection(),
                             - 45 * getDirection() * getPrevious().getDirection());
        }
        else
        {
            graphics.drawLine(getPrevious().getXPos() + getDirection() * previousRadius,
                              getYPos(),
                              getNext().getXPos() - getDirection() * nextRadius,
                              getYPos());
            graphics.drawArc(getPrevious().getXPos() + getDirection() * previousRadius - previousRadius,
                             getYPos() - getPrevious().getDirection() * previousRadius - previousRadius,
                             2 * previousRadius,
                             2 * previousRadius,
                             - 90 * getPrevious().getDirection(),
                             - 45 * getDirection() * getPrevious().getDirection());
            graphics.drawArc(getNext().getXPos() - getDirection() * nextRadius - nextRadius,
                             getYPos() + getNext().getDirection() * nextRadius - nextRadius,
                             2 * nextRadius,
                             2 * nextRadius,
                             90 * getNext().getDirection() ,
                             - 45 * getDirection() * getNext().getDirection());
        }
    }

    public int getDimension()
    {
        return SpatialDimension.Y;
    }

    public Segment getMaxSeg()
    {
        if (getNext().getXPos() > getPrevious().getXPos())
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
        return Math.max(getPrevious().getXPos(), getNext().getXPos());
    }

    public int getMaxYPos()
    {
        return getYPos();
    }

    public Segment getMinSeg()
    {
        if (getNext().getXPos() < getPrevious().getXPos())
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
        return Math.min(getPrevious().getXPos(), getNext().getXPos());
    }

    public int getMinYPos()
    {
        return getYPos();
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
        if (GeometryHelper.isCloseToLine(x, y, getPrevious().getXPos(), point.y, getNext().getXPos(), point.y))
        {
            return true;
        }

        return false;
    }

    public void moveRel(int xShift, int yShift)
    {
        if (getPrevious().isFirst())
        {
            IntegerWrapper xPrevious = new IntegerWrapper(getPrevious().getXPos());
            IntegerWrapper yPrevious = new IntegerWrapper(getPrevious().getYPos() + yShift);

            // Determine adjusted drag

            if (path.validLeftDrag(xPrevious, yPrevious))
            {
                getPrevious().moveTo(xPrevious.intValue(), yPrevious.intValue());
                setYPos(yPrevious.intValue());
            }
        }
        else if (getNext().isLast())
        {
            IntegerWrapper xNext = new IntegerWrapper(getNext().getXPos());
            IntegerWrapper yNext = new IntegerWrapper(getNext().getYPos() + yShift);

            // Determine adjusted drag

            if (path.validLeftDrag(xNext, yNext))
            {
                getNext().moveTo(xNext.intValue(), yNext.intValue());
                setYPos(yNext.intValue());
            }
        }
        else
        {
            translate(0, yShift);
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
        if (getNext().getXPos() > getPrevious().getXPos())
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
        return Math.abs(getNext().getXPos() - getPrevious().getXPos());
    }
}
