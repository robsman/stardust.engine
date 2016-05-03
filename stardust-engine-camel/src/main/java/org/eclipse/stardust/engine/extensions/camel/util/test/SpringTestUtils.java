package org.eclipse.stardust.engine.extensions.camel.util.test;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.DefaultPropertiesProvider;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DDLManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

public class SpringTestUtils implements InitializingBean, ApplicationContextAware
{
   private static final DeploymentOptions DEFAULT_DEPLOYMENT_OPTIONS;
   static {
      DEFAULT_DEPLOYMENT_OPTIONS = new DeploymentOptions();
      DEFAULT_DEPLOYMENT_OPTIONS.setIgnoreWarnings(true);
   }
   /**
    * Instance of this class for static access.
    */
   private static SpringTestUtils _INSTANCE;

   private DataSource auditTrailDataSource;

   private ServiceFactoryAccess serviceFactoryAccess;

   /**
    * Filename of model file which is searched for in a directory "models" on the
    * classpath
    */
   private String[] modelFilenames = new String[] {"TestModel.xpdl"};

   // the model as a Resource
   private Resource[] modelFiles = null;

   /**
    * Specifies whether the test environment is dependent on a deployed model
    */
   private boolean deployModelIfNoneExists = true;

   private String derbyHomePath;

   // Default values are read from carnot.properties
   private String auditTrailUser;

   private String auditTrailPassword;

   private String auditTrailUrl;

   private String auditTrailType;

   private String auditTrailDriverClass;
   
   private String testModelsFolderPath = "models/";

   public void setTestModelsFolderPath(String path)
   {
      this.testModelsFolderPath = path.endsWith("/") ? path : path.concat("/");
   }

   private static final transient Logger log = LogManager.getLogger(SpringTestUtils.class);

   /**
    * The model OID of the model deployed during this test.
    */
   private long testProcessModelOID = -1;

   private ApplicationContext applicationContext;

   /**
    * Creates a new IppSpringTestUtils instance and sets it as the {@link #_INSTANCE} for
    * static access.
    */
   public SpringTestUtils()
   {
      _INSTANCE = this;
      Map<String, Object> bundle = new DefaultPropertiesProvider().getProperties();
      this.auditTrailDriverClass = (String) bundle.get("AuditTrail.DriverClass");
      this.auditTrailType = (String) bundle.get("AuditTrail.Type");
      this.auditTrailUrl = (String) bundle.get("AuditTrail.URL");
      this.auditTrailUser = (String) bundle.get("AuditTrail.User");
      this.auditTrailPassword = (String) bundle.get("AuditTrail.Password");
   }

   /**
    * boot and prepare embedded Derby DB and make sure a model is deployed
    * 
    * @throws Exception
    */
   public void setUpGlobal() throws Exception
   {
      // boot and prepare embedded Derby DB
      prepareAuditTrailDatabase();
      // make sure a model is deployed
      prepareModel();
   }

   /**
    * make sure a model is deployed
    * 
    * @throws Exception
    */
   protected void prepareModel() throws Exception
   {
      if (deployModelIfNoneExists && (
            (null != modelFiles && modelFiles.length > 0) |
            (null != modelFilenames && modelFilenames.length > 0) ) )
      {
         ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
         try
         {
            // use QueryService to avoid ObjectNotFoundException
            QueryService qService = sf.getQueryService();
            Models models = qService.getModels(DeployedModelQuery.findAll());
            if (models.size() == 0)
               deployModel();
         }
         finally
         {
            if (null != sf)
               sf.close();
         }
      }
   }

