package de.schoenebaum.budgetbook.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;

public class UnclosableInputStream extends InputStream {

	private final InputStream inputStream;

	public UnclosableInputStream(InputStream inputStream) {
		Validate.notNull(inputStream);

		this.inputStream = inputStream;
	}

	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

	@Override
	public void close() throws IOException {
		// do nothing!
	}
}
