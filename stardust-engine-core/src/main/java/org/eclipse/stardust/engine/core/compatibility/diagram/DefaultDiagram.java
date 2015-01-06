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
package org.eclipse.stardust.engine.core.compatibility.diagram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.SplicingIterator;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.utils.Connections;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementBean;


public class DefaultDiagram extends ModelElementBean implements Diagram
{
   private static final long serialVersionUID = 1L;

   static protected int jpgInsetLeft = 50;
   static protected int jpgInsetRigth = 50;
   static protected int jpgInsetTop = 25;
   static protected int jpgInsetBottom = 25;

   private static ImageIcon icon;

   private String name;
   private Link nodes = new Link(this, "Symbols");
   private Connections connections = new Connections(this, "Connections", "outConnections", "inConnections");
   private int xTranslate;
   private int yTranslate;
   private double scale;

   private transient DrawArea drawArea;

   DefaultDiagram()
   {
   }

   public DefaultDiagram(String name)
   {
      this.name = name;
   }

   /**
    * Returns true if the symbol is contained in the diagram otherwise
    * false is returned.
    */
   public boolean contains(NodeSymbol symbol)
   {
      return nodes.contains(symbol);
   }

   public void addToNodes(NodeSymbol symbol, int elementOID)
   {
      if (nodes.contains(symbol) == false)
      {
         nodes.add(symbol);
      }
      symbol.register(elementOID);
   }

   public void addToConnections(ConnectionSymbol connection, int elementOID)
   {
      if (connections.contains(connection) == false)
      {
         connections.add(connection);
      }
      connection.register(elementOID);
   }

   public void addToNodes(NodeSymbol symbol)
   {
      if (nodes.contains(symbol) == false)
      {
         nodes.add(symbol);
      }
   }

   public Iterator getAllConnections(Class type)
   {
      return connections.iterator(type);
   }

   /**
    * Returns <code>true</code> if at least one connection exist between
    * the two symbols
    */
   public boolean existConnectionBetween(NodeSymbol symbol1, NodeSymbol symbol2)
   {
      return existConnectionBetween(symbol1, symbol2, Object.class, false);
   }

   /**
    * Returns <code>true</code> if at least one connection exist between
    * the two symbols. The connection must be a instance of the class <code>connectionType</code>.
    * If the parameter <code>uniDirectional</code> is <code>true</code> only
    * connection from <code>symbol1</code> to <code>symbol2</code> will be found.
    */
   public boolean existConnectionBetween(Symbol symbol1, Symbol symbol2
         , Class connectionType
         , boolean uniDirectional)
   {
      if (getExistingConnectionsBetween(symbol1, symbol2, connectionType, uniDirectional).hasNext())
      {
         return true;
      }
      return false;
   }

   /**
    * Returns <code>true</code> if at least one connection exist between
    * the two symbols. The connection must be a instance of the class <code>connectionType</code>.
    * If the parameter <code>uniDirectional</code> is <code>true</code> only
    * connection from <code>symbol1</code> to <code>symbol2</code> will be found.
    */
   public Iterator getExistingConnectionsBetween(final Symbol symbol1, final Symbol symbol2
         , final Class connectionType
         , final boolean uniDirectional)
   {
      return new FilteringIterator(getAllConnections(), new Predicate()
      {
         public boolean accept(Object o)
         {
            ConnectionSymbol _connection = (ConnectionSymbol) o;
            if ((_connection.getFirstSymbol() != null)
                  && (_connection.getSecondSymbol() != null)
                  && (connectionType.isAssignableFrom(_connection.getClass()))
            )
            {
               if (((_connection.getFirstSymbol() == symbol1)
                     && (_connection.getSecondSymbol() == symbol2)
                     )
                     || ((uniDirectional == false)
                     && (_connection.getFirstSymbol() == symbol2)
                     && (_connection.getSecondSymbol() == symbol1)
                     ))
               {
                  return true;
               }
            }
            return false;
         }

      });
   }

