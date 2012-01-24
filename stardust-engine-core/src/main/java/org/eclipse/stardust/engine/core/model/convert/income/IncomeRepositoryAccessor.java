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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class IncomeRepositoryAccessor
{
   public final static Logger log = Logger.getLogger(IncomeRepositoryAccessor.class);

   private IncomeProcessDefinition incomeModel;

   private IncomeConverter converter;

   public Map diamgrams;

   public Map objecttypes;

   public Map roles;

   public Map documents;

   public Map conditions;

   public Map projects;

   public Map getRoles()
   {
      return roles;
   }

   public IncomeRepositoryAccessor(IncomeConverter converter)
   {
      this.objecttypes = new HashMap();
      this.documents = new HashMap();
      this.conditions = new HashMap();
      this.projects = new HashMap();
      this.converter = converter;
      this.projects = this.loadProjects();
   }

   public void addObjecttype(IncomeObjecttype objecttype)
   {
      this.objecttypes.put(objecttype.getId(), objecttype);
   }

   public IncomeProcessDefinition getIncomeModel()
   {
      return incomeModel;
   }

   public void setIncomeModel(IncomeProcessDefinition incomeModel)
   {
      this.incomeModel = incomeModel;
   }

   /**
    * Method loads all available in/out connections and creates the associations between
    * activities and objectstores loaded before.
    * 
    * @param activities
    * @param objectstores
    * @return Map of IncomeConnection objects.
    */
   private Map loadConnections(Map activities, Map objectstores)
   {

      if (activities == null || activities.isEmpty())
      {
         log.error("Load activities first.");
         throw new RuntimeException("Load activities first.");
      }

      if (objectstores == null || objectstores.isEmpty())
      {
         log.error("Load objectstores first.");
         throw new RuntimeException("Load objectstores first.");
      }

      Map connections = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeConnection.LOAD_QUERY); // TODO Type
         // resctriction

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            int activityID = rSet.getInt(IncomeConnection.ACTIVITY_ID_FIELD);
            int objectstoreID = rSet.getInt(IncomeConnection.OBJECTSTORE_ID_FIELD);
            int type = rSet.getInt(IncomeConnection.TYPE_FIELD);
            int color = rSet.getInt(IncomeConnection.OBJECTTYPE_COLOR_FIELD);

            String connectionType = null;
            String id = activityID + "_" + objectstoreID;
            String name = activityID + "_" + objectstoreID;

            if (type == 0)
            {
               connectionType = IncomeConnection.IN_CONNECTION_TYPE;
            }
            else if (type == 1)
            {
               connectionType = IncomeConnection.OUT_CONNECTION_TYPE;
            }

            IncomeConnection iConnection = new IncomeConnection(id, name, "N/A",
                  connectionType);

            IncomeActivity activity = (IncomeActivity) activities.get(new Integer(
                  activityID));
            IncomeObjectstore objectstore = (IncomeObjectstore) objectstores
                  .get(new Integer(objectstoreID));

            if (activity != null && objectstore != null)
            {
               iConnection.setActivity(activity);
               iConnection.setObjectstore(objectstore);
            }

            if (color > 0 && conditions.containsKey("" + color))
            {
               String condition = (String) conditions.get("" + color);
               iConnection.setCondition(condition);
            }

            connections.put(activityID + "_" + objectstoreID, iConnection);
         }
      }
      catch (SQLException e)
      {
         log.error("Cannot load connections from repository.");
         throw new RuntimeException("Cannot load connections from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return connections;
   }

   /**
    * Method loads all objectstores assigned to a given diagram.
    * 
    * @param diagramID
    * @return Map of IncomeObjectstores objects.
    */
   private Map loadObjectstoresByDiagramID(int diagramID)
   {
      Map objectstores = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeObjectstore.LOAD_QUERY);
         pStmt.setInt(1, diagramID);

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            // retreive a row of the result set
            String id = new Integer(rSet.getInt(IncomeObjectstore.ID_FIELD)).toString();
            String name = rSet.getString(IncomeObjectstore.NAME_FIELD);
            String description = rSet.getString(IncomeObjectstore.DESCRIPTION_FIELD);
            Integer objecttypeID = new Integer(rSet
                  .getInt(IncomeObjectstore.OBJECTTYPE_FIELD));

            // get objectstore out of the cache or create a new one
            IncomeObjectstore objectstore = null;
            if (objectstores.containsKey(new Integer(id)))
            {
               objectstore = (IncomeObjectstore) objectstores.get(new Integer(id));
            }
            else
            {
               objectstore = new IncomeObjectstore(id, name, description);
            }

            // add the objecttype to the objectstore
            if (objecttypeID != null && objecttypeID.intValue() > 0)
            {
               IncomeObjecttype objecttype = (IncomeObjecttype) this.objecttypes
                     .get(objecttypeID);
               objectstore.addObjecttype(objecttype);
            }

            // put the objectectstore to the cache if not already exists
            if (!objectstores.containsKey(new Integer(id)))
            {
               objectstores.put(new Integer(id), objectstore);
            }

         }
      }
      catch (SQLException e)
      {
         log.error("Cannot load objectstores from repository.");
         throw new RuntimeException("Cannot load objectstores from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return objectstores;
   }

   /**
    * Method loads all conditions assigned to an project.
    * 
    * @param projectID
    * @return Map of String objects.
    */
   private Map loadConditionsByProjectID(int projectID)
   {
      Map conds = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection
               .prepareStatement("select p.*  from inc4_user_prefs p where  p.upr_preference like 'RES_INC_PREFERENCES_KIND_OF_OBJECT_%'   and upr_pro_id = ?");
         pStmt.setInt(1, projectID);

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {

            String key = rSet.getString("upr_preference");
            String value = rSet.getString("upr_pref_value");

            key = key.substring(key.length() - 1);

            try
            {
               Integer i = new Integer(key);
               if (!conditions.containsKey(i))
               {
                  conds.put(key, value);
               }
            }
            catch (NumberFormatException e)
            {
               // ignore
            }

         }
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Cannot load conditions from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return conds;
   }

   /**
    * Method loads all projects.
    * 
    * @return
    */
   private Map loadProjects()
   {
      Map projects = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeProject.LOAD_QUERY);

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            // retreive a row of the result set
            String id = new Integer(rSet.getInt(IncomeProject.ID_FIELD)).toString();
            String name = rSet.getString(IncomeProject.NAME_FIELD);

            projects.put(id, new IncomeProject(id, name,
                  IncomeProject._DEFAULT_DESCRIPTION));
         }
      }
      catch (SQLException e)
      {
         log.error("Cannot load projects from repository.");
         throw new RuntimeException("Cannot load projects from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return projects;
   }

   /**
    * Method loads all activities assigned to a given diagram.
    * 
    * @param diagramID
    * @return Map of IncomeActivity objects.
    */
   private Map loadActivitiesByDiagramID(int diagramID)
   {
      Map activities = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeActivity.LOAD_QUERY);
         pStmt.setInt(1, diagramID);

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            String id = new Integer(rSet.getInt(IncomeActivity.ID_FIELD)).toString();
            String name = rSet.getString(IncomeActivity.NAME_FIELD);
            String description = rSet.getString(IncomeActivity.DESCRIPTION_FIELD);
            Integer roleId = new Integer(rSet.getInt(IncomeActivity.ROLE_FIELD));
            Integer subprocessId = new Integer(rSet
                  .getInt(IncomeActivity.SUBPROCESS_ID_FIELD));

            IncomeActivity activity = new IncomeActivity(id, name, description);

            if (subprocessId != null && subprocessId.intValue() > 0)
            {
               activity.setSubprocess(subprocessId);
            }

            if (roleId != null)
            {
               IncomeRole role = (IncomeRole) this.roles.get(roleId);
               activity.setRole(role);
            }

            IncomeDocument document = this.loadDocumentByActivityID(new Integer(id)
                  .intValue());
            if (document != null)
            {
               activity.setDocument(document);
               documents.put(new Integer(id), document);
            }

            activities.put(new Integer(id), activity);
         }
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Cannot load activities from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return activities;
   }

   private IncomeDocument loadDocumentByActivityID(int activityID)
   {
      IncomeDocument document = null;

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeDocument.LOAD_QUERY);
         pStmt.setInt(1, activityID);

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            String id = new Integer(rSet.getInt(IncomeDocument.ID_FIELD)).toString();
            String name = rSet.getString(IncomeDocument.NAME_FIELD);
            String description = rSet.getString(IncomeDocument.DESCRIPTION_FIELD);
            String reference = rSet.getString(IncomeDocument.REFERENCE_FIELD);

            document = new IncomeDocument(id, name, description, reference);

            break;
         }
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Cannot load attributes from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return document;
   }

   /**
    * Method loads all roles for a project.
    * 
    * @param projectID
    * @return Map of IncomeRoles objects.
    */
   private Map loadRolesByProjectID(int projectID)
   {
      Map roles = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeRole.LOAD_QUERY);
         pStmt.setInt(1, projectID);

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            String id = new Integer(rSet.getInt(IncomeRole.ID_FIELD)).toString();
            String name = rSet.getString(IncomeRole.NAME_FIELD);
            String description = rSet.getString(IncomeRole.DESCRIPTION_FIELD);

            IncomeRole role = new IncomeRole(id, name, description);

            roles.put(new Integer(id), role);
         }
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Cannot load attributes from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return roles;
   }

   /**
    * Method loads all diagrams for a project.
    * 
    * @param projectID
    * @return Map of IncomeProcessDefinition objects.
    */
   private Map loadDiagramsByProjectID(int projectID)
   {
      Map models = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeProcessDefinition.LOAD_QUERY);
         pStmt.setInt(1, projectID);

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            String id = new Integer(rSet.getInt(IncomeProcessDefinition.ID_FIELD))
                  .toString();
            String name = rSet.getString(IncomeProcessDefinition.NAME_FIELD);
            String description = rSet
                  .getString(IncomeProcessDefinition.DESCRIPTION_FIELD);

            IncomeProcessDefinition model = new IncomeProcessDefinition(id, name,
                  description);

            models.put(new Integer(id), model);
         }
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Cannot load diagram from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return models;
   }

   /**
    * Method loads all objecttypes for a project.
    * 
    * @param projectID
    * @return Map of IncomeObjecttype objects.
    */
   private Map loadObjecttypeByProjectID(int projectID)
   {
      Map objecttypes = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeObjecttype.LOAD_QUERY);
         pStmt.setInt(1, projectID);

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            String id = new Integer(rSet.getInt(IncomeObjecttype.ID_FIELD)).toString();
            String name = rSet.getString(IncomeObjecttype.NAME_FIELD);
            String description = rSet.getString(IncomeObjecttype.DESCRIPTION_FIELD);

            IncomeObjecttype objecttype = new IncomeObjecttype(id, name, description);

            this.loadAttributesByObjecttypeID(objecttype);

            objecttypes.put(new Integer(id), objecttype);
         }
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Cannot load objecttypes from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return objecttypes;
   }

   /**
    * Method loads all atributes for a objecttype.
    * 
    * @param objecttype
    * @return Map of IncomeRoles objects.
    */
   private Map loadAttributesByObjecttypeID(IncomeObjecttype objecttype)
   {
      Map attributes = new HashMap();

      Connection connection = null;
      PreparedStatement pStmt = null;
      ResultSet rSet = null;

      try
      {
         connection = this.getConnection(converter);

         pStmt = connection.prepareStatement(IncomeObjecttype.IncomeAttribute.LOAD_QUERY);
         pStmt.setInt(1, new Integer(objecttype.getId()).intValue());

         rSet = pStmt.executeQuery();

         while (rSet.next())
         {
            String id = new Integer(rSet
                  .getInt(IncomeObjecttype.IncomeAttribute.ID_FIELD)).toString();
            String name = rSet.getString(IncomeObjecttype.IncomeAttribute.NAME_FIELD);
            String description = rSet
                  .getString(IncomeObjecttype.IncomeAttribute.DESCRIPTION_FIELD);
            int dataType = rSet.getInt(IncomeObjecttype.IncomeAttribute.DATA_TYPE_FIELD);

            IncomeObjecttype.IncomeAttribute attribute = objecttype.new IncomeAttribute(
                  id, name, description, dataType);

            attributes.put(id, attribute);
         }
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Cannot load attributes from repository.");
      }
      finally
      {
         this.releaseConnection(connection, pStmt, rSet);
      }

      return attributes;
   }

   private Connection getConnection(IncomeConverter converter)
   {
      if (log.isDebugEnabled())
      {
         log.debug("Getting connection (" + converter.getDatabase() + ", "
               + converter.getUsername() + ", " + converter.getPassword() + ").");
      }

      String databaseURL = converter.getDatabase();
      String username = converter.getUsername();
      String password = converter.getPassword();

      String driverName = "oracle.jdbc.driver.OracleDriver";

      try
      {
         Class.forName(driverName);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("Cannot find jdbc driver class.");
      }

      Connection connection = null;

      try
      {
         connection = DriverManager.getConnection(databaseURL, username, password);
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Cannot get connection from database '" + databaseURL
               + "'.");
      }

      return connection;
   }

   private void releaseConnection(Connection connection, PreparedStatement pStatement,
         ResultSet rSet)
   {
      try
      {
         if (rSet != null)
         {
            rSet.close();
            rSet = null;
         }

         if (pStatement != null)
         {
            pStatement.close();
            pStatement = null;
         }

         if (rSet != null)
         {
            connection.close();
            connection = null;
         }
      }
      catch (SQLException e)
      {
      }
   }

   public Map getObjecttypes()
   {
      return objecttypes;
   }

   public void setObjecttypes(Map objecttypes)
   {
      this.objecttypes = objecttypes;
   }

   public Map getDocuments()
   {
      return documents;
   }

   public void load()
   {
      this.diamgrams = this.loadDiagramsByProjectID(converter.getProjectID());
      this.objecttypes = this.loadObjecttypeByProjectID(converter.getProjectID());
      this.conditions = this.loadConditionsByProjectID(converter.getProjectID());
      this.roles = this.loadRolesByProjectID(converter.getProjectID());

      for (Iterator diagramIterator = this.diamgrams.values().iterator(); diagramIterator
            .hasNext();)
      {
         IncomeProcessDefinition pDef = (IncomeProcessDefinition) diagramIterator.next();

         Map activities = this.loadActivitiesByDiagramID(new Integer(pDef.getId())
               .intValue());
         Map objectstores = this.loadObjectstoresByDiagramID(new Integer(pDef.getId())
               .intValue());

         this.loadConnections(activities, objectstores);

         for (Iterator activityIterator = activities.values().iterator(); activityIterator
               .hasNext();)
         {
            pDef.addActivity((IncomeActivity) activityIterator.next());
         }

         for (Iterator objectstoreIterator = objectstores.values().iterator(); objectstoreIterator
               .hasNext();)
         {
            pDef.addObjectstore((IncomeObjectstore) objectstoreIterator.next());
         }

         converter.addProcessDefinition(pDef);
      }
   }

   public Map getProjects()
   {
      return projects;
   }
}
