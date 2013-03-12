
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
 *         &lt;element name="logEntries" type="{http://eclipse.org/stardust/ws/v2012a/api}LogEntryQueryResult"/>
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
    "logEntries"
})
@XmlRootElement(name = "findLogEntriesResponse")
public class FindLogEntriesResponse {

    @XmlElement(required = true, nillable = true)
    protected LogEntryQueryResultXto logEntries;

    /**
     * Gets the value of the logEntries property.
     * 
     * @return
     *     possible object is
     *     {@link LogEntryQueryResultXto }
     *     
     */
    public LogEntryQueryResultXto getLogEntries() {
        return logEntries;
    }

    /**
     * Sets the value of the logEntries property.
     * 
     * @param value
     *     allowed object is
     *     {@link LogEntryQueryResultXto }
     *     
     */
    public void setLogEntries(LogEntryQueryResultXto value) {
        this.logEntries = value;
    }

}
