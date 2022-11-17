package de.schoenebaum.budgetbook.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;

public class LimitedInputStream extends InputStream {

	private final InputStream inputStream;
	private final long limit;
	private long readBytes = 0;

	public LimitedInputStream(InputStream inputStream, long limit) {
		Validate.notNull(inputStream);
		Validate.validState(limit > 0);

		this.inputStream = inputStream;
		this.limit = limit;
	}

	@Override
	public int read() throws IOException {
		if (readBytes < limit) {
			readBytes++;
			return inputStream.read();
		}
		return -1;
	}

}
