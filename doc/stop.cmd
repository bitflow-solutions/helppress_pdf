@echo off
set cmd="type application.pid"
FOR /F %%i IN (' %cmd% ') DO SET pid=%%i
taskkill /pid %pid% /f