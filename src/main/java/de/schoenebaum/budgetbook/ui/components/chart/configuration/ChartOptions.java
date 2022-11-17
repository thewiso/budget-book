package de.schoenebaum.budgetbook.ui.components.chart.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class ChartOptions {

	@JsonProperty
    private Boolean maintainAspectRatio;

	
	
}
