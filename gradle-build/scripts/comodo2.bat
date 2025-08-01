@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  comodo2 startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and COMODO2_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\comodo2-1.0.jar;%APP_HOME%\lib\org.eclipse.xtext-2.19.0.jar;%APP_HOME%\lib\org.eclipse.xtext.util-2.19.0.jar;%APP_HOME%\lib\guice-3.0.jar;%APP_HOME%\lib\javax.inject-1.jar;%APP_HOME%\lib\commons-cli-1.4.jar;%APP_HOME%\lib\org.eclipse.xtend.lib-2.19.0.jar;%APP_HOME%\lib\org.eclipse.xtend.lib.macro-2.19.0.jar;%APP_HOME%\lib\org.eclipse.xtext.xbase.lib-2.19.0.jar;%APP_HOME%\lib\guava-27.1-jre.jar;%APP_HOME%\lib\org.eclipse.emf.common-2.15.0.jar;%APP_HOME%\lib\ST4-4.3.1.jar;%APP_HOME%\lib\org.eclipse.uml2.uml_5.5.0.v20181203-1331.jar;%APP_HOME%\lib\org.eclipse.uml2.common_2.5.0.v20181203-1331.jar;%APP_HOME%\lib\org.eclipse.uml2.types_2.5.0.v20181203-1331.jar;%APP_HOME%\lib\org.eclipse.uml2.uml.profile.standard_1.5.0.v20181203-1331.jar;%APP_HOME%\lib\org.eclipse.uml2.uml.resources_5.5.0.v20181203-1331.jar;%APP_HOME%\lib\org.eclipse.emf.ecore_2.17.0.v20190116-0940.jar;%APP_HOME%\lib\org.eclipse.emf.common_2.15.0.v20181220-0846.jar;%APP_HOME%\lib\org.eclipse.emf.ecore.xmi_2.15.0.v20180706-1146.jar;%APP_HOME%\lib\org.eclipse.core.runtime_3.15.200.v20190301-1641.jar;%APP_HOME%\lib\org.eclipse.equinox.common_3.10.300.v20190218-2100.jar;%APP_HOME%\lib\aopalliance-1.0.jar;%APP_HOME%\lib\cglib-2.2.1-v20090111.jar;%APP_HOME%\lib\antlr-runtime-3.5.2.jar;%APP_HOME%\lib\log4j-1.2.17.jar;%APP_HOME%\lib\org.eclipse.equinox.common-3.10.400.jar;%APP_HOME%\lib\org.eclipse.osgi-3.14.0.jar;%APP_HOME%\lib\org.eclipse.emf.ecore.xmi-2.12.0.jar;%APP_HOME%\lib\asm-3.1.jar;%APP_HOME%\lib\org.eclipse.emf.ecore-2.12.0.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-2.5.2.jar;%APP_HOME%\lib\error_prone_annotations-2.2.0.jar;%APP_HOME%\lib\j2objc-annotations-1.1.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.17.jar


@rem Execute comodo2
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %COMODO2_OPTS%  -classpath "%CLASSPATH%" comodo2.engine.Main %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable COMODO2_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%COMODO2_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
