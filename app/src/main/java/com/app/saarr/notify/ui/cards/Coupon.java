package com.app.saarr.notify.ui.cards;

public class Coupon {
    private int _Id;
    private int smallIcon;
    private String title;
    private String source;
    private String displayText;
    private String rawText;

    public int get_Id() {
        return _Id;
    }

    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    private int status;
    private Long creationTime;
    private String targetIntent;

    public Coupon(int smallIcon, String title, String displayText, String targetIntent) {
        this.smallIcon = smallIcon;
        this.title = title;
        this.displayText = displayText;
        this.creationTime = System.currentTimeMillis();
        this.targetIntent = targetIntent;
    }

    public int getSmallIcon() {
        return smallIcon;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getTargetIntent() {
        return targetIntent;
    }

    public void setTargetIntent(String targetIntent) {
        this.targetIntent = targetIntent;
    }
}
