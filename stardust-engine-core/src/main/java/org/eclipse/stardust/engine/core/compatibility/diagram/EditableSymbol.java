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

import java.awt.Component;

/**
 * serves the interface for editable symbols
 * the symbol must be able to create its editor-component on request
 */
public interface EditableSymbol
{
    /** return the editor-component for the symbol*/
    public Component activateEditing();

    /** */
    public void deactivateEditing();
}
