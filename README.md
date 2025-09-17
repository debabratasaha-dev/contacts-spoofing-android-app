# Contact Spoofing App (casual project)

**Short note:** This is a small, casual Android + Flask practice purpose project — built for learning and demo purposes only. Use responsibly: get user consent before reading/sending contacts.

---

# Demo
<center><video src="demo.mp4" controls width="600"></video></center>

---
# What it is
A simple end-to-end demo that:
- Reads device contacts (with runtime permission), deduplicates and displays them in a `RecyclerView`.
- Provides a toolbar search (live filtering).
- Sends the contacts as JSON to a backend via Retrofit.
- Backend (Flask + SQLite) accepts the contact array and stores them per `user_id`. Also supports exporting contacts to PDF.

---

# Features
- Fetch contacts from device (handles runtime permissions)
- Normalize/dedupe phone numbers (avoid duplicates from WhatsApp/other accounts)
- RecyclerView list with search (live, per keystroke)
- Send contacts to server (Retrofit + Gson)
- Flask backend storing contacts in SQLite
- Export contacts to PDF (ReportLab)
- Small, clear codebase for learning

---

# Tech stack
- Android (Java), AndroidX, RecyclerView
- Retrofit 2 + Gson (network)
- SQLite (Android local)
- Backend: Python 3, Flask, sqlite3
- PDF export: ReportLab (Python)

---

# Quick demo (what to expect)
1. Open the app → grant **READ_CONTACTS**.
2. Contacts appear (duplicates removed).
3. Tap search icon → type to filter live.
4. App posts contacts to server endpoint `/contacts` (JSON array).
5. Server stores contacts in SQLite and can export a user’s contacts to a PDF.

---

# Getting started

## Android (client)
**Prerequisites:** Android Studio (To make the `.apk` file), Android SDK (API level matching your target)

---

## Backend (Flask)
**Prerequisites:** Python 3.8+ (or suitable), pip

### Example `requirements.txt`
```bash
Flask
reportlab
```

Install:
```bash
python -m venv venv
# Windows
venv\Scripts\activate
# Unix
source venv/bin/activate

pip install -r requirements.txt
```

### Run Flask Server
```bash
flask run
```
> **⚠️ Important:**  Use any tunneling service like [cloudflared](https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/) to transfer the contacts data from the mobile to the flask server.

---

# Export to PDF
- Use ReportLab (`pip install reportlab`).
- Query contacts by `user_id` and build a table in a PDF. (A sample exporter script was used during development.)

---

# Privacy & Usage / Disclaimer (must read)
- This project reads and (optionally) uploads users’ contacts. **Only use with explicit, informed consent.**
- Do **not** use to harvest or share personal data without permission.
- This repository is for learning and demo purposes only. I (author) am not responsible for misuse.

---

# Project structure (high-level)
```
/Contacts/         # Android Studio project (Java)
  app/src/main/java/...
  app/src/main/res/...
/server/              # Flask server
  app.py
  requirements.txt
  contacts.db         # SQLite DB (created at runtime)
  export_contacts_pdf.py         # Export user contacts to PDF
  schema.sql          # sql file for database schema
README.md
Contacts.apk          # sample apk file
```

---

# Troubleshooting / tips
- If contacts show duplicates, ensure the dedupe logic normalizes numbers and uses a stable key (last 10 digits or `NORMALIZED_NUMBER` when available).
- If `SearchView` hint isn't visible, expand the view or set iconified false in code for testing.
- For Logcat crashes, filter by `E/AndroidRuntime` and share the stack trace.

---

# License
This is a casual demo — use freely for learning.  

---