import argparse
import subprocess
import time
from pathlib import Path

def run(cmd):
    print(">>", " ".join(cmd))
    return subprocess.run(cmd, check=False)

def job_ready(job_dir: Path) -> bool:
    photos = job_dir / "photos"
    return photos.exists() and any(photos.iterdir())

def already_processed(job_dir: Path) -> bool:
    return (job_dir / "outputs" / "DONE").exists()

def mark_done(job_dir: Path):
    out = job_dir / "outputs"
    out.mkdir(parents=True, exist_ok=True)
    (out / "DONE").write_text(time.strftime("%Y-%m-%d %H:%M:%S"))

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--data", required=True)
    ap.add_argument("--meshroom", required=True)
    ap.add_argument("--poll", type=int, default=5)
    ap.add_argument("--blender", default=None, help="Optional path to blender.exe for OBJ->GLB conversion")
    args = ap.parse_args()

    blender = args.blender

    data = Path(args.data).resolve()
    meshroom = args.meshroom
    print(f"Watching: {data}")

    while True:
        for job_dir in data.iterdir():
            if not job_dir.is_dir():
                continue
            if not job_ready(job_dir) or already_processed(job_dir):
                continue

            photos = job_dir / "photos"
            out_dir = job_dir / "outputs"
            out_dir.mkdir(exist_ok=True)

            cache = out_dir / "cache"
            meshroom_out = out_dir / "meshroom_out"
            cache.mkdir(exist_ok=True)
            meshroom_out.mkdir(exist_ok=True)

            cmd = [meshroom, "--input", str(photos), "--output", str(meshroom_out), "--cache", str(cache)]
            res = run(cmd)
            if res.returncode == 0:
                mark_done(job_dir)
                print(f"[OK] {job_dir.name}")
            else:
                print(f"[WARN] Meshroom failed for {job_dir.name} (code {res.returncode}).")

        time.sleep(args.poll)

if __name__ == "__main__":
    main()
