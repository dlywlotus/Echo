import { Outlet } from "react-router"

const RootLayout = () => {
  return (
    <div className="flex flex-col items-center justify-center h-svh">
      <Outlet />
    </div>
  )
}
export default RootLayout
