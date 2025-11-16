/**
 * Mesh Deformation System
 * Applies deformations based on slider values
 */

class MeshDeformer {
  constructor(scene, meshProcessor) {
    this.scene = scene;
    this.meshProcessor = meshProcessor;
    this.activeMesh = null;
    this.originalGeometry = null;
    this.deformationCache = new Map();
    this.hairAttachments = new Map();
    this.facialHairAttachments = new Map();
  }

  /**
   * Set the active mesh to deform
   */
  setActiveMesh(mesh) {
    this.activeMesh = mesh;
    
    // Store original geometry for reset capability
    if (mesh && mesh.geometry) {
      this.originalGeometry = mesh.geometry.clone();
    }
    
    // Initialize hair and facial hair attachments
    this.initializeAttachments(mesh);
  }

  /**
   * Initialize hair and facial hair attachment systems
   */
  initializeAttachments(mesh) {
    if (!mesh) return;
    
    // Find hair meshes
    mesh.traverse(child => {
      if (child.isMesh) {
        const name = child.name.toLowerCase();
        
        if (name.includes('hair') && !name.includes('facial')) {
          this.hairAttachments.set(child.uuid, {
            mesh: child,
            originalGeometry: child.geometry.clone(),
            attachmentBone: this.findAttachmentBone(child, 'head')
          });
        }
        
        if (name.includes('beard') || name.includes('mustache') || 
            name.includes('facial') || name.includes('whisker')) {
          this.facialHairAttachments.set(child.uuid, {
            mesh: child,
            originalGeometry: child.geometry.clone(),
            attachmentBone: this.findAttachmentBone(child, 'face')
          });
        }
      }
    });
  }

  /**
   * Find the bone to attach hair/facial hair to
   */
  findAttachmentBone(mesh, region) {
    if (!this.activeMesh || !this.activeMesh.skeleton) return null;
    
    const bones = this.activeMesh.skeleton.bones;
    const regionKeywords = {
      'head': ['head', 'skull', 'neck'],
      'face': ['head', 'jaw', 'chin']
    };
    
    const keywords = regionKeywords[region] || [];
    
    for (const bone of bones) {
      const boneName = bone.name.toLowerCase();
      if (keywords.some(keyword => boneName.includes(keyword))) {
        return bone;
      }
    }
    
    return null;
  }

  /**
   * Apply deformation based on slider value
   */
  applyDeformation(sliderId, value, deformationType, axis, zone) {
    if (!this.activeMesh) {
      console.warn('No active mesh to deform');
      return;
    }

    // Cache the deformation
    this.deformationCache.set(sliderId, { value, deformationType, axis, zone });

    // Apply the appropriate deformation type
    switch (deformationType) {
      case 'scale':
        this.applyScaleDeformation(zone, value, axis);
        break;
      case 'elongation':
        this.applyElongationDeformation(zone, value, axis);
        break;
      case 'translation':
        this.applyTranslationDeformation(zone, value, axis);
        break;
      case 'protrusion':
        this.applyProtrusionDeformation(zone, value, axis);
        break;
      case 'bend':
        this.applyBendDeformation(zone, value, axis);
        break;
      case 'conform':
        this.applyConformDeformation(zone, value);
        break;
      default:
        console.warn('Unknown deformation type:', deformationType);
    }

    // Update mesh geometry
    if (this.activeMesh.geometry) {
      this.activeMesh.geometry.attributes.position.needsUpdate = true;
      this.activeMesh.geometry.computeVertexNormals();
    }

    // Update hair attachments if this affects the head
    if (zone && (zone.region === 'head' || zone.region === 'face')) {
      this.updateHairAttachments();
      this.updateFacialHairAttachments();
    }
  }

  /**
   * Apply scale deformation
   */
  applyScaleDeformation(zone, value, axis) {
    if (!zone || !zone.vertices) return;

    const positions = this.activeMesh.geometry.attributes.position;
    const originalPositions = this.originalGeometry.attributes.position;

    zone.vertices.forEach(vertex => {
      const idx = vertex.index;
      if (idx >= positions.count) return;

      const origX = originalPositions.getX(idx);
      const origY = originalPositions.getY(idx);
      const origZ = originalPositions.getZ(idx);

      let newX = origX;
      let newY = origY;
      let newZ = origZ;

      // Apply scale based on axis
      if (axis === 'all' || axis === 'xyz') {
        newX = origX * value;
        newY = origY * value;
        newZ = origZ * value;
      } else if (axis === 'x' || axis.includes('x')) {
        newX = origX * value;
      }
      if (axis === 'y' || axis.includes('y')) {
        newY = origY * value;
      }
      if (axis === 'z' || axis.includes('z')) {
        newZ = origZ * value;
      }
      if (axis === 'xy') {
        newX = origX * value;
        newY = origY * value;
      }
      if (axis === 'xz') {
        newX = origX * value;
        newZ = origZ * value;
      }

      // Apply influence weight if available
      const influence = vertex.influence || 1.0;
      newX = origX + (newX - origX) * influence;
      newY = origY + (newY - origY) * influence;
      newZ = origZ + (newZ - origZ) * influence;

      positions.setXYZ(idx, newX, newY, newZ);
    });
  }

