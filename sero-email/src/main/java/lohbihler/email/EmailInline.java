/*
    Copyright (C) 2006-2018 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
 */
package lohbihler.email;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.mail.MessagingException;

import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * @author Matthew Lohbihler
 */
abstract public class EmailInline {
    protected String contentId;

    public EmailInline(final String contentId) {
        this.contentId = contentId;
    }

    public String getContentId() {
        return contentId;
    }

    abstract public void attach(MimeMessageHelper mimeMessageHelper) throws MessagingException;

    public static class FileInline extends EmailInline {
        private final File file;

        public FileInline(final String contentId, final String filename) {
            this(contentId, new File(filename));
        }

        public FileInline(final String contentId, final File file) {
            super(contentId);
            this.file = file;
        }

        @Override
        public void attach(final MimeMessageHelper mimeMessageHelper) throws MessagingException {
            mimeMessageHelper.addInline(contentId, file);
        }
    }

    public static class ByteArrayInline extends EmailInline {
        final byte[] content;
        private final String contentType;

        public ByteArrayInline(final String contentId, final byte[] content) {
            super(contentId);
            this.content = content;

            final ConfigurableMimeFileTypeMap fileTypeMap = new ConfigurableMimeFileTypeMap();
            fileTypeMap.afterPropertiesSet();
            this.contentType = fileTypeMap.getContentType(contentId);
        }

        public ByteArrayInline(final String contentId, final byte[] content, final String contentType) {
            super(contentId);
            this.content = content;
            this.contentType = contentType;
        }

        @Override
        public void attach(final MimeMessageHelper mimeMessageHelper) throws MessagingException {
            mimeMessageHelper.addInline(contentId, new InputStreamSource() {
                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(content);
                }
            }, contentType);
        }
    }
}
