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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.eclipse.stardust.common.Assert;


/**
 */
public class CreateSymbolEdit extends AbstractSymbolUndoableEdit
{
    private NodeSymbol createdSymbol;
    private DrawArea drawArea;

    /**
     */
    public CreateSymbolEdit(NodeSymbol symbol)
    {
        super(symbol);
        Assert.isNotNull(symbol, "Created Symbol is not null");

        drawArea = symbol.getDrawArea();
        this.createdSymbol = symbol;
    }

    /**
     *
     */
    public String getUndoPresentationName()
    {
        return "Undo Symbol Creation";
    }

    /**
     *
     */
    public String getRedoPresentationName()
    {
        return "Redo Symbol Creation";
    }

    /**
     *
     */
    public void undo() throws CannotUndoException
    {
        ConnectionSymbol _connection = null;

        boolean _ignoreFlagWasSet = false;

        if (drawArea != null)
        {
            try
            {
                super.undo();

                if (createdSymbol.isDeleted()
                        || drawArea.getDiagram().contains(createdSymbol) == false
                )
                {
                    throw new CannotUndoException();
                }

                _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();

                if (!_ignoreFlagWasSet)
                {
                    drawArea.setIgnoreFireUndoEditRequests(true);
                }

                createdSymbol.delete();

                drawArea.repaint();
            }
            finally
            {
                if (!_ignoreFlagWasSet)
                {
                    drawArea.setIgnoreFireUndoEditRequests(false);
                }
            }
        }
    }

    /**
     *
     */
    public void redo() throws CannotRedoException
    {
        Assert.isNotNull(createdSymbol, "Created Symbol is not null");
        boolean _ignoreFlagWasSet = false;

        try
        {
            if (createdSymbol.isDeleted())
            {
                throw new CannotRedoException();
            }

            super.redo();

            _ignoreFlagWasSet = drawArea.getIgnoreFireUndoEditRequests();
            if (!_ignoreFlagWasSet)
            {
                drawArea.setIgnoreFireUndoEditRequests(true);
            }

            drawArea.placeSymbol(createdSymbol, createdSymbol.getX(), createdSymbol.getY());
        }
        finally
        {
            if (!_ignoreFlagWasSet)
            {
                drawArea.setIgnoreFireUndoEditRequests(false);
            }
        }
    }

}

