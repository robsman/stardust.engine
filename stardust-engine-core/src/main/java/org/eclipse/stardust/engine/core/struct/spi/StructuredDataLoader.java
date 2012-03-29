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
package org.eclipse.stardust.engine.core.struct.spi;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.runtime.UnresolvedExternalReference;
import org.eclipse.stardust.engine.core.model.beans.AccessPointBean;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.model.utils.RuntimeAttributeHolder;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeOidUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataLoader;
import org.eclipse.stardust.engine.core.struct.*;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;


/**
 * Data loader for structured data. When structured data is deployed, the xpath table is filled.
 * When runtime loads a structured data definition, the xpath map (attribute) is filled from
 * the xpath table. 
 * 
 * @version $Revision$
 */
public class StructuredDataLoader implements DataLoader, Stateless
{
   private static final Logger trace = LogManager.getLogger(StructuredDataLoader.class);

   public static final String ALL_DATA_XPATHS_ATT = "ALL_DATA_XPATHS";

   public void loadData(IData data)
   {
      IXPathMap xPathMap;
      if (ModelManagerFactory.isAvailable() && data.getModel().getModelOID() != 0 && !SessionFactory.isDebugSession())
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();
         xPathMap = loadFullMode(modelManager, data);
      } 
      else
      {
         trace.debug("ModelManager is not available, assuming 'local mode'");
         xPathMap = loadLocalMode(data);
      }
      data.setRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT, xPathMap);
   }

   public IXPathMap loadAccessPoint(AccessPoint accessPoint)
   {
      String declaredTypeId = (String) accessPoint.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
      if (null != declaredTypeId && accessPoint instanceof AccessPointBean)
      {
         IModel model = (IModel) ((AccessPointBean) accessPoint).getModel();
         Set<TypedXPath> allXPaths = StructuredTypeRtUtils.getAllXPaths(model, declaredTypeId);
         IXPathMap xPathMap = createXPathMap(allXPaths, accessPoint);
         if (accessPoint instanceof RuntimeAttributeHolder)
         {
            ((RuntimeAttributeHolder) accessPoint).setRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT, xPathMap);
         }
         return xPathMap;
      }
      else
      {
         for (Iterator i = ExtensionProviderUtils.getExtensionProviders(
               ISchemaTypeProvider.Factory.class).iterator(); i.hasNext();)
         {
            ISchemaTypeProvider.Factory stpFactory = (ISchemaTypeProvider.Factory) i.next();
            
            ISchemaTypeProvider provider = stpFactory.getSchemaTypeProvider(accessPoint);
            if (null != provider)
            {
               Set/*<TypedXPath>*/ allXPaths = provider.getSchemaType(accessPoint);
               
               if (null != allXPaths)
               {
                   IXPathMap xPathMap = createXPathMap(allXPaths, accessPoint);
                   if (accessPoint instanceof RuntimeAttributeHolder)
                   {
                      ((RuntimeAttributeHolder) accessPoint).setRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT, xPathMap);
                   }
                   return xPathMap;
               }
            }
         }
      }
      throw new InternalException("Could not find predefined XPaths for access point '"
            + accessPoint.getId()
            + "'. Check if schema providers are configured correctly.");

   }

   private IXPathMap loadLocalMode(IData data)
   {
      try
      {
         Set /* <TypedXPath> */ allXPaths = this.findAllXPaths(data, data.getModel());
         return createXPathMap(allXPaths, data);
      }
      catch (UnresolvedExternalReference ex)
      {
         throw ex;
      }
      catch (Exception e)
      {
         throw new InternalException(
               "Could not create XPath mapping using schema for data '" + data.getId()
                     + "'", e);
      }
   }

   private IXPathMap createXPathMap(Set /* <TypedXPath> */ allXPaths, AccessPoint accessPoint)
   {
      // this is _only_ needed for debugger:
      // try to assign unique OIDs derived from hashcodes of XPaths 
      // No ModelManager is present in the debugger scenario, 
      // but process execution involving structured data requires having XPath OIDs

      Map /*<Long,TypedXPath>*/xPathMap = CollectionUtils.newMap();
      int cnt = 0;
      for (Iterator i = allXPaths.iterator(); i.hasNext(); cnt++)
      {
         TypedXPath p = (TypedXPath)i.next();
         String hash = accessPoint.getId()+"."+p.getXPath();
         xPathMap.put(new Long(hash.hashCode()), p);
      }
      return new DataXPathMap(xPathMap);
   }

   private IXPathMap loadFullMode(ModelManager modelManager, IData data)
   {
      try
      {
         // create xpath mapping
         Set /*<TypedXPath>*/xPaths = this.findAllXPaths(data, data.getModel());
         
         Map /*<Long,TypedXPath>*/allXPaths = CollectionUtils.newMap();

         if (null != xPaths)
         {
            for (Iterator i = xPaths.iterator(); i.hasNext(); )
            {
               TypedXPath xPath = (TypedXPath) i.next();

               long xPathOid = modelManager.getRuntimeOid(data, xPath.getXPath());
               if (0 == xPathOid)
               {
                  trace.warn("The XPath '" + xPath.getXPath() + "' retrieved from XSD definition could not be found in the audit trail and will be ignored. Did the external XSD change after deployment? Incompatible changes may prevent process instances of older model versions to execute properly.");
                  continue;
               }
               allXPaths.put(new Long(xPathOid), xPath);
            }
         }

         return new DataXPathMap(allXPaths);
      }
      catch (Exception e)
      {
         throw new InternalException(
               "Could not create XPath mapping using schema for data '" + data.getId()
                     + "'", e);
      }
   }

   public void deployData(IRuntimeOidRegistry rtOidRegistry, IData data, long dataRtOid,
         long modelOID, RootElement model)
   {
      try
      {
         // create xpath mapping
         Set /*<TypedXPath>*/xPaths = this.findAllXPaths(data, model);

         if (null != xPaths)
         {
            Map<Long, StructuredDataBean> structDataDefRecords = loadXPathDefinitions(modelOID, dataRtOid);
            
            Map /*<Long,TypedXPath>*/allXPaths = CollectionUtils.newMap();
            for (Iterator i = xPaths.iterator(); i.hasNext();)
            {
               TypedXPath xPath = (TypedXPath) i.next();
               
               // dataId and xPath must uniquely map to an xPathRtOid
               long xPathRtOid = rtOidRegistry.getRuntimeOid(
                     IRuntimeOidRegistry.STRUCTURED_DATA_XPATH, RuntimeOidUtils.getFqId(data, xPath.getXPath()));
               
               StructuredDataBean xPathBean = null;
               if (xPathRtOid == 0)
               {
                  // there is no StructuredDataBean this xPathRtOid and modelOID, new one must be created
                  xPathRtOid = rtOidRegistry.registerNewRuntimeOid(
                        IRuntimeOidRegistry.STRUCTURED_DATA_XPATH, RuntimeOidUtils.getFqId(data, xPath.getXPath()));

                  xPathBean = new StructuredDataBean(xPathRtOid, dataRtOid, modelOID,
                        xPath.getXPath());
               }
               else
               {
                  // try to find StructuredDataBean for this rtOid and modelOID
                  xPathBean = (StructuredDataBean) structDataDefRecords.get(Long.valueOf(xPathRtOid));
                  if (xPathBean == null)
                  {
                     // if, for some reason, the StructuredDataBean for exising xPathRtOid was not found 
                     // for the current modelOID, create one
                     xPathBean = new StructuredDataBean(xPathRtOid, dataRtOid, modelOID,
                           xPath.getXPath());
                  }
               }
               
               allXPaths.put(new Long(xPathBean.getOID()), xPath);
            }
         }
      }
      catch (Exception e)
      {
         throw new InternalException(
               "Could not create XPath mapping using schema for data '" + data.getId()
                     + "'", e);
      }
   }

   private Set<TypedXPath> findAllXPaths(IData data, RootElement model)
         throws Exception
   {
      Set<TypedXPath> result = null;
      
      String declaredTypeId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
      if (null != declaredTypeId)
      {
         return StructuredTypeRtUtils.getAllXPaths((IModel)model, declaredTypeId);
      }
      else
      {
         IReference ref = data.getExternalReference();
         if (ref != null)
         {
            return StructuredTypeRtUtils.getAllXPaths(ref);
         }
         for (Iterator i = ExtensionProviderUtils.getExtensionProviders(
               ISchemaTypeProvider.Factory.class).iterator(); i.hasNext();)
         {
            ISchemaTypeProvider.Factory stpFactory = (ISchemaTypeProvider.Factory) i.next();
            
            ISchemaTypeProvider provider = stpFactory.getSchemaTypeProvider(data.getType().getId());
            if (null != provider)
            {
               result = provider.getSchemaType(data);
               
               if (null != result)
               {
                  return result;
               }
            }
         }
         throw new InternalException("Could not find predefined XPaths for data type '"
               + data.getType()
               + "'. Check if schema providers are configured correctly.");
      }
   }

   
   private static Map<Long, StructuredDataBean> loadXPathDefinitions(long modelOid, long dataRtOid)
   {
      Map<Long, StructuredDataBean> result = CollectionUtils.newHashMap();
      
      for (Iterator i = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .getIterator(
                  StructuredDataBean.class, //
                  QueryExtension.where(Predicates.andTerm(//
                        Predicates.isEqual(StructuredDataBean.FR__DATA, dataRtOid),
                        Predicates.isEqual(StructuredDataBean.FR__MODEL, modelOid)))); i.hasNext();)
      {
         StructuredDataBean sdd = (StructuredDataBean) i.next();
         result.put(new Long(sdd.getOID()), sdd);
      }
      
      return result;
   }

   public boolean isStateless()
   {
      return true;
   }
}
