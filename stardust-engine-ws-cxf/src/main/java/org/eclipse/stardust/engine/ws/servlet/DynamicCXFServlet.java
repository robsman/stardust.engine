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
package org.eclipse.stardust.engine.ws.servlet;

import static java.util.Collections.emptySet;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.classloader.ClassLoaderUtils.ClassLoaderHolder;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.transport.servlet.AbstractHTTPServlet;
import org.apache.cxf.transport.servlet.ServletContextResourceResolver;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.servicelist.ServiceListGeneratorServlet;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.ws.configurer.WebServiceEnvSessionPropConfigurer;
import org.eclipse.stardust.engine.ws.configurer.WebServiceEnvUsernameHttpBasicAuthConfigurer;
import org.eclipse.stardust.engine.ws.processinterface.AuthMode;
import org.eclipse.stardust.engine.ws.processinterface.EndpointConfiguration;
import org.eclipse.stardust.engine.ws.processinterface.EndpointConfigurationStorage;
import org.eclipse.stardust.engine.ws.processinterface.GenericWebServiceConfigurationProvider;
import org.eclipse.stardust.engine.ws.processinterface.GenericWebServiceEnv;
import org.eclipse.stardust.engine.ws.processinterface.GenericWebServiceProviderHttpBasicAuth;
import org.eclipse.stardust.engine.ws.processinterface.GenericWebServiceProviderHttpBasicAuthSsl;
import org.eclipse.stardust.engine.ws.processinterface.GenericWebServiceProviderWssUsernameToken;
import org.eclipse.stardust.engine.ws.processinterface.WsUtils;



/**
 * <p>
 * The web service servlet which is able to provide static as well as dynamic endpoints
 * (reconfiguration of dynamic endpoints does not affect the static ones).
 * </p>
 * 
 * @author Nicolas.Werlein
 * @author Roland.Stamm
 * @version $Revision: 56704 $
 */
public class DynamicCXFServlet extends AbstractHTTPServlet
{
   private static final long serialVersionUID = 3388475961996137407L;

   private static final Logger trace = LogManager.getLogger(DynamicCXFServlet.class);

   /**
    * the config location of the CXF application context
    */
   private static final String CXF_APP_CTX_CONFIG_LOCATION = "classpath:/META-INF/cxf/cxf.xml";

   /**
    * the ID of the predefined CXF bus bean
    */
   private static final String CXF_BUS_BEAN_ID = "cxf";

   /**
    * ???
    */
   private static final String CXF_HTTP_CONFIGURATION_NS = "http://cxf.apache.org/transports/http/configuration";

   /**
    * the servlet delegate for static requests, i.e. requests targeting static endpoints
    */
   private ServletDelegate staticServletDelegate;

   /**
    * the servlet delegate for dynamic requests, i.e. requests targeting dynamic endpoints
    */
   private ServletDelegate dynamicServletDelegate;
   
   private static String CLIENT_CONTEXT_PARAM = "clientContext";
   
   private static String context = null;
   
