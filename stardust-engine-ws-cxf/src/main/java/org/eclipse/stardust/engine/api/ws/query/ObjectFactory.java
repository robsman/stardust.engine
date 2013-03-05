
package org.eclipse.stardust.engine.api.ws.query;

import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.eclipse.stardust.engine.api.ws.query package. 
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

    private final static QName _PerformanceCriticalityPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "performanceCriticalityPolicy");
    private final static QName _CasePolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "casePolicy");
    private final static QName _ForProcessDefinition_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "forProcessDefinition");
    private final static QName _FilterCriterion_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "filterCriterion");
    private final static QName _ParticipantAssociation_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "participantAssociation");
    private final static QName _IsNull_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "isNull");
    private final static QName _HistoricalEventPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "historicalEventPolicy");
    private final static QName _Between_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "between");
    private final static QName _SubsetPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "subsetPolicy");
    private final static QName _CriticalExecutionTimePolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "criticalExecutionTimePolicy");
    private final static QName _ExcludeUserPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "excludeUserPolicy");
    private final static QName _PerformingOnBehalfOf_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "performingOnBehalfOf");
    private final static QName _DescriptorPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "descriptorPolicy");
    private final static QName _CriticalProcessingTimePolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "criticalProcessingTimePolicy");
    private final static QName _HistoricalEventType_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "historicalEventType");
    private final static QName _ModelVersionPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "modelVersionPolicy");
    private final static QName _DataOrder_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "dataOrder");
    private final static QName _ForActivity_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "forActivity");
    private final static QName _ProcessCumulationPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "processCumulationPolicy");
    private final static QName _PerformingUser_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "performingUser");
    private final static QName _CriticalCostPerExecutionPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "criticalCostPerExecutionPolicy");
    private final static QName _HistoricalStatesPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "historicalStatesPolicy");
    private final static QName _UserDetailsLevel_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "userDetailsLevel");
    private final static QName _ProcessesInHierarchy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "processesInHierarchy");
    private final static QName _ActivitiesInState_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "activitiesInState");
    private final static QName _AbstractCriticalityPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "AbstractCriticalityPolicy");
    private final static QName _AbstractStoplightPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "abstractStoplightPolicy");
    private final static QName _StartingUser_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "startingUser");
    private final static QName _AbstractCriticalDurationPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "AbstractCriticalDurationPolicy");
    private final static QName _ForActivityDefinition_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "forActivityDefinition");
    private final static QName _PerformingParticipant_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "performingParticipant");
    private final static QName _ProcessInstanceDetailsPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "processInstanceDetailsPolicy");
    private final static QName _AbstractStoplightDurationPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "abstractStoplightDurationPolicy");
    private final static QName _EvaluateByWorkitemsPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "evaluateByWorkitemsPolicy");
    private final static QName _NotInList_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "notInList");
    private final static QName _InList_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "inList");
    private final static QName _HistoricalEventTypes_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "historicalEventTypes");
    private final static QName _TimeoutPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "timeoutPolicy");
    private final static QName _NotEqual_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "notEqual");
    private final static QName _PerformedByUser_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "performedByUser");
    private final static QName _UserState_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "userState");
    private final static QName _LessThan_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "lessThan");
    private final static QName _ProcessInstanceDetailsLevel_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "processInstanceDetailsLevel");
    private final static QName _LessOrEqual_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "lessOrEqual");
    private final static QName _ProcessesHavingLink_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "processesHavingLink");
    private final static QName _And_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "and");
    private final static QName _AbstractStoplightCostPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "abstractStoplightCostPolicy");
    private final static QName _ProcessesInState_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "processesInState");
    private final static QName _Or_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "or");
    private final static QName _NotNull_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "notNull");
    private final static QName _GreaterOrEqual_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "greaterOrEqual");
    private final static QName _SubFolderPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "subFolderPolicy");
    private final static QName _GreaterThan_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "greaterThan");
    private final static QName _ForProcess_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "forProcess");
    private final static QName _Policy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "policy");
    private final static QName _UserDetailsPolicy_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "userDetailsPolicy");
    private final static QName _IsLike_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "isLike");
    private final static QName _AttributeOrder_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "attributeOrder");
    private final static QName _OrderCriteria_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "orderCriteria");
    private final static QName _IsEqual_QNAME = new QName("http://eclipse.org/stardust/ws/v2012a/api/query", "isEqual");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.eclipse.stardust.engine.api.ws.query
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ModelVersionPolicyXto }
     * 
     */
    public ModelVersionPolicyXto createModelVersionPolicyXto() {
        return new ModelVersionPolicyXto();
    }

    /**
     * Create an instance of {@link AndTermXto }
     * 
     */
    public AndTermXto createAndTermXto() {
        return new AndTermXto();
    }

    /**
     * Create an instance of {@link ValuesLiteralXto }
     * 
     */
    public ValuesLiteralXto createValuesLiteralXto() {
        return new ValuesLiteralXto();
    }

    /**
     * Create an instance of {@link ParticipantContributionsXto }
     * 
     */
    public ParticipantContributionsXto createParticipantContributionsXto() {
        return new ParticipantContributionsXto();
    }

    /**
     * Create an instance of {@link LessOrEqualPredicateXto }
     * 
     */
    public LessOrEqualPredicateXto createLessOrEqualPredicateXto() {
        return new LessOrEqualPredicateXto();
    }

    /**
     * Create an instance of {@link ParticipantAssociationFilterXto.ModelParticipantXto }
     * 
     */
    public ParticipantAssociationFilterXto.ModelParticipantXto createParticipantAssociationFilterXtoModelParticipantXto() {
        return new ParticipantAssociationFilterXto.ModelParticipantXto();
    }

    /**
     * Create an instance of {@link CriticalCostPerExecutionPolicyXto }
     * 
     */
    public CriticalCostPerExecutionPolicyXto createCriticalCostPerExecutionPolicyXto() {
        return new CriticalCostPerExecutionPolicyXto();
    }

    /**
     * Create an instance of {@link UserStateFilterXto }
     * 
     */
    public UserStateFilterXto createUserStateFilterXto() {
        return new UserStateFilterXto();
    }

    /**
     * Create an instance of {@link StartingUserFilterXto }
     * 
     */
    public StartingUserFilterXto createStartingUserFilterXto() {
        return new StartingUserFilterXto();
    }

    /**
     * Create an instance of {@link PerformingOnBehalfOfFilterXto.ModelParticipantXto }
     * 
     */
    public PerformingOnBehalfOfFilterXto.ModelParticipantXto createPerformingOnBehalfOfFilterXtoModelParticipantXto() {
        return new PerformingOnBehalfOfFilterXto.ModelParticipantXto();
    }

    /**
     * Create an instance of {@link TimeoutPolicyXto }
     * 
     */
    public TimeoutPolicyXto createTimeoutPolicyXto() {
        return new TimeoutPolicyXto();
    }

    /**
     * Create an instance of {@link BetweenPredicateXto }
     * 
     */
    public BetweenPredicateXto createBetweenPredicateXto() {
        return new BetweenPredicateXto();
    }

    /**
     * Create an instance of {@link ActivityInstanceFilterXto }
     * 
     */
    public ActivityInstanceFilterXto createActivityInstanceFilterXto() {
        return new ActivityInstanceFilterXto();
    }

    /**
     * Create an instance of {@link VariableDefinitionQueryXto }
     * 
     */
    public VariableDefinitionQueryXto createVariableDefinitionQueryXto() {
        return new VariableDefinitionQueryXto();
    }

    /**
     * Create an instance of {@link LogEntryQueryXto }
     * 
     */
    public LogEntryQueryXto createLogEntryQueryXto() {
        return new LogEntryQueryXto();
    }

    /**
     * Create an instance of {@link SubFolderPolicyXto }
     * 
     */
    public SubFolderPolicyXto createSubFolderPolicyXto() {
        return new SubFolderPolicyXto();
    }

    /**
     * Create an instance of {@link ParticipantContributionXto }
     * 
     */
    public ParticipantContributionXto createParticipantContributionXto() {
        return new ParticipantContributionXto();
    }

    /**
     * Create an instance of {@link UserQueryXto }
     * 
     */
    public UserQueryXto createUserQueryXto() {
        return new UserQueryXto();
    }

    /**
     * Create an instance of {@link DocumentQueryXto }
     * 
     */
    public DocumentQueryXto createDocumentQueryXto() {
        return new DocumentQueryXto();
    }

    /**
     * Create an instance of {@link SubsetPolicyXto }
     * 
     */
    public SubsetPolicyXto createSubsetPolicyXto() {
        return new SubsetPolicyXto();
    }

    /**
     * Create an instance of {@link VariableReferenceXto }
     * 
     */
    public VariableReferenceXto createVariableReferenceXto() {
        return new VariableReferenceXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceHierarchyFilterXto }
     * 
     */
    public ProcessInstanceHierarchyFilterXto createProcessInstanceHierarchyFilterXto() {
        return new ProcessInstanceHierarchyFilterXto();
    }

    /**
     * Create an instance of {@link PolicyXto }
     * 
     */
    public PolicyXto createPolicyXto() {
        return new PolicyXto();
    }

    /**
     * Create an instance of {@link PreferenceQueryXto }
     * 
     */
    public PreferenceQueryXto createPreferenceQueryXto() {
        return new PreferenceQueryXto();
    }

    /**
     * Create an instance of {@link OrTermXto }
     * 
     */
    public OrTermXto createOrTermXto() {
        return new OrTermXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceDetailsPolicyXto }
     * 
     */
    public ProcessInstanceDetailsPolicyXto createProcessInstanceDetailsPolicyXto() {
        return new ProcessInstanceDetailsPolicyXto();
    }

    /**
     * Create an instance of {@link PerformingUserFilterXto }
     * 
     */
    public PerformingUserFilterXto createPerformingUserFilterXto() {
        return new PerformingUserFilterXto();
    }

    /**
     * Create an instance of {@link CriticalProcessingTimePolicyXto }
     * 
     */
    public CriticalProcessingTimePolicyXto createCriticalProcessingTimePolicyXto() {
        return new CriticalProcessingTimePolicyXto();
    }

    /**
     * Create an instance of {@link ProcessStateFilterXto }
     * 
     */
    public ProcessStateFilterXto createProcessStateFilterXto() {
        return new ProcessStateFilterXto();
    }

    /**
     * Create an instance of {@link UserContributionXto }
     * 
     */
    public UserContributionXto createUserContributionXto() {
        return new UserContributionXto();
    }

    /**
     * Create an instance of {@link InListPredicateXto }
     * 
     */
    public InListPredicateXto createInListPredicateXto() {
        return new InListPredicateXto();
    }

    /**
     * Create an instance of {@link PerformingParticipantFilterXto.ModelParticipantXto }
     * 
     */
    public PerformingParticipantFilterXto.ModelParticipantXto createPerformingParticipantFilterXtoModelParticipantXto() {
        return new PerformingParticipantFilterXto.ModelParticipantXto();
    }

    /**
     * Create an instance of {@link WorklistQueryXto }
     * 
     */
    public WorklistQueryXto createWorklistQueryXto() {
        return new WorklistQueryXto();
    }

    /**
     * Create an instance of {@link CriticalExecutionTimePolicyXto }
     * 
     */
    public CriticalExecutionTimePolicyXto createCriticalExecutionTimePolicyXto() {
        return new CriticalExecutionTimePolicyXto();
    }

    /**
     * Create an instance of {@link GreaterOrEqualPredicateXto }
     * 
     */
    public GreaterOrEqualPredicateXto createGreaterOrEqualPredicateXto() {
        return new GreaterOrEqualPredicateXto();
    }

    /**
     * Create an instance of {@link PerformingOnBehalfOfFilterXto.ModelParticipantsXto.ParticipantsXto }
     * 
     */
    public PerformingOnBehalfOfFilterXto.ModelParticipantsXto.ParticipantsXto createPerformingOnBehalfOfFilterXtoModelParticipantsXtoParticipantsXto() {
        return new PerformingOnBehalfOfFilterXto.ModelParticipantsXto.ParticipantsXto();
    }

    /**
     * Create an instance of {@link ActivityDefinitionFilterXto }
     * 
     */
    public ActivityDefinitionFilterXto createActivityDefinitionFilterXto() {
        return new ActivityDefinitionFilterXto();
    }

    /**
     * Create an instance of {@link PerformingParticipantFilterXto }
     * 
     */
    public PerformingParticipantFilterXto createPerformingParticipantFilterXto() {
        return new PerformingParticipantFilterXto();
    }

    /**
     * Create an instance of {@link ProcessQueryXto }
     * 
     */
    public ProcessQueryXto createProcessQueryXto() {
        return new ProcessQueryXto();
    }

    /**
     * Create an instance of {@link HistoricalStatesPolicyXto }
     * 
     */
    public HistoricalStatesPolicyXto createHistoricalStatesPolicyXto() {
        return new HistoricalStatesPolicyXto();
    }

    /**
     * Create an instance of {@link EvaluateByWorkitemsPolicyXto }
     * 
     */
    public EvaluateByWorkitemsPolicyXto createEvaluateByWorkitemsPolicyXto() {
        return new EvaluateByWorkitemsPolicyXto();
    }

    /**
     * Create an instance of {@link UserDetailsPolicyXto }
     * 
     */
    public UserDetailsPolicyXto createUserDetailsPolicyXto() {
        return new UserDetailsPolicyXto();
    }

    /**
     * Create an instance of {@link NotNullPredicateXto }
     * 
     */
    public NotNullPredicateXto createNotNullPredicateXto() {
        return new NotNullPredicateXto();
    }

    /**
     * Create an instance of {@link DescriptorPolicyXto }
     * 
     */
    public DescriptorPolicyXto createDescriptorPolicyXto() {
        return new DescriptorPolicyXto();
    }

    /**
     * Create an instance of {@link AttributeReferenceXto }
     * 
     */
    public AttributeReferenceXto createAttributeReferenceXto() {
        return new AttributeReferenceXto();
    }

    /**
     * Create an instance of {@link PerformingOnBehalfOfFilterXto }
     * 
     */
    public PerformingOnBehalfOfFilterXto createPerformingOnBehalfOfFilterXto() {
        return new PerformingOnBehalfOfFilterXto();
    }

    /**
     * Create an instance of {@link PerformingOnBehalfOfFilterXto.ModelParticipantsXto }
     * 
     */
    public PerformingOnBehalfOfFilterXto.ModelParticipantsXto createPerformingOnBehalfOfFilterXtoModelParticipantsXto() {
        return new PerformingOnBehalfOfFilterXto.ModelParticipantsXto();
    }

    /**
     * Create an instance of {@link CasePolicyXto }
     * 
     */
    public CasePolicyXto createCasePolicyXto() {
        return new CasePolicyXto();
    }

    /**
     * Create an instance of {@link ParticipantAssociationFilterXto }
     * 
     */
    public ParticipantAssociationFilterXto createParticipantAssociationFilterXto() {
        return new ParticipantAssociationFilterXto();
    }

    /**
     * Create an instance of {@link ActivityStateFilterXto.StatesXto }
     * 
     */
    public ActivityStateFilterXto.StatesXto createActivityStateFilterXtoStatesXto() {
        return new ActivityStateFilterXto.StatesXto();
    }

    /**
     * Create an instance of {@link ProcessDefinitionFilterXto }
     * 
     */
    public ProcessDefinitionFilterXto createProcessDefinitionFilterXto() {
        return new ProcessDefinitionFilterXto();
    }

    /**
     * Create an instance of {@link ExcludeUserPolicyXto }
     * 
     */
    public ExcludeUserPolicyXto createExcludeUserPolicyXto() {
        return new ExcludeUserPolicyXto();
    }

    /**
     * Create an instance of {@link ProcessDefinitionQueryXto }
     * 
     */
    public ProcessDefinitionQueryXto createProcessDefinitionQueryXto() {
        return new ProcessDefinitionQueryXto();
    }

    /**
     * Create an instance of {@link UserGroupQueryXto }
     * 
     */
    public UserGroupQueryXto createUserGroupQueryXto() {
        return new UserGroupQueryXto();
    }

    /**
     * Create an instance of {@link IsEqualPredicateXto }
     * 
     */
    public IsEqualPredicateXto createIsEqualPredicateXto() {
        return new IsEqualPredicateXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceLinkFilterXto.LinkTypesXto }
     * 
     */
    public ProcessInstanceLinkFilterXto.LinkTypesXto createProcessInstanceLinkFilterXtoLinkTypesXto() {
        return new ProcessInstanceLinkFilterXto.LinkTypesXto();
    }

    /**
     * Create an instance of {@link DataOrderXto }
     * 
     */
    public DataOrderXto createDataOrderXto() {
        return new DataOrderXto();
    }

    /**
     * Create an instance of {@link HistoricalEventPolicyXto }
     * 
     */
    public HistoricalEventPolicyXto createHistoricalEventPolicyXto() {
        return new HistoricalEventPolicyXto();
    }

    /**
     * Create an instance of {@link ProcessCumulationPolicyXto }
     * 
     */
    public ProcessCumulationPolicyXto createProcessCumulationPolicyXto() {
        return new ProcessCumulationPolicyXto();
    }

    /**
     * Create an instance of {@link ActivityQueryXto }
     * 
     */
    public ActivityQueryXto createActivityQueryXto() {
        return new ActivityQueryXto();
    }

    /**
     * Create an instance of {@link ProcessStateFilterXto.StatesXto }
     * 
     */
    public ProcessStateFilterXto.StatesXto createProcessStateFilterXtoStatesXto() {
        return new ProcessStateFilterXto.StatesXto();
    }

    /**
     * Create an instance of {@link ValueLiteralXto }
     * 
     */
    public ValueLiteralXto createValueLiteralXto() {
        return new ValueLiteralXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceLinkFilterXto }
     * 
     */
    public ProcessInstanceLinkFilterXto createProcessInstanceLinkFilterXto() {
        return new ProcessInstanceLinkFilterXto();
    }

    /**
     * Create an instance of {@link IsLikePredicateXto }
     * 
     */
    public IsLikePredicateXto createIsLikePredicateXto() {
        return new IsLikePredicateXto();
    }

    /**
     * Create an instance of {@link PerformanceCriticalityPolicyXto }
     * 
     */
    public PerformanceCriticalityPolicyXto createPerformanceCriticalityPolicyXto() {
        return new PerformanceCriticalityPolicyXto();
    }

    /**
     * Create an instance of {@link QueryXto }
     * 
     */
    public QueryXto createQueryXto() {
        return new QueryXto();
    }

    /**
     * Create an instance of {@link GreaterThanPredicateXto }
     * 
     */
    public GreaterThanPredicateXto createGreaterThanPredicateXto() {
        return new GreaterThanPredicateXto();
    }

    /**
     * Create an instance of {@link LessThanPredicateXto }
     * 
     */
    public LessThanPredicateXto createLessThanPredicateXto() {
        return new LessThanPredicateXto();
    }

    /**
     * Create an instance of {@link NotEqualPredicateXto }
     * 
     */
    public NotEqualPredicateXto createNotEqualPredicateXto() {
        return new NotEqualPredicateXto();
    }

    /**
     * Create an instance of {@link ActivityStateFilterXto }
     * 
     */
    public ActivityStateFilterXto createActivityStateFilterXto() {
        return new ActivityStateFilterXto();
    }

    /**
     * Create an instance of {@link ProcessInstanceFilterXto }
     * 
     */
    public ProcessInstanceFilterXto createProcessInstanceFilterXto() {
        return new ProcessInstanceFilterXto();
    }

    /**
     * Create an instance of {@link PerformingParticipantFilterXto.AnyForUserXto }
     * 
     */
    public PerformingParticipantFilterXto.AnyForUserXto createPerformingParticipantFilterXtoAnyForUserXto() {
        return new PerformingParticipantFilterXto.AnyForUserXto();
    }

    /**
     * Create an instance of {@link IsNullPredicateXto }
     * 
     */
    public IsNullPredicateXto createIsNullPredicateXto() {
        return new IsNullPredicateXto();
    }

    /**
     * Create an instance of {@link OrderCriteriaXto }
     * 
     */
    public OrderCriteriaXto createOrderCriteriaXto() {
        return new OrderCriteriaXto();
    }

    /**
     * Create an instance of {@link DeployedModelQueryXto }
     * 
     */
    public DeployedModelQueryXto createDeployedModelQueryXto() {
        return new DeployedModelQueryXto();
    }

    /**
     * Create an instance of {@link AttributeOrderXto }
     * 
     */
    public AttributeOrderXto createAttributeOrderXto() {
        return new AttributeOrderXto();
    }

    /**
     * Create an instance of {@link NotInListPredicateXto }
     * 
     */
    public NotInListPredicateXto createNotInListPredicateXto() {
        return new NotInListPredicateXto();
    }

    /**
     * Create an instance of {@link PerformedByUserFilterXto }
     * 
     */
    public PerformedByUserFilterXto createPerformedByUserFilterXto() {
        return new PerformedByUserFilterXto();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformanceCriticalityPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "performanceCriticalityPolicy")
    public JAXBElement<PerformanceCriticalityPolicyXto> createPerformanceCriticalityPolicy(PerformanceCriticalityPolicyXto value) {
        return new JAXBElement<PerformanceCriticalityPolicyXto>(_PerformanceCriticalityPolicy_QNAME, PerformanceCriticalityPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CasePolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "casePolicy")
    public JAXBElement<CasePolicyXto> createCasePolicy(CasePolicyXto value) {
        return new JAXBElement<CasePolicyXto>(_CasePolicy_QNAME, CasePolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessDefinitionFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "forProcessDefinition", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ProcessDefinitionFilterXto> createForProcessDefinition(ProcessDefinitionFilterXto value) {
        return new JAXBElement<ProcessDefinitionFilterXto>(_ForProcessDefinition_QNAME, ProcessDefinitionFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PredicateBaseXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "filterCriterion")
    public JAXBElement<PredicateBaseXto> createFilterCriterion(PredicateBaseXto value) {
        return new JAXBElement<PredicateBaseXto>(_FilterCriterion_QNAME, PredicateBaseXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ParticipantAssociationFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "participantAssociation", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ParticipantAssociationFilterXto> createParticipantAssociation(ParticipantAssociationFilterXto value) {
        return new JAXBElement<ParticipantAssociationFilterXto>(_ParticipantAssociation_QNAME, ParticipantAssociationFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IsNullPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "isNull", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<IsNullPredicateXto> createIsNull(IsNullPredicateXto value) {
        return new JAXBElement<IsNullPredicateXto>(_IsNull_QNAME, IsNullPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HistoricalEventPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "historicalEventPolicy")
    public JAXBElement<HistoricalEventPolicyXto> createHistoricalEventPolicy(HistoricalEventPolicyXto value) {
        return new JAXBElement<HistoricalEventPolicyXto>(_HistoricalEventPolicy_QNAME, HistoricalEventPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BetweenPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "between", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<BetweenPredicateXto> createBetween(BetweenPredicateXto value) {
        return new JAXBElement<BetweenPredicateXto>(_Between_QNAME, BetweenPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubsetPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "subsetPolicy")
    public JAXBElement<SubsetPolicyXto> createSubsetPolicy(SubsetPolicyXto value) {
        return new JAXBElement<SubsetPolicyXto>(_SubsetPolicy_QNAME, SubsetPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CriticalExecutionTimePolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "criticalExecutionTimePolicy")
    public JAXBElement<CriticalExecutionTimePolicyXto> createCriticalExecutionTimePolicy(CriticalExecutionTimePolicyXto value) {
        return new JAXBElement<CriticalExecutionTimePolicyXto>(_CriticalExecutionTimePolicy_QNAME, CriticalExecutionTimePolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExcludeUserPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "excludeUserPolicy")
    public JAXBElement<ExcludeUserPolicyXto> createExcludeUserPolicy(ExcludeUserPolicyXto value) {
        return new JAXBElement<ExcludeUserPolicyXto>(_ExcludeUserPolicy_QNAME, ExcludeUserPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformingOnBehalfOfFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "performingOnBehalfOf", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<PerformingOnBehalfOfFilterXto> createPerformingOnBehalfOf(PerformingOnBehalfOfFilterXto value) {
        return new JAXBElement<PerformingOnBehalfOfFilterXto>(_PerformingOnBehalfOf_QNAME, PerformingOnBehalfOfFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DescriptorPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "descriptorPolicy")
    public JAXBElement<DescriptorPolicyXto> createDescriptorPolicy(DescriptorPolicyXto value) {
        return new JAXBElement<DescriptorPolicyXto>(_DescriptorPolicy_QNAME, DescriptorPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CriticalProcessingTimePolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "criticalProcessingTimePolicy")
    public JAXBElement<CriticalProcessingTimePolicyXto> createCriticalProcessingTimePolicy(CriticalProcessingTimePolicyXto value) {
        return new JAXBElement<CriticalProcessingTimePolicyXto>(_CriticalProcessingTimePolicy_QNAME, CriticalProcessingTimePolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HistoricalEventTypeXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "historicalEventType")
    public JAXBElement<HistoricalEventTypeXto> createHistoricalEventType(HistoricalEventTypeXto value) {
        return new JAXBElement<HistoricalEventTypeXto>(_HistoricalEventType_QNAME, HistoricalEventTypeXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ModelVersionPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "modelVersionPolicy")
    public JAXBElement<ModelVersionPolicyXto> createModelVersionPolicy(ModelVersionPolicyXto value) {
        return new JAXBElement<ModelVersionPolicyXto>(_ModelVersionPolicy_QNAME, ModelVersionPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataOrderXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "dataOrder")
    public JAXBElement<DataOrderXto> createDataOrder(DataOrderXto value) {
        return new JAXBElement<DataOrderXto>(_DataOrder_QNAME, DataOrderXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivityInstanceFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "forActivity", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ActivityInstanceFilterXto> createForActivity(ActivityInstanceFilterXto value) {
        return new JAXBElement<ActivityInstanceFilterXto>(_ForActivity_QNAME, ActivityInstanceFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessCumulationPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "processCumulationPolicy")
    public JAXBElement<ProcessCumulationPolicyXto> createProcessCumulationPolicy(ProcessCumulationPolicyXto value) {
        return new JAXBElement<ProcessCumulationPolicyXto>(_ProcessCumulationPolicy_QNAME, ProcessCumulationPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformingUserFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "performingUser", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<PerformingUserFilterXto> createPerformingUser(PerformingUserFilterXto value) {
        return new JAXBElement<PerformingUserFilterXto>(_PerformingUser_QNAME, PerformingUserFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CriticalCostPerExecutionPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "criticalCostPerExecutionPolicy")
    public JAXBElement<CriticalCostPerExecutionPolicyXto> createCriticalCostPerExecutionPolicy(CriticalCostPerExecutionPolicyXto value) {
        return new JAXBElement<CriticalCostPerExecutionPolicyXto>(_CriticalCostPerExecutionPolicy_QNAME, CriticalCostPerExecutionPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HistoricalStatesPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "historicalStatesPolicy")
    public JAXBElement<HistoricalStatesPolicyXto> createHistoricalStatesPolicy(HistoricalStatesPolicyXto value) {
        return new JAXBElement<HistoricalStatesPolicyXto>(_HistoricalStatesPolicy_QNAME, HistoricalStatesPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserDetailsLevelXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "userDetailsLevel")
    public JAXBElement<UserDetailsLevelXto> createUserDetailsLevel(UserDetailsLevelXto value) {
        return new JAXBElement<UserDetailsLevelXto>(_UserDetailsLevel_QNAME, UserDetailsLevelXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessInstanceHierarchyFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "processesInHierarchy", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ProcessInstanceHierarchyFilterXto> createProcessesInHierarchy(ProcessInstanceHierarchyFilterXto value) {
        return new JAXBElement<ProcessInstanceHierarchyFilterXto>(_ProcessesInHierarchy_QNAME, ProcessInstanceHierarchyFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivityStateFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "activitiesInState", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ActivityStateFilterXto> createActivitiesInState(ActivityStateFilterXto value) {
        return new JAXBElement<ActivityStateFilterXto>(_ActivitiesInState_QNAME, ActivityStateFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractCriticalityPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "AbstractCriticalityPolicy")
    public JAXBElement<AbstractCriticalityPolicyXto> createAbstractCriticalityPolicy(AbstractCriticalityPolicyXto value) {
        return new JAXBElement<AbstractCriticalityPolicyXto>(_AbstractCriticalityPolicy_QNAME, AbstractCriticalityPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractStoplightPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "abstractStoplightPolicy")
    public JAXBElement<AbstractStoplightPolicyXto> createAbstractStoplightPolicy(AbstractStoplightPolicyXto value) {
        return new JAXBElement<AbstractStoplightPolicyXto>(_AbstractStoplightPolicy_QNAME, AbstractStoplightPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartingUserFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "startingUser", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<StartingUserFilterXto> createStartingUser(StartingUserFilterXto value) {
        return new JAXBElement<StartingUserFilterXto>(_StartingUser_QNAME, StartingUserFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractCriticalDurationPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "AbstractCriticalDurationPolicy")
    public JAXBElement<AbstractCriticalDurationPolicyXto> createAbstractCriticalDurationPolicy(AbstractCriticalDurationPolicyXto value) {
        return new JAXBElement<AbstractCriticalDurationPolicyXto>(_AbstractCriticalDurationPolicy_QNAME, AbstractCriticalDurationPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivityDefinitionFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "forActivityDefinition", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ActivityDefinitionFilterXto> createForActivityDefinition(ActivityDefinitionFilterXto value) {
        return new JAXBElement<ActivityDefinitionFilterXto>(_ForActivityDefinition_QNAME, ActivityDefinitionFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformingParticipantFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "performingParticipant", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<PerformingParticipantFilterXto> createPerformingParticipant(PerformingParticipantFilterXto value) {
        return new JAXBElement<PerformingParticipantFilterXto>(_PerformingParticipant_QNAME, PerformingParticipantFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessInstanceDetailsPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "processInstanceDetailsPolicy")
    public JAXBElement<ProcessInstanceDetailsPolicyXto> createProcessInstanceDetailsPolicy(ProcessInstanceDetailsPolicyXto value) {
        return new JAXBElement<ProcessInstanceDetailsPolicyXto>(_ProcessInstanceDetailsPolicy_QNAME, ProcessInstanceDetailsPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractStoplightDurationPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "abstractStoplightDurationPolicy")
    public JAXBElement<AbstractStoplightDurationPolicyXto> createAbstractStoplightDurationPolicy(AbstractStoplightDurationPolicyXto value) {
        return new JAXBElement<AbstractStoplightDurationPolicyXto>(_AbstractStoplightDurationPolicy_QNAME, AbstractStoplightDurationPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EvaluateByWorkitemsPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "evaluateByWorkitemsPolicy")
    public JAXBElement<EvaluateByWorkitemsPolicyXto> createEvaluateByWorkitemsPolicy(EvaluateByWorkitemsPolicyXto value) {
        return new JAXBElement<EvaluateByWorkitemsPolicyXto>(_EvaluateByWorkitemsPolicy_QNAME, EvaluateByWorkitemsPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotInListPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "notInList", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<NotInListPredicateXto> createNotInList(NotInListPredicateXto value) {
        return new JAXBElement<NotInListPredicateXto>(_NotInList_QNAME, NotInListPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InListPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "inList", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<InListPredicateXto> createInList(InListPredicateXto value) {
        return new JAXBElement<InListPredicateXto>(_InList_QNAME, InListPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link HistoricalEventTypeXto }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "historicalEventTypes")
    public JAXBElement<List<HistoricalEventTypeXto>> createHistoricalEventTypes(List<HistoricalEventTypeXto> value) {
        return new JAXBElement<List<HistoricalEventTypeXto>>(_HistoricalEventTypes_QNAME, ((Class) List.class), null, ((List<HistoricalEventTypeXto> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimeoutPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "timeoutPolicy")
    public JAXBElement<TimeoutPolicyXto> createTimeoutPolicy(TimeoutPolicyXto value) {
        return new JAXBElement<TimeoutPolicyXto>(_TimeoutPolicy_QNAME, TimeoutPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotEqualPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "notEqual", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<NotEqualPredicateXto> createNotEqual(NotEqualPredicateXto value) {
        return new JAXBElement<NotEqualPredicateXto>(_NotEqual_QNAME, NotEqualPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PerformedByUserFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "performedByUser", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<PerformedByUserFilterXto> createPerformedByUser(PerformedByUserFilterXto value) {
        return new JAXBElement<PerformedByUserFilterXto>(_PerformedByUser_QNAME, PerformedByUserFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserStateFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "userState", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<UserStateFilterXto> createUserState(UserStateFilterXto value) {
        return new JAXBElement<UserStateFilterXto>(_UserState_QNAME, UserStateFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LessThanPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "lessThan", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<LessThanPredicateXto> createLessThan(LessThanPredicateXto value) {
        return new JAXBElement<LessThanPredicateXto>(_LessThan_QNAME, LessThanPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessInstanceDetailsLevelXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "processInstanceDetailsLevel")
    public JAXBElement<ProcessInstanceDetailsLevelXto> createProcessInstanceDetailsLevel(ProcessInstanceDetailsLevelXto value) {
        return new JAXBElement<ProcessInstanceDetailsLevelXto>(_ProcessInstanceDetailsLevel_QNAME, ProcessInstanceDetailsLevelXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LessOrEqualPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "lessOrEqual", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<LessOrEqualPredicateXto> createLessOrEqual(LessOrEqualPredicateXto value) {
        return new JAXBElement<LessOrEqualPredicateXto>(_LessOrEqual_QNAME, LessOrEqualPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessInstanceLinkFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "processesHavingLink", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ProcessInstanceLinkFilterXto> createProcessesHavingLink(ProcessInstanceLinkFilterXto value) {
        return new JAXBElement<ProcessInstanceLinkFilterXto>(_ProcessesHavingLink_QNAME, ProcessInstanceLinkFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AndTermXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "and", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<AndTermXto> createAnd(AndTermXto value) {
        return new JAXBElement<AndTermXto>(_And_QNAME, AndTermXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractStoplightCostPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "abstractStoplightCostPolicy")
    public JAXBElement<AbstractStoplightCostPolicyXto> createAbstractStoplightCostPolicy(AbstractStoplightCostPolicyXto value) {
        return new JAXBElement<AbstractStoplightCostPolicyXto>(_AbstractStoplightCostPolicy_QNAME, AbstractStoplightCostPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessStateFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "processesInState", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ProcessStateFilterXto> createProcessesInState(ProcessStateFilterXto value) {
        return new JAXBElement<ProcessStateFilterXto>(_ProcessesInState_QNAME, ProcessStateFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OrTermXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "or", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<OrTermXto> createOr(OrTermXto value) {
        return new JAXBElement<OrTermXto>(_Or_QNAME, OrTermXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotNullPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "notNull", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<NotNullPredicateXto> createNotNull(NotNullPredicateXto value) {
        return new JAXBElement<NotNullPredicateXto>(_NotNull_QNAME, NotNullPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GreaterOrEqualPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "greaterOrEqual", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<GreaterOrEqualPredicateXto> createGreaterOrEqual(GreaterOrEqualPredicateXto value) {
        return new JAXBElement<GreaterOrEqualPredicateXto>(_GreaterOrEqual_QNAME, GreaterOrEqualPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubFolderPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "subFolderPolicy")
    public JAXBElement<SubFolderPolicyXto> createSubFolderPolicy(SubFolderPolicyXto value) {
        return new JAXBElement<SubFolderPolicyXto>(_SubFolderPolicy_QNAME, SubFolderPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GreaterThanPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "greaterThan", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<GreaterThanPredicateXto> createGreaterThan(GreaterThanPredicateXto value) {
        return new JAXBElement<GreaterThanPredicateXto>(_GreaterThan_QNAME, GreaterThanPredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessInstanceFilterXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "forProcess", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<ProcessInstanceFilterXto> createForProcess(ProcessInstanceFilterXto value) {
        return new JAXBElement<ProcessInstanceFilterXto>(_ForProcess_QNAME, ProcessInstanceFilterXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "policy")
    public JAXBElement<PolicyXto> createPolicy(PolicyXto value) {
        return new JAXBElement<PolicyXto>(_Policy_QNAME, PolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserDetailsPolicyXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "userDetailsPolicy")
    public JAXBElement<UserDetailsPolicyXto> createUserDetailsPolicy(UserDetailsPolicyXto value) {
        return new JAXBElement<UserDetailsPolicyXto>(_UserDetailsPolicy_QNAME, UserDetailsPolicyXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IsLikePredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "isLike", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<IsLikePredicateXto> createIsLike(IsLikePredicateXto value) {
        return new JAXBElement<IsLikePredicateXto>(_IsLike_QNAME, IsLikePredicateXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttributeOrderXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "attributeOrder")
    public JAXBElement<AttributeOrderXto> createAttributeOrder(AttributeOrderXto value) {
        return new JAXBElement<AttributeOrderXto>(_AttributeOrder_QNAME, AttributeOrderXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OrderCriteriaXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "orderCriteria")
    public JAXBElement<OrderCriteriaXto> createOrderCriteria(OrderCriteriaXto value) {
        return new JAXBElement<OrderCriteriaXto>(_OrderCriteria_QNAME, OrderCriteriaXto.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IsEqualPredicateXto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eclipse.org/stardust/ws/v2012a/api/query", name = "isEqual", substitutionHeadNamespace = "http://eclipse.org/stardust/ws/v2012a/api/query", substitutionHeadName = "filterCriterion")
    public JAXBElement<IsEqualPredicateXto> createIsEqual(IsEqualPredicateXto value) {
        return new JAXBElement<IsEqualPredicateXto>(_IsEqual_QNAME, IsEqualPredicateXto.class, null, value);
    }

}
