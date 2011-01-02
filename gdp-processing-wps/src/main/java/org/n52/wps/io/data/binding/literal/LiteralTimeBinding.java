package org.n52.wps.io.data.binding.literal;

import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import org.n52.wps.io.data.ILiteralData;

public class LiteralTimeBinding implements ILiteralData {
	
    private transient DateTime time;

    public LiteralTimeBinding(DateTime time) {
		this.time = time;
	}

	@Override
	public DateTime getPayload() {
		return time;
	}

	@Override
	public Class<?> getSupportedClass() {
		return DateTime.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(ISODateTimeFormat.time().toString());
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		time = ISODateTimeFormat.time().parseDateTime((String) oos.readObject());
	}

}
