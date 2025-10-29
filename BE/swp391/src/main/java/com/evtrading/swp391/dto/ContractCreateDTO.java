package com.evtrading.swp391.dto;

public class ContractCreateDTO {
    private Integer orderId;
    private String templateId;
    private String signerEmail;
    private String signerName;
    private String content;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getSignerEmail() { return signerEmail; }
    public void setSignerEmail(String signerEmail) { this.signerEmail = signerEmail; }
    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
