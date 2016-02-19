package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.runtime.beans.AbstractProperty;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;

public class PublicRealm implements IUserRealm
{

   private PublicRealm()
   {
   }

   public static IUserRealm getInstance()
   {
      return new PublicRealm();
   }

   public void addPropertyValues(Map map)
   {
   }

   public Map getAllPropertyValues()
   {
      return Collections.EMPTY_MAP;
   }

   public Map getAllProperties()
   {
      return Collections.EMPTY_MAP;
   }

   public Serializable getPropertyValue(String name)
   {
      return null;
   }

   public void setPropertyValue(String name, Serializable value)
   {
   }

   public void setPropertyValue(String name, Serializable value, boolean force)
   {
   }

   public void removeProperty(String name)
   {
   }

   public void removeProperty(String name, Serializable value)
   {
   }

   public AbstractProperty createProperty(String name, Serializable value)
   {
      return null;
   }

   public long getOID()
   {
      return 0;
   }

   public void setOID(long oid)
   {
   }

   public void lock() throws ConcurrencyException
   {
   }

   public void lock(int timeout) throws ConcurrencyException
   {
   }

   public void delete()
   {
   }

   public void delete(boolean writeThrough)
   {
   }

   public PersistenceController getPersistenceController()
   {
      return null;
   }

   public void setPersistenceController(PersistenceController PersistenceController)
   {
   }

   public void disconnectPersistenceController()
   {
   }

   public void markModified()
   {
   }

   public void markModified(String fieldName)
   {
   }

   public void fetch()
   {
   }

   public void markCreated()
   {
   }

   public String getId()
   {
      return "public";
   }

   public void setId(String id)
   {
   }

   public String getName()
   {
      return null;
   }

   public void setName(String name)
   {
   }

   public String getDescription()
   {
      return null;
   }

   public void setDescription(String description)
   {
   }

   public IAuditTrailPartition getPartition()
   {
      return null;
   }

}
