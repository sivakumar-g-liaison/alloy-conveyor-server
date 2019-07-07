echo off

::
:: CACHE ORIGINAL PATH
::
IF "%_ORIGINAL_PATH%" == "" set _ORIGINAL_PATH=%Path%


::
:: DEFAULT SETTINGS
::
set _PLATFORM=windows
set _ROOT=%CD%
set _TARGET=%_ROOT%\install
set DEPLOY_TARGET=%_ROOT%\install
set INSTALL_HOME=%_ROOT%\install
set SCRIPTS_HOME=%_ROOT%\tools\scripts
set DISTROS_HOME=%INSTALL_HOME%\3rdParty


::
:: DETERMINE PROCESSOR ARCHITECTURE
::
set OS_ARCH=x86
set JDK_ARCH=i586
if not ""%PROCESSOR_ARCHITECTURE%"" == ""x86"" (
    set OS_BITS=64
    set OS_64_BIT=x64
    set JDK_ARCH=x64
    if ""%PROCESSOR_ARCHITECTURE%"" == ""AMD64"" (set OS_ARCH=x64) ELSE (set OS_ARCH=i64)
)


::
:: COMMAND LINE ARGUMENTS
::
set ARGS=
set QUIET=
set USE_DEFAULTS=
:PARSE_ARGS
if ""%1""=="""" goto ARGS_DONE
if ""%1""==""-quiet"" (
    set QUIET=true
    set USE_DEFAULTS=true
) else if ""%1""==""-q"" (
    set QUIET=true
    set USE_DEFAULTS=true
) else if ""%1""==""-default"" (
    set USE_DEFAULTS=true
) else (
    set ARGS=%ARGS% %1
)
shift
goto PARSE_ARGS
:ARGS_DONE


::
:: PROPERTIES SETTINGS
::
for /F "tokens=1*" %%A IN ('type %_ROOT%\setenv.properties') DO set %%A


::
:: TOOLS
::

:: CURL
set EXE_CURL="%_ROOT%\tools\curl\curl.exe"
set TOOL_PATH=%_ROOT%\tools\curl

:: 7 ZIP
set VERSION_7ZIP=9.20
set PATH_7ZIP=%_ROOT%\tools\7zip-%VERSION_7ZIP%
if not exist "%PATH_7ZIP%" mkdir "%PATH_7ZIP%"
set EXE_7ZIP="%PATH_7ZIP%\7za.exe"
set URL_7ZIP=%ARTIFACT_REPO_URL%/thirdparty/org/7-zip/7-zip/%VERSION_7ZIP%/7-zip-%VERSION_7ZIP%.exe
if not exist %EXE_7ZIP% (
    echo ................................................................................
    echo Downloading %URL_7ZIP%
    %EXE_CURL% -o %EXE_7ZIP% %URL_7ZIP%
)
set TOOL_PATH=%TOOL_PATH%;%PATH_7ZIP%

:: Flyway CLI
:: NOTE: You need to execute 'gradlew stageFlyway' for this path to exist.
:: http://repo1.maven.org/maven2/com/googlecode/flyway/flyway-commandline/%VERSION_FLYWAY%/flyway-commandline-%VERSION_FLYWAY%.tar.gz
set PATH_FLYWAY=%_ROOT%\tools\flyway-%VERSION_FLYWAY%
set URL_FLYWAY=http://repo1.maven.org/maven2/com/googlecode/flyway/flyway-commandline/%VERSION_FLYWAY%/flyway-commandline-%VERSION_FLYWAY%.zip
set EXE_FLYWAY="%PATH_FLYWAY%\flyway.cmd"
:: is Flyway on the path?
for /F "tokens=1*" %%A IN ('echo "%PATH%" ^| find /c "Flyway"') DO (
    if ""%%A"" == ""0"" (
        if not exist "%EXE_FLYWAY%" (
            echo ................................................................................
            echo Downloading %URL_FLYWAY%
            %EXE_CURL% -o "%_ROOT%\tools\flyway-commandline-%VERSION_FLYWAY%.zip" %URL_FLYWAY%
            %EXE_7ZIP% x -y "%_ROOT%\tools\flyway-commandline-%VERSION_FLYWAY%.zip" -o%_ROOT%\tools
            del "%_ROOT%\tools\flyway-commandline-%VERSION_FLYWAY%.zip"
        )
    )
)

set TOOL_PATH=%PATH_FLYWAY%;%TOOL_PATH%


:: GRAPHVIZ
set PATH_GRAPHVIZ=%_ROOT%\tools\Graphviz%VERSION_GRAPHVIZ%
::if not exist "%PATH_GRAPHVIZ%" mkdir "%PATH_GRAPHVIZ%"
set EXE_DOT="%PATH_GRAPHVIZ%\bin\dot.exe"
set URL_GRAPHVIZ=%ARTIFACT_REPO_URL%/thirdparty/org/graphviz/graphviz-windows/%VERSION_GRAPHVIZ%/graphviz-windows-%VERSION_GRAPHVIZ%.zip
:: is Graphviz on the path?
for /F "tokens=1*" %%A IN ('echo "%PATH%" ^| find /c "Graphviz"') DO (
    if ""%%A"" == ""0"" (
        if not exist "%EXE_DOT%" (
            echo ................................................................................
            echo Downloading %URL_GRAPHVIZ%
            %EXE_CURL% -o "%_ROOT%\tools\graphviz-windows-%VERSION_GRAPHVIZ%.zip" %URL_GRAPHVIZ%
            %EXE_7ZIP% x -y "%_ROOT%\tools\graphviz-windows-%VERSION_GRAPHVIZ%.zip" -o%_ROOT%\tools
            del "%_ROOT%\tools\graphviz-windows-%VERSION_GRAPHVIZ%.zip"
        )
    )
    set TOOL_PATH=%TOOL_PATH%;%PATH_GRAPHVIZ%\bin
)

::
:: SETUP JDK
::
set TOOLS_JDK_HOME=%_ROOT%\tools\jdk-%JAVA_FULL_VERSION%
set URL_JDK=%ARTIFACT_REPO_URL%/thirdparty/com/oracle/java/jdk-%_PLATFORM%-%JDK_ARCH%/%JAVA_VER%u%JAVA_REL_VER%/jdk-%_PLATFORM%-%JDK_ARCH%-%JAVA_VER%u%JAVA_REL_VER%.zip
for /F "tokens=1,2,3" %%A IN ('java -version 2^>^&1 ^| find "java version ""1.7."') DO set MY_JDK_VERSION=%%~C
if ""%MY_JDK_VERSION%"" == """" set MY_JDK_VERSION="NONE"
set MY_JDK_VERSION=%MY_JDK_VERSION:""=%
for /F "tokens=1,2*" %%A IN ('reg query "HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Development Kit\%MY_JDK_VERSION%" ^/v JavaHome 2^> nul ^| find "JavaHome"') DO set REG_JAVA_HOME=%%~C
set MY_JDK_MAJOR=%MY_JDK_VERSION:~2,-5%
set MY_JDK_GOOD_ENOUGH=false
if "%MY_JDK_VERSION%" == "%JAVA_FULL_VERSION%" set MY_JDK_GOOD_ENOUGH=true
if exist "%TOOLS_JDK_HOME%\bin\java.exe" set MY_JDK_GOOD_ENOUGH=true
if exist "%TOOLS_JDK_HOME%\no-jdk.txt" set MY_JDK_GOOD_ENOUGH=true
if ""%USE_DEFAULTS%""==""true"" set MY_JDK_GOOD_ENOUGH=true
::echo MY_JDK_GOOD_ENOUGH=%MY_JDK_GOOD_ENOUGH%

