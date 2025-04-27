package com.campusflow.context;

import com.campusflow.model.Booking;
import com.campusflow.model.Booking.BookingStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class BookingContext {
    private static BookingContext instance;
    private final ConcurrentHashMap<String, List<Booking>> userBookings;
    private final ConcurrentHashMap<String, List<Booking>> resourceBookings;
    private final ExecutorService bookingExecutor;
    private final List<Consumer<Booking>> bookingListeners;

    private BookingContext() {
        this.userBookings = new ConcurrentHashMap<>();
        this.resourceBookings = new ConcurrentHashMap<>();
        this.bookingExecutor = Executors.newSingleThreadExecutor();
        this.bookingListeners = new ArrayList<>();
    }

    public static synchronized BookingContext getInstance() {
        if (instance == null) {
            instance = new BookingContext();
        }
        return instance;
    }

    public void addBookingListener(Consumer<Booking> listener) {
        bookingListeners.add(listener);
    }

    public void removeBookingListener(Consumer<Booking> listener) {
        bookingListeners.remove(listener);
    }

    public Booking createBooking(Booking booking) {
        booking.setId(generateBookingId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(new Date());
        booking.setUpdatedAt(new Date());

        bookingExecutor.submit(() -> {
            // Add to user's bookings
            List<Booking> userBookingsList = userBookings.computeIfAbsent(
                booking.getUserId(), k -> new ArrayList<>()
            );
            userBookingsList.add(booking);

            // Add to resource's bookings
            List<Booking> resourceBookingsList = resourceBookings.computeIfAbsent(
                booking.getResourceId(), k -> new ArrayList<>()
            );
            resourceBookingsList.add(booking);

            // Notify listeners
            for (Consumer<Booking> listener : bookingListeners) {
                listener.accept(booking);
            }

            // Persist booking
            persistBooking(booking);
        });

        return booking;
    }

    private String generateBookingId() {
        return "BK-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private void persistBooking(Booking booking) {
        // Implement database persistence
        // This could be JDBC, JPA, etc.
    }

    public List<Booking> getUserBookings(String userId) {
        return userBookings.getOrDefault(userId, new ArrayList<>());
    }

    public List<Booking> getResourceBookings(String resourceId) {
        return resourceBookings.getOrDefault(resourceId, new ArrayList<>());
    }

    public List<Booking> getPendingBookings() {
        List<Booking> pendingBookings = new ArrayList<>();
        userBookings.values().forEach(bookings -> 
            bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING)
                .forEach(pendingBookings::add)
        );
        return pendingBookings;
    }

    public void approveBooking(String bookingId, String approvedBy) {
        bookingExecutor.submit(() -> {
            userBookings.values().forEach(bookings -> 
                bookings.stream()
                    .filter(booking -> booking.getId().equals(bookingId))
                    .findFirst()
                    .ifPresent(booking -> {
                        booking.setStatus(BookingStatus.APPROVED);
                        booking.setApprovedBy(approvedBy);
                        booking.setUpdatedAt(new Date());
                        updateBookingInDatabase(booking);
                    })
            );
        });
    }

    public void rejectBooking(String bookingId, String rejectedBy, String reason) {
        bookingExecutor.submit(() -> {
            userBookings.values().forEach(bookings -> 
                bookings.stream()
                    .filter(booking -> booking.getId().equals(bookingId))
                    .findFirst()
                    .ifPresent(booking -> {
                        booking.setStatus(BookingStatus.REJECTED);
                        booking.setRejectedBy(rejectedBy);
                        booking.setRejectionReason(reason);
                        booking.setUpdatedAt(new Date());
                        updateBookingInDatabase(booking);
                    })
            );
        });
    }

    public void cancelBooking(String bookingId, String userId) {
        bookingExecutor.submit(() -> {
            List<Booking> userBookingsList = userBookings.get(userId);
            if (userBookingsList != null) {
                userBookingsList.stream()
                    .filter(booking -> booking.getId().equals(bookingId))
                    .findFirst()
                    .ifPresent(booking -> {
                        booking.setStatus(BookingStatus.CANCELLED);
                        booking.setUpdatedAt(new Date());
                        updateBookingInDatabase(booking);
                    });
            }
        });
    }

    private void updateBookingInDatabase(Booking booking) {
        // Implement database update
    }

    public boolean isResourceAvailable(String resourceId, Date startTime, Date endTime) {
        List<Booking> resourceBookingsList = resourceBookings.get(resourceId);
        if (resourceBookingsList == null) return true;

        return resourceBookingsList.stream()
            .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED && 
                             booking.getStatus() != BookingStatus.REJECTED)
            .noneMatch(booking -> 
                (startTime.after(booking.getStartTime()) && startTime.before(booking.getEndTime())) ||
                (endTime.after(booking.getStartTime()) && endTime.before(booking.getEndTime())) ||
                (startTime.before(booking.getStartTime()) && endTime.after(booking.getEndTime()))
            );
    }

    public void shutdown() {
        bookingExecutor.shutdown();
    }
} 