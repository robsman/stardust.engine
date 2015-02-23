
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
 * <p>Java-Klasse für ProcessDefinition complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
    protected ProcessInterfaceXto implementedProcessInterface;
    protected ProcessInterfaceXto declaredProcessInterface;

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
     * Ruft den Wert der detailsLevel-Eigenschaft ab.
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
     * Legt den Wert der detailsLevel-Eigenschaft fest.
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
     * Ruft den Wert der dataPaths-Eigenschaft ab.
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
     * Legt den Wert der dataPaths-Eigenschaft fest.
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
     * Ruft den Wert der triggers-Eigenschaft ab.
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
     * Legt den Wert der triggers-Eigenschaft fest.
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
     * Ruft den Wert der activities-Eigenschaft ab.
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
     * Legt den Wert der activities-Eigenschaft fest.
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
     * Ruft den Wert der implementedProcessInterface-Eigenschaft ab.
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
     * Legt den Wert der implementedProcessInterface-Eigenschaft fest.
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
     * Ruft den Wert der declaredProcessInterface-Eigenschaft ab.
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
     * Legt den Wert der declaredProcessInterface-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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

}
