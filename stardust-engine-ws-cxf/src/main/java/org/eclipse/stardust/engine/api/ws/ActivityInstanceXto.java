
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;


/**
 * 
 *  			The ActivityInstance represents a snapshot of the execution state of an activity instance.
 * 			
 * 
 * <p>Java-Klasse für ActivityInstance complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
    @XmlJavaTypeAdapter(Adapter4 .class)
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
     * Ruft den Wert der oid-Eigenschaft ab.
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
     * Legt den Wert der oid-Eigenschaft fest.
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
     * Ruft den Wert der modelElementId-Eigenschaft ab.
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
     * Legt den Wert der modelElementId-Eigenschaft fest.
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
     * Ruft den Wert der modelElementOid-Eigenschaft ab.
     * 
     */
    public int getModelElementOid() {
        return modelElementOid;
    }

    /**
     * Legt den Wert der modelElementOid-Eigenschaft fest.
     * 
     */
    public void setModelElementOid(int value) {
        this.modelElementOid = value;
    }

    /**
     * Ruft den Wert der modelOid-Eigenschaft ab.
     * 
     */
    public int getModelOid() {
        return modelOid;
    }

    /**
     * Legt den Wert der modelOid-Eigenschaft fest.
     * 
     */
    public void setModelOid(int value) {
        this.modelOid = value;
    }

    /**
     * Ruft den Wert der activityId-Eigenschaft ab.
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
     * Legt den Wert der activityId-Eigenschaft fest.
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
     * Ruft den Wert der activityName-Eigenschaft ab.
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
     * Legt den Wert der activityName-Eigenschaft fest.
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
     * Ruft den Wert der processOid-Eigenschaft ab.
     * 
     */
    public long getProcessOid() {
        return processOid;
    }

    /**
     * Legt den Wert der processOid-Eigenschaft fest.
     * 
     */
    public void setProcessOid(long value) {
        this.processOid = value;
    }

    /**
     * Ruft den Wert der processDefinitionId-Eigenschaft ab.
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
     * Legt den Wert der processDefinitionId-Eigenschaft fest.
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
     * Ruft den Wert der processDefinitionName-Eigenschaft ab.
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
     * Legt den Wert der processDefinitionName-Eigenschaft fest.
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
     * Ruft den Wert der conditionalPerformerId-Eigenschaft ab.
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
     * Legt den Wert der conditionalPerformerId-Eigenschaft fest.
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
     * Ruft den Wert der conditionalPerformerName-Eigenschaft ab.
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
     * Legt den Wert der conditionalPerformerName-Eigenschaft fest.
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
     * Ruft den Wert der startTime-Eigenschaft ab.
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
     * Legt den Wert der startTime-Eigenschaft fest.
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
     * Ruft den Wert der lastModificationTime-Eigenschaft ab.
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
     * Legt den Wert der lastModificationTime-Eigenschaft fest.
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
     * Ruft den Wert der currentPerformer-Eigenschaft ab.
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
     * Legt den Wert der currentPerformer-Eigenschaft fest.
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
     * Ruft den Wert der performedBy-Eigenschaft ab.
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
     * Legt den Wert der performedBy-Eigenschaft fest.
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
     * Ruft den Wert der performedOnBehalfOf-Eigenschaft ab.
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
     * Legt den Wert der performedOnBehalfOf-Eigenschaft fest.
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
     * Ruft den Wert der userPerformer-Eigenschaft ab.
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
     * Legt den Wert der userPerformer-Eigenschaft fest.
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
     * Ruft den Wert der assignedToModelParticipant-Eigenschaft ab.
     * 
     */
    public boolean isAssignedToModelParticipant() {
        return assignedToModelParticipant;
    }

    /**
     * Legt den Wert der assignedToModelParticipant-Eigenschaft fest.
     * 
     */
    public void setAssignedToModelParticipant(boolean value) {
        this.assignedToModelParticipant = value;
    }

    /**
     * Ruft den Wert der assignedToUser-Eigenschaft ab.
     * 
     */
    public boolean isAssignedToUser() {
        return assignedToUser;
    }

    /**
     * Legt den Wert der assignedToUser-Eigenschaft fest.
     * 
     */
    public void setAssignedToUser(boolean value) {
        this.assignedToUser = value;
    }

    /**
     * Ruft den Wert der assignedToUserGroup-Eigenschaft ab.
     * 
     */
    public boolean isAssignedToUserGroup() {
        return assignedToUserGroup;
    }

    /**
     * Legt den Wert der assignedToUserGroup-Eigenschaft fest.
     * 
     */
    public void setAssignedToUserGroup(boolean value) {
        this.assignedToUserGroup = value;
    }

    /**
     * Ruft den Wert der scopeProcessInstanceNoteAvailable-Eigenschaft ab.
     * 
     */
    public boolean isScopeProcessInstanceNoteAvailable() {
        return scopeProcessInstanceNoteAvailable;
    }

    /**
     * Legt den Wert der scopeProcessInstanceNoteAvailable-Eigenschaft fest.
     * 
     */
    public void setScopeProcessInstanceNoteAvailable(boolean value) {
        this.scopeProcessInstanceNoteAvailable = value;
    }

    /**
     * Ruft den Wert der state-Eigenschaft ab.
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
     * Legt den Wert der state-Eigenschaft fest.
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
     * Ruft den Wert der permissionStates-Eigenschaft ab.
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
     * Legt den Wert der permissionStates-Eigenschaft fest.
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
     * Ruft den Wert der instanceProperties-Eigenschaft ab.
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
     * Legt den Wert der instanceProperties-Eigenschaft fest.
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
     * Ruft den Wert der descriptorDefinitions-Eigenschaft ab.
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
     * Legt den Wert der descriptorDefinitions-Eigenschaft fest.
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
     * Ruft den Wert der historicalStates-Eigenschaft ab.
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
     * Legt den Wert der historicalStates-Eigenschaft fest.
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
     * Ruft den Wert der historicalEvents-Eigenschaft ab.
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
     * Legt den Wert der historicalEvents-Eigenschaft fest.
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
     * Ruft den Wert der criticality-Eigenschaft ab.
     * 
     */
    public double getCriticality() {
        return criticality;
    }

    /**
     * Legt den Wert der criticality-Eigenschaft fest.
     * 
     */
    public void setCriticality(double value) {
        this.criticality = value;
    }

    /**
     * Ruft den Wert der qualityAssuranceInfo-Eigenschaft ab.
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
     * Legt den Wert der qualityAssuranceInfo-Eigenschaft fest.
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
     * Ruft den Wert der qualityAssuranceState-Eigenschaft ab.
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
     * Legt den Wert der qualityAssuranceState-Eigenschaft fest.
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
     * Ruft den Wert der attributes-Eigenschaft ab.
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
     * Legt den Wert der attributes-Eigenschaft fest.
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
