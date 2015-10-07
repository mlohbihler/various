#Overview

In short, this is a backup/restore mechanism.

It's a very small Java app to zip up a set of directories, encrypt the file, and upload it to AWS Glacier. The code is
functional, but little effort has been made to handle specific error conditions. Upon error is pretty much just logs to
a file and/or emails the error details.

This is intended to be run from a cron or something similar. To use it, you'll need:

- An AWS account
- An SMTP (to send confirmation or error emails)

# Configuration

The configuration file is a JSON file that needs to be on the classpath. It looks like this:

    {
      "prefix":"nas-backup",
      "maxFiles":5,
      "password":"** the password for the secret key encryption **",
      "smtp":{
        "host":"smtp.gmail.com",
        "port":587,
        "useAuth":true,
        "username":"** your username **",
        "password":"** your password **",
        "tls":true,
        "to":"** email address to send to **",
        "from":"** the email address from which to send - probably the same as username **"
      },
      "files":[
        {
          "prefix":"Z:/",  // Included to find the paths, but not in the zip file
          "paths":[  // Paths are files or directories. Directories are deep-copied.
            "dir1",
            "dir2/subdir1",
            "dir3",
            "dir4"
          ]
        }
      ],
      "glacier":{
        "accessKey":"** your AWS access key **",
        "secretKey":"** your AWS secret key **",
        "endpoint":"https://glacier.us-west-2.amazonaws.com/",
        "vaultName":"** name of your AWS Glacier vault **"
      }
    }

# Execution

This section assumes that you've put all of the gradle dependencies (jar files) into a 'lib' subdirectory. Your installation dir should look like this:

- ai/serotonin/backup (contains the three Java files and/or the compiled .class versions)
- config.json (the above config file)
- lib (containing of the jar dependencies)

A file named 'log.txt' will be created that describes what happened during execution. 

Note that some Glacier operations can take around 4 hours to complete. You will see log messages that look like "Job not completed. Waiting 15 minutes..." as it polls for job completion.

To backup, run something along the lines of (*nix):
java -classpath .:lib/* ai.serotonin.backup.Backup &

To restore the last file that was backed up, run (*nix):
java -classpath .:lib/* ai.serotonin.backup.Restore &

The restore will leave the backup zip file in the current directory.

# License

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
