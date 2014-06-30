#!/usr/bin/python
#
# Copyright 2014 Liaison Technologies, Inc.
# This software is the confidential and proprietary information of
# Liaison Technologies, Inc. ("Confidential Information").  You shall
# not disclose such Confidential Information and shall use it only in
# accordance with the terms of the license agreement you entered into
# with Liaison Technologies.
#


import sys, getopt, os, shutil

def main( argv ):

    # Setup and initialize primary settings
    # --------------------------------------
    tnsname = 'kili1a.dev'
    moniker = 'GATEWAY'
    applpassword = '12345678'
    ownerpassword = '12345678'
    userprefix = ''
    sysname = 'system'
    syspassword = 'oracle'
    replacementToken = 'GATEWAY'

    # Retrieve command line arguments
    # --------------------------------
    try:
        opts, args = getopt.getopt( argv, "u:m:p:a:o:s:t:h", ["userprefix=","moniker=","applpassword=","ownerpassword=","syspassword=","tnsname="] )
    except getopt.GetoptError:
        printUsage()
        sys.exit( 2 )

    # Process command line arguments
    # -------------------------------
    for opt, arg in opts:
        if opt == '-h':
            printUsage()
            sys.exit();
        elif opt in ("-u", "--userprefix"):
            userprefix = arg;
        elif opt in ("-m", "--moniker"):
            moniker = arg;
        elif opt in ("-a", "--applpassword"):
            applpassword = arg;
        elif opt in ("-o", "--ownerpassword"):
            ownerpassword = arg;
        elif opt in ("-s", "--syspassword"):
            syspassword = arg;
        elif opt in ("-t", "--tnsname"):
            tnsname = arg;

    # Very important that these things be upper case.
    moniker = moniker.upper()
    userprefix = userprefix.upper()

    if len(userprefix) == 0:
        dbuserprefix = moniker
    else:
        # script fails if userprefix has a period, so trim it
        if userprefix.find(".") >= 0:
            userprefix = userprefix[:userprefix.find(".")]
        dbuserprefix = userprefix + '_' + moniker

    templatefolder = 'provision'
    workingfolder = '~work/provision/' + dbuserprefix
    logfile = '~provision.' + dbuserprefix + '.log.txt'

    # Display current settings
    # -------------------------
    print '===================================================='
    print 'logfile', logfile
    print 'tnsname', tnsname
    print 'userprefix', userprefix
    print 'moniker', moniker
    print 'dbuserprefix', dbuserprefix
    print 'applpassword', applpassword
    print 'ownerpassword', ownerpassword
    print 'sysname', sysname
    print 'syspassword', syspassword
    print 'tnsname', tnsname
    print '===================================================='

    msg = 'Provision the specified database user schema?'
    doProvision = True if raw_input("%s (y/N) " % msg).lower() == 'y' else False

    if doProvision:
        sqlplus = 'sqlplus ' + sysname + '/' + syspassword + '@' + tnsname

        # Provision Database
        # ---------------------------
        if not os.path.exists(workingfolder + '/flyway'):
            os.makedirs(workingfolder + '/flyway')

        # Tablespaces
        msg = 'Creating Tablespaces...'
        print '\n' + msg + '\n================================================'
        os.system('echo ' + msg + ' > ' + logfile)
        os.system('echo ================================================ >> ' + logfile)
        template_script = templatefolder + '/tablespaces.sql'
        target_script = workingfolder + '/tablespaces.sql'
        params = [(replacementToken, dbuserprefix)]
        translateFileUsingToken( template_script, target_script, params )
        if os.system( sqlplus + ' @' + target_script + ' >> ' + logfile) == 0:
            print '\n==> Tablespace Creation Successful\n'
        else:
            print '\n==> Problem with Tablespace Creation\n'
        os.system('echo. >> ' + logfile)

        # Profiles
        msg = 'Creating Profiles...'
        print '\n' + msg + '\n================================================'
        os.system('echo ' + msg + ' >> ' + logfile)
        os.system('echo ================================================ >> ' + logfile)
        template_script = templatefolder + '/profiles.sql'
        target_script = workingfolder + '/profiles.sql'
        params = [(replacementToken, dbuserprefix), ('{APPL_PASSWORD}', applpassword), ('{OWNR_PASSWORD}', ownerpassword)]
        translateFileUsingToken( template_script, target_script, params )
        if os.system( sqlplus + ' @' + target_script + ' >> ' + logfile) == 0:
            print '\n==> ' + target_script + ' Execution Successful\n'
        else:
            print '\n==> Problem with Profile Creation\n'
        os.system('echo. >> ' + logfile)

        # Roles and Users
        msg = 'Creating Roles and Users...'
        print '\n' + msg + '\n================================================'
        os.system('echo ' + msg + ' >> ' + logfile)
        os.system('echo ================================================ >> ' + logfile)
        template_script = templatefolder + '/users.sql'
        target_script = workingfolder + '/users.sql'
        params = [(replacementToken, dbuserprefix), ('{APPL_PASSWORD}', applpassword), ('{OWNR_PASSWORD}', ownerpassword)]
        translateFileUsingToken( template_script, target_script, params )
        if os.system( sqlplus + ' @' + target_script + ' >> ' + logfile) == 0:
            print '\n==> ' + target_script + ' Execution Successful\n'
        else:
            print '\n==> Problem with Role/User Creation\n'
        os.system('echo. >> ' + logfile)

        # Quotas
        msg = 'Assigning Quotas...'
        print '\n' + msg + '\n================================================'
        os.system('echo ' + msg + ' >> ' + logfile)
        os.system('echo ================================================ >> ' + logfile)
        template_script = templatefolder + '/quotas.sql'
        target_script = workingfolder + '/quotas.sql'
        params = [(replacementToken, dbuserprefix)]
        translateFileUsingToken( template_script, target_script, params )
        if os.system( sqlplus + ' @' + target_script + ' >> ' + logfile) == 0:
            print '\n==> ' + target_script + ' Execution Successful\n'
        else:
            print '\n==> Problem Specifying Quotas\n'
        os.system('echo. >> ' + logfile)

        # Flyway Tracking Table
        msg = 'Creating Flyway Tracking Table...'
        print '\n' + msg + '\n================================================'
        os.system('echo ' + msg + ' >> ' + logfile)
        os.system('echo ================================================ >> ' + logfile)
        template_script = templatefolder + '/flyway/schema_version_table.sql'
        target_script = workingfolder + '/flyway/schema_version_table.sql'
        params = [(replacementToken, dbuserprefix)]
        translateFileUsingToken( template_script, target_script, params )
        if os.system( sqlplus + ' @' + target_script + ' -X >> ' + logfile) == 0:
            print '\n==> ' + target_script + ' Execution Successful\n'
        else:
            print '\n==> Problem with ' + target_script + ' Execution\n'
        os.system('echo. >> ' + logfile)
        print '\nSee ' + logfile + 'for details'
    else:
        print '================================================'
        print 'Oracle schema provisioning skipped.'
        print '================================================'

