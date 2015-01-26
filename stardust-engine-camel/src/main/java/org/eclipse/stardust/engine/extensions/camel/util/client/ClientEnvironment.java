package org.eclipse.stardust.engine.extensions.camel.util.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.camel.util.security.UserIdentificationKey;
import org.eclipse.stardust.engine.extensions.camel.util.security.UserSecurityProperties;


/**
 * This class provides convenience methods for client code that needs to invoke IPP
 * functionality.
 * 
 * @author JanHendrik.Scheufen
 */
public class ClientEnvironment
{

   private static final ThreadLocal<ServiceFactory> CURRENT = new ThreadLocal<ServiceFactory>();
   private static Logger LOG = LogManager.getLogger(ClientEnvironment.class);
   protected static final ServiceFactoryCache _sfCache;
   protected static ServiceFactoryAccess factoryAccess;
   protected static ClientEnvironment _instance = new ClientEnvironment();
   private static Map<UserIdentificationKey, UserSecurityProperties> predefinedUsers = new HashMap<UserIdentificationKey, UserSecurityProperties>();

   static
   {
      if (Parameters.instance().getBoolean("IPP.ClientEnvironment.UseServiceFactoryCache", true))
      {
         _sfCache = new ServiceFactoryCache(new ServiceFactoryAccess());
      }
      else
      {
         _sfCache = null;
      }
   }

   /**
    * Creates a new ClientEnvironment and registers it as the singleton {@link #_instance}
    * if it has not been set.
    */
   protected ClientEnvironment()
   {
      if (null == _instance)
         _instance = this;
      else
         LOG.info("Static class member ClientEnvironment already exists!");
   }

   /**
    * Returns a ServiceFactory for the specified parameters. This is a shortcut to calling
    * {@link #getServiceFactory(String, String, Map)} when no parameters other than
    * partition, realm, domain need to be passed to the ServiceFactory login.
    * 
    * @param account
    * @param password
    * @param partition
    * @param realm
    * @param domain
    * @return a service factory
    */
   public ServiceFactory getServiceFactory(String account, String password, String partition, String realm,
         String domain)
   {
      return getServiceFactory(account, password,
            ServiceFactoryAccess.createSecurityProperties(partition, realm, domain));
   }

   /**
    * Returns a ServiceFactory for the specified parameters. If the {@link #_sfCache} is
    * enabled, the ServiceFactory will be served from the cache, if a valid one is found.
    * Otherwise a new ServiceFactory will be created which will cause a fresh login.
    * 
    * @param account
    * @param password
    * @param properties
    * @return a service factory
    */
   public ServiceFactory getServiceFactory(String account, String password, Map<String, ? > properties)
   {
      if (null != _sfCache)
         return _sfCache.getServiceFactory(account, password, properties);
      else
         return factoryAccess.getServiceFactory(account, password, properties);
   }

   /**
    * Returns a ServiceFactory for the predefined account. The appropriate
    * {@link UserSecurityProperties} must be set up in the
    * {@link ServiceFactoryAccess#predefinedUsers}.
    * 
    * @param userKey
    *           a {@link UserIdentificationKey} containing the necessary information to
    *           identify the predefined user
    * @return a service factory
    */
   public ServiceFactory getServiceFactory(UserIdentificationKey userKey)
   {
      return getServiceFactory(userKey.getAccount(), userKey.getPartition(), userKey.getRealm(), userKey.getDomain());
   }

   /**
    * Returns a ServiceFactory for the predefined account. This is a shortcut to calling
    * {@link #getServiceFactory(String, Map)} when no parameters other than partition,
    * realm, domain need to be passed to the ServiceFactory login. <br/>
    * The appropriate {@link UserSecurityProperties} must be set up in the
    * {@link ServiceFactoryAccess#predefinedUsers}.
    * 
    * @param account
    * @param partition
    * @param realm
    * @param domain
    * @return a service factory
    */
   public ServiceFactory getServiceFactory(String account, String partition, String realm, String domain)
   {
      return getServiceFactory(account, ServiceFactoryAccess.createSecurityProperties(partition, realm, domain));
   }

   /**
    * Returns a ServiceFactory for the predefined account. <br/>
    * The appropriate {@link UserSecurityProperties} must be set up in the
    * {@link ServiceFactoryAccess#predefinedUsers}.
    * 
    * @param account
    * @param properties
    * @return a service factory
    */
   public ServiceFactory getServiceFactory(String account, Map<String, ? > properties)
   {
      UserSecurityProperties userKey = predefinedUsers.get(UserIdentificationKey.createKey(account,
            (String) properties.get(SecurityProperties.PARTITION), (String) properties.get(SecurityProperties.REALM),
            (String) properties.get(SecurityProperties.DOMAIN)));
      if (null == userKey)
      {
         LOG.warn("No predefined user found for account " + account);
         return null;
      }
      return getServiceFactory(account, userKey.getPassword(), properties);
   }

