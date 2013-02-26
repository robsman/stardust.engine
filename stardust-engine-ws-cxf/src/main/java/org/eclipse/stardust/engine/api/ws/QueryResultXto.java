
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			 Result of a Query execution.
 * 			 Holds retrieved items and a flag if more items would be available beyond that subset.
 * 			
 * 
 * <p>Java class for QueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="totalCount" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="hasMore" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryResult", propOrder = {
    "totalCount",
    "hasMore"
})
@XmlSeeAlso({
    UserQueryResultXto.class,
    ActivityQueryResultXto.class,
    ProcessInstanceQueryResultXto.class,
    UserGroupQueryResultXto.class,
    org.eclipse.stardust.engine.api.ws.WorklistXto.UserWorklistXto.class,
    org.eclipse.stardust.engine.api.ws.WorklistXto.SharedWorklistsXto.SharedWorklistXto.class,
    DocumentQueryResultXto.class,
    VariableDefinitionQueryResultXto.class,
    ProcessDefinitionQueryResultXto.class,
    LogEntryQueryResultXto.class,
    ModelsQueryResultXto.class
})
public class QueryResultXto {

    protected Long totalCount;
    protected boolean hasMore;

    /**
     * Gets the value of the totalCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the value of the totalCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTotalCount(Long value) {
        this.totalCount = value;
    }

    /**
     * Gets the value of the hasMore property.
     * 
     */
    public boolean isHasMore() {
        return hasMore;
    }

    /**
     * Sets the value of the hasMore property.
     * 
     */
    public void setHasMore(boolean value) {
        this.hasMore = value;
    }

}
