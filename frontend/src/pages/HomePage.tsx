import { Input } from "@/components/ui/input"
import { BotMessageSquare } from "lucide-react"

import CustomCard from "@/components/custom/CustomCard"
import { Button } from "@/components/ui/button"

export default function HomePage() {
  return (
    <CustomCard>

      <div className="mx-auto bg-primary/10 w-24 h-24 rounded-xl flex items-center justify-center">
        <BotMessageSquare className="text-primary w-12 h-12" />
      </div>
      <div className="text-base">
        <span className="text-primary">Echo,</span> annonymous real time chats
      </div>
      <div className="w-full">
        <label className="self-start">
          Display Name
        </label>
        <Input className="mt-2"
          placeholder="e.g. dlywlotus"
        />
      </div>
      <Button className="w-full" size={"lg"}>Start chatting</Button>
    </CustomCard>
  )
}