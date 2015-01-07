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
package org.eclipse.stardust.engine.core.model.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.ConnectionSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.DecorationStrategy;
import org.eclipse.stardust.engine.core.compatibility.diagram.Stylesheet;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;
import org.eclipse.stardust.engine.core.compatibility.gui.IconProvider;


/**
 * @author mgille
 */
public class ActivitySymbol extends NamedSymbol
{
   private static final Logger trace = LogManager.getLogger(ActivitySymbol.class);

   private static boolean resourcesInitialized;
   private static ImageIcon iconUnknown;
   private static ImageIcon iconManual;
   private static ImageIcon iconApplication;
   private static ImageIcon iconSubProcess;
   private static ImageIcon iconRoute;
   private static ImageIcon iconLoop;
   private static ImageIcon iconSplitAND;
   private static ImageIcon iconSplitXOR;
   private static ImageIcon iconJoinAND;
   private static ImageIcon iconJoinXOR;
   private static Stroke endCircleStroke;
   private static Stroke startCircleStroke;

   private static String STYLE;
   private static int EDGE_RADIUS;
   private static int LEFT_MARGIN;
   private static int RIGHT_MARGIN;
   private static int TOP_MARGIN;
   private static int BOTTOM_MARGIN;
   private static int ICON_WIDTH;
   private static int MARGIN;
   private static int START_END_LINE_LENGTH;
   private static int START_END_TRIANGLE_HEIGHT;
   private static int START_END_TRIANGLE_WIDTH;
   private static int START_CIRCLE_RADIUS;
   private static int END_CIRCLE_RADIUS;
   private static Color FILL_COLOR;
   private static Color PEN_COLOR;
   private static Stroke DEFAULT_STROKE;
   private static Stroke SELECTED_STROKE;
   private static Font NAME_FONT;
   private static ImageIcon MULTIPLE_TRIGGERS_ICON;
   private static ImageIcon MESSAGE_TRIGGER_ICON;
   private static ImageIcon TIMER_TRIGGER_ICON;

   private transient JMenuItem transitionItem;
   private transient JMenuItem openDefaultDiagramItem;
   private transient JMenuItem traverseItem;
//   private transient ArrayList metricsPanels;

   //popupmenu to handle metrics panels hooked to activity symbols
/*   private transient JPopupMenu metricsPopupMenu;
   private transient JMenuItem deleteFrameItem;
   private transient JCheckBoxMenuItem sizeItem;
   private transient JMenu toggleDataItem;
   private transient JCheckBoxMenuItem initDurationItem;
   private transient JCheckBoxMenuItem activeDurationItem;
   private transient JCheckBoxMenuItem waitingDurationItem;*/
   // @todo (pwh, ub): retrofit
   //private transient MetricsPanel metricsPopupRootPanel;
   private transient JMenuItem useDataItem;
   private int minSize;

   public static synchronized void initializeResources()
   {
      if (!resourcesInitialized)
      {
         STYLE = Stylesheet.instance().getString("Activity", "style", "carnot");
         TOP_MARGIN = Stylesheet.instance().getInteger("Activity", "top-margin", 20);
         BOTTOM_MARGIN = Stylesheet.instance().getInteger("Activity", "bottom-margin", 20);
         LEFT_MARGIN = Stylesheet.instance().getInteger("Activity", "left-margin", 20);
         RIGHT_MARGIN = Stylesheet.instance().getInteger("Activity", "right-margin", 20);
         EDGE_RADIUS = Stylesheet.instance().getInteger("Activity", "edge-radius", 10);
         ICON_WIDTH = Stylesheet.instance().getInteger("Activity", "icon-width", 15);
         MARGIN = Stylesheet.instance().getInteger("Activity", "margin", 10);
         START_END_LINE_LENGTH = Stylesheet.instance().getInteger("Activity", "start-end-line-length", 15);
         START_END_TRIANGLE_HEIGHT = Stylesheet.instance().getInteger("Activity", "start-end-triangle-height", 6);
         START_END_TRIANGLE_WIDTH = Stylesheet.instance().getInteger("Activity", "start-end-triangle-width", 16);
         START_CIRCLE_RADIUS = Stylesheet.instance().getInteger("Activity", "start-circle-radius", 10);
         END_CIRCLE_RADIUS = Stylesheet.instance().getInteger("Activity", "end-circle-radius", 10);
         DEFAULT_STROKE = Stylesheet.instance().getStroke("Activity", "default-stroke", new BasicStroke(1.0f));
         SELECTED_STROKE = Stylesheet.instance().getStroke("Activity", "selected-stroke", new BasicStroke(2.0f));
         FILL_COLOR = Stylesheet.instance().getColor("Activity", "fill-color", Color.lightGray);
         PEN_COLOR = Stylesheet.instance().getColor("Activity", "pen-color", Color.black);
         NAME_FONT = Stylesheet.instance().getFont("Activity", "name-font", new Font("SansSerif", Font.BOLD, 12));
         MULTIPLE_TRIGGERS_ICON = new ImageIcon(ActivitySymbol.class.getResource("images/activity.gif"));
         MESSAGE_TRIGGER_ICON = new ImageIcon(ActivitySymbol.class.getResource("images/activity.gif"));
         TIMER_TRIGGER_ICON = new ImageIcon(ActivitySymbol.class.getResource("images/activity.gif"));

         trace.debug("Icons initialized.");

         startCircleStroke = new BasicStroke(1.0f);
         endCircleStroke = new BasicStroke(2.0f);

         resourcesInitialized = true;
      }
   }

