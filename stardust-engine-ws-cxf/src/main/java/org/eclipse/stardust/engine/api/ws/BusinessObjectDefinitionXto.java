
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for BusinessObjectDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BusinessObjectDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="typeName" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="primaryKey" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="items" type="{http://eclipse.org/stardust/ws/v2012a/api}BusinessObjectDefinitions" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="isList" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BusinessObjectDefinition", propOrder = {
    "name",
    "type",
    "typeName",
    "key",
    "primaryKey",
    "items",
    "isList"
})
public class BusinessObjectDefinitionXto {

    @XmlElement(required = true)
    protected String name;
    protected int type;
    @XmlElement(required = true)
    protected QName typeName;
    protected boolean key;
    protected boolean primaryKey;
    protected List<BusinessObjectDefinitionsXto> items;
    protected boolean isList;

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
     * Gets the value of the type property.
     * 
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     */
    public void setType(int value) {
        this.type = value;
    }

    /**
     * Gets the value of the typeName property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getTypeName() {
        return typeName;
    }

    /**
     * Sets the value of the typeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setTypeName(QName value) {
        this.typeName = value;
    }

    /**
     * Gets the value of the key property.
     * 
     */
    public boolean isKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     */
    public void setKey(boolean value) {
        this.key = value;
    }

    /**
     * Gets the value of the primaryKey property.
     * 
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    /**
     * Sets the value of the primaryKey property.
     * 
     */
    public void setPrimaryKey(boolean value) {
        this.primaryKey = value;
    }

    /**
     * Gets the value of the items property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the items property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItems().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BusinessObjectDefinitionsXto }
     * 
     * 
     */
    public List<BusinessObjectDefinitionsXto> getItems() {
        if (items == null) {
            items = new ArrayList<BusinessObjectDefinitionsXto>();
        }
        return this.items;
    }

    /**
     * Gets the value of the isList property.
     * 
     */
    public boolean isIsList() {
        return isList;
    }

    /**
     * Sets the value of the isList property.
     * 
     */
    public void setIsList(boolean value) {
        this.isList = value;
    }

}
