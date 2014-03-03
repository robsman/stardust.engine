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
/*
 * $Id: $
 * (C) 2000 - 2009 CARNOT AG
 */
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.marshalUserRealmList;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.toWs;

import java.util.List;

import javax.jws.WebService;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.engine.api.runtime.Deputy;
import org.eclipse.stardust.engine.api.runtime.DeputyOptions;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.ws.BpmFault;
import org.eclipse.stardust.engine.api.ws.DeputiesXto;
import org.eclipse.stardust.engine.api.ws.DeputyOptionsXto;
import org.eclipse.stardust.engine.api.ws.DeputyXto;
import org.eclipse.stardust.engine.api.ws.GetUserRealmsResponse.UserRealmsXto;
import org.eclipse.stardust.engine.api.ws.IUserService;
import org.eclipse.stardust.engine.api.ws.MapXto;
import org.eclipse.stardust.engine.api.ws.UserGroupXto;
import org.eclipse.stardust.engine.api.ws.UserInfoXto;
import org.eclipse.stardust.engine.api.ws.UserRealmXto;
import org.eclipse.stardust.engine.api.ws.UserXto;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
@WebService(name = "IUserService", serviceName = "StardustBpmServices", portName = "UserServiceEndpoint", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", endpointInterface = "org.eclipse.stardust.engine.api.ws.IUserService")
public class UserServiceFacade implements IUserService
{

   public boolean isInternalAuthentication() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         return us.isInternalAuthentication();
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return false;
   }

   public boolean isInternalAuthorization() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         return us.isInternalAuthorization();
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return false;
   }

   public UserXto getSessionUser() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         User user = us.getUser();

         return toWs(user);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserXto modifyUser(UserXto user, Boolean generatePassword) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         User inputUser = XmlAdapterUtils.unmarshalUser(user, us); 
         User u;
         if (generatePassword != null)
         {
            u = us.modifyUser(inputUser, generatePassword.booleanValue());
         }
         else
         {
            u = us.modifyUser(inputUser);
         }

         return toWs(u);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }   
   
   public void resetPassword(String account, MapXto properties) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
         
         UserService us = wsEnv.getServiceFactory().getUserService();
         
         us.resetPassword(account, XmlAdapterUtils.unmarshalMap(properties, String.class, String.class), null);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }
   
   public UserXto getUser(long oid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         User user = us.getUser(oid);

         return toWs(user);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserXto createUser(UserXto user) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();
         User ret = null;
         if (user.getUserRealm() == null || isEmpty(user.getUserRealm().getId()))
         {
            ret = us.createUser(user.getAccountId(), user.getFirstName(),
                  user.getLastName(), user.getDescription(), user.getPassword(),
                  user.getEMail(), user.getValidFrom(), user.getValidTo());
         }
         else
         {
            ret = us.createUser(user.getUserRealm().getId(), user.getAccountId(),
                  user.getFirstName(), user.getLastName(), user.getDescription(),
                  user.getPassword(), user.getEMail(), user.getValidFrom(),
                  user.getValidTo());
         }
         return toWs(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserXto invalidateUser(String accountId, String realmId) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         User user = null;

         if (isEmpty(realmId))
         {
            user = us.invalidateUser(accountId);
         }
         else
         {
            user = us.invalidateUser(realmId, accountId);
         }

         return toWs(user);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserGroupXto createUserGroup(UserGroupXto userGroup) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         UserGroup ug = us.createUserGroup(userGroup.getId(), userGroup.getName(),
               userGroup.getDescription(), userGroup.getValidFrom(),
               userGroup.getValidTo());

         return toWs(ug);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserGroupXto getUserGroup(long oid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         UserGroup ug = us.getUserGroup(oid);

         return toWs(ug);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserGroupXto modifyUserGroup(UserGroupXto userGroup) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         UserGroup ug = us.modifyUserGroup(XmlAdapterUtils.unmarshalUserGroup(userGroup,
               us));

         return toWs(ug);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserGroupXto invalidateUserGroup(Long userGroupOid, String userGroupId)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         UserGroup ug = null;
         if (userGroupOid != null)
         {
            ug = us.invalidateUserGroup(userGroupOid);
         }
         else
         {
            ug = us.invalidateUserGroup(userGroupId);
         }

         return toWs(ug);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserRealmXto createUserRealm(String id, String name, String description)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         UserRealm ret = us.createUserRealm(id, name, description);

         return toWs(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void dropUserRealm(String id) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         us.dropUserRealm(id);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public UserRealmsXto getUserRealms() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();

         @SuppressWarnings("unchecked")
         List<UserRealm> urList = us.getUserRealms();

         return marshalUserRealmList(urList);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DeputiesXto getDeputies(UserInfoXto userXto) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService(); 
         
         UserInfo user = (UserInfo) XmlAdapterUtils.unmarshalParticipantInfo(userXto);
         DeputiesXto deputiesXto = XmlAdapterUtils.marshalDeputies(us.getDeputies(user));
         
         return deputiesXto;
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DeputyXto modifyDeputy(UserInfoXto userXto, UserInfoXto deputyUserXto,
         DeputyOptionsXto optionsXto) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();
         
         UserInfo user = (UserInfo) XmlAdapterUtils.unmarshalParticipantInfo(userXto);
         UserInfo deputyUser = (UserInfo) XmlAdapterUtils.unmarshalParticipantInfo(deputyUserXto);
         DeputyOptions options = XmlAdapterUtils.unmarshalDeputyOptions(optionsXto);
         
         Deputy deputy = us.modifyDeputy(user, deputyUser, options);
         
         return XmlAdapterUtils.marshalDeputy(deputy);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DeputyXto addDeputy(UserInfoXto userXto, UserInfoXto deputyUserXto,
         DeputyOptionsXto optionsXto) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();
         
         UserInfo user = (UserInfo) XmlAdapterUtils.unmarshalParticipantInfo(userXto);
         UserInfo deputyUser = (UserInfo) XmlAdapterUtils.unmarshalParticipantInfo(deputyUserXto);
         DeputyOptions options = XmlAdapterUtils.unmarshalDeputyOptions(optionsXto);
         
         Deputy deputy = us.addDeputy(user, deputyUser, options);
         
         return XmlAdapterUtils.marshalDeputy(deputy);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void removeDeputy(UserInfoXto userXto, UserInfoXto deputyUserXto) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService();
         
         UserInfo user = (UserInfo) XmlAdapterUtils.unmarshalParticipantInfo(userXto);
         UserInfo deputyUser = (UserInfo) XmlAdapterUtils.unmarshalParticipantInfo(deputyUserXto);
         
         us.removeDeputy(user, deputyUser);
         
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      
   }

   @Override
   public DeputiesXto getUsersBeingDeputyFor(UserInfoXto deputyUserXto) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         UserService us = wsEnv.getServiceFactory().getUserService(); 
         
         UserInfo deputyUser = (UserInfo) XmlAdapterUtils.unmarshalParticipantInfo(deputyUserXto);
         DeputiesXto deputiesXto = XmlAdapterUtils.marshalDeputies(us.getUsersBeingDeputyFor(deputyUser));
         
         return deputiesXto;
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }



}
