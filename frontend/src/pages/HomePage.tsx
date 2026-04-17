import { Input } from "@/components/ui/input";
import { BotMessageSquare } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import type { Client } from "@stomp/stompjs";
import { useState } from "react";

type props = {
  setPage: React.Dispatch<React.SetStateAction<"home" | "lobby" | "chat">>;
  socketClient: Client | null;
  activeUserCount: number;
};

const HomePage = ({ setPage, socketClient, activeUserCount }: props) => {
  const [username, setUsername] = useState<string>("");

  const onInputChange = (e: React.ChangeEvent<HTMLInputElement, HTMLInputElement>) => {
    if (e.target.value.length > 25) return;
    setUsername(e.target.value);
  };

  const onJoinLobby = (e: React.SubmitEvent) => {
    e.preventDefault();
    if (!socketClient || !username) return;

    socketClient.publish({
      destination: "/app/lobby/join",
      body: JSON.stringify({ username }),
    });
    setPage("lobby");
  };

  return (
    <div className="relative flex h-full flex-col items-center justify-center gap-4">
      <Card className="p-4">
        <BotMessageSquare className="h-10 w-10 text-primary" strokeWidth={1.5} />
      </Card>
      <div>
        <span className="text-primary">Echo,</span> annonymous real time chats
      </div>
      <form onSubmit={onJoinLobby} className="p-4">
        <Input placeholder="Enter your display name" className="text-sm" onChange={onInputChange} value={username} />
        <Button className="mt-4 w-full" size={"lg"}>
          Start chatting
        </Button>
      </form>
      <div className="absolute bottom-0 left-0 p-4">
        <span className="text-primary">{activeUserCount} </span>
        users online
      </div>
    </div>
  );
};

export default HomePage;
