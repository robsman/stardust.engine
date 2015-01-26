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
package org.eclipse.stardust.engine.cli.console;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.ImplementationDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ListCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();
   private Map runOptions;

   private static final String DEPLOYMENTS = "deployments";
   private static final String ALLVERSIONS = "allVersions";
   private static final String MODEL = "model";
   private static final String SHORT = "short";
   private static final String VERBOSE = "verbose";

   static
   {
      argTypes.register("-deployments", "-d", DEPLOYMENTS,
            "Lists deployed model versions.", false);
      argTypes.register("-allVersions", "-a", ALLVERSIONS, "Lists all deployed model versions and their relationships to other models.", false);
      argTypes.register("-model", "-m", MODEL, "Lists the deployment information for a specific model id. ", true);
      argTypes.register("-short", "-s", SHORT, "With this option detailed information concerning the linking is left out.", false);
      argTypes.register("-verbose", "-v", VERBOSE, "With this option inactive implementation relations are shown as well.", false);
      argTypes.addExclusionRule(new String[] {DEPLOYMENTS, ALLVERSIONS}, false);
      argTypes.addExclusionRule(new String[] {DEPLOYMENTS, MODEL}, false);
      argTypes.addExclusionRule(new String[] {DEPLOYMENTS, SHORT}, false);
      argTypes.addExclusionRule(new String[] {DEPLOYMENTS, VERBOSE}, false);      
   }

   private QueryService service;

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      this.runOptions = options;      
      if (options.containsKey(DEPLOYMENTS))
      {
         listDeployments();
      } else {
         retrieveModelVersionTree();
      }
      return 0;
   }

   private void listDeployments()
   {
      print("\nDeployed models:");
      print("-------------------------\n");
      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
      try
      {
         QueryService service = serviceFactory.getQueryService();
         DeployedModelDescription activeModel = null;
         try
         {
            activeModel = service.getActiveModelDescription();
         }
         catch (ObjectNotFoundException e)
         {
         }

         for (Iterator i = service.getAllModelDescriptions().iterator(); i.hasNext();)
         {
            DeployedModelDescription model = (DeployedModelDescription) i.next();
            printModel(model);
            print("\n");
         }

         if (activeModel != null)
         {
            print("\nActive model in the audit trail:");
            print("--------------------------------\n");
            printModel(activeModel);

         }
         else
         {
            print("No active model found in the audit trail.\n");
         }
      }
      catch (ApplicationException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
      finally
      {
         serviceFactory.close();
      }
   }
   
   private void retrieveModelVersionTree() {
      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
      service = serviceFactory.getQueryService();
      DeployedModelQuery query; 
      if (runOptions.containsKey(MODEL)) {
         String modelID = (String) runOptions.get(MODEL);
         modelID = modelID.replaceAll("'", "");
         if (runOptions.containsKey(ALLVERSIONS)) {
            query = DeployedModelQuery.findForId(modelID);
         } else {
            query = DeployedModelQuery.findActiveForId(modelID);
         }                     
      } else {
         if (runOptions.containsKey(ALLVERSIONS)) {
            query = DeployedModelQuery.findAll();
         } else {
            query = DeployedModelQuery.findActive();
         }
      }
      Models models = service.getModels(query);
      printModelVersionTree(models);      
   }

   public String getSummary()
   {
      return "Lists audit trail contents.";
   }

   private void printModel(DeployedModelDescription model)
   {
      print("Model Name: " + model.getName());
      print("Model Id:   " + model.getId());
      print("Model OID:  " + model.getModelOID());
      print("Valid From: " + formatDate(model.getValidFrom()));
   }
   
   private void printModelVersionTree(Models models) {
      String result = "";
      String actModel = "";
      for (Iterator<DeployedModelDescription> i = models.iterator(); i.hasNext();) {         
         DeployedModelDescription desc = i.next();
         if (!actModel.equals(desc.getName())) {
            result = result + desc.getName() + "\n";
            actModel = desc.getName();
         } else {
            result = result + "\n";
         }
         result = result + "  " + desc.getName() + " - Version " + desc.getVersion() + "(OID:" + desc.getModelOID() + ")";
         if (desc.isActive()) {
            result = result + " (active)";
         }
         result = result + "\n";
         if (!runOptions.containsKey(SHORT)) {
            result = result + "    Provider relationships for model elements used by other models:" + "\n";
            for (Iterator<Long> j = desc.getConsumerModels().iterator(); j.hasNext();) {
               Long oid = j.next();
               DeployedModelDescription desc2 = service.getModelDescription(oid);
               result = result + "       used by " + desc2.getName() + " - Version " + desc2.getVersion() + "(OID:" + desc2.getModelOID() + ")\n";
            }
            result = result + "    Consumer relationships for model elements provided by other models:" + "\n";
            for (Iterator<Long> j = desc.getProviderModels().iterator(); j.hasNext();) {
               Long oid = j.next();
               DeployedModelDescription desc2 = service.getModelDescription(oid);
               result = result + "       using " + desc2.getName() + " - Version " + desc2.getVersion() + "(OID:" + desc2.getModelOID() + ")\n";
            }
            result = result + "    Provided Process Interfaces\n";
            Map<String, List<ImplementationDescription>> map = desc.getImplementationProcesses();
            for (Iterator<String> j = map.keySet().iterator();j.hasNext();) {
               String processID = j.next();
               result = result + "       " + processID + "\n";            
               List<ImplementationDescription> list = map.get(processID);
               for (Iterator<ImplementationDescription> k = list.iterator(); k.hasNext();) {
                  ImplementationDescription idesc = k.next();
                  if ((runOptions.containsKey(VERBOSE) && !idesc.isActive()) || idesc.isActive()) {
                     DeployedModelDescription desc2 = service.getModelDescription(idesc.getImplementationModelOid());
                     result = result + "          implemented by " + desc2.getName() + " - Version " + desc2.getVersion() + "(OID:" + desc2.getModelOID() + ")";
                     if (desc2.getModelOID() == desc.getModelOID()) {
                        result = result + " (Default)";
                     }
                     if (idesc.isPrimaryImplementation()) {
                        result = result + " (Primary Implementation)";
                     }
                     if (!idesc.isActive()) {
                        result = result + " - usage inactive";
                     }
                     result = result + "\n";                     
                  }
               }
            }  
         }
         result = result + "\n";
      }
      print(result);
   }

   private String formatDate(Date date)
   {
      if (date != null)
      {
         return DateFormat.getDateInstance().format(date);
      }
      else
      {
         return "UnSpecified";
      }
   }

}
