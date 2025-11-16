/**
 * Dynamic Slider Generator
 * Automatically creates UI sliders from mesh analysis
 */

class SliderGenerator {
  constructor() {
    this.sliders = [];
    this.categories = new Map();
    this.activeSliders = new Map();
  }

  /**
   * Generate sliders from deformation zones
   */
  generateSliders(deformationZones, meshAnalysis) {
    console.log('Generating sliders from', deformationZones.length, 'zones');
    
    const sliders = [];
    const categories = this.organizeIntoCategories(deformationZones);
    
    categories.forEach((zones, categoryName) => {
      zones.forEach(zone => {
        const slider = this.createSliderFromZone(zone, categoryName);
        if (slider) {
          sliders.push(slider);
        }
      });
    });
    
    this.sliders = sliders;
    return sliders;
  }

  /**
   * Organize deformation zones into categories
   */
  organizeIntoCategories(zones) {
    const categories = new Map();
    
    zones.forEach(zone => {
      let category = this.determineCategoryForZone(zone);
      
      if (!categories.has(category)) {
        categories.set(category, []);
      }
      
      categories.get(category).push(zone);
    });
    
    return categories;
  }

  /**
   * Determine category for a deformation zone
   */
  determineCategoryForZone(zone) {
    // Primary categorization by type
    if (zone.type === 'muscle') {
      return 'Muscle Definition';
    } else if (zone.type === 'animalFeature') {
      if (zone.region === 'head') {
        return 'Animal Head Features';
      } else if (zone.region === 'tail') {
        return 'Tail Features';
      } else if (zone.region === 'wings') {
        return 'Wing Features';
      } else if (zone.region === 'horns') {
        return 'Horn Features';
      }
      return 'Animal Features';
    } else if (zone.type === 'hairDeformation') {
      return 'Hair Customization';
    } else if (zone.type === 'facialHairDeformation') {
      return 'Facial Hair Customization';
    }
    
    // Secondary categorization by region
    if (zone.region) {
      const regionCategories = {
        'head': 'Head & Face',
        'torso': 'Torso',
        'arms': 'Arms',
        'legs': 'Legs',
        'hands': 'Hands',
        'feet': 'Feet'
      };
      
      return regionCategories[zone.region] || 'Body';
    }
    
    return 'Other';
  }

  /**
   * Create a slider configuration from a deformation zone
   */
  createSliderFromZone(zone, category) {
    const slider = {
      id: this.generateSliderId(zone),
      label: this.generateSliderLabel(zone),
      category: category,
      zone: zone,
      min: zone.range.min,
      max: zone.range.max,
      default: zone.range.default,
      step: this.calculateStep(zone.range),
      value: zone.range.default,
      deformationType: zone.deformationType,
      axis: zone.axis,
      vertices: zone.vertices,
      bones: zone.bones,
      description: this.generateDescription(zone),
      icon: this.selectIcon(zone)
    };
    
    return slider;
  }

  /**
   * Generate unique slider ID
   */
  generateSliderId(zone) {
    const baseName = zone.name.replace(/[^a-zA-Z0-9]/g, '_');
    return `slider_${zone.type}_${baseName}_${Date.now()}`;
  }

  /**
   * Generate human-readable slider label
   */
  generateSliderLabel(zone) {
    // Convert camelCase or snake_case to Title Case
    let label = zone.name
      .replace(/([A-Z])/g, ' $1')
      .replace(/_/g, ' ')
      .trim();
    
    // Capitalize first letter of each word
    label = label.split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
    
    return label;
  }

  /**
   * Calculate appropriate step size for slider
   */
  calculateStep(range) {
    const rangeSize = range.max - range.min;
    
    if (rangeSize <= 1) {
      return 0.01;
    } else if (rangeSize <= 2) {
      return 0.05;
    } else if (rangeSize <= 5) {
      return 0.1;
    } else {
      return 0.25;
    }
  }

