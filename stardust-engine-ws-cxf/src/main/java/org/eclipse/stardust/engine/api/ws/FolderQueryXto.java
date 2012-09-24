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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * 
 * 			The FolderQuery is a simple query to find folders using either a name pattern or a XPath query.
 * 			Specifying a metaDataType tries to retrieve and include metaData of that type for the queries results.
 * 			The default level of detail for folders is including direct members.
 * 			
 * 
 * <p>Java class for FolderQuery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FolderQuery">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="namePattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="xpathQuery" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="folderLevelOfDetail" type="{http://eclipse.org/stardust/ws/v2012a/api}FolderLevelOfDetail"/>
 *         &lt;element name="documentMetaDataType" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="folderMetaDataType" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FolderQuery", propOrder = {
    "namePattern",
    "xpathQuery",
    "folderLevelOfDetail",
    "documentMetaDataType",
    "folderMetaDataType"
})
public class FolderQueryXto {

    protected String namePattern;
    protected String xpathQuery;
    @XmlElement(required = true, nillable = true)
    protected FolderLevelOfDetailXto folderLevelOfDetail;
    @XmlElement(required = true, nillable = true)
    protected QName documentMetaDataType;
    @XmlElement(required = true, nillable = true)
    protected QName folderMetaDataType;

    /**
     * Gets the value of the namePattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNamePattern() {
        return namePattern;
    }

    /**
     * Sets the value of the namePattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNamePattern(String value) {
        this.namePattern = value;
    }

    /**
     * Gets the value of the xpathQuery property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXpathQuery() {
        return xpathQuery;
    }

    /**
     * Sets the value of the xpathQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXpathQuery(String value) {
        this.xpathQuery = value;
    }

    /**
     * Gets the value of the folderLevelOfDetail property.
     * 
     * @return
     *     possible object is
     *     {@link FolderLevelOfDetailXto }
     *     
     */
    public FolderLevelOfDetailXto getFolderLevelOfDetail() {
        return folderLevelOfDetail;
    }

    /**
     * Sets the value of the folderLevelOfDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderLevelOfDetailXto }
     *     
     */
    public void setFolderLevelOfDetail(FolderLevelOfDetailXto value) {
        this.folderLevelOfDetail = value;
    }

    /**
     * Gets the value of the documentMetaDataType property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getDocumentMetaDataType() {
        return documentMetaDataType;
    }

    /**
     * Sets the value of the documentMetaDataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setDocumentMetaDataType(QName value) {
        this.documentMetaDataType = value;
    }

    /**
     * Gets the value of the folderMetaDataType property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getFolderMetaDataType() {
        return folderMetaDataType;
    }

    /**
     * Sets the value of the folderMetaDataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setFolderMetaDataType(QName value) {
        this.folderMetaDataType = value;
    }

}
