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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.*;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.ClosableIterator;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


/**
 * @author Marc Gille
 * @version $Revision$
 */
public class DetailsFactory
{
   private static final Logger trace = LogManager.getLogger(DetailsFactory.class);

   public static ModelParticipant createModelDetails(IModelParticipant participant, IDepartment department)
   {
      ModelParticipant detail = (ModelParticipant) createParticipantDetails(participant);

      if (department != null)
      {
         final Department departmentDetail = create(department, IDepartment.class,
               DepartmentDetails.class);
         if (participant instanceof IOrganization)
         {
            // decorate the unbound model participant with an department
            final Organization orgDetail = (Organization) detail;
            detail = new Organization()
            {
               private static final long serialVersionUID = 1L;

               public Map getAllAttributes()
               {
                  return orgDetail.getAllAttributes();
               }

               public List getAllSubOrganizations()
               {
                  return orgDetail.getAllSubOrganizations();
               }

               public List getAllSubParticipants()
               {
                  return orgDetail.getAllSubParticipants();
               }

               public List getAllSubRoles()
               {
                  return orgDetail.getAllSubRoles();
               }

               public List<Organization> getAllSuperOrganizations()
               {
                  return orgDetail.getAllSuperOrganizations();
               }

               public Object getAttribute(String name)
               {
                  return orgDetail.getAttribute(name);
               }

               public DepartmentInfo getDepartment()
               {
                  return departmentDetail;
               }

               public int getElementOID()
               {
                  return orgDetail.getElementOID();
               }

               public String getNamespace()
               {
                  return orgDetail.getNamespace();
               }

               public String getId()
               {
                  return orgDetail.getId();
               }

               public int getModelOID()
               {
                  return orgDetail.getModelOID();
               }

               public String getDescription()
               {
                  return orgDetail.getDescription();
               }

               public String getName()
               {
                  return orgDetail.getName();
               }

               @SuppressWarnings("deprecation")
               public String getPartitionId()
               {
                  return orgDetail.getPartitionId();
               }

               @SuppressWarnings("deprecation")
               public short getPartitionOID()
               {
                  return orgDetail.getPartitionOID();
               }

               public long getRuntimeElementOID()
               {
                  return orgDetail.getRuntimeElementOID();
               }

               public Role getTeamLead()
               {
                  return orgDetail.getTeamLead();
               }

               public boolean isDepartmentScoped()
               {
                  return orgDetail.isDepartmentScoped();
               }

               public boolean definesDepartmentScope()
               {
                  return orgDetail.definesDepartmentScope();
               }

               @Override
               public boolean equals(Object obj)
               {
                  final boolean orgDetailsEquals = orgDetail.equals(obj);
                  if (orgDetailsEquals && obj instanceof ModelParticipantInfo)
                  {
                     ModelParticipantInfo mpInfo = (ModelParticipantInfo) obj;
                     return CompareHelper.areEqual(getDepartment(), mpInfo
                           .getDepartment());
                  }
                  else
                  {
                     return orgDetailsEquals;
                  }
               }

               @Override
               public int hashCode()
               {
                  // only use the hash code from the unbound participant
                  return orgDetail.hashCode();
               }

               public String getQualifiedId()
               {
                  return orgDetail.getQualifiedId();
               }
            };
         }
         else if (participant instanceof IRole)
         {
            // decorate the unbound model participant with an department
            final Role roleDetails = (Role) detail;
            detail = new Role()
            {
               private static final long serialVersionUID = 1L;

               public Map getAllAttributes()
               {
                  return roleDetails.getAllAttributes();
               }

               public List<Organization> getAllSuperOrganizations()
               {
                  return roleDetails.getAllSuperOrganizations();
               }

               public Object getAttribute(String name)
               {
                  return roleDetails.getAttribute(name);
               }

               public List getClientOrganizations()
               {
                  return roleDetails.getClientOrganizations();
               }

               public DepartmentInfo getDepartment()
               {
                  return departmentDetail;
               }

               public int getElementOID()
               {
                  return roleDetails.getElementOID();
               }

               public String getNamespace()
               {
                  return roleDetails.getNamespace();
               }

               public String getId()
               {
                  return roleDetails.getId();
               }

               public int getModelOID()
               {
                  return roleDetails.getModelOID();
               }

               public String getDescription()
               {
                  return roleDetails.getDescription();
               }

               public String getName()
               {
                  return roleDetails.getName();
               }

               @SuppressWarnings("deprecation")
               public String getPartitionId()
               {
                  return roleDetails.getPartitionId();
               }

               @SuppressWarnings("deprecation")
               public short getPartitionOID()
               {
                  return roleDetails.getPartitionOID();
               }

               public long getRuntimeElementOID()
               {
                  return roleDetails.getRuntimeElementOID();
               }

               public List getTeams()
               {
                  return roleDetails.getTeams();
               }

               public boolean isDepartmentScoped()
               {
                  return roleDetails.isDepartmentScoped();
               }

               public boolean definesDepartmentScope()
               {
                  return roleDetails.definesDepartmentScope();
               }

               @Override
               public boolean equals(Object obj)
               {
                  final boolean roleDetailsEquals = roleDetails.equals(obj);
                  if (roleDetailsEquals && obj instanceof ModelParticipantInfo)
                  {
                     ModelParticipantInfo mpInfo = (ModelParticipantInfo) obj;
                     return CompareHelper.areEqual(getDepartment(), mpInfo
                           .getDepartment());
                  }
                  else
                  {
                     return roleDetailsEquals;
                  }
               }

               @Override
               public int hashCode()
               {
                  // only use the hash code from the unbound participant
                  return roleDetails.hashCode();
               }

               public String getQualifiedId()
               {
                  return roleDetails.getQualifiedId();
               }
            };
         }
         else
         {
            throw new InternalException("Creating details for participants of type "
                  + participant.getClass() + " is currently not supported.");
         }
      }

      return detail;
   }

