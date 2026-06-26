import re

file_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt"

with open(file_path, 'r') as f:
    lines = f.readlines()

matches = []
for i, line in enumerate(lines):
    if 'mutableStateOf' in line:
        matches.append((i+1, line.strip()))

output_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /all_states.txt"
with open(output_path, 'w') as out:
    out.write(f"Total mutableStateOf count: {len(matches)}\n")
    for num, content in matches:
        out.write(f"Line {num}: {content}\n")

print(f"Written {len(matches)} states to {output_path}")
