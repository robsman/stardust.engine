
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;


/**
 * 
 * 	        The ProcessInstance represents a snapshot of the execution state of an process instance.
 * 	        
 * 
 * <p>Java-Klasse für ProcessInstance complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstance">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="modelElementId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modelElementOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="processDefinitionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="processDefinitionName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="rootProcessOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="scopeProcessOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="parentProcessOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="priority" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="terminationTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="startingUser" type="{http://eclipse.org/stardust/ws/v2012a/api}User" minOccurs="0"/>
 *         &lt;element name="state" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceState"/>
 *         &lt;element name="detailsLevel" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceDetailsLevel"/>
 *         &lt;element name="detailsOptions" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceDetailsOptions"/>
 *         &lt;element name="instanceProperties" type="{http://eclipse.org/stardust/ws/v2012a/api}InstanceProperties" minOccurs="0"/>
 *         &lt;element name="historicalEvents" type="{http://eclipse.org/stardust/ws/v2012a/api}HistoricalEvents" minOccurs="0"/>
 *         &lt;element name="permissionStates" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionStates" minOccurs="0"/>
 *         &lt;element name="caseProcessInstance" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="linkedProcessInstances" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceLinks" minOccurs="0"/>
 *         &lt;element name="descriptorDefinitions" type="{http://eclipse.org/stardust/ws/v2012a/api}DataPaths" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstance", propOrder = {
    "oid",
    "modelElementId",
    "modelElementOid",
    "modelOid",
    "processDefinitionId",
    "processDefinitionName",
    "rootProcessOid",
    "scopeProcessOid",
    "parentProcessOid",
    "priority",
    "startTime",
    "terminationTime",
    "startingUser",
    "state",
    "detailsLevel",
    "detailsOptions",
    "instanceProperties",
    "historicalEvents",
    "permissionStates",
    "caseProcessInstance",
    "linkedProcessInstances",
    "descriptorDefinitions"
})
public class ProcessInstanceXto {

    protected long oid;
    @XmlElement(required = true)
    protected String modelElementId;
    protected int modelElementOid;
    protected int modelOid;
    @XmlElement(required = true)
    protected String processDefinitionId;
    @XmlElement(required = true)
    protected String processDefinitionName;
    protected long rootProcessOid;
    protected long scopeProcessOid;
    protected long parentProcessOid;
    protected int priority;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date startTime;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date terminationTime;
    protected UserXto startingUser;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter3 .class)
    protected ProcessInstanceState state;
    @XmlElement(required = true)
    protected ProcessInstanceDetailsLevelXto detailsLevel;
    @XmlElement(required = true)
    protected ProcessInstanceDetailsOptionsXto detailsOptions;
    protected InstancePropertiesXto instanceProperties;
    protected HistoricalEventsXto historicalEvents;
    protected PermissionStatesXto permissionStates;
    protected boolean caseProcessInstance;
    protected ProcessInstanceLinksXto linkedProcessInstances;
    protected DataPathsXto descriptorDefinitions;

    /**
     * Ruft den Wert der oid-Eigenschaft ab.
     * 
     */
    public long getOid() {
        return oid;
    }

    /**
     * Legt den Wert der oid-Eigenschaft fest.
     * 
     */
    public void setOid(long value) {
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
     * Ruft den Wert der rootProcessOid-Eigenschaft ab.
     * 
     */
    public long getRootProcessOid() {
        return rootProcessOid;
    }

    /**
     * Legt den Wert der rootProcessOid-Eigenschaft fest.
     * 
     */
    public void setRootProcessOid(long value) {
        this.rootProcessOid = value;
    }

    /**
     * Ruft den Wert der scopeProcessOid-Eigenschaft ab.
     * 
     */
    public long getScopeProcessOid() {
        return scopeProcessOid;
    }

    /**
     * Legt den Wert der scopeProcessOid-Eigenschaft fest.
     * 
     */
    public void setScopeProcessOid(long value) {
        this.scopeProcessOid = value;
    }

    /**
     * Ruft den Wert der parentProcessOid-Eigenschaft ab.
     * 
     */
    public long getParentProcessOid() {
        return parentProcessOid;
    }

    /**
     * Legt den Wert der parentProcessOid-Eigenschaft fest.
     * 
     */
    public void setParentProcessOid(long value) {
        this.parentProcessOid = value;
    }

    /**
     * Ruft den Wert der priority-Eigenschaft ab.
     * 
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Legt den Wert der priority-Eigenschaft fest.
     * 
     */
    public void setPriority(int value) {
        this.priority = value;
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
     * Ruft den Wert der terminationTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getTerminationTime() {
        return terminationTime;
    }

    /**
     * Legt den Wert der terminationTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminationTime(Date value) {
        this.terminationTime = value;
    }

    /**
     * Ruft den Wert der startingUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserXto }
     *     
     */
    public UserXto getStartingUser() {
        return startingUser;
    }

    /**
     * Legt den Wert der startingUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserXto }
     *     
     */
    public void setStartingUser(UserXto value) {
        this.startingUser = value;
    }

    /**
     * Ruft den Wert der state-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ProcessInstanceState getState() {
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
    public void setState(ProcessInstanceState value) {
        this.state = value;
    }

    /**
     * Ruft den Wert der detailsLevel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceDetailsLevelXto }
     *     
     */
    public ProcessInstanceDetailsLevelXto getDetailsLevel() {
        return detailsLevel;
    }

    /**
     * Legt den Wert der detailsLevel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceDetailsLevelXto }
     *     
     */
    public void setDetailsLevel(ProcessInstanceDetailsLevelXto value) {
        this.detailsLevel = value;
    }

    /**
     * Ruft den Wert der detailsOptions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceDetailsOptionsXto }
     *     
     */
    public ProcessInstanceDetailsOptionsXto getDetailsOptions() {
        return detailsOptions;
    }

    /**
     * Legt den Wert der detailsOptions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceDetailsOptionsXto }
     *     
     */
    public void setDetailsOptions(ProcessInstanceDetailsOptionsXto value) {
        this.detailsOptions = value;
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
     * Ruft den Wert der caseProcessInstance-Eigenschaft ab.
     * 
     */
    public boolean isCaseProcessInstance() {
        return caseProcessInstance;
    }

    /**
     * Legt den Wert der caseProcessInstance-Eigenschaft fest.
     * 
     */
    public void setCaseProcessInstance(boolean value) {
        this.caseProcessInstance = value;
    }

    /**
     * Ruft den Wert der linkedProcessInstances-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceLinksXto }
     *     
     */
    public ProcessInstanceLinksXto getLinkedProcessInstances() {
        return linkedProcessInstances;
    }

    /**
     * Legt den Wert der linkedProcessInstances-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceLinksXto }
     *     
     */
    public void setLinkedProcessInstances(ProcessInstanceLinksXto value) {
        this.linkedProcessInstances = value;
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

}
