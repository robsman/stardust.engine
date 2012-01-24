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
package org.eclipse.stardust.common.security.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.security.DesEncrypter;
import org.eclipse.stardust.common.security.HMAC;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.PasswordRules;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ClobDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.MailHelper;
import org.eclipse.stardust.engine.core.runtime.beans.PropertyPersistor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;



public class SecurityUtils
{
   public static String LAST_PASSWORDS = "Infinity.Security.LastPasswords";
   public static String PASSWORD_RULES = "Infinity.Security.PasswordRules";
   public static String PASSWORD_ENCRYPTION = "Security.Password.Encryption";
   public static String LOGIN_DIALOG_URL = "Security.Password.LoginDialogUrl";
   
   private static String splitExpression = ";";

   private static final Method[] EXPIRED_USER_METHOD_WHITE_LIST;
      
   static
   {
      try
      {
         EXPIRED_USER_METHOD_WHITE_LIST = new Method[]{               
               AdministrationService.class.getMethod("getUser", new Class[] {}),
               AdministrationService.class.getMethod("getPasswordRules", new Class[] {}),
               UserService.class.getMethod("startSession", new Class[] {String.class}),
               UserService.class.getMethod("closeSession", new Class[] {String.class}),
               UserService.class.getMethod("getUser", new Class[] {}),
               UserService.class.getMethod("modifyLoginUser", new Class[] {
                     String.class, String.class, String.class, String.class, String.class})};
      }
      catch(NoSuchMethodException x)
      {
         throw new InternalException(x.getMessage(), x);
      }
   }
   
   private static String getLoginUrl()
   {
      return Parameters.instance().getString(LOGIN_DIALOG_URL, "").trim();
   }   
   
   public static List getPreviousPasswords(IUser user, String password)
   {
      String propertyValue = (String) user.getPropertyValue(LAST_PASSWORDS);
      if(StringUtils.isEmpty(propertyValue))
      {
         return null;
      }
      
      List<String> previous = new ArrayList<String>();
      try
      {
         HMAC hmac = new HMAC(HMAC.MD5);
         if(!hmac.isHashed(password))
         {         
            password = hmac.hashToString(user.getOID(), password);
         }
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new InternalException("Encryption failed.", e);
      }
      catch (NoSuchProviderException e)
      {
         throw new InternalException("Encryption failed.", e);
      }
      catch (UnsupportedEncodingException e)
      {
         throw new InternalException("Encryption failed.", e);
      }            
      
      DesEncrypter decrypter = new DesEncrypter(password);
      
      String[] matches = propertyValue.split(splitExpression);
      if(matches.length > 1)
      {
         for (int i = 0; i < matches.length; i++)
         {
            String part = matches[i];
            previous.add(decrypter.decrypt(part));
         }         
      }
      else
      {
         previous.add(decrypter.decrypt(propertyValue));         
      }
      
      return previous;
   }   

   public static void updatePasswordHistory(IUser user, String password)
   {
      String plainPassword = password;
      String propertyValue = (String) user.getPropertyValue(LAST_PASSWORDS);
      if(propertyValue == null)
      {
         try
         {
            HMAC hmac = new HMAC(HMAC.MD5);
            password = hmac.hashToString(user.getOID(), password);
         }
         catch (NoSuchAlgorithmException e)
         {
            throw new InternalException("Encryption failed.", e);
         }
         catch (NoSuchProviderException e)
         {
            throw new InternalException("Encryption failed.", e);
         }
         catch (UnsupportedEncodingException e)
         {
            throw new InternalException("Encryption failed.", e);
         }            
         
         DesEncrypter encrypter = new DesEncrypter(password);               
         user.setPropertyValue(LAST_PASSWORDS, encrypter.encrypt(plainPassword), true);
      }
   }
   
   public static void changePassword(IUser user, String oldPassword, String newPassword)
   {
      String newPlainPassword = newPassword;
      PasswordRules rules = getPasswordRules(SecurityProperties.getPartitionOid());
      if(rules == null || rules.getPasswordTracking() == 0)
      {
         user.setPropertyValue(LAST_PASSWORDS, "", true);
         return;
      }      
      
      try
      {
         HMAC hmac = new HMAC(HMAC.MD5);         
         if(!hmac.isHashed(oldPassword))
         {         
            oldPassword = hmac.hashToString(user.getOID(), oldPassword);
         }
         newPassword = hmac.hashToString(user.getOID(), newPassword);
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new InternalException("Encryption failed.", e);
      }
      catch (NoSuchProviderException e)
      {
         throw new InternalException("Encryption failed.", e);
      }
      catch (UnsupportedEncodingException e)
      {
         throw new InternalException("Encryption failed.", e);
      }
            
      DesEncrypter encrypter = new DesEncrypter(newPassword);      
      String propertyValue = (String) user.getPropertyValue(LAST_PASSWORDS);      
      String newValue = "";
      
      if(StringUtils.isEmpty(propertyValue))
      {
         newValue = encrypter.encrypt(newPlainPassword);
      }
      else
      {
         List<String> previous = getPreviousPasswords(user, oldPassword);
         previous.add(newPlainPassword);
         while(previous.size() > rules.getPasswordTracking())
         {
            previous.remove(0);
         }         
         
         for(int i = 0; i < previous.size(); i++)
         {
            newValue = newValue.concat(encrypter.encrypt(previous.get(i)));
            if(i < previous.size() - 1)
            {
               newValue = newValue.concat(splitExpression);
            }
         }
      }
      user.setPropertyValue(LAST_PASSWORDS, newValue, true);
   }
         
