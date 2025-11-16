/**
 * Advanced Mesh Processing System
 * Automatically unwraps meshes and generates dynamic sliders
 */

class MeshProcessor {
  constructor() {
    this.morphTargets = new Map();
    this.blendShapes = new Map();
    this.boneStructure = null;
    this.hairSystem = null;
    this.facialHairSystem = null;
  }

  /**
   * Analyze uploaded mesh and extract deformable features
   */
  async analyzeMesh(meshData, format = 'glb') {
    console.log('Analyzing mesh:', format);
    
    const analysis = {
      vertices: [],
      morphTargets: [],
      bones: [],
      blendShapes: [],
      hairGroups: [],
      facialHairGroups: [],
      bodyParts: []
    };

    try {
      // Parse mesh based on format
      const parsedMesh = await this.parseMeshFormat(meshData, format);
      
      // Extract vertex data
      analysis.vertices = this.extractVertices(parsedMesh);
      
      // Detect morph targets/blend shapes
      analysis.morphTargets = this.detectMorphTargets(parsedMesh);
      analysis.blendShapes = this.detectBlendShapes(parsedMesh);
      
      // Analyze bone structure
      analysis.bones = this.analyzeBoneStructure(parsedMesh);
      
      // Detect hair systems
      analysis.hairGroups = this.detectHairGroups(parsedMesh);
      analysis.facialHairGroups = this.detectFacialHairGroups(parsedMesh);
      
      // Identify body parts
      analysis.bodyParts = this.identifyBodyParts(parsedMesh);
      
      // Generate deformation zones
      analysis.deformationZones = this.generateDeformationZones(analysis);
      
      return analysis;
    } catch (error) {
      console.error('Mesh analysis failed:', error);
      throw error;
    }
  }

  /**
   * Parse mesh data based on format
   */
  async parseMeshFormat(meshData, format) {
    switch (format.toLowerCase()) {
      case 'glb':
      case 'gltf':
        return await this.parseGLTF(meshData);
      case 'fbx':
        return await this.parseFBX(meshData);
      case 'obj':
        return await this.parseOBJ(meshData);
      case 'vrm':
        return await this.parseVRM(meshData);
      default:
        throw new Error(`Unsupported format: ${format}`);
    }
  }

  /**
   * Extract vertices from parsed mesh
   */
  extractVertices(parsedMesh) {
    const vertices = [];
    
    if (parsedMesh.geometry && parsedMesh.geometry.attributes) {
      const positions = parsedMesh.geometry.attributes.position;
      if (positions) {
        for (let i = 0; i < positions.count; i++) {
          vertices.push({
            x: positions.getX(i),
            y: positions.getY(i),
            z: positions.getZ(i),
            index: i
          });
        }
      }
    }
    
    return vertices;
  }

  /**
   * Detect morph targets in mesh
   */
  detectMorphTargets(parsedMesh) {
    const morphTargets = [];
    
    if (parsedMesh.morphTargetInfluences && parsedMesh.morphTargetDictionary) {
      Object.keys(parsedMesh.morphTargetDictionary).forEach(name => {
        const index = parsedMesh.morphTargetDictionary[name];
        morphTargets.push({
          name: name,
          index: index,
          influence: parsedMesh.morphTargetInfluences[index] || 0,
          category: this.categorizeMorphTarget(name)
        });
      });
    }
    
    return morphTargets;
  }

  /**
   * Detect blend shapes
   */
  detectBlendShapes(parsedMesh) {
    const blendShapes = [];
    
    // Check for blend shape data
    if (parsedMesh.geometry && parsedMesh.geometry.morphAttributes) {
      const morphAttributes = parsedMesh.geometry.morphAttributes;
      
      Object.keys(morphAttributes).forEach(attributeName => {
        const morphAttribute = morphAttributes[attributeName];
        morphAttribute.forEach((morph, index) => {
          blendShapes.push({
            name: `${attributeName}_${index}`,
            attribute: attributeName,
            index: index,
            data: morph,
            category: this.categorizeBlendShape(attributeName)
          });
        });
      });
    }
    
    return blendShapes;
  }

