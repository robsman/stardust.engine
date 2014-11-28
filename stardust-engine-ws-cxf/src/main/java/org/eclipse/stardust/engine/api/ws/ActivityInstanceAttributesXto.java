
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				This class contains attributes for an activity instance. 
 * 			
 * 
 * <p>Java class for ActivityInstanceAttributes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivityInstanceAttributes">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="activityInstanceOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="qualityAssuranceResult" type="{http://eclipse.org/stardust/ws/v2012a/api}QualityAssuranceResult"/>
 *         &lt;element name="notes" type="{http://eclipse.org/stardust/ws/v2012a/api}Note" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityInstanceAttributes", propOrder = {
    "activityInstanceOid",
    "qualityAssuranceResult",
    "notes"
})
public class ActivityInstanceAttributesXto {

    protected long activityInstanceOid;
    @XmlElement(required = true)
    protected QualityAssuranceResultXto qualityAssuranceResult;
    protected List<NoteXto> notes;

    /**
     * Gets the value of the activityInstanceOid property.
     * 
     */
    public long getActivityInstanceOid() {
        return activityInstanceOid;
    }

    /**
     * Sets the value of the activityInstanceOid property.
     * 
     */
    public void setActivityInstanceOid(long value) {
        this.activityInstanceOid = value;
    }

    /**
     * Gets the value of the qualityAssuranceResult property.
     * 
     * @return
     *     possible object is
     *     {@link QualityAssuranceResultXto }
     *     
     */
    public QualityAssuranceResultXto getQualityAssuranceResult() {
        return qualityAssuranceResult;
    }

    /**
     * Sets the value of the qualityAssuranceResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualityAssuranceResultXto }
     *     
     */
    public void setQualityAssuranceResult(QualityAssuranceResultXto value) {
        this.qualityAssuranceResult = value;
    }

    /**
     * Gets the value of the notes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the notes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNotes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NoteXto }
     * 
     * 
     */
    public List<NoteXto> getNotes() {
        if (notes == null) {
            notes = new ArrayList<NoteXto>();
        }
        return this.notes;
    }

}