   public static String getClientContext()
   {
      return context;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.cxf.transport.servlet.AbstractHTTPServlet#init(javax.servlet
    * .ServletConfig)
    */
   @Override
   public void init(final ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
           
      context = servletConfig.getInitParameter(CLIENT_CONTEXT_PARAM);
      context = context != null ? context.toLowerCase() : null;

      staticServletDelegate = new StaticServletDelegate(servletConfig);
      staticServletDelegate.init();

      dynamicServletDelegate = new DynamicServletDelegate(servletConfig);
      dynamicServletDelegate.init();
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.GenericServlet#destroy()
    */
   @Override
   public void destroy()
   {
      if (dynamicServletDelegate.isEnabled())
      {
         dynamicServletDelegate.destroy();
         dynamicServletDelegate = null;
      }

      staticServletDelegate.destroy();
      staticServletDelegate = null;
   }

   @Override
   protected void invoke(final HttpServletRequest request,
         final HttpServletResponse response) throws ServletException
   {
      if (staticServletDelegate.canHandle(request))
      {
         staticServletDelegate.invoke(request, response);
      }
      else
      {
         dynamicServletDelegate.invoke(request, response);
      }
   }

   @Override
   protected Bus getBus()
   {
      return staticServletDelegate != null ? staticServletDelegate.getBus() : null;
   }

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
         throws IOException, ServletException
   {
      filterChain.doFilter(request, response);
   }

   /**
    * @return the {@link Bus}es held by this {@link DynamicCXFServlet}, or an empty {@link Set} if the servlet hasn't been initialized yet or is already destroyed
    */
   protected Set<Bus> getBuses()
   {
      if (staticServletDelegate == null || dynamicServletDelegate == null)
      {
         return emptySet();
      }

      final Set<Bus> buses = newHashSet();
      buses.add(staticServletDelegate.bus);
      buses.add(dynamicServletDelegate.bus);
      return buses;
   }

   /**
    * <p>
    * Wraps all required objects a servlet has to hold in order to be able to provide CXF
    * web services.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision: 56704 $
    */
   private static abstract class ServletDelegate
   {
      protected final ServletConfig servletConfig;

      private ConfigurableApplicationContext appCtx;

      private Bus bus;

      private ClassLoader classLoader;

      private ResourceManager resourceManager;

      protected DestinationRegistry destinationRegistry;

      protected ServletController servletController;

      public ServletDelegate(final ServletConfig servletConfig)
      {
         if (servletConfig == null)
         {
            throw new NullPointerException("Servlet config must not be null.");
         }

         this.servletConfig = servletConfig;
      }

      public abstract boolean canHandle(HttpServletRequest request);

      public void init()
      {
         appCtx = initApplicationContext(servletConfig.getServletContext());
         bus = initBus(appCtx);

         classLoader = initClassLoader(bus);
         resourceManager = initResourceManager(servletConfig.getServletContext(), bus);
         destinationRegistry = initDestinationRegistry(bus);

         servletController = initServletController(servletConfig, destinationRegistry,
               bus);
      }

      public void destroy()
      {
         destroyServletController(servletController);

         destroyDestinationRegistry(destinationRegistry);
         destroyResourceManager(resourceManager);
         destroyClassLoader(classLoader);

         destroyBus(bus);
         destroyApplicationContext(appCtx);
      }

      public abstract void invoke(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException;

      protected abstract ConfigurableApplicationContext initApplicationContext(
            final ServletContext servletCtx);

      protected Bus initBus(final ConfigurableApplicationContext appCtx)
      {
         return retrieveBus(appCtx);
      }

      protected ClassLoader initClassLoader(final Bus bus)
      {
         final ClassLoader classLoader = bus.getExtension(ClassLoader.class);
         if (classLoader == null)
         {
            throw new NullPointerException("Class loader must not be null.");
         }
         return classLoader;
      }

      protected ResourceManager initResourceManager(final ServletContext servletCtx,
            final Bus bus)
      {
         final ResourceManager resourceManager = bus.getExtension(ResourceManager.class);
         final ServletContextResourceResolver servletCtxResourceResolver = new ServletContextResourceResolver(
               servletCtx);
         resourceManager.addResourceResolver(servletCtxResourceResolver);
         return resourceManager;
      }

      protected DestinationRegistry initDestinationRegistry(final Bus bus)
      {
         final DestinationFactoryManager destinationFactoryManager = bus.getExtension(DestinationFactoryManager.class);
         final DestinationFactory destinationFactory;
         try
         {
            destinationFactory = destinationFactoryManager.getDestinationFactory(CXF_HTTP_CONFIGURATION_NS);
         }
         catch (final BusException e)
         {
            throw new IllegalArgumentException("No destination factory found.", e);
         }
         final HTTPTransportFactory httpTransportFactory = (HTTPTransportFactory) destinationFactory;
         final DestinationRegistry registry = httpTransportFactory.getRegistry();
         if (registry == null)
         {
            throw new NullPointerException("Registry must not be null.");
         }
         return registry;
      }

      protected ServletController initServletController(
            final ServletConfig servletConfig,
            final DestinationRegistry destinationRegistry, final Bus bus)
      {
         final HttpServlet serviceListGeneratorServlet = new ServiceListGeneratorServlet(
               destinationRegistry, bus);
         final ServletController controller = new ServletController(destinationRegistry,
               servletConfig, serviceListGeneratorServlet);
         return controller;
      }

      protected void destroyServletController(final ServletController servletController)
      {
         /* nothing to do */
      }

      protected void destroyDestinationRegistry(
            final DestinationRegistry destinationRegistry)
      {
         for (final String path : destinationRegistry.getDestinationsPaths())
         {
            final AbstractHTTPDestination destination = destinationRegistry.getDestinationForPath(path);
            synchronized (destination)
            {
               destinationRegistry.removeDestination(path);
               destination.releaseRegistry();
            }
         }
      }

      protected void destroyResourceManager(final ResourceManager resourceManager)
      {
         /* nothing to do */
      }

      protected void destroyClassLoader(final ClassLoader classLoader)
      {
         /* nothing to do */
      }

      protected void destroyBus(final Bus bus)
      {
         /* nothing to do */
      }
      
      public Bus getBus()
      {
         return bus;
      }
      
      protected void destroyApplicationContext(final ConfigurableApplicationContext appCtx)
      {
         appCtx.close();
      }

      protected void invokeInternal(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException
      {
         ClassLoaderHolder origLoader = null;
         try
         {
            origLoader = ClassLoaderUtils.setThreadContextClassloader(classLoader);
            BusFactory.setThreadDefaultBus(bus);
            servletController.invoke(request, response);
         }
         finally
         {
            BusFactory.setThreadDefaultBus(null);
            if (origLoader != null)
            {
               origLoader.reset();
            }
         }
      }

      protected void invokeInternalDestination(final HttpServletRequest request,
            final HttpServletResponse response, AbstractHTTPDestination destination)
            throws ServletException
      {
         ClassLoaderHolder origLoader = null;
         try
         {
            origLoader = ClassLoaderUtils.setThreadContextClassloader(classLoader);
            BusFactory.setThreadDefaultBus(bus);
            servletController.invokeDestination(request, response, destination);
         }
         finally
         {
            BusFactory.setThreadDefaultBus(null);
            if (origLoader != null)
            {
               origLoader.reset();
            }
         }
      }

      protected boolean isEnabled()
      {
         return true;
      }

      protected Bus retrieveBus(final ApplicationContext appCtx)
      {
         final Bus bus = appCtx.getBean(CXF_BUS_BEAN_ID, Bus.class);
         if (bus == null)
         {
            throw new IllegalArgumentException(
                  "Application Context MUST contain the CXF bus bean '" + CXF_BUS_BEAN_ID
                        + "'.");
         }
         return bus;
      }
   }

   /**
    * <p>
    * Represents a servlet delegate for static endpoints.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision: 56704 $
    */
   private static class StaticServletDelegate extends ServletDelegate
   {
      public StaticServletDelegate(final ServletConfig servletConfig)
      {
         super(servletConfig);
      }

      @Override
      public boolean canHandle(HttpServletRequest request)
      {
         Set<String> destinationsPaths = super.destinationRegistry.getDestinationsPaths();
         String pathInfo = request.getPathInfo();
         if (pathInfo != null)
         {
            return destinationsPaths.contains(pathInfo)
                  || ((pathInfo.endsWith("/services") || pathInfo.endsWith("/services/")) && request.getParameterMap()
                        .containsKey("static"));
         }
         return false;
      }

      @Override
      public void invoke(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException
      {
         invokeInternal(request, response);
      }

      @Override
      protected ConfigurableApplicationContext initApplicationContext(
            final ServletContext servletCtx)
      {
         final ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) WebApplicationContextUtils.getWebApplicationContext(servletCtx);
         if ( !ctx.isActive() || !ctx.isRunning())
         {
            ctx.refresh();
         }
         return ctx;
      }
   }

   /**
    * <p>
    * Represents a servlet delegate for dynamic endpoints.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision: 56704 $
    */
   private static class DynamicServletDelegate extends ServletDelegate
   {
      private static final String CONFIGURATION_ENDPOINT_ID = "/internal/GenericWebServiceConfigurationProvider";

      private BeanDefinition configurationEndpoint;

      /**
       * indicates whether the dynamic endpoints are enabled
       */
      private boolean enabled;

      /**
       * The time the last synchronization of WS endpoints happened.
       */
      private final ConcurrentHashMap<String, AtomicLong> lastSync = new ConcurrentHashMap<String, AtomicLong>();

      /**
       * Time until next sync is performed.
       */
      private long endpointSyncPeriod;

      /**
       * Provides configured endpoint names for the different authorization methods and
       * different partitions.
       */
      private DynamicEndpointNameProvider nameProvider = new DynamicEndpointNameProvider();

      /**
       * This lock ensures that
       * <ol>
       * <li>all new requests are delayed until the WS Endpoint reconfiguration has been
       * finished and</li>
       * <li>all active requests will be served before the WS Endpoint reconfiguration
       * starts</li>
       * </ol>
       * by leveraging a read-write lock:
       * <ul>
       * <li>Acquiring a read lock for simply doing a WS Request and</li>
       * <li>acquiring a write lock for reconfiguring the WS Endpoints.</li>
       * </ul>
       */
      private final ReentrantReadWriteLock endpointConfigLock = new ReentrantReadWriteLock();

      private Map<String, GenericApplicationContext> currentEndpointContexts = CollectionUtils.newMap();

      private Map<String, EndpointConfiguration> currentEndpoints = CollectionUtils.newMap();

      private Map<String, EndpointConfigurationStorage> endpointConfigurationStoragePerPartition = CollectionUtils.newMap();

      public DynamicServletDelegate(final ServletConfig servletConfig)
      {
         super(servletConfig);

         long syncPeriodInSeconds = WsUtils.getEndpointSyncPeriod();
         this.endpointSyncPeriod = syncPeriodInSeconds * 1000;
      }

      @Override
      public boolean canHandle(HttpServletRequest request)
      {
         return true;
      }

      @Override
      public void init()
      {
         super.init();
         enabled = true;
      }

      @Override
      public void destroy()
      {
         enabled = false;
         super.destroy();
      }

      @Override
      public void invoke(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException
      {
         GenericWebServiceEnv.instance().setEnv(request);

         HttpServletRequest wrappedCloneRequest = null;
         String partitionId = null;
         String modelId = null;
         String pathInfo = request.getPathInfo();
         Map<?, ?> parameterMap = request.getParameterMap();
         
         if (pathInfo == null || "/".equals(pathInfo) || parameterMap.containsKey("stylesheet"))
         {
            // Return blank page for calls that would run into NullPointerException.
            // Request with ?stylesheet is ignored as it is always is sent as a second request from the
            // browser when requesting the /services listing and it should not update the default partition.
            return;
         }
         else if ("GET".equals(request.getMethod()))
         {
            // WSDL or WSDL listing request. Determine partition and modelId if specified.
            
            partitionId = request.getParameter("partition");

            modelId = request.getParameter("modelId");
         }
         else
         {
            // Send a cloned request to the configuration endpoint to extract WsAdressing
            // information.
            wrappedCloneRequest = doConfigurationRequest(request, response);

            // partitionId is in WsAdressing header.
            partitionId = GenericWebServiceEnv.instance().getPartitionId();
            modelId = GenericWebServiceEnv.instance().getModelId();  
         }
         
         // Fallback to partitionId and modelId in URL
         if (partitionId == null && modelId == null)
         {
            Pair<String, String> extracted = extractFromUrl(pathInfo);
            partitionId = extracted.getFirst();
            modelId = extracted.getSecond();
            
            // initialize environment for web service call
            if ( !"GET".equals(request.getMethod()))
            {
               GenericWebServiceEnv.instance().setPartitionId(partitionId);
               GenericWebServiceEnv.instance().setModelId(modelId);
            }
         }

         if (StringUtils.isEmpty(partitionId))
         {
            partitionId = PredefinedConstants.DEFAULT_PARTITION_ID;
         }
         
         // limit to enabled partitions
         List<String> enabledPartitions = WsUtils.getEnabledPartitions();
         if (enabledPartitions != null && !enabledPartitions.isEmpty()
               && !enabledPartitions.contains(partitionId))
         {
            try
            {
               response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access for '"
                     + partitionId + "' is disabled.");
            }
            catch (IOException e)
            {
               new ServletException(e);
            }
         }

         if (StringUtils.isEmpty(modelId))
         {
            modelId = WsUtils.getDefaultModelId(partitionId);
         }

         // servlet path must be empty for CXF
         ensureEndpointsAreUpToDate(partitionId, "");

         if ( !isEnabled())
         {
            return;
         }

         // handle WSDL listing
         if (pathInfo.endsWith("/services") || pathInfo.endsWith("/services/"))
         {
            WsdlListingHandler.handleWsdlListingResponse(request, response, partitionId,
                  destinationRegistry.getDestinationsPaths(), "/services");

            return;
         }
         else
         {
            // forward to CXF
            ReadLock readLock = endpointConfigLock.readLock();
            readLock.lock();
            try
            {
               HttpServletRequest internalRequest = request;
               if (wrappedCloneRequest != null)
               {
                  internalRequest = wrappedCloneRequest;
               }

               AbstractHTTPDestination destination = null;

               if ( !StringUtils.isEmpty(modelId))
               {
                  destination = destinationRegistry.getDestinationForPath(WsUtils.encodeInternalEndpointPath(
                        "", partitionId, modelId, pathInfo.substring(1)));
               }

               if (destination != null)
               {
                  invokeInternalDestination(internalRequest, response, destination);
               }
               else
               {
                  invokeInternal(internalRequest, response);
               }

            }
            finally
            {
               readLock.unlock();
            }
         }
      }

      private Pair<String,String> extractFromUrl(String pathInfo)
      {
         String partitionId = null;
         String modelId = null;
         try
         {
            String tempString = pathInfo.substring(pathInfo.indexOf("/") + 1);
            partitionId = tempString.substring(0, tempString.indexOf("/"));

            tempString = pathInfo.substring(pathInfo.indexOf(partitionId)
                  + partitionId.length() + 1);
            modelId = tempString.substring(0, tempString.indexOf("/"));
         }
         catch (NullPointerException npe)
         {
            partitionId = null;
         }
         catch (IndexOutOfBoundsException ie)
         {
            partitionId = null;
         }
         return new Pair<String,String>(partitionId, modelId);
      }

      private HttpServletRequest doConfigurationRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException
      {
         HttpServletRequest wrappedCloneRequest = new CloneRequestWrapper(request);
         HttpServletResponse wrappedNullResponse = new NullResponseWrapper(response);

         AbstractHTTPDestination destinationForPath = destinationRegistry.getDestinationForPath(CONFIGURATION_ENDPOINT_ID);
         servletController.invokeDestination(wrappedCloneRequest, wrappedNullResponse,
               destinationForPath);

         return wrappedCloneRequest;
      }

      @Override
      protected ConfigurableApplicationContext initApplicationContext(
            final ServletContext servletCtx)
      {
         final ApplicationContext parentCtx = loadCxfContext(servletCtx);

         GenericApplicationContext ctx = new GenericApplicationContext();
         ctx.setParent(parentCtx);

         final Bus bus = retrieveBus(ctx);
         configurationEndpoint = createConfigurationEndpointBean(bus);
         ctx.registerBeanDefinition(CONFIGURATION_ENDPOINT_ID, configurationEndpoint);

         ctx.refresh();
         
         currentEndpointContexts.put(CONFIGURATION_ENDPOINT_ID, ctx);

         return ctx;
      }

      @Override
      protected void destroyApplicationContext(final ConfigurableApplicationContext appCtx)
      {
         super.destroyApplicationContext(appCtx);

         final ConfigurableApplicationContext parentCtx = (ConfigurableApplicationContext) appCtx.getParent();
         parentCtx.close();

         currentEndpointContexts.clear();
      }

      @Override
      protected boolean isEnabled()
      {
         return enabled;
      }

      private void ensureEndpointsAreUpToDate(String partitionId, String servletPath)
      {
         lastSync.putIfAbsent(partitionId, new AtomicLong(0));

         if ((System.currentTimeMillis() - lastSync.get(partitionId).get()) > endpointSyncPeriod)
         {
            WriteLock writeLock = endpointConfigLock.writeLock();
            writeLock.lock();
            try
            {
               trace.info("Synchronizing dynamic endpoints for partition: " + partitionId);

               // loading endpoint names from configuration
               nameProvider.initEndpointNames(partitionId);
               
               if ((System.currentTimeMillis() - lastSync.get(partitionId).get()) > endpointSyncPeriod)
               {
                  Set<Pair<AuthMode, String>> endpointNameSet = nameProvider.getEndpointNameSet(partitionId);

                  EndpointConfigurationStorage endpointConfigurationStorage = getEndpointConfigurationStorage(partitionId);
                  endpointConfigurationStorage.syncProcessInterfaces(partitionId,
                        servletPath, endpointNameSet);

                  if (endpointConfigurationStorage.hasEndpointConfigurationChanged())
                  {
                     updateEndpoints(servletPath, partitionId);
                  }

                  synchronized (DynamicCXFServlet.class)
                  {
                     AtomicLong a = lastSync.get(partitionId);
                     a.set(System.currentTimeMillis());
                     lastSync.put(partitionId, a);
                  }
               }
            }
            finally
            {
               writeLock.unlock();
            }
         }
      }

      private void updateEndpoints(String servletPath, String partitionId)
      {
         EndpointConfigurationStorage endpointConfigurationStorage = getEndpointConfigurationStorage(partitionId);
         final Set<EndpointConfiguration> endpoints2Add = endpointConfigurationStorage.getEndpoints2Add();

         for (final EndpointConfiguration endpoint : endpoints2Add)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Endpoint to add: " + endpoint.id());
            }
            currentEndpoints.put(endpoint.id(), endpoint);
         }

         final Set<String> endpoints2Remove = endpointConfigurationStorage.getEndpoints2Remove();

         for (String endpointId : endpoints2Remove)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Endpoint to remove: " + endpointId);
            }
            currentEndpoints.remove(endpointId);

         }
         updateEndpointSpringContext(partitionId);
      }

