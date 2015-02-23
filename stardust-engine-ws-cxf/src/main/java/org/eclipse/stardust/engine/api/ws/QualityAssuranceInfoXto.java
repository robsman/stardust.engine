
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
 * <p>Java-Klasse für QualityAssuranceInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der failedQualityAssuranceInstance-Eigenschaft ab.
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
     * Legt den Wert der failedQualityAssuranceInstance-Eigenschaft fest.
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
     * Ruft den Wert der monitoredInstance-Eigenschaft ab.
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
     * Legt den Wert der monitoredInstance-Eigenschaft fest.
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
