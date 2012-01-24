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
package org.eclipse.stardust.engine.api.runtime;

import java.util.List;

import org.eclipse.stardust.engine.api.model.DataPath;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface IDescriptorProvider
{
   String PRP_PROPVIDE_DESCRIPTORS = IDescriptorProvider.class.getName() + ".Enabled";
   String PRP_DESCRIPTOR_IDS = IDescriptorProvider.class.getName() + ".IDs";

   /**
    * Gets the current value of a descriptor with the specified ID.
    *
    * @param id the id of the descriptor.
    *
    * @return the value of the descriptor.
    */
   Object getDescriptorValue(String id);

   /**
    * Retrieves definitions for available descriptors.
    * @return the descriptor definitions that are available.
    */
   List<DataPath> getDescriptorDefinitions();
}
