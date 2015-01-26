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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


public class RuntimeOidRegistry implements IRuntimeOidRegistry
{
   private static final Logger trace = LogManager.getLogger(IRuntimeOidRegistry.class);

   public static final int PARTITION_PART_SHIFT = 48;
   
   private final Map rtOidRegistry = CollectionUtils.newMap();
   private final Map rtOidReverseMap = CollectionUtils.newMap();
   private final short partitionOid;
   
   public RuntimeOidRegistry(final short partitionOid)
   {
      this.partitionOid = partitionOid;
   }

   public long getRuntimeOid(ElementType type, String[] fqId)
   {
      long result = 0;
      
      Map typeRegistry = (Map) rtOidRegistry.get(type);
      if (null != typeRegistry)
      {
         String fqIdKey = RuntimeOidUtils.internalizeFqId(fqId);
         Long oid = (Long) typeRegistry.get(fqIdKey);
         if (null != oid)
         {
            result = oid.longValue();
         }
      }

      if (0 != result)
      {
         Assert.condition(((result >> PARTITION_PART_SHIFT) + 1l) == partitionOid,
               "Partition part of runtime oid "+result+" does not match with partiton oid "+partitionOid+".");
      }

      return result;
   }
   
   public String[] getFqId(ElementType type, long rtOid)
   {
      String[] result = StringUtils.EMPTY_STRING_ARRAY;
      
      Map idRegistry = (Map) rtOidReverseMap.get(type);
      if (null != idRegistry)
      {
         String[] id = (String[]) idRegistry.get(new Long(rtOid));
         if (null != id)
         {
            result = id;
         }
      }
      
      return result;
   }
   
   public void registerRuntimeOid(ElementType type, String[] fqId, long rtOid)
         throws InternalException
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Registered runtime OID: " + type + RuntimeOidUtils.internalizeFqId(fqId) + " = " + rtOid);
      }
      try
      {
         Assert.condition(((rtOid >> PARTITION_PART_SHIFT) + 1l) == partitionOid,
               "Partition part of runtime oid " + rtOid
                     + " does not match with partiton oid " + partitionOid + ".");
      }
      catch(RuntimeException x)
      {
         throw x;
      }

      Map typeRegistry = (Map) rtOidRegistry.get(type);
      if (null == typeRegistry)
      {
         typeRegistry = new HashMap();
         rtOidRegistry.put(type, typeRegistry);
      }
      
      String fqIdKey = RuntimeOidUtils.internalizeFqId(fqId);
      Long oid = (Long) typeRegistry.get(fqIdKey);
      if (null != oid)
      {
         if (oid.longValue() != rtOid)
         {
            throw new InternalException("Inconsistent runtime OIDs for " + type
                  + " with ID " + StringUtils.join(Arrays.asList(fqId).iterator(), "::")
                  + ": " + oid + " vs. " + rtOid + ". ");
         }
      }
      else
      {
         typeRegistry.put(fqIdKey, new Long(rtOid));
         
         Map idRegistry = (Map) rtOidReverseMap.get(type);
         if (null == idRegistry)
         {
            idRegistry = new HashMap();
            rtOidReverseMap.put(type, idRegistry);
         }
         idRegistry.put(new Long(rtOid), fqId);
      }
   }
   
   public long registerNewRuntimeOid(ElementType type, String[] fqId)
         throws InternalException
   {
      long newOid;

      Map typeRegistry = (Map) rtOidRegistry.get(type);
      if (null != typeRegistry)
      {
         long maxOid = 0;
         for (Iterator i = typeRegistry.values().iterator(); i.hasNext();)
         {
            Long rtOid = (Long) i.next();
            maxOid = Math.max(maxOid, rtOid.longValue());
         }
         newOid = maxOid + 1;
      }
      else
      {
         newOid = 1 + ((partitionOid - 1l) << PARTITION_PART_SHIFT);
      }
      
      registerRuntimeOid(type, fqId, newOid);
      return newOid;
   }
}
