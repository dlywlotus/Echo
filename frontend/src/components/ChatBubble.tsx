import { cn } from "@/lib/utils";
import type { ChatEvent } from "@/pages/ChatPage";

type props = {
    chatEvent: ChatEvent;
    currentUserId: string;
};

export function ChatBubble({ chatEvent, currentUserId }: props) {
    const isMe = chatEvent.userId === "123";

    return (
        <div
            className={cn(
                "mb-4 flex w-full px-4",
                isMe ? "justify-end" : "justify-start"
            )}
        >
            <div
                className={cn(
                    "relative max-w-[75%] px-4 py-2 text-sm shadow-sm",
                    isMe
                        ? "rounded-l-2xl rounded-tr-2xl rounded-br-none border border-border"
                        : "rounded-tl-2xl rounded-r-2xl rounded-bl-none bg-muted text-foreground"
                )}
            >
                <p className="leading-relaxed wrap-break-word">
                    {chatEvent.content}
                </p>
                <span className="mt-1 block text-right text-[10px] opacity-70">
                    {/* {chatEvent.timestamp} */ "10: 20"}
                </span>
            </div>
        </div>
    );
}
