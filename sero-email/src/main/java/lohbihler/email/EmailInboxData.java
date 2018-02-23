/*
    Copyright (C) 2006-2018 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
 */
package lohbihler.email;

public class EmailInboxData {
    private String protocol = "pop3";
    private String host;
    private int port = -1;
    private String user;
    private String password;
    private EmailMessageHandler handler;
    private boolean deleteOnError = true;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public EmailMessageHandler getHandler() {
        return handler;
    }

    public void setHandler(final EmailMessageHandler handler) {
        this.handler = handler;
    }

    public boolean isDeleteOnError() {
        return deleteOnError;
    }

    public void setDeleteOnError(final boolean deleteOnError) {
        this.deleteOnError = deleteOnError;
    }
}
