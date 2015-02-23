
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * 
 * 			The DocumentQuery is a simple query to find documents using either a name pattern or a XPath query.
 * 			Specifying a metaDataType tries to retrieve and include metaData of that type for the queries results.
 * 			
 * 
 * <p>Java-Klasse für DocumentQuery complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DocumentQuery">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="namePattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="xpathQuery" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="metaDataType" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentQuery", propOrder = {
    "namePattern",
    "xpathQuery",
    "metaDataType"
})
public class DocumentQueryXto {

    protected String namePattern;
    protected String xpathQuery;
    @XmlElement(required = true, nillable = true)
    protected QName metaDataType;

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

}
