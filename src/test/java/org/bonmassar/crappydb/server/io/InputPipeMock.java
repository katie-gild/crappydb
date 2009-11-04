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

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.bonmassar.crappydb.server.io.BufferReader;

public class InputPipeMock extends BufferReader {
	
	class Chunk {
		public byte[] packet;
	}
	
	private boolean channelOpened;
	private List<Chunk> wire;

	public InputPipeMock(SelectionKey requestsDescriptor) {
		super(requestsDescriptor);
		channelOpened = false;
		wire = new LinkedList<Chunk>();
	}
	
	public void openChannel(){
		channelOpened = true;
	}
	
	public void addChunk(byte[] data){
		Chunk chunk = new Chunk();
		chunk.packet = data;
		wire.add(chunk);
	}
	
	@Override
	protected boolean invalidSocket(SocketChannel channel) {
		if(null == channel)
			return true;
		
		return !channelOpened;
	}
	
	@Override
	protected int channelRead(SocketChannel channel)
			throws IOException {
		
		if(wire.size() == 0 || null == wire.get(0).packet)
			return -1;
		
		Chunk chk = wire.remove(0);
		
		if(chk.packet.length>0)
			try{
				buffer.put(chk.packet);
			}catch(java.nio.BufferOverflowException boe){
				throw new IOException("Chunk too large");
			}
		
		return chk.packet.length;
	}
}
