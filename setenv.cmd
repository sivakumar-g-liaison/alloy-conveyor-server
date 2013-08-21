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
) else if ""%1""==""-q"" (
    set QUIET=true
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

:: GRAPHVIZ
set VERSION_GRAPHVIZ=2.30
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
set DOWNLOAD_JDK=n
set JDK_HOME=%_ROOT%\tools\jdk-%JAVA_FULL_VERSION%
set URL_JDK=%ARTIFACT_REPO_URL%/thirdparty/com/oracle/java/jdk-%_PLATFORM%-%JDK_ARCH%/%JAVA_VER%u%JAVA_REL_VER%/jdk-%_PLATFORM%-%JDK_ARCH%-%JAVA_VER%u%JAVA_REL_VER%.zip
if not exist "%JDK_HOME%\no-jdk.txt" (
    if not exist "%JDK_HOME%\bin\java.exe" (
        :: if not automatically accepting default behavior, ask
        if not ""%USE_DEFAULTS%""==""true"" (
            call set /P DOWNLOAD_JDK="JDK 7 was not found. Download JDK for this project? (y/n) ": %=%
        ) else (
            set DOWNLOAD_JDK=y
        )
    )
) else (
    if not exist "%JAVA_HOME%\bin\java.exe" (
        echo You have chosen to skip downloading JDK 7 and the JAVA_HOME environment variable is not set. Please set JAVA_HOME to an install of JDK 7.
    )
)
if /I ""%DOWNLOAD_JDK%""==""y"" (
    echo ................................................................................
    echo Downloading %URL_JDK%
    %EXE_CURL% -o "%_ROOT%\tools\jdk-%_PLATFORM%-%JDK_ARCH%-%JAVA_VER%u%JAVA_REL_VER%.zip" %URL_JDK%
    %EXE_7ZIP% x -y "%_ROOT%\tools\jdk-%_PLATFORM%-%JDK_ARCH%-%JAVA_VER%u%JAVA_REL_VER%.zip" -o%_ROOT%\tools 
    ren "%_ROOT%\tools\jdk-%JAVA_FULL_VERSION%-%_PLATFORM%-%JDK_ARCH%" jdk-%JAVA_FULL_VERSION%
    del "%_ROOT%\tools\jdk-%_PLATFORM%-%JDK_ARCH%-%JAVA_VER%u%JAVA_REL_VER%.zip"
) else (
    if not exist "%JDK_HOME%\no-jdk.txt" (
        if not exist "%JDK_HOME%" mkdir "%JDK_HOME%"
        echo "no java">"%JDK_HOME%\no-jdk.txt"
    )
)

if exist "%JDK_HOME%\bin\java.exe"  (
    set JAVA_HOME=%JDK_HOME%
)
set TOOL_PATH=%TOOL_PATH%;%JAVA_HOME%\bin
:JDK_COMPLETE

:: set the build path
set PATH=%TOOL_PATH%;%_ORIGINAL_PATH%

:: GRADLE TEMPLATING
if not exist "gradle.properties" (
    echo WARNING!
    echo "gradle.properties" was generated from "gradle.properties.template"
    echo Please modify the new properties file to match your environment!
    copy /Y "gradle.properties.template" "gradle.properties"
)

if ""%QUIET%"" == """" (
::    cls
    echo Running on %OS_ARCH% Architecture
    echo ................................................................................
    path
    echo ................................................................................
    java -version
    echo ................................................................................
)

:END

