
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.eclipse.stardust.common.Direction;


/**
 * 
 * 			The DataPath provides read or write access to the workflow data.
 * 			
 * 
 * <p>Java class for DataPath complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataPath">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="direction" type="{http://eclipse.org/stardust/ws/v2012a/api}Direction"/>
 *         &lt;element name="descriptor" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="accessPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mappedJavaType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="keyDescriptor" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataPath", propOrder = {
    "type",
    "direction",
    "descriptor",
    "accessPath",
    "dataId",
    "mappedJavaType",
    "keyDescriptor"
})
public class DataPathXto
    extends ModelElementXto
{

    @XmlElement(required = true)
    protected QName type;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Direction direction;
    protected boolean descriptor;
    protected String accessPath;
    protected String dataId;
    @XmlElement(required = true)
    protected String mappedJavaType;
    protected boolean keyDescriptor;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setType(QName value) {
        this.type = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirection(Direction value) {
        this.direction = value;
    }

    /**
     * Gets the value of the descriptor property.
     * 
     */
    public boolean isDescriptor() {
        return descriptor;
    }

    /**
     * Sets the value of the descriptor property.
     * 
     */
    public void setDescriptor(boolean value) {
        this.descriptor = value;
    }

    /**
     * Gets the value of the accessPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessPath() {
        return accessPath;
    }

    /**
     * Sets the value of the accessPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessPath(String value) {
        this.accessPath = value;
    }

    /**
     * Gets the value of the dataId property.
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
     * Sets the value of the dataId property.
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
     * Gets the value of the mappedJavaType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMappedJavaType() {
        return mappedJavaType;
    }

    /**
     * Sets the value of the mappedJavaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMappedJavaType(String value) {
        this.mappedJavaType = value;
    }

    /**
     * Gets the value of the keyDescriptor property.
     * 
     */
    public boolean isKeyDescriptor() {
        return keyDescriptor;
    }

    /**
     * Sets the value of the keyDescriptor property.
     * 
     */
    public void setKeyDescriptor(boolean value) {
        this.keyDescriptor = value;
    }

}
