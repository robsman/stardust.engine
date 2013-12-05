package org.eclipse.stardust.engine.extensions.camel.util.client;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * Convenience class to deal with the usage of the different IPP ServiceFactory types.
 * 
 * Supported modes are "client", "web", or "ejb" with client mode being the default. Each
 * of the different modes produces a different ServiceFactory kind which needs different
 * parameters in order to work. The parameters should be provided via the
 * carnot.properties file. For example: The web mode requires the property
 * <code>Web.ServiceFactory</code> to be set in the client-side carnot.properties.
 * 
 * @author JanHendrik.Scheufen
 */
public class ServiceFactoryAccess implements INewServiceFactoryProvider
{
   private static Logger LOG = LogManager.getLogger(ServiceFactoryAccess.class);

   public static final String CONNECT_MODE_CLIENT = "client";
   public static final String CONNECT_MODE_WEB = "web";
   public static final String CONNECT_MODE_EJB = "ejb";

   private String connectMode = CONNECT_MODE_CLIENT;
   private String defaultUser = "";
   private String defaultPassword = "";
   private Map<String, String> defaultProperties = new HashMap<String, String>();

   public ServiceFactory getWebServiceFactory()
   {
      return getWebServiceFactory(this.defaultUser, this.defaultPassword, this.defaultProperties);
   }

   /**
 * @param user
 * @param password
 * @param properties
 * @return Web Service Factory
 */
private ServiceFactory getWebServiceFactory(String user, String password, Map<String, ? > properties)
   {
      try
      {
         ServiceFactory sf = org.eclipse.stardust.engine.api.web.ServiceFactoryLocator.get(user, password, properties);
         return sf;
      }
      catch (RuntimeException e)
      {
         LOG.error("Unable to retrieve Web ServiceFactory.", e);
         throw e;
      }
   }

 /**
 * @return default EJB Service Factory
 */
public ServiceFactory getEjbServiceFactory()
   {
      return getEjbServiceFactory(this.defaultUser, this.defaultPassword, this.defaultProperties);
   }

 /**
 * @param user
 * @param password
 * @param properties
 * @return EJB Service Factory
 */
private ServiceFactory getEjbServiceFactory(String user, String password, Map<String, ? > properties)
   {
      try
      {
         ServiceFactory sf = org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator.get(user, password, properties);
         return sf;
      }
      catch (RuntimeException e)
      {
         LOG.error("Unable to retrieve EJB ServiceFactory.", e);
         throw e;
      }
   }

/**
  * @return default Client Service Factory
  */
public ServiceFactory getClientServiceFactory()
   {
      return getClientServiceFactory(this.defaultUser, this.defaultPassword, this.defaultProperties);
   }

   /**
 * @param user
 * @param password
 * @param properties
 * @return Client Service Factory
 */
private ServiceFactory getClientServiceFactory(String user, String password, Map<String, ? > properties)
   {
      try
      {
         ServiceFactory sf = org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator.get(user, password,
               properties);
         return sf;
      }
      catch (RuntimeException e)
      {
         LOG.error("Unable to retrieve Client ServiceFactory.", e);
         throw e;
      }
   }

   /**
    * Sets the mode how to connect to the IPP server.
    * 
    * @param connectMode
    * @throws IllegalArgumentException
    *            if an unsupported value is passed in
    */
   public void setConnectMode(String connectMode)
   {
      if (null == connectMode
            | (null != connectMode && !"web".equalsIgnoreCase(connectMode) && !"ejb".equalsIgnoreCase(connectMode) && !"client"
                  .equalsIgnoreCase(connectMode)))
      {
         throw new IllegalArgumentException("Only values " + CONNECT_MODE_WEB + ", " + CONNECT_MODE_EJB + ", or "
               + CONNECT_MODE_CLIENT + " are supported for the property 'connectMode'. " + "Provided property was: "
               + connectMode);
      }
      this.connectMode = connectMode;
   }

   /**
    * Returns a {@link ServiceFactory} with the current {@link #connectMode} using the
    * default values for user, password, and properties.
    * 
    * @return the ServiceFactory or <code>null</code> if the connectMode has been
    *         compromised (should not happen).
    */
   public ServiceFactory getDefaultServiceFactory()
   {
      return getServiceFactory(defaultUser, defaultPassword, defaultProperties);
   }

   /**
    * 
    * @param user
    * @param password
    * @param partition
    * @param realm
    * @param domain
    * @return Service Factory
    */
   public ServiceFactory getServiceFactory(String user, String password, String partition, String realm, String domain)
   {
      if (StringUtils.isEmpty(user))
         user = defaultUser;
      if (StringUtils.isEmpty(password))
         password = defaultPassword;
      Map<String, String> props = createSecurityProperties(partition, realm, domain);
      return getServiceFactory(user, password, props);
   }

   /**
    * All other methods in this class rely on this method, because it takes the
    * {@link #connectMode} into account.
    * 
    * @param user
    * @param password
    * @param properties
    * @return Service Factory
    */
   public ServiceFactory getServiceFactory(String user, String password, Map<String, ? > properties)
   {
      ServiceFactory sf = null;
      if (null == sf)
      {

         if (CONNECT_MODE_WEB.equalsIgnoreCase(this.connectMode))
            sf = getWebServiceFactory(user, password, properties);
         else if (CONNECT_MODE_EJB.equalsIgnoreCase(this.connectMode))
            sf = getEjbServiceFactory(user, password, properties);
         else if (CONNECT_MODE_CLIENT.equalsIgnoreCase(this.connectMode))
            sf = getClientServiceFactory(user, password, properties);
         else
         {
            LOG.warn("Unknown connectMode: " + this.connectMode + ". No ServiceFactory created.");
         }
      }
      return sf;
   }

   /**
    * Creates a Map with security properties for the specified parameters. The keys used
    * are
    * <ul>
    * <li>{@link SecurityProperties#PARTITION}</li>
    * <li>{@link SecurityProperties#REALM}</li>
    * <li>{@link SecurityProperties#DOMAIN}</li>
    * </ul>
    * 
    * @param partition
    * @param realm
    * @param domain
    * @return
    */
   public static Map<String, String> createSecurityProperties(String partition, String realm, String domain)
   {
      Map<String, String> props = new HashMap<String, String>();
      if (StringUtils.isNotEmpty(partition))
         props.put(SecurityProperties.PARTITION, partition);
      if (StringUtils.isNotEmpty(realm))
         props.put(SecurityProperties.REALM, realm);
      if (StringUtils.isNotEmpty(domain))
         props.put(SecurityProperties.DOMAIN, domain);
      return props;
   }

   /**
    * Sets the default authentication user.
    * 
    * @param user
    */
   public void setDefaultUser(String user)
   {
      this.defaultUser = user;
   }

   /**
    * Sets the default authentication password
    * 
    * @param password
    */
   public void setDefaultPassword(String password)
   {
      this.defaultPassword = password;
   }

   /**
    * Sets default multi-tenant properties for login. (Optional)<br/>
    * Possible keys in the Map:
    * <ul>
    * <li>{@link SecurityProperties#PARTITION}</li>
    * <li>{@link SecurityProperties#REALM}</li>
    * <li>{@link SecurityProperties#DOMAIN}</li>
    * </ul>
    * 
    * @param properties
    */
   public void setDefaultProperties(Map<String, String> properties)
   {
      this.defaultProperties = properties;
   }

}
