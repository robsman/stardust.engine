
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Filter criterion for matching specific activity instances.
 *         
 * 
 * <p>Java-Klasse für ActivityInstanceFilter complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ActivityInstanceFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityInstanceFilter", propOrder = {
    "activityOid"
})
public class ActivityInstanceFilterXto
    extends PredicateBaseXto
{

    protected long activityOid;

    /**
     * Ruft den Wert der activityOid-Eigenschaft ab.
     * 
     */
    public long getActivityOid() {
        return activityOid;
    }

    /**
     * Legt den Wert der activityOid-Eigenschaft fest.
     * 
     */
    public void setActivityOid(long value) {
        this.activityOid = value;
    }

}
