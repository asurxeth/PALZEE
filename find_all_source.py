import os
import re

root = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL "

kotlin_files = []
gradle_files = []
toml_files = []
sql_files = []

for dirpath, dirnames, filenames in os.walk(root):
    # Skip build folders, gradle folders, .idea, etc.
    if any(p in dirpath for p in ['.git', '.gradle', 'build', '.idea', 'gradle-home']):
        continue
    for f in filenames:
        fp = os.path.join(dirpath, f)
        if f.endswith('.kt'):
            kotlin_files.append(fp)
        elif f.endswith('.gradle') or f.endswith('.gradle.kts'):
            gradle_files.append(fp)
        elif f.endswith('.toml'):
            toml_files.append(fp)
        elif f.endswith('.sql'):
            sql_files.append(fp)

print(f"Found {len(kotlin_files)} Kotlin files.")
print(f"Found {len(gradle_files)} Gradle files.")
print(f"Found {len(toml_files)} TOML files.")
print(f"Found {len(sql_files)} SQL files.")

# Let's inspect TOML files and Gradle files
for g in gradle_files:
    print(f"Gradle: {g}")
for t in toml_files:
    print(f"TOML: {t}")
for s in sql_files:
    print(f"SQL: {s}")
