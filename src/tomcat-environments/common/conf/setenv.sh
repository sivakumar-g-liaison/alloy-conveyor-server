
## GGT-251 GGT-292 ## export CATALINA_OPTS="$CATALINA_OPTS -Xms64m"
## GGT-251 GGT-292 ## export CATALINA_OPTS="$CATALINA_OPTS -Xmx512m"

# Only set CATALINA_HOME if not already set
[ -z "$CATALINA_HOME" ] && CATALINA_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

# Copy CATALINA_BASE from CATALINA_HOME if not already set
[ -z "$CATALINA_BASE" ] && CATALINA_BASE="$CATALINA_HOME"

#set conversion contivo runtime classpath 
export CATALINA_OPTS="$CATALINA_OPTS -Dcontivo.runtime.classpath=$CATALINA_HOME/webapps/conversion/"

#set conversion contivo maps directory
export CATALINA_OPTS="$CATALINA_OPTS -Dcontivo.runtime.map.root=/opt/liaison/maps"
