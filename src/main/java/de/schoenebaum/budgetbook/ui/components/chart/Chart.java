package de.schoenebaum.budgetbook.ui.components.chart;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;

import de.schoenebaum.budgetbook.ui.components.chart.configuration.ChartConfiguration;
import de.schoenebaum.budgetbook.ui.components.chart.configuration.Color;

@NpmPackage(value = "chart.js", version = "3.9.1")
@JsModule("chart.js/dist/chart.js")
public class Chart extends Div {

	private static final long serialVersionUID = -2206302697544303341L;
	private static final Logger LOG = LoggerFactory.getLogger(Chart.class);
	private static final ObjectMapper CONFIGURATION_MAPPER = new ObjectMapper()
		.setSerializationInclusion(JsonInclude.Include.NON_NULL)
		.registerModule(new JavaTimeModule())
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

	// Use https://medialab.github.io/iwanthue/, great tool!
	public static final Color[] COLORS = { new Color(86, 107, 207), new Color(89, 183, 75), new Color(204, 93, 201),
			new Color(172, 183, 59), new Color(138, 80, 186), new Color(213, 157, 51), new Color(96, 143, 204),
			new Color(204, 86, 43), new Color(64, 186, 200), new Color(212, 68, 90), new Color(93, 190, 143),
			new Color(209, 68, 142), new Color(62, 122, 66), new Color(168, 130, 204), new Color(133, 150, 71),
			new Color(219, 132, 180), new Color(137, 103, 42), new Color(153, 71, 105), new Color(215, 153, 98),
			new Color(200, 110, 101) };

	public static final Color[] COLORS_50_PERCENT_ALPHA;

	static {
		COLORS_50_PERCENT_ALPHA = new Color[COLORS.length];
		for (int i = 0; i < COLORS.length; i++) {
			COLORS_50_PERCENT_ALPHA[i] = Color.Transparentize(COLORS[i], 0.5f);
		}
	}

	protected final HtmlComponent canvas;
	protected final String canvasId;
	protected ChartConfiguration chartConfiguration;

	public Chart(ChartConfiguration chartConfiguration) {
		this.canvasId = createNewId();
		canvas = new HtmlComponent("canvas");
		canvas.setId(canvasId);
		canvas.setHeightFull();
		canvas.setWidthFull();
		this.add(canvas);
		
		this.setWidthFull();
		this.setHeight("400px");
		
		if (chartConfiguration != null) {
			setConfiguration(chartConfiguration);
		}
	}

	public Chart() {
		this(null);
	}

	public void setConfiguration(ChartConfiguration chartConfiguration) {
		Objects.requireNonNull(chartConfiguration);
		this.chartConfiguration = chartConfiguration;
		UI.getCurrent()
			.getPage()
			.executeJs(createInitJavascriptCode());
	}

	protected static String createNewId() {
		return "canvas-" + UUID.randomUUID()
			.toString()
			.replace("-", "");
	}

	protected String createInitJavascriptCode() {
		try {
			String config = CONFIGURATION_MAPPER.writeValueAsString(chartConfiguration);
			LOG.debug("Using config {}", config);
			return String.format("""
					let prevChart = Chart.getChart('%1$s');
					if(typeof prevChart !== 'undefined'){
						prevChart.destroy();
					}
					let ctx = document.getElementById('%1$s');
					new Chart(ctx, %2$s);
					""", this.canvasId, config);
		} catch (JsonProcessingException e) {
			LOG.error("Could not serialize ChartConfiguration {}", chartConfiguration, e);
			return "";
		}
	}

}
