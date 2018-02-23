/*
    Copyright (C) 2006-2018 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
 */
package lohbihler.email;

import javax.mail.Message;

/**
 * @author Matthew Lohbihler
 */
@FunctionalInterface
public interface EmailMessageHandler {
    boolean handle(Message message);
}