set DOWNLOAD_JDK=n
if not "%MY_JDK_GOOD_ENOUGH%" == "true" (
    echo.
    if "%MY_JDK_MAJOR%" == "7" echo NOTE: JDK %MY_JDK_VERSION% was found. JDK %JAVA_FULL_VERSION% is recommended.
    if not "%MY_JDK_MAJOR%" == "7" echo JDK 7 not was found!
    call set /P DOWNLOAD_JDK="Do you want to download a local JDK %JAVA_FULL_VERSION% for this project? (y/N) ": %=%
)
if ""%USE_DEFAULTS%""==""true"" set DOWNLOAD_JDK=y
if exist "%TOOLS_JDK_HOME%\bin\java.exe" set DOWNLOAD_JDK=n
if exist "%TOOLS_JDK_HOME%\no-jdk.txt" set DOWNLOAD_JDK=n
::echo DOWNLOAD_JDK=%DOWNLOAD_JDK%

if /I ""%DOWNLOAD_JDK%""==""y"" (
    echo ................................................................................
    echo Downloading %URL_JDK%
    if exist "%TOOLS_JDK_HOME%" del /S /F /Q "%TOOLS_JDK_HOME%"
    if exist "%TOOLS_JDK_HOME%" rmdir /Q "%TOOLS_JDK_HOME%"
    %EXE_CURL% -o "%_ROOT%\tools\jdk-%_PLATFORM%-%JDK_ARCH%-%JAVA_VER%u%JAVA_REL_VER%.zip" %URL_JDK%
    :: keep directories intact on unzip
    %EXE_7ZIP% x -y "%_ROOT%\tools\jdk-%_PLATFORM%-%JDK_ARCH%-%JAVA_VER%u%JAVA_REL_VER%.zip" -o%_ROOT%\tools
    ren "%_ROOT%\tools\jdk-%JAVA_FULL_VERSION%-%_PLATFORM%-%JDK_ARCH%" jdk-%JAVA_FULL_VERSION%
    del "%_ROOT%\tools\jdk-%_PLATFORM%-%JDK_ARCH%-%JAVA_VER%u%JAVA_REL_VER%.zip"
)