      private EndpointConfigurationStorage getEndpointConfigurationStorage(
            String partitionId)
      {
         EndpointConfigurationStorage endpointConfigurationStorage = endpointConfigurationStoragePerPartition.get(partitionId);
         if (endpointConfigurationStorage == null)
         {
            endpointConfigurationStorage = new EndpointConfigurationStorage();
            endpointConfigurationStoragePerPartition.put(partitionId, endpointConfigurationStorage);
         }
         return endpointConfigurationStorage;
      }

      private void updateEndpointSpringContext(String partitionId)
      {
         GenericApplicationContext configurationCtx = currentEndpointContexts.get(CONFIGURATION_ENDPOINT_ID);
         ApplicationContext parentCtx = configurationCtx.getParent();

         GenericApplicationContext ctx = currentEndpointContexts.get(partitionId);
         if (ctx != null)
         {
            ctx.close();
         }
         ctx = new GenericApplicationContext();
         ctx.setParent(parentCtx);

         final Bus bus = retrieveBus(ctx);
         for (EndpointConfiguration endpoint : currentEndpoints.values())
         {
            if (partitionId != null && partitionId.equals(endpoint.getPartitionId()))
            {
               final BeanDefinition dynamicWsProvider = createDynamicWsProviderBean(bus,
                     endpoint);
               ctx.registerBeanDefinition(endpoint.id(), dynamicWsProvider);
            }
         }
         ctx.refresh();

         currentEndpointContexts.put(partitionId, ctx);
      }

