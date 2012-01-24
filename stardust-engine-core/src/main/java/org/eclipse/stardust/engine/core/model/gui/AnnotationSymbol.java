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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.engine.core.compatibility.diagram.*;


/** */
public class AnnotationSymbol extends AbstractWorkflowSymbol
      implements TextSymbolListener
{
   static protected final String NAME_STRING = "Annotation No. ";

   static final protected int EDGE_HIGH = 10;
   static final protected int EDGE_WIDTH = 10;
   static final protected int OUTER_MARGIN = 5;
   static final protected int MIN_HEIGHT = 30;
   static final protected int MIN_WIDTH = 45;

   private transient TextAreaSymbol annotationSymbol;

   private transient JMenuItem createAnotationItem;
   private transient JMenuItem genericLinkMenu;
   private transient JMenuItem removeSymbolItem;
   private transient JMenuItem propertiesItem;
   private transient JMenuItem refersToItem;
   private String annotation;
   private Symbol annotationTarget;

   public AnnotationSymbol()
   {
      this(null, null);
   }

   public AnnotationSymbol(String text, Symbol annotationTarget)
   {
      super();
      annotation = text;
      this.annotationTarget = annotationTarget;
   }

   public TextAreaSymbol getAnnotationSymbol()
   {
      if (annotationSymbol == null)
      {
         annotationSymbol = new TextAreaSymbol(annotation);
         annotationSymbol.addListener(this);
      }
      return annotationSymbol;
   }

   /** */
   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == createAnotationItem)
      {
         getDrawArea().startSymbolDefinition(new AnnotationSymbol("", this));
      }
      else if (event.getSource() == removeSymbolItem)
      {
         delete();
      }
      else if (event.getSource() == propertiesItem)
      {
         editProperties();
      }
      else if (event.getSource() == refersToItem)
      {
         getDrawArea().startConnectionDefinition(new RefersToConnection(this));
      }
      else
      {
         super.actionPerformed(event);
      }
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      AnnotationSymbol _copy = new AnnotationSymbol(annotation, null);
      _copy.setX(getX());
      _copy.setY(getY());

      return _copy;
   }

   /** */
   public void createPopupMenu()
   {
      JPopupMenu _popupMenu = new JPopupMenu();

      setPopupMenu(_popupMenu);

      createAnotationItem = new JMenuItem("Create Annotation");
      createAnotationItem.addActionListener(this);
      createAnotationItem.setMnemonic('c');
      _popupMenu.add(createAnotationItem);

      _popupMenu.addSeparator();
      removeSymbolItem = new JMenuItem("Remove symbol");
      removeSymbolItem.setMnemonic('r');
      removeSymbolItem.addActionListener(this);
      _popupMenu.add(removeSymbolItem);

      _popupMenu.addSeparator();

      propertiesItem = new JMenuItem("Properties ...");
      propertiesItem.addActionListener(this);
      propertiesItem.setMnemonic('e');
      _popupMenu.add(propertiesItem);

      genericLinkMenu = getGenericLinkMenu();
      if (genericLinkMenu != null)
      {
         _popupMenu.addSeparator();
         _popupMenu.add(genericLinkMenu);
      }

      _popupMenu.addSeparator();

      refersToItem = new JMenuItem("Refers to");
      refersToItem.addActionListener(this);
      refersToItem.setMnemonic('r');
      _popupMenu.add(refersToItem);
   }

   /** */
   public void draw(Graphics g)
   {
      Assert.isNotNull(g);

      Graphics2D graphics = (Graphics2D) g;
      Stroke oldStroke = graphics.getStroke();
      Color oldColor = graphics.getColor();

      if (getSelected())
      {
         graphics.setStroke(selectedStroke);
      }

      int left = getLeft();
      int right = getRight();
      int up = getTop();
      int down = getBottom();

      if (Stylesheet.instance().isLoaded())
      {
         graphics.setColor(Color.white);
         graphics.setColor(PEN_COLOR);
         graphics.drawLine(left, down, left, up);
         graphics.drawLine(left, up, right, up);
         graphics.drawLine(left, down, right, down);

         graphics.setColor(oldColor);
         graphics.setStroke(oldStroke);

         getAnnotationSymbol().setPoint(getX() + OUTER_MARGIN, getY() + EDGE_HIGH);
         getAnnotationSymbol().draw(g);
      }
      else
      {
         int[] xPoints = new int[]{right - EDGE_WIDTH, left, left, right, right};
         int[] yPoints = new int[]{up, up, down, down, up + EDGE_HIGH};
         Polygon body = new Polygon(xPoints, yPoints, 5);

         graphics.setColor(Color.white);
         graphics.fillPolygon(body);
         graphics.setColor(PEN_COLOR);
         graphics.drawPolygon(body);
         graphics.drawLine(right - EDGE_WIDTH, up, right - EDGE_WIDTH, up + EDGE_HIGH);
         graphics.drawLine(right - EDGE_WIDTH, up + EDGE_HIGH, right, up + EDGE_HIGH);

         graphics.setColor(oldColor);
         graphics.setStroke(oldStroke);

         getAnnotationSymbol().setPoint(getX() + OUTER_MARGIN, getY() + EDGE_HIGH);
         getAnnotationSymbol().draw(g);
      }

   }

   /**
    * Show and edit the Properties
    */
   protected void editProperties()
   {
   }

   /** */
   public int getHeight()
   {
      return Math.max(getAnnotationSymbol().getHeight() + OUTER_MARGIN + EDGE_HIGH, MIN_HEIGHT);
   }

   /**
    *	Method for interface ModelElement
    *
    * @see org.eclipse.stardust.engine.core.model.utils.ModelElement
    */
   public String getName()
   {
      return NAME_STRING + getElementOID();
   }

   /** */
   public String getText()
   {
      return annotation;
   }

   /** */
   public int getWidth()
   {
      return Math.max(getAnnotationSymbol().getWidth() + (OUTER_MARGIN + EDGE_WIDTH), MIN_WIDTH);
   }

   /** */
   public void mouseMoved(MouseEvent event)
   {
      if (getAnnotationSymbol().isHitBy(event.getX(), event.getY()))
      {
         event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
      }
      else if (isHitBy(event.getX(), event.getY()))
      {
         event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      }
   }

   /** */
   public void onPress(MouseEvent event)
   {
      if ((annotationSymbol != null) && (annotationSymbol.isHitBy(event.getX(), event.getY())))
      {
         annotationSymbol.onPress(event);
      }

      super.onPress(event);
   }

   /*
    * Called before a popup menu is activated and may be used to enable or
    * disable menu items according to the state of the represented object.
    */
   public void preparePopupMenu()
   {
      boolean _isWritable = (getDrawArea() != null) && (!getDrawArea().isReadOnly());

      if (genericLinkMenu != null)
      {
         // hint: item could be disabled if there doesn't exist
         // 		any GenericLinkType for this modelelement
         genericLinkMenu.setEnabled(_isWritable && genericLinkMenu.isEnabled());
      }
      removeSymbolItem.setEnabled(_isWritable);
      //		propertiesItem.setEnabled(true);	 // item is always enabled

      refersToItem.setEnabled(_isWritable);
   }

   /**
    *
    */
   public boolean setPoint(int x, int y)
   {
      setX(x);
      setY(y);

      if (annotationTarget != null)
      {
         ConnectionSymbol _newConnection = new RefersToConnection(this);
         getDiagram().addToConnections(_newConnection, 0);
         _newConnection.setSecondSymbol(annotationTarget);
         annotationTarget = null;

      }
      return false;
   }

   /** */
   public void setText(String text)
   {
      annotation = text;
      if (annotationSymbol != null)
      {
         annotationSymbol.setText(text);
      }
   }

   /** */
   public void textSymbolChanged(TextSymbolEvent event)
   {
      if (getDrawArea() != null)
      {
         getDrawArea().repaint();
      }
   }

   public void textSymbolDeselected(TextSymbolEvent event)
   {
   }

   public String toString()
   {
      return "Annotation Symbol";
   }

}
