
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BusinessObject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BusinessObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modelId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="values" type="{http://eclipse.org/stardust/ws/v2012a/api}BusinessObjectValues"/>
 *         &lt;element name="items" type="{http://eclipse.org/stardust/ws/v2012a/api}BusinessObjectDefinitions"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BusinessObject", propOrder = {
    "modelOid",
    "id",
    "modelId",
    "name",
    "values",
    "items"
})
public class BusinessObjectXto {

    protected long modelOid;
    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String modelId;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected BusinessObjectValuesXto values;
    @XmlElement(required = true)
    protected BusinessObjectDefinitionsXto items;

    /**
     * Ruft den Wert der modelOid-Eigenschaft ab.
     * 
     */
    public long getModelOid() {
        return modelOid;
    }

    /**
     * Legt den Wert der modelOid-Eigenschaft fest.
     * 
     */
    public void setModelOid(long value) {
        this.modelOid = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der modelId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Legt den Wert der modelId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelId(String value) {
        this.modelId = value;
    }

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
     * Ruft den Wert der values-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BusinessObjectValuesXto }
     *     
     */
    public BusinessObjectValuesXto getValues() {
        return values;
    }

    /**
     * Legt den Wert der values-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BusinessObjectValuesXto }
     *     
     */
    public void setValues(BusinessObjectValuesXto value) {
        this.values = value;
    }

    /**
     * Ruft den Wert der items-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BusinessObjectDefinitionsXto }
     *     
     */
    public BusinessObjectDefinitionsXto getItems() {
        return items;
    }

    /**
     * Legt den Wert der items-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BusinessObjectDefinitionsXto }
     *     
     */
    public void setItems(BusinessObjectDefinitionsXto value) {
        this.items = value;
    }

}
