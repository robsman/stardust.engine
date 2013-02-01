/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws.processinterface;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.model.FormalParameter;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.ProcessInterface;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.w3c.dom.Document;



/**
 * This class holds reconfiguration data and can synchronize against the EPM API.
 *
 * @author Nicolas.Werlein
 */
public class EndpointConfigurationStorage
{
   private static EndpointConfigurationStorage instance;

   private Set<EndpointConfiguration> endpointConfs2Add = new HashSet<EndpointConfiguration>();

   private Set<String> endpointConfs2Remove = new HashSet<String>();

   private Map<Pair<String, String>, ProcessInterface> processInterfaces = CollectionUtils.newHashMap();

   private Map<String, DeployedModelDescription> lastDeployedModels = new HashMap<String, DeployedModelDescription>();

   public synchronized void add(final EndpointConfiguration endpoint)
   {
      if (endpoint == null)
      {
         throw new NullPointerException("Endpoint Configuration must not be null.");
      }

      if (endpointConfs2Remove.contains(endpoint.id()))
      {
         endpointConfs2Remove.remove(endpoint.id());
      }
      else
      {
         endpointConfs2Add.add(endpoint);
      }
   }

   public synchronized void remove(final String partitionId, final String modelId)
   {
      boolean found = false;
      boolean found2 = false;
      boolean found3 = false;
      String endpointId = EndpointConfiguration.buildEndpointId(partitionId, modelId,
            AuthMode.HttpBasicAuth);
      String endpointId2 = EndpointConfiguration.buildEndpointId(partitionId, modelId,
            AuthMode.HttpBasicAuthSsl);
      String endpointId3 = EndpointConfiguration.buildEndpointId(partitionId, modelId,
            AuthMode.WssUsernameToken);
      for (final Iterator<EndpointConfiguration> iter = endpointConfs2Add.iterator(); iter.hasNext();)
      {
         String id = iter.next().id();
         if (id.equals(endpointId))
         {
            found = true;
            iter.remove();
         }
         if (id.equals(endpointId2))
         {
            found2 = true;
            iter.remove();
         }
         if (id.equals(endpointId3))
         {
            found3 = true;
            iter.remove();
         }
      }

      if ( !found)
      {
         endpointConfs2Remove.add(endpointId);
      }
      if ( !found2)
      {
         endpointConfs2Remove.add(endpointId2);
      }
      if ( !found3)
      {
         endpointConfs2Remove.add(endpointId3);
      }
   }

   public synchronized Set<EndpointConfiguration> getEndpoints2Add()
   {
      final Set<EndpointConfiguration> endpoints = endpointConfs2Add;
      endpointConfs2Add = new HashSet<EndpointConfiguration>();
      return endpoints;
   }

   public synchronized Set<String> getEndpoints2Remove()
   {
      final Set<String> endpoints = endpointConfs2Remove;
      endpointConfs2Remove = new HashSet<String>();
      return endpoints;
   }

   public synchronized boolean hasEndpointConfigurationChanged()
   {
      return !endpointConfs2Add.isEmpty() || !endpointConfs2Remove.isEmpty();
   }

   public static synchronized EndpointConfigurationStorage instance()
   {
      if (instance == null)
      {
         instance = new EndpointConfigurationStorage();
      }
      return instance;
   }

