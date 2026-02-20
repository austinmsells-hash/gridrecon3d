# GridRecon 3D — Project State (Single Source of Truth)

## Goal
Android + desktop pipeline for photo-to-3D with:
- Capture (offline) and Import (DSLR photos supported)
- Upload (Wi‑Fi/USB) to desktop processor (Meshroom)
- Viewer on Android (Field Mode + Presentation Mode)
- Scaling and measurement (Door/Human/Wheel; US/EU/Custom)
- Exports: GLB / OBJ / STL + optional encrypted bundle
- Security controls: biometric lock, zeroize
- Field Guide + Capture Advisor (pro refresher)

## Current Build Line
- Step 9: Field Guide + Capture Advisor integrated, plus prior steps:
  - tap-to-measure
  - scale presets and multi-sample scale confidence
  - region standards + per-model scale foundation

## Known “Next” Items (Planned)
1) Finish DSLR Import workflow end-to-end:
   - pick photos
   - ask ordering (sort by time vs keep order)
   - create Job
2) Job system: Jobs own photos, models, scale samples, exports
3) Viewer modes: Field vs Presentation
4) APK build + signing guide for release installs
5) Desktop: “one-click” processing runner and clear sync folder conventions

## Non-goals (for v1)
- Live streaming / VPN / RTSP bridging (tracked as future companion app concept)

## File Outputs
- Mobile viewer: .glb
- Desktop workflows: .obj
- 3D printing: .stl
- Optional photo cutout helper: desktop/remove_bg.py

## Notes
- Photogrammetry scale is ambiguous until calibrated. Use references:
  Door / Human / Wheel. Add 2–3 scale samples to improve confidence.
