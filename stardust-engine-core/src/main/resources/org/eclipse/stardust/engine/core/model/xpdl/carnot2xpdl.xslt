<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xpdl="http://www.wfmc.org/2008/XPDL2.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wfm="http://www.carnot.ag/workflowmodel/3.1" exclude-result-prefixes="wfm xpdl">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/">
		<xsl:apply-templates select="wfm:model"/>
	</xsl:template>

	<xsl:template match="wfm:model">
		<Package xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">

		    <xsl:attribute namespace="http://www.w3.org/2001/XMLSchema-instance" name="xsi:schemaLocation">http://www.wfmc.org/2008/XPDL2.1 http://www.wfmc.org/standards/docs/bpmnxpdl_31.xsd</xsl:attribute>

			<xsl:call-template name="element-id-and-name" />

			<PackageHeader>
				<XPDLVersion>2.1</XPDLVersion>
				<!-- TODO replace with real version during build -->
				<Vendor><xsl:value-of select="@vendor" /></Vendor>
				<Created><xsl:value-of select="@created" /></Created>
				<xsl:call-template name="element-description" />
				<!-- Documentation not available from Carnot -->
				<!-- PriorityUnit not available from Carnot -->
				<!-- CostUnit not available from Carnot -->
			</PackageHeader>

			<RedefinableHeader>
				<xsl:attribute name="PublicationStatus">
					<xsl:choose>
						<xsl:when test="wfm:attribute[@name='carnot:engine:released']/@value='true'">RELEASED</xsl:when>
						<!-- UNDER_TEST not available from Carnot -->
						<xsl:otherwise>UNDER_REVISION</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>

				<Author><xsl:value-of select="@author" /></Author>
				<xsl:if test="wfm:attribute[@name='carnot:engine:version']">
					<Version><xsl:value-of select="wfm:attribute[@name='carnot:engine:version']/@value" /></Version>
				</xsl:if>
				<!-- CodePage not available from Carnot -->
				<!-- CountryKey not available from Carnot -->
				<!-- Responsibles is not available from Carnot -->
			</RedefinableHeader>

			<!-- TODO consider full conformance computation -->
			<ConformanceClass GraphConformance="NON_BLOCKED" />

			<xsl:copy-of select="xpdl:Script"/>
			<xsl:copy-of select="xpdl:ExternalPackages"/>
			<xsl:copy-of select="xpdl:TypeDeclarations"/>
			<xsl:copy-of select="carnot:qualityControl"/>

			<Participants>
				<xsl:apply-templates select="wfm:role"/>
				<xsl:apply-templates select="wfm:organization"/>
				<xsl:apply-templates select="wfm:conditionalPerformer"/>
			</Participants>

			<Applications>
				<xsl:apply-templates select="wfm:application"/>
			</Applications>

			<DataFields>
				<!-- TODO -->
				<xsl:apply-templates select="wfm:data" />
			</DataFields>

			<WorkflowProcesses>
				<xsl:apply-templates select="wfm:processDefinition"/>
			</WorkflowProcesses>

			<ExtendedAttributes>
				<ExtendedAttribute Name="CarnotExt">
					<carnot:Package>
					    <xsl:call-template name="element-oid" />

						<xsl:attribute name="CarnotVersion"><xsl:value-of select="@carnotVersion" /></xsl:attribute>
						<xsl:if test="@modelOID">
							<xsl:attribute name="ModelOid"><xsl:value-of select="@modelOID" /></xsl:attribute>
						</xsl:if>

						<carnot:MetaTypes>
							<xsl:call-template name="carnot-data-types" />
							<xsl:call-template name="carnot-application-types" />
							<xsl:call-template name="carnot-application-context-types" />
							<xsl:call-template name="carnot-trigger-types" />
							<xsl:call-template name="carnot-event-condition-types" />
							<xsl:call-template name="carnot-event-action-types" />
							<xsl:call-template name="carnot-link-types" />
						</carnot:MetaTypes>

						<xsl:call-template name="carnot-modelers" />

						<xsl:call-template name="carnot-diagrams" />

						<xsl:call-template name="carnot-views" />

						<xsl:if test="wfm:attribute">
						    <carnot:Attributes>
								<xsl:for-each select="wfm:attribute[@name != 'carnot:model:xpdl:extendedAttributes']">
								    <xsl:if test="(@name != 'carnot:engine:version') and (@name != 'carnot:engine:released')">
								        <xsl:call-template name="carnot-attribute" />
								    </xsl:if>
								</xsl:for-each>
							</carnot:Attributes>
						</xsl:if>

					</carnot:Package>
				</ExtendedAttribute>
				<!-- TODO -->

				<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />

			</ExtendedAttributes>
		</Package>
	</xsl:template>

	<xsl:template match="wfm:role">
		<Participant xmlns="http://www.wfmc.org/2008/XPDL2.1">

			<xsl:call-template name="element-id-and-name" />

			<ParticipantType Type="ROLE" />
			<xsl:call-template name="element-proxy" />

			<xsl:call-template name="element-description" />

			<xsl:if test="not(@proxy)">
				<ExtendedAttributes>
					<ExtendedAttribute Name="CarnotExt" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
						<carnot:Role>
						    <xsl:call-template name="element-oid" />

							<xsl:if test="@cardinality">
							  <xsl:attribute name="Cardinality"><xsl:value-of select="@cardinality" /></xsl:attribute>
							</xsl:if>

							<xsl:call-template name="carnot-attributes" />
						</carnot:Role>
					</ExtendedAttribute>

					<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />

				</ExtendedAttributes>
			</xsl:if>
		</Participant>
	</xsl:template>

	<xsl:template match="wfm:organization">
		<Participant xmlns="http://www.wfmc.org/2008/XPDL2.1">
			<xsl:call-template name="element-id-and-name" />

			<ParticipantType Type="ORGANIZATIONAL_UNIT" />
			<xsl:call-template name="element-proxy" />

			<xsl:call-template name="element-description" />

			<xsl:if test="not(@proxy)">
				<ExtendedAttributes>
					<ExtendedAttribute Name="CarnotExt" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
						<carnot:Organization>
							<xsl:call-template name="element-oid" />

							<xsl:if test="@teamLead">
							    <xsl:attribute name="TeamLead"><xsl:value-of select="@teamLead" /></xsl:attribute>
							</xsl:if>

							<xsl:if test="wfm:participant">
								<carnot:Members>
									<xsl:for-each select="wfm:participant">
										<carnot:Member>
											<xsl:attribute name="Id"><xsl:value-of select="@participant" /></xsl:attribute>
										</carnot:Member>
									</xsl:for-each>
								</carnot:Members>
							</xsl:if>
							<xsl:call-template name="carnot-attributes" />
						</carnot:Organization>
					</ExtendedAttribute>

					<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />

				</ExtendedAttributes>
			</xsl:if>
		</Participant>
	</xsl:template>

	<xsl:template match="wfm:conditionalPerformer">
		<Participant xmlns="http://www.wfmc.org/2008/XPDL2.1">
			<xsl:call-template name="element-id-and-name" />

			<ParticipantType>
				<xsl:attribute name="Type">
					<xsl:choose>
						<xsl:when test="@is_user='true'">HUMAN</xsl:when>
						<!-- TODO is ROLE the best guess for participant type conditional performers? -->
						<xsl:otherwise>ROLE</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</ParticipantType>
			<xsl:call-template name="element-proxy" />

			<xsl:call-template name="element-description" />

			<xsl:if test="not(@proxy)">
				<ExtendedAttributes>
					<ExtendedAttribute Name="CarnotExt" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
						<carnot:ConditionalPerformer>
							<xsl:call-template name="element-oid" />
							<xsl:attribute name="DataId"><xsl:value-of select="@data" /></xsl:attribute>
							<xsl:if test="@dataPath">
								<xsl:attribute name="DataPath"><xsl:value-of select="@dataPath" /></xsl:attribute>
							</xsl:if>

							<xsl:call-template name="carnot-attributes" />
						</carnot:ConditionalPerformer>
					</ExtendedAttribute>

					<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />

				</ExtendedAttributes>
			</xsl:if>
		</Participant>
	</xsl:template>

	<xsl:template match="wfm:application">
		<Application xmlns="http://www.wfmc.org/2008/XPDL2.1">
			<xsl:call-template name="element-id-and-name" />
			<xsl:call-template name="element-description" />

			<!-- TODO apply access points at all? -->

			<xsl:choose>
				<xsl:when test="@type='sessionBean'">
					<ExternalReference>
					    <xsl:variable name="methodName" select="carnot-xpdl-utils:encodeMethodName(wfm:attribute[@name='carnot:engine:methodName']/@value)" xmlns:carnot-xpdl-utils="xalan://org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils" />

						<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:remoteInterface']/@value" /></xsl:attribute>
						<xsl:attribute name="xref"><xsl:value-of select="$methodName" /></xsl:attribute>
					</ExternalReference>
				</xsl:when>
				<xsl:when test="@type='plainJava'">
					<ExternalReference>
					    <xsl:variable name="methodName" select="carnot-xpdl-utils:encodeMethodName(wfm:attribute[@name='carnot:engine:methodName']/@value)" xmlns:carnot-xpdl-utils="xalan://org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils" />

						<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:className']/@value" /></xsl:attribute>
						<xsl:attribute name="xref"><xsl:value-of select="$methodName" /></xsl:attribute>
					</ExternalReference>
				</xsl:when>
				<xsl:when test="@type='jms'">
					<!-- TODO consider Text and Object message types having implicit accss points -->
					<xsl:call-template name="formal-parameters" />
				</xsl:when>
				<xsl:when test="@type='messageTransformationBean'">
					<xsl:call-template name="formal-parameters" />
				</xsl:when>
				<xsl:when test="@type='messageSerializationBean'">
					<xsl:call-template name="formal-parameters" />
				</xsl:when>
				<xsl:when test="@type='messageParsingBean'">
					<xsl:call-template name="formal-parameters" />
				</xsl:when>
				<xsl:when test="@type='sapr3application'">
					<!-- TODO -->
					<xsl:call-template name="formal-parameters" />
				</xsl:when>
				<xsl:when test="@type='webservice'">
					<ExternalReference>
						<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:wsdlUrl']/@value" /></xsl:attribute>
						<xsl:attribute name="xref"><xsl:value-of select="wfm:attribute[@name='carnot:engine:wsOperationName']/@value" /></xsl:attribute>
						<xsl:attribute name="namespace"><xsl:value-of select="wfm:attribute[@name='carnot:engine:wsPortName']/@value" /></xsl:attribute>
					</ExternalReference>
				</xsl:when>
				<xsl:when test="@interactive='true'">
					<ExternalReference>
						<xsl:attribute name="location">org.eclipse.stardust.engine.api.model.IApplication</xsl:attribute>
						<xsl:attribute name="xref">INTERACTIVE</xsl:attribute>
					</ExternalReference>
				</xsl:when>
			</xsl:choose>

			<!-- TODO refine -->
			<ExtendedAttributes>
				<ExtendedAttribute Name="CarnotExt">
					<carnot:Application xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
						<xsl:call-template name="element-oid" />
						<xsl:if test="@type">
							<xsl:attribute name="Type"><xsl:value-of select="@type" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@interactive">
							<xsl:attribute name="IsInteractive"><xsl:value-of select="@interactive" /></xsl:attribute>
						</xsl:if>

						<carnot:ApplicationContexts>
							<xsl:for-each select="wfm:context">
								<carnot:ApplicationContext>
									<xsl:call-template name="element-oid" />
									<xsl:attribute name="Type"><xsl:value-of select="@type" /></xsl:attribute>

									<xsl:call-template name="carnot-description" />

									<xsl:call-template name="carnot-access-points" />

									<xsl:call-template name="carnot-attributes" />
								</carnot:ApplicationContext>
							</xsl:for-each>
						</carnot:ApplicationContexts>

						<xsl:call-template name="carnot-access-points" />

						<xsl:call-template name="carnot-attributes" />
					</carnot:Application>
				</ExtendedAttribute>

				<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />

			</ExtendedAttributes>
		</Application>
	</xsl:template>

	<xsl:template match="wfm:data">
		<DataField xmlns="http://www.wfmc.org/2008/XPDL2.1">

			<xsl:call-template name="element-id-and-name" />

			<xsl:if test="not(@proxy)">
				<!-- Carnot currently does not support arrays -->
				<xsl:attribute name="IsArray">FALSE</xsl:attribute>
			</xsl:if>

			<xsl:call-template name="data-type" />

			<xsl:if test="not(@proxy)">
				<xsl:if test="@type = 'primitive' and wfm:attribute[@name='carnot:engine:defaultValue']">
					<InitialValue><xsl:value-of select="wfm:attribute[@name='carnot:engine:defaultValue']/@value" /></InitialValue>
				</xsl:if>
				<!-- Length not needed as Carnot does not support arrays currently -->

				<xsl:call-template name="element-description" />

				<ExtendedAttributes>
					<ExtendedAttribute Name="CarnotExt">
						<carnot:DataField xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
							<xsl:call-template name="element-oid" />
							<xsl:if test="@type">
								<xsl:attribute name="Type"><xsl:value-of select="@type" /></xsl:attribute>
							</xsl:if>
							<xsl:if test="@predefined">
								<xsl:attribute name="IsPredefined"><xsl:value-of select="@predefined" /></xsl:attribute>
							</xsl:if>

							<xsl:if test="wfm:attribute">
							    <carnot:Attributes>
									<xsl:for-each select="wfm:attribute[@name != 'carnot:model:xpdl:extendedAttributes']">
									    <xsl:if test="(@name != 'carnot:engine:dataType')">
									        <xsl:call-template name="carnot-attribute" />
									    </xsl:if>
									</xsl:for-each>
								</carnot:Attributes>
							</xsl:if>
						</carnot:DataField>
					</ExtendedAttribute>

					<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />

				</ExtendedAttributes>
			</xsl:if>

		</DataField>
	</xsl:template>

	<xsl:template match="wfm:processDefinition">
		<WorkflowProcess xmlns="http://www.wfmc.org/2008/XPDL2.1">
			<xsl:call-template name="element-id-and-name" />
			<xsl:attribute name="AccessLevel">
				<xsl:choose>
					<xsl:when test="wfm:trigger">PUBLIC</xsl:when>
					<xsl:otherwise>PRIVATE</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>

			<ProcessHeader>
				<!-- no individual creation date available -->
				<xsl:call-template name="element-description" />

				<xsl:if test="@defaultPriority">
		            <Priority xmlns="http://www.wfmc.org/2008/XPDL2.1"><xsl:value-of select="@defaultPriority" /></Priority>
                </xsl:if>

				<!-- Limit is not available from Carnot, as timeouts are separately handled with events -->
				<!-- ValidFrom will be inherited from Package, TODO investigate Package:ValidFrom -->
				<!-- ValidTo will be inherited from Package, TODO investigate Package:ValidTo -->
				<!-- TimeEstimation is not available from Carnot -->
			</ProcessHeader>

			<!-- no need for RedefinableHeader, as all contained info is inherited from Package -->

			<xsl:copy-of select="xpdl:FormalParameters"/>

			<!-- DataTypes with process scope are not available from Carnot -->

			<!-- DataFields with process scope are not available from Carnot -->

			<!-- Participants with process scope are not available from Carnot -->

			<!-- Applications with process scope are not available from Carnot -->

			<!-- no need for ActivitySets, as Carnot does not support anonymous blocks -->

			<Activities>
				<xsl:apply-templates select="wfm:activity"/>
			</Activities>

			<Transitions>
				<xsl:apply-templates select="wfm:transition"/>
			</Transitions>

			<ExtendedAttributes>
				<ExtendedAttribute Name="CarnotExt">
					<carnot:WorkflowProcess xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
						<xsl:call-template name="element-oid" />
					    <xsl:if test="wfm:externalReference">
							<carnot:Implements>
								<xsl:if test="wfm:externalReference/@PackageRef">
									<xsl:attribute name="PackageRef"><xsl:value-of select="wfm:externalReference/@PackageRef" /></xsl:attribute>
									<xsl:attribute name="ref"><xsl:value-of select="wfm:externalReference/@ref" /></xsl:attribute>
								</xsl:if>
							</carnot:Implements>
						</xsl:if>
						<carnot:Triggers>
							<xsl:for-each select="wfm:trigger">
								<carnot:Trigger>
									<!-- TODO -->
									<xsl:call-template name="element-oid" />
									<xsl:call-template name="element-id-and-name" />

									<xsl:attribute name="Type"><xsl:value-of select="@type" /></xsl:attribute>

									<xsl:call-template name="carnot-description" />
									<xsl:call-template name="carnot-access-points" />

									<carnot:DataFlows>
										<xsl:for-each select="wfm:parameterMapping">
											<carnot:DataFlow>
												<xsl:call-template name="element-oid" />
												<xsl:if test="@data or @dataPath">
													<carnot:DataRef Id="{@data}">
													    <xsl:if test="@data">
															<xsl:attribute name="Id"><xsl:value-of select="@data" /></xsl:attribute>
													    </xsl:if>
														<xsl:if test="@dataPath">
															<xsl:attribute name="Expression"><xsl:value-of select="@dataPath" /></xsl:attribute>
														</xsl:if>
													</carnot:DataRef>
												</xsl:if>
												<xsl:if test="@parameter or @parameterPath">
													<carnot:AccessPointRef>
													    <xsl:if test="@parameter">
															<xsl:attribute name="Id"><xsl:value-of select="@parameter" /></xsl:attribute>
													    </xsl:if>
														<xsl:if test="@parameterPath">
															<xsl:attribute name="Expression"><xsl:value-of select="@parameterPath" /></xsl:attribute>
														</xsl:if>
													</carnot:AccessPointRef>
												</xsl:if>
											</carnot:DataFlow>
										</xsl:for-each>
									</carnot:DataFlows>

									<xsl:call-template name="carnot-attributes" />
								</carnot:Trigger>
							</xsl:for-each>
						</carnot:Triggers>

						<carnot:DataPaths>
							<xsl:for-each select="wfm:dataPath">
								<carnot:DataPath>
									<xsl:call-template name="element-oid" />
									<xsl:call-template name="element-id-and-name" />

									<xsl:attribute name="Direction"><xsl:value-of select="@direction" /></xsl:attribute>
									<xsl:if test="@descriptor">
										<xsl:attribute name="IsDescriptor"><xsl:value-of select="@descriptor" /></xsl:attribute>
									</xsl:if>
									<xsl:if test="@key">
										<xsl:attribute name="IsKey"><xsl:value-of select="@key" /></xsl:attribute>
									</xsl:if>
									<carnot:DataRef>
										<xsl:attribute name="Id"><xsl:value-of select="@data" /></xsl:attribute>
										<xsl:if test="@dataPath">
											<xsl:attribute name="Expression"><xsl:value-of select="@dataPath" /></xsl:attribute>
										</xsl:if>
									</carnot:DataRef>

									<xsl:call-template name="carnot-description" />
									<xsl:call-template name="carnot-attributes" />
								</carnot:DataPath>
							</xsl:for-each>
						</carnot:DataPaths>

						<xsl:call-template name="carnot-event-handlers" />

						<xsl:call-template name="carnot-attributes" />

						<xsl:call-template name="carnot-diagrams" />

						<xsl:copy-of select="carnot:FormalParameterMappings"/>

					</carnot:WorkflowProcess>
				</ExtendedAttribute>

				<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />
			</ExtendedAttributes>
		</WorkflowProcess>
	</xsl:template>

	<xsl:template match="wfm:activity">
		<Activity xmlns="http://www.wfmc.org/2008/XPDL2.1">
			<xsl:call-template name="element-id-and-name" />
			<xsl:call-template name="element-description" />

			<!-- Limit is not available from Carnot, as timeouts are separately handled with events -->

			<!-- configure (Route | Implementation), Performer, StartMode and FinishMode according to activity implementation type -->
			<xsl:choose>
				<xsl:when test="@implementation='Manual' or not(@implementation)">
					<Implementation>
						<No/>
					</Implementation>
					<Performer><xsl:value-of select="@performer"/></Performer>
					<xsl:if test="@qualityControlPerformer">
						<QualityControlPerformer><xsl:value-of select="@qualityControlPerformer"/></QualityControlPerformer>
					</xsl:if>
					<StartMode>
						<Automatic/>
					</StartMode>
					<FinishMode>
						<Manual/>
					</FinishMode>
				</xsl:when>

				<xsl:when test="@implementation='Application'">
					<Implementation>
						<Task>
							<TaskApplication>
							    <xsl:choose>
							    	<xsl:when test="wfm:externalReference">
										<xsl:if test="wfm:externalReference/@ref">
											<xsl:attribute name="Id"><xsl:value-of select="wfm:externalReference/@ref"/></xsl:attribute>
										</xsl:if>
										<xsl:if test="wfm:externalReference/@PackageRef">
											<xsl:attribute name="PackageRef"><xsl:value-of select="wfm:externalReference/@PackageRef"/></xsl:attribute>
										</xsl:if>
							    	</xsl:when>
							    	<xsl:otherwise>
										<xsl:attribute name="Id"><xsl:value-of select="@application"/></xsl:attribute>
							    	</xsl:otherwise>
							    </xsl:choose>

								<!-- TODO order ActualParameters -->
								<ActualParameters>
									<xsl:for-each select="wfm:dataMapping">
										<xsl:choose>
											<xsl:when test="@dataPath">
												<ActualParameter><xsl:value-of select="concat(concat(@data, '->'), @dataPath)" /></ActualParameter>
											</xsl:when>
											<xsl:otherwise>
												<ActualParameter><xsl:value-of select="@data" /></ActualParameter>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:for-each>
								</ActualParameters>

								<xsl:call-template name="element-description" />

								<!-- TODO ExtendedAttributes -->
							</TaskApplication>
						</Task>
					</Implementation>
					<xsl:if test="@performer">
						<Performer><xsl:value-of select="@performer"/></Performer>
					</xsl:if>
					<xsl:if test="@qualityControlPerformer">
						<QualityControlPerformer><xsl:value-of select="@qualityControlPerformer"/></QualityControlPerformer>
					</xsl:if>
					<StartMode>
						<Automatic/>
					</StartMode>
					<FinishMode>
						<Automatic/>
					</FinishMode>
				</xsl:when>

				<!--  TODO consider JMS trigger calls as asynchronous SubFlow-->
				<xsl:when test="@implementation='Subprocess'">
					<Implementation>
						<SubFlow>
						    <xsl:choose>
						    	<xsl:when test="wfm:externalReference">
									<xsl:if test="wfm:externalReference/@ref">
										<xsl:attribute name="Id"><xsl:value-of select="wfm:externalReference/@ref"/></xsl:attribute>
									</xsl:if>
									<xsl:if test="wfm:externalReference/@PackageRef">
										<xsl:attribute name="PackageRef"><xsl:value-of select="wfm:externalReference/@PackageRef"/></xsl:attribute>
									</xsl:if>
						    	</xsl:when>
						    	<xsl:otherwise>
									<xsl:attribute name="Id"><xsl:value-of select="@implementationProcess"/></xsl:attribute>
						    	</xsl:otherwise>
						    </xsl:choose>
							<xsl:attribute name="Execution">SYNCHR</xsl:attribute>

							<!-- ActualParameters are not available, as Carnot currently does not support parameter passing to subprocesses -->
						</SubFlow>
					</Implementation>
					<xsl:if test="@performer">
					    <Performer><xsl:value-of select="@performer" /></Performer>
					</xsl:if>
   					<StartMode>
						<Automatic/>
					</StartMode>
					<FinishMode>
						<Automatic/>
					</FinishMode>
				</xsl:when>

				<xsl:otherwise>
					<Route />
					<!-- TODO -->
				</xsl:otherwise>
			</xsl:choose>

			<!-- Priority is not available from Carnot -->
			<!-- Deadline is not available from Carnot, as timeouts are separately handled with events -->
			<!-- SimulationInformation is not available from Carnot -->
			<!-- Icon is not available from Carnot -->
			<!-- Documentation is not available from Carnot -->

			<xsl:if test="@split='AND' or @join='AND' or @split='XOR' or @join='XOR'">
				<TransitionRestrictions>
					<xsl:if test="@join='AND' or @join='XOR'">
						<TransitionRestriction>
							<Join>
								<xsl:attribute name="Type"><xsl:value-of select="@join"/></xsl:attribute>
							</Join>
						</TransitionRestriction>
					</xsl:if>
					<xsl:if test="@split='AND' or @split='XOR'">
						<TransitionRestriction>
							<Split>
								<xsl:attribute name="Type"><xsl:value-of select="@split"/></xsl:attribute>
								<TransitionRefs>
									<xsl:call-template name="source-transition-ref">
										<xsl:with-param name="source" select="@id"/>
									</xsl:call-template>
								</TransitionRefs>
							</Split>
						</TransitionRestriction>
					</xsl:if>
				</TransitionRestrictions>
			</xsl:if>

			<!-- TODO ExtendedAttributes -->
			<ExtendedAttributes>
				<ExtendedAttribute Name="CarnotExt">
					<carnot:Activity xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
						<xsl:call-template name="element-oid" />
						<xsl:if test="@implementation">
							<xsl:attribute name="Implementation"><xsl:value-of select="@implementation" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@loopType">
							<xsl:attribute name="LoopType"><xsl:value-of select="@loopType" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@loopCondition">
							<xsl:attribute name="LoopCondition"><xsl:value-of select="@loopCondition" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@allowsAbortByPerformer">
							<xsl:attribute name="IsAbortableByPerformer"><xsl:value-of select="@allowsAbortByPerformer" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@hibernateOnCreation">
							<xsl:attribute name="IsHibernatedOnCreation"><xsl:value-of select="@hibernateOnCreation" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@subProcessMode">
							<xsl:attribute name="SubProcessMode"><xsl:value-of select="@subProcessMode" /></xsl:attribute>
						</xsl:if>

						<xsl:call-template name="carnot-event-handlers" />

						<carnot:QualityCodes>
							<xsl:for-each select="wfm:validQualityCodes">
								<carnot:QualityCode><xsl:value-of select="." /></carnot:QualityCode>
							</xsl:for-each>
						</carnot:QualityCodes>

						<carnot:DataFlows>
							<xsl:for-each select="wfm:dataMapping">
								<carnot:DataFlow>
								    <xsl:call-template name="element-oid" />
									<xsl:call-template name="element-id-and-name" />

									<xsl:if test="@id">
										<xsl:attribute name="Id"><xsl:value-of select="@id" /></xsl:attribute>
									</xsl:if>
									<xsl:attribute name="Direction"><xsl:value-of select="@direction" /></xsl:attribute>
									<xsl:if test="@context">
										<xsl:attribute name="Context"><xsl:value-of select="@context" /></xsl:attribute>
									</xsl:if>

									<carnot:DataRef>
										<xsl:attribute name="Id"><xsl:value-of select="@data" /></xsl:attribute>
										<xsl:if test="@dataPath">
											<xsl:attribute name="Expression"><xsl:value-of select="@dataPath" /></xsl:attribute>
										</xsl:if>
									</carnot:DataRef>
									<carnot:AccessPointRef>
										<xsl:if test="@applicationAccessPoint">
											<xsl:attribute name="Id"><xsl:value-of select="@applicationAccessPoint" /></xsl:attribute>
										</xsl:if>
										<xsl:if test="@applicationPath">
											<xsl:attribute name="Expression"><xsl:value-of select="@applicationPath" /></xsl:attribute>
										</xsl:if>
									</carnot:AccessPointRef>
								</carnot:DataFlow>
							</xsl:for-each>
						</carnot:DataFlows>

						<xsl:call-template name="carnot-attributes" />
					</carnot:Activity>
				</ExtendedAttribute>

				<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />

			</ExtendedAttributes>

		</Activity>
	</xsl:template>

	<xsl:template match="wfm:transition">
		<Transition xmlns="http://www.wfmc.org/2008/XPDL2.1">
			<xsl:call-template name="element-id-and-name" />

			<xsl:attribute name="From"><xsl:value-of select="@from"/></xsl:attribute>
			<xsl:attribute name="To"><xsl:value-of select="@to"/></xsl:attribute>

			<!-- TODO consider EXCEPTION, DEFAULTEXCEPTION -->
	    	<xsl:variable name="expression" select="wfm:expression" />
	    	<xsl:variable name="condition" select="@condition" />
			<xsl:choose>
			    <xsl:when test="$condition = 'OTHERWISE'">
					<Condition>
						<xsl:attribute name="Type">OTHERWISE</xsl:attribute>
					</Condition>
			    </xsl:when>
			    <xsl:otherwise>
				    <xsl:if test="$condition = 'CONDITION'">
						<Condition>
							<xsl:attribute name="Type">CONDITION</xsl:attribute>
							<xsl:value-of select="$expression" />
						</Condition>
				    </xsl:if>
			    </xsl:otherwise>
		    </xsl:choose>
			<xsl:call-template name="element-description" />

			<ExtendedAttributes>
				<ExtendedAttribute Name="CarnotExt">
					<carnot:Transition xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
						<xsl:call-template name="element-oid" />
						<xsl:if test="@forkOnTraversal">
							<xsl:attribute name="IsForkingOnTraversal"><xsl:value-of select="@forkOnTraversal" /></xsl:attribute>
						</xsl:if>

						<xsl:call-template name="carnot-attributes" />
					</carnot:Transition>
				</ExtendedAttribute>

				<xsl:copy-of select="wfm:attribute[@name='carnot:model:xpdl:extendedAttributes']/*" />

			</ExtendedAttributes>
		</Transition>
	</xsl:template>

	<xsl:template name="source-transition-ref">
		<xsl:param name="source"/>
		<xsl:for-each select="../wfm:transition[@from=$source]">
			<TransitionRef xmlns="http://www.wfmc.org/2008/XPDL2.1">
				<xsl:attribute name="Id"><xsl:value-of select="@id"/></xsl:attribute>
			</TransitionRef>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="carnot-data-types" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:DataTypes>
			<xsl:for-each select="wfm:dataType">
				<carnot:DataType>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />

					<xsl:if test="@panelClass">
						<xsl:attribute name="PanelClass"><xsl:value-of select="@panelClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@instanceClass">
						<xsl:attribute name="InstanceClass"><xsl:value-of select="@instanceClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@predefined">
						<xsl:attribute name="IsPredefined"><xsl:value-of select="@predefined" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@accessPathEditor">
						<xsl:attribute name="AccssPathEditor"><xsl:value-of select="@accessPathEditor" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@evaluator">
						<xsl:attribute name="Evaluator"><xsl:value-of select="@evaluator" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@storageStrategy">
						<xsl:attribute name="StorageStrategy"><xsl:value-of select="@storageStrategy" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@validatorClass">
						<xsl:attribute name="ValidatorClass"><xsl:value-of select="@validatorClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@valueCreator">
						<xsl:attribute name="ValueCreator"><xsl:value-of select="@valueCreator" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@writable">
						<xsl:attribute name="IsWriteable"><xsl:value-of select="@writable" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@readable">
						<xsl:attribute name="IsReadable"><xsl:value-of select="@readable" /></xsl:attribute>
					</xsl:if>

					<xsl:call-template name="carnot-description" />
					<xsl:call-template name="carnot-attributes" />
				</carnot:DataType>
			</xsl:for-each>
		</carnot:DataTypes>
	</xsl:template>

	<xsl:template name="carnot-application-types" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:ApplicationTypes>
			<xsl:for-each select="wfm:applicationType">
				<carnot:ApplicationType>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />
					<xsl:if test="@panelClass">
						<xsl:attribute name="PanelClass"><xsl:value-of select="@panelClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@instanceClass">
						<xsl:attribute name="InstanceClass"><xsl:value-of select="@instanceClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@predefined">
						<xsl:attribute name="IsPredefined"><xsl:value-of select="@predefined" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@synchronous">
						<xsl:attribute name="IsSynchronous"><xsl:value-of select="@synchronous" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@accessPointProviderClass">
						<xsl:attribute name="AccessPointProviderClass"><xsl:value-of select="@accessPointProviderClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@validatorClass">
						<xsl:attribute name="ValidatorClass"><xsl:value-of select="@validatorClass" /></xsl:attribute>
					</xsl:if>

					<xsl:call-template name="carnot-description" />
					<xsl:call-template name="carnot-attributes" />
				</carnot:ApplicationType>
			</xsl:for-each>
		</carnot:ApplicationTypes>
	</xsl:template>

	<xsl:template name="carnot-application-context-types" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:ApplicationContextTypes>
			<xsl:for-each select="wfm:applicationContextType">
				<carnot:ApplicationContextType>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />
					<xsl:if test="@predefined">
						<xsl:attribute name="IsPredefined"><xsl:value-of select="@predefined" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@accessPointProviderClass">
						<xsl:attribute name="AccessPointProviderClass"><xsl:value-of select="@accessPointProviderClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@panelClass">
						<xsl:attribute name="PanelClass"><xsl:value-of select="@panelClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@validatorClass">
						<xsl:attribute name="ValidatorClass"><xsl:value-of select="@validatorClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@hasApplicationPath">
						<xsl:attribute name="HasApplicationPath"><xsl:value-of select="@hasApplicationPath" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@hasMappingId">
						<xsl:attribute name="HasMappingId"><xsl:value-of select="@hasMappingId" /></xsl:attribute>
					</xsl:if>

					<xsl:call-template name="carnot-description" />
					<xsl:call-template name="carnot-attributes" />
				</carnot:ApplicationContextType>
			</xsl:for-each>
		</carnot:ApplicationContextTypes>
	</xsl:template>

	<xsl:template name="carnot-trigger-types" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:TriggerTypes>
			<xsl:for-each select="wfm:triggerType">
				<carnot:TriggerType>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />
					<xsl:if test="@panelClass">
						<xsl:attribute name="PanelClass"><xsl:value-of select="@panelClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@predefined">
						<xsl:attribute name="IsPredefined"><xsl:value-of select="@predefined" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@pullTrigger">
						<xsl:attribute name="IsPullTrigger"><xsl:value-of select="@pullTrigger" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@pullTriggerEvaluator">
						<xsl:attribute name="PullTriggerEvaluator"><xsl:value-of select="@pullTriggerEvaluator" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@rule">
						<xsl:attribute name="Rule"><xsl:value-of select="@rule" /></xsl:attribute>
					</xsl:if>

					<xsl:call-template name="carnot-description" />
					<xsl:call-template name="carnot-attributes" />
				</carnot:TriggerType>
			</xsl:for-each>
		</carnot:TriggerTypes>
	</xsl:template>

	<xsl:template name="carnot-event-condition-types" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:EventConditionTypes>
			<xsl:for-each select="wfm:eventConditionType">
				<carnot:EventConditionType>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />
					<xsl:if test="@predefined">
						<xsl:attribute name="IsPredefined"><xsl:value-of select="@predefined" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@processCondition">
						<xsl:attribute name="IsProcessCondition"><xsl:value-of select="@processCondition" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@activityCondition">
						<xsl:attribute name="IsActivityCondition"><xsl:value-of select="@activityCondition" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@implementation">
						<xsl:attribute name="Implementation">
							<xsl:choose>
								<xsl:when test="@implementation='engine'">ENGINE</xsl:when>
								<xsl:when test="@implementation='push'">PUSH</xsl:when>
								<xsl:when test="@implementation='pull'">PULL</xsl:when>
							</xsl:choose>
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="@panelClass">
						<xsl:attribute name="PanelClass"><xsl:value-of select="@panelClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@binderClass">
						<xsl:attribute name="BinderClass"><xsl:value-of select="@binderClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@pullEventEmitterClass">
						<xsl:attribute name="PullEventEmitterClass"><xsl:value-of select="@pullEventEmitterClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@rule">
						<xsl:attribute name="Rule"><xsl:value-of select="@rule" /></xsl:attribute>
					</xsl:if>

					<xsl:call-template name="carnot-description" />
					<xsl:call-template name="carnot-attributes" />
				</carnot:EventConditionType>
			</xsl:for-each>
		</carnot:EventConditionTypes>
	</xsl:template>

	<xsl:template name="carnot-event-action-types" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:EventActionTypes>
			<xsl:for-each select="wfm:eventActionType">
				<carnot:EventActionType>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />
					<xsl:if test="@predefined">
						<xsl:attribute name="IsPredefined"><xsl:value-of select="@predefined" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@processAction">
						<xsl:attribute name="IsProcessAction"><xsl:value-of select="@processAction" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@activityAction">
						<xsl:attribute name="IsActivityAction"><xsl:value-of select="@activityAction" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@panelClass">
						<xsl:attribute name="PanelClass"><xsl:value-of select="@panelClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@actionClass">
						<xsl:attribute name="ActionClass"><xsl:value-of select="@actionClass" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@supportedConditionTypes">
						<xsl:attribute name="SupportedConditionTypes"><xsl:value-of select="@supportedConditionTypes" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@unsupportedContexts">
						<xsl:attribute name="UnsupportedContexts"><xsl:value-of select="@unsupportedContexts" /></xsl:attribute>
					</xsl:if>

					<xsl:call-template name="carnot-description" />
					<xsl:call-template name="carnot-attributes" />
				</carnot:EventActionType>
			</xsl:for-each>
		</carnot:EventActionTypes>
	</xsl:template>

	<xsl:template name="carnot-link-types" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:LinkTypes>
			<xsl:for-each select="wfm:linkType">
				<carnot:LinkType>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />

					<xsl:if test="@lineType">
						<xsl:attribute name="LineType"><xsl:value-of select="@lineType" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@lineColor">
						<xsl:attribute name="LineColor"><xsl:value-of select="@lineColor" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@show_linktype_name">
						<xsl:attribute name="HasLinkTypeLabel"><xsl:value-of select="@show_linktype_name" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@show_role_names">
						<xsl:attribute name="HasRoleLabels"><xsl:value-of select="@show_role_names" /></xsl:attribute>
					</xsl:if>

                                       <xsl:call-template name="carnot-description" />
                                       <xsl:call-template name="carnot-attributes" />

					<carnot:LinkSource>
						<xsl:if test="@source_classname">
							<xsl:attribute name="ClassName"><xsl:value-of select="@source_classname" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@source_cardinality">
							<xsl:attribute name="Cardinality"><xsl:value-of select="@source_cardinality" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@source_rolename">
							<xsl:attribute name="Role"><xsl:value-of select="@source_rolename" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@source_symbol">
							<xsl:attribute name="Symbol"><xsl:value-of select="@source_symbol" /></xsl:attribute>
						</xsl:if>
					</carnot:LinkSource>
					<carnot:LinkTarget>
						<xsl:if test="@target_classname">
							<xsl:attribute name="ClassName"><xsl:value-of select="@target_classname" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@target_cardinality">
							<xsl:attribute name="Cardinality"><xsl:value-of select="@target_cardinality" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@target_rolename">
							<xsl:attribute name="Role"><xsl:value-of select="@target_rolename" /></xsl:attribute>
						</xsl:if>
						<xsl:if test="@target_symbol">
							<xsl:attribute name="Symbol"><xsl:value-of select="@target_symbol" /></xsl:attribute>
						</xsl:if>
					</carnot:LinkTarget>

				</carnot:LinkType>
			</xsl:for-each>
		</carnot:LinkTypes>
	</xsl:template>

	<xsl:template name="carnot-modelers" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Modelers>
			<xsl:for-each select="wfm:modeler">
				<carnot:Modeler>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />

					<xsl:if test="@password">
						<xsl:attribute name="Password"><xsl:value-of select="@password" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@email">
						<xsl:attribute name="EMail"><xsl:value-of select="@email" /></xsl:attribute>
					</xsl:if>

					<xsl:call-template name="carnot-description" />
					<xsl:call-template name="carnot-attributes" />
				</carnot:Modeler>
			</xsl:for-each>
		</carnot:Modelers>
	</xsl:template>

    <xsl:template name="element-oid">
        <xsl:if test="@oid">
            <xsl:attribute name="Oid"><xsl:value-of select="@oid" /></xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template name="element-proxy">
        <xsl:if test="@proxy">
			<xsl:call-template name="external-reference"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="external-reference">
		<xsl:param name="ref" select="@proxy"/>
       	<ExternalReference xmlns="http://www.wfmc.org/2008/XPDL2.1">
		    <xsl:variable name="qname" select="substring-after($ref, '{')" />

			<xsl:attribute name="location"><xsl:value-of select="substring-before($qname, '}')" /></xsl:attribute>
			<xsl:attribute name="xref"><xsl:value-of select="substring-after($qname, '}')" /></xsl:attribute>
			<xsl:attribute name="namespace"><xsl:value-of select="substring-before($ref, ':')" /></xsl:attribute>
		</ExternalReference>
    </xsl:template>

	<xsl:template name="element-id-and-name">
        <xsl:if test="@id">
			<xsl:attribute name="Id"><xsl:value-of select="@id" /></xsl:attribute>
        </xsl:if>
        <xsl:if test="@name">
		    <xsl:attribute name="Name"><xsl:value-of select="@name" /></xsl:attribute>
        </xsl:if>
	</xsl:template>

	<xsl:template name="element-description">
		<xsl:if test="wfm:description">
			<Description xmlns="http://www.wfmc.org/2008/XPDL2.1"><xsl:value-of select="wfm:description" /></Description>
		</xsl:if>
	</xsl:template>

	<xsl:template name="data-type">
        <xsl:if test="@type or @proxy">
		<DataType xmlns="http://www.wfmc.org/2008/XPDL2.1">
			<xsl:choose>
				<xsl:when test="@proxy">
					<xsl:call-template name="element-proxy"/>
				</xsl:when>
				<xsl:when test="@type='primitive'">
						<xsl:choose>
						<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='Money'">
							<ExternalReference>
								<xsl:attribute name="location">org.eclipse.stardust.common.Money</xsl:attribute>
							</ExternalReference>
						</xsl:when>
						<xsl:otherwise>
					        <BasicType>
								<xsl:attribute name="Type">
									<xsl:choose>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='boolean'">BOOLEAN</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='char'">INTEGER</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='byte'">INTEGER</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='short'">INTEGER</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='int'">INTEGER</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='long'">INTEGER</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='float'">FLOAT</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='double'">FLOAT</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='String'">STRING</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='Calendar'">DATETIME</xsl:when>
										<xsl:when test="wfm:attribute[@name='carnot:engine:type']/@value='Timestamp'">DATETIME</xsl:when>
									</xsl:choose>
								</xsl:attribute>
					        </BasicType>
						</xsl:otherwise>
					</xsl:choose>
					<!-- Length not needed as Carnot does not support arrays currently -->
				</xsl:when>

				<xsl:when test="@type='serializable'">
					<ExternalReference>
						<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:className']/@value" /></xsl:attribute>
					</ExternalReference>
				</xsl:when>

				<xsl:when test="@type='entity'">
					<ExternalReference>
						<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:remoteInterface']/@value" /></xsl:attribute>
					</ExternalReference>
				</xsl:when>

				<xsl:when test="@type='plainXML'">
				    <xsl:choose>
				        <xsl:when test="../@type='webservice'">
							<!-- Web Service parameter -->
							<ExternalReference>
								<xsl:attribute name="location"><xsl:value-of select="../wfm:attribute[@name='carnot:engine:wsdlUrl']/@value" /></xsl:attribute>
								<xsl:attribute name="xref"><xsl:value-of select="@id" /></xsl:attribute>
							</ExternalReference>
				        </xsl:when>
				        <xsl:when test="wfm:attribute[@name='carnot:engine:schemaType']/@value='xsd'">
							<ExternalReference>
								<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:schemaURL']/@value" /></xsl:attribute>

								<xsl:variable name="elementName" select="carnot-xpdl-utils:getQnameLocalPart(string(wfm:attribute[@name='carnot:engine:typeId']/@value))" xmlns:carnot-xpdl-utils="xalan://org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils" />
								<xsl:variable name="elementNs" select="carnot-xpdl-utils:getQnameNsUri(string(wfm:attribute[@name='carnot:engine:typeId']/@value))" xmlns:carnot-xpdl-utils="xalan://org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils" />

								<xsl:if test="$elementName">
									<xsl:attribute name="xref"><xsl:value-of select="$elementName" /></xsl:attribute>
								</xsl:if>
								<xsl:if test="$elementNs">
									<xsl:attribute name="namespace"><xsl:value-of select="$elementNs" /></xsl:attribute>
								</xsl:if>
							</ExternalReference>
				        </xsl:when>
				        <xsl:otherwise>
							<SchemaType />
				        </xsl:otherwise>
				    </xsl:choose>
				</xsl:when>

				<xsl:when test="@type='struct'">
					<xsl:choose>
						<xsl:when test="xpdl:ExternalReference">
							<xsl:copy-of select="xpdl:ExternalReference"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:variable name="dataType" select="wfm:attribute[@name='carnot:engine:dataType']/@value"/>
							<xsl:choose>
						        <xsl:when test="substring-before($dataType, ':{')">
									<xsl:call-template name="external-reference">
										<xsl:with-param name="ref" select="$dataType"/>
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<DeclaredType>
										<xsl:attribute name="Id"><xsl:value-of select="$dataType"/></xsl:attribute>
									</DeclaredType>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>

				<xsl:when test="@type='dmsDocument'">
					<xsl:choose>
						<xsl:when test="xpdl:ExternalReference">
							<xsl:copy-of select="xpdl:ExternalReference"/>
						</xsl:when>
						<xsl:otherwise>
							<ExternalReference>
								<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:className']/@value" /></xsl:attribute>
							</ExternalReference>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>

				<xsl:when test="@type='dmsDocumentList'">
					<xsl:choose>
						<xsl:when test="xpdl:ExternalReference">
							<xsl:copy-of select="xpdl:ExternalReference"/>
						</xsl:when>
						<xsl:otherwise>
							<ExternalReference>
								<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:className']/@value" /></xsl:attribute>
							</ExternalReference>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>

				<xsl:otherwise>
					<ExternalReference>
						<xsl:choose>
							<xsl:when test="wfm:attribute[@name='carnot:engine:className']/@value">
								<xsl:attribute name="location"><xsl:value-of select="wfm:attribute[@name='carnot:engine:className']/@value" /></xsl:attribute>
							</xsl:when>

							<xsl:otherwise>
								<xsl:attribute name="location">java.lang.Object</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
					</ExternalReference>
				</xsl:otherwise>

				<!-- TODO missing types -->

			</xsl:choose>
		</DataType>
        </xsl:if>
	</xsl:template>

	<xsl:template name="formal-parameters">
		<FormalParameters xmlns="http://www.wfmc.org/2008/XPDL2.1">
			<xsl:for-each select="wfm:accessPoint">
				<xsl:sort select="@direction" data-type="text" order="ascending" />

				<FormalParameter>
					<xsl:attribute name="Id"><xsl:value-of select="@id"/></xsl:attribute>
					<xsl:attribute name="Index"><xsl:value-of select="position()" /></xsl:attribute>
					<xsl:attribute name="Mode"><xsl:value-of select="@direction"/></xsl:attribute>
					<xsl:attribute name="Name"><xsl:value-of select="@name"/></xsl:attribute>

					<xsl:call-template name="data-type" />
				</FormalParameter>
			</xsl:for-each>
		</FormalParameters>
	</xsl:template>

	<xsl:template name="carnot-description">
		<xsl:if test="wfm:description">
			<carnot:Description xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
				<xsl:value-of select="wfm:description" />
			</carnot:Description>
		</xsl:if>
	</xsl:template>

	<xsl:template name="carnot-attributes">
		<xsl:if test="wfm:attribute">
			<carnot:Attributes xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
				<xsl:for-each select="wfm:attribute[@name != 'carnot:model:xpdl:extendedAttributes']">
				    <xsl:call-template name="carnot-attribute" />
				</xsl:for-each>
			</carnot:Attributes>
		</xsl:if>
	</xsl:template>

	<xsl:template name="carnot-attribute">
		<carnot:Attribute xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
			<xsl:attribute name="Name"><xsl:value-of select="@name" /></xsl:attribute>
			<xsl:if test="@value">
				<xsl:attribute name="Value"><xsl:value-of select="@value" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="@type">
				<xsl:attribute name="Type"><xsl:value-of select="@type" /></xsl:attribute>
			</xsl:if>

			<xsl:if test="not(@value)">
				<carnot:Value><xsl:copy-of select="*" /></carnot:Value>
			</xsl:if>
		</carnot:Attribute>
	</xsl:template>

	<xsl:template name="carnot-access-points">
		<carnot:AccessPoints xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
			<xsl:for-each select="wfm:accessPoint">
				<carnot:AccessPoint>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />

					<xsl:if test="@direction">
						<xsl:attribute name="Direction"><xsl:value-of select="@direction" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@type">
						<xsl:attribute name="Type"><xsl:value-of select="@type" /></xsl:attribute>
					</xsl:if>

                    <xsl:call-template name="carnot-description" />
					<xsl:call-template name="carnot-attributes" />
				</carnot:AccessPoint>
			</xsl:for-each>
		</carnot:AccessPoints>
	</xsl:template>

	<xsl:template name="carnot-event-handlers">
		<carnot:EventHandlers xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
			<xsl:for-each select="wfm:eventHandler">
				<carnot:EventHandler>
					<xsl:call-template name="element-oid" />
					<xsl:call-template name="element-id-and-name" />

					<xsl:attribute name="Type"><xsl:value-of select="@type" /></xsl:attribute>
					<xsl:if test="@autoBind">
						<xsl:attribute name="IsAutomaticallyBound"><xsl:value-of select="@autoBind" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@consumeOnMatch">
						<xsl:attribute name="IsConsumedOnMatch"><xsl:value-of select="@consumeOnMatch" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@unbindOnMatch">
						<xsl:attribute name="IsUnboundOnMatch"><xsl:value-of select="@unbindOnMatch" /></xsl:attribute>
					</xsl:if>
					<xsl:if test="@logHandler">
						<xsl:attribute name="IsLogged"><xsl:value-of select="@logHandler" /></xsl:attribute>
					</xsl:if>

					<xsl:call-template name="carnot-description" />

					<carnot:EventActions>
						<xsl:for-each select="wfm:bindAction">
							<xsl:call-template name="carnot-event-action">
								<xsl:with-param name="kind">BIND</xsl:with-param>
							</xsl:call-template>
						</xsl:for-each>
						<xsl:for-each select="wfm:eventAction">
							<xsl:call-template name="carnot-event-action">
								<xsl:with-param name="kind">EVENT</xsl:with-param>
							</xsl:call-template>
						</xsl:for-each>
						<xsl:for-each select="wfm:unbindAction">
							<xsl:call-template name="carnot-event-action">
								<xsl:with-param name="kind">UNBIND</xsl:with-param>
							</xsl:call-template>
						</xsl:for-each>
					</carnot:EventActions>

					<xsl:call-template name="carnot-attributes" />
				</carnot:EventHandler>
			</xsl:for-each>
		</carnot:EventHandlers>
	</xsl:template>

	<xsl:template name="carnot-event-action" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<xsl:param name="kind" />

		<carnot:EventAction>
			<xsl:call-template name="element-oid" />
			<xsl:call-template name="element-id-and-name" />

			<xsl:attribute name="Type"><xsl:value-of select="@type" /></xsl:attribute>
			<xsl:attribute name="Kind"><xsl:value-of select="$kind" /></xsl:attribute>

			<xsl:call-template name="carnot-description" />

			<xsl:call-template name="carnot-attributes" />
		</carnot:EventAction>
	</xsl:template>

	<xsl:template name="carnot-diagrams" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Diagrams>
			<xsl:for-each select="wfm:diagram">
				<carnot:Diagram>
					<xsl:call-template name="element-oid" />
					<xsl:attribute name="Name"><xsl:value-of select="@name" /></xsl:attribute>
		            <xsl:if test="@orientation">
		                <xsl:attribute name="Orientation"><xsl:value-of select="@orientation" /></xsl:attribute>
		            </xsl:if>
		            <xsl:if test="@mode">
		                <xsl:attribute name="Mode"><xsl:value-of select="@mode" /></xsl:attribute>
		            </xsl:if>
					<xsl:call-template name="carnot-attributes" />
					<xsl:call-template name="carnot-symbols-and-connections" />
				</carnot:Diagram>
			</xsl:for-each>
		</carnot:Diagrams>
	</xsl:template>

	<xsl:template name="carnot-views" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Views>
			<xsl:for-each select="wfm:view">
				<carnot:View>
					<xsl:call-template name="element-oid" />
					<xsl:attribute name="Name"><xsl:value-of select="@name" /></xsl:attribute>

					<xsl:call-template name="carnot-description" />

					<carnot:Viewables>
						<xsl:for-each select="wfm:viewable">
							<carnot:Viewable>
								<xsl:attribute name="ModelElement"><xsl:value-of select="@viewable" /></xsl:attribute>
							</carnot:Viewable>
						</xsl:for-each>
					</carnot:Viewables>

					<xsl:call-template name="carnot-views" />

					<xsl:call-template name="carnot-attributes" />
				</carnot:View>
			</xsl:for-each>
		</carnot:Views>
	</xsl:template>

	<xsl:template name="carnot-symbols-and-connections" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Symbols>
			<xsl:apply-templates select="wfm:*[contains(local-name(), 'Symbol')]" />
		</carnot:Symbols>
		<carnot:Connections>
			<xsl:apply-templates select="wfm:*[contains(local-name(), 'Connection')]" />
		</carnot:Connections>
	</xsl:template>

	<xsl:template match="wfm:activitySymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">ACTIVITY</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:annotationSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">ANNOTATION</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:applicationSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">APPLICATION</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:conditionalPerformerSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">CONDITIONAL_PERFORMER</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:dataSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">DATA</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:groupSymbol" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Symbol Kind="GROUP">
			<xsl:call-template name="element-oid" />
            <xsl:call-template name="common-node-symbol-attributes" />

			<xsl:call-template name="carnot-symbols-and-connections" />
		</carnot:Symbol>
	</xsl:template>

	<xsl:template match="wfm:modelerSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">MODELER</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:organizationSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">ORGANIZATION</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:processSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">PROCESS</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:roleSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">ROLE</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="wfm:textSymbol">
		<xsl:call-template name="carnot-symbol">
			<xsl:with-param name="kind">TEXT</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

    <xsl:template match="wfm:poolSymbol" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
        <carnot:Symbol Kind="POOL">
            <xsl:call-template name="element-oid" />
            <xsl:call-template name="element-id-and-name" />

            <xsl:call-template name="common-swimlane-symbol-attributes" />

            <xsl:if test="@boundaryVisible">
                <xsl:attribute name="BoundaryVisible"><xsl:value-of select="@boundaryVisible" /></xsl:attribute>
            </xsl:if>
            <xsl:if test="@process">
                <xsl:attribute name="Process"><xsl:value-of select="@process" /></xsl:attribute>
            </xsl:if>

            <xsl:call-template name="carnot-symbols-and-connections" />
			<xsl:call-template name="carnot-attributes" />
        </carnot:Symbol>
    </xsl:template>

    <xsl:template match="wfm:laneSymbol" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
        <carnot:Symbol Kind="LANE">
            <xsl:call-template name="element-oid" />
            <xsl:call-template name="element-id-and-name" />

            <xsl:call-template name="common-swimlane-symbol-attributes" />

            <xsl:if test="@parentLane">
                <xsl:attribute name="ParentLane"><xsl:value-of select="@parentLane" /></xsl:attribute>
            </xsl:if>

            <xsl:call-template name="carnot-symbols-and-connections" />
			<xsl:call-template name="carnot-attributes" />
        </carnot:Symbol>
    </xsl:template>

       <xsl:template match="wfm:endEventSymbol">
               <xsl:call-template name="carnot-symbol">
                       <xsl:with-param name="kind">ENDEVENT</xsl:with-param>
               </xsl:call-template>
       </xsl:template>

       <xsl:template match="wfm:gatewaySymbol">
               <xsl:call-template name="carnot-symbol">
                       <xsl:with-param name="kind">GATEWAY</xsl:with-param>
               </xsl:call-template>
       </xsl:template>

       <xsl:template match="wfm:intermediateEventSymbol">
               <xsl:call-template name="carnot-symbol">
                       <xsl:with-param name="kind">INTERMEDIATEEVENT</xsl:with-param>
               </xsl:call-template>
       </xsl:template>

       <xsl:template match="wfm:startEventSymbol">
               <xsl:call-template name="carnot-symbol">
                       <xsl:with-param name="kind">STARTEVENT</xsl:with-param>
               </xsl:call-template>
       </xsl:template>

       <xsl:template match="wfm:publicInterfaceSymbol">
               <xsl:call-template name="carnot-symbol">
                       <xsl:with-param name="kind">PUBLICINTERFACE</xsl:with-param>
               </xsl:call-template>
       </xsl:template>

	<xsl:template name="carnot-symbol" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<xsl:param name="kind" />

		<carnot:Symbol>
			<xsl:attribute name="Kind"><xsl:value-of select="$kind" /></xsl:attribute>

			<xsl:call-template name="element-oid" />

            <xsl:call-template name="common-node-symbol-attributes" />

			<xsl:if test="@refer">
				<xsl:attribute name="ModelElement"><xsl:value-of select="@refer" /></xsl:attribute>
			</xsl:if>

            <xsl:if test="@flowKind">
                <xsl:attribute name="FlowKind"><xsl:value-of select="@flowKind" /></xsl:attribute>
            </xsl:if>

			<xsl:if test="@text">
				<carnot:Text><xsl:value-of select="@text" /></carnot:Text>
			</xsl:if>

			<xsl:if test="wfm:text">
				<carnot:Text>
					<xsl:value-of select="wfm:text" />
				</carnot:Text>
			</xsl:if>

			<xsl:if test="@label">
				<xsl:attribute name="Label"><xsl:value-of select="@label" /></xsl:attribute>
			</xsl:if>

        </carnot:Symbol>
	</xsl:template>

	<xsl:template match="wfm:dataMappingConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="DATA_FLOW">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@dataSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@activitySymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:executedByConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="EXECUTED_BY">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@applicationSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@activitySymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:genericLinkConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="GENERIC_LINK">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="LinkType"><xsl:value-of select="@linkType" /></xsl:attribute>
			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@sourceSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@targetSymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:partOfConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="PART_OF">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@suborganizationSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@organizationSymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:performsConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="PERFORMS">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@participantSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@activitySymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:triggersConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="TRIGGERS">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@participantSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@startEventSymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:refersToConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="REFERS_TO">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@from" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@to" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:subProcessOfConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="SUBPROCESS_OF">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@subprocessSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@processSymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:teamLeadConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="TEAM_LEAD">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@teamLeadSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@teamSymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:transitionConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="TRANSITION">
			<xsl:call-template name="element-oid" />

            <xsl:if test="@transition">
                <xsl:attribute name="ModelElement"><xsl:value-of select="@transition" /></xsl:attribute>
            </xsl:if>
			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@sourceActivitySymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@targetActivitySymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />

			<xsl:if test="@points">
				<carnot:Points><xsl:value-of select="@points" /></carnot:Points>
			</xsl:if>
		</carnot:Connection>
	</xsl:template>

	<xsl:template match="wfm:worksForConnection" xmlns="http://www.wfmc.org/2008/XPDL2.1" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
		<carnot:Connection Kind="WORKS_FOR">
			<xsl:call-template name="element-oid" />

			<xsl:attribute name="SourceSymbol"><xsl:value-of select="@participantSymbol" /></xsl:attribute>
			<xsl:attribute name="TargetSymbol"><xsl:value-of select="@organizationSymbol" /></xsl:attribute>

            <xsl:call-template name="carnot-connection-attributes" />
		</carnot:Connection>
	</xsl:template>

    <xsl:template name="carnot-connection-attributes">
        <xsl:call-template name="color-and-style" />

        <xsl:if test="@sourceAnchor">
            <xsl:attribute name="SourceAnchor"><xsl:value-of select="@sourceAnchor" /></xsl:attribute>
        </xsl:if>
        <xsl:if test="@targetAnchor">
            <xsl:attribute name="TargetAnchor"><xsl:value-of select="@targetAnchor" /></xsl:attribute>
        </xsl:if>
        <xsl:if test="@routing">
            <xsl:attribute name="Routing"><xsl:value-of select="@routing" /></xsl:attribute>
        </xsl:if>

        <xsl:call-template name="carnot-coordinates" />

    </xsl:template>

    <xsl:template name="carnot-coordinates" xmlns:carnot="http://www.carnot.ag/xpdl/3.1">
        <xsl:for-each select="wfm:coordinate">
            <carnot:Coordinate>
                <xsl:attribute name="XPos"><xsl:value-of select="@x" /></xsl:attribute>
                <xsl:attribute name="YPos"><xsl:value-of select="@y" /></xsl:attribute>
            </carnot:Coordinate>
        </xsl:for-each>
    </xsl:template>

	<xsl:template name="common-node-symbol-attributes">
		<xsl:call-template name="color-and-style" />

		<xsl:attribute name="X"><xsl:value-of select="@x" /></xsl:attribute>
		<xsl:attribute name="Y"><xsl:value-of select="@y" /></xsl:attribute>

		<xsl:if test="@width">
			<xsl:attribute name="Width"><xsl:value-of select="@width" /></xsl:attribute>
		</xsl:if>
		<xsl:if test="@height">
			<xsl:attribute name="Height"><xsl:value-of select="@height" /></xsl:attribute>
		</xsl:if>
		<xsl:if test="@shape">
			<xsl:attribute name="Shape"><xsl:value-of select="@shape" /></xsl:attribute>
		</xsl:if>
	</xsl:template>

	<xsl:template name="common-swimlane-symbol-attributes">
		<xsl:call-template name="common-node-symbol-attributes" />

		<xsl:if test="@orientation">
			<xsl:attribute name="Orientation"><xsl:value-of select="@orientation" /></xsl:attribute>
		</xsl:if>
		<xsl:if test="@participant">
			<xsl:attribute name="Participant"><xsl:value-of select="@participant" /></xsl:attribute>
		</xsl:if>
		<xsl:if test="@participantReference">
			<xsl:attribute name="ParticipantReference"><xsl:value-of select="@participantReference" /></xsl:attribute>
		</xsl:if>
		<xsl:if test="@collapsed">
			<xsl:attribute name="Collapsed"><xsl:value-of select="@collapsed" /></xsl:attribute>
		</xsl:if>
	</xsl:template>

    <xsl:template name="color-and-style">
        <xsl:if test="@borderColor">
            <xsl:attribute name="BorderColor"><xsl:value-of select="@borderColor" /></xsl:attribute>
        </xsl:if>
        <xsl:if test="@fillColor">
            <xsl:attribute name="FillColor"><xsl:value-of select="@fillColor" /></xsl:attribute>
        </xsl:if>
        <xsl:if test="@style">
            <xsl:attribute name="Style"><xsl:value-of select="@style" /></xsl:attribute>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
