# 🦯 TalkAI - Voice Assistant for Blind Users

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Kotlin-purple)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)

TalkAI is an Android application designed to assist visually impaired users through voice interaction, object detection, and AI-powered conversation. The app aims to make everyday tasks easier and more accessible for blind and low-vision users.

---

## ✨ Features

- 🎙️ **Voice Assistant** — Hands-free voice interaction powered by AI
- 👁️ **Object Detection** — Identifies objects around the user using the camera
- 🗣️ **Text-to-Speech** — Reads out responses and information aloud
- 🤖 **AI Conversation** — Intelligent responses using Groq AI
- ♿ **Accessibility First** — Designed specifically for blind and visually impaired users

---

## 🛠️ Tech Stack

| Technology | Usage |
|------------|-------|
| Kotlin | Primary programming language |
| Android Studio | Development environment |
| Groq AI API | AI-powered conversation |
| Android Speech Recognition | Voice input |
| Text-to-Speech (TTS) | Audio output |

---

## 📱 Screenshots

> Coming soon...

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- Android device or emulator (API 24+)
- Groq API Key ([Get one here](https://console.groq.com))

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/bsharmabe23-ops/TalkAI.git
```

2. **Open in Android Studio**
   - Open Android Studio
   - Click `File → Open`
   - Select the cloned `TalkAI` folder

3. **Add your API Key**
   - Open `local.properties` file in the project root
   - Add your Groq API key:
```
GROQ_API_KEY=your_api_key_here
```

4. **Run the app**
   - Connect your Android device or start an emulator
   - Click the ▶️ Run button in Android Studio

---

## 🔑 API Key Setup

This app uses the **Groq AI API** for intelligent conversation.

1. Go to [console.groq.com](https://console.groq.com)
2. Sign up / Log in
3. Go to **API Keys** → **Create API Key**
4. Copy the key and paste it in `local.properties` as shown above

> ⚠️ **Never share your API key publicly or commit it to GitHub!**

---

## 📂 Project Structure

```
TalkAI/
├── app/
│   └── src/
│       └── main/
│           └── java/
│               └── assistant/
│                   └── GroqManager.kt
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🤝 Contributing

Contributions are welcome! If you'd like to improve TalkAI:

1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m "Add your feature"`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 👩‍💻 Developer

**Bipasha Sharma**
- GitHub: [@bsharmabe23-ops](https://github.com/bsharmabe23-ops)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 💡 Motivation

> *"Technology should be accessible to everyone."*
>
> TalkAI was built with the belief that AI can make a real difference in the lives of visually impaired people — giving them independence, confidence, and a voice.
