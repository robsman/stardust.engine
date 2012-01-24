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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.eclipse.stardust.common.Assert;


/**
 * PopupAdapter - just needed: create(Component, JPopupMenu)
 */
public abstract class PopupAdapter implements MouseListener
{
   protected JComponent Invoker;
   protected Object Data;
   protected Object rootData = null;// only needed for trees
   protected int MandatoryModifiers = InputEvent.BUTTON3_MASK;
   protected int ArbitraryModifiers = InputEvent.BUTTON3_MASK;

   protected boolean isShowing = true;

   /**
    * PopupAdapter constructor
    */
   public PopupAdapter()
   {
      super();
   }

   /**
    *
    */
   public PopupAdapter(JComponent Invoker, Object Data)
   {
      this.Invoker = Invoker;
      this.Data = Data;
      Invoker.addMouseListener(this);
      structor();
   }

   /**
    *
    */
   public static PopupAdapter create(JComponent Invoker,
         JPopupMenu NewInvokePopup)
   {
      if (Invoker instanceof GenericTable)
      {
         return new PopupAdapter(Invoker, NewInvokePopup)
         {
            protected JPopupMenu InvokePopup;

            public void structor()
            {
               InvokePopup = (JPopupMenu) Data;
            }

            public void doPopup(int X, int Y)
            {
               JTable _table = ((GenericTable) this.Invoker).getTable();
               int _indexSelectedRow = -1;
               Iterator _iterator = null;

               if (!isShowing
                     || !this.Invoker.isEnabled()
                     || (this.Invoker instanceof Entry
                     && ((Entry) this.Invoker).isReadonly()))
               {
                  return;
               }

               // move the selection (but only if this is a SingleSelectionModel !!!
               // otherwise we can't call a contextmenu for a multiselection
               if ((_table != null)
                     && (_table.getSelectionModel() != null)
                     && (_table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION)
                     && (_table.getRowCount() > 0)
               )
               {
                  _indexSelectedRow = _table.rowAtPoint(new Point(X, Y));
                  if (_indexSelectedRow >= 0)
                  {
                     _table.clearSelection();
                     _table.setRowSelectionInterval(_indexSelectedRow, _indexSelectedRow);
                  }
               }

               // inform all the popupMenuListener
               _iterator = ((GenericTable) this.Invoker).getAllPopupMenuListeners();
               if (_iterator != null)
               {
                  if (_indexSelectedRow >= 0)
                  {
                     while (_iterator.hasNext())
                     {
                        ((TablePopupMenuListener) _iterator.next()).updateMenuState((GenericTable) this.Invoker
                              , ((GenericTable) this.Invoker).getObjectAt(_indexSelectedRow)
                              , InvokePopup);
                     }
                  }
                  else
                  {
                     while (_iterator.hasNext())
                     {
                        ((TablePopupMenuListener) _iterator.next()).updateMenuState((GenericTable) this.Invoker
                              , null
                              , InvokePopup);
                     }
                  }
               }

               // Attention! The position must be changed if the table is located in
               //            a JScrollPane
               int _yPosOffset = ((GenericTable) this.Invoker).getTable().getVisibleRect().y;

               GUI.showPopup(InvokePopup, this.Invoker, X + 5, Y - _yPosOffset + 5);
            }
         };
      }
      else if (Invoker instanceof JTable)
      {
         return new PopupAdapter(Invoker, NewInvokePopup)
         {
            protected JPopupMenu InvokePopup;

            public void structor()
            {
               InvokePopup = (JPopupMenu) Data;
            }

            public void doPopup(int X, int Y)
            {
               JTable _table = (JTable) this.Invoker;
               int _indexSelectedRow = -1;

               if (!isShowing
                     || !this.Invoker.isEnabled()
                     || (this.Invoker instanceof Entry
                     && ((Entry) this.Invoker).isReadonly()))
               {
                  return;
               }
               // move the selection (but only if this is a SingleSelectionModel !!!
               // otherwise we can't call a contextmenu for a multiselection
               if ((_table != null)
                     && (_table.getSelectionModel() != null)
                     && (_table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION)
                     && (_table.getRowCount() > 0)
               )
               {
                  _indexSelectedRow = _table.rowAtPoint(new Point(X, Y));
                  if (_indexSelectedRow >= 0)
                  {
                     _table.clearSelection();
                     _table.setRowSelectionInterval(_indexSelectedRow, _indexSelectedRow);
                  }
               }
               GUI.showPopup(InvokePopup, this.Invoker, X + 5, Y + 5);
            }
         };
      }
      else if (Invoker instanceof JList)
      {
         return new PopupAdapter(Invoker, NewInvokePopup)
         {
            protected JPopupMenu InvokePopup;

            public void structor()
            {
               InvokePopup = (JPopupMenu) Data;
            }

            public void doPopup(int X, int Y)
            {
               JList _list = (JList) this.Invoker;
               int _indexSelectedRow = -1;

               if (!isShowing
                     || !this.Invoker.isEnabled()
                     || (this.Invoker instanceof Entry
                     && ((Entry) this.Invoker).isReadonly()))
               {
                  return;
               }
               // move the selection (but only if this is a SingleSelectionModel !!!
               // otherwise we can't call a contextmenu for a multiselection
               if ((_list != null)
                     && (_list.getSelectionModel() != null)
                     && (_list.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION)
                     && (_list.getModel() != null)
                     && (_list.getModel().getSize() > 0)
               )
               {
                  _indexSelectedRow = _list.locationToIndex(new Point(X, Y));
                  if (_indexSelectedRow >= 0)
                  {
                     _list.clearSelection();
                     _list.setSelectedIndex(_indexSelectedRow);
                  }
               }
               GUI.showPopup(InvokePopup, this.Invoker, X + 5, Y + 5);
            }
         };
      }
      else if (!(Invoker instanceof JTree))// Popup not added to JTree
      {
         return new PopupAdapter(Invoker, NewInvokePopup)
         {
            protected JPopupMenu InvokePopup;

            public void structor()
            {
               InvokePopup = (JPopupMenu) Data;
            }

            public void doPopup(int X, int Y)
            {
               if (!isShowing
                     || !this.Invoker.isEnabled()
                     || (this.Invoker instanceof Entry
                     && ((Entry) this.Invoker).isReadonly()))
               {
                  return;
               }
               GUI.showPopup(InvokePopup, this.Invoker, X + 5, Y + 5);
            }
         };
      }
      else if (Invoker instanceof JTree)// add some tree specific things
      {
         return new PopupAdapter(Invoker, NewInvokePopup)
         {
            protected JPopupMenu InvokePopup;

            public void structor()
            {
               InvokePopup = (JPopupMenu) Data;
            }

            public void doPopup(int X, int Y)
            {
               if (!isShowing
                     || !this.Invoker.isEnabled()
                     || (this.Invoker instanceof Entry
                     && ((Entry) this.Invoker).isReadonly()))
               {
                  return;
               }

               InvokePopup.show(this.Invoker, X + 5, Y + 5);
            }

            private void doRootPopup(int X, int Y)
            {
               if (!isShowing
                     || !this.Invoker.isEnabled()
                     || (this.Invoker instanceof Entry
                     && ((Entry) this.Invoker).isReadonly()))
               {
                  return;
               }

               GUI.showPopup((JPopupMenu) rootData, this.Invoker, X + 5, Y + 5);
            }

            public void setRootPopup(Object rootData)
            {
               this.rootData = rootData;
            }

            public void mouseReleased(MouseEvent e)
            {
               JTree tree = (JTree) this.Invoker;

               if ((e.getModifiers() & MandatoryModifiers) == MandatoryModifiers
                     && (e.getModifiers() & ArbitraryModifiers) != 0
                     && tree.getRowForLocation(e.getX(), e.getY()) != -1)
               { // root node can have a different Popup

                  // move the selection (but only if this is a SingleSelectionModel !!!
                  // otherwise we can't call a contextmenu for a multiselection
                  TreePath _selectionPath = null;
                  if ((tree.getSelectionModel() != null)
                        && (tree.getSelectionModel().getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION)
                  )
                  {
                     _selectionPath = tree.getClosestPathForLocation(e.getX(), e.getY());
                     if (_selectionPath != null)
                     {
                        tree.clearSelection();
                        tree.setSelectionPath(_selectionPath);
                     }
                  }
                  if (rootData != null
                        && tree.isRootVisible()
                        && tree.getRowForPath(tree.getSelectionPath()) == 0)
                  {
                     doRootPopup(e.getX(), e.getY());
                  }
                  else // standard node
                  {
                     doPopup(e.getX(), e.getY());
                  }
               }
            }
         };
      }
      else
      {
         Assert.lineNeverReached();
         return null;
      }
   }

