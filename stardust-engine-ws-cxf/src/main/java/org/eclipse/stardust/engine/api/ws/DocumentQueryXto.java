
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
 * <p>Java class for DocumentQuery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the metaDataType property.
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
     * Sets the value of the metaDataType property.
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
