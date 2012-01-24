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

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 */
public class UngroupSymbolsEdit extends AbstractUndoableEdit
{
    private GroupSymbol groupSymbol;
    private Diagram diagram;

    /**
     *
     */
    public UngroupSymbolsEdit(GroupSymbol groupSymbol)
    {
        //Assert.isNotNull(groupSymbol, "GroupSymbol is not null");
        //this.groupSymbol = groupSymbol;
        //
        //diagram = groupSymbol.getDiagram();
    }

    /**
     *
     */
    public void undo() throws CannotUndoException
    {
        //Iterator _childIterator = null;
        //Symbol _child = null;
        //boolean _ignoreFlagWasSet = false;
        //
        //try
        //{
        //    _ignoreFlagWasSet = diagram.getDrawArea().getIgnoreFireUndoEditRequests();
        //    if (!_ignoreFlagWasSet)
        //    {
        //        diagram.getDrawArea().setIgnoreFireUndoEditRequests(true);
        //    }
        //
        //    super.undo();
        //
        //    if (groupSymbol.isDeleted())
        //    {
        //        throw new CannotUndoException();
        //    }
        //    else
        //    {
        //        _childIterator = groupSymbol.getAllChildren();
        //        if (_childIterator != null)
        //        {
        //            while (_childIterator.hasNext())
        //            {
        //                if (((Symbol) _childIterator.next()).isDeleted())
        //                {
        //                    throw new CannotUndoException();
        //                }
        //            }
        //        }
        //    }
        //
        //    _childIterator = groupSymbol.getAllChildren();
        //    diagram.addToSymbols(groupSymbol, groupSymbol.getElementOID());
        //
        //    groupSymbol.setSelected(true);
        //    while (_childIterator.hasNext())
        //    {
        //        _child = (Symbol) _childIterator.next();
        //        _child.setSelected(false);
        //        diagram.removeFromSymbols(_child);
        //    }
        //    diagram.getDrawArea().refreshSelectedSymbols();
        //}
        //finally
        //{
        //    if (!_ignoreFlagWasSet)
        //    {
        //        diagram.getDrawArea().setIgnoreFireUndoEditRequests(false);
        //    }
        //}

    }

    /**
     *
     */
    public void redo() throws CannotRedoException
    {
        //Iterator _childIterator = null;
        //Symbol _child = null;
        //boolean _ignoreFlagWasSet = false;
        //
        //try
        //{
        //    _ignoreFlagWasSet = diagram.getDrawArea().getIgnoreFireUndoEditRequests();
        //    if (!_ignoreFlagWasSet)
        //    {
        //        diagram.getDrawArea().setIgnoreFireUndoEditRequests(true);
        //    }
        //
        //    super.redo();
        //
        //    if (groupSymbol.isDeleted())
        //    {
        //        throw new CannotRedoException();
        //    }
        //    else
        //    {
        //        _childIterator = groupSymbol.getAllChildren();
        //        if (_childIterator != null)
        //        {
        //            while (_childIterator.hasNext())
        //            {
        //                if (((Symbol) _childIterator.next()).isDeleted())
        //                {
        //                    throw new CannotRedoException();
        //                }
        //            }
        //        }
        //    }
        //
        //    groupSymbol.ungroup();
        //}
        //finally
        //{
        //    if (!_ignoreFlagWasSet)
        //    {
        //        diagram.getDrawArea().setIgnoreFireUndoEditRequests(false);
        //    }
        //}
    }

    /**
     *
     */
    public String getUndoPresentationName()
    {
        return "Undo Symbol Ungrouping";
    }

    /**
     *
     */
    public String getRedoPresentationName()
    {
        return "Redo Symbol Ungrouping";
    }
}
