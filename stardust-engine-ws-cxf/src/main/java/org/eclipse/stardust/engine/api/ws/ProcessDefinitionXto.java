
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		    The client view of a workflow process.
 *  			A process definition normally comprises a number of discrete activity steps,
 * 			with associated computer and/or human operations and rules governing the progression
 * 			of the process through the various activity steps.
 * 		    
 * 
 * <p>Java class for ProcessDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="rtOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="detailsLevel" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessDefinitionDetailsLevel"/>
 *         &lt;element name="dataPaths" type="{http://eclipse.org/stardust/ws/v2012a/api}DataPaths"/>
 *         &lt;element name="triggers" type="{http://eclipse.org/stardust/ws/v2012a/api}Triggers" minOccurs="0"/>
 *         &lt;element name="activities">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="activity" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="eventHandlers" type="{http://eclipse.org/stardust/ws/v2012a/api}EventHandlerDefinitions" minOccurs="0"/>
 *         &lt;element name="transitions">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="transition" type="{http://eclipse.org/stardust/ws/v2012a/api}Transition" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="implementedProcessInterface" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInterface" minOccurs="0"/>
 *         &lt;element name="declaredProcessInterface" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInterface" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDefinition", propOrder = {
    "rtOid",
    "detailsLevel",
    "dataPaths",
    "triggers",
    "activities",
    "eventHandlers",
    "transitions",
    "implementedProcessInterface",
    "declaredProcessInterface"
})
public class ProcessDefinitionXto
    extends ModelElementXto
{

    protected long rtOid;
    @XmlElement(required = true)
    protected ProcessDefinitionDetailsLevelXto detailsLevel;
    @XmlElement(required = true, nillable = true)
    protected DataPathsXto dataPaths;
    protected TriggersXto triggers;
    @XmlElement(required = true)
    protected ProcessDefinitionXto.ActivitiesXto activities;
    protected EventHandlerDefinitionsXto eventHandlers;
    @XmlElement(required = true)
    protected ProcessDefinitionXto.TransitionsXto transitions;
    protected ProcessInterfaceXto implementedProcessInterface;
    protected ProcessInterfaceXto declaredProcessInterface;

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
     * Gets the value of the detailsLevel property.
     *
     * @return
     *     possible object is
     *     {@link ProcessDefinitionDetailsLevelXto }
     *
     */
    public ProcessDefinitionDetailsLevelXto getDetailsLevel() {
        return detailsLevel;
    }

    /**
     * Sets the value of the detailsLevel property.
     *
     * @param value
     *     allowed object is
     *     {@link ProcessDefinitionDetailsLevelXto }
     *
     */
    public void setDetailsLevel(ProcessDefinitionDetailsLevelXto value) {
        this.detailsLevel = value;
    }

    /**
     * Gets the value of the dataPaths property.
     * 
     * @return
     *     possible object is
     *     {@link DataPathsXto }
     *     
     */
    public DataPathsXto getDataPaths() {
        return dataPaths;
    }

    /**
     * Sets the value of the dataPaths property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataPathsXto }
     *     
     */
    public void setDataPaths(DataPathsXto value) {
        this.dataPaths = value;
    }

    /**
     * Gets the value of the triggers property.
     * 
     * @return
     *     possible object is
     *     {@link TriggersXto }
     *     
     */
    public TriggersXto getTriggers() {
        return triggers;
    }

    /**
     * Sets the value of the triggers property.
     * 
     * @param value
     *     allowed object is
     *     {@link TriggersXto }
     *     
     */
    public void setTriggers(TriggersXto value) {
        this.triggers = value;
    }

    /**
     * Gets the value of the activities property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessDefinitionXto.ActivitiesXto }
     *     
     */
    public ProcessDefinitionXto.ActivitiesXto getActivities() {
        return activities;
    }

    /**
     * Sets the value of the activities property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessDefinitionXto.ActivitiesXto }
     *     
     */
    public void setActivities(ProcessDefinitionXto.ActivitiesXto value) {
        this.activities = value;
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
     * Gets the value of the transitions property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessDefinitionXto.TransitionsXto }
     *     
     */
    public ProcessDefinitionXto.TransitionsXto getTransitions() {
        return transitions;
    }

    /**
     * Sets the value of the transitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessDefinitionXto.TransitionsXto }
     *     
     */
    public void setTransitions(ProcessDefinitionXto.TransitionsXto value) {
        this.transitions = value;
    }

    /**
     * Gets the value of the implementedProcessInterface property.
     *
     * @return
     *     possible object is
     *     {@link ProcessInterfaceXto }
     *
     */
    public ProcessInterfaceXto getImplementedProcessInterface() {
        return implementedProcessInterface;
    }

    /**
     * Sets the value of the implementedProcessInterface property.
     *
     * @param value
     *     allowed object is
     *     {@link ProcessInterfaceXto }
     *
     */
    public void setImplementedProcessInterface(ProcessInterfaceXto value) {
        this.implementedProcessInterface = value;
    }

    /**
     * Gets the value of the declaredProcessInterface property.
     *
     * @return
     *     possible object is
     *     {@link ProcessInterfaceXto }
     *
     */
    public ProcessInterfaceXto getDeclaredProcessInterface() {
        return declaredProcessInterface;
    }

    /**
     * Sets the value of the declaredProcessInterface property.
     *
     * @param value
     *     allowed object is
     *     {@link ProcessInterfaceXto }
     *
     */
    public void setDeclaredProcessInterface(ProcessInterfaceXto value) {
        this.declaredProcessInterface = value;
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
     *         &lt;element name="activity" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityDefinition" maxOccurs="unbounded" minOccurs="0"/>
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
        "activity"
    })
    public static class ActivitiesXto {

        protected List<ActivityDefinitionXto> activity;

        /**
         * Gets the value of the activity property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the activity property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getActivity().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ActivityDefinitionXto }
         * 
         * 
         */
        public List<ActivityDefinitionXto> getActivity() {
            if (activity == null) {
                activity = new ArrayList<ActivityDefinitionXto>();
            }
            return this.activity;
        }

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
     *         &lt;element name="transition" type="{http://eclipse.org/stardust/ws/v2012a/api}Transition" maxOccurs="unbounded" minOccurs="0"/>
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
        "transition"
    })
    public static class TransitionsXto {

        protected List<TransitionXto> transition;

        /**
         * Gets the value of the transition property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the transition property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTransition().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link TransitionXto }
         * 
         * 
         */
        public List<TransitionXto> getTransition() {
            if (transition == null) {
                transition = new ArrayList<TransitionXto>();
            }
            return this.transition;
        }

    }

}
