import { Card } from "@/components/ui/card"
import { Outlet } from "react-router"

const RootLayout = () => {
  return (
    <div className="flex min-w-svh flex-col items-center justify-center">
      <Card className="w-full max-w-lg">
        <Outlet />
      </Card>
    </div>
  )
}
export default RootLayout
