# Photo Cutout (Background Removal) - Beta

This is an optional side feature: create a PNG of a selected photo with the background removed.

## Install (Windows)
```bash
pip install rembg pillow
```

## Run
```bash
python remove_bg.py --in input.jpg --out cutout.png
```

Notes:
- Works best on people/vehicles/foreground objects.
- Buildings can work, but results vary; you may need to crop first.
- This runs locally on your PC for privacy.
