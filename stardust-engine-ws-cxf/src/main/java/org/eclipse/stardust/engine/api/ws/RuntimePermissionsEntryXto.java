
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für RuntimePermissionsEntry complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RuntimePermissionsEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="valueList" type="{http://eclipse.org/stardust/ws/v2012a/api}StringList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuntimePermissionsEntry", propOrder = {
    "name",
    "valueList"
})
public class RuntimePermissionsEntryXto {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected StringListXto valueList;

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
     * Ruft den Wert der valueList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link StringListXto }
     *     
     */
    public StringListXto getValueList() {
        return valueList;
    }

    /**
     * Legt den Wert der valueList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link StringListXto }
     *     
     */
    public void setValueList(StringListXto value) {
        this.valueList = value;
    }

}
