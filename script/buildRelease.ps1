$releasePath = 'C:\Program Files (x86)\Fractal Softworks\Starsector\mods\VAO'

Remove-Item -Path $releasePath -Recurse
New-Item -Path $releasePath'\jars\distribution' -ItemType Directory

Copy-Item -Path ..\data -Recurse -Destination $releasePath
Copy-Item -Path ..\graphics -Recurse -Destination $releasePath
Copy-Item -Path ..\Credits.txt  -Destination $releasePath
Copy-Item -Path ..\icon.ico  -Destination $releasePath
Copy-Item -Path ..\mod_info.json  -Destination $releasePath
Copy-Item -Path ..\jars\ProjectVao.jar  -Destination $releasePath\jars\distribution\ProjectVao.jar
