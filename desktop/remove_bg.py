import argparse
from pathlib import Path
from PIL import Image

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--in", dest="inp", required=True)
    ap.add_argument("--out", dest="out", required=True)
    args = ap.parse_args()

    try:
        from rembg import remove
    except Exception as e:
        raise SystemExit("rembg not installed. Run: pip install rembg pillow") from e

    im = Image.open(args.inp).convert("RGBA")
    out = remove(im)
    out.save(args.out)
    print("Wrote", args.out)

if __name__ == "__main__":
    main()
