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
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;

/**
 * Represents options of detail for a {@link ProcessInstance}.
 *
 * @author stephan.born
 */
public enum ProcessInstanceDetailsOptions
{
   /**
    * The process instance details will contain information of the parent process instance.
    */
   WITH_HIERARCHY_INFO,
   
   /**
    * The process instance details will contain note information.
    */
   WITH_NOTES,

   /**
    * The process instance details will contain information about the linked process instances.
    */
   WITH_LINK_INFO
}
