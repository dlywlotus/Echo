import { cn } from "@/lib/utils";
import type { ChatEvent, RoomDetails } from "@/pages/ChatPage";
import { Badge } from "./ui/badge";

type props = {
  chatEvent: ChatEvent;
  currentUserId: string;
  roomDetails: RoomDetails;
};

export function ChatBubble({ chatEvent, currentUserId, roomDetails }: props) {
  const timestamp = new Date(chatEvent.timestamp);

  if (chatEvent.type == "DISCONNECT") {
    return (
      <div className="mb-4 flex w-full justify-center">
        <Badge variant={"outline"} className="p-4">
          {chatEvent.userId == roomDetails.userOneId ? roomDetails.userOneName : roomDetails.userTwoName} has left
        </Badge>
      </div>
    );
  }

  return (
    <div className={cn("mb-4 flex w-full", chatEvent.userId == currentUserId ? "justify-end" : "justify-start")}>
      <div
        className={cn(
          "relative max-w-[75%] px-3 py-1.5 text-xs",
          chatEvent.userId == currentUserId
            ? "border-borders rounded-l-2xl rounded-tr-2xl rounded-br-none border dark:bg-input/30"
            : "rounded-tl-2xl rounded-r-2xl rounded-bl-none border border-primary bg-primary text-primary-foreground"
        )}
      >
        <p className="leading-relaxed wrap-break-word">{chatEvent.content}</p>
        <div className="text-right">
          {timestamp.getHours()}:{timestamp.getMinutes()}
        </div>
      </div>
    </div>
  );
}
