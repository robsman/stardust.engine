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
package org.eclipse.stardust.engine.core.struct.beans;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.Loader;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


/**
 * Caches instances of structrured data value bean in PropertyLayer
 */
public class StructuredDataValueLoader implements Loader
{

   public static final String SDV_BY_PI_OID_CACHE = StructuredDataValueLoader.class.getName()+".SDV_BY_PI_OID_CACHE";

   public void load(Persistent persistent)
   {
      StructuredDataValueBean sdv = (StructuredDataValueBean) persistent;
      PropertyLayer propertyLayer = (PropertyLayer) ParametersFacade.instance().get(PropertyLayerProviderInterceptor.PROPERTY_LAYER);
      
      // contains sdv caches (piOid is the map key)
      Map /*<Long,Map>*/ sdvCacheByPiOid = (Map) propertyLayer.get(SDV_BY_PI_OID_CACHE);
      if (sdvCacheByPiOid == null) 
      {
         sdvCacheByPiOid = new HashMap();
         propertyLayer.setProperty(SDV_BY_PI_OID_CACHE, sdvCacheByPiOid);
      }
      Long piOid = new Long(sdv.getProcessInstance().getScopeProcessInstanceOID());
      // contains lists of sdv (xPathOid is the map key)
      Map /*<Long,List>*/ sdvListByXPathOid = (Map) sdvCacheByPiOid.get(piOid);
      if (sdvListByXPathOid == null)
      {
         sdvListByXPathOid = new HashMap();
         sdvCacheByPiOid.put(piOid, sdvListByXPathOid);
      }
      Long xPathOid = new Long(sdv.getXPathOID());
      List /*<IStructuredDataValue>*/ sdvList = (List) sdvListByXPathOid.get(xPathOid);
      if (sdvList == null)
      {
         sdvList = new LinkedList();
         sdvListByXPathOid.put(xPathOid, sdvList);
      }
      sdvList.add(sdv);
   }

}
