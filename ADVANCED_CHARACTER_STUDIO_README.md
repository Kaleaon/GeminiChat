# Advanced Character Studio - Complete Documentation

## Overview

The Advanced Character Studio is a comprehensive character customization system that automatically unwraps uploaded meshes into dynamic sliders. It supports any body part, shape, or character type, including animalistic features like werewolf heads, and automatically adapts hair and facial hair to unusual head shapes.

## Features

### 🎭 Automatic Mesh Analysis
- **Multi-Format Support**: GLB, GLTF, FBX, OBJ, VRM
- **Intelligent Detection**: Automatically identifies body parts, hair, facial hair, and animalistic features
- **Blend Shape Extraction**: Detects and exposes all morph targets and blend shapes
- **Bone Structure Analysis**: Analyzes skeleton for proper deformation zones

### 💪 Muscle Definition System
Automatically generates sliders for:
- Biceps, Triceps, Forearms
- Chest, Abs, Back, Shoulders
- Quadriceps, Hamstrings, Calves, Glutes
- Custom muscle groups based on mesh structure

### 🐺 Animalistic Features
Full support for non-human characters:
- **Head Features**: Snout length/width, jaw width, ear size/position
- **Facial Features**: Eye size/spacing, brow ridge, cheekbones
- **Body Features**: Tail length/thickness, wing span, horn length/curve
- **Automatic Detection**: System identifies animal features from mesh structure

### 💇 Hair & Facial Hair System
Advanced hair deformation that adapts to head shape changes:
- **Hair Customization**: Length, volume, curve, flow
- **Facial Hair**: Beard, mustache, sideburns, whiskers
- **Automatic Conforming**: Hair follows head deformations in real-time
- **Attachment System**: Hair stays properly attached to unusual head shapes

### 🎚️ Dynamic Slider Generation
- **Automatic Creation**: Sliders generated from mesh analysis
- **Smart Categorization**: Organized by body region and feature type
- **Real-time Preview**: See changes instantly
- **Preset System**: Save and load character configurations

## System Architecture

### Core Components

#### 1. MeshProcessor (`mesh-processor.js`)
Analyzes uploaded meshes and extracts deformable features:
```javascript
const processor = new MeshProcessor();
const analysis = await processor.analyzeMesh(meshData, 'glb');
// Returns: vertices, morphTargets, bones, hairGroups, facialHairGroups, bodyParts
```

**Key Methods:**
- `analyzeMesh(meshData, format)` - Main analysis entry point
- `detectMorphTargets(mesh)` - Finds blend shapes
- `detectHairGroups(mesh)` - Identifies hair systems
- `detectFacialHairGroups(mesh)` - Finds facial hair
- `identifyBodyParts(mesh)` - Categorizes mesh components
- `generateDeformationZones(analysis)` - Creates slider zones

#### 2. SliderGenerator (`slider-generator.js`)
Creates dynamic UI sliders from mesh analysis:
```javascript
const generator = new SliderGenerator();
const sliders = generator.generateSliders(deformationZones, meshAnalysis);
const html = generator.generateSliderUI(sliders);
```

**Key Methods:**
- `generateSliders(zones, analysis)` - Creates slider configurations
- `generateSliderUI(sliders)` - Generates HTML interface
- `organizeIntoCategories(zones)` - Groups sliders logically
- `exportSliderConfig(sliders)` - Saves slider setup
- `importSliderConfig(json)` - Loads slider setup

**Slider Categories:**
- Muscle Definition
- Animal Head Features
- Hair Customization
- Facial Hair Customization
- Tail/Wing/Horn Features
- Head & Face
- Torso, Arms, Legs

#### 3. MeshDeformer (`mesh-deformer.js`)
Applies deformations based on slider values:
```javascript
const deformer = new MeshDeformer(scene, meshProcessor);
deformer.setActiveMesh(characterMesh);
deformer.applyDeformation(sliderId, value, 'scale', 'xyz', zone);
```

**Deformation Types:**
- `scale` - Resize along specified axes
- `elongation` - Stretch/compress along axis
- `translation` - Move vertices
- `protrusion` - Push outward/inward along normals
- `bend` - Curve deformation
- `conform` - Match to target surface (for hair)

**Key Methods:**
- `applyDeformation(id, value, type, axis, zone)` - Apply single deformation
- `updateHairAttachments()` - Keep hair following head
- `updateFacialHairAttachments()` - Keep facial hair following face
- `resetAllDeformations()` - Restore original mesh
- `exportDeformationState()` - Save current state
- `importDeformationState(json)` - Load saved state

