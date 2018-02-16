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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (attachments == null ? 0 : attachments.hashCode());
        result = prime * result + (encoding == null ? 0 : encoding.hashCode());
        result = prime * result + (htmlContent == null ? 0 : htmlContent.hashCode());
        result = prime * result + (inlineParts == null ? 0 : inlineParts.hashCode());
        result = prime * result + (plainContent == null ? 0 : plainContent.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final EmailContent other = (EmailContent) obj;
        if (attachments == null) {
            if (other.attachments != null)
                return false;
        } else if (!attachments.equals(other.attachments))
            return false;
        if (encoding == null) {
            if (other.encoding != null)
                return false;
        } else if (!encoding.equals(other.encoding))
            return false;
        if (htmlContent == null) {
            if (other.htmlContent != null)
                return false;
        } else if (!htmlContent.equals(other.htmlContent))
            return false;
        if (inlineParts == null) {
            if (other.inlineParts != null)
                return false;
        } else if (!inlineParts.equals(other.inlineParts))
            return false;
        if (plainContent == null) {
            if (other.plainContent != null)
                return false;
        } else if (!plainContent.equals(other.plainContent))
            return false;
        return true;
    }
}
