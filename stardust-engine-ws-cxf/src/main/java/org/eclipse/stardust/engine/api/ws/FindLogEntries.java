
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.LogEntryQueryXto;


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
 *         &lt;element name="logEntryQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}LogEntryQuery"/>
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
    "logEntryQuery"
})
@XmlRootElement(name = "findLogEntries")
public class FindLogEntries {

    @XmlElement(required = true, nillable = true)
    protected LogEntryQueryXto logEntryQuery;

    /**
     * Gets the value of the logEntryQuery property.
     * 
     * @return
     *     possible object is
     *     {@link LogEntryQueryXto }
     *     
     */
    public LogEntryQueryXto getLogEntryQuery() {
        return logEntryQuery;
    }

    /**
     * Sets the value of the logEntryQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link LogEntryQueryXto }
     *     
     */
    public void setLogEntryQuery(LogEntryQueryXto value) {
        this.logEntryQuery = value;
    }

}
