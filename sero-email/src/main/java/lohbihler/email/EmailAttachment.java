/*
    Copyright (C) 2006-2018 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
 */
package lohbihler.email;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.mail.MessagingException;

import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * @author Matthew Lohbihler
 */
abstract public class EmailAttachment {
    protected String filename;

    public EmailAttachment(final String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    abstract public void attach(MimeMessageHelper mimeMessageHelper) throws MessagingException;

    public static class FileAttachment extends EmailAttachment {
        private final File file;

        public FileAttachment(final String filename, final String systemFilename) {
            this(filename, new File(systemFilename));
        }

        public FileAttachment(final File file) {
            super(file.getName());
            this.file = file;
        }

        public FileAttachment(final String filename, final File file) {
            super(filename);
            this.file = file;
        }

        @Override
        public void attach(final MimeMessageHelper mimeMessageHelper) throws MessagingException {
            mimeMessageHelper.addAttachment(filename, file);
        }
    }

    public static class DataSourceAttachment extends EmailAttachment {
        private final DataSource dataSource;

        public DataSourceAttachment(final String filename, final DataSource dataSource) {
            super(filename);
            this.dataSource = dataSource;
        }

        @Override
        public void attach(final MimeMessageHelper mimeMessageHelper) throws MessagingException {
            mimeMessageHelper.addAttachment(filename, dataSource);
        }
    }

    public static class ByteArrayAttachment extends EmailAttachment {
        final byte[] data;
        private final String contentType;

        public ByteArrayAttachment(final String filename, final byte[] data) {
            this(filename, data, null);
        }

        public ByteArrayAttachment(final String filename, final byte[] data, final String contentType) {
            super(filename);
            this.data = data;
            this.contentType = contentType;
        }

        @Override
        public void attach(final MimeMessageHelper mimeMessageHelper) throws MessagingException {
            final InputStreamSource source = new InputStreamSource() {
                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(data);
                }
            };

            if (contentType == null)
                mimeMessageHelper.addAttachment(filename, source);
            else
                mimeMessageHelper.addAttachment(filename, source, contentType);
        }
    }
}
