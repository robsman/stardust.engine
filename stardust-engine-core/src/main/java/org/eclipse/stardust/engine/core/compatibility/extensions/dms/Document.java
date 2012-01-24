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
package org.eclipse.stardust.engine.core.compatibility.extensions.dms;

import java.io.Serializable;
import java.util.Map;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface Document extends org.eclipse.stardust.engine.api.runtime.Document
{
   ////
   //// Inherited from Document
   ////

   String getId();

   String getName();

   void setName(String name);

   String getContentType();

   void setContentType(String type);

   Map getProperties();

   Serializable getProperty(String name);

   void setProperty(String name, Serializable value);

   ////
   //// legacy methods for backwards compatibility
   ////

   /**
    * @deprecated
    */
   void setId(String id);

   /**
    * @deprecated
    */
   String getDisplayName();

   /**
    * @deprecated
    */
   void setDisplayName(String name);

   /**
    * @deprecated
    */
   void setDocumentType(String type);

}
