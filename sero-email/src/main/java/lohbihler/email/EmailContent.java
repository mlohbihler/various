/*
    Copyright (C) 2006-2007 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
 */
package lohbihler.email;

import java.util.ArrayList;
import java.util.List;

public class EmailContent {
    protected String plainContent;
    protected String htmlContent;
    private final List<EmailAttachment> attachments = new ArrayList<>(2);
    private final List<EmailInline> inlineParts = new ArrayList<>(2);
    protected String encoding;

    protected EmailContent() {
        // no op
    }

    public EmailContent(final String plainContent) {
        this(plainContent, null, null);
    }

    public EmailContent(final String plainContent, final String htmlContent) {
        this(plainContent, htmlContent, null);
    }

    public EmailContent(final String plainContent, final String htmlContent, final String encoding) {
        this.plainContent = plainContent;
        this.htmlContent = htmlContent;
        this.encoding = encoding;
    }

    public void setPlainContent(final String plainContent) {
        this.plainContent = plainContent;
    }

    public void setHtmlContent(final String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public boolean isMultipart() {
        return plainContent != null && htmlContent != null || !attachments.isEmpty() || !inlineParts.isEmpty();
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public String getPlainContent() {
        return plainContent;
    }

    public void addAttachment(final EmailAttachment emailAttachment) {
        attachments.add(emailAttachment);
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void addInline(final EmailInline emailInline) {
        inlineParts.add(emailInline);
    }

    public List<EmailInline> getInlines() {
        return inlineParts;
    }

    public String getEncoding() {
        return encoding;
    }
}
