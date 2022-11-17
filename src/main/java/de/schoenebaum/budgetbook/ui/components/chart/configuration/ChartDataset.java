package de.schoenebaum.budgetbook.ui.components.chart.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collection;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class ChartDataset {

    @JsonProperty
    private String label;

    @JsonProperty
    @Singular("dataPoint")
    private Collection<Object> data;

    @JsonProperty
    @Singular("backgroundColorEntry")
    private Collection<Color> backgroundColor;

    @JsonProperty
    @Singular("borderColorEntry")
    private Collection<Color> borderColor;

    @JsonProperty
    @Singular("colorEntry")
    private Collection<Color> color;

    @JsonProperty
    private Integer borderWidth;
}
