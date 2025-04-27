import React, { createContext, useContext, useState, useEffect } from 'react';
import { useApp } from './AppContext';
import { useUser } from './UserContext';
import { db } from '../firebase';
import { collection, query, where, onSnapshot, orderBy, limit, addDoc, serverTimestamp } from 'firebase/firestore';

interface Message {
  id: string;
  senderId: string;
  receiverId: string;
  content: string;
  timestamp: Date;
  read: boolean;
  type: 'text' | 'image' | 'file';
  metadata?: any;
}

interface Chat {
  id: string;
  participants: string[];
  lastMessage?: Message;
  unreadCount: number;
  lastActivity: Date;
}

interface MessageContextType {
  chats: Chat[];
  currentChat?: Chat;
  messages: Message[];
  sendMessage: (content: string, receiverId: string, type?: 'text' | 'image' | 'file', metadata?: any) => Promise<void>;
  markAsRead: (chatId: string) => void;
  setCurrentChat: (chatId: string) => void;
}

const MessageContext = createContext<MessageContextType>({
  chats: [],
  messages: [],
  sendMessage: async () => {},
  markAsRead: () => {},
  setCurrentChat: () => {},
});

export const useMessage = () => useContext(MessageContext);

export const MessageProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { settings } = useApp();
  const { user } = useUser();
  const [chats, setChats] = useState<Chat[]>([]);
  const [currentChat, setCurrentChat] = useState<Chat>();
  const [messages, setMessages] = useState<Message[]>([]);

  // Fetch user's chats
  useEffect(() => {
    if (!user || !settings.messaging.enabled) return;

    const chatsRef = collection(db, 'chats');
    const q = query(
      chatsRef,
      where('participants', 'array-contains', user.uid),
      orderBy('lastActivity', 'desc')
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const newChats = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
        lastActivity: doc.data().lastActivity.toDate(),
        lastMessage: doc.data().lastMessage
          ? {
              ...doc.data().lastMessage,
              timestamp: doc.data().lastMessage.timestamp.toDate(),
            }
          : undefined,
      })) as Chat[];

      setChats(newChats);
    });

    return () => unsubscribe();
  }, [user, settings.messaging.enabled]);

  // Fetch messages for current chat
  useEffect(() => {
    if (!user || !currentChat) return;

    const messagesRef = collection(db, 'messages');
    const q = query(
      messagesRef,
      where('chatId', '==', currentChat.id),
      orderBy('timestamp', 'desc'),
      limit(50)
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const newMessages = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
        timestamp: doc.data().timestamp.toDate(),
      })) as Message[];

      setMessages(newMessages);
    });

    return () => unsubscribe();
  }, [user, currentChat]);

  const sendMessage = async (
    content: string,
    receiverId: string,
    type: 'text' | 'image' | 'file' = 'text',
    metadata?: any
  ) => {
    if (!user) return;

    // Find or create chat
    let chat = chats.find(
      (c) =>
        c.participants.includes(user.uid) && c.participants.includes(receiverId)
    );

    if (!chat) {
      // Create new chat
      const chatRef = await addDoc(collection(db, 'chats'), {
        participants: [user.uid, receiverId],
        lastActivity: serverTimestamp(),
      });
      chat = {
        id: chatRef.id,
        participants: [user.uid, receiverId],
        unreadCount: 0,
        lastActivity: new Date(),
      };
    }

    // Add message
    await addDoc(collection(db, 'messages'), {
      chatId: chat.id,
      senderId: user.uid,
      receiverId,
      content,
      type,
      metadata,
      timestamp: serverTimestamp(),
      read: false,
    });

    // Update chat's last message and activity
    // This will trigger the chats listener
  };

  const markAsRead = async (chatId: string) => {
    if (!user) return;
    // Update messages in Firestore
    // This will trigger the messages listener
  };

  return (
    <MessageContext.Provider
      value={{
        chats,
        currentChat,
        messages,
        sendMessage,
        markAsRead,
        setCurrentChat: (chatId: string) => {
          const chat = chats.find((c) => c.id === chatId);
          setCurrentChat(chat);
        },
      }}
    >
      {children}
    </MessageContext.Provider>
  );
}; 