   public synchronized void syncProcessInterfaces(String servletPath,
         Set<Pair<AuthMode, String>> endpointNameSet)
   {
      try
      {         
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
         ServiceFactory sf = wsEnv.getServiceFactory();
         QueryService qs = sf.getQueryService();

         Set<DeployedModelDescription> addModels = new HashSet<DeployedModelDescription>();
         Set<Pair<String, String>> removeCandidateModels = new HashSet<Pair<String, String>>();
         Models allActiveModels = qs.getModels(DeployedModelQuery.findActive());

         List<DeployedModelDescription> modelsToUpdate = new LinkedList<DeployedModelDescription>();
         for (DeployedModelDescription deployedModelDescription : allActiveModels)
         {
            DeployedModelDescription lastDeployedModel = lastDeployedModels.get(deployedModelDescription.getId());
            if (lastDeployedModel == null
                  || !lastDeployedModel.getDeploymentTime().equals(
                        deployedModelDescription.getDeploymentTime()))
            {
               lastDeployedModels.put(deployedModelDescription.getId(),
                     deployedModelDescription);
               modelsToUpdate.add(deployedModelDescription);
            }
         }

         for (DeployedModelDescription md : modelsToUpdate)
         {
            removeCandidateModels.add(new Pair<String, String>(md.getPartitionId(),
                  md.getId()));

            List<ProcessDefinition> pds = qs.getAllProcessDefinitions(md.getModelOID());
            for (ProcessDefinition processDefinition : pds)
            {
               if (PredefinedConstants.PROCESSINTERFACE_INVOCATION_SOAP.equals(processDefinition.getAttribute(PredefinedConstants.PROCESSINTERFACE_INVOCATION_TYPE))
                     || PredefinedConstants.PROCESSINTERFACE_INVOCATION_BOTH.equals(processDefinition.getAttribute(PredefinedConstants.PROCESSINTERFACE_INVOCATION_TYPE)))
               {
                  ProcessInterface processInterface = processDefinition.getDeclaredProcessInterface();
                  if (processInterface != null)
                  {
                     ProcessInterface currentProcessInterface = getProcessInterfacesMap().get(
                           processDefinition.getQualifiedId());
                     if (currentProcessInterface == null
                           || !isEqual(currentProcessInterface.getFormalParameters(),
                                 processInterface.getFormalParameters()))
                     {
                        getProcessInterfacesMap().put(
                              new Pair<String, String>(
                                    processDefinition.getPartitionId(),
                                    processDefinition.getQualifiedId()), processInterface);
                        addModels.add(md);
                     }
                     // Did not change or was added; no removal needed.
                     removeCandidateModels.remove(new Pair<String, String>(
                           md.getPartitionId(), md.getId()));
                  }
               }
               else
               {
                  if (null == getProcessInterfacesMap().remove(
                        new Pair<String, String>(processDefinition.getPartitionId(),
                              processDefinition.getQualifiedId())))
                  {
                     // Is not a published ProcessInterface so no removal is needed.
                     removeCandidateModels.remove(new Pair<String, String>(
                           md.getPartitionId(), md.getId()));
                  }
               }
            }
         }

         // Remove models that are not active or not existing anymore.
         List<Pair<String, String>> removalList = new LinkedList<Pair<String, String>>();
         Map<Pair<String, String>, ProcessInterface> processInterfacesMap = getProcessInterfacesMap();
         for (Pair<String, String> key : processInterfacesMap.keySet())
         {
            QName qn = QName.valueOf(key.getSecond());
            String modelId = qn.getNamespaceURI();
            String partitionId = key.getFirst();
            boolean keep = false;
            for (DeployedModelDescription md : allActiveModels)
            {
               // keep if modelId and partitionId is correct, or from different partition.
               if (!md.getPartitionId().equals(partitionId) || md.getId().equals(modelId))
               {
                  keep = true;
               }
            }
            if ( !keep)
            {
               removeCandidateModels.add(key);
               removalList.add(key);
            }
         }

         for (Pair<String, String> partitionModelIdPair : removalList)
         {
            getProcessInterfacesMap().remove(partitionModelIdPair);
         }

         for (Pair<String, String> partitionModelIdPair : removeCandidateModels)
         {
            String qualifiedProcessDefinitionId = partitionModelIdPair.getSecond();
            QName qn = QName.valueOf(qualifiedProcessDefinitionId);
            remove(partitionModelIdPair.getFirst(), qn.getNamespaceURI());
         }

         for (DeployedModelDescription md : addModels)
         {
            String modelString = qs.getModelAsXML(md.getModelOID());

            Document wsdl = new WSDLGenerator(modelString).generateDocument();

            for (Pair<AuthMode, String> pair : endpointNameSet)
            {
               AuthMode authMode = pair.getFirst();
               String endpointPath = pair.getSecond();
               String modelId = md.getId();

               String partitionId = md.getPartitionId();
               if (AuthMode.HttpBasicAuthSsl.equals(authMode))
               {
                  add(new EndpointConfiguration(new EndpointConfiguration.ModelIdUrlPair(
                        md.getId(), WsUtils.encodeInternalEndpointPath(servletPath,
                              partitionId, modelId, endpointPath)), partitionId, wsdl,
                        GenericWebServiceProviderHttpBasicAuthSsl.class,
                        AuthMode.HttpBasicAuthSsl));

               }
               else if (AuthMode.HttpBasicAuth.equals(authMode))
               {
                  add(new EndpointConfiguration(new EndpointConfiguration.ModelIdUrlPair(
                        md.getId(), WsUtils.encodeInternalEndpointPath(servletPath,
                              partitionId, modelId, endpointPath)), partitionId, wsdl,
                        GenericWebServiceProviderHttpBasicAuth.class,
                        AuthMode.HttpBasicAuth));

               }
               else if (AuthMode.WssUsernameToken.equals(authMode))
               {
                  add(new EndpointConfiguration(new EndpointConfiguration.ModelIdUrlPair(
                        md.getId(), WsUtils.encodeInternalEndpointPath(servletPath,
                              partitionId, modelId, endpointPath)), partitionId, wsdl,
                        GenericWebServiceProviderWssUsernameToken.class,
                        AuthMode.WssUsernameToken));
               }
            }
         }
      }
      catch (RuntimeException e)
      {
         WebServiceEnv.removeCurrent();
         throw e;
      }
   }

   private boolean isEqual(List<FormalParameter> fp1, List<FormalParameter> fp2)
   {
      if (fp1 == null && fp2 == null)
      {
         return true;
      }
      if (fp1 == null && fp2 != null || fp2 == null && fp1 != null)
      {
         return false;
      }
      if (fp1.size() != fp2.size())
      {
         return false;
      }
      TreeMap<String, FormalParameter> fp1Map = new TreeMap<String, FormalParameter>();
      TreeMap<String, FormalParameter> fp2Map = new TreeMap<String, FormalParameter>();

      for (FormalParameter formalParameter : fp2)
      {
         fp2Map.put(formalParameter.getId(), formalParameter);
      }

      for (FormalParameter formalParameter : fp1)
      {
         fp1Map.put(formalParameter.getId(), formalParameter);
      }

      for (FormalParameter f1 : fp1Map.values())
      {
         FormalParameter f2 = fp2Map.get(f1.getId());
         if ( !f2.getName().equals(f1.getName()))
         {
            return false;
         }
         if ( !f2.getDirection().equals(f1.getDirection()))
         {
            return false;
         }
         if ( !f2.getTypeId().equals(f1.getTypeId()))
         {
            return false;
         }
         if ( !f2.getAllAttributes().equals(f1.getAllAttributes()))
         {
            return false;
         }
      }
      return true;
   }

   private Map<Pair<String, String>, ProcessInterface> getProcessInterfacesMap()
   {
      return processInterfaces;
   }

   public Map<Pair<String, String>, ProcessInterface> getProcessInterfaces()
   {
      return Collections.unmodifiableMap(getProcessInterfacesMap());
   }

   private EndpointConfigurationStorage()
   {
      /* Singleton */
   }
}
