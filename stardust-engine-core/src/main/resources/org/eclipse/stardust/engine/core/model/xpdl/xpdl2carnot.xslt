<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" exclude-result-prefixes="x1 x2 c"
	xmlns="http://www.carnot.ag/workflowmodel/3.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:x1="http://www.wfmc.org/2002/XPDL1.0"
	xmlns:x2="http://www.wfmc.org/2008/XPDL2.1"
	xmlns:c="http://www.carnot.ag/xpdl/3.1">
	
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" cdata-section-elements="description annotationSymbol expression value"/>
       
	<xsl:template match="/">
		<xsl:apply-templates select="x1:Package|x2:Package"/>
	</xsl:template>
	
	<xsl:template match="x1:Package|x2:Package">
	    <model>
			<!-- attributes -->
            <xsl:call-template name="xpdl-id-and-name"/>
	        
	    	<xsl:variable name="attrs" select="x1:ExtendedAttributes/x1:ExtendedAttribute|x2:ExtendedAttributes/x2:ExtendedAttribute"/>
    		<xsl:variable name="CarnotExt" select="$attrs[@Name='CarnotExt']"/>
	    	<xsl:variable name="Package" select="$CarnotExt/c:Package"/>
			<xsl:for-each select="$Package">
		        <xsl:call-template name="carnot-element-oid"/>
			    <xsl:if test="@ModelOid">
			        <xsl:attribute name="modelOID"><xsl:value-of select="@ModelOid"/></xsl:attribute>
			    </xsl:if>
			    <xsl:choose>
	            	<xsl:when test="@CarnotVersion">
				        <xsl:attribute name="carnotVersion"><xsl:value-of select="@CarnotVersion"/></xsl:attribute>
				    </xsl:when>
				    <xsl:otherwise>
				        <xsl:attribute name="carnotVersion"><xsl:value-of select="carnot-xpdl-utils:getCarnotVersion()" xmlns:carnot-xpdl-utils="xalan://org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils"/></xsl:attribute>
				    </xsl:otherwise>
			    </xsl:choose>
			</xsl:for-each>

	    	<xsl:variable name="PackageHeader" select="x1:PackageHeader|x2:PackageHeader"/>
	        <xsl:for-each select="$PackageHeader">
		    	<xsl:variable name="Vendor" select="x1:Vendor|x2:Vendor"/>
			    <xsl:if test="$Vendor">
			        <xsl:attribute name="vendor"><xsl:value-of select="$Vendor"/></xsl:attribute>
			    </xsl:if>
		    	<xsl:variable name="Created" select="x1:Created|x2:Created"/>
			    <xsl:if test="$Created">
			        <xsl:attribute name="created"><xsl:value-of select="$Created"/></xsl:attribute>
			    </xsl:if>
	        </xsl:for-each>

	    	<xsl:variable name="RedefinableHeader" select="x1:RedefinableHeader|x2:RedefinableHeader"/>
	        <xsl:for-each select="$RedefinableHeader">
		    	<xsl:variable name="Author" select="x1:Author|x2:Author"/>
			    <xsl:if test="$Author">
			        <xsl:attribute name="author"><xsl:value-of select="$Author"/></xsl:attribute>
			    </xsl:if>
	        </xsl:for-each>

			<!-- child elements -->	        
	        <xsl:for-each select="$RedefinableHeader">
		    	<xsl:variable name="Version" select="x1:Version|x2:Version"/>
		        <xsl:if test="$Version">
				    <attribute>
				        <xsl:attribute name="name">carnot:engine:version</xsl:attribute>
				        <xsl:attribute name="value"><xsl:value-of select="$Version"/></xsl:attribute>
				    </attribute>
		        </xsl:if>
		        <xsl:if test="@PublicationStatus">
				    <attribute>
				        <xsl:attribute name="name">carnot:engine:released</xsl:attribute>
				        <xsl:attribute name="type">boolean</xsl:attribute>
				        <xsl:attribute name="value">
				            <xsl:choose>
				                <xsl:when test="@PublicationStatus='RELEASED'">true</xsl:when>
				                <xsl:otherwise>false</xsl:otherwise>
				            </xsl:choose>
				        </xsl:attribute>
				    </attribute>
		        </xsl:if>
	        </xsl:for-each>
	        
			<xsl:for-each select="$Package">
                <xsl:call-template name="carnot-attributes"/>
			</xsl:for-each>

           	<xsl:call-template name="third-party-extended-attributes"/>

	        <xsl:for-each select="$PackageHeader">
	            <xsl:call-template name="xpdl-description"/>
	        </xsl:for-each>

			<xsl:for-each select="$Package">
                <xsl:call-template name="carnot-data-type"/>
                <xsl:call-template name="carnot-application-type"/>
                <xsl:call-template name="carnot-application-context-type"/>
                <xsl:call-template name="carnot-trigger-type"/>
                <xsl:call-template name="carnot-event-condition-type"/>
                <xsl:call-template name="carnot-event-action-type"/>
			</xsl:for-each>

	        <xsl:for-each select="x1:DataFields|x2:DataFields">
	            <xsl:apply-templates select="x1:DataField|x2:DataField"/>
	        </xsl:for-each>
	        
	        <xsl:for-each select="x1:Applications|x2:Applications">
	            <xsl:apply-templates select="x1:Application|x2:Application"/>
	        </xsl:for-each>
	        
            <xsl:apply-templates select="x1:Participants|x2:Participants"/>

			<xsl:for-each select="$Package">
	            <xsl:apply-templates select="c:Modelers/c:Modeler"/>
			</xsl:for-each>

	        <xsl:for-each select="x1:WorkflowProcesses|x2:WorkflowProcesses">
	            <xsl:apply-templates select="x1:WorkflowProcess|x2:WorkflowProcess"/>
	        </xsl:for-each>

			<xsl:copy-of select="x1:ExternalPackages|x2:ExternalPackages"/>
            <xsl:copy-of select="x1:Script|x2:Script"/>
			<xsl:copy-of select="x1:TypeDeclarations|x2:TypeDeclarations"/>
			<xsl:copy-of select="c:qualityControl"/>
	        
			<xsl:for-each select="$Package">
	            <xsl:call-template name="carnot-diagram"/>
                <xsl:call-template name="carnot-link-type"/>
                <xsl:call-template name="carnot-view"/>
			</xsl:for-each>
        </model>
	</xsl:template>
	
	<xsl:template match="x1:Participants|x2:Participants">
	    <xsl:for-each select="x1:Participant|x2:Participant">
	    	<xsl:variable name="attrs" select="x1:ExtendedAttributes/x1:ExtendedAttribute|x2:ExtendedAttributes/x2:ExtendedAttribute"/>
    		<xsl:variable name="CarnotExt" select="$attrs[@Name='CarnotExt']"/>
	    	<xsl:variable name="ParticipantType" select="x1:ParticipantType|x2:ParticipantType"/>
	    	<xsl:variable name="ExternalReferenceType" select="x1:ExternalReference|x2:ExternalReference"/>	    	
	        <xsl:choose>
	            <xsl:when test="$ParticipantType/@Type='ROLE' and not($CarnotExt/c:ConditionalPerformer)">
	            	<xsl:if test="not($ExternalReferenceType[@namespace='conditionalPerformer'])">
				        <xsl:call-template name="carnot-role">
				        	<xsl:with-param name="CarnotExt" select="$CarnotExt"/>
				        </xsl:call-template>
			        </xsl:if>
	            	<xsl:if test="$ExternalReferenceType[@namespace='conditionalPerformer']">
			        	<xsl:call-template name="carnot-conditionalPerformer">
				        	<xsl:with-param name="CarnotExt" select="$CarnotExt"/>
				        </xsl:call-template>
			        </xsl:if>			        
	            </xsl:when>
	            <xsl:when test="$ParticipantType/@Type='ORGANIZATIONAL_UNIT'">
			        <xsl:call-template name="carnot-organization">
			        	<xsl:with-param name="CarnotExt" select="$CarnotExt"/>
			        </xsl:call-template>
	            </xsl:when>
	            <xsl:when test="($ParticipantType/@Type='HUMAN' or $ParticipantType/@Type='ROLE') and $CarnotExt/c:ConditionalPerformer">
					<!-- conditional performers can be detected by testing for an extended attribute c:ConditionalPerformer -->
			        <xsl:call-template name="carnot-conditionalPerformer">
			        	<xsl:with-param name="CarnotExt" select="$CarnotExt"/>
			        </xsl:call-template>
	            </xsl:when>
	            <xsl:otherwise>
		            <!-- TODO import as role? -->
		            <xsl:message>Ignoring unsupported participant '<xsl:value-of select="@Id"/>' of type '<xsl:value-of select="$ParticipantType/@Type"/>'.</xsl:message>
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-role">
		<xsl:param name="CarnotExt"/>
		<role>
			<!-- attributes -->
	    	<xsl:variable name="Role" select="$CarnotExt/c:Role"/>
	        <xsl:for-each select="$Role">
	            <xsl:call-template name="carnot-element-oid"/>
	        </xsl:for-each>

            <xsl:call-template name="xpdl-id-and-name"/>  
	        <xsl:if test="x1:ExternalReference|x2:ExternalReference">
        		<xsl:call-template name="carnot-element-proxy">
        			<xsl:with-param name="ref" select="x1:ExternalReference|x2:ExternalReference"/>
        		</xsl:call-template>
	        </xsl:if>

            <xsl:if test="$Role/@Cardinality">
                <xsl:attribute name="cardinality"><xsl:value-of select="$Role/@Cardinality"/></xsl:attribute>
            </xsl:if>
	        
	        <!-- child elements -->
	        <xsl:for-each select="$Role">
				<xsl:call-template name="carnot-attributes"/>
	        </xsl:for-each>
            <xsl:call-template name="third-party-extended-attributes"/>
			<xsl:call-template name="xpdl-description"/>
	    </role>
	</xsl:template>
	
	<xsl:template name="carnot-organization">
		<xsl:param name="CarnotExt"/>
	    <organization>
	    	<!-- attributes -->
	    	<xsl:variable name="Organization" select="$CarnotExt/c:Organization"/>
	        <xsl:for-each select="$Organization">
	            <xsl:call-template name="carnot-element-oid"/>
	        </xsl:for-each>

            <xsl:call-template name="xpdl-id-and-name"/>  
	        <xsl:if test="x1:ExternalReference|x2:ExternalReference">
        		<xsl:call-template name="carnot-element-proxy">
        			<xsl:with-param name="ref" select="x1:ExternalReference|x2:ExternalReference"/>
        		</xsl:call-template>
	        </xsl:if>

			<xsl:if test="$Organization/@TeamLead">
			  <xsl:attribute name="teamLead"><xsl:value-of select="$Organization/@TeamLead"/></xsl:attribute>
			</xsl:if>

	        <!-- child elements -->
	        <xsl:for-each select="$Organization">
				<xsl:call-template name="carnot-attributes"/>
	        </xsl:for-each>
            <xsl:call-template name="third-party-extended-attributes"/>
			<xsl:call-template name="xpdl-description"/>
	        <xsl:for-each select="$Organization">
	            <xsl:for-each select="c:Members/c:Member">
	                <participant>
	                    <xsl:attribute name="participant"><xsl:value-of select="@Id"/></xsl:attribute>
	                </participant>
	            </xsl:for-each>
	        </xsl:for-each>
	    </organization>
	</xsl:template>
	
	<xsl:template name="carnot-conditionalPerformer">
		<xsl:param name="CarnotExt"/>
	    <conditionalPerformer>
	    	<!-- attributes -->
			<xsl:variable name="ConditionalPerformer" select="$CarnotExt/c:ConditionalPerformer"/>
	        <xsl:for-each select="$ConditionalPerformer">
	            <xsl:call-template name="carnot-element-oid"/>
	        </xsl:for-each>

            <xsl:call-template name="xpdl-id-and-name"/>  
	        <xsl:if test="x1:ExternalReference|x2:ExternalReference">
        		<xsl:call-template name="carnot-element-proxy">
        			<xsl:with-param name="ref" select="x1:ExternalReference|x2:ExternalReference"/>
        		</xsl:call-template>
	        </xsl:if>

            <xsl:if test="$ConditionalPerformer/@DataId and not($ConditionalPerformer/@DataId='')">
                <xsl:attribute name="data"><xsl:value-of select="$ConditionalPerformer/@DataId" /></xsl:attribute>
            </xsl:if>
            <xsl:if test="$ConditionalPerformer/@DataPath">
                <xsl:attribute name="dataPath"><xsl:value-of select="$ConditionalPerformer/@DataPath" /></xsl:attribute>
            </xsl:if>
			<xsl:variable name="ParticipantType" select="x1:ParticipantType|x2:ParticipantType"/>
			<xsl:choose>
		        <xsl:when test="$ParticipantType/@Type">
		        	<xsl:attribute name="is_user"><xsl:value-of select="$ParticipantType/@Type='HUMAN'"/></xsl:attribute>
		        </xsl:when>
		        <xsl:otherwise>
		        	<xsl:attribute name="is_user">false</xsl:attribute>
		        </xsl:otherwise>
			</xsl:choose>	    

	        <!-- child elements -->
	        <xsl:for-each select="$ConditionalPerformer">
				<xsl:call-template name="carnot-attributes"/>
	        </xsl:for-each>
            <xsl:call-template name="third-party-extended-attributes"/>
			<xsl:call-template name="xpdl-description"/>
	    </conditionalPerformer>
	</xsl:template>
	
	<xsl:template match="c:Modeler">
	    <modeler>
	    	<!-- attributes -->
            <xsl:call-template name="xpdl-id-and-name"/>        
	        <xsl:call-template name="carnot-element-oid"/>
			<xsl:if test="@Password">
	            <xsl:attribute name="password"><xsl:value-of select="@Password"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@EMail">
	            <xsl:attribute name="email"><xsl:value-of select="@EMail"/></xsl:attribute>
			</xsl:if>
			
			<!-- child elements -->
			<xsl:call-template name="carnot-attributes"/>
			<xsl:call-template name="carnot-description"/>
	    </modeler>
	</xsl:template>
	
	<xsl:template match="x1:Application|x2:Application">
	    <application>
	    	<!-- attributes -->
	    	<xsl:variable name="attrs" select="x1:ExtendedAttributes/x1:ExtendedAttribute|x2:ExtendedAttributes/x2:ExtendedAttribute"/>
    		<xsl:variable name="CarnotExt" select="$attrs[@Name='CarnotExt']"/>
    		<xsl:variable name="Application" select="$CarnotExt/c:Application"/>
            <xsl:for-each select="$Application">
	            <xsl:call-template name="carnot-element-oid"/>
            </xsl:for-each>
            <xsl:call-template name="xpdl-id-and-name"/>
            <xsl:for-each select="$Application">
                <xsl:if test="@IsInteractive">
                    <xsl:attribute name="interactive"><xsl:value-of select="@IsInteractive"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="@Type">
                    <xsl:attribute name="type"><xsl:value-of select="@Type"/></xsl:attribute>
                </xsl:if>
            </xsl:for-each>
    		
    		<!-- child elements -->
            <xsl:for-each select="$Application">
                <xsl:call-template name="carnot-attributes"/>
            </xsl:for-each>
            <xsl:call-template name="third-party-extended-attributes"/>
            <xsl:call-template name="xpdl-description"/>
            <xsl:for-each select="$Application">
                <xsl:call-template name="carnot-contexts"/>
                <xsl:call-template name="carnot-access-points"/>
            </xsl:for-each>
	    </application>
	</xsl:template>

	<xsl:template match="x1:DataField|x2:DataField">
	    <data>
	    	<!-- attributes -->
	    	<xsl:variable name="attrs" select="x1:ExtendedAttributes/x1:ExtendedAttribute|x2:ExtendedAttributes/x2:ExtendedAttribute"/>
	   		<xsl:variable name="CarnotExt" select="$attrs[@Name='CarnotExt']"/>
	    	<xsl:variable name="DataField" select="$CarnotExt/c:DataField"/>
            <xsl:for-each select="$DataField">
		        <xsl:call-template name="carnot-element-oid"/>
            </xsl:for-each>
	        <xsl:call-template name="xpdl-id-and-name"/>
            <xsl:if test="$DataField/@IsPredefined">
                <xsl:attribute name="predefined"><xsl:value-of select="$DataField/@IsPredefined"/></xsl:attribute>
            </xsl:if>

        	<xsl:variable name="carnot_type">
        		<xsl:choose>
        			<xsl:when test="$DataField/@Type"><xsl:value-of select="$DataField/@Type" /></xsl:when>
        			<xsl:otherwise></xsl:otherwise>
        		</xsl:choose>
        	</xsl:variable>
        	<xsl:variable name="carnot_type_hint">
        		<xsl:choose>
        			<xsl:when test="$DataField/c:Attributes/c:Attribute[@Name='carnot:engine:typeHint']/@Value"><xsl:value-of select="$DataField/c:Attributes/c:Attribute[@Name='carnot:engine:typeHint']/@Value" /></xsl:when>
        			<xsl:when test="$carnot_type = ''">text</xsl:when>
        			<xsl:otherwise></xsl:otherwise>
        		</xsl:choose>
        	</xsl:variable>
	    	<xsl:variable name="DataType" select="x1:DataType|x2:DataType"/>
	    	<xsl:variable name="BasicType" select="$DataType/x1:BasicType|$DataType/x2:BasicType"/>
	    	<xsl:variable name="DeclaredType" select="$DataType/x1:DeclaredType|$DataType/x2:DeclaredType"/>
	    	<xsl:variable name="SchemaType" select="$DataType/x1:SchemaType|$DataType/x2:SchemaType"/>
	    	<xsl:variable name="ExternalReference" select="$DataType/x1:ExternalReference|$DataType/x2:ExternalReference"/>
	        <xsl:variable name="type">
		        <xsl:choose>
		        	<xsl:when test="$BasicType">primitive</xsl:when>
		        	<xsl:when test="$carnot_type='entity' and $ExternalReference">entity</xsl:when>
		        	<xsl:when test="$carnot_type='plainXML' and $SchemaType">plainXML</xsl:when>
		        	<xsl:when test="$carnot_type='plainXML' and $ExternalReference">plainXML</xsl:when>
		        	<xsl:when test="$carnot_type='struct' and $DeclaredType">struct</xsl:when>
		        	<xsl:when test="$carnot_type!='' and $ExternalReference"><xsl:value-of select="$carnot_type"/></xsl:when>
		        	<xsl:when test="$carnot_type='' and $carnot_type_hint!=''"></xsl:when>
		        	<xsl:when test="$ExternalReference">
		        		<xsl:choose>
		        			<xsl:when test="$ExternalReference[@location='ag.carnot.base.Money']">primitive</xsl:when>
		        			<xsl:when test="$ExternalReference[@location='org.eclipse.stardust.common.Money']">primitive</xsl:when>
		        			<xsl:when test="$ExternalReference[@namespace='data']"/>
		        			<xsl:otherwise>serializable</xsl:otherwise>
		        		</xsl:choose>
		        	</xsl:when>
					<xsl:otherwise>primitive</xsl:otherwise>		        
		        </xsl:choose>
	        </xsl:variable>
			<xsl:if test="$type!=''">
            	<xsl:attribute name="type"><xsl:value-of select="$type"/></xsl:attribute>
			</xsl:if>

			<!-- child elements -->
			<xsl:variable name="filter1">
				<xsl:choose>
		            <xsl:when test="$type='' and $carnot_type_hint!=''">carnot:engine:className</xsl:when>
		            <xsl:when test="$type='primitive'">carnot:engine:type</xsl:when>
		            <xsl:when test="$type='serializable'">carnot:engine:className</xsl:when>
		            <xsl:when test="$type='entity'">carnot:engine:remoteInterface</xsl:when>
		            <xsl:when test="$type='plainXML' and $ExternalReference">carnot:engine:schemaURL</xsl:when>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="filter2">
				<xsl:choose>
		            <xsl:when test="$type='plainXML' and $ExternalReference">carnot:engine:typeId</xsl:when>
				</xsl:choose>
			</xsl:variable>
            <xsl:for-each select="$DataField">
                <xsl:call-template name="carnot-attributes">
                	<xsl:with-param name="filter1"><xsl:value-of select="$filter1"/></xsl:with-param>
                	<xsl:with-param name="filter2"><xsl:value-of select="$filter2"/></xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>

            <xsl:if test="$type='primitive'">
            	<attribute name="carnot:engine:type" type="org.eclipse.stardust.engine.core.pojo.data.Type">
		        	<xsl:variable name="primtype" select="$DataField/c:Attributes/c:Attribute[@Name='carnot:engine:type']/@Value"/>
            		<xsl:attribute name="value">
	            		<xsl:choose>
	            			<xsl:when test="$BasicType">
            					<xsl:choose>
            						<xsl:when test="$BasicType/@Type='BOOLEAN'">boolean</xsl:when>
            						<xsl:when test="$BasicType/@Type='INTEGER'">
            							<xsl:choose>
            								<xsl:when test="$primtype='char' or $primtype='byte' or $primtype='short' or $primtype='int' or $primtype='long'">
            									<xsl:value-of select="$primtype"/>
            								</xsl:when>
            								<xsl:otherwise>long</xsl:otherwise>
            							</xsl:choose>
        							</xsl:when>
            						<xsl:when test="$BasicType/@Type='FLOAT'">
            							<xsl:choose>
            								<xsl:when test="$primtype='float' or $primtype='double'">
            									<xsl:value-of select="$primtype"/>
            								</xsl:when>
            								<xsl:otherwise>double</xsl:otherwise>
            							</xsl:choose>
            						</xsl:when>
            						<xsl:when test="$BasicType/@Type='DATETIME'">
            							<xsl:choose>
            								<xsl:when test="$primtype='Calendar' or $primtype='Timestamp'">
            									<xsl:value-of select="$primtype"/>
            								</xsl:when>
            								<xsl:otherwise>Timestamp</xsl:otherwise>
            							</xsl:choose>
            						</xsl:when>
            						<xsl:otherwise>String</xsl:otherwise>
            					</xsl:choose>
	            			</xsl:when>
	            			<xsl:when test="$ExternalReference[@location='ag.carnot.base.Money']">Money</xsl:when>
	            			<xsl:when test="$ExternalReference[@location='org.eclipse.stardust.common.Money']">Money</xsl:when>
	            			<xsl:otherwise>String</xsl:otherwise>
	            		</xsl:choose>
            		</xsl:attribute>
            	</attribute>
            </xsl:if>
            
            <xsl:if test="$type='serializable' and $ExternalReference">
            	<attribute name="carnot:engine:className">
            		<xsl:attribute name="value"><xsl:value-of select="$ExternalReference/@location" /></xsl:attribute>
            	</attribute>
            </xsl:if>
            <xsl:if test="$type='entity' and $ExternalReference/@location and not($ExternalReference/@location='')">
            	<attribute name="carnot:engine:remoteInterface">
            		<xsl:attribute name="value"><xsl:value-of select="$ExternalReference/@location" /></xsl:attribute>
            	</attribute>
            </xsl:if>
            <xsl:if test="$type='struct' and $DeclaredType">
            	<attribute>
            		<xsl:attribute name="name">carnot:engine:dataType</xsl:attribute>
            		<xsl:attribute name="value"><xsl:value-of select="$DeclaredType/@Id"/></xsl:attribute>
            	</attribute>
            </xsl:if>
            <xsl:if test="$type='plainXML' and $ExternalReference">
            	<attribute name="carnot:engine:schemaURL">
            		<xsl:attribute name="value"><xsl:value-of select="$ExternalReference/@location" /></xsl:attribute>
            	</attribute>

				<xsl:variable name="elementName" select="$ExternalReference/@xref" />
				<xsl:variable name="elementNs" select="$ExternalReference/@namespace" />

				<xsl:if test="$elementName">
	            	<attribute name="carnot:engine:typeId">
						<xsl:attribute name="value">
							<xsl:choose>
								<xsl:when test="$elementNs">{<xsl:value-of select="$elementNs"/>}<xsl:value-of select="$elementName"/></xsl:when>
								<xsl:otherwise>{<xsl:value-of select="$elementNs"/>}<xsl:value-of select="$elementName"/></xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
					</attribute>
				</xsl:if>
            </xsl:if>
            <xsl:call-template name="third-party-extended-attributes"/>
            <xsl:call-template name="xpdl-description"/>

            <xsl:if test="$type='struct' and $ExternalReference">
            	<xsl:copy-of select="$ExternalReference"/>
            </xsl:if>
            
            <xsl:if test="$ExternalReference[@namespace='data']">
        		<xsl:call-template name="carnot-element-proxy">
        			<xsl:with-param name="ref" select="$ExternalReference"/>
        		</xsl:call-template>
            </xsl:if>
            
            <xsl:if test="$type='dmsDocument' and $ExternalReference/@xref">
            	<xsl:copy-of select="$ExternalReference"/>
            </xsl:if>
            
            <xsl:if test="$type='dmsDocumentList' and $ExternalReference/@xref">
            	<xsl:copy-of select="$ExternalReference"/>
            </xsl:if>
            
            <xsl:for-each select="x1:Length|x2:Length">
                <xsl:message>Ignoring array length for data <xsl:value-of select="../@Id"/>.</xsl:message>
            </xsl:for-each>
	    </data>
	</xsl:template>
	
	<xsl:template match="x1:WorkflowProcess|x2:WorkflowProcess">
	    <processDefinition>
	    	<!-- attributes -->
	    	<xsl:variable name="attrs" select="x1:ExtendedAttributes/x1:ExtendedAttribute|x2:ExtendedAttributes/x2:ExtendedAttribute"/>
	   		<xsl:variable name="CarnotExt" select="$attrs[@Name='CarnotExt']"/>
	    	<xsl:variable name="WorkflowProcess" select="$CarnotExt/c:WorkflowProcess"/>
            <xsl:for-each select="$WorkflowProcess">
		        <xsl:call-template name="carnot-element-oid"/>
            </xsl:for-each>
	        <xsl:call-template name="xpdl-id-and-name"/>

	    	<xsl:variable name="ProcessHeader" select="x1:ProcessHeader|x2:ProcessHeader"/>
	    	<xsl:variable name="Priority" select="$ProcessHeader/x1:Priority|$ProcessHeader/x2:Priority"/>
	        <xsl:if test="$Priority">
		        <xsl:attribute name="defaultPriority"><xsl:value-of select="$Priority"/></xsl:attribute>
	        </xsl:if>
	        
			<!-- child elements -->
            <xsl:for-each select="$WorkflowProcess">
                <xsl:call-template name="carnot-attributes"/>
            </xsl:for-each>
            <xsl:call-template name="third-party-extended-attributes"/>
            <xsl:for-each select="$ProcessHeader">
                <xsl:call-template name="xpdl-description"/>
            </xsl:for-each>
            
	    	<xsl:variable name="DataFields" select="x1:DataFields|x2:DataFields"/>
            <xsl:for-each select="$DataFields/x1:DataField|$DataFields/x2:DataField">
                <xsl:message>Ignoring process-local data <xsl:value-of select="@Id"/> for process <xsl:value-of select="../../@Id"/></xsl:message>
            </xsl:for-each>
            
	    	<xsl:variable name="Participants" select="x1:Participants|x2:Participants"/>
            <xsl:for-each select="$Participants/x1:Participant|$Participants/x2:Participant">
                <xsl:message>Ignoring process-local participant <xsl:value-of select="@Id"/> for process <xsl:value-of select="../../@Id"/></xsl:message>
            </xsl:for-each>
            
	    	<xsl:variable name="Applications" select="x1:Applications|x2:Applications"/>
            <xsl:for-each select="$Applications/x1:Application|$Applications/x2:Application">
                <xsl:message>Ignoring process-local application <xsl:value-of select="@Id"/> for process <xsl:value-of select="../../@Id"/></xsl:message>
            </xsl:for-each>
            
	    	<xsl:variable name="ActivitySets" select="x1:ActivitySets|x2:ActivitySets"/>
            <xsl:for-each select="$ActivitySets/x1:ActivitySet|$ActivitySets/x2:ActivitySet">
                <xsl:message>Ignoring activity set <xsl:value-of select="@Id"/> for process <xsl:value-of select="../../@Id"/></xsl:message>
            </xsl:for-each>
            
	    	<xsl:variable name="Activities" select="x1:Activities|x2:Activities"/>
            <xsl:apply-templates select="$Activities/x1:Activity|$Activities/x2:Activity"/>
            
	    	<xsl:variable name="Transitions" select="x1:Transitions|x2:Transitions"/>
            <xsl:apply-templates select="$Transitions/x1:Transition|$Transitions/x2:Transition"/>
            
            <xsl:for-each select="$WorkflowProcess">
	            <xsl:call-template name="carnot-triggers"/>
	            <xsl:call-template name="carnot-data-paths"/>
	            <xsl:call-template name="carnot-event-handlers"/>
	            <xsl:call-template name="carnot-diagram"/>
            </xsl:for-each>
            
   			<xsl:copy-of select="x1:FormalParameters|x2:FormalParameters"/>
   			<xsl:copy-of select="$WorkflowProcess/c:FormalParameterMappings"/>
	        <xsl:if test="$WorkflowProcess/c:Implements">
	        	<externalReference>
	        		<xsl:if test="$WorkflowProcess/c:Implements/@PackageRef">
			            <xsl:attribute name="PackageRef"><xsl:value-of select="$WorkflowProcess/c:Implements/@PackageRef"/></xsl:attribute>
	        		</xsl:if>
	        		<xsl:if test="$WorkflowProcess/c:Implements/@ref">
			            <xsl:attribute name="ref"><xsl:value-of select="$WorkflowProcess/c:Implements/@ref"/></xsl:attribute>
	        		</xsl:if>
	        	</externalReference>	        	
	        </xsl:if>	
	        
	    </processDefinition>
	</xsl:template>
	
	<xsl:template match="x1:Activity|x2:Activity">
	    <xsl:element name="activity">
	    	<!-- attributes -->
	    	<xsl:variable name="attrs" select="x1:ExtendedAttributes/x1:ExtendedAttribute|x2:ExtendedAttributes/x2:ExtendedAttribute"/>
    		<xsl:variable name="CarnotExt" select="$attrs[@Name='CarnotExt']"/>
    		<xsl:variable name="Activity" select="$CarnotExt/c:Activity"/>
            <xsl:for-each select="$Activity">
	            <xsl:call-template name="carnot-element-oid"/>
            </xsl:for-each>
            <xsl:call-template name="xpdl-id-and-name"/>
            <xsl:if test="$Activity/@IsAbortableByPerformer">
                <xsl:attribute name="allowsAbortByPerformer"><xsl:value-of select="$Activity/@IsAbortableByPerformer"/></xsl:attribute>
            </xsl:if>
	        
            <!-- TODO @application -->
    		<xsl:variable name="Implementation" select="x1:Implementation|x2:Implementation"/>
    		<xsl:variable name="No" select="$Implementation/x1:No|$Implementation/x2:No"/>
    		<xsl:variable name="Tool" select="$Implementation/x1:Tool|$Implementation/x2:Tool"/>
    		<xsl:variable name="TaskApplication" select="$Implementation/x2:Task/x2:TaskApplication"/>
    		<xsl:variable name="SubFlow" select="$Implementation/x1:SubFlow|$Implementation/x2:SubFlow"/>
	        <xsl:if test="$Tool/@Type='APPLICATION'">
                <xsl:attribute name="application"><xsl:value-of select="$Tool/@Id"/></xsl:attribute>
	        </xsl:if>
            <xsl:if test="$TaskApplication and not($TaskApplication/@PackageRef)">
                <xsl:attribute name="application"><xsl:value-of select="$TaskApplication/@Id"/></xsl:attribute>
	        </xsl:if>

            <xsl:if test="$Activity/@IsHibernatedOnCreation">
                <xsl:attribute name="hibernateOnCreation"><xsl:value-of select="$Activity/@IsHibernatedOnCreation"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="$Activity/@SubProcessMode">
                <xsl:attribute name="subProcessMode"><xsl:value-of select="$Activity/@SubProcessMode"/></xsl:attribute>
            </xsl:if>
	        
	        <!-- TODO @implementation -->
        	<xsl:variable name="carnot_impl" select="$Activity/@Implementation"/>
	        <xsl:variable name="impl">
		        <xsl:choose>
		        	<xsl:when test="$No and $carnot_impl='Manual'">Manual</xsl:when>
		        	<xsl:when test="$No"></xsl:when>
		        	<xsl:when test="$Tool">Application</xsl:when>
		        	<xsl:when test="$TaskApplication">Application</xsl:when>
		        	<xsl:when test="$SubFlow">Subprocess</xsl:when>
					<xsl:otherwise>Route</xsl:otherwise>		        
		        </xsl:choose>
	        </xsl:variable>
	        
    		<xsl:variable name="BlockActivity" select="x1:BlockActivity|x2:BlockActivity"/>
			<xsl:if test="$BlockActivity">
				<xsl:message>Ignoring reference to activity set <xsl:value-of select="$BlockActivity/@BlockId"/> for activity <xsl:value-of select="@Id"/>.</xsl:message>
			</xsl:if>

			<xsl:if test="$impl!=''">
	        	<xsl:attribute name="implementation"><xsl:value-of select="$impl"/></xsl:attribute>
	        </xsl:if>

            <!-- TODO @implementationProcess -->
	        <xsl:if test="$SubFlow">
	        	<xsl:if test="not($SubFlow/@PackageRef)">
                	<xsl:attribute name="implementationProcess"><xsl:value-of select="$SubFlow/@Id"/></xsl:attribute>
                </xsl:if>
                <!-- TODO @Execution -->
	    		<xsl:variable name="ActualParameters" select="$SubFlow/x1:ActualParameters|$SubFlow/x2:ActualParameters"/>
                <xsl:if test="$ActualParameters">
                	<xsl:message>Ignoring parameters for subprocess call from activity <xsl:value-of select="../../@Id"/>.</xsl:message>
                </xsl:if>
	        </xsl:if>

	        <!-- TODO @join -->
    		<xsl:variable name="TransitionRestrictions" select="x1:TransitionRestrictions|x2:TransitionRestrictions"/>
    		<xsl:variable name="TransitionRestriction" select="$TransitionRestrictions/x1:TransitionRestriction|$TransitionRestrictions/x2:TransitionRestriction"/>
    		<xsl:variable name="Join" select="$TransitionRestriction/x1:Join|$TransitionRestriction/x2:Join"/>
	        <xsl:choose>
	            <xsl:when test="$Join">
		            <xsl:attribute name="join"><xsl:value-of select="$Join/@Type"/></xsl:attribute>
	            </xsl:when>
	            <!-- xsl:otherwise>
		            <xsl:attribute name="join">None</xsl:attribute>
	            </xsl:otherwise -->
	        </xsl:choose>

            <xsl:if test="$Activity/@LoopType">
                <xsl:attribute name="loopType"><xsl:value-of select="$Activity/@LoopType"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$Activity/@LoopCondition">
                <xsl:attribute name="loopCondition"><xsl:value-of select="$Activity/@LoopCondition"/></xsl:attribute>
            </xsl:if>
	            
            <!-- TODO @performer -->
    		<xsl:variable name="Performer" select="x1:Performer|x2:Performer"/>
	        <xsl:if test="$Performer">
                <xsl:attribute name="performer"><xsl:value-of select="$Performer"/></xsl:attribute>
	        </xsl:if>
    		<xsl:variable name="QualityControlPerformer" select="x1:QualityControlPerformer|x2:QualityControlPerformer"/>	        
	        <xsl:if test="$QualityControlPerformer">
                <xsl:attribute name="qualityControlPerformer"><xsl:value-of select="$QualityControlPerformer"/></xsl:attribute>
	        </xsl:if>
	            
	        <!-- TODO @split -->
    		<xsl:variable name="Split" select="$TransitionRestriction/x1:Split|$TransitionRestriction/x2:Split"/>
	        <xsl:choose>
	            <xsl:when test="$Split">
		            <xsl:attribute name="split"><xsl:value-of select="$Split/@Type"/></xsl:attribute>
	            </xsl:when>
	            <!-- xsl:otherwise>
		            <xsl:attribute name="split">None</xsl:attribute>
	            </xsl:otherwise -->
	        </xsl:choose>

			<!-- child elements -->
	        <xsl:for-each select="$Activity">
	            <xsl:call-template name="carnot-attributes"/>
            </xsl:for-each>
            <xsl:call-template name="third-party-extended-attributes"/>
	        <xsl:call-template name="xpdl-description"/>

	        <xsl:for-each select="$Activity">
	            <xsl:for-each select="c:QualityCodes/c:QualityCode">
	                <validQualityCodes><xsl:value-of select="." /></validQualityCodes>
				</xsl:for-each>
            </xsl:for-each>
            
	        <xsl:for-each select="$Activity">
	            <!-- TODO dataMapping -->
	            <xsl:for-each select="c:DataFlows/c:DataFlow">
	                <dataMapping>
	                	<!-- attributes -->
				        <xsl:call-template name="carnot-element-oid"/>
            			<xsl:call-template name="xpdl-id-and-name"/>
	                    <xsl:if test="c:AccessPointRef/@Id">
		                    <xsl:attribute name="applicationAccessPoint"><xsl:value-of select="c:AccessPointRef/@Id"/></xsl:attribute>
	                    </xsl:if>
	                    <xsl:if test="c:AccessPointRef/@Expression">
		                    <xsl:attribute name="applicationPath"><xsl:value-of select="c:AccessPointRef/@Expression"/></xsl:attribute>
		                </xsl:if>
				        <xsl:if test="@Context">
				            <xsl:attribute name="context"><xsl:value-of select="@Context"/></xsl:attribute>
				        </xsl:if>	        
	                    <xsl:if test="c:DataRef/@Id">
		                    <xsl:attribute name="data"><xsl:value-of select="c:DataRef/@Id"/></xsl:attribute>
		                </xsl:if>
	                    <xsl:if test="c:DataRef/@Expression">
		                    <xsl:attribute name="dataPath"><xsl:value-of select="c:DataRef/@Expression"/></xsl:attribute>
		                </xsl:if>
				        <xsl:if test="@Direction">
				            <xsl:attribute name="direction"><xsl:value-of select="@Direction"/></xsl:attribute>
				        </xsl:if>	        
	                </dataMapping>
	            </xsl:for-each>
	            <xsl:call-template name="carnot-event-handlers"/>
            </xsl:for-each>
            
            <xsl:if test="$TaskApplication/@PackageRef">
            	<externalReference>
	                <xsl:attribute name="PackageRef"><xsl:value-of select="$TaskApplication/@PackageRef"/></xsl:attribute>
                	<xsl:attribute name="ref"><xsl:value-of select="$TaskApplication/@Id"/></xsl:attribute>
            	</externalReference>
	        </xsl:if>
            
            <xsl:if test="$SubFlow/@PackageRef">
            	<externalReference>
	                <xsl:attribute name="PackageRef"><xsl:value-of select="$SubFlow/@PackageRef"/></xsl:attribute>
                	<xsl:attribute name="ref"><xsl:value-of select="$SubFlow/@Id"/></xsl:attribute>
            	</externalReference>
	        </xsl:if>
            
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="x1:Transition|x2:Transition">
	    <transition>
	    	<!-- attributes -->
	    	<xsl:variable name="attrs" select="x1:ExtendedAttributes/x1:ExtendedAttribute|x2:ExtendedAttributes/x2:ExtendedAttribute"/>
    		<xsl:variable name="CarnotExt" select="$attrs[@Name='CarnotExt']"/>
    		<xsl:variable name="Transition" select="$CarnotExt/c:Transition"/>
            <xsl:for-each select="$Transition">
	            <xsl:call-template name="carnot-element-oid"/>
            </xsl:for-each>
            <xsl:call-template name="xpdl-id-and-name"/>
	        <xsl:for-each select="$Transition">
	            <xsl:if test="@IsForkingOnTraversal">
	                <xsl:attribute name="forkOnTraversal"><xsl:value-of select="@IsForkingOnTraversal"/></xsl:attribute>
	            </xsl:if>
	        </xsl:for-each>
	        <xsl:if test="@From">
	            <xsl:attribute name="from"><xsl:value-of select="@From"/></xsl:attribute>
	        </xsl:if>	        
	        <xsl:if test="@To">
	            <xsl:attribute name="to"><xsl:value-of select="@To"/></xsl:attribute>
	        </xsl:if>	        

	        <!-- TODO @condition, handle OTHERWISE, EXCEPTION, DEFAULTEXCEPTION -->
    		<xsl:variable name="Condition" select="x1:Condition|x2:Condition"/>
    		<xsl:variable name="Xpression" select="$Condition/x1:Xpression|$Condition/x2:Xpression"/>
	        <xsl:choose>
	            <xsl:when test="$Condition/@Type='CONDITION'">
	                <xsl:choose>
	                    <xsl:when test="$Xpression">
					        <xsl:message>Probably loosing Xpression condition for transition '<xsl:value-of select="@Id"/>' of process '<xsl:value-of select="../../@Id"/>': <xsl:copy-of select="$Xpression/*"/></xsl:message>
	                    </xsl:when>
	                    <xsl:otherwise>
			        		<xsl:attribute name="condition">CONDITION</xsl:attribute>
	                    </xsl:otherwise>
	                </xsl:choose>
	            </xsl:when>

	            <xsl:when test="$Condition/@Type='OTHERWISE'">
			        <xsl:attribute name="condition">OTHERWISE</xsl:attribute>
	            </xsl:when>

	            <xsl:when test="$Condition/@Type='EXCEPTION'">
	                <xsl:message>Ignoring EXCEPTION condition for transition '<xsl:value-of select="@Id"/>'.</xsl:message>
	            </xsl:when>

	            <xsl:when test="$Condition/@Type='DEFAULTEXCEPTION'">
	                <xsl:message>Ignoring DEFAULTEXCEPTION condition for transition '<xsl:value-of select="@Id"/>'.</xsl:message>
	            </xsl:when>
	        </xsl:choose>
	        
	        <!-- child elements -->
		  	<xsl:for-each select="$Transition">
        		<xsl:call-template name="carnot-attributes"/>
      		</xsl:for-each>
	        <xsl:call-template name="third-party-extended-attributes"/>
	  		<xsl:call-template name="xpdl-description"/>

	        <xsl:if test="$Condition/@Type='CONDITION'">
                <xsl:choose>
                    <xsl:when test="$Xpression">
				        <expression><xsl:copy-of select="$Xpression/*"/></expression>
                    </xsl:when>
                    <xsl:otherwise>
				        <expression><xsl:value-of select="$Condition"/></expression>
                    </xsl:otherwise>
                </xsl:choose>
	        </xsl:if>
		</transition>        
	</xsl:template>
	
	<xsl:template name="xpdl-description">
		<xsl:variable name="Description" select="x1:Description|x2:Description"/>
	    <xsl:if test="$Description">
	        <description>
	        	<!--xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text-->
	        	<xsl:value-of select="$Description"/>
	        	<!--xsl:text disable-output-escaping="yes">]]&gt;</xsl:text-->
	        </description>
	    </xsl:if>
	</xsl:template>

	<xsl:template name="xpdl-id-and-name">
        <xsl:if test="@Id">
	        <xsl:attribute name="id"><xsl:value-of select="@Id"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@Name">
	        <xsl:attribute name="name"><xsl:value-of select="@Name"/></xsl:attribute>
        </xsl:if>
	</xsl:template>

    <xsl:template name="carnot-element-oid">
		<xsl:if test="@Oid">
			<xsl:attribute name="oid"><xsl:value-of select="@Oid"/></xsl:attribute>
		</xsl:if>
    </xsl:template>

    <xsl:template name="carnot-element-proxy">
    	<xsl:param name="ref"/>
    	<xsl:attribute name="proxy"><xsl:value-of select="$ref/@namespace" />:{<xsl:value-of select="$ref/@location" />}<xsl:value-of select="$ref/@xref" /></xsl:attribute>
    </xsl:template>
	
	<xsl:template name="carnot-description">
	    <xsl:if test="c:Description">
	        <description>
	        	<!--xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text-->
	        	<xsl:value-of select="c:Description"/>
	        	<!--xsl:text disable-output-escaping="yes">]]&gt;</xsl:text-->
	        </description>
	    </xsl:if>
	</xsl:template>
	
	<xsl:template name="carnot-data-type">
	    <xsl:for-each select="c:MetaTypes/c:DataTypes/c:DataType">
		    <dataType>
		    	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
		        <xsl:if test="@IsPredefined">
		            <xsl:attribute name="predefined"><xsl:value-of select="@IsPredefined"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@PanelClass">
		            <xsl:attribute name="panelClass"><xsl:value-of select="@PanelClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@AccessPathEditor">
		            <xsl:attribute name="accessPathEditor"><xsl:value-of select="@AccessPathEditor"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@ValidatorClass">
		            <xsl:attribute name="validatorClass"><xsl:value-of select="@ValidatorClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@Evaluator">
		            <xsl:attribute name="evaluator"><xsl:value-of select="@Evaluator"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@InstanceClass">
		            <xsl:attribute name="instanceClass"><xsl:value-of select="@InstanceClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@ValueCreator">
		            <xsl:attribute name="valueCreator"><xsl:value-of select="@ValueCreator"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@StorageStrategy">
		            <xsl:attribute name="storageStrategy"><xsl:value-of select="@StorageStrategy"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@IsWriteable">
		            <xsl:attribute name="writable"><xsl:value-of select="@IsWriteable"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@IsReadable">
		            <xsl:attribute name="readable"><xsl:value-of select="@IsReadable"/></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->
		        <xsl:call-template name="carnot-attributes"/>
		        <xsl:call-template name="carnot-description"/>
		    </dataType>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-application-type">
	    <xsl:for-each select="c:MetaTypes/c:ApplicationTypes/c:ApplicationType">
		    <applicationType>
		    	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
		        <xsl:if test="@IsPredefined">
		            <xsl:attribute name="predefined"><xsl:value-of select="@IsPredefined"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@IsSynchronous">
		            <xsl:attribute name="synchronous"><xsl:value-of select="@IsSynchronous"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@PanelClass">
		            <xsl:attribute name="panelClass"><xsl:value-of select="@PanelClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@AccessPointProviderClass">
		            <xsl:attribute name="accessPointProviderClass"><xsl:value-of select="@AccessPointProviderClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@ValidatorClass">
		            <xsl:attribute name="validatorClass"><xsl:value-of select="@ValidatorClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@InstanceClass">
		            <xsl:attribute name="instanceClass"><xsl:value-of select="@InstanceClass"/></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->
		        <xsl:call-template name="carnot-attributes"/>
		        <xsl:call-template name="carnot-description"/>
		    </applicationType>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-application-context-type">
	    <xsl:for-each select="c:MetaTypes/c:ApplicationContextTypes/c:ApplicationContextType">
		    <applicationContextType>
		    	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
		        <xsl:if test="@IsPredefined">
		            <xsl:attribute name="predefined"><xsl:value-of select="@IsPredefined"/></xsl:attribute>
		        </xsl:if>
                <xsl:if test="@HasApplicationPath">
                    <xsl:attribute name="hasApplicationPath"><xsl:value-of select="@HasApplicationPath"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="@HasMappingId">
                    <xsl:attribute name="hasMappingId"><xsl:value-of select="@HasMappingId"/></xsl:attribute>
                </xsl:if>
		        <xsl:if test="@PanelClass">
		            <xsl:attribute name="panelClass"><xsl:value-of select="@PanelClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@AccessPointProviderClass">
		            <xsl:attribute name="accessPointProviderClass"><xsl:value-of select="@AccessPointProviderClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@ValidatorClass">
		            <xsl:attribute name="validatorClass"><xsl:value-of select="@ValidatorClass"/></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->
		        <xsl:call-template name="carnot-attributes"/>
		        <xsl:call-template name="carnot-description"/>
		    </applicationContextType>
	    </xsl:for-each>
	</xsl:template>
	
	
	
	<xsl:template name="carnot-event-condition-type">
	    <xsl:for-each select="c:MetaTypes/c:EventConditionTypes/c:EventConditionType">
		    <eventConditionType>
		    	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
                <xsl:if test="@IsPredefined">
                    <xsl:attribute name="predefined"><xsl:value-of select="@IsPredefined"/></xsl:attribute>
                </xsl:if>
		        <xsl:if test="@IsActivityCondition">
		            <xsl:attribute name="activityCondition"><xsl:value-of select="@IsActivityCondition"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@Implementation">
		            <xsl:attribute name="implementation">
		                <xsl:choose>
		                    <xsl:when test="@Implementation='ENGINE'">engine</xsl:when>
		                    <xsl:when test="@Implementation='PUSH'">push</xsl:when>
		                    <xsl:when test="@Implementation='PULL'">pull</xsl:when>
		                </xsl:choose>
	            	</xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@IsProcessCondition">
		            <xsl:attribute name="processCondition"><xsl:value-of select="@IsProcessCondition"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@PanelClass">
		            <xsl:attribute name="panelClass"><xsl:value-of select="@PanelClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@BinderClass">
		            <xsl:attribute name="binderClass"><xsl:value-of select="@BinderClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@PullEventEmitterClass">
		            <xsl:attribute name="pullEventEmitterClass"><xsl:value-of select="@PullEventEmitterClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@Rule">
		            <xsl:attribute name="rule"><xsl:value-of select="@Rule"/></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->
		        <xsl:call-template name="carnot-attributes"/>
		        <xsl:call-template name="carnot-description"/>
		    </eventConditionType>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-event-action-type">
	    <xsl:for-each select="c:MetaTypes/c:EventActionTypes/c:EventActionType">
		    <eventActionType>
		    	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
		        <xsl:if test="@IsPredefined">
		            <xsl:attribute name="predefined"><xsl:value-of select="@IsPredefined"/></xsl:attribute>
		        </xsl:if>
                <xsl:if test="@IsActivityAction">
                    <xsl:attribute name="activityAction"><xsl:value-of select="@IsActivityAction"/></xsl:attribute>
                </xsl:if> 
		        <xsl:if test="@IsProcessAction">
		            <xsl:attribute name="processAction"><xsl:value-of select="@IsProcessAction"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@PanelClass">
		            <xsl:attribute name="panelClass"><xsl:value-of select="@PanelClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@ActionClass">
		            <xsl:attribute name="actionClass"><xsl:value-of select="@ActionClass"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@SupportedConditionTypes">
		            <xsl:attribute name="supportedConditionTypes"><xsl:value-of select="@SupportedConditionTypes"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@UnsupportedContexts">
		            <xsl:attribute name="unsupportedContexts"><xsl:value-of select="@UnsupportedContexts"/></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->
		        <xsl:call-template name="carnot-attributes"/>
		        <xsl:call-template name="carnot-description"/>
		    </eventActionType>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-link-type">
	    <xsl:for-each select="c:MetaTypes/c:LinkTypes/c:LinkType">
		    <linkType>
		    	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid" />
	            <xsl:call-template name="xpdl-id-and-name"/>
		        <xsl:if test="@LineColor">
		            <xsl:attribute name="lineColor"><xsl:value-of select="@LineColor"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@LineType">
		            <xsl:attribute name="lineType"><xsl:value-of select="@LineType"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@HasLinkTypeLabel">
		            <xsl:attribute name="show_linktype_name"><xsl:value-of select="@HasLinkTypeLabel"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@HasRoleLabels">
		            <xsl:attribute name="show_role_names"><xsl:value-of select="@HasRoleLabels"/></xsl:attribute>
		        </xsl:if>
				<xsl:for-each select="c:LinkSource">
			        <xsl:if test="@ClassName">
				        <xsl:attribute name="source_classname"><xsl:value-of select="@ClassName"/></xsl:attribute>
			        </xsl:if>
			        <xsl:if test="@Cardinality">
				        <xsl:attribute name="source_cardinality"><xsl:value-of select="@Cardinality"/></xsl:attribute>
			        </xsl:if>
			        <xsl:if test="@Role">
				        <xsl:attribute name="source_rolename"><xsl:value-of select="@Role"/></xsl:attribute>
			        </xsl:if>
			        <xsl:if test="@Symbol">
				        <xsl:attribute name="source_symbol"><xsl:value-of select="@Symbol"/></xsl:attribute>
			        </xsl:if>
				</xsl:for-each>		        
				<xsl:for-each select="c:LinkTarget">
			        <xsl:if test="@ClassName">
				        <xsl:attribute name="target_classname"><xsl:value-of select="@ClassName"/></xsl:attribute>
			        </xsl:if>
			        <xsl:if test="@Cardinality">
				        <xsl:attribute name="target_cardinality"><xsl:value-of select="@Cardinality"/></xsl:attribute>
			        </xsl:if>
			        <xsl:if test="@Role">
				        <xsl:attribute name="target_rolename"><xsl:value-of select="@Role"/></xsl:attribute>
			        </xsl:if>
			        <xsl:if test="@Symbol">
				        <xsl:attribute name="target_symbol"><xsl:value-of select="@Symbol"/></xsl:attribute>
			        </xsl:if>
				</xsl:for-each>
                
                <!-- child elements -->
                <xsl:call-template name="carnot-attributes"/>
                <xsl:call-template name="carnot-description"/>
		    </linkType>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-triggers">
	    <xsl:for-each select="c:Triggers/c:Trigger">
	        <trigger>
	        	<!-- attributes -->
	        	<xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
		        <xsl:if test="@Type">
		            <xsl:attribute name="type"><xsl:value-of select="@Type"/></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->
	            <xsl:call-template name="carnot-attributes"/>
	            <xsl:call-template name="carnot-description"/>
	            <xsl:call-template name="carnot-access-points"/>
	            <xsl:call-template name="carnot-parameter-mappings"/>
	        </trigger>
	    </xsl:for-each>
	</xsl:template>
		    
	<xsl:template name="carnot-trigger-type">
	    <xsl:for-each select="c:MetaTypes/c:TriggerTypes/c:TriggerType">
		    <triggerType>
		    	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid" />
	            <xsl:call-template name="xpdl-id-and-name" />
		        <xsl:if test="@IsPredefined">
		            <xsl:attribute name="predefined"><xsl:value-of select="@IsPredefined" /></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@IsPullTrigger">
		            <xsl:attribute name="pullTrigger"><xsl:value-of select="@IsPullTrigger" /></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@PanelClass">
		            <xsl:attribute name="panelClass"><xsl:value-of select="@PanelClass" /></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@PullTriggerEvaluator">
		            <xsl:attribute name="pullTriggerEvaluator"><xsl:value-of select="@PullTriggerEvaluator" /></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@Rule">
		            <xsl:attribute name="rule"><xsl:value-of select="@Rule" /></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->
		        <xsl:call-template name="carnot-attributes"/>
		        <xsl:call-template name="carnot-description"/>
		    </triggerType>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-data-paths">
	    <xsl:for-each select="c:DataPaths/c:DataPath">
	        <dataPath>
	        	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
	            <xsl:for-each select="c:DataRef">
	                <xsl:if test="@Id">
	                	<xsl:attribute name="data"><xsl:value-of select="@Id" /></xsl:attribute>
	                </xsl:if>
	                <xsl:if test="@Expression">
	                	<xsl:attribute name="dataPath"><xsl:value-of select="@Expression" /></xsl:attribute>
	                </xsl:if>
	            </xsl:for-each>
	            <xsl:if test="@IsDescriptor">
	                <xsl:attribute name="descriptor"><xsl:value-of select="@IsDescriptor" /></xsl:attribute>
	            </xsl:if>
	            <xsl:if test="@IsKey">
	                <xsl:attribute name="key"><xsl:value-of select="@IsKey" /></xsl:attribute>
	            </xsl:if>
		        <xsl:if test="@Direction">
		            <xsl:attribute name="direction"><xsl:value-of select="@Direction" /></xsl:attribute>
		        </xsl:if>	        

				<!-- child elements -->
                <xsl:call-template name="carnot-attributes"/>
                <xsl:call-template name="carnot-description"/>
	        </dataPath>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-event-handlers">
	    <xsl:for-each select="c:EventHandlers/c:EventHandler">
	        <eventHandler>
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
	            <xsl:if test="@IsAutomaticallyBound">
	                <xsl:attribute name="autoBind"><xsl:value-of select="@IsAutomaticallyBound" /></xsl:attribute>
	            </xsl:if>
	            <xsl:if test="@IsConsumedOnMatch">
	                <xsl:attribute name="consumeOnMatch"><xsl:value-of select="@IsConsumedOnMatch" /></xsl:attribute>
	            </xsl:if>
	            <xsl:if test="@IsLogged">
	                <xsl:attribute name="logHandler"><xsl:value-of select="@IsLogged" /></xsl:attribute>
	            </xsl:if>
		        <xsl:if test="@Type">
		            <xsl:attribute name="type"><xsl:value-of select="@Type" /></xsl:attribute>
		        </xsl:if>	        
	            <xsl:if test="@IsUnboundOnMatch">
	                <xsl:attribute name="unbindOnMatch"><xsl:value-of select="@IsUnboundOnMatch" /></xsl:attribute>
	            </xsl:if>

				<!-- child elements -->	            
	            <xsl:call-template name="carnot-attributes"/>
	            <xsl:call-template name="carnot-description"/>
	            
	            <xsl:for-each select="c:EventActions/c:EventAction[@Kind='BIND']">
	                <xsl:call-template name="carnot-event-action">
	                    <xsl:with-param name="elementName">bindAction</xsl:with-param>
	                </xsl:call-template>
	            </xsl:for-each>
	            <xsl:for-each select="c:EventActions/c:EventAction[@Kind='EVENT']">
	                <xsl:call-template name="carnot-event-action">
	                    <xsl:with-param name="elementName">eventAction</xsl:with-param>
	                </xsl:call-template>
	            </xsl:for-each>
	            <xsl:for-each select="c:EventActions/c:EventAction[@Kind='UNBIND']">
	                <xsl:call-template name="carnot-event-action">
	                    <xsl:with-param name="elementName">unbindAction</xsl:with-param>
	                </xsl:call-template>
	            </xsl:for-each>
	        </eventHandler>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-event-action">
	    <xsl:param name="elementName" />
	    <xsl:element name="{$elementName}">
	    	<!-- attributes -->
	        <xsl:call-template name="carnot-element-oid"/>
            <xsl:call-template name="xpdl-id-and-name"/>
	        <xsl:if test="@Type">
	            <xsl:attribute name="type"><xsl:value-of select="@Type"/></xsl:attribute>
	        </xsl:if>
	        
	        <!-- child elements -->
	        <xsl:call-template name="carnot-attributes"/>
	        <xsl:call-template name="carnot-description"/>
	    </xsl:element>
	</xsl:template>
	
	<xsl:template name="carnot-contexts">
	    <xsl:for-each select="c:ApplicationContexts/c:ApplicationContext">
	        <context>
	        	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
		        <xsl:if test="@Type">
		            <xsl:attribute name="type"><xsl:value-of select="@Type"/></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->     
	            <xsl:call-template name="carnot-attributes"/>
	            <xsl:call-template name="carnot-description"/>
	            <xsl:call-template name="carnot-access-points"/>
	        </context>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-access-points">
	    <xsl:for-each select="c:AccessPoints/c:AccessPoint">
		    <accessPoint>
		    	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
		        <xsl:if test="@Direction">
		            <xsl:attribute name="direction"><xsl:value-of select="@Direction"/></xsl:attribute>
		        </xsl:if>
		        <xsl:if test="@Type">
		            <xsl:attribute name="type"><xsl:value-of select="@Type"/></xsl:attribute>
		        </xsl:if>
		        
		        <!-- child elements -->
		        <xsl:call-template name="carnot-attributes"/>
                <xsl:call-template name="carnot-description"/>
		    </accessPoint>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-parameter-mappings">
	    <xsl:for-each select="c:DataFlows/c:DataFlow">
	        <parameterMapping>
	        	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	            
	            <xsl:if test="c:DataRef/@Expression">
		            <xsl:attribute name="dataPath"><xsl:value-of select="c:DataRef/@Expression" /></xsl:attribute>
	            </xsl:if>

	            <xsl:if test="c:DataRef/@Id">
		            <xsl:attribute name="data"><xsl:value-of select="c:DataRef/@Id"/></xsl:attribute>
	            </xsl:if>
	            <xsl:if test="c:AccessPointRef/@Id">
		            <xsl:attribute name="parameter"><xsl:value-of select="c:AccessPointRef/@Id"/></xsl:attribute>
	            </xsl:if>
	            <xsl:if test="c:AccessPointRef/@Expression">
		            <xsl:attribute name="parameterPath"><xsl:value-of select="c:AccessPointRef/@Expression"/></xsl:attribute>
	            </xsl:if>
	        </parameterMapping>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-view">
	    <xsl:for-each select="c:Views/c:View">
	        <view>
	        	<!-- attributes -->
		        <xsl:call-template name="carnot-element-oid"/>
	        	<xsl:call-template name="xpdl-id-and-name"/>
	        	
	            <!-- child elements -->
	            <xsl:call-template name="carnot-attributes"/>
	            <xsl:call-template name="carnot-description"/>
	            
	            <xsl:call-template name="carnot-view"/>
	            
	            <xsl:for-each select="c:Viewables/c:Viewable">
	                <viewable viewable="{@ModelElement}"/>
	            </xsl:for-each>
	        </view>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-diagram">
	    <xsl:for-each select="c:Diagrams/c:Diagram">
		    <diagram>
		    	<!-- attributes -->
	    		<!-- legacy model may not contain an oid attribute -->
		        <xsl:call-template name="carnot-element-oid"/>
	            <xsl:call-template name="xpdl-id-and-name"/>
	            <xsl:if test="@Orientation">
	                <xsl:attribute name="orientation"><xsl:value-of select="@Orientation"/></xsl:attribute>
	            </xsl:if>
	            <xsl:if test="@Mode">
	                <xsl:attribute name="mode"><xsl:value-of select="@Mode"/></xsl:attribute>
	            </xsl:if>
	            
	            <!-- child elements -->
		        <xsl:call-template name="carnot-attributes"/>
	            <xsl:call-template name="carnot-symbols"/>
	            <xsl:call-template name="carnot-connections"/>
		    </diagram>
	    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-symbols">
        <xsl:for-each select="c:Symbols/c:Symbol">
            <xsl:call-template name="carnot-symbol">
                <xsl:with-param name="elementName">
		            <xsl:choose>
		                <xsl:when test="@Kind='ACTIVITY'">activitySymbol</xsl:when>
		                <xsl:when test="@Kind='ANNOTATION'">annotationSymbol</xsl:when>
		                <xsl:when test="@Kind='APPLICATION'">applicationSymbol</xsl:when>
		                <xsl:when test="@Kind='CONDITIONAL_PERFORMER'">conditionalPerformerSymbol</xsl:when>
		                <xsl:when test="@Kind='DATA'">dataSymbol</xsl:when>
		                <xsl:when test="@Kind='GROUP'">groupSymbol</xsl:when>
		                <xsl:when test="@Kind='MODELER'">modelerSymbol</xsl:when>
		                <xsl:when test="@Kind='ORGANIZATION'">organizationSymbol</xsl:when>
		                <xsl:when test="@Kind='PROCESS'">processSymbol</xsl:when>
		                <xsl:when test="@Kind='ROLE'">roleSymbol</xsl:when>
		                <xsl:when test="@Kind='TEXT'">textSymbol</xsl:when>
                        <xsl:when test="@Kind='POOL'">poolSymbol</xsl:when>
                        <xsl:when test="@Kind='LANE'">laneSymbol</xsl:when>
                        <xsl:when test="@Kind='ENDEVENT'">endEventSymbol</xsl:when>
                        <xsl:when test="@Kind='GATEWAY'">gatewaySymbol</xsl:when>
                        <xsl:when test="@Kind='INTERMEDIATEEVENT'">intermediateEventSymbol</xsl:when>
                        <xsl:when test="@Kind='STARTEVENT'">startEventSymbol</xsl:when>
                        <xsl:when test="@Kind='PUBLICINTERFACE'">publicInterfaceSymbol</xsl:when>
		            </xsl:choose>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="carnot-symbol">
	    <xsl:param name="elementName" />
	    <xsl:element name="{$elementName}">
	    	<!-- attributes -->
	        <xsl:call-template name="carnot-element-oid"/>
		    <xsl:if test="@X">
			    <xsl:attribute name="x"><xsl:value-of select="@X"/></xsl:attribute>
		    </xsl:if>
		    <xsl:if test="@Y">
			    <xsl:attribute name="y"><xsl:value-of select="@Y"/></xsl:attribute>
		    </xsl:if>
            <xsl:if test="@Width">
                <xsl:attribute name="width"><xsl:value-of select="@Width"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@Height">
                <xsl:attribute name="height"><xsl:value-of select="@Height"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@Shape">
                <xsl:attribute name="shape"><xsl:value-of select="@Shape"/></xsl:attribute>
            </xsl:if>
			<xsl:if test="@BorderColor">
			    <xsl:attribute name="borderColor"><xsl:value-of select="@BorderColor"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@FillColor">
			    <xsl:attribute name="fillColor"><xsl:value-of select="@FillColor"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@Style">
			    <xsl:attribute name="style"><xsl:value-of select="@Style"/></xsl:attribute>
			</xsl:if>
            <xsl:if test="@FlowKind">
                <xsl:attribute name="flowKind"><xsl:value-of select="@FlowKind"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@ModelElement">
                <xsl:attribute name="refer"><xsl:value-of select="@ModelElement"/></xsl:attribute>
            </xsl:if>
            <xsl:call-template name="color-and-style"/>
            <xsl:if test="@Kind='POOL' or @Kind='LANE'">
	            <xsl:call-template name="xpdl-id-and-name"/>
                <xsl:if test="@Orientation">            
                    <xsl:attribute name="orientation"><xsl:value-of select="@Orientation"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="@Collapsed">            
                    <xsl:attribute name="collapsed"><xsl:value-of select="@Collapsed"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="@Participant">
                    <xsl:attribute name="participant"><xsl:value-of select="@Participant"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="@BoundaryVisible">
                    <xsl:attribute name="boundaryVisible"><xsl:value-of select="@BoundaryVisible"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="@Process">
                    <xsl:attribute name="process"><xsl:value-of select="@Process"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="@ParentLane">
                    <xsl:attribute name="parentLane"><xsl:value-of select="@ParentLane"/></xsl:attribute>
                </xsl:if>
				<xsl:call-template name="carnot-attributes"/>                
            </xsl:if>
		    <xsl:if test="c:Text">
		        <xsl:choose>
		            <xsl:when test="@Kind='TEXT'">
				        <xsl:attribute name="text"><xsl:value-of select="c:Text"/></xsl:attribute>
		            </xsl:when>
			        
			<!-- child elements -->
			        <xsl:otherwise>
				        <text>
				        	<!--xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text-->
				        	<xsl:value-of select="c:Text"/>
				        	<!--xsl:text disable-output-escaping="yes">]]&gt;</xsl:text-->
				        </text>
			        </xsl:otherwise>
		        </xsl:choose>
		    </xsl:if>
		    
		    <xsl:call-template name="carnot-symbols"/>
		    <xsl:call-template name="carnot-connections"/>
	    </xsl:element>
	</xsl:template>
	
	<xsl:template name="carnot-connections">
	    <xsl:for-each select="c:Connections/c:Connection">
            <xsl:choose>
                <xsl:when test="@Kind='DATA_FLOW'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">dataMappingConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">dataSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">activitySymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='EXECUTED_BY'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">executedByConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">applicationSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">activitySymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='GENERIC_LINK'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">genericLinkConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">sourceSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">targetSymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='PART_OF'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">partOfConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">suborganizationSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">organizationSymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='PERFORMS'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">performsConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">participantSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">activitySymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='TRIGGERS'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">triggersConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">participantSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">startEventSymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>                
                <xsl:when test="@Kind='REFERS_TO'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">refersToConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">from</xsl:with-param>
			            <xsl:with-param name="targetName">to</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='SUBPROCESS_OF'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">subProcessOfConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">subprocessSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">processSymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='TEAM_LEAD'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">teamLeadConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">teamLeadSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">teamSymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='TRANSITION'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">transitionConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">sourceActivitySymbol</xsl:with-param>
			            <xsl:with-param name="targetName">targetActivitySymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
                <xsl:when test="@Kind='WORKS_FOR'">
			        <xsl:call-template name="carnot-connection">
			            <xsl:with-param name="elementName">worksForConnection</xsl:with-param>
			            <xsl:with-param name="sourceName">participantSymbol</xsl:with-param>
			            <xsl:with-param name="targetName">organizationSymbol</xsl:with-param>
			        </xsl:call-template>
                </xsl:when>
            </xsl:choose>
	    </xsl:for-each>
	</xsl:template>
	
    <xsl:template name="color-and-style">
        <xsl:if test="@BorderColor">
            <xsl:attribute name="borderColor"><xsl:value-of select="@BorderColor"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@FillColor">
            <xsl:attribute name="fillColor"><xsl:value-of select="@FillColor"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@Style">
            <xsl:attribute name="style"><xsl:value-of select="@Style"/></xsl:attribute>
        </xsl:if>
    </xsl:template>
    
	<xsl:template name="carnot-connection">
	    <xsl:param name="elementName"/>
	    <xsl:param name="sourceName"/>
	    <xsl:param name="targetName"/>
	    <xsl:element name="{$elementName}">
	    	<!-- attributes -->
	        <xsl:call-template name="carnot-element-oid"/>
	        <xsl:if test="c:Points">
	            <xsl:attribute name="points"><xsl:value-of select="c:Points"/></xsl:attribute>
	        </xsl:if>
            <xsl:if test="@TargetAnchor">
                <xsl:attribute name="targetAnchor"><xsl:value-of select="@TargetAnchor"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@SourceAnchor">
                <xsl:attribute name="sourceAnchor"><xsl:value-of select="@SourceAnchor"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@TargetSymbol">
		        <xsl:attribute name="{$targetName}"><xsl:value-of select="@TargetSymbol"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@SourceSymbol">
		        <xsl:attribute name="{$sourceName}"><xsl:value-of select="@SourceSymbol"/></xsl:attribute>
            </xsl:if>
	        <xsl:if test="@LinkType">
	            <xsl:attribute name="linkType"><xsl:value-of select="@LinkType"/></xsl:attribute>
	        </xsl:if>
	        <xsl:if test="@ModelElement">
	            <xsl:choose>
	                <xsl:when test="@Kind='TRANSITION'">
	                    <xsl:attribute name="transition"><xsl:value-of select="@ModelElement"/></xsl:attribute>
	                </xsl:when>
	            </xsl:choose>
	        </xsl:if>
            <xsl:call-template name="color-and-style"/>
            <xsl:if test="@Routing">
                <xsl:attribute name="routing"><xsl:value-of select="@Routing"/></xsl:attribute>
            </xsl:if>
            
            <!-- child elements -->
            <xsl:call-template name="carnot-coordinates"/>
	    </xsl:element>
	</xsl:template>

    <xsl:template name="carnot-coordinates">
       	<xsl:for-each select="c:Coordinate">
            <coordinate>
                <xsl:attribute name="x"><xsl:value-of select="@XPos"/></xsl:attribute>
                <xsl:attribute name="y"><xsl:value-of select="@YPos"/></xsl:attribute>
            </coordinate>
		</xsl:for-each>
    </xsl:template>
 
	<xsl:template name="third-party-extended-attributes">
		<!-- Save any non-Carnot ExtendedAttributes -->
		<xsl:variable name="attrs" select="x1:ExtendedAttributes/x1:ExtendedAttribute|x2:ExtendedAttributes/x2:ExtendedAttribute"/>
		<xsl:variable name="other" select="$attrs[@Name!='CarnotExt']"/>
		<xsl:if test="$other">
			<attribute name="carnot:model:xpdl:extendedAttributes">
				<xsl:copy-of select="$other"/>
			</attribute>
		</xsl:if>
	</xsl:template>

	<xsl:template name="carnot-attributes">
		<!-- Will get called with the carnot specific IExtensibleElement as current element, i.e. x2:ExtendedAttribute/c:Activity -->	
		<xsl:param name="filter1"/>
		<xsl:param name="filter2"/>
		<xsl:for-each select="c:Attributes/c:Attribute">
			<xsl:if test="not(@Name=$filter1) and not(@Name=$filter2)">
				<attribute>
					<!-- attributes -->
					<xsl:call-template name="xpdl-id-and-name"/>
					<xsl:if test="@Type">
						<xsl:attribute name="type"><xsl:value-of select="@Type"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@Value">
						<xsl:attribute name="value"><xsl:value-of select="@Value"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="c:Value">
						<xsl:copy-of select="c:Value/*"/>
					</xsl:if>
					<xsl:if test="not(@Value) and not(c:Value)">
						<xsl:message>Missing value for attribute '<xsl:value-of select="@Name"/>'.</xsl:message>
					</xsl:if>
				</attribute>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>
