
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * 
 *  			The ActivityInstance represents a snapshot of the execution state of an activity instance.
 * 			
 * 
 * <p>Java class for ActivityInstance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivityInstance">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="modelElementId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modelElementOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="activityId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="activityName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="processOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="processDefinitionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="processDefinitionName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="conditionalPerformerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="conditionalPerformerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="lastModificationTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="currentPerformer" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo" minOccurs="0"/>
 *         &lt;element name="performedBy" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo" minOccurs="0"/>
 *         &lt;element name="performedOnBehalfOf" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo" minOccurs="0"/>
 *         &lt;element name="userPerformer" type="{http://eclipse.org/stardust/ws/v2012a/api}User" minOccurs="0"/>
 *         &lt;element name="assignedToModelParticipant" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="assignedToUser" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="assignedToUserGroup" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="scopeProcessInstanceNoteAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="state" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstanceState"/>
 *         &lt;element name="permissionStates" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionStates" minOccurs="0"/>
 *         &lt;element name="instanceProperties" type="{http://eclipse.org/stardust/ws/v2012a/api}InstanceProperties" minOccurs="0"/>
 *         &lt;element name="descriptorDefinitions" type="{http://eclipse.org/stardust/ws/v2012a/api}DataPaths" minOccurs="0"/>
 *         &lt;element name="historicalStates" type="{http://eclipse.org/stardust/ws/v2012a/api}HistoricalStates" minOccurs="0"/>
 *         &lt;element name="historicalEvents" type="{http://eclipse.org/stardust/ws/v2012a/api}HistoricalEvents" minOccurs="0"/>
 *         &lt;element name="criticality" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="qualityAssuranceInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}QualityAssuranceInfo" minOccurs="0"/>
 *         &lt;element name="qualityAssuranceState" type="{http://eclipse.org/stardust/ws/v2012a/api}QualityAssuranceState" minOccurs="0"/>
 *         &lt;element name="attributes" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstanceAttributes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityInstance", propOrder = {
    "oid",
    "modelElementId",
    "modelElementOid",
    "modelOid",
    "activityId",
    "activityName",
    "processOid",
    "processDefinitionId",
    "processDefinitionName",
    "conditionalPerformerId",
    "conditionalPerformerName",
    "startTime",
    "lastModificationTime",
    "currentPerformer",
    "performedBy",
    "performedOnBehalfOf",
    "userPerformer",
    "assignedToModelParticipant",
    "assignedToUser",
    "assignedToUserGroup",
    "scopeProcessInstanceNoteAvailable",
    "state",
    "permissionStates",
    "instanceProperties",
    "descriptorDefinitions",
    "historicalStates",
    "historicalEvents",
    "criticality",
    "qualityAssuranceInfo",
    "qualityAssuranceState",
    "attributes"
})
public class ActivityInstanceXto {

