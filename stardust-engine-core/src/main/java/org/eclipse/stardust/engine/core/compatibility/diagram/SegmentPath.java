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
import java.io.Serializable;
import java.util.ArrayList;

/** */
public abstract class SegmentPath implements Serializable
{
   public static final int EPS = 1;
   public static final int MIN_SIZE = 10;
   public static final int SENS = 3;

   EndSegment leftEnd;
   EndSegment rightEnd;
   private int left;
   private int bottom;
   private int right;
   private int top;

   public SegmentPath()
   {
   }

   /**
    * Database load constructor.
    */
   public SegmentPath(int[] points, int pointsCount)
   {
      leftEnd = new EndSegment(this, points[1], points[2]);
      rightEnd = new EndSegment(this, points[pointsCount - 2], points[pointsCount - 1]);

      Segment segment = leftEnd;

      for (int n = 3; n < pointsCount - 2; ++n)
      {
         if (n % 2 == points[0])
         {
            segment.setNext(new XSegment(this, points[n]));
         }
         else
         {
            segment.setNext(new YSegment(this, points[n]));
         }

         segment = segment.getNext();
      }

      segment.setNext(rightEnd);
   }

   // Copy constructor.
   public SegmentPath(SegmentPath CopySym)
   {
      Segment CurrSeg;

      left = CopySym.left;
      right = CopySym.right;
      top = CopySym.top;
      bottom = CopySym.bottom;

      leftEnd = new EndSegment(this, CopySym.leftEnd.getXPos(),
            CopySym.leftEnd.getYPos());
      rightEnd = new EndSegment(this, CopySym.rightEnd.getXPos(),
            CopySym.rightEnd.getYPos());

      // Copy path segments

      Segment saveLoadItr = CopySym.leftEnd.getNext();
      CurrSeg = leftEnd;

      while (!saveLoadItr.isLast())
      {
         if (saveLoadItr.getDimension() == SpatialDimension.X)
         {
            CurrSeg.setNext(new XSegment(this,
                  saveLoadItr.getXPos()));
         }
         else
         {
            CurrSeg.setNext(new YSegment(this,
                  saveLoadItr.getYPos()));
         }

         CurrSeg = CurrSeg.getNext();
         saveLoadItr = saveLoadItr.getNext();
      }

      CurrSeg.setNext(rightEnd);
   }

   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append('{');
      Segment seg = leftEnd;
      while (seg != null)
      {
         sb.append(seg);
         if (!seg.isLast())
         {
            sb.append(',');
         }
         seg = seg.getNext();
      }
      sb.append('}');
      return sb.toString();
   }

   public int bottom()
   {
      computeSize();

      return bottom;
   }

   // change left and right side of the path
   public void changeDirection()
   {
      EndSegment LE = leftEnd;
      EndSegment RE = rightEnd;

      Segment S1 = LE;
      Segment S2 = S1.getNext();
      Segment S3 = S2.getNext();

      S1.setNext(null);

      while (S3 != null)
      {
         S2.setNext(S1);

         S1 = S2;
         S2 = S3;
         S3 = S3.getNext();
      }

      S2.setNext(S1);
      S2.setPrevious(null);

      leftEnd = RE;
      rightEnd = LE;
   }

   private void computeSize()
   {
      IntegerWrapper tempRight = new IntegerWrapper(leftEnd.getXPos());
      IntegerWrapper tempLeft = new IntegerWrapper(rightEnd.getXPos());
      IntegerWrapper tempTop = new IntegerWrapper(
            Math.min(leftEnd.getYPos(), rightEnd.getYPos()));
      IntegerWrapper tempBottom = new IntegerWrapper(leftEnd.getYPos());

      Segment segment = leftEnd;

      do
      {
         segment.computeSize(tempLeft, tempBottom, tempRight, tempTop);
      }
      while ((segment = segment.getNext()) != null);

      right = tempRight.intValue();
      left = tempLeft.intValue();
      bottom = tempBottom.intValue();
      top = tempTop.intValue();

   }

   /**
    * Drags a segment hit by (xOld, yOld) to (xNew, yNew)
    */
   public boolean dragSegment(int xOld, int yOld, int xNew, int yNew)
   {
      Segment segment = leftEnd;

      do
      {
         if (segment.isHitBy(xOld, yOld))
         {
            segment.moveRel(xNew - xOld, yNew - yOld);

            return false;
         }
      }
      while ((segment = segment.getNext()) != null);
      return false;
   }

   void draw(Graphics graphics)
   {
      Segment segment = leftEnd;

      do
      {
         segment.draw(graphics);
      }
      while ((segment = segment.getNext()) != null);
   }

   // Returns the X and Y starting points of the left end segment as well as
   // the orientation of the segment.
   public void getLeftExit(IntegerWrapper X, IntegerWrapper Y, IntegerWrapper Orient)
   {
      X.setValue(leftEnd.getXPos());
      Y.setValue(leftEnd.getYPos());

      if (SpatialDimension.Y == leftEnd.getNext().getDimension())
      {
         // Constants.HORIZONTAL orientation

         if (leftEnd.getNext().getNext().getXPos() > X.intValue())
         {
            Orient.setValue(Heading.EAST);

            return;
         }
         else
         {
            Orient.setValue(Heading.WEST);

            return;
         }
      }
      else
      {
         // Constants.VERTICAL orientation

         if (leftEnd.getNext().getNext().getYPos() > Y.intValue())
         {
            Orient.setValue(Heading.NORTH);

            return;
         }
         else
         {
            Orient.setValue(Heading.SOUTH);

            return;
         }
      }
   }

   /**
    * @return
    */
   public Point getMiddlePoint()
   {
      Segment middleSegment = getMiddleSeg();

      if (middleSegment instanceof XSegment)
      {
         return new Point(middleSegment.getXPos(),
               (middleSegment.getMinYPos() + middleSegment.getMaxYPos()) / 2);
      }
      else
      {
         return new Point((middleSegment.getMinXPos() + middleSegment.getMaxXPos()) / 2,
               middleSegment.getYPos());
      }
   }

   private Segment getMiddleSeg()
   {
      Segment TmpSeg;
      int SegCnt;

      SegCnt = 1;
      TmpSeg = leftEnd;

      // Count segments

      while ((TmpSeg = TmpSeg.getNext()) != null)
      {
         SegCnt++;
      }

      TmpSeg = leftEnd;

      for (int N = 1; N <= SegCnt / 2; N++)
      {
         TmpSeg = TmpSeg.getNext();
      }

      return TmpSeg;
   }

   // Returns the X and Y starting points of the right end segment as well as
   // the orientation of the segment.
   public void getRightExit(IntegerWrapper X, IntegerWrapper Y, IntegerWrapper Orient)
   {
      X.setValue(rightEnd.getXPos());
      Y.setValue(rightEnd.getYPos());

      if (SpatialDimension.Y == rightEnd.getPrevious().getDimension())
      {
         // Constants.HORIZONTAL orientation

         if (rightEnd.getPrevious().getPrevious().getXPos() > X.intValue())
         {
            Orient.setValue(Heading.EAST);

            return;
         }
         else
         {
            Orient.setValue(Heading.WEST);

            return;
         }
      }
      else
      {
         // Constants.VERTICAL orientation

         if (rightEnd.getPrevious().getPrevious().getYPos() > Y.intValue())
         {
            Orient.setValue(Heading.NORTH);

            return;
         }
         else
         {
            Orient.setValue(Heading.SOUTH);

            return;
         }
      }
   }

   public Segment getSegByNum(int SegNum)
   {
      Segment TmpSeg;
      int SegCnt;

      SegCnt = 1;
      TmpSeg = leftEnd;

      if (SegCnt == SegNum)
      {
         return TmpSeg;
      }

      while ((TmpSeg = TmpSeg.getNext()) != null)
      {
         SegCnt++;

         if (SegCnt == SegNum)
         {
            return TmpSeg;
         }
      }

      return null;
   }

   public int getSegNum(Segment NumSeg)
   {
      Segment TmpSeg;
      int SegCnt;

      SegCnt = 1;
      TmpSeg = leftEnd;

      if (TmpSeg == NumSeg)
      {
         return SegCnt;
      }

      while ((TmpSeg = TmpSeg.getNext()) != null)
      {
         SegCnt++;

         if (TmpSeg == NumSeg)
         {
            return SegCnt;
         }
      }

      return 0;
   }

   public boolean isHitBy(int x, int y)
   {
      Segment segment = leftEnd;

      if (segment != null)
      {
         do
         {
            if (segment.isHitBy(x, y))
            {
               return true;
            }
         }
         while ((segment = segment.getNext()) != null);
      }

      return false;
   }

   public int left()
   {
      computeSize();

      return left;
   }

   public int right()
   {
      computeSize();

      return right;
   }

   /**
    * @return all coordinates representing the path.
    */
   public java.util.List getPoints()
   {
      ArrayList points = new ArrayList();
      Segment segment = leftEnd;

      points.add(new Integer(segment.getXPos()));
      points.add(new Integer(segment.getYPos()));

      do
      {
         segment = segment.getNext();

         if (segment.getDimension() == SpatialDimension.X)
         {
            points.add(new Integer(segment.getXPos()));
         }
         else
         {
            points.add(new Integer(segment.getYPos()));
         }

      }
      while (!segment.getNext().isLast());

      points.add(new Integer(rightEnd.getXPos()));
      points.add(new Integer(rightEnd.getYPos()));

      return points;
   }

   public void setPoints(java.util.List points)
   {
      leftEnd =
            new EndSegment(this, ((Integer) points.get(0)).intValue(),
                  ((Integer) points.get(1)).intValue());

      Segment segment1 = leftEnd;
      Segment segment2 = null;

      for (int n = 2; n < points.size() - 2; ++n)
      {
         if (n % 2 == 0)
         {
            segment2 = new YSegment(this, ((Integer) points.get(n)).intValue());
         }
         else
         {
            segment2 = new XSegment(this, ((Integer) points.get(n)).intValue());
         }

         segment1.setNext(segment2);

         segment1 = segment2;
      }

      rightEnd =
            new EndSegment(this, ((Integer) points.get(points.size() - 2)).intValue(),
                  ((Integer) points.get(points.size() - 1)).intValue());

      segment1.setNext(rightEnd);
   }

   public void startRouting(int XLeftPos, int YLeftPos, int LeftOrient,
         int XRightPos, int YRightPos, int RightOrient)
   {
      right = 0;
      left = 0;
      top = 0;
      bottom = 0;

      leftEnd = new EndSegment(this, XLeftPos, YLeftPos);
      rightEnd = new EndSegment(this, XRightPos, YRightPos);

      switch (LeftOrient)
      {
         case Heading.NORTH:
            {
               switch (RightOrient)
               {
                  case Heading.NORTH:
                     {
                        startRoutingNN(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.EAST:
                     {
                        startRoutingNE(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.SOUTH:
                     {
                        startRoutingNS(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.WEST:
                  default:
                     {
                        startRoutingNW(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
               }
               break;
            }
         case Heading.EAST:
            {
               switch (RightOrient)
               {
                  case Heading.NORTH:
                     {
                        startRoutingEN(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.EAST:
                     {
                        startRoutingEE(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.SOUTH:
                     {
                        startRoutingES(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.WEST:
                  default:
                     {
                        startRoutingEW(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
               }
               break;
            }
         case Heading.SOUTH:
            {
               switch (RightOrient)
               {
                  case Heading.NORTH:
                     {
                        startRoutingSN(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.EAST:
                     {
                        startRoutingSE(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.SOUTH:
                     {
                        startRoutingSS(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.WEST:
                  default    :
                     {
                        startRoutingSW(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
               }
               break;
            }
         case Heading.WEST:
         default:
            {
               switch (RightOrient)
               {
                  case Heading.NORTH:
                     {
                        startRoutingWN(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.EAST:
                     {
                        startRoutingWE(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.SOUTH:
                     {
                        startRoutingWS(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
                  case Heading.WEST:
                  default:
                     {
                        startRoutingWW(XLeftPos, YLeftPos, XRightPos, YRightPos);
                        break;
                     }
               }
               break;
            }
      }
   }

   private void startRoutingEE(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new YSegment(this, YLeftPos);
      Segment LastSeg = new YSegment(this, YRightPos);
      Segment TmpSeg = new XSegment(this, Math.max(XLeftPos, XRightPos) + Segment.MIN_LENGTH);

      leftEnd.setNext(FirstSeg);
      FirstSeg.setNext(TmpSeg);
      TmpSeg.setNext(LastSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingEN(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new YSegment(this, YLeftPos);
      Segment LastSeg = new XSegment(this, XRightPos);

      if (XRightPos > XLeftPos && YLeftPos > YRightPos)
      {
         FirstSeg.setNext(LastSeg);
      }
      else
      {
         Segment TmpSeg1 = new XSegment(this, XLeftPos + Segment.MIN_LENGTH);
         Segment TmpSeg2 = new YSegment(this, YRightPos + Segment.MIN_LENGTH);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingES(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new YSegment(this, YLeftPos);
      Segment LastSeg = new XSegment(this, XRightPos);

      if (XRightPos > XLeftPos && YLeftPos < YRightPos)
      {
         FirstSeg.setNext(LastSeg);
      }
      else
      {
         Segment TmpSeg1 = new XSegment(this, XLeftPos + Segment.MIN_LENGTH);
         Segment TmpSeg2 = new YSegment(this, YRightPos - Segment.MIN_LENGTH);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingEW(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new YSegment(this, YLeftPos);
      Segment LastSeg;
      Segment TmpSeg1;
      Segment TmpSeg2;
      Segment TmpSeg3;

      if (XLeftPos < XRightPos)
      {
         TmpSeg1 = new XSegment(this, (XLeftPos + XRightPos) / 2);
         LastSeg = new YSegment(this, YRightPos);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(LastSeg);
      }
      else
      {
         IntegerWrapper XLeftTest = new IntegerWrapper(XLeftPos);
         IntegerWrapper YLeftTest = new IntegerWrapper((YLeftPos + YRightPos) / 2);
         IntegerWrapper XRightTest = new IntegerWrapper(XRightPos);
         IntegerWrapper YRightTest = new IntegerWrapper((YLeftPos + YRightPos) / 2);

         TmpSeg1 = new XSegment(this, XLeftPos + Segment.MIN_LENGTH);

         if (!validLeftDrag(XLeftTest, YLeftTest) &&
               !validRightDrag(XRightTest, YRightTest))
         {
            //TmpSeg2 = new YSegment(this, (YLeftPos + YRightPos) / 2);
            TmpSeg2 = new YSegment(this, Math.max(YLeftPos, YRightPos) + 30);
         }
         else
         {
            YLeftTest = new IntegerWrapper(0);
            YRightTest = new IntegerWrapper(0);

            validLeftDrag(XLeftTest, YLeftTest);
            validRightDrag(XRightTest, YRightTest);

            TmpSeg2 = new YSegment(this,
                  Math.min(YLeftTest.intValue(),
                        YRightTest.intValue()) - Segment.MIN_LENGTH);
         }

         TmpSeg3 = new XSegment(this, XRightPos - Segment.MIN_LENGTH);
         LastSeg = new YSegment(this, YRightPos);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(TmpSeg3);
         TmpSeg3.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingNE(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new XSegment(this, XLeftPos);
      Segment LastSeg = new YSegment(this, YRightPos);

      if (YRightPos > YLeftPos && XLeftPos > XRightPos)
      {
         FirstSeg.setNext(LastSeg);
      }
      else
      {
         Segment TmpSeg1 = new YSegment(this, YLeftPos + Segment.MIN_LENGTH);
         Segment TmpSeg2 = new XSegment(this, XRightPos + Segment.MIN_LENGTH);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingNN(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new XSegment(this, XLeftPos);
      Segment LastSeg = new XSegment(this, XRightPos);
      Segment TmpSeg = new YSegment(this, Math.max(YLeftPos, YRightPos) + Segment.MIN_LENGTH);

      leftEnd.setNext(FirstSeg);
      FirstSeg.setNext(TmpSeg);
      TmpSeg.setNext(LastSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingNS(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new XSegment(this, XLeftPos);
      Segment LastSeg;

      Segment TmpSeg1;
      Segment TmpSeg2;
      Segment TmpSeg3;

      if (YLeftPos < YRightPos)
      {
         TmpSeg1 = new YSegment(this, (YLeftPos + YRightPos) / 2);
         LastSeg = new XSegment(this, XRightPos);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(LastSeg);
      }
      else
      {
         IntegerWrapper XLeftTest = new IntegerWrapper((XLeftPos + XRightPos) / 2);
         IntegerWrapper YLeftTest = new IntegerWrapper(YLeftPos);
         IntegerWrapper XRightTest = new IntegerWrapper((XLeftPos + XRightPos) / 2);
         IntegerWrapper YRightTest = new IntegerWrapper(YRightPos);

         TmpSeg1 = new YSegment(this, YLeftPos + Segment.MIN_LENGTH);

         if (!validLeftDrag(XLeftTest, YLeftTest) &&
               !validRightDrag(XRightTest, YRightTest))
         {
            TmpSeg2 = new XSegment(this, (XLeftPos + XRightPos) / 2);
         }
         else
         {
            XLeftTest = new IntegerWrapper(0);
            XRightTest = new IntegerWrapper(0);

            validLeftDrag(XLeftTest, YLeftTest);
            validRightDrag(XRightTest, YRightTest);

            TmpSeg2 = new XSegment(this, Math.min(XLeftTest.intValue(),
                  XRightTest.intValue()) - Segment.MIN_LENGTH);
         }

         TmpSeg3 = new YSegment(this, YRightPos - Segment.MIN_LENGTH);
         LastSeg = new XSegment(this, XRightPos);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(TmpSeg3);
         TmpSeg3.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingNW(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg;
      Segment LastSeg;

      FirstSeg = new XSegment(this, XLeftPos);

      Segment TmpSeg1;
      Segment TmpSeg2;

      IntegerWrapper XLeftTest = new IntegerWrapper(XRightPos - Segment.MIN_LENGTH);
      IntegerWrapper YLeftTest = new IntegerWrapper(YLeftPos);
      IntegerWrapper XRightTest = new IntegerWrapper(XRightPos);
      IntegerWrapper YRightTest = new IntegerWrapper(YLeftPos + Segment.MIN_LENGTH);

      if (!validRightDrag(XRightTest, YRightTest))
      {
         TmpSeg1 = new YSegment(this, YLeftPos + Segment.MIN_LENGTH);
      }
      else
      {
         validRightDrag(XRightTest, YRightTest);

         TmpSeg1 = new YSegment(this, YRightTest.intValue() + Segment.MIN_LENGTH);

      }

      if (!validLeftDrag(XLeftTest, YLeftTest))
      {
         TmpSeg2 = new XSegment(this, XRightPos - Segment.MIN_LENGTH);
      }
      else
      {
         validLeftDrag(XLeftTest, YLeftTest);

         TmpSeg2 = new XSegment(this, XRightTest.intValue() - Segment.MIN_LENGTH);
      }

      LastSeg = new YSegment(this, YRightPos);

      FirstSeg.setNext(TmpSeg1);
      TmpSeg1.setNext(TmpSeg2);
      TmpSeg2.setNext(LastSeg);

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingSE(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new XSegment(this, XLeftPos);
      Segment LastSeg = new YSegment(this, YRightPos);

      if (YRightPos < YLeftPos && XLeftPos > XRightPos)
      {
         FirstSeg.setNext(LastSeg);
      }
      else
      {
         Segment TmpSeg1 = new YSegment(this, YLeftPos - Segment.MIN_LENGTH);
         Segment TmpSeg2 = new XSegment(this, XRightPos + Segment.MIN_LENGTH);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingSN(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new XSegment(this, XLeftPos);
      Segment LastSeg;

      Segment TmpSeg1;
      Segment TmpSeg2;
      Segment TmpSeg3;

      if (YLeftPos > YRightPos)
      {
         TmpSeg1 = new YSegment(this, (YLeftPos + YRightPos) / 2);
         LastSeg = new XSegment(this, XRightPos);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(LastSeg);
      }
      else
      {
         IntegerWrapper XLeftTest = new IntegerWrapper((XLeftPos + XRightPos) / 2);
         IntegerWrapper YLeftTest = new IntegerWrapper(YLeftPos);
         IntegerWrapper XRightTest = new IntegerWrapper((XLeftPos + XRightPos) / 2);
         IntegerWrapper YRightTest = new IntegerWrapper(YRightPos);

         TmpSeg1 = new YSegment(this, YLeftPos - Segment.MIN_LENGTH);

         if (!validLeftDrag(XLeftTest, YLeftTest) &&
               !validRightDrag(XRightTest, YRightTest))
         {
            TmpSeg2 = new XSegment(this, (XLeftPos + XRightPos) / 2);
         }
         else
         {
            XLeftTest = new IntegerWrapper(0);
            XRightTest = new IntegerWrapper(0);

            validLeftDrag(XLeftTest, YLeftTest);
            validRightDrag(XRightTest, YRightTest);

            TmpSeg2 = new XSegment(this, Math.min(XLeftTest.intValue(),
                  XRightTest.intValue()) - Segment.MIN_LENGTH);
         }

         TmpSeg3 = new YSegment(this, YRightPos + Segment.MIN_LENGTH);
         LastSeg = new XSegment(this, XRightPos);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(TmpSeg3);
         TmpSeg3.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingSS(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new XSegment(this, XLeftPos);
      Segment LastSeg = new XSegment(this, XRightPos);
      Segment TmpSeg = new YSegment(this, Math.min(YLeftPos, YRightPos) - Segment.MIN_LENGTH);

      FirstSeg.setNext(TmpSeg);
      TmpSeg.setNext(LastSeg);
      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingSW(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new XSegment(this, XLeftPos);
      Segment LastSeg = new YSegment(this, YRightPos);

      if (YRightPos < YLeftPos &&
            XLeftPos < XRightPos)
      {
         FirstSeg.setNext(LastSeg);
      }
      else
      {
         Segment TmpSeg1 = new YSegment(this, YLeftPos - Segment.MIN_LENGTH);
         Segment TmpSeg2 = new XSegment(this, XRightPos - Segment.MIN_LENGTH);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingWE(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new YSegment(this, YLeftPos);
      Segment LastSeg;

      Segment TmpSeg1;
      Segment TmpSeg2;
      Segment TmpSeg3;

      if (XLeftPos > XRightPos)
      {
         TmpSeg1 = new XSegment(this, (XLeftPos + XRightPos) / 2);
         LastSeg = new YSegment(this, YRightPos);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(LastSeg);
      }
      else
      {
         IntegerWrapper XLeftTest = new IntegerWrapper(XLeftPos);
         IntegerWrapper YLeftTest = new IntegerWrapper((YLeftPos + YRightPos) / 2);
         IntegerWrapper XRightTest = new IntegerWrapper(XRightPos);
         IntegerWrapper YRightTest = new IntegerWrapper((YLeftPos + YRightPos) / 2);

         TmpSeg1 = new XSegment(this,
               XLeftPos - Segment.MIN_LENGTH);

         if (!validLeftDrag(XLeftTest, YLeftTest) &&
               !validRightDrag(XRightTest, YRightTest))
         {
            TmpSeg2 = new YSegment(this, (YLeftPos + YRightPos) / 2);
         }
         else
         {
            YLeftTest = new IntegerWrapper(0);
            YRightTest = new IntegerWrapper(0);

            validLeftDrag(XLeftTest, YLeftTest);
            validRightDrag(XRightTest, YRightTest);

            TmpSeg2 =
                  new YSegment(this,
                        Math.min(YLeftTest.intValue(), YRightTest.intValue())
                  - Segment.MIN_LENGTH);
         }

         TmpSeg3 = new XSegment(this, XRightPos + Segment.MIN_LENGTH);
         LastSeg = new YSegment(this, YRightPos);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(TmpSeg3);
         TmpSeg3.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingWN(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new YSegment(this, YLeftPos);
      Segment LastSeg = new XSegment(this, XRightPos);

      if (XRightPos < XLeftPos && YLeftPos > YRightPos)
      {
         FirstSeg.setNext(LastSeg);
      }
      else
      {
         Segment TmpSeg1 = new XSegment(this, XLeftPos - Segment.MIN_LENGTH);
         Segment TmpSeg2 = new YSegment(this, YRightPos + Segment.MIN_LENGTH);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingWS(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new YSegment(this, YLeftPos);
      Segment LastSeg = new XSegment(this, XRightPos);

      if (XRightPos < XLeftPos && YLeftPos < YRightPos)
      {
         FirstSeg.setNext(LastSeg);
      }
      else
      {
         Segment TmpSeg1 = new XSegment(this, XLeftPos - Segment.MIN_LENGTH);
         Segment TmpSeg2 = new YSegment(this, YRightPos - Segment.MIN_LENGTH);

         FirstSeg.setNext(TmpSeg1);
         TmpSeg1.setNext(TmpSeg2);
         TmpSeg2.setNext(LastSeg);
      }

      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   private void startRoutingWW(int XLeftPos, int YLeftPos, int XRightPos, int YRightPos)
   {
      Segment FirstSeg = new YSegment(this, YLeftPos);
      Segment LastSeg = new YSegment(this, YRightPos);
      Segment TmpSeg = new XSegment(this, Math.min(XLeftPos, XRightPos) - Segment.MIN_LENGTH);

      FirstSeg.setNext(TmpSeg);
      TmpSeg.setNext(LastSeg);
      leftEnd.setNext(FirstSeg);
      LastSeg.setNext(rightEnd);
   }

   public int top()
   {
      computeSize();

      return top;
   }

   /* Performs a translation for all points of the segment path. The relative structure
      of the segments is not changed. */
   void translate(int xDelta, int yDelta)
   {
      Segment segment = leftEnd;

      do
      {
         segment.translate(xDelta, yDelta);
      }
      while ((segment = segment.getNext()) != null);
   }

   public abstract boolean validLeftDrag(IntegerWrapper xWrapper,
         IntegerWrapper yWrapper);

   public abstract boolean validRightDrag(IntegerWrapper xWrapper,
         IntegerWrapper yWrapper);
}
