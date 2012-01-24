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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.error.InternalException;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface IRuntimeOidRegistry
{
   public static final ElementType PARTICIPANT = new ElementType("ModelParticipant");

   public static final ElementType DATA = new ElementType("Data");

   public static final ElementType PROCESS = new ElementType("ProcessDefinition");

   public static final ElementType TRIGGER = new ElementType("Trigger");

   public static final ElementType ACTIVITY = new ElementType("Activity");

   public static final ElementType TRANSITION = new ElementType("Transition");

   public static final ElementType EVENT_HANDLER = new ElementType("EventHandler");

   public static final ElementType STRUCTURED_DATA_XPATH = new ElementType("StructuredData");

   long getRuntimeOid(ElementType type, String[] fqId);

   String[] getFqId(ElementType type, long rtOid);

   void registerRuntimeOid(ElementType type, String[] fqId, long rtOid)
         throws InternalException;

   long registerNewRuntimeOid(ElementType type, String[] fqId) throws InternalException;

   public static final class ElementType extends StringKey
   {
      private ElementType(String id)
      {
         super(id, id);
      }
   }
}