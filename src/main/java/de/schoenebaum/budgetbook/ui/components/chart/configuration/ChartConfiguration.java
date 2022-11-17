package de.schoenebaum.budgetbook.ui.components.chart.configuration;

import javax.validation.constraints.NotNull;

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
public class ChartConfiguration {

    @JsonProperty
    @NotNull
    private ChartType type;

    @JsonProperty
    @NotNull
    private ChartData data;
    
    @JsonProperty
    private ChartOptions options;

}
