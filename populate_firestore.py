from firebase_admin import credentials, initialize_app, firestore
import datetime
import random
import uuid

# Initialize Firebase Admin
cred = credentials.Certificate("path/to/your/serviceAccountKey.json")  # You'll need to download this from Firebase Console
initialize_app(cred)
db = firestore.client()

def create_test_users():
    users = [
        {
            "uid": "admin1",
            "name": "Admin User",
            "email": "admin@campusflow.com",
            "designation": "Admin",
            "department": "Administration"
        },
        {
            "uid": "faculty1",
            "name": "Professor Smith",
            "email": "faculty@campusflow.com",
            "designation": "Faculty",
            "department": "Computer Science"
        },
        {
            "uid": "secretary1",
            "name": "Secretary Johnson",
            "email": "secretary@campusflow.com",
            "designation": "Secretary",
            "department": "Administration"
        },
        {
            "uid": "student1",
            "name": "Student Williams",
            "email": "student@campusflow.com",
            "designation": "Student",
            "department": "Computer Science"
        }
    ]
    
    for user in users:
        db.collection("users").document(user["uid"]).set(user)
        print(f"Created user: {user['name']}")

def create_test_rooms():
    rooms = [
        {
            "id": "room1",
            "name": "CS101",
            "type": "Classroom",
            "capacity": 50,
            "location": "Computer Science Building",
            "buildingName": "CS Building",
            "latitude": 12.9716,
            "longitude": 77.5946,
            "currentBookings": []
        },
        {
            "id": "room2",
            "name": "Lab1",
            "type": "Laboratory",
            "capacity": 30,
            "location": "Engineering Building",
            "buildingName": "Engineering Building",
            "latitude": 12.9717,
            "longitude": 77.5947,
            "currentBookings": []
        },
        {
            "id": "room3",
            "name": "Conf1",
            "type": "Conference Room",
            "capacity": 20,
            "location": "Administration Building",
            "buildingName": "Admin Building",
            "latitude": 12.9718,
            "longitude": 77.5948,
            "currentBookings": []
        }
    ]
    
    for room in rooms:
        db.collection("rooms").document(room["id"]).set(room)
        print(f"Created room: {room['name']}")

def create_test_booking_requests():
    # Create some booking requests
    start_time = datetime.datetime.now() + datetime.timedelta(days=1)
    end_time = start_time + datetime.timedelta(hours=2)
    
    requests = [
        {
            "id": str(uuid.uuid4()),
            "roomId": "room1",
            "requesterId": "faculty1",
            "requesterName": "Professor Smith",
            "requesterDesignation": "Faculty",
            "startTime": start_time,
            "endTime": end_time,
            "status": "PENDING",
            "purpose": "Faculty Meeting"
        },
        {
            "id": str(uuid.uuid4()),
            "roomId": "room2",
            "requesterId": "secretary1",
            "requesterName": "Secretary Johnson",
            "requesterDesignation": "Secretary",
            "startTime": start_time + datetime.timedelta(days=1),
            "endTime": end_time + datetime.timedelta(days=1),
            "status": "PENDING",
            "purpose": "Department Meeting"
        }
    ]
    
    for request in requests:
        db.collection("bookingRequests").document(request["id"]).set(request)
        print(f"Created booking request for room: {request['roomId']}")

def create_test_events():
    events = [
        {
            "id": str(uuid.uuid4()),
            "title": "Tech Symposium",
            "description": "Annual technology symposium",
            "organizerId": "faculty1",
            "date": datetime.datetime.now() + datetime.timedelta(days=7),
            "location": "CS101",
            "capacity": 100
        },
        {
            "id": str(uuid.uuid4()),
            "title": "Career Fair",
            "description": "Annual career fair for students",
            "organizerId": "admin1",
            "date": datetime.datetime.now() + datetime.timedelta(days=14),
            "location": "Main Auditorium",
            "capacity": 200
        }
    ]
    
    for event in events:
        db.collection("events").document(event["id"]).set(event)
        print(f"Created event: {event['title']}")

def create_test_timetables():
    timetables = [
        {
            "id": str(uuid.uuid4()),
            "semester": "Spring 2024",
            "department": "Computer Science",
            "fileUrl": "https://example.com/timetable1.pdf",
            "uploadedBy": "secretary1",
            "uploadDate": datetime.datetime.now()
        },
        {
            "id": str(uuid.uuid4()),
            "semester": "Spring 2024",
            "department": "Engineering",
            "fileUrl": "https://example.com/timetable2.pdf",
            "uploadedBy": "secretary1",
            "uploadDate": datetime.datetime.now()
        }
    ]
    
    for timetable in timetables:
        db.collection("timeTables").document(timetable["id"]).set(timetable)
        print(f"Created timetable for {timetable['department']}")

def create_test_maps():
    maps = [
        {
            "id": str(uuid.uuid4()),
            "name": "Campus Map",
            "fileUrl": "https://example.com/campus_map.pdf",
            "uploadedBy": "admin1",
            "uploadDate": datetime.datetime.now()
        },
        {
            "id": str(uuid.uuid4()),
            "name": "Building Layout",
            "fileUrl": "https://example.com/building_layout.pdf",
            "uploadedBy": "admin1",
            "uploadDate": datetime.datetime.now()
        }
    ]
    
    for map in maps:
        db.collection("maps").document(map["id"]).set(map)
        print(f"Created map: {map['name']}")

def create_test_glimpses():
    glimpses = [
        {
            "id": str(uuid.uuid4()),
            "title": "Campus Life",
            "description": "A glimpse into campus activities",
            "mediaUrl": "https://example.com/campus_life.jpg",
            "uploadedBy": "faculty1",
            "uploadDate": datetime.datetime.now()
        },
        {
            "id": str(uuid.uuid4()),
            "title": "Research Lab",
            "description": "Our state-of-the-art research facilities",
            "mediaUrl": "https://example.com/research_lab.jpg",
            "uploadedBy": "faculty1",
            "uploadDate": datetime.datetime.now()
        }
    ]
    
    for glimpse in glimpses:
        db.collection("glimpses").document(glimpse["id"]).set(glimpse)
        print(f"Created glimpse: {glimpse['title']}")

def create_test_community_posts():
    posts = [
        {
            "id": str(uuid.uuid4()),
            "userId": "student1",
            "content": "Looking for study partners for CS101",
            "timestamp": datetime.datetime.now(),
            "likes": 0,
            "comments": []
        },
        {
            "id": str(uuid.uuid4()),
            "userId": "faculty1",
            "content": "Important announcement about tomorrow's class",
            "timestamp": datetime.datetime.now(),
            "likes": 0,
            "comments": []
        }
    ]
    
    for post in posts:
        db.collection("community").document(post["id"]).set(post)
        print(f"Created community post by {post['userId']}")

def main():
    print("Starting to populate Firestore with test data...")
    
    create_test_users()
    create_test_rooms()
    create_test_booking_requests()
    create_test_events()
    create_test_timetables()
    create_test_maps()
    create_test_glimpses()
    create_test_community_posts()
    
    print("Finished populating Firestore with test data!")

if __name__ == "__main__":
    main() 