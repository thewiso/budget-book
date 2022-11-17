package de.schoenebaum.budgetbook.ui.components.chart.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collection;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class ChartData {

    @JsonProperty
    @Singular
    private Collection<String> labels;

    @JsonProperty
    @Singular
    private Collection<ChartDataset> datasets;
}
