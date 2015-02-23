
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
 * <p>Java-Klasse für DataPath complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
    @XmlJavaTypeAdapter(Adapter2 .class)
    protected Direction direction;
    protected boolean descriptor;
    protected String accessPath;
    protected String dataId;
    @XmlElement(required = true)
    protected String mappedJavaType;
    protected boolean keyDescriptor;

    /**
     * Ruft den Wert der type-Eigenschaft ab.
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
     * Legt den Wert der type-Eigenschaft fest.
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
     * Ruft den Wert der direction-Eigenschaft ab.
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
     * Legt den Wert der direction-Eigenschaft fest.
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
     * Ruft den Wert der descriptor-Eigenschaft ab.
     * 
     */
    public boolean isDescriptor() {
        return descriptor;
    }

    /**
     * Legt den Wert der descriptor-Eigenschaft fest.
     * 
     */
    public void setDescriptor(boolean value) {
        this.descriptor = value;
    }

    /**
     * Ruft den Wert der accessPath-Eigenschaft ab.
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
     * Legt den Wert der accessPath-Eigenschaft fest.
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
     * Ruft den Wert der mappedJavaType-Eigenschaft ab.
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
     * Legt den Wert der mappedJavaType-Eigenschaft fest.
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
     * Ruft den Wert der keyDescriptor-Eigenschaft ab.
     * 
     */
    public boolean isKeyDescriptor() {
        return keyDescriptor;
    }

    /**
     * Legt den Wert der keyDescriptor-Eigenschaft fest.
     * 
     */
    public void setKeyDescriptor(boolean value) {
        this.keyDescriptor = value;
    }

}
