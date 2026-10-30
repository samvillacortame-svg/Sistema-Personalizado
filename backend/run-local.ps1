$envFile = Join-Path $PSScriptRoot ".env"
$env:JAVA_TOOL_OPTIONS = ($env:JAVA_TOOL_OPTIONS + " -Duser.timezone=UTC").Trim()
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#=]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
        }
    }
}
& (Join-Path $PSScriptRoot "mvnw.cmd") spring-boot:run
