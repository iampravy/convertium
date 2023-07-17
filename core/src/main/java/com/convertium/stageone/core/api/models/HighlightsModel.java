package com.convertium.stageone.core.api.models;

import java.util.Calendar;

public class HighlightsModel {

    private String articleTitle;
    private String articleContent;
    private Calendar articlePublishDate;

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public void setArticleContent(String articleContent) {
        this.articleContent = articleContent;
    }

    public Calendar getArticlePublishDate() {
        return articlePublishDate;
    }

    public void setArticlePublishDate(Calendar articlePublishDate) {
        this.articlePublishDate = articlePublishDate;
    }
}
