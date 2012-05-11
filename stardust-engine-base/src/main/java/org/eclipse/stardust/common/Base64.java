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
package org.eclipse.stardust.common;

import org.eclipse.stardust.common.error.InternalException;

/**
 * This class provides encode/decode for RFC 2045 Base64 as
 * defined by RFC 2045, N. Freed and N. Borenstein.
 * RFC 2045: Multipurpose Internet Mail Extensions (MIME)
 * Part One: Format of Internet Message Bodies. Reference
 * 1996 Available at: http://www.ietf.org/rfc/rfc2045.txt
 *
 * This implementation does not encode/decode streaming
 * data. You need the data that you will encode/decode
 * already on a byte array.
 *
 * @version $Revision$
 */
public final class  Base64
{
   private static final int  BASELENGTH         = 255;
   private static final int  LOOKUPLENGTH       = 64;
   private static final int  TWENTYFOURBITGROUP = 24;
   private static final int  EIGHTBIT           = 8;
   private static final int  SIXTEENBIT         = 16;
   //private static final int  SIXBIT             = 6;
   private static final int  FOURBYTE           = 4;
   private static final int  SIGN               = -128;
   private static final byte PAD                = (byte)'=';
   private static byte [] base64Alphabet        = new byte[BASELENGTH];
   private static byte [] lookUpBase64Alphabet  = new byte[LOOKUPLENGTH];

   static {
      for (int i = 0; i < BASELENGTH; i++)
      {
         base64Alphabet[i] = -1;
      }

      for (int i = 'Z'; i >= 'A'; i--)
      {
         base64Alphabet[i] = (byte)(i - 'A');
      }

      for (int i = 'z'; i >= 'a'; i--)
      {
         base64Alphabet[i] = (byte)(i - 'a' + 26);
      }

      for (int i = '9'; i >= '0'; i--)
      {
         base64Alphabet[i] = (byte)(i - '0' + 52);
      }

      base64Alphabet['+'] = 62;
      base64Alphabet['/'] = 63;

      for (int i = 0; i <= 25; i++)
      {
         lookUpBase64Alphabet[i] = (byte)('A' + i);
      }

      for (int i = 26, j = 0; i <= 51; i++, j++)
      {
         lookUpBase64Alphabet[i] = (byte)('a' + j);
      }

      for (int i = 52, j = 0; i <= 61; i++, j++)
      {
         lookUpBase64Alphabet[i] = (byte)('0' + j);
      }

      lookUpBase64Alphabet[62] = (byte)'+';
      lookUpBase64Alphabet[63] = (byte)'/';
   }

   /**
    *
    */
   protected static boolean isWhiteSpace(byte octect)
   {
      return (octect == 0x20 ||
            octect == 0xd ||
            octect == 0xa ||
            octect == 0x9);
   }

   /**
    *
    */
   protected static boolean isPad(byte octect)
   {
      return (octect == PAD);
   }

   /**
    *
    */
   protected static boolean isData(byte octect)
   {
      return (base64Alphabet[octect] != -1);
   }

   /**
    *
    */
   public static boolean isBase64(String isValidString)
   {
      if (isValidString == null)
      {
         return false;
      }
      return (isArrayByteBase64(isValidString.getBytes()));
   }

   /**
    *
    */
   public static boolean isBase64(byte octect)
   {
      return(isWhiteSpace(octect) || isPad(octect) || isData(octect));
   }

   /**
    * remove WhiteSpace from MIME containing encoded Base64
    * data.
    * e.g.
    * "   sdffferererrereresfsdfsdfsdff\n\r
    * iiiiiiiiierejrlkwjerklwjerwerwr==\n\r"
    *
    * @param data
    * @return The cleaned data. May be the original if that contains no
    *       WhiteSpace.
    */
   public static byte[] removeWhiteSpace(byte[] data)
   {
      if (data == null)
      {
         return null;
      }

      int newSize = 0;
      int len = data.length;
      int i = 0;

      for (; i < len; i++)
      {
         if (!isWhiteSpace(data[i]))
            newSize++;
      }

      if (newSize == len)
      {
         return data;       // return input array since no whiteSpace
      }

      // Allocate new array without whiteSpace
      byte[] arrayWithoutSpaces = new byte[newSize];
      int j = 0;
      for (i = 0; i < len; i++)
      {
         if (isWhiteSpace(data[i]))
         {
            continue;
         }
         else
         {
            arrayWithoutSpaces[j++] = data[i];     //copy non-WhiteSpace
         }
      }
      return arrayWithoutSpaces;
   }

