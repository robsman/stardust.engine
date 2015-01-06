
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				RuntimePermissions present permissions that are changeable at runtime. While other permissions are bound to
 * 				model elements in the process model RuntimePermissions can be set via the public API.
 * 			
 * 
 * <p>Java class for RuntimePermissions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuntimePermissions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RuntimePermissionsMap" type="{http://eclipse.org/stardust/ws/v2012a/api}RuntimePermissionsMap"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuntimePermissions", propOrder = {
    "runtimePermissionsMap"
})
public class RuntimePermissionsXto {

    @XmlElement(name = "RuntimePermissionsMap", required = true)
    protected RuntimePermissionsMapXto runtimePermissionsMap;

    /**
     * Gets the value of the runtimePermissionsMap property.
     * 
     * @return
     *     possible object is
     *     {@link RuntimePermissionsMapXto }
     *     
     */
    public RuntimePermissionsMapXto getRuntimePermissionsMap() {
        return runtimePermissionsMap;
    }

    /**
     * Sets the value of the runtimePermissionsMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuntimePermissionsMapXto }
     *     
     */
    public void setRuntimePermissionsMap(RuntimePermissionsMapXto value) {
        this.runtimePermissionsMap = value;
    }

}
