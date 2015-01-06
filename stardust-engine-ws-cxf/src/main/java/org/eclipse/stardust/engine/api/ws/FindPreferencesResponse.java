
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
 *         &lt;element name="preferencesList" type="{http://eclipse.org/stardust/ws/v2012a/api}PreferencesList"/>
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
    "preferencesList"
})
@XmlRootElement(name = "findPreferencesResponse")
public class FindPreferencesResponse {

    @XmlElement(required = true)
    protected PreferencesListXto preferencesList;

    /**
     * Gets the value of the preferencesList property.
     * 
     * @return
     *     possible object is
     *     {@link PreferencesListXto }
     *     
     */
    public PreferencesListXto getPreferencesList() {
        return preferencesList;
    }

    /**
     * Sets the value of the preferencesList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreferencesListXto }
     *     
     */
    public void setPreferencesList(PreferencesListXto value) {
        this.preferencesList = value;
    }

}
