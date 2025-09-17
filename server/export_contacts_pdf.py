#!/usr/bin/env python3
"""
export_contacts_pdf.py
Usage:
    python export_contacts_pdf.py --user 1 --db contacts.db --out user1_contacts.pdf
"""
import argparse
import sqlite3
from cs50 import SQL
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle

def fetch_contacts(db_path: str, user_id: int):
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()
    cur.execute("SELECT name, number FROM contacts WHERE user_id = ? ORDER BY name", (user_id,))
    rows = cur.fetchall()
    conn.close()
    return rows

def build_pdf(rows,db_path: str, out_path: str, user_id: int):
    doc = SimpleDocTemplate(out_path, pagesize=A4, rightMargin=36, leftMargin=36, topMargin=36, bottomMargin=36)
    styles = getSampleStyleSheet()
    elems = []

    db = SQL(f"sqlite:///{db_path}")
    row = db.execute("SELECT * FROM users WHERE id = ?", user_id)
    time = str(row[0]['time']) if row else 'unknown'
    name = str(row[0]['name']) if row and row[0]['name'] else 'unknown'
    elems.append(Paragraph(f"Contacts of {name}", styles['Title']))

    elems.append(Spacer(1, 12))

    elems.append(Paragraph(f"Time {time}", styles['Normal']))
    elems.append(Spacer(1, 12))


    if not rows:
        elems.append(Paragraph("No contacts found.", styles['Normal']))
    else:
        data = [["Name", "Number"]] + [[r[0] or "", r[1] or ""] for r in rows]
        table = Table(data, colWidths=[330, 160])
        tbl_style = TableStyle([
            ('BACKGROUND', (0,0), (-1,0), colors.lightgrey),
            ('TEXTCOLOR', (0,0), (-1,0), colors.black),
            ('ALIGN', (0,0), (-1,-1), 'LEFT'),
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
            ('GRID', (0,0), (-1,-1), 0.5, colors.grey),
            ('BOTTOMPADDING', (0,0), (-1,0), 8),
        ])
        table.setStyle(tbl_style)
        elems.append(table)

    doc.build(elems)

def main():
    parser = argparse.ArgumentParser(description="Export user contacts from SQLite to PDF")
    parser.add_argument("--user", "-u", type=int, required=True, help="user_id to export")
    parser.add_argument("--db", "-d", default="contacts.db", help="path to sqlite db (default: contacts.db)")
    parser.add_argument("--out", "-o", default=None, help="output PDF path (default: contacts_<user>.pdf)")
    args = parser.parse_args()

    out_path = args.out or f"contacts_{args.user}.pdf"
    rows = fetch_contacts(args.db, args.user)
    build_pdf(rows, args.db, out_path, args.user)
    print(f"Exported {len(rows)} contacts for user_id={args.user} -> {out_path}")

if __name__ == "__main__":
    main()
