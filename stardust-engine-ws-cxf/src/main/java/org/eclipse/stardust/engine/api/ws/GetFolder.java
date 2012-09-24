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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


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
 *         &lt;element name="folderId" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "", propOrder = {
    "folderId",
    "folderLevelOfDetail",
    "documentMetaDataType",
    "folderMetaDataType"
})
@XmlRootElement(name = "getFolder")
public class GetFolder {

    @XmlElement(required = true, nillable = true)
    protected String folderId;
    @XmlElement(required = true, nillable = true)
    protected FolderLevelOfDetailXto folderLevelOfDetail;
    @XmlElement(required = true, nillable = true)
    protected QName documentMetaDataType;
    @XmlElement(required = true, nillable = true)
    protected QName folderMetaDataType;

    /**
     * Gets the value of the folderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Sets the value of the folderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolderId(String value) {
        this.folderId = value;
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
