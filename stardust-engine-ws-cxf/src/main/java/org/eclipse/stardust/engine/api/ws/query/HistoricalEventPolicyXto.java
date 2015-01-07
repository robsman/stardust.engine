
package org.eclipse.stardust.engine.api.ws.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Evaluation policy determining the inclusion of the given event types in process and activity instances.
 *          Not specifying a 'eventTypes' element defaults to all eventTypes.
 *          
 * 
 * <p>Java class for HistoricalEventPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HistoricalEventPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="eventTypes" type="{http://eclipse.org/stardust/ws/v2012a/api/query}HistoricalEventTypes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoricalEventPolicy", propOrder = {
    "eventTypes"
})
public class HistoricalEventPolicyXto
    extends EvaluationPolicyXto
{

    @XmlList
    protected List<HistoricalEventTypeXto> eventTypes;

    /**
     * Gets the value of the eventTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eventTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEventTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HistoricalEventTypeXto }
     * 
     * 
     */
    public List<HistoricalEventTypeXto> getEventTypes() {
        if (eventTypes == null) {
            eventTypes = new ArrayList<HistoricalEventTypeXto>();
        }
        return this.eventTypes;
    }

}
