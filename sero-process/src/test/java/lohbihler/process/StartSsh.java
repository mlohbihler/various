/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.process;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class StartSsh {
    public static void main(final String[] args) throws Exception {
        final ProcessRunner pr = new ProcessRunner(new ProcessBuilder("ssh", "-f", "-N", "-T", "-i", "support-id",
                "-R0:localhost:22", "-o", "StrictHostKeyChecking no", "-p", "30022", "bacchanal@serotonin.homeip.net") //
                        .directory(new File("/home/pi/test")) //
                        .redirectError(new File("/home/pi/test", "support-port")));
        pr.waitFor(10, TimeUnit.SECONDS);
        System.out.println(pr.exitCode());
    }
}
