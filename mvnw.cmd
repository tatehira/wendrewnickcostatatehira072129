@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Optional ENV vars
@REM   MVNW_REPOURL - repo url base for downloading maven distribution
@REM   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
@REM   MVNW_VERBOSE - true: enable verbose log; others: silence the output
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE__=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0teleplace'; $env:MVNW_VERBOSE='true'; (Get-Content %~dp0.mvn\wrapper\maven-wrapper.properties -Raw) | ForEach-Object {$_ -replace '(?m)^(.+)=(.*)\r?\n','SET __MVNW_$1__=$2' + [char]10}} 2>nul"`) DO @(
    IF "%%A"=="SET __MVNW_MVNW_REPOURL__" SET "MVNW_REPOURL=%%B"
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE__%

@SET JAVA_EXE=
@SET WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"

@IF NOT EXIST %WRAPPER_JAR% (
    @FOR /F %%i IN ('powershell -noprofile -c "& {(Get-Command java).Source}"') DO @SET "JAVA_EXE=%%i"
    @IF "%JAVA_EXE%"=="" (
        @ECHO Error: JAVA_HOME is not set and no 'java' command could be found in your PATH.
        @ECHO Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
        @GOTO error
    )
)

@SET MAVEN_PROJECTBASEDIR=%~dp0
@SET MAVEN_OPTS=-Xmx1024m %MAVEN_OPTS%

@IF EXIST "%~dp0mvnw" (
    @SET MVNW_SCRIPT="%~dp0mvnw"
) ELSE (
    @SET MVNW_SCRIPT=mvn
)

@ECHO.
@IF NOT EXIST %WRAPPER_JAR% (
    @ECHO Downloading Maven wrapper...
    @powershell -noprofile -command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%~dp0\.mvn\wrapper\maven-wrapper.jar'}"
)

@IF EXIST %WRAPPER_JAR% (
    "%JAVA_EXE%" %MAVEN_OPTS% -jar %WRAPPER_JAR% %*
) ELSE (
    mvn %*
)

@GOTO end

:error
@SET ERROR_CODE=1

:end
@EXIT /B %ERROR_CODE%
