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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.*;

import org.eclipse.stardust.common.error.InternalException;


/** */
public class ImageEntry extends JLabel
      implements ActionListener
{
   protected static ImageIcon defaultIcon = new ImageIcon(GUI.class.getResource("images/search.gif"));

   protected byte[] imageData;
   protected FileDialog fileDialog;
   protected Dimension size;

   protected JMenuItem openItem;
   protected JMenuItem saveItem;
   protected JMenuItem delItem;

   public ImageEntry()
   {
      imageData = null;

      setHorizontalAlignment(CENTER);
      setAlignmentX(0.0f);
      setAlignmentY(0.5f);

      openItem = new JMenuItem("Load File ...");
      saveItem = new JMenuItem("Save As ...");
      delItem = new JMenuItem("Delete");

      openItem.addActionListener(this);
      saveItem.addActionListener(this);
      delItem.addActionListener(this);

      JPopupMenu popupMenu = new JPopupMenu();

      popupMenu.add(openItem);
      popupMenu.add(saveItem);
      popupMenu.add(delItem);

      PopupAdapter.create(this, popupMenu);

      setIcon(defaultIcon);
   }

   public void setValue(byte[] imageData)
   {
      invalidate();

      if (imageData == null ||
            imageData.length == 0)
      {
         setIcon(defaultIcon);

         return;
      }

      this.imageData = imageData;

      // create new image icon

      Toolkit toolkit = Toolkit.getDefaultToolkit();

      ImageIcon imageIcon = new ImageIcon(toolkit.createImage(imageData));

      // change the observer for animations

      if (getIcon() != null)
      {
         ((ImageIcon) getIcon()).setImageObserver(null);
      }

      imageIcon.setImageObserver(this);

      // set label icon

      setIcon(imageIcon);

      // redraw

      setEnabled(true);
      validate();
      repaint();
   }

   public byte[] getValue()
   {
      return imageData;
   }

   public void setIcon(ImageIcon imageIcon)
   {
      super.setIcon(imageIcon);

      size = new Dimension(imageIcon.getIconWidth(),
            imageIcon.getIconHeight());
   }

   public void setDefaultIcon()
   {
      setIcon(defaultIcon);
   }

   public void deleteImage()
   {
      imageData = null;

      setDefaultIcon();
   }

   public void loadImage()
   {
      String filename = null;
      String directory = null;

      Container container = getTopLevelAncestor();
      Frame frame = null;

      if (container == null)
      {
         throw new InternalException("Using ImageEntry in an application without top level anchestor.");
      }
      else if (container instanceof Frame)
      {
         frame = (Frame) container;
      }
      else if (container instanceof Window)
      {
         Window owner = ((Window) container).getOwner();

         // Lookup top level window recursively - must be a frame

         while (!(owner == null || Frame.class.isInstance(owner)))
         {
            owner = owner.getOwner();
         }

         if (owner == null)
         {
            throw new InternalException("Using ImageEntry in an application without root frame.");
         }

         frame = (Frame) owner;
      }

      if (fileDialog == null)
      {
         fileDialog = new FileDialog(frame, "Bilddatei öffnen", FileDialog.LOAD);
      }

      // @todo: Filename Filter setzen!!
      //	setFilenameFilter("*.gif");

      fileDialog.show();

      filename = fileDialog.getFile();

      if (filename == null)
      {
         return;
      }

      directory = fileDialog.getDirectory();

      try
      {
         File file = new File(directory + filename);

         FileInputStream in = new FileInputStream(file);

         int length1 = in.available();

         byte[] array = new byte[length1];

         int length2 = in.read(array);

         in.close();

         if (length1 != length2)
         {
            throw new IOException("Error reading " + filename);
         }

         setValue(array);

         frame.setVisible(false);
         frame.pack();
         frame.show();
      }
      catch (IOException exception)
      {
         JOptionPane.showMessageDialog(this,
               "Can't load image '" + filename + "'.",
               "Error",
               JOptionPane.ERROR_MESSAGE);
      }
   }

   public void saveImage()
   {
      JFrame frame = (JFrame) this.getTopLevelAncestor();

      if (frame == null)
      {
         return;
      }

      fileDialog = new FileDialog(frame, "Open File", FileDialog.SAVE);
      fileDialog.show();

      String file = fileDialog.getFile();

      if (file != null)
      {
         String directory = fileDialog.getDirectory();
         /***
          if (image.saveImage(directory + file) == false)
          {
          JOptionPane.showMessageDialog(this,
          e.getMessage(),
          "Fehler",
          JOptionPane.ERROR_MESSAGE);
          }
          ***/
      }
   }

   /** replacement for deprecated methods: enable()/ disable()*/
   public void setEnabled(boolean enabled)
   {
      openItem.setEnabled(enabled);

      if (enabled == false || imageData == null)
      {
         saveItem.setEnabled(false);
         delItem.setEnabled(false);
      }
      else // enabled == true && iamgeData != null
      {
         saveItem.setEnabled(true);
         delItem.setEnabled(true);
      }
   }

   /*
   public void enable()
   {
      openItem.setEnabled(true);

      if (imageData == null)
      {
         saveItem.setEnabled(false);
         delItem.setEnabled(false);
      }
      else
      {
         openItem.setEnabled(true);
         saveItem.setEnabled(true);
      }
   }
   public void disable()
   {
      openItem.setEnabled(false);
      saveItem.setEnabled(false);
      delItem.setEnabled(false);
   }*/
   public Dimension getMinimumSize()
   {
      return getPreferredSize();
   }

   public Dimension getMaximumSize()
   {
      return getPreferredSize();
   }

   public Dimension getPreferredSize()
   {
      return size;
   }

   /** */
   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == openItem)
      {
         loadImage();
      }
      else if (event.getSource() == saveItem)
      {
         saveImage();
      }
      else if (event.getSource() == delItem)
      {
      }
   }
}