      private ApplicationContext loadCxfContext(final ServletContext servletCtx)
      {
         final XmlWebApplicationContext ctx = new XmlWebApplicationContext();
         ctx.setServletContext(servletCtx);
         ctx.setConfigLocation(CXF_APP_CTX_CONFIG_LOCATION);
         ctx.refresh();
         return ctx;
      }

      private BeanDefinition createDynamicWsProviderBean(final Bus bus,
            EndpointConfiguration endpoint)
      {
         final Definition wsdl;
         try
         {
            wsdl = WSDLFactory.newInstance()
                  .newWSDLReader()
                  .readWSDL(null, endpoint.wsdl());
         }
         catch (final Exception e)
         {
            throw new RuntimeException("Unable to read Dynamic WS Provider WSDL file.", e);
         }

         String wsdlBeanId = "wsdl:" + endpoint.id();
         final WSDLManager wsdlManager = getWsdlManager(bus);
         wsdlManager.removeDefinition(wsdl);
         wsdlManager.addDefinition(wsdlBeanId, wsdl);

         EndpointBeanDefinitionBuilder builder = null;

         List<Handler< ? >> handlers = CollectionUtils.newArrayList();
         Class< ? > implClass = endpoint.getImplClass();

         if (GenericWebServiceProviderHttpBasicAuth.class.isAssignableFrom(implClass))
         {
            builder = new EndpointBeanDefinitionBuilder(CXF_BUS_BEAN_ID,
                  GenericWebServiceProviderHttpBasicAuth.class);
            handlers.add(new WebServiceEnvSessionPropConfigurer());
            handlers.add(new WebServiceEnvUsernameHttpBasicAuthConfigurer());
         }
         else if (GenericWebServiceProviderHttpBasicAuthSsl.class.isAssignableFrom(implClass))
         {
            builder = new EndpointBeanDefinitionBuilder(CXF_BUS_BEAN_ID,
                  GenericWebServiceProviderHttpBasicAuthSsl.class);
            handlers.add(new WebServiceEnvSessionPropConfigurer());
            handlers.add(new WebServiceEnvUsernameHttpBasicAuthConfigurer());
         }
         else if (GenericWebServiceProviderWssUsernameToken.class.isAssignableFrom(implClass))
         {
            builder = new EndpointBeanDefinitionBuilder(CXF_BUS_BEAN_ID,
                  GenericWebServiceProviderWssUsernameToken.class);
            handlers.add(new WebServiceEnvSessionPropConfigurer());
         }

         final BeanDefinition dynamicWsProvider = builder.address(
               endpoint.modelIdUrlPair().relativeUrl())
               .wsdlLocation(wsdlBeanId)
               .handlers(handlers)
               .build();

         return dynamicWsProvider;
      }

