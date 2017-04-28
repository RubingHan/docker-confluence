/*     */ package com.atlassian.extras.decoder.v2;
/*     */ 
/*     */ import com.atlassian.extras.common.LicenseException;
/*     */ import com.atlassian.extras.common.org.springframework.util.DefaultPropertiesPersister;
/*     */ import com.atlassian.extras.decoder.api.AbstractLicenseDecoder;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.security.InvalidKeyException;
/*     */ import java.security.KeyFactory;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ import java.security.PublicKey;
/*     */ import java.security.Signature;
/*     */ import java.security.SignatureException;
/*     */ import java.security.spec.InvalidKeySpecException;
/*     */ import java.security.spec.X509EncodedKeySpec;
/*     */ import java.util.Properties;
/*     */ import java.util.zip.Inflater;
/*     */ import java.util.zip.InflaterInputStream;
/*     */ import org.apache.commons.codec.binary.Base64;
/*     */ 
/*     */ public class Version2LicenseDecoder extends AbstractLicenseDecoder
/*     */ {
/*     */   public static final int VERSION_NUMBER_1 = 1;
/*     */   public static final int VERSION_NUMBER_2 = 2;
/*     */   public static final int VERSION_LENGTH = 3;
/*     */   public static final int ENCODED_LICENSE_LENGTH_BASE = 31;
/*  56 */   public static final byte[] LICENSE_PREFIX = { 13, 14, 12, 10, 15 };
/*     */   public static final char SEPARATOR = 'X';
/*     */   private static final PublicKey PUBLIC_KEY;
/*     */   private static final int ENCODED_LICENSE_LINE_LENGTH = 76;
/*     */ 
/*     */   public boolean canDecode(String licenseString)
/*     */   {
/*  90 */     licenseString = removeWhiteSpaces(licenseString);
/*     */ 
/*  92 */     int pos = licenseString.lastIndexOf('X');
/*  93 */     if ((pos == -1) || (pos + 3 >= licenseString.length()))
/*     */     {
/*  95 */       return false;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 101 */       int version = Integer.parseInt(licenseString.substring(pos + 1, pos + 3));
/* 102 */       if ((version != 1) && (version != 2))
/*     */       {
/* 104 */         return false;
/*     */       }
/*     */ 
/* 107 */       String lengthStr = licenseString.substring(pos + 3);
/* 108 */       int encodedLicenseLength = Integer.valueOf(lengthStr, 31).intValue();
/* 109 */       if (pos != encodedLicenseLength)
/*     */       {
/* 111 */         return false;
/*     */       }
/*     */ 
/* 114 */       return true;
/*     */     }
/*     */     catch (NumberFormatException e) {
/*     */     }
/* 118 */     return false;
/*     */   }
/*     */ 
/*     */   public Properties doDecode(String licenseString)
/*     */   {
/* 124 */     String encodedLicenseTextAndHash = getLicenseContent(removeWhiteSpaces(licenseString));
/* 125 */     byte[] zippedLicenseBytes = checkAndGetLicenseText(encodedLicenseTextAndHash);
/* 126 */     Reader licenseText = unzipText(zippedLicenseBytes);
/*     */ 
/* 128 */     return loadLicenseConfiguration(licenseText);
/*     */   }
/*     */ 
/*     */   protected int getLicenseVersion()
/*     */   {
/* 133 */     return 2;
/*     */   }
/*     */ 
/*     */   private Reader unzipText(byte[] licenseText)
/*     */   {
/* 138 */     ByteArrayInputStream in = new ByteArrayInputStream(licenseText);
/* 139 */     in.skip(LICENSE_PREFIX.length);
/* 140 */     InflaterInputStream zipIn = new InflaterInputStream(in, new Inflater());
/*     */     try
/*     */     {
/* 143 */       return new InputStreamReader(zipIn, "UTF-8");
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/* 148 */       throw new LicenseException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private String getLicenseContent(String licenseString)
/*     */   {
/* 154 */     String lengthStr = licenseString.substring(licenseString.lastIndexOf('X') + 3);
/*     */     try
/*     */     {
/* 157 */       int encodedLicenseLength = Integer.valueOf(lengthStr, 31).intValue();
/* 158 */       return licenseString.substring(0, encodedLicenseLength);
/*     */     }
/*     */     catch (NumberFormatException e)
/*     */     {
/* 162 */       throw new LicenseException("Could NOT decode license length <" + lengthStr + ">", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private byte[] checkAndGetLicenseText(String licenseContent)
/*     */   {
/*     */     byte[] licenseText;
/*     */     try
/*     */     {
/* 171 */       byte[] decodedBytes = Base64.decodeBase64(licenseContent.getBytes());
/* 172 */       ByteArrayInputStream in = new ByteArrayInputStream(decodedBytes);
/* 173 */       DataInputStream dIn = new DataInputStream(in);
/* 174 */       int textLength = dIn.readInt();
/* 175 */       licenseText = new byte[textLength];
/* 176 */       dIn.read(licenseText);
/* 177 */       byte[] hash = new byte[dIn.available()];
/* 178 */       dIn.read(hash);
/*     */       try
/*     */       {
/* 182 */         Signature signature = Signature.getInstance("SHA1withDSA");
/* 183 */         signature.initVerify(PUBLIC_KEY);
/* 184 */         signature.update(licenseText);
/* 185 */         if (!signature.verify(hash))
/*     */         {
/* 187 */           throw new LicenseException("Failed to verify the license.");
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (InvalidKeyException e)
/*     */       {
/* 193 */         throw new LicenseException(e);
/*     */       }
/*     */       catch (SignatureException e)
/*     */       {
/* 197 */         throw new LicenseException(e);
/*     */       }
/*     */       catch (NoSuchAlgorithmException e)
/*     */       {
/* 202 */         throw new LicenseException(e);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 208 */       throw new LicenseException(e);
/*     */     }
/*     */ 
/* 211 */     return licenseText;
/*     */   }
/*     */ 
/*     */   private Properties loadLicenseConfiguration(Reader text)
/*     */   {
/*     */     try
/*     */     {
/* 218 */       Properties props = new Properties();
/* 219 */       new DefaultPropertiesPersister().load(props, text);
/* 220 */       return props;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 224 */       throw new LicenseException("Could NOT load properties from reader", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private static String removeWhiteSpaces(String licenseData)
/*     */   {
/* 233 */     if ((licenseData == null) || (licenseData.length() == 0))
/*     */     {
/* 235 */       return licenseData;
/*     */     }
/*     */ 
/* 238 */     char[] chars = licenseData.toCharArray();
/* 239 */     StringBuffer buf = new StringBuffer(chars.length);
/* 240 */     for (int i = 0; i < chars.length; i++)
/*     */     {
/* 242 */       if (!Character.isWhitespace(chars[i]))
/*     */       {
/* 244 */         buf.append(chars[i]);
/*     */       }
/*     */     }
/*     */ 
/* 248 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public static String packLicense(byte[] text, byte[] hash)
/*     */     throws LicenseException
/*     */   {
/*     */     try
/*     */     {
/* 260 */       ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 261 */       DataOutputStream dOut = new DataOutputStream(out);
/* 262 */       dOut.writeInt(text.length);
/* 263 */       dOut.write(text);
/* 264 */       dOut.write(hash);
/*     */ 
/* 266 */       byte[] allData = out.toByteArray();
/* 267 */       String result = new String(Base64.encodeBase64(allData)).trim();
/*     */ 
/* 272 */       result = result + 'X' + "0" + 2 + Integer.toString(result.length(), 31);
/* 273 */       return split(result);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 279 */       throw new LicenseException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private static String split(String licenseData)
/*     */   {
/* 288 */     if ((licenseData == null) || (licenseData.length() == 0))
/*     */     {
/* 290 */       return licenseData;
/*     */     }
/*     */ 
/* 293 */     char[] chars = licenseData.toCharArray();
/* 294 */     StringBuffer buf = new StringBuffer(chars.length + chars.length / 76);
/* 295 */     for (int i = 0; i < chars.length; i++)
/*     */     {
/* 297 */       buf.append(chars[i]);
/* 298 */       if ((i > 0) && (i % 76 == 0))
/*     */       {
/* 300 */         buf.append('\n');
/*     */       }
/*     */     }
/*     */ 
/* 304 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*     */     try
/*     */     {
/*  66 */       String pubKeyEncoded = "MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1_U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq_xfW6MPbLm1Vs14E7gB00b_JmYLdrmVClpJ-f6AR7ECLCT7up1_63xhv4O1fnxqimFQ8E-4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC_BYHPUCgYEA9-GghdabPd7LvKtcNrhXuXmUr7v6OuqC-VdMCz0HgmdRWVeOutRZT-ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN_C_ohNWLx-2J6ASQ7zKTxvqhRkImog9_hWuWfBpKLZl6Ae1UlZAFMO_7PSSoDgYUAAoGBAOshUqTDMJgJhrrooXl9ajUjDyunW8FSX1IjOOyNRwd0TEwtzfZzzAzUsGm4bPYjIHQpe1ovONVVUpEzYJGJMxVXbbBHQYMbevdvSUdq90LLWXhgwwlXRAwqPq9S0YZP7r9uisPruk59LVj-D-L_GVacH01LlWkm74ya1CusMxDc";
/*     */ 
/*  73 */       KeyFactory keyFactory = KeyFactory.getInstance("DSA");
/*  74 */       PUBLIC_KEY = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64("MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1_U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq_xfW6MPbLm1Vs14E7gB00b_JmYLdrmVClpJ-f6AR7ECLCT7up1_63xhv4O1fnxqimFQ8E-4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC_BYHPUCgYEA9-GghdabPd7LvKtcNrhXuXmUr7v6OuqC-VdMCz0HgmdRWVeOutRZT-ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN_C_ohNWLx-2J6ASQ7zKTxvqhRkImog9_hWuWfBpKLZl6Ae1UlZAFMO_7PSSoDgYUAAoGBAOshUqTDMJgJhrrooXl9ajUjDyunW8FSX1IjOOyNRwd0TEwtzfZzzAzUsGm4bPYjIHQpe1ovONVVUpEzYJGJMxVXbbBHQYMbevdvSUdq90LLWXhgwwlXRAwqPq9S0YZP7r9uisPruk59LVj-D-L_GVacH01LlWkm74ya1CusMxDc".getBytes())));
/*     */     }
/*     */     catch (NoSuchAlgorithmException e)
/*     */     {
/*  79 */       throw new Error(e);
/*     */     }
/*     */     catch (InvalidKeySpecException e)
/*     */     {
/*  84 */       throw new Error(e);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /data/workspace/JiraLicense/atlassian-extras-decoder-v2-3.2.jar
 * Qualified Name:     com.atlassian.extras.decoder.v2.Version2LicenseDecoder
 * JD-Core Version:    0.6.2
 */