    @XmlElement(required = true, type = Long.class, nillable = true)
    protected Long oid;
    @XmlElement(required = true)
    protected String modelElementId;
    protected int modelElementOid;
    protected int modelOid;
    @XmlElement(required = true)
    protected String activityId;
    @XmlElement(required = true)
    protected String activityName;
    protected long processOid;
    @XmlElement(required = true)
    protected String processDefinitionId;
    @XmlElement(required = true)
    protected String processDefinitionName;
    protected String conditionalPerformerId;
    protected String conditionalPerformerName;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date startTime;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date lastModificationTime;
    protected ParticipantInfoXto currentPerformer;
    protected UserInfoXto performedBy;
    protected UserInfoXto performedOnBehalfOf;
    protected UserXto userPerformer;
    protected boolean assignedToModelParticipant;
    protected boolean assignedToUser;
    protected boolean assignedToUserGroup;
    protected boolean scopeProcessInstanceNoteAvailable;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter3 .class)
    protected ActivityInstanceState state;
    protected PermissionStatesXto permissionStates;
    protected InstancePropertiesXto instanceProperties;
    protected DataPathsXto descriptorDefinitions;
    protected HistoricalStatesXto historicalStates;
    protected HistoricalEventsXto historicalEvents;
    protected double criticality;
    protected QualityAssuranceInfoXto qualityAssuranceInfo;
    protected QualityAssuranceStateXto qualityAssuranceState;
    protected ActivityInstanceAttributesXto attributes;

    /**
     * Gets the value of the oid property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOid(Long value) {
        this.oid = value;
    }

    /**
     * Gets the value of the modelElementId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelElementId() {
        return modelElementId;
    }

    /**
     * Sets the value of the modelElementId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelElementId(String value) {
        this.modelElementId = value;
    }

    /**
     * Gets the value of the modelElementOid property.
     * 
     */
    public int getModelElementOid() {
        return modelElementOid;
    }

    /**
     * Sets the value of the modelElementOid property.
     * 
     */
    public void setModelElementOid(int value) {
        this.modelElementOid = value;
    }

    /**
     * Gets the value of the modelOid property.
     * 
     */
    public int getModelOid() {
        return modelOid;
    }

    /**
     * Sets the value of the modelOid property.
     * 
     */
    public void setModelOid(int value) {
        this.modelOid = value;
    }

    /**
     * Gets the value of the activityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * Sets the value of the activityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityId(String value) {
        this.activityId = value;
    }

    /**
     * Gets the value of the activityName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * Sets the value of the activityName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityName(String value) {
        this.activityName = value;
    }

    /**
     * Gets the value of the processOid property.
     * 
     */
    public long getProcessOid() {
        return processOid;
    }

    /**
     * Sets the value of the processOid property.
     * 
     */
    public void setProcessOid(long value) {
        this.processOid = value;
    }

    /**
     * Gets the value of the processDefinitionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    /**
     * Sets the value of the processDefinitionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessDefinitionId(String value) {
        this.processDefinitionId = value;
    }

    /**
     * Gets the value of the processDefinitionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    /**
     * Sets the value of the processDefinitionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessDefinitionName(String value) {
        this.processDefinitionName = value;
    }

    /**
     * Gets the value of the conditionalPerformerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConditionalPerformerId() {
        return conditionalPerformerId;
    }

    /**
     * Sets the value of the conditionalPerformerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConditionalPerformerId(String value) {
        this.conditionalPerformerId = value;
    }

    /**
     * Gets the value of the conditionalPerformerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConditionalPerformerName() {
        return conditionalPerformerName;
    }

    /**
     * Sets the value of the conditionalPerformerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConditionalPerformerName(String value) {
        this.conditionalPerformerName = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartTime(Date value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the lastModificationTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    /**
     * Sets the value of the lastModificationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastModificationTime(Date value) {
        this.lastModificationTime = value;
    }

    /**
     * Gets the value of the currentPerformer property.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public ParticipantInfoXto getCurrentPerformer() {
        return currentPerformer;
    }

    /**
     * Sets the value of the currentPerformer property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public void setCurrentPerformer(ParticipantInfoXto value) {
        this.currentPerformer = value;
    }

    /**
     * Gets the value of the performedBy property.
     * 
     * @return
     *     possible object is
     *     {@link UserInfoXto }
     *     
     */
    public UserInfoXto getPerformedBy() {
        return performedBy;
    }

    /**
     * Sets the value of the performedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfoXto }
     *     
     */
    public void setPerformedBy(UserInfoXto value) {
        this.performedBy = value;
    }

    /**
     * Gets the value of the performedOnBehalfOf property.
     * 
     * @return
     *     possible object is
     *     {@link UserInfoXto }
     *     
     */
    public UserInfoXto getPerformedOnBehalfOf() {
        return performedOnBehalfOf;
    }

    /**
     * Sets the value of the performedOnBehalfOf property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfoXto }
     *     
     */
    public void setPerformedOnBehalfOf(UserInfoXto value) {
        this.performedOnBehalfOf = value;
    }

    /**
     * Gets the value of the userPerformer property.
     * 
     * @return
     *     possible object is
     *     {@link UserXto }
     *     
     */
    public UserXto getUserPerformer() {
        return userPerformer;
    }

    /**
     * Sets the value of the userPerformer property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserXto }
     *     
     */
    public void setUserPerformer(UserXto value) {
        this.userPerformer = value;
    }

    /**
     * Gets the value of the assignedToModelParticipant property.
     * 
     */
    public boolean isAssignedToModelParticipant() {
        return assignedToModelParticipant;
    }

    /**
     * Sets the value of the assignedToModelParticipant property.
     * 
     */
    public void setAssignedToModelParticipant(boolean value) {
        this.assignedToModelParticipant = value;
    }

    /**
     * Gets the value of the assignedToUser property.
     * 
     */
    public boolean isAssignedToUser() {
        return assignedToUser;
    }

    /**
     * Sets the value of the assignedToUser property.
     * 
     */
    public void setAssignedToUser(boolean value) {
        this.assignedToUser = value;
    }

    /**
     * Gets the value of the assignedToUserGroup property.
     * 
     */
    public boolean isAssignedToUserGroup() {
        return assignedToUserGroup;
    }

    /**
     * Sets the value of the assignedToUserGroup property.
     * 
     */
    public void setAssignedToUserGroup(boolean value) {
        this.assignedToUserGroup = value;
    }

    /**
     * Gets the value of the scopeProcessInstanceNoteAvailable property.
     * 
     */
    public boolean isScopeProcessInstanceNoteAvailable() {
        return scopeProcessInstanceNoteAvailable;
    }

    /**
     * Sets the value of the scopeProcessInstanceNoteAvailable property.
     * 
     */
    public void setScopeProcessInstanceNoteAvailable(boolean value) {
        this.scopeProcessInstanceNoteAvailable = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ActivityInstanceState getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(ActivityInstanceState value) {
        this.state = value;
    }

    /**
     * Gets the value of the permissionStates property.
     * 
     * @return
     *     possible object is
     *     {@link PermissionStatesXto }
     *     
     */
    public PermissionStatesXto getPermissionStates() {
        return permissionStates;
    }

    /**
     * Sets the value of the permissionStates property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionStatesXto }
     *     
     */
    public void setPermissionStates(PermissionStatesXto value) {
        this.permissionStates = value;
    }

    /**
     * Gets the value of the instanceProperties property.
     * 
     * @return
     *     possible object is
     *     {@link InstancePropertiesXto }
     *     
     */
    public InstancePropertiesXto getInstanceProperties() {
        return instanceProperties;
    }

    /**
     * Sets the value of the instanceProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link InstancePropertiesXto }
     *     
     */
    public void setInstanceProperties(InstancePropertiesXto value) {
        this.instanceProperties = value;
    }

    /**
     * Gets the value of the descriptorDefinitions property.
     * 
     * @return
     *     possible object is
     *     {@link DataPathsXto }
     *     
     */
    public DataPathsXto getDescriptorDefinitions() {
        return descriptorDefinitions;
    }

    /**
     * Sets the value of the descriptorDefinitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataPathsXto }
     *     
     */
    public void setDescriptorDefinitions(DataPathsXto value) {
        this.descriptorDefinitions = value;
    }

    /**
     * Gets the value of the historicalStates property.
     * 
     * @return
     *     possible object is
     *     {@link HistoricalStatesXto }
     *     
     */
    public HistoricalStatesXto getHistoricalStates() {
        return historicalStates;
    }

    /**
     * Sets the value of the historicalStates property.
     * 
     * @param value
     *     allowed object is
     *     {@link HistoricalStatesXto }
     *     
     */
    public void setHistoricalStates(HistoricalStatesXto value) {
        this.historicalStates = value;
    }

    /**
     * Gets the value of the historicalEvents property.
     * 
     * @return
     *     possible object is
     *     {@link HistoricalEventsXto }
     *     
     */
    public HistoricalEventsXto getHistoricalEvents() {
        return historicalEvents;
    }

    /**
     * Sets the value of the historicalEvents property.
     * 
     * @param value
     *     allowed object is
     *     {@link HistoricalEventsXto }
     *     
     */
    public void setHistoricalEvents(HistoricalEventsXto value) {
        this.historicalEvents = value;
    }

    /**
     * Gets the value of the criticality property.
     * 
     */
    public double getCriticality() {
        return criticality;
    }

    /**
     * Sets the value of the criticality property.
     * 
     */
    public void setCriticality(double value) {
        this.criticality = value;
    }

    /**
     * Gets the value of the qualityAssuranceInfo property.
     * 
     * @return
     *     possible object is
     *     {@link QualityAssuranceInfoXto }
     *     
     */
    public QualityAssuranceInfoXto getQualityAssuranceInfo() {
        return qualityAssuranceInfo;
    }

    /**
     * Sets the value of the qualityAssuranceInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualityAssuranceInfoXto }
     *     
     */
    public void setQualityAssuranceInfo(QualityAssuranceInfoXto value) {
        this.qualityAssuranceInfo = value;
    }

    /**
     * Gets the value of the qualityAssuranceState property.
     * 
     * @return
     *     possible object is
     *     {@link QualityAssuranceStateXto }
     *     
     */
    public QualityAssuranceStateXto getQualityAssuranceState() {
        return qualityAssuranceState;
    }

    /**
     * Sets the value of the qualityAssuranceState property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualityAssuranceStateXto }
     *     
     */
    public void setQualityAssuranceState(QualityAssuranceStateXto value) {
        this.qualityAssuranceState = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link ActivityInstanceAttributesXto }
     *     
     */
    public ActivityInstanceAttributesXto getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityInstanceAttributesXto }
     *     
     */
    public void setAttributes(ActivityInstanceAttributesXto value) {
        this.attributes = value;
    }

}
