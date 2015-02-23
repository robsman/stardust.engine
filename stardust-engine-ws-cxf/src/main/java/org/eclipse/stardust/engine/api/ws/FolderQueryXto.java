
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
 * <p>Java-Klasse für FolderQuery complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der namePattern-Eigenschaft ab.
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
     * Legt den Wert der namePattern-Eigenschaft fest.
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
     * Ruft den Wert der xpathQuery-Eigenschaft ab.
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
     * Legt den Wert der xpathQuery-Eigenschaft fest.
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
     * Ruft den Wert der folderLevelOfDetail-Eigenschaft ab.
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
     * Legt den Wert der folderLevelOfDetail-Eigenschaft fest.
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
     * Ruft den Wert der documentMetaDataType-Eigenschaft ab.
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
     * Legt den Wert der documentMetaDataType-Eigenschaft fest.
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
     * Ruft den Wert der folderMetaDataType-Eigenschaft ab.
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
     * Legt den Wert der folderMetaDataType-Eigenschaft fest.
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
