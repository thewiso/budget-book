package de.schoenebaum.budgetbook.ui.components.chart.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class ChartTwoDimensionalDataPoint {

    @JsonProperty
    @NotNull
    private Object x;

    @JsonProperty
    @NotNull
    private Object y;
}
