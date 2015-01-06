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

import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.model.IApplicationContext;


// work for florin

/**
 * The panel for configuring a pluggable application context.
 * <p>
 * Providers of an application context have to override this class if there should be support
 * for the context in the CARNOT Process Definition Desktop.
 *
 * @see org.eclipse.stardust.engine.api.model.IApplicationContextType
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class ApplicationContextPropertiesPanel extends JPanel
{
   /**
    * Callback to return the access points explicitely set in the panel.
    */
   //public abstract Collection getAccessPoints();

   /**
    * Callback to return the context attributes set in the panel.
    * 
    * @return the application context specific attributes
    * @see org.eclipse.stardust.engine.api.model.ModelElement#getAllAttributes
    */
   public abstract Map getAttributes();

   /**
    * Callback to set the panel state from a context.
    * 
    * @param attributes   the application context properties
    * @param accessPoints the application context access points
    */
   public abstract void setData(Map attributes, Iterator accessPoints);

   /**
    * This methods resets the panel state to the initial values.
    */
   public abstract void reset();

   /**
    * Callback requesting the panel to create any dynamically configured access points on
    * the given application context. Basically needed to allow for data mapping
    * configurations against dynamically typed application contexts.
    *
    * @param context The application context to create the access points on.
    */
   public abstract void createAccessPoints(IApplicationContext context);

   /**
    * Callback allowing for validation of type specific context attributes. Used i.e.
    * while checking model validity or before closing the
    * <code>ApplicationContextPropertiesDialog</code>.
    * 
    * @throws ValidationException if the current state of the context is invalid
    */
   public abstract void validatePanel();
}
