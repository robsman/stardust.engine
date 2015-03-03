
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.eclipse.stardust.engine.api.ws.query.ActivityQueryXto;
import org.eclipse.stardust.engine.api.ws.query.ProcessDefinitionQueryXto;
import org.eclipse.stardust.engine.api.ws.query.ProcessQueryXto;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.eclipse.stardust.engine.api.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Documents_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "documents");
    private final static QName _Document_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "document");
    private final static QName _Folder_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "folder");
    private final static QName _Note_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "note");
    private final static QName _Folders_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "folders");
    private final static QName _BpmFault_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "bpmFault");
    private final static QName _UpdateDocumentVersionInfo_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "versionInfo");
    private final static QName _UpdateDocumentContent_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "content");
    private final static QName _FindActivitiesQuery_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "query");
    private final static QName _ModifyDepartmentDescription_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "description");
    private final static QName _StartProcessForModelAttachments_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "attachments");
    private final static QName _CompleteActivityAndActivateNextActivate_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "activate");
    private final static QName _GetDaemonStatusResponseDeamons_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "deamons");
    private final static QName _GetActivityInDataDataIds_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "dataIds");
    private final static QName _DeployModelIgnoreWarnings_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "ignoreWarnings");
    private final static QName _DeployModelValidTo_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "validTo");
    private final static QName _DeployModelDisabled_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "disabled");
    private final static QName _DeployModelPredecessorOid_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "predecessorOid");
    private final static QName _DeployModelConfiguration_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "configuration");
    private final static QName _DeployModelComment_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "comment");
    private final static QName _DeployModelValidFrom_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "validFrom");
    private final static QName _GetProcessPropertiesPropertyIds_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "propertyIds");
    private final static QName _InvalidateUserRealmId_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "realmId");
    private final static QName _GetModelModelOid_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "modelOid");
    private final static QName _GetModelComputeAliveness_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "computeAliveness");
    private final static QName _InvalidateUserGroupUserGroupId_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "userGroupId");
    private final static QName _GetDaemonStatusDaemonParameters_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "daemonParameters");
    private final static QName _AbortActivityAbortScope_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "abortScope");
    private final static QName _GetConfigurationVariablesModelIds_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "modelIds");
    private final static QName _WriteLogEntryProcessOid_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "processOid");
    private final static QName _WriteLogEntryActivityOid_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "activityOid");
    private final static QName _GetDocumentTypesModelId_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api", "modelId");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.eclipse.stardust.engine.api.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetConfigurationVariablesResponse }
     * 
     */
    public GetConfigurationVariablesResponse createGetConfigurationVariablesResponse() {
        return new GetConfigurationVariablesResponse();
    }

    /**
     * Create an instance of {@link GetParticipant }
     * 
     */
    public GetParticipant createGetParticipant() {
        return new GetParticipant();
    }

    /**
     * Create an instance of {@link RepositoryMigrationReportXto }
     * 
     */
    public RepositoryMigrationReportXto createRepositoryMigrationReportXto() {
        return new RepositoryMigrationReportXto();
    }

    /**
     * Create an instance of {@link InstancePropertiesXto }
     * 
     */
    public InstancePropertiesXto createInstancePropertiesXto() {
        return new InstancePropertiesXto();
    }

    /**
     * Create an instance of {@link GetAllModelDescriptionsResponse }
     * 
     */
    public GetAllModelDescriptionsResponse createGetAllModelDescriptionsResponse() {
        return new GetAllModelDescriptionsResponse();
    }

    /**
     * Create an instance of {@link DeputyOptionsXto }
     * 
     */
    public DeputyOptionsXto createDeputyOptionsXto() {
        return new DeputyOptionsXto();
    }

    /**
     * Create an instance of {@link RolesXto }
     * 
     */
    public RolesXto createRolesXto() {
        return new RolesXto();
    }

    /**
     * Create an instance of {@link ProcessInterfaceXto }
     * 
     */
    public ProcessInterfaceXto createProcessInterfaceXto() {
        return new ProcessInterfaceXto();
    }

    /**
     * Create an instance of {@link GetDocumentTypesResponse }
     * 
     */
    public GetDocumentTypesResponse createGetDocumentTypesResponse() {
        return new GetDocumentTypesResponse();
    }

    /**
     * Create an instance of {@link ProcessDefinitionXto.ActivitiesXto }
     * 
     */
    public ProcessDefinitionXto.ActivitiesXto createProcessDefinitionXtoActivitiesXto() {
        return new ProcessDefinitionXto.ActivitiesXto();
    }

    /**
     * Create an instance of {@link GetFolders.FolderIdsXto }
     * 
     */
    public GetFolders.FolderIdsXto createGetFoldersFolderIdsXto() {
        return new GetFolders.FolderIdsXto();
    }

    /**
     * Create an instance of {@link GetPreferencesResponse }
     * 
     */
    public GetPreferencesResponse createGetPreferencesResponse() {
        return new GetPreferencesResponse();
    }

    /**
     * Create an instance of {@link FindProcessDefinitions }
     * 
     */
    public FindProcessDefinitions createFindProcessDefinitions() {
        return new FindProcessDefinitions();
    }

    /**
     * Create an instance of {@link CreateActivityEventBindingResponse }
     * 
     */
    public CreateActivityEventBindingResponse createCreateActivityEventBindingResponse() {
        return new CreateActivityEventBindingResponse();
    }

    /**
     * Create an instance of {@link EventActionDefinitionXto }
     * 
     */
    public EventActionDefinitionXto createEventActionDefinitionXto() {
        return new EventActionDefinitionXto();
    }

    /**
     * Create an instance of {@link MapXto }
     * 
     */
    public MapXto createMapXto() {
        return new MapXto();
    }

    /**
     * Create an instance of {@link FindWorklistResponse }
     * 
     */
    public FindWorklistResponse createFindWorklistResponse() {
        return new FindWorklistResponse();
    }

    /**
     * Create an instance of {@link DaemonsXto }
     * 
     */
    public DaemonsXto createDaemonsXto() {
        return new DaemonsXto();
    }

    /**
     * Create an instance of {@link HibernateActivity }
     * 
     */
    public HibernateActivity createHibernateActivity() {
        return new HibernateActivity();
    }

    /**
     * Create an instance of {@link BusinessObjectDefinitionXto }
     * 
     */
    public BusinessObjectDefinitionXto createBusinessObjectDefinitionXto() {
        return new BusinessObjectDefinitionXto();
    }

    /**
     * Create an instance of {@link GetProcess }
     * 
     */
    public GetProcess createGetProcess() {
        return new GetProcess();
    }

    /**
     * Create an instance of {@link ActivityDefinitionXto }
     * 
     */
    public ActivityDefinitionXto createActivityDefinitionXto() {
        return new ActivityDefinitionXto();
    }

    /**
     * Create an instance of {@link GetGlobalPermissionsResponse }
     * 
     */
    public GetGlobalPermissionsResponse createGetGlobalPermissionsResponse() {
        return new GetGlobalPermissionsResponse();
    }

    /**
     * Create an instance of {@link DepartmentXto }
     * 
     */
    public DepartmentXto createDepartmentXto() {
        return new DepartmentXto();
    }

    /**
     * Create an instance of {@link FindDocumentsResponse }
     * 
     */
    public FindDocumentsResponse createFindDocumentsResponse() {
        return new FindDocumentsResponse();
    }

    /**
     * Create an instance of {@link ModelParticipantInfoXto }
     * 
     */
    public ModelParticipantInfoXto createModelParticipantInfoXto() {
        return new ModelParticipantInfoXto();
    }

    /**
     * Create an instance of {@link DropUserRealm }
     * 
     */
    public DropUserRealm createDropUserRealm() {
        return new DropUserRealm();
    }

    /**
     * Create an instance of {@link HistoricalStateXto }
     * 
     */
    public HistoricalStateXto createHistoricalStateXto() {
        return new HistoricalStateXto();
    }

    /**
     * Create an instance of {@link XmlValueXto }
     * 
     */
    public XmlValueXto createXmlValueXto() {
        return new XmlValueXto();
    }

    /**
     * Create an instance of {@link CleanupRuntimeAndModelsResponse }
     * 
     */
    public CleanupRuntimeAndModelsResponse createCleanupRuntimeAndModelsResponse() {
        return new CleanupRuntimeAndModelsResponse();
    }

    /**
     * Create an instance of {@link DeployModelResponse }
     * 
     */
    public DeployModelResponse createDeployModelResponse() {
        return new DeployModelResponse();
    }

    /**
     * Create an instance of {@link CreateDocument }
     * 
     */
    public CreateDocument createCreateDocument() {
        return new CreateDocument();
    }

    /**
     * Create an instance of {@link FindDepartmentResponse }
     * 
     */
    public FindDepartmentResponse createFindDepartmentResponse() {
        return new FindDepartmentResponse();
    }

    /**
     * Create an instance of {@link DeleteBusinessObjectInstance }
     * 
     */
    public DeleteBusinessObjectInstance createDeleteBusinessObjectInstance() {
        return new DeleteBusinessObjectInstance();
    }

    /**
     * Create an instance of {@link GetProcessResponse }
     * 
     */
    public GetProcessResponse createGetProcessResponse() {
        return new GetProcessResponse();
    }

    /**
     * Create an instance of {@link PropertiesXto }
     * 
     */
    public PropertiesXto createPropertiesXto() {
        return new PropertiesXto();
    }

    /**
     * Create an instance of {@link AbortActivity }
     * 
     */
    public AbortActivity createAbortActivity() {
        return new AbortActivity();
    }

    /**
     * Create an instance of {@link CreateActivityEventBinding }
     * 
     */
    public CreateActivityEventBinding createCreateActivityEventBinding() {
        return new CreateActivityEventBinding();
    }

    /**
     * Create an instance of {@link CompleteActivityResponse }
     * 
     */
    public CompleteActivityResponse createCompleteActivityResponse() {
        return new CompleteActivityResponse();
    }

    /**
     * Create an instance of {@link GetAllBusinessObjects }
     * 
     */
    public GetAllBusinessObjects createGetAllBusinessObjects() {
        return new GetAllBusinessObjects();
    }

    /**
     * Create an instance of {@link FindLogEntriesResponse }
     * 
     */
    public FindLogEntriesResponse createFindLogEntriesResponse() {
        return new FindLogEntriesResponse();
    }

    /**
     * Create an instance of {@link GetDocumentVersions }
     * 
     */
    public GetDocumentVersions createGetDocumentVersions() {
        return new GetDocumentVersions();
    }

    /**
     * Create an instance of {@link FindUserGroups }
     * 
     */
    public FindUserGroups createFindUserGroups() {
        return new FindUserGroups();
    }

    /**
     * Create an instance of {@link RemoveDocument }
     * 
     */
    public RemoveDocument createRemoveDocument() {
        return new RemoveDocument();
    }

    /**
     * Create an instance of {@link FindModels }
     * 
     */
    public FindModels createFindModels() {
        return new FindModels();
    }

    /**
     * Create an instance of {@link ConditionalPerformerInfoXto }
     * 
     */
    public ConditionalPerformerInfoXto createConditionalPerformerInfoXto() {
        return new ConditionalPerformerInfoXto();
    }

    /**
     * Create an instance of {@link ResetPasswordResponse }
     * 
     */
    public ResetPasswordResponse createResetPasswordResponse() {
        return new ResetPasswordResponse();
    }

    /**
     * Create an instance of {@link QualityAssuranceResultXto }
     * 
     */
    public QualityAssuranceResultXto createQualityAssuranceResultXto() {
        return new QualityAssuranceResultXto();
    }

    /**
     * Create an instance of {@link UserGroupsXto }
     * 
     */
    public UserGroupsXto createUserGroupsXto() {
        return new UserGroupsXto();
    }

    /**
     * Create an instance of {@link DocumentsXto }
     * 
     */
    public DocumentsXto createDocumentsXto() {
        return new DocumentsXto();
    }

    /**
     * Create an instance of {@link VariableDefinitionsXto }
     * 
     */
    public VariableDefinitionsXto createVariableDefinitionsXto() {
        return new VariableDefinitionsXto();
    }

    /**
     * Create an instance of {@link RuntimePermissionsXto }
     * 
     */
    public RuntimePermissionsXto createRuntimePermissionsXto() {
        return new RuntimePermissionsXto();
    }

    /**
     * Create an instance of {@link RequestDocumentContentUpload }
     * 
     */
    public RequestDocumentContentUpload createRequestDocumentContentUpload() {
        return new RequestDocumentContentUpload();
    }

    /**
     * Create an instance of {@link FormalParametersXto }
     * 
     */
    public FormalParametersXto createFormalParametersXto() {
        return new FormalParametersXto();
    }

    /**
     * Create an instance of {@link DocumentTypeXto }
     * 
     */
    public DocumentTypeXto createDocumentTypeXto() {
        return new DocumentTypeXto();
    }

    /**
     * Create an instance of {@link GetActivityInData }
     * 
     */
    public GetActivityInData createGetActivityInData() {
        return new GetActivityInData();
    }

    /**
     * Create an instance of {@link FindAllDepartments }
     * 
     */
    public FindAllDepartments createFindAllDepartments() {
        return new FindAllDepartments();
    }

    /**
     * Create an instance of {@link ResetPassword }
     * 
     */
    public ResetPassword createResetPassword() {
        return new ResetPassword();
    }

    /**
     * Create an instance of {@link EventActionTypeDefinitionXto }
     * 
     */
    public EventActionTypeDefinitionXto createEventActionTypeDefinitionXto() {
        return new EventActionTypeDefinitionXto();
    }

    /**
     * Create an instance of {@link SetGlobalPermissions }
     * 
     */
    public SetGlobalPermissions createSetGlobalPermissions() {
        return new SetGlobalPermissions();
    }

    /**
     * Create an instance of {@link InvalidateUserGroup }
     * 
     */
    public InvalidateUserGroup createInvalidateUserGroup() {
        return new InvalidateUserGroup();
    }

    /**
     * Create an instance of {@link ParticipantInfoBaseXto }
     * 
     */
    public ParticipantInfoBaseXto createParticipantInfoBaseXto() {
        return new ParticipantInfoBaseXto();
    }

    /**
     * Create an instance of {@link ParameterMappingXto }
     * 
     */
    public ParameterMappingXto createParameterMappingXto() {
        return new ParameterMappingXto();
    }

    /**
     * Create an instance of {@link ActivateActivityAndGetInData }
     * 
     */
    public ActivateActivityAndGetInData createActivateActivityAndGetInData() {
        return new ActivateActivityAndGetInData();
    }

    /**
     * Create an instance of {@link WriteLogEntryResponse }
     * 
     */
    public WriteLogEntryResponse createWriteLogEntryResponse() {
        return new WriteLogEntryResponse();
    }

    /**
     * Create an instance of {@link ModelElementXto }
     * 
     */
    public ModelElementXto createModelElementXto() {
        return new ModelElementXto();
    }

    /**
     * Create an instance of {@link RecoverProcessInstancesResponse }
     * 
     */
    public RecoverProcessInstancesResponse createRecoverProcessInstancesResponse() {
        return new RecoverProcessInstancesResponse();
    }

    /**
     * Create an instance of {@link StringListXto }
     * 
     */
    public StringListXto createStringListXto() {
        return new StringListXto();
    }

    /**
     * Create an instance of {@link GetDocumentContent }
     * 
     */
    public GetDocumentContent createGetDocumentContent() {
        return new GetDocumentContent();
    }

    /**
     * Create an instance of {@link GetActivityEventBinding }
     * 
     */
    public GetActivityEventBinding createGetActivityEventBinding() {
        return new GetActivityEventBinding();
    }

    /**
     * Create an instance of {@link ActivityDefinitionXto.InteractionContextsXto }
     * 
     */
    public ActivityDefinitionXto.InteractionContextsXto createActivityDefinitionXtoInteractionContextsXto() {
        return new ActivityDefinitionXto.InteractionContextsXto();
    }

    /**
     * Create an instance of {@link DefaultParticipantXto }
     * 
     */
    public DefaultParticipantXto createDefaultParticipantXto() {
        return new DefaultParticipantXto();
    }

    /**
     * Create an instance of {@link BpmFaultXto }
     * 
     */
    public BpmFaultXto createBpmFaultXto() {
        return new BpmFaultXto();
    }

    /**
     * Create an instance of {@link BusinessObjectDefinitionsXto }
     * 
     */
    public BusinessObjectDefinitionsXto createBusinessObjectDefinitionsXto() {
        return new BusinessObjectDefinitionsXto();
    }

    /**
     * Create an instance of {@link GetActivity }
     * 
     */
    public GetActivity createGetActivity() {
        return new GetActivity();
    }

    /**
     * Create an instance of {@link GetAllParticipantsResponse }
     * 
     */
    public GetAllParticipantsResponse createGetAllParticipantsResponse() {
        return new GetAllParticipantsResponse();
    }

    /**
     * Create an instance of {@link ParticipantXto }
     * 
     */
    public ParticipantXto createParticipantXto() {
        return new ParticipantXto();
    }

    /**
     * Create an instance of {@link QualityAssuranceInfoXto }
     * 
     */
    public QualityAssuranceInfoXto createQualityAssuranceInfoXto() {
        return new QualityAssuranceInfoXto();
    }

    /**
     * Create an instance of {@link ActivateNextActivityForProcess }
     * 
     */
    public ActivateNextActivityForProcess createActivateNextActivityForProcess() {
        return new ActivateNextActivityForProcess();
    }

    /**
     * Create an instance of {@link UserQueryResultXto }
     * 
     */
    public UserQueryResultXto createUserQueryResultXto() {
        return new UserQueryResultXto();
    }

    /**
     * Create an instance of {@link DataFlowXto }
     * 
     */
    public DataFlowXto createDataFlowXto() {
        return new DataFlowXto();
    }

    /**
     * Create an instance of {@link ModelReconfigurationInfoXto }
     * 
     */
    public ModelReconfigurationInfoXto createModelReconfigurationInfoXto() {
        return new ModelReconfigurationInfoXto();
    }

    /**
     * Create an instance of {@link EventHandlerTypeDefinitionXto }
     * 
     */
    public EventHandlerTypeDefinitionXto createEventHandlerTypeDefinitionXto() {
        return new EventHandlerTypeDefinitionXto();
    }

    /**
     * Create an instance of {@link ModelXto.GlobalVariablesXto }
     * 
     */
    public ModelXto.GlobalVariablesXto createModelXtoGlobalVariablesXto() {
        return new ModelXto.GlobalVariablesXto();
    }

    /**
     * Create an instance of {@link DeleteProcesses }
     * 
     */
    public DeleteProcesses createDeleteProcesses() {
        return new DeleteProcesses();
    }

    /**
     * Create an instance of {@link StartProcessResponse }
     * 
     */
    public StartProcessResponse createStartProcessResponse() {
        return new StartProcessResponse();
    }

    /**
     * Create an instance of {@link SchemaTypeXto }
     * 
     */
    public SchemaTypeXto createSchemaTypeXto() {
        return new SchemaTypeXto();
    }

    /**
     * Create an instance of {@link GetFolder }
     * 
     */
    public GetFolder createGetFolder() {
        return new GetFolder();
    }

    /**
     * Create an instance of {@link OrganizationXto }
     * 
     */
    public OrganizationXto createOrganizationXto() {
        return new OrganizationXto();
    }

    /**
     * Create an instance of {@link ModifyUser }
     * 
     */
    public ModifyUser createModifyUser() {
        return new ModifyUser();
    }

    /**
     * Create an instance of {@link ProcessInstancesXto }
     * 
     */
    public ProcessInstancesXto createProcessInstancesXto() {
        return new ProcessInstancesXto();
    }

    /**
     * Create an instance of {@link AccessPointsXto }
     * 
     */
    public AccessPointsXto createAccessPointsXto() {
        return new AccessPointsXto();
    }

    /**
     * Create an instance of {@link DeleteProcessesResponse }
     * 
     */
    public DeleteProcessesResponse createDeleteProcessesResponse() {
        return new DeleteProcessesResponse();
    }

    /**
     * Create an instance of {@link ModifyDeputyResponse }
     * 
     */
    public ModifyDeputyResponse createModifyDeputyResponse() {
        return new ModifyDeputyResponse();
    }

    /**
     * Create an instance of {@link CreateDepartmentResponse }
     * 
     */
    public CreateDepartmentResponse createCreateDepartmentResponse() {
        return new CreateDepartmentResponse();
    }

    /**
     * Create an instance of {@link ProcessEventBindingXto }
     * 
     */
    public ProcessEventBindingXto createProcessEventBindingXto() {
        return new ProcessEventBindingXto();
    }

    /**
     * Create an instance of {@link GetStartableProcessDefinitions }
     * 
     */
    public GetStartableProcessDefinitions createGetStartableProcessDefinitions() {
        return new GetStartableProcessDefinitions();
    }

    /**
     * Create an instance of {@link AccessControlPolicyXto }
     * 
     */
    public AccessControlPolicyXto createAccessControlPolicyXto() {
        return new AccessControlPolicyXto();
    }

    /**
     * Create an instance of {@link OverwriteModel }
     * 
     */
    public OverwriteModel createOverwriteModel() {
        return new OverwriteModel();
    }

    /**
     * Create an instance of {@link UpdateDocument }
     * 
     */
    public UpdateDocument createUpdateDocument() {
        return new UpdateDocument();
    }

    /**
     * Create an instance of {@link FindActivities }
     * 
     */
    public FindActivities createFindActivities() {
        return new FindActivities();
    }

    /**
     * Create an instance of {@link DocumentXto }
     * 
     */
    public DocumentXto createDocumentXto() {
        return new DocumentXto();
    }

    /**
     * Create an instance of {@link OrganizationsXto }
     * 
     */
    public OrganizationsXto createOrganizationsXto() {
        return new OrganizationsXto();
    }

    /**
     * Create an instance of {@link DocumentTypesXto }
     * 
     */
    public DocumentTypesXto createDocumentTypesXto() {
        return new DocumentTypesXto();
    }

    /**
     * Create an instance of {@link ProcessDefinitionXto }
     * 
     */
    public ProcessDefinitionXto createProcessDefinitionXto() {
        return new ProcessDefinitionXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceXto }
     * 
     */
    public ProcessInstanceXto createProcessInstanceXto() {
        return new ProcessInstanceXto();
    }

    /**
     * Create an instance of {@link ForceCompletionResponse }
     * 
     */
    public ForceCompletionResponse createForceCompletionResponse() {
        return new ForceCompletionResponse();
    }

    /**
     * Create an instance of {@link InconsistencyXto }
     * 
     */
    public InconsistencyXto createInconsistencyXto() {
        return new InconsistencyXto();
    }

    /**
     * Create an instance of {@link BindRepositoryResponse }
     * 
     */
    public BindRepositoryResponse createBindRepositoryResponse() {
        return new BindRepositoryResponse();
    }

    /**
     * Create an instance of {@link AttributesXto }
     * 
     */
    public AttributesXto createAttributesXto() {
        return new AttributesXto();
    }

    /**
     * Create an instance of {@link GetSchemaDefinition }
     * 
     */
    public GetSchemaDefinition createGetSchemaDefinition() {
        return new GetSchemaDefinition();
    }

    /**
     * Create an instance of {@link ModelDescriptionsXto }
     * 
     */
    public ModelDescriptionsXto createModelDescriptionsXto() {
        return new ModelDescriptionsXto();
    }

    /**
     * Create an instance of {@link PreferencesXto }
     * 
     */
    public PreferencesXto createPreferencesXto() {
        return new PreferencesXto();
    }

    /**
     * Create an instance of {@link GetUserResponse }
     * 
     */
    public GetUserResponse createGetUserResponse() {
        return new GetUserResponse();
    }

    /**
     * Create an instance of {@link RepositoryProviderInfosXto }
     * 
     */
    public RepositoryProviderInfosXto createRepositoryProviderInfosXto() {
        return new RepositoryProviderInfosXto();
    }

    /**
     * Create an instance of {@link StartDaemonResponse }
     * 
     */
    public StartDaemonResponse createStartDaemonResponse() {
        return new StartDaemonResponse();
    }

    /**
     * Create an instance of {@link DelegateActivityResponse }
     * 
     */
    public DelegateActivityResponse createDelegateActivityResponse() {
        return new DelegateActivityResponse();
    }

    /**
     * Create an instance of {@link BindRepository }
     * 
     */
    public BindRepository createBindRepository() {
        return new BindRepository();
    }

    /**
     * Create an instance of {@link CreateUserRealm }
     * 
     */
    public CreateUserRealm createCreateUserRealm() {
        return new CreateUserRealm();
    }

    /**
     * Create an instance of {@link TriggersXto }
     * 
     */
    public TriggersXto createTriggersXto() {
        return new TriggersXto();
    }

    /**
     * Create an instance of {@link GetModelAsXMLResponse }
     * 
     */
    public GetModelAsXMLResponse createGetModelAsXMLResponse() {
        return new GetModelAsXMLResponse();
    }

    /**
     * Create an instance of {@link CreateFolder }
     * 
     */
    public CreateFolder createCreateFolder() {
        return new CreateFolder();
    }

    /**
     * Create an instance of {@link CreateUserResponse }
     * 
     */
    public CreateUserResponse createCreateUserResponse() {
        return new CreateUserResponse();
    }

    /**
     * Create an instance of {@link GetAuditTrailHealthReportResponse }
     * 
     */
    public GetAuditTrailHealthReportResponse createGetAuditTrailHealthReportResponse() {
        return new GetAuditTrailHealthReportResponse();
    }

    /**
     * Create an instance of {@link RepositoryCapabilitiesXto }
     * 
     */
    public RepositoryCapabilitiesXto createRepositoryCapabilitiesXto() {
        return new RepositoryCapabilitiesXto();
    }

    /**
     * Create an instance of {@link GetProcessProperties.PropertyIdsXto }
     * 
     */
    public GetProcessProperties.PropertyIdsXto createGetProcessPropertiesPropertyIdsXto() {
        return new GetProcessProperties.PropertyIdsXto();
    }

    /**
     * Create an instance of {@link GetDeputies }
     * 
     */
    public GetDeputies createGetDeputies() {
        return new GetDeputies();
    }

    /**
     * Create an instance of {@link PreferencesMapXto }
     * 
     */
    public PreferencesMapXto createPreferencesMapXto() {
        return new PreferencesMapXto();
    }

    /**
     * Create an instance of {@link ActivateActivity }
     * 
     */
    public ActivateActivity createActivateActivity() {
        return new ActivateActivity();
    }

    /**
     * Create an instance of {@link AbortProcessInstance }
     * 
     */
    public AbortProcessInstance createAbortProcessInstance() {
        return new AbortProcessInstance();
    }

    /**
     * Create an instance of {@link GetDocumentResponse }
     * 
     */
    public GetDocumentResponse createGetDocumentResponse() {
        return new GetDocumentResponse();
    }

    /**
     * Create an instance of {@link ActivateNextActivityResponse }
     * 
     */
    public ActivateNextActivityResponse createActivateNextActivityResponse() {
        return new ActivateNextActivityResponse();
    }

    /**
     * Create an instance of {@link SetPolicy }
     * 
     */
    public SetPolicy createSetPolicy() {
        return new SetPolicy();
    }

    /**
     * Create an instance of {@link GetDaemonStatusResponse }
     * 
     */
    public GetDaemonStatusResponse createGetDaemonStatusResponse() {
        return new GetDaemonStatusResponse();
    }

    /**
     * Create an instance of {@link WorklistXto.SharedWorklistsXto }
     * 
     */
    public WorklistXto.SharedWorklistsXto createWorklistXtoSharedWorklistsXto() {
        return new WorklistXto.SharedWorklistsXto();
    }

    /**
     * Create an instance of {@link FolderXto }
     * 
     */
    public FolderXto createFolderXto() {
        return new FolderXto();
    }

    /**
     * Create an instance of {@link CreateBusinessObjectInstanceResponse }
     * 
     */
    public CreateBusinessObjectInstanceResponse createCreateBusinessObjectInstanceResponse() {
        return new CreateBusinessObjectInstanceResponse();
    }

    /**
     * Create an instance of {@link RemoveProcessEventBinding }
     * 
     */
    public RemoveProcessEventBinding createRemoveProcessEventBinding() {
        return new RemoveProcessEventBinding();
    }

    /**
     * Create an instance of {@link ProcessInstanceLinkXto }
     * 
     */
    public ProcessInstanceLinkXto createProcessInstanceLinkXto() {
        return new ProcessInstanceLinkXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceLinkTypeXto }
     * 
     */
    public ProcessInstanceLinkTypeXto createProcessInstanceLinkTypeXto() {
        return new ProcessInstanceLinkTypeXto();
    }

    /**
     * Create an instance of {@link RemoveDocumentResponse }
     * 
     */
    public RemoveDocumentResponse createRemoveDocumentResponse() {
        return new RemoveDocumentResponse();
    }

    /**
     * Create an instance of {@link GetModelDescriptionResponse }
     * 
     */
    public GetModelDescriptionResponse createGetModelDescriptionResponse() {
        return new GetModelDescriptionResponse();
    }

    /**
     * Create an instance of {@link RecoverProcessInstances.OidsXto }
     * 
     */
    public RecoverProcessInstances.OidsXto createRecoverProcessInstancesOidsXto() {
        return new RecoverProcessInstances.OidsXto();
    }

    /**
     * Create an instance of {@link UserGroupInfoXto }
     * 
     */
    public UserGroupInfoXto createUserGroupInfoXto() {
        return new UserGroupInfoXto();
    }

    /**
     * Create an instance of {@link GetPreferences }
     * 
     */
    public GetPreferences createGetPreferences() {
        return new GetPreferences();
    }

    /**
     * Create an instance of {@link GetAllAliveModelDescriptions }
     * 
     */
    public GetAllAliveModelDescriptions createGetAllAliveModelDescriptions() {
        return new GetAllAliveModelDescriptions();
    }

    /**
     * Create an instance of {@link ModelsQueryResultXto }
     * 
     */
    public ModelsQueryResultXto createModelsQueryResultXto() {
        return new ModelsQueryResultXto();
    }

    /**
     * Create an instance of {@link AccessControlPoliciesXto }
     * 
     */
    public AccessControlPoliciesXto createAccessControlPoliciesXto() {
        return new AccessControlPoliciesXto();
    }

    /**
     * Create an instance of {@link GetUserRealms }
     * 
     */
    public GetUserRealms createGetUserRealms() {
        return new GetUserRealms();
    }

    /**
     * Create an instance of {@link GetAllBusinessObjectsResponse }
     * 
     */
    public GetAllBusinessObjectsResponse createGetAllBusinessObjectsResponse() {
        return new GetAllBusinessObjectsResponse();
    }

    /**
     * Create an instance of {@link SetDefaultRepository }
     * 
     */
    public SetDefaultRepository createSetDefaultRepository() {
        return new SetDefaultRepository();
    }

    /**
     * Create an instance of {@link TypeDeclarationXto }
     * 
     */
    public TypeDeclarationXto createTypeDeclarationXto() {
        return new TypeDeclarationXto();
    }

    /**
     * Create an instance of {@link ActivateNextActivity }
     * 
     */
    public ActivateNextActivity createActivateNextActivity() {
        return new ActivateNextActivity();
    }

    /**
     * Create an instance of {@link BusinessObjectsXto }
     * 
     */
    public BusinessObjectsXto createBusinessObjectsXto() {
        return new BusinessObjectsXto();
    }

    /**
     * Create an instance of {@link DaemonXto }
     * 
     */
    public DaemonXto createDaemonXto() {
        return new DaemonXto();
    }

    /**
     * Create an instance of {@link StartProcess }
     * 
     */
    public StartProcess createStartProcess() {
        return new StartProcess();
    }

    /**
     * Create an instance of {@link GetSessionUserResponse }
     * 
     */
    public GetSessionUserResponse createGetSessionUserResponse() {
        return new GetSessionUserResponse();
    }

    /**
     * Create an instance of {@link GetProcessEventBinding }
     * 
     */
    public GetProcessEventBinding createGetProcessEventBinding() {
        return new GetProcessEventBinding();
    }

    /**
     * Create an instance of {@link StartProcessForModelResponse }
     * 
     */
    public StartProcessForModelResponse createStartProcessForModelResponse() {
        return new StartProcessForModelResponse();
    }

    /**
     * Create an instance of {@link FindVariableDefinitions }
     * 
     */
    public FindVariableDefinitions createFindVariableDefinitions() {
        return new FindVariableDefinitions();
    }

    /**
     * Create an instance of {@link LeaveCase }
     * 
     */
    public LeaveCase createLeaveCase() {
        return new LeaveCase();
    }

    /**
     * Create an instance of {@link OrganizationInfoXto }
     * 
     */
    public OrganizationInfoXto createOrganizationInfoXto() {
        return new OrganizationInfoXto();
    }

    /**
     * Create an instance of {@link XpdlTypeXto }
     * 
     */
    public XpdlTypeXto createXpdlTypeXto() {
        return new XpdlTypeXto();
    }

    /**
     * Create an instance of {@link FindWorklist }
     * 
     */
    public FindWorklist createFindWorklist() {
        return new FindWorklist();
    }

    /**
     * Create an instance of {@link DataPathsXto }
     * 
     */
    public DataPathsXto createDataPathsXto() {
        return new DataPathsXto();
    }

    /**
     * Create an instance of {@link FindModelsResponse }
     * 
     */
    public FindModelsResponse createFindModelsResponse() {
        return new FindModelsResponse();
    }

    /**
     * Create an instance of {@link GetRepositoryProviderInfos }
     * 
     */
    public GetRepositoryProviderInfos createGetRepositoryProviderInfos() {
        return new GetRepositoryProviderInfos();
    }

    /**
     * Create an instance of {@link DeployModel }
     * 
     */
    public DeployModel createDeployModel() {
        return new DeployModel();
    }

    /**
     * Create an instance of {@link OverwriteModelResponse }
     * 
     */
    public OverwriteModelResponse createOverwriteModelResponse() {
        return new OverwriteModelResponse();
    }

    /**
     * Create an instance of {@link FlushCaches }
     * 
     */
    public FlushCaches createFlushCaches() {
        return new FlushCaches();
    }

    /**
     * Create an instance of {@link GetPrivilegesResponse }
     * 
     */
    public GetPrivilegesResponse createGetPrivilegesResponse() {
        return new GetPrivilegesResponse();
    }

    /**
     * Create an instance of {@link GetAllProcessDefinitions }
     * 
     */
    public GetAllProcessDefinitions createGetAllProcessDefinitions() {
        return new GetAllProcessDefinitions();
    }

    /**
     * Create an instance of {@link CompleteActivity }
     * 
     */
    public CompleteActivity createCompleteActivity() {
        return new CompleteActivity();
    }

    /**
     * Create an instance of {@link SpawnSubprocessInstances }
     * 
     */
    public SpawnSubprocessInstances createSpawnSubprocessInstances() {
        return new SpawnSubprocessInstances();
    }

    /**
     * Create an instance of {@link MergeCasesResponse }
     * 
     */
    public MergeCasesResponse createMergeCasesResponse() {
        return new MergeCasesResponse();
    }

    /**
     * Create an instance of {@link MergeCases }
     * 
     */
    public MergeCases createMergeCases() {
        return new MergeCases();
    }

    /**
     * Create an instance of {@link CleanupRuntimeAndModels }
     * 
     */
    public CleanupRuntimeAndModels createCleanupRuntimeAndModels() {
        return new CleanupRuntimeAndModels();
    }

    /**
     * Create an instance of {@link DaemonParameterXto }
     * 
     */
    public DaemonParameterXto createDaemonParameterXto() {
        return new DaemonParameterXto();
    }

    /**
     * Create an instance of {@link UnbindRepositoryResponse }
     * 
     */
    public UnbindRepositoryResponse createUnbindRepositoryResponse() {
        return new UnbindRepositoryResponse();
    }

    /**
     * Create an instance of {@link ModifyLoginUserResponse }
     * 
     */
    public ModifyLoginUserResponse createModifyLoginUserResponse() {
        return new ModifyLoginUserResponse();
    }

    /**
     * Create an instance of {@link SuspendActivity }
     * 
     */
    public SuspendActivity createSuspendActivity() {
        return new SuspendActivity();
    }

    /**
     * Create an instance of {@link DeleteModel }
     * 
     */
    public DeleteModel createDeleteModel() {
        return new DeleteModel();
    }

    /**
     * Create an instance of {@link RepositoryInstanceInfosXto }
     * 
     */
    public RepositoryInstanceInfosXto createRepositoryInstanceInfosXto() {
        return new RepositoryInstanceInfosXto();
    }

    /**
     * Create an instance of {@link PermissionsXto }
     * 
     */
    public PermissionsXto createPermissionsXto() {
        return new PermissionsXto();
    }

    /**
     * Create an instance of {@link WorklistXto }
     * 
     */
    public WorklistXto createWorklistXto() {
        return new WorklistXto();
    }

    /**
     * Create an instance of {@link DataFlowsXto }
     * 
     */
    public DataFlowsXto createDataFlowsXto() {
        return new DataFlowsXto();
    }

    /**
     * Create an instance of {@link FindDocuments }
     * 
     */
    public FindDocuments createFindDocuments() {
        return new FindDocuments();
    }

    /**
     * Create an instance of {@link RemoveDeputy }
     * 
     */
    public RemoveDeputy createRemoveDeputy() {
        return new RemoveDeputy();
    }

    /**
     * Create an instance of {@link ModifyLoginUser }
     * 
     */
    public ModifyLoginUser createModifyLoginUser() {
        return new ModifyLoginUser();
    }

    /**
     * Create an instance of {@link GrantsXto }
     * 
     */
    public GrantsXto createGrantsXto() {
        return new GrantsXto();
    }

    /**
     * Create an instance of {@link GetModelResponse }
     * 
     */
    public GetModelResponse createGetModelResponse() {
        return new GetModelResponse();
    }

    /**
     * Create an instance of {@link ModelReconfigurationInfoListXto }
     * 
     */
    public ModelReconfigurationInfoListXto createModelReconfigurationInfoListXto() {
        return new ModelReconfigurationInfoListXto();
    }

    /**
     * Create an instance of {@link CreateCase }
     * 
     */
    public CreateCase createCreateCase() {
        return new CreateCase();
    }

    /**
     * Create an instance of {@link FindProcessDefinitionsResponse }
     * 
     */
    public FindProcessDefinitionsResponse createFindProcessDefinitionsResponse() {
        return new FindProcessDefinitionsResponse();
    }

    /**
     * Create an instance of {@link SavePreferencesResponse }
     * 
     */
    public SavePreferencesResponse createSavePreferencesResponse() {
        return new SavePreferencesResponse();
    }

    /**
     * Create an instance of {@link LogEntryXto }
     * 
     */
    public LogEntryXto createLogEntryXto() {
        return new LogEntryXto();
    }

    /**
     * Create an instance of {@link HistoricalEventsXto }
     * 
     */
    public HistoricalEventsXto createHistoricalEventsXto() {
        return new HistoricalEventsXto();
    }

    /**
     * Create an instance of {@link OidListXto }
     * 
     */
    public OidListXto createOidListXto() {
        return new OidListXto();
    }

    /**
     * Create an instance of {@link GetSchemaDefinitionResponse }
     * 
     */
    public GetSchemaDefinitionResponse createGetSchemaDefinitionResponse() {
        return new GetSchemaDefinitionResponse();
    }

    /**
     * Create an instance of {@link SpawnSubprocessInstancesResponse }
     * 
     */
    public SpawnSubprocessInstancesResponse createSpawnSubprocessInstancesResponse() {
        return new SpawnSubprocessInstancesResponse();
    }

    /**
     * Create an instance of {@link SaveConfigurationVariablesResponse }
     * 
     */
    public SaveConfigurationVariablesResponse createSaveConfigurationVariablesResponse() {
        return new SaveConfigurationVariablesResponse();
    }

    /**
     * Create an instance of {@link UpdateBusinessObjectInstanceResponse }
     * 
     */
    public UpdateBusinessObjectInstanceResponse createUpdateBusinessObjectInstanceResponse() {
        return new UpdateBusinessObjectInstanceResponse();
    }

    /**
     * Create an instance of {@link RoleInfoXto }
     * 
     */
    public RoleInfoXto createRoleInfoXto() {
        return new RoleInfoXto();
    }

    /**
     * Create an instance of {@link ApplicationXto }
     * 
     */
    public ApplicationXto createApplicationXto() {
        return new ApplicationXto();
    }

    /**
     * Create an instance of {@link SpawnPeerProcessInstance }
     * 
     */
    public SpawnPeerProcessInstance createSpawnPeerProcessInstance() {
        return new SpawnPeerProcessInstance();
    }

    /**
     * Create an instance of {@link ModifyUserGroupResponse }
     * 
     */
    public ModifyUserGroupResponse createModifyUserGroupResponse() {
        return new ModifyUserGroupResponse();
    }

    /**
     * Create an instance of {@link SetPasswordRulesResponse }
     * 
     */
    public SetPasswordRulesResponse createSetPasswordRulesResponse() {
        return new SetPasswordRulesResponse();
    }

    /**
     * Create an instance of {@link CreateBusinessObjectInstance }
     * 
     */
    public CreateBusinessObjectInstance createCreateBusinessObjectInstance() {
        return new CreateBusinessObjectInstance();
    }

    /**
     * Create an instance of {@link IsInternalAuthenticationResponse }
     * 
     */
    public IsInternalAuthenticationResponse createIsInternalAuthenticationResponse() {
        return new IsInternalAuthenticationResponse();
    }

    /**
     * Create an instance of {@link GetRepositoryProviderInfosResponse }
     * 
     */
    public GetRepositoryProviderInfosResponse createGetRepositoryProviderInfosResponse() {
        return new GetRepositoryProviderInfosResponse();
    }

    /**
     * Create an instance of {@link DepartmentInfoXto }
     * 
     */
    public DepartmentInfoXto createDepartmentInfoXto() {
        return new DepartmentInfoXto();
    }

    /**
     * Create an instance of {@link DeputiesXto }
     * 
     */
    public DeputiesXto createDeputiesXto() {
        return new DeputiesXto();
    }

    /**
     * Create an instance of {@link SaveConfigurationVariables }
     * 
     */
    public SaveConfigurationVariables createSaveConfigurationVariables() {
        return new SaveConfigurationVariables();
    }

    /**
     * Create an instance of {@link SpawnPeerProcessInstanceResponse }
     * 
     */
    public SpawnPeerProcessInstanceResponse createSpawnPeerProcessInstanceResponse() {
        return new SpawnPeerProcessInstanceResponse();
    }

    /**
     * Create an instance of {@link VersionDocument }
     * 
     */
    public VersionDocument createVersionDocument() {
        return new VersionDocument();
    }

    /**
     * Create an instance of {@link SetPasswordRules }
     * 
     */
    public SetPasswordRules createSetPasswordRules() {
        return new SetPasswordRules();
    }

    /**
     * Create an instance of {@link ActivateActivityResponse }
     * 
     */
    public ActivateActivityResponse createActivateActivityResponse() {
        return new ActivateActivityResponse();
    }

    /**
     * Create an instance of {@link StartProcessForModel }
     * 
     */
    public StartProcessForModel createStartProcessForModel() {
        return new StartProcessForModel();
    }

    /**
     * Create an instance of {@link ImplementationProcessesMapXto }
     * 
     */
    public ImplementationProcessesMapXto createImplementationProcessesMapXto() {
        return new ImplementationProcessesMapXto();
    }

    /**
     * Create an instance of {@link ActivityInstanceAttributesXto }
     * 
     */
    public ActivityInstanceAttributesXto createActivityInstanceAttributesXto() {
        return new ActivityInstanceAttributesXto();
    }

    /**
     * Create an instance of {@link GetPasswordRules }
     * 
     */
    public GetPasswordRules createGetPasswordRules() {
        return new GetPasswordRules();
    }

    /**
     * Create an instance of {@link UserGroupQueryResultXto }
     * 
     */
    public UserGroupQueryResultXto createUserGroupQueryResultXto() {
        return new UserGroupQueryResultXto();
    }

    /**
     * Create an instance of {@link AddDeputy }
     * 
     */
    public AddDeputy createAddDeputy() {
        return new AddDeputy();
    }

    /**
     * Create an instance of {@link ActivityInstancesXto }
     * 
     */
    public ActivityInstancesXto createActivityInstancesXto() {
        return new ActivityInstancesXto();
    }

    /**
     * Create an instance of {@link CreateDocumentsResponse }
     * 
     */
    public CreateDocumentsResponse createCreateDocumentsResponse() {
        return new CreateDocumentsResponse();
    }

    /**
     * Create an instance of {@link ProcessDefinitionQueryResultXto }
     * 
     */
    public ProcessDefinitionQueryResultXto createProcessDefinitionQueryResultXto() {
        return new ProcessDefinitionQueryResultXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceQueryResultXto }
     * 
     */
    public ProcessInstanceQueryResultXto createProcessInstanceQueryResultXto() {
        return new ProcessInstanceQueryResultXto();
    }

    /**
     * Create an instance of {@link GetUser }
     * 
     */
    public GetUser createGetUser() {
        return new GetUser();
    }

    /**
     * Create an instance of {@link RemoveDepartmentResponse }
     * 
     */
    public RemoveDepartmentResponse createRemoveDepartmentResponse() {
        return new RemoveDepartmentResponse();
    }

    /**
     * Create an instance of {@link FindUserGroupsResponse }
     * 
     */
    public FindUserGroupsResponse createFindUserGroupsResponse() {
        return new FindUserGroupsResponse();
    }

    /**
     * Create an instance of {@link UserXto }
     * 
     */
    public UserXto createUserXto() {
        return new UserXto();
    }

    /**
     * Create an instance of {@link GeneratePasswordResetToken }
     * 
     */
    public GeneratePasswordResetToken createGeneratePasswordResetToken() {
        return new GeneratePasswordResetToken();
    }

    /**
     * Create an instance of {@link DeploymentInfoXto.WarningsXto }
     * 
     */
    public DeploymentInfoXto.WarningsXto createDeploymentInfoXtoWarningsXto() {
        return new DeploymentInfoXto.WarningsXto();
    }

    /**
     * Create an instance of {@link CreateUserGroupResponse }
     * 
     */
    public CreateUserGroupResponse createCreateUserGroupResponse() {
        return new CreateUserGroupResponse();
    }

    /**
     * Create an instance of {@link QueryResultXto }
     * 
     */
    public QueryResultXto createQueryResultXto() {
        return new QueryResultXto();
    }

    /**
     * Create an instance of {@link WriteLogEntry }
     * 
     */
    public WriteLogEntry createWriteLogEntry() {
        return new WriteLogEntry();
    }

    /**
     * Create an instance of {@link MigrateRepository }
     * 
     */
    public MigrateRepository createMigrateRepository() {
        return new MigrateRepository();
    }

    /**
     * Create an instance of {@link ModifyUserResponse }
     * 
     */
    public ModifyUserResponse createModifyUserResponse() {
        return new ModifyUserResponse();
    }

    /**
     * Create an instance of {@link GeneratePasswordResetTokenResponse }
     * 
     */
    public GeneratePasswordResetTokenResponse createGeneratePasswordResetTokenResponse() {
        return new GeneratePasswordResetTokenResponse();
    }

    /**
     * Create an instance of {@link EventBindingBaseXto }
     * 
     */
    public EventBindingBaseXto createEventBindingBaseXto() {
        return new EventBindingBaseXto();
    }

    /**
     * Create an instance of {@link JoinCase }
     * 
     */
    public JoinCase createJoinCase() {
        return new JoinCase();
    }

    /**
     * Create an instance of {@link CreateProcessEventBindingResponse }
     * 
     */
    public CreateProcessEventBindingResponse createCreateProcessEventBindingResponse() {
        return new CreateProcessEventBindingResponse();
    }

    /**
     * Create an instance of {@link FindActivitiesResponse }
     * 
     */
    public FindActivitiesResponse createFindActivitiesResponse() {
        return new FindActivitiesResponse();
    }

    /**
     * Create an instance of {@link NoteXto }
     * 
     */
    public NoteXto createNoteXto() {
        return new NoteXto();
    }

    /**
     * Create an instance of {@link GetAuditTrailHealthReport }
     * 
     */
    public GetAuditTrailHealthReport createGetAuditTrailHealthReport() {
        return new GetAuditTrailHealthReport();
    }

    /**
     * Create an instance of {@link FindDepartment }
     * 
     */
    public FindDepartment createFindDepartment() {
        return new FindDepartment();
    }

    /**
     * Create an instance of {@link InputDocumentXto }
     * 
     */
    public InputDocumentXto createInputDocumentXto() {
        return new InputDocumentXto();
    }

    /**
     * Create an instance of {@link GetUserGroupResponse }
     * 
     */
    public GetUserGroupResponse createGetUserGroupResponse() {
        return new GetUserGroupResponse();
    }

    /**
     * Create an instance of {@link GetActivityInData.DataIdsXto }
     * 
     */
    public GetActivityInData.DataIdsXto createGetActivityInDataDataIdsXto() {
        return new GetActivityInData.DataIdsXto();
    }

    /**
     * Create an instance of {@link GetFolders }
     * 
     */
    public GetFolders createGetFolders() {
        return new GetFolders();
    }

    /**
     * Create an instance of {@link WorklistXto.UserWorklistXto }
     * 
     */
    public WorklistXto.UserWorklistXto createWorklistXtoUserWorklistXto() {
        return new WorklistXto.UserWorklistXto();
    }

    /**
     * Create an instance of {@link ModifyDepartmentResponse }
     * 
     */
    public ModifyDepartmentResponse createModifyDepartmentResponse() {
        return new ModifyDepartmentResponse();
    }

    /**
     * Create an instance of {@link DaemonParametersXto }
     * 
     */
    public DaemonParametersXto createDaemonParametersXto() {
        return new DaemonParametersXto();
    }

    /**
     * Create an instance of {@link ModelsXto }
     * 
     */
    public ModelsXto createModelsXto() {
        return new ModelsXto();
    }

    /**
     * Create an instance of {@link ModifyDeputy }
     * 
     */
    public ModifyDeputy createModifyDeputy() {
        return new ModifyDeputy();
    }

    /**
     * Create an instance of {@link ModifyUserGroup }
     * 
     */
    public ModifyUserGroup createModifyUserGroup() {
        return new ModifyUserGroup();
    }

    /**
     * Create an instance of {@link CreateCaseResponse }
     * 
     */
    public CreateCaseResponse createCreateCaseResponse() {
        return new CreateCaseResponse();
    }

    /**
     * Create an instance of {@link StopDaemonResponse }
     * 
     */
    public StopDaemonResponse createStopDaemonResponse() {
        return new StopDaemonResponse();
    }

    /**
     * Create an instance of {@link GrantsXto.GrantXto }
     * 
     */
    public GrantsXto.GrantXto createGrantsXtoGrantXto() {
        return new GrantsXto.GrantXto();
    }

    /**
     * Create an instance of {@link EventActionDefinitionsXto }
     * 
     */
    public EventActionDefinitionsXto createEventActionDefinitionsXto() {
        return new EventActionDefinitionsXto();
    }

    /**
     * Create an instance of {@link SuspendActivityResponse }
     * 
     */
    public SuspendActivityResponse createSuspendActivityResponse() {
        return new SuspendActivityResponse();
    }

    /**
     * Create an instance of {@link GetPasswordRulesResponse }
     * 
     */
    public GetPasswordRulesResponse createGetPasswordRulesResponse() {
        return new GetPasswordRulesResponse();
    }

    /**
     * Create an instance of {@link GetProcessPropertiesResponse }
     * 
     */
    public GetProcessPropertiesResponse createGetProcessPropertiesResponse() {
        return new GetProcessPropertiesResponse();
    }

    /**
     * Create an instance of {@link PreferenceEntryXto.ValueListXto }
     * 
     */
    public PreferenceEntryXto.ValueListXto createPreferenceEntryXtoValueListXto() {
        return new PreferenceEntryXto.ValueListXto();
    }

    /**
     * Create an instance of {@link ExternalReferenceXto }
     * 
     */
    public ExternalReferenceXto createExternalReferenceXto() {
        return new ExternalReferenceXto();
    }

    /**
     * Create an instance of {@link GetDocument }
     * 
     */
    public GetDocument createGetDocument() {
        return new GetDocument();
    }

    /**
     * Create an instance of {@link GetFolderResponse }
     * 
     */
    public GetFolderResponse createGetFolderResponse() {
        return new GetFolderResponse();
    }

    /**
     * Create an instance of {@link CompleteActivityAndActivateNextResponse }
     * 
     */
    public CompleteActivityAndActivateNextResponse createCompleteActivityAndActivateNextResponse() {
        return new CompleteActivityAndActivateNextResponse();
    }

    /**
     * Create an instance of {@link PermissionStatesXto.PermissionStateXto }
     * 
     */
    public PermissionStatesXto.PermissionStateXto createPermissionStatesXtoPermissionStateXto() {
        return new PermissionStatesXto.PermissionStateXto();
    }

    /**
     * Create an instance of {@link ImplementationProcessesMapEntryXto }
     * 
     */
    public ImplementationProcessesMapEntryXto createImplementationProcessesMapEntryXto() {
        return new ImplementationProcessesMapEntryXto();
    }

    /**
     * Create an instance of {@link RuntimePermissionsMapXto }
     * 
     */
    public RuntimePermissionsMapXto createRuntimePermissionsMapXto() {
        return new RuntimePermissionsMapXto();
    }

    /**
     * Create an instance of {@link GetProcessDefinitionResponse }
     * 
     */
    public GetProcessDefinitionResponse createGetProcessDefinitionResponse() {
        return new GetProcessDefinitionResponse();
    }

    /**
     * Create an instance of {@link GetActivityResponse }
     * 
     */
    public GetActivityResponse createGetActivityResponse() {
        return new GetActivityResponse();
    }

    /**
     * Create an instance of {@link PreferencesListXto }
     * 
     */
    public PreferencesListXto createPreferencesListXto() {
        return new PreferencesListXto();
    }

    /**
     * Create an instance of {@link ProcessSpawnInfosXto }
     * 
     */
    public ProcessSpawnInfosXto createProcessSpawnInfosXto() {
        return new ProcessSpawnInfosXto();
    }

    /**
     * Create an instance of {@link FoldersXto }
     * 
     */
    public FoldersXto createFoldersXto() {
        return new FoldersXto();
    }

    /**
     * Create an instance of {@link FindFolders }
     * 
     */
    public FindFolders createFindFolders() {
        return new FindFolders();
    }

    /**
     * Create an instance of {@link GetModelDescription }
     * 
     */
    public GetModelDescription createGetModelDescription() {
        return new GetModelDescription();
    }

    /**
     * Create an instance of {@link IsInternalAuthentication }
     * 
     */
    public IsInternalAuthentication createIsInternalAuthentication() {
        return new IsInternalAuthentication();
    }

    /**
     * Create an instance of {@link DynamicParticipantInfoXto }
     * 
     */
    public DynamicParticipantInfoXto createDynamicParticipantInfoXto() {
        return new DynamicParticipantInfoXto();
    }

    /**
     * Create an instance of {@link PreferenceEntryXto }
     * 
     */
    public PreferenceEntryXto createPreferenceEntryXto() {
        return new PreferenceEntryXto();
    }

    /**
     * Create an instance of {@link JoinProcessInstanceResponse }
     * 
     */
    public JoinProcessInstanceResponse createJoinProcessInstanceResponse() {
        return new JoinProcessInstanceResponse();
    }

    /**
     * Create an instance of {@link GetProcessProperties }
     * 
     */
    public GetProcessProperties createGetProcessProperties() {
        return new GetProcessProperties();
    }

    /**
     * Create an instance of {@link GetGlobalPermissions }
     * 
     */
    public GetGlobalPermissions createGetGlobalPermissions() {
        return new GetGlobalPermissions();
    }

    /**
     * Create an instance of {@link GetAllAliveModelDescriptionsResponse }
     * 
     */
    public GetAllAliveModelDescriptionsResponse createGetAllAliveModelDescriptionsResponse() {
        return new GetAllAliveModelDescriptionsResponse();
    }

    /**
     * Create an instance of {@link GetUsersBeingDeputyFor }
     * 
     */
    public GetUsersBeingDeputyFor createGetUsersBeingDeputyFor() {
        return new GetUsersBeingDeputyFor();
    }

    /**
     * Create an instance of {@link BusinessObjectXto }
     * 
     */
    public BusinessObjectXto createBusinessObjectXto() {
        return new BusinessObjectXto();
    }

    /**
     * Create an instance of {@link QualityAssuranceCodeXto }
     * 
     */
    public QualityAssuranceCodeXto createQualityAssuranceCodeXto() {
        return new QualityAssuranceCodeXto();
    }

    /**
     * Create an instance of {@link RemoveFolderResponse }
     * 
     */
    public RemoveFolderResponse createRemoveFolderResponse() {
        return new RemoveFolderResponse();
    }

    /**
     * Create an instance of {@link WorklistXto.SharedWorklistsXto.SharedWorklistXto }
     * 
     */
    public WorklistXto.SharedWorklistsXto.SharedWorklistXto createWorklistXtoSharedWorklistsXtoSharedWorklistXto() {
        return new WorklistXto.SharedWorklistsXto.SharedWorklistXto();
    }

    /**
     * Create an instance of {@link SetProcessPropertiesResponse }
     * 
     */
    public SetProcessPropertiesResponse createSetProcessPropertiesResponse() {
        return new SetProcessPropertiesResponse();
    }

    /**
     * Create an instance of {@link GetAllParticipants }
     * 
     */
    public GetAllParticipants createGetAllParticipants() {
        return new GetAllParticipants();
    }

    /**
     * Create an instance of {@link FlushCachesResponse }
     * 
     */
    public FlushCachesResponse createFlushCachesResponse() {
        return new FlushCachesResponse();
    }

    /**
     * Create an instance of {@link RoleXto }
     * 
     */
    public RoleXto createRoleXto() {
        return new RoleXto();
    }

    /**
     * Create an instance of {@link ParticipantsXto }
     * 
     */
    public ParticipantsXto createParticipantsXto() {
        return new ParticipantsXto();
    }

    /**
     * Create an instance of {@link DeleteModelResponse }
     * 
     */
    public DeleteModelResponse createDeleteModelResponse() {
        return new DeleteModelResponse();
    }

    /**
     * Create an instance of {@link DeployedModelDescriptionXto }
     * 
     */
    public DeployedModelDescriptionXto createDeployedModelDescriptionXto() {
        return new DeployedModelDescriptionXto();
    }

    /**
     * Create an instance of {@link DocumentInfoXto }
     * 
     */
    public DocumentInfoXto createDocumentInfoXto() {
        return new DocumentInfoXto();
    }

    /**
     * Create an instance of {@link GetDepartment }
     * 
     */
    public GetDepartment createGetDepartment() {
        return new GetDepartment();
    }

    /**
     * Create an instance of {@link CreateUser }
     * 
     */
    public CreateUser createCreateUser() {
        return new CreateUser();
    }

    /**
     * Create an instance of {@link TypeDeclarationsXto }
     * 
     */
    public TypeDeclarationsXto createTypeDeclarationsXto() {
        return new TypeDeclarationsXto();
    }

    /**
     * Create an instance of {@link RecoverRuntimeEnvironment }
     * 
     */
    public RecoverRuntimeEnvironment createRecoverRuntimeEnvironment() {
        return new RecoverRuntimeEnvironment();
    }

    /**
     * Create an instance of {@link GetDocumentTypes }
     * 
     */
    public GetDocumentTypes createGetDocumentTypes() {
        return new GetDocumentTypes();
    }

    /**
     * Create an instance of {@link GetParticipantResponse }
     * 
     */
    public GetParticipantResponse createGetParticipantResponse() {
        return new GetParticipantResponse();
    }

    /**
     * Create an instance of {@link GetDefaultRepositoryResponse }
     * 
     */
    public GetDefaultRepositoryResponse createGetDefaultRepositoryResponse() {
        return new GetDefaultRepositoryResponse();
    }

    /**
     * Create an instance of {@link CreateDepartment }
     * 
     */
    public CreateDepartment createCreateDepartment() {
        return new CreateDepartment();
    }

    /**
     * Create an instance of {@link FindUsers }
     * 
     */
    public FindUsers createFindUsers() {
        return new FindUsers();
    }

    /**
     * Create an instance of {@link UpdateFolderResponse }
     * 
     */
    public UpdateFolderResponse createUpdateFolderResponse() {
        return new UpdateFolderResponse();
    }

    /**
     * Create an instance of {@link ProcessInstanceLinksXto }
     * 
     */
    public ProcessInstanceLinksXto createProcessInstanceLinksXto() {
        return new ProcessInstanceLinksXto();
    }

    /**
     * Create an instance of {@link GetProcessEventBindingResponse }
     * 
     */
    public GetProcessEventBindingResponse createGetProcessEventBindingResponse() {
        return new GetProcessEventBindingResponse();
    }

    /**
     * Create an instance of {@link CreateDocuments }
     * 
     */
    public CreateDocuments createCreateDocuments() {
        return new CreateDocuments();
    }

    /**
     * Create an instance of {@link GetSessionUser }
     * 
     */
    public GetSessionUser createGetSessionUser() {
        return new GetSessionUser();
    }

    /**
     * Create an instance of {@link RemoveFolder }
     * 
     */
    public RemoveFolder createRemoveFolder() {
        return new RemoveFolder();
    }

    /**
     * Create an instance of {@link ParticipantInfoXto }
     * 
     */
    public ParticipantInfoXto createParticipantInfoXto() {
        return new ParticipantInfoXto();
    }

    /**
     * Create an instance of {@link FindProcesses }
     * 
     */
    public FindProcesses createFindProcesses() {
        return new FindProcesses();
    }

    /**
     * Create an instance of {@link InvalidateUserResponse }
     * 
     */
    public InvalidateUserResponse createInvalidateUserResponse() {
        return new InvalidateUserResponse();
    }

    /**
     * Create an instance of {@link UnbindActionDefinitionsXto }
     * 
     */
    public UnbindActionDefinitionsXto createUnbindActionDefinitionsXto() {
        return new UnbindActionDefinitionsXto();
    }

    /**
     * Create an instance of {@link GetAllDocumentsResponse }
     * 
     */
    public GetAllDocumentsResponse createGetAllDocumentsResponse() {
        return new GetAllDocumentsResponse();
    }

    /**
     * Create an instance of {@link CreateUserGroup }
     * 
     */
    public CreateUserGroup createCreateUserGroup() {
        return new CreateUserGroup();
    }

    /**
     * Create an instance of {@link AccessControlEntriesXto }
     * 
     */
    public AccessControlEntriesXto createAccessControlEntriesXto() {
        return new AccessControlEntriesXto();
    }

    /**
     * Create an instance of {@link ImplementationDescriptionXto }
     * 
     */
    public ImplementationDescriptionXto createImplementationDescriptionXto() {
        return new ImplementationDescriptionXto();
    }

    /**
     * Create an instance of {@link GetDocumentsResponse }
     * 
     */
    public GetDocumentsResponse createGetDocumentsResponse() {
        return new GetDocumentsResponse();
    }

    /**
     * Create an instance of {@link GetProcessDefinition }
     * 
     */
    public GetProcessDefinition createGetProcessDefinition() {
        return new GetProcessDefinition();
    }

    /**
     * Create an instance of {@link PasswordRulesXto }
     * 
     */
    public PasswordRulesXto createPasswordRulesXto() {
        return new PasswordRulesXto();
    }

    /**
     * Create an instance of {@link SetDefaultRepositoryResponse }
     * 
     */
    public SetDefaultRepositoryResponse createSetDefaultRepositoryResponse() {
        return new SetDefaultRepositoryResponse();
    }

    /**
     * Create an instance of {@link UpdateFolder }
     * 
     */
    public UpdateFolder createUpdateFolder() {
        return new UpdateFolder();
    }

    /**
     * Create an instance of {@link GetRepositoryInstanceInfos }
     * 
     */
    public GetRepositoryInstanceInfos createGetRepositoryInstanceInfos() {
        return new GetRepositoryInstanceInfos();
    }

    /**
     * Create an instance of {@link RepositoryProviderInfoXto }
     * 
     */
    public RepositoryProviderInfoXto createRepositoryProviderInfoXto() {
        return new RepositoryProviderInfoXto();
    }

    /**
     * Create an instance of {@link GetActivityInDataResponse }
     * 
     */
    public GetActivityInDataResponse createGetActivityInDataResponse() {
        return new GetActivityInDataResponse();
    }

    /**
     * Create an instance of {@link ActivityInstanceXto }
     * 
     */
    public ActivityInstanceXto createActivityInstanceXto() {
        return new ActivityInstanceXto();
    }

    /**
     * Create an instance of {@link StopDaemon }
     * 
     */
    public StopDaemon createStopDaemon() {
        return new StopDaemon();
    }

    /**
     * Create an instance of {@link ActivityEventBindingXto }
     * 
     */
    public ActivityEventBindingXto createActivityEventBindingXto() {
        return new ActivityEventBindingXto();
    }

    /**
     * Create an instance of {@link HistoricalEventXto }
     * 
     */
    public HistoricalEventXto createHistoricalEventXto() {
        return new HistoricalEventXto();
    }

    /**
     * Create an instance of {@link DeploymentInfoXto.ErrorsXto }
     * 
     */
    public DeploymentInfoXto.ErrorsXto createDeploymentInfoXtoErrorsXto() {
        return new DeploymentInfoXto.ErrorsXto();
    }

    /**
     * Create an instance of {@link SetPolicyResponse }
     * 
     */
    public SetPolicyResponse createSetPolicyResponse() {
        return new SetPolicyResponse();
    }

    /**
     * Create an instance of {@link FindAllDepartmentsResponse }
     * 
     */
    public FindAllDepartmentsResponse createFindAllDepartmentsResponse() {
        return new FindAllDepartmentsResponse();
    }

    /**
     * Create an instance of {@link IsInternalAuthorization }
     * 
     */
    public IsInternalAuthorization createIsInternalAuthorization() {
        return new IsInternalAuthorization();
    }

    /**
     * Create an instance of {@link GetUsersBeingDeputyForResponse }
     * 
     */
    public GetUsersBeingDeputyForResponse createGetUsersBeingDeputyForResponse() {
        return new GetUsersBeingDeputyForResponse();
    }

    /**
     * Create an instance of {@link DelegateCase }
     * 
     */
    public DelegateCase createDelegateCase() {
        return new DelegateCase();
    }

    /**
     * Create an instance of {@link ForceSuspendResponse }
     * 
     */
    public ForceSuspendResponse createForceSuspendResponse() {
        return new ForceSuspendResponse();
    }

    /**
     * Create an instance of {@link CompleteActivityAndActivateNext }
     * 
     */
    public CompleteActivityAndActivateNext createCompleteActivityAndActivateNext() {
        return new CompleteActivityAndActivateNext();
    }

    /**
     * Create an instance of {@link GetDocuments }
     * 
     */
    public GetDocuments createGetDocuments() {
        return new GetDocuments();
    }

    /**
     * Create an instance of {@link MigrateRepositoryResponse }
     * 
     */
    public MigrateRepositoryResponse createMigrateRepositoryResponse() {
        return new MigrateRepositoryResponse();
    }

    /**
     * Create an instance of {@link UnbindRepository }
     * 
     */
    public UnbindRepository createUnbindRepository() {
        return new UnbindRepository();
    }

    /**
     * Create an instance of {@link FindVariableDefinitionsResponse }
     * 
     */
    public FindVariableDefinitionsResponse createFindVariableDefinitionsResponse() {
        return new FindVariableDefinitionsResponse();
    }

    /**
     * Create an instance of {@link BusinessObjectValueXto }
     * 
     */
    public BusinessObjectValueXto createBusinessObjectValueXto() {
        return new BusinessObjectValueXto();
    }

    /**
     * Create an instance of {@link GetAllDocuments }
     * 
     */
    public GetAllDocuments createGetAllDocuments() {
        return new GetAllDocuments();
    }

    /**
     * Create an instance of {@link GetAllProcessDefinitionsResponse }
     * 
     */
    public GetAllProcessDefinitionsResponse createGetAllProcessDefinitionsResponse() {
        return new GetAllProcessDefinitionsResponse();
    }

    /**
     * Create an instance of {@link AbortProcessInstanceResponse }
     * 
     */
    public AbortProcessInstanceResponse createAbortProcessInstanceResponse() {
        return new AbortProcessInstanceResponse();
    }

    /**
     * Create an instance of {@link DocumentVersionInfoXto }
     * 
     */
    public DocumentVersionInfoXto createDocumentVersionInfoXto() {
        return new DocumentVersionInfoXto();
    }

    /**
     * Create an instance of {@link GetDefaultRepository }
     * 
     */
    public GetDefaultRepository createGetDefaultRepository() {
        return new GetDefaultRepository();
    }

    /**
     * Create an instance of {@link JoinCaseResponse }
     * 
     */
    public JoinCaseResponse createJoinCaseResponse() {
        return new JoinCaseResponse();
    }

    /**
     * Create an instance of {@link GetDocumentVersionsResponse }
     * 
     */
    public GetDocumentVersionsResponse createGetDocumentVersionsResponse() {
        return new GetDocumentVersionsResponse();
    }

    /**
     * Create an instance of {@link AbortActivityResponse }
     * 
     */
    public AbortActivityResponse createAbortActivityResponse() {
        return new AbortActivityResponse();
    }

    /**
     * Create an instance of {@link HistoricalStatesXto }
     * 
     */
    public HistoricalStatesXto createHistoricalStatesXto() {
        return new HistoricalStatesXto();
    }

    /**
     * Create an instance of {@link LogEntriesXto }
     * 
     */
    public LogEntriesXto createLogEntriesXto() {
        return new LogEntriesXto();
    }

    /**
     * Create an instance of {@link UserRealmXto }
     * 
     */
    public UserRealmXto createUserRealmXto() {
        return new UserRealmXto();
    }

    /**
     * Create an instance of {@link RemoveActivityEventBinding }
     * 
     */
    public RemoveActivityEventBinding createRemoveActivityEventBinding() {
        return new RemoveActivityEventBinding();
    }

    /**
     * Create an instance of {@link GetPolicies }
     * 
     */
    public GetPolicies createGetPolicies() {
        return new GetPolicies();
    }

    /**
     * Create an instance of {@link ResourceInfoXto }
     * 
     */
    public ResourceInfoXto createResourceInfoXto() {
        return new ResourceInfoXto();
    }

    /**
     * Create an instance of {@link InputDocumentsXto }
     * 
     */
    public InputDocumentsXto createInputDocumentsXto() {
        return new InputDocumentsXto();
    }

    /**
     * Create an instance of {@link ModelParticipantInfosXto }
     * 
     */
    public ModelParticipantInfosXto createModelParticipantInfosXto() {
        return new ModelParticipantInfosXto();
    }

    /**
     * Create an instance of {@link UserGroupXto }
     * 
     */
    public UserGroupXto createUserGroupXto() {
        return new UserGroupXto();
    }

    /**
     * Create an instance of {@link JoinProcessInstance }
     * 
     */
    public JoinProcessInstance createJoinProcessInstance() {
        return new JoinProcessInstance();
    }

    /**
     * Create an instance of {@link UpdateBusinessObjectInstance }
     * 
     */
    public UpdateBusinessObjectInstance createUpdateBusinessObjectInstance() {
        return new UpdateBusinessObjectInstance();
    }

    /**
     * Create an instance of {@link CleanupRuntime }
     * 
     */
    public CleanupRuntime createCleanupRuntime() {
        return new CleanupRuntime();
    }

    /**
     * Create an instance of {@link TriggerXto }
     * 
     */
    public TriggerXto createTriggerXto() {
        return new TriggerXto();
    }

    /**
     * Create an instance of {@link SavePreferences }
     * 
     */
    public SavePreferences createSavePreferences() {
        return new SavePreferences();
    }

    /**
     * Create an instance of {@link RuntimePermissionsEntryXto }
     * 
     */
    public RuntimePermissionsEntryXto createRuntimePermissionsEntryXto() {
        return new RuntimePermissionsEntryXto();
    }

    /**
     * Create an instance of {@link LeaveCaseResponse }
     * 
     */
    public LeaveCaseResponse createLeaveCaseResponse() {
        return new LeaveCaseResponse();
    }

    /**
     * Create an instance of {@link PermissionsXto.PermissionXto }
     * 
     */
    public PermissionsXto.PermissionXto createPermissionsXtoPermissionXto() {
        return new PermissionsXto.PermissionXto();
    }

    /**
     * Create an instance of {@link ModelXto }
     * 
     */
    public ModelXto createModelXto() {
        return new ModelXto();
    }

    /**
     * Create an instance of {@link GetPrivileges }
     * 
     */
    public GetPrivileges createGetPrivileges() {
        return new GetPrivileges();
    }

    /**
     * Create an instance of {@link UpdateDocumentResponse }
     * 
     */
    public UpdateDocumentResponse createUpdateDocumentResponse() {
        return new UpdateDocumentResponse();
    }

    /**
     * Create an instance of {@link ModelDescriptionXto }
     * 
     */
    public ModelDescriptionXto createModelDescriptionXto() {
        return new ModelDescriptionXto();
    }

    /**
     * Create an instance of {@link RequestDocumentContentDownload }
     * 
     */
    public RequestDocumentContentDownload createRequestDocumentContentDownload() {
        return new RequestDocumentContentDownload();
    }

    /**
     * Create an instance of {@link SetProcessInstancePriorityResponse }
     * 
     */
    public SetProcessInstancePriorityResponse createSetProcessInstancePriorityResponse() {
        return new SetProcessInstancePriorityResponse();
    }

    /**
     * Create an instance of {@link RecoverProcessInstances }
     * 
     */
    public RecoverProcessInstances createRecoverProcessInstances() {
        return new RecoverProcessInstances();
    }

    /**
     * Create an instance of {@link RequestDocumentContentDownloadResponse }
     * 
     */
    public RequestDocumentContentDownloadResponse createRequestDocumentContentDownloadResponse() {
        return new RequestDocumentContentDownloadResponse();
    }

    /**
     * Create an instance of {@link ConfigurationVariablesXto }
     * 
     */
    public ConfigurationVariablesXto createConfigurationVariablesXto() {
        return new ConfigurationVariablesXto();
    }

    /**
     * Create an instance of {@link AttributeXto }
     * 
     */
    public AttributeXto createAttributeXto() {
        return new AttributeXto();
    }

    /**
     * Create an instance of {@link FormalParameterXto }
     * 
     */
    public FormalParameterXto createFormalParameterXto() {
        return new FormalParameterXto();
    }

    /**
     * Create an instance of {@link RepositoryInstanceInfoXto }
     * 
     */
    public RepositoryInstanceInfoXto createRepositoryInstanceInfoXto() {
        return new RepositoryInstanceInfoXto();
    }

    /**
     * Create an instance of {@link VariableDefinitionXto }
     * 
     */
    public VariableDefinitionXto createVariableDefinitionXto() {
        return new VariableDefinitionXto();
    }

    /**
     * Create an instance of {@link GetUserGroup }
     * 
     */
    public GetUserGroup createGetUserGroup() {
        return new GetUserGroup();
    }

    /**
     * Create an instance of {@link ForceSuspend }
     * 
     */
    public ForceSuspend createForceSuspend() {
        return new ForceSuspend();
    }

    /**
     * Create an instance of {@link RecoverRuntimeEnvironmentResponse }
     * 
     */
    public RecoverRuntimeEnvironmentResponse createRecoverRuntimeEnvironmentResponse() {
        return new RecoverRuntimeEnvironmentResponse();
    }

    /**
     * Create an instance of {@link InvalidateUser }
     * 
     */
    public InvalidateUser createInvalidateUser() {
        return new InvalidateUser();
    }

    /**
     * Create an instance of {@link UserInfoXto }
     * 
     */
    public UserInfoXto createUserInfoXto() {
        return new UserInfoXto();
    }

    /**
     * Create an instance of {@link PrivilegesXto }
     * 
     */
    public PrivilegesXto createPrivilegesXto() {
        return new PrivilegesXto();
    }

    /**
     * Create an instance of {@link RemoveActivityEventBindingResponse }
     * 
     */
    public RemoveActivityEventBindingResponse createRemoveActivityEventBindingResponse() {
        return new RemoveActivityEventBindingResponse();
    }

    /**
     * Create an instance of {@link AccessControlEntryXto }
     * 
     */
    public AccessControlEntryXto createAccessControlEntryXto() {
        return new AccessControlEntryXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceDetailsOptionsXto }
     * 
     */
    public ProcessInstanceDetailsOptionsXto createProcessInstanceDetailsOptionsXto() {
        return new ProcessInstanceDetailsOptionsXto();
    }

    /**
     * Create an instance of {@link BusinessObjectValuesXto }
     * 
     */
    public BusinessObjectValuesXto createBusinessObjectValuesXto() {
        return new BusinessObjectValuesXto();
    }

    /**
     * Create an instance of {@link BindActionDefinitionsXto }
     * 
     */
    public BindActionDefinitionsXto createBindActionDefinitionsXto() {
        return new BindActionDefinitionsXto();
    }

    /**
     * Create an instance of {@link FindProcessesResponse }
     * 
     */
    public FindProcessesResponse createFindProcessesResponse() {
        return new FindProcessesResponse();
    }

    /**
     * Create an instance of {@link GetRepositoryInstanceInfosResponse }
     * 
     */
    public GetRepositoryInstanceInfosResponse createGetRepositoryInstanceInfosResponse() {
        return new GetRepositoryInstanceInfosResponse();
    }

    /**
     * Create an instance of {@link DataPathXto }
     * 
     */
    public DataPathXto createDataPathXto() {
        return new DataPathXto();
    }

    /**
     * Create an instance of {@link ModelParticipantXto }
     * 
     */
    public ModelParticipantXto createModelParticipantXto() {
        return new ModelParticipantXto();
    }

    /**
     * Create an instance of {@link ModelXto.ProcessesXto }
     * 
     */
    public ModelXto.ProcessesXto createModelXtoProcessesXto() {
        return new ModelXto.ProcessesXto();
    }

    /**
     * Create an instance of {@link GetConfigurationVariables }
     * 
     */
    public GetConfigurationVariables createGetConfigurationVariables() {
        return new GetConfigurationVariables();
    }

    /**
     * Create an instance of {@link DocumentTypeResultsXto.DocumentTypeResultXto }
     * 
     */
    public DocumentTypeResultsXto.DocumentTypeResultXto createDocumentTypeResultsXtoDocumentTypeResultXto() {
        return new DocumentTypeResultsXto.DocumentTypeResultXto();
    }

    /**
     * Create an instance of {@link EventHandlerDefinitionsXto }
     * 
     */
    public EventHandlerDefinitionsXto createEventHandlerDefinitionsXto() {
        return new EventHandlerDefinitionsXto();
    }

    /**
     * Create an instance of {@link IsInternalAuthorizationResponse }
     * 
     */
    public IsInternalAuthorizationResponse createIsInternalAuthorizationResponse() {
        return new IsInternalAuthorizationResponse();
    }

    /**
     * Create an instance of {@link CleanupRuntimeResponse }
     * 
     */
    public CleanupRuntimeResponse createCleanupRuntimeResponse() {
        return new CleanupRuntimeResponse();
    }

    /**
     * Create an instance of {@link GetActivityEventBindingResponse }
     * 
     */
    public GetActivityEventBindingResponse createGetActivityEventBindingResponse() {
        return new GetActivityEventBindingResponse();
    }

    /**
     * Create an instance of {@link GetUserRealmsResponse }
     * 
     */
    public GetUserRealmsResponse createGetUserRealmsResponse() {
        return new GetUserRealmsResponse();
    }

    /**
     * Create an instance of {@link GetFoldersResponse }
     * 
     */
    public GetFoldersResponse createGetFoldersResponse() {
        return new GetFoldersResponse();
    }

    /**
     * Create an instance of {@link ParameterMappingsXto }
     * 
     */
    public ParameterMappingsXto createParameterMappingsXto() {
        return new ParameterMappingsXto();
    }

    /**
     * Create an instance of {@link DocumentQueryResultXto }
     * 
     */
    public DocumentQueryResultXto createDocumentQueryResultXto() {
        return new DocumentQueryResultXto();
    }

    /**
     * Create an instance of {@link DocumentXto.VersionLabelsXto }
     * 
     */
    public DocumentXto.VersionLabelsXto createDocumentXtoVersionLabelsXto() {
        return new DocumentXto.VersionLabelsXto();
    }

    /**
     * Create an instance of {@link ActivateNextActivityForProcessResponse }
     * 
     */
    public ActivateNextActivityForProcessResponse createActivateNextActivityForProcessResponse() {
        return new ActivateNextActivityForProcessResponse();
    }

    /**
     * Create an instance of {@link DropUserRealmResponse }
     * 
     */
    public DropUserRealmResponse createDropUserRealmResponse() {
        return new DropUserRealmResponse();
    }

    /**
     * Create an instance of {@link org.eclipse.stardust.engine.api.ws.DocumentQueryXto }
     * 
     */
    public org.eclipse.stardust.engine.api.ws.DocumentQueryXto createDocumentQueryXto() {
        return new org.eclipse.stardust.engine.api.ws.DocumentQueryXto();
    }

    /**
     * Create an instance of {@link HibernateActivityResponse }
     * 
     */
    public HibernateActivityResponse createHibernateActivityResponse() {
        return new HibernateActivityResponse();
    }

    /**
     * Create an instance of {@link RemoveProcessEventBindingResponse }
     * 
     */
    public RemoveProcessEventBindingResponse createRemoveProcessEventBindingResponse() {
        return new RemoveProcessEventBindingResponse();
    }

    /**
     * Create an instance of {@link PermissionScopeXto }
     * 
     */
    public PermissionScopeXto createPermissionScopeXto() {
        return new PermissionScopeXto();
    }

    /**
     * Create an instance of {@link ProcessDefinitionsXto }
     * 
     */
    public ProcessDefinitionsXto createProcessDefinitionsXto() {
        return new ProcessDefinitionsXto();
    }

    /**
     * Create an instance of {@link ModifyDepartment }
     * 
     */
    public ModifyDepartment createModifyDepartment() {
        return new ModifyDepartment();
    }

    /**
     * Create an instance of {@link ActivityQueryResultXto }
     * 
     */
    public ActivityQueryResultXto createActivityQueryResultXto() {
        return new ActivityQueryResultXto();
    }

    /**
     * Create an instance of {@link GetDeputiesResponse }
     * 
     */
    public GetDeputiesResponse createGetDeputiesResponse() {
        return new GetDeputiesResponse();
    }

    /**
     * Create an instance of {@link UserQueryResultXto.UsersXto }
     * 
     */
    public UserQueryResultXto.UsersXto createUserQueryResultXtoUsersXto() {
        return new UserQueryResultXto.UsersXto();
    }

    /**
     * Create an instance of {@link PermissionStatesXto }
     * 
     */
    public PermissionStatesXto createPermissionStatesXto() {
        return new PermissionStatesXto();
    }

    /**
     * Create an instance of {@link GetDocumentContentResponse }
     * 
     */
    public GetDocumentContentResponse createGetDocumentContentResponse() {
        return new GetDocumentContentResponse();
    }

    /**
     * Create an instance of {@link DeleteBusinessObjectInstanceResponse }
     * 
     */
    public DeleteBusinessObjectInstanceResponse createDeleteBusinessObjectInstanceResponse() {
        return new DeleteBusinessObjectInstanceResponse();
    }

    /**
     * Create an instance of {@link DelegateCaseResponse }
     * 
     */
    public DelegateCaseResponse createDelegateCaseResponse() {
        return new DelegateCaseResponse();
    }

    /**
     * Create an instance of {@link FolderQueryXto }
     * 
     */
    public FolderQueryXto createFolderQueryXto() {
        return new FolderQueryXto();
    }

    /**
     * Create an instance of {@link GetModel }
     * 
     */
    public GetModel createGetModel() {
        return new GetModel();
    }

    /**
     * Create an instance of {@link RepositoryMigrationJobInfoXto }
     * 
     */
    public RepositoryMigrationJobInfoXto createRepositoryMigrationJobInfoXto() {
        return new RepositoryMigrationJobInfoXto();
    }

    /**
     * Create an instance of {@link GetPermissionsResponse }
     * 
     */
    public GetPermissionsResponse createGetPermissionsResponse() {
        return new GetPermissionsResponse();
    }

    /**
     * Create an instance of {@link DepartmentsXto }
     * 
     */
    public DepartmentsXto createDepartmentsXto() {
        return new DepartmentsXto();
    }

    /**
     * Create an instance of {@link ConfigurationVariableXto }
     * 
     */
    public ConfigurationVariableXto createConfigurationVariableXto() {
        return new ConfigurationVariableXto();
    }

    /**
     * Create an instance of {@link GetPermissions }
     * 
     */
    public GetPermissions createGetPermissions() {
        return new GetPermissions();
    }

    /**
     * Create an instance of {@link GetPoliciesResponse }
     * 
     */
    public GetPoliciesResponse createGetPoliciesResponse() {
        return new GetPoliciesResponse();
    }

    /**
     * Create an instance of {@link CreateUserRealmResponse }
     * 
     */
    public CreateUserRealmResponse createCreateUserRealmResponse() {
        return new CreateUserRealmResponse();
    }

    /**
     * Create an instance of {@link LogEntryQueryResultXto }
     * 
     */
    public LogEntryQueryResultXto createLogEntryQueryResultXto() {
        return new LogEntryQueryResultXto();
    }

    /**
     * Create an instance of {@link CreateProcessEventBinding }
     * 
     */
    public CreateProcessEventBinding createCreateProcessEventBinding() {
        return new CreateProcessEventBinding();
    }

    /**
     * Create an instance of {@link SetGlobalPermissionsResponse }
     * 
     */
    public SetGlobalPermissionsResponse createSetGlobalPermissionsResponse() {
        return new SetGlobalPermissionsResponse();
    }

    /**
     * Create an instance of {@link FindLogEntries }
     * 
     */
    public FindLogEntries createFindLogEntries() {
        return new FindLogEntries();
    }

    /**
     * Create an instance of {@link EventHandlerDefinitionXto }
     * 
     */
    public EventHandlerDefinitionXto createEventHandlerDefinitionXto() {
        return new EventHandlerDefinitionXto();
    }

    /**
     * Create an instance of {@link SetProcessInstancePriority }
     * 
     */
    public SetProcessInstancePriority createSetProcessInstancePriority() {
        return new SetProcessInstancePriority();
    }

    /**
     * Create an instance of {@link FolderInfoXto }
     * 
     */
    public FolderInfoXto createFolderInfoXto() {
        return new FolderInfoXto();
    }

    /**
     * Create an instance of {@link RemoveDeputyResponse }
     * 
     */
    public RemoveDeputyResponse createRemoveDeputyResponse() {
        return new RemoveDeputyResponse();
    }

    /**
     * Create an instance of {@link FindPreferencesResponse }
     * 
     */
    public FindPreferencesResponse createFindPreferencesResponse() {
        return new FindPreferencesResponse();
    }

    /**
     * Create an instance of {@link ActivateActivityAndGetInDataResponse }
     * 
     */
    public ActivateActivityAndGetInDataResponse createActivateActivityAndGetInDataResponse() {
        return new ActivateActivityAndGetInDataResponse();
    }

    /**
     * Create an instance of {@link GetStartableProcessDefinitionsResponse }
     * 
     */
    public GetStartableProcessDefinitionsResponse createGetStartableProcessDefinitionsResponse() {
        return new GetStartableProcessDefinitionsResponse();
    }

    /**
     * Create an instance of {@link RemoveDepartment }
     * 
     */
    public RemoveDepartment createRemoveDepartment() {
        return new RemoveDepartment();
    }

    /**
     * Create an instance of {@link GetModelAsXML }
     * 
     */
    public GetModelAsXML createGetModelAsXML() {
        return new GetModelAsXML();
    }

    /**
     * Create an instance of {@link ProcessSpawnInfoXto }
     * 
     */
    public ProcessSpawnInfoXto createProcessSpawnInfoXto() {
        return new ProcessSpawnInfoXto();
    }

    /**
     * Create an instance of {@link FindFoldersResponse }
     * 
     */
    public FindFoldersResponse createFindFoldersResponse() {
        return new FindFoldersResponse();
    }

    /**
     * Create an instance of {@link ForceCompletion }
     * 
     */
    public ForceCompletion createForceCompletion() {
        return new ForceCompletion();
    }

    /**
     * Create an instance of {@link InvalidateUserGroupResponse }
     * 
     */
    public InvalidateUserGroupResponse createInvalidateUserGroupResponse() {
        return new InvalidateUserGroupResponse();
    }

    /**
     * Create an instance of {@link GetUserRealmsResponse.UserRealmsXto }
     * 
     */
    public GetUserRealmsResponse.UserRealmsXto createGetUserRealmsResponseUserRealmsXto() {
        return new GetUserRealmsResponse.UserRealmsXto();
    }

    /**
     * Create an instance of {@link RepositoryConfigurationXto }
     * 
     */
    public RepositoryConfigurationXto createRepositoryConfigurationXto() {
        return new RepositoryConfigurationXto();
    }

    /**
     * Create an instance of {@link CreateDocumentResponse }
     * 
     */
    public CreateDocumentResponse createCreateDocumentResponse() {
        return new CreateDocumentResponse();
    }

    /**
     * Create an instance of {@link DeputyXto }
     * 
     */
    public DeputyXto createDeputyXto() {
        return new DeputyXto();
    }

    /**
     * Create an instance of {@link DocumentTypeResultsXto }
     * 
     */
    public DocumentTypeResultsXto createDocumentTypeResultsXto() {
        return new DocumentTypeResultsXto();
    }

    /**
     * Create an instance of {@link GetDepartmentResponse }
     * 
     */
    public GetDepartmentResponse createGetDepartmentResponse() {
        return new GetDepartmentResponse();
    }

    /**
     * Create an instance of {@link GetDocumentTypeSchema }
     * 
     */
    public GetDocumentTypeSchema createGetDocumentTypeSchema() {
        return new GetDocumentTypeSchema();
    }

    /**
     * Create an instance of {@link AccessPointXto }
     * 
     */
    public AccessPointXto createAccessPointXto() {
        return new AccessPointXto();
    }

    /**
     * Create an instance of {@link SetProcessProperties }
     * 
     */
    public SetProcessProperties createSetProcessProperties() {
        return new SetProcessProperties();
    }

    /**
     * Create an instance of {@link ConfigurationVariablesListXto }
     * 
     */
    public ConfigurationVariablesListXto createConfigurationVariablesListXto() {
        return new ConfigurationVariablesListXto();
    }

    /**
     * Create an instance of {@link RequestDocumentContentUploadResponse }
     * 
     */
    public RequestDocumentContentUploadResponse createRequestDocumentContentUploadResponse() {
        return new RequestDocumentContentUploadResponse();
    }

    /**
     * Create an instance of {@link VersionDocumentResponse }
     * 
     */
    public VersionDocumentResponse createVersionDocumentResponse() {
        return new VersionDocumentResponse();
    }

    /**
     * Create an instance of {@link ParameterXto }
     * 
     */
    public ParameterXto createParameterXto() {
        return new ParameterXto();
    }

    /**
     * Create an instance of {@link GetDocumentTypeSchemaResponse }
     * 
     */
    public GetDocumentTypeSchemaResponse createGetDocumentTypeSchemaResponse() {
        return new GetDocumentTypeSchemaResponse();
    }

    /**
     * Create an instance of {@link GetDocuments.DocumentIdsXto }
     * 
     */
    public GetDocuments.DocumentIdsXto createGetDocumentsDocumentIdsXto() {
        return new GetDocuments.DocumentIdsXto();
    }

    /**
     * Create an instance of {@link InteractionContextXto }
     * 
     */
    public InteractionContextXto createInteractionContextXto() {
        return new InteractionContextXto();
    }

    /**
     * Create an instance of {@link AuditTrailHealthReportXto }
     * 
     */
    public AuditTrailHealthReportXto createAuditTrailHealthReportXto() {
        return new AuditTrailHealthReportXto();
    }

    /**
     * Create an instance of {@link VariableDefinitionQueryResultXto }
     * 
     */
    public VariableDefinitionQueryResultXto createVariableDefinitionQueryResultXto() {
        return new VariableDefinitionQueryResultXto();
    }

    /**
     * Create an instance of {@link GetAllModelDescriptions }
     * 
     */
    public GetAllModelDescriptions createGetAllModelDescriptions() {
        return new GetAllModelDescriptions();
    }

    /**
     * Create an instance of {@link DeploymentInfoXto }
     * 
     */
    public DeploymentInfoXto createDeploymentInfoXto() {
        return new DeploymentInfoXto();
    }

    /**
     * Create an instance of {@link DeleteProcesses.OidsXto }
     * 
     */
    public DeleteProcesses.OidsXto createDeleteProcessesOidsXto() {
        return new DeleteProcesses.OidsXto();
    }

    /**
     * Create an instance of {@link GetDaemonStatus }
     * 
     */
    public GetDaemonStatus createGetDaemonStatus() {
        return new GetDaemonStatus();
    }

    /**
     * Create an instance of {@link CreateFolderResponse }
     * 
     */
    public CreateFolderResponse createCreateFolderResponse() {
        return new CreateFolderResponse();
    }

    /**
     * Create an instance of {@link FindPreferences }
     * 
     */
    public FindPreferences createFindPreferences() {
        return new FindPreferences();
    }

    /**
     * Create an instance of {@link ItemXto }
     * 
     */
    public ItemXto createItemXto() {
        return new ItemXto();
    }

    /**
     * Create an instance of {@link StartDaemon }
     * 
     */
    public StartDaemon createStartDaemon() {
        return new StartDaemon();
    }

    /**
     * Create an instance of {@link DelegateActivity }
     * 
     */
    public DelegateActivity createDelegateActivity() {
        return new DelegateActivity();
    }

    /**
     * Create an instance of {@link FindUsersResponse }
     * 
     */
    public FindUsersResponse createFindUsersResponse() {
        return new FindUsersResponse();
    }

    /**
     * Create an instance of {@link ParametersXto }
     * 
     */
    public ParametersXto createParametersXto() {
        return new ParametersXto();
    }

    /**
     * Create an instance of {@link AddDeputyResponse }
     * 
     */
    public AddDeputyResponse createAddDeputyResponse() {
        return new AddDeputyResponse();
    }

    /**
     * Create an instance of {@link HistoricalEventDetailsXto }
     * 
     */
    public HistoricalEventDetailsXto createHistoricalEventDetailsXto() {
        return new HistoricalEventDetailsXto();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DocumentsXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "documents")
    public JAXBElement<DocumentsXto> createDocuments(DocumentsXto value) {
        return new JAXBElement<DocumentsXto>(_Documents_QNAME, DocumentsXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DocumentXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "document")
    public JAXBElement<DocumentXto> createDocument(DocumentXto value) {
        return new JAXBElement<DocumentXto>(_Document_QNAME, DocumentXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FolderXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "folder")
    public JAXBElement<FolderXto> createFolder(FolderXto value) {
        return new JAXBElement<FolderXto>(_Folder_QNAME, FolderXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NoteXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "note")
    public JAXBElement<NoteXto> createNote(NoteXto value) {
        return new JAXBElement<NoteXto>(_Note_QNAME, NoteXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FoldersXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "folders")
    public JAXBElement<FoldersXto> createFolders(FoldersXto value) {
        return new JAXBElement<FoldersXto>(_Folders_QNAME, FoldersXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BpmFaultXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "bpmFault")
    public JAXBElement<BpmFaultXto> createBpmFault(BpmFaultXto value) {
        return new JAXBElement<BpmFaultXto>(_BpmFault_QNAME, BpmFaultXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DocumentVersionInfoXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "versionInfo", scope = UpdateDocument.class)
    public JAXBElement<DocumentVersionInfoXto> createUpdateDocumentVersionInfo(DocumentVersionInfoXto value) {
        return new JAXBElement<DocumentVersionInfoXto>(_UpdateDocumentVersionInfo_QNAME, DocumentVersionInfoXto.class, UpdateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataHandler }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "content", scope = UpdateDocument.class)
    @XmlMimeType("*/*")
    public JAXBElement<DataHandler> createUpdateDocumentContent(DataHandler value) {
        return new JAXBElement<DataHandler>(_UpdateDocumentContent_QNAME, DataHandler.class, UpdateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivityQueryXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "query", scope = FindActivities.class)
    public JAXBElement<ActivityQueryXto> createFindActivitiesQuery(ActivityQueryXto value) {
        return new JAXBElement<ActivityQueryXto>(_FindActivitiesQuery_QNAME, ActivityQueryXto.class, FindActivities.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "description", scope = ModifyDepartment.class)
    public JAXBElement<String> createModifyDepartmentDescription(String value) {
        return new JAXBElement<String>(_ModifyDepartmentDescription_QNAME, String.class, ModifyDepartment.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InputDocumentsXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "attachments", scope = StartProcessForModel.class)
    public JAXBElement<InputDocumentsXto> createStartProcessForModelAttachments(InputDocumentsXto value) {
        return new JAXBElement<InputDocumentsXto>(_StartProcessForModelAttachments_QNAME, InputDocumentsXto.class, StartProcessForModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "activate", scope = CompleteActivityAndActivateNext.class)
    public JAXBElement<Boolean> createCompleteActivityAndActivateNextActivate(Boolean value) {
        return new JAXBElement<Boolean>(_CompleteActivityAndActivateNextActivate_QNAME, Boolean.class, CompleteActivityAndActivateNext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DaemonsXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "deamons", scope = GetDaemonStatusResponse.class)
    public JAXBElement<DaemonsXto> createGetDaemonStatusResponseDeamons(DaemonsXto value) {
        return new JAXBElement<DaemonsXto>(_GetDaemonStatusResponseDeamons_QNAME, DaemonsXto.class, GetDaemonStatusResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetActivityInData.DataIdsXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "dataIds", scope = GetActivityInData.class)
    public JAXBElement<GetActivityInData.DataIdsXto> createGetActivityInDataDataIds(GetActivityInData.DataIdsXto value) {
        return new JAXBElement<GetActivityInData.DataIdsXto>(_GetActivityInDataDataIds_QNAME, GetActivityInData.DataIdsXto.class, GetActivityInData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link org.eclipse.stardust.engine.api.ws.query.DocumentQueryXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "query", scope = GetAllDocuments.class)
    public JAXBElement<org.eclipse.stardust.engine.api.ws.query.DocumentQueryXto> createGetAllDocumentsQuery(org.eclipse.stardust.engine.api.ws.query.DocumentQueryXto value) {
        return new JAXBElement<org.eclipse.stardust.engine.api.ws.query.DocumentQueryXto>(_FindActivitiesQuery_QNAME, org.eclipse.stardust.engine.api.ws.query.DocumentQueryXto.class, GetAllDocuments.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "ignoreWarnings", scope = DeployModel.class)
    public JAXBElement<Boolean> createDeployModelIgnoreWarnings(Boolean value) {
        return new JAXBElement<Boolean>(_DeployModelIgnoreWarnings_QNAME, Boolean.class, DeployModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Date }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "validTo", scope = DeployModel.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    public JAXBElement<Date> createDeployModelValidTo(Date value) {
        return new JAXBElement<Date>(_DeployModelValidTo_QNAME, Date.class, DeployModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "disabled", scope = DeployModel.class)
    public JAXBElement<Boolean> createDeployModelDisabled(Boolean value) {
        return new JAXBElement<Boolean>(_DeployModelDisabled_QNAME, Boolean.class, DeployModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "predecessorOid", scope = DeployModel.class)
    public JAXBElement<Integer> createDeployModelPredecessorOid(Integer value) {
        return new JAXBElement<Integer>(_DeployModelPredecessorOid_QNAME, Integer.class, DeployModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "configuration", scope = DeployModel.class)
    public JAXBElement<String> createDeployModelConfiguration(String value) {
        return new JAXBElement<String>(_DeployModelConfiguration_QNAME, String.class, DeployModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "comment", scope = DeployModel.class)
    public JAXBElement<String> createDeployModelComment(String value) {
        return new JAXBElement<String>(_DeployModelComment_QNAME, String.class, DeployModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Date }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "validFrom", scope = DeployModel.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    public JAXBElement<Date> createDeployModelValidFrom(Date value) {
        return new JAXBElement<Date>(_DeployModelValidFrom_QNAME, Date.class, DeployModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProcessProperties.PropertyIdsXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "propertyIds", scope = GetProcessProperties.class)
    public JAXBElement<GetProcessProperties.PropertyIdsXto> createGetProcessPropertiesPropertyIds(GetProcessProperties.PropertyIdsXto value) {
        return new JAXBElement<GetProcessProperties.PropertyIdsXto>(_GetProcessPropertiesPropertyIds_QNAME, GetProcessProperties.PropertyIdsXto.class, GetProcessProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "realmId", scope = InvalidateUser.class)
    public JAXBElement<String> createInvalidateUserRealmId(String value) {
        return new JAXBElement<String>(_InvalidateUserRealmId_QNAME, String.class, InvalidateUser.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessQueryXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "query", scope = FindProcesses.class)
    public JAXBElement<ProcessQueryXto> createFindProcessesQuery(ProcessQueryXto value) {
        return new JAXBElement<ProcessQueryXto>(_FindActivitiesQuery_QNAME, ProcessQueryXto.class, FindProcesses.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DocumentVersionInfoXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "versionInfo", scope = CreateDocument.class)
    public JAXBElement<DocumentVersionInfoXto> createCreateDocumentVersionInfo(DocumentVersionInfoXto value) {
        return new JAXBElement<DocumentVersionInfoXto>(_UpdateDocumentVersionInfo_QNAME, DocumentVersionInfoXto.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataHandler }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "content", scope = CreateDocument.class)
    @XmlMimeType("*/*")
    public JAXBElement<DataHandler> createCreateDocumentContent(DataHandler value) {
        return new JAXBElement<DataHandler>(_UpdateDocumentContent_QNAME, DataHandler.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "activate", scope = CompleteActivity.class)
    public JAXBElement<Boolean> createCompleteActivityActivate(Boolean value) {
        return new JAXBElement<Boolean>(_CompleteActivityAndActivateNextActivate_QNAME, Boolean.class, CompleteActivity.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "modelOid", scope = GetModel.class)
    public JAXBElement<Long> createGetModelModelOid(Long value) {
        return new JAXBElement<Long>(_GetModelModelOid_QNAME, Long.class, GetModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "computeAliveness", scope = GetModel.class)
    public JAXBElement<Boolean> createGetModelComputeAliveness(Boolean value) {
        return new JAXBElement<Boolean>(_GetModelComputeAliveness_QNAME, Boolean.class, GetModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessDefinitionQueryXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "query", scope = FindProcessDefinitions.class)
    public JAXBElement<ProcessDefinitionQueryXto> createFindProcessDefinitionsQuery(ProcessDefinitionQueryXto value) {
        return new JAXBElement<ProcessDefinitionQueryXto>(_FindActivitiesQuery_QNAME, ProcessDefinitionQueryXto.class, FindProcessDefinitions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "userGroupId", scope = InvalidateUserGroup.class)
    public JAXBElement<String> createInvalidateUserGroupUserGroupId(String value) {
        return new JAXBElement<String>(_InvalidateUserGroupUserGroupId_QNAME, String.class, InvalidateUserGroup.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DaemonParametersXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "daemonParameters", scope = GetDaemonStatus.class)
    public JAXBElement<DaemonParametersXto> createGetDaemonStatusDaemonParameters(DaemonParametersXto value) {
        return new JAXBElement<DaemonParametersXto>(_GetDaemonStatusDaemonParameters_QNAME, DaemonParametersXto.class, GetDaemonStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DaemonsXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "deamons", scope = StopDaemonResponse.class)
    public JAXBElement<DaemonsXto> createStopDaemonResponseDeamons(DaemonsXto value) {
        return new JAXBElement<DaemonsXto>(_GetDaemonStatusResponseDeamons_QNAME, DaemonsXto.class, StopDaemonResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DaemonsXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "deamons", scope = StartDaemonResponse.class)
    public JAXBElement<DaemonsXto> createStartDaemonResponseDeamons(DaemonsXto value) {
        return new JAXBElement<DaemonsXto>(_GetDaemonStatusResponseDeamons_QNAME, DaemonsXto.class, StartDaemonResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbortScopeXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "abortScope", scope = AbortActivity.class)
    public JAXBElement<AbortScopeXto> createAbortActivityAbortScope(AbortScopeXto value) {
        return new JAXBElement<AbortScopeXto>(_AbortActivityAbortScope_QNAME, AbortScopeXto.class, AbortActivity.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringListXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "modelIds", scope = GetConfigurationVariables.class)
    public JAXBElement<StringListXto> createGetConfigurationVariablesModelIds(StringListXto value) {
        return new JAXBElement<StringListXto>(_GetConfigurationVariablesModelIds_QNAME, StringListXto.class, GetConfigurationVariables.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "description", scope = CreateUserRealm.class)
    public JAXBElement<String> createCreateUserRealmDescription(String value) {
        return new JAXBElement<String>(_ModifyDepartmentDescription_QNAME, String.class, CreateUserRealm.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "processOid", scope = WriteLogEntry.class)
    public JAXBElement<Long> createWriteLogEntryProcessOid(Long value) {
        return new JAXBElement<Long>(_WriteLogEntryProcessOid_QNAME, Long.class, WriteLogEntry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "activityOid", scope = WriteLogEntry.class)
    public JAXBElement<Long> createWriteLogEntryActivityOid(Long value) {
        return new JAXBElement<Long>(_WriteLogEntryActivityOid_QNAME, Long.class, WriteLogEntry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DaemonParametersXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "daemonParameters", scope = StartDaemon.class)
    public JAXBElement<DaemonParametersXto> createStartDaemonDaemonParameters(DaemonParametersXto value) {
        return new JAXBElement<DaemonParametersXto>(_GetDaemonStatusDaemonParameters_QNAME, DaemonParametersXto.class, StartDaemon.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DaemonParametersXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "daemonParameters", scope = StopDaemon.class)
    public JAXBElement<DaemonParametersXto> createStopDaemonDaemonParameters(DaemonParametersXto value) {
        return new JAXBElement<DaemonParametersXto>(_GetDaemonStatusDaemonParameters_QNAME, DaemonParametersXto.class, StopDaemon.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "modelId", scope = GetDocumentTypes.class)
    public JAXBElement<String> createGetDocumentTypesModelId(String value) {
        return new JAXBElement<String>(_GetDocumentTypesModelId_QNAME, String.class, GetDocumentTypes.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "description", scope = CreateDepartment.class)
    public JAXBElement<String> createCreateDepartmentDescription(String value) {
        return new JAXBElement<String>(_ModifyDepartmentDescription_QNAME, String.class, CreateDepartment.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "ignoreWarnings", scope = OverwriteModel.class)
    public JAXBElement<Boolean> createOverwriteModelIgnoreWarnings(Boolean value) {
        return new JAXBElement<Boolean>(_DeployModelIgnoreWarnings_QNAME, Boolean.class, OverwriteModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Date }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "validTo", scope = OverwriteModel.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    public JAXBElement<Date> createOverwriteModelValidTo(Date value) {
        return new JAXBElement<Date>(_DeployModelValidTo_QNAME, Date.class, OverwriteModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "disabled", scope = OverwriteModel.class)
    public JAXBElement<Boolean> createOverwriteModelDisabled(Boolean value) {
        return new JAXBElement<Boolean>(_DeployModelDisabled_QNAME, Boolean.class, OverwriteModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "configuration", scope = OverwriteModel.class)
    public JAXBElement<String> createOverwriteModelConfiguration(String value) {
        return new JAXBElement<String>(_DeployModelConfiguration_QNAME, String.class, OverwriteModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "comment", scope = OverwriteModel.class)
    public JAXBElement<String> createOverwriteModelComment(String value) {
        return new JAXBElement<String>(_DeployModelComment_QNAME, String.class, OverwriteModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Date }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "validFrom", scope = OverwriteModel.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    public JAXBElement<Date> createOverwriteModelValidFrom(Date value) {
        return new JAXBElement<Date>(_DeployModelValidFrom_QNAME, Date.class, OverwriteModel.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InputDocumentsXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "attachments", scope = StartProcess.class)
    public JAXBElement<InputDocumentsXto> createStartProcessAttachments(InputDocumentsXto value) {
        return new JAXBElement<InputDocumentsXto>(_StartProcessForModelAttachments_QNAME, InputDocumentsXto.class, StartProcess.class, value);
    }

}
