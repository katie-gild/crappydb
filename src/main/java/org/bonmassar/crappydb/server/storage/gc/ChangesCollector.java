/*
 *  This file is part of CrappyDB-Server, 
 *  developed by Luca Bonmassar <luca.bonmassar at gmail.com>
 *
 *  CrappyDB-Server is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  CrappyDB-Server is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CrappyDB-Server.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonmassar.crappydb.server.storage.gc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.bonmassar.crappydb.server.storage.data.Key;
import org.bonmassar.crappydb.server.storage.data.Timestamp;

public class ChangesCollector implements GarbageCollector{
	Collection<ReferenceBean> incoming;
	
	public ChangesCollector() {
		incoming = new LinkedList<ReferenceBean>();
	}

	public void monitor(Key k, long expiration) {
		if(expiration <= 0)
			return;
		
		ReferenceBean rb = new ReferenceBean(k, new Timestamp(expiration));
		synchronized(this){
			incoming.add(rb);
		}
	}

	public void replace(Key k, long expiration, long oldExpiration) {
		if(expiration <= 0 && oldExpiration <= 0)
			return;
		
		ReferenceBean rb = new ReplaceReferenceBean(k, new Timestamp(expiration), new Timestamp(oldExpiration));
		incoming.add(rb);
	}

	public void stop(Key k, long expiration) {
		if(expiration <= 0)
			return;

		ReferenceBean rb = new DeleteReferenceBean(k, new Timestamp(expiration));
		incoming.add(rb);
	}

	public void flush() {
		incoming.add(new FlushReferenceBean(new Key("FAKEKEY"), new Timestamp(0)));
	}

	
	public void visitIncoming(Set<ReferenceBean> timemap) {
		Collection<ReferenceBean> prevIncoming = incoming;
		incoming = new LinkedList<ReferenceBean>(); 
		
		for(ReferenceBean bean : prevIncoming){
			bean.visit(timemap);
		}
	}

}