set PROMPT_REMEMBER=true
if "%MY_JDK_GOOD_ENOUGH%" == "true" set PROMPT_REMEMBER=false
if /I ""%DOWNLOAD_JDK%""==""y"" set PROMPT_REMEMBER=false
if exist "%TOOLS_JDK_HOME%\bin\javac.exe" set PROMPT_REMEMBER=false
if exist "%TOOLS_JDK_HOME%\no-jdk.txt" set PROMPT_REMEMBER=false
::echo PROMPT_REMEMBER=%PROMPT_REMEMBER%

set REMEMBER=n
if /I ""%PROMPT_REMEMBER%""==""true"" (
    call set /P REMEMBER="Remember this decision? (Y/n) ": %=%
)
::echo REMEMBER=%REMEMBER%
if /I ""%REMEMBER%""==""y"" (
    echo.
    echo =================================================================================
    echo.
    echo You have chosen to not install a project local JDK. 
    echo Be sure your JDK is Java version 7 and has the unlimited security policy applied.
    echo.
    echo =================================================================================
    echo.
    if not exist "%TOOLS_JDK_HOME%" mkdir "%TOOLS_JDK_HOME%"
    echo "no java">"%TOOLS_JDK_HOME%\no-jdk.txt"
)


:: Use cascading rules of thumb to locate JDK home
:: First check local to the project
if exist ""%JAVA_HOME%\bin\java.exe""  (
    goto APPLY_JCE_POLICY
)
:: Parse JAVA_HOME from PATH, in the case where JAVA_HOME is set but wrong
:: http://stackoverflow.com/questions/5471556/pretty-print-windows-path-variable-how-to-split-on-in-cmd-shell
setlocal DisableDelayedExpansion
set "var=%PATH%"
set "var=%var:"=""%"
set "var=%var:^=^^%"
set "var=%var:&=^&%"
set "var=%var:|=^|%"
set "var=%var:<=^<%"
set "var=%var:>=^>%"
set "var=%var:;=^;^;%"
set var=%var:""="%
set "var=%var:"=""%"
set "var=%var:;;="";""%"
set "var=%var:^;^;=;%"
set "var=%var:""="%"
set "var=%var:"=""%"
set "var=%var:"";""=";"%"
set "var=%var:"""="%"
setlocal EnableDelayedExpansion
for %%a in ("!var!") do (
    endlocal
    for /F "tokens=1 delims=\" %%M IN ('echo == %%~a ^| find "jdk-%%JAVA_FULL_VERSION%%"') DO (
        set JAVA_HOME=%%~a
        set JAVA_HOME=%JAVA_HOME~0,-4%
    )
    set JAVA_HOME=%JAVA_HOME%
    setlocal EnableDelayedExpansion
)
if exist ""%JAVA_HOME%\bin\java.exe""  (
    goto APPLY_JCE_POLICY
)
if exist ""%TOOLS_JDK_HOME%\bin\java.exe""  (
    set JAVA_HOME=%TOOLS_JDK_HOME%
    goto APPLY_JCE_POLICY
)
if exist ""%REG_JAVA_HOME%\bin\java.exe""  (
    set JAVA_HOME=%REG_JAVA_HOME%
    goto APPLY_JCE_POLICY
)
if exist ""%JDK_HOME%\bin\java.exe""  (
    set JAVA_HOME=%JDK_HOME%
    goto APPLY_JCE_POLICY
)
if "%MY_JDK_VERSION%" == ""NONE""  (
    goto JDK_COMPLETE
)
if exist ""%ProgramFiles%\Java\jdk%MY_JDK_VERSION%""  (
    set JAVA_HOME=%ProgramFiles%\Java\jdk%MY_JDK_VERSION%
    goto APPLY_JCE_POLICY
)
if exist ""%ProgramFiles(x86)%\Java\jdk%MY_JDK_VERSION%"" (
    set JAVA_HOME=%ProgramFiles(x86)%\Java\jdk%MY_JDK_VERSION%
    goto APPLY_JCE_POLICY
)
if exist ""%ProgramW6432%\Java\jdk%MY_JDK_VERSION%"" (
    set JAVA_HOME=%ProgramW6432%\Java\jdk%MY_JDK_VERSION%
    goto APPLY_JCE_POLICY
)

