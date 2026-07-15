import os
import json

dest_assets_dir = "iosApp/iosApp/Assets.xcassets"

images = [
    "blob_light", "blob_dark", "dm_bingsu", "dm_envalope", "dm_fire",
    "dm_moon", "dm_orange", "dm_pizza", "dm_plant",
    "dm_star_1", "dm_star_2", "dm_star_3", "dm_star_4", "dm_star_5", "dm_tea"
]

for img_name in images:
    imgset_dir = os.path.join(dest_assets_dir, f"{img_name}.imageset")
    if not os.path.exists(imgset_dir):
        continue
        
    imgset_contents = {
        "images": [
            {
                "idiom": "universal",
                "scale": "1x",
                "filename": f"{img_name}.png"
            },
            {
                "idiom": "universal",
                "scale": "2x"
            },
            {
                "idiom": "universal",
                "scale": "3x"
            }
        ],
        "info": {
            "version": 1,
            "author": "xcode"
        }
    }
    
    with open(os.path.join(imgset_dir, "Contents.json"), "w") as f:
        json.dump(imgset_contents, f, indent=4)

print("Contents.json files corrected successfully!")
