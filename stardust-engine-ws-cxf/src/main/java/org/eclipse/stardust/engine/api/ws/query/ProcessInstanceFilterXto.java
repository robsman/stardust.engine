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

package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Filter criterion for matching specific process instances.
 *         
 * 
 * <p>Java class for ProcessInstanceFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="processOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *       &lt;attribute name="includingSubprocesses" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceFilter", propOrder = {
    "processOid"
})
public class ProcessInstanceFilterXto
    extends PredicateBaseXto
{

    protected long processOid;
    @XmlAttribute
    protected Boolean includingSubprocesses;

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
     * Gets the value of the includingSubprocesses property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncludingSubprocesses() {
        if (includingSubprocesses == null) {
            return true;
        } else {
            return includingSubprocesses;
        }
    }

    /**
     * Sets the value of the includingSubprocesses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludingSubprocesses(Boolean value) {
        this.includingSubprocesses = value;
    }

}
