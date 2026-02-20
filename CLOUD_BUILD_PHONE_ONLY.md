# Build a GridRecon 3D APK using ONLY your phone (GitHub Actions)

This method uses a free GitHub build runner to compile the APK.
You do NOT need a PC.

## What you need
- A GitHub account
- Termux on Android (recommended install from F-Droid)
- Wi‑Fi recommended

## Step 1 — Create a GitHub repo (on your phone)
1) Open GitHub in a browser or GitHub app.
2) Create a new repository named: `gridrecon3d`
3) Keep it Public (simplest for Actions) or Private (still works).

## Step 2 — Create a GitHub Personal Access Token (PAT)
1) GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2) Generate a token with:
   - `repo`
3) Copy the token (you’ll paste it into Termux once).

## Step 3 — Install Termux + tools
In Termux:
```bash
pkg update -y
pkg install -y git unzip rsync
```

## Step 4 — Put the project on your phone
1) Download the project zip to your phone.
2) In Termux, find the download folder (usually `~/storage/downloads`).
If you haven’t enabled storage yet:
```bash
termux-setup-storage
```

## Step 5 — Unzip + push to GitHub
Replace YOURNAME with your GitHub username.

```bash
cd ~/storage/downloads
unzip gridrecon3d_step9_fieldguide_advisor_WITH_DOCS.zip -d gridrecon3d_src

mkdir -p ~/gridrecon3d
rsync -a gridrecon3d_src/ ~/gridrecon3d/

cd ~/gridrecon3d
git init
git branch -M main
git add .
git commit -m "GridRecon3D Step 9 baseline"

git remote add origin https://github.com/YOURNAME/gridrecon3d.git
git push -u origin main
```

When Git asks for credentials:
- Username: YOUR GitHub username
- Password: paste your PAT token (NOT your GitHub password)

## Step 6 — Download the APK
1) Go to your repo → **Actions**
2) Open the latest workflow run “Build Debug APK”
3) Scroll to **Artifacts**
4) Download **GridRecon3D-debug-apk**
5) On your phone, open the downloaded artifact zip → install the `.apk`

## Notes
- This produces a DEBUG apk (fine for testing).
- For sharing widely, we’ll later add a signed release build.
