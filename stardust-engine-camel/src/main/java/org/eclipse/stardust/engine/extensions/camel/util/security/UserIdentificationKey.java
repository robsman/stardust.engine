package org.eclipse.stardust.engine.extensions.camel.util.security;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class UserIdentificationKey
{

   private String account;
   private String partition;
   private String realm;
   private String domain;

   protected UserIdentificationKey(String account, String partition, String realm, String domain)
   {
      this.account = account;
      this.partition = partition;
      this.realm = realm;
      this.domain = domain;
   }

   public String getAccount()
   {
      return account;
   }

   public String getPartition()
   {
      return partition;
   }

   public String getRealm()
   {
      return realm;
   }

   public String getDomain()
   {
      return domain;
   }

   /**
    * For two {@link UserIdentificationKey} objects to be considered equal, they need to
    * match all fields.
    */
   public boolean equals(Object other)
   {
      if (this == other)
         return true;
      if (!(other instanceof UserIdentificationKey))
         return false;
      UserIdentificationKey otherA = (UserIdentificationKey) other;
      return (account.equals(otherA.account))
            && ((partition == null) ? otherA.partition == null : partition.equals(otherA.partition))
            && ((realm == null) ? otherA.realm == null : realm.equals(otherA.realm))
            && ((domain == null) ? otherA.domain == null : domain.equals(otherA.domain));
   }

   public int hashCode()
   {
      int hash = 1;
      hash = hash * 31 + account.hashCode();
      hash = hash * 31 + (partition == null ? 0 : partition.hashCode());
      hash = hash * 31 + (realm == null ? 0 : realm.hashCode());
      hash = hash * 31 + (domain == null ? 0 : domain.hashCode());
      return hash;
   }

   /**
    * Creates a UserIdentificationKey which is initialized with all three IPP security
    * properties (partition, realm, domain). Empty parameters will be initialized with
    * IPP's default settings.
    * 
    * @param account
    * @param partition
    * @param realm
    * @param domain
    * @return a new UserIdentificationKey
    */
   public static UserIdentificationKey createMergedDefaultKey(String account, String partition, String realm,
         String domain)
   {
      Map properties = mergeDefaultProperties(partition, realm, domain);
      return new UserIdentificationKey(account, (String) properties.get(SecurityProperties.PARTITION),
            (String) properties.get(SecurityProperties.REALM), (String) properties.get(SecurityProperties.DOMAIN));
   }

   /**
    * Creates a UserIdentificationKey initialized with the specified parameters.
    * 
    * @param account
    * @param partition
    * @param realm
    * @param domain
    * @return a new UserIdentificationKey
    */
   public static UserIdentificationKey createKey(String account, String partition, String realm, String domain)
   {
      return new UserIdentificationKey(account, partition, realm, domain);
   }

   /**
    * Returns a Map that contains all security properties with initialized values. For
    * empty parameters the default value will be set.
    * 
    * @param partition
    * @param realm
    * @param domain
    * @return a Map with initialized security properties
    */
   private static Map mergeDefaultProperties(String partition, String realm, String domain)
   {
      Map<String, Object> userProperties = new HashMap<String, Object>(3);
      userProperties.put(SecurityProperties.PARTITION, partition);
      userProperties.put(SecurityProperties.REALM, realm);
      userProperties.put(SecurityProperties.DOMAIN, domain);
      return LoginUtils.mergeDefaultCredentials(userProperties);
   }
}
