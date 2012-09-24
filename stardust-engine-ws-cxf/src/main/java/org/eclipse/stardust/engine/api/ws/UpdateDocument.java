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

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="documentId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="documentInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentInfo"/>
 *         &lt;element name="content" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="versionInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentVersionInfo" minOccurs="0"/>
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
    "documentId",
    "documentInfo",
    "content",
    "versionInfo"
})
@XmlRootElement(name = "updateDocument")
public class UpdateDocument {

    @XmlElement(required = true, nillable = true)
    protected String documentId;
    @XmlElement(required = true, nillable = true)
    protected DocumentInfoXto documentInfo;
    @XmlElementRef(name = "content", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<DataHandler> content;
    @XmlElementRef(name = "versionInfo", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<DocumentVersionInfoXto> versionInfo;

    /**
     * Gets the value of the documentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the value of the documentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentId(String value) {
        this.documentId = value;
    }

    /**
     * Gets the value of the documentInfo property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentInfoXto }
     *     
     */
    public DocumentInfoXto getDocumentInfo() {
        return documentInfo;
    }

    /**
     * Sets the value of the documentInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentInfoXto }
     *     
     */
    public void setDocumentInfo(DocumentInfoXto value) {
        this.documentInfo = value;
    }

    /**
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DataHandler }{@code >}
     *     
     */
    public JAXBElement<DataHandler> getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DataHandler }{@code >}
     *     
     */
    public void setContent(JAXBElement<DataHandler> value) {
        this.content = ((JAXBElement<DataHandler> ) value);
    }

    /**
     * Gets the value of the versionInfo property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DocumentVersionInfoXto }{@code >}
     *     
     */
    public JAXBElement<DocumentVersionInfoXto> getVersionInfo() {
        return versionInfo;
    }

    /**
     * Sets the value of the versionInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DocumentVersionInfoXto }{@code >}
     *     
     */
    public void setVersionInfo(JAXBElement<DocumentVersionInfoXto> value) {
        this.versionInfo = ((JAXBElement<DocumentVersionInfoXto> ) value);
    }

}
