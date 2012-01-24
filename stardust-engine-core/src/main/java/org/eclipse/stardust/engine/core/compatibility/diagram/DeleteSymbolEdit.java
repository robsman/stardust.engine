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

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * We need to buffer implicitely deleted connections to restore these.
 *
 * Attention: 	For correct functionality you must call the constructor
 * 				before any remove operation because we must store the
 *  				connections of a removed symbol and a remove operation
 *					may remove same connections.
 */
public class DeleteSymbolEdit extends AbstractSymbolUndoableEdit
{
    private NodeSymbol deletedSymbol;
    private List implicitelyDeletedConnections;
    private DrawArea drawArea;

    /**
     */
    public DeleteSymbolEdit(NodeSymbol symbol)
    {
        super(symbol);
        //Assert.isNotNull(symbol, "Deleted Symbol is not null");
        //
        //drawArea = symbol.getDrawArea();
        //if (symbol.getDrawArea() != null)
        //{
        //    this.deletedSymbol = symbol;
        //    Iterator _iteratorConnections = null;
        //    Connection _connection = null;
        //
        //    _iteratorConnections = deletedSymbol.getAllConnectionsRecursively().iterator();
        //    if (_iteratorConnections != null)
        //    {
        //        implicitelyDeletedConnections = new Vector();
        //        while (_iteratorConnections.hasNext())
        //        {
        //            _connection = (Connection) _iteratorConnections.next();
        //            implicitelyDeletedConnections.add(_connection);
        //        }
        //    }
        //}
    }

    /**
     *
     */
    public String getUndoPresentationName()
    {
        return "Undo Symbol Deletion";
    }

    /**
     *
     */
    public String getRedoPresentationName()
    {
        return "Redo Symbol Deletion";
    }

    /**
     *
     */
    public void undo() throws CannotUndoException
    {
        //super.undo();
        //
        //Iterator _iteratorConnections = null;
        //Connection _connection = null;
        //boolean _ignoreFlagWasSet = false;
        //
        //if (drawArea != null)
        //{
        //    try
        //    {
        //
        //        if (deletedSymbol.isDeleted())
        //        {
        //            throw new CannotUndoException();
        //        }
        //
        //        _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
        //        if (!_ignoreFlagWasSet)
        //        {
        //            drawArea.setIgnoreFireUndoEditRequests(true);
        //        }
        //        if (deletedSymbol instanceof Connection)
        //        {
        //            _connection = (Connection) deletedSymbol;
        //
        //            _connection.getSecondSymbol().addToConnections(_connection);
        //            _connection.getFirstSymbol().addToConnections(_connection);
        //            drawArea.placeSymbol(deletedSymbol, deletedSymbol.getX(), deletedSymbol.getY());
        //        }
        //        else
        //        {
        //            drawArea.placeSymbol(deletedSymbol, deletedSymbol.getX(), deletedSymbol.getY());
        //
        //            if (implicitelyDeletedConnections != null)
        //            {
        //                _iteratorConnections = implicitelyDeletedConnections.iterator();
        //
        //                // Restore implicitely deleted connections
        //                while (_iteratorConnections.hasNext())
        //                {
        //                    _connection = (Connection) _iteratorConnections.next();
        //
        //                    // Restore links from symbols
        //                    _connection.getSecondSymbol().addToConnections(_connection);
        //                    _connection.getFirstSymbol().addToConnections(_connection);
        //                    drawArea.placeSymbol(_connection, _connection.getX(), _connection.getY());
        //                }
        //            }
        //        }
        //        drawArea.repaint();
        //    }
        //    finally
        //    {
        //        if (!_ignoreFlagWasSet)
        //        {
        //            drawArea.setIgnoreFireUndoEditRequests(false);
        //        }
        //    }
        //}
    }

    /**
     *
     */
    public void redo() throws CannotRedoException
    {
        //boolean _ignoreFlagWasSet = false;
        //super.redo();
        //
        //Assert.isNotNull(deletedSymbol);
        //
        //try
        //{
        //    if (deletedSymbol.isDeleted())
        //    {
        //        throw new CannotRedoException();
        //    }
        //
        //    _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
        //    if (!_ignoreFlagWasSet)
        //    {
        //        drawArea.setIgnoreFireUndoEditRequests(true);
        //    }
        //    deletedSymbol.delete();
        //}
        //finally
        //{
        //    if (!_ignoreFlagWasSet)
        //    {
        //        drawArea.setIgnoreFireUndoEditRequests(false);
        //    }
        //}
    }

}