:APPLY_JCE_POLICY
set INSTALL_JCE_POLICY=true
if not exist "%JAVA_HOME%\bin\javac.exe" set INSTALL_JCE_POLICY=false
if exist "%JAVA_HOME%\jre\lib\security\README.txt" set INSTALL_JCE_POLICY=false
if exist "%JAVA_HOME%\jre\lib\security\local_policy.jar.original" set INSTALL_JCE_POLICY=false
set URL_JCE_POLICY=%ARTIFACT_REPO_URL%/thirdparty/com/oracle/java/UnlimitedJCEPolicy/JDK7/UnlimitedJCEPolicy-JDK7.zip
if /I ""%INSTALL_JCE_POLICY%""==""true"" (
    echo ................................................................................
    echo Downloading %URL_JCE_POLICY%
    %EXE_CURL% -o "%_ROOT%\tools\UnlimitedJCEPolicy-JDK7.zip" "%URL_JCE_POLICY%"
    ren "%JAVA_HOME%\jre\lib\security\local_policy.jar" local_policy.jar.original
    ren "%JAVA_HOME%\jre\lib\security\US_export_policy.jar" US_export_policy.jar.original
    :: flatten directories on unzip
    %EXE_7ZIP% e -y "%_ROOT%\tools\UnlimitedJCEPolicy-JDK7.zip" -o"%JAVA_HOME%\jre\lib\security"
    rmdir "%JAVA_HOME%\jre\lib\security\UnlimitedJCEPolicy" 
    del "%_ROOT%\tools\UnlimitedJCEPolicy-JDK7.zip"
)

:JDK_COMPLETE

:: set the build path
set PATH=%TOOL_PATH%;%_ORIGINAL_PATH%

if ""%QUIET%"" == """" (
::    cls
    echo Running on %OS_ARCH% Architecture
    echo ................................................................................
    echo "%path%"
    echo ................................................................................
    echo JAVA_HOME "%JAVA_HOME%"
    java -version
    echo ................................................................................
)

:END