   public static ModelParticipantInfo createModelInfoDetails(
         IModelParticipant participant, IDepartment department)
   {
      if (department == null)
      {
         return (ModelParticipantInfo) createParticipantInfoDetails(participant);
      }

      ModelParticipantInfo detail = null;
      DepartmentInfo departmentInfoDetail = create(department, IDepartment.class,
            DepartmentInfoDetails.class);
      Pair pair = new Pair(participant, departmentInfoDetail);
      if (participant instanceof IOrganization)
      {
         detail = (ModelParticipantInfo) create(pair, Pair.class,
               OrganizationInfoDetails.class);
      }
      else if (participant instanceof IRole)
      {
         detail = (ModelParticipantInfo) create(pair, Pair.class, RoleInfoDetails.class);
      }
      else
      {
         throw new InternalException("Creating details for participants of type "
               + participant.getClass() + " is currently not supported.");
      }

      return detail;
   }

   private static final String PRP_CTOR_CACHE_DC = DetailsFactory.class.getName() + ".CtorCache.WithDetailsCache";

   private static final String PRP_CTOR_CACHE_NO_DC = DetailsFactory.class.getName() + ".CtorCache.WithoutDetailsCache";

   private static final Object NO_CTOR = new Object();

   public static Participant createParticipantDetails(IParticipant participant)
   {
      Participant details = null;
      if (null != participant)
      {
         if (participant instanceof IOrganization)
         {
            details = create(participant, IOrganization.class,
                  OrganizationDetails.class);
         }
         else if (participant instanceof IRole)
         {
            details = create(participant, IRole.class,
                  RoleDetails.class);
         }
         else if (participant instanceof IConditionalPerformer)
         {
            details = create(participant,
                  IConditionalPerformer.class, ConditionalPerformerDetails.class);
         }
         else if (participant instanceof IUserGroup)
         {
            details = create(participant, IUserGroup.class,
                  UserGroupDetails.class);
         }
         else if (participant instanceof IUser)
         {
            details = createUser((IUser) participant);
         }
         else
         {
            throw new InternalException("Creating details for participants of type "
                  + participant.getClass() + " is currently not supported.");
         }
      }
      return details;
   }

   public static ParticipantInfo createParticipantInfoDetails(IParticipant participant)
   {
      ParticipantInfo details = null;
      if (null != participant)
      {
         if (participant instanceof IModelParticipant)
         {
            // get or create uncached info details object

            final Pair pair = new Pair(participant, null);
            if (participant instanceof IOrganization)
            {
               details = create(pair, Pair.class, OrganizationInfoDetails.class);
            }
            else if (participant instanceof IRole)
            {
               details = create(pair, Pair.class, RoleInfoDetails.class);
            }
            else if (participant instanceof IConditionalPerformer)
            {
               details = create(pair, Pair.class,
                     ConditionalPerformerInfoDetails.class);
            }
         }
         else if (participant instanceof IUserGroup)
         {
            details = create(participant, IUserGroup.class, UserGroupInfoDetails.class);
         }
         else if (participant instanceof IUser)
         {
            details = create(participant, IUser.class, UserInfoDetails.class);
         }
         else
         {
            throw new InternalException("Creating details for participants of type "
                  + participant.getClass() + " is currently not supported.");
         }
      }
      return details;
   }

   public static <I, T extends I> List<I> createCollection(List<?> source, Class<?> baseType, Class<T> detailsType)
   {
      return DetailsFactory.<I, T>createCollection(source.iterator(), baseType, detailsType);
   }

   public static <I, T extends I> List<I> createCollection(ModelElementList source, Class<?> baseType, Class<T> detailsType)
   {
      List<I> list = new ArrayList<I>();
      try
      {
         for (int i = 0, len = source.size(); i < len; i++)
         {
            list.add(create(source.get(i), baseType, detailsType));
         }
      }
      finally
      {
         if (source instanceof ClosableIterator)
         {
            ((ClosableIterator) source).close();
         }
      }

      return list;
   }

   public static <I, T extends I> List<I> createCollection(Iterator<?> source, Class<?> baseType, Class<T> detailsType)
   {
      List<I> list = new ArrayList<I>();
      try
      {
         while (source.hasNext())
         {
            list.add(create(source.next(), baseType, detailsType));
         }
      }
      finally
      {
         if (source instanceof ClosableIterator)
         {
            ((ClosableIterator) source).close();
         }
      }

      return list;
   }

