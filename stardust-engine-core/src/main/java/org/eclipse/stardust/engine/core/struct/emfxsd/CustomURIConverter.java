/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.struct.emfxsd;

import java.util.Map;

import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

public abstract class CustomURIConverter extends ExtensibleURIConverterImpl
{
   private Map customMap;

   public Map getCustomMap()
   {
      return customMap;
   }

   public void setCustomMap(Map customMap)
   {
      this.customMap = customMap;
   }
}
