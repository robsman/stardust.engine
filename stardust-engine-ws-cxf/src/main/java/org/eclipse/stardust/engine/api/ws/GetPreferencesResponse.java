
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="preferences" type="{http://eclipse.org/stardust/ws/v2012a/api}Preferences"/>
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
    "preferences"
})
@XmlRootElement(name = "getPreferencesResponse")
public class GetPreferencesResponse {

    @XmlElement(required = true, nillable = true)
    protected PreferencesXto preferences;

    /**
     * Gets the value of the preferences property.
     * 
     * @return
     *     possible object is
     *     {@link PreferencesXto }
     *     
     */
    public PreferencesXto getPreferences() {
        return preferences;
    }

    /**
     * Sets the value of the preferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreferencesXto }
     *     
     */
    public void setPreferences(PreferencesXto value) {
        this.preferences = value;
    }

}