#### 4. FileUploadHandler (`file-upload-handler.js`)
Manages file uploads and processing workflow:
```javascript
const handler = new FileUploadHandler(processor, generator, deformer);
handler.initializeUploadUI();
```

**Features:**
- Drag & drop interface
- File validation (format, size)
- Processing options (auto-rig, detect features)
- Progress tracking
- Results summary

#### 5. AdvancedCharacterStudio (`advanced-character-studio.js`)
Main integration layer:
```javascript
const studio = new AdvancedCharacterStudio();
await studio.initialize(scene);
```

**Global API:**
- `window.uploadCustomMesh(file)` - Upload mesh
- `window.applyDeformation(id, value, type, axis)` - Apply deformation
- `window.resetAllDeformations()` - Reset all
- `window.exportCharacter()` - Export configuration
- `window.importCharacter(data)` - Import configuration
- `window.savePreset(name)` - Save preset
- `window.loadPreset(name)` - Load preset

## Usage Guide

### Basic Usage

1. **Upload a Custom Mesh**
   - Click "📁 Upload Custom Mesh" button
   - Drag & drop or browse for your mesh file
   - Supported formats: GLB, GLTF, FBX, OBJ, VRM
   - Maximum file size: 50MB

2. **Configure Processing Options**
   - ✅ Auto-detect rigging
   - ✅ Detect hair systems
   - ✅ Detect facial hair
   - ✅ Detect animal features
   - ✅ Generate muscle definition zones
   - ✅ Optimize mesh geometry

3. **Process & Generate Sliders**
   - Click "Process Mesh & Generate Sliders"
   - Wait for analysis to complete
   - Review the results summary

4. **Apply to Character**
   - Click "Apply to Character"
   - Dynamic sliders appear on the left side
   - Use sliders to customize your character

### Advanced Features

#### Hair Adaptation to Head Shape
The system automatically makes hair follow head deformations:

```javascript
// Hair automatically conforms to head shape changes
// Adjust head features, and hair follows:
applyDeformation('snoutLength', 1.5, 'elongation', 'z', zone);
// Hair automatically adjusts to new head shape
```

**Hair Sliders:**
- **Length**: Adjust hair length
- **Volume**: Control thickness and fullness
- **Curve**: Modify hair flow and curvature
- **Head Adaptation**: How closely hair follows head shape (0-100%)

#### Facial Hair Matching
Facial hair automatically adapts to face shape changes:

```javascript
// Facial hair follows jaw and face deformations
applyDeformation('jawWidth', 1.4, 'scale', 'x', zone);
// Beard automatically adjusts to new jaw shape
```

**Facial Hair Sliders:**
- **Length**: Adjust facial hair length
- **Density**: Control thickness
- **Face Adaptation**: How closely it follows face contours (0-100%)

#### Animalistic Features
Full support for non-human characters:

**Werewolf Example:**
```javascript
// Create werewolf head
applyDeformation('snoutLength', 1.5, 'elongation', 'z', zone);
applyDeformation('earSize', 1.8, 'scale', 'all', zone);
applyDeformation('jawWidth', 1.4, 'scale', 'x', zone);
applyDeformation('hairVolume', 1.6, 'scale', 'xz', zone);
```

**Available Animal Features:**
- Snout length and width
- Ear size and position
- Jaw width
- Eye size and spacing
- Brow ridge prominence
- Tail length and thickness
- Wing span
- Horn length and curve

#### Muscle Definition
Precise control over muscle groups:

```javascript
// Create muscular character
applyDeformation('biceps', 1.8, 'scale', 'all', zone);
applyDeformation('chest', 1.6, 'scale', 'all', zone);
applyDeformation('shoulders', 1.7, 'scale', 'all', zone);
applyDeformation('abs', 1.5, 'scale', 'all', zone);
```

**Muscle Groups:**
- Upper Body: Biceps, Triceps, Forearms, Shoulders, Chest, Back, Abs
- Lower Body: Quadriceps, Hamstrings, Calves, Glutes

### Preset System

#### Save Preset
```javascript
// Save current character configuration
window.savePreset('My Werewolf Character');
```

#### Load Preset
```javascript
// Load saved configuration
window.loadPreset('My Werewolf Character');
```

#### Export/Import
```javascript
// Export character data
const characterData = window.exportCharacter();
// Save to file or database

// Import character data
window.importCharacter(characterData);
```

## Integration with Existing Character Studio

The Advanced Character Studio seamlessly integrates with the existing Character Studio:

