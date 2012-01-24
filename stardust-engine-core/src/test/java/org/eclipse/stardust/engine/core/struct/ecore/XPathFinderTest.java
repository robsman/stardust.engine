/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.struct.ecore;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.stardust.common.utils.xml.XmlUtils;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.w3c.dom.*;
import org.xml.sax.InputSource;


public class XPathFinderTest extends TestCase
{

   protected void setUp() throws Exception
   {
      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xsd",
            new XSDResourceFactoryImpl());
   }

   //
   // public void testFindMT505() throws Exception
   // {
   // String name = "root";
   //
   // ResourceSet resourceSet = new ResourceSetImpl();
   //
   // HashMap options = new HashMap();
   // options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
   //
   // Resource resource =
   // resourceSet.createResource(URI.createURI(XPathFinderTest.class.getResource("MT515.xsd").toString()));
   // resource.load(options);
   //
   // List l = resource.getContents();
   // XSDSchema xsdSchema = (XSDSchema) l.get(0);
   //
   // Set allXPaths_Old = StructuredDataXPathUtils.findAllXPaths(new
   // InputSource(XPathFinderTest.class.getResource("MT515_TD.xsd").openStream()), name);
   //
   // Set allXPaths_New = XPathFinder.findAllXPaths(xsdSchema, name);
   //
   // // System.out.println(allXPaths_New);
   // // System.out.println(allXPaths_Old);
   //
   // assertEquals(allXPaths_Old, allXPaths_New);
   // }

   public void testAllKnownXSDs() throws Exception
   {
      findXPathsFor("MT515.xsd", null);
      findXPathsFor("MT103.xsd", null);
      findXPathsFor("swift.cashrepv1$camt.005.001.03.xsd", null);
      findXPathsFor("pain.001.001.02.xsd", null);
      findXPathsFor("EFET-CoreCmpts-V3R2.xsd", null);
      findXPathsFor("EFET-CoreElements-V3R2.xsd", null);
      findXPathsFor("EFET-ACK-V3R2.xsd", null);
      findXPathsFor("EFET-BCN-V3R2.xsd", null);
      findXPathsFor("EFET-BFI-V3R2.xsd", null);
      findXPathsFor("EFET-BMN-V3R2.xsd", null);
      findXPathsFor("EFET-CAN-V3R2.xsd", null);
      findXPathsFor("EFET-CNF-V3R2.xsd", null);
      findXPathsFor("EFET-MSA-V3R2.xsd", null);
      findXPathsFor("EFET-MSR-V3R2.xsd", null);
      findXPathsFor("EFET-MSU-V3R2.xsd", null);
      findXPathsFor("EFET-REJ-V3R2.xsd", null);

      findXPathsFor("acmt.001.001.02.xsd", null);
      findXPathsFor("acmt.002.001.02.xsd", null);
      findXPathsFor("acmt.003.001.02.xsd", null);
      findXPathsFor("acmt.004.001.02.xsd", null);
      findXPathsFor("acmt.005.001.02.xsd", null);
      findXPathsFor("acmt.006.001.02.xsd", null);
      findXPathsFor("camt.040.001.03.xsd", null);
      findXPathsFor("camt.041.001.03.xsd", null);
      findXPathsFor("camt.042.001.03.xsd", null);
      findXPathsFor("camt.043.001.03.xsd", null);
      findXPathsFor("camt.044.001.02.xsd", null);
      findXPathsFor("camt.045.001.02.xsd", null);
      findXPathsFor("ctm.xsd", null);
      findXPathsFor("ftt-ctminstruction-schema.xsd", null);
      findXPathsFor("ftt-oasys-schema.xsd", null);
      findXPathsFor("ftt-type-schema.xsd", null);
      findXPathsFor("MT100.FormatModel.xsd", null);
      findXPathsFor("MT101.FormatModel.xsd", null);
      findXPathsFor("MT102.FormatModel.xsd", null);
      findXPathsFor("MT103.FormatModel.xsd", null);
      findXPathsFor("MT104.FormatModel.xsd", null);
      findXPathsFor("MT105.FormatModel.xsd", null);
      findXPathsFor("MT106.FormatModel.xsd", null);
      findXPathsFor("MT107.FormatModel.xsd", null);
      findXPathsFor("MT110.FormatModel.xsd", null);
      findXPathsFor("MT111.FormatModel.xsd", null);
      findXPathsFor("MT112.FormatModel.xsd", null);
      findXPathsFor("MT190.FormatModel.xsd", null);
      findXPathsFor("MT191.FormatModel.xsd", null);
      findXPathsFor("MT192.FormatModel.xsd", null);
      findXPathsFor("MT193.FormatModel.xsd", null);
      findXPathsFor("MT195.FormatModel.xsd", null);
      findXPathsFor("MT196.FormatModel.xsd", null);
      findXPathsFor("MT198.FormatModel.xsd", null);
      findXPathsFor("MT199.FormatModel.xsd", null);
      findXPathsFor("MT200.FormatModel.xsd", null);
      findXPathsFor("MT201.FormatModel.xsd", null);
      findXPathsFor("MT202.FormatModel.xsd", null);
      findXPathsFor("MT203.FormatModel.xsd", null);
      findXPathsFor("MT204.FormatModel.xsd", null);
      findXPathsFor("MT205.FormatModel.xsd", null);
      findXPathsFor("MT206.FormatModel.xsd", null);
      findXPathsFor("MT207.FormatModel.xsd", null);
      findXPathsFor("MT210.FormatModel.xsd", null);
      findXPathsFor("MT256.FormatModel.xsd", null);
      findXPathsFor("MT290.FormatModel.xsd", null);
      findXPathsFor("MT291.FormatModel.xsd", null);
      findXPathsFor("MT292.FormatModel.xsd", null);
      findXPathsFor("MT293.FormatModel.xsd", null);
      findXPathsFor("MT295.FormatModel.xsd", null);
      findXPathsFor("MT296.FormatModel.xsd", null);
      findXPathsFor("MT298.FormatModel.xsd", null);
      findXPathsFor("MT299.FormatModel.xsd", null);
      findXPathsFor("MT300.FormatModel.xsd", null);
      findXPathsFor("MT303.FormatModel.xsd", null);
      findXPathsFor("MT304.FormatModel.xsd", null);
      findXPathsFor("MT305.FormatModel.xsd", null);
      findXPathsFor("MT306.FormatModel.xsd", null);
      findXPathsFor("MT307.FormatModel.xsd", null);
      findXPathsFor("MT308.FormatModel.xsd", null);
      findXPathsFor("MT320.FormatModel.xsd", null);
      findXPathsFor("MT321.FormatModel.xsd", null);
      findXPathsFor("MT330.FormatModel.xsd", null);
      findXPathsFor("MT335.FormatModel.xsd", null);
      findXPathsFor("MT340.FormatModel.xsd", null);
      findXPathsFor("MT341.FormatModel.xsd", null);
      findXPathsFor("MT350.FormatModel.xsd", null);
      findXPathsFor("MT360.FormatModel.xsd", null);
      findXPathsFor("MT361.FormatModel.xsd", null);
      findXPathsFor("MT362.FormatModel.xsd", null);
      findXPathsFor("MT364.FormatModel.xsd", null);
      findXPathsFor("MT365.FormatModel.xsd", null);
      findXPathsFor("MT380.FormatModel.xsd", null);
      findXPathsFor("MT381.FormatModel.xsd", null);
      findXPathsFor("MT390.FormatModel.xsd", null);
      findXPathsFor("MT391.FormatModel.xsd", null);
      findXPathsFor("MT392.FormatModel.xsd", null);
      findXPathsFor("MT393.FormatModel.xsd", null);
      findXPathsFor("MT395.FormatModel.xsd", null);
      findXPathsFor("MT396.FormatModel.xsd", null);
      findXPathsFor("MT398.FormatModel.xsd", null);
      findXPathsFor("MT399.FormatModel.xsd", null);
      findXPathsFor("MT400.FormatModel.xsd", null);
      findXPathsFor("MT405.FormatModel.xsd", null);
      findXPathsFor("MT410.FormatModel.xsd", null);
      findXPathsFor("MT412.FormatModel.xsd", null);
      findXPathsFor("MT416.FormatModel.xsd", null);
      findXPathsFor("MT420.FormatModel.xsd", null);
      findXPathsFor("MT422.FormatModel.xsd", null);
      findXPathsFor("MT430.FormatModel.xsd", null);
      findXPathsFor("MT450.FormatModel.xsd", null);
      findXPathsFor("MT455.FormatModel.xsd", null);
      findXPathsFor("MT456.FormatModel.xsd", null);
      findXPathsFor("MT490.FormatModel.xsd", null);
      findXPathsFor("MT491.FormatModel.xsd", null);
      findXPathsFor("MT492.FormatModel.xsd", null);
      findXPathsFor("MT493.FormatModel.xsd", null);
      findXPathsFor("MT495.FormatModel.xsd", null);
      findXPathsFor("MT496.FormatModel.xsd", null);
      findXPathsFor("MT498.FormatModel.xsd", null);
      findXPathsFor("MT499.FormatModel.xsd", null);
      findXPathsFor("MT500.FormatModel.xsd", null);
      findXPathsFor("MT501.FormatModel.xsd", null);
      findXPathsFor("MT502.FormatModel.xsd", null);
      findXPathsFor("MT502F.FormatModel.xsd", null);
      findXPathsFor("MT503.FormatModel.xsd", null);
      findXPathsFor("MT504.FormatModel.xsd", null);
      findXPathsFor("MT505.FormatModel.xsd", null);
      findXPathsFor("MT506.FormatModel.xsd", null);
      findXPathsFor("MT507.FormatModel.xsd", null);
      findXPathsFor("MT508.FormatModel.xsd", null);
      findXPathsFor("MT509.FormatModel.xsd", null);
      findXPathsFor("MT509F.FormatModel.xsd", null);
      findXPathsFor("MT510.FormatModel.xsd", null);
      findXPathsFor("MT512.FormatModel.xsd", null);
      findXPathsFor("MT513.FormatModel.xsd", null);
      findXPathsFor("MT514.FormatModel.xsd", null);
      findXPathsFor("MT515.FormatModel.xsd", null);
      findXPathsFor("MT515F.FormatModel.xsd", null);
      findXPathsFor("MT516.FormatModel.xsd", null);
      findXPathsFor("MT517.FormatModel.xsd", null);
      findXPathsFor("MT518.FormatModel.xsd", null);
      findXPathsFor("MT519.FormatModel.xsd", null);
      findXPathsFor("MT520.FormatModel.xsd", null);
      findXPathsFor("MT521.FormatModel.xsd", null);
      findXPathsFor("MT521_ISITC.FormatModel.xsd", null);
      findXPathsFor("MT522.FormatModel.xsd", null);
      findXPathsFor("MT523.FormatModel.xsd", null);
      findXPathsFor("MT523_ISITC.FormatModel.xsd", null);
      findXPathsFor("MT524.FormatModel.xsd", null);
      findXPathsFor("MT526.FormatModel.xsd", null);
      findXPathsFor("MT527.FormatModel.xsd", null);
      findXPathsFor("MT528.FormatModel.xsd", null);
      findXPathsFor("MT529.FormatModel.xsd", null);
      findXPathsFor("MT530.FormatModel.xsd", null);
      findXPathsFor("MT531.FormatModel.xsd", null);
      findXPathsFor("MT532.FormatModel.xsd", null);
      findXPathsFor("MT533.FormatModel.xsd", null);
      findXPathsFor("MT534.FormatModel.xsd", null);
      findXPathsFor("MT535.FormatModel.xsd", null);
      findXPathsFor("MT535F.FormatModel.xsd", null);
      findXPathsFor("MT536.FormatModel.xsd", null);
      findXPathsFor("MT537.FormatModel.xsd", null);
      findXPathsFor("MT538.FormatModel.xsd", null);
      findXPathsFor("MT539.FormatModel.xsd", null);
      findXPathsFor("MT540.FormatModel.xsd", null);
      findXPathsFor("MT541.FormatModel.xsd", null);
      findXPathsFor("MT542.FormatModel.xsd", null);
      findXPathsFor("MT543.FormatModel.xsd", null);
      findXPathsFor("MT544.FormatModel.xsd", null);
      findXPathsFor("MT545.FormatModel.xsd", null);
      findXPathsFor("MT546.FormatModel.xsd", null);
      findXPathsFor("MT547.FormatModel.xsd", null);
      findXPathsFor("MT548.FormatModel.xsd", null);
      findXPathsFor("MT549.FormatModel.xsd", null);
      findXPathsFor("MT550.FormatModel.xsd", null);
      findXPathsFor("MT551.FormatModel.xsd", null);
      findXPathsFor("MT552.FormatModel.xsd", null);
      findXPathsFor("MT553.FormatModel.xsd", null);
      findXPathsFor("MT554.FormatModel.xsd", null);
      findXPathsFor("MT555.FormatModel.xsd", null);
      findXPathsFor("MT556.FormatModel.xsd", null);
      findXPathsFor("MT557.FormatModel.xsd", null);
      findXPathsFor("MT558.FormatModel.xsd", null);
      findXPathsFor("MT559.FormatModel.xsd", null);
      findXPathsFor("MT560.FormatModel.xsd", null);
      findXPathsFor("MT561.FormatModel.xsd", null);
      findXPathsFor("MT562.FormatModel.xsd", null);
      findXPathsFor("MT563.FormatModel.xsd", null);
      findXPathsFor("MT564.FormatModel.xsd", null);
      findXPathsFor("MT565.FormatModel.xsd", null);
      findXPathsFor("MT566.FormatModel.xsd", null);
      findXPathsFor("MT567.FormatModel.xsd", null);
      findXPathsFor("MT568.FormatModel.xsd", null);
      findXPathsFor("MT569.FormatModel.xsd", null);
      findXPathsFor("MT570.FormatModel.xsd", null);
      findXPathsFor("MT571.FormatModel.xsd", null);
      findXPathsFor("MT572.FormatModel.xsd", null);
      findXPathsFor("MT573.FormatModel.xsd", null);
      findXPathsFor("MT574B.FormatModel.xsd", null);
      findXPathsFor("MT574L.FormatModel.xsd", null);
      findXPathsFor("MT575.FormatModel.xsd", null);
      findXPathsFor("MT576.FormatModel.xsd", null);
      findXPathsFor("MT577.FormatModel.xsd", null);
      findXPathsFor("MT578.FormatModel.xsd", null);
      findXPathsFor("MT579.FormatModel.xsd", null);
      findXPathsFor("MT580.FormatModel.xsd", null);
      findXPathsFor("MT581.FormatModel.xsd", null);
      findXPathsFor("MT582.FormatModel.xsd", null);
      findXPathsFor("MT583.FormatModel.xsd", null);
      findXPathsFor("MT584.FormatModel.xsd", null);
      findXPathsFor("MT586.FormatModel.xsd", null);
      findXPathsFor("MT587.FormatModel.xsd", null);
      findXPathsFor("MT588.FormatModel.xsd", null);
      findXPathsFor("MT589.FormatModel.xsd", null);
      findXPathsFor("MT590.FormatModel.xsd", null);
      findXPathsFor("MT591.FormatModel.xsd", null);
      findXPathsFor("MT592.FormatModel.xsd", null);
      findXPathsFor("MT593.FormatModel.xsd", null);
      findXPathsFor("MT595.FormatModel.xsd", null);
      findXPathsFor("MT596.FormatModel.xsd", null);
      findXPathsFor("MT598.FormatModel.xsd", null);
      findXPathsFor("MT599.FormatModel.xsd", null);
      findXPathsFor("MT600.FormatModel.xsd", null);
      findXPathsFor("MT601.FormatModel.xsd", null);
      findXPathsFor("MT604.FormatModel.xsd", null);
      findXPathsFor("MT605.FormatModel.xsd", null);
      findXPathsFor("MT606.FormatModel.xsd", null);
      findXPathsFor("MT607.FormatModel.xsd", null);
      findXPathsFor("MT608.FormatModel.xsd", null);
      findXPathsFor("MT609.FormatModel.xsd", null);
      findXPathsFor("MT643.FormatModel.xsd", null);
      findXPathsFor("MT644.FormatModel.xsd", null);
      findXPathsFor("MT645.FormatModel.xsd", null);
      findXPathsFor("MT646.FormatModel.xsd", null);
      findXPathsFor("MT649.FormatModel.xsd", null);
      findXPathsFor("MT690.FormatModel.xsd", null);
      findXPathsFor("MT691.FormatModel.xsd", null);
      findXPathsFor("MT692.FormatModel.xsd", null);
      findXPathsFor("MT693.FormatModel.xsd", null);
      findXPathsFor("MT695.FormatModel.xsd", null);
      findXPathsFor("MT696.FormatModel.xsd", null);
      findXPathsFor("MT698.FormatModel.xsd", null);
      findXPathsFor("MT699.FormatModel.xsd", null);
      findXPathsFor("MT700.FormatModel.xsd", null);
      findXPathsFor("MT701.FormatModel.xsd", null);
      findXPathsFor("MT705.FormatModel.xsd", null);
      findXPathsFor("MT707.FormatModel.xsd", null);
      findXPathsFor("MT710.FormatModel.xsd", null);
      findXPathsFor("MT711.FormatModel.xsd", null);
      findXPathsFor("MT720.FormatModel.xsd", null);
      findXPathsFor("MT721.FormatModel.xsd", null);
      findXPathsFor("MT730.FormatModel.xsd", null);
      findXPathsFor("MT732.FormatModel.xsd", null);
      findXPathsFor("MT734.FormatModel.xsd", null);
      findXPathsFor("MT740.FormatModel.xsd", null);
      findXPathsFor("MT742.FormatModel.xsd", null);
      findXPathsFor("MT747.FormatModel.xsd", null);
      findXPathsFor("MT750.FormatModel.xsd", null);
      findXPathsFor("MT752.FormatModel.xsd", null);
      findXPathsFor("MT754.FormatModel.xsd", null);
      findXPathsFor("MT756.FormatModel.xsd", null);
      findXPathsFor("MT760.FormatModel.xsd", null);
      findXPathsFor("MT767.FormatModel.xsd", null);
      findXPathsFor("MT768.FormatModel.xsd", null);
      findXPathsFor("MT769.FormatModel.xsd", null);
      findXPathsFor("MT790.FormatModel.xsd", null);
      findXPathsFor("MT791.FormatModel.xsd", null);
      findXPathsFor("MT792.FormatModel.xsd", null);
      findXPathsFor("MT793.FormatModel.xsd", null);
      findXPathsFor("MT795.FormatModel.xsd", null);
      findXPathsFor("MT796.FormatModel.xsd", null);
      findXPathsFor("MT798.FormatModel.xsd", null);
      findXPathsFor("MT799.FormatModel.xsd", null);
      findXPathsFor("MT800.FormatModel.xsd", null);
      findXPathsFor("MT801.FormatModel.xsd", null);
      findXPathsFor("MT802.FormatModel.xsd", null);
      findXPathsFor("MT810.FormatModel.xsd", null);
      findXPathsFor("MT812.FormatModel.xsd", null);
      findXPathsFor("MT813.FormatModel.xsd", null);
      findXPathsFor("MT820.FormatModel.xsd", null);
      findXPathsFor("MT821.FormatModel.xsd", null);
      findXPathsFor("MT822.FormatModel.xsd", null);
      findXPathsFor("MT823.FormatModel.xsd", null);
      findXPathsFor("MT824.FormatModel.xsd", null);
      findXPathsFor("MT890.FormatModel.xsd", null);
      findXPathsFor("MT891.FormatModel.xsd", null);
      findXPathsFor("MT892.FormatModel.xsd", null);
      findXPathsFor("MT893.FormatModel.xsd", null);
      findXPathsFor("MT895.FormatModel.xsd", null);
      findXPathsFor("MT896.FormatModel.xsd", null);
      findXPathsFor("MT898.FormatModel.xsd", null);
      findXPathsFor("MT899.FormatModel.xsd", null);
      findXPathsFor("MT900.FormatModel.xsd", null);
      findXPathsFor("MT910.FormatModel.xsd", null);
      findXPathsFor("MT920.FormatModel.xsd", null);
      findXPathsFor("MT935.FormatModel.xsd", null);
      findXPathsFor("MT940.FormatModel.xsd", null);
      findXPathsFor("MT941.FormatModel.xsd", null);
      findXPathsFor("MT942.FormatModel.xsd", null);
      findXPathsFor("MT950.FormatModel.xsd", null);
      findXPathsFor("MT960.FormatModel.xsd", null);
      findXPathsFor("MT961.FormatModel.xsd", null);
      findXPathsFor("MT962.FormatModel.xsd", null);
      findXPathsFor("MT963.FormatModel.xsd", null);
      findXPathsFor("MT964.FormatModel.xsd", null);
      findXPathsFor("MT965.FormatModel.xsd", null);
      findXPathsFor("MT966.FormatModel.xsd", null);
      findXPathsFor("MT967.FormatModel.xsd", null);
      findXPathsFor("MT970.FormatModel.xsd", null);
      findXPathsFor("MT971.FormatModel.xsd", null);
      findXPathsFor("MT972.FormatModel.xsd", null);
      findXPathsFor("MT973.FormatModel.xsd", null);
      findXPathsFor("MT985.FormatModel.xsd", null);
      findXPathsFor("MT986.FormatModel.xsd", null);
      findXPathsFor("MT990.FormatModel.xsd", null);
      findXPathsFor("MT991.FormatModel.xsd", null);
      findXPathsFor("MT992.FormatModel.xsd", null);
      findXPathsFor("MT993.FormatModel.xsd", null);
      findXPathsFor("MT995.FormatModel.xsd", null);
      findXPathsFor("MT996.FormatModel.xsd", null);
      findXPathsFor("MT998.FormatModel.xsd", null);
      findXPathsFor("MT999.FormatModel.xsd", null);
      findXPathsFor("reda.001.001.03.xsd", null);
      findXPathsFor("reda.002.001.03.xsd", null);
      findXPathsFor("reda.003.001.03.xsd", null);
      findXPathsFor("semt.001.001.02.xsd", null);
      findXPathsFor("semt.002.001.02.xsd", null);
      findXPathsFor("semt.003.001.02.xsd", null);
      findXPathsFor("semt.004.001.02.xsd", null);
      findXPathsFor("semt.005.001.02.xsd", null);
      findXPathsFor("semt.006.001.02.xsd", null);
      findXPathsFor("semt.007.001.02.xsd", null);
      findXPathsFor("sese.001.001.02.xsd", null);
      findXPathsFor("sese.002.001.02.xsd", null);
      findXPathsFor("sese.003.001.02.xsd", null);
      findXPathsFor("sese.004.001.02.xsd", null);
      findXPathsFor("sese.005.001.02.xsd", null);
      findXPathsFor("sese.006.001.02.xsd", null);
      findXPathsFor("sese.007.001.02.xsd", null);
      findXPathsFor("sese.008.001.02.xsd", null);
      findXPathsFor("sese.009.001.02.xsd", null);
      findXPathsFor("sese.010.001.02.xsd", null);
      findXPathsFor("sese.011.001.02.xsd", null);
      findXPathsFor("sese.012.001.02.xsd", null);
      findXPathsFor("sese.013.001.02.xsd", null);
      findXPathsFor("sese.014.001.02.xsd", null);
      findXPathsFor("sese.018.001.01.xsd", null);
      findXPathsFor("sese.019.001.01.xsd", null);
      findXPathsFor("setr.001.001.03.xsd", null);
      findXPathsFor("setr.002.001.03.xsd", null);
      findXPathsFor("setr.003.001.03.xsd", null);
      findXPathsFor("setr.004.001.03.xsd", null);
      findXPathsFor("setr.005.001.03.xsd", null);
      findXPathsFor("setr.006.001.03.xsd", null);
      findXPathsFor("setr.007.001.03.xsd", null);
      findXPathsFor("setr.008.001.03.xsd", null);
      findXPathsFor("setr.009.001.03.xsd", null);
      findXPathsFor("setr.010.001.03.xsd", null);
      findXPathsFor("setr.011.001.03.xsd", null);
      findXPathsFor("setr.012.001.03.xsd", null);
      findXPathsFor("setr.013.001.03.xsd", null);
      findXPathsFor("setr.014.001.03.xsd", null);
      findXPathsFor("setr.015.001.03.xsd", null);
      findXPathsFor("setr.016.001.03.xsd", null);
      findXPathsFor("setr.017.001.03.xsd", null);
      findXPathsFor("setr.018.001.03.xsd", null);
      findXPathsFor("setr.047.001.01.xsd", null);
      findXPathsFor("setr.048.001.01.xsd", null);
      findXPathsFor("setr.049.001.01.xsd", null);
      findXPathsFor("setr.050.001.01.xsd", null);
      findXPathsFor("setr.051.001.01.xsd", null);
      findXPathsFor("setr.052.001.01.xsd", null);
      findXPathsFor("setr.053.001.01.xsd", null);
      findXPathsFor("setr.054.001.01.xsd", null);
      findXPathsFor("setr.055.001.01.xsd", null);
      findXPathsFor("setr.056.001.01.xsd", null);
      findXPathsFor("setr.057.001.01.xsd", null);
      findXPathsFor("setr.058.001.01.xsd", null);
      findXPathsFor("User-to-UserMessageACK_NAK.FormatModel.xsd", null);

      // findXPathsFor("MT515.xsd", "root", true);

      findXPathsFor("orderbook_elements.xsd", "orderbook", false);
      findXPathsFor("orderbook_attributes.xsd", "orderbook", false);
   }

   public void testSpecific() throws Exception
   {
      // findXPathsFor("testschema.xsd", "AllDataTypesMetadata");
      findXPathsFor("orderbook_elements.xsd", "orderbook");
   }

   private void findXPathsFor(String xsdFileName, String name) throws Exception
   {
      findXPathsFor(xsdFileName, name, false);
   }

   private void findXPathsFor(String xsdFileName, String name, boolean verify)
         throws Exception
   {
      // System.out.println("XSD: "+xsdFileName);

      ResourceSet resourceSet = new ResourceSetImpl();

      HashMap options = new HashMap();
      options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);

      Resource resource = resourceSet.createResource(URI.createURI(XPathFinderTest.class.getResource(
            xsdFileName)
            .toString()));
      resource.load(options);

      List l = resource.getContents();
      XSDSchema xsdSchema = (XSDSchema) l.get(0);

      // System.out.println("Types: ");
      List xsdTypeDefinitions = xsdSchema.getTypeDefinitions();
      for (int i = 0; i < xsdTypeDefinitions.size(); i++ )
      {
         XSDTypeDefinition xsdTypeDefinition = (XSDTypeDefinition) xsdTypeDefinitions.get(i);
         String typeName = xsdTypeDefinition.getName();
         if (name == null || name.equals(typeName))
         {
            XPathFinder.findAllXPaths(xsdSchema, typeName, false);
         }
      }

      //System.out.println("Elements: ");
      List xsdElementDeclarations = xsdSchema.getElementDeclarations();
      for (int i = 0; i < xsdElementDeclarations.size(); i++ )
      {
         XSDElementDeclaration xsdElementDeclaration = (XSDElementDeclaration) xsdElementDeclarations.get(i);
         String elementName = xsdElementDeclaration.getName();
         if (name == null || name.equals(elementName))
         {
            Set allXPaths_New = XPathFinder.findAllXPaths(xsdSchema, elementName, false);
            //System.out.println(elementName + ":" + allXPaths_New.size());

            if (verify)
            {
               verifyXml(allXPaths_New, "c:/tmp/xml/" + xsdFileName + "/" + elementName
                     + ".xml");
               verifyXPaths(allXPaths_New, "c:/tmp/xml/" + xsdFileName + "/"
                     + elementName + ".xml");
            }
         }
      }
   }

   private void verifyXPaths(Set allXPaths, String xmlFileName) throws Exception
   {
      Document xmlDocument = XmlUtils.parseSource(new InputSource(new FileInputStream(
            xmlFileName)), null);
      IXPathMap xPathMap = new ClientXPathMap(allXPaths);

      for (Iterator i = xPathMap.getAllXPaths().iterator(); i.hasNext();)
      {
         TypedXPath typedXPath = (TypedXPath) i.next();

         if (xPathMap.getRootXPath() == typedXPath)
         {
            continue;
         }

         XPathExpression xPath = XPathFactory.newInstance().newXPath().compile(typedXPath.getXPath());
         Object result = xPath.evaluate(xmlDocument.getDocumentElement(), XPathConstants.NODESET);
         assertNotNull("no entry in xml found for xpath " + typedXPath.getXPath(), result);
         if (result instanceof NodeList)
         {
            assertFalse("no entry in xml found for xpath " + typedXPath.getXPath(),
                  ((NodeList) result).getLength() == 0);
         }
      }
   }

   private void verifyXml(Set allXPaths, String xmlFileName) throws Exception
   {
      Document xmlDocument = XmlUtils.parseSource(new InputSource(new FileInputStream(
            xmlFileName)), null);

      verifyElement("", xmlDocument.getDocumentElement(), new ClientXPathMap(allXPaths));
   }

   private void verifyElement(String xPath, Element element, IXPathMap xPathMap)
         throws Exception
   {

      NamedNodeMap attributes = element.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++ )
      {
         Attr attr = (Attr) attributes.item(i);
         String attributeXPath;
         if ("".equals(xPath))
         {
            attributeXPath = "@" + attr.getLocalName();
         }
         else
         {
            attributeXPath = xPath + "@" + attr.getLocalName();
         }
         TypedXPath typedXPath = xPathMap.getXPath(attributeXPath);
         assertNotNull("no xpath found for attribute " + attributeXPath, typedXPath);
      }

      NodeList nodes = element.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++ )
      {
         Node node = (Node) nodes.item(i);
         if (node.getNodeType() != Node.ELEMENT_NODE)
         {
            continue;
         }
         String elementXPath;
         if ("".equals(xPath))
         {
            elementXPath = node.getLocalName();
         }
         else
         {
            elementXPath = xPath + "/" + node.getLocalName();
         }
         TypedXPath typedXPath = xPathMap.getXPath(elementXPath);
         assertNotNull("no xpath found for attribute " + elementXPath, typedXPath);
      }
   }
}