   public static <T> T create(Object internal, Class<?> baseType, Class<? extends T> detailsType)
   {
      T detail = null;
      if (internal != null)
      {
         if (isCacheable(internal))
         {
            DetailsCache<T> cache = ModelManagerFactory.getCurrent().getDependentCache();
            detail = cache.get(internal);
            if (null == detail || !detail.getClass().equals(detailsType))
            {
               detail = createDetailsObject(internal, baseType, detailsType, cache);
               cache.put(internal, detail);
            }
         }
         else
         {
            detail = createDetailsObject(internal, baseType, detailsType, null);
         }
      }
      return detail;
   }

   public static DataPath create(IDataPath internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static DataPath create(CaseDescriptorRef internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static ProcessInstance create(IProcessInstance internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static ProcessInstanceLinkType create(IProcessInstanceLinkType internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static ProcessInstanceLink create(IProcessInstanceLink internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static UserInfo create(IUser internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static User createUser(IUser internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createUserDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static UserGroupInfo create(IUserGroup internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static DepartmentInfo create(IDepartment internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   public static Department createDepartment(IDepartment internal)
   {
      if (internal == null)
      {
         return null;
      }
      try
      {
         BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
         return rte.getDetailsFactory().createDepartmentDetails(internal);
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
   }

   private static boolean isCacheable(Object internal)
   {
      // hint model details are not cached because they contain fragile information
      // (activeness/aliveness)
      return (internal instanceof ModelElement)
            && (null != ((ModelElement) internal).getModel())
            && !(internal instanceof  IModel);
   }

   private static <T> T createDetailsObject(Object internal, Class baseType,
         Class<T> detailsType, DetailsCache detailsCache)
   {
      T detail = null;
      try
      {
         if (null != detailsCache)
         {
            ConcurrentHashMap ctorCache = getCtorCache(PRP_CTOR_CACHE_DC);
            if ( !ctorCache.containsKey(detailsType))
            {
               try
               {
                  ctorCache.putIfAbsent(detailsType,
                        detailsType.getDeclaredConstructor(new Class[] {
                        baseType, DetailsCache.class}));
               }
               catch (NoSuchMethodException nsme)
               {
                  ctorCache.putIfAbsent(detailsType, NO_CTOR);
               }
            }

            Constructor<? extends T> ctor = (ctorCache.get(detailsType) instanceof Constructor)
                  ? (Constructor) ctorCache.get(detailsType)
                  : null;
            if (null != ctor)
            {
               try
                  {
                  ctor.setAccessible(true);
                  detail = ctor.newInstance(new Object[] {internal, detailsCache});
                  }
               catch (InvocationTargetException e)
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Failed creating details object cache-aware.",
                           e.getTargetException());
                  }
               }
               catch (Exception e)
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Failed creating details object cache-aware.", e);
                  }
               }
            }
         }

         if (null == detail)
         {
            ConcurrentHashMap ctorCache = getCtorCache(PRP_CTOR_CACHE_NO_DC);
            if ( !ctorCache.containsKey(detailsType))
            {
               try
               {
                  ctorCache.putIfAbsent(detailsType,
                        detailsType.getDeclaredConstructor(new Class[] {baseType}));
               }
               catch (NoSuchMethodException nsme)
               {
                  ctorCache.putIfAbsent(detailsType, NO_CTOR);
               }
            }

            Constructor<? extends T> ctor = (ctorCache.get(detailsType) instanceof Constructor)
                  ? (Constructor) ctorCache.get(detailsType)
                  : null;
            if (null != ctor)
            {
               ctor.setAccessible(true);
               detail = ctor.newInstance(new Object[] {internal});
            }
            else
            {
               throw new InternalException("Details type " + detailsType.getName() + " does not declare a proper constructor.");
            }
         }
      }
      catch (InvocationTargetException x)
      {
         if (x.getTargetException() instanceof ApplicationException)
         {
            throw (ApplicationException) x.getTargetException();
         }
         throw new InternalException(
               "Failed computing details object: " + x.getTargetException(),
               x.getTargetException());
      }
      catch (InternalException ie)
      {
         throw ie;
      }
      catch (Exception x)
      {
         throw new InternalException(
               "Failed computing details object: " + x.getMessage(), x);
      }
      return detail;
   }

   private static ConcurrentHashMap getCtorCache(String cacheId)
   {
      GlobalParameters globals = GlobalParameters.globals();

      ConcurrentHashMap ctorCache = (ConcurrentHashMap) globals.get(cacheId);
      if (null == ctorCache)
      {
         ctorCache = (ConcurrentHashMap) globals.getOrInitialize(cacheId,
               new ValueProvider()
               {
                  public Object getValue()
                  {
                     return new ConcurrentHashMap();
                  }
               });
      }

      return ctorCache;
   }

   private DetailsFactory()
   {
      // utility class
   }
}