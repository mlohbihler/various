/*
    Copyright (C) 2006-2007 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
 */
package lohbihler.email;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class EmailSender {
    private final JavaMailSenderImpl senderImpl;
    private final InternetAddress defaultFrom;
    private final InternetAddress defaultReplyTo;

    //
    //
    // Constructors
    //
    private EmailSender(final JavaMailSenderImpl senderImpl, final InternetAddress defaultFrom,
            final InternetAddress defaultReplyTo) {
        this.senderImpl = senderImpl;
        this.defaultFrom = defaultFrom;
        this.defaultReplyTo = defaultReplyTo;
    }

    public void send(final EmailBuilder message) {
        final InternetAddress from = message.getFrom() == null ? defaultFrom : message.getFrom();
        final InternetAddress replyTo = message.getReplyTo() == null ? defaultReplyTo : message.getReplyTo();
        send(createPreparator(from, replyTo, message.getTo(), message.getCc(), message.getBcc(), message.getSubject(),
                message.getContent()));
    }

    public MimeMessagePreparator createPreparator(final InternetAddress from, final InternetAddress replyTo,
            final InternetAddress[] to, final InternetAddress[] cc, final InternetAddress[] bcc, final String subject,
            final EmailContent content) throws MailException {
        return new MimeMessagePreparator() {
            @Override
            public void prepare(final MimeMessage mimeMessage) throws Exception {
                final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, content.isMultipart(),
                        content.getEncoding());
                helper.setFrom(from);
                if (replyTo != null)
                    helper.setReplyTo(replyTo);
                helper.setTo(to);
                if (cc != null)
                    helper.setCc(cc);
                if (bcc != null)
                    helper.setBcc(bcc);

                // Ensure that line breaks in the subject are removed.
                String sub;
                if (subject == null)
                    sub = "";
                else {
                    sub = subject.replaceAll("\\r", "");
                    sub = sub.replaceAll("\\n", "");
                }

                helper.setSubject(sub);

                if (content.getHtmlContent() == null)
                    helper.setText(content.getPlainContent(), false);
                else if (content.getPlainContent() == null)
                    helper.setText(content.getHtmlContent(), true);
                else
                    helper.setText(content.getPlainContent(), content.getHtmlContent());

                for (final EmailAttachment att : content.getAttachments())
                    att.attach(helper);
                for (final EmailInline inline : content.getInlines())
                    inline.attach(helper);
            }
        };
    }

    public void send(final MimeMessagePreparator mimeMessagePreparator) throws MailException {
        senderImpl.send(mimeMessagePreparator);
    }

    public void send(final MimeMessagePreparator[] mimeMessagePreparators) throws MailException {
        senderImpl.send(mimeMessagePreparators);
    }

    public static class SenderBuilder {
        private String host;
        private int port = -1;
        private boolean useAuth;
        private String username;
        private String password;
        private boolean tls;
        private InternetAddress defaultFrom;
        private InternetAddress defaultReplyTo;
        private final Properties props = new Properties();

        public SenderBuilder withHost(final String host) {
            this.host = host;
            return this;
        }

        public SenderBuilder withPort(final int port) {
            this.port = port;
            return this;
        }

        public SenderBuilder withAuth(final String username, final String password) {
            if (!isBlank(username) || !isBlank(password)) {
                useAuth = true;
                this.username = username;
                this.password = password;
            }
            return this;
        }

        private static boolean isBlank(final String s) {
            return s == null || s.trim().length() == 0;
        }

        public SenderBuilder withTls(final boolean tls) {
            this.tls = tls;
            return this;
        }

        public SenderBuilder withDefaultFrom(final String defaultFrom) {
            try {
                this.defaultFrom = new InternetAddress(defaultFrom);
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public SenderBuilder withDefaultFrom(final String defaultFrom, final String personal) {
            try {
                this.defaultFrom = new InternetAddress(defaultFrom, personal);
            } catch (final UnsupportedEncodingException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public SenderBuilder withDefaultFrom(final InternetAddress defaultFrom) {
            this.defaultFrom = defaultFrom;
            return this;
        }

        public SenderBuilder withDefaultReplyTo(final String defaultReplyTo) {
            try {
                this.defaultReplyTo = new InternetAddress(defaultReplyTo);
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public SenderBuilder withDefaultReplyTo(final String defaultReplyTo, final String personal) {
            try {
                this.defaultReplyTo = new InternetAddress(defaultReplyTo, personal);
            } catch (final UnsupportedEncodingException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public SenderBuilder withDefaultReplyTo(final InternetAddress defaultReplyTo) {
            this.defaultReplyTo = defaultReplyTo;
            return this;
        }

        public SenderBuilder withProperty(final Object key, final Object value) {
            props.put(key, value);
            return this;
        }

        public EmailSender build() {
            final JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();
            if (useAuth) {
                props.put("mail.smtp.auth", "true");
                senderImpl.setUsername(username);
                senderImpl.setPassword(password);
            }
            if (tls) {
                props.put("mail.smtp.starttls.enable", "true");
            }
            senderImpl.setJavaMailProperties(props);
            senderImpl.setHost(host);
            if (port != -1) {
                senderImpl.setPort(port);
            }

            return new EmailSender(senderImpl, defaultFrom, defaultReplyTo);
        }
    }
}