  /**
   * Generate description for slider
   */
  generateDescription(zone) {
    const descriptions = {
      'muscle': {
        'biceps': 'Adjust the size and definition of the bicep muscles',
        'triceps': 'Control tricep muscle prominence',
        'chest': 'Modify chest muscle size and definition',
        'abs': 'Adjust abdominal muscle definition',
        'shoulders': 'Control shoulder muscle size',
        'back': 'Modify back muscle definition',
        'quadriceps': 'Adjust thigh muscle size',
        'calves': 'Control calf muscle definition'
      },
      'animalFeature': {
        'snoutLength': 'Elongate or shorten the snout/muzzle',
        'snoutWidth': 'Adjust the width of the snout',
        'earSize': 'Scale the size of the ears',
        'earPosition': 'Move ears up or down on the head',
        'tailLength': 'Adjust the length of the tail',
        'tailThickness': 'Control tail thickness',
        'hornLength': 'Modify horn length',
        'hornCurve': 'Adjust the curvature of horns',
        'wingSpan': 'Control the span of wings'
      },
      'hairDeformation': {
        'length': 'Adjust hair length',
        'volume': 'Control hair volume and thickness',
        'curve': 'Modify hair flow and curvature',
        'headAdaptation': 'How closely hair conforms to head shape'
      },
      'facialHairDeformation': {
        'length': 'Adjust facial hair length',
        'density': 'Control facial hair thickness',
        'faceAdaptation': 'How closely facial hair follows face contours'
      }
    };
    
    // Try to find a matching description
    if (descriptions[zone.type]) {
      for (const [key, desc] of Object.entries(descriptions[zone.type])) {
        if (zone.name.toLowerCase().includes(key.toLowerCase())) {
          return desc;
        }
      }
    }
    
    // Generate generic description
    return `Adjust ${this.generateSliderLabel(zone).toLowerCase()}`;
  }

  /**
   * Select appropriate icon for slider
   */
  selectIcon(zone) {
    const icons = {
      'muscle': '💪',
      'animalFeature': '🐺',
      'hairDeformation': '💇',
      'facialHairDeformation': '🧔'
    };
    
    return icons[zone.type] || '⚙️';
  }

  /**
   * Generate slider UI HTML
   */
  generateSliderUI(sliders) {
    const categorizedSliders = this.groupSlidersByCategory(sliders);
    let html = '<div class="dynamic-sliders-container">';
    
    categorizedSliders.forEach((categorySliders, categoryName) => {
      html += `
        <div class="slider-category" data-category="${categoryName}">
          <div class="category-header">
            <h3>${categoryName}</h3>
            <button class="category-toggle" onclick="toggleCategory('${categoryName}')">▼</button>
          </div>
          <div class="category-content" id="category-${categoryName.replace(/\s+/g, '-')}">
      `;
      
      categorySliders.forEach(slider => {
        html += this.generateSingleSliderHTML(slider);
      });
      
      html += `
          </div>
        </div>
      `;
    });
    
    html += '</div>';
    return html;
  }

  /**
   * Generate HTML for a single slider
   */
  generateSingleSliderHTML(slider) {
    return `
      <div class="slider-control" data-slider-id="${slider.id}">
        <div class="slider-header">
          <label for="${slider.id}">
            <span class="slider-icon">${slider.icon}</span>
            <span class="slider-label">${slider.label}</span>
          </label>
          <span class="slider-value" id="${slider.id}-value">${slider.value.toFixed(2)}</span>
        </div>
        <input 
          type="range" 
          id="${slider.id}"
          class="slider-input"
          min="${slider.min}" 
          max="${slider.max}" 
          step="${slider.step}" 
          value="${slider.default}"
          data-zone-type="${slider.deformationType}"
          data-zone-axis="${slider.axis}"
        />
        <div class="slider-description">${slider.description}</div>
        <div class="slider-actions">
          <button class="reset-button" onclick="resetSlider('${slider.id}')">Reset</button>
        </div>
      </div>
    `;
  }

