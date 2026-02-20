# Blender script: import OBJ and export GLB
# Run: blender -b -P convert_obj_to_glb.py -- --in "model.obj" --out "model.glb"
import argparse, sys, os
import bpy

def parse_args():
    argv = sys.argv
    if "--" in argv:
        argv = argv[argv.index("--") + 1:]
    else:
        argv = []
    ap = argparse.ArgumentParser()
    ap.add_argument("--in", dest="inp", required=True)
    ap.add_argument("--out", dest="out", required=True)
    return ap.parse_args(argv)

def main():
    args = parse_args()
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.ops.import_scene.obj(filepath=args.inp)
    bpy.ops.export_scene.gltf(filepath=args.out, export_format='GLB')
    print("Exported", args.out)

if __name__ == "__main__":
    main()
