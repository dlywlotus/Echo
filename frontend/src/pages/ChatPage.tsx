import { ChatBubble } from "@/components/ChatBubble";
import PageCard from "@/components/PageCard";
import { Button } from "@/components/ui/button";
import { CardAction, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { Client } from "@stomp/stompjs";
import { SendHorizonal } from "lucide-react";
import { useEffect } from "react";

type props = {
    setPage: React.Dispatch<React.SetStateAction<"home" | "lobby" | "chat">>;
    socketClient: Client;
    roomDetails: RoomDetails | null;
    currentUserId: string;
};

export type ChatEvent = {
    type: string;
    userId: string;
    content: string | null;
    isTyping: boolean | null;
};

const mockEventList = [
    {
        type: "MESSAGE",
        userId: "123",
        content: "First message",
        isTyping: null,
    },
    {
        type: "MESSAGE",
        userId: "456",
        content: "Second message",
        isTyping: null,
    },
    {
        type: "MESSAGE",
        userId: "123",
        content: "Third message",
        isTyping: null,
    },
    {
        type: "MESSAGE",
        userId: "456",
        content: "Fourth message",
        isTyping: null,
    },
    { type: "MESSAGE", userId: "123", content: "Last message", isTyping: null },
];

export type RoomDetails = {
    roomId: string;
    userOneId: string;
    userOneName: string;
    userTwoId: string;
    userTwoName: string;
};

const ChatPage = ({ setPage, socketClient, roomDetails, currentUserId }: props) => {
    const onSendMessage = () => {};

    const onLeaveRoom = () => {
        setPage("home");
    };

    useEffect(() => {
        if (!socketClient?.connected || !roomDetails?.roomId) return;

        const roomSubscription = socketClient.subscribe(`/topic/room/${roomDetails?.roomId}`, (message) => {
            console.log(`RECEIVED ${message.body}`);
        });

        socketClient.publish({
            destination: `app/room/${roomDetails?.roomId}/validate`,
        });

        const username = currentUserId == roomDetails?.userOneId ? roomDetails?.userOneName : roomDetails?.userTwoName;
        console.log(username);

        return () => {
            roomSubscription.unsubscribe();
        };
    }, [socketClient, roomDetails?.roomId]);

    return (
        <PageCard>
            <CardHeader>
                <CardTitle>
                    Chatting with:{" "}
                    <span className="text-primary">
                        {roomDetails?.userOneId ? roomDetails?.userTwoName : roomDetails?.userOneName}
                    </span>
                </CardTitle>
                <CardAction>
                    <Button size={"sm"} onClick={onLeaveRoom}>
                        Leave
                    </Button>
                </CardAction>
            </CardHeader>
            <ScrollArea className="flex-1">
                {mockEventList.map((chatEvent, index) => (
                    <ChatBubble key={index} chatEvent={chatEvent} currentUserId={currentUserId} />
                ))}
            </ScrollArea>
            <CardFooter>
                <form className="flex w-full flex-row gap-4" onSubmit={onSendMessage}>
                    <Input type="text" placeholder="Type something" className="flex-1" />
                    <Button>
                        <SendHorizonal />
                    </Button>
                </form>
            </CardFooter>
        </PageCard>
    );
};

export default ChatPage;