   /**
    * Either releases the specified {@link ServiceFactory} from the cache (if a
    * ServiceFactoryCache is used) or closes the instance.
    * 
    * @param serviceFactory
    */
   public void returnServiceFactory(ServiceFactory serviceFactory)
   {
      if (null != serviceFactory)
      {
         if (null != _sfCache)
            _sfCache.release(serviceFactory);
         else
         {
            try
            {
               serviceFactory.close();
            }
            catch (Exception e)
            {
               LOG.warn("Unexpected exception while trying to close ServiceFactory.", e);
            }
         }
      }
      else if (LOG.isDebugEnabled())
         LOG.debug("ServiceFactory NULL returned!");

   }

   /**
    * Sets the specified {@link ServiceFactoryAccess} to be used for retrieving
    * {@link ServiceFactory} instances.
    * 
    * @param factoryAccess
    */
   public void setServiceFactoryAccess(ServiceFactoryAccess factoryAccess)
   {
      this.factoryAccess = factoryAccess;
      if (null != _sfCache)
         _sfCache.setNewServiceFactoryProvider(factoryAccess);

   }

   /**
    * Sets the {@link #predefinedUsers} to the specified ones.
    * 
    * @param predefinedUsers
    */
   public void setPredefinedUsers(List<UserSecurityProperties> predefinedUsers)
   {
      this.predefinedUsers = new HashMap<UserIdentificationKey, UserSecurityProperties>(predefinedUsers.size());
      for (UserSecurityProperties props : predefinedUsers)
         this.predefinedUsers.put(props.getUserIdentificationKey(), props);
   }

   /**
    * @return the ClientEnvironment singleton
    */
   public static ClientEnvironment instance()
   {
      return _instance;
   }

   /**
    * @return the ServiceFactory from the {@link #CURRENT} thread context
    */
   public static ServiceFactory getCurrentServiceFactory()
   {
      return CURRENT.get();
   }

   /**
    * Retrieves a ServiceFactory based on the specified parameters and sets it as the
    * {@link #CURRENT} service factory instance. <br/>
    * This is a shortcut to calling {@link #setCurrent(String, String, Map)} when no
    * parameters other than partition, realm, domain need to be passed to the
    * ServiceFactory login.
    * 
    * @see #getCurrentServiceFactory()
    * @param account
    * @param password
    * @param partition
    * @param realm
    * @param domain
    */
   public static void setCurrent(String account, String password, String partition, String realm, String domain)
   {
      setCurrent(account, password, ServiceFactoryAccess.createSecurityProperties(partition, realm, domain));
   }

   /**
    * Sets the specified ServiceFactory in the {@link #CURRENT} context.
    * 
    * @param factory
    */
   public static void setCurrent(ServiceFactory factory)
   {
      CURRENT.set(factory);
   }

   /**
    * Retrieves a ServiceFactory based on the specified parameters and sets it as the
    * {@link #CURRENT} service factory instance.
    * 
    * @see #getCurrentServiceFactory()
    * @param account
    * @param password
    * @param properties
    */
   public static void setCurrent(String account, String password, Map<String, ? > properties)
   {
      setCurrent(instance().getServiceFactory(account, password, properties));
   }

   /**
    * Retrieves a ServiceFactory for the predefined account and sets it as the
    * {@link #CURRENT} service factory instance. This is a shortcut to calling
    * {@link #setCurrent(String, Map)} when no parameters other than partition, realm,
    * domain need to be passed to the ServiceFactory login. <br/>
    * The appropriate {@link UserSecurityProperties} must be set up in the
    * {@link ServiceFactoryAccess#predefinedUsers}.
    * 
    * @see #getCurrentServiceFactory()
    * @param account
    * @param partition
    * @param realm
    * @param domain
    */
   public static void setCurrent(String account, String partition, String realm, String domain)
   {
      setCurrent(account, ServiceFactoryAccess.createSecurityProperties(partition, realm, domain));
   }

   /**
    * Retrieves a ServiceFactory for the predefined account and sets it as the
    * {@link #CURRENT} service factory instance. <br/>
    * The appropriate {@link UserSecurityProperties} must be set up in the
    * {@link ServiceFactoryAccess#predefinedUsers}.
    * 
    * @see #getCurrentServiceFactory()
    * @param account
    * @param properties
    */
   public static void setCurrent(String account, Map<String, ? > properties)
   {
      setCurrent(instance().getServiceFactory(account, properties));
   }

   /**
    * Unregisters and removes the ServiceFactory instance currently stored under
    * {@link #CURRENT}
    */
   public static void removeCurrent()
   {
      ServiceFactory current = CURRENT.get();
      CURRENT.remove();

      if (null != current)
         instance().returnServiceFactory(current);
   }

   /**
    * public Model getActiveModel() { return getModel(PredefinedConstants.ACTIVE_MODEL); }
    * 
    * public Model getModel(int modelOid) { DeployedModel model = null;
    * 
    * if (null != modelCache) { model = modelCache.getModel(modelOid); }
    * 
    * if (null == model) { model = serviceFactory.getQueryService().getModel(modelOid,
    * false);
    * 
    * if ((null != model) && (null != modelCache)) { modelCache.putModel(model); } }
    * 
    * return model; }
    * 
    * public void clearModelCache() { if (null != modelCache) { modelCache.reset(); } }
    */

}
