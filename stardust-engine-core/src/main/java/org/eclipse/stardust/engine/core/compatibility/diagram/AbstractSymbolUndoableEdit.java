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

import org.eclipse.stardust.common.Assert;


/**
 * Abstract base class for undoable edits in symbols
 */
public abstract class AbstractSymbolUndoableEdit extends AbstractUndoableEdit
{
    private NodeSymbol editedSymbol;
    private DrawArea drawArea;

    /**
     */
    protected AbstractSymbolUndoableEdit(NodeSymbol editedSymbol)
    {
        Assert.isNotNull(editedSymbol, "Edited Symbol is not null");

        this.editedSymbol = editedSymbol;
        drawArea = editedSymbol.getDrawArea();
    }

    //	 Returns true is the edited symbol and its DrawArea not null and writable
    public boolean canRedo()
    {
        return super.canRedo()
                && (drawArea != null)
                && (editedSymbol != null)
                && (!drawArea.isReadOnly())
                && (drawArea.isVisible())
                ;
    }

    //	 Returns true is the edited symbol and its DrawArea not null and writable
    public boolean canUndo()
    {
        return super.canUndo()
                && (drawArea != null)
                && (editedSymbol != null)
                && (!drawArea.isReadOnly())
                && (drawArea.isVisible())
                ;
    }
}

