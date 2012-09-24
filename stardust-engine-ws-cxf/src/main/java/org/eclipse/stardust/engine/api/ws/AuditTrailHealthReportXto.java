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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Provides key indicators of audit trail health.
 * 			
 * 
 * <p>Java class for AuditTrailHealthReport complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuditTrailHealthReport">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="numberOfProcessInstancesLackingCompletion" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfProcessInstancesLackingAbortion" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfActivityInstancesLackingAbortion" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfProcessInstancesHavingCrashedActivities" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfProcessInstancesHavingCrashedThreads" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfProcessInstancesHavingCrashedEventBindings" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditTrailHealthReport", propOrder = {
    "numberOfProcessInstancesLackingCompletion",
    "numberOfProcessInstancesLackingAbortion",
    "numberOfActivityInstancesLackingAbortion",
    "numberOfProcessInstancesHavingCrashedActivities",
    "numberOfProcessInstancesHavingCrashedThreads",
    "numberOfProcessInstancesHavingCrashedEventBindings"
})
public class AuditTrailHealthReportXto {

    protected long numberOfProcessInstancesLackingCompletion;
    protected long numberOfProcessInstancesLackingAbortion;
    protected long numberOfActivityInstancesLackingAbortion;
    protected long numberOfProcessInstancesHavingCrashedActivities;
    protected long numberOfProcessInstancesHavingCrashedThreads;
    protected long numberOfProcessInstancesHavingCrashedEventBindings;

    /**
     * Gets the value of the numberOfProcessInstancesLackingCompletion property.
     * 
     */
    public long getNumberOfProcessInstancesLackingCompletion() {
        return numberOfProcessInstancesLackingCompletion;
    }

    /**
     * Sets the value of the numberOfProcessInstancesLackingCompletion property.
     * 
     */
    public void setNumberOfProcessInstancesLackingCompletion(long value) {
        this.numberOfProcessInstancesLackingCompletion = value;
    }

    /**
     * Gets the value of the numberOfProcessInstancesLackingAbortion property.
     * 
     */
    public long getNumberOfProcessInstancesLackingAbortion() {
        return numberOfProcessInstancesLackingAbortion;
    }

    /**
     * Sets the value of the numberOfProcessInstancesLackingAbortion property.
     * 
     */
    public void setNumberOfProcessInstancesLackingAbortion(long value) {
        this.numberOfProcessInstancesLackingAbortion = value;
    }

    /**
     * Gets the value of the numberOfActivityInstancesLackingAbortion property.
     * 
     */
    public long getNumberOfActivityInstancesLackingAbortion() {
        return numberOfActivityInstancesLackingAbortion;
    }

    /**
     * Sets the value of the numberOfActivityInstancesLackingAbortion property.
     * 
     */
    public void setNumberOfActivityInstancesLackingAbortion(long value) {
        this.numberOfActivityInstancesLackingAbortion = value;
    }

    /**
     * Gets the value of the numberOfProcessInstancesHavingCrashedActivities property.
     * 
     */
    public long getNumberOfProcessInstancesHavingCrashedActivities() {
        return numberOfProcessInstancesHavingCrashedActivities;
    }

    /**
     * Sets the value of the numberOfProcessInstancesHavingCrashedActivities property.
     * 
     */
    public void setNumberOfProcessInstancesHavingCrashedActivities(long value) {
        this.numberOfProcessInstancesHavingCrashedActivities = value;
    }

    /**
     * Gets the value of the numberOfProcessInstancesHavingCrashedThreads property.
     * 
     */
    public long getNumberOfProcessInstancesHavingCrashedThreads() {
        return numberOfProcessInstancesHavingCrashedThreads;
    }

    /**
     * Sets the value of the numberOfProcessInstancesHavingCrashedThreads property.
     * 
     */
    public void setNumberOfProcessInstancesHavingCrashedThreads(long value) {
        this.numberOfProcessInstancesHavingCrashedThreads = value;
    }

    /**
     * Gets the value of the numberOfProcessInstancesHavingCrashedEventBindings property.
     * 
     */
    public long getNumberOfProcessInstancesHavingCrashedEventBindings() {
        return numberOfProcessInstancesHavingCrashedEventBindings;
    }

    /**
     * Sets the value of the numberOfProcessInstancesHavingCrashedEventBindings property.
     * 
     */
    public void setNumberOfProcessInstancesHavingCrashedEventBindings(long value) {
        this.numberOfProcessInstancesHavingCrashedEventBindings = value;
    }

}