# ------------------------------------------
# Function Merges template file with provided values
# ------------------------------------------
def translateFileUsingToken( infile, outfile, params ):
    if ( os.path.exists( outfile ) ):
        os.remove( outfile );

    with open( outfile, "wt" ) as out:
        for line in open( infile ):
            for name, value in params:
                line = line.replace( name, value )
            out.write( line )

    return;

# ------------------------------------------
# Function Print Usage
# ------------------------------------------
def printUsage():
    print 'Creates Oracle tablespaces and new schema users'
    print '    provision.py -u <userprefix> -m <moniker> -a <applpassword> -o <ownerpassword> -s <syspassword> -t <tnsname>'
    print 'Arguments:'
    print ' --moniker (-m):       an optional parameter that defines the base name from which'
    print '                       database usernames are built from. (default "GATEWAY")'
    print ' --userprefix (-u):    an additional optional parameter that is added to the'
    print '                       moniker as a prefix.'
    print '                       (This is useful for deploying the same schema multiple times'
    print '                       in a single database instance. If the userprefix contains'
    print '                       a period it will be truncated just before the period.)'
    print ' --ownerpassword (-o): the password for the schema owner user (default "12345678")'
    print ' --applpassword (-a):  the password for the application user (default "12345678")'
    print ' --syspassword (-s):   the password for the Oracle "system" user'
    print ' --tnsname (-t):       the TNS name for the Oracle instance'

    return;


# -----------------
# Main entry point
# -----------------
if __name__ == "__main__":
    main(sys.argv[1:])