   public static PasswordRules getPasswordRules(short partitionOid)
   {      
      PropertyPersistor property = PropertyPersistor.findByName(PASSWORD_RULES, partitionOid);
      if(property != null)
      {
         long oid = property.getOID();
         ClobDataBean data = ClobDataBean.find(oid, PropertyPersistor.class);
         if(data != null)
         {
            String stringValue = data.getStringValue();
            try
            {
               return (PasswordRules) Serialization.deserializeObject(Base64.decode(stringValue.getBytes()));
            }
            catch (IOException e)
            {
               throw new InternalException("Argument not deserializable.", e);
            }
            catch (ClassNotFoundException e)
            {
               throw new InternalException("Argument not deserializable.", e);
            }
         }         
      }
      return null;
   }
   
   public static void setPasswordRules(PasswordRules rules)
   {
      String stringValue = null;
      
      try
      {
         stringValue = new String(Base64.encode(Serialization.serializeObject(rules)));
      }
      catch (IOException e)
      {
         throw new InternalException("Argument not serializable.", e);
      }
      
      PropertyPersistor property = PropertyPersistor.findByName(PASSWORD_RULES, SecurityProperties.getPartitionOid());
      if(property != null)
      {
         long oid = property.getOID();
         ClobDataBean data = ClobDataBean.find(oid, PropertyPersistor.class);
         if(data != null)
         {
            data.setStringValue(stringValue);
         }         
      }
      else
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);         
         property = new PropertyPersistor(PASSWORD_RULES, "", SecurityProperties.getPartition());
         long oid = property.getOID();
         ClobDataBean clobDataBean = new ClobDataBean(oid, PropertyPersistor.class, stringValue);         
         session.cluster(clobDataBean);         
      }
   }  
   
   public static void checkPasswordExpired(IUser user, MethodInvocation invocation) throws AccessForbiddenException
   {
      if(user.isPasswordExpired())
      {
         if(!acceptMethod(invocation.getMethod()))
         {
            throw new AccessForbiddenException(
                  BpmRuntimeError.AUTHx_USER_PASSWORD_EXPIRED.raise(user.getOID()));
         }         
      }      
   }
   
   private static boolean acceptMethod(Method method)
   {
      for (int i = 0; i < EXPIRED_USER_METHOD_WHITE_LIST.length; ++i)
      {
         if (EXPIRED_USER_METHOD_WHITE_LIST[i].equals(method))
         {
            return true;
         }
      }      
      return false;
   }
   
   public static void sendGeneratedPassword(IUser user, String generatedPassword)
   {
      if(!StringUtils.isEmpty(user.getEMail()))
      {      
         try
         {
            String loginUrl = getLoginUrl();
            String message = "Dear user '" + user.getAccount() + "'!\n\n" +
               "Your password has been changed to \"" + generatedPassword + "\". Please change your password or contact an Administrator.\n" +
               "Login with your new password, the Dialog will force you next to change your password.\n\n";
            if(!StringUtils.isEmpty(loginUrl))
            {
               message += loginUrl;
            }            
            
            MailHelper.sendSimpleMessage(new String[] {user.getEMail()}, "Password has been changed!", message);
         }
         catch (PublicException e)
         {
            throw e;
         }
      }
   }
   
   public static boolean isPasswordExpired(IUser user)
   {
      if(user.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE))
      {
         return false;
      }
      
      if(user.isPasswordExpired())
      {
         return true;
      }
      
      PasswordRules rules = getPasswordRules(SecurityProperties.getPartitionOid());
      if(rules == null)
      {
         return false;
      }
      
      if(!rules.isForcePasswordChange())
      {
         return false;
      }
      
      long lastModified = getLastModificationTime(user);
      if(lastModified == -1)
      {
         return false;
      }

      Date now = new Date();      
      
      Calendar expires = Calendar.getInstance();
      expires.setTimeInMillis(lastModified);
      expires.add(Calendar.DATE, rules.getExpirationTime());
      
      if(expires.getTime().getTime() <= now.getTime())
      {
         return true;
      }
      
      return false;
   }
   
   public static boolean isUserDisabled(IUser user)
   {
      PasswordRules rules = getPasswordRules(SecurityProperties.getPartitionOid());
      if(rules == null)
      {
         return false;
      }

      if(!rules.isForcePasswordChange())
      {
         return false;
      }
      
      if(rules.getDisableUserTime() == -1)
      {
         return false;
      }
            
      long lastModified = getLastModificationTime(user);
      if(lastModified == -1)
      {
         return false;
      }      

      Date now = new Date();      
      
      Calendar disabled = Calendar.getInstance();
      disabled.setTimeInMillis(lastModified);
      disabled.add(Calendar.DATE, rules.getExpirationTime());
      disabled.add(Calendar.DATE, rules.getDisableUserTime());
      
      if(disabled.getTime().getTime() <= now.getTime())
      {
         return true;
      }
            
      return false;
   }
   
   private static long getLastModificationTime(IUser user)
   {
      Attribute property = (Attribute) user.getAllProperties().get(LAST_PASSWORDS);
      if(property != null)
      {
         Date date = property.getLastModificationTime();
         return date.getTime();
      }      
      return -1;
   }  
   
   public static boolean isUserInvalid(IUser user)
   {
      if(user.getValidTo() == null || user.getValidTo().after(new Date()))
      {
         return false;
      }      
      return true;
   }  
   
   public static void generatePassword(IUser user)
   {
      PasswordRules rules = getPasswordRules(SecurityProperties.getPartitionOid()); 
      String previousPassword = user.getPassword();
      List<String> history = getPreviousPasswords(user, previousPassword);               
      String newPassword = new String(PasswordGenerator.generatePassword(rules, history));
      user.setPassword(newPassword);
      sendGeneratedPassword(user, newPassword);
      
      user.setPasswordExpired(true);               
      changePassword(user, previousPassword, newPassword);            
   }
}