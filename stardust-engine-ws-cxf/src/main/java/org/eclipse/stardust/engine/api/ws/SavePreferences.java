
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
 *         &lt;element name="preferenceList" type="{http://eclipse.org/stardust/ws/v2012a/api}PreferencesList"/>
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
    "preferenceList"
})
@XmlRootElement(name = "savePreferences")
public class SavePreferences {

    @XmlElement(required = true)
    protected PreferencesListXto preferenceList;

    /**
     * Gets the value of the preferenceList property.
     * 
     * @return
     *     possible object is
     *     {@link PreferencesListXto }
     *     
     */
    public PreferencesListXto getPreferenceList() {
        return preferenceList;
    }

    /**
     * Sets the value of the preferenceList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreferencesListXto }
     *     
     */
    public void setPreferenceList(PreferencesListXto value) {
        this.preferenceList = value;
    }

}
