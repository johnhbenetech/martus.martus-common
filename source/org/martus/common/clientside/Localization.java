/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.common.clientside;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.utilities.DateUtilities;
import org.martus.jarverifier.JarVerifier;
import org.martus.swing.UiLanguageDirection;
import org.martus.util.UnicodeReader;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeekThatClosesZipFile;


public class Localization
{
	public Localization(File directoryToUse)
	{
		directory = directoryToUse;
		includeOfficialLanguagesOnly = true;
		textResources = new TreeMap();
		rightToLeftLanguages = new Vector();
	}
	
	/////////////////////////////////////////////////////////////////
	// Text-oriented stuff
	public String getCurrentLanguageCode()
	{
		return currentLanguageCode;
	}

	public void setCurrentLanguageCode(String newLanguageCode)
	{
		loadTranslationFile(newLanguageCode);
		currentLanguageCode = newLanguageCode;
		if(isRightToLeftLanguage())
			UiLanguageDirection.setDirection(UiLanguageDirection.RIGHT_TO_LEFT);
		else
			UiLanguageDirection.setDirection(UiLanguageDirection.LEFT_TO_RIGHT);
	}
	
	protected String getLabel(String languageCode, String key)
	{
		Map availableTranslations = getAvailableTranslations(key);
		if(availableTranslations == null)
			return formatAsUntranslated(key);

		String translatedText = (String)availableTranslations.get(languageCode);
		if(translatedText != null)
			return translatedText;

		String englishText = (String)availableTranslations.get(ENGLISH);
		if(englishText == null)
			System.out.println("Error, probably an invalid Martus-en.mtf file in C:\\Martus, try removing this file.");
		return formatAsUntranslated(englishText);
	}
	
	protected String getMtfEntry(String languageCode, String key)
	{
		String value = getLabel(languageCode, key);
		String hash = getHashOfEnglish(key);
		value = value.replaceAll("\\n", "\\\\n");
		return "-" + hash + "-" + key + "=" + value;
	}

	protected void addEnglishTranslation(String mtfEntry)
	{
		addTranslation(ENGLISH, mtfEntry);
	}

	public void addTranslation(String languageCode, String mtfEntryText)
	{
		if(mtfEntryText == null)
			return;
		
		if(mtfEntryText.equals(MTF_RIGHT_TO_LEFT_LANGUAGE_FLAG))
		{
			addRightToLeftLanguage(languageCode);
			return;
		}
			
		if(mtfEntryText.startsWith(MTF_COMMENT_FLAG))
			return;
		
		if(mtfEntryText.indexOf('=') < 0)
			return;
		
		String key = extractKeyFromMtfEntry(mtfEntryText);
		Map availableTranslations = getAvailableTranslations(key);
		if(availableTranslations == null)
		{
			if(!languageCode.equals(ENGLISH))
				return;
			availableTranslations = new TreeMap();
			textResources.put(key, availableTranslations);
		}
		
		String translatedText = extractValueFromMtfEntry(mtfEntryText);
		String hash = extractHashFromMtfEntry(mtfEntryText);
		if(hash != null && !hash.equals(getHashOfEnglish(key)))
			translatedText = formatAsUntranslated(translatedText);
		availableTranslations.put(languageCode, translatedText);
	}
	
	private String extractKeyFromMtfEntry(String mtfEntryText)
	{
		int keyStart = HASH_LENGTH + 2;
		if(!mtfEntryText.startsWith("-"))
			keyStart = 0;
		
		int splitAt = mtfEntryText.indexOf('=', keyStart);
		if(splitAt < 0)
			splitAt = 0;
		return mtfEntryText.substring(keyStart, splitAt);
	}
	
	private String extractValueFromMtfEntry(String mtfEntryText)
	{
		int keyEnd = mtfEntryText.indexOf('=');
		if(keyEnd < 0)
			return "";
		
		String value = mtfEntryText.substring(keyEnd+1);
		value = value.replaceAll("\\\\n", "\n");
		return value;
	}

