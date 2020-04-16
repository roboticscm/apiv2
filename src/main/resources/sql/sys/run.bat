set folder="D:\suntech\skyapi\skyhub-api\system\src\main\resources\sql"
set PGPASSWORD=st@skyhub2016
IF EXIST "%folder%" (
    cd /d %folder%
    for /F "delims=" %%i in ('dir /b') do psql -h localhost -d hubdb -U hub -p 5432 -a -q -f "%%i"
)
 