   /**
    * Dumps the diagram to a JPEG file.
    * The method calculates the smallest size to fit the diagram to the JPEG
    */
   public void dumpToJPEGFile(String filePath)
   {
      // Determine size of diagram

      int left = Integer.MAX_VALUE;
      int right = Integer.MIN_VALUE;
      int top = Integer.MAX_VALUE;
      int bottom = Integer.MIN_VALUE;

      Iterator _symbolWalker = getAllNodeSymbols();
      while (_symbolWalker.hasNext())
      {
         NodeSymbol symbol = (NodeSymbol) _symbolWalker.next();

         left = Math.min(left, symbol.getLeft());
         right = Math.max(right, symbol.getRight());
         top = Math.min(top, symbol.getTop());
         bottom = Math.max(bottom, symbol.getBottom());
      }

      Dimension size = new Dimension(right - left + jpgInsetLeft + jpgInsetRigth
            , bottom - top + jpgInsetTop + jpgInsetBottom);

      BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = image.createGraphics();

      Assert.isNotNull(graphics);

      // check the clip rectangle
      // @optimize .. jdk depending code
      // attention!! under jdk 1.2.2 an NullPointerException occurs
      //             in PlainView (line 247)
      //        See BugParade No.4240373 !
      //        With JDK 1.1 and 1.3 it works (-aj-)
      // Hint: The following workaround was added:
      if (graphics.getClipBounds() == null)
      {
         graphics.setClip(0, 0, size.width, size.height);
      }

      // Draw background

      graphics.setColor(Color.white);
      graphics.fillRect(0, 0, size.width, size.height);

      // All symbols need to be moved by the x/y-offset

      AffineTransform _transform = new AffineTransform();

      _transform.translate(-left + jpgInsetLeft
            , -top + jpgInsetTop);

      graphics.transform(_transform);

      _symbolWalker = getAllSymbols();
      while (_symbolWalker.hasNext())
      {
         Symbol symbol = (Symbol) _symbolWalker.next();

         symbol.draw(graphics);
      }

      // Encode the image
      FileOutputStream _stream = null;
      try
      {
         _stream = new FileOutputStream(filePath);
         ImageIO.write(image, "jpeg", _stream);
      }
      catch (Exception x)
      {
         throw new PublicException(x);
      }
      finally
      {
         if (_stream != null)
         {
            try
            {
               _stream.close();
            }
            catch (IOException _ex)
            {
               throw new PublicException(_ex);
            }
         }
      }
   }

   public Iterator getAllSymbols()
   {
      return new SplicingIterator(nodes.iterator(), connections.iterator());
   }

   public void removeFromNodes(NodeSymbol symbol)
   {
      nodes.remove(symbol);
   }

   public void removeFromConnections(ConnectionSymbol connection)
   {
      connections.remove(connection);
   }

   /**
    * Find the first symbol that use the <code>searchedObject</code> as its
    * userobject.
    * <p/>
    * Hint: It is not possible to find a second or third symbol with the same
    * userobject.
    */
   public Symbol findSymbolForUserObject(Object searchedObject)
   {
      for (Iterator i = getAllSymbols(); i.hasNext();)
      {
         Symbol symbol = (Symbol) i.next();
         if (symbol.getUserObject() instanceof Iterator)
         {
            Iterator itr = (Iterator) symbol.getUserObject();
            while (itr.hasNext())
            {
               Object o = itr.next();
               if (searchedObject == o)
               {
                  return symbol;
               }
            }
         }
         else if (searchedObject == symbol.getUserObject())
         {
            return symbol;
         }
      }
      return null;
   }

   /**
    * Find the first symbol that use the <code>searchedObject</code> as its
    * userobject.
    * <p/>
    * Hint: It is not possible to find a second or third symbol with the same
    * userobject.
    */
   public Symbol findSymbolForUserObject(Class userObjectType, int userObjectOID)
   {
      for (Iterator i = getAllSymbols(); i.hasNext();)
      {
         Symbol symbol = (Symbol) i.next();
         Object userObject = symbol.getUserObject();
         if (userObject != null && userObjectType.isInstance(userObject))
         {
            IdentifiableElement element = (IdentifiableElement) userObject;
            if (element.getElementOID() == userObjectOID)
            {
               return symbol;
            }
         }
      }
      return null;
   }

   public Iterator getAllNodeSymbols()
   {
      return nodes.iterator();
   }

   public Iterator getAllNodes(final Class type)
   {
      return nodes.iterator(type);
   }

   public Iterator getAllConnections()
   {
      return connections.iterator();
   }

   public String getId()
   {
      return getName();
   }

   public Dimension getMaximumSize()
   {
      Dimension size = new Dimension(0, 0);
      for (Iterator i = nodes.iterator(); i.hasNext();)
      {
         NodeSymbol symbol = (NodeSymbol) i.next();
         int x = symbol.getRight();
         int y = symbol.getBottom();
         if (size.width < x)
         {
            size.width = x;
         }
         if (size.height < y)
         {
            size.height = y;
         }
      }
      return size;
   }

   public DrawArea getDrawArea()
   {
      return drawArea;
   }

   public ImageIcon getIcon()
   {
      if (icon == null)
      {
         try
         {
            icon = new ImageIcon(DefaultDiagram.class.getResource("images/diagram.gif"));
         }
         catch (Exception x)
         {
            throw new PublicException(
                  BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE.raise("images/diagram.gif"));
         }
      }

      return icon;
   }

   public String getName()
   {
      return name;
   }

   public double getScale()
   {
      return scale;
   }

   public int getXTranslate()
   {
      return xTranslate;
   }

   public int getYTranslate()
   {
      return yTranslate;
   }

