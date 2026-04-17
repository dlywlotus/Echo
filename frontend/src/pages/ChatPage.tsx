import { ChatBubble } from "@/components/ChatBubble";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ItemSeparator } from "@/components/ui/item";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Spinner } from "@/components/ui/spinner";
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

  const onSendMessage = (e: React.SubmitEvent) => {
    e.preventDefault();
    if (!socketClient.connected || !roomDetails?.roomId || input == "") return;
    setInput("");

    socketClient.publish({
      destination: `/app/room/${roomDetails.roomId}/message`,
      body: JSON.stringify({ content: input.trim() }),
    });
  };

  const onLeaveRoom = () => {
    if (!socketClient.connected || !roomDetails?.roomId) return;

    socketClient.publish({
      destination: `/app/room/${roomDetails?.roomId}/leave`,
    });
    setPage("home");
  };

  // Scroll to the bottom of the scroll container when a new message is appended
  useEffect(() => {
    if (bottomOfChatRef?.current) bottomOfChatRef.current.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages]);

  useEffect(() => {
    if (!socketClient?.connected || !roomDetails?.roomId) return;

    const roomSubscription = socketClient.subscribe(`/topic/room/${roomDetails?.roomId}`, (message) => {
      const event: ChatEvent = JSON.parse(message.body);
      if (event.type == "MESSAGE") {
        setMessages((messages) => [...messages, event]);
      } else if (event.type == "DISCONNECT") {
        // Set the user id to be that of the disconncted person
        event.userId = roomDetails.userOneId == currentUserId ? roomDetails.userTwoId : roomDetails.userOneId;
        setMessages((messages) => [...messages, event]);
      } else if (event.type == "TYPING") {
        // render a typing animation
      }
    });

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
      <Card className="h-full w-full bg-background py-0 lg:max-h-150 lg:w-100">
        <CardHeader className="border-b py-4">
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
        <ScrollArea className="min-h-0 flex-1 px-4">
          {messages.map((chatEvent) => (
            <ChatBubble
              key={`${chatEvent.timestamp}:${chatEvent.userId}`}
              chatEvent={chatEvent}
              currentUserId={currentUserId}
              roomDetails={roomDetails}
            />
          ))}
          <div ref={bottomOfChatRef}></div>
        </ScrollArea>
        <CardFooter className="border-t py-4">
          <form className="flex w-full flex-row gap-4" onSubmit={onSendMessage}>
            <Input
              type="text"
              placeholder="Type something"
              className="flex-1"
              onChange={(e) => setInput(e.target.value)}
              value={input}
            />
            <Button>Send</Button>
          </form>
        </CardFooter>
      </Card>
    </div>
  );
};

export default ChatPage;
