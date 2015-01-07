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
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Color;

/**
 */
public abstract class LineConnection extends AbstractConnectionSymbol
{
    public static final int STRAIGHT_LINE_TYPE = 0;
    public static final int MANHATTAN_LINE_TYPE = 1;

    public static final ArrowKey DEFAULT_ARROW = ArrowKey.FILLED_TRIANGLE;

    private int type;
    private int firstArrowKey;
    private int secondArrowKey;

    private int x = 0;
    private int y = 0;
    private int height = 0;
    private int width = 0;
    private Color color;

    /**
     *
     */
    public LineConnection()
    {
        this(STRAIGHT_LINE_TYPE);
    }

    /**
     *
     */
    public LineConnection(int type)
    {
        this.type = type;
        firstArrowKey = ArrowKey.NO_ARROW.getValue();
        secondArrowKey = firstArrowKey;
    }

    public void setColor(Color color)
    {
        if (this.color == color)
        {
            return;
        }
        this.color = color;
        if (getDrawArea() != null)
        {
            getDrawArea().repaint();
        }
    }

    /**
     *  calculates the position an the diagram, the width and height
     */
    protected void calculateMeasurements()
    {
        int[] line = GeometryHelper.interRectLine(getFirstSymbol().getLeft(), getFirstSymbol().getBottom(),
                                                  getFirstSymbol().getRight(), getFirstSymbol().getTop(),
                                                  getSecondSymbol().getLeft(), getSecondSymbol().getBottom(),
                                                  getSecondSymbol().getRight(), getSecondSymbol().getTop());

        // correct the start- and/or the end-point in case auf connected Connections
        if (getFirstSymbol() instanceof ConnectionSymbol)
        {
            line[0] = getFirstSymbol().getX() + (getFirstSymbol().getWidth() / 2);
            line[1] = getFirstSymbol().getY() + (getFirstSymbol().getHeight() / 2);
        }
        if (getSecondSymbol() instanceof ConnectionSymbol)
        {
            line[2] = getSecondSymbol().getX() + (getSecondSymbol().getWidth() / 2);
            line[3] = getSecondSymbol().getY() + (getSecondSymbol().getHeight() / 2);
        }

        // save the start- and the end-point
        x = Math.min(line[0], line[2]);
        y = Math.min(line[1], line[3]);
        width = Math.abs(line[2] - line[0]);
        height = Math.abs(line[3] - line[1]);
    }

    /**
     *
     */
    public void draw(Graphics g)
    {
        Graphics2D graphics = (Graphics2D) g;
        Stroke oldStroke = graphics.getStroke();

        if (color != null)
        {
            graphics.setColor(color);
        }
        int[] line = GeometryHelper.interRectLine(getFirstSymbol().getLeft(), getFirstSymbol().getBottom(),
                                                  getFirstSymbol().getRight(), getFirstSymbol().getTop(),
                                                  getSecondSymbol().getLeft(), getSecondSymbol().getBottom(),
                                                  getSecondSymbol().getRight(), getSecondSymbol().getTop());

        // correct the start- and/or the end-point in case auf connected Connections
        if (getFirstSymbol() instanceof ConnectionSymbol)
        {
            line[0] = getFirstSymbol().getX() + (getFirstSymbol().getWidth() / 2);
            line[1] = getFirstSymbol().getY() + (getFirstSymbol().getHeight() / 2);
        }
        if (getSecondSymbol() instanceof ConnectionSymbol)
        {
            line[2] = getSecondSymbol().getX() + (getSecondSymbol().getWidth() / 2);
            line[3] = getSecondSymbol().getY() + (getSecondSymbol().getHeight() / 2);
        }

        // save the start- and the end-point
        x = Math.min(line[0], line[2]);
        y = Math.min(line[1], line[3]);
        width = Math.abs(line[2] - line[0]);
        height = Math.abs(line[3] - line[1]);

        graphics.setStroke(getStroke());
        if (type == STRAIGHT_LINE_TYPE)
        {
            graphics.drawLine(line[0], line[1], line[2], line[3]);
        }
        else if (type == MANHATTAN_LINE_TYPE)
        {
            graphics.drawLine(line[0], line[1], line[2], line[1]);
            graphics.drawLine(line[2], line[1], line[2], line[3]);
        }

        graphics.setStroke(oldStroke);
        GraphicsHelper.drawArrow(graphics
                                 , line[0], line[1], getFirstArrow()
                                 , line[2], line[3], getSecondArrow());
    }

    /**
     *
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Insert the method's description here.
     * Creation date: (12.07.2000 16:23:49)
     * @return java.awt.Stroke
     */
    protected Stroke getStroke()
    {

        if (getSelected())
        {
            return selectedStroke;
        }
        else
        {
            return standardStroke;
        }
    }

    /**
     *
     */
    public int getWidth()
    {
        return width;
    }

    /** */
    public int getX()
    {
        return x;
    }

    /** */
    public int getY()
    {
        return y;
    }

    /**
     *
     */
    public boolean isHitBy(int x, int y)
    {
        try
        {
            int[] line = GeometryHelper.interRectLine(getFirstSymbol().getLeft(), getFirstSymbol().getBottom(),
                                                      getFirstSymbol().getRight(), getFirstSymbol().getTop(),
                                                      getSecondSymbol().getLeft(), getSecondSymbol().getBottom(),
                                                      getSecondSymbol().getRight(), getSecondSymbol().getTop());

            return GeometryHelper.isCloseToLine(x, y, line[0], line[1], line[2], line[3]);
        }
        catch (NullPointerException _ex)
        {
            return false;
        }
    }

    /**
     * @deprecated ... use the set-Method with ArrowKey as parameter
     */
    public void setFirstArrow(boolean arrow)
    {
        if (arrow)
        {
            firstArrowKey = DEFAULT_ARROW.getValue();
        }
        else
        {
            firstArrowKey = ArrowKey.NO_ARROW.getValue();
        }
    }

    /** */
    public void setFirstArrow(ArrowKey arrowKey)
    {
        if (arrowKey != null)
        {
            firstArrowKey = arrowKey.getValue();
        }
        else
        {
            firstArrowKey = ArrowKey.NO_ARROW.getValue();
        }
    }

    /** */
    public ArrowKey getFirstArrow()
    {
        return new ArrowKey(firstArrowKey);
    }

    /** */
    public void setSecondArrow(ArrowKey arrowKey)
    {
        if (arrowKey != null)
        {
            secondArrowKey = arrowKey.getValue();
        }
        else
        {
            secondArrowKey = ArrowKey.NO_ARROW.getValue();
        }
    }

    /** */
    public ArrowKey getSecondArrow()
    {
        return new ArrowKey(secondArrowKey);
    }

    /**
     * @deprecated ... use the set-Method with ArrowKey as parameter
     */
    public void setSecondArrow(boolean arrow)
    {
        if (arrow)
        {
            secondArrowKey = DEFAULT_ARROW.getValue();
        }
        else
        {
            secondArrowKey = ArrowKey.NO_ARROW.getValue();
        }
    }

}
