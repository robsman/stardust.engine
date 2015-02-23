
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			An access control entry consists of a principal that is associated with privileges.
 * 			
 * 
 * <p>Java-Klasse für AccessControlEntry complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AccessControlEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="principal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="privileges" type="{http://eclipse.org/stardust/ws/v2012a/api}Privileges" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccessControlEntry", propOrder = {
    "principal",
    "privileges"
})
public class AccessControlEntryXto {

    protected String principal;
    protected PrivilegesXto privileges;

    /**
     * Ruft den Wert der principal-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrincipal() {
        return principal;
    }

    /**
     * Legt den Wert der principal-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrincipal(String value) {
        this.principal = value;
    }

    /**
     * Ruft den Wert der privileges-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PrivilegesXto }
     *     
     */
    public PrivilegesXto getPrivileges() {
        return privileges;
    }

    /**
     * Legt den Wert der privileges-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PrivilegesXto }
     *     
     */
    public void setPrivileges(PrivilegesXto value) {
        this.privileges = value;
    }

}
