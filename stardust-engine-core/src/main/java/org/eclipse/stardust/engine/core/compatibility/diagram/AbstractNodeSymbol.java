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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.undo.UndoableEdit;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementBean;


/** */
public abstract class AbstractNodeSymbol extends ModelElementBean
      implements NodeSymbol, ActionListener
{

   protected static final Stroke selectedStroke = new BasicStroke(2.0f);
   private static final int DIRECTION_NONE = 0;
   private static final int DIRECTION_NORTH = 1;
   private static final int DIRECTION_NORTH_WEST = 2;
   private static final int DIRECTION_WEST = 3;
   private static final int DIRECTION_SOUTH_WEST = 4;
   private static final int DIRECTION_SOUTH = 5;
   private static final int DIRECTION_SOUTH_EAST = 6;
   private static final int DIRECTION_EAST = 7;
   private static final int DIRECTION_NORTH_EAST = 8;
   private static final int MARKER_SIZE = 6;
   private static final Color MARKER_COLOR = Color.black;

   private static ImageIcon icon;

   private static final String X_ATT = "X";
   private int x;
   private static final String Y_ATT = "X";
   private int y;

   private transient boolean selected;
   private transient JPopupMenu popupMenu;
   private transient boolean deleted;

   /** */
   public AbstractNodeSymbol()
   {
   }

   public void actionPerformed(ActionEvent event)
   {
   }

   /**
    * The implementation calls once the method createPopupMenu() and
    * then returns the existing menu.
    *
    * If an popup menu with dynamic content is needed, you should overload
    * this method.
    *
    */
   public void activatePopupMenu(DrawArea drawArea, int x, int y)
   {
      if (getPopupMenu() == null)
      {
         createPopupMenu();
      }

      if (getPopupMenu() != null)
      {
         preparePopupMenu();

         GUI.showPopup(getPopupMenu(), drawArea, x, y);
      }
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public abstract Symbol copySymbol();

   /**
    */
   public void createPopupMenu()
   {
   }

   /**
    *
    */
   public void deleteAll()
   {
      if (getUserObject() != null && getUserObject() instanceof ModelElement)
      {
         ModelElement element = (ModelElement) getUserObject();
         IModel model = (IModel) element.getModel();
         deleteSymbols(model.getAllDiagrams(), element);
         for (Iterator i = model.getAllProcessDefinitions(); i.hasNext();)
         {
            IProcessDefinition pd = (IProcessDefinition) i.next();
            deleteSymbols(pd.getAllDiagrams(), element);
         }
      }
      delete();
   }

   private void deleteSymbols(Iterator diagrams, ModelElement element)
   {
      for (; diagrams.hasNext();)
      {
         Diagram d = (Diagram) diagrams.next();
         Symbol symbol = d.findSymbolForUserObject(element);
         if (symbol != null)
         {
            symbol.delete();
         }
      }
   }

   /** */
   public void draw(Graphics graphics)
   {
      if (getSelected())
      {
         drawMarkers(graphics);
      }
   }

   /** */
   private void drawMarkers(Graphics graphics)
   {
      graphics.setColor(MARKER_COLOR);
      graphics.fillRect(getLeft() - MARKER_SIZE / 2, getBottom() - MARKER_SIZE / 2,
            MARKER_SIZE, MARKER_SIZE);
      graphics.fillRect(getRight() - MARKER_SIZE / 2, getBottom() - MARKER_SIZE / 2,
            MARKER_SIZE, MARKER_SIZE);
      graphics.fillRect(getRight() - MARKER_SIZE / 2, getTop() - MARKER_SIZE / 2,
            MARKER_SIZE, MARKER_SIZE);
      graphics.fillRect(getLeft() - MARKER_SIZE / 2, getTop() - MARKER_SIZE / 2,
            MARKER_SIZE, MARKER_SIZE);
   }

   /**
    *
    */
   public int getBottom()
   {
      return getY() + getHeight();
   }

   /** */
   public DrawArea getDrawArea()
   {
      if (getDiagram() != null)
      {
         return getDiagram().getDrawArea();
      }
      return null;
   }

   /**
    */
   public abstract int getHeight();

   /**
    */
   public ImageIcon getIcon()
   {
      if (icon == null)
      {
         try
         {
            icon = new ImageIcon(AbstractNodeSymbol.class.getResource("images/symbol.gif"));
         }
         catch (Exception x)
         {
            throw new PublicException(
                  BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE.raise("images/symbol.gif"));
         }
      }
      return icon;
   }

   /** */
   public int getLeft()
   {
      return getX();
   }

   /**
    * Returns the popup menu for the symbol
    * @return javax.swing.JPopupMenu
    */
   protected JPopupMenu getPopupMenu()
   {
      return popupMenu;
   }

   /**
    *
    */
   public int getRight()
   {
      return getX() + getWidth();
   }

   /** */
   public boolean getSelected()
   {
      return selected;
   }

   /**
    *
    */
   public int getTop()
   {
      return getY();
   }

   /*
    * Retrieves the (model) information represented by the symbol. If not set,
    * the method returns <code>null</code>.
    */
   public Object getUserObject()
   {
      return null;
   }

   /** */
   public abstract int getWidth();

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
    * Returns the direction/position of the marker hit. If no marker is hit
    * DIRECTION_NONE is returned.
    */
   private int hitMarkers(int pickX, int pickY)
   {
      int relX = pickX - getX();
      int relY = pickY - getY();
      int halfWidth = getWidth() / 2;
      int halfHeight = getHeight() / 2;

      if (hitsMarker(relX, relY, 0, 0))
      {
         return DIRECTION_NORTH_WEST;
      }
      else if (hitsMarker(relX, relY, halfWidth, 0))
      {
         return DIRECTION_NORTH;
      }
      else if (hitsMarker(relX, relY, getWidth(), 0))
      {
         return DIRECTION_NORTH_EAST;
      }
      else if (hitsMarker(relX, relY, getWidth(), halfHeight))
      {
         return DIRECTION_EAST;
      }
      else if (hitsMarker(relX, relY, getWidth(), getHeight()))
      {
         return DIRECTION_SOUTH_EAST;
      }
      else if (hitsMarker(relX, relY, halfWidth, getHeight()))
      {
         return DIRECTION_SOUTH;
      }
      else if (hitsMarker(relX, relY, 0, getHeight()))
      {
         return DIRECTION_SOUTH_WEST;
      }
      else if (hitsMarker(relX, relY, 0, halfHeight))
      {
         return DIRECTION_WEST;
      }
      else
      {
         return DIRECTION_NONE;
      }
   }

   /** Checks, wether a marker with center (markerX, markerY) is hit by
    (x,y) given the MARKER_SIZE constant. */
   private static boolean hitsMarker(int x, int y, int markerX, int markerY)
   {
      return (x >= markerX - MARKER_SIZE && x <= markerX + MARKER_SIZE &&
            y >= markerY - MARKER_SIZE && y <= markerY + MARKER_SIZE);
   }

   /** */
   public boolean isContainedIn(int x1, int y1, int x2, int y2)
   {
      return x1 <= getLeft() && y1 <= getTop() && x2 >= getRight() && y2 >= getBottom();
   }

   /** */
   public boolean isDeleted()
   {
      return deleted;
   }

   /** */
   public boolean isHitBy(int x, int y)
   {
      return getLeft() <= x && getRight() >= x && getBottom() >= y && getTop() <= y;
   }

   /**
    * Markes the Symbol as deleted
    */
   public void markAsDeleted()
   {
      deleted = true;
   }

   /**
    *
    */
   public void mouseDragged(MouseEvent event, int lastXDrag, int lastYDrag)
   {
      move(event.getX() - lastXDrag, event.getY() - lastYDrag);
      // hint: the Connection will be notified in the move()-method
   }

   /**
    *
    */
   public void mouseMoved(MouseEvent event)
   {
      if (isHitBy(event.getX(), event.getY()))
      {
         int direction = hitMarkers(event.getX(), event.getY());

         if (getSelected())
         {
            switch (direction)
            {
               case DIRECTION_NONE:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                     break;
                  }
               case DIRECTION_NORTH:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                     break;
                  }
               case DIRECTION_NORTH_WEST:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                     break;
                  }
               case DIRECTION_WEST:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                     break;
                  }
               case DIRECTION_SOUTH_WEST:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                     break;
                  }
               case DIRECTION_SOUTH:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                     break;
                  }
               case DIRECTION_SOUTH_EAST:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                     break;
                  }
               case DIRECTION_EAST:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                     break;
                  }
               case DIRECTION_NORTH_EAST:
                  {
                     event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                     break;
                  }
            }
         }
         else
         {
            event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
         }

         return;
      }
   }

   /** */
   public void move(int xDelta, int yDelta)
   {
      markModified();
      setX(getX() + xDelta);
      setY(getY() + yDelta);
   }

   /*
    *
    */
   public void onDoubleClick(int x, int y)
   {
   }

   /**
    * Called, whenever there is a mousePressed event on the symbol.
    */
   public void onPress(MouseEvent event)
   {
   }

   /*
    * Called before a popup menu is activated and may be used to enable or
    * disable menu items according to the state of the represented object.
    */
   public void preparePopupMenu()
   {
   }

   /** Removes a symbol and all connections from the drawing area. */
   public void delete()
   {
      UndoableEdit _edit = new DeleteSymbolEdit(this);

      boolean _ignoreFlagWasSet = false;

      try
      {
         // stopp editmode
         if (getDrawArea() != null)
         {
            _ignoreFlagWasSet = getDrawArea().getIgnoreFireUndoEditRequests();
            getDrawArea().deactivateEditing();
            getDrawArea().setIgnoreFireUndoEditRequests(true);
         }

         // @todo (egypt): obsolete
         //         getDiagram().removeFromSymbols(this);

         super.delete();

         // @todo (egypt): this should be automatically done
         //for (Iterator i = getAllConnectionsRecursively().iterator();i.hasNext();)
         //{
         //   ((Connection) i.next()).delete();
         //}

         if (getDrawArea() != null)
         {
            getDrawArea().repaint();
            // generate a UndoableEdit-Event
            getDrawArea().setIgnoreFireUndoEditRequests(_ignoreFlagWasSet);
            getDrawArea().fireUndoableEdit(_edit);
         }
      }
      finally
      {
         if (getDrawArea() != null)
         {
            getDrawArea().setIgnoreFireUndoEditRequests(_ignoreFlagWasSet);
         }
      }
   }

   /** */
   protected void setDeleted(boolean wasDeleted)
   {
      this.deleted = wasDeleted;
   }

   /**
    * Used during construction. As long as true is returned by this method, the
    * symbol needs more points for its definition.
    */
   public abstract boolean setPoint(int x, int y);

   /**
    * set a new popup menu for the symbol
    */
   protected void setPopupMenu(JPopupMenu menu)
   {
      popupMenu = menu;
   }

   /** */
   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   /** */
   public void setX(int x)
   {
      this.x = x;
   }

   /** */
   public void setY(int y)
   {
      this.y = y;
   }

   /*
    * Forces the symbol to change its appearance according to the changes on its
    * user object.
    */
   public void userObjectChanged()
   {
   }

   public Diagram getDiagram()
   {
      if (parent instanceof NodeSymbol)
      {
         return ((NodeSymbol) parent).getDiagram();
      }
      return (Diagram) parent;
   }

   public void setUserObject(IdentifiableElement userObject)
   {
      throw new UnsupportedOperationException();
   }

   public void setDiagram(Diagram diagram)
   {
      parent = diagram;
   }

}