1. **Automatic Initialization**: Loads when Character Studio is ready
2. **Non-Intrusive**: Adds features without modifying core functionality
3. **Compatible**: Works with existing manifests and characters
4. **Extensible**: Easy to add new features and deformation types

## Technical Details

### Deformation Algorithm

The system uses a multi-stage deformation pipeline:

1. **Vertex Selection**: Identify vertices in deformation zone
2. **Influence Calculation**: Calculate per-vertex influence weights
3. **Transformation**: Apply deformation based on type and axis
4. **Blending**: Blend with original position using influence weight
5. **Normal Update**: Recalculate vertex normals
6. **Attachment Update**: Update hair/facial hair attachments

### Hair Attachment System

Hair follows head deformations using an attachment point system:

```javascript
// Hair attachment structure
{
  mesh: hairMesh,
  originalGeometry: originalHairGeometry,
  attachmentBone: headBone,
  conformValue: 0.8 // How closely to follow (0-1)
}
```

**Conforming Algorithm:**
1. Find nearest head vertex for each hair vertex
2. Calculate offset from head surface
3. Update hair vertex to maintain offset from deformed head
4. Blend based on conform value

### Performance Optimization

- **Lazy Evaluation**: Only recalculate affected vertices
- **Geometry Caching**: Store original geometry for reset
- **Batch Updates**: Group deformations for efficiency
- **Progressive Loading**: Load large meshes progressively

## File Format Support

### GLB/GLTF
- Full support for morph targets
- Skeleton and bone structure
- Texture and material preservation
- Animation support

### FBX
- Blend shapes
- Bone hierarchy
- Material properties
- Animation curves

### OBJ
- Basic geometry
- Material files (.mtl)
- Texture coordinates
- Vertex groups

### VRM
- VRM-specific blend shapes
- Humanoid bone mapping
- Expression presets
- Spring bone physics

## API Reference

### MeshProcessor API

```javascript
// Analyze mesh
const analysis = await processor.analyzeMesh(meshData, format);

// Analysis result structure
{
  vertices: Array<{x, y, z, index}>,
  morphTargets: Array<{name, index, influence, category}>,
  bones: Array<{name, position, rotation, scale, type}>,
  blendShapes: Array<{name, attribute, index, data, category}>,
  hairGroups: Array<{name, mesh, vertices, attachmentPoint}>,
  facialHairGroups: Array<{name, mesh, vertices, attachmentPoint, type}>,
  bodyParts: Array<{name, type, mesh, vertices}>,
  deformationZones: Array<{name, type, region, vertices, deformationType, range}>
}
```

### SliderGenerator API

```javascript
// Generate sliders
const sliders = generator.generateSliders(zones, analysis);

// Slider structure
{
  id: string,
  label: string,
  category: string,
  zone: DeformationZone,
  min: number,
  max: number,
  default: number,
  step: number,
  value: number,
  deformationType: string,
  axis: string,
  description: string,
  icon: string
}
```

### MeshDeformer API

```javascript
// Apply deformation
deformer.applyDeformation(sliderId, value, type, axis, zone);

// Deformation types
'scale'       // Resize along axes
'elongation'  // Stretch/compress
'translation' // Move vertices
'protrusion'  // Push along normals
'bend'        // Curve deformation
'conform'     // Match surface

// Axis options
'x', 'y', 'z'           // Single axis
'xy', 'xz', 'yz'        // Two axes
'xyz', 'all'            // All axes
```

## Troubleshooting

### Common Issues

**Issue**: Sliders not appearing after upload
- **Solution**: Check browser console for errors, ensure mesh format is supported

**Issue**: Hair not following head deformations
- **Solution**: Ensure hair is properly detected (check "Detect hair systems" option)

**Issue**: Deformations look incorrect
- **Solution**: Try resetting and reapplying, check vertex influence weights

**Issue**: Large file upload fails
- **Solution**: Optimize mesh before upload, reduce polygon count, compress textures

### Debug Mode

Enable debug logging:
```javascript
window.AdvancedCharacterStudio.debugMode = true;
```

## Future Enhancements

- [ ] Real-time physics simulation for hair
- [ ] Advanced muscle simulation with flex/relax states
- [ ] Procedural texture generation for animal features
- [ ] Multi-mesh support for complex characters
- [ ] Animation retargeting for custom skeletons
- [ ] Cloud storage for presets
- [ ] Collaborative character editing
- [ ] AI-assisted feature detection

## Credits

Developed by NinjaTech AI for the GeminiChat Character Studio.

## License

This system is part of the GeminiChat project and follows the same license terms.