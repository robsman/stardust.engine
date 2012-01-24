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

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface IApplicationContextType extends IdentifiableElement, PluggableType
{
   /**
    * Indicates whether for this context data mappings IDs should be shown in the data
    * mapping configuration for this context in the definition desktop.
    */
   boolean hasMappingId();

   /**
    * Indicates whether for this context application paths should be shown in the data
    * mapping configuration for this context in the definition desktop.
    */
   boolean hasApplicationPath();

   void setHasMappingId(boolean hasMappingId);

   void setHasApplicationPath(boolean hasApplicationPath);

}
