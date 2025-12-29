@ECHO OFF
ECHO Set Package Java Binary Environment Variable
set mvn=D:\Maven\apache-maven-3.9.6
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.10

echo mvn: %mvn%
echo JAVA_HOME: %JAVA_HOME%

echo ====================package Java Binary========================
echo %mvn%\bin\mvn
CALL %mvn%\bin\mvn clean package