   /**
    * deploy the model
    * 
    * @throws IOException
    * @throws Exception
    */
   public List<DeploymentInfo> deployModel() throws IOException, Exception
   {
      final List<DeploymentInfo> deployments = new ArrayList<DeploymentInfo>();
      try
      {
         if (null != modelFiles)
         {
            for( Resource r : modelFiles )
            {
               deployments.add( deployModel(r.getInputStream()) );
            }
         }
         else if(null != modelFilenames)
         {
            for( String filename : modelFilenames )
            {
               deployments.add( deployModel(ClassLoader.getSystemResourceAsStream(
                     testModelsFolderPath + filename)) );
            }
         }
      }
      catch (Exception e)
      {
         log.error("Unable to read model file for deployment. Make sure the correct model file is declared for this test!",e);
         throw e;
      }

      return deployments;
   }
      
   protected DeploymentInfo deployModel(InputStream modelIn) throws IOException, Exception
   {
      return deployModel2( IOUtils.toString(modelIn) );
   }

   protected DeploymentInfo deployModel2(String modelXml) throws Exception
   {
      // if not able to find model for deployment, simply inform via log
      if (null == modelXml)
      {
         log.warn("Aborting attempt to deploy process model: No model information found."
               + " You can either specify a Spring @Resource via the modelFile property or place"
               + " a model into the 'models' folder on the classpath and specify its name via"
               + " the modelFilename property.");
         return null;
      }

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      try
      {
         AdministrationService admin = sf.getAdministrationService();
         List<DeploymentElement> deploymentelements=new ArrayList<DeploymentElement>();
         final DeploymentElement element = new DeploymentElement(getModelByteStream(modelXml));
         deploymentelements.add(element);
         List<DeploymentInfo> deploymentInfos=  admin.deployModel(deploymentelements, DEFAULT_DEPLOYMENT_OPTIONS);
         DeploymentInfo deploymentInfo = deploymentInfos.get(0);
         if (deploymentInfo.getErrors().size() > 0)
            throw new Exception("Errors during deployment of model.");
         // Save the deployed model OID for this test run
         testProcessModelOID = deploymentInfo.getModelOID();
         log.info("Deployed model ID: " + deploymentInfo.getId() + " with OID: " + deploymentInfo.getModelOID());
         return  deploymentInfo;
      }
      finally
      {
         if (null != sf)
            sf.close();
      }
   }

   public void bootstrapSpringContext()
   {
      log.info("Bootstrapping IPP Spring application context from: carnot-spring-context.xml");
      this.applicationContext = new ClassPathXmlApplicationContext("classpath:carnot-spring-context.xml");
   }
   
   private byte[] getModelByteStream(final String modelXml) {
      try {
         final String encoding = Parameters.instance().getObject(PredefinedConstants.XML_ENCODING, XpdlUtils.ISO8859_1_ENCODING);
         return modelXml.getBytes(encoding);
      } catch (final UnsupportedEncodingException e) {
         throw new RuntimeException("Unsupported encoding for model set in '" + PredefinedConstants.XML_ENCODING + "'.");
      }
   }

   /**
    * Tests connection to the AuditTrail database and makes simple check to verify that
    * the MODEL table exists. If there is reason to believe the AuditTrail schema does not
    * exist, the schema is deployed.
    * 
    * @throws Exception
    */
   protected void prepareAuditTrailDatabase() throws Exception
   {
      Connection conn = null;
      try
      {
         if (null != auditTrailDataSource)
         {
            conn = auditTrailDataSource.getConnection();
         }
         else
         {
            conn = getConnectionFromDriver();
         }
      }
      catch (SQLException e)
      {
         if (e.getMessage().matches("^Database .* not found\\.$"))
         {
            log.info("AuditTrail database does not seem to exist. Attempting to create embedded DB ...");
            if (null != auditTrailDataSource)
               log.warn("Injected datasource detected! It might differ from the configuration of the"
                     + " embedded database (URL:" + auditTrailUrl + ")!");
            // bootstrap AuditTrail as embedded to create the database
            conn = getConnectionFromDriver();
         }
         else
            throw e;
      }

      if (null != conn)
      {
         try
         {
            Statement s = conn.createStatement();
            s.execute("SELECT count(*) FROM MODEL");
         }
         catch (SQLException e)
         {
            // create the IPP schema if the exception suggests it
            if (null != conn && e.getMessage().matches("^Schema .* does not exist.*$"))
            {
               log.info("AuditTrail schema does not seem to exist. Attempting to create it ...");
               try
               {
                  // IMPORTANT: SchemaHelper automatically reads the settings from the
                  // carnot.properties file on the classpath!
                  SchemaHelper.createSchema();
               }
               catch (Exception e1)
               {
                  log.error("Unable to create the AuditTrail schema. Make sure all DB settings "
                        + "are correct in the carnot.properties file!", e1);
               }
            }
            else
               throw e;
         }
         finally
         {
            if (null != conn)
               conn.close();
         }
      }
   }