  /**
   * Apply elongation deformation
   */
  applyElongationDeformation(zone, value, axis) {
    if (!zone || !zone.vertices) return;

    const positions = this.activeMesh.geometry.attributes.position;
    const originalPositions = this.originalGeometry.attributes.position;

    // Calculate center point of the zone
    const center = this.calculateZoneCenter(zone.vertices, originalPositions);

    zone.vertices.forEach(vertex => {
      const idx = vertex.index;
      if (idx >= positions.count) return;

      const origX = originalPositions.getX(idx);
      const origY = originalPositions.getY(idx);
      const origZ = originalPositions.getZ(idx);

      // Calculate distance from center along elongation axis
      let distance = 0;
      if (axis === 'x') {
        distance = origX - center.x;
      } else if (axis === 'y') {
        distance = origY - center.y;
      } else if (axis === 'z') {
        distance = origZ - center.z;
      }

      // Apply elongation
      let newX = origX;
      let newY = origY;
      let newZ = origZ;

      if (axis === 'x') {
        newX = center.x + distance * value;
      } else if (axis === 'y') {
        newY = center.y + distance * value;
      } else if (axis === 'z') {
        newZ = center.z + distance * value;
      }

      // Apply influence weight
      const influence = vertex.influence || 1.0;
      newX = origX + (newX - origX) * influence;
      newY = origY + (newY - origY) * influence;
      newZ = origZ + (newZ - origZ) * influence;

      positions.setXYZ(idx, newX, newY, newZ);
    });
  }

  /**
   * Apply translation deformation
   */
  applyTranslationDeformation(zone, value, axis) {
    if (!zone || !zone.vertices) return;

    const positions = this.activeMesh.geometry.attributes.position;
    const originalPositions = this.originalGeometry.attributes.position;

    zone.vertices.forEach(vertex => {
      const idx = vertex.index;
      if (idx >= positions.count) return;

      const origX = originalPositions.getX(idx);
      const origY = originalPositions.getY(idx);
      const origZ = originalPositions.getZ(idx);

      let newX = origX;
      let newY = origY;
      let newZ = origZ;

      // Apply translation based on axis
      if (axis === 'x') {
        newX = origX + value;
      } else if (axis === 'y') {
        newY = origY + value;
      } else if (axis === 'z') {
        newZ = origZ + value;
      }

      // Apply influence weight
      const influence = vertex.influence || 1.0;
      newX = origX + (newX - origX) * influence;
      newY = origY + (newY - origY) * influence;
      newZ = origZ + (newZ - origZ) * influence;

      positions.setXYZ(idx, newX, newY, newZ);
    });
  }

  /**
   * Apply protrusion deformation (push vertices outward/inward)
   */
  applyProtrusionDeformation(zone, value, axis) {
    if (!zone || !zone.vertices) return;

    const positions = this.activeMesh.geometry.attributes.position;
    const originalPositions = this.originalGeometry.attributes.position;
    const normals = this.activeMesh.geometry.attributes.normal;

    zone.vertices.forEach(vertex => {
      const idx = vertex.index;
      if (idx >= positions.count) return;

      const origX = originalPositions.getX(idx);
      const origY = originalPositions.getY(idx);
      const origZ = originalPositions.getZ(idx);

      // Get vertex normal
      const normalX = normals ? normals.getX(idx) : 0;
      const normalY = normals ? normals.getY(idx) : 0;
      const normalZ = normals ? normals.getZ(idx) : 0;

      // Apply protrusion along normal
      const protrusionAmount = (value - 1.0) * 0.1; // Scale factor
      let newX = origX + normalX * protrusionAmount;
      let newY = origY + normalY * protrusionAmount;
      let newZ = origZ + normalZ * protrusionAmount;

      // Apply influence weight
      const influence = vertex.influence || 1.0;
      newX = origX + (newX - origX) * influence;
      newY = origY + (newY - origY) * influence;
      newZ = origZ + (newZ - origZ) * influence;

      positions.setXYZ(idx, newX, newY, newZ);
    });
  }

