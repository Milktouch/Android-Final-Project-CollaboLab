package com.collabolab.model;

public class Update {
    private String title;
    private String description;
    private String viewType;

    private String id;

    public Update(String title, String description, String viewType,String id) {
        this.title = title;
        this.description = description;
        this.viewType = viewType;
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public String getViewType() {
        return viewType;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }
}
