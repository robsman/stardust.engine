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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.SplicingIterator;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DeploymentElement;
import org.eclipse.stardust.engine.api.runtime.ParsedDeploymentUnit;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;

/**
 * @author rsauer
 * @version $Revision$
 */
public class ModelUtils
{
   private static final ModelElementList NULL_MODEL_ELEMENT_LIST =
      new ModelElementListAdapter(Collections.emptyList());

   private static final String PREDEFINED_MODEL_PATH = "/META-INF/resources/models/PredefinedModel.xpdl";

   public static IModel getModel(long modelOID) throws ObjectNotFoundException
   {
      IModel result = ModelManagerFactory.getCurrent().findModel(modelOID);
      if (result == null)
      {
         if (PredefinedConstants.ACTIVE_MODEL == modelOID)
         {
            throw new ObjectNotFoundException(BpmRuntimeError.MDL_NO_ACTIVE_MODEL.raise());
         }
         else
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_MODEL_OID.raise(modelOID), modelOID);
         }
      }
      return result;
   }

   public static IProcessDefinition getProcessDefinition(String id)
         throws ObjectNotFoundException
   {
      IProcessDefinition processDefinition = null;
      String namespace = null;
      if (id.startsWith("{"))
      {
         QName qname = QName.valueOf(id);
         namespace = qname.getNamespaceURI();
         id = qname.getLocalPart();
      }

      if (namespace != null)
      {
         IModel model = ModelManagerFactory.getCurrent().findActiveModel(namespace);
         if (model != null)
         {
            processDefinition = model.findProcessDefinition(id);
         }
      }
      else
      {
         processDefinition = getModel(PredefinedConstants.ACTIVE_MODEL).findProcessDefinition(id);
      }

      if (processDefinition == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_PROCESS_DEFINITION_ID.raise(id), id);
      }

      return processDefinition;
   }

   public static <T extends ModelElement> ModelElementList<T> getModelElementList(List<T> modelElements)
   {
      if (modelElements == null)
      {
         return NULL_MODEL_ELEMENT_LIST;
      }
      return new ModelElementListAdapter(modelElements);
   }

   public static int nullSafeGetModelOID(ModelElement element)
   {
      return ((null != element) && (null != element.getModel()))
            ? element.getModel().getModelOID() : 0;
   }

   public static String nullSafeGetModelNamespace(ModelElement element)
   {
      return element == null ? null : element.getModel().getId();
   }

   public static String nullSafeGetID(Identifiable identifiable)
   {
      return (null != identifiable) ? identifiable.getId() : null;
   }

   public static String nullSafeGetName(Nameable nameable)
   {
      return (null != nameable) ? nameable.getName() : null;
   }

   public static IData getMappedData(IProcessDefinition process, String formalParameterId)
      throws ObjectNotFoundException
   {
      IData result = process.getMappedData(formalParameterId);
      if (null == result)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_DATA_FOR_FORMAL_PARAMETER.raise(
               formalParameterId, getQualifiedId(process)), formalParameterId);
      }
      return result;
   }

   public static IData getData(IProcessDefinition process, String dataId)
      throws ObjectNotFoundException
   {
      IModel model = (IModel) process.getModel();
      IData result = model.findData(dataId);
      if (null == result)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(dataId), dataId);
      }
      return result;
   }

   public static <T extends Identifiable> T findById(List<T> items, String id)
   {
      if (items != null && StringUtils.isNotEmpty(id))
      {
         for (T item : items)
         {
            if (id.equals(item.getId()))
            {
               return item;
            }
         }
      }
      return null;
   }

   public static Iterator iterator(Collection coll)
   {
      if (coll == null)
      {
         return Collections.emptyList().iterator();
      }
      return coll.iterator();
   }

   public static <V> List<V> trim(List<V> list)
   {
      if (list == null || list.isEmpty())
      {
         return Collections.emptyList();
      }
      if (list.size() == 1)
      {
         return Collections.singletonList(list.get(0));
      }
      if (list instanceof ArrayList)
      {
         ((ArrayList<V>) list).trimToSize();
         return list;
      }
      return new ArrayList<V>(list);
   }

   public static <K, V> Map<K, V> trim(Map<K, V> map)
   {
      if (map == null || map.isEmpty())
      {
         return Collections.emptyMap();
      }
      if (map.size() == 1)
      {
         K key = map.keySet().iterator().next();
         return Collections.singletonMap(key, map.get(key));
      }
      return new HashMap<K, V>(map);
   }

   public static String getExtendedVersionString(IModel model)
   {
      if (model == null)
      {
         return "";
      }

      String version = model.getStringAttribute(PredefinedConstants.VERSION_ATT);
      int revision = model.getIntegerAttribute(PredefinedConstants.REVISION_ATT);

      return "(model oid = " + model.getModelOID() + ", version = " + version
            + ", revision = " + revision + ")";
   }

   public static String getQualifiedId(IdentifiableElement element)
   {
      return element == null ? null : element.getQualifiedId();
   }
   
   public static String getQualifiedId(RootElement model, String elementId)
   {
      String qualifiedId = elementId;
      if (model != null)
      {
         String modelId = model.getId();
         qualifiedId = '{' + (modelId == null ? "" : modelId) + '}' + qualifiedId;
      }
      return qualifiedId;
   }

   public static long size(Collection coll)
   {
      return coll == null ? 0 : coll.size();
   }

   public static List<IModel> findUsing(IModel used)
   {
      List<IModel> using = null;
      if (ModelManagerFactory.isAvailable())
      {
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         Iterator<IModel> overrideModelsIterator = rtEnv.getModelOverrides() != null
               ? rtEnv.getModelOverrides().values().iterator()
               : null;
         Iterator<IModel> allModelsIterator = ModelManagerFactory.getCurrent()
               .getAllModels();

         Iterator<IModel> models = new SplicingIterator<IModel>(
               overrideModelsIterator,
               allModelsIterator);
         while (models.hasNext())
         {
            IModel candidate = models.next();
            for (IExternalPackage pkg : candidate.getExternalPackages())
            {
               if (pkg.getReferencedModel().equals(used))
               {
                  (using == null ? using = CollectionUtils.newList() : using).add(candidate);
               }
            }
         }
      }
      return using == null ? Collections.<IModel>emptyList() : using;
   }

   public static List<IdentifiableElement> findUsing(IProcessDefinition used)
   {
      List<IdentifiableElement> using = null;
      for (IModel model : findUsing((IModel) used.getModel()))
      {
         for (IProcessDefinition pd : model.getProcessDefinitions())
         {
            if (isUsing(pd.getExternalReference(), used))
            {
               (using == null ? using = CollectionUtils.newList() : using).add(pd);
            }
            for (IActivity activity : pd.getActivities())
            {
               if (isUsing(activity.getExternalReference(), used))
               {
                  (using == null ? using = CollectionUtils.newList() : using).add(activity);
               }
            }
         }
      }
      return using == null ? Collections.<IdentifiableElement>emptyList() : using;
   }

   public static List<ParsedDeploymentUnit> getPredefinedModelElement()
   {
      byte[] content = null;
      try
      {
         String modelPath = PREDEFINED_MODEL_PATH;
         if (!ParametersFacade.instance().getBoolean(
               KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
         {
            modelPath = PREDEFINED_MODEL_PATH.replace("." + XpdlUtils.EXT_XPDL, "." + XpdlUtils.EXT_CWM);
         }

         InputStream in = AdministrationServiceImpl.class.getResourceAsStream(modelPath);
         content = XmlUtils.getContent(in);
      }
      catch (IOException e)
      {
         content = null;
      }
      if (content != null)
      {
         return Collections.singletonList(new ParsedDeploymentUnit(new DeploymentElement(content), 0));
      }
      return null;
   }
   
   public static void validateData(IModel iModel, Map<String, ? > data)
   {
      if ((null != data) && !data.isEmpty())
      {
         for (Iterator< ? > iterator = data.entrySet().iterator(); iterator.hasNext(); )
         {
            Map.Entry<String, ? > entry = (Entry<String, ? >) iterator.next();

            String dataId = entry.getKey();
            IData idata = iModel.findData(dataId);
            if (idata == null)
            {
               throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(dataId), dataId);
            }
         }
      }      
   }
   
   private static boolean isUsing(IReference ref, IProcessDefinition process)
   {
      return ref != null && ref.getExternalPackage().getReferencedModel() == process.getModel()
          && ref.getId().equals(process.getId());
   }

   private ModelUtils()
   {
      // utility class
   }
}