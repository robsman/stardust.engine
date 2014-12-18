
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
 * <p>Java class for ActivityDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
 *         &lt;element name="joinType" type="{http://eclipse.org/stardust/ws/v2012a/api}GatewayType" minOccurs="0"/>
 *         &lt;element name="splitType" type="{http://eclipse.org/stardust/ws/v2012a/api}GatewayType" minOccurs="0"/>
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
    "eventHandlers",
    "joinType",
    "splitType"
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
    protected GatewayTypeXto joinType;
    protected GatewayTypeXto splitType;

    /**
     * Gets the value of the rtOid property.
     * 
     */
    public long getRtOid() {
        return rtOid;
    }

    /**
     * Sets the value of the rtOid property.
     * 
     */
    public void setRtOid(long value) {
        this.rtOid = value;
    }

    /**
     * Gets the value of the abortable property.
     * 
     */
    public boolean isAbortable() {
        return abortable;
    }

    /**
     * Sets the value of the abortable property.
     * 
     */
    public void setAbortable(boolean value) {
        this.abortable = value;
    }

    /**
     * Gets the value of the interactive property.
     * 
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Sets the value of the interactive property.
     * 
     */
    public void setInteractive(boolean value) {
        this.interactive = value;
    }

    /**
     * Gets the value of the implementationType property.
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
     * Sets the value of the implementationType property.
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
     * Gets the value of the defaultPerformer property.
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
     * Sets the value of the defaultPerformer property.
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
     * Gets the value of the application property.
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
     * Sets the value of the application property.
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
     * Gets the value of the interactionContexts property.
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
     * Sets the value of the interactionContexts property.
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
     * Gets the value of the eventHandlers property.
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
     * Sets the value of the eventHandlers property.
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
     * Gets the value of the joinType property.
     * 
     * @return
     *     possible object is
     *     {@link GatewayTypeXto }
     *     
     */
    public GatewayTypeXto getJoinType() {
        return joinType;
    }

    /**
     * Sets the value of the joinType property.
     * 
     * @param value
     *     allowed object is
     *     {@link GatewayTypeXto }
     *     
     */
    public void setJoinType(GatewayTypeXto value) {
        this.joinType = value;
    }

    /**
     * Gets the value of the splitType property.
     * 
     * @return
     *     possible object is
     *     {@link GatewayTypeXto }
     *     
     */
    public GatewayTypeXto getSplitType() {
        return splitType;
    }

    /**
     * Sets the value of the splitType property.
     * 
     * @param value
     *     allowed object is
     *     {@link GatewayTypeXto }
     *     
     */
    public void setSplitType(GatewayTypeXto value) {
        this.splitType = value;
    }


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
