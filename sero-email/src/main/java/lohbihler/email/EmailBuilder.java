package lohbihler.email;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;

public class EmailBuilder {
    private InternetAddress from;
    private InternetAddress replyTo;
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
        ensureContent();
        content.setPlainContent(plainText);
        return this;
    }

    public EmailBuilder withHtmlText(final String htmlText) {
        ensureContent();
        content.setHtmlContent(htmlText);
        return this;
    }

    public EmailBuilder withEncoding(final String encoding) {
        ensureContent();
        content.setEncoding(encoding);
        return this;
    }

    public EmailBuilder withAttachment(final EmailAttachment attachment) {
        ensureContent();
        content.addAttachment(attachment);
        return this;
    }

    public EmailBuilder withInline(final EmailInline inline) {
        ensureContent();
        content.addInline(inline);
        return this;
    }

    private void ensureContent() {
        if (content == null) {
            content = new EmailContent();
        }
    }

    public InternetAddress getFrom() {
        return from;
    }

    public InternetAddress getReplyTo() {
        return replyTo;
    }

    public InternetAddress[] getTo() {
        return to;
    }

    public InternetAddress[] getCc() {
        return cc;
    }

    public InternetAddress[] getBcc() {
        return bcc;
    }

    public String getSubject() {
        return subject;
    }

    public EmailContent getContent() {
        return content;
    }

    public void send(final EmailSender emailSender) throws MailException {
        emailSender.send(this);
    }
}
