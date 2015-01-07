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

import java.util.Map;

import javax.swing.*;

import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.model.IApplication;


/**
 * Base class for an application provider to provide a panel for an
 * <code>ApplicationType</code> to show in the <code>ApplicationPropertiesDialog</code>.
 * Application providers have to subclass this class if they want to use the CARNOT
 * definitiondesktop for modelling.
 * 
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public abstract class ApplicationPropertiesPanel extends JPanel
{
   protected Map typeAttributes;

   /**
    * Callback to populate the panel from the underlying Application.
    * 
    * @param properties   the application properties
    * @param accessPoints the application access points
    */
   public abstract void setData(Map properties, java.util.Iterator accessPoints);

   /**
    * Callback to return the application specific properties to be put in the
    * corresponding Application object. Together with the <code>getAccessPoints</code>
    * method this method is responsible for transferring the state from the panel to the
    * application.
    * 
    * @return the application specific attributes
    * @see org.eclipse.stardust.engine.api.model.ModelElement#getAllAttributes
    */
   public abstract Map getAttributes();

   /**
    * Callback allowing for validation of type specific application attributes. Used i.e.
    * while checking model validity or before closing the
    * <code>ApplicationPropertiesDialog</code>.
    * 
    * @throws ValidationException if the current state of the application is invalid
    */
   public abstract void validatePanel() throws ValidationException;

   /**
    * Callback requesting the panel to create any dynamically configured access points on
    * the given application. Basically needed to allow for data mapping configurations
    * against dynamically typed applications.
    *
    * @param application The application to create the access points on.
    */
   public abstract void createAccessPoints(IApplication application);

   /**
    * Configures application type specific dynamic attributes needed for configuring
    * applications.
    *
    * @param attributes The aplication type specific attributes.
    */
   public void setTypeAttributes(Map attributes)
   {
      this.typeAttributes = attributes;
   }
}