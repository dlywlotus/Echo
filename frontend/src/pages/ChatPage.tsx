import { ChatBubble } from "@/components/ChatBubble";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Spinner } from "@/components/ui/spinner";
import { cn } from "@/lib/utils";
import type { Client } from "@stomp/stompjs";
import { useEffect, useRef, useState } from "react";

type props = {
  setPage: React.Dispatch<React.SetStateAction<"home" | "lobby" | "chat">>;
  socketClient: Client;
  roomDetails: RoomDetails | null;
  currentUserId: string;
};

export type ChatEvent = {
  type: "MESSAGE" | "DISCONNECT" | "TYPING";
  userId: string;
  content: string | null;
  timestamp: string;
};

export type RoomDetails = {
  roomId: string;
  userOneId: string;
  userOneName: string;
  userTwoId: string;
  userTwoName: string;
};

const ChatPage = ({ setPage, socketClient, roomDetails, currentUserId }: props) => {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState<ChatEvent[]>([]);
  const bottomOfChatRef = useRef<HTMLDivElement>(null);
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [typingUsers, setTypingUsers] = useState<Set<String>>(new Set());

  const onInputChange = (e: React.ChangeEvent<HTMLInputElement, HTMLInputElement>) => {
    const inputLength = e.target.value.length;
    setInput(e.target.value);
    if (!socketClient.connected || !roomDetails?.roomId || inputLength > 1) return;

    // len == 2: send start typing event, len == 2: send stop typing event
    publishTypingEvent(inputLength == 1);
  };

  const onSendMessage = (e: React.SubmitEvent) => {
    e.preventDefault();
    if (!socketClient.connected || !roomDetails?.roomId || input == "") return;
    setInput("");

    socketClient.publish({
      destination: `/app/room/${roomDetails.roomId}/message`,
      body: JSON.stringify({ content: input.trim() }),
    });
    publishTypingEvent(false);
  };

  const onLeaveRoom = () => {
    if (!socketClient.connected || !roomDetails?.roomId) return;

    socketClient.publish({
      destination: `/app/room/${roomDetails?.roomId}/leave`,
    });
    setPage("home");
    publishTypingEvent(false);
  };

  const publishTypingEvent = (isTyping: boolean) => {
    socketClient.publish({
      destination: `/app/room/${roomDetails?.roomId}/typing`,
      body: JSON.stringify({ isTyping }),
    });
  };

  const onStartTyping = (userId: string) => {
    setTypingUsers((typingUsers) => new Set(typingUsers).add(userId));
  };

  const onStopTyping = (userId: string) => {
    setTypingUsers((typingUsers) => {
      const newSet = new Set(typingUsers);
      newSet.delete(userId);
      return newSet;
    });
  };

  const isTyping = () => {
    const otherUserId = roomDetails?.userOneId == currentUserId ? roomDetails.userTwoId : roomDetails?.userOneId;
    return typingUsers.has(otherUserId ?? "");
  };

  // Scroll to the bottom of the scroll container when a new message is appended
  useEffect(() => {
    if (bottomOfChatRef?.current) bottomOfChatRef.current.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages]);

  // Subscribe to room events
  useEffect(() => {
    if (!socketClient?.connected || !roomDetails?.roomId) return;

    const roomSubscription = socketClient.subscribe(`/topic/room/${roomDetails?.roomId}`, (message) => {
      const event: ChatEvent = JSON.parse(message.body);
      if (event.type === "MESSAGE") {
        console.log(scrollContainerRef?.current?.scrollTop);
        setMessages((messages) => [...messages, event]);
      } else if (event.type === "DISCONNECT") {
        // No point publishing the "stop typing" event when theres only one person left in the room, so just manually update typing set
        onStopTyping(event.userId);

        // Set the user id to be that of the disconncted person
        event.userId = roomDetails.userOneId == currentUserId ? roomDetails.userTwoId : roomDetails.userOneId;
        setMessages((messages) => [...messages, event]);
      } else if (event.type === "TYPING") {
        event.content === "true" ? onStartTyping(event.userId) : onStopTyping(event.userId);
      }
    });

    // Check if other person left the room already
    // If the other user left, a disconnect event will be sent to the room topic
    socketClient.publish({
      destination: `app/room/${roomDetails?.roomId}/validate`,
    });

    return () => {
      roomSubscription.unsubscribe();
    };
  }, [socketClient, roomDetails?.roomId]);

  if (!roomDetails) {
    return (
      <div className="flex h-full w-full flex-col items-center justify-center">
        <Spinner className="text-primary" />
      </div>
    );
  }

  return (
    <div className="flex h-full items-center justify-center lg:p-4">
      <Card className="h-full w-full gap-0 bg-background lg:max-h-150 lg:w-100">
        <CardHeader className="border-b">
          <CardTitle>
            Chatting with{" "}
            {roomDetails?.userOneId == currentUserId ? roomDetails?.userTwoName : roomDetails?.userOneName}
          </CardTitle>
          <CardDescription>
            You are {roomDetails?.userOneId == currentUserId ? roomDetails?.userOneName : roomDetails?.userTwoName}
          </CardDescription>
          <CardAction>
            <Button size={"sm"} onClick={onLeaveRoom}>
              Leave
            </Button>
          </CardAction>
        </CardHeader>
        {/* Note: The bottom padding is hard coded to fit the loading dots exactly */}
        <ScrollArea className="relative min-h-0 flex-1 px-4 pt-4 pb-11" ref={scrollContainerRef}>
          {messages.map((chatEvent) => (
            <ChatBubble
              key={`${chatEvent.timestamp}:${chatEvent.userId}`}
              chatEvent={chatEvent}
              currentUserId={currentUserId}
              roomDetails={roomDetails}
            />
          ))}
          <div ref={bottomOfChatRef}></div>
          <div className="absolute bottom-0 left-0 p-4">
            <span
              className={cn("loading loading-lg bg-primary loading-dots", !isTyping() ? "opacity-0" : "opacity-100")}
            ></span>
          </div>
        </ScrollArea>

        <CardFooter className="border-t">
          <form className="flex w-full flex-row gap-4" onSubmit={onSendMessage}>
            <Input type="text" placeholder="Type something" className="flex-1" onChange={onInputChange} value={input} />
            <Button>Send</Button>
          </form>
        </CardFooter>
      </Card>
    </div>
  );
};

export default ChatPage;
