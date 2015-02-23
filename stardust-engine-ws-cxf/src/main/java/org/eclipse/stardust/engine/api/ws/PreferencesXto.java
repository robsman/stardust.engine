
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				none yet
 * 			
 * 
 * <p>Java-Klasse für Preferences complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Preferences">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="moduleId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="preferencesId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="partitionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="realmId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="preferenceScope" type="{http://eclipse.org/stardust/ws/v2012a/api}PreferenceScope"/>
 *         &lt;element name="preferencesMap" type="{http://eclipse.org/stardust/ws/v2012a/api}PreferencesMap"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Preferences", propOrder = {
    "moduleId",
    "preferencesId",
    "partitionId",
    "realmId",
    "userId",
    "preferenceScope",
    "preferencesMap"
})
public class PreferencesXto {

    @XmlElement(required = true)
    protected String moduleId;
    @XmlElement(required = true)
    protected String preferencesId;
    protected String partitionId;
    protected String realmId;
    protected String userId;
    @XmlElement(required = true)
    protected PreferenceScopeXto preferenceScope;
    @XmlElement(required = true)
    protected PreferencesMapXto preferencesMap;

    /**
     * Ruft den Wert der moduleId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * Legt den Wert der moduleId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModuleId(String value) {
        this.moduleId = value;
    }

    /**
     * Ruft den Wert der preferencesId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreferencesId() {
        return preferencesId;
    }

    /**
     * Legt den Wert der preferencesId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreferencesId(String value) {
        this.preferencesId = value;
    }

    /**
     * Ruft den Wert der partitionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Legt den Wert der partitionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartitionId(String value) {
        this.partitionId = value;
    }

    /**
     * Ruft den Wert der realmId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRealmId() {
        return realmId;
    }

    /**
     * Legt den Wert der realmId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRealmId(String value) {
        this.realmId = value;
    }

    /**
     * Ruft den Wert der userId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Legt den Wert der userId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * Ruft den Wert der preferenceScope-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PreferenceScopeXto }
     *     
     */
    public PreferenceScopeXto getPreferenceScope() {
        return preferenceScope;
    }

    /**
     * Legt den Wert der preferenceScope-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PreferenceScopeXto }
     *     
     */
    public void setPreferenceScope(PreferenceScopeXto value) {
        this.preferenceScope = value;
    }

    /**
     * Ruft den Wert der preferencesMap-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PreferencesMapXto }
     *     
     */
    public PreferencesMapXto getPreferencesMap() {
        return preferencesMap;
    }

    /**
     * Legt den Wert der preferencesMap-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PreferencesMapXto }
     *     
     */
    public void setPreferencesMap(PreferencesMapXto value) {
        this.preferencesMap = value;
    }

}
