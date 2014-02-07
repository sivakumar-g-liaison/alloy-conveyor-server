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
:: RUN GRADLE
::
:: Use "gradle" instead of "gradlew" if you are NOT using the Gradle Wrapper.
call gradlew %* -I init.gradle
goto END

:USAGE
echo.
echo.
echo .................................................................................
echo A gradle runner for including init.gradle in the command line automatically
echo .................................................................................
echo.
echo Usage:
echo     %0 ^<script_path^> ^<command_arguments^>
echo.

:END