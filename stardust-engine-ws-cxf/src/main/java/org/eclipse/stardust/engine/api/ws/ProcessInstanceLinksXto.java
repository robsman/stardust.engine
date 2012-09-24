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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessInstanceLinks complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceLinks">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processInstanceLink" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceLink" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceLinks", propOrder = {
    "processInstanceLink"
})
public class ProcessInstanceLinksXto {

    protected List<ProcessInstanceLinkXto> processInstanceLink;

    /**
     * Gets the value of the processInstanceLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the processInstanceLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcessInstanceLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProcessInstanceLinkXto }
     * 
     * 
     */
    public List<ProcessInstanceLinkXto> getProcessInstanceLink() {
        if (processInstanceLink == null) {
            processInstanceLink = new ArrayList<ProcessInstanceLinkXto>();
        }
        return this.processInstanceLink;
    }

}
