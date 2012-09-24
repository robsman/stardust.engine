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
 * <p>Java class for ImplementationProcessesMap complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ImplementationProcessesMap">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="implementationProcessMapEntry" type="{http://eclipse.org/stardust/ws/v2012a/api}ImplementationProcessesMapEntry" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImplementationProcessesMap", propOrder = {
    "implementationProcessMapEntry"
})
public class ImplementationProcessesMapXto {

    protected List<ImplementationProcessesMapEntryXto> implementationProcessMapEntry;

    /**
     * Gets the value of the implementationProcessMapEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the implementationProcessMapEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImplementationProcessMapEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ImplementationProcessesMapEntryXto }
     * 
     * 
     */
    public List<ImplementationProcessesMapEntryXto> getImplementationProcessMapEntry() {
        if (implementationProcessMapEntry == null) {
            implementationProcessMapEntry = new ArrayList<ImplementationProcessesMapEntryXto>();
        }
        return this.implementationProcessMapEntry;
    }

}
