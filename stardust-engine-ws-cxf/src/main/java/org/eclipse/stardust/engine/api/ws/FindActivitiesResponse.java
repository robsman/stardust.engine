
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
 *         &lt;element name="activityInstances" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityQueryResult"/>
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
    "activityInstances"
})
@XmlRootElement(name = "findActivitiesResponse")
public class FindActivitiesResponse {

    @XmlElement(required = true, nillable = true)
    protected ActivityQueryResultXto activityInstances;

    /**
     * Gets the value of the activityInstances property.
     * 
     * @return
     *     possible object is
     *     {@link ActivityQueryResultXto }
     *     
     */
    public ActivityQueryResultXto getActivityInstances() {
        return activityInstances;
    }

    /**
     * Sets the value of the activityInstances property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityQueryResultXto }
     *     
     */
    public void setActivityInstances(ActivityQueryResultXto value) {
        this.activityInstances = value;
    }

}
