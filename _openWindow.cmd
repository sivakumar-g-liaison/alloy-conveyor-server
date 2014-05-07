@rem
@rem this is just a convenient way to spawn a window and do setpath at the same time
@echo off

start "%CD%" /D%CD% setenv.cmd /K
