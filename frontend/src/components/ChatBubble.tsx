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
        <Badge>
          {chatEvent.userId == roomDetails.userOneId ? roomDetails.userOneName : roomDetails.userTwoName} has left
        </Badge>
      </div>
    );
  }

  return (
    <div className={cn("mb-4 flex w-full px-4", chatEvent.userId == currentUserId ? "justify-end" : "justify-start")}>
      <div
        className={cn(
          "relative max-w-[75%] px-4 py-2 text-sm shadow-sm",
          chatEvent.userId == currentUserId
            ? "rounded-l-2xl rounded-tr-2xl rounded-br-none border border-border"
            : "rounded-tl-2xl rounded-r-2xl rounded-bl-none border border-primary"
        )}
      >
        <p className="leading-relaxed wrap-break-word">{chatEvent.content}</p>
        <div className="mt-1 text-right text-xs">
          {timestamp.getHours()}:{timestamp.getMinutes()}
        </div>
      </div>
    </div>
  );
}
