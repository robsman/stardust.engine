
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.PreferenceQueryXto;


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
 *         &lt;element name="preferenceQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}PreferenceQuery"/>
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
    "preferenceQuery"
})
@XmlRootElement(name = "findPreferences")
public class FindPreferences {

    @XmlElement(required = true)
    protected PreferenceQueryXto preferenceQuery;

    /**
     * Gets the value of the preferenceQuery property.
     * 
     * @return
     *     possible object is
     *     {@link PreferenceQueryXto }
     *     
     */
    public PreferenceQueryXto getPreferenceQuery() {
        return preferenceQuery;
    }

    /**
     * Sets the value of the preferenceQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreferenceQueryXto }
     *     
     */
    public void setPreferenceQuery(PreferenceQueryXto value) {
        this.preferenceQuery = value;
    }

}
