
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Provides key indicators of audit trail health.
 * 			
 * 
 * <p>Java-Klasse für AuditTrailHealthReport complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AuditTrailHealthReport">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="numberOfProcessInstancesLackingCompletion" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfProcessInstancesLackingAbortion" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfActivityInstancesLackingAbortion" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfProcessInstancesHavingCrashedActivities" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfProcessInstancesHavingCrashedThreads" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="numberOfProcessInstancesHavingCrashedEventBindings" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditTrailHealthReport", propOrder = {
    "numberOfProcessInstancesLackingCompletion",
    "numberOfProcessInstancesLackingAbortion",
    "numberOfActivityInstancesLackingAbortion",
    "numberOfProcessInstancesHavingCrashedActivities",
    "numberOfProcessInstancesHavingCrashedThreads",
    "numberOfProcessInstancesHavingCrashedEventBindings"
})
public class AuditTrailHealthReportXto {

    protected long numberOfProcessInstancesLackingCompletion;
    protected long numberOfProcessInstancesLackingAbortion;
    protected long numberOfActivityInstancesLackingAbortion;
    protected long numberOfProcessInstancesHavingCrashedActivities;
    protected long numberOfProcessInstancesHavingCrashedThreads;
    protected long numberOfProcessInstancesHavingCrashedEventBindings;

    /**
     * Ruft den Wert der numberOfProcessInstancesLackingCompletion-Eigenschaft ab.
     * 
     */
    public long getNumberOfProcessInstancesLackingCompletion() {
        return numberOfProcessInstancesLackingCompletion;
    }

    /**
     * Legt den Wert der numberOfProcessInstancesLackingCompletion-Eigenschaft fest.
     * 
     */
    public void setNumberOfProcessInstancesLackingCompletion(long value) {
        this.numberOfProcessInstancesLackingCompletion = value;
    }

    /**
     * Ruft den Wert der numberOfProcessInstancesLackingAbortion-Eigenschaft ab.
     * 
     */
    public long getNumberOfProcessInstancesLackingAbortion() {
        return numberOfProcessInstancesLackingAbortion;
    }

    /**
     * Legt den Wert der numberOfProcessInstancesLackingAbortion-Eigenschaft fest.
     * 
     */
    public void setNumberOfProcessInstancesLackingAbortion(long value) {
        this.numberOfProcessInstancesLackingAbortion = value;
    }

    /**
     * Ruft den Wert der numberOfActivityInstancesLackingAbortion-Eigenschaft ab.
     * 
     */
    public long getNumberOfActivityInstancesLackingAbortion() {
        return numberOfActivityInstancesLackingAbortion;
    }

    /**
     * Legt den Wert der numberOfActivityInstancesLackingAbortion-Eigenschaft fest.
     * 
     */
    public void setNumberOfActivityInstancesLackingAbortion(long value) {
        this.numberOfActivityInstancesLackingAbortion = value;
    }

    /**
     * Ruft den Wert der numberOfProcessInstancesHavingCrashedActivities-Eigenschaft ab.
     * 
     */
    public long getNumberOfProcessInstancesHavingCrashedActivities() {
        return numberOfProcessInstancesHavingCrashedActivities;
    }

    /**
     * Legt den Wert der numberOfProcessInstancesHavingCrashedActivities-Eigenschaft fest.
     * 
     */
    public void setNumberOfProcessInstancesHavingCrashedActivities(long value) {
        this.numberOfProcessInstancesHavingCrashedActivities = value;
    }

    /**
     * Ruft den Wert der numberOfProcessInstancesHavingCrashedThreads-Eigenschaft ab.
     * 
     */
    public long getNumberOfProcessInstancesHavingCrashedThreads() {
        return numberOfProcessInstancesHavingCrashedThreads;
    }

    /**
     * Legt den Wert der numberOfProcessInstancesHavingCrashedThreads-Eigenschaft fest.
     * 
     */
    public void setNumberOfProcessInstancesHavingCrashedThreads(long value) {
        this.numberOfProcessInstancesHavingCrashedThreads = value;
    }

    /**
     * Ruft den Wert der numberOfProcessInstancesHavingCrashedEventBindings-Eigenschaft ab.
     * 
     */
    public long getNumberOfProcessInstancesHavingCrashedEventBindings() {
        return numberOfProcessInstancesHavingCrashedEventBindings;
    }

    /**
     * Legt den Wert der numberOfProcessInstancesHavingCrashedEventBindings-Eigenschaft fest.
     * 
     */
    public void setNumberOfProcessInstancesHavingCrashedEventBindings(long value) {
        this.numberOfProcessInstancesHavingCrashedEventBindings = value;
    }

}
