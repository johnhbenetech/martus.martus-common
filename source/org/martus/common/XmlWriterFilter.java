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

package org.martus.common;

import java.io.IOException;
import java.io.Writer;

import org.martus.common.crypto.MartusCrypto;

public class XmlWriterFilter
{
	public XmlWriterFilter(Writer writerToUse)
	{
		writer = writerToUse;
	}

	public void writeStartTag(String text) throws IOException
	{
		writeDirect("<" + text + ">");
	}

	public void writeEndTag(String text) throws IOException
	{
		writeStartTag("/" + text);
		writeDirect("\n");
	}

	public void writeEncoded(String text) throws IOException
	{
		writeDirect(MartusUtilities.getXmlEncoded(text));
	}

	public void writeDirect(String s) throws IOException
	{
		if(sigGen != null)
		{
			try
			{
				byte[] bytes = s.getBytes("UTF-8");
				sigGen.signatureDigestBytes(bytes);
			}
			catch(MartusCrypto.MartusSignatureException e)
			{
				throw new IOException("Signature Exception: " + e.getMessage());
			}
		}
		writer.write(s);
	}

	public void startSignature(MartusCrypto sigGenToUse) throws
				MartusCrypto.MartusSignatureException
	{
		sigGen = sigGenToUse;
		sigGen.signatureInitializeSign();
	}

	public byte[] getSignature() throws
				MartusCrypto.MartusSignatureException
	{
		if(sigGen == null)
			throw new MartusCrypto.MartusSignatureException();

		byte[] sig = sigGen.signatureGet();
		sigGen = null;
		return sig;
	}

	private Writer writer;
	private MartusCrypto sigGen;
}
