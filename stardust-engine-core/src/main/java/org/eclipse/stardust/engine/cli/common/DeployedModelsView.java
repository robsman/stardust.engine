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
package org.eclipse.stardust.engine.cli.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.compatibility.gui.ToolbarButton;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.DateEntry;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class DeployedModelsView extends JComponent
{
   private static final long serialVersionUID = 1L;

   public static final int DEPLOYMENT_MODE = 0;
   public static final int ADMINISTRATION_MODE = 1;
   public static final int SELECTION_MODE = 2;

   private int mode;
   private boolean overwrite;
   private ModelTemplate deployment;

   private JRadioButton oneYear;
   private JRadioButton twoYears;
   private JRadioButton fullScale;
   private ToolbarButton deleteButton;
   private ToolbarButton restoreButton;
   private JButton up;
   private JButton down;
   private JList list;
   private DateEntry validFromEntry;
   private JTextArea commentEntry;

   private long now;
   private long start;
   private long end;
   private List models;
   private Map segments = new HashMap();

   private Action restoreAction = new AbstractAction(null, GUI.getIcon("images/cross_add.gif"))
   {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e)
      {
         ModelTemplate template = (ModelTemplate) list.getSelectedValue();
         if (template != null)
         {
            template.setDeleted(false);
            computeSegments();
            list.repaint();
            restoreAction.setEnabled(false);
            deleteAction.setEnabled(true);
         }
      }
   };
   
   private Action deleteAction = new AbstractAction(null, GUI.getIcon("images/cross_delete.gif"))
   {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e)
      {
         ModelTemplate template = (ModelTemplate) list.getSelectedValue();
         if (template != null)
         {
            template.setDeleted(true);
            computeSegments();
            list.repaint();
            restoreAction.setEnabled(true);
            deleteAction.setEnabled(false);
         }
      }
   };

   private Action upAction = new AbstractAction(null, GUI.getIcon("images/arrow_up.gif"))
   {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e)
      {
         int index = list.getSelectedIndex();
         if (index > 0)
         {
            swap(index, index - 1);
         }
      }
   };

   private Action downAction = new AbstractAction(null, GUI.getIcon("images/arrow_down.gif"))
   {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e)
      {
         int index = list.getSelectedIndex();
         DefaultListModel model = (DefaultListModel) list.getModel();
         if (index >= 0 && index < model.size() - 1)
         {
            swap(index, index + 1);
         }
      }
   };

   private Action oneYearAction = new AbstractAction("1 year")
   {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e)
      {
         set1YearScale();
         list.repaint();
      }
   };

   private Action twoYearsAction = new AbstractAction("2 years")
   {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e)
      {
         Calendar now = TimestampProviderUtils.getCalendar();
         Calendar start = (Calendar) now.clone();
         start.add(Calendar.MONTH, -4);
         Calendar end = (Calendar) now.clone();
         end.add(Calendar.MONTH, 20);
         DeployedModelsView.this.now = now.getTime().getTime();
         DeployedModelsView.this.start = start.getTime().getTime();
         DeployedModelsView.this.end = end.getTime().getTime();
         list.repaint();
      }
   };

   private Action fullScaleAction = new AbstractAction("all")
   {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent event)
      {
         Calendar now = TimestampProviderUtils.getCalendar();
         long n = now.getTime().getTime();
         long s = Long.MAX_VALUE;
         long e = Long.MIN_VALUE;
         boolean set = false;
         for (int i = 0; i < models.size(); i++)
         {
            Date d = ((ModelTemplate) models.get(i)).getValidFrom();
            if (d != null && s >= d.getTime() )
            {
               s = d.getTime();
               set = true;
            }
            d = null;
            if (d != null && e <= d.getTime() )
            {
               e = d.getTime();
               set = true;
            }
         }
         if (set)
         {
            long ds = n - s;
            long es = e - n;
            if (ds < es)
            {
               ds = es;
            }
            ds += 1000l * 3600 * 24 * 7;
            DeployedModelsView.this.now = n;
            DeployedModelsView.this.start = n - ds;
            DeployedModelsView.this.end = n + ds;
            list.repaint();
         }
      }
   };

   private ListSelectionListener listener = new ListSelectionListener()
   {
      public void valueChanged(ListSelectionEvent e)
      {
         if (mode == DEPLOYMENT_MODE )
         {
            if (!overwrite)
            {
               list.setSelectedValue(deployment, false);
            }
            else
            {
               if (e != null)
               {
                  if (list.getSelectedIndex() == -1)
                  {
                     list.setSelectedIndex(e.getFirstIndex());
                  }
               }
            }
         }
         else if (mode == ADMINISTRATION_MODE)
         {
            ModelTemplate t = (ModelTemplate) list.getSelectedValue();
            if (t == null)
            {
               validFromEntry.setDate(null);
               commentEntry.setText(null);
               validFromEntry.setEnabled(false);
               commentEntry.setEnabled(false);
               deleteAction.setEnabled(false);
               restoreAction.setEnabled(true);
               upAction.setEnabled(false);
               downAction.setEnabled(false);
            }
            else
            {
               validFromEntry.setEnabled(true);
               commentEntry.setEnabled(true);
               validFromEntry.setDate(t.getValidFrom());
               commentEntry.setText(t.getComment());
               deleteAction.setEnabled(!t.isDeleted());
               restoreAction.setEnabled(t.isDeleted());
               upAction.setEnabled(true);
               downAction.setEnabled(true);
            }
         }
         else if (mode == SELECTION_MODE)
         {
            ModelTemplate t = (ModelTemplate) list.getSelectedValue();
            if (t == null)
            {
               validFromEntry.setDate(null);
               commentEntry.setText(null);
            }
            else
            {
               validFromEntry.setDate(t.getValidFrom());
               commentEntry.setText(t.getComment());
            }
         }
      }
   };

   private InputVerifier detailsVerifier = new InputVerifier()
   {
      boolean inMessage = false;

      public boolean verify(JComponent input)
      {
         if (inMessage)
         {
            return false;
         }

         ModelTemplate m = null;
         switch (mode)
         {
            case DEPLOYMENT_MODE:
               m = deployment;
               break;
            case ADMINISTRATION_MODE:
            case SELECTION_MODE:
               m = (ModelTemplate) list.getSelectedValue();
               break;
         }
         if (m != null)
         {
            Date from = validFromEntry.getDate();
            m.setValidFrom(from);
            Date to = null;
            // todo: disallow 'to' values before 'from'
            if (to != null && from != null && to.before(from))
            {
               inMessage = true;
               int option = JOptionPane.showConfirmDialog(DeployedModelsView.this,
                     "'Valid To' date is before 'Valid From' date\n" +
                     "Do you want to reset it to the default (empty) value ?",
                     "Warning", JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.WARNING_MESSAGE);
               inMessage = false;
               switch (option)
               {
                  case JOptionPane.YES_OPTION:
                     to = null;
                     break;
                  case JOptionPane.CANCEL_OPTION:
                     return false;
               }
            }
            m.setComment(commentEntry.getText());
            computeSegments();
            list.repaint();
         }
         return true;
      }
   };

   public DeployedModelsView(int mode)
   {
      this.mode = mode;

      setLayout(new BorderLayout());

      JToolBar buttons = new JToolBar();
      buttons.setFloatable(false);
      buttons.setBorderPainted(false);

      if (mode != SELECTION_MODE)
      {
         up = new ToolbarButton();
         up.setAction(upAction);
         up.setMargin(GUI.noInsets);
         up.setToolTipText("Up");
         buttons.add(up);

         down = new ToolbarButton();
         down.setAction(downAction);
         down.setMargin(GUI.noInsets);
         down.setToolTipText("Down");
         buttons.add(down);

         if (mode == ADMINISTRATION_MODE)
         {
            deleteButton = new ToolbarButton();
            deleteButton.setAction(deleteAction);
            deleteButton.setMargin(GUI.noInsets);
            deleteButton.setToolTipText("Delete");
            buttons.add(deleteButton);

            restoreButton = new ToolbarButton();
            restoreButton.setAction(restoreAction);
            restoreButton.setMargin(GUI.noInsets);
            restoreButton.setToolTipText("Restore");
            buttons.add(restoreButton);
         }

         buttons.addSeparator();
      }

      oneYear = new JRadioButton(oneYearAction);
      twoYears = new JRadioButton(twoYearsAction);
      fullScale = new JRadioButton(fullScaleAction);
      ButtonGroup group = new ButtonGroup();
      group.add(oneYear);
      group.add(twoYears);
      group.add(fullScale);
      oneYear.setSelected(true);

      buttons.add(new JLabel("Scale: "));
      buttons.add(oneYear);
      buttons.add(twoYears);
      buttons.add(fullScale);
      add(buttons, BorderLayout.NORTH);
      set1YearScale();

      list = new JList()
      {
         private static final long serialVersionUID = 1L;

         public void paint(Graphics g)
         {
            super.paint(g);
            double w = getSize().getWidth();
            double scale = w / (end - start);
            g.setColor(Color.black);
            int n = (int) (scale * (now - start));
            g.drawLine(n, 0, n, getSize().height);
         }
      };
      list.setCellRenderer(new TimeFrameRenderer());
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane scroller = new JScrollPane(list);
      scroller.setPreferredSize(new Dimension(400, 200));
      add(scroller);

      validFromEntry = new DateEntry();
      commentEntry = new JTextArea(5, 15);
      validFromEntry.setInputVerifier(detailsVerifier);
      commentEntry.setInputVerifier(detailsVerifier);

      CellConstraints cc = new CellConstraints();
      FormLayout layout = new FormLayout("4dlu, default, 4dlu, default",
            "default, 4dlu, default, 4dlu, default, 4dlu, default, default, default");
      JPanel panel = new JPanel(layout);
      panel.add(new JLabel("Valid from:"), cc.xy(2, 1));
      panel.add(validFromEntry, cc.xy(4, 1));
      panel.add(new JLabel("Deployment comment:"), cc.xy(2, 3));
      panel.add(new JScrollPane(commentEntry), cc.xywh(2, 5, 3, 1));
      add(panel, BorderLayout.EAST);
      list.addListSelectionListener(listener);
   }

   private void set1YearScale()
   {
      Calendar now = TimestampProviderUtils.getCalendar();
      Calendar start = (Calendar) now.clone();
      start.add(Calendar.MONTH, -2);
      Calendar end = (Calendar) now.clone();
      end.add(Calendar.MONTH, 10);
      this.now = now.getTime().getTime();
      this.start = start.getTime().getTime();
      this.end = end.getTime().getTime();
   }

   public void setData(List models)
   {
      deployment = null;
      this.models = new ArrayList();
      for (int i = 0; i < models.size(); i++)
      {
         Object model = models.get(i);
         ModelTemplate template = new ModelTemplate(model);
         this.models.add(template);
      }

      ModelTemplate selection = null;

      boolean enabled = models.size() > 0 && mode == ADMINISTRATION_MODE;
      validFromEntry.setEnabled(enabled);
      commentEntry.setEnabled(enabled);

      if (models.size() > 0)
      {
         selection = (ModelTemplate) this.models.get(0);

         validFromEntry.setDate(selection.getValidFrom());
         commentEntry.setText(selection.getComment());
      }

      initialize(selection);
   }

   private void initialize(ModelTemplate selection)
   {
      computeSegments();
      DefaultListModel listModel = new DefaultListModel();
      for (int i = 0; i < this.models.size(); listModel.addElement(this.models.get(i++)));
      this.list.setModel(listModel);
      this.list.setSelectedValue(selection, true);
      listener.valueChanged(null);
      set1YearScale();
   }

   private void swap(int i1, int i2)
   {
      Object aux = models.get(i1);
      models.set(i1, models.get(i2));
      models.set(i2, aux);
      computeSegments();

      DefaultListModel model = (DefaultListModel) list.getModel();
      Object o = model.remove(i1);
      model.add(i2, o);
      list.setSelectedValue(o, true);
   }

   private void computeSegments()
   {
      ArrayList list = new ArrayList();
      for (int i = 0; i < models.size(); i++)
      {
         ModelTemplate m = (ModelTemplate) models.get(i);
         Long ts = new Long(DateUtils.getTimestamp(m.getValidFrom(), Long.MIN_VALUE));
         if (!list.contains(ts))
         {
            list.add(ts);
         }
         ts = Long.MAX_VALUE;
         if (!list.contains(ts))
         {
            list.add(ts);
         }
      }
      Collections.sort(list);
      segments.clear();
      for (int i = 0; i < list.size() - 1; i++)
      {
         Segment s = new Segment(((Long) list.get(i)).longValue(), ((Long) list.get(i + 1)).longValue());
         for (int j = 0; j < models.size(); j++)
         {
            ModelTemplate m = (ModelTemplate) models.get(j);
            long ts = DateUtils.getTimestamp(m.getValidFrom(), Long.MIN_VALUE);
            if (ts <= s.start)
            {
               segments.put(s, m);
               break;
            }
         }
      }
   }

   public int getPredecessor(ModelTemplate model)
   {
      for (int i = 0; i < models.size(); i++)
      {
         ModelTemplate template = (ModelTemplate) models.get(i);
         if (model == template)
         {
            if (i > 0)
            {
               return ((ModelTemplate) models.get(i-1)).getModelOID();
            }
            else
            {
               return 0;
            }
         }
      }
      Assert.lineNeverReached();
      return 0;
   }


   public ModelTemplate getDeploymentCandidate()
   {
      return deployment;
   }

   public Iterator getChangedModels()
   {
      return new FilteringIterator(models.iterator(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return ((ModelTemplate) o).notDeletedButModified();
         }

      });
   }

   public Iterator getDeletedModels()
   {
      return new FilteringIterator(models.iterator(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return ((ModelTemplate) o).isDeleted();
         }

      });
   }

   public ModelTemplate getSelectedModel()
   {
      return (ModelTemplate) list.getSelectedValue();
   }

   private static class Segment
   {
      long start;
      long end;

      public Segment(long start, long end)
      {
         this.start = start;
         this.end = end;
      }
   }

   private class TimeFrameRenderer extends DefaultListCellRenderer
   {
      private static final long serialVersionUID = 1L;

      private Color Active = new Color(0, 0.7f, 0);
      private Color Deleted = new Color(0.85f, 0.85f, 0.85f);

      private ModelTemplate current;
      private Border border;
      private Font bold;

      public TimeFrameRenderer()
      {
         border = BorderFactory.createEmptyBorder(1, 1, 8, 1);
         setHorizontalAlignment(SwingConstants.LEFT);
      }

      public void paint(Graphics g)
      {
         super.paint(g);
         double w = getSize().getWidth();
         int h = getSize().height - 8;
         double scale = w / (end - start);
         int s = (int) (current.getValidFrom() == null ? -1 :
               scale * (current.getValidFrom().getTime() - start));
         int e = (int) (w + 1);
         if (current.isDeleted())
         {
            g.setColor(Deleted);
         }
         else
         {
            g.setColor(Active);
         }
         g.drawLine(s, h, e, h);
         if (!current.isDeleted())
         {
            g.setColor(Active);
            for (Iterator i = segments.entrySet().iterator(); i.hasNext();)
            {
               Map.Entry entry = (Map.Entry) i.next();
               if (entry.getValue() == current)
               {
                  Segment seg = (Segment) entry.getKey();
                  int s1 = seg.start <= start ? s : (int) (scale * (seg.start - start));
                  int e1 = seg.end >= end ? e : (int) (scale * (seg.end - start));
                  g.drawLine(s1, h - 1, e1, h - 1);
                  g.drawLine(s1, h, e1, h);
                  g.drawLine(s1, h + 1, e1, h + 1);
               }
            }
            g.setColor(Color.black);
         }
         g.drawLine(s, h - 3, s, h + 3);
         g.drawLine(e, h - 3, e, h + 3);
      }

      public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus)
      {

         if (isSelected && overwrite)
         {
            current = deployment;
         }
         else
         {
            current = (ModelTemplate) value;
         }

         super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         setOpaque(isSelected);
         StringBuffer text = new StringBuffer();
         text.append(' ');
         if (current.isDeleted())
         {
            setForeground(Deleted);
            text.append("(deleted) ");
         }
         text.append(current.getName());
         text.append(": ");
         text.append(DateUtils.formatDate(current.getValidFrom()));
         setText(text.toString());
         if (current.equals(deployment))
         {
            setEnabled(true);
            setForeground(Color.red);
            setFont(bold == null ? bold = getFont().deriveFont(Font.BOLD) : bold);
         }
         setBorder(border);
         return this;
      }
   }

   public class ModelTemplate
   {
      private int modelOID;
      private String name;
      private Date validFrom;
      private String comment;
      private boolean deleted;
      private DeployedModelDescription source;

      public ModelTemplate(Object source)
      {
         if (source instanceof DeployedModelDescription)
         {
            set((DeployedModelDescription) source);
            this.source = (DeployedModelDescription) source;
         }
      }

      private void set(DeployedModelDescription md)
      {
         modelOID = md.getModelOID();
         name = md.getName() + " (version: " + md.getVersion() +
               ", OID: " + md.getModelOID() + ")";
         validFrom = md.getValidFrom();
         comment = md.getDeploymentComment();
      }

      public String getName()
      {
         return name;
      }

      public int getModelOID()
      {
         return modelOID;
      }

      public Date getValidFrom()
      {
         return validFrom;
      }

      public void setValidFrom(Date time)
      {
         validFrom = time;
      }

      public String getComment()
      {
         return comment;
      }

      public void setComment(String text)
      {
         comment = text;
      }

      public boolean notDeletedButModified()
      {
         if (source == null)
         {
            Assert.lineNeverReached();
         }
         if (isDeleted())
         {
            return false;
         }
         return !CompareHelper.areEqual(source.getDeploymentComment(), getComment())
         || !CompareHelper.areEqual(source.getValidFrom(), getValidFrom());
      }

      public int getPredecessorOID()
      {
         return getPredecessor(this);
      }

      public boolean isDeleted()
      {
         return deleted;
      }

      public void setDeleted(boolean deleted)
      {
         this.deleted = deleted;
      }
   }
}
