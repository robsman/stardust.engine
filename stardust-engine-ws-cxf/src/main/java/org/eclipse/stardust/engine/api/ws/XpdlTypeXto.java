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
 * 	        XPDL specific type declaration definition object.
 * 	        
 * 
 * <p>Java class for XpdlType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XpdlType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="externalReference" type="{http://eclipse.org/stardust/ws/v2012a/api}ExternalReference" minOccurs="0"/>
 *         &lt;element name="schemaType" type="{http://eclipse.org/stardust/ws/v2012a/api}SchemaType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XpdlType", propOrder = {
    "externalReference",
    "schemaType"
})
public class XpdlTypeXto {

    protected ExternalReferenceXto externalReference;
    protected SchemaTypeXto schemaType;

    /**
     * Gets the value of the externalReference property.
     * 
     * @return
     *     possible object is
     *     {@link ExternalReferenceXto }
     *     
     */
    public ExternalReferenceXto getExternalReference() {
        return externalReference;
    }

    /**
     * Sets the value of the externalReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExternalReferenceXto }
     *     
     */
    public void setExternalReference(ExternalReferenceXto value) {
        this.externalReference = value;
    }

    /**
     * Gets the value of the schemaType property.
     * 
     * @return
     *     possible object is
     *     {@link SchemaTypeXto }
     *     
     */
    public SchemaTypeXto getSchemaType() {
        return schemaType;
    }

    /**
     * Sets the value of the schemaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemaTypeXto }
     *     
     */
    public void setSchemaType(SchemaTypeXto value) {
        this.schemaType = value;
    }

}
