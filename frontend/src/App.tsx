import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import HomePage from "./pages/HomePage";
import LobbyPage from "./pages/LobbyPage";
import ChatPage, { type RoomDetails } from "./pages/ChatPage";
import { v4 as uuidv4 } from "uuid";
import { Spinner } from "./components/ui/spinner";

const userId = uuidv4();

const App = () => {
  const [page, setPage] = useState<"home" | "lobby" | "chat">("home");
  const [activeUserCount, setActiveUserCount] = useState<number>(0);
  const [socketClient, setSocketClient] = useState<Client | null>(null);
  const [roomDetails, setRoomDetails] = useState<RoomDetails | null>(null);
  const [currentUserId, setCurrentUserId] = useState<string | null>(null);

  useEffect(() => {
    setCurrentUserId(userId);

    const client = new Client({
      brokerURL: "ws://localhost:8080/web-socket",
      connectHeaders: {
        "user-id": userId,
      },
      onConnect: () => {
        client.subscribe(`/queue/user/${userId}/new-room`, (message) => {
          const newRoomDetails: RoomDetails = JSON.parse(message.body);
          setRoomDetails(newRoomDetails);
          setPage("chat");
        });
        client.subscribe(`/topic/global/stats/active-users`, (message) => {
          setActiveUserCount(parseInt(message.body));
        });
        setSocketClient(client);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  if (!socketClient || !currentUserId)
    return (
      <div className="flex h-svh w-full flex-col items-center justify-center">
        <Spinner className="text-primary" />
      </div>
    );

  return (
    <>
      <div className="h-svh">
        {page == "home" && <HomePage setPage={setPage} socketClient={socketClient} activeUserCount={activeUserCount} />}
        {page == "lobby" && <LobbyPage setPage={setPage} activeUserCount={activeUserCount} />}
        {page == "chat" && (
          <ChatPage
            setPage={setPage}
            socketClient={socketClient}
            roomDetails={roomDetails}
            currentUserId={currentUserId}
          />
        )}
      </div>
    </>
  );
};
export default App;
