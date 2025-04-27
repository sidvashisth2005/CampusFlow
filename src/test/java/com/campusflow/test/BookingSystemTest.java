package com.campusflow.test;

import com.campusflow.context.BookingContext;
import com.campusflow.context.ResourceContext;
import com.campusflow.context.UserContext;
import com.campusflow.model.Booking;
import com.campusflow.model.Resource;
import com.campusflow.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingSystemTest {
    private BookingContext bookingContext;
    private ResourceContext resourceContext;
    private UserContext userContext;
    private List<UserProfile> testUsers;
    private List<Resource> testResources;

    @BeforeEach
    void setUp() {
        // Initialize contexts
        bookingContext = BookingContext.getInstance();
        resourceContext = ResourceContext.getInstance();
        userContext = UserContext.getInstance();

        // Create test users
        testUsers = createTestUsers();
        testUsers.forEach(userContext::updateUserProfile);

        // Create test resources
        testResources = createTestResources();
        testResources.forEach(resourceContext::createResource);
    }

    private List<UserProfile> createTestUsers() {
        List<UserProfile> users = new ArrayList<>();

        // Student
        UserProfile student = new UserProfile();
        student.setId("ST001");
        student.setUsername("john.doe");
        student.setEmail("john.doe@university.edu");
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setDepartment("Computer Science");
        student.setDesignation("Student");
        student.setRoles(Arrays.asList(UserProfile.Role.STUDENT.name()));
        student.setPermissions(Arrays.asList(UserProfile.Permission.BOOK_RESOURCES.name()));
        student.setActive(true);
        users.add(student);

        // Faculty
        UserProfile faculty = new UserProfile();
        faculty.setId("FC001");
        faculty.setUsername("jane.smith");
        faculty.setEmail("jane.smith@university.edu");
        faculty.setFirstName("Jane");
        faculty.setLastName("Smith");
        faculty.setDepartment("Computer Science");
        faculty.setDesignation("Professor");
        faculty.setRoles(Arrays.asList(UserProfile.Role.FACULTY.name()));
        faculty.setPermissions(Arrays.asList(
            UserProfile.Permission.BOOK_RESOURCES.name(),
            UserProfile.Permission.APPROVE_BOOKINGS.name()
        ));
        faculty.setActive(true);
        users.add(faculty);

        // Resource Manager
        UserProfile manager = new UserProfile();
        manager.setId("RM001");
        manager.setUsername("robert.wilson");
        manager.setEmail("robert.wilson@university.edu");
        manager.setFirstName("Robert");
        manager.setLastName("Wilson");
        manager.setDepartment("Facilities");
        manager.setDesignation("Resource Manager");
        manager.setRoles(Arrays.asList(UserProfile.Role.RESOURCE_MANAGER.name()));
        manager.setPermissions(Arrays.asList(
            UserProfile.Permission.MANAGE_RESOURCES.name(),
            UserProfile.Permission.APPROVE_BOOKINGS.name()
        ));
        manager.setActive(true);
        users.add(manager);

        return users;
    }

    private List<Resource> createTestResources() {
        List<Resource> resources = new ArrayList<>();

        // Conference Room
        Resource conferenceRoom = new Resource();
        conferenceRoom.setId("CR001");
        conferenceRoom.setName("Main Conference Room");
        conferenceRoom.setType("ROOM");
        conferenceRoom.setLocation("Building A, Floor 2");
        conferenceRoom.setCapacity(20);
        conferenceRoom.setFeatures(Arrays.asList("Projector", "Whiteboard", "Video Conferencing"));
        conferenceRoom.setDescription("Main conference room for department meetings");
        conferenceRoom.setActive(true);
        conferenceRoom.setManagedBy("RM001");
        conferenceRoom.setAllowedRoles(Arrays.asList(
            UserProfile.Role.FACULTY.name(),
            UserProfile.Role.STAFF.name(),
            UserProfile.Role.ADMIN.name()
        ));
        conferenceRoom.setMaxBookingDuration(240); // 4 hours
        conferenceRoom.setMinBookingDuration(30); // 30 minutes
        resources.add(conferenceRoom);

        // Computer Lab
        Resource computerLab = new Resource();
        computerLab.setId("CL001");
        computerLab.setName("Computer Lab 101");
        computerLab.setType("ROOM");
        computerLab.setLocation("Building B, Floor 1");
        computerLab.setCapacity(30);
        computerLab.setFeatures(Arrays.asList("30 Computers", "Projector", "Printer"));
        computerLab.setDescription("General purpose computer lab");
        computerLab.setActive(true);
        computerLab.setManagedBy("RM001");
        computerLab.setAllowedRoles(Arrays.asList(
            UserProfile.Role.STUDENT.name(),
            UserProfile.Role.FACULTY.name(),
            UserProfile.Role.STAFF.name()
        ));
        computerLab.setMaxBookingDuration(180); // 3 hours
        computerLab.setMinBookingDuration(60); // 1 hour
        resources.add(computerLab);

        // Projector Equipment
        Resource projector = new Resource();
        projector.setId("EQ001");
        projector.setName("HD Projector");
        projector.setType("EQUIPMENT");
        projector.setLocation("Equipment Room, Building A");
        projector.setCapacity(1);
        projector.setFeatures(Arrays.asList("HD Resolution", "HDMI Input", "Wireless"));
        projector.setDescription("High-definition projector for presentations");
        projector.setActive(true);
        projector.setManagedBy("RM001");
        projector.setAllowedRoles(Arrays.asList(
            UserProfile.Role.FACULTY.name(),
            UserProfile.Role.STAFF.name()
        ));
        projector.setMaxBookingDuration(480); // 8 hours
        projector.setMinBookingDuration(60); // 1 hour
        resources.add(projector);

        return resources;
    }

    @Test
    void testCreateBooking() {
        // Get test user and resource
        UserProfile student = testUsers.get(0);
        Resource computerLab = testResources.get(1);

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(student.getId());
        booking.setResourceId(computerLab.getId());
        booking.setResourceType(computerLab.getType());

        // Set booking time (next Monday, 10:00 AM to 12:00 PM)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startTime = cal.getTime();
        
        cal.set(Calendar.HOUR_OF_DAY, 12);
        Date endTime = cal.getTime();

        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose("Group Study Session");

        // Create booking
        Booking createdBooking = bookingContext.createBooking(booking);

        // Verify booking
        assertNotNull(createdBooking.getId());
        assertEquals(Booking.BookingStatus.PENDING, createdBooking.getStatus());
        assertEquals(student.getId(), createdBooking.getUserId());
        assertEquals(computerLab.getId(), createdBooking.getResourceId());
    }

    @Test
    void testApproveBooking() {
        // Get test users and resource
        UserProfile student = testUsers.get(0);
        UserProfile manager = testUsers.get(2);
        Resource conferenceRoom = testResources.get(0);

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(student.getId());
        booking.setResourceId(conferenceRoom.getId());
        booking.setResourceType(conferenceRoom.getType());

        // Set booking time
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 0);
        Date startTime = cal.getTime();
        
        cal.set(Calendar.HOUR_OF_DAY, 16);
        Date endTime = cal.getTime();

        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose("Team Meeting");

        // Create and approve booking
        Booking createdBooking = bookingContext.createBooking(booking);
        bookingContext.approveBooking(createdBooking.getId(), manager.getId());

        // Verify booking status
        List<Booking> userBookings = bookingContext.getUserBookings(student.getId());
        Booking approvedBooking = userBookings.stream()
            .filter(b -> b.getId().equals(createdBooking.getId()))
            .findFirst()
            .orElse(null);

        assertNotNull(approvedBooking);
        assertEquals(Booking.BookingStatus.APPROVED, approvedBooking.getStatus());
        assertEquals(manager.getId(), approvedBooking.getApprovedBy());
    }

    @Test
    void testResourceAvailability() {
        // Get test user and resource
        UserProfile faculty = testUsers.get(1);
        Resource conferenceRoom = testResources.get(0);

        // Create first booking
        Booking booking1 = new Booking();
        booking1.setUserId(faculty.getId());
        booking1.setResourceId(conferenceRoom.getId());
        booking1.setResourceType(conferenceRoom.getType());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        Date startTime1 = cal.getTime();
        
        cal.set(Calendar.HOUR_OF_DAY, 12);
        Date endTime1 = cal.getTime();

        booking1.setStartTime(startTime1);
        booking1.setEndTime(endTime1);
        booking1.setPurpose("Department Meeting");

        // Create and approve first booking
        Booking createdBooking1 = bookingContext.createBooking(booking1);
        bookingContext.approveBooking(createdBooking1.getId(), faculty.getId());

        // Try to create overlapping booking
        Booking booking2 = new Booking();
        booking2.setUserId(faculty.getId());
        booking2.setResourceId(conferenceRoom.getId());
        booking2.setResourceType(conferenceRoom.getType());

        cal.set(Calendar.HOUR_OF_DAY, 11);
        Date startTime2 = cal.getTime();
        
        cal.set(Calendar.HOUR_OF_DAY, 13);
        Date endTime2 = cal.getTime();

        booking2.setStartTime(startTime2);
        booking2.setEndTime(endTime2);
        booking2.setPurpose("Team Meeting");

        // Verify resource availability
        assertFalse(bookingContext.isResourceAvailable(conferenceRoom.getId(), startTime2, endTime2));
    }

    @Test
    void testRoleBasedAccess() {
        // Get test users and resource
        UserProfile student = testUsers.get(0);
        Resource conferenceRoom = testResources.get(0);

        // Verify student cannot book conference room
        assertFalse(resourceContext.isResourceAvailableForRole(conferenceRoom.getId(), UserProfile.Role.STUDENT.name()));

        // Verify faculty can book conference room
        assertTrue(resourceContext.isResourceAvailableForRole(conferenceRoom.getId(), UserProfile.Role.FACULTY.name()));
    }

    @Test
    void testBookingCancellation() {
        // Get test user and resource
        UserProfile faculty = testUsers.get(1);
        Resource projector = testResources.get(2);

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(faculty.getId());
        booking.setResourceId(projector.getId());
        booking.setResourceType(projector.getType());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        Date startTime = cal.getTime();
        
        cal.set(Calendar.HOUR_OF_DAY, 11);
        Date endTime = cal.getTime();

        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose("Lecture Presentation");

        // Create and approve booking
        Booking createdBooking = bookingContext.createBooking(booking);
        bookingContext.approveBooking(createdBooking.getId(), faculty.getId());

        // Cancel booking
        bookingContext.cancelBooking(createdBooking.getId(), faculty.getId());

        // Verify booking status
        List<Booking> userBookings = bookingContext.getUserBookings(faculty.getId());
        Booking cancelledBooking = userBookings.stream()
            .filter(b -> b.getId().equals(createdBooking.getId()))
            .findFirst()
            .orElse(null);

        assertNotNull(cancelledBooking);
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());
    }
} 