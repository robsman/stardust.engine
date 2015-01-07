/**
 * 
 */
package org.eclipse.stardust.engine.extensions.camel.util.security;

import org.eclipse.stardust.common.StringUtils;

public class UserSecurityProperties
{
   private String account;
   private String password;
   private String partition;
   private String realm;
   private String domain;

   public UserSecurityProperties()
   {

   }

   /**
    * @param account
    * @param password
    * @param partition
    * @param realm
    * @param domain
    * @throws IllegalArgumentException
    *            if the account is not specified
    */
   public UserSecurityProperties(String account, String password, String partition, String realm, String domain)
   {
      if (StringUtils.isEmpty(account))
         throw new IllegalArgumentException("Cannot create user properties with empty account!");
      this.account = account;
      this.password = password;
      this.partition = partition;
      this.realm = realm;
      this.domain = domain;
   }

   public UserIdentificationKey getUserIdentificationKey()
   {
      return new UserIdentificationKey(account, partition, realm, domain);
   }

   public String getAccount()
   {
      return account;
   }

   public void setAccount(String account)
   {
      if (StringUtils.isEmpty(account))
         throw new IllegalArgumentException("Cannot create user properties with empty account!");
      this.account = account;
   }

   public String getPartition()
   {
      return partition;
   }

   public void setPartition(String partition)
   {
      this.partition = partition;
   }

   public String getRealm()
   {
      return realm;
   }

   public void setRealm(String realm)
   {
      this.realm = realm;
   }

   public String getDomain()
   {
      return domain;
   }

   public void setDomain(String domain)
   {
      this.domain = domain;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }
}