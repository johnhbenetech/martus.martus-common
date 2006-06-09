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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class PopUpTreeFieldSpec extends FieldSpec
{
	public PopUpTreeFieldSpec()
	{
		this(new SearchFieldTreeModel(new DefaultMutableTreeNode()));
	}

	public PopUpTreeFieldSpec(SearchFieldTreeModel modelToUse)
	{
		super(new FieldTypePopUpTree());
		model = modelToUse;
	}
	
	public SearchableFieldChoiceItem findSearchTag(String tagToFind)
	{
		TreeNode root = (TreeNode)model.getRoot();
		for(int i = 0; i < root.getChildCount(); ++i)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
			SearchableFieldChoiceItem item = (SearchableFieldChoiceItem)node.getUserObject();
			if(item.getSearchTag().equals(tagToFind))
				return item;
		}
		return null;
	}
	
	public SearchableFieldChoiceItem findCode(String codeToFind)
	{
		TreeNode root = (TreeNode)model.getRoot();
		for(int i = 0; i < root.getChildCount(); ++i)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
			SearchableFieldChoiceItem item = (SearchableFieldChoiceItem)node.getUserObject();
			if(item.getCode().equals(codeToFind))
				return item;
		}
		return null;
	}
	
	public SearchFieldTreeModel getModel()
	{
		return model;
	}
	
	SearchFieldTreeModel model;
}