  /**
   * Analyze bone structure for deformation
   */
  analyzeBoneStructure(parsedMesh) {
    const bones = [];
    
    if (parsedMesh.skeleton && parsedMesh.skeleton.bones) {
      parsedMesh.skeleton.bones.forEach(bone => {
        bones.push({
          name: bone.name,
          position: bone.position.clone(),
          rotation: bone.rotation.clone(),
          scale: bone.scale.clone(),
          parent: bone.parent ? bone.parent.name : null,
          children: bone.children.map(child => child.name),
          type: this.classifyBoneType(bone.name)
        });
      });
    }
    
    return bones;
  }

  /**
   * Detect hair groups in mesh
   */
  detectHairGroups(parsedMesh) {
    const hairGroups = [];
    
    // Look for hair-related mesh groups
    if (parsedMesh.children) {
      parsedMesh.children.forEach(child => {
        const name = child.name.toLowerCase();
        if (name.includes('hair') || name.includes('strand') || 
            name.includes('follicle') || name.includes('scalp')) {
          hairGroups.push({
            name: child.name,
            mesh: child,
            vertices: this.extractVertices(child),
            deformable: true,
            attachmentPoint: this.findAttachmentPoint(child, 'head')
          });
        }
      });
    }
    
    return hairGroups;
  }

  /**
   * Detect facial hair groups
   */
  detectFacialHairGroups(parsedMesh) {
    const facialHairGroups = [];
    
    if (parsedMesh.children) {
      parsedMesh.children.forEach(child => {
        const name = child.name.toLowerCase();
        if (name.includes('beard') || name.includes('mustache') || 
            name.includes('facial') || name.includes('whisker')) {
          facialHairGroups.push({
            name: child.name,
            mesh: child,
            vertices: this.extractVertices(child),
            deformable: true,
            attachmentPoint: this.findAttachmentPoint(child, 'face'),
            type: this.classifyFacialHairType(name)
          });
        }
      });
    }
    
    return facialHairGroups;
  }

  /**
   * Identify body parts from mesh
   */
  identifyBodyParts(parsedMesh) {
    const bodyParts = [];
    const bodyPartKeywords = {
      head: ['head', 'skull', 'cranium'],
      torso: ['torso', 'chest', 'body', 'spine'],
      arms: ['arm', 'shoulder', 'elbow', 'wrist', 'hand'],
      legs: ['leg', 'thigh', 'knee', 'ankle', 'foot'],
      tail: ['tail'],
      wings: ['wing'],
      horns: ['horn', 'antler'],
      ears: ['ear']
    };
    
    if (parsedMesh.children) {
      parsedMesh.children.forEach(child => {
        const name = child.name.toLowerCase();
        
        for (const [partType, keywords] of Object.entries(bodyPartKeywords)) {
          if (keywords.some(keyword => name.includes(keyword))) {
            bodyParts.push({
              name: child.name,
              type: partType,
              mesh: child,
              vertices: this.extractVertices(child),
              deformable: true
            });
            break;
          }
        }
      });
    }
    
    return bodyParts;
  }

  /**
   * Generate deformation zones for slider creation
   */
  generateDeformationZones(analysis) {
    const zones = [];
    
    // Create zones for muscle definition
    zones.push(...this.createMuscleZones(analysis));
    
    // Create zones for animalistic features
    zones.push(...this.createAnimalFeatureZones(analysis));
    
    // Create zones for hair deformation
    zones.push(...this.createHairDeformationZones(analysis));
    
    // Create zones for facial features
    zones.push(...this.createFacialFeatureZones(analysis));
    
    return zones;
  }

