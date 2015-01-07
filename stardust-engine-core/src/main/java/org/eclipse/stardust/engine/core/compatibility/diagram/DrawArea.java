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
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.print.PageFormat;
import java.util.*;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.compatibility.gui.StatusListener;


/**
 * Serves as an adapter between mouse and key events on the drawing area
 * panel and the drawables contained in this panel. For example it transforms
 * coordinates of mouse events to world coordinates.
 * <p/>
 * The draw area manages multiple status possibly being notified by the symbols
 * contained in the drawing area.
 * <p/>
 * The draw area class supports undo/redo logging. Do distinguish between
 * those methods doing log and graphics operations and those doing the pure
 * graphics, we have chosen the following naming convention:
 * <p/>
 * The methods doing the log are public and named <code><action>()</code>,
 * while the pure graphical methods are private and named <code><action>Action()</code>, e.g.
 * <p/>
 * <pre>public void moveSymbol(...)<p>
 * {<p>
 * }<p>
 * </pre><p>
 * and
 * <pre>private void moveSymbolAction(...)<p>
 * {<p>
 * }<p>
 * </pre><p>
 */
public class DrawArea extends JPanel
      implements ActionListener, MouseListener, MouseMotionListener,
      KeyListener
{
   private static final Logger trace = LogManager.getLogger(DrawArea.class);

   public static final Color DEFAULT_READONLY_COLOR = new Color(240, 240, 240);

   /** Propertyname use for PropertyChangeEvents if the symbols selection has changed */
   public static final String STRING_PROPERTY_SELECTION = "Selection";
   /** Propertyname use for PropertyChangeEvents if the clipboard content has changed */
   public static final String STRING_PROPERTY_CLIPBOARD_CONTENT = "ClipboardContent";

   static protected final int PRINT_INSET_LEFT = 35;
   static protected final int PRINT_INSET_RIGTH = 35;
   static protected final int PRINT_INSET_TOP = 35;
   static protected final int PRINT_INSET_BOTTOM = 35;

   static protected final double DEFAULT_PRINTING_SCALE = 0.8;

   protected static final int IDLE = 0;
   protected static final int RUBBER_BAND = 1;
   protected static final int SYMBOL_DEFINITION = 2;
   protected static final int SYMBOL_DEFINITION_START = 3;
   protected static final int PREPARED_FOR_ZOOM_IN = 4;
   protected static final int PREPARED_FOR_ZOOM_OUT = 5;
   protected static final int SYMBOLS_SELECTED = 6;
   protected static final int CONNECTION_DEFINITION = 7;
   protected static final int TRAVERSE_PATH = 8;
   protected static final int PREPARED_FOR_PASTE = 9;

   private static final double ZOOM_FACTOR = 1.1;

   /**
    * May need to extend this, currently only one for
    * all draw areas of a program.
    */
   protected static Hashtable clipboard = new Hashtable();
   protected static int xClipboard;
   protected static int yClipboard;

   protected Object userObject;
   protected Dimension size = new Dimension(0, 0);
   protected Diagram diagram;
   protected List selectedSymbols;
   protected NodeSymbol newSymbol;
   protected ConnectionSymbol newConnection;
   protected Component currentSymbolEditor;
   protected EditableSymbol currentEditableSymbol;
   protected NodeSymbol currentSymbol;
   protected int lastXPress;
   protected int lastYPress;
   protected int lastXMousePos;
   protected int lastYMousePos;
   protected int state;
   protected AffineTransform transform;
   protected AffineTransform inverseTransform;
   private JPopupMenu popupMenu;
   private JMenuItem selectAllItem;
   private JMenuItem copyItem;
   private JMenuItem cutItem;
   private JMenuItem pasteItem;
   private JMenuItem groupItem;
   private JMenuItem ungroupItem;
   private JMenuItem removeSymbolsItem;
   private JMenuItem deleteAllItem;
   protected List statusListeners;
   protected LinkedList stepList;
   protected int traverseIndex;

   private UndoableEditSupport undoableEditSupport;

   private boolean ignoreRepaintRequests = false;
   private boolean ignoreFireUndoEditRequests = false;

   private boolean readOnly = false;

   private double zoomFactor = 1.0;

   private DecorationStrategy decorationStrategy = DefaultDecorationStrategy.instance();

   /**
    * Serves as an adapter between mouse and key events on the drawing area
    * panel and the drawables contained in this panel. For example it transforms
    * coordinates of mouse events to world coordinates.
    */
   public DrawArea(Diagram diagram, Object userObject)
   {
      this.diagram = diagram;
      setReadOnly(false);

      diagram.setDrawArea(this);

      this.userObject = userObject;

      adjustSize();

      addMouseListener(this);
      addMouseMotionListener(this);

      selectedSymbols = CollectionUtils.newList();

      transform = new AffineTransform();

      try
      {
         inverseTransform = transform.createInverse();
      }
      catch (NoninvertibleTransformException x)
      {
         throw new InternalException(x);
      }

      setLayout(null);
      // @test ------------------
      //      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

      state = IDLE;
      stepList = null;

      popupMenu = createPopupMenu();

      statusListeners = CollectionUtils.newList();

      addKeyListener(this);

      undoableEditSupport = new UndoableEditSupport();
   }

   private void adjustSize()
   {
      Dimension dim = diagram.getMaximumSize();
      Dimension size = new Dimension(
            Math.max(3000, dim.width + 200), Math.max(3000, dim.height + 200));
      // adjustments are done only to increase the size, not to decrease.
      if (this.size.getWidth() < size.getWidth() ||
          this.size.getHeight() < size.getHeight())
      {
         size.setSize(Math.max(size.getWidth(), this.size.getWidth()),
                      Math.max(size.getHeight(), this.size.getHeight()));
         this.size = size;
         setSize(size);
         setMinimumSize(size);
         setPreferredSize(size);
      }
   }

   /**
    *
    */
   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == selectAllItem)
      {
         selectAllSymbols();
      }
      else if (event.getSource() == copyItem)
      {
         copySelectedToClipboard();
      }
      else if (event.getSource() == cutItem)
      {
         cutSelectedToClipboard();
      }
      else if (event.getSource() == pasteItem)
      {
         prepareForPaste();
      }
      else if (event.getSource() == groupItem)
      {
         groupSelectedSymbols();
      }
      else if (event.getSource() == ungroupItem)
      {
         ungroupSelectedSymbols();
      }
      else if (event.getSource() == removeSymbolsItem)
      {
         removeSymbolForSelectedSymbols();
      }
      else if (event.getSource() == deleteAllItem)
      {
         deleteAllForSelectedSymbols();
      }

      repaint();
   }

   /**
    *
    */
   public void activateEditing(EditableSymbol editableSymbol)
   {
      if (!isReadOnly())
      {
         currentEditableSymbol = editableSymbol;
         add(currentSymbolEditor = editableSymbol.activateEditing());
         currentSymbolEditor.requestFocus();

         // Redraw
         validate();
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    * Must be overwritten if a subclass creates it own popupmenu.
    * Update the enable state for the menueitems and show the menu.
    */
   protected void activatePopupMenu(int x, int y)
   {
      // update item state
      boolean _noSymbolSelected = isNoSymbolSelected();
      boolean _manySymbolsSelected = isManySymbolsSelected();
      boolean _groupSymbolSelected = isGroupSymbolSelected();

      deactivateEditing();

      selectAllItem.setEnabled(true);

      copyItem.setEnabled(!isReadOnly() && !_noSymbolSelected);
      cutItem.setEnabled(!isReadOnly() && !_noSymbolSelected);
      pasteItem.setEnabled(!isReadOnly() && !isClipboardEmpty());

      groupItem.setEnabled(!isReadOnly() && _manySymbolsSelected);
      ungroupItem.setEnabled(!isReadOnly() && _groupSymbolSelected);

      removeSymbolsItem.setEnabled(!isReadOnly() && !_noSymbolSelected);
      deleteAllItem.setEnabled(!isReadOnly() && !_noSymbolSelected);

      // show popupMenu
      GUI.showPopup(popupMenu, this, x, y);
   }

   /**
    *
    */
   public void addSymbolToClipboard(NodeSymbol symbol)
   {
      Assert.isNotNull(symbol, "Added Symbol is not null.");
      Assert.isNotNull(clipboard, "Clipboard is not null.");

      clipboard.put(symbol, symbol);
      xClipboard = Math.min(xClipboard, symbol.getX());
      yClipboard = Math.min(yClipboard, symbol.getY());
   }

   /**
    *
    */
   public void addToStatusListeners(StatusListener statusListener)
   {
      statusListeners.add(statusListener);
   }

   /**
    *
    */
   public void addUndoableEditListener(UndoableEditListener listener)
   {
      undoableEditSupport.addUndoableEditListener(listener);
   }

   /**
    *
    */
   public void bottomAlign()
   {
      if (!isReadOnly() && selectedSymbols.size() > 1)
      {
         int _move = 0;
         int _yPos = 0;

         List _realSymbols = CollectionUtils.newList(50);
         Iterator _iterator = null;

         //         AlignSymbolsEdit _undoableEdit = null;

         // remove all connections
         _iterator = selectedSymbols.iterator();
         while (_iterator.hasNext())
         {
            Symbol _symbol = (Symbol) _iterator.next();
            if (!(_symbol instanceof ConnectionSymbol))
            {
               _realSymbols.add(_symbol);
            }
         }

         if (_realSymbols.size() > 1)
         {

            // sort symbols by its yPosition
            Collections.sort(_realSymbols, SymbolComparator.createComparatorForBottomSide());

            //          _undoableEdit = new AlignSymbolsEdit(this);

            // calculate the new space between the symbols
            Symbol _symbol = (Symbol) _realSymbols.get(_realSymbols.size() - 1);
            _yPos = _symbol.getBottom();

            // move the symbols
            _iterator = _realSymbols.iterator();
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               {
                  _move = _yPos - _symbol.getY() - _symbol.getHeight();
                  //              _undoableEdit.addMoveSymbolEdit(new MoveSymbolEdit(_symbol, 0, _move));
                  _symbol.move(0, _move);
               }
            }

            //      fireUndoableEdit(_undoableEdit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   public void copySelectedToClipboard()
   {
      resetClipboard();
      for (Iterator e = selectedSymbols.iterator(); e.hasNext();)
      {
         Symbol symbol = (Symbol) e.next();
         clipboard.put(symbol, symbol);
         xClipboard = Math.min(xClipboard, symbol.getX());
         yClipboard = Math.min(yClipboard, symbol.getY());
      }
      firePropertyChange(STRING_PROPERTY_CLIPBOARD_CONTENT, null, null);
   }

   /**
    *
    */
   protected JPopupMenu createPopupMenu()
   {
      popupMenu = new JPopupMenu();

      selectAllItem = new JMenuItem("Select All");

      selectAllItem.addActionListener(this);
      selectAllItem.setMnemonic('s');
      popupMenu.add(selectAllItem);
      popupMenu.addSeparator();

      copyItem = new JMenuItem("Copy");

      copyItem.addActionListener(this);
      copyItem.setMnemonic('y');
      popupMenu.add(copyItem);

      cutItem = new JMenuItem("Cut");

      cutItem.addActionListener(this);
      cutItem.setMnemonic('c');
      popupMenu.add(cutItem);

      pasteItem = new JMenuItem("Paste");
      pasteItem.addActionListener(this);
      pasteItem.setMnemonic('e');
      popupMenu.add(pasteItem);
      popupMenu.addSeparator();

      groupItem = new JMenuItem("Group");
      groupItem.addActionListener(this);
      groupItem.setMnemonic('g');
      popupMenu.add(groupItem);

      ungroupItem = new JMenuItem("Ungroup");
      ungroupItem.addActionListener(this);
      ungroupItem.setMnemonic('u');
      popupMenu.add(ungroupItem);
      popupMenu.addSeparator();

      removeSymbolsItem = new JMenuItem("Remove Symbol(s)");
      removeSymbolsItem.addActionListener(this);
      removeSymbolsItem.setMnemonic('r');
      popupMenu.add(removeSymbolsItem);

      deleteAllItem = new JMenuItem("Delete All");
      deleteAllItem.addActionListener(this);
      deleteAllItem.setMnemonic('a');
      popupMenu.add(deleteAllItem);

      return popupMenu;
   }

   /**
    *
    */
   public void cutSelectedToClipboard()
   {
      clipboard.clear();

      Symbol _symbol = null;

      xClipboard = Integer.MAX_VALUE;
      yClipboard = Integer.MAX_VALUE;

      for (Iterator e = selectedSymbols.iterator(); e.hasNext();)
      {
         _symbol = (Symbol) e.next();

         clipboard.put(_symbol, _symbol);

         xClipboard = Math.min(xClipboard, _symbol.getX());
         yClipboard = Math.min(yClipboard, _symbol.getY());
      }
      firePropertyChange(STRING_PROPERTY_CLIPBOARD_CONTENT, null, null);

      removeSymbolForSelectedSymbols();
   }

   /**
    *
    */
   public void deactivateEditing()
   {
      if (currentSymbolEditor != null)
      {
         currentEditableSymbol.deactivateEditing();
         remove(currentSymbolEditor);
         currentSymbolEditor = null;
         currentEditableSymbol = null;
      }
   }

   /**
    * Deletes the symbol of all selected symbols as well as their corresponding
    * model elements.
    */
   private void deleteAllForSelectedSymbols()
   {

      boolean _ignoreFlagWasSet = false;

      if (!isReadOnly())
      {
         if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this)
               , "You are going to delete one or more modelelements.\n\n" +
               "This operation cannot be undone. Continue?", "Modelelement Deletion",
               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
         {
            // clone the selected symbols to avoid conflicts in delete process
            List _selectionList = CollectionUtils.newList(selectedSymbols);
            Iterator _enum = _selectionList.iterator();

            try
            {
               _ignoreFlagWasSet = getIgnoreFireUndoEditRequests();
               if (!_ignoreFlagWasSet)
               {
                  setIgnoreFireUndoEditRequests(true);
               }
               setIgnoreRepaintRequests(true);

               // @todo (egypt): maybe we can just call delete iteratively

               // Delete all connections in the first step to avoid problems with
               // double delete() calls.
               // Remember a delete() of a symbol deletes also the associated connections.
               while (_enum.hasNext())
               {
                  try
                  {
                     Symbol _symbol = (Symbol) _enum.next();
                     if (_symbol instanceof AbstractConnectionSymbol)
                     {
                        _symbol.deleteAll();
                     }
                  }
                  catch (Exception _ex)
                  {
                     // ignore
                  }
               }

               // delete all 'real' symbols in the first step
               _enum = _selectionList.iterator();
               while (_enum.hasNext())
               {
                  try
                  {
                     Symbol _symbol = (Symbol) _enum.next();
                     if (!(_symbol instanceof AbstractConnectionSymbol))
                     {
                        _symbol.deleteAll();
                     }
                  }
                  catch (Exception _ex)
                  {
                     // ignore
                  }
               }

               selectedSymbols.clear();
            }
            catch (Exception _ex)
            {
               throw new InternalException(_ex);
            }
            finally
            {
               if (!_ignoreFlagWasSet)
               {
                  setIgnoreFireUndoEditRequests(false);
               }
               setIgnoreRepaintRequests(false);
               repaint();
            }
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    */
   protected void removeDanglingConnections(Collection symbols)
   {
      List groupSymbols = CollectionUtils.newList(symbols.size());
      List _potentialDanglingConnections = CollectionUtils.newList(symbols.size());
      boolean _parentGroupFound = false;
      boolean _danglingConnectionRemoved = false;

      try
      {
         // collect all GroupSymbols in a special container
         // (used to simplify the searching for dangling connections)

         for (Iterator i = symbols.iterator(); i.hasNext();)
         {
            Symbol symbol = (Symbol) i.next();
            if (symbol instanceof GroupSymbol)
            {
               groupSymbols.add(symbol);
            }
         }

         do
         {
            _danglingConnectionRemoved = false;
            _potentialDanglingConnections.clear();

            // collect all potential dangling connections in a special container

            for (Iterator i = symbols.iterator(); i.hasNext();)
            {
               Symbol symbol = (Symbol) i.next();
               if (symbol instanceof ConnectionSymbol)
               {
                  ConnectionSymbol connection = (ConnectionSymbol) symbol;
                  if ((!symbols.contains(connection.getFirstSymbol()))
                        || (!symbols.contains(connection.getSecondSymbol()))
                  )
                  {
                     _potentialDanglingConnections.add(connection);
                  }
               }
            }

            // remove "dangling connections" from the collection

            for (Iterator i = _potentialDanglingConnections.iterator(); i.hasNext();)
            {
               _parentGroupFound = true;
               ConnectionSymbol connection = (ConnectionSymbol) i.next();
               if (!symbols.contains(connection.getFirstSymbol()))
               {
                  // Hint: As second step we must check if the missing first symbol
                  //       is a child of a GroupSymbol that is copied
                  _parentGroupFound = false;

                  for (Iterator j = groupSymbols.iterator(); j.hasNext() && (!_parentGroupFound);)
                  {
                     _parentGroupFound = ((GroupSymbol) j.next()).isChildSymbol(connection.getFirstSymbol());
                  }
                  if (!_parentGroupFound)
                  {
                     symbols.remove(connection);
                     _danglingConnectionRemoved = true;
                  }

               }
               if (_parentGroupFound && !symbols.contains(connection.getSecondSymbol()))
               {
                  // Hint: As second step we must check if the missing second symbol
                  //       is a child of a GroupSymbol that is copied
                  _parentGroupFound = false;

                  for (Iterator j = groupSymbols.iterator(); j.hasNext() && (!_parentGroupFound);)
                  {
                     _parentGroupFound = ((GroupSymbol) j.next()).isChildSymbol(connection.getSecondSymbol());
                  }
                  if (!_parentGroupFound)
                  {
                     trace.debug("The clipboard contains a dangling connection! "
                           + connection.getClass() + " with id: " + connection.getElementOID() + " will be ignored!");
                     symbols.remove(connection);
                     _danglingConnectionRemoved = true;
                  }

               }
            }
         }
         while (_danglingConnectionRemoved);
      }
      catch (Exception _ex)
      {
         throw new InternalException(_ex);
      }
   }

   /**
    * removes the symbol of all selected symbols, but leaves their corresponding
    * model elements.
    */
   private void removeSymbolForSelectedSymbols()

   {
      boolean _ignoreFlagWasSet = false;

      // Record symbols and connections before really removing them
      if (!isReadOnly())
      {
         List _selectionList = CollectionUtils.newList(selectedSymbols);
         //UndoableEdit _edit = new RemoveSymbolsEdit(_selectionList);

         try
         {
            _ignoreFlagWasSet = getIgnoreFireUndoEditRequests();
            if (!_ignoreFlagWasSet)
            {
               setIgnoreFireUndoEditRequests(true);
            }
            setIgnoreRepaintRequests(true);

            for (Iterator _e = _selectionList.iterator(); _e.hasNext();)
            {
               ((Symbol) _e.next()).delete();
            }
         }
         catch (Exception _ex)
         {
            throw new InternalException(_ex);
         }
         finally
         {
            if (!_ignoreFlagWasSet)
            {
               setIgnoreFireUndoEditRequests(false);
            }
            setIgnoreRepaintRequests(false);
            // fireUndoableEdit(_edit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    * Fire a new undoable edit
    */
   public void fireUndoableEdit(UndoableEdit edit)
   {
      if ((undoableEditSupport != null)
            && (edit != null)
            && (ignoreFireUndoEditRequests == false)
            && (!isReadOnly())
      )
      {
         trace.debug("===[ fireUndoableEdit ]=== " + edit);

         undoableEditSupport.postEdit(edit);
      }
   }

   /**
    * Deselect all symbols.
    */
   public void deselectAllSymbols()
   {
      selectedSymbols.clear();

      try
      {
         setIgnoreRepaintRequests(true);

         for (java.util.Iterator e = getAllSymbols(); e.hasNext();)
         {
            ((Symbol) e.next()).setSelected(false);
         }
      }
      catch (Exception _ex)
      {
         throw new InternalException(_ex);
      }
      finally
      {
         setIgnoreRepaintRequests(false);
         repaint();
         firePropertyChange(STRING_PROPERTY_SELECTION, null, null);
      }
   }

   /**
    *
    */
   public Iterator getAllSymbols()
   {
      return diagram.getAllSymbols();
   }

   /**
    * @return java.awt.Color
    */
   protected Color getBackgroundColorForReadOnly()
   {
      return DEFAULT_READONLY_COLOR;
   }

   /**
    *
    */
   public Diagram getDiagram()
   {
      return diagram;
   }

   /**
    *
    */
   private Symbol getHitSymbol(int x, int y)
   {
      for (java.util.Iterator e = getAllSymbols(); e.hasNext();)
      {
         Symbol symbol = (Symbol) e.next();

         if (symbol.isHitBy(x, y))
         {
            return symbol;
         }
      }

      return null;
   }

   /** */
   protected boolean getIgnoreFireUndoEditRequests()
   {
      return ignoreFireUndoEditRequests;
   }

   /** */
   protected boolean getIgnoreRepaintRequests()
   {
      return ignoreRepaintRequests;
   }

   /**
    *
    */
   public Object getUserObject()
   {
      return userObject;
   }

   /**
    *
    */
   public double getZoomFactor()
   {
      return zoomFactor;
   }

   /**
    *
    */
   public void groupSelectedSymbols()
   {
      if (!isReadOnly())
      {
         groupSelectedSymbolsAction();
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   private void groupSelectedSymbolsAction()
   {
      GroupSymbol _groupSymbol = null;
      List _groupList = CollectionUtils.newList(30);
      Iterator _symbolIterator = null;
      NodeSymbol _symbol = null;
      ConnectionSymbol _connection = null;
      boolean _ignoreFlagWasSet = false;

      try
      {
         setIgnoreRepaintRequests(true);
         _ignoreFlagWasSet = getIgnoreFireUndoEditRequests();
         if (!_ignoreFlagWasSet)
         {
            setIgnoreFireUndoEditRequests(true);
         }

         _groupSymbol = GroupSymbol.createGroupSymbol(getDiagram(), selectedSymbols);

         selectedSymbols.clear();
         _groupSymbol.setSelected(true);
         selectedSymbols.add(_groupSymbol);
      }
      catch (Exception _ex)
      {
         throw new InternalException(_ex);
      }
      finally
      {
         if (!_ignoreFlagWasSet)
         {
            setIgnoreFireUndoEditRequests(false);
         }
         fireUndoableEdit(new GroupSymbolsEdit(_groupSymbol));

         setIgnoreRepaintRequests(false);
         repaint();
      }

   }

   /**
    *
    */
   public void horizontalCenter()
   {
      if (!isReadOnly() && selectedSymbols.size() > 1)
      {
         int _topSide = 0;
         int _bottomSide = 0;
         int _move = 0;
         int _yPos = 0;

         List _realSymbols = CollectionUtils.newList(50);
         Iterator _iterator = null;

         //   AlignSymbolsEdit _undoableEdit = null;

         // remove all connections
         _iterator = selectedSymbols.iterator();
         while (_iterator.hasNext())
         {
            Symbol _symbol = (Symbol) _iterator.next();
            if (!(_symbol instanceof ConnectionSymbol))
            {
               _realSymbols.add(_symbol);
            }
         }

         if (_realSymbols.size() > 1)
         {

            // sort symbols by its yPosition
            Collections.sort(_realSymbols, SymbolComparator.createComparatorForYPosition());

            //      _undoableEdit = new AlignSymbolsEdit(this);

            // calculate the new space between the symbols
            Symbol _symbol = (Symbol) _realSymbols.get(0);
            _topSide = _symbol.getTop();

            _symbol = (Symbol) _realSymbols.get(_realSymbols.size() - 1);
            _bottomSide = _symbol.getBottom();

            _yPos = _topSide + (_bottomSide - _topSide) / 2;

            // move the symbols
            _iterator = _realSymbols.iterator();
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               {
                  _move = _yPos - _symbol.getY() - (_symbol.getHeight() / 2);
                  //          _undoableEdit.addMoveSymbolEdit(new MoveSymbolEdit(_symbol, 0, _move));
                  _symbol.move(0, _move);
               }
            }

            //  fireUndoableEdit(_undoableEdit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   public void horizontalDistribute()
   {
      if (!isReadOnly() && selectedSymbols.size() > 1)
      {
         int _leftSide = 0;
         int _rightSide = 0;
         int _horicontalSpace = 0;
         int _gapBetweenSymbols = 0;
         int _move = 0;
         int _xPos = 0;
         int _countSymbols = 0;

         List _realSymbols = CollectionUtils.newList(50);
         Iterator _iterator = null;

         //         AlignSymbolsEdit _undoableEdit = null;

         // remove all connections
         _iterator = selectedSymbols.iterator();
         while (_iterator.hasNext())
         {
            Symbol _symbol = (Symbol) _iterator.next();
            if (!(_symbol instanceof ConnectionSymbol))
            {
               _realSymbols.add(_symbol);
            }
         }

         _countSymbols = _realSymbols.size();
         if (_countSymbols > 2)
         {

            // sort symbols by its xPosition
            Collections.sort(_realSymbols, SymbolComparator.createComparatorForXPosition());

            //            _undoableEdit = new AlignSymbolsEdit(this);

            // calculate the new space between the symbols
            Symbol _symbol = (Symbol) _realSymbols.get(0);
            _leftSide = _symbol.getLeft();

            _symbol = (Symbol) _realSymbols.get(_realSymbols.size() - 1);
            _rightSide = _symbol.getRight();

            _horicontalSpace = _rightSide - _leftSide;

            _iterator = _realSymbols.iterator();
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               _horicontalSpace = _horicontalSpace - _symbol.getWidth();
            }

            _gapBetweenSymbols = _horicontalSpace / (_countSymbols - 1);

            // move the symbols
            _iterator = _realSymbols.iterator();
            _xPos = _leftSide;
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               {
                  _move = _xPos - _symbol.getX();
                  //_undoableEdit.addMoveSymbolEdit(new MoveSymbolEdit(_symbol, _move, 0));
                  _symbol.move(_move, 0);

                  _xPos = _xPos + _symbol.getWidth() + _gapBetweenSymbols;
               }
            }

            //fireUndoableEdit(_undoableEdit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    * Returns true if the clipboard is empty
    */
   public boolean isClipboardEmpty()
   {
      if (clipboard != null)
      {
         return clipboard.isEmpty();
      }
      else
      {
         return false;
      }
   }

   /**
    * Needed to use the panel as a key listener.
    */
   public boolean isFocusTraversable()
   {
      return true;
   }

   /**
    * Returns true if a goupsymbol is selected otherwise false
    */
   public boolean isGroupSymbolSelected()
   {
      if (selectedSymbols != null)
      {
         return (selectedSymbols.size() == 1)
               && (selectedSymbols.get(0) instanceof GroupSymbol);
      }
      else
      {
         return false;
      }
   }

   /**
    * Returns true if more then one symbol is selected otherwise false
    */
   public boolean isManySymbolsSelected()
   {
      if (selectedSymbols != null)
      {
         return (selectedSymbols.size() > 1);
      }
      else
      {
         return false;
      }
   }

   /**
    * Returns true if no symbol is selected otherwise false
    */
   public boolean isNoSymbolSelected()
   {
      if (selectedSymbols != null)
      {
         return selectedSymbols.isEmpty();
      }
      else
      {
         return false;
      }
   }

   /**
    * @return boolean
    */
   public boolean isReadOnly()
   {
      return readOnly;
   }

   /**
    *
    */
   public void keyPressed(KeyEvent e)
   {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
      {
         deselectAllSymbols();
      }
      if (e.getKeyCode() == KeyEvent.VK_DELETE ||
            e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
      {
         removeSymbolForSelectedSymbols();
      }
      else if (e.getKeyCode() == KeyEvent.VK_LEFT &&
            e.isShiftDown())
      {
         moveSelectedSymbols(-1, 0);
      }
      else if (e.getKeyCode() == KeyEvent.VK_RIGHT &&
            e.isShiftDown())
      {
         moveSelectedSymbols(1, 0);
      }
      else if (e.getKeyCode() == KeyEvent.VK_UP &&
            e.isShiftDown())
      {
         moveSelectedSymbols(0, -1);
      }
      else if (e.getKeyCode() == KeyEvent.VK_DOWN &&
            e.isShiftDown())
      {
         moveSelectedSymbols(0, 1);
      }
      else if (e.getKeyCode() == KeyEvent.VK_DOWN &&
            e.isShiftDown())
      {
         moveSelectedSymbols(0, 1);
      }
   }

   /**
    *
    */
   public void keyReleased(KeyEvent e)
   {
   }

   /**
    *
    */
   public void keyTyped(KeyEvent e)
   {
   }

   /**
    *
    */
   public void leftAlign()
   {
      if (!isReadOnly() && selectedSymbols.size() > 1)
      {
         int _move = 0;
         int _xPos = 0;

         List _realSymbols = CollectionUtils.newList(50);
         Iterator _iterator = null;
         Symbol _symbol = null;

         //AlignSymbolsEdit _undoableEdit = null;

         // remove all connections
         _iterator = selectedSymbols.iterator();
         while (_iterator.hasNext())
         {
            _symbol = (Symbol) _iterator.next();
            if (!(_symbol instanceof ConnectionSymbol))
            {
               _realSymbols.add(_symbol);
            }
         }

         if (_realSymbols.size() > 1)
         {

            // sort symbols by its xPosition
            Collections.sort(_realSymbols, SymbolComparator.createComparatorForXPosition());

            //_undoableEdit = new AlignSymbolsEdit(this);

            // calculate the new space between the symbols
            _symbol = (Symbol) _realSymbols.get(0);
            _xPos = _symbol.getLeft();
            // move the symbols
            _iterator = _realSymbols.iterator();
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               {
                  _move = _xPos - _symbol.getX();
                  //_undoableEdit.addMoveSymbolEdit(new MoveSymbolEdit(_symbol, _move, 0));
                  _symbol.move(_move, 0);
               }
            }

            //fireUndoableEdit(_undoableEdit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }

   }

   /**
    *
    */
   public void mouseClicked(MouseEvent event)
   {
   }

   /**
    *
    */
   public void mouseDragged(MouseEvent event)

   {
      int minX = 0;
      int minY = 0;
      int moveX = 0;
      int moveY = 0;
      boolean needAdjustment = false;

      translateEvent(event);

      switch (state)
      {
         case RUBBER_BAND:
            {
               break;
            }
         case SYMBOLS_SELECTED:
            {
               if (isReadOnly())
               {
                  getToolkit().beep();
                  state = IDLE;
               }
               else
               {
                  if (selectedSymbols.size() == 1)
                  {
                     ((Symbol) selectedSymbols.get(0)).mouseDragged(event, lastXMousePos, lastYMousePos);
                     needAdjustment = true;
                  }
                  else
                  {
                     for (Iterator itr = selectedSymbols.iterator(); itr.hasNext();)
                     {
                        Symbol symbol = (Symbol) itr.next();
                        // (fh) do not move the connection symbols
                        if (shouldBeMoved(selectedSymbols, symbol))
                        {
                           symbol.move(event.getX() - lastXMousePos, event.getY() - lastYMousePos);
                           needAdjustment = true;
                        }
                        minX = Math.min(minX, symbol.getLeft());
                        minY = Math.min(minY, symbol.getTop());
                     }
                  }

                  // If a symbol is moved to negative position, adjust the positions
                  if ((minX < 0) || (minY < 0))
                  {
                     if (minX < 0)
                     {
                        moveX = Math.abs(minX);
                     }
                     if (minY < 0)
                     {
                        moveY = Math.abs(minY);
                     }
                     for (Iterator itr = selectedSymbols.iterator(); itr.hasNext();)
                     {
                        Symbol symbol = (Symbol) itr.next();
                        if (symbol instanceof AbstractNodeSymbol)
                        {
                           symbol.move(moveX, moveY);
                           needAdjustment = true;
                        }
                     }
                  }
               }
               break;
            }
      }

      lastXMousePos = event.getX();
      lastYMousePos = event.getY();

      if (needAdjustment)
      {
         adjustSize();
      }

      repaint();
   }

   private boolean shouldBeMoved(List selectedSymbols, Symbol symbol)
   {
      // (fh) a symbol should be moved if it's not a connection or
      // if both connected symbols are moved too.
      if (symbol instanceof AbstractConnectionSymbol)
      {
         AbstractConnectionSymbol cs = (AbstractConnectionSymbol) symbol;
         return selectedSymbols.contains(cs.getFirstSymbol()) &&
                selectedSymbols.contains(cs.getSecondSymbol());
      }
      return true;
   }

   /**
    *
    */
   public void mouseEntered(MouseEvent event)
   {
   }

   /**
    *
    */
   public void mouseExited(MouseEvent event)
   {
   }

   /**
    *
    */
   public void mouseMoved(MouseEvent event)
   {
      translateEvent(event);

      switch (state)
      {
         case CONNECTION_DEFINITION:
            {
               setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
               lastXMousePos = event.getX();
               lastYMousePos = event.getY();
               repaint();
               break;
            }
         case IDLE:
         case SYMBOLS_SELECTED:
            {
               Iterator e = getAllSymbols();
               while (e.hasNext())
               {
                  Symbol symbol = (Symbol) e.next();

                  if (symbol.isHitBy(event.getX(), event.getY()))
                  {
                     symbol.mouseMoved(event);

                     return;
                  }
               }

               setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               break;
            }
      }
   }

   public void mousePressed(MouseEvent event)
   {
      Iterator _enum = null;
      Symbol _symbol = null;
      boolean _selectedSymbolIsHit = false;

      translateEvent(event);

      // Get the focus to handle subsequent key events

      requestFocus();

      if ((event.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK && (event.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
      {
         // Right presses are ignored at this point

         return;
      }

      lastXPress = lastXMousePos = event.getX();
      lastYPress = lastYMousePos = event.getY();

      deactivateEditing();

      switch (state)
      {
         case IDLE:

            {
               // Select symbols
               if (!selectSymbols(lastXPress, lastYPress))
               {
                  state = RUBBER_BAND;
               }
               else
               {
                  if (event.getClickCount() == 2 && selectedSymbols.size() == 1)
                  {
                     ((Symbol) selectedSymbols.get(0)).onDoubleClick(lastXPress, lastYPress);
                  }
                  state = SYMBOLS_SELECTED;
                  _enum = selectedSymbols.iterator();
                  while (_enum.hasNext())
                  {
                     ((Symbol) _enum.next()).onPress(event);
                  }
                  repaint();
               }
               break;
            }
         case SYMBOLS_SELECTED:
            {
               _selectedSymbolIsHit = false;
               _enum = selectedSymbols.iterator();
               while (_enum.hasNext() && _selectedSymbolIsHit == false)
               {
                  _symbol = (Symbol) _enum.next();
                  _selectedSymbolIsHit = _symbol.isHitBy(lastXPress, lastYPress);
               }

               if (_selectedSymbolIsHit == false)
               {
                  // handle event in the same way as there were no selected symbols
                  deselectAllSymbols();
                  state = IDLE;
                  mousePressed(event);
               }
               else
               {
                  if (event.getClickCount() == 2 && selectedSymbols.size() == 1)
                  {
                     ((Symbol) selectedSymbols.get(0)).onDoubleClick(lastXPress, lastYPress);
                  }
                  _enum = selectedSymbols.iterator();
                  while (_enum.hasNext())
                  {
                     ((Symbol) _enum.next()).onPress(event);
                  }
                  repaint();
               }
               break;
            }
         case SYMBOL_DEFINITION_START:

            {
               diagram.addToNodes(newSymbol, 0);
               if (!newSymbol.setPoint(lastXPress, lastYPress))
               {
                  // hint: isn't undoable because creation of userobject
                  setIdle();
                  setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               }
               else
               {
                  state = SYMBOL_DEFINITION;
               }
               adjustSize();
               repaint();
               break;
            }
         case SYMBOL_DEFINITION:

            {
               if (!newSymbol.setPoint(lastXPress, lastYPress))
               {
                  // hint: isn't undoable because creation of userobject
                  setIdle();
                  setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               }
               adjustSize();
               repaint();
               break;
            }
         case CONNECTION_DEFINITION:

            {
               Symbol hitSymbol = getHitSymbol(lastXPress, lastYPress);
               if (hitSymbol != null)
               {
                  try
                  {
                     newConnection.setSecondSymbol(hitSymbol);
                     if (newConnection.getSecondSymbol() != null &&
                           newConnection.getSecondSymbol().equals(hitSymbol))
                     {
                        diagram.addToConnections(newConnection, 0);
                        // hint: isn't undoable because creation of userobject
                     }
                     else
                     {
                        throw new InternalException("Connection was not established.");
                     }
                  }
                  catch (Exception _ex)
                  {
                     trace.warn("", _ex);
                     setIdle();
                     notifyAllStatusListeners(_ex.getMessage(), true);
                     break;
                  }
               }
               setIdle();
               break;
            }
         case TRAVERSE_PATH:
            {
               Object[] previousObjects = (Object[]) stepList.get(traverseIndex - 1);
               Object[] currentObjects = (Object[]) stepList.get(traverseIndex);
               Symbol previousSymbol = (Symbol) previousObjects[1];
               ConnectionSymbol currentConnection = (ConnectionSymbol) currentObjects[0];
               NodeSymbol currentSymbol = (NodeSymbol) currentObjects[1];
               diagram.addToConnections(currentConnection, currentConnection.getElementOID());
               diagram.addToNodes(currentSymbol, currentSymbol.getElementOID());
               if (currentSymbol.setPoint(lastXPress, lastYPress))
               {
                  Assert.lineNeverReached();
               }
               if (currentObjects[2] == null)
               {
                  currentConnection.setFirstSymbol(previousSymbol);
                  currentConnection.setSecondSymbol(currentSymbol, false);
               }
               else
               {
                  currentConnection.setFirstSymbol(currentSymbol);
                  currentConnection.setSecondSymbol(previousSymbol, false);
               }
               if (traverseIndex == stepList.size() - 1)
               {
                  stepList = null;
                  setIdle();
                  setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               }
               else
               {
                  ++traverseIndex;
               }
               repaint();
               break;
            }
         case PREPARED_FOR_ZOOM_IN:

            {
               state = IDLE;
               setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               transform.translate((-1.0) * lastXPress, (-1.0) * lastYPress);
               transform.scale(ZOOM_FACTOR, ZOOM_FACTOR);
               transform.translate(lastXPress / ZOOM_FACTOR, 1.0 * lastYPress / ZOOM_FACTOR);
               try
               {
                  inverseTransform = transform.createInverse();
               }
               catch (NoninvertibleTransformException x)
               {
                  throw new InternalException(x);
               }
               repaint();
               break;
            }
         case PREPARED_FOR_ZOOM_OUT:

            {
               state = IDLE;
               setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               transform.translate((-1.0) * lastXPress, (-1.0) * lastYPress);
               transform.scale(1 / ZOOM_FACTOR, 1 / ZOOM_FACTOR);
               transform.translate(lastXPress * ZOOM_FACTOR, lastYPress * ZOOM_FACTOR);
               try
               {
                  inverseTransform = transform.createInverse();
               }
               catch (NoninvertibleTransformException x)
               {
                  throw new InternalException(x);
               }
               repaint();
               break;
            }
         case PREPARED_FOR_PASTE:

            {
               state = IDLE;
               setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               deselectAllSymbols();
               pasteFromClipboard(lastXPress, lastYPress);
               repaint();
               break;
            }
      }
   }

   /**
    *
    */
   public void mouseReleased(MouseEvent event)
   {
      translateEvent(event);

      int screenX = event.getX();
      int screenY = event.getY();

      int currentX = event.getX();
      int currentY = event.getY();

      if ((event.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK &&
            (event.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
      {
         // rigth mouse button released
         deactivateEditing();

         Symbol hitSymbol = getHitSymbol(currentX, currentY);

         if ((hitSymbol != null)
               && (!selectedSymbols.contains(hitSymbol)
               || (selectedSymbols.size() == 1)
               )
         )
         {
            // show popupmenu of the symbol
            hitSymbol.activatePopupMenu(this, screenX, screenY);
         }
         else
         {
            // show popupmenu of the DrawArea
            activatePopupMenu(screenX, screenY);
         }
      }
      else if (state == RUBBER_BAND)
      {
         for (Iterator e = getAllSymbols(); e.hasNext();)
         {
            Symbol symbol = (Symbol) e.next();

            if (symbol.isContainedIn(Math.min(lastXPress, currentX), Math.min(lastYPress, currentY)
                  , Math.max(lastXPress, currentX), Math.max(lastYPress, currentY))
            )
            {
               symbol.setSelected(true);
               selectedSymbols.add(symbol);
            }
         }
         firePropertyChange(STRING_PROPERTY_SELECTION, null, null);

         if (selectedSymbols.size() == 0)
         {
            state = IDLE;
         }
         else
         {
            state = SYMBOLS_SELECTED;
         }

         repaint();
      }
      else
      {
         if (lastXPress != currentX ||
               lastYPress != currentY)
         {
            // Log symbol move for undo; cannot do that in the drag event, because each drag event
            // is an incremental part of the whole drag
            //            fireUndoableEdit(new MoveSymbolsEdit(selectedSymbols
            //                  , currentX - lastXPress
            //                  , currentY - lastYPress));
         }
      }
   }

   /**
    * Moves all selected symbols and log undo object.
    */
   public void moveSelectedSymbols(int x, int y)
   {
      if (!isReadOnly())
      {
         // check if at least one "real" symbol (no connection) is moved
         // otherwise don't create a new 'undoableEdit'
         if (moveSelectedSymbolsAction(x, y))
         {
            //            fireUndoableEdit(new MoveSymbolsEdit(selectedSymbols, x, y));
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    * Moves all selected symbols.
    * Returns true if at least one Symbol was moved otherwise false.
    * (remember connections can't be moved).
    */
   private boolean moveSelectedSymbolsAction(int x, int y)
   {
      boolean symbolMoved = false;
      if (!isReadOnly())
      {
         Iterator list = selectedSymbols.iterator();
         while (list.hasNext())
         {
            Symbol symbol = (Symbol) list.next();
            // (fh) connections should not be moved
            if (shouldBeMoved(selectedSymbols, symbol))
            {
               symbol.move(x, y);
               symbolMoved = true;
            }
         }
         adjustSize();
         repaint();
      }
      else
      {
         getToolkit().beep();
      }
      return symbolMoved;
   }

   /**
    *
    */
   public void notifyAllStatusListeners(String message, boolean beep)
   {
      Iterator e = statusListeners.iterator();
      while (e.hasNext())
      {
         ((StatusListener) e.next()).notify(message, beep);
      }
   }

   /**
    *
    */
   public void paint(Graphics g)
   {
      Graphics2D graphics = (Graphics2D) g;

      // Set rendering hints

      //  graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // hint: Don't call transform() before super.paint() otherwise
      //       painting problems occour for zoom = 10%
      super.paint(g);

      graphics.transform(transform);

      switch (state)
      {
         case CONNECTION_DEFINITION:
            {
               // Draw the connection-rubberband

               graphics.setColor(Color.lightGray);
               graphics.drawLine(lastXPress, lastYPress, lastXMousePos, lastYMousePos);
               break;
            }
         case RUBBER_BAND:
            {
               // Draw the rubberband-rectangle

               graphics.setColor(Color.lightGray);
               graphics.drawRect(Math.min(lastXPress, lastXMousePos)
                     , Math.min(lastYPress, lastYMousePos)
                     , Math.abs(lastXMousePos - lastXPress)
                     , Math.abs(lastYMousePos - lastYPress));

               break;
            }

      }

      for (Iterator e = getAllSymbols(); e.hasNext();)
      {
         Symbol symbol = (Symbol) e.next();
         symbol.draw(graphics);
      }

      // For text symbols
      paintChildren(graphics);
   }

   /**
    *
    */
   public void pasteFromClipboard(int x, int y)
   {
      // @optimize ...  realization of symbolgroups with the goal to find a simple
      //                way for copying (hard to find the copy for a child)
      //                Maybe it is easier to see a symbolgroup as a collection
      //                of symbols and not itself as a real symbol.

      Hashtable copies = new Hashtable();

      boolean ignoreFlagWasSet = false;

      if (isReadOnly())
      {
         getToolkit().beep();
         return;
      }

      try
      {
         setIgnoreRepaintRequests(true);
         ignoreFlagWasSet = getIgnoreFireUndoEditRequests();
         if (!ignoreFlagWasSet)
         {
            setIgnoreFireUndoEditRequests(true);
         }

         // remove "dangling connections" from the clipboard
         removeDanglingConnections(clipboard.values());

         // create _copies from the symbols

         for (Iterator i = clipboard.values().iterator(); i.hasNext();)
         {
            Symbol symbol = (Symbol) i.next();
            copies.put(new Integer(symbol.getElementOID()), symbol.copySymbol());
         }

         // place the _copies at the DrawArea
         // hint: remember to connect the connection copies with the copies
         //       of the originally connected symbols
         for (Iterator i = clipboard.values().iterator(); i.hasNext();)
         {
            Symbol symbol = (Symbol) i.next();
            Symbol symbolCopy = (Symbol) copies.get(new Integer(symbol.getElementOID()));

            symbolCopy.setSelected(true);

            if (symbol instanceof ConnectionSymbol)
            {
               ConnectionSymbol connection = (ConnectionSymbol) symbol;
               diagram.addToConnections(connection, 0);

               ConnectionSymbol connectionCopy = (ConnectionSymbol) copies.get(new Integer(connection.getElementOID()));

               // connect the connection copy with the copy of the originally connected first symbol
               symbolCopy = GroupSymbol.findCopyForChild(connection.getFirstSymbol(), clipboard.values(), copies);

               connectionCopy.setFirstSymbol(symbolCopy);

               // connect the connection copy with the copy of the originally connected second symbol
               symbolCopy = GroupSymbol.findCopyForChild(connection.getSecondSymbol(), clipboard.values(), copies);

               connectionCopy.setSecondSymbol(symbolCopy, false);
            }
            else
            {
               diagram.addToNodes((NodeSymbol) symbolCopy, 0);
               if (symbolCopy.setPoint(symbol.getX() + x - xClipboard, symbol.getY() + y - yClipboard))
               {
                  Assert.lineNeverReached();
               }
            }
         }

         // create the undoableEdit
         // Hint: Wait until the connections are connected with the new symbolcopies
         //_edit = new PasteSymbolsEdit(_copies.values());

      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
      finally
      {
         if (!ignoreFlagWasSet)
         {
            setIgnoreFireUndoEditRequests(false);
         }

         //fireUndoableEdit(_edit);

         refreshSelectedSymbols();

         setIgnoreRepaintRequests(false);
         repaint();
      }
   }

   /**
    *
    */
   public void placeSymbol(NodeSymbol symbol, int x, int y)
   {
      if (!isReadOnly())
      {
         diagram.addToNodes(symbol, symbol.getElementOID());
         symbol.setDiagram(diagram);
         if (symbol.setPoint(x, y))
         {
            throw new InternalException("Symbol definition requires more than one point.");
         }
         fireUndoableEdit(new CreateSymbolEdit(symbol));
         adjustSize();

         repaint();
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   public void prepareForPaste()
   {
      state = PREPARED_FOR_PASTE;

      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
   }

   /**
    *
    */
   public void prepareForZoomIn()
   {
      state = PREPARED_FOR_ZOOM_IN;

      setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
   }

   /**
    *
    */
   public void prepareForZoomOut()
   {
      state = PREPARED_FOR_ZOOM_OUT;

      setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
   }

   /**
    * Method for printig the drawArea. This method do only the printing,
    * but not the page handling.
    *
    * @see java.awt.print.Printable
    * @see java.awt.print.Pageable
    */
   public void print(Graphics g, PageFormat pageFormat)
   {
      // @todo ... support more than one page
      // hava a look at http://java.sun.com/products/java-media/2D/forDevelopers/sdk12print.html#pfdialog
      // hint: The current implementation print the whole diagram at one page

      Graphics2D graphics = (Graphics2D) g;

      // Determine size of diagram
      int left = Integer.MAX_VALUE;
      int right = Integer.MIN_VALUE;
      int top = Integer.MAX_VALUE;
      int bottom = Integer.MIN_VALUE;

      for (java.util.Iterator e = getAllNodes(); e.hasNext();)
      {
         // hint: The PathConnections have a problem to calculate its size.
         //       Thats why we will ignore any connection for calculating the
         //       diagram size.
         NodeSymbol _symbol = (NodeSymbol) e.next();
         left = Math.min(left, _symbol.getLeft());
         right = Math.max(right, _symbol.getRight());
         top = Math.min(top, _symbol.getTop());
         bottom = Math.max(bottom, _symbol.getBottom());
      }

      Dimension _diagramSize = new Dimension(right - left + PRINT_INSET_LEFT + PRINT_INSET_RIGTH
            , bottom - top + PRINT_INSET_TOP + PRINT_INSET_BOTTOM);

      AffineTransform _printTransform = AffineTransform.getTranslateInstance(pageFormat.getImageableX()
            , pageFormat.getImageableY());

      // Scale diagram with DEFAULT_PRINTING_SCALE or fit to page scale
      double _pageScale = Math.min(pageFormat.getImageableWidth() / _diagramSize.getWidth()
            , pageFormat.getImageableHeight() / _diagramSize.getHeight());
      _pageScale = Math.min(_pageScale, DEFAULT_PRINTING_SCALE);
      _printTransform.scale(_pageScale, _pageScale);

      graphics.transform(_printTransform);

      for (Iterator e = getAllSymbols(); e.hasNext();)
      {
         Symbol symbol = (Symbol) e.next();

         symbol.draw(graphics);
      }
      // For text symbols

      paintChildren(graphics);

      // undo the grahic transformation
      try
      {
         graphics.transform(_printTransform.createInverse());
      }
      catch (Exception _ex)
      {
         // intentionally left empty
      }

   }

   public Iterator getAllNodes()
   {
      return diagram.getAllNodeSymbols();
   }

   /**
    * Search all symbols with state selected. The previous selection is cleared before.
    */
   protected void refreshSelectedSymbols()
   {
      Iterator _symbolIterator = getAllSymbols();

      selectedSymbols.clear();

      while (_symbolIterator.hasNext())
      {
         Symbol _symbol = (Symbol) _symbolIterator.next();
         if (_symbol.getSelected())
         {
            selectedSymbols.add(_symbol);
         }
      }
      repaint();
      firePropertyChange(STRING_PROPERTY_SELECTION, null, null);
   }

   /**
    *
    */
   public void removeFromStatusListeners(StatusListener statusListener)
   {
      statusListeners.remove(statusListener);
   }

   /**
    *
    */
   public void removeUndoableEditListener(UndoableEditListener listener)
   {
      undoableEditSupport.removeUndoableEditListener(listener);
   }

   /**
    * Overwritten to optimze repaint request in complex operation
    */
   public void repaint()
   {
      if (!ignoreRepaintRequests)
      {
         super.repaint();
      }
   }

   /**
    *
    */
   public void resetClipboard()
   {
      if (!clipboard.isEmpty())
      {
         clipboard.clear();
      }
      xClipboard = Integer.MAX_VALUE;
      yClipboard = Integer.MAX_VALUE;
   }

   /**
    *
    */
   public void rightAlign()
   {
      if (!isReadOnly() && selectedSymbols.size() > 1)
      {
         int _move = 0;
         int _xPos = 0;

         List _realSymbols = CollectionUtils.newList(50);
         Iterator _iterator = null;
         Symbol _symbol = null;

         //AlignSymbolsEdit _undoableEdit = null;

         // remove all connections
         _iterator = selectedSymbols.iterator();
         while (_iterator.hasNext())
         {
            _symbol = (Symbol) _iterator.next();
            if (!(_symbol instanceof ConnectionSymbol))
            {
               _realSymbols.add(_symbol);
            }
         }

         if (_realSymbols.size() > 1)
         {

            // sort symbols by its xPosition
            Collections.sort(_realSymbols, SymbolComparator.createComparatorForRightSide());

            // _undoableEdit = new AlignSymbolsEdit(this);

            // calculate the new space between the symbols
            _symbol = (Symbol) _realSymbols.get(_realSymbols.size() - 1);
            _xPos = _symbol.getRight();
            // move the symbols
            _iterator = _realSymbols.iterator();
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               {
                  _move = _xPos - _symbol.getX() - _symbol.getWidth();
                  //     _undoableEdit.addMoveSymbolEdit(new MoveSymbolEdit(_symbol, _move, 0));
                  _symbol.move(_move, 0);
               }
            }

            //fireUndoableEdit(_undoableEdit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    * Selects all symbols. The previous selection is cleared before.
    */
   protected void selectAllSymbols()
   {
      deselectAllSymbols();

      for (Iterator e = getAllSymbols(); e.hasNext();)
      {
         Symbol symbol = (Symbol) e.next();

         symbol.setSelected(true);

         selectedSymbols.add(symbol);

      }
      if (selectedSymbols.size() == 0)
      {
         state = IDLE;
      }
      else
      {
         state = SYMBOLS_SELECTED;
      }

      repaint();
      firePropertyChange(STRING_PROPERTY_SELECTION, null, null);
   }

   /**
    * Selects all symbols, representing the user object <code>element</code>.
    * The previous selection is cleared before.
    */
   public void selectAllSymbols(Object element)
   {
      Assert.isNotNull(element);

      deselectAllSymbols();

      for (Iterator e = getAllSymbols(); e.hasNext();)
      {
         Symbol symbol = (Symbol) e.next();
         if (symbol.getUserObject() instanceof Iterator)
         {
            Iterator itr = (Iterator) symbol.getUserObject();
            while (itr.hasNext())
            {
               Object o = itr.next();
               if (element == o)
               {
                  symbol.setSelected(true);
                  selectedSymbols.add(symbol);
                  break;
               }
            }
         }
         else if (element == symbol.getUserObject())
         {
            symbol.setSelected(true);
            selectedSymbols.add(symbol);
         }
      }
      if (selectedSymbols.size() == 0)
      {
         state = IDLE;
      }
      else
      {
         state = SYMBOLS_SELECTED;
      }

      repaint();
      firePropertyChange(STRING_PROPERTY_SELECTION, null, null);
   }

   /**
    *
    */
   private boolean selectSymbols(int x, int y)
   {
      deselectAllSymbols();

      for (Iterator e = getAllSymbols(); e.hasNext();)
      {
         Symbol symbol = (Symbol) e.next();

         if (symbol.isHitBy(x, y))
         {
            symbol.setSelected(true);

            selectedSymbols.add(symbol);
            break;
         }
      }
      firePropertyChange(STRING_PROPERTY_SELECTION, null, null);

      return (selectedSymbols.size() > 0);
   }

   /**
    * Deselect all symbols, sets the state to idle, and notifies all status
    * listeners to clear their messages.
    */
   private void setIdle()
   {
      deselectAllSymbols();
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      newSymbol = null;
      state = IDLE;
   }

   /** */
   protected void setIgnoreFireUndoEditRequests(boolean ignore)
   {
      ignoreFireUndoEditRequests = ignore;
   }

   /** */
   protected void setIgnoreRepaintRequests(boolean ignore)
   {
      ignoreRepaintRequests = ignore;
   }

   /**
    * @param newReadOnly boolean
    */
   public void setReadOnly(boolean newReadOnly)
   {
      readOnly = newReadOnly;

      if (readOnly)
      {
         setBackground(getBackgroundColorForReadOnly());
      }
      else
      {
         setBackground(Color.white);
      }
   }

   /**
    *
    */
   public void setUserObject(Object userObject)
   {
      this.userObject = userObject;
   }

   /**
    *
    */
   public void setZoomFactor(double zoomFactor)
   {
      if (this.zoomFactor != zoomFactor)
      {
         this.zoomFactor = zoomFactor;

         transform.setToIdentity();
         transform.setToScale(zoomFactor, zoomFactor);

         try
         {
            inverseTransform = transform.createInverse();
         }
         catch (NoninvertibleTransformException x)
         {
            throw new InternalException(x);
         }

         repaint();
      }
   }

   /**
    *
    */
   public void startConnectionDefinition(ConnectionSymbol newConnection)
   {
      deactivateEditing();

      if (!isReadOnly() && (newConnection != null))
      {
         this.newConnection = newConnection;

         newConnection.setDiagram(diagram);

         Symbol firstSymbol = newConnection.getFirstSymbol();
         lastXPress = firstSymbol.getX() + (firstSymbol.getWidth() / 2);
         lastYPress = firstSymbol.getY() + (firstSymbol.getHeight() / 2);

         state = CONNECTION_DEFINITION;
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   public void startSymbolDefinition(NodeSymbol newSymbol)
   {
      deactivateEditing();

      if (!isReadOnly())
      {
         this.newSymbol = newSymbol;

         newSymbol.setDiagram(diagram);

         state = SYMBOL_DEFINITION_START;

         setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   public void topAlign()
   {
      if (!isReadOnly() && selectedSymbols.size() > 1)
      {
         int _move = 0;
         int _yPos = 0;

         List _realSymbols = CollectionUtils.newList(50);
         Iterator _iterator = null;
         Symbol _symbol = null;

         //AlignSymbolsEdit _undoableEdit = null;

         // remove all connections
         _iterator = selectedSymbols.iterator();
         while (_iterator.hasNext())
         {
            _symbol = (Symbol) _iterator.next();
            if (!(_symbol instanceof ConnectionSymbol))
            {
               _realSymbols.add(_symbol);
            }
         }

         if (_realSymbols.size() > 1)
         {

            // sort symbols by its yPosition
            Collections.sort(_realSymbols, SymbolComparator.createComparatorForYPosition());

            // _undoableEdit = new AlignSymbolsEdit(this);

            // calculate the new space between the symbols
            _symbol = (Symbol) _realSymbols.get(0);
            _yPos = _symbol.getTop();

            // move the symbols
            _iterator = _realSymbols.iterator();
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               {
                  _move = _yPos - _symbol.getY();
                  //      _undoableEdit.addMoveSymbolEdit(new MoveSymbolEdit(_symbol, 0, _move));
                  _symbol.move(0, _move);
               }
            }

            //fireUndoableEdit(_undoableEdit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   private void translateEvent(MouseEvent event)
   {
      double[] points = new double[]{event.getX(), event.getY()};

      inverseTransform.transform(points, 0, points, 0, 1);

      event.translatePoint((int) points[0] - event.getX(), (int) points[1] - event.getY());
   }

   /**
    *
    */
   public void traversePath(LinkedList stepList)
   {
      if (!isReadOnly())
      {
         this.stepList = stepList;

         if (stepList.size() < 2)
         {
            setIdle();
         }
         else
         {
            traverseIndex = 1;
            state = TRAVERSE_PATH;

            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   public void ungroupSelectedSymbols()
   {
      if (!isReadOnly())
      {
         ungroupSelectedSymbolsAction();
      }
      else
      {
         getToolkit().beep();
      }
   }

   /**
    *
    */
   public void ungroupSelectedSymbolsAction()
   {
      Iterator _childIterator = null;
      Iterator _e = selectedSymbols.iterator();
      List _buffer = CollectionUtils.newList();
      Symbol _symbol = null;
      Symbol _child = null;

      boolean _ignoreFlagWasSet = false;

      if (!isReadOnly())
      {
         try
         {
            setIgnoreRepaintRequests(true);
            while (_e.hasNext())
            {
               _symbol = (Symbol) _e.next();

               if (_symbol instanceof GroupSymbol)
               {
                  try
                  {
                     _ignoreFlagWasSet = getIgnoreFireUndoEditRequests();
                     if (!_ignoreFlagWasSet)
                     {
                        setIgnoreFireUndoEditRequests(true);
                     }

                     ((GroupSymbol) _symbol).ungroup();

                     // mark the children as selected
                     _childIterator = ((GroupSymbol) _symbol).getAllSymbols();
                     while (_childIterator.hasNext())
                     {
                        _child = (Symbol) _childIterator.next();
                        _buffer.add(_child);
                        _child.setSelected(true);
                     }
                     break;
                  }
                  finally
                  {
                     if (!_ignoreFlagWasSet)
                     {
                        setIgnoreFireUndoEditRequests(false);
                     }
                     fireUndoableEdit(new UngroupSymbolsEdit((GroupSymbol) _symbol));
                  }
               }
            }

            selectedSymbols.clear();

            _e = _buffer.iterator();
            while (_e.hasNext())
            {
               selectedSymbols.add(_e.next());
            }

         }
         catch (Exception _ex)
         {
            throw new InternalException(_ex);
         }
         finally
         {
            setIgnoreRepaintRequests(false);
            repaint();
         }

      }
      else
      {
         getToolkit().beep();
      }

   }

   /**
    *
    */
   public void verticalCenter()
   {
      if (!isReadOnly() && selectedSymbols.size() > 1)
      {
         int _leftSide = 0;
         int _rightSide = 0;
         int _move = 0;
         int _xPos = 0;

         List _realSymbols = CollectionUtils.newList(50);
         Iterator _iterator = null;
         Symbol _symbol = null;

         //AlignSymbolsEdit _undoableEdit = null;

         // remove all connections
         _iterator = selectedSymbols.iterator();
         while (_iterator.hasNext())
         {
            _symbol = (Symbol) _iterator.next();
            if (!(_symbol instanceof ConnectionSymbol))
            {
               _realSymbols.add(_symbol);
            }
         }

         if (_realSymbols.size() > 1)
         {

            // sort symbols by its xPosition
            Collections.sort(_realSymbols, SymbolComparator.createComparatorForXPosition());

            // _undoableEdit = new AlignSymbolsEdit(this);

            // calculate the new space between the symbols
            _symbol = (Symbol) _realSymbols.get(0);
            _leftSide = _symbol.getLeft();

            _symbol = (Symbol) _realSymbols.get(_realSymbols.size() - 1);
            _rightSide = _symbol.getRight();

            _xPos = _leftSide + (_rightSide - _leftSide) / 2;

            // move the symbols
            _iterator = _realSymbols.iterator();
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               {
                  _move = _xPos - _symbol.getX() - (_symbol.getWidth() / 2);
                  //_undoableEdit.addMoveSymbolEdit(new MoveSymbolEdit(_symbol, _move, 0));
                  _symbol.move(_move, 0);
               }
            }

            //fireUndoableEdit(_undoableEdit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }

   }

   /**
    *
    */
   public void verticalDistribute()
   {
      if (!isReadOnly() && selectedSymbols.size() > 1)
      {
         int _topSide = 0;
         int _bottomSide = 0;
         int _verticalSpace = 0;
         int _gapBetweenSymbols = 0;
         int _move = 0;
         int _yPos = 0;
         int _countSymbols = 0;

         List _realSymbols = CollectionUtils.newList(50);
         Iterator _iterator = null;
         Symbol _symbol = null;

         //AlignSymbolsEdit _undoableEdit = null;

         // remove all connections
         _iterator = selectedSymbols.iterator();
         while (_iterator.hasNext())
         {
            _symbol = (Symbol) _iterator.next();
            if (!(_symbol instanceof ConnectionSymbol))
            {
               _realSymbols.add(_symbol);
            }
         }

         _countSymbols = _realSymbols.size();
         if (_countSymbols > 2)
         {

            // sort symbols by its yPosition
            Collections.sort(_realSymbols, SymbolComparator.createComparatorForYPosition());

            //_undoableEdit = new AlignSymbolsEdit(this);

            // calculate the new space between the symbols
            _symbol = (Symbol) _realSymbols.get(0);
            _topSide = _symbol.getTop();

            _symbol = (Symbol) _realSymbols.get(_realSymbols.size() - 1);
            _bottomSide = _symbol.getBottom();

            _verticalSpace = _bottomSide - _topSide;

            _iterator = _realSymbols.iterator();
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               _verticalSpace = _verticalSpace - _symbol.getHeight();
            }

            _gapBetweenSymbols = _verticalSpace / (_countSymbols - 1);

            // move the symbols
            _iterator = _realSymbols.iterator();
            _yPos = _topSide;
            while (_iterator.hasNext())
            {
               _symbol = (Symbol) _iterator.next();
               {
                  _move = _yPos - _symbol.getY();
                  //    _undoableEdit.addMoveSymbolEdit(new MoveSymbolEdit(_symbol, 0, _move));
                  _symbol.move(0, _move);

                  _yPos = _yPos + _symbol.getHeight() + _gapBetweenSymbols;
               }
            }

            //fireUndoableEdit(_undoableEdit);
            repaint();
         }
      }
      else
      {
         getToolkit().beep();
      }
   }

   public DecorationStrategy getDecorationStrategy()
   {
      return decorationStrategy == null ?
            DefaultDecorationStrategy.instance() : decorationStrategy;
   }

   /**
    * Configures the drawing behavior of this draw area.
    *
    * @param decorationStrategy The custom draw behavior instance, or <code>null</code> to reset to
    *                           default behavior.
    */
   public void setDecorationStrategy(DecorationStrategy decorationStrategy)
   {
      this.decorationStrategy = decorationStrategy;
   }

   ///**
   // * We need to buffer implicitely removed connections to restore these.
   // *
   // * Attention: 	For correct functionality you must call the constructor
   // * 				before any remove operation because we must store the
   // *  				connections of a removed symbol and a remove operation
   // *					may remove same connections.
   // */
   //protected class RemoveSymbolsEdit extends AbstractUndoableEdit
   //{
   //   private Vector deletedSymbols;
   //   private Vector implicitelyRemovedConnections;
   //   private DrawArea drawArea;
   //
   //   public RemoveSymbolsEdit()
   //   {
   //      this(selectedSymbols);
   //   }
   //
   //   public RemoveSymbolsEdit(java.util.Collection symbolList)
   //   {
   //      this.deletedSymbols = new Vector(symbolList);
   //      implicitelyRemovedConnections = new Vector(30);
   //
   //      Iterator _iteratorSymbols = deletedSymbols.iterator();
   //      Iterator _iteratorSymbolConnections = null;
   //      Connection _connection = null;
   //      Symbol _symbol = null;
   //
   //      while (_iteratorSymbols.hasNext())
   //      {
   //         try
   //         {
   //
   //            _symbol = (ISymbol) _iteratorSymbols.next();
   //
   //            drawArea = _symbol.getDrawArea();
   //            _iteratorSymbolConnections = _symbol.getAllConnections();
   //
   //            while (_iteratorSymbolConnections.hasNext())
   //            {
   //               _connection = (Connection) _iteratorSymbolConnections.next();
   //
   //               if ((!deletedSymbols.contains(_connection))
   //                     && (!implicitelyRemovedConnections.contains(_connection))
   //               )
   //               {
   //                  implicitelyRemovedConnections.add(_connection);
   //               }
   //            }
   //         }
   //         catch (Exception _ex)
   //         {
   //            throw new InternalException(_ex);
   //         }
   //      }
   //   }
   //
   //   public void undo() throws CannotUndoException
   //   {
   //      boolean _ignoreFlagWasSet = false;
   //      try
   //      {
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //
   //         Iterator _iteratorSymbols = deletedSymbols.iterator();
   //         Iterator _iteratorRemovedConnections = implicitelyRemovedConnections.iterator();
   //         Connection _connection = null;
   //         Symbol _symbol = null;
   //
   //         super.undo();
   //
   //         // check if event can be undo
   //         if (_iteratorSymbols != null)
   //         {
   //            while (_iteratorSymbols.hasNext())
   //            {
   //               if (((ISymbol) _iteratorSymbols.next()).isDeleted())
   //               {
   //                  throw new CannotUndoException();
   //               }
   //            }
   //         }
   //         _iteratorSymbols = deletedSymbols.iterator();
   //
   //         // restore removed symbols
   //         while (_iteratorSymbols.hasNext())
   //         {
   //            try
   //            {
   //               _symbol = (ISymbol) _iteratorSymbols.next();
   //
   //               if (_symbol instanceof Connection)
   //               {
   //                  _connection = (Connection) _symbol;
   //
   //                  _connection.getSecondSymbol().addToConnections(_connection);
   //                  _connection.getFirstSymbol().addToConnections(_connection);
   //               }
   //               placeSymbol(_symbol, _symbol.getX(), _symbol.getY());
   //            }
   //            catch (Exception _ex)
   //            {
   //               throw new InternalException(_ex);
   //            }
   //         }
   //
   //         // Restore implicitely removed connections
   //         while (_iteratorRemovedConnections.hasNext())
   //         {
   //            try
   //            {
   //               _connection = (Connection) _iteratorRemovedConnections.next();
   //
   //               _connection.getSecondSymbol().addToConnections(_connection);
   //               _connection.getFirstSymbol().addToConnections(_connection);
   //               placeSymbol(_connection, _connection.getX(), _connection.getY());
   //            }
   //            catch (Exception _ex)
   //            {
   //               throw new InternalException(_ex);
   //            }
   //         }
   //
   //         repaint();
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public void redo() throws CannotRedoException
   //   {
   //      boolean _ignoreFlagWasSet = false;
   //      try
   //      {
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //
   //         super.redo();
   //
   //         // check if event can be undo
   //         Iterator _iteratorSymbols = deletedSymbols.iterator();
   //         if (_iteratorSymbols != null)
   //         {
   //            while (_iteratorSymbols.hasNext())
   //            {
   //               if (((ISymbol) _iteratorSymbols.next()).isDeleted())
   //               {
   //                  throw new CannotRedoException();
   //               }
   //            }
   //         }
   //         _iteratorSymbols = deletedSymbols.iterator();
   //
   //         if (_iteratorSymbols != null)
   //         {
   //            while (_iteratorSymbols.hasNext())
   //            {
   //               ((ISymbol) _iteratorSymbols.next()).delete();
   //            }
   //         }
   //
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getUndoPresentationName()
   //   {
   //      return "Undo Symbol Deletion";
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getRedoPresentationName()
   //   {
   //      return "Redo Symbol Deletion";
   //   }
   //}
   //
   ///**
   // *
   // */
   //protected class MoveSymbolEdit extends AbstractUndoableEdit
   //{
   //   private Symbol symbol;
   //   private int xMove;
   //   private int yMove;
   //   private DrawArea drawArea;
   //
   //   /**
   //    *
   //    */
   //   public MoveSymbolEdit(Symbol symbol, int xMove, int yMove)
   //   {
   //      this.symbol = symbol;
   //      this.xMove = xMove;
   //      this.yMove = yMove;
   //
   //      drawArea = symbol.getDrawArea();
   //   }
   //
   //   /**
   //    *
   //    */
   //   public void undo() throws CannotUndoException
   //   {
   //      boolean _ignoreFlagWasSet = false;
   //      try
   //      {
   //         super.undo();
   //
   //         if (symbol.isDeleted()
   //               || drawArea.getDiagram().contains(symbol) == false
   //         )
   //         {
   //            throw new CannotUndoException();
   //         }
   //
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //
   //         symbol.move(-xMove, -yMove);
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public void redo() throws CannotRedoException
   //   {
   //      boolean _ignoreFlagWasSet = false;
   //      try
   //      {
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //
   //         super.redo();
   //
   //         if (symbol.isDeleted())
   //         {
   //            throw new CannotRedoException();
   //         }
   //
   //         symbol.move(xMove, yMove);
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getUndoPresentationName()
   //   {
   //      return "Undo Symbol Move";
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getRedoPresentationName()
   //   {
   //      return "Redo Symbol Move";
   //   }
   //}
   //
   ///**
   // *
   // */
   //protected class MoveSymbolsEdit extends AbstractUndoableEdit
   //{
   //   private Vector symbols;
   //   private int xMove;
   //   private int yMove;
   //   private DrawArea drawArea;
   //
   //   /**
   //    *
   //    */
   //   public MoveSymbolsEdit(Collection symbols, int xMove, int yMove)
   //   {
   //      this.symbols = new Vector(symbols);
   //      this.xMove = xMove;
   //      this.yMove = yMove;
   //
   //      if (!symbols.isEmpty())
   //      {
   //         drawArea = ((ISymbol) symbols.iterator().next()).getDrawArea();
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public void undo() throws CannotUndoException
   //   {
   //      if (symbols.isEmpty())
   //      {
   //         return;
   //      }
   //      boolean _ignoreFlagWasSet = false;
   //      try
   //      {
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //
   //         super.undo();
   //
   //         for (Enumeration e = symbols.elements(); e.hasMoreElements();)
   //         {
   //            if (((ISymbol) e.nextElement()).isDeleted())
   //            {
   //               throw new CannotUndoException();
   //            }
   //         }
   //
   //         for (Enumeration e = symbols.elements(); e.hasMoreElements();)
   //         {
   //            ((ISymbol) e.nextElement()).move(-xMove, -yMove);
   //         }
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public void redo() throws CannotRedoException
   //   {
   //      if (symbols.isEmpty())
   //      {
   //         return;
   //      }
   //      boolean _ignoreFlagWasSet = false;
   //      try
   //      {
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //
   //         super.redo();
   //
   //         for (Enumeration e = symbols.elements(); e.hasMoreElements();)
   //         {
   //            if (((ISymbol) e.nextElement()).isDeleted())
   //            {
   //               throw new CannotRedoException();
   //            }
   //         }
   //
   //         for (Enumeration e = symbols.elements(); e.hasMoreElements();)
   //         {
   //            ((ISymbol) e.nextElement()).move(xMove, yMove);
   //         }
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getUndoPresentationName()
   //   {
   //      return "Undo Symbols Move";
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getRedoPresentationName()
   //   {
   //      return "Redo Symbols Move";
   //   }
   //}
   //
   ///**
   // *
   // */
   //protected class PasteSymbolsEdit extends AbstractUndoableEdit
   //{
   //   private Vector symbols = null;
   //   private DrawArea drawArea = null;
   //
   //   /**
   //    *
   //    */
   //   public PasteSymbolsEdit(Collection symbols)
   //   {
   //      Assert.isNotNull(symbols, "Symbols collection is not null");
   //      Assert.isNotEmpty(symbols, "Symbols collection is not empty");
   //
   //      this.symbols = new Vector(symbols);
   //
   //      if (!symbols.isEmpty())
   //      {
   //         drawArea = ((ISymbol) symbols.iterator().next()).getDrawArea();
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public void undo() throws CannotUndoException
   //   {
   //      if (symbols.isEmpty())
   //      {
   //         return;
   //      }
   //      boolean _ignoreFlagWasSet = false;
   //      try
   //      {
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //
   //         super.undo();
   //
   //         for (Enumeration e = symbols.elements(); e.hasMoreElements();)
   //         {
   //            if (((ISymbol) e.nextElement()).isDeleted())
   //            {
   //               throw new CannotUndoException();
   //            }
   //         }
   //
   //         for (Enumeration e = symbols.elements(); e.hasMoreElements();)
   //         {
   //            ((ISymbol) e.nextElement()).delete();
   //         }
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public void redo() throws CannotRedoException
   //   {
   //      /*
   //      if (symbols.isEmpty())
   //               {
   //                  return;
   //               }
   //               Symbol _symbol = null;
   //               Iterator _symbolIterator = symbols.iterator();
   //               boolean _ignoreFlagWasSet = false;
   //
   //               try
   //               {
   //                  _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //                  if (!_ignoreFlagWasSet)
   //                  {
   //                     drawArea.setIgnoreFireUndoEditRequests(true);
   //                  }
   //
   //                  super.redo();
   //
   //                  for (Enumeration e = symbols.elements(); e.hasMoreElements();)
   //                  {
   //                     if (((ISymbol) e.nextElement()).isDeleted())
   //                     {
   //                        throw new CannotRedoException();
   //                     }
   //                  }
   //
   //                  while (_symbolIterator.hasNext())
   //                  {
   //                     _symbol = (ISymbol) _symbolIterator.next();
   //                     diagram.addToSymbols(_symbol, _symbol.getElementOID());
   //                  }
   //               }
   //               finally
   //               {
   //                  if (!_ignoreFlagWasSet)
   //                  {
   //                     drawArea.setIgnoreFireUndoEditRequests(false);
   //                  }
   //               }
   //               */
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getUndoPresentationName()
   //   {
   //      return "Undo Symbol Paste";
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getRedoPresentationName()
   //   {
   //      return "Redo Symbol Paste";
   //   }
   //}
   ///**
   // *
   // */
   //protected class AlignSymbolsEdit extends AbstractUndoableEdit
   //{
   //   private Vector moveList;
   //   private DrawArea drawArea;
   //
   //   /**
   //    */
   //   public AlignSymbolsEdit(DrawArea drawArea)
   //   {
   //      this.drawArea = drawArea;
   //      moveList = new Vector(50);
   //   }
   //
   //   /**
   //    */
   //   public AlignSymbolsEdit(DrawArea drawArea, Collection moveEditList)
   //   {
   //      Assert.isNotNull(drawArea, "DrawArea is not null");
   //      Assert.isNotNull(moveEditList, "List of MoveEdit is not null");
   //
   //      this.drawArea = drawArea;
   //      moveList = new Vector(moveEditList);
   //   }
   //
   //   /**
   //    */
   //   public void addMoveSymbolEdit(MoveSymbolEdit moveEdit)
   //   {
   //      Assert.isNotNull(moveList, "MoveEdit list is not null");
   //      Assert.isNotNull(moveEdit, "MoveSymbolEdit is not null");
   //      moveList.add(moveEdit);
   //   }
   //
   //   /**
   //    */
   //   public void addMoveSymbolsEdit(MoveSymbolsEdit moveEdit)
   //   {
   //      Assert.isNotNull(moveList, "MoveEdit list is not null");
   //      Assert.isNotNull(moveEdit, "MoveSymbolsEdit is not null");
   //      moveList.add(moveEdit);
   //   }
   //
   //   /**
   //    */
   //   public void undo() throws CannotUndoException
   //   {
   //      boolean _ignoreFlagWasSet = false;
   //
   //      Iterator _iterator = moveList.iterator();
   //      try
   //      {
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //
   //         super.undo();
   //         while (_iterator.hasNext())
   //         {
   //            ((UndoableEdit) _iterator.next()).undo();
   //         }
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //         drawArea.repaint();
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public void redo() throws CannotRedoException
   //   {
   //      Iterator _iterator = moveList.iterator();
   //      boolean _ignoreFlagWasSet = false;
   //
   //      try
   //      {
   //         _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(true);
   //         }
   //         super.redo();
   //
   //         while (_iterator.hasNext())
   //         {
   //            ((UndoableEdit) _iterator.next()).redo();
   //         }
   //      }
   //      finally
   //      {
   //         if (!_ignoreFlagWasSet)
   //         {
   //            drawArea.setIgnoreFireUndoEditRequests(false);
   //         }
   //         drawArea.repaint();
   //      }
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getUndoPresentationName()
   //   {
   //      return "Undo Symbols Align";
   //   }
   //
   //   /**
   //    *
   //    */
   //   public String getRedoPresentationName()
   //   {
   //      return "Redo Symbols Align";
   //   }
   //}
   //

}
