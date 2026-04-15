import { Input } from "@/components/ui/input";
import { BotMessageSquare } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import PageCard from "@/components/PageCard";
import type { Client } from "@stomp/stompjs";
import { useState } from "react";

type props = {
    setPage: React.Dispatch<React.SetStateAction<"home" | "lobby" | "chat">>;
    socketClient: Client | null;
    activeUserCount: number;
};

const HomePage = ({ setPage, socketClient, activeUserCount }: props) => {
    const [username, setUsername] = useState<string>();

    const onJoinLobby = () => {
        if (!socketClient || !username) return;
        socketClient.publish({
            destination: "/app/lobby/join",
            body: JSON.stringify({ username }),
        });
        setPage("lobby");
    };

    return (
        <PageCard>
            <CardContent className="flex h-full flex-col items-center justify-center gap-4">
                <Card className="p-6">
                    <BotMessageSquare
                        className="h-12 w-12 text-primary"
                        strokeWidth={1.5}
                    />
                </Card>
                <div className="text-base">
                    <span className="text-primary">Echo,</span> annonymous real
                    time chats
                </div>
                <div className="w-full">
                    <label className="self-start">Display Name</label>
                    <Input
                        className="mt-2"
                        placeholder="e.g. dlywlotus"
                        onChange={(e) => setUsername(e.target.value)}
                    />
                </div>
                <Button className="w-full" size={"lg"} onClick={onJoinLobby}>
                    Start chatting
                </Button>
            </CardContent>
            <CardFooter>
                <div>
                    <span className="text-primary">{activeUserCount} </span>
                    users online
                </div>
            </CardFooter>
        </PageCard>
    );
};

export default HomePage;
