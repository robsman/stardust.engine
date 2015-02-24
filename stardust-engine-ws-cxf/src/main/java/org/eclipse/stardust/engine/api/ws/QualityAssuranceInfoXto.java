
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Class containing information considering the qa workflow
 * 			
 * 
 * <p>Java class for QualityAssuranceInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QualityAssuranceInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="failedQualityAssuranceInstance" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstance"/>
 *         &lt;element name="monitoredInstance" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstance"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QualityAssuranceInfo", propOrder = {
    "failedQualityAssuranceInstance",
    "monitoredInstance"
})
public class QualityAssuranceInfoXto {

    @XmlElement(required = true)
    protected ActivityInstanceXto failedQualityAssuranceInstance;
    @XmlElement(required = true)
    protected ActivityInstanceXto monitoredInstance;

    /**
     * Gets the value of the failedQualityAssuranceInstance property.
     * 
     * @return
     *     possible object is
     *     {@link ActivityInstanceXto }
     *     
     */
    public ActivityInstanceXto getFailedQualityAssuranceInstance() {
        return failedQualityAssuranceInstance;
    }

    /**
     * Sets the value of the failedQualityAssuranceInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityInstanceXto }
     *     
     */
    public void setFailedQualityAssuranceInstance(ActivityInstanceXto value) {
        this.failedQualityAssuranceInstance = value;
    }

    /**
     * Gets the value of the monitoredInstance property.
     * 
     * @return
     *     possible object is
     *     {@link ActivityInstanceXto }
     *     
     */
    public ActivityInstanceXto getMonitoredInstance() {
        return monitoredInstance;
    }

    /**
     * Sets the value of the monitoredInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityInstanceXto }
     *     
     */
    public void setMonitoredInstance(ActivityInstanceXto value) {
        this.monitoredInstance = value;
    }

}
