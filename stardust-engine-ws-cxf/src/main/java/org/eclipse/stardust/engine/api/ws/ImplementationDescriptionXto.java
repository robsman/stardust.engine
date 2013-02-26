
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			The ImplementationDescription provides information concerning a specific implementation of a Process Interface.
 * 			
 * 
 * <p>Java class for ImplementationDescription complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ImplementationDescription">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="implementationModelOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="implementationProcessId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="interfaceModelOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="processInterfaceId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="active" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="primaryImplementation" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImplementationDescription", propOrder = {
    "implementationModelOid",
    "implementationProcessId",
    "interfaceModelOid",
    "processInterfaceId",
    "active",
    "primaryImplementation"
})
public class ImplementationDescriptionXto {

    protected long implementationModelOid;
    @XmlElement(required = true)
    protected String implementationProcessId;
    protected long interfaceModelOid;
    @XmlElement(required = true)
    protected String processInterfaceId;
    protected boolean active;
    protected boolean primaryImplementation;

    /**
     * Gets the value of the implementationModelOid property.
     * 
     */
    public long getImplementationModelOid() {
        return implementationModelOid;
    }

    /**
     * Sets the value of the implementationModelOid property.
     * 
     */
    public void setImplementationModelOid(long value) {
        this.implementationModelOid = value;
    }

    /**
     * Gets the value of the implementationProcessId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImplementationProcessId() {
        return implementationProcessId;
    }

    /**
     * Sets the value of the implementationProcessId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImplementationProcessId(String value) {
        this.implementationProcessId = value;
    }

    /**
     * Gets the value of the interfaceModelOid property.
     * 
     */
    public long getInterfaceModelOid() {
        return interfaceModelOid;
    }

    /**
     * Sets the value of the interfaceModelOid property.
     * 
     */
    public void setInterfaceModelOid(long value) {
        this.interfaceModelOid = value;
    }

    /**
     * Gets the value of the processInterfaceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessInterfaceId() {
        return processInterfaceId;
    }

    /**
     * Sets the value of the processInterfaceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessInterfaceId(String value) {
        this.processInterfaceId = value;
    }

    /**
     * Gets the value of the active property.
     * 
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     */
    public void setActive(boolean value) {
        this.active = value;
    }

    /**
     * Gets the value of the primaryImplementation property.
     * 
     */
    public boolean isPrimaryImplementation() {
        return primaryImplementation;
    }

    /**
     * Sets the value of the primaryImplementation property.
     * 
     */
    public void setPrimaryImplementation(boolean value) {
        this.primaryImplementation = value;
    }

}