  /**
   * Apply bend deformation
   */
  applyBendDeformation(zone, value, axis) {
    if (!zone || !zone.vertices) return;

    const positions = this.activeMesh.geometry.attributes.position;
    const originalPositions = this.originalGeometry.attributes.position;

    // Calculate bounding box of zone
    const bounds = this.calculateZoneBounds(zone.vertices, originalPositions);
    const center = {
      x: (bounds.min.x + bounds.max.x) / 2,
      y: (bounds.min.y + bounds.max.y) / 2,
      z: (bounds.min.z + bounds.max.z) / 2
    };

    zone.vertices.forEach(vertex => {
      const idx = vertex.index;
      if (idx >= positions.count) return;

      const origX = originalPositions.getX(idx);
      const origY = originalPositions.getY(idx);
      const origZ = originalPositions.getZ(idx);

      // Calculate bend based on distance from center
      let newX = origX;
      let newY = origY;
      let newZ = origZ;

      if (axis === 'x') {
        const distY = origY - center.y;
        const bendAmount = value * 0.5;
        newX = origX + distY * bendAmount;
      } else if (axis === 'y') {
        const distX = origX - center.x;
        const bendAmount = value * 0.5;
        newY = origY + distX * bendAmount;
      } else if (axis === 'z') {
        const distY = origY - center.y;
        const bendAmount = value * 0.5;
        newZ = origZ + distY * bendAmount;
      }

      // Apply influence weight
      const influence = vertex.influence || 1.0;
      newX = origX + (newX - origX) * influence;
      newY = origY + (newY - origY) * influence;
      newZ = origZ + (newZ - origZ) * influence;

      positions.setXYZ(idx, newX, newY, newZ);
    });
  }

  /**
   * Apply conform deformation (make hair/facial hair follow head shape)
   */
  applyConformDeformation(zone, value) {
    if (!zone || !zone.vertices) return;

    // This is specifically for hair/facial hair conforming to head shape
    const targetRegion = zone.targetRegion || 'head';
    const attachmentPoint = zone.attachmentPoint;

    if (!attachmentPoint) return;

    // Get the current head shape
    const headVertices = this.getRegionVertices(targetRegion);
    if (headVertices.length === 0) return;

    const positions = this.activeMesh.geometry.attributes.position;
    const originalPositions = this.originalGeometry.attributes.position;

    zone.vertices.forEach(vertex => {
      const idx = vertex.index;
      if (idx >= positions.count) return;

      const origX = originalPositions.getX(idx);
      const origY = originalPositions.getY(idx);
      const origZ = originalPositions.getZ(idx);

      // Find nearest head vertex
      const nearestHeadVertex = this.findNearestVertex(
        { x: origX, y: origY, z: origZ },
        headVertices
      );

      if (nearestHeadVertex) {
        // Calculate offset from head surface
        const offsetX = origX - nearestHeadVertex.x;
        const offsetY = origY - nearestHeadVertex.y;
        const offsetZ = origZ - nearestHeadVertex.z;

        // Get current head vertex position
        const currentHeadPos = positions.getXYZ(nearestHeadVertex.index);

        // Calculate new position maintaining offset
        let newX = currentHeadPos.x + offsetX;
        let newY = currentHeadPos.y + offsetY;
        let newZ = currentHeadPos.z + offsetZ;

        // Blend with original position based on conform value
        newX = origX + (newX - origX) * value;
        newY = origY + (newY - origY) * value;
        newZ = origZ + (newZ - origZ) * value;

        // Apply influence weight
        const influence = vertex.influence || 1.0;
        newX = origX + (newX - origX) * influence;
        newY = origY + (newY - origY) * influence;
        newZ = origZ + (newZ - origZ) * influence;

        positions.setXYZ(idx, newX, newY, newZ);
      }
    });
  }

  /**
   * Update hair attachments to follow head deformations
   */
  updateHairAttachments() {
    this.hairAttachments.forEach((attachment, uuid) => {
      const hairMesh = attachment.mesh;
      const attachmentBone = attachment.attachmentBone;

      if (!hairMesh || !attachmentBone) return;

      // Update hair position and rotation to follow bone
      hairMesh.position.copy(attachmentBone.position);
      hairMesh.quaternion.copy(attachmentBone.quaternion);

      // Apply any cached hair deformations
      this.deformationCache.forEach((deformation, sliderId) => {
        if (deformation.zone && deformation.zone.type === 'hairDeformation') {
          this.applyDeformation(
            sliderId,
            deformation.value,
            deformation.deformationType,
            deformation.axis,
            deformation.zone
          );
        }
      });
    });
  }

