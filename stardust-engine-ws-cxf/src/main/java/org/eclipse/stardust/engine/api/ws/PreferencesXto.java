
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
 * <p>Java class for Preferences complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the moduleId property.
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
     * Sets the value of the moduleId property.
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
     * Gets the value of the preferencesId property.
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
     * Sets the value of the preferencesId property.
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
     * Gets the value of the partitionId property.
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
     * Sets the value of the partitionId property.
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
     * Gets the value of the realmId property.
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
     * Sets the value of the realmId property.
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
     * Gets the value of the userId property.
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
     * Sets the value of the userId property.
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
     * Gets the value of the preferenceScope property.
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
     * Sets the value of the preferenceScope property.
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
     * Gets the value of the preferencesMap property.
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
     * Sets the value of the preferencesMap property.
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
