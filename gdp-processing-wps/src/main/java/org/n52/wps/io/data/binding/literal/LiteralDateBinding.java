package org.n52.wps.io.data.binding.literal;

import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import org.n52.wps.io.data.ILiteralData;

public class LiteralDateBinding implements ILiteralData {
	
    private transient DateTime date;

    public LiteralDateBinding(DateTime date) {
		this.date = date;
	}

	@Override
	public DateTime getPayload() {
		return date;
	}

	@Override
	public Class<?> getSupportedClass() {
		return DateTime.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(ISODateTimeFormat.date().toString());
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		date = ISODateTimeFormat.date().parseDateTime((String) oos.readObject());
	}

}
