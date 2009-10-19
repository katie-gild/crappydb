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

package org.bonmassar.crappydb.server.io;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bonmassar.crappydb.server.ShutdownExecutionRegister.Registry;
import org.bonmassar.crappydb.server.config.Configuration;
import org.bonmassar.crappydb.server.memcache.protocol.CommandFactory;

class DBPoolThreadExecutor {

	private final CommandFactory cmdFactory;
	private final Selector serverSelectorForAccept;

	private int nThreads;
	protected ExecutorService executor;
	
	public DBPoolThreadExecutor(CommandFactory cmdFactory, Selector serverSelectorForAccept) {
		this(cmdFactory, serverSelectorForAccept, Configuration.INSTANCE.getEngineThreads());
	}
	
	public DBPoolThreadExecutor(CommandFactory cmdFactory, Selector serverSelectorForAccept, int nThreads) {
		this.cmdFactory = cmdFactory;
		this.serverSelectorForAccept = serverSelectorForAccept;
		this.nThreads = nThreads;
		initFrontendThreads();
	}
	
	public Future<Void> submit(SelectionKey key) {
		return executor.submit (new FrontendTask(cmdFactory, serverSelectorForAccept, key) );
	}
		
	protected void initFrontendThreads() {
		executor = Executors.newFixedThreadPool(nThreads);
		Registry.INSTANCE.book(executor);
	}
}
