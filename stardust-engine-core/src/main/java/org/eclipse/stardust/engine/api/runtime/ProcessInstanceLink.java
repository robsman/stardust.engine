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
import java.util.Date;

/**
 * Represents a link from a process instance to a process instance. Links are not part of
 * the process hierarchy. They are used to define relations of a specific link type
 * between two process instances.
 */
public interface ProcessInstanceLink extends Serializable
{
   /**
    * @return the source process instance, i.e. the process instance this link originates from
    */
   long getSourceOID();

   /**
    * @return the target process instance, i.e. the process instance this link points to
    */
   long getTargetOID();

   /**
    * @return the link type of this link
    */
   ProcessInstanceLinkType getLinkType();

   /**
    * @return the date this link was created
    */
   Date getCreateTime();

   /**
    * @return the comment of this link
    */
   String getComment();
   
   /**
    * @return the <code>User</code> object of the user who created the link
    */
   User getCreatingUser();
}
