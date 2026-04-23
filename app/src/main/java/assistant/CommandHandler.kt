package com.example.talkai.assistant

class CommandHandler {

    enum class CommandType {
        NEARBY_HOSPITAL,
        NEARBY_SHOP,
        NEARBY_RESTAURANT,
        NEARBY_PHARMACY,
        NEARBY_BANK,
        NEARBY_POLICE,
        NEARBY_SCHOOL,
        WHAT_IS_AROUND,
        START_NAVIGATION,
        STOP_NAVIGATION,
        HOW_AM_I_LOOKING,
        READ_TEXT,
        GENERAL_CHAT
    }

    fun detectCommand(input: String): CommandType {
        val text = input.lowercase().trim()
        return when {

            // 📖 Book / Text Reading
            containsAny(
                text,
                "read book", "read this", "read page",
                "what does this say", "read text",
                "read for me", "what is written",
                "read document", "scan this"
            ) -> CommandType.READ_TEXT

            // 🏥 Hospital
            containsAny(
                text,
                "hospital", "doctor", "clinic",
                "medical", "emergency", "ambulance"
            ) -> CommandType.NEARBY_HOSPITAL

            // 💊 Pharmacy
            containsAny(
                text,
                "pharmacy", "medicine", "chemist",
                "drug", "tablet", "pills"
            ) -> CommandType.NEARBY_PHARMACY

            // 🛒 Shop
            containsAny(
                text,
                "shop", "store", "market",
                "buy", "purchase", "grocery",
                "supermarket"
            ) -> CommandType.NEARBY_SHOP

            // 🍽️ Restaurant
            containsAny(
                text,
                "restaurant", "food", "eat",
                "hungry", "cafe", "hotel", "dhaba"
            ) -> CommandType.NEARBY_RESTAURANT

            // 🏦 Bank
            containsAny(
                text,
                "bank", "atm", "cash",
                "money", "withdraw"
            ) -> CommandType.NEARBY_BANK

            // 👮 Police
            containsAny(
                text,
                "police", "station", "danger",
                "unsafe", "crime", "help police"
            ) -> CommandType.NEARBY_POLICE

            // 🏫 School
            containsAny(
                text,
                "school", "college",
                "university", "education"
            ) -> CommandType.NEARBY_SCHOOL

            // 👁️ What is around
            containsAny(
                text,
                "what is around", "what around",
                "around me", "what do you see",
                "describe", "surroundings",
                "what can you see", "look around"
            ) -> CommandType.WHAT_IS_AROUND

            // 🧭 Start Navigation
            containsAny(
                text,
                "start navigation", "navigation mode",
                "help me walk", "guide me",
                "start guiding", "navigate",
                "start walking", "help me move"
            ) -> CommandType.START_NAVIGATION

            // 🛑 Stop Navigation
            containsAny(
                text,
                "stop navigation", "stop guiding",
                "stop helping", "end navigation",
                "normal mode", "stop walking"
            ) -> CommandType.STOP_NAVIGATION

            // 😊 Emotion
            containsAny(
                text,
                "how do i look", "how am i looking",
                "my expression", "my face",
                "my emotion", "my mood",
                "what is my emotion"
            ) -> CommandType.HOW_AM_I_LOOKING

            // 💬 General Chat
            else -> CommandType.GENERAL_CHAT
        }
    }

    private fun containsAny(
        text: String,
        vararg keywords: String
    ): Boolean {
        return keywords.any { text.contains(it) }
    }
}