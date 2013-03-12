
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;


/**
 * 
 * 			Contains details about the historical event.
 * 			Different event types use different elements.
 * 			Delegation uses: fromPerformer, toPerformer.
 * 			StateChange uses: fromState, toState, toPerformer.
 * 			Other event types only use text.
 * 			
 * 
 * <p>Java class for HistoricalEventDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HistoricalEventDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fromPerformer" type="{http://eclipse.org/stardust/ws/v2012a/api}Participant" minOccurs="0"/>
 *         &lt;element name="toPerformer" type="{http://eclipse.org/stardust/ws/v2012a/api}Participant" minOccurs="0"/>
 *         &lt;element name="fromState" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstanceState" minOccurs="0"/>
 *         &lt;element name="toState" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstanceState" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoricalEventDetails", propOrder = {
    "text",
    "fromPerformer",
    "toPerformer",
    "fromState",
    "toState"
})
public class HistoricalEventDetailsXto {

    protected String text;
    protected ParticipantXto fromPerformer;
    protected ParticipantXto toPerformer;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter3 .class)
    protected ActivityInstanceState fromState;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter3 .class)
    protected ActivityInstanceState toState;

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the fromPerformer property.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantXto }
     *     
     */
    public ParticipantXto getFromPerformer() {
        return fromPerformer;
    }

    /**
     * Sets the value of the fromPerformer property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantXto }
     *     
     */
    public void setFromPerformer(ParticipantXto value) {
        this.fromPerformer = value;
    }

    /**
     * Gets the value of the toPerformer property.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantXto }
     *     
     */
    public ParticipantXto getToPerformer() {
        return toPerformer;
    }

    /**
     * Sets the value of the toPerformer property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantXto }
     *     
     */
    public void setToPerformer(ParticipantXto value) {
        this.toPerformer = value;
    }

    /**
     * Gets the value of the fromState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ActivityInstanceState getFromState() {
        return fromState;
    }

    /**
     * Sets the value of the fromState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromState(ActivityInstanceState value) {
        this.fromState = value;
    }

    /**
     * Gets the value of the toState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ActivityInstanceState getToState() {
        return toState;
    }

    /**
     * Sets the value of the toState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToState(ActivityInstanceState value) {
        this.toState = value;
    }

}
