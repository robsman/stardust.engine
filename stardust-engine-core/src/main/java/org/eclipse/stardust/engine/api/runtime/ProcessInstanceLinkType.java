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

import java.io.Serializable;

/**
 * Specifies the type of link that associates one process instance to another.
 * 
 * There are two predefined link types:
 * <ul>
 * <li><b>switch</b> - a process instance was aborted and execution switched to another process instance.</li>
 * <li><b>join</b> - a process instance has joined as subprocess another process instance.</li> 
 * </ul>
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface ProcessInstanceLinkType extends Serializable
{
   /**
    * @return the OID of this link type
    */
   long getOID();

   /**
    * @return the ID of this link type
    */
   String getId();

   /**
    * @return the description of this link type
    */
   String getDescription();
}
