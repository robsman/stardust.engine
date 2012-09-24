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

package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * 
 * 	        The ProcessInstance represents a snapshot of the execution state of an process instance.
 * 	        
 * 
 * <p>Java class for ProcessInstance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
 *         &lt;element name="priority" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="terminationTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="startingUser" type="{http://eclipse.org/stardust/ws/v2012a/api}User" minOccurs="0"/>
 *         &lt;element name="state" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceState"/>
 *         &lt;element name="detailsLevel" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceDetailsLevel"/>
 *         &lt;element name="detailsOptions" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceDetailsOptions"/>
 *         &lt;element name="instanceProperties" type="{http://eclipse.org/stardust/ws/v2012a/api}InstanceProperties" minOccurs="0"/>
 *         &lt;element name="historicalEvents" type="{http://eclipse.org/stardust/ws/v2012a/api}HistoricalEvents" minOccurs="0"/>
 *         &lt;element name="caseProcessInstance" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="linkedProcessInstances" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceLinks" minOccurs="0"/>
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
    "priority",
    "startTime",
    "terminationTime",
    "startingUser",
    "state",
    "detailsLevel",
    "detailsOptions",
    "instanceProperties",
    "historicalEvents",
    "caseProcessInstance",
    "linkedProcessInstances"
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
    @XmlJavaTypeAdapter(Adapter2 .class)
    protected ProcessInstanceState state;
    @XmlElement(required = true)
    protected ProcessInstanceDetailsLevelXto detailsLevel;
    @XmlElement(required = true)
    protected ProcessInstanceDetailsOptionsXto detailsOptions;
    protected InstancePropertiesXto instanceProperties;
    protected HistoricalEventsXto historicalEvents;
    protected boolean caseProcessInstance;
    protected ProcessInstanceLinksXto linkedProcessInstances;

    /**
     * Gets the value of the oid property.
     * 
     */
    public long getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     */
    public void setOid(long value) {
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
     * Gets the value of the rootProcessOid property.
     * 
     */
    public long getRootProcessOid() {
        return rootProcessOid;
    }

    /**
     * Sets the value of the rootProcessOid property.
     * 
     */
    public void setRootProcessOid(long value) {
        this.rootProcessOid = value;
    }

    /**
     * Gets the value of the scopeProcessOid property.
     * 
     */
    public long getScopeProcessOid() {
        return scopeProcessOid;
    }

    /**
     * Sets the value of the scopeProcessOid property.
     * 
     */
    public void setScopeProcessOid(long value) {
        this.scopeProcessOid = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     */
    public void setPriority(int value) {
        this.priority = value;
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
     * Gets the value of the terminationTime property.
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
     * Sets the value of the terminationTime property.
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
     * Gets the value of the startingUser property.
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
     * Sets the value of the startingUser property.
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
     * Gets the value of the state property.
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
     * Sets the value of the state property.
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
     * Gets the value of the detailsLevel property.
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
     * Sets the value of the detailsLevel property.
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
     * Gets the value of the detailsOptions property.
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
     * Sets the value of the detailsOptions property.
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
     * Gets the value of the caseProcessInstance property.
     * 
     */
    public boolean isCaseProcessInstance() {
        return caseProcessInstance;
    }

    /**
     * Sets the value of the caseProcessInstance property.
     * 
     */
    public void setCaseProcessInstance(boolean value) {
        this.caseProcessInstance = value;
    }

    /**
     * Gets the value of the linkedProcessInstances property.
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
     * Sets the value of the linkedProcessInstances property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceLinksXto }
     *     
     */
    public void setLinkedProcessInstances(ProcessInstanceLinksXto value) {
        this.linkedProcessInstances = value;
    }

}