  /**
   * Create muscle definition zones
   */
  createMuscleZones(analysis) {
    const muscleZones = [];
    const muscleGroups = [
      { name: 'biceps', bones: ['upperarm'], region: 'arms' },
      { name: 'triceps', bones: ['upperarm'], region: 'arms' },
      { name: 'forearm', bones: ['lowerarm'], region: 'arms' },
      { name: 'chest', bones: ['spine', 'chest'], region: 'torso' },
      { name: 'abs', bones: ['spine'], region: 'torso' },
      { name: 'shoulders', bones: ['shoulder', 'clavicle'], region: 'torso' },
      { name: 'back', bones: ['spine'], region: 'torso' },
      { name: 'quadriceps', bones: ['thigh'], region: 'legs' },
      { name: 'hamstrings', bones: ['thigh'], region: 'legs' },
      { name: 'calves', bones: ['calf', 'shin'], region: 'legs' },
      { name: 'glutes', bones: ['hip'], region: 'legs' }
    ];
    
    muscleGroups.forEach(muscle => {
      const affectedVertices = this.findVerticesNearBones(
        analysis.vertices, 
        analysis.bones, 
        muscle.bones
      );
      
      if (affectedVertices.length > 0) {
        muscleZones.push({
          name: muscle.name,
          type: 'muscle',
          region: muscle.region,
          vertices: affectedVertices,
          bones: muscle.bones,
          deformationType: 'scale',
          range: { min: 0.5, max: 2.0, default: 1.0 }
        });
      }
    });
    
    return muscleZones;
  }

  /**
   * Create zones for animalistic features
   */
  createAnimalFeatureZones(analysis) {
    const animalZones = [];
    
    // Werewolf/animal head features
    const headFeatures = [
      { name: 'snoutLength', type: 'elongation', axis: 'z' },
      { name: 'snoutWidth', type: 'scale', axis: 'x' },
      { name: 'jawWidth', type: 'scale', axis: 'x' },
      { name: 'earSize', type: 'scale', axis: 'all' },
      { name: 'earPosition', type: 'translation', axis: 'y' },
      { name: 'eyeSize', type: 'scale', axis: 'all' },
      { name: 'eyeSpacing', type: 'translation', axis: 'x' },
      { name: 'browRidge', type: 'protrusion', axis: 'z' },
      { name: 'cheekbones', type: 'protrusion', axis: 'x' },
      { name: 'chinProminence', type: 'protrusion', axis: 'z' }
    ];
    
    headFeatures.forEach(feature => {
      const headVertices = this.findVerticesInRegion(
        analysis.vertices,
        analysis.bones,
        'head'
      );
      
      if (headVertices.length > 0) {
        animalZones.push({
          name: feature.name,
          type: 'animalFeature',
          region: 'head',
          vertices: headVertices,
          deformationType: feature.type,
          axis: feature.axis,
          range: { min: 0.0, max: 2.0, default: 1.0 }
        });
      }
    });
    
    // Tail features
    if (analysis.bodyParts.some(part => part.type === 'tail')) {
      animalZones.push({
        name: 'tailLength',
        type: 'animalFeature',
        region: 'tail',
        deformationType: 'elongation',
        axis: 'z',
        range: { min: 0.0, max: 3.0, default: 1.0 }
      });
      
      animalZones.push({
        name: 'tailThickness',
        type: 'animalFeature',
        region: 'tail',
        deformationType: 'scale',
        axis: 'xy',
        range: { min: 0.3, max: 2.0, default: 1.0 }
      });
    }
    
    // Wing features
    if (analysis.bodyParts.some(part => part.type === 'wings')) {
      animalZones.push({
        name: 'wingSpan',
        type: 'animalFeature',
        region: 'wings',
        deformationType: 'scale',
        axis: 'x',
        range: { min: 0.5, max: 2.5, default: 1.0 }
      });
    }
    
    // Horn features
    if (analysis.bodyParts.some(part => part.type === 'horns')) {
      animalZones.push({
        name: 'hornLength',
        type: 'animalFeature',
        region: 'horns',
        deformationType: 'elongation',
        axis: 'y',
        range: { min: 0.0, max: 3.0, default: 1.0 }
      });
      
      animalZones.push({
        name: 'hornCurve',
        type: 'animalFeature',
        region: 'horns',
        deformationType: 'bend',
        axis: 'z',
        range: { min: -1.0, max: 1.0, default: 0.0 }
      });
    }
    
    return animalZones;
  }

