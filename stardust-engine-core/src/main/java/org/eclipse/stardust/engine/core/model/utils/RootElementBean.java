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
package org.eclipse.stardust.engine.core.model.utils;

import java.util.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class RootElementBean extends IdentifiableElementBean implements RootElement
{
   private static final long serialVersionUID = 2L;

   private static final Logger trace =
         LogManager.getLogger(RootElementBean.class);

   private static final int ELEMENT_ID_OFFSET = 10000;

   private HashMap parts = new HashMap();
   private int currentElementOID = ELEMENT_ID_OFFSET;
   private int currentTransientElementOID = -1;
   private int modelOID;

   protected RootElementBean()
   {
   }

   public RootElementBean(String id, String name)
   {
      super(id, name);
   }

   public RootElement getModel()
   {
      return this;
   }

   public void register(ModelElement part)
   {
      ModelElement existing = lookupElement(part.getElementOID());
      if (trace.isDebugEnabled())
      {
         trace.debug("Registering '" + part + "' with oid " + part.getElementOID());
      }
      if (existing != null)
      {
         if (existing != part)
         {
            // @todo (france, ub): temporarily throw an exception is here, somehow it is still broken
            // play e.g. with event actions
            throw  new InternalException("OID " + part.getElementOID() + " for " + part
                  + " already in use by '" + existing + "'.");
            /*int newOID = createElementOID();
            trace.warn("OID " + part.getElementOID() + " for " + part + " already in use by '"
                  + existing + "'. Registering with new OID " + newOID);
            part.setElementOID(newOID);
            parts.put(new Integer(newOID), part);
            */
         }
      }
      else
      {
         parts.put(new Integer(part.getElementOID()), part);
      }
   }

   public int createTransientElementOID()
   {
      return currentTransientElementOID--;
   }

   public void deregister(ModelElement part)
   {
      parts.remove(new Integer(part.getElementOID()));
   }

   public ModelElement lookupElement(int elementOID)
   {
      return (ModelElement) parts.get(new Integer(elementOID));
   }

   public Set getElementOIDs()
   {
      return Collections.unmodifiableSet(parts.keySet());
   }

   /**
    * Retrieves a unique 64-bit OID for all model elements composed by their
    * element id and the version ID of the model version.
    */
   public int createElementOID()
   {
      return ++currentElementOID;
   }

   public void setCurrentElementOID(int i)
   {
      currentElementOID = i;
   }

   public int getModelOID()
   {
      return modelOID;
   }

   public void setModelOID(int oid)
   {
      modelOID = oid;
   }

   public long getOID()
   {
      return ((long) getModelOID()) << 32;
   }
}
