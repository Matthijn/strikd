package strikd.net.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import strikd.communication.Opcodes;

public class MessageDecoder extends ByteToMessageDecoder
{
	private static final int MAX_MESSAGE_SIZE = 512;
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception
	{
		// Length header available? (2 bytes)
		if(buffer.isReadable(2))
		{
			// Mark reader index
			buffer.markReaderIndex();
	
			// Read message length (2 bytes)
			short length = buffer.readShort();
	
			// Valid length and all data arrived?
			if(length > 0 && length <= MAX_MESSAGE_SIZE && buffer.isReadable(length))
			{
				// Parse opcode
				Opcodes.Incoming op = Opcodes.Incoming.valueOf(buffer.readByte());
				
				// Copy bytes to an IncomingMessage (heapbuffer) and export this as a decoded message
				out.add(new IncomingMessage(op, buffer.readBytes(length - 1)));
				
				// TODO: investigate usage of ByteBuf#slice(length) after retaining it, to avoid making a heapbuffer copy
			}
			else
			{
				// Restart reading again when there's more data
				buffer.resetReaderIndex();
			}
		}
	}
}