   /**
    *
    */
   public static PopupAdapter create(JComponent Invoker,
         PopupInterface NewInvokeListener)
   {
      return new PopupAdapter(Invoker, NewInvokeListener)
      {
         protected PopupInterface InvokeListener;

         public void structor()
         {
            InvokeListener = (PopupInterface) Data;
         }

         public void doPopup(int X, int Y)
         {
            if (!isShowing
                  || !this.Invoker.isEnabled()
                  || (this.Invoker instanceof Entry
                  && ((Entry) this.Invoker).isReadonly()))
            {
               return;
            }

            InvokeListener.popup(this.Invoker, X + 5, Y + 5);
         }
      };
   }

   /**
    * Show the popup window.
    */
   public abstract void doPopup(int X, int Y);

   /**
    * En/disable popup dynamically.
    */
   public void setShowing(boolean isShowing)
   {
      this.isShowing = isShowing;
   }

   /**
    * En/disable popup dynamically.
    */
   public boolean isShowing()
   {
      return isShowing;
   }

   /**
    *
    */
   public void mouseClicked(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mouseEntered(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mouseExited(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mousePressed(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mouseReleased(MouseEvent CurrEvent)
   {
      if ((CurrEvent.getModifiers() & MandatoryModifiers) == MandatoryModifiers
            && (CurrEvent.getModifiers() & ArbitraryModifiers) != 0)
      {
         doPopup(CurrEvent.getX(), CurrEvent.getY());
      }
   }

   /**
    *
    */
   void newMethod()
   {
   }

   /**
    * Remove all listeners that are added internally.
    */
   public void removeAllListeners()
   {
      Invoker.removeMouseListener(this);
   }

   /**
    * Popup method
    */
   public void popup(java.awt.Component c, int x, int y)
   {
   }

   /**
    *
    */
   public void setArbitraryModifiers(int Modifiers)
   {
      this.ArbitraryModifiers = Modifiers;
   }

   /**
    *
    */
   public void setMandatoryModifiers(int Modifiers)
   {
      this.MandatoryModifiers = Modifiers;
   }

   /**
    *
    */
   public void setModifiers(int Modifiers)
   {
      this.MandatoryModifiers = Modifiers;
      this.ArbitraryModifiers = Modifiers;
   }

   /**
    * Set a different menu for the root of a tree.
    */
   public void setRootPopup(Object rootData)
   {
   }

   /**
    * Prepare the popup.
    */
   public void structor()
   {
   }
}
