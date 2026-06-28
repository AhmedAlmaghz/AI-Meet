package com.example.core.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Auth : Screen("auth")
    object MeetingRoom : Screen("meeting_room")
    object Chat : Screen("chat")
    object AICopilot : Screen("ai_copilot")
}
