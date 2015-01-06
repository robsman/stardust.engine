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

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.model.utils.ConnectionBean;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;


/** */
public abstract class AbstractConnectionSymbol extends ConnectionBean
      implements ConnectionSymbol, ActionListener
{

   private static final String STRING_BETWEEN = " between ";
   private static final String STRING_AND = " and ";
   private static final String STRING_UNKNOWN = "<unknown>";

   static protected final Stroke selectedStroke = new BasicStroke(2.0f);
   static protected final Stroke standardStroke = new BasicStroke(1.0f);

   private static ImageIcon icon;

   private transient boolean selected;
   private transient JPopupMenu popupMenu;
   private transient boolean deleted;

   public AbstractConnectionSymbol()
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

   /** */
   public void createPopupMenu()
   {
   }

   /**
    *
    */
   public void deleteAll()
   {
      boolean _ignoreFlagWasSet = false;

      try
      {
         if (getDrawArea() != null)
         {
            _ignoreFlagWasSet = getDrawArea().getIgnoreFireUndoEditRequests();

            if (!_ignoreFlagWasSet)
            {
               getDrawArea().setIgnoreFireUndoEditRequests(true);
            }
         }

         delete();

         super.delete();
      }
      finally
      {
         if (!_ignoreFlagWasSet && getDrawArea() != null)
         {
            getDrawArea().setIgnoreFireUndoEditRequests(false);
         }

         setDeleted(true);
      }
   }

   /**
    */
   public abstract void draw(Graphics graphics);

   /** */
   public int getBottom()
   {
      return getY() + getHeight();
   }

   // @todo (france, ub): delete?
   /**
    * Returns the name of the connection.
    */
   abstract public String getConnectionName();

   /** */
   public DrawArea getDrawArea()
   {
      if (getDiagram() != null)
      {
         return getDiagram().getDrawArea();
      }
      return null;
   }

   /** */
   public Symbol getFirstSymbol()
   {
      return (Symbol) getFirst();
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
            icon = new ImageIcon(AbstractConnectionSymbol.class.getResource("images/connection.gif"));
         }
         catch (Exception x)
         {
            throw new PublicException(
                  BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE.raise("/images/connection.gif"));
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
    * Returns the popup menu for the connection
    * @return javax.swing.JPopupMenu
    */
   protected JPopupMenu getPopupMenu()
   {
      return popupMenu;
   }

   /** */
   public int getRight()
   {
      return getX() + getWidth();
   }

   /** */
   public Symbol getSecondSymbol()
   {
      return (Symbol) getSecond();
   }

   /** */
   public boolean getSelected()
   {
      return selected;
   }

   /** */
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
      return 0;
   }

   /** */
   public int getY()
   {
      return 0;
   }

   /**
    *
    */
   public boolean isContainedIn(int x1, int y1, int x2, int y2)
   {
      return (x1 <= getLeft())
            && (y1 <= getTop())
            && (x2 >= getRight())
            && (y2 >= getBottom());
   }

   /** */
   public abstract boolean isHitBy(int x, int y);

   /** */
   public void mouseDragged(MouseEvent event, int lastXDrag, int lastYDrag)
   {
   }

   /**
    * default implementation does nothing because a connection can't be moved or edited
    */
   public void mouseMoved(MouseEvent event)
   {
      if (isHitBy(event.getX(), event.getY()))
      {
         event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      }
   }

   /**
    * default implementation does nothing because a connection can't be moved
    */
   public void move(int xDelta, int yDelta)
   {
      // intentionally empty
   }

   /*
    *
    */
   public void onDoubleClick(int x, int y)
   {
   }

   /** Called, whenever there is a mousePressed event on the symbol. */
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

   /**
    * Removes a connection from the drawing area and from the symbols connected
    * by this connection. All connections of this connection also will be removed.
    */
   public void delete()
   {
      super.delete();
      if (getDrawArea() != null)
      {
         getDrawArea().repaint();
      }
   }

   /** */
   private void setDeleted(boolean wasDeleted)
   {
      this.deleted = wasDeleted;
   }

   /** */
   public void setFirstSymbol(Symbol symbol)
   {
      setFirst(symbol);
   }

   /** */
   public boolean setPoint(int x, int y)
   {
      return false;
   }

   /**
    * set a new popup menu for the symbol
    */
   protected void setPopupMenu(JPopupMenu menu)
   {
      popupMenu = menu;
   }

   /** */
   public void setSecondSymbol(Symbol symbol)
   {
      setSecondSymbol(symbol, true);
   }

   /** */
   public void setSecondSymbol(Symbol symbol, boolean link)
   {
      setSecond(symbol);
   }

   /** */
   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   /** */
   public void symbolChanged(Symbol symbol)
   {
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
      if (parent instanceof GroupSymbol)
      {
         return ((GroupSymbol) parent).getDiagram();
      }
      else
      {
         return (Diagram) parent;
      }
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
