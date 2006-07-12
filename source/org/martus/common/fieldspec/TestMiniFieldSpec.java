/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
package org.martus.common.fieldspec;

import org.martus.util.TestCaseEnhanced;

public class TestMiniFieldSpec extends TestCaseEnhanced
{
	public TestMiniFieldSpec(String name)
	{
		super(name);
	}

	public void testBasics()
	{
		String tag1 = "tag";
		String tag2 = "tag2";
		String label1 = "Label";
		String label2 = "Label2";
		FieldType type1 = new FieldTypeNormal();
		FieldType type2 = new FieldTypeDate();
		
		MiniFieldSpec simple = create(tag1, label1, type1);
		MiniFieldSpec same = create(tag1, label1, type1);
		assertEquals("identical don't match?", simple, same);
		
		MiniFieldSpec differentTag = create(tag2, label1, type1);
		assertNotEquals("different tag still equal?", simple, differentTag);
		
		MiniFieldSpec differentLabel = create(tag1, label2, type1);
		assertNotEquals("different label still equal?", simple, differentLabel);
		
		MiniFieldSpec differentType = create(tag1, label1, type2);
		assertNotEquals("different type still equal?", simple, differentType);
	}

	public void testForNotGlueingComponentsTogether() throws Exception
	{
		MiniFieldSpec similarButLower = create("56", "1234", new FieldTypeNormal());
		MiniFieldSpec similarButHigher = create("3456", "12", new FieldTypeNormal());
		assertNotEquals("Glued label+tag?", similarButLower, similarButHigher);
	}
	
	public void testSorting()
	{
		MiniFieldSpec lower = create("tag", "a", new FieldTypeNormal());
		MiniFieldSpec higher = create("tag", "b", new FieldTypeNormal());
		verifyOrder(lower, higher);
	}
	
	private void verifyOrder(MiniFieldSpec lower, MiniFieldSpec higher)
	{
		assertNotEquals("compareTo said equal?", lower, higher);
		assertTrue("compareTo < failed?", lower.compareTo(higher) < 0);
		assertTrue("compareTo > failed?", higher.compareTo(lower) > 0);
	}
	
	private MiniFieldSpec create(String tag, String label, FieldType type)
	{
		FieldSpec spec = FieldSpec.createCustomField(tag, label, type);
		return new MiniFieldSpec(spec);
	}
}
