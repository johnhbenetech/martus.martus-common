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

import org.martus.common.MagicWordEntry;


public class TestMagicWordEntry extends TestCaseEnhanced
{
	public TestMagicWordEntry(String name)
	{
		super(name);
	}
	public void testBasics()
	{
		String groupEntry = "group";
		String validMagicWord = "magic";
		MagicWordEntry entry = new MagicWordEntry(validMagicWord, groupEntry);
		assertEquals("magic word not the same?", validMagicWord, entry.getMagicWord());
		assertEquals("getMagicWordWithActiveSign word not the same?", validMagicWord, entry.getMagicWordWithActiveSign());
		assertEquals("group not the same?", groupEntry, entry.getGroupName());
		assertTrue("magicWord invalid?", entry.isActive());
	
		String invalidMagicWord = "#magic2";
		MagicWordEntry entry2 = new MagicWordEntry(invalidMagicWord, groupEntry);
		assertFalse("magicWord valid?", entry2.isActive());
		assertNotEquals("inactive magic word is the same?", invalidMagicWord, entry2.getMagicWord());
		assertEquals("getMagicWordWithActiveSign word not the same?", invalidMagicWord, entry2.getMagicWordWithActiveSign());
		assertEquals("group not the same?", groupEntry, entry2.getGroupName());
	}
}