	private String extractHashFromMtfEntry(String mtfEntryText)
	{
		if(!mtfEntryText.startsWith("-"))
			return null;
		
		return mtfEntryText.substring(1, HASH_LENGTH + 1);
	}
	
	public void loadTranslations(String languageCode, InputStream inputStream)
	{
		try
		{
			UnicodeReader reader = new UnicodeReader(inputStream);
			while(true)
			{
				String mtfEntry = reader.readLine();
				if(mtfEntry == null)
					break;
				addTranslation(languageCode, mtfEntry);
			}
			reader.close();
		}
		catch (IOException e)
		{
			System.out.println("BulletinDisplay.loadTranslations " + e);
		}
	}
	
	protected SortedSet getAllKeysSorted()
	{
		Set allKeys = textResources.keySet();
		SortedSet sorted = new TreeSet(allKeys);
		return sorted;
	}

	private Map getAvailableTranslations(String key)
	{
		return (Map)textResources.get(key);
	}

	private String formatAsUntranslated(String value)
	{
		if(value.startsWith("<"))
			return value;
		return "<" + value + ">";
	}

	public String getHashOfEnglish(String key)
	{
		return MartusCrypto.getHexDigest(getLabel(ENGLISH, key)).substring(0,HASH_LENGTH);
	}


	/////////////////////////////////////////////////////////////////
	// File-oriented stuff
	
