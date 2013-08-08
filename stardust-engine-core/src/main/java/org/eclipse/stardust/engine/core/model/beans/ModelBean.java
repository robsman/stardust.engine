/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.beans;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.*;
import org.eclipse.stardust.engine.core.model.utils.*;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableDefinition;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.IConfigurationVariableDefinition;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.utils.AuthorizationContext;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

/**
 *
 * @author mgille
 * @version $Revision$
 */
public class ModelBean extends RootElementBean
      implements IModel
{
   private static final String REFERENCED_MODEL_NOT_IN_BUNDLE = "Model ''{0}'' is referenced but not part of this deployment.\n" +
         		"Please make sure that all required process models have already been deployed\n" +
         		"or deploy all interdependent artifacts in a single bundle.";
   private static final String UNRESOLVED_MODEL_REFERENCE = "Unresolved reference to model ''{0}''.";
   private static final String REFERENCE_IS_INVALID_UNTIL = "Reference to model ''{0}'' is invalid until {1}.";
   private static final String CIRCULAR_REFERENCES = "Circular references to model ''{0}''.";
   private static final String RESOLVED_TO_MULTIPLE_VERSIONS = "Reference for ''{0}'' is resolved to multiple model versions.";
   private static final String NOT_RESOLVED_TO_LAST_DEPLOYED = "Reference for ''{0}'' was not resolved to the last deployed model version.";

   private static final long serialVersionUID = 3L;

   private static final Logger trace = LogManager.getLogger(ModelBean.class);
   
   private static final String IPP_VARIABLES = "ipp:variables[";
   private static final String IPP_VARIABLES_DESCRIPTION = "]:description";
   private static final String IPP_VARIABLES_DEFAULT_VALUE = "]:defaultValue";
   private static final String IPP_VARIABLES_NAME = "]:name";

   private static final String APPLICATION_STRING = "Application";
   private static final String CONDITIONAL_PERFORMER_STRING =
         "ConditionalPerformer";
   private static final String DATA_STRING = "Data";
   private static final String DIAGRAM_STRING = "Diagram";
   private static final String MODELER_STRING = "Modeler";
   private static final String ORGANIZATION_STRING = "Organization";
   private static final String PROCESS_DEFINITION_STRING =
         "ProcessDefinition";
   private static final String ROLE_STRING = "Role";
   private static final String VIEW_STRING = "View";

   public static final String CARNOT_VERSION_ATT = "CARNOT version";
   private Version carnotVersion;

   private List<IExternalPackage> externalPackages = CollectionUtils.newList();
   private Scripting scripting;
   private Link typeDeclarations = new Link(this, "TypeDeclarations");
   private Link processDefinitions = new Link(this, "Process Definitions");
   private SearchableList<IData> data = new SearchableList<IData>();
   private Link applications = new Link(this, "Applications");
   private SearchableList<IModelParticipant> participants = new SearchableList<IModelParticipant>();
   private Link diagrams = new Link(this, "Diagrams");
   private Link views = new Link(this, "Views");
   private Link linkTypes = new Link(this, "Link Types");
   private Link applicationTypes = new Link(this, "Application Types");
   private Link dataTypes = new Link(this, "Data Types");
   private Link applicationContextTypes = new Link(this, "Application Context Types");
   private Link triggerTypes = new Link(this, "Trigger Types");
   private Link eventConditionTypes = new Link(this, "Event Condition Types");
   private Link eventActionTypes = new Link(this, "Event Action Types");
   private Map<QName, List<IProcessDefinition>> implementations = Collections.emptyMap();

   // @todo (france, ub): how is the merge behaviour of all that stuff?!
   private int defaultProcessDefinitionId = 1;
   private int defaultApplicationId = 1;
   private int defaultDataId = 1;
   private int defaultModelerId = 1;
   private int defaultRoleId = 1;
   private int defaultOrganizationId = 1;
   private int defaultConditionalPerformerId = 1;
   private int defaultDiagramId = 1;
   private int defaultViewId = 1;
   
   private Set<String> configurationVariableReferences = CollectionUtils.newSet();
   private QualityAssuranceBean qualityAssuranceBean;

   public ModelBean(String id, String name, String description)
   {
      super(id, name);

      trace.debug("Creating model " + id + ".");

      setDescription(description);
   }

   public void addToParticipants(IModelParticipant participant)
   {
      participants.add(participant);

      if (participant instanceof IRole)
      {
         defaultRoleId = nextID(ROLE_STRING, defaultRoleId, participant.getId());
      }
      else if (participant instanceof IOrganization)
      {
         defaultOrganizationId = nextID(ORGANIZATION_STRING,
               defaultOrganizationId, participant.getId());
      }
      else if (participant instanceof IConditionalPerformer)
      {
         defaultConditionalPerformerId = nextID(CONDITIONAL_PERFORMER_STRING,
               defaultConditionalPerformerId, participant.getId());
      }
      else if (participant instanceof IModeler)
      {
         defaultModelerId = nextID(MODELER_STRING, defaultModelerId, participant.getId());
      }
      else
      {
         Assert.lineNeverReached();
      }
   }

   public void addToViews(org.eclipse.stardust.engine.api.model.IView view)
   {
      markModified();
      views.add(view);
   }

   public IModeler authentify(String id, String password)
   {
      IModeler modeler = participants.find(id, IModeler.class);
      if (modeler != null && modeler.checkPassword(password))
      {
         return modeler;
      }
      throw new PublicException("The user '" + id + "' does not exist or is using an incorrect password.");
   }

   /**
    * Retrieves a vector with all inconsistencies of the model.
    */
   public List checkConsistency()
   {
      List inconsistencies = CollectionUtils.newList();
      
      if (!validateReferences(inconsistencies))
      {
         return inconsistencies;
      }

      super.checkConsistency(inconsistencies);

      try
      {         
         IQualityAssurance qualityAssurance = getQualityAssurance();
         if (qualityAssurance != null)
         {
            List<IQualityAssuranceCode> allCodes = qualityAssurance.getAllCodes();

            // validate id
            for (IQualityAssuranceCode code : allCodes)
            {
               boolean isValidId = true;
               if (StringUtils.isEmpty(code.getCode()))
               {
                  isValidId = false;
               }
               else if (!StringUtils.isValidIdentifier(code.getCode()))
               {
                  isValidId = false;
               }

               if (!isValidId)
               {
                  BpmRuntimeError error = BpmRuntimeError.MDL_INVALID_QA_CODE_ID
                        .raise(code.getCode());
                  inconsistencies.add(new Inconsistency(error.toString(), this,
                        Inconsistency.ERROR));
               }
            }

            // validate duplicates
            Map<IQualityAssuranceCode, Integer> duplicatesInfo = validateQaCodeDuplicates(allCodes);
            if (!duplicatesInfo.isEmpty())
               {
               for (IQualityAssuranceCode qaCode : duplicatesInfo.keySet())
               {               
                  Integer duplicatesCount = duplicatesInfo.get(qaCode);
                  String code = qaCode.getCode();

                  BpmRuntimeError error = BpmRuntimeError.MDL_DUPLICATE_QA_CODE.raise(
                        code, duplicatesCount);
                  inconsistencies.add(new Inconsistency(error.toString(), this,
                        Inconsistency.ERROR));
               }
            }
         }
         
         for (IData data : getData())
         {
            data.checkConsistency(inconsistencies);
         }

         for (Iterator i = getAllApplications();i.hasNext();)
         {
            ((IApplication) i.next()).checkConsistency(inconsistencies);
         }

         for (IProcessDefinition processDefinition : getProcessDefinitions())
         {
            processDefinition.checkConsistency(inconsistencies);
         }

         for (Iterator i = getAllParticipants();i.hasNext();)
         {
            ((IModelParticipant) i.next()).checkConsistency(inconsistencies);
         }
         
         IModelParticipant administrator = findParticipant("Administrator");
         if (administrator == null)
         {
            inconsistencies.add(new Inconsistency("Missing 'Administrator' participant.",
                  this, Inconsistency.ERROR));
         }
         else if (!(administrator instanceof IRole))
         {
            inconsistencies.add(new Inconsistency("'Administrator' participant must be a role.",
                  administrator, Inconsistency.ERROR));
         }
         
         if (!scripting.isSupported())
         {
            inconsistencies.add(new Inconsistency("Unsupported scripting language: " + scripting.getType(),
                  this, Inconsistency.ERROR));
         }
         
         if (ModelManagerFactory.isAvailable())
         {
            if (administrator.getAllOrganizations().hasNext())
            {
               inconsistencies.add(new Inconsistency(
                     "Administrator is not allowed to have relationships to any organizations.",
                     this, Inconsistency.ERROR));
            }
            Set<String> grants = new HashSet<String>();
            addAllGrants(grants, QueryService.class, "getAllUsers", UserQuery.class);
            addAllGrants(grants, AdministrationService.class, "deployModel", String.class, int.class);
            addAllGrants(grants, AdministrationService.class, "cleanupRuntimeAndModels");
            addAllGrants(grants, AdministrationService.class, "recoverRuntimeEnvironment");
            addAllGrants(grants, AdministrationService.class, "getAllDaemons", boolean.class);
            addAllGrants(grants, AdministrationService.class, "flushCaches");
            addAllGrants(grants, AdministrationService.class, "getAuditTrailHealthReport");
            addAllGrants(grants, UserService.class, "modifyUser", User.class);
            addAllGrants(grants, QueryService.class, "getActiveModel");
            addAllGrants(grants, AdministrationService.class, "getDepartment", long.class);
            addAllGrants(grants, AdministrationService.class, "removeDepartment", long.class);

            Iterator allParticipants = getAllParticipants();
            while (allParticipants.hasNext())
            {
               IModelParticipant participant = (IModelParticipant) allParticipants.next();
               if (grants.contains(participant.getId()) && isScoped(participant))
               {
                  inconsistencies.add(new Inconsistency(
                        "Scoped participants are not allowed for model level grants.",
                        this, Inconsistency.ERROR));
                  break;
               }
            }
         }
         
       ModelElementList typeDecls = getTypeDeclarations();
       for (int i = 0; i < typeDecls.size(); i++)
       {
          TypeDeclarationBean typeDecl = (TypeDeclarationBean) typeDecls.get(i);
          typeDecl.checkConsistency(inconsistencies);
       }
       
         Set<String> configurationVariableReferences = getConfigurationVariableReferences();
         Set<IConfigurationVariableDefinition> configurationVariableDefinitions = getConfigurationVariableDefinitions();
         Set<String> definedVarNames = CollectionUtils.newSet();
         for (IConfigurationVariableDefinition varDefinition : configurationVariableDefinitions)
         {
            definedVarNames.add(varDefinition.getName());
            if (StringUtils.isEmpty(varDefinition.getDefaultValue()))
            {
               inconsistencies.add(new Inconsistency("Configuration Variable '"
                     + varDefinition.getName() + "' has no default value defined.", this,
                     Inconsistency.WARNING));
            }
            
            if ( !configurationVariableReferences.contains(varDefinition.getName()))
            {
               inconsistencies.add(new Inconsistency("Configuration Variable '"
                     + varDefinition.getName() + "' is never used.", this,
                     Inconsistency.WARNING));
            }
         }
         
         for (String varReferenceName : configurationVariableReferences)
         {
            if ( !definedVarNames.contains(varReferenceName))
            {
               inconsistencies.add(new Inconsistency("Referenced Configuration Variable '"
                     + varReferenceName + "' does not exist.", this,
                     Inconsistency.WARNING));
            }
         }
         
         return inconsistencies;
      }
      catch (ApplicationException ae)
      {
         throw ae;
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   private Map<IQualityAssuranceCode, Integer> validateQaCodeDuplicates(
         List<IQualityAssuranceCode> allCodes)
   {

      Map<IQualityAssuranceCode, Integer> duplicatesInfo = new HashMap<IQualityAssuranceCode, Integer>();
      Map<IQualityAssuranceCode, IQualityAssuranceCode> detectedDuplicates = new HashMap<IQualityAssuranceCode, IQualityAssuranceCode>();

      for (IQualityAssuranceCode outerCode : allCodes)
      {
         if (!detectedDuplicates.containsKey(outerCode))
         {
            for (IQualityAssuranceCode innerCode : allCodes)
            {
               if (outerCode != innerCode)
               {
                  // duplicate detected
                  if (!StringUtils.isEmpty(outerCode.getCode())
                        && outerCode.getCode().equals(innerCode.getCode()))
                  {
                     Integer duplicateCount = null;
                     // remember the duplicates
                     if (duplicatesInfo.containsKey(outerCode))
                     {
                        duplicateCount = duplicatesInfo.get(outerCode);
                        duplicateCount++;
         }
                     else
                     {
                        duplicateCount = 1;

   }

                     // update duplicate counter
                     duplicatesInfo.put(outerCode, duplicateCount);
                     // make sure the detected & counted duplicate wont be considered
                     // again
                     detectedDuplicates.put(innerCode, innerCode);
                  }
               }
            }
         }
      }

      return duplicatesInfo;
   }
   
   private boolean validateReferences(List<Inconsistency> inconsistencies)
   {
      for (IExternalPackage externalPackage : externalPackages)
      {
         try
         {
            if (externalPackage.getReferencedModel() == null)
            {
               return addReferenceInconsistency(inconsistencies, externalPackage);
            }
         }
         catch (Exception ex)
         {
            return addReferenceInconsistency(inconsistencies, externalPackage);
         }
      }
      
      if (ModelManagerFactory.isAvailable())
      {
         Date ref = (Date) getAttribute(PredefinedConstants.VALID_FROM_ATT);
         return validateTransitiveConsistency(ModelManagerFactory.getCurrent(), ref == null ? new Date() : ref,
               CollectionUtils.<String, IModel>newMap(), this, inconsistencies);
      }
      return true;
   }

   private boolean addReferenceInconsistency(List<Inconsistency> inconsistencies,
         IExternalPackage externalPackage)
   {
      boolean modelManagerAvailable = ModelManagerFactory.isAvailable();
      inconsistencies.add(new Inconsistency(modelManagerAvailable ? Inconsistency.ERROR : Inconsistency.WARNING, this,
            modelManagerAvailable ? UNRESOLVED_MODEL_REFERENCE : REFERENCED_MODEL_NOT_IN_BUNDLE, externalPackage.getHref()));
      return false;
   }

   private boolean validateTransitiveConsistency(ModelManager mm, Date ref, Map<String, IModel> referencedModels,
         IModel model, List<Inconsistency> inconsistencies)
   {
      String id = model.getId();
      IModel other = referencedModels.get(id);
      if (other == null)
      {
         referencedModels.put(id, model);
         for (IExternalPackage pkg : model.getExternalPackages())
         {
            IModel referencedModel = null;
            try
            {
               referencedModel = pkg.getReferencedModel();
            }
            catch (UnresolvedExternalReference ex)
            {
               // ignore this since it should have been already reported.
               continue;
            }
            if (referencedModel == this)
            {
               inconsistencies.add(new Inconsistency(Inconsistency.ERROR, this, CIRCULAR_REFERENCES, id));
               return false;
            }
            if (ref != null)
            {
               Date from = (Date) referencedModel.getAttribute(PredefinedConstants.VALID_FROM_ATT);
               if (from != null && from.after(ref))
               {
                  inconsistencies.add(new Inconsistency(Inconsistency.ERROR, this, REFERENCE_IS_INVALID_UNTIL, id, from));
                  return false;
               }
            }
            if (!validateTransitiveConsistency(mm, ref, referencedModels, referencedModel, inconsistencies))
            {
               return false;
            }
            String refId = referencedModel.getId();
            BpmRuntimeEnvironment rte = PropertyLayerProviderInterceptor.getCurrent();
            if (rte != null)
            {
               Map<String, IModel> overrides = rte.getModelOverrides();
               if (overrides != null && referencedModel == overrides.get(refId))
               {
                  trace.info("Using deployment reference for " + refId);
                  break;
               }
            }
            IModel lastDeployedModel = mm.findLastDeployedModel(refId);
            if (lastDeployedModel != null)
            {
               if (referencedModel != lastDeployedModel)
               {
                  inconsistencies.add(new Inconsistency(Inconsistency.WARNING, model, NOT_RESOLVED_TO_LAST_DEPLOYED, refId));
               }
            }
            else
            {
               trace.info("No previous version for " + refId);
            }
         }
      }
      else if (model != other && id.equals(other.getId()))
      {
         inconsistencies.add(new Inconsistency(Inconsistency.ERROR, model, RESOLVED_TO_MULTIPLE_VERSIONS, id));
         return false;
      }
      return true;
   }

   private boolean isScoped(IModelParticipant participant)
   {
      boolean scopedOrg = false;
      if (participant instanceof IOrganization)
      {
         IOrganization organization = (IOrganization) participant;
         scopedOrg = organization
               .getBooleanAttribute(PredefinedConstants.BINDING_ATT);
         if (!scopedOrg)
         {
            scopedOrg = checkSuperOrganizationScoped(organization);
         }
      }
      else
      {
         scopedOrg = checkSuperOrganizationScoped(participant);
      }
      return scopedOrg;
   }

   private boolean checkSuperOrganizationScoped(IModelParticipant participant)
   {
      boolean scopedOrg = false;
      Iterator organizations = participant.getAllOrganizations();
      while (organizations.hasNext())
      {
         IOrganization org = (IOrganization) organizations.next();
         if (isScoped(org))
         {
            scopedOrg = true;
            break;
         }
      }
      return scopedOrg;
   }

   private void addAllGrants(Set<String> grants, Class service, String method, Class... params)
   {
      AuthorizationContext authorizationContext = AuthorizationContext.create(service, method, params);
      authorizationContext.setModels(Collections.singletonList((IModel) this));
      grants.addAll(Arrays.asList(authorizationContext.getGrants()));
   }

   public ITypeDeclaration createTypeDeclaration(String id, String name,
         String description, Map attributes, IXpdlType xpdlType)
   {
      if (null != findTypeDeclaration(id))
      {
         throw new PublicException("There is already a type declaration with ID '" + id + "'.");
      }
      
      markModified();

      TypeDeclarationBean typeDeclaration = new TypeDeclarationBean(id, name,
            description, attributes, xpdlType);

      addToTypeDeclarations(typeDeclaration);
      typeDeclaration.setTransient(true);
      // since typeDeclaration is transient, 0 will not be used
      // next available transient id will be assigned
      typeDeclaration.register(0);

      return typeDeclaration;
   }

   public IApplication createApplication(String id, String name,
         String description, int elementOID)
   {
      if (findApplication(id) != null)
      {
         throw new PublicException("There is already an application with ID '" + id + "'.");
      }

      markModified();

      ApplicationBean application = new ApplicationBean(id, name, description);

      addToApplications(application);
      application.register(elementOID);

      return application;
   }

   public IData createData(String id, IDataType type, String name, String description,
         boolean predefined, int elementOID, Map attributes)
   {
      if (findData(id) != null)
      {
         throw new PublicException("There is already a workflow data with ID '" + id + "'.");
      }

      markModified();

      DataBean date = new DataBean(id, type, name, description, predefined, attributes);
      date.setParent(this);
      addToData(date);
      date.register(elementOID);

      return date;
   }

   public Diagram createDiagram(String name)
   {
      return createDiagram(name, 0);
   }

   public Diagram createDiagram(String name, int elementOID)
   {
      markModified();

      DefaultDiagram diagram = new DefaultDiagram(name);

      addToDiagrams(diagram);
      diagram.register(elementOID);

      return diagram;
   }

   public IModeler createModeler(String id, String name, String description,
         String password, int elementOID)
   {
      return addParticipant(elementOID, new ModelerBean(id, name, description, password));
   }

   public IConditionalPerformer createConditionalPerformer(String id, String name,
         String description, IData data, int elementOID)
   {
      return addParticipant(elementOID, new ConditionalPerformerBean(id, name, description, data));
   }

   public IView createView(String name, String description, int elementOID)
   {
      markModified();

      ViewBean view = new ViewBean(name, description);

      addToViews(view);

      view.register(elementOID);

      return view;
   }

   public IOrganization createOrganization(String id, String name,
         String description, int elementOID)
   {
      return addParticipant(elementOID, new OrganizationBean(id, name, description));
   }

   public IProcessDefinition createProcessDefinition(String id, String name,
         String description)
   {
      return createProcessDefinition(id, name, description, true, 0);
   }

   public IProcessDefinition createProcessDefinition(String id, String name,
         String description, boolean createDefaultDiagram, int elementOID)
   {
      if (findProcessDefinition(id) != null)
      {
         throw new PublicException("There is already a process definition with ID '" + id + "'.");
      }

      markModified();

      ProcessDefinitionBean processDefinition = new ProcessDefinitionBean(id,
            name, description);

      addToProcessDefinitions(processDefinition);
      processDefinition.register(elementOID);

      if (createDefaultDiagram)
      {
         processDefinition.createDiagram("Default");
      }

      return processDefinition;
   }

   public IRole createRole(String id, String name, String description, int elementOID)
   {
      return addParticipant(elementOID, new RoleBean(id, name, description));
   }

   public <T extends IModelParticipant> T addParticipant(int elementOID, T participant)
   {
      participant.setParent(this);
      addToParticipants(participant);
      participant.register(elementOID);
      return participant;
   }

   public ModelElementList getTypeDeclarations()
   {
      return typeDeclarations;
   }

   public ITypeDeclaration findTypeDeclaration(String id)
   {
      return (ITypeDeclaration) typeDeclarations.findById(id);
   }
   
   public IApplication findApplication(String id)
   {
      return (IApplication) applications.findById(id);
   }

   public IData findData(String id)
   {
      return data.find(id);
   }

   public Diagram findDiagram(String id)
   {
      return (Diagram) diagrams.findById(id);
   }

   public ILinkType findLinkType(String id)
   {
      return (ILinkType) linkTypes.findById(id);
   }

   public IModelParticipant findParticipant(String id)
   {
      return participants.find(id);
   }

   public IProcessDefinition findProcessDefinition(String id)
   {
      return (IProcessDefinition) processDefinitions.findById(id);
   }

   public Iterator getAllApplications()
   {
      return applications.iterator();
   }

   public ModelElementList getApplications()
   {
      return applications;
   }

   public int getApplicationsCount()
   {
      return applications.size();
   }

   public Iterator getAllData()
   {
      return data.iterator();
   }
   
   public ModelElementList<IData> getData()
   {
      return data;
   }

   public int getDataCount()
   {
      return data.size();
   }

   public Iterator getAllDiagrams()
   {
      return diagrams.iterator();
   }

   public int getDiagramsCount()
   {
      return diagrams.size();
   }

   /**
    * Returns all participants participating in workflow execution and modeling
    * that are organizations.
    *
    * @see IModel#getAllParticipants
    */
   public Iterator getAllOrganizations()
   {
      return participants.iterator(IOrganization.class);
   }

   /**
    * Returns all participants participating in workflow execution and modeling
    * that are roles.
    *
    * @see IModel#getAllParticipants
    */
   public Iterator getAllRoles()
   {
      return participants.iterator(IRole.class);
   }

   /**
    * Returns all participants participating in workflow execution and modeling.
    * Currently roles, organizations and modelers.
    */
   public Iterator getAllParticipants()
   {
      return participants.iterator();
   }

   public ModelElementList getParticipants()
   {
      return participants;
   }

   /**
    * Returns all participants participating in workflow execution.
    * Currently roles and organizations.
    */
   public Iterator getAllWorkflowParticipants()
   {
      return new FilteringIterator(getAllParticipants(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return (o instanceof IRole) || (o instanceof IOrganization);
         }
      });
   }

   public int getModelersCount()
   {
      return participants.size(IModeler.class);
   }

   public int getRolesCount()
   {
      return participants.size(IRole.class);
   }

   public int getOrganizationsCount()
   {
      return participants.size(IRole.class);
   }

   public int getConditionalPerformersCount()
   {
      return participants.size(IConditionalPerformer.class);
   }

   public Iterator getAllProcessDefinitions()
   {
      return processDefinitions.iterator();
   }

   public ModelElementList<IProcessDefinition> getProcessDefinitions()
   {
      return processDefinitions;
   }

   public int getProcessDefinitionsCount()
   {
      return processDefinitions.size();
   }

   public Iterator getAllTopLevelParticipants()
   {
      return new FilteringIterator(getAllParticipants(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return !((IModelParticipant) o).getAllOrganizations().hasNext();
         }
      });
   }

   public Iterator getAllViews()
   {
      return views.iterator();
   }

   public IApplicationType findApplicationType(String id)
   {
      return (IApplicationType) applicationTypes.findById(id);
   }

   public IApplicationType createApplicationType(String id, String name,
         boolean predefined, boolean synchronous, int elementOID)
   {
      IApplicationType type = new ApplicationTypeBean(id, name, predefined, synchronous);
      applicationTypes.add(type);
      type.register(elementOID);
      return type;
   }

   public Iterator getAllApplicationTypes()
   {
      return applicationTypes.iterator();
   }

   public IDataType findDataType(String id)
   {
      return (IDataType) dataTypes.findById(id);
   }

   public IDataType createDataType(String id, String name, boolean predefined, int elementOID)
   {
      IDataType type = new DataTypeBean(id, name, predefined);
      dataTypes.add(type);
      type.register(elementOID);
      return type;
   }

   public Iterator getAllDataTypes()
   {
      return dataTypes.iterator();
   }

   public void addToDataTypes(IDataType type)
   {
      dataTypes.add(type);
   }

   public void removeFromEventConditionTypes(IEventConditionType type)
   {
      eventConditionTypes.remove(type);
   }

   public void removeFromDataTypes(IDataType type)
   {
      dataTypes.remove(type);
   }

   public IApplicationContextType findApplicationContextType(String id)
   {
      return (IApplicationContextType) applicationContextTypes.findById(id);
   }

   public IApplicationContextType createApplicationContextType(String id, String name,
         boolean predefined, boolean hasMappingId, boolean hasApplicationPath,
         int elementOID)
   {
      IApplicationContextType type = new ApplicationContextTypeBean(id, name, predefined,
         hasMappingId, hasApplicationPath);
      applicationContextTypes.add(type);
      type.register(elementOID);

      return type;
   }

   public void addToApplicationContextTypes(IApplicationContextType type)
   {
      applicationContextTypes.add(type);
   }

   public void removeFromEventActionTypes(IEventActionType type)
   {
      eventActionTypes.remove(type);
   }
   
   public Iterator getAllApplicationContextTypes()
   {
      return applicationContextTypes.iterator();
   }

   public void removeFromApplicationContextTypes(IApplicationContextType type)
   {
      applicationContextTypes.remove(type);
   }

   public ITriggerType createTriggerType(String id, String name, boolean predefined,
         boolean pullTrigger, int elementOID)
   {
      ITriggerType result = new TriggerTypeBean(id, name, predefined, pullTrigger);
      triggerTypes.add(result);
      result.register(elementOID);
      return result;
   }

   public ITriggerType findTriggerType(String id)
   {
      return (ITriggerType) triggerTypes.findById(id);
   }

   public Iterator getAllTriggerTypes()
   {
      return triggerTypes.iterator();
   }

   public Iterator getAllEventConditionTypes()
   {
      return eventConditionTypes.iterator();
   }

   /**
    * @return Unique ID for newly created applications.
    */
   public String getDefaultApplicationId()
   {
      return APPLICATION_STRING + defaultApplicationId;

   }

   /**
    * @return Unique ID for newly created data.
    */
   public String getDefaultDataId()
   {
      return DATA_STRING + defaultDataId;
   }

   /**
    * @return Unique ID for newly created diagrams.
    */
   public String getDefaultDiagramId()
   {
      return DIAGRAM_STRING + defaultDiagramId;
   }

   /**
    * @return Unique ID for newly created human.
    */
   public String getDefaultModelerId()
   {
      return MODELER_STRING + defaultModelerId;
   }

   /**
    * @return Unique ID for newly created organizations.
    */
   public String getDefaultOrganizationId()
   {
      return ORGANIZATION_STRING + defaultOrganizationId;
   }

   /**
    * @return Unique ID for newly created conditional resources.
    */
   public String getDefaultConditionalPerformerId()
   {
      return CONDITIONAL_PERFORMER_STRING + defaultConditionalPerformerId;
   }

   /**
    * @return Unique ID for newly created process definitions.
    */
   public String getDefaultProcessDefinitionId()
   {
      return PROCESS_DEFINITION_STRING + defaultProcessDefinitionId;
   }

   /**
    * @return Unique ID for newly created roles.
    */
   public String getDefaultRoleId()
   {
      return ROLE_STRING + defaultRoleId;
   }

   /**
    * @return Unique ID for newly created views.
    */
   public String getDefaultViewId()
   {
      return VIEW_STRING + defaultViewId;
   }

   /**
    * <code>ag.carnot.utils.predicate.SymbolTable</code> protocol.
    */
   public AccessPoint lookupSymbolType(String name)
   {
      IData data = findData(name);

      if (null == data)
      {
         throw new PublicException("Invalid symbol '" + name + "'");
      }

      return data;
   }

   /**
    * <code>ag.carnot.utils.predicate.SymbolTable</code> protocol.
    */
   public Object lookupSymbol(String name)
   {
      if (null == findData(name))
      {
         throw new PublicException("Invalid symbol '" + name + "'");
      }

      return null;
   }

   public void addToTypeDeclarations(ITypeDeclaration typeDeclaration)
   {
      markModified();
      typeDeclarations.add(typeDeclaration);
      //defaultTypeDeclarationId = nextID(TYPE_DECLARATION_STRING, defaultTypeDeclarationId, typeDeclaration.getId());
   }

   public void addToApplications(IApplication application)
   {
      markModified();
      applications.add(application);
      defaultApplicationId = nextID(APPLICATION_STRING, defaultApplicationId, application.getId());
   }

   public void addToEventActionTypes(IEventActionType type)
   {
      markModified();
      eventActionTypes.add(type);
   }


   public void addToEventConditionTypes(IEventConditionType type)
   {
      markModified();
      eventConditionTypes.add(type);
   }

   public void removeFromApplications(IApplication application)
   {
      markModified();
      applications.remove(application);
   }

   public void addToData(IData data)
   {
      this.data.add(data);
      defaultDataId = nextID(DATA_STRING, defaultDataId, data.getId());
   }

   public void removeFromData(IData data)
   {
      this.data.remove(data);
   }

   public void addToDiagrams(Diagram diagram)
   {
      markModified();
      diagrams.add(diagram);
      defaultDiagramId = nextID(DIAGRAM_STRING, defaultDiagramId, diagram.getId());
   }

   public void removeFromDiagrams(Diagram diagram)
   {
      markModified();
      diagrams.remove(diagram);
   }

   public void removeFromParticipants(IModelParticipant participant)
   {
      participants.remove(participant);
   }

   public void removeFromProcessDefinitions(IProcessDefinition processDefinition)
   {
      markModified();
      processDefinitions.remove(processDefinition);
   }

   public void addToProcessDefinitions(IProcessDefinition processDefinition)
   {
      markModified();
      processDefinitions.add(processDefinition);
      defaultProcessDefinitionId = nextID("ProcessDefinition", defaultProcessDefinitionId, processDefinition.getId());
   }

   public void addToTriggerTypes(ITriggerType type)
   {
      markModified();
      triggerTypes.add(type);
   }

   public void removeFromTriggerTypes(ITriggerType type)
   {
      markModified();
      triggerTypes.remove(type);
   }

   public void addToApplicationTypes(IApplicationType type)
   {
      applicationTypes.add(type);
   }

   public void removeFromApplicationTypes(IApplicationType type)
   {
      applicationTypes.remove(type);
   }
   public void removeFromViews(org.eclipse.stardust.engine.api.model.IView view)
   {
      markModified();
      views.remove(view);
   }

   public String toString()
   {
      return "Model: " + getName();
   }

   public void addToLinkTypes(ILinkType linkType)
   {
      markModified();
      linkTypes.add(linkType);
   }

   /**
    * Retrieves all (predefined and user defined) link types for the model version
    */
   public Iterator getAllLinkTypes()
   {
      return linkTypes.iterator();
   }

   /**
    * Creates a user-defined link type for the model.
    */
   public ILinkType createLinkType(String name, Class firstClass,
         Class secondClass, String firstRole, String secondRole,
         CardinalityKey firstCardinality,
         CardinalityKey secondCardinality
         , ArrowKey firstArrowType, ArrowKey secondArrowType
         , ColorKey lineColor
         , LineKey lineType
         , boolean showLinkTypeName
         , boolean showRoleNames, int elementOID)
   {
      markModified();

      LinkTypeBean _linkType = new LinkTypeBean(name, firstClass,
            secondClass, firstRole, secondRole,
            firstCardinality, secondCardinality,
            firstArrowType,
            secondArrowType, lineColor,
            lineType, showLinkTypeName,
            showRoleNames);

      addToLinkTypes(_linkType);
      _linkType.register(elementOID);

      //    fireModelElementCreated(_linkType, this);

      return _linkType;
   }

   /**
    * Removes the link type <tt>linkType</tt>.
    * <p/>
    * Will throw an internal exception, if the link type is predefined.
    */
   public void removeFromLinkTypes(ILinkType linkType)
   {
      markModified();

      linkTypes.remove(linkType);
   }

   /**
    * Retrieves all link types whose first or second type is the class provided
    * by <tt>type</tt>.
    */
   public Iterator getAllLinkTypesForType(final Class type)
   {
      return new FilteringIterator(getAllLinkTypes(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return ((ILinkType) o).getFirstClass().isAssignableFrom(type)
                  || ((ILinkType) o).getSecondClass().isAssignableFrom(type);
         }
      });
   }

   public String getUniqueId()
   {
      return Long.toString(getModelOID());
   }


   public IEventActionType createEventActionType(String id, String name,
         boolean predefined, boolean processAction, boolean activityAction,
         int elementOID)
   {
      IEventActionType result = new EventActionTypeBean(id, name,
            predefined, processAction, activityAction);
      eventActionTypes.add(result);
      result.register(elementOID);
      return result;
   }

   public Iterator getAllModelers()
   {
      return participants.iterator(IModeler.class);
   }

   public Iterator getAllConditionalPerformers()
   {
      return participants.iterator(IConditionalPerformer.class);
   }

   @Deprecated
   public void setCarnotVersion(String version)
   {
      carnotVersion = null;
      if (StringUtils.isNotEmpty(version))
      {
         carnotVersion =  new Version(version);
      }
   }

   public void setCarnotVersion(Version version)
   {
      this.carnotVersion = version;
   }

   public Version getCarnotVersion()
   {
      return carnotVersion;
   }

   public IEventConditionType createEventConditionType(String id, String name,
         boolean predefined, EventType implementation,
         boolean processCondition, boolean activityCondition, int elementOID)
   {
      IEventConditionType result = new EventConditionTypeBean(id, name, predefined,
            implementation, processCondition, activityCondition);
      eventConditionTypes.add(result);
      result.register(elementOID);
      return result;
   }

   public IEventConditionType findEventConditionType(String id)
   {
      return (IEventConditionType) eventConditionTypes.findById(id);
   }

   public IEventActionType findEventActionType(String id)
   {
      return (IEventActionType) eventActionTypes.findById(id);
   }

   public Iterator getAllEventActionTypes()
   {
      return eventActionTypes.iterator();
   }

   public Scripting getScripting()
   {
      return scripting;
   }

   public void setScripting(Scripting scripting)
   {
      this.scripting = scripting;
   }
   
   public void addToExternalPackages(IExternalPackage externalPackage)
   {
      externalPackages.add(externalPackage);
   }

   public List<IExternalPackage> getExternalPackages()
   {
      return Collections.unmodifiableList(externalPackages);
   }

   public IExternalPackage findExternalPackage(String id)
   {
      return ModelUtils.findById(externalPackages, id);
   }
   
   public Set<IConfigurationVariableDefinition> getConfigurationVariableDefinitions()
   {
      Set<IConfigurationVariableDefinition> defs = CollectionUtils.newSet();

      boolean foundAttribute = true;
      int i = 0;
      while (foundAttribute)
      {
         String name = (String) getAttribute(IPP_VARIABLES + i + IPP_VARIABLES_NAME);
         if (name == null)
         {
            foundAttribute = false;
            continue;
         }
         String defaultValue = (String) getAttribute(IPP_VARIABLES + i
               + IPP_VARIABLES_DEFAULT_VALUE);
         String description = (String) getAttribute(IPP_VARIABLES + i
               + IPP_VARIABLES_DESCRIPTION);

         defs.add(new ConfigurationVariableDefinition(name, defaultValue, description, getModelOID()));
         i++;
      }
      return defs;
   }
   
   public Set<String> getConfigurationVariableReferences()
   {
      return Collections.unmodifiableSet(configurationVariableReferences);
   }
   
   public void setConfigurationVariableReferences(Set<String> configurationVariableReferences)
   {
      this.configurationVariableReferences = configurationVariableReferences; 
   }

   public IProcessDefinition getImplementingProcess(QName processId)
   {
      List<IProcessDefinition> list = implementations.get(processId);
      return list == null || list.isEmpty() ? null : list.get(0);
   }

   public List<IProcessDefinition> getAllImplementingProcesses(QName processId)
   {
      return implementations.get(processId);
   }

   public void setImplementations(Map<QName, List<IProcessDefinition>> trim)
   {
      implementations = trim;
   }

   public Set<QName> getImplementedInterfaces()
   {
      return implementations.keySet();
   }

   public IQualityAssurance createQualityAssurance()
   {
      qualityAssuranceBean = new QualityAssuranceBean();
      
      return qualityAssuranceBean;
   }

   public IQualityAssurance getQualityAssurance()
   {
      return qualityAssuranceBean;
   }
}