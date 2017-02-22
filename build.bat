@echo off

set CLASS=T
set JDK="C:\Program Files\Java\jdk1.6.0_45"
rem set JDK="C:\Program Files\Java\jdk1.7.0_51"
set ZIP="C:\Program Files\7-Zip\7z"
set PRO="D:\Dev\SDKs\proguard4.10\lib\proguard.jar"
set DO="D:\Dev\SDKs\DeflOpt207\DeflOpt.exe"

echo --- saving old files --- 
del %CLASS%.*.old
ren %CLASS%.zip.pro.jar %CLASS%.zip.pro.jar.old
ren %CLASS%.pro.pack.gz %CLASS%.pro.pack.gz.old
ren %CLASS%.pro.pack.do.gz %CLASS%.pro.pack.do.gz.old
ren %CLASS%.pro.pack.zip.gz %CLASS%.pro.pack.zip.gz.old

echo --- cleaning up --- 
del %CLASS%.*.jar
del %CLASS%.*.gz
del %CLASS%.*.pack
del %CLASS%*.class

echo --- compiling ---
%JDK%\bin\javac.exe -deprecation %CLASS%.java

rem METHOD 1
rem echo --- creating jar: %CLASS%.zip.pro.jar ---
rem %ZIP% a -tzip -mx=9 %CLASS%.jar %CLASS%.class META-INF/MANIFES%CLASS%.MF
rem java -jar proguard.jar @%CLASS%.pro
rem %ZIP% a -tzip -mx=9 %CLASS%.zip.jar %CLASS%.class
rem java -jar %PRO% -injars %CLASS%.zip.jar -outjars %CLASS%.zip.pro.jar -keep class %CLASS% -libraryjars %JDK%\jre\lib\rt.jar

rem METHOD 2
echo --- creating %CLASS%.jar ---
%JDK%\bin\jar.exe cfM %CLASS%.jar %CLASS%*.class
echo --- creating %CLASS%.pro.jar ---
java -jar %PRO% -injars %CLASS%.jar -outjars %CLASS%.pro.jar -keep class %CLASS% -libraryjars %JDK%\jre\lib\rt.jar
echo --- creating %CLASS%.pro.pack.gz ---
%JDK%\bin\pack200 -G %CLASS%.pro.pack.gz %CLASS%.pro.jar
echo --- creating %CLASS%.pro.pack.do.gz ---
copy %CLASS%.pro.pack.gz %CLASS%.pro.pack.do.gz
%DO% %CLASS%.pro.pack.do.gz

rem METHOD 2b
echo --- creating %CLASS%.pro.pack ---
%JDK%\bin\pack200 -G --no-gzip %CLASS%.pro.pack %CLASS%.pro.jar
echo --- creating %CLASS%.pro.pack.zip.gz ---
%ZIP% a -tzip -mx=9 %CLASS%.pro.pack.zip.gz %CLASS%.pro.pack

echo --- results ---
dir %CLASS%.*.gz*

rem %JDK%\bin\pack200 --help

rem echo --- press key to continue ---
rem pause