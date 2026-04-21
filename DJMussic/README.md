DJ Mussic - Ứng dụng DJ & Audio Tools
📱 Giới thiệu
DJ Mussic là ứng dụng Android chuyên nghiệp dành cho những người yêu thích âm nhạc, DJ và sản xuất âm thanh. Ứng dụng cung cấp bộ công cụ âm thanh đa năng cho phép người dùng trộn nhạc, tạo beat, cắt nhạc chuông và nhiều tính năng khác.
🎵 Các tính năng chính
Tính năngMô tảDJ MixerMixer 2 deck với crossfader, điều chỉnh volume riêng biệtDrum Pad8 pad trống với khả năng ghi âm, lưu pattern và phát lạiRingtone CutterCắt và trích xuất đoạn nhạc để làm nhạc chuôngAudio MixerMix nhiều track âm thanh với solo/mute từng trackSound MergerGhép nhiều file âm thanh thành một file duy nhấtMusic LibraryQuản lý nhạc, folder và playlist từ thiết bị🛠 Công nghệ sử dụng
• Ngôn ngữ: Kotlin
• Media Player: ExoPlayer, MediaPlayer
• Sound Management: SoundPool
• Coroutines: Xử lý bất đồng bộ
• Material Design: Material Components
• Permissions: Xử lý quyền truy cập file (Android 13+)
📋 Yêu cầu hệ thống
• Minimum SDK: API 21 (Android 5.0 Lollipop)
• Target SDK: API 34 (Android 14)
• IDE: Android Studio Hedgehog | 2023.1.1 trở lên
• Gradle: 8.0+
🚀 Cách cài đặt và chạy dự án
1. Clone dự án
bash
git clone <repository-url>
cd DJMussic
2. Mở bằng Android Studio
• Mở Android Studio
• Chọn File → Open
• Chọn thư mục DJMussic
• Chờ Gradle sync hoàn tất
3. Thêm file âm thanh mẫu (tùy chọn)
Để các tính năng DJ Mixer và Audio Mixer hoạt động đầy đủ, thêm file âm thanh vào app/src/main/res/raw/:
text
raw/
├── demo_track.mp3   # File nhạc mẫu cho DJ Mixer
└── silent.mp3       # File silent cho Audio Mixer (đã có)
Lưu ý: Bạn có thể thay thế demo_track.mp3 bằng file nhạc bất kỳ (đặt tên đúng).
4. Cấu hình dependencies
File app/build.gradle.kts đã được cấu hình với các dependencies cần thiết:
kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
5. Cấu hình quyền trong AndroidManifest.xml
xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
6. Chạy ứng dụng
• Kết nối thiết bị Android hoặc mở emulator
• Nhấn nút Run (▶) trong Android Studio
• Hoặc sử dụng lệnh:
bash
./gradlew installDebug
📂 Cấu trúc dự án
text
app/src/main/
├── java/com/example/djmussic/
│   ├── adapter/           # RecyclerView Adapters
│   │   ├── FeatureAdapter.kt
│   │   └── SongAdapter.kt
│   ├── data/              # Data models & repository
│   │   ├── MusicRepository.kt
│   │   └── Song.kt
│   ├── service/           # Background services
│   │   └── MusicService.kt
│   ├── ui/                # Activities
│   │   ├── AudioMixerActivity.kt
│   │   ├── DjMixerActivity.kt
│   │   ├── DrumPadActivity.kt
│   │   ├── MyLibraryActivity.kt
│   │   ├── RingtoneCutterActivity.kt
│   │   ├── SettingActivity.kt
│   │   └── SoundMergerActivity.kt
│   └── MainActivity.kt    # Main entry point
├── res/
│   ├── layout/            # UI layouts
│   ├── drawable/          # Icons & drawables
│   ├── raw/               # Audio sample files
│   ├── values/            # Colors, strings, themes
│   └── menu/              # Bottom navigation menu
└── AndroidManifest.xml
🎮 Hướng dẫn sử dụng
DJ Mixer
1. Chọn bài hát cho Deck 1 và Deck 2
2. Điều chỉnh volume từng deck
3. Kéo crossfader để chuyển đổi giữa 2 deck
Drum Pad
• Gõ pad: Phát âm thanh trống
• REC: Bắt đầu ghi pattern
• Play: Phát lại pattern đã ghi
• Save: Lưu pattern để dùng sau
Ringtone Cutter
1. Chọn file nhạc
2. Kéo seekbar hoặc dùng Set Start/End để chọn đoạn
3. Nhấn Cut and Save để lưu nhạc chuông
Sound Merger
1. Nhấn Add Audio Files để chọn nhiều file
2. Xem danh sách file đã chọn
3. Nhấn Merge để ghép các file

