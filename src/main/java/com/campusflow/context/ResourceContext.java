package com.campusflow.context;

import com.campusflow.model.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ResourceContext {
    private static ResourceContext instance;
    private final ConcurrentHashMap<String, Resource> resources;
    private final ExecutorService resourceExecutor;
    private final List<Consumer<Resource>> resourceListeners;

    private ResourceContext() {
        this.resources = new ConcurrentHashMap<>();
        this.resourceExecutor = Executors.newSingleThreadExecutor();
        this.resourceListeners = new ArrayList<>();
    }

    public static synchronized ResourceContext getInstance() {
        if (instance == null) {
            instance = new ResourceContext();
        }
        return instance;
    }

    public void addResourceListener(Consumer<Resource> listener) {
        resourceListeners.add(listener);
    }

    public void removeResourceListener(Consumer<Resource> listener) {
        resourceListeners.remove(listener);
    }

    public Resource createResource(Resource resource) {
        resource.setId(generateResourceId());
        resource.setActive(true);

        resourceExecutor.submit(() -> {
            resources.put(resource.getId(), resource);
            
            // Notify listeners
            for (Consumer<Resource> listener : resourceListeners) {
                listener.accept(resource);
            }

            // Persist resource
            persistResource(resource);
        });

        return resource;
    }

    private String generateResourceId() {
        return "RS-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private void persistResource(Resource resource) {
        // Implement database persistence
        // This could be JDBC, JPA, etc.
    }

    public Resource getResource(String resourceId) {
        return resources.get(resourceId);
    }

    public List<Resource> getAllResources() {
        return new ArrayList<>(resources.values());
    }

    public List<Resource> getResourcesByType(String type) {
        List<Resource> filteredResources = new ArrayList<>();
        resources.values().forEach(resource -> {
            if (resource.getType().equals(type)) {
                filteredResources.add(resource);
            }
        });
        return filteredResources;
    }

    public List<Resource> getActiveResources() {
        List<Resource> activeResources = new ArrayList<>();
        resources.values().forEach(resource -> {
            if (resource.isActive()) {
                activeResources.add(resource);
            }
        });
        return activeResources;
    }

    public void updateResource(Resource resource) {
        resourceExecutor.submit(() -> {
            if (resources.containsKey(resource.getId())) {
                resources.put(resource.getId(), resource);
                
                // Notify listeners
                for (Consumer<Resource> listener : resourceListeners) {
                    listener.accept(resource);
                }

                // Update in database
                updateResourceInDatabase(resource);
            }
        });
    }

    private void updateResourceInDatabase(Resource resource) {
        // Implement database update
    }

    public void deactivateResource(String resourceId) {
        resourceExecutor.submit(() -> {
            Resource resource = resources.get(resourceId);
            if (resource != null) {
                resource.setActive(false);
                resources.put(resourceId, resource);
                
                // Notify listeners
                for (Consumer<Resource> listener : resourceListeners) {
                    listener.accept(resource);
                }

                // Update in database
                updateResourceInDatabase(resource);
            }
        });
    }

    public boolean isResourceAvailableForRole(String resourceId, String role) {
        Resource resource = resources.get(resourceId);
        if (resource == null || !resource.isActive()) {
            return false;
        }
        return resource.getAllowedRoles().contains(role);
    }

    public void shutdown() {
        resourceExecutor.shutdown();
    }
} 