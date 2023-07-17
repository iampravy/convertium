package com.convertium.stageone.core.api.models;

import java.util.ArrayList;
import java.util.List;

public class Highlights {

    private List<HighlightsModel> highlightsModels = new ArrayList<>();

    public List<HighlightsModel> getHighlightsModels() {
        return highlightsModels;
    }

    public void setHighlightsModels(List<HighlightsModel> highlightsModels) {
        this.highlightsModels = highlightsModels;
    }

}
