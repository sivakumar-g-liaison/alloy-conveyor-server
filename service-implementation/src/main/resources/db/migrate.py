#!/usr/bin/python

import sys, getopt, os
from os import walk

def main( argv ):

    # Setup and initialize primary settings
    # --------------------------------------
    dbserver = 'seadv01-db03.hs.com'
    oraservice = 'kili1.dev.liaison.net'
    moniker = ''
    password = '12345678'
    userprefix = ''
    replacementToken = 'GATEWAY'

    # Retrieve command line arguments
    # --------------------------------
    try:
        opts, args = getopt.getopt( argv, "u:m:p:o:d:h", ["userprefix=","moniker=","password=","oraservice=","dbserver="] )
    except getopt.GetoptError:
        printUsage()
        sys.exit( 2 )

    # Process command line arguments
    # -------------------------------
    isMonikerSupplied = False
    for opt, arg in opts:
        if opt == '-h':
            printUsage()
            sys.exit();
        elif opt in ("-u", "--userprefix"):
            userprefix = arg;
        elif opt in ("-m", "--moniker"):
            moniker = arg;
            isMonikerSupplied = True
        elif opt in ("-p", "--password"):
            password = arg;
        elif opt in ("-o", "--oraservice"):
            oraservice = arg;
        elif opt in ("-d", "--dbserver"):
            dbserver = arg;

    if isMonikerSupplied == False:
        printUsage()
        sys.exit( 2 )

    if len(userprefix) == 0:
        dbuserprefix = moniker
    else:
        # script fails if userprefix has a period, so trim it
        if userprefix.find(".") >= 0:
            userprefix = userprefix[:userprefix.find(".")]
        dbuserprefix = userprefix + '_' + moniker

    templatefolder = 'migration/'
    workingfolder = '~work/migration/' + dbuserprefix
    logfile = '~migration.' + dbuserprefix + '.log.txt'

    # Very important that these things be upper case.
    moniker = moniker.upper()
    dbuserprefix = dbuserprefix.upper()

    # Display current settings
    # -------------------------
    print '===================================================='
    print 'logfile', logfile
    print 'dbserver', dbserver
    print 'oraservice', oraservice
    print 'userprefix', userprefix
    print 'moniker', moniker
    print 'dbuserprefix', dbuserprefix
    print 'password', password
    print '===================================================='

    msg = 'Upgrade objects in the specified database?'
    doUpgrade = True if raw_input("%s (y/N) " % msg).lower() == 'y' else False

    if doUpgrade:
        # Create Database Objects
        # --------------------------------
        msg = 'Creating Database Objects...'
        print '\n' + msg + '\n================================================'
        os.system('echo ' + msg + ' >> ' + logfile)
        os.system('echo ================================================ >> ' + logfile)
        params = [(replacementToken, dbuserprefix)]
        translateFilesUsingToken( templatefolder, workingfolder, params )
        dbUrl = 'jdbc:oracle:thin:@//' + dbserver + ':1521/' + oraservice

        # See http://flywaydb.org/documentation/commandline/ for more info
        migrationfolder = os.getcwd() + '/' + workingfolder
		flyway = 'flyway -url=' + dbUrl + ' -user=' + dbuserprefix + '_OWNR -password=' + password + ' -locations=filesystem:' + migrationfolder + ' migrate'
        if os.system( flyway + ' > ' + logfile) == 0:
            print 'Flyway Migration Successful'
        else:
            print 'Problem with Flyway Migration'
        print '\nSee ' + logfile + 'for details'
    else:
        print 'Flyway upgrade migration skipped.'


# ------------------------------------------
# Function Merges template file with provided values
# ------------------------------------------
def translateFilesUsingToken( sourcepath, targetpath, params ):
    if not os.path.exists(targetpath):
        os.makedirs(targetpath)

    files = []
    for (dirpath, dirnames, filenames) in walk(sourcepath):
        files.extend(filenames)
        break

    for f in files:
        outfile = targetpath + '/' + f
        if ( os.path.exists( outfile ) ):
            os.remove( outfile );

        infile = sourcepath + '/' + f
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
    print 'Upgrades the schema objects'
    print '    migrate.py -u <userprefix> -m <moniker> -p <password> -o <oraservice> -d <dbserver>'
    print 'Arguments:'
    print ' --moniker (-m):       an required parameter that defines the base name from which'
    print '                       database usernames are built from. (must be "SB_EDM" or "SB_RTDM")'
    print ' --userprefix (-u):    an additional optional parameter that is added to the'
    print '                       moniker as a prefix.'
    print '                       (This is useful for deploying the same schema multiple times'
    print '                       in a single database instance. If the userprefix contains'
    print '                       a period it will be truncated just before the period.)'
    print ' --password (-p):      the password for the schema owner user (default "12345678")'
    print ' --oraservice (-o):    the Oracle service name (not TNS)'
    print ' --dbserver (-d):      the domain name or IP address for the Oracle server'

    return;


# -----------------
# Main entry point
# -----------------
if __name__ == "__main__":
    main(sys.argv[1:])