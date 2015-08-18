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
package org.eclipse.stardust.engine.core.compatibility.spi.model.gui;

import javax.swing.*;

import org.eclipse.stardust.common.error.ValidationException;

import java.util.Map;

/**
 * Base class for a data type implementation to provide a <code>DataType</code> specific
 * panel in the <code>DataPropertiesDialog</code>.
 * Data type providers have to subclass this class if they want to use the CARNOT
 * definitiondesktop for modelling type specific attributes.
 * 
 * @author rsauer
 * @version $Revision$
 */
public abstract class DataPropertiesPanel extends JPanel
{
   /**
    * Callback to populate the panel from the underlying data.
    * 
    * @param attributes     the custom, type specific data attributes
    * @param typeAttributes any custom data type attributes, not specific to the data
    */
   public abstract void setAttributes(Map attributes, Map typeAttributes);

   /**
    * Callback to return the data type specific properties to be put in the
    * corresponding Data object. Used to transfer state from the panel to the
    * data.
    * 
    * @return the custom, type specific data attributes
    * @see org.eclipse.stardust.engine.api.model.ModelElement#getAllAttributes
    */
   public abstract Map getAttributes();

   // superfluous. validation should be done in getAttributes.
   /**
    * Callback allowing for validation of type specific data attributes. Used i.e. while
    * checking model validity or before closing the <code>DataPropertiesDialog</code>.
    * 
    * @throws ValidationException if the current state of the data is invalid
    */
   public abstract void checkProperties() throws ValidationException;
}
