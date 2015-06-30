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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.engine.api.runtime.UnresolvedExternalReference;
import org.eclipse.stardust.engine.core.model.utils.Identifiable;
import org.eclipse.stardust.engine.core.model.utils.Nameable;


/**
 * @author florin.herinean
 * @version $Revision: 31061 $
 */
public interface IExternalPackage extends Identifiable, Nameable
{
   /** Returns the model id of the referenced model */
   String getHref();
   
   /** Returns the model which contains the external package declaration */
   IModel getModel();
   
   /** Returns the referenced model */
   IModel getReferencedModel() throws UnresolvedExternalReference;
   
   /** Returns model attributes like the connection URI prefix from the referenced model */
   String getExtendedAttribute(String name);
}
