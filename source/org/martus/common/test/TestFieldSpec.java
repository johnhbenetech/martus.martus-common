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

package org.martus.common.test;

import org.martus.common.FieldSpec;
import org.martus.common.LegacyCustomFields;
import org.martus.util.*;


public class TestFieldSpec extends TestCaseEnhanced
{
	public TestFieldSpec(String name)
	{
		super(name);
	}

	public void testLegacy()
	{
		FieldSpec plainField = LegacyCustomFields.createFromLegacy("a,b");
		assertFalse("has unknown?", plainField.hasUnknownStuff());
		assertEquals("a", plainField.getTag());
		assertEquals("b", plainField.getLabel());
		assertEquals("not normal?", FieldSpec.TYPE_NORMAL, plainField.getType());
		
		FieldSpec fieldWithExtra = LegacyCustomFields.createFromLegacy("c,d,e");
		assertTrue("doesn't have unknown?", fieldWithExtra.hasUnknownStuff());
		assertEquals("c", fieldWithExtra.getTag());
		assertEquals("d", fieldWithExtra.getLabel());
		assertEquals("not unknown?", FieldSpec.TYPE_UNKNOWN, fieldWithExtra.getType());
	}
	
	public void testCreateFromTag()
	{
		FieldSpec plainField = LegacyCustomFields.createFromLegacy("author");
		assertFalse("has unknown?", plainField.hasUnknownStuff());
		assertEquals("author", plainField.getTag());
		assertEquals("", plainField.getLabel());
		assertEquals("not normal?", FieldSpec.TYPE_NORMAL, plainField.getType());

		FieldSpec dateField = LegacyCustomFields.createFromLegacy("entrydate");
		assertFalse("has unknown?", dateField.hasUnknownStuff());
		assertEquals("entrydate", dateField.getTag());
		assertEquals("", dateField.getLabel());
		assertEquals("not date?", FieldSpec.TYPE_DATE, dateField.getType());
	}
	
	public void testToString()
	{
		FieldSpec plainField = LegacyCustomFields.createFromLegacy("a,<&b>");
		String xml = "<Field><Tag>a</Tag><Label>&lt;&amp;b&gt;</Label><Type>STRING</Type></Field>";
		assertEquals(xml, plainField.toString());
	}
	
	public void testGetTypeString()
	{
		assertEquals("STRING", FieldSpec.getTypeString(FieldSpec.TYPE_NORMAL));
		assertEquals("MULTILINE", FieldSpec.getTypeString(FieldSpec.TYPE_MULTILINE));
		assertEquals("DATE", FieldSpec.getTypeString(FieldSpec.TYPE_DATE));
		assertEquals("DATERANGE", FieldSpec.getTypeString(FieldSpec.TYPE_DATERANGE));
		assertEquals("BOOLEAN", FieldSpec.getTypeString(FieldSpec.TYPE_BOOLEAN));
		assertEquals("SINGLECHOICE", FieldSpec.getTypeString(FieldSpec.TYPE_CHOICE));
		assertEquals("UNKNOWN", FieldSpec.getTypeString(-99));
	}
	
	public void testGetTypeCode()
	{
		assertEquals(FieldSpec.TYPE_UNKNOWN, FieldSpec.getTypeCode("anything else"));
		assertEquals(FieldSpec.TYPE_NORMAL, FieldSpec.getTypeCode("STRING"));
		assertEquals(FieldSpec.TYPE_MULTILINE, FieldSpec.getTypeCode("MULTILINE"));
		assertEquals(FieldSpec.TYPE_DATE, FieldSpec.getTypeCode("DATE"));
		assertEquals(FieldSpec.TYPE_DATERANGE, FieldSpec.getTypeCode("DATERANGE"));
		assertEquals(FieldSpec.TYPE_BOOLEAN, FieldSpec.getTypeCode("BOOLEAN"));
		assertEquals(FieldSpec.TYPE_CHOICE, FieldSpec.getTypeCode("SINGLECHOICE"));
		
	}
}
