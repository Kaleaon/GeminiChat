# Quick Start Guide - Advanced Character Studio

## 🚀 Getting Started in 5 Minutes

### Step 1: Open Character Studio
Launch the GeminiChat app and navigate to the Character Studio section.

### Step 2: Upload Your Mesh
1. Click the **"📁 Upload Custom Mesh"** button (top right)
2. Drag & drop your character file or click to browse
3. Supported formats: `.glb`, `.gltf`, `.fbx`, `.obj`, `.vrm`

### Step 3: Configure Options
Select what you want the system to detect:
- ✅ **Auto-detect rigging** - Finds bones automatically
- ✅ **Detect hair systems** - Identifies hair for deformation
- ✅ **Detect facial hair** - Finds beards, mustaches, etc.
- ✅ **Detect animal features** - Identifies snouts, tails, horns, etc.
- ✅ **Generate muscle definition zones** - Creates muscle sliders
- ✅ **Optimize mesh geometry** - Improves performance

### Step 4: Process & Apply
1. Click **"Process Mesh & Generate Sliders"**
2. Wait for analysis (usually 5-30 seconds)
3. Review the results summary
4. Click **"Apply to Character"**

### Step 5: Customize!
Use the dynamic sliders on the left to customize your character:
- **Muscle Definition** - Adjust muscle size and definition
- **Animal Features** - Modify snout, ears, tail, etc.
- **Hair** - Control length, volume, and flow
- **Facial Hair** - Adjust beard, mustache, etc.

## 📋 Example Workflows

### Creating a Werewolf Character

1. **Upload** a humanoid base mesh
2. **Enable** "Detect animal features"
3. **Process** the mesh
4. **Adjust sliders:**
   - Snout Length: 1.5
   - Ear Size: 1.8
   - Jaw Width: 1.4
   - Hair Volume: 1.6
5. **Save preset** as "Werewolf"

### Creating a Muscular Hero

1. **Upload** your character mesh
2. **Enable** "Generate muscle definition zones"
3. **Process** the mesh
4. **Adjust sliders:**
   - Biceps: 1.8
   - Chest: 1.6
   - Shoulders: 1.7
   - Abs: 1.5
5. **Save preset** as "Muscular Hero"

### Adapting Hair to Unusual Head Shapes

1. **Upload** character with hair
2. **Enable** "Detect hair systems"
3. **Process** the mesh
4. **Modify head shape** using animal feature sliders
5. **Adjust** "Hair Head Adaptation" slider (0.8-1.0 recommended)
6. Hair automatically follows the new head shape!

## 🎨 Tips & Tricks

### Best Practices
- **Start with default values** and make small adjustments
- **Use presets** to save your favorite configurations
- **Combine features** for unique characters (e.g., muscular werewolf)
- **Adjust hair adaptation** if hair doesn't follow head changes perfectly

### Performance Tips
- **Optimize large meshes** before uploading
- **Reduce polygon count** for better performance
- **Use compressed textures** to reduce file size
- **Close unused sliders** to improve UI responsiveness

### Common Slider Ranges
- **Muscle Definition**: 0.5 (lean) to 2.0 (very muscular)
- **Animal Features**: 0.0 (human) to 2.0 (very animalistic)
- **Hair Length**: 0.1 (very short) to 3.0 (very long)
- **Hair Volume**: 0.5 (thin) to 2.0 (very thick)

## 🔧 Keyboard Shortcuts

- **Ctrl + Z**: Undo last change (coming soon)
- **Ctrl + R**: Reset all sliders
- **Ctrl + S**: Save current preset
- **Ctrl + E**: Export character

## 📱 Mobile Support

The Advanced Character Studio works on mobile devices:
- **Touch gestures** for slider control
- **Pinch to zoom** in 3D view
- **Swipe** to rotate character
- **Tap** to select sliders

## 🆘 Need Help?

### Quick Fixes
- **Sliders not working?** Try refreshing the page
- **Hair not following head?** Increase "Head Adaptation" slider
- **Mesh looks broken?** Click "Reset All" and try again
- **Upload failed?** Check file size (max 50MB) and format

### Getting Support
- Check the full documentation: `ADVANCED_CHARACTER_STUDIO_README.md`
- Report issues on GitHub
- Join our Discord community

## 🎯 What's Next?

After mastering the basics:
1. **Experiment** with different mesh types
2. **Create** a library of presets
3. **Share** your creations with the community
4. **Combine** with other Character Studio features

## 🌟 Pro Tips

### For Artists
- Use reference images when adjusting proportions
- Save multiple variations of the same character
- Export configurations for use in other projects

### For Developers
- Access the API via `window.AdvancedCharacterStudio`
- Create custom deformation types
- Integrate with your own tools

### For Game Designers
- Create character templates for different roles
- Build a preset library for your game
- Use the export feature for asset pipelines

---

**Ready to create amazing characters? Start uploading now! 🚀**