  /**
   * Group sliders by category
   */
  groupSlidersByCategory(sliders) {
    const grouped = new Map();
    
    sliders.forEach(slider => {
      if (!grouped.has(slider.category)) {
        grouped.set(slider.category, []);
      }
      grouped.get(slider.category).push(slider);
    });
    
    // Sort categories
    const sortedCategories = new Map([...grouped.entries()].sort((a, b) => {
      const order = [
        'Muscle Definition',
        'Animal Head Features',
        'Hair Customization',
        'Facial Hair Customization',
        'Tail Features',
        'Wing Features',
        'Horn Features',
        'Head & Face',
        'Torso',
        'Arms',
        'Legs'
      ];
      
      const indexA = order.indexOf(a[0]);
      const indexB = order.indexOf(b[0]);
      
      if (indexA === -1 && indexB === -1) return 0;
      if (indexA === -1) return 1;
      if (indexB === -1) return -1;
      
      return indexA - indexB;
    }));
    
    return sortedCategories;
  }

  /**
   * Generate CSS for slider UI
   */
  generateSliderCSS() {
    return `
      .dynamic-sliders-container {
        width: 100%;
        max-width: 400px;
        background: rgba(20, 20, 30, 0.95);
        border-radius: 12px;
        padding: 16px;
        color: #e8f6ff;
        font-family: 'Inter', 'Roboto', sans-serif;
        max-height: 80vh;
        overflow-y: auto;
      }

      .slider-category {
        margin-bottom: 20px;
        border: 1px solid rgba(88, 185, 237, 0.3);
        border-radius: 8px;
        overflow: hidden;
      }

      .category-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        background: rgba(88, 185, 237, 0.1);
        cursor: pointer;
        user-select: none;
      }

      .category-header h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #58b9ed;
      }

      .category-toggle {
        background: none;
        border: none;
        color: #58b9ed;
        font-size: 18px;
        cursor: pointer;
        transition: transform 0.3s ease;
      }

      .category-toggle.collapsed {
        transform: rotate(-90deg);
      }

      .category-content {
        padding: 16px;
        display: block;
      }

      .category-content.collapsed {
        display: none;
      }

      .slider-control {
        margin-bottom: 20px;
        padding: 12px;
        background: rgba(255, 255, 255, 0.05);
        border-radius: 6px;
        transition: background 0.2s ease;
      }

      .slider-control:hover {
        background: rgba(255, 255, 255, 0.08);
      }

      .slider-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
      }

      .slider-header label {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
      }

      .slider-icon {
        font-size: 18px;
      }

      .slider-value {
        font-size: 13px;
        color: #ffd479;
        font-weight: 600;
        min-width: 50px;
        text-align: right;
      }

      .slider-input {
        width: 100%;
        height: 6px;
        border-radius: 3px;
        background: rgba(88, 185, 237, 0.2);
        outline: none;
        -webkit-appearance: none;
        margin: 8px 0;
      }

      .slider-input::-webkit-slider-thumb {
        -webkit-appearance: none;
        appearance: none;
        width: 18px;
        height: 18px;
        border-radius: 50%;
        background: #58b9ed;
        cursor: pointer;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        transition: all 0.2s ease;
      }

      .slider-input::-webkit-slider-thumb:hover {
        background: #6dc9ff;
        transform: scale(1.1);
      }

      .slider-input::-moz-range-thumb {
        width: 18px;
        height: 18px;
        border-radius: 50%;
        background: #58b9ed;
        cursor: pointer;
        border: none;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        transition: all 0.2s ease;
      }

      .slider-input::-moz-range-thumb:hover {
        background: #6dc9ff;
        transform: scale(1.1);
      }

      .slider-description {
        font-size: 12px;
        color: rgba(232, 246, 255, 0.7);
        margin-top: 4px;
        line-height: 1.4;
      }

      .slider-actions {
        margin-top: 8px;
        display: flex;
        gap: 8px;
      }

      .reset-button {
        padding: 4px 12px;
        background: rgba(88, 185, 237, 0.2);
        border: 1px solid rgba(88, 185, 237, 0.4);
        border-radius: 4px;
        color: #58b9ed;
        font-size: 11px;
        cursor: pointer;
        transition: all 0.2s ease;
      }

      .reset-button:hover {
        background: rgba(88, 185, 237, 0.3);
        border-color: #58b9ed;
      }

      /* Scrollbar styling */
      .dynamic-sliders-container::-webkit-scrollbar {
        width: 8px;
      }

      .dynamic-sliders-container::-webkit-scrollbar-track {
        background: rgba(0, 0, 0, 0.2);
        border-radius: 4px;
      }

      .dynamic-sliders-container::-webkit-scrollbar-thumb {
        background: rgba(88, 185, 237, 0.4);
        border-radius: 4px;
      }

      .dynamic-sliders-container::-webkit-scrollbar-thumb:hover {
        background: rgba(88, 185, 237, 0.6);
      }
    `;
  }

