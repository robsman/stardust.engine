
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Contains only important core information used to identify a department.
 * 	        
 * 
 * <p>Java-Klasse für DepartmentInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DepartmentInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="runtimeOrganizationOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DepartmentInfo", propOrder = {
    "oid",
    "id",
    "name",
    "runtimeOrganizationOid"
})
@XmlSeeAlso({
    DepartmentXto.class
})
public class DepartmentInfoXto {

    protected long oid;
    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String name;
    protected long runtimeOrganizationOid;

    /**
     * Ruft den Wert der oid-Eigenschaft ab.
     * 
     */
    public long getOid() {
        return oid;
    }

    /**
     * Legt den Wert der oid-Eigenschaft fest.
     * 
     */
    public void setOid(long value) {
        this.oid = value;
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
     * Ruft den Wert der runtimeOrganizationOid-Eigenschaft ab.
     * 
     */
    public long getRuntimeOrganizationOid() {
        return runtimeOrganizationOid;
    }

    /**
     * Legt den Wert der runtimeOrganizationOid-Eigenschaft fest.
     * 
     */
    public void setRuntimeOrganizationOid(long value) {
        this.runtimeOrganizationOid = value;
    }

}