  /**
   * Create hair deformation zones
   */
  createHairDeformationZones(analysis) {
    const hairZones = [];
    
    analysis.hairGroups.forEach(hairGroup => {
      // Hair length
      hairZones.push({
        name: `${hairGroup.name}_length`,
        type: 'hairDeformation',
        region: 'hair',
        hairGroup: hairGroup.name,
        vertices: hairGroup.vertices,
        deformationType: 'elongation',
        axis: 'y',
        range: { min: 0.1, max: 3.0, default: 1.0 },
        attachmentPoint: hairGroup.attachmentPoint
      });
      
      // Hair volume
      hairZones.push({
        name: `${hairGroup.name}_volume`,
        type: 'hairDeformation',
        region: 'hair',
        hairGroup: hairGroup.name,
        vertices: hairGroup.vertices,
        deformationType: 'scale',
        axis: 'xz',
        range: { min: 0.5, max: 2.0, default: 1.0 },
        attachmentPoint: hairGroup.attachmentPoint
      });
      
      // Hair curve/flow
      hairZones.push({
        name: `${hairGroup.name}_curve`,
        type: 'hairDeformation',
        region: 'hair',
        hairGroup: hairGroup.name,
        vertices: hairGroup.vertices,
        deformationType: 'bend',
        axis: 'z',
        range: { min: -1.0, max: 1.0, default: 0.0 },
        attachmentPoint: hairGroup.attachmentPoint
      });
      
      // Hair adaptation to head shape
      hairZones.push({
        name: `${hairGroup.name}_headAdaptation`,
        type: 'hairDeformation',
        region: 'hair',
        hairGroup: hairGroup.name,
        vertices: hairGroup.vertices,
        deformationType: 'conform',
        targetRegion: 'head',
        range: { min: 0.0, max: 1.0, default: 0.8 },
        attachmentPoint: hairGroup.attachmentPoint
      });
    });
    
    return hairZones;
  }

  /**
   * Create facial feature zones
   */
  createFacialFeatureZones(analysis) {
    const facialZones = [];
    
    analysis.facialHairGroups.forEach(facialHair => {
      // Facial hair length
      facialZones.push({
        name: `${facialHair.name}_length`,
        type: 'facialHairDeformation',
        region: 'face',
        facialHairGroup: facialHair.name,
        vertices: facialHair.vertices,
        deformationType: 'elongation',
        axis: 'z',
        range: { min: 0.0, max: 2.0, default: 1.0 },
        attachmentPoint: facialHair.attachmentPoint
      });
      
      // Facial hair density
      facialZones.push({
        name: `${facialHair.name}_density`,
        type: 'facialHairDeformation',
        region: 'face',
        facialHairGroup: facialHair.name,
        vertices: facialHair.vertices,
        deformationType: 'scale',
        axis: 'xy',
        range: { min: 0.3, max: 1.5, default: 1.0 },
        attachmentPoint: facialHair.attachmentPoint
      });
      
      // Facial hair adaptation to face shape
      facialZones.push({
        name: `${facialHair.name}_faceAdaptation`,
        type: 'facialHairDeformation',
        region: 'face',
        facialHairGroup: facialHair.name,
        vertices: facialHair.vertices,
        deformationType: 'conform',
        targetRegion: 'face',
        range: { min: 0.0, max: 1.0, default: 0.9 },
        attachmentPoint: facialHair.attachmentPoint
      });
    });
    
    return facialZones;
  }

  /**
   * Helper: Find vertices near specific bones
   */
  findVerticesNearBones(vertices, bones, boneNames) {
    const affectedVertices = [];
    const targetBones = bones.filter(bone => 
      boneNames.some(name => bone.name.toLowerCase().includes(name.toLowerCase()))
    );
    
    if (targetBones.length === 0) return affectedVertices;
    
    // Find vertices within influence radius of bones
    vertices.forEach(vertex => {
      targetBones.forEach(bone => {
        const distance = this.calculateDistance(vertex, bone.position);
        if (distance < 0.5) { // Influence radius
          affectedVertices.push({
            ...vertex,
            bone: bone.name,
            influence: 1.0 - (distance / 0.5)
          });
        }
      });
    });
    
    return affectedVertices;
  }

