import os

root = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /"

for dirpath, dirnames, filenames in os.walk(root):
    for f in filenames:
        if f.endswith('.sql'):
            print(os.path.join(dirpath, f))
