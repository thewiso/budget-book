package de.schoenebaum.budgetbook.ui.components.chart.configuration;

import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonSerialize(using = Color.ColorSerializer.class)
public class Color {

	private final float r;
	private final float g;
	private final float b;
	private float alpha = 1;

	public static Color Transparentize(Color color, float alpha) {
		return new Color(color.r, color.g, color.b, alpha);
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "rgba(%.2f,%.2f,%.2f,%.2f", r, g, b, alpha);
	}

	public static class ColorSerializer extends StdSerializer<Color> {

		private static final long serialVersionUID = 4986356087800330742L;

		public ColorSerializer() {
			this(null);
		}

		protected ColorSerializer(Class<Color> t) {
			super(t);
		}

		@Override
		public void serialize(Color value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeString(value.toString());
		}

	}

}