   public ActivitySymbol()
   {
      super("Activity");
      initializeResources();
//      metricsPanels = new ArrayList();
      // @todo (pwh, ub): retrofit
      // setupMetricsPopup();
   }

   public ActivitySymbol(IActivity activity)
   {
      super("Activity");
      initializeResources();
      setActivity(activity);
//      metricsPanels = new ArrayList();
      // @todo (pwh, ub): retrofit
      // setupMetricsPopup();
   }

   public void actionPerformed(ActionEvent event)
   {
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      ActivitySymbol _copy = new ActivitySymbol(getActivity());
      _copy.setX(getX());
      _copy.setY(getY());

      return _copy;
   }

   public void createPopupMenu()
   {
      super.createPopupMenu();

      JPopupMenu popupMenu = getPopupMenu();
      popupMenu.addSeparator();

      transitionItem = new JMenuItem("Transition");

      transitionItem.addActionListener(this);
      transitionItem.setMnemonic('T');
      popupMenu.add(transitionItem);

      popupMenu.addSeparator();

      openDefaultDiagramItem = new JMenuItem("Open Default Diagram");
      openDefaultDiagramItem.addActionListener(this);
      openDefaultDiagramItem.setMnemonic('o');
      popupMenu.add(openDefaultDiagramItem);

      traverseItem = new JMenuItem("Traverse ...");
      traverseItem.addActionListener(this);
      traverseItem.setMnemonic('v');
      popupMenu.add(traverseItem);

      popupMenu.addSeparator();

      useDataItem = new JMenuItem("Use Data ...");
      popupMenu.add(useDataItem);
      useDataItem.addActionListener(this);

   }

