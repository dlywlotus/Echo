import { Button } from "@/components/ui/button";

type props = {
  setPage: React.Dispatch<React.SetStateAction<"home" | "lobby" | "chat">>;
  activeUserCount: number;
};

const LobbyPage = ({ setPage, activeUserCount }: props) => {
  const onCancel = () => {
    setPage("home");
  };

  return (
    <div className="flex h-full flex-col items-center justify-center gap-2">
      <div className="text-sm">
        <span className="text-primary">{activeUserCount} </span>
        users online
      </div>
      <div>Looking for a match</div>
      <span className="loading my-2 loading-xl loading-ring text-primary"></span>
      <Button variant="outline" onClick={onCancel}>
        Cancel
      </Button>
    </div>
  );
};
export default LobbyPage;
