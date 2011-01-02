package org.n52.wps.io.data.binding.literal;

import java.io.IOException;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

import org.n52.wps.io.data.ILiteralData;

public class LiteralDurationBinding implements ILiteralData {
	
    private transient Period period;

    public LiteralDurationBinding(Period period) {
		this.period = period;
	}

	@Override
	public Period getPayload() {
		return period;
	}

	@Override
	public Class<?> getSupportedClass() {
		return Period.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(ISOPeriodFormat.standard().toString());
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		period = ISOPeriodFormat.standard().parsePeriod((String) oos.readObject());
	}

}
