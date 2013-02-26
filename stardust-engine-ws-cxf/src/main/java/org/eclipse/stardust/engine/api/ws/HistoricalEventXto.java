
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * 
 * 		    The HistoricalEvent represents a single event which was recorded during lifetime of a process or activity instance.
 * 		    Mainly those events consist of state change, delegation, note or exception events.
 *             
 * 
 * <p>Java class for HistoricalEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HistoricalEvent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventType" type="{http://eclipse.org/stardust/ws/v2012a/api}HistoricalEventType"/>
 *         &lt;element name="eventTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="user" type="{http://eclipse.org/stardust/ws/v2012a/api}User"/>
 *         &lt;element name="eventDetails" type="{http://eclipse.org/stardust/ws/v2012a/api}HistoricalEventDetails"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoricalEvent", propOrder = {
    "eventType",
    "eventTime",
    "user",
    "eventDetails"
})
public class HistoricalEventXto {

    @XmlElement(required = true)
    protected HistoricalEventTypeXto eventType;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date eventTime;
    @XmlElement(required = true)
    protected UserXto user;
    @XmlElement(required = true)
    protected HistoricalEventDetailsXto eventDetails;

    /**
     * Gets the value of the eventType property.
     * 
     * @return
     *     possible object is
     *     {@link HistoricalEventTypeXto }
     *     
     */
    public HistoricalEventTypeXto getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link HistoricalEventTypeXto }
     *     
     */
    public void setEventType(HistoricalEventTypeXto value) {
        this.eventType = value;
    }

    /**
     * Gets the value of the eventTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getEventTime() {
        return eventTime;
    }

    /**
     * Sets the value of the eventTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventTime(Date value) {
        this.eventTime = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link UserXto }
     *     
     */
    public UserXto getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserXto }
     *     
     */
    public void setUser(UserXto value) {
        this.user = value;
    }

    /**
     * Gets the value of the eventDetails property.
     * 
     * @return
     *     possible object is
     *     {@link HistoricalEventDetailsXto }
     *     
     */
    public HistoricalEventDetailsXto getEventDetails() {
        return eventDetails;
    }

    /**
     * Sets the value of the eventDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link HistoricalEventDetailsXto }
     *     
     */
    public void setEventDetails(HistoricalEventDetailsXto value) {
        this.eventDetails = value;
    }

}
