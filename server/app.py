from flask import Flask, jsonify, request
from cs50 import SQL

# Initialize Flask app and database
app = Flask(__name__)
db = SQL("sqlite:///contacts.db")

# Function to insert contacts into the database
def insert_contacts(contacts, user_id):
    for contact in contacts:
        db.execute(
            "INSERT INTO contacts (user_id, name, number) VALUES (?, ?, ?)",
            user_id, contact["name"], contact["number"]
        )


@app.route("/contacts", methods=["POST"])
def save_contacts():

    if request.method == "POST":
        contacts = request.get_json()

        user_id = None
        #setiing the user's profile
        if "(You)" in contacts[0]['name']: #if user data exists

            if not contacts[0]['number']: #if user number is empty

                #get unique id from counters table and increment it
                unoique_value = db.execute("SELECT value FROM counter WHERE name = 'unique_user_counter'")
                db.execute("UPDATE counter SET value = value + 1 WHERE name = 'unique_user_counter'")

                unique_id =  "user_id" + unoique_value[0]['value'] #creating unique id instead of phone number (phone number empty case)
                name = contacts[0]['name']
                if not name:
                    name = unique_id
                else:
                    name.replace(" (You)", "") #removing (You) from name
                db.execute("INSERT INTO users (name, phone) VALUES(?, ?)", name, unique_id)
                contacts.pop(0)

                user_id = db.execute("SELECT id FROM users WHERE phone = ?", unique_id)
                insert_contacts(contacts, user_id[0]['id'])

            else:
                name = contacts[0]['name'].replace(" (You)", "") #removing (You) from name
                try:
                    db.execute("INSERT INTO users (name, phone) VALUES(?, ?)", name, contacts[0]['number'])
                except ValueError:
                    return {"error": "Phone number already exists"}, 400
                
                user_id = db.execute("SELECT id FROM users WHERE phone = ?", contacts[0]['number'])
                contacts.pop(0)
                insert_contacts(contacts, user_id[0]['id'])

        else:
            unoique_value = db.execute("SELECT value FROM counter WHERE name = 'unique_user_counter'")
            db.execute("UPDATE counter SET value = value + 1 WHERE name = 'unique_user_counter'")

            unique_id =  "user_id" + str(unoique_value[0]['value'])
            name = unique_id
            db.execute("INSERT INTO users (name, phone) VALUES(?, ?)", name, unique_id)
            
            user_id = db.execute("SELECT id FROM users WHERE phone = ?", unique_id)
            insert_contacts(contacts, user_id[0]['id'])
        return {"status": "ok"}, 200


@app.route("/test", methods=["GET"])
def get_contacts():
    # Sample data
    contacts = [
        {"name": "Alice Smith", "number": "1234567890"},
        {"name": "Bob Johnson", "number": "9876543210"},
        {"name": "Charlie Brown", "number": "5556667777"}
    ]
    return jsonify(contacts)