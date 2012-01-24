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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.dto.OrganizationInfoDetails;
import org.eclipse.stardust.engine.api.dto.RoleInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserGroupInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserInfoDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.core.model.beans.ScopedModelParticipant;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;



/**
 * @author stephan.born
 *
 */
public final class DepartmentUtils
{
   /**
    * Returns the department oid for the specified participant. The oid will be evaluated
    * by data values from the given process instance for the data the participant is bound to.
    * 
    * if the participant is not bound to any data then it will return 0.
    * 
    * @param participant the participant.
    * @param processInstance the process instance in which scope the data values will be retrieved.
    * @return the department oid < may be 0 if no department exists.
    */
   public static long getDepartmentOid(IModelParticipant participant,
         IProcessInstance processInstance)
   {
      List<IOrganization> restrictions = Authorization2.findRestricted(participant);
      if (restrictions.isEmpty())
      {
         return 0;
      }
      else
      {
         AuthorizationContext context = AuthorizationContext.create((ClientPermission) null);
         context.setProcessInstance(processInstance);
         return Authorization2.evaluateData(context, restrictions);
      }
   }
   
   /**
    * @param participant the participant.
    * @param processInstance the process instance in which scope the data values will be retrieved.
    * @return the department; may be null if no department exists.
    * 
    * @see #getDepartmentOid(IModelParticipant, IProcessInstance);
    */
   public static IDepartment getDepartment(IModelParticipant participant,
         IProcessInstance processInstance)
   {
      long departmentOid = getDepartmentOid(participant, processInstance);
      if (departmentOid > 0)
      {
         return DepartmentBean.findByOID(departmentOid);
      }
      return null;
   }
   
   public static IParticipant getScopedParticipant(ParticipantInfo participant,
         ModelManager modelManager)
   {
      IParticipant result;
      if (participant instanceof ModelParticipantInfo)
      {
         ModelParticipantInfo mpInfo = (ModelParticipantInfo) participant;
         IModelParticipant modelParticipant = modelManager.findModelParticipant(mpInfo);

         IDepartment department = getDepartment(mpInfo.getDepartment());
         result = new ScopedModelParticipant(modelParticipant, department);
      }
      else if (participant instanceof UserGroupInfo)
      {
         UserGroupInfo ugInfo = (UserGroupInfo) participant;
         IUserGroup userGroup;
         if (ugInfo.getOID() == 0)
         {
            userGroup = UserGroupBean.findById(ugInfo.getId(), SecurityProperties
                  .getPartitionOid());
         }
         else
         {
            userGroup = UserGroupBean.findByOid(ugInfo.getOID());
         }

         result = userGroup;
      }
      else
      {
         throw new InternalException("ParticipantInfo " + participant + " not supported.");
      }

      return result;
   }
   
   public static ParticipantInfo getParticipantInfo(PerformerType performerType,
         long runtimeOid, long departmentOid, long modelOid)
   {
      ParticipantInfo result = null;
      
      switch (performerType.getValue())
      {
         case PerformerType.MODEL_PARTICIPANT:
            IModelParticipant mp = ModelManagerFactory.getCurrent().
               findModelParticipant(modelOid, runtimeOid);
            DepartmentInfo departmentInfo = null;
            if(departmentOid != 0)
            {
               IDepartment department = DepartmentBean.findByOID(departmentOid);
               departmentInfo = getDepartmentInfo(department);
            }
            if (mp instanceof IOrganization)
            {
               result = new OrganizationInfoDetails(runtimeOid, ModelUtils.getQualifiedId(mp),
                     mp.getName(), DepartmentUtils.getFirstScopedOrganization(mp) != null,
                     mp.getBooleanAttribute(PredefinedConstants.BINDING_ATT),
                     departmentInfo);

            }
            else if (mp instanceof IRole)
            {
               result = new RoleInfoDetails(runtimeOid, ModelUtils.getQualifiedId(mp),
                     mp.getName(), DepartmentUtils.getFirstScopedOrganization(mp) != null,
                     false, departmentInfo);
            }
            break;
   
         case PerformerType.USER_GROUP:
            IUserGroup group = UserGroupBean.findByOid(runtimeOid);
            result = new UserGroupInfoDetails(group);
            break;
         
         case PerformerType.USER:
            IUser user = UserBean.findByOid(runtimeOid);
            result = new UserInfoDetails(user);
            break;
            
         default:
            break;
      }
      return result;
   }
   
   
   
   public static ParticipantInfo getParticipantInfo(IParticipant participant,
         ModelManager modelManager)
   {
      ParticipantInfo result;

      IParticipant rawParticipant = participant;
      DepartmentInfo departmentInfo = null;
      
      if (rawParticipant instanceof IScopedModelParticipant)
      {
         IScopedModelParticipant scopedModelParticipant = (IScopedModelParticipant) rawParticipant;
         
         rawParticipant = scopedModelParticipant.getModelParticipant();
         departmentInfo = getDepartmentInfo(scopedModelParticipant.getDepartment());
      }

      if (rawParticipant instanceof IOrganization)
      {
         IOrganization org = (IOrganization) rawParticipant;
         result = new OrganizationInfoDetails(modelManager.getRuntimeOid(org), ModelUtils.getQualifiedId(org),
               org.getName(), DepartmentUtils.getFirstScopedOrganization(org) != null,
               org.getBooleanAttribute(PredefinedConstants.BINDING_ATT),
               departmentInfo);
         
      }
      else if (rawParticipant instanceof IRole)
      {
         IRole role = (IRole) rawParticipant;
         result = new RoleInfoDetails(modelManager.getRuntimeOid(role), ModelUtils.getQualifiedId(role),
               role.getName(), DepartmentUtils.getFirstScopedOrganization(role) != null,
               false, departmentInfo);
      }
      else if (rawParticipant instanceof IUserGroup)
      {
         IUserGroup group = (IUserGroup) rawParticipant;
         result = new UserGroupInfoDetails(group);
      }
      else if (rawParticipant instanceof IUser)
      {
         IUser user = (IUser) rawParticipant;
         result = new UserInfoDetails(user);
      }
      else
      {
         throw new InternalException("Participant " + participant
               + " cannot be converted to ParticipantInfo.");
      }

      return result;
   }
   
   public static IDepartment getDepartment(ParticipantInfo participant)
   {
      IDepartment department = null;
      if (participant instanceof ModelParticipantInfo)
      {
         department = getDepartment(((ModelParticipantInfo) participant).getDepartment());
      }

      return department;
   }
   
   public static IDepartment getDepartment(DepartmentInfo depInfo)
   {
      return depInfo == null ? null : 
             depInfo == Department.DEFAULT ? IDepartment.NULL :
                        DepartmentBean.findByOID(depInfo.getOID());
   }
   
   public static DepartmentInfo getDepartmentInfo(IDepartment department)
   {
      DepartmentInfo depInfo = null;
      if (null != department)
      {
         depInfo = new DepartmentInfoDetails(department);
      }

      return depInfo;
   }
   
   public static boolean areEqual(IDepartment dep1, DepartmentInfo dep2)
   {
      if (dep1 == null)
      {
         return dep2 == null || dep2 == Department.DEFAULT;
      }
      if (dep2 == null)
      {
         return false;
      }
      if (dep2.getOID() == 0)
      {
         return CompareHelper.areEqual(dep1.getId(), dep2.getId()) &&
            dep1.getRuntimeOrganizationOID() == dep2.getRuntimeOrganizationOID();
      }
      return dep1.getOID() == dep2.getOID();
   }
   
   public static boolean areEqual(DepartmentInfo dep1, DepartmentInfo dep2)
   {
      if (dep1 == null || dep1 == Department.DEFAULT)
      {
         return dep2 == null || dep2 == Department.DEFAULT;
      }
      if (dep2 == null)
      {
         return false;
      }
      if (dep1.getOID() == 0 || dep2.getOID() == 0)
      {
         return CompareHelper.areEqual(dep1.getId(), dep2.getId()) &&
            dep1.getRuntimeOrganizationOID() == dep2.getRuntimeOrganizationOID();
      }
      return dep1.getOID() == dep2.getOID();
   }
   
   /**
    * @param participant a model participant
    * @return true if participant is an organization and defines its own departments. 
    */
   public static boolean isRestrictedModelParticipant(IModelParticipant participant)
   {
      return participant instanceof IOrganization
            && participant
                  .getBooleanAttribute(PredefinedConstants.BINDING_ATT);
   }
   
   public static Iterator<IDepartment> getChildDepartmentsIterator(IDepartment parent)
   {
      // TODO: Use department cache in order to retrieve that information.
      return DepartmentBean.findAllForParent(parent);
   }
   
