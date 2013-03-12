
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an LogEntryQuery execution.
 * 			
 * 
 * <p>Java class for LogEntryQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LogEntryQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="logEntries" type="{http://eclipse.org/stardust/ws/v2012a/api}LogEntries" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LogEntryQueryResult", propOrder = {
    "logEntries"
})
public class LogEntryQueryResultXto
    extends QueryResultXto
{

    protected LogEntriesXto logEntries;

    /**
     * Gets the value of the logEntries property.
     * 
     * @return
     *     possible object is
     *     {@link LogEntriesXto }
     *     
     */
    public LogEntriesXto getLogEntries() {
        return logEntries;
    }

    /**
     * Sets the value of the logEntries property.
     * 
     * @param value
     *     allowed object is
     *     {@link LogEntriesXto }
     *     
     */
    public void setLogEntries(LogEntriesXto value) {
        this.logEntries = value;
    }

}
