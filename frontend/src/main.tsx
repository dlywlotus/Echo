import { StrictMode } from "react"
import { createRoot } from "react-dom/client"

import "./index.css"
import { ThemeProvider } from "@/components/theme-provider.tsx"
import { BrowserRouter, Route, Routes } from "react-router"
import HomePage from "./pages/HomePage"
import LobbyPage from "./pages/LobbyPage"
import ChatRoomPage from "./pages/ChatRoomPage"
import RootLayout from "./pages/RootLayout"

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <Routes>
          <Route element={<RootLayout />}>
            <Route index element={<HomePage />} />
            <Route path="lobby" element={<LobbyPage />} />
            <Route path="chat-room" element={<ChatRoomPage />} />
          </Route>
        </Routes>
      </ThemeProvider>
    </BrowserRouter>
  </StrictMode>
)
