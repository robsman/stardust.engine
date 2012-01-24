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

/**
 *
 */
public class PointPointSegmentPath extends SegmentPath
{
    int leftX;
    int leftY;
    int rightX;
    int rightY;

    /**
     * Database load constructor.
     */
    public PointPointSegmentPath(int[] points, int pointsCount)
    {
        super(points, pointsCount);
    }

    public PointPointSegmentPath()
    {
        super();
    }

    // Copy constructor.
    public PointPointSegmentPath(PointPointSegmentPath CopyPath)
    {
        super(CopyPath);
    }

    public void startRouting(int XLeftPos, int YLeftPos, int LeftOrient,
                             int XRightPos, int YRightPos, int RightOrient)
    {
        leftX = XLeftPos;
        leftY = YLeftPos;
        rightX = XRightPos;
        rightY = YRightPos;

        super.startRouting(XLeftPos, YLeftPos, LeftOrient, XRightPos, YRightPos, RightOrient);
    }

    public boolean validLeftDrag(IntegerWrapper x, IntegerWrapper y)
    {
        x.setValue(leftX);
        y.setValue(leftY);

        return false;
    }

    public boolean validRightDrag(IntegerWrapper x, IntegerWrapper y)
    {
        x.setValue(rightX);
        y.setValue(rightY);

        return false;
    }

   public boolean dragSegment(int xOld, int yOld, int xNew, int yNew)
   {
      super.dragSegment(xOld, yOld, xNew, yNew);
      return needsRerouting();
   }

    /**
     * Shifts the starting point of the left end segment of the segment path
     * without changing any other point of the path.
     */
    public boolean stretchLeftSeg(int x, int y)
    {
        leftX = x;
        leftY = y;

        leftEnd.moveRel(x - leftEnd.getXPos(), y - leftEnd.getYPos());
//        leftEnd.checkRedundancy();
        return needsRerouting();
    }

    private boolean needsRerouting()
    {
        return leftEnd.getNext().getDirection() < 0 ||
               rightEnd.getPrevious().getDirection() < 0;
    }

   /**
     * Shifts the starting point of the left end segment of the segment path
     * without changing any other point of the path.
     */
    public boolean stretchRightSeg(int x, int y)
    {
        rightX = x;
        rightY = y;

        rightEnd.moveRel(x - rightEnd.getXPos(), y - rightEnd.getYPos());
//        rightEnd.checkRedundancy();
        return needsRerouting();
    }
}
