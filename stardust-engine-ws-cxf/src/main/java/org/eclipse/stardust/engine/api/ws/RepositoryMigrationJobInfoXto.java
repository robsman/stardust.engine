
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für RepositoryMigrationJobInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RepositoryMigrationJobInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fromVersion" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="toVersion" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryMigrationJobInfo", propOrder = {
    "name",
    "description",
    "fromVersion",
    "toVersion"
})
public class RepositoryMigrationJobInfoXto {

    @XmlElement(required = true)
    protected String name;
    protected String description;
    protected int fromVersion;
    protected int toVersion;

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
     * Ruft den Wert der description-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der fromVersion-Eigenschaft ab.
     * 
     */
    public int getFromVersion() {
        return fromVersion;
    }

    /**
     * Legt den Wert der fromVersion-Eigenschaft fest.
     * 
     */
    public void setFromVersion(int value) {
        this.fromVersion = value;
    }

    /**
     * Ruft den Wert der toVersion-Eigenschaft ab.
     * 
     */
    public int getToVersion() {
        return toVersion;
    }

    /**
     * Legt den Wert der toVersion-Eigenschaft fest.
     * 
     */
    public void setToVersion(int value) {
        this.toVersion = value;
    }

}