   private Connection getConnectionFromDriver() throws ClassNotFoundException, SQLException
   {
      if ("derby".equalsIgnoreCase(auditTrailType) && !StringUtils.isEmpty(derbyHomePath))
      {
         // Set Derby system home, if not specified as VM parameter
         if (StringUtils.isEmpty(System.getProperty("derby.system.home")))
         {
            if (log.isDebugEnabled())
               log.debug("Setting system property 'derby.system.home' to " + derbyHomePath);
            System.setProperty("derby.system.home", derbyHomePath);
         }
      }

      Connection conn;
      // register driver
      Class.forName(auditTrailDriverClass);
      // NOTE for Derby: connecting to the DB for the first time will boot Derby as
      // embedded DB
      conn = DriverManager.getConnection(auditTrailUrl, auditTrailUser, auditTrailPassword);
      return conn;
   }

   /**
    * Deletes the model with the {@link #testProcessModelOID} from the AuditTrail if the
    * OID is set. Otherwise "Cleanup Runtime" is executed.
    * 
    * @throws Exception
    */
   public void tearDownGlobal() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      try
      {
         AdministrationService admin = sf.getAdministrationService();
         if (testProcessModelOID > -1)
         {
            log.info("Global Tear Down: Deleting process model with OID " + testProcessModelOID);
            admin.deleteModel(testProcessModelOID);
         }
         else
         {
            log.info("Global Tear Down: Initiating Runtime Cleanup");
            admin.cleanupRuntime(true);
         }
      }
      finally
      {
         if (null != sf)
            sf.close();
      }
   }

   /**
    * Aborts and deletes all process instances associated with the
    * {@link #testProcessModelOID} if it is set.
    * 
    * @throws Exception
    */
   public void tearDown() throws Exception
   {
      // Remove created processes after every test.
      if (testProcessModelOID > -1)
      {
         log.info("Test Tear Down: Deleting process instances for model " + testProcessModelOID);
         ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
         try
         {
            QueryService qService = sf.getQueryService();
            AdministrationService admin = sf.getAdministrationService();

            ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
            ProcessInstances instances = qService.getAllProcessInstances(query);
            List<Long> piOids = new ArrayList<Long>();
            for (ProcessInstance pi : (List<ProcessInstance>) instances)
            {
               if (testProcessModelOID == pi.getModelOID())
               {
                  admin.abortProcessInstance(pi.getOID());
                  piOids.add(pi.getOID());
               }
            }
            // if delete processes is called to soon, some PIs might still be in
            // state ABORTING and cannot be deleted!
            admin.deleteProcesses(piOids);
            admin.deleteModel(testProcessModelOID);
         }
         finally
         {
            if (null != sf)
               sf.close();
         }
      }
   }

   /**
    * Creates the schema file and returns the path to it.
    * 
    * @return the filepath to the IPP schema
    */
   public static String createAuditTrailSchemaFile()
   {
      String filepath = null;
      try
      {
         filepath = System.getProperty("java.io.tmpdir");
         if (!filepath.endsWith(System.getProperty("file.separator")))
            filepath += System.getProperty("file.separator");
         filepath += "IPP-AuditTrailSchema-Derby.sql";
         String[] sysconsoleArgs = {"-dbtype", getInstance().auditTrailType, "ddl", "-file", filepath};
         org.eclipse.stardust.engine.cli.sysconsole.Main.run(sysconsoleArgs);
      }
      catch (Exception e)
      {
         log.error(
               "Unexpected exception while executing sysconsole 'ddl' command. Make sure the auditTrailType property has been set!",
               e);
      }
      return filepath;
   }

   /**
    * Creates the partition with the specified name.
    * 
    * @param partition
    */
   public void createPartition(String partitionId)
   {

      Session session = null;
      session = (Session) SessionFactory.createSession(SessionFactory.AUDIT_TRAIL, auditTrailDataSource);

      try
      {
         final Parameters params = Parameters.instance();

         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);

         final String schemaName = params.getString(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               params.getString(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         ddlManager.createAuditTrailPartition(partitionId, session.getConnection(), schemaName, null, null);
      }
      catch (SQLIntegrityConstraintViolationException e)
      {
         // this exception is already caught by the DDLManager and logged as a warning.
         // But just in case the exception
         // is ever thrown up to here ...
         log.info("Partition " + partitionId + " might exist already. Ignoring SQL Constraint Violation Exception ...");
      }
      catch (Exception e)
      {
         throw new PublicException("Failed creating partiton.", e);
      }
   }

   public long getTestProcessModelOID()
   {
      return testProcessModelOID;
   }

   public void setServiceFactoryAccess(ServiceFactoryAccess serviceFactoryAccess)
   {
      this.serviceFactoryAccess = serviceFactoryAccess;
   }

   public void setAuditTrailDataSource(DataSource auditTrailDataSource)
   {
      this.auditTrailDataSource = auditTrailDataSource;
   }

   public void setModelFilename(String... filenames)
   {
      this.modelFilenames = filenames;
      this.modelFiles = null;
   }

   public void setModelFile(Resource... modelFiles)
   {
      this.modelFiles = modelFiles;
   }

   public void setAuditTrailUser(String auditTrailUser)
   {
      this.auditTrailUser = auditTrailUser;
   }

   public void setAuditTrailPassword(String auditTrailPassword)
   {
      this.auditTrailPassword = auditTrailPassword;
   }

   public void setAuditTrailUrl(String auditTrailUrl)
   {
      this.auditTrailUrl = auditTrailUrl;
   }

   public void setAuditTrailType(String auditTrailType)
   {
      this.auditTrailType = auditTrailType;
   }

   public void setAuditTrailDriverClass(String auditTrailDriverClass)
   {
      this.auditTrailDriverClass = auditTrailDriverClass;
   }

   public void setDerbyHomePath(String derbyHomePath)
   {
      this.derbyHomePath = derbyHomePath;
   }

   /**
    * Returns a static reference to an instance of this class that has previously been
    * initiated.
    * 
    * @return the instance
    * @throws IllegalStateException
    *            if no instance of this class has ever been created.
    */
   public static SpringTestUtils getInstance()
   {
      if (null == _INSTANCE)
      {
         log.warn("You need to initialize an instance of class " + SpringTestUtils.class.getSimpleName()
               + " prior to static access! For example, create a single bean in your Spring context.");
         throw new IllegalStateException("No instance of " + SpringTestUtils.class.getSimpleName() + " found!");
      }
      return _INSTANCE;
   }

   public void afterPropertiesSet() throws Exception
   {
      if (null == this.auditTrailDataSource)
      {
         log.warn("No injected AuditTrail datasource detected! Searching Spring application context for possible matches ...");
         Map<String, DataSource> datasources = applicationContext.getBeansOfType(DataSource.class);
         for (String id : datasources.keySet())
         {
            if (id.contains("AuditTrail"))
               this.auditTrailDataSource = datasources.get(id);
            log.info("Using datasource bean ID " + id + " detected in Spring context as AuditTrail.");
            break;
         }
      }
   }

   public void setApplicationContext(ApplicationContext context) throws BeansException
   {
      this.applicationContext = context;
   }
}