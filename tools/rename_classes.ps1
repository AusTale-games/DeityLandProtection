param(
    [Parameter(Mandatory=$true)][hashtable]$Renames
)

$srcRoot = Join-Path (Split-Path $PSScriptRoot -Parent) "src\main\java\group\austale\deitylandprotection"
$javaFiles = Get-ChildItem $srcRoot -Filter *.java -File

# Step 1: textual replacement across every .java file (word-boundary safe).
foreach ($file in $javaFiles) {
    $text = [System.IO.File]::ReadAllText($file.FullName)
    $original = $text
    foreach ($pair in $Renames.GetEnumerator()) {
        $old = [regex]::Escape($pair.Key)
        $text = [regex]::Replace($text, "\b$old\b", $pair.Value)
    }
    if ($text -ne $original) {
        [System.IO.File]::WriteAllText($file.FullName, $text)
    }
}

# Step 2: file renames.
foreach ($pair in $Renames.GetEnumerator()) {
    $oldPath = Join-Path $srcRoot ("{0}.java" -f $pair.Key)
    $newPath = Join-Path $srcRoot ("{0}.java" -f $pair.Value)
    if (Test-Path $oldPath) {
        if (Test-Path $newPath) {
            Write-Warning ("Target already exists: {0}" -f $newPath)
            continue
        }
        Rename-Item -LiteralPath $oldPath -NewName ("{0}.java" -f $pair.Value)
    }
}
