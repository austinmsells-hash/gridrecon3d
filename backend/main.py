import os, uuid, shutil, time
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

ROOT = os.environ.get("GRIDRECON_DATA", "./data")
os.makedirs(ROOT, exist_ok=True)

app = FastAPI(title="GridRecon 3D Backend (Skeleton)")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class Job(BaseModel):
    job_id: str
    status: str
    created_at: float

JOBS = {}

@app.post("/v1/jobs")
async def create_job():
    jid = str(uuid.uuid4())
    JOBS[jid] = {"job_id": jid, "status": "UPLOADING", "created_at": time.time()}
    os.makedirs(os.path.join(ROOT, jid, "photos"), exist_ok=True)
    return JOBS[jid]

@app.post("/v1/jobs/{job_id}/photos")
async def upload_photo(job_id: str, file: UploadFile = File(...)):
    if job_id not in JOBS:
        raise HTTPException(404, "job not found")
    out = os.path.join(ROOT, job_id, "photos", file.filename)
    with open(out, "wb") as f:
        shutil.copyfileobj(file.file, f)
    return {"ok": True, "saved_as": out}

@app.post("/v1/jobs/{job_id}/finalize")
async def finalize(job_id: str):
    if job_id not in JOBS:
        raise HTTPException(404, "job not found")
    JOBS[job_id]["status"] = "QUEUED"
    # Skeleton: no photogrammetry here; in production, enqueue a worker job.
    return JOBS[job_id]

@app.get("/v1/jobs/{job_id}")
async def get_job(job_id: str):
    if job_id not in JOBS:
        raise HTTPException(404, "job not found")
    return JOBS[job_id]

@app.delete("/v1/jobs/{job_id}")
async def delete_job(job_id: str):
    # best-effort delete job data
    if job_id in JOBS:
        JOBS[job_id]["status"] = "DELETE_REQUESTED"
    path = os.path.join(ROOT, job_id)
    if os.path.exists(path):
        shutil.rmtree(path, ignore_errors=True)
    if job_id in JOBS:
        JOBS[job_id]["status"] = "DELETED"
    return {"job_id": job_id, "status": "DELETED"}

@app.post("/v1/account/zeroize")
async def zeroize():
    # Skeleton: wipes all server data in ROOT (single-tenant beta mode)
    for name in os.listdir(ROOT):
        shutil.rmtree(os.path.join(ROOT, name), ignore_errors=True)
    JOBS.clear()
    return {"status": "DELETED_ALL"}

from fastapi.responses import FileResponse
from pathlib import Path

def _job_dir(job_id: str) -> str:
    return os.path.join(ROOT, job_id)

@app.get("/v1/jobs")
async def list_jobs():
    # In beta, list what we have on disk + in memory
    ids = set(JOBS.keys())
    for name in os.listdir(ROOT):
        if os.path.isdir(os.path.join(ROOT, name)):
            ids.add(name)
    out = []
    for jid in sorted(ids):
        j = JOBS.get(jid) or {"job_id": jid, "status": "UNKNOWN", "created_at": 0.0}
        # If outputs exist, treat as COMPLETE
        if os.path.exists(os.path.join(ROOT, jid, "outputs", "DONE")):
            j = {**j, "status": "COMPLETE"}
        out.append(j)
    return out

@app.get("/v1/jobs/{job_id}/outputs")
async def list_outputs(job_id: str):
    base = Path(_job_dir(job_id)) / "outputs"
    if not base.exists():
        raise HTTPException(404, "outputs not found")
    files = []
    for p in base.rglob("*"):
        if p.is_file() and p.name != "DONE":
            files.append(str(p.relative_to(base)).replace("\\", "/"))
    return {"job_id": job_id, "files": sorted(files)}

@app.get("/v1/jobs/{job_id}/download")
async def download_output(job_id: str, path: str):
    base = Path(_job_dir(job_id)) / "outputs"
    target = (base / path).resolve()
    if not str(target).startswith(str(base.resolve())):
        raise HTTPException(400, "invalid path")
    if not target.exists() or not target.is_file():
        raise HTTPException(404, "file not found")
    return FileResponse(str(target), filename=target.name)