   /**
    * Initializes all symbols of the the diagram for drawing.
    */
   public void setDrawArea(DrawArea drawArea)
   {
      this.drawArea = drawArea;
   }

   public void setName(String name)
   {
      markModified();
      this.name = name;
   }

   public void setScale(double scale)
   {
      markModified();

      this.scale = scale;
   }

   /**
    *
    */
   public void setXTranslate(int xTranslate)
   {
      markModified();

      this.xTranslate = xTranslate;
   }

   /**
    *
    */
   public void setYTranslate(int yTranslate)
   {
      markModified();

      this.yTranslate = yTranslate;
   }

   /**
    * Forces all symbols representing the user object <code>userObject</code>
    * to adjust their appearance according to the changes.
    */
   public void userObjectChanged(Object userObject)
   {
      Iterator _symbolWalker = getAllSymbols();
      while (_symbolWalker.hasNext())
      {
         Symbol symbol = (Symbol) _symbolWalker.next();

         if (symbol.getUserObject() instanceof Iterator)
         {
            Iterator itr = (Iterator) symbol.getUserObject();
            while (itr.hasNext())
            {
               Object o = itr.next();
               if (userObject == o)
               {
                  symbol.userObjectChanged();
                  break;
               }
            }
         }
         else if (symbol.getUserObject() == userObject)
         {
            symbol.userObjectChanged();
         }
      }
   }

   /**
    * Removes all symbols representing the user object <code>userObject</code>.
    */
   public void userObjectDeleted(Object userObject)
   {
      // To avoid unsafe iterator usage: buffer objects to be deleted

      boolean _ignoreFlagWasSet = false;

      try
      {
         List vector = CollectionUtils.newList();

         Iterator _symbolWalker = getAllSymbols();
         while (_symbolWalker.hasNext())
         {
            Symbol symbol = (Symbol) _symbolWalker.next();

            if (symbol.getUserObject() instanceof Iterator)
            {
               Iterator itr = (Iterator) symbol.getUserObject();
               while (itr.hasNext())
               {
                  Object o = itr.next();
                  if (userObject == o)
                  {
                     vector.add(symbol);
                     break;
                  }
               }
            }
            else if (symbol.getUserObject() == userObject)
            {
               vector.add(symbol);
            }
         }

         if (getDrawArea() != null)
         {
            _ignoreFlagWasSet = getDrawArea().getIgnoreFireUndoEditRequests();
            if (!_ignoreFlagWasSet)
            {
               getDrawArea().setIgnoreFireUndoEditRequests(true);
            }
         }

         for (java.util.Iterator e = vector.iterator(); e.hasNext();)
         {
            Symbol symbol = (Symbol) e.next();
            symbol.delete();
            symbol.markModified();
         }
      }
      finally
      {
         if (!_ignoreFlagWasSet && getDrawArea() != null)
         {
            getDrawArea().setIgnoreFireUndoEditRequests(false);
         }
      }
   }

   /**
    * Removes all connections representing the connection <code>firstUserObject</code> and
    * <code>secondUserObject</code>.
    * <p/>
    * Note, that this methode currently assumes, that there exists only one connection between
    * two symbols. This may be enhanced in the future.
    */
   public void userObjectsUnlinked(Object firstUserObject, Object secondUserObject)
   {
      // To avoid unsafe iterator usage: buffer objects to be deleted
      boolean _ignoreFlagWasSet = false;

      List vector = CollectionUtils.newList();

      try
      {
         Iterator _symbolWalker = getAllConnections();

         while (_symbolWalker.hasNext())
         {
            ConnectionSymbol connection = (ConnectionSymbol) _symbolWalker.next();

            Symbol firstSymbol = connection.getFirstSymbol();
            Symbol secondSymbol = connection.getSecondSymbol();
            if (firstSymbol != null && secondSymbol != null
                  && ((firstSymbol.getUserObject() == firstUserObject
                  && secondSymbol.getUserObject() == secondUserObject)
                  || (firstSymbol.getUserObject() == secondUserObject
                  && secondSymbol.getUserObject() == firstUserObject)))
            {
               vector.add(connection);
            }
         }

         if (getDrawArea() != null)
         {
            _ignoreFlagWasSet = getDrawArea().getIgnoreFireUndoEditRequests();

            if (!_ignoreFlagWasSet)
            {
               getDrawArea().setIgnoreFireUndoEditRequests(true);
            }
         }

         for (java.util.Iterator e = vector.iterator(); e.hasNext();)
         {
            ((ConnectionSymbol) e.next()).delete();
         }
      }
      finally
      {
         if (getDrawArea() != null && !_ignoreFlagWasSet)
         {
            getDrawArea().setIgnoreFireUndoEditRequests(false);
         }
      }
   }

   public String toString()
   {
      return "Diagram: " + getName();
   }
}
