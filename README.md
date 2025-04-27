# CampusFlow - Digital Permission Management System

CampusFlow is a comprehensive Android application designed to streamline campus operations and enhance the college experience. It features an intuitive interface for event management, facility booking, community engagement, and more.

## Features

### Authentication
- Secure login and registration
- Role-based access control (Student, Faculty, Admin, Staff)
- Email verification

### Event Management
- Create and register for events
- View upcoming events
- Get event reminders and notifications
- Event approval workflow

### Facility Booking
- Book classrooms, labs, and other spaces
- Check availability in real-time
- Booking approval system
- Schedule management

### Campus Map & Navigation
- Interactive campus map
- Find classrooms and facilities
- Navigate around campus

### Community Building
- Create and join communities
- Participate in discussions
- Share resources and announcements

### Permission Management
- Create permission requests
- Approve or deny requests
- Track request status
- Multi-level approval workflows

## Technical Details

### Architecture
- MVVM (Model-View-ViewModel) architecture
- Repository pattern for data access

### Backend
- Firebase Authentication for user management
- Firebase Realtime Database for data storage
- Firebase Cloud Messaging for notifications
- Firebase Storage for media storage

### Libraries Used
- AndroidX components
- Material Design components
- Picasso for image loading
- Firebase SDK suite

## Setup

1. Clone this repository
2. Open the project in Android Studio
3. Connect your Firebase project:
   - Create a new Firebase project
   - Add an Android app to the project
   - Download the `google-services.json` file and place it in the app directory
   - Enable Authentication, Realtime Database, Storage, and Cloud Messaging
4. Build and run the app

## Future Enhancements

- Offline support with data synchronization
- Advanced analytics for usage patterns
- Integration with existing college ERP systems
- Web dashboard for administrators
- QR code scanning for check-ins
- Document verification and approval system

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 