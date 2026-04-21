param(
    [switch]$NoClean
)

$ErrorActionPreference = "Stop"

Set-Location -Path $PSScriptRoot

if (-not $NoClean -and (Test-Path "out")) {
    Remove-Item -Recurse -Force "out"
}

if (-not (Test-Path "out")) {
    New-Item -ItemType Directory "out" | Out-Null
}

$sourceFiles = Get-ChildItem -Recurse -Filter *.java "src/main/java" | ForEach-Object { $_.FullName }
if (-not $sourceFiles -or $sourceFiles.Count -eq 0) {
    throw "No Java source files found under src/main/java."
}

javac -d out $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "javac failed."
}

java -cp "out;src/main/resources" japan26.Main