      private WSDLManager getWsdlManager(Bus bus)
      {
         WSDLManager wsdlManager = bus.getExtension(WSDLManager.class);

         if (wsdlManager instanceof WSDLManagerImpl)
         {
            WSDLManagerImpl wsdlManagerImpl = (WSDLManagerImpl) wsdlManager;
            if ( !wsdlManagerImpl.isDisableSchemaCache())
            {
               wsdlManagerImpl.setDisableSchemaCache(true);
            }
         }

         return wsdlManager;
      }

      private BeanDefinition createConfigurationEndpointBean(final Bus bus)
      {
         final EndpointBeanDefinitionBuilder builder = new EndpointBeanDefinitionBuilder(
               CXF_BUS_BEAN_ID, GenericWebServiceConfigurationProvider.class);

         List<Handler< ? >> handlers = CollectionUtils.newArrayList();
         handlers.add(new WebServiceEnvSessionPropConfigurer(false));

         final BeanDefinition dynamicWsProvider = builder.address(
               CONFIGURATION_ENDPOINT_ID)
               .handlers(handlers)
               .build();

         return dynamicWsProvider;
      }

      private class CloneRequestWrapper extends HttpServletRequestWrapper
      {
         private ByteArrayOutputStream out = null;

         public CloneRequestWrapper(HttpServletRequest request)
         {
            super(request);
         }

