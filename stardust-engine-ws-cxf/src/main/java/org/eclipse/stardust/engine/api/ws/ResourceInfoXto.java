
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * 
 * 			Holds base information of a resource.
 * 			The resource can either be a document or folder.
 * 			
 * 
 * <p>Java-Klasse für ResourceInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ResourceInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="owner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dateCreated" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="dateLastModified" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="metaDataType" type="{http://www.w3.org/2001/XMLSchema}QName" minOccurs="0"/>
 *         &lt;element name="metaData" type="{http://eclipse.org/stardust/ws/v2012a/api}XmlValue" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourceInfo", propOrder = {
    "name",
    "description",
    "owner",
    "dateCreated",
    "dateLastModified",
    "metaDataType",
    "metaData"
})
@XmlSeeAlso({
    DocumentInfoXto.class,
    FolderInfoXto.class
})
public class ResourceInfoXto {

    @XmlElement(required = true)
    protected String name;
    protected String description;
    protected String owner;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date dateCreated;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date dateLastModified;
    protected QName metaDataType;
    protected XmlValueXto metaData;

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der description-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der owner-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Legt den Wert der owner-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    /**
     * Ruft den Wert der dateCreated-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Legt den Wert der dateCreated-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateCreated(Date value) {
        this.dateCreated = value;
    }

    /**
     * Ruft den Wert der dateLastModified-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getDateLastModified() {
        return dateLastModified;
    }

    /**
     * Legt den Wert der dateLastModified-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateLastModified(Date value) {
        this.dateLastModified = value;
    }

    /**
     * Ruft den Wert der metaDataType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getMetaDataType() {
        return metaDataType;
    }

    /**
     * Legt den Wert der metaDataType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setMetaDataType(QName value) {
        this.metaDataType = value;
    }

    /**
     * Ruft den Wert der metaData-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XmlValueXto }
     *     
     */
    public XmlValueXto getMetaData() {
        return metaData;
    }

    /**
     * Legt den Wert der metaData-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlValueXto }
     *     
     */
    public void setMetaData(XmlValueXto value) {
        this.metaData = value;
    }

}
