
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
 *         &lt;element name="activityInstance" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstance"/>
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
    "activityInstance"
})
@XmlRootElement(name = "activateNextActivityForProcessResponse")
public class ActivateNextActivityForProcessResponse {

    @XmlElement(required = true, nillable = true)
    protected ActivityInstanceXto activityInstance;

    /**
     * Gets the value of the activityInstance property.
     * 
     * @return
     *     possible object is
     *     {@link ActivityInstanceXto }
     *     
     */
    public ActivityInstanceXto getActivityInstance() {
        return activityInstance;
    }

    /**
     * Sets the value of the activityInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityInstanceXto }
     *     
     */
    public void setActivityInstance(ActivityInstanceXto value) {
        this.activityInstance = value;
    }

}
