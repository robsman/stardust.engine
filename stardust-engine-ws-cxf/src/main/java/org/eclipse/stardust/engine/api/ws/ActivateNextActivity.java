
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.WorklistQueryXto;


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
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="worklistQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}WorklistQuery" minOccurs="0"/>
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
    "worklistQuery"
})
@XmlRootElement(name = "activateNextActivity")
public class ActivateNextActivity {

    protected Long activityOid;
    protected WorklistQueryXto worklistQuery;

    /**
     * Gets the value of the activityOid property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getActivityOid() {
        return activityOid;
    }

    /**
     * Sets the value of the activityOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setActivityOid(Long value) {
        this.activityOid = value;
    }

    /**
     * Gets the value of the worklistQuery property.
     * 
     * @return
     *     possible object is
     *     {@link WorklistQueryXto }
     *     
     */
    public WorklistQueryXto getWorklistQuery() {
        return worklistQuery;
    }

    /**
     * Sets the value of the worklistQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorklistQueryXto }
     *     
     */
    public void setWorklistQuery(WorklistQueryXto value) {
        this.worklistQuery = value;
    }

}
