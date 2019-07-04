@echo off
javac JSpider.java

rem IMPORTANT: In a manifest file, make sure you add newline after the first line otherwise the resulting .jar file won't run properly
jar cfm JSpider.jar JSpider.manifest *.class

del *.class