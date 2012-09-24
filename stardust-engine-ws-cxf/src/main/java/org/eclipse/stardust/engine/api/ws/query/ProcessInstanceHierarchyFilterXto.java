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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Filter criterion for finding root or sub process instances.
 *         
 * 
 * <p>Java class for ProcessInstanceHierarchyFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceHierarchyFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="mode" type="{http://eclipse.org/stardust/ws/v2012a/api/query}HierarchyMode"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceHierarchyFilter", propOrder = {
    "mode"
})
public class ProcessInstanceHierarchyFilterXto
    extends PredicateBaseXto
{

    @XmlElement(required = true)
    protected HierarchyModeXto mode;

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link HierarchyModeXto }
     *     
     */
    public HierarchyModeXto getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link HierarchyModeXto }
     *     
     */
    public void setMode(HierarchyModeXto value) {
        this.mode = value;
    }

}
