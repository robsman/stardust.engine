
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		    The client view of a workflow activity.
 *  			An activity is a piece of work, which will be processed by a combination of resource
 *  			(specified by participant assignment) and/or computer applications (specified by
 *  			application assignment), forming one logical step in the realization of the process.
 * 		    
 * 
 * <p>Java-Klasse für ActivityDefinition complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ActivityDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="rtOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="abortable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="interactive" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="implementationType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="defaultPerformer" type="{http://eclipse.org/stardust/ws/v2012a/api}Participant"/>
 *         &lt;element name="application" type="{http://eclipse.org/stardust/ws/v2012a/api}Application"/>
 *         &lt;element name="interactionContexts">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="interactionContext" type="{http://eclipse.org/stardust/ws/v2012a/api}InteractionContext" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="eventHandlers" type="{http://eclipse.org/stardust/ws/v2012a/api}EventHandlerDefinitions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityDefinition", propOrder = {
    "rtOid",
    "abortable",
    "interactive",
    "implementationType",
    "defaultPerformer",
    "application",
    "interactionContexts",
    "eventHandlers"
})
public class ActivityDefinitionXto
    extends ModelElementXto
{

    protected long rtOid;
    protected boolean abortable;
    protected boolean interactive;
    @XmlElement(required = true)
    protected String implementationType;
    @XmlElement(required = true, nillable = true)
    protected ParticipantXto defaultPerformer;
    @XmlElement(required = true, nillable = true)
    protected ApplicationXto application;
    @XmlElement(required = true)
    protected ActivityDefinitionXto.InteractionContextsXto interactionContexts;
    protected EventHandlerDefinitionsXto eventHandlers;

    /**
     * Ruft den Wert der rtOid-Eigenschaft ab.
     * 
     */
    public long getRtOid() {
        return rtOid;
    }

    /**
     * Legt den Wert der rtOid-Eigenschaft fest.
     * 
     */
    public void setRtOid(long value) {
        this.rtOid = value;
    }

    /**
     * Ruft den Wert der abortable-Eigenschaft ab.
     * 
     */
    public boolean isAbortable() {
        return abortable;
    }

    /**
     * Legt den Wert der abortable-Eigenschaft fest.
     * 
     */
    public void setAbortable(boolean value) {
        this.abortable = value;
    }

    /**
     * Ruft den Wert der interactive-Eigenschaft ab.
     * 
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Legt den Wert der interactive-Eigenschaft fest.
     * 
     */
    public void setInteractive(boolean value) {
        this.interactive = value;
    }

    /**
     * Ruft den Wert der implementationType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImplementationType() {
        return implementationType;
    }

    /**
     * Legt den Wert der implementationType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImplementationType(String value) {
        this.implementationType = value;
    }

    /**
     * Ruft den Wert der defaultPerformer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantXto }
     *     
     */
    public ParticipantXto getDefaultPerformer() {
        return defaultPerformer;
    }

    /**
     * Legt den Wert der defaultPerformer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantXto }
     *     
     */
    public void setDefaultPerformer(ParticipantXto value) {
        this.defaultPerformer = value;
    }

    /**
     * Ruft den Wert der application-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationXto }
     *     
     */
    public ApplicationXto getApplication() {
        return application;
    }

    /**
     * Legt den Wert der application-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationXto }
     *     
     */
    public void setApplication(ApplicationXto value) {
        this.application = value;
    }

    /**
     * Ruft den Wert der interactionContexts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ActivityDefinitionXto.InteractionContextsXto }
     *     
     */
    public ActivityDefinitionXto.InteractionContextsXto getInteractionContexts() {
        return interactionContexts;
    }

    /**
     * Legt den Wert der interactionContexts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityDefinitionXto.InteractionContextsXto }
     *     
     */
    public void setInteractionContexts(ActivityDefinitionXto.InteractionContextsXto value) {
        this.interactionContexts = value;
    }

    /**
     * Ruft den Wert der eventHandlers-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EventHandlerDefinitionsXto }
     *     
     */
    public EventHandlerDefinitionsXto getEventHandlers() {
        return eventHandlers;
    }

    /**
     * Legt den Wert der eventHandlers-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EventHandlerDefinitionsXto }
     *     
     */
    public void setEventHandlers(EventHandlerDefinitionsXto value) {
        this.eventHandlers = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="interactionContext" type="{http://eclipse.org/stardust/ws/v2012a/api}InteractionContext" maxOccurs="unbounded" minOccurs="0"/>
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
        "interactionContext"
    })
    public static class InteractionContextsXto {

        protected List<InteractionContextXto> interactionContext;

        /**
         * Gets the value of the interactionContext property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the interactionContext property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getInteractionContext().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InteractionContextXto }
         * 
         * 
         */
        public List<InteractionContextXto> getInteractionContext() {
            if (interactionContext == null) {
                interactionContext = new ArrayList<InteractionContextXto>();
            }
            return this.interactionContext;
        }

    }

}
