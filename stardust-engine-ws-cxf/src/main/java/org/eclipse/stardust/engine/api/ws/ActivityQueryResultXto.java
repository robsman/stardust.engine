
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an ActivityQuery execution.
 * 			
 * 
 * <p>Java-Klasse für ActivityQueryResult complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ActivityQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="activityInstances" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstances" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityQueryResult", propOrder = {
    "activityInstances"
})
public class ActivityQueryResultXto
    extends QueryResultXto
{

    protected ActivityInstancesXto activityInstances;

    /**
     * Ruft den Wert der activityInstances-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ActivityInstancesXto }
     *     
     */
    public ActivityInstancesXto getActivityInstances() {
        return activityInstances;
    }

    /**
     * Legt den Wert der activityInstances-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityInstancesXto }
     *     
     */
    public void setActivityInstances(ActivityInstancesXto value) {
        this.activityInstances = value;
    }

}
