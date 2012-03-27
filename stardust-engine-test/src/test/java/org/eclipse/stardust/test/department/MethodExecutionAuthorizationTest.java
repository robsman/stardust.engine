/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.department;

import static org.eclipse.stardust.test.department.DepartmentModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.RuntimeConfigurer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether declarative security rules for method
 * execution depending and the algorithm described in
 * chapter "Department Evaluation" of the functional
 * specification are implemented correctly, i.e. method
 * execution should only be possible if the performing
 * user is part of the sub hierarchy of the target
 * participant (always true in this test case) and has
 * either a grant on the <i>target-department</i> or a
 * grant on a sub department of <i>target-department</i>.
 * </p>
 * 
 * <p>
 * <table border="1">
 *    <tr>
 *       <th>No.</th>
 *       <th><i>reference-department</i></th>
 *       <th><i>reference-organization</i></th>
 *       <th>process variables</th>
 *       <th><i>target-participant</i></th>
 *       <th><i>target-organization</i></th>
 *       <th><i>target-department</i></th>
 *       <th>grants allowed</th>
 *       <th>grants NOT allowed</th>
 *    </tr>
 *    <tr>
 *       <td>1</td>
 *       <td>(u)</td>
 *       <td>Org1</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(u,i)</td>
 *       <td>Org3&lt;u,i&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org5&lt;u,i&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;v,j&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;v,null,null&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,j&gt;, Org5&lt;v,null&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>2</td>
 *       <td>(u,i)</td>
 *       <td>Org3</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(u,i)</td>
 *       <td>Org3&lt;u,i&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org5&lt;u,i&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;v,j&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;v,null,null&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,j&gt;, Org5&lt;v,null&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>3</td>
 *       <td>(u,i,m)</td>
 *       <td>Org4</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(u,i)</td>
 *       <td>Org3&lt;u,i&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org5&lt;u,i&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;v,j&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;v,null,null&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,j&gt;, Org5&lt;v,null&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>4</td>
 *       <td>(u)</td>
 *       <td>Org1</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>5</td>
 *       <td>(u,i)</td>
 *       <td>Org3</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>6</td>
 *       <td>(u,i,m)</td>
 *       <td>Org4</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>7</td>
 *       <td>(v)</td>
 *       <td>Org1</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(v,null)</td>
 *       <td>Org3&lt;v,null&gt;, Org4&lt;v,null,null&gt;, Org5&lt;v,null&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;u,i&gt;, Org3&lt;v,j&gt;, Org4&lt;null&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org4&lt;u,null,null&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;u,i&gt;, Org5&lt;v,j&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>8</td>
 *       <td>(v,j)</td>
 *       <td>Org3</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(v,j)</td>
 *       <td>Org3&lt;v,j&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5&lt;v,j&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;u,i&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org4&lt;v,null,null&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,null&gt;, Org5&lt;u,i&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>9</td>
 *       <td>(v,j,n)</td>
 *       <td>Org4</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(v,j)</td>
 *       <td>Org3&lt;v,j&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5&lt;v,j&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;u,i&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org4&lt;v,null,null&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,null&gt;, Org5&lt;u,i&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>10</td>
 *       <td>(v)</td>
 *       <td>Org1</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>11</td>
 *       <td>(v,j)</td>
 *       <td>Org3</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>12</td>
 *       <td>(v,j,n)</td>
 *       <td>Org4</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 * </table>
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class MethodExecutionAuthorizationTest extends LocalJcrH2Test
{
   private static final String USER_NAME = "u1";
   private static final String USER_PWD = "u1";
   
   private Role admin;
   
   private Organization org1;
   private Organization org3;
   private Organization org4;
   private Organization org5;
   private Organization readerOrg;
   
   private Department depU;
   private Department depV;
   private Department depUi;
   private Department depVj;
   private Department depUim;
   private Department depVjn;
   private Department depA;
   private Department depB;
   
   private ModelParticipantInfo org1u;
   private ModelParticipantInfo org1v;
   private ModelParticipantInfo org3ui;
   private ModelParticipantInfo org3uNull;
   private ModelParticipantInfo org3vj;
   private ModelParticipantInfo org3vNull;
   private ModelParticipantInfo org5ui;
   private ModelParticipantInfo org5uNull;
   private ModelParticipantInfo org5vj;
   private ModelParticipantInfo org5vNull;
   private ModelParticipantInfo org4uim;
   private ModelParticipantInfo org4uiNull;
   private ModelParticipantInfo org4uNullNull;
   private ModelParticipantInfo org4vjn;
   private ModelParticipantInfo org4vjNull;
   private ModelParticipantInfo org4vNullNull;
   private ModelParticipantInfo readerOrgA;
   private ModelParticipantInfo readerOrgB;
   
   private ActivityInstance ai;

   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(MODEL_NAME, adminSf);
   private final ClientServiceFactory userSf = new ClientServiceFactory(USER_NAME, USER_PWD);

   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(userSf);
   
   @Before
   public void setUp()
   {
      createOrgsAndDepartments();
      createScopedParticipants();
      
      final User user = adminSf.getUserService().createUser(USER_NAME, USER_NAME, USER_NAME, USER_NAME, USER_PWD, null, null, null);
      addAllGrantsForDelegationTo(user);

      startProcess();
      ai = adminSf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAll());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * 
    * @methodDescription When delegating to scoped participant org1-u, only descendants with proper scope can read 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition org3-u-i and org4-u-i-null,org4-u-i-m as well as org5-u-i can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase1()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1u);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3vj, org4, org4uNullNull, org4vNullNull, org4vjNull, org4vjn, org5, org5uNull, org5vj, org5vNull };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3ui, org4uiNull, org4uim, org5ui };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * 
    * @methodDescription When delegating to scoped participant org3-u-i, only descendants with proper scope can read 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition org3-u-i and org4-u-i-null,org4-u-i-m as well as org5-u-i can read
    *   Any with null scope, not descending, or unscoped participant cannot read. 
    */
   @Test
   public void testCase2()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3ui);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3vj, org4, org4uNullNull, org4vNullNull, org4vjNull, org4vjn, org5, org5uNull, org5vj, org5vNull };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3ui, org4uiNull, org4uim, org5ui };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * 
    * @methodDescription When delegating to scoped participant org4-u-i-m, only descendants with proper scope can read 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition org3-u-i and org4-u-i-null,org4-u-i-m as well as org5-u-i can read
    *   Any with null scope, not descending, or unscoped participant cannot read. 
    */
   @Test
   public void testCase3()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org4uim);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3vj, org4, org4uNullNull, org4vNullNull, org4vjNull, org4vjn, org5, org5uNull, org5vj, org5vNull };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3ui, org4uiNull, org4uim, org5ui };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition readerOrg-a can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase4()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1u);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition readerOrg-a can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase5()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3ui);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition readerOrg-a can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase6()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org4uim);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * 
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition org3-v-null and org4-v-null-null as well as org5-v-null can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase7()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1v);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3ui, org3vj, org4, org4vjNull, org4vjn, org4uNullNull, org4uiNull, org4uim, org5, org5uNull, org5ui, org5vj };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3vNull, org4vNullNull, org5vNull };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * 
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition org3-v-j and org4-v-j-null,org4-v-j-n as well as org5-v-j can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase8()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3vj);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3ui, org4, org4uNullNull, org4uiNull, org4uim, org4vNullNull, org5, org5uNull, org5vNull, org5ui };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3vj, org4vjNull, org4vjn, org5vj };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * 
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition org3-v-j and org4-v-j-null,org4-v-j-n as well as org5-v-j can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase9()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org4vjn);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3ui, org4, org4uNullNull, org4uiNull, org4uim, org4vNullNull, org5, org5uNull, org5vNull, org5ui };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3vj, org4vjNull, org4vjn, org5vj };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition readerOrg-a can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase10()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1v);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition readerOrg-a can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase11()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3vj);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    * @methodDescription 
    * @Pre_Condition Users generated for all scopes, started process8 with scoped data set X=U,Y=I,Z=M,A=A; read grant for Reader and Org3
    * @Post_Condition readerOrg-a can read
    *   Any with null scope, not descending, or unscoped participant cannot read.
    */
   @Test
   public void testCase12()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org4vjn);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   private void createOrgsAndDepartments()
   {
      final Model model = adminSf.getQueryService().getActiveModel();
      admin = model.getRole(ROLE_ADMIN_ID);
      org1 = model.getOrganization(ORG1_ID);
      org3 = model.getOrganization(ORG3_ID);
      org4 = model.getOrganization(ORG4_ID);
      org5 = model.getOrganization(ORG5_ID);
      readerOrg = model.getOrganization(READER_ORG_ID);
      
      depU = adminSf.getAdministrationService().createDepartment(DEP_ID_U, DEP_ID_U, null, null, org1);
      depV = adminSf.getAdministrationService().createDepartment(DEP_ID_V, DEP_ID_V, null, null, org1);
      depA = adminSf.getAdministrationService().createDepartment(DEP_ID_A, DEP_ID_A, null, null, readerOrg);
      depB = adminSf.getAdministrationService().createDepartment(DEP_ID_B, DEP_ID_B, null, null, readerOrg);
      
      depUi = adminSf.getAdministrationService().createDepartment(SUB_DEP_ID_I, SUB_DEP_ID_I, null, depU, org3);
      depUim = adminSf.getAdministrationService().createDepartment(SUB_SUB_DEP_ID_M, SUB_SUB_DEP_ID_M, null, depUi, org4);
      
      depVj = adminSf.getAdministrationService().createDepartment(SUB_DEP_ID_J, SUB_DEP_ID_J, null, depV, org3);
      depVjn = adminSf.getAdministrationService().createDepartment(SUB_SUB_DEP_ID_N, SUB_SUB_DEP_ID_N, null, depVj, org4);
   }
   
   private void createScopedParticipants()
   {
      org1u = depU.getScopedParticipant(org1);
      org1v = depV.getScopedParticipant(org1);
      
      org3ui = depUi.getScopedParticipant(org3);
      org3uNull = depU.getScopedParticipant(org3);
      org3vj = depVj.getScopedParticipant(org3);
      org3vNull = depV.getScopedParticipant(org3);

      org5ui = depUi.getScopedParticipant(org5);
      org5uNull = depU.getScopedParticipant(org5);
      org5vj = depVj.getScopedParticipant(org5);
      org5vNull = depV.getScopedParticipant(org5);
      
      org4uim = depUim.getScopedParticipant(org4);
      org4uiNull = depUi.getScopedParticipant(org4);
      org4uNullNull = depU.getScopedParticipant(org4);
      org4vjn = depVjn.getScopedParticipant(org4);
      org4vjNull = depVj.getScopedParticipant(org4);
      org4vNullNull = depV.getScopedParticipant(org4);
      
      readerOrgA = depA.getScopedParticipant(readerOrg);
      readerOrgB = depB.getScopedParticipant(readerOrg);
   }
   
   private void addAllGrantsForDelegationTo(final User user)
   {
      user.removeAllGrants();
      user.addGrant(org1u);
      user.addGrant(org1v);
      user.addGrant(org3ui);
      user.addGrant(org4uim);
      user.addGrant(org3vj);
      user.addGrant(org4vjn);
      user.addGrant(admin);
      adminSf.getUserService().modifyUser(user);
   }
   
   private void startProcess()
   {
      /* start process in scope (u,i,m,a) */
      final Map<String, String> piData = new HashMap<String, String>();
      piData.put(X_SCOPE, DEP_ID_U);
      piData.put(Y_SCOPE, SUB_DEP_ID_I);
      piData.put(Z_SCOPE, SUB_SUB_DEP_ID_M);
      piData.put(A_SCOPE, DEP_ID_A);
      userSf.getWorkflowService().startProcess(PROCESS_ID_8, piData, true);
   }
   
   private void removeAllGrantsFor(final User user)
   {
      user.removeAllGrants();
      adminSf.getUserService().modifyUser(user);
   }
   
   private void addGrantFor(final User user, final ModelParticipantInfo grant)
   {
      user.addGrant(grant);
      adminSf.getUserService().modifyUser(user);
   }
   
   private void ensureReadActivityInstanceEventHandlerFailsFor(final ModelParticipantInfo[] grants, final long aiOID)
   {
      final User user = userSf.getWorkflowService().getUser();
      removeAllGrantsFor(user);
      ensureReadActivityInstanceEventHandlerFailsInternal(null, aiOID);
      
      for (ModelParticipantInfo grant : grants)
      {
         removeAllGrantsFor(user);
         addGrantFor(user, grant);
         
         ensureReadActivityInstanceEventHandlerFailsInternal(grant, aiOID);
      }
   }

   private void ensureReadActivityInstanceEventHandlerFailsInternal(final ModelParticipantInfo grant, final long aiOID)
   {
      final StringBuffer sb = new StringBuffer("Attempt to read AI data should throw AccessForbiddenException for grant: ");
      sb.append(getDepartmentStringFor(grant) + " ... ");
      
      try
      {
         userSf.getWorkflowService().getActivityInstanceEventHandler(aiOID, ON_EXCEPTION_HANDLER_ID);
         Assert.fail("Read should fail due to insufficient permissions.");
      }
      catch (AccessForbiddenException e)
      {
         /* expected */
      }
      catch (RuntimeException e)
      {
         throw e;
      }
   }
   
   private void ensureReadActivityInstanceEventHandlerSucceedsFor(final ModelParticipantInfo[] grants, final long aiOID)
   {
      final User user = userSf.getWorkflowService().getUser();
      
      for (ModelParticipantInfo grant : grants)
      {
         removeAllGrantsFor(user);
         addGrantFor(user, grant);

         final StringBuffer sb = new StringBuffer("Attempt to read AI data should succeed for grant: ");
         sb.append(getDepartmentStringFor(grant) + " ... ");
         
         try
         {
            userSf.getWorkflowService().getActivityInstanceEventHandler(aiOID, ON_EXCEPTION_HANDLER_ID);
         }
         catch (AccessForbiddenException e)
         {
            throw e;
         }
      }
   }
   
   private String getDepartmentStringFor(final ModelParticipantInfo grant)
   {
      if (grant == null) return "<no grant>";
      
      final String orgStr = grant.getId();

      final DepartmentInfo depInfo = grant.getDepartment();
      
      String depStr;
      if (depInfo != null)
      {
         final Department dep = adminSf.getAdministrationService().getDepartment(depInfo.getOID());
         depStr = getDepartmentId(dep);
      }
      else
      {
         depStr = "null";
      }         
      
      return orgStr + "<" + depStr + ">";
   }
   
   private String getDepartmentId(final Department department)
   {
      final Department parent = department.getParentDepartment(); 
      if (parent == null)
      {
         return department.getId();
      }
      else
      {
         final String parentStr = getDepartmentId(parent);
         return parentStr + (parentStr.length() > 0 ? "," : "") + department.getId();
      }
   }
}
