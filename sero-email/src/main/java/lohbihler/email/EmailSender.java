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

        public EmailSender build() {
            final JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();
            final Properties props = new Properties();
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

    public class EmailBuilder {
        private InternetAddress from = defaultFrom;
        private InternetAddress replyTo = defaultReplyTo;
        private InternetAddress[] to;
        private InternetAddress[] cc;
        private InternetAddress[] bcc;
        private String subject;
        private EmailContent content;

        public EmailBuilder withFrom(final String from) {
            try {
                this.from = new InternetAddress(from);
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withFrom(final String from, final String personal) {
            try {
                this.from = new InternetAddress(from, personal);
            } catch (final UnsupportedEncodingException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withFrom(final InternetAddress from) {
            this.from = from;
            return this;
        }

        public EmailBuilder withReplyTo(final String replyTo) {
            try {
                this.replyTo = new InternetAddress(replyTo);
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withReplyTo(final String replyTo, final String personal) {
            try {
                this.replyTo = new InternetAddress(replyTo, personal);
            } catch (final UnsupportedEncodingException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withReplyTo(final InternetAddress replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public EmailBuilder withTo(final String to) {
            try {
                this.to = new InternetAddress[] { new InternetAddress(to) };
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withTo(final String[] to) {
            try {
                this.to = new InternetAddress[to.length];
                for (int i = 0; i < to.length; i++) {
                    this.to[i] = new InternetAddress(to[i]);
                }
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withTo(final InternetAddress to) {
            this.to = new InternetAddress[] { to };
            return this;
        }

        public EmailBuilder withTo(final InternetAddress[] to) {
            this.to = to;
            return this;
        }

        public EmailBuilder withCC(final String cc) {
            try {
                this.cc = new InternetAddress[] { new InternetAddress(cc) };
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withCC(final String[] cc) {
            try {
                this.cc = new InternetAddress[cc.length];
                for (int i = 0; i < cc.length; i++) {
                    this.cc[i] = new InternetAddress(cc[i]);
                }
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withCC(final InternetAddress cc) {
            this.cc = new InternetAddress[] { cc };
            return this;
        }

        public EmailBuilder withCC(final InternetAddress[] cc) {
            this.cc = cc;
            return this;
        }

        public EmailBuilder withBCC(final String bcc) {
            try {
                this.bcc = new InternetAddress[] { new InternetAddress(bcc) };
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withBCC(final String[] bcc) {
            try {
                this.bcc = new InternetAddress[bcc.length];
                for (int i = 0; i < bcc.length; i++) {
                    this.bcc[i] = new InternetAddress(bcc[i]);
                }
            } catch (final AddressException e) {
                throw new MailPreparationException(e);
            }
            return this;
        }

        public EmailBuilder withBCC(final InternetAddress bcc) {
            this.bcc = new InternetAddress[] { bcc };
            return this;
        }

        public EmailBuilder withBCC(final InternetAddress[] bcc) {
            this.bcc = bcc;
            return this;
        }

        public EmailBuilder withSubject(final String subject) {
            this.subject = subject;
            return this;
        }

        public EmailBuilder withPlainText(final String plainText) {
            if (content == null) {
                content = new EmailContent();
            }
            content.setPlainContent(plainText);
            return this;
        }

        public EmailBuilder withHtmlText(final String htmlText) {
            if (content == null) {
                content = new EmailContent();
            }
            content.setHtmlContent(htmlText);
            return this;
        }

        public EmailBuilder withEncoding(final String encoding) {
            if (content == null) {
                content = new EmailContent();
            }
            content.setEncoding(encoding);
            return this;
        }

        public void send() throws MailException {
            EmailSender.this.send(createPreparator(from, replyTo, to, cc, bcc, subject, content));
        }
    }
}
