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
package org.eclipse.stardust.engine.core.runtime.utils;

import java.util.*;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.*;


/**
 * Client side utility class that computes the permissions the user has to invoke the
 * service methods.
 * 
 * <p>Since permission computations may be expensive, the class by default is caching the
 * results of those computations. Because of that, it is important to maintain the
 * consistency of the cache by always using services obtained from the same instance
 * of a <code>ServiceFactory</code>. If the client needs to check permissions using two (or more) service factories,
 * it is required to instantiate separate <code>PermissionHelpers</code>, one for each
 * <code>ServiceFactory</code>.
 * 
 * <p>Example:
 * <blockquote><pre>
 * ServiceFactory factory = ... // obtained somehow.
 * WorkflowService wfs = factory.getWorkflowService();
 * DeployedModel model = wfs.getModel();
 * 
 * PermissionHelper helper = new PermissionHelper();
 * boolean canReadModelData = helper.hasPermission(wfs, Permissions.MODEL_READ_MODEL_DATA, model);
 * if (canReadModelData)
 * {
 *     List<ProcessDefinition> pds = wfs.getStartableProcessDefinitions();
 * }
 * </pre></blockquote>
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class PermissionHelper
{
   private Map<Class<? extends Service>, List<Permission>> permissions = CollectionUtils.newMap();
   private Set<String> startableProcesses = null;
   
   private boolean useCaches;
   private User user;
   
   /**
    * Default constructor. Identical with PermissionHelper(true).
    */
   public PermissionHelper()
   {
      this(true);
   }

   /**
    * Constructor used when caching should be disabled. In this case, each permission check
    * will fetch the permissions new from the engine.
    * 
    * @param useCaches false if you want to disable caching.
    */
   public PermissionHelper(boolean useCaches)
   {
      this.useCaches = useCaches;
   }
   
   /**
    * Constructor that enable caching and optionally provides an already fetched
    * <code>User</code> or the set of startable process IDs. If the <code>User</code>
    * is provided, then calls to filter access methods will use the provided user 
    * instead of fetching it from the service. Similarly, invocations of
    * <code>canStartProcess</code> will use the provided set of startable process IDs.
    *  
    * @param user the <code>User</code> corresponding to the used <code>ServiceFactory</code>.
    * @param startableProcesses a <code>Set</code> containing the IDs of the startable processes.
    */
   public PermissionHelper(User user, Set<String> startableProcesses)
   {
      this(true);
      this.user = user;
      this.startableProcesses = startableProcesses;
   }

   /**
    * Filters a list of process definitions based on the <code>processDefinition.readProcessInstanceData</code> permission.
    * The filtering operation will create a new list leaving the raw list unmodified.
    * 
    * @param service an instance of the <code>WorkflowService</code> from which permissions can be fetched.
    * @param processes the raw list of process definitions.
    * @return the filtered list containing only process definitions that grants the user access to it's process instances.
    */
   public List<ProcessDefinition> filterProcessAccess(WorkflowService service, List<ProcessDefinition> processes)
   {
      return filterProcessAccess(useCaches && user != null ? user : service.getUser(), service, processes);
   }
   
   /**
    * Filters a list of activities based on the <code>activity.readActivityInstanceData</code> permission.
    * The filtering operation will create a new list leaving the raw list unmodified. The actual grant requires
    * both the <code>activity.readActivityInstanceData</code> on the <code>Activity</code> and the
    * <code>processDefinition.readProcessInstanceData</code> on the <code>ProcessDefinition</code> containing the
    * <code>Activity</code>.
    * 
    * @param service an instance of the <code>WorkflowService</code> from which permissions can be fetched.
    * @param activities the raw list of activities.
    * @return the filtered list containing only activities that grants the user access to it's activity instances.
    */
   public List<Activity> filterActivityAccess(WorkflowService service, List<Activity> activities)
   {
      return filterActivityAccess(useCaches && user != null ? user : service.getUser(), service, activities);
   }
   
   /**
    * Checks if the <code>Activity</code> is granting the specific permission to the user.
    * 
    * @param service an instance of a <code>Service</code> from which permissions can be fetched.
    * @param permissionId the permission to be checked, i.e. <code>activity.readActivityInstanceData</code>.
    * @param activity the <code>Activity</code> on which the permission is defined.
    * @return <code>true</code> if the user has the permission granted.
    */
   public boolean hasPermission(Service service, String permissionId, Activity activity)
   {
      Scope scope = new ActivityScope(new ProcessScope(new ModelScope(activity.getModelOID()), activity.getProcessDefinitionId()), activity.getId());
      return hasPermission(service, permissionId, scope);
   }

   /**
    * Checks if the <code>ProcessDefinition</code> is granting the specific permission to the user.
    * 
    * @param service an instance of a <code>Service</code> from which permissions can be fetched.
    * @param permissionId the permission to be checked, i.e. <code>processDefinition.readProcessInstanceData</code>.
    * @param activity the <code>ProcessDefinition</code> on which the permission is defined.
    * @return <code>true</code> if the user has the permission granted.
    */
   public boolean hasPermission(Service service, String permissionId, ProcessDefinition process)
   {
      Scope scope = new ProcessScope(new ModelScope(process.getModelOID()), process.getId());
      return hasPermission(service, permissionId, scope);
   }

   /**
    * Checks if the <code>DeployedModelDescription</code> is granting the specific permission to the user.
    * 
    * @param service an instance of a <code>Service</code> from which permissions can be fetched.
    * @param permissionId the permission to be checked, i.e. <code>model.readModelData</code>.
    * @param activity the <code>DeployedModelDescription</code> on which the permission is defined.
    * @return <code>true</code> if the user has the permission granted.
    */
   public boolean hasPermission(Service service, String permissionId, DeployedModelDescription model)
   {
      Scope scope = new ModelScope(model.getModelOID());
      return hasPermission(service, permissionId, scope);
   }

   /**
    * Checks if the user can start the given process definition (and create a new process instance).
    * 
    * @param service an instance of the <code>WorkflowService</code> from which permissions can be fetched.
    * @param process the <code>ProcessDefinition</code> that will be started.
    * @return <code>true</code> if the user is allowed to start the process definition.
    */
   public boolean canStartProcess(WorkflowService service, ProcessDefinition process)
   {
      Set<String> startableProcesses = useCaches ? this.startableProcesses : null;
      if (startableProcesses == null)
      {
         startableProcesses = CollectionUtils.newSet();
         List<ProcessDefinition> processes = service.getStartableProcessDefinitions();
         for (ProcessDefinition pd : processes)
         {
            startableProcesses.add(pd.getId());
         }
         if (useCaches)
         {
            this.startableProcesses = startableProcesses;
         }
      }
      return startableProcesses.contains(process.getId());
   }

   /**
    * Checks if the user can perform the activity (activate, complete).
    * 
    * @param user the <code>User</code> that defines the grants.
    * @param activity the <code>Activity</code> to be performed.
    * @return <code>true</code> if the user can perform the activity.
    */
   public boolean canPerformActivity(User user, Activity activity)
   {
      ModelParticipant performer = activity.getDefaultPerformer();
      while (performer instanceof ConditionalPerformer)
      {
         ConditionalPerformer cp = (ConditionalPerformer) performer;
         Participant resolved = cp.getResolvedPerformer();
         // should always be null !
         if (resolved == null)
         {
            return true;
         }
         if (resolved instanceof ModelParticipant)
         {
            ModelParticipant participant = (ModelParticipant) resolved;
            if (CompareHelper.areEqual(participant.getId(), performer.getId()))
            {
               // prevent infinite loop
               return true;
            }
            performer = participant;
         }
         if (resolved instanceof User)
         {
            return CompareHelper.areEqual(user.getId(), ((User) resolved).getId());
         }
         if (resolved instanceof UserGroup)
         {
            List<UserGroup> groups = user.getAllGroups();
            for (UserGroup ug : groups)
            {
               if (CompareHelper.areEqual(ug.getId(), ((UserGroup) resolved).getId()))
               {
                  return true;
               }
            }
            return false;
         }
      }
      return checkGrants(performer, user.getAllGrants());
   }

   private List<ProcessDefinition> filterProcessAccess(User user, WorkflowService service, List<ProcessDefinition> processes)
   {
      List<ProcessDefinition> filtered = CollectionUtils.newList();
      for (ProcessDefinition process : processes)
      {
         if (hasPermission(service, Permissions.PROCESS_DEFINITION_READ_PROCESS_INSTANCE_DATA, process))
         {
            filtered.add(process);
         }
      }
      return filtered;
   }
   
   private List<Activity> filterActivityAccess(User user, WorkflowService service, List<Activity> activities)
   {
      List<Activity> filtered = CollectionUtils.newList();
      for (Activity activity : activities)
      {
         Scope processScope = new ProcessScope(new ModelScope(activity.getModelOID()), activity.getProcessDefinitionId());
         if (hasPermission(service, Permissions.ACTIVITY_READ_ACTIVITY_INSTANCE_DATA, activity) &&
               hasPermission(service, Permissions.PROCESS_DEFINITION_READ_PROCESS_INSTANCE_DATA, processScope))
         {
            filtered.add(activity);
         }
      }
      return filtered;
   }
   
   private boolean checkGrants(ModelParticipant performer, List<Grant> grants)
   {
      String namespace = performer.getNamespace();
      List<Grant> filteredGrants = CollectionUtils.newList(grants.size());
      for (Grant grant : grants)
      {
         if (CompareHelper.areEqual(namespace, grant.getNamespace()))
         {
            filteredGrants.add(grant);
         }
      }
      if (filteredGrants.isEmpty())
      {
         return false;
      }
      return checkFilteredGrants(performer, filteredGrants);
   }
   
   private boolean checkFilteredGrants(ModelParticipant performer, List<Grant> grants)
   {
      for (Grant grant : grants)
      {
         if (CompareHelper.areEqual(performer.getId(), grant.getId()))
         {
            return true;
         }
      }
      for (Organization organization : performer.getAllSuperOrganizations())
      {
         if (checkGrants(organization, grants))
         {
            return true;
         }
      }
      return false;
   }

   private boolean hasPermission(Service service, String permissionId, Scope scope)
   {
      if (permissionId.startsWith(Permissions.PREFIX))
      {
         permissionId = permissionId.substring(Permissions.PREFIX.length());
      }
      List<Permission> permissions = useCaches ? this.permissions.get(service.getClass()) : null;
      if (permissions == null)
      {
         if (service instanceof AdministrationService)
         {
            permissions = ((AdministrationService) service).getPermissions();
         }
         else if (service instanceof WorkflowService)
         {
            permissions = ((WorkflowService) service).getPermissions();
         }
         else if (service instanceof QueryService)
         {
            permissions = ((QueryService) service).getPermissions();
         }
         else
         {
            permissions = CollectionUtils.newList();
         }
         if (useCaches)
         {
            this.permissions.put(service.getClass(), permissions);
         }
      }
      for (Permission permission : permissions)
      {
         if (permission.getPermissionId().equals(permissionId))
         {
            return permission.getScopes().contains(scope);
         }
      }
      return false;
   }
}
