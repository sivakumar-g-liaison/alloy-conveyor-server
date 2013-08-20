echo off

:: 
:: ASK FOR HELP?
::
if ""%1""=="""" goto USAGE
if ""%1""==""-help"" (
    goto USAGE
) else if ""%1""==""-h"" (
    goto USAGE
) else if ""%1""==""-?"" (
    goto USAGE
) 

:: 
:: RUN THE NAMED SCRIPT
::
call setenv.cmd -q 
call %*
goto END

:USAGE
echo.
echo.
echo .................................................................................
echo A script runner for executing scripts in an environment configured by sentenv.cmd
echo .................................................................................
echo.
echo Usage:
echo     %0 ^<script_path^> ^<command_arguments^>
echo.


:END