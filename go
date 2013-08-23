#!/bin/bash

# 
# ASK FOR HELP?
#
if [ $# -ge 1 -a "$1" != "-?" -a "$1" != "--help" ];
then

    # 
    # RUN THE NAMED SCRIPT
    #
    source ./setenv -q 
    source $*

else
    echo ''
    echo ''
    echo '.................................................................................'
    echo 'A script runner for executing scripts in an environment configured by sentenv'
    echo '.................................................................................'
    echo ''
    echo 'Usage:'
    echo '    %0 \<script_path\> \<command_arguments\>'
    echo ''
fi
