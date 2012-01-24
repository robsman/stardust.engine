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
package org.eclipse.stardust.engine.core.model.convert.income;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.core.model.builder.DefaultModelBuilder;
import org.eclipse.stardust.engine.core.model.builder.ModelBuilder;
import org.eclipse.stardust.engine.core.model.convert.Converter;
import org.eclipse.stardust.engine.core.pojo.data.Type;


/**
 * @author amueller
 * @version $$
 */
public class IncomeConverter extends Converter
{
   public final static Logger log = Logger.getLogger(IncomeConverter.class);
   
   private final static String _UNDERSCORE_DELMITTER = "_";

   private final static String _SPACE_DELMITTER = " ";

   private Set processDefinitions; // Set of IncomeProcessDefinition objects

   private int mode;
   
   private int projectID = 2809;
   
   private String database;
   
   private String username;
   
   private String password;

   private IncomeRepositoryAccessor accessor;

   public IncomeConverter()
   {
      this.processDefinitions = new HashSet();
   }

   public IModel createModel() // TODO
   {
      return this.createModel(Converter.PROTOTYPE_MODE, "PROCESS_ID", "PROCESS_NAME",
            "N/A");
   }

   private IModel createModel(int mode, String id, String name, String description)
   {
      this.mode = mode;
      
      this.accessor.load();
      
      ModelBuilder builder = DefaultModelBuilder.create();
      this.model = builder.createModel(id, name, description);

      this.createAllRoles(builder, this.model);

      this.createAllDatas(builder, this.model);

      this.createAllApplications(builder, this.model);

      this.createAllProcessDefinitions(this.model, this.processDefinitions);

      this.createSubprocessImplementation(this.model, this.processDefinitions);

      populateDefaultDiagrams();

      return this.model;
   }

   public void addProcessDefinition(IncomeProcessDefinition processDefinition)
   {
      this.processDefinitions.add(processDefinition);
   }

   public boolean isProductionMode()
   {
      return mode == 1 ? true : false;
   }

   /**
    * Method creates all data objects defined as objecttypes in the income model.
    * 
    * @param builder
    * @param aModel
    */
   private void createAllDatas(ModelBuilder builder, IModel aModel)
   {
      for (Iterator _dataIterator = this.accessor.getObjecttypes().values().iterator(); _dataIterator
            .hasNext();)
      {
         IncomeObjecttype objecttype = (IncomeObjecttype) _dataIterator.next();

         for (Iterator _attributeIterator = objecttype.getAttributes().iterator(); _attributeIterator
               .hasNext();)
         {
            IncomeObjecttype.IncomeAttribute attribute = (IncomeObjecttype.IncomeAttribute) _attributeIterator
                  .next();

            Type type = null;
            String defaultValue = null;

            if (attribute.getDataType().equals(Integer.class))
            {
               type = Type.Integer;
               defaultValue = new String("0");
            }
            else if (attribute.getDataType().equals(String.class))
            {
               type = Type.String;
               defaultValue = new String();
            }
            builder.createPrimitiveData(aModel, objecttype.getName()
                  + IncomeConverter._UNDERSCORE_DELMITTER + attribute.getName(),
                  objecttype.getName() + IncomeConverter._SPACE_DELMITTER
                        + attribute.getName(), type, defaultValue);
         }
      }
   }

   /**
    * Method creates all roles defined in the income model.
    * @param builder
    * @param aModel
    */
   private void createAllRoles(ModelBuilder builder, IModel aModel)
   {
      for (Iterator _roleIterator = this.accessor.getRoles().values().iterator(); _roleIterator
            .hasNext();)
      {
         IncomeRole role = (IncomeRole) _roleIterator.next();

         builder.createRole(aModel, role.getId(), role.getName());
      }
   }

   /**
    * Mehtod creates JSP Application defined in the income model.
    * @param builder
    * @param aModel
    */
   private void createAllApplications(ModelBuilder builder, IModel aModel)
   {
      for (Iterator documentIterator = this.accessor.getDocuments().values().iterator(); documentIterator
            .hasNext();)
      {
         IncomeDocument document = (IncomeDocument) documentIterator.next();

         builder.createJSPApplication(aModel, document.getId(), document.getName(),
               document.getReference());
      }
   }

   /**
    * After creating the activity graph of each process, this method will find
    * and prepare all activities that should be implemented as sub processes.
    * @param aModel
    * @param processDefintions
    */
   private void createSubprocessImplementation(IModel aModel, Set processDefintions)
   {
      // processing subprocesses
      for (Iterator _iterator = processDefintions.iterator(); _iterator.hasNext();)
      {
         IncomeProcessDefinition iProcessDefinition = (IncomeProcessDefinition) _iterator
               .next();
         for (Iterator activityIterator = iProcessDefinition.getActivities().iterator(); activityIterator
               .hasNext();)
         {
            IncomeActivity iActivity = (IncomeActivity) activityIterator.next();
            if (iActivity.getSubprocess() != null)
            {
               log.info("Activity '" + iActivity.getName()
                     + "' should be implemented as subprocess.");

               IProcessDefinition subprocess = aModel.findProcessDefinition(iActivity
                     .getSubprocess()
                     + "");
               IProcessDefinition process = aModel
                     .findProcessDefinition(iProcessDefinition.getId());

               if (subprocess != null && process != null)
               {
                  IActivity activity = process.findActivity(iActivity.getId());
                  activity.setImplementationType(ImplementationType.SubProcess);
                  activity.setImplementationProcessDefinition(subprocess);
               }
            }
         }
      }
   }

   /**
    * Method creates all process definitions defined in the income model.
    * @param aModel
    * @param processDefinitions
    */
   public void createAllProcessDefinitions(IModel aModel, Set processDefinitions)
   {
      for (Iterator pDefIterator = processDefinitions.iterator(); pDefIterator.hasNext();)
      {

         IncomeProcessDefinition iProcessDefinition = (IncomeProcessDefinition) pDefIterator
               .next();

         // create a carnot process definition
         IProcessDefinition processDefinition = aModel.createProcessDefinition(
               iProcessDefinition.getId(), iProcessDefinition.getName(),
               iProcessDefinition.getDescription());

         iProcessDefinition.create(processDefinition);
      }
   }
   
   public int getProjectID()
   {
      return projectID;
   }

   public void setProjectID(int projectID)
   {
      this.projectID = projectID;
   }
   
   public boolean isInitialized()
   {
      return this.accessor != null;
   }
   
   public Map load(String database, String username, String password)
   {
      this.database = database;
      this.username = username;
      this.password = password;
      this.accessor = new IncomeRepositoryAccessor(this);
      return this.accessor.getProjects();
   }

   public String getDatabase()
   {
      return database;
   }

   public void setDatabase(String database)
   {
      this.database = database;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }
}
