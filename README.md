# GridRecon 3D (Buildable Skeleton)

This package gives you a **working Android UI skeleton** (Compose) + **backend skeleton** (FastAPI) that you can run today.
It includes:
- Consent gate
- Biometric/device-credential lock (where available)
- Capture-mode selection (Structure / Object / Person)
- Upload screen (demo wiring)
- Viewer screen (select a .GLB file and view it on-device)
- Export bundle creation with optional AES-encrypted ZIP
- Zeroize (local wipe) placeholder + backend zeroize endpoint

## What this is / isn't
- ✅ It is a real starting point you can open in Android Studio and build into an APK today.
- ✅ It demonstrates the security/privacy scaffolding + viewer + encrypted exports.
- ❌ It does NOT yet run full photogrammetry reconstruction (COLMAP/OpenMVS pipeline). That is the next milestone.

## Run backend locally
```bash
cd backend
docker compose up
```
Backend: http://localhost:8080

## Build Android APK
1) Install Android Studio (Giraffe+ recommended)  
2) Open `android/` as a project  
3) Let Gradle sync  
4) Run on device or emulator  
5) To build APK: Build > Build Bundle(s) / APK(s) > Build APK(s)

### Viewer
- In Viewer, tap **PICK GLB** and select a GLB file from storage (Downloads).
- Tip: export a GLB from Blender or grab any CC0 sample model.

## Next milestones (recommended order)
1) Guided CameraX capture + coverage scoring + blur/exposure checks
2) Upload manager: chunked/resumable uploads + background retry
3) Reconstruction worker pipeline (COLMAP + OpenMVS/AliceVision) + scaling via calibration marker
4) Measurements: point-to-point + bounding box
5) Export bundle: include model + metadata + optional GeoJSON/KML

## Privacy
- Location overlays are intended to be OFF by default.
- Export encryption is optional; when enabled it creates an AES-encrypted ZIP (password required).



## Option C: Wi‑Fi + USB
- Wi‑Fi upload: set PC server URL in Capture screen, select photos, tap UPLOAD TO PC.
- USB: use Export screen to create an (optionally encrypted) ZIP, or copy model files directly.

## Step 2: End-to-end loop (Phone -> PC -> Meshroom -> Phone)
- Upload photos from phone (Capture screen).
- Run Meshroom watcher (desktop/meshroom_watch.py).
- (Optional) Install Blender to auto-convert OBJ -> GLB.
- In app: My Models -> GET GLB to download the processed model back to your phone and open in Viewer.

## Step 3: Hybrid mode (Offline-first)
- Capture works offline and saves photos to app storage.
- Upload can happen later (Wi‑Fi to PC) when available.
- AI analysis is planned as an opt-in online step after processing.


## Step 5: Units + Scale (beta)
- Units toggle: Metric / Standard / Both.
- Viewer shows bounding-box measurements.
- Set Scale: enter a known dimension (meters) to scale measurements.


## Step 7: Reference scale presets + multi-sample confidence + cutout (beta)
- Scale presets: Door / Human / Wheel / Power line / Street light / Manual.
- Add multiple scale samples; app uses median scale and reports confidence.
- Cutout (background removal) helper runs locally on your PC (desktop/remove_bg.py).


## Step 8: Region standards + per-model scaling + settings hardening
- Reference Standard: US / EU / Custom (affects preset defaults).
- Scale samples and scale factor are stored per model (keyed by model URI hash).
- Settings includes permissions guidance and defaults.
- Import flow prompt (sort by time vs keep order) planned for next UI wiring.