         @Override
         public ServletInputStream getInputStream() throws IOException
         {
            ServletInputStream inputStream = super.getInputStream();

            if (out == null)
            {
               out = new ByteArrayOutputStream();

               byte[] bytes = new byte[1024];
               int bytesRead = bytes.length;
               while (bytesRead > 0)
               {
                  bytesRead = inputStream.read(bytes);

                  if (bytesRead > 0)
                  {
                     out.write(bytes, 0, bytesRead);
                  }
               }
            }

            return new CloneServletInputStream(out.toByteArray());
         }
      }

      private class CloneServletInputStream extends ServletInputStream
      {

         private final ByteArrayInputStream in;

         public CloneServletInputStream(byte[] b)
         {
            this.in = new ByteArrayInputStream(b);

         }

         @Override
         public int read() throws IOException
         {
            return in.read();
         }

         @Override
         public int read(byte[] b) throws IOException
         {
            return in.read(b);
         }

         @Override
         public int read(byte[] b, int off, int len) throws IOException
         {
            return in.read(b, off, len);
         }

         @Override
         public int readLine(byte[] b, int off, int len) throws IOException
         {
            return in.read();
         }

         @Override
         public void close() throws IOException
         {
            in.close();
         }

      }

      private class NullResponseWrapper extends HttpServletResponseWrapper
      {
         public NullResponseWrapper(HttpServletResponse response)
         {
            super(response);
         }

         @Override
         public ServletOutputStream getOutputStream() throws IOException
         {
            return new NullServletOutputStream();
         }
      }

      private class NullServletOutputStream extends ServletOutputStream
      {
         @Override
         public void write(int arg0) throws IOException
         {
            // null
         }

         @Override
         public void write(byte[] b) throws IOException
         {
            // null
         }

         @Override
         public void write(byte[] b, int off, int len) throws IOException
         {
            // null
         }
      }
   }
}
