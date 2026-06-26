import os

root = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL "

for dirpath, dirnames, filenames in os.walk(root):
    if any(p in dirpath for p in ['.git', '.gradle', 'build', '.idea', 'gradle-home', 'app/src/androidTest', 'app/src/test']):
        continue
    for f in filenames:
        if f.endswith('.kt'):
            rel_path = os.path.relpath(os.path.join(dirpath, f), root)
            print(rel_path)
