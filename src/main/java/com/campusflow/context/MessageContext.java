package com.campusflow.context;

import com.google.firebase.database.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class MessageContext {
    private static MessageContext instance;
    private final FirebaseDatabase database;
    private final FirebaseAuth auth;
    private List<Chat> chats;
    private Chat currentChat;
    private List<Message> messages;
    private ValueEventListener chatsListener;
    private ValueEventListener messagesListener;

    private MessageContext() {
        this.database = FirebaseDatabase.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.chats = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public static synchronized MessageContext getInstance() {
        if (instance == null) {
            instance = new MessageContext();
        }
        return instance;
    }

    public void initialize() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // Listen for chats
        DatabaseReference chatsRef = database.getReference("chats")
            .orderByChild("lastActivity");

        chatsListener = chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Chat> newChats = new ArrayList<>();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    if (chat != null && chat.getParticipants().contains(user.getUid())) {
                        chat.setId(chatSnapshot.getKey());
                        newChats.add(chat);
                    }
                }
                chats = newChats;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    public void setCurrentChat(String chatId) {
        currentChat = chats.stream()
            .filter(chat -> chat.getId().equals(chatId))
            .findFirst()
            .orElse(null);

        if (currentChat != null) {
            loadMessages(chatId);
        }
    }

    private void loadMessages(String chatId) {
        if (messagesListener != null) {
            database.getReference("messages")
                .child(chatId)
                .removeEventListener(messagesListener);
        }

        DatabaseReference messagesRef = database.getReference("messages")
            .child(chatId)
            .orderByChild("timestamp")
            .limitToLast(50);

        messagesListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Message> newMessages = new ArrayList<>();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        message.setId(messageSnapshot.getKey());
                        newMessages.add(message);
                    }
                }
                messages = newMessages;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    public void sendMessage(String content, String receiverId, String type, Object metadata) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // Find or create chat
        Chat chat = chats.stream()
            .filter(c -> c.getParticipants().contains(user.getUid()) && 
                        c.getParticipants().contains(receiverId))
            .findFirst()
            .orElse(null);

        if (chat == null) {
            // Create new chat
            DatabaseReference newChatRef = database.getReference("chats").push();
            chat = new Chat();
            chat.setId(newChatRef.getKey());
            chat.setParticipants(List.of(user.getUid(), receiverId));
            chat.setUnreadCount(0);
            chat.setLastActivity(new Date());
            newChatRef.setValue(chat);
        }

        // Add message
        Message message = new Message();
        message.setChatId(chat.getId());
        message.setSenderId(user.getUid());
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(type);
        message.setMetadata(metadata);
        message.setTimestamp(new Date());
        message.setRead(false);

        database.getReference("messages")
            .child(chat.getId())
            .push()
            .setValue(message);

        // Update chat's last message and activity
        chat.setLastMessage(message);
        chat.setLastActivity(new Date());
        database.getReference("chats")
            .child(chat.getId())
            .setValue(chat);
    }

    public void markAsRead(String chatId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        DatabaseReference messagesRef = database.getReference("messages")
            .child(chatId);

        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null && 
                        message.getReceiverId().equals(user.getUid()) && 
                        !message.isRead()) {
                        messageSnapshot.getRef().child("read").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    public List<Chat> getChats() {
        return chats;
    }

    public Chat getCurrentChat() {
        return currentChat;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void cleanup() {
        if (chatsListener != null) {
            database.getReference("chats").removeEventListener(chatsListener);
        }
        if (messagesListener != null && currentChat != null) {
            database.getReference("messages")
                .child(currentChat.getId())
                .removeEventListener(messagesListener);
        }
    }
} 