package de.schoenebaum.budgetbook.ui.components.chart.configuration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChartType {

    Line("line"),
    Doughnut("doughnut"),
    Pie("pie");

    @JsonValue
    private final String name;

    private ChartType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
