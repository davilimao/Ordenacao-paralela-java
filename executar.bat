@echo off
if exist resultados rmdir /s /q resultados
mkdir resultados
javac -d bin src\*.java
java -cp bin Main
pause
