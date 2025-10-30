package com.sources.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "CLICK_EVENTS")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "click_event_seq")
    @SequenceGenerator(name = "click_event_seq", sequenceName = "SEQ_CLICK_EVENTS", allocationSize = 1)
    @Column(name = "ID_CLICK")
    private Long idClick;

    @Column(name = "USER_ID")
    private Integer userId; // opcional

    @Column(name = "SESSION_ID", length = 100)
    private String sessionId;

    @Column(name = "EVENT_TIME", length = 30, nullable = false)
    private String eventTime; // ISO-8601 string

    @Column(name = "URL_PATH", length = 500)
    private String urlPath;

    @Column(name = "FULL_URL", length = 1000)
    private String fullUrl;

    @Column(name = "PAGE_TITLE", length = 500)
    private String pageTitle;

    @Column(name = "ELEMENT_TAG", length = 50)
    private String elementTag;

    @Column(name = "ELEMENT_ID", length = 200)
    private String elementIdAttr;

    @Column(name = "ELEMENT_CLASSES", length = 500)
    private String elementClasses;

    @Column(name = "TEXT_SNIPPET", length = 500)
    private String textSnippet;

    @Column(name = "CSS_SELECTOR", length = 1000)
    private String cssSelector;

    @Column(name = "X_POS")
    private Integer xPos;

    @Column(name = "Y_POS")
    private Integer yPos;

    @Column(name = "VP_WIDTH")
    private Integer viewportWidth;

    @Column(name = "VP_HEIGHT")
    private Integer viewportHeight;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "IP_ADDRESS", length = 100)
    private String ipAddress;

    // Getters y setters
    public Long getIdClick() { return idClick; }
    public void setIdClick(Long idClick) { this.idClick = idClick; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    public String getUrlPath() { return urlPath; }
    public void setUrlPath(String urlPath) { this.urlPath = urlPath; }

    public String getFullUrl() { return fullUrl; }
    public void setFullUrl(String fullUrl) { this.fullUrl = fullUrl; }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

    public String getElementTag() { return elementTag; }
    public void setElementTag(String elementTag) { this.elementTag = elementTag; }

    public String getElementIdAttr() { return elementIdAttr; }
    public void setElementIdAttr(String elementIdAttr) { this.elementIdAttr = elementIdAttr; }

    public String getElementClasses() { return elementClasses; }
    public void setElementClasses(String elementClasses) { this.elementClasses = elementClasses; }

    public String getTextSnippet() { return textSnippet; }
    public void setTextSnippet(String textSnippet) { this.textSnippet = textSnippet; }

    public String getCssSelector() { return cssSelector; }
    public void setCssSelector(String cssSelector) { this.cssSelector = cssSelector; }

    public Integer getxPos() { return xPos; }
    public void setxPos(Integer xPos) { this.xPos = xPos; }

    public Integer getyPos() { return yPos; }
    public void setyPos(Integer yPos) { this.yPos = yPos; }

    public Integer getViewportWidth() { return viewportWidth; }
    public void setViewportWidth(Integer viewportWidth) { this.viewportWidth = viewportWidth; }

    public Integer getViewportHeight() { return viewportHeight; }
    public void setViewportHeight(Integer viewportHeight) { this.viewportHeight = viewportHeight; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}