  /**
   * Generate JavaScript for slider interactions
   */
  generateSliderJS() {
    return `
      // Slider interaction handlers
      function toggleCategory(categoryName) {
        const categoryId = 'category-' + categoryName.replace(/\\s+/g, '-');
        const content = document.getElementById(categoryId);
        const button = content.previousElementSibling.querySelector('.category-toggle');
        
        if (content.classList.contains('collapsed')) {
          content.classList.remove('collapsed');
          button.classList.remove('collapsed');
        } else {
          content.classList.add('collapsed');
          button.classList.add('collapsed');
        }
      }

      function resetSlider(sliderId) {
        const slider = document.getElementById(sliderId);
        if (slider) {
          const defaultValue = parseFloat(slider.getAttribute('data-default') || slider.min);
          slider.value = defaultValue;
          updateSliderValue(sliderId, defaultValue);
          applySliderDeformation(sliderId, defaultValue);
        }
      }

      function updateSliderValue(sliderId, value) {
        const valueDisplay = document.getElementById(sliderId + '-value');
        if (valueDisplay) {
          valueDisplay.textContent = parseFloat(value).toFixed(2);
        }
      }

      function applySliderDeformation(sliderId, value) {
        const slider = document.getElementById(sliderId);
        if (!slider) return;
        
        const zoneType = slider.getAttribute('data-zone-type');
        const axis = slider.getAttribute('data-zone-axis');
        
        // Call the deformation system
        if (window.MeshDeformer) {
          window.MeshDeformer.applyDeformation(sliderId, value, zoneType, axis);
        }
        
        // Emit event for external listeners
        window.dispatchEvent(new CustomEvent('sliderChanged', {
          detail: { sliderId, value, zoneType, axis }
        }));
      }

      // Initialize all sliders
      function initializeSliders() {
        const sliders = document.querySelectorAll('.slider-input');
        sliders.forEach(slider => {
          slider.addEventListener('input', function() {
            updateSliderValue(this.id, this.value);
            applySliderDeformation(this.id, this.value);
          });
          
          // Store default value
          slider.setAttribute('data-default', slider.value);
        });
      }

      // Auto-initialize when DOM is ready
      if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeSliders);
      } else {
        initializeSliders();
      }
    `;
  }

  /**
   * Export slider configuration as JSON
   */
  exportSliderConfig(sliders) {
    return JSON.stringify({
      version: '1.0',
      timestamp: new Date().toISOString(),
      sliders: sliders.map(slider => ({
        id: slider.id,
        label: slider.label,
        category: slider.category,
        min: slider.min,
        max: slider.max,
        default: slider.default,
        step: slider.step,
        deformationType: slider.deformationType,
        axis: slider.axis,
        description: slider.description
      }))
    }, null, 2);
  }

  /**
   * Import slider configuration from JSON
   */
  importSliderConfig(jsonConfig) {
    try {
      const config = JSON.parse(jsonConfig);
      return config.sliders || [];
    } catch (error) {
      console.error('Failed to import slider config:', error);
      return [];
    }
  }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = SliderGenerator;
}