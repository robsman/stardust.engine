
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BusinessObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the modelOid property.
     * 
     */
    public long getModelOid() {
        return modelOid;
    }

    /**
     * Sets the value of the modelOid property.
     * 
     */
    public void setModelOid(long value) {
        this.modelOid = value;
    }

    /**
     * Gets the value of the id property.
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
     * Sets the value of the id property.
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
     * Gets the value of the modelId property.
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
     * Sets the value of the modelId property.
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
     * Gets the value of the name property.
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
     * Sets the value of the name property.
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
     * Gets the value of the values property.
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
     * Sets the value of the values property.
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
     * Gets the value of the items property.
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
     * Sets the value of the items property.
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
