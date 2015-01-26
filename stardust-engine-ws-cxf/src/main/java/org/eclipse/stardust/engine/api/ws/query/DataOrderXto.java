
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		Criterion for ordering elements resulting from a query according to workflow data,
 *         either with ascending or descending values.
 *         
 * 
 * <p>Java-Klasse für DataOrder complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DataOrder">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}OrderCriterion">
 *       &lt;sequence>
 *         &lt;element name="dataId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="attribute" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ascending" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataOrder", propOrder = {
    "dataId",
    "attribute",
    "ascending"
})
public class DataOrderXto
    extends OrderCriterionXto
{

    @XmlElement(required = true)
    protected String dataId;
    protected String attribute;
    @XmlElement(defaultValue = "true")
    protected boolean ascending;

    /**
     * Ruft den Wert der dataId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataId() {
        return dataId;
    }

    /**
     * Legt den Wert der dataId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataId(String value) {
        this.dataId = value;
    }

    /**
     * Ruft den Wert der attribute-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Legt den Wert der attribute-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttribute(String value) {
        this.attribute = value;
    }

    /**
     * Ruft den Wert der ascending-Eigenschaft ab.
     * 
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Legt den Wert der ascending-Eigenschaft fest.
     * 
     */
    public void setAscending(boolean value) {
        this.ascending = value;
    }

}
