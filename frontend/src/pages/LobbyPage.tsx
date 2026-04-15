import PageCard from "@/components/PageCard";
import { Button } from "@/components/ui/button";
import { CardContent } from "@/components/ui/card";

type props = {
    setPage: React.Dispatch<React.SetStateAction<"home" | "lobby" | "chat">>;
    activeUserCount: number;
};

const LobbyPage = ({ setPage, activeUserCount }: props) => {
    const onCancel = () => {
        setPage("home");
    };

    return (
        <PageCard>
            <CardContent className="flex h-full flex-col items-center justify-center gap-2">
                <div>
                    <span className="text-primary">{activeUserCount} </span>
                    users online
                </div>
                <div className="text-lg">Looking for a match</div>
                <Button
                    variant="outline"
                    className="mt-2 text-primary"
                    onClick={onCancel}
                >
                    Cancel
                </Button>
            </CardContent>
        </PageCard>
    );
};
export default LobbyPage;
