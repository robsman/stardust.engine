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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.SplicingIterator;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.model.utils.Connections;
import org.eclipse.stardust.engine.core.model.utils.Link;


/** */
public class GroupSymbol extends AbstractNodeSymbol implements SymbolOwner
{
   protected static final Color SELECTED_COLOR = Color.lightGray;

   private static final String X2_ATT = "X2";
   private int x2;
   private static final String Y2_ATT = "Y2";
   private int y2;

   private Link nodes = new Link(this, "Symbols");
   private Connections connections = new Connections(this, "Connections", "outConnections", "inConnections");

   private transient JMenuItem ungroupItem;
   private transient JMenuItem removeSymbolItem;
   private transient JMenuItem deleteAllItem;

   GroupSymbol()
   {
   }

   public GroupSymbol(Collection nodes, Collection connections)
   {
      Iterator _childIterator = null;

      setX(Integer.MAX_VALUE);
      setY(Integer.MAX_VALUE);

      x2 = Integer.MIN_VALUE;
      y2 = Integer.MIN_VALUE;

      _childIterator = nodes.iterator();
      while (_childIterator.hasNext())
      {
         NodeSymbol _symbol = (NodeSymbol) _childIterator.next();
         addToNodes(_symbol);
      }

      _childIterator = connections.iterator();
      while (_childIterator.hasNext())
      {
         ConnectionSymbol connection = (ConnectionSymbol) _childIterator.next();
         addToConnections(connection);
      }

      createPopupMenu();
   }

   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == ungroupItem)
      {
         ungroup();
      }
      else if (event.getSource() == removeSymbolItem)
      {
         delete();
      }
      else if (event.getSource() == deleteAllItem)
      {
         if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea())
               , "You are going to delete one or more modelelements.\n\n" +
               "This operation cannot be undone. Continue?", "Modelelement Deletion",
               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
         {
            deleteAll();
         }
      }
   }

   public void addToNodes(NodeSymbol symbol)
   {
      if (symbol != null)
      {
         if (nodes.contains(symbol))
         {
            return;
         }
         recalculateGroupBounds(symbol);
         nodes.add(symbol);
      }
   }

   public void addToConnections(ConnectionSymbol symbol)
   {
      if (symbol != null)
      {
         if (connections.contains(symbol))
         {
            return;
         }
         recalculateGroupBounds(symbol);
         connections.add(symbol);
      }

   }

   private void recalculateGroupBounds(Symbol symbol)
   {
      setX(Math.min(getX(), symbol.getLeft()));
      setY(Math.min(getY(), symbol.getTop()));
      x2 = Math.max(x2, symbol.getRight());
      y2 = Math.max(y2, symbol.getBottom());
   }

   /**
    * Creates a new groupsymbol in the diagram and removes the childsymbols
    * from this diagram.
    * This method don't create a undoable edit.
    */
   static public GroupSymbol createGroupSymbol(Diagram diagram, Collection symbols)
   {
      Assert.isNotNull(diagram, "Diagram is not null");
      Assert.isNotNull(symbols, "Symbolcollection is not null");

      java.util.List connections = CollectionUtils.newList();
      java.util.List nodes = CollectionUtils.newList();

      try
      {
         // Hint: To avoid problems ignore "dangling" connections
         
         for (Iterator i = symbols.iterator(); i.hasNext();)
         {
            Symbol symbol = (Symbol) i.next();
            symbol.setSelected(false);
            if (symbol instanceof ConnectionSymbol)
            {
               ConnectionSymbol connection = (ConnectionSymbol) symbol;
               if ((symbols.contains(connection.getFirstSymbol()))
                     && (symbols.contains(connection.getSecondSymbol()))
               )
               {
                  connections.add(connection);
               }
            }
            else
            {
               nodes.add(symbol);
            }
         }

         GroupSymbol result = new GroupSymbol(nodes, connections);

         for (Iterator i = nodes.iterator(); i.hasNext();)
         {
            NodeSymbol node = (NodeSymbol) i.next();
            diagram.removeFromNodes(node);
         }

         for (Iterator i = connections.iterator(); i.hasNext();)
         {
            ConnectionSymbol connection = (ConnectionSymbol) i.next();
            diagram.removeFromConnections(connection);
         }

         diagram.addToNodes(result, 0);
         return result;
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   // @todo (egypt): handle by deepcopy
   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      // attention:  A simply copy of all children doesn't fit the requirement
      //             because there can be connections as children and a we
      //             need a connection copy that refers to the copies of the
      //             originally connected symbols

      HashMap nodeCopies = new HashMap();
      HashMap connectionCopies = new HashMap();
      Iterator _childIterator = nodes.iterator();
      Symbol _symbol = null;
      ConnectionSymbol _connection = null;
      ConnectionSymbol _connectionCopy = null;
      GroupSymbol _copy = null;


      // copy all symbols that are not connections
      while (_childIterator.hasNext())
      {
         _symbol = (NodeSymbol) _childIterator.next();
         nodeCopies.put(new Integer(_symbol.getElementOID()), (_symbol.copySymbol()));
      }

      // copy all connections and connect with the copies of the originally connected symbols
      // attention:  All dangling connections will be removed!! because
      //             we can't create copies from symbols that are not in the group
      _childIterator = connections.iterator();
      while (_childIterator.hasNext())
      {
         _connection = (ConnectionSymbol) _childIterator.next();
         Integer _keyFirstSymbol = new Integer(_connection.getFirstSymbol().getElementOID());
         Integer _keySecondSymbol = new Integer(_connection.getSecondSymbol().getElementOID());

         if ((nodeCopies.containsKey(_keyFirstSymbol))
               && (nodeCopies.containsKey(_keySecondSymbol))
         )
         {
            _connectionCopy = (ConnectionSymbol) _connection.copySymbol();
            connectionCopies.put(new Integer(_connection.getElementOID()), _connectionCopy);

            // @todo (egypt): an endpoint is not necessary a node symbol
            _connectionCopy.setFirstSymbol((NodeSymbol) nodeCopies.get(_keyFirstSymbol));
            _connectionCopy.setSecondSymbol((NodeSymbol) nodeCopies.get(_keySecondSymbol), false);
         }
      }

      _copy = new GroupSymbol(nodeCopies.values(), connectionCopies.values());

      return _copy;
   }

   /** */
   public void createPopupMenu()
   {
      JPopupMenu _popupMenu = null;

      super.createPopupMenu();
      if (getPopupMenu() == null)
      {
         _popupMenu = new JPopupMenu();
         setPopupMenu(_popupMenu);
      }
      else
      {
         _popupMenu = getPopupMenu();
         _popupMenu.addSeparator();
      }

      removeSymbolItem = new JMenuItem("Remove Symbol(s)");
      removeSymbolItem.addActionListener(this);
      removeSymbolItem.setMnemonic('r');
      _popupMenu.add(removeSymbolItem);

      deleteAllItem = new JMenuItem("Delete All");
      deleteAllItem.addActionListener(this);
      deleteAllItem.setMnemonic('a');
      _popupMenu.add(deleteAllItem);

      _popupMenu.addSeparator();

      ungroupItem = new JMenuItem("Ungroup");
      ungroupItem.addActionListener(this);
      ungroupItem.setMnemonic('u');
      _popupMenu.add(ungroupItem);

   }

   /**
    */
   public void deleteAll()
   {
      java.util.Iterator _iterator = getAllSymbols();

      Symbol _symbol = null;
      // Delete all connections in the first step to avoid problems with
      // double delete() calls.
      // Remember a delete() of a symbol deletes also the associated connections.
      while (_iterator.hasNext())
      {
         _symbol = (Symbol) _iterator.next();
         if (!(_symbol instanceof ConnectionSymbol))
         {
            _symbol.deleteAll();
         }
      }

      // delete all 'real' symbols in the first step
      _iterator = getAllSymbols();
      while (_iterator.hasNext())
      {
         _symbol = (Symbol) _iterator.next();
         if (_symbol instanceof ConnectionSymbol)
         {
            _symbol.deleteAll();
         }
      }
      delete();
      super.deleteAll();
   }

   /** */
   public void draw(Graphics graphics)
   {
      java.util.Iterator iterator = getAllSymbols();

      while (iterator.hasNext())
      {
         ((Symbol) iterator.next()).draw(graphics);
      }

      if (getSelected())
      {
         graphics.setColor(SELECTED_COLOR);
         graphics.drawRect(getX(), getY(), getWidth(), getHeight());
      }
   }

   /**
    */
   public static Symbol findCopyForChild(Symbol originalSymbol
         , Collection originalSymbolList
         , Map copiedSymbolList)
   {
      Assert.isNotNull(originalSymbol, "Original symbol is not null");
      Assert.isNotNull(originalSymbolList, "List of original symbols is not null");
      Assert.isNotEmpty(originalSymbolList, "List of original symbols is not empty");
      Assert.isNotNull(copiedSymbolList, "List of symbolcopies is not null");
      Assert.isNotEmpty(copiedSymbolList.values(), "List of symbolcopies is not empty");

      // @optimize ...  realization of symbolgroups with the goal to find a simple
      //                way for copying (hard to find the copy for a child)
      //                Maybe it is easier to see a symbolgroup as a collection
      //                of symbols and not itself as a real symbol.
      Symbol _searchedCopy = null;

      Iterator _symbolIterator = null;
      Symbol _symbol = null;

      GroupSymbol _groupSymbol = null;
      GroupSymbol _groupSymbolCopy = null;

      if (copiedSymbolList.containsKey(new Integer(originalSymbol.getElementOID())))
      {
         _searchedCopy = (Symbol) copiedSymbolList.get(new Integer(originalSymbol.getElementOID()));
      }
      else
      {
         // Damned! This is a connection that connects a child of a GroupSymbol
         // Remember it isn't easy to find the copy of the originally childsymbol

         _symbolIterator = originalSymbolList.iterator();
         while (_symbolIterator.hasNext())
         {
            _symbol = (Symbol) _symbolIterator.next();
            if (_symbol instanceof GroupSymbol)
            {
               _groupSymbol = (GroupSymbol) _symbol;
               if (_groupSymbol.isChildSymbol(originalSymbol))
               {
                  _groupSymbolCopy = (GroupSymbol) copiedSymbolList.get(new Integer(_groupSymbol.getElementOID()));
                  _searchedCopy = findCopyInGroup(originalSymbol, _groupSymbol, _groupSymbolCopy);
               }
            }
         }
      }

      return _searchedCopy;
   }

   /**
    */
   public static Symbol findCopyInGroup(Symbol originalSymbol
         , GroupSymbol originalGroupSymbol
         , GroupSymbol copyGroupSymbol)
   {
      Assert.isNotNull(originalSymbol, "Original child symbol is not null");
      Assert.isNotNull(originalGroupSymbol, "Original GroupSymbol is not null");
      Assert.isNotNull(copyGroupSymbol, "Copy GroupSymbol is not null");

      Symbol _searchedCopy = null;

      Iterator _childIteratorInOriginal = originalGroupSymbol.getAllSymbols();
      Iterator _childIteratorInCopy = copyGroupSymbol.getAllSymbols();

      Symbol _childOriginal = null;
      Symbol _childCopy = null;

      while (_childIteratorInOriginal.hasNext())
      {
         _childOriginal = (Symbol) _childIteratorInOriginal.next();
         _childCopy = (Symbol) _childIteratorInCopy.next();

         if (((_childOriginal.getUserObject() == null)
               && (_childCopy.getUserObject() == null)
               && (_childOriginal.getClass().equals(_childCopy.getClass()))
               )
               || ((_childOriginal.getUserObject() != null)
               && (_childCopy.getUserObject() != null)
               && (_childOriginal.getUserObject().equals(_childCopy.getUserObject()))
               )
         )
         {
            // Juhuuuu! We have found the copy!!!
            _searchedCopy = _childCopy;
         }
         else if ((_childOriginal instanceof GroupSymbol)
               && (((GroupSymbol) _childOriginal).isChildSymbol(originalSymbol))
         )
         {
            // it is not a direct child (it is a child of a child)
            _searchedCopy = findCopyInGroup(originalSymbol, (GroupSymbol) _childOriginal, (GroupSymbol) _childCopy);
         }
      }

      return _searchedCopy;
   }

   public Iterator getAllSymbols()
   {
      return new SplicingIterator(nodes.iterator(), connections.iterator());
   }

   public int getHeight()
   {
      return Math.abs(y2 - getY());
   }

   /** */
   public int getWidth()
   {
      return Math.abs(x2 - getX());
   }

   public boolean contains(Symbol symbol)
   {
      return nodes.contains(symbol) || connections.contains(symbol);
   }

   /**
    * Return true if the symbol is a child (direct or indirect) otherwise false
    */
   public boolean isChildSymbol(Symbol symbol)
   {
      if (symbol != null)
      {
         for (Iterator i = getAllSymbols(); i.hasNext();)
         {
            Symbol child = (Symbol) i.next();
            if (child instanceof GroupSymbol)
            {
               if (((GroupSymbol) child).isChildSymbol(symbol))
               {
                  return true;
               }
            }
            else
            {
               if (symbol.equals(child))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   public void mouseDragged(MouseEvent event, int lastXDrag, int lastYDrag)
   {
      move(event.getX() - lastXDrag, event.getY() - lastYDrag);
   }

   /** */
   public void move(int xDelta, int yDelta)
   {
      setX(getX() + xDelta);
      setY(getY() + yDelta);

      x2 += xDelta;
      y2 += yDelta;

      java.util.Iterator iterator = getAllSymbols();

      while (iterator.hasNext())
      {
         ((Symbol) iterator.next()).move(xDelta, yDelta);
      }
   }

   /*
    * Called before a popup menu is activated and may be used to enable or
    * disable menu items according to the state of the represented object.
    */
   public void preparePopupMenu()
   {
      super.preparePopupMenu();

      boolean _isWritable = (getDrawArea() != null) && (!getDrawArea().isReadOnly());

      ungroupItem.setEnabled(_isWritable);
      removeSymbolItem.setEnabled(_isWritable);
      deleteAllItem.setEnabled(_isWritable);
   }

   /** */
   public boolean setPoint(int x, int y)
   {

      move(x - getX(), y - getY());

      return false;
   }

   /**
    *
    */
   public void ungroup()
   {
      Assert.isNotNull(getDrawArea(), "DrawArea is not null");
      Assert.isNotNull(getDrawArea().getDiagram(), "Diagram is not null");

      // Hint: Do not deactivate explicitly the 'IgnoreFireUndoEditRequests'
      //       property in the DrawArea. This would leads to problems because
      //       this method is used in "UdoableEdit.undo" with activated
      //       'IgnoreFireUndoEditRequests'!!!

      Diagram diagram = getDrawArea().getDiagram();

      try
      {
         getDrawArea().setIgnoreRepaintRequests(true);

         getDrawArea().deselectAllSymbols();

         setSelected(false);
         diagram.removeFromNodes(this);

         for (Iterator i = nodes.iterator(); i.hasNext();)
         {
            NodeSymbol node = (NodeSymbol) i.next();
            diagram.addToNodes(node, node.getElementOID());
            node.setSelected(true);
         }

         for (Iterator i = connections.iterator(); i.hasNext();)
         {
            ConnectionSymbol connection = (ConnectionSymbol) i.next();
            diagram.addToConnections(connection, connection.getElementOID());
            connection.setSelected(true);
         }
         getDrawArea().fireUndoableEdit(new UngroupSymbolsEdit(this));
      }
      finally
      {
         getDrawArea().setIgnoreRepaintRequests(false);
         getDrawArea().refreshSelectedSymbols();
      }
   }
}