  /**
   * Helper: Find vertices in a specific region
   */
  findVerticesInRegion(vertices, bones, regionName) {
    const regionBones = bones.filter(bone => 
      bone.name.toLowerCase().includes(regionName.toLowerCase())
    );
    
    return this.findVerticesNearBones(vertices, bones, 
      regionBones.map(b => b.name)
    );
  }

  /**
   * Helper: Calculate distance between two points
   */
  calculateDistance(point1, point2) {
    const dx = point1.x - point2.x;
    const dy = point1.y - point2.y;
    const dz = point1.z - point2.z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  /**
   * Helper: Find attachment point for hair/facial hair
   */
  findAttachmentPoint(mesh, targetRegion) {
    // Find the closest bone or vertex group to attach to
    return {
      region: targetRegion,
      position: mesh.position.clone(),
      normal: { x: 0, y: 1, z: 0 }
    };
  }

  /**
   * Helper: Categorize morph target
   */
  categorizeMorphTarget(name) {
    const categories = {
      face: ['eye', 'mouth', 'nose', 'brow', 'cheek', 'jaw'],
      body: ['chest', 'waist', 'hip', 'shoulder'],
      muscle: ['bicep', 'tricep', 'abs', 'pec', 'quad', 'calf'],
      animal: ['snout', 'ear', 'tail', 'horn', 'wing', 'claw']
    };
    
    const lowerName = name.toLowerCase();
    for (const [category, keywords] of Object.entries(categories)) {
      if (keywords.some(keyword => lowerName.includes(keyword))) {
        return category;
      }
    }
    
    return 'other';
  }

  /**
   * Helper: Categorize blend shape
   */
  categorizeBlendShape(attributeName) {
    return this.categorizeMorphTarget(attributeName);
  }

  /**
   * Helper: Classify bone type
   */
  classifyBoneType(boneName) {
    const types = {
      head: ['head', 'skull', 'neck'],
      spine: ['spine', 'chest', 'back'],
      arm: ['shoulder', 'arm', 'elbow', 'wrist', 'hand', 'finger'],
      leg: ['hip', 'thigh', 'knee', 'shin', 'ankle', 'foot', 'toe'],
      tail: ['tail'],
      wing: ['wing'],
      other: []
    };
    
    const lowerName = boneName.toLowerCase();
    for (const [type, keywords] of Object.entries(types)) {
      if (keywords.some(keyword => lowerName.includes(keyword))) {
        return type;
      }
    }
    
    return 'other';
  }

  /**
   * Helper: Classify facial hair type
   */
  classifyFacialHairType(name) {
    if (name.includes('beard')) return 'beard';
    if (name.includes('mustache') || name.includes('moustache')) return 'mustache';
    if (name.includes('goatee')) return 'goatee';
    if (name.includes('sideburn')) return 'sideburns';
    if (name.includes('whisker')) return 'whiskers';
    return 'other';
  }

  /**
   * Parse GLTF/GLB format
   */
  async parseGLTF(meshData) {
    // This would use THREE.GLTFLoader or similar
    // For now, return a placeholder structure
    return {
      geometry: null,
      morphTargetInfluences: [],
      morphTargetDictionary: {},
      skeleton: { bones: [] },
      children: []
    };
  }

  /**
   * Parse FBX format
   */
  async parseFBX(meshData) {
    // This would use THREE.FBXLoader or similar
    return this.parseGLTF(meshData);
  }

  /**
   * Parse OBJ format
   */
  async parseOBJ(meshData) {
    // This would use THREE.OBJLoader or similar
    return this.parseGLTF(meshData);
  }

  /**
   * Parse VRM format
   */
  async parseVRM(meshData) {
    // This would use VRM loader
    return this.parseGLTF(meshData);
  }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = MeshProcessor;
}