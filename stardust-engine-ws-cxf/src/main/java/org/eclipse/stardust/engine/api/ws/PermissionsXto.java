
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Permissions complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Permissions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="permission" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="permissionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="scopes" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionScope" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "Permissions", propOrder = {
    "permission"
})
public class PermissionsXto {

    protected List<PermissionsXto.PermissionXto> permission;

    /**
     * Gets the value of the permission property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permission property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermission().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PermissionsXto.PermissionXto }
     * 
     * 
     */
    public List<PermissionsXto.PermissionXto> getPermission() {
        if (permission == null) {
            permission = new ArrayList<PermissionsXto.PermissionXto>();
        }
        return this.permission;
    }


    /**
     * 
     * 						Represents a permission that is granted by using the declarative security aspect of the workflow engine.
     * 						
     * 
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="permissionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="scopes" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionScope" maxOccurs="unbounded" minOccurs="0"/>
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
        "scopes"
    })
    public static class PermissionXto {

        @XmlElement(required = true)
        protected String permissionId;
        protected List<PermissionScopeXto> scopes;

        /**
         * Ruft den Wert der permissionId-Eigenschaft ab.
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
         * Legt den Wert der permissionId-Eigenschaft fest.
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
         * Gets the value of the scopes property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the scopes property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getScopes().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PermissionScopeXto }
         * 
         * 
         */
        public List<PermissionScopeXto> getScopes() {
            if (scopes == null) {
                scopes = new ArrayList<PermissionScopeXto>();
            }
            return this.scopes;
        }

    }

}