  /**
   * Update facial hair attachments to follow face deformations
   */
  updateFacialHairAttachments() {
    this.facialHairAttachments.forEach((attachment, uuid) => {
      const facialHairMesh = attachment.mesh;
      const attachmentBone = attachment.attachmentBone;

      if (!facialHairMesh || !attachmentBone) return;

      // Update facial hair position and rotation to follow bone
      facialHairMesh.position.copy(attachmentBone.position);
      facialHairMesh.quaternion.copy(attachmentBone.quaternion);

      // Apply any cached facial hair deformations
      this.deformationCache.forEach((deformation, sliderId) => {
        if (deformation.zone && deformation.zone.type === 'facialHairDeformation') {
          this.applyDeformation(
            sliderId,
            deformation.value,
            deformation.deformationType,
            deformation.axis,
            deformation.zone
          );
        }
      });
    });
  }

  /**
   * Helper: Calculate center of zone vertices
   */
  calculateZoneCenter(vertices, positions) {
    let sumX = 0, sumY = 0, sumZ = 0;
    let count = 0;

    vertices.forEach(vertex => {
      if (vertex.index < positions.count) {
        sumX += positions.getX(vertex.index);
        sumY += positions.getY(vertex.index);
        sumZ += positions.getZ(vertex.index);
        count++;
      }
    });

    return {
      x: count > 0 ? sumX / count : 0,
      y: count > 0 ? sumY / count : 0,
      z: count > 0 ? sumZ / count : 0
    };
  }

  /**
   * Helper: Calculate bounding box of zone
   */
  calculateZoneBounds(vertices, positions) {
    const bounds = {
      min: { x: Infinity, y: Infinity, z: Infinity },
      max: { x: -Infinity, y: -Infinity, z: -Infinity }
    };

    vertices.forEach(vertex => {
      if (vertex.index < positions.count) {
        const x = positions.getX(vertex.index);
        const y = positions.getY(vertex.index);
        const z = positions.getZ(vertex.index);

        bounds.min.x = Math.min(bounds.min.x, x);
        bounds.min.y = Math.min(bounds.min.y, y);
        bounds.min.z = Math.min(bounds.min.z, z);

        bounds.max.x = Math.max(bounds.max.x, x);
        bounds.max.y = Math.max(bounds.max.y, y);
        bounds.max.z = Math.max(bounds.max.z, z);
      }
    });

    return bounds;
  }

  /**
   * Helper: Get vertices in a specific region
   */
  getRegionVertices(regionName) {
    // This would need to be implemented based on the mesh structure
    // For now, return empty array
    return [];
  }

  /**
   * Helper: Find nearest vertex to a point
   */
  findNearestVertex(point, vertices) {
    let nearest = null;
    let minDistance = Infinity;

    vertices.forEach(vertex => {
      const dx = vertex.x - point.x;
      const dy = vertex.y - point.y;
      const dz = vertex.z - point.z;
      const distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

      if (distance < minDistance) {
        minDistance = distance;
        nearest = vertex;
      }
    });

    return nearest;
  }

  /**
   * Reset all deformations
   */
  resetAllDeformations() {
    if (!this.activeMesh || !this.originalGeometry) return;

    // Restore original geometry
    this.activeMesh.geometry.copy(this.originalGeometry);
    this.activeMesh.geometry.attributes.position.needsUpdate = true;
    this.activeMesh.geometry.computeVertexNormals();

    // Clear deformation cache
    this.deformationCache.clear();

    // Reset hair attachments
    this.hairAttachments.forEach(attachment => {
      if (attachment.mesh && attachment.originalGeometry) {
        attachment.mesh.geometry.copy(attachment.originalGeometry);
        attachment.mesh.geometry.attributes.position.needsUpdate = true;
      }
    });

    // Reset facial hair attachments
    this.facialHairAttachments.forEach(attachment => {
      if (attachment.mesh && attachment.originalGeometry) {
        attachment.mesh.geometry.copy(attachment.originalGeometry);
        attachment.mesh.geometry.attributes.position.needsUpdate = true;
      }
    });
  }

  /**
   * Export current deformation state
   */
  exportDeformationState() {
    const state = {
      deformations: []
    };

    this.deformationCache.forEach((deformation, sliderId) => {
      state.deformations.push({
        sliderId,
        value: deformation.value,
        deformationType: deformation.deformationType,
        axis: deformation.axis
      });
    });

    return JSON.stringify(state, null, 2);
  }

  /**
   * Import deformation state
   */
  importDeformationState(stateJSON) {
    try {
      const state = JSON.parse(stateJSON);
      
      // Clear current deformations
      this.resetAllDeformations();

      // Apply imported deformations
      state.deformations.forEach(deformation => {
        this.applyDeformation(
          deformation.sliderId,
          deformation.value,
          deformation.deformationType,
          deformation.axis,
          null // Zone will be looked up from slider
        );
      });
    } catch (error) {
      console.error('Failed to import deformation state:', error);
    }
  }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = MeshDeformer;
}