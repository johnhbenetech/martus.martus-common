/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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

package org.martus.common.bulletinstore;

import java.util.HashMap;
import java.util.Vector;

import org.martus.common.HQKeys;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;

public class LeafNodeCache extends BulletinStoreCache implements Database.PacketVisitor
{
	public LeafNodeCache(BulletinStore storeToUse)
	{
		store = storeToUse;
		storeWasCleared();
	}
	
	public synchronized void storeWasCleared()
	{
		isValid = false;
	}
	
	public synchronized void revisionWasSaved(UniversalId uid)
	{
		// TODO: definitely could be optimized!
		storeWasCleared();
	}
	
	public synchronized void revisionWasRemoved(UniversalId uid)
	{
		// TODO: definitely could be optimized!
		storeWasCleared();
	}
	
	public synchronized Vector getLeafKeys()
	{
		fill();
		return leafKeys;
	}
	
	public synchronized Vector getNonLeafUids()
	{
		fill();
		return nonLeafUids;
	}
	
	public synchronized Vector getFieldOffices(String hqAccountId)
	{
		fill();
		return internalGetFieldOffices(hqAccountId);
	}
	
	// TODO: NOTE! There is a corner case where this could return an incorrect value:
	// Client A starts a scan, and gets an error
	// Client B clears the cache and starts their own scan
	// Client A asks if there were errors, and is told "no"
	// The proper way to handle it is for all the getXxx calls to return an 
	// object that contains both the data, and whether or not there was an error
	public synchronized boolean hadErrors()
	{
		return hitErrorsDuringScan;
	}
	
	
	
	
	
	
	
	private void fill()
	{
		if(isValid)
			return;

		hitErrorsDuringScan = false;
		leafKeys = new Vector();
		nonLeafUids = new Vector();
		fieldOfficesPerHq = new HashMap();
		store.visitAllBulletinRevisions(this);
		isValid = true;
	}

	private Vector internalGetFieldOffices(String hqAccountId)
	{
		Vector results = (Vector)fieldOfficesPerHq.get(hqAccountId);
		if(results == null)
			results = new Vector();
		
		return results;
	}

	public synchronized boolean isCacheValid()
	{
		return isValid;
	}
	
	public void visit(DatabaseKey key)
	{
		try
		{
			BulletinHeaderPacket bhp = BulletinStore.loadBulletinHeaderPacket(getDatabase(), key, getSecurity());
			addToCachedHqInformation(key, bhp.getAuthorizedToReadKeys());
			addToCachedLeafInformation(key, bhp.getHistory());
		}
		catch(Exception e)
		{
			hitErrorsDuringScan = true;
			// FIXME: change this to use a logger so we see problems on the server!
			//e.printStackTrace();
		}
	}

	private ReadableDatabase getDatabase()
	{
		return store.getDatabase();
	}
	
	private MartusCrypto getSecurity()
	{
		return store.getSignatureVerifier();
	}
	
	private void addToCachedHqInformation(DatabaseKey key, HQKeys hqs)
	{
		for(int i=0; i < hqs.size(); ++i)
		{
			String thisHqKey = hqs.get(i).getPublicKey();
			Vector fieldOffices = internalGetFieldOffices(thisHqKey);
			if(!fieldOffices.contains(key.getAccountId()))
			{
				fieldOffices.add(key.getAccountId());
				fieldOfficesPerHq.put(thisHqKey, fieldOffices);
			}
		}
	}

	private void addToCachedLeafInformation(DatabaseKey key, BulletinHistory history)
	{
		UniversalId maybeLeaf = key.getUniversalId();
		if(!nonLeafUids.contains(maybeLeaf))
			leafKeys.add(key);
		
		for(int i=0; i < history.size(); ++i)
		{
			String thisLocalId = history.get(i);
			UniversalId uidOfNonLeaf = UniversalId.createFromAccountAndLocalId(key.getAccountId(), thisLocalId);
			leafKeys.remove(DatabaseKey.createSealedKey(uidOfNonLeaf));
			leafKeys.remove(DatabaseKey.createDraftKey(uidOfNonLeaf));
			nonLeafUids.add(uidOfNonLeaf);
		}
	}

	private BulletinStore store;
	private boolean isValid;
	private boolean hitErrorsDuringScan;
	private Vector leafKeys;
	private Vector nonLeafUids;
	private HashMap fieldOfficesPerHq;
}