   /**
    *
    */
   public static boolean isArrayByteBase64(byte[] arrayOctect)
   {
      return(getDecodedDataLength(arrayOctect) >= 0);
   }

   /**
    * Encodes hex octects into Base64
    *
    * @param binaryData Array containing binaryData
    * @return Encoded Base64 array
    */
   public static byte[] encode(byte[] binaryData)
   {
      if (binaryData == null)
      {
         return null;
      }

      int lengthDataBits = binaryData.length*EIGHTBIT;
      int fewerThan24bits = lengthDataBits%TWENTYFOURBITGROUP;
      int numberTriplets = lengthDataBits/TWENTYFOURBITGROUP;
      byte encodedData[] = null;

      if (fewerThan24bits != 0) //data not divisible by 24 bit
      {
            encodedData = new byte[(numberTriplets + 1 ) * 4];
      }
      else // 16 or 8 bit
      {
         encodedData = new byte[numberTriplets*4];
      }

      byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;

      int encodedIndex = 0;
      int dataIndex = 0;
      int i = 0;

      for (i = 0; i < numberTriplets; i++)
      {
         dataIndex = i * 3;
         b1 = binaryData[dataIndex];
         b2 = binaryData[dataIndex + 1];
         b3 = binaryData[dataIndex + 2];

         l  = (byte)(b2 & 0x0f);
         k  = (byte)(b1 & 0x03);

         encodedIndex = i * 4;

         byte val1 =
               ((b1 & SIGN) == 0) ? (byte)(b1 >> 2) : (byte)((b1) >> 2^0xc0);
         byte val2 =
               ((b2 & SIGN) == 0) ? (byte)(b2 >> 4) : (byte)((b2) >> 4^0xf0);
         byte val3 =

               ((b3 & SIGN) == 0) ? (byte)(b3 >> 6) : (byte)((b3) >> 6^0xfc);

         encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
         encodedData[encodedIndex+1] = lookUpBase64Alphabet[val2 | (k << 4)];
         encodedData[encodedIndex+2] = lookUpBase64Alphabet[(l << 2) | val3];
         encodedData[encodedIndex+3] = lookUpBase64Alphabet[b3 & 0x3f];
      }

      // form integral number of 6-bit groups

      dataIndex    = i * 3;
      encodedIndex = i * 4;

      if (fewerThan24bits == EIGHTBIT)
      {
         b1 = binaryData[dataIndex];
         k = (byte) ( b1 &0x03 );
         byte val1 =
               ((b1 & SIGN) == 0) ? (byte)(b1 >> 2) : (byte)((b1) >> 2^0xc0);

         encodedData[encodedIndex]     = lookUpBase64Alphabet[ val1 ];
         encodedData[encodedIndex + 1] = lookUpBase64Alphabet[ k<<4 ];
         encodedData[encodedIndex + 2] = PAD;
         encodedData[encodedIndex + 3] = PAD;
      }
      else if (fewerThan24bits == SIXTEENBIT)
      {
         b1 = binaryData[dataIndex];
         b2 = binaryData[dataIndex +1];
         l = (byte)(b2 & 0x0f);
         k = (byte)(b1 & 0x03);

         byte val1 =
               ((b1 & SIGN) == 0) ? (byte)(b1 >> 2) : (byte)((b1) >> 2^0xc0);
         byte val2 =
               ((b2 & SIGN) == 0) ? (byte)(b2 >> 4) : (byte)((b2) >> 4^0xf0);

         encodedData[encodedIndex]     = lookUpBase64Alphabet[val1];
         encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val2 | (k << 4)];
         encodedData[encodedIndex + 2] = lookUpBase64Alphabet[l << 2];
         encodedData[encodedIndex + 3] = PAD;
      }
      return encodedData;
    }

   /**
    * Decodes Base64 data into octects
    *
    * @param base64Data Byte array containing Base64 data
    * @return Array containind decoded data.
    */
   public static byte[] decode(byte[] base64Data)
   {
      if (base64Data == null)
      {
         return null;
      }

      byte[] normalizedBase64Data = removeWhiteSpace(base64Data);

      if (normalizedBase64Data.length % FOURBYTE != 0)
      {
         throw new InternalException(
               "Invalid data legth (must be multiple of four): "
               + normalizedBase64Data.length);
//         return null;    //should be divisible by four
      }

      int numberQuadruple = (normalizedBase64Data.length / FOURBYTE );

      if (numberQuadruple == 0)
      {
         return new byte[0];
      }

      byte decodedData[] = null;
      byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
      byte d1 = 0, d2 = 0, d3 = 0, d4 = 0;

      // Throw away anything not in normalizedBase64Data
      // Adjust size

      int i = 0;
      int encodedIndex = 0;
      int dataIndex    = 0;
      decodedData      = new byte[(numberQuadruple) * 3];

      for (; i<numberQuadruple-1; i++)
      {
         if (!isData((d1 = normalizedBase64Data[dataIndex++]))||
             !isData((d2 = normalizedBase64Data[dataIndex++]))||
             !isData((d3 = normalizedBase64Data[dataIndex++]))||
             !isData((d4 = normalizedBase64Data[dataIndex++])))
         {
            throw new InternalException("Wrong format found.");
//            return null;   //if found "no data" just return null
         }
         b1 = base64Alphabet[d1];
         b2 = base64Alphabet[d2];
         b3 = base64Alphabet[d3];
         b4 = base64Alphabet[d4];

         decodedData[encodedIndex++] = (byte)(  b1 << 2 | b2 >> 4 );
         decodedData[encodedIndex++] =
               (byte)(((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
         decodedData[encodedIndex++] = (byte)(b3 << 6 | b4);
      }

      if (!isData((d1 = normalizedBase64Data[dataIndex++])) ||
          !isData((d2 = normalizedBase64Data[dataIndex++])))
      {
         throw new InternalException("Wrong format found.");
//         return null;      //if found "no data" just return null
      }

      b1 = base64Alphabet[d1];
      b2 = base64Alphabet[d2];

      d3 = normalizedBase64Data[dataIndex++];
      d4 = normalizedBase64Data[dataIndex++];

      if (!isData((d3)) || !isData((d4))) // Check if they are PAD characters
      {
         if (isPad(d3) && isPad(d4))   // Two PAD e.g. 3c[Pad][Pad]
         {
            if ((b2 & 0xf) != 0)       //last 4 bits should be zero
            {
               throw new InternalException(
                     "Corrupted data: last 4 bits should be zero.");
//               return null;
            }

            byte[] tmp = new byte[i * 3 + 1];

            System.arraycopy(decodedData, 0, tmp, 0, i * 3);

            tmp[encodedIndex] = (byte)(b1 << 2 | b2 >> 4) ;

            return tmp;
         }
         else if (!isPad(d3) && isPad(d4))   // One PAD  e.g. 3cQ[Pad]
         {
            b3 = base64Alphabet[d3];

            if ((b3 & 0x3 ) != 0)          //last 2 bits should be zero
            {
               throw new InternalException(
                     "Corrupted data: last 2 bits should be zero.");
//               return null;
            }

            byte[] tmp = new byte[i * 3 + 2];

            System.arraycopy(decodedData, 0, tmp, 0, i * 3);

            tmp[encodedIndex++] = (byte)( b1 << 2 | b2 >> 4);

            tmp[encodedIndex] = (byte)(((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));

            return tmp;
         }
         else
         {
            // an error  like "3c[Pad]r", "3cdX", "3cXd",
            // "3cXX" where X is non data

            throw new InternalException(
                  "Corrupted data: invalid ending bytes.");
//            return null;
         }
      }
      else   //No PAD e.g 3cQl
      {
         b3 = base64Alphabet[d3];
         b4 = base64Alphabet[d4];
         decodedData[encodedIndex++] = (byte)(b1 << 2 | b2 >> 4);
         decodedData[encodedIndex++] =
               (byte)(((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
         decodedData[encodedIndex++] = (byte)(b3 << 6 | b4);
      }
      return decodedData;
    }

   /**
    * returns length of decoded data given an
    * array containing encoded data.
    * WhiteSpace removing is done if data array not
    * valid.
    *
    * @param base64Data
    * @return         a -1 would be return if not
    */
   public static int getDecodedDataLength(byte[] base64Data)
   {
      if (base64Data == null)
      {
         return -1;
      }

      if (base64Data.length == 0)
      {
         return 0;
      }

      byte[] decodedData = null;

      //decode could return a null byte array

      if ((decodedData = decode(base64Data)) == null)
      {
         return -1;
      }
      return decodedData.length;
    }
}