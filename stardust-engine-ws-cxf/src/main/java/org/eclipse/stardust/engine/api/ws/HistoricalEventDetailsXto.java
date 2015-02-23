
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
 * <p>Java-Klasse für HistoricalEventDetails complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
    @XmlJavaTypeAdapter(Adapter4 .class)
    protected ActivityInstanceState fromState;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter4 .class)
    protected ActivityInstanceState toState;

    /**
     * Ruft den Wert der text-Eigenschaft ab.
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
     * Legt den Wert der text-Eigenschaft fest.
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
     * Ruft den Wert der fromPerformer-Eigenschaft ab.
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
     * Legt den Wert der fromPerformer-Eigenschaft fest.
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
     * Ruft den Wert der toPerformer-Eigenschaft ab.
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
     * Legt den Wert der toPerformer-Eigenschaft fest.
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
     * Ruft den Wert der fromState-Eigenschaft ab.
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
     * Legt den Wert der fromState-Eigenschaft fest.
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
     * Ruft den Wert der toState-Eigenschaft ab.
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
     * Legt den Wert der toState-Eigenschaft fest.
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
