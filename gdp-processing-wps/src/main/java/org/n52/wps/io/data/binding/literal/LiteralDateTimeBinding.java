package org.n52.wps.io.data.binding.literal;

import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import org.n52.wps.io.data.ILiteralData;

public class LiteralDateTimeBinding implements ILiteralData {
	
    private transient DateTime dateTime;

    public LiteralDateTimeBinding(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public DateTime getPayload() {
		return dateTime;
	}

	@Override
	public Class<?> getSupportedClass() {
		return DateTime.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(ISODateTimeFormat.dateTime().toString());
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		dateTime = ISODateTimeFormat.dateTime().parseDateTime((String) oos.readObject());
	}

}