	public void loadTranslationFile(String languageCode)
	{
		InputStream transStream = null;
		try
		{
			File translationFile = getTranslationFile(languageCode);
			String mtfFileShortName = getMtfFilename(languageCode);

			if(translationFile == null)
			{
				transStream = getClass().getResourceAsStream(mtfFileShortName);
			}
			else if(isTranslationPackFile(translationFile))
			{
				ZipFile zip = new ZipFile(translationFile);
				ZipEntry zipEntry = zip.getEntry(mtfFileShortName);
				transStream = new ZipEntryInputStreamWithSeekThatClosesZipFile(zip, zipEntry);
			}
			else
			{
				transStream = new FileInputStream(translationFile);
			}
			
			if(transStream == null)
				return;
			loadTranslations(languageCode, transStream);
		}
		catch (IOException e)
		{
			System.out.println("Localization.loadTranslationFile " + e);
		}
		finally
		{
			try
			{
				if(transStream != null)
					transStream.close();
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
			
		}
	}
	
	public File getMtfFile(String translationFileLanguageCode)
	{
		return new File(directory, getMtfFilename(translationFileLanguageCode));
	}

	public File getMlpkFile(String translationFileLanguageCode)
	{
		return new File(directory, getMlpkFilename(translationFileLanguageCode));
	}
	
	public static String getLanguageCodeFromFilename(String filename)
	{
		if(!isLanguageFile(filename))
			return "";
	
		int codeStart = filename.indexOf('-') + 1;
		int codeEnd = filename.indexOf('.');
		return filename.substring(codeStart, codeEnd);
	}

	public static boolean isLanguageFile(String filename)
	{
		String filenameLower = filename.toLowerCase();
		String martusLanguageFilePrefixLower = MARTUS_LANGUAGE_FILE_PREFIX.toLowerCase();
		String martusLanguageFileSufixLower = MARTUS_LANGUAGE_FILE_SUFFIX.toLowerCase();
		String martusLanguagePackSufixLower = MARTUS_LANGUAGE_PACK_SUFFIX.toLowerCase();
		return (filenameLower.startsWith(martusLanguageFilePrefixLower) 
				&&(filenameLower.endsWith(martusLanguageFileSufixLower) ||
			       filenameLower.endsWith(martusLanguagePackSufixLower)));
	}

	public static String getMtfFilename(String languageCode)
	{
		return MARTUS_LANGUAGE_FILE_PREFIX + languageCode + MARTUS_LANGUAGE_FILE_SUFFIX;
	}

	public static String getMlpkFilename(String languageCode)
	{
		return MARTUS_LANGUAGE_FILE_PREFIX + languageCode + MARTUS_LANGUAGE_PACK_SUFFIX;
	}

	/////////////////////////////////////////////////////////////////
	// Language-oriented stuff

	public static boolean isRecognizedLanguage(String testLanguageCode)
	{
		for(int i = 0 ; i < ALL_LANGUAGE_CODES.length; ++i)
		{
			if(ALL_LANGUAGE_CODES[i].equals(testLanguageCode))
				return true;
		}
		return false;
	}
	
	public File getTranslationFile(String languageCode)
	{
		if(!includeOfficialLanguagesOnly)
		{
			File mtfFile = new File(directory, getMtfFilename(languageCode));
			if(mtfFile.exists())
				return mtfFile;
		}
		File mlpFile = new File(directory, getMlpkFilename(languageCode));
		if(mlpFile.exists())
		{
			if(includeOfficialLanguagesOnly)
			{ 
				if(!isOfficialMlpTranslation(mlpFile))
					return null;
			}
			return mlpFile;
		}
		return null;
	}
	
	public boolean isTranslationPackFile(File translationFile)
	{
		return (translationFile.getName().endsWith(MARTUS_LANGUAGE_PACK_SUFFIX));
	}
	
	public boolean isOfficialTranslationFile(File translationFile)
	{
		return isOfficialMlpTranslation(translationFile);
	}
	
	public boolean isCurrentTranslationOfficial()
	{
		return isOfficialTranslation(currentLanguageCode);
	}
	
	public boolean isOfficialTranslation(String languageCode)
	{
		File translationFile = getTranslationFile(languageCode);
		if(translationFile == null)
		{
			if(languageCode.equals(ENGLISH))
				return true;
			InputStream internal = getClass().getResourceAsStream(getMtfFilename(languageCode));
			if(internal == null)
				return false;
			return true;
		}
		
		if(isTranslationPackFile(translationFile))
			return isOfficialMlpTranslation(translationFile);

		return false;
	}
	
	private boolean isOfficialMlpTranslation(File translationFile)
	{
		return (JarVerifier.verify(translationFile, false) == JarVerifier.JAR_VERIFIED_TRUE);
	}
	
	private boolean isRightToLeftLanguage()
	{
		return rightToLeftLanguages.contains(currentLanguageCode);
	}
	
	public void addRightToLeftLanguage(String languageCode)
	{
		if(rightToLeftLanguages.contains(languageCode))
			return;
		rightToLeftLanguages.add(languageCode);
	}

	/////////////////////////////////////////////////////////////////
	// Date-oriented stuff
	public String getCurrentDateFormatCode()
	{
		return currentDateFormat;
	}

	public void setCurrentDateFormatCode(String code)
	{
		currentDateFormat = code;
	}

	public String convertStoredDateToDisplay(String storedDate)
	{
		DateFormat dfStored = Bulletin.getStoredDateFormat();
		DateFormat dfDisplay = new SimpleDateFormat(getCurrentDateFormatCode());
		String result = "";
		try
		{
			Date d = dfStored.parse(storedDate);
			result = dfDisplay.format(d);
		}
		catch(ParseException e)
		{
			// unparsable dates simply become blank strings,
			// so we don't want to do anything for this exception
			//System.out.println(e);
		}
		return result;
	}
	
	
	static public class NoDateSeparatorException extends Exception{};
	
	
	private String reverseDate(String dateToReverse)
	{
		StringBuffer reversedDate= new StringBuffer();
		try
		{
			char dateSeparator = UiBasicLocalization.getDateSeparator(dateToReverse);
			int beginningIndex = dateToReverse.indexOf(dateSeparator);
			int endingIndex = dateToReverse.lastIndexOf(dateSeparator);
			String dateField1 = dateToReverse.substring(0, beginningIndex);
			String dateField2 = dateToReverse.substring(beginningIndex+1, endingIndex);
			String dateField3 = dateToReverse.substring(endingIndex+1);
			reversedDate.append(dateField3);
			reversedDate.append(dateSeparator);
			reversedDate.append(dateField2);
			reversedDate.append(dateSeparator);
			reversedDate.append(dateField1);
			return reversedDate.toString();
		}
		catch(NoDateSeparatorException e)
		{
			return dateToReverse;
		}
	}
	
	public String convertStoredDateToDisplayReverseIfNecessary(String date)
	{
		String displayDate = convertStoredDateToDisplay(date);
		if(UiLanguageDirection.isRightToLeftLanguage())
			return reverseDate(displayDate);
		return displayDate;
	}
	
	public String convertStoredDateTimeToDisplay(String storedDate)
	{		
		DateFormat dfStored = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		DateFormat dfDisplay = new SimpleDateFormat(getCurrentDateFormatCode());
		String result = "";
		try
		{
			Date date = dfStored.parse(storedDate);
			String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(date);		
			result = dfDisplay.format(date)+" "+time;
		}
		catch(ParseException e)
		{
			// unparsable dates simply become blank strings,
			// so we don't want to do anything for this exception
			//System.out.println(e);
		}
	
		return result;
	}

	private static Map getDefaultDateFormats()
	{
		Map defaultLanguageDateFormat = new HashMap();
		defaultLanguageDateFormat.put(ENGLISH, DateUtilities.getDefaultDateFormatCode());
		defaultLanguageDateFormat.put(SPANISH, DateUtilities.DMY_SLASH.getCode());
		defaultLanguageDateFormat.put(RUSSIAN, DateUtilities.DMY_DOT.getCode());
		defaultLanguageDateFormat.put(THAI, DateUtilities.DMY_SLASH.getCode());
		defaultLanguageDateFormat.put(ARABIC, DateUtilities.DMY_SLASH.getCode());
		return defaultLanguageDateFormat;
	}
	
	public static String getDefaultDateFormatForLanguage(String languageCode)
	{
		Map defaultLanguageDateFormat = getDefaultDateFormats();
		if(!defaultLanguageDateFormat.containsKey(languageCode))
			return DateUtilities.getDefaultDateFormatCode();
		return (String)defaultLanguageDateFormat.get(languageCode);
	}

	public File directory;
	public Map textResources;
	public String currentLanguageCode;
	public String currentDateFormat;

	public static final String UNUSED_TAG = "";
	public static final String MARTUS_LANGUAGE_FILE_PREFIX = "Martus-";
	public static final String MARTUS_LANGUAGE_FILE_SUFFIX = ".mtf";
	public static final String MARTUS_LANGUAGE_PACK_SUFFIX = ".mlp";
	public static final String MTF_COMMENT_FLAG = "#";
	public static final String MTF_RIGHT_TO_LEFT_LANGUAGE_FLAG = "!right-to-left";
	
	public static final int HASH_LENGTH = 4;
	
	public static final String LANGUAGE_OTHER = "?";
	public static final String ENGLISH = "en";
	public static final String FRENCH = "fr";
	public static final String SPANISH = "es";
	public static final String RUSSIAN = "ru";
	public static final String THAI = "th";
	public static final String ARABIC = "ar";
	public static final String[] ALL_LANGUAGE_CODES = {
				LANGUAGE_OTHER, ENGLISH, ARABIC,
				"az", "bg", "bn", "km","my","zh", "nl", "eo", "fa", FRENCH, "de","gu","ha","he","hi","hu",
				"it", "ja","jv","kn","kk","ky","ko","ml","mr","ne","or","pa","ps","pl","pt","ro",RUSSIAN,"sr",
				"sr", "sd","si",SPANISH,"ta","tg","te",THAI,"tr","tk","uk","ur","uz","vi"};
	public Vector rightToLeftLanguages;
	public boolean includeOfficialLanguagesOnly;
	
}
