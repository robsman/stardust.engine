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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.ProcessQueryXto;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="query" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessQuery" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "query"
})
@XmlRootElement(name = "findProcesses")
public class FindProcesses {

    @XmlElementRef(name = "query", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<ProcessQueryXto> query;

    /**
     * Gets the value of the query property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ProcessQueryXto }{@code >}
     *     
     */
    public JAXBElement<ProcessQueryXto> getQuery() {
        return query;
    }

    /**
     * Sets the value of the query property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ProcessQueryXto }{@code >}
     *     
     */
    public void setQuery(JAXBElement<ProcessQueryXto> value) {
        this.query = ((JAXBElement<ProcessQueryXto> ) value);
    }

}
