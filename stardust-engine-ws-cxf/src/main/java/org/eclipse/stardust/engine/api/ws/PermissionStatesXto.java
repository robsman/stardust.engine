
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PermissionStates complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PermissionStates">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="permissionState" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="permissionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="state" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionState" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PermissionStates", propOrder = {
    "permissionState"
})
public class PermissionStatesXto {

    protected List<PermissionStatesXto.PermissionStateXto> permissionState;

    /**
     * Gets the value of the permissionState property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permissionState property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermissionState().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PermissionStatesXto.PermissionStateXto }
     * 
     * 
     */
    public List<PermissionStatesXto.PermissionStateXto> getPermissionState() {
        if (permissionState == null) {
            permissionState = new ArrayList<PermissionStatesXto.PermissionStateXto>();
        }
        return this.permissionState;
    }


    /**
     * 
     * 						A representation of the state of a permission.
     * 						
     * 
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="permissionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="state" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionState" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "permissionId",
        "state"
    })
    public static class PermissionStateXto {

        @XmlElement(required = true)
        protected String permissionId;
        protected org.eclipse.stardust.engine.api.ws.PermissionStateXto state;

        /**
         * Gets the value of the permissionId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPermissionId() {
            return permissionId;
        }

        /**
         * Sets the value of the permissionId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPermissionId(String value) {
            this.permissionId = value;
        }

        /**
         * Gets the value of the state property.
         * 
         * @return
         *     possible object is
         *     {@link org.eclipse.stardust.engine.api.ws.PermissionStateXto }
         *     
         */
        public org.eclipse.stardust.engine.api.ws.PermissionStateXto getState() {
            return state;
        }

        /**
         * Sets the value of the state property.
         * 
         * @param value
         *     allowed object is
         *     {@link org.eclipse.stardust.engine.api.ws.PermissionStateXto }
         *     
         */
        public void setState(org.eclipse.stardust.engine.api.ws.PermissionStateXto value) {
            this.state = value;
        }

    }

}
