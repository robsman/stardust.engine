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
import java.awt.event.MouseEvent;

/**
 * Connects two rectangular shaped symbols with a segment path.
 */
public abstract class PathConnection extends AbstractConnectionSymbol
{
   private PointPointSegmentPath path;

   /**
    *
    */
   public PathConnection()
   {
      path = new PointPointSegmentPath();
   }

   /**
    *
    */
   public void draw(Graphics g)
   {
      Graphics2D graphics = (Graphics2D) g;
      Stroke oldStroke = graphics.getStroke();

      if (getSelected())
      {
         graphics.setStroke(selectedStroke);
      }

      getPath().draw(graphics);

      graphics.setStroke(oldStroke);
   }

   /**
    *
    */
   public int getHeight()
   {
      if (path != null)
      {
         return Math.abs(path.top() - path.bottom());
      }
      else
      {
         return 0;
      }
   }

   /**
    *
    */
   public SegmentPath getPath()
   {
      return path;
   }

   /**
    *
    */
   public int getWidth()
   {
      if (path != null)
      {
         return Math.abs(path.right() - path.left());
      }
      else
      {
         return 0;
      }
   }

   /** */
   public int getX()
   {
      if (path != null)
      {
         return path.left();
      }
      else
      {
         return 0;
      }
   }

   /** */
   public int getY()
   {
      if (path != null)
      {
         return path.bottom();
      }
      else
      {
         return 0;
      }
   }

   /**
    *
    */
   public boolean isHitBy(int x, int y)
   {
      return getPath().isHitBy(x, y);
   }

   /**
    * @param xDelta
    * @param yDelta
    */
   public void move(int xDelta, int yDelta)
   {
      markModified();
      getPath().translate(xDelta, yDelta);
   }

   /**
    *
    */
   public void mouseDragged(MouseEvent event, int lastXDrag, int lastYDrag)
   {
      markModified();
      if (getPath().dragSegment(lastXDrag, lastYDrag, event.getX(), event.getY()))
      {
//         reroute();
      }
   }

   /**
    *
    */
   public void reroute()
   {
      if ((getPath() != null)
            && (getFirstSymbol() != null)
            && (getSecondSymbol() != null)
      )
      {
         markModified();
         getPath().startRouting(getFirstSymbol().getRight()
               , (getFirstSymbol().getBottom() + getFirstSymbol().getTop()) / 2
               , Heading.EAST
               , getSecondSymbol().getLeft()
               , (getSecondSymbol().getBottom() + getSecondSymbol().getTop()) / 2
               , Heading.WEST);
      }
   }

   /**
    *
    */
   public void setSecondSymbol(Symbol symbol, boolean link)
   {
      super.setSecondSymbol(symbol, link);

      reroute();
   }

   /**
    *
    */
   public void symbolChanged(Symbol symbol)
   {
      if (symbol == getFirstSymbol())
      {
         if (path.stretchLeftSeg(getFirstSymbol().getRight(),
               (getFirstSymbol().getBottom() + getFirstSymbol().getTop()) / 2))
         {
            reroute();
         }
      }

      if (symbol == getSecondSymbol())
      {
         if (path.stretchRightSeg(getSecondSymbol().getLeft(),
               (getSecondSymbol().getBottom() + getSecondSymbol().getTop()) / 2))
         {
            reroute();
         }
      }
   }
}
