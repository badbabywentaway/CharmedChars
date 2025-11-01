# MyMinecraftPlugin - IntelliJ IDEA Setup Guide

## 🚀 Quick Setup (5 minutes)

### Step 1: Prerequisites
- ✅ Java 17 or higher installed
- ✅ IntelliJ IDEA (Community or Ultimate)
- ✅ Internet connection for Gradle dependencies

### Step 2: Project Setup
1. **Download** the complete project files (use the main download button)
2. **Create** a new directory: MyMinecraftPlugin
3. **Extract/Copy** all files into the project directory
4. **Organize** files according to the directory structure

### Step 3: IntelliJ Import
1. **Open IntelliJ IDEA**
2. **File → Open** (or "Open" from start screen)
3. **Select** your MyMinecraftPlugin directory
4. **Choose** "Import Gradle Project" if prompted
5. **Accept** default Gradle settings
6. **Wait** for Gradle sync to complete (may take a few minutes)

### Step 4: Customization
1. **Replace package names**: 
   - Find/Replace com.yourname.myplugin → com.yourdomain.yourplugin
   - Update in all .kt files and plugin.yml
2. **Update plugin.yml**:
   - Change author, description, website
   - Modify name if desired
3. **Customize config.yml**:
   - Update welcome message
   - Configure custom blocks/textures settings

### Step 5: Build & Test
1. **Open terminal** in IntelliJ (Alt+F12)
2. **Build project**: ./gradlew build
3. **Test with server**: ./gradlew runServer
4. **Development build**: ./gradlew devBuild

## 🎮 Testing Your Plugin

### Using Built-in Test Server
./gradlew runServer

- Downloads Paper server automatically
- Starts server with your plugin loaded
- Available at localhost:25565
- Use "stop" command to shutdown

### Manual Testing
1. Build: ./gradlew build
2. Find JAR: build/libs/MyMinecraftPlugin-1.0.0.jar
3. Copy to your Paper server's plugins/ folder
4. Restart server

## 🔧 Development Workflow

### Gradle Tasks
./gradlew build          # Standard build
./gradlew devBuild       # Development build with info
./gradlew prepareRelease # Clean release build
./gradlew runServer      # Start test Paper server
./gradlew clean          # Clean build files

## 🐛 Troubleshooting

### Common Issues

**"Unresolved reference" errors**:
- Wait for Gradle sync to complete
- Refresh Gradle project (Gradle tool window → Refresh)
- Reimport project

**Build fails**:
- Check Java version (java -version)
- Clean and rebuild (./gradlew clean build)
- Check internet connection for dependencies

**Plugin doesn't load**:
- Verify plugin.yml syntax
- Check server logs for errors
- Ensure Java 17+ on server
- Verify Paper 1.20.4+

## 🎯 Next Steps

### Custom Blocks Available
- **Magic Stone**: Healing block with particle effects
- **Enchanted Log**: Fast-growing magical wood  
- **Crystal Block**: Rare decorative block

### Commands to Try
- /example - Test the example command
- /blocks give magic_stone - Get a magic stone
- /textures download - Get custom textures
- /reload - Hot reload configuration

🎉 **Congratulations!** Your Minecraft plugin development environment is ready!