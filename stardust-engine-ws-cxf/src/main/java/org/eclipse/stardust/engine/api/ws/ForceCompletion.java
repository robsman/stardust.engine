
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
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="outDataValues" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameters"/>
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
    "activityOid",
    "outDataValues"
})
@XmlRootElement(name = "forceCompletion")
public class ForceCompletion {

    protected long activityOid;
    @XmlElement(required = true, nillable = true)
    protected ParametersXto outDataValues;

    /**
     * Gets the value of the activityOid property.
     * 
     */
    public long getActivityOid() {
        return activityOid;
    }

    /**
     * Sets the value of the activityOid property.
     * 
     */
    public void setActivityOid(long value) {
        this.activityOid = value;
    }

    /**
     * Gets the value of the outDataValues property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersXto }
     *     
     */
    public ParametersXto getOutDataValues() {
        return outDataValues;
    }

    /**
     * Sets the value of the outDataValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersXto }
     *     
     */
    public void setOutDataValues(ParametersXto value) {
        this.outDataValues = value;
    }

}
