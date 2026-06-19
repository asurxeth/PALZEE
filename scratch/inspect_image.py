import os
from PIL import Image

img_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /app/src/main/res/drawable/capture_smile.png"
if os.path.exists(img_path):
    img = Image.open(img_path).convert("RGBA")
    print("Mode converted to RGBA")
    colors = img.getcolors(maxcolors=10000)
    sorted_colors = sorted(colors, key=lambda x: x[0], reverse=True)
    for count, color in sorted_colors[:15]:
        print(f"Color (RGBA): {color}, Count: {count}")
else:
    print("File not found")