   public static List<IDepartment> getChildDepartments(IDepartment parent)
   {
      // TODO: Use department cache in order to retrieve that information.
      return CollectionUtils.newListFromIterator(getChildDepartmentsIterator(parent));
   }
   
   public static IOrganization getOrganization(final IDepartment department) throws ObjectNotFoundException
   {
      final long orgOid = department.getRuntimeOrganizationOID();
      final ModelManager manager = ModelManagerFactory.getCurrent();
      final IModelParticipant participant = manager.findModelParticipant(
            PredefinedConstants.ANY_MODEL, orgOid);
      if (participant instanceof IOrganization)
      {
         return (IOrganization) participant;
      }
      throw new ObjectNotFoundException(BpmRuntimeError
            .MDL_UNKNOWN_PARTICIPANT_RUNTIME_OID.raise(orgOid));
   }
   
   private DepartmentUtils()
   {
      // plain utility class needs no instantiation
   }

   public static boolean isChild(IModelParticipant child, IOrganization parent)
   {
      Set<IModelParticipant> visited = CollectionUtils.newSet();
      return isChild(child, parent, visited);
   }

   private static boolean isChild(IModelParticipant child, IOrganization parent, Set<IModelParticipant> visited)
   {
      if (child == null || visited.contains(child))
      {
         return false;
      }
      visited.add(child);
      Iterator<IOrganization> orgs = child.getAllOrganizations();
      while (orgs.hasNext())
      {
         IOrganization org = orgs.next();
         if(parent != null
               && org.getId() != null 
               && parent.getId() != null
               && org.getId().equals(parent.getId()))
         {
            return true;
         }
         if (isChild(org, parent, visited))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean isChild(IDepartment child, IDepartment parent)
   {
      if (child == null || parent == null)
      {
         return false;
      }
      if (parent.equals(child.getParentDepartment()))
      {
         return true;
      }
      return isChild(child.getParentDepartment(), parent);
   }

/*   public static ModelParticipantInfo getParticipant(final DepartmentInfo department, final ModelParticipantInfo participant)
   {
      final long runtimeElementOID = participant.getRuntimeElementOID();
      final String id = participant.getId();
      final String name = participant.getName();
      final boolean isDepartmentScoped = participant.isDepartmentScoped();
      final boolean definesDepartmentScope = participant.definesDepartmentScope();
      
      if (participant instanceof OrganizationInfo)
      {
         return new OrganizationInfoDetails(runtimeElementOID, id, name, isDepartmentScoped, definesDepartmentScope, department);
      }
      if (participant instanceof RoleInfo)
      {
         return new RoleInfoDetails(runtimeElementOID, id, name, isDepartmentScoped, definesDepartmentScope, department);
      }
      
      return new ModelParticipantInfo()
      {
         private static final long serialVersionUID = 1L;
   
         public DepartmentInfo getDepartment()
         {
            return department;
         }
   
         public long getRuntimeElementOID()
         {
            return runtimeElementOID;
         }
   
         public String getId()
         {
            return id;
         }
   
         public String getName()
         {
            return name;
         }
   
         @Override
         public String toString()
         {
            return participant.getId() + '[' + department.getId() + ']';
         }
   
         public boolean isDepartmentScoped()
         {
            return isDepartmentScoped;
         }

         public boolean definesDepartmentScope()
         {
            return definesDepartmentScope;
         }
      };
   }*/

   public static IOrganization getParentOrg(final IModelParticipant participant)
   {
      final Iterator<IOrganization> allOrgs = participant.getAllOrganizations();
      while (allOrgs.hasNext())
      {
         final IOrganization aOrg = allOrgs.next();
         
         final Iterator<?> childOrgs = aOrg.getAllParticipants();
         while (childOrgs.hasNext())
         {
            if (participant.equals(childOrgs.next()))
            {
               /* found parent of org */
               return aOrg;
            }
         }
      }
      
      /* top level org */
      return null;
   }

   public static IOrganization getFirstScopedOrganization(final IModelParticipant participant)
   {
      if (participant == null)
      {
         return null;
      }
      
      IOrganization tmpOrg;
      if (participant instanceof IOrganization)
      {
         tmpOrg = (IOrganization) participant;
      }
      else
      {
         tmpOrg = getParentOrg(participant);
         if (tmpOrg == null)
         {
            return null;
         }
      }
      
      while (tmpOrg != null && !isRestrictedModelParticipant(tmpOrg))
      {
         tmpOrg = getParentOrg(tmpOrg);
      }
      return tmpOrg;
   }
}