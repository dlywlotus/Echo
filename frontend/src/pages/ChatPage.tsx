import { ChatBubble } from "@/components/ChatBubble";
import PageCard from "@/components/PageCard";
import { Button } from "@/components/ui/button";
import { CardAction, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Spinner } from "@/components/ui/spinner";
import type { Client } from "@stomp/stompjs";
import { useEffect, useState } from "react";

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

  useEffect(() => {
    if (!socketClient?.connected || !roomDetails?.roomId) return;

    const roomSubscription = socketClient.subscribe(`/topic/room/${roomDetails?.roomId}`, (message) => {
      const event: ChatEvent = JSON.parse(message.body);
      console.log("received event!", event);
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
    return <Spinner />;
  }

  return (
    <PageCard>
      <CardHeader>
        <CardTitle>
          Chatting with:{" "}
          <span className="text-primary">
            {roomDetails?.userOneId == currentUserId ? roomDetails?.userTwoName : roomDetails?.userOneName}
          </span>
        </CardTitle>
        <CardAction>
          <Button size={"sm"} onClick={onLeaveRoom}>
            Leave
          </Button>
        </CardAction>
      </CardHeader>
      <ScrollArea className="flex-1">
        {messages.map((chatEvent) => (
          <ChatBubble
            key={`${chatEvent.timestamp}:${chatEvent.userId}`}
            chatEvent={chatEvent}
            currentUserId={currentUserId}
            roomDetails={roomDetails}
          />
        ))}
      </ScrollArea>
      <CardFooter>
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
    </PageCard>
  );
};

export default ChatPage;
