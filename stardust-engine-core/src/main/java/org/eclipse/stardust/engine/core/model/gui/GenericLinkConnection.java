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
package org.eclipse.stardust.engine.core.model.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ILinkType;
import org.eclipse.stardust.engine.core.compatibility.diagram.ArrowKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.ColorKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.LineKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.NodeSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;
import org.eclipse.stardust.engine.core.model.utils.SingleRef;


/**
 * @todo Insert the type's description here.
 */
public class GenericLinkConnection extends AbstractWorkflowLineConnection
      implements ActionListener
{
   private static final Logger trace = LogManager.getLogger(GenericLinkConnection.class);
   protected static final String STRING_UNKNOWN_LINK = "Unnamed GenericLink";

   protected static final float[] SHORT_DASH = {4.0f, 3.0f};
   protected static final float[] LONG_DASH = {9.0f, 3.0f};

   protected static final Stroke NORMAL_STROKE = new BasicStroke(1.0f);
   protected static final Stroke NORMAL_STROKE_BOLD = new BasicStroke(2.0f);
   protected static final Stroke SHORT_STROKE = new BasicStroke(1.0f
         , BasicStroke.CAP_BUTT
         , BasicStroke.JOIN_MITER
         , 10.0f
         , SHORT_DASH
         , 0.0f);
   protected static final Stroke SHORT_STROKE_BOLD = new BasicStroke(2.0f
         , BasicStroke.CAP_BUTT
         , BasicStroke.JOIN_MITER
         , 10.0f
         , SHORT_DASH
         , 0.0f);
   protected static final Stroke LONG_STROKE = new BasicStroke(1.0f
         , BasicStroke.CAP_BUTT
         , BasicStroke.JOIN_MITER
         , 10.0f
         , LONG_DASH
         , 0.0f);
   protected static final Stroke LONG_STROKE_BOLD = new BasicStroke(2.0f
         , BasicStroke.CAP_BUTT
         , BasicStroke.JOIN_MITER
         , 10.0f
         , LONG_DASH
         , 0.0f);

   private SingleRef linkType = new SingleRef(this, "Link Type");

   /**
    * GenericLinkConnection constructor comment.
    */
   public GenericLinkConnection()
   {
      super();
   }

   /**
    * GenericLinkConnection constructor comment.
    */
   public GenericLinkConnection(ILinkType linkType, NodeSymbol firstSymbol)
   {
      super();
      setLinkType(linkType);
      setFirstSymbol(firstSymbol);
   }

   public GenericLinkConnection(ILinkType linkType)
   {
      super();
      setLinkType(linkType);
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      GenericLinkConnection _copySymbol = new GenericLinkConnection();
      _copySymbol.setLinkType(getLinkType());
      return _copySymbol;
   }

   public void deleteAll()
   {
      if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea()),
            "Do you want to delete the generic link connection? " +
            "This operation cannot be undone.", "Connection Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();
      }
   }

   /**
    * draw method comment.
    */
   public void draw(Graphics graphics)
   {
      Assert.isNotNull(getLinkType(), "the linkType for the genericLink is not null");

      Graphics2D _graphics = (Graphics2D) graphics;
      Color _oldColor = graphics.getColor();
      Color penColor = null;
      Stroke _oldStroke = _graphics.getStroke();
      int _nameWidth = 0;
      int _fontHeight = 0;
      int _nameX = 0;
      int _nameY = 0;
      float _xPosFactor1 = 0.0f;
      float _yPosFactor1 = 0.0f;
      String _name = null;
      ColorKey color = getLinkType().getLineColor();

      // set the line color
      if (color.equals(ColorKey.BLACK))
      {
         penColor = Color.black;
      }
      else if (color.equals(ColorKey.DARK_BLUE))
      {
         penColor = CI.BLUE;
      }
      else if (color.equals(ColorKey.DARK_GRAY))
      {
         penColor = CI.GREY;
      }
      else if (color.equals(ColorKey.BLUE))
      {
         penColor = Color.blue;
      }
      else if (color.equals(ColorKey.LIGTH_GRAY))
      {
         penColor = CI.LIGHTGREY;
      }
      else if (color.equals(ColorKey.RED))
      {
         penColor = CI.RED;
      }
      else if (color.equals(ColorKey.YELLOW))
      {
         penColor = Color.yellow;
      }
      else
      {
         trace.debug("Unexpected keyvalue '"+ color + "' for line color.");
         penColor = CI.BLUE;
      }

      _graphics.setColor(penColor);

      // draw the Line (inclusive the arrrows)
      super.draw(_graphics);

      // draw the LinkType-Name
      if ((getLinkType().getShowLinkTypeName())
            && (getLinkType().getName() != null))
      {
         _nameWidth = _graphics.getFontMetrics().stringWidth(getLinkType().getName());
         _fontHeight = _graphics.getFontMetrics().getHeight();
         _nameX = getX() + (getWidth() - _nameWidth) / 2;
         _nameY = getY() + (getHeight() - _fontHeight) / 2;

         _graphics.setColor(Color.white);
         _graphics.fillRect(_nameX - 3
               , _nameY - _fontHeight + 3
               , _nameWidth + 4
               , _fontHeight);

         _graphics.setColor(penColor);
         _graphics.drawRect(_nameX - 3
               , _nameY - _fontHeight + 3
               , _nameWidth + 4
               , _fontHeight);

         _graphics.drawString(getLinkType().getName(), _nameX, _nameY);

      }
      // draw the role-names
      if (getLinkType().getShowRoleNames())
      {
         // @optimize .. the algorithm for the positions of the role names
         if (getFirstSymbol().getTop() < getSecondSymbol().getTop())
         {
            _yPosFactor1 = 0.25f;
         }
         else
         {
            _yPosFactor1 = 0.75f;
         }

         if (getFirstSymbol().getLeft() < getSecondSymbol().getLeft())
         {
            _xPosFactor1 = 0.2f;
         }
         else
         {
            _xPosFactor1 = 0.8f;
         }

         _name = getLinkType().getFirstRole();
         if (_name != null)
         {
            _nameWidth = _graphics.getFontMetrics().stringWidth(_name);
            _fontHeight = _graphics.getFontMetrics().getHeight();
            _nameX = Math.round(getX() + (getWidth() - _nameWidth) * _xPosFactor1);
            _nameY = Math.round(getY() + (getHeight() - _fontHeight) * _yPosFactor1);
            _graphics.setColor(Color.white);
            _graphics.fillRect(_nameX - 3
                  , _nameY - _fontHeight + 3
                  , _nameWidth + 4
                  , _fontHeight);

            _graphics.setColor(penColor);
            _graphics.drawString(_name, _nameX, _nameY);
         }
         _name = getLinkType().getSecondRole();
         if (_name != null)
         {
            _nameWidth = _graphics.getFontMetrics().stringWidth(_name);
            _fontHeight = _graphics.getFontMetrics().getHeight();
            _nameX = Math.round(getX() + (getWidth() - _nameWidth) * (1 - _xPosFactor1));
            _nameY = Math.round(getY() + (getHeight() - _fontHeight) * (1 - _yPosFactor1));

            _graphics.setColor(Color.white);
            _graphics.fillRect(_nameX - 3
                  , _nameY - _fontHeight + 3
                  , _nameWidth + 4
                  , _fontHeight);

            _graphics.setColor(penColor);
            _graphics.drawString(_name, _nameX, _nameY);
         }
      }

      _graphics.setColor(_oldColor);
      _graphics.setStroke(_oldStroke);
   }

   /**
    * Returns the name of the connection.
    */
   public String getConnectionName()
   {
      if (getLinkType() != null)
      {
         return getLinkType().getName();
      }
      else
      {
         return STRING_UNKNOWN_LINK;
      }
   }

   /** */
   public ArrowKey getFirstArrow()
   {
      return getLinkType().getFirstArrowType();
   }

   /** */
   public ArrowKey getSecondArrow()
   {
      return getLinkType().getSecondArrowType();
   }

   /**
    * @todo Insert the method's description here.
    * @return ag.carnot.workflow.LinkType
    */
   public ILinkType getLinkType()
   {
      return (ILinkType) linkType.getElement();
   }

   /** */
   public void setFirstSymbol(Symbol symbol)
   {
      // check if the first Symbol is valid for the linkType
      if ((getLinkType() != null)
            && (symbol.getUserObject() != null)
            && (getLinkType().getFirstClass().isAssignableFrom(symbol.getUserObject().getClass()))
      )
      {
         super.setFirstSymbol(symbol);
      }
      else
      {
         throw new PublicException("The Userobject of the first Symbol is not valid for the LinkType!");
      }
   }

   /**
    * @todo Insert the method's description here.
    * @param newLinkType ag.carnot.workflow.LinkType
    */
   public void setLinkType(ILinkType newLinkType)
   {
      linkType.setElement(newLinkType);
      if (newLinkType != null)
      {
         setFirstArrow(newLinkType.getFirstArrowType());
         setSecondArrow(newLinkType.getSecondArrowType());
      }
      else
      {
         setFirstArrow(ArrowKey.NO_ARROW);
         setSecondArrow(ArrowKey.NO_ARROW);
      }
   }

   /** */
   public void setSecondSymbol(Symbol secondSymbol, boolean link)
   {

      // check if the second Symbol is valid for the linkType
      if ((getLinkType() != null)
            && (secondSymbol.getUserObject() != null)
            && (getLinkType().getSecondClass().isAssignableFrom(secondSymbol.getUserObject().getClass()))
      )
      {
         super.setSecondSymbol(secondSymbol, link);
      }
      else
      {
         throw new PublicException("The Userobject of the second Symbol is not valid for the LinkType!");
      }

   }

   /**
    */
   protected Stroke getStroke()
   {
      Assert.isNotNull(getLinkType(), "the link-type for the generic connection is not null");
      Stroke _newStroke = null;
      LineKey lineKey = getLinkType().getLineType();

      if (getSelected())
      {
         if (lineKey.equals(LineKey.NORMAL))
         {
            _newStroke = NORMAL_STROKE_BOLD;
         }
         else if (lineKey.equals(LineKey.LONG_STROKES))
         {
            _newStroke = LONG_STROKE_BOLD;
         }
         else if (lineKey.equals(LineKey.SHORT_STROKES))
         {
            _newStroke = SHORT_STROKE_BOLD;
         }
         else
         {
            trace.debug("Unexpected key value '" + lineKey + "' for line type.");
            _newStroke = NORMAL_STROKE_BOLD;
         }
      }
      else
      {
         if (lineKey.equals(LineKey.NORMAL))
         {
            _newStroke = NORMAL_STROKE;
         }
         else if (lineKey.equals(LineKey.LONG_STROKES))
         {
            _newStroke = LONG_STROKE;
         }
         else if (lineKey.equals(LineKey.SHORT_STROKES))
         {
            _newStroke = SHORT_STROKE;
         }
         else
         {
            trace.debug("Unexpected key value '" + lineKey + "' for line type.");
            _newStroke = NORMAL_STROKE;
         }
      }
      return _newStroke;
   }

   public String toString()
   {
      return "Generic Link Connection  for " + linkType.getElement();
   }

}
