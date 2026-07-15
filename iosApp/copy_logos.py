import os
import shutil
import json

src_dir = "app/src/main/res/drawable"
dest_assets_dir = "iosApp/iosApp/Assets.xcassets"

# 1. Copy pal_logo.png
pal_logo_dir = os.path.join(dest_assets_dir, "pal_logo.imageset")
os.makedirs(pal_logo_dir, exist_ok=True)
shutil.copy(os.path.join(src_dir, "pal_logo.png"), os.path.join(pal_logo_dir, "pal_logo.png"))
with open(os.path.join(pal_logo_dir, "Contents.json"), "w") as f:
    json.dump({
        "images": [
            {"idiom": "universal", "scale": "1x", "filename": "pal_logo.png"},
            {"idiom": "universal", "scale": "2x"},
            {"idiom": "universal", "scale": "3x"}
        ],
        "info": {"version": 1, "author": "xcode"}
    }, f, indent=4)

# 2. Copy pal_circular_logo.png
pal_circ_dir = os.path.join(dest_assets_dir, "pal_circular_logo.imageset")
os.makedirs(pal_circ_dir, exist_ok=True)
shutil.copy(os.path.join(src_dir, "pal_circular_logo.png"), os.path.join(pal_circ_dir, "pal_circular_logo.png"))
with open(os.path.join(pal_circ_dir, "Contents.json"), "w") as f:
    json.dump({
        "images": [
            {"idiom": "universal", "scale": "1x", "filename": "pal_circular_logo.png"},
            {"idiom": "universal", "scale": "2x"},
            {"idiom": "universal", "scale": "3x"}
        ],
        "info": {"version": 1, "author": "xcode"}
    }, f, indent=4)

# 3. Setup AppIcon.appiconset (Single Size Xcode 14+ configuration)
appicon_dir = os.path.join(dest_assets_dir, "AppIcon.appiconset")
os.makedirs(appicon_dir, exist_ok=True)
shutil.copy(os.path.join(src_dir, "pal_logo.png"), os.path.join(appicon_dir, "appicon_1024.png"))
with open(os.path.join(appicon_dir, "Contents.json"), "w") as f:
    json.dump({
        "images": [
            {
                "idiom": "ios-marketing",
                "scale": "1x",
                "size": "1024x1024",
                "filename": "appicon_1024.png"
            },
            {
                "idiom": "universal",
                "platform": "ios",
                "size": "1024x1024",
                "filename": "appicon_1024.png"
            }
        ],
        "info": {"version": 1, "author": "xcode"}
    }, f, indent=4)

print("Logos and AppIcon set up successfully!")