   public void deleteAll()
   {
      if (getActivity() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea()),
            "You are going to delete the activity '" + getActivity().getName() + "'.\n\n"
            + "This operation cannot be undone. Continue?", "Activity Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();
         if (getActivity() != null)
         {
            getActivity().delete();
            setActivity(null);
         }
      }
   }

   public void draw(Graphics g)
   {
      Graphics2D graphics = (Graphics2D) g;

      // Backup old stroke

      Stroke _oldStroke = graphics.getStroke();
      Font oldFont = graphics.getFont();

      ImageIcon _icon = null;
      int _rectangleLeft = getX() + getJoinIconWidth();
      int _rectangleWidth = getWidth() - getJoinIconWidth() - getSplitIconWidth();
      int _rectangleRight = _rectangleLeft + _rectangleWidth;

      try
      {
         if (STYLE.equalsIgnoreCase("bpmn"))
         {
            graphics.setColor(getStartEndLineColor());

            if (getSelected())
            {
               graphics.setStroke(SELECTED_STROKE);
            }
            else
            {
               graphics.setStroke(DEFAULT_STROKE);
            }

            Stroke currentStroke = graphics.getStroke();

            // Draw arrow for start activity

            if (!getActivity().getAllInTransitions().hasNext())
            {
               graphics.drawLine(getX() - START_END_LINE_LENGTH, (getBottom() + getTop()) / 2, getX(), (getBottom() + getTop()) / 2);
               graphics.setStroke(currentStroke);
               graphics.drawArc(getX() - START_END_LINE_LENGTH - 2 * START_CIRCLE_RADIUS, (getBottom() + getTop()) / 2 - START_CIRCLE_RADIUS, 2 * START_CIRCLE_RADIUS, 2 * START_CIRCLE_RADIUS, 0, 360);

               Iterator triggers = getActivity().getProcessDefinition().getAllTriggers();

               IconProvider iconProvider = SymbolIconProvider.instance();
               if (triggers.hasNext())
               {
                  ITrigger trigger = (ITrigger) triggers.next();

                  ImageIcon icon;

                  if (triggers.hasNext())
                  {
                     icon = MULTIPLE_TRIGGERS_ICON;
                  }
                  else
                  {
                     icon = iconProvider.getIcon(trigger);
                  }

                  graphics.drawImage(icon.getImage(), getX() - START_END_LINE_LENGTH - START_CIRCLE_RADIUS - icon.getIconWidth() / 2,
                        getY() + icon.getIconHeight() / 2, null);
               }

               graphics.setStroke(currentStroke);
            }

            // Draw arrow for end activity

            if (!getActivity().getAllOutTransitions().hasNext())
            {
               graphics.drawLine(getRight(), (getBottom() + getTop()) / 2, getRight() + START_END_LINE_LENGTH, (getBottom() + getTop()) / 2);
               graphics.setStroke(endCircleStroke);
               graphics.drawArc(getRight() + START_END_LINE_LENGTH, (getBottom() + getTop()) / 2 - END_CIRCLE_RADIUS, 2 * END_CIRCLE_RADIUS, 2 * END_CIRCLE_RADIUS, 0, 360);
               graphics.setStroke(currentStroke);
            }

            // Draw the border

            int radius = EDGE_RADIUS;
            int diameter = 2 * radius;

            int left = getX();
            int right = getX() + LEFT_MARGIN + getNameSymbol().getWidth() + RIGHT_MARGIN;
            int top = getY();
            int bottom = getY() + TOP_MARGIN + getNameSymbol().getHeight() + BOTTOM_MARGIN;

            // Fill rectangle

            graphics.setColor(FILL_COLOR);

            graphics.fillRect(left + radius, top, Math.abs(right - left) - diameter, Math.abs(top - bottom));
            graphics.fillRect(left, top + radius, Math.abs(right - left), Math.abs(top - bottom) - diameter);
            graphics.fillArc(left, bottom - diameter, diameter, diameter, 180, 90);
            graphics.fillArc(right - diameter, bottom - diameter, diameter, diameter, 270, 90);
            graphics.fillArc(right - diameter, top, diameter, diameter, 0, 90);
            graphics.fillArc(left, top, diameter, diameter, 90, 90);

            // Draw borders

            graphics.setColor(PEN_COLOR);

            graphics.drawLine(left, bottom - radius, left, top + radius);
            graphics.drawLine(left + radius, top, right - radius, top);
            graphics.drawLine(right, top + radius, right, bottom - radius);
            graphics.drawLine(right - radius, bottom, left + radius, bottom);

            if (radius != 0)
            {
               graphics.drawArc(left, bottom - diameter, diameter, diameter, 180, 90);
               graphics.drawArc(right - diameter, bottom - diameter, diameter, diameter, 270, 90);
               graphics.drawArc(right - diameter, top, diameter, diameter, 0, 90);
               graphics.drawArc(left, top, diameter, diameter, 90, 90);
            }

            // Draw the name

            getNameSymbol().setFont(NAME_FONT);
            getNameSymbol().setPoint(left + LEFT_MARGIN, top + TOP_MARGIN);
            getNameSymbol().draw(graphics);

            graphics.setFont(oldFont);

            // Draw the icon for the implementation type

            _icon = getImplementationTypeImage();

            int iconX = getX() + (getWidth() - _icon.getIconWidth()) / 2;
            int iconY = getY() + TOP_MARGIN + getNameSymbol().getHeight() + BOTTOM_MARGIN - _icon.getIconHeight();

            if (_icon != null)
            {
               graphics.drawImage(_icon.getImage(), iconX, iconY, null);
            }

            // Draw the icon for the loop type

            _icon = getLoopTypeImage();

            if (_icon != null)
            {
               graphics.drawImage(_icon.getImage(), _rectangleRight - _icon.getIconWidth() - 4, getTop() + 4, null);
            }
         }
         else
         {
            graphics.setColor(getStartEndLineColor());

            if (getSelected())
            {
               graphics.setStroke(selectedStroke);
            }

            // Draw arrow for start activity

            if (!getActivity().getAllInTransitions().hasNext())
            {
               graphics.drawLine(getX() - START_END_LINE_LENGTH, (getBottom() + getTop()) / 2, getX(), (getBottom() + getTop()) / 2);
               graphics.fillPolygon(new int[]{getX() - START_END_LINE_LENGTH, getX() - START_END_LINE_LENGTH - START_END_TRIANGLE_WIDTH, getX() - START_END_LINE_LENGTH - START_END_TRIANGLE_WIDTH}, new int[]{(getBottom() + getTop()) / 2, (getBottom() + getTop()) / 2 + START_END_TRIANGLE_HEIGHT, (getBottom() + getTop()) / 2 - START_END_TRIANGLE_HEIGHT}, 3);
            }

            // Draw arrow for end activity

            if (!getActivity().getAllOutTransitions().hasNext())
            {
               graphics.drawLine(getRight(), (getBottom() + getTop()) / 2, getRight() + START_END_LINE_LENGTH, (getBottom() + getTop()) / 2);
               graphics.fillPolygon(new int[]{getRight() + START_END_LINE_LENGTH, getRight() + START_END_LINE_LENGTH + START_END_TRIANGLE_WIDTH, getRight() + START_END_LINE_LENGTH + START_END_TRIANGLE_WIDTH}, new int[]{(getBottom() + getTop()) / 2, (getBottom() + getTop()) / 2 + START_END_TRIANGLE_HEIGHT, (getBottom() + getTop()) / 2 - START_END_TRIANGLE_HEIGHT}, 3);
            }

            // Reset to the thin stroke

            graphics.setStroke(_oldStroke);

            // Draw the rectangle

            graphics.setColor(getBackgroundColor());
            graphics.fillRect(_rectangleLeft, getY(), _rectangleWidth, getHeight());

            // Draw the border

            graphics.setColor(getBorderHighlightColor());
            graphics.drawRect(_rectangleLeft + 1, getY() + 1, _rectangleWidth - 4, getHeight() - 4);

            graphics.setColor(getBorderShadowColor());
            graphics.drawLine(_rectangleLeft + 2, getBottom() - 4, _rectangleLeft + 2, getTop() + 2);
            graphics.drawLine(_rectangleLeft + 2, getTop() + 2, _rectangleRight - 4, getTop() + 2);
            graphics.drawLine(_rectangleRight - 2, getTop() + 1, _rectangleRight - 2, getBottom() - 2);
            graphics.drawLine(_rectangleRight - 2, getBottom() - 2, _rectangleLeft + 2, getBottom() - 2);

            // Draw the name

            getNameSymbol().setPoint(_rectangleLeft + MARGIN, getY() + MARGIN);
            getNameSymbol().draw(graphics);

            // Draw the icon for the implementation type

            _icon = getImplementationTypeImage();

            if (_icon != null)
            {
               graphics.drawImage(_icon.getImage(), _rectangleRight - _icon.getIconWidth() - 10, getTop() + 10, null);
            }

            // Draw the icon for the loop type

            _icon = getLoopTypeImage();

            if (_icon != null)
            {
               graphics.drawImage(_icon.getImage(), _rectangleRight - _icon.getIconWidth() - 4, getTop() + 4, null);
            }

            // Draw symbol for join type

            _icon = getJoinTypeImage();

            if (_icon != null)
            {
               graphics.drawImage(_icon.getImage(), getX(), getTop() + ((getHeight() - _icon.getIconHeight()) / 2), null);
            }

            // Draw symbol for split type

            _icon = getSplitTypeImage();

            if (_icon != null)
            {
               graphics.drawImage(_icon.getImage(), _rectangleRight, getTop() + ((getHeight() - _icon.getIconHeight()) / 2), null);
            }
         }
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
      finally
      {
         // Reset to old stroke

         graphics.setStroke(_oldStroke);
         graphics.setFont(oldFont);
      }
   }

   /**
    * Show and edit the Properties
    */
   protected void editProperties()
   {
   }

   public IActivity getActivity()
   {
      return (IActivity) getUserObject();
   }

   /**
    * @return java.awt.Color
    */
   protected Color getBackgroundColor()
   {
      switch (getDrawArea().getDecorationStrategy().getSymbolStyle(this))
      {
         //         case DecorationStrategy.STYLE_ACTIVE: return CI.RED;
         case DecorationStrategy.STYLE_TRAVERSED:
            return CI.GREY;
         default:
            return CI.LIGHTGREY;
      }
   }

   /**
    * Return the Color that is used for the highlight of the border
    *
    * @return java.awt.Color
    */
   protected Color getBorderHighlightColor()
   {
      if (getSelected())
      {
         return getBorderShadowColor().brighter();
      }
      else
      {
         return getBorderShadowColor().brighter().brighter();
      }
   }

   /**
    * return the Color that is used for the shadow of the border
    *
    * @return java.awt.Color
    */
   protected Color getBorderShadowColor()
   {
      if (getDrawArea().getDecorationStrategy()
            .getSymbolStyle(this) == DecorationStrategy.STYLE_ACTIVE)
      {
         return CI.RED;
      }
      else if (getSelected())
      {
         return getBackgroundColor().darker().darker();
      }
      else
      {
         return getBackgroundColor().darker();
      }
   }

   /**
    * return the Color that is used for the start-end-line and the triangle
    *
    * @return java.awt.Color
    */
   protected Color getStartEndLineColor()
   {
      return CI.BLUE;
   }

   public int getHeight()
   {
      if (STYLE.equalsIgnoreCase("carnot"))
      {
         return MARGIN + getNameSymbol().getHeight() + MARGIN;
      }
      else
      {
         return TOP_MARGIN + getNameSymbol().getHeight() + BOTTOM_MARGIN;
      }
   }

   /**
    * Returns the width of the image for the current split type or 0 if there is
    * not such image.
    */
   protected int getSplitIconWidth()
   {
      ImageIcon _icon = getSplitTypeImage();
      if (_icon != null)
      {
         return _icon.getIconWidth();
      }
      else
      {
         return 0;
      }
   }

   /**
    * returns the width of the image for the current join type or 0 if there is
    * not such image
    */
   protected int getJoinIconWidth()
   {
      ImageIcon _icon = getJoinTypeImage();

      if (_icon != null)
      {
         return _icon.getIconWidth();
      }
      else
      {
         return 0;
      }
   }

   protected ImageIcon getImplementationTypeImage()
   {
      Assert.isNotNull(getActivity(), "activity is not null");
      try
      {
         ImplementationType type = getActivity().getImplementationType();

         if (type == ImplementationType.Manual)
         {
            if (iconManual == null)
            {
               iconManual = new ImageIcon(getClass().getResource("images/activity_manual.gif"));
            }
            return iconManual;
         }
         else if (type == ImplementationType.Application)
         {
            if (iconApplication == null)
            {
               iconApplication = new ImageIcon(getClass().getResource("images/activity_application.gif"));
            }
            return iconApplication;
         }
         else if (type == ImplementationType.SubProcess)
         {
            if (iconSubProcess == null)
            {
               iconSubProcess = new ImageIcon(getClass().getResource("images/activity_subprocess.gif"));
            }
            return iconSubProcess;
         }
         else if (type == ImplementationType.Route)
         {
            if (iconRoute == null)
            {
               iconRoute = new ImageIcon(getClass().getResource("images/activity_route.gif"));
            }
            return iconRoute;
         }
         Assert.lineNeverReached();
         return null;
      }
      catch (Exception e)
      {
         trace.warn("", e);
         return null;
      }
   }

   protected ImageIcon getJoinTypeImage()
   {
      Assert.isNotNull(getActivity(), "activity is null");
      try
      {
         if (getActivity().getJoinType() == JoinSplitType.Xor)
         {
            if (iconJoinXOR == null)
            {
               iconJoinXOR =
                     new ImageIcon(getClass().getResource("images/activity_join_xor.gif"));
            }
            return iconJoinXOR;
         }
         else if (getActivity().getJoinType() == JoinSplitType.And)

         {
            if (iconJoinAND == null)
            {
               iconJoinAND =
                     new ImageIcon(getClass().getResource("images/activity_join_and.gif"));
            }
            return iconJoinAND;
         }
         else
         {
            return null;
         }
      }
      catch (Exception e)
      {
         trace.warn("", e);
         return null;
      }
   }

   protected ImageIcon getLoopTypeImage()
   {
      Assert.isNotNull(getActivity(), "activity is not null");

      try
      {
         if ((null != getActivity().getLoopType())
               && (!LoopType.None.equals(getActivity().getLoopType())))
         {
            if (iconLoop == null)
            {
               iconLoop = new ImageIcon(getClass().getResource("images/activity_loop.gif"));
            }
            return iconLoop;
         }
         else
         {
            return null;
         }
      }
      catch (Exception _ex)
      {
         return null;
      }
   }

   public ImageIcon getSplitTypeImage()
   {
      Assert.isNotNull(getActivity(), "activity is not null");
      try
      {
         if (getActivity().getSplitType() == JoinSplitType.Xor)
         {
            if (iconSplitXOR == null)
            {
               iconSplitXOR =
                     new ImageIcon(getClass().getResource("images/activity_split_xor.gif"));
            }
            return iconSplitXOR;
         }
         else if (getActivity().getSplitType() == JoinSplitType.And)
         {
            if (iconSplitAND == null)
            {
               iconSplitAND =
                     new ImageIcon(getClass().getResource("images/activity_split_and.gif"));
            }
            return iconSplitAND;
         }
         else
         {
            return null;
         }
      }
      catch (Exception e)
      {
         trace.warn("", e);
         return null;
      }
   }

   public int getWidth()
   {
      if (STYLE.equalsIgnoreCase("carnot"))
      {
         return getJoinIconWidth() + MARGIN + getTextWidth() + MARGIN + ICON_WIDTH + MARGIN + getSplitIconWidth();
      }
      else
      {
         return LEFT_MARGIN + getTextWidth() + RIGHT_MARGIN;
      }
   }

   private int getTextWidth()
   {
      if (minSize == 0)
      {
         minSize = Toolkit.getDefaultToolkit().getFontMetrics(NAME_FONT).stringWidth("Activity88");
      }
      return Math.max(minSize, getNameSymbol().getWidth());
   }

   /*
    * Called before a popup menu is activated and is used to enable or
    * disable menu items according to the state of the activity.
    */
   public void preparePopupMenu()
   {
      Assert.isNotNull(getActivity(), "activity is not null");

      super.preparePopupMenu();

      boolean isWritable = (getDrawArea() != null) && (!getDrawArea().isReadOnly());

      ImplementationType type = getActivity().getImplementationType();

      transitionItem.setEnabled(isWritable &&
            (!(getActivity().getSplitType().equals(JoinSplitType.None))
            || !(getActivity().getAllOutTransitions().hasNext())));
      openDefaultDiagramItem.setEnabled((type.equals(
            ImplementationType.SubProcess)));
      traverseItem.setEnabled(isWritable);

      if (type == ImplementationType.Manual || type == ImplementationType.Application)
      {
         useDataItem.setEnabled(true);
      }
      else
      {
         useDataItem.setEnabled(false);
      }
   }

   public void setActivity(IActivity activity)
   {
      setUserObject(activity);
   }

   public boolean setPoint(int x, int y)
   {
      if (getUserObject() == null)
      {
         IProcessDefinition pd = (IProcessDefinition) getDrawArea().getUserObject();
         String id = pd.getDefaultActivityId();
         setActivity(pd.createActivity(id, id, "", 0));
      }
      return super.setPoint(x, y);
   }

   public void setX(int x)
   {
      super.setX(x);

      // @todo (pwh, ub): retrofit
      /*
      // Distribute metrics panels
      for (int n = 0; n < metricsPanels.size(); ++n)
      {
         MetricsPanel _panel = (MetricsPanel) metricsPanels.get(n);
         _panel.setBounds(getX() + 30, getY() + 30 + ((5 + MetricsPanel.HEIGHT) * n), _panel.getPreferredSize().width, _panel.getPreferredSize().height);
      }
      */
   }

   public void setY(int y)
   {
      super.setY(y);

      // @todo (pwh, ub): retrofit
      /*
      // Distribute metrics panels
      for (int n = 0; n < metricsPanels.size(); ++n)
      {
         MetricsPanel _panel = (MetricsPanel) metricsPanels.get(n);
         _panel.setBounds(getX() + 30, getY() + 30 + ((5 + MetricsPanel.HEIGHT) * n), _panel.getPreferredSize().width, _panel.getPreferredSize().height);
      }
      */
   }

   // @todo (pwh, ub): retrofit
   /*
   public MetricsPanel activateChartPanel(DurationMetrics durationMetrics, String processId, String activityId)
   {
      //only one panel per symbol is allowed at the moment
      for (int n = 0; n < metricsPanels.size(); ++n)
      {
         if (metricsPanels.get(n) != null)
         {
            return null;
         }
      }

      MetricsChart _panel = new DurationMetricsChart(durationMetrics, processId, activityId);
      activatePanel(_panel);
      return _panel;
   }

   public MetricsPanel activateFrequencyChartPanel(XYPlotModel xyPlot, String processId, String activityId)
   {
      //only one panel per symbol is allowed at the moment
      for (int n = 0; n < metricsPanels.size(); ++n)
      {
         if (metricsPanels.get(n) != null)
         {
            return null;
         }
      }

      MetricsChart _panel = new FrequencyMetricsChart(xyPlot, processId, activityId);
      activatePanel(_panel);
      return _panel;
   }

   public MetricsPanel activateCostChartPanel(XYPlotModel xyPlot, String processId, String activityId)
   {
      //only one panel per symbol is allowed at the moment
      for (int n = 0; n < metricsPanels.size(); ++n)
      {
         if (metricsPanels.get(n) != null)
         {
            return null;
         }
      }

      MetricsChart _panel = new CostMetricsChart(xyPlot, processId, activityId);
      activatePanel(_panel);
      return _panel;
   }

   public void activatePanel(MetricsChart _panel)
   {
      MouseListener mouseListener = new MetricsMouseListener(this);
      _panel.addMouseListener(mouseListener);
      JCChart chart = _panel.getChart();
      chart.addMouseListener(mouseListener);
      _panel.setParent(this);

      metricsPanels.add(_panel);
      setX(getX());
      setY(getY());
      getDrawArea().add(_panel);
      getDrawArea().validate();
   }

   public MetricsPanel activateScalarMetricsPanel(java.util.List scalarActivityMetrics, String processId, String activityId)
   {
      //only one panel per symbol is allowed at the moment
      for (int n = 0; n < metricsPanels.size(); ++n)
      {
         if (metricsPanels.get(n) != null)
         {
            return null;
         }
      }

      MetricsPanel _panel = new ScalarMetricsPanel(scalarActivityMetrics, processId, activityId);
      _panel.addMouseListener(new MetricsMouseListener(this));
      _panel.setParent(this);

      metricsPanels.add(_panel);
      setX(getX());
      setY(getY());
      getDrawArea().add(_panel);
      getDrawArea().validate();

      return _panel;
   }

   public void deactivateMetricsPanel()
   {
      for (int n = 0; n < metricsPanels.size(); ++n)
      {
         MetricsPanel _panel = (MetricsPanel) metricsPanels.get(n);

         metricsPanels.remove(_panel);
         getDrawArea().remove(_panel);
         getDrawArea().validate();
      }
      getDrawArea().repaint();
   }
*/
   /**
    * prepare and show popupmenu (invoked by metricsmouselistener)
    */
  // @todo (pwh, ub): retrofit
   /*
   public void showPopup(Component component, int x, int y)
   {
      Component c;
      if (component.getClass().isAssignableFrom(JCChart.class))
      {
         c = component.getParent();
      }
      else
      {
         c = component;
      }

      if (c.getClass().isAssignableFrom(DurationMetricsChart.class))
      {
         toggleDataItem.setEnabled(true);
         DurationMetricsChart chart = (DurationMetricsChart) c;
         initDurationItem.setState(chart.getModelSeriesState(DurationMetricsChart.INITDURATION));
         activeDurationItem.setState(chart.getModelSeriesState(DurationMetricsChart.ACTIVEDURATION));
         waitingDurationItem.setState(chart.getModelSeriesState(DurationMetricsChart.WAITINGDURATION));
      }
      else
      {
         toggleDataItem.setEnabled(false);
      }

      MetricsPanel panel = (MetricsPanel) c;
      sizeItem.setState(panel.isLarge());

      metricsPopupMenu.show(c, x, y);
      this.metricsPopupRootPanel = panel;
   }

   public void setupMetricsPopup()
   {
      metricsPopupMenu = new JPopupMenu();

      deleteFrameItem = new JMenuItem("Remove Metric");
      deleteFrameItem.addActionListener(this);
      deleteFrameItem.setMnemonic('r');
      metricsPopupMenu.add(deleteFrameItem);

      sizeItem = new JCheckBoxMenuItem("Enlarge Metric");
      sizeItem.addActionListener(this);
      sizeItem.setMnemonic('s');
      metricsPopupMenu.add(sizeItem);

      toggleDataItem = new JMenu("Toggle Data Series");
      toggleDataItem.addActionListener(this);
      toggleDataItem.setMnemonic('t');
      metricsPopupMenu.add(toggleDataItem);
      initDurationItem = new JCheckBoxMenuItem("Init Duration");
      initDurationItem.addActionListener(this);
      initDurationItem.setMnemonic('i');
      toggleDataItem.add(initDurationItem);
      activeDurationItem = new JCheckBoxMenuItem("Active Duration");
      activeDurationItem.addActionListener(this);
      activeDurationItem.setMnemonic('a');
      toggleDataItem.add(activeDurationItem);
      waitingDurationItem = new JCheckBoxMenuItem("Waiting Duration");
      waitingDurationItem.setMnemonic('w');
      waitingDurationItem.addActionListener(this);
      toggleDataItem.add(waitingDurationItem);

   }

   public void metricsPopupActionPerformed(ActionEvent e)
   {
      if (e.getActionCommand() == deleteFrameItem.getActionCommand())
      {
         if (metricsPopupRootPanel != null)
         {
            metricsPopupRootPanel.getDisplayedMetric().deleteViews();
         }
      }

      if (e.getActionCommand() == sizeItem.getActionCommand())
      {
         if (metricsPopupRootPanel != null)
         {
            metricsPopupRootPanel.toggleSize();
         }
         setX(getX());
         setY(getY());
         getDrawArea().validate();

      }

      if (e.getActionCommand() == initDurationItem.getActionCommand())
      {
         DurationMetricsChart chart = (DurationMetricsChart) (metricsPopupRootPanel);
         if (chart.getModelSeriesState(DurationMetricsChart.INITDURATION)) //active
         {
            chart.setModelSeriesInactive(DurationMetricsChart.INITDURATION);
         }
         else
         {
            chart.setModelSeriesActive(DurationMetricsChart.INITDURATION);
         }
      }

      if (e.getActionCommand() == activeDurationItem.getActionCommand())
      {
         DurationMetricsChart chart = (DurationMetricsChart) (metricsPopupRootPanel);
         if (chart.getModelSeriesState(DurationMetricsChart.ACTIVEDURATION)) //active
         {
            chart.setModelSeriesInactive(DurationMetricsChart.ACTIVEDURATION);
         }
         else
         {
            chart.setModelSeriesActive(DurationMetricsChart.ACTIVEDURATION);
         }
      }

      if (e.getActionCommand() == waitingDurationItem.getActionCommand())
      {
         DurationMetricsChart chart = (DurationMetricsChart) (metricsPopupRootPanel);
         if (chart.getModelSeriesState(DurationMetricsChart.WAITINGDURATION)) //active
         {
            chart.setModelSeriesInactive(DurationMetricsChart.WAITINGDURATION);
         }
         else
         {
            chart.setModelSeriesActive(DurationMetricsChart.WAITINGDURATION);
         }
      }

   }
*/
   public void addConnectionsFromModel()
   {
      Symbol searchedSymbol = null;
      ConnectionSymbol _addedConnection = null;
      IApplication _application;
      Iterator iterator;
      ITransition _transition;
      IModelParticipant _participant;
      IActivity activity = getActivity();
      if (activity != null)
      {
         // check for connections to Application
         _application = activity.getApplication();
         if (_application != null)
         {
            searchedSymbol = getDiagram().findSymbolForUserObject(_application);

            if ((searchedSymbol != null) && (!getDiagram().existConnectionBetween(searchedSymbol, this, ExecutedByConnection.class, true)))
            {
               _addedConnection = new ExecutedByConnection((ApplicationSymbol) searchedSymbol);
               _addedConnection.setSecondSymbol(this, false);
               getDiagram().addToConnections(_addedConnection, 0);
               //                    getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
            }
         }

         // check for out transitions
         iterator = activity.getAllOutTransitions();
         if (iterator != null)
         {
            while (iterator.hasNext())
            {
               _transition = (ITransition) iterator.next();
               searchedSymbol = getDiagram().findSymbolForUserObject(_transition.getToActivity());
               if (searchedSymbol != null)
               {
                  boolean found = false;
                  Iterator itr = getDiagram().getExistingConnectionsBetween(this, searchedSymbol, TransitionConnection.class, true);
                  while (itr.hasNext())
                  {
                     TransitionConnection existingConnection = (TransitionConnection) itr.next();
                     if (existingConnection.getUserObject().equals(_transition))
                     {
                        found = true;
                        break;
                     }
                  }
                  if (!found)
                  {
                     _addedConnection = new TransitionConnection(this);
                     _addedConnection.setSecondSymbol(searchedSymbol, false);
                     _addedConnection.setUserObject(_transition);
                     getDiagram().addToConnections(_addedConnection, 0);
                     //getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
                  }
               }
            }
         }

         // todo (fh) do we need to check the in transitions ?
         // todo if yes, then should we make the same check as with out transitions ?
         // check for in transitions
         iterator = activity.getAllInTransitions();
         if (iterator != null)
         {
            while (iterator.hasNext())
            {
               _transition = (ITransition) iterator.next();
               searchedSymbol = getDiagram().findSymbolForUserObject(_transition.getFromActivity());

               if ((searchedSymbol != null) && (!getDiagram().existConnectionBetween(searchedSymbol, this, TransitionConnection.class, true)))
               {
                  _addedConnection = new TransitionConnection((ActivitySymbol) searchedSymbol);
                  _addedConnection.setSecondSymbol(this, false);
                  _addedConnection.setUserObject(_transition);
                  getDiagram().addToConnections(_addedConnection, 0);
                  //getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
               }
            }
         }

         // data mapping connections
         updateDataMappingConnections();

         // check for performs by
         _participant = activity.getPerformer();
         if (_participant != null)
         {
            searchedSymbol = getDiagram().findSymbolForUserObject(_participant);
            if ((searchedSymbol != null) && (!getDiagram().existConnectionBetween(searchedSymbol, this, PerformsConnection.class, true)))
            {
               if (searchedSymbol instanceof RoleSymbol)
               {
                  _addedConnection = new PerformsConnection((RoleSymbol) searchedSymbol);
               }
               else if (searchedSymbol instanceof OrganizationSymbol)
               {
                  _addedConnection = new PerformsConnection((OrganizationSymbol) searchedSymbol);
               }
               else if (searchedSymbol instanceof ConditionalPerformerSymbol)
               {
                  _addedConnection = new PerformsConnection((ConditionalPerformerSymbol) searchedSymbol);
               }
               else if (searchedSymbol instanceof ModelerSymbol)
               {
                  _addedConnection = new PerformsConnection((ModelerSymbol) searchedSymbol);
               }
               else
               {
                  Assert.lineNeverReached();
               }
               _addedConnection.setSecondSymbol(this, false);
               getDiagram().addToConnections(_addedConnection, 0);
               //getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
            }
         }
      }
   }

   public void updateDataMappingConnections()
   {
      Iterator iterator = getDiagram().getAllNodes(DataSymbol.class);
      while (iterator.hasNext())
      {
         updateDataMappingConnection((DataSymbol) iterator.next());
      }
   }

   public void updateDataMappingConnection(DataSymbol dataSymbol)
   {
      IData data = dataSymbol.getData();
      Direction direction = DataMappingConnection.getConnectionDirection(
            getActivity(), data);
      DataMappingConnection connection = null;
      Iterator it = getDiagram().getExistingConnectionsBetween(
            this, dataSymbol, DataMappingConnection.class, false);
      if (it.hasNext())
      {
         connection = (DataMappingConnection) it.next();
      }
      if (direction == null)
      {
         if (connection != null)
         {
            connection.delete();
         }
      }
      else
      {
         if (connection == null)
         {
            connection = new DataMappingConnection(this);
            connection.setSecondSymbol(dataSymbol, false);
            getDiagram().addToConnections(connection, 0);
         }
         connection.updateDirection(direction);
      }
      if (connection != null)
      {

         if (DataMappingConnection.hasExceptionHandlers(getActivity(), data))
         {
            connection.setColor(CI.RED);
         }
         else
         {
            connection.setColor(null);
         }
      }
   }

   public String toString()
   {
      return "Activity Symbol for " + getUserObject();
   }
}
