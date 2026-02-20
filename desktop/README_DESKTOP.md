# GridRecon 3D Desktop Side (Meshroom automation)

This folder sets up a **PC workflow**:
- Android uploads photos over Wi‑Fi to your PC (backend in `backend/`).
- A watcher detects new jobs and runs **Meshroom**.
- Outputs are written to `backend/data/<jobId>/outputs/`.

## Run backend (Wi‑Fi upload target)
```bash
cd backend
docker compose up
```
Backend: `http://YOUR_PC_IP:8080`

## Run Meshroom watcher
```bash
cd desktop
python meshroom_watch.py --data ../backend/data --meshroom "C:\\Program Files\\Meshroom\\meshroom_batch.exe"
```

## Bring model back to phone
- Copy the exported model (e.g., OBJ/PLY/GLB) to your phone via USB, then open it in the app Viewer.
- Later we can add a download endpoint + in-app download.

## Optional: auto-convert OBJ -> GLB for phone viewing
If you install Blender, you can have the watcher export a GLB automatically.

Example:
```bash
python meshroom_watch.py --data ../backend/data --meshroom "C:\\Program Files\\Meshroom\\meshroom_batch.exe" --blender "C:\\Program Files\\Blender Foundation\\Blender 4.1\\blender.exe"
```

The GLB will be saved to:
`backend/data/<jobId>/outputs/model.glb`
