/**
 * Advanced Character Studio Integration
 * Main integration file that brings all systems together
 */

class AdvancedCharacterStudio {
  constructor() {
    this.meshProcessor = null;
    this.sliderGenerator = null;
    this.meshDeformer = null;
    this.fileUploadHandler = null;
    this.scene = null;
    this.currentCharacter = null;
    this.initialized = false;
  }

  /**
   * Initialize the advanced character studio
   */
  async initialize(scene) {
    console.log('Initializing Advanced Character Studio...');
    
    this.scene = scene;
    
    // Initialize core systems
    this.meshProcessor = new MeshProcessor();
    this.sliderGenerator = new SliderGenerator();
    this.meshDeformer = new MeshDeformer(scene, this.meshProcessor);
    this.fileUploadHandler = new FileUploadHandler(
      this.meshProcessor,
      this.sliderGenerator,
      this.meshDeformer
    );
    
    // Initialize file upload UI
    this.fileUploadHandler.initializeUploadUI();
    
    // Set up global API
    this.setupGlobalAPI();
    
    // Initialize preset system
    this.initializePresetSystem();
    
    this.initialized = true;
    console.log('Advanced Character Studio initialized successfully');
    
    // Dispatch initialization event
    window.dispatchEvent(new CustomEvent('AdvancedCharacterStudioReady', {
      detail: { studio: this }
    }));
  }

  /**
   * Set up global API for external access
   */
  setupGlobalAPI() {
    // Make systems available globally
    window.AdvancedCharacterStudio = this;
    window.MeshProcessor = this.meshProcessor;
    window.SliderGenerator = this.sliderGenerator;
    window.MeshDeformer = this.meshDeformer;
    
    // Expose key functions
    window.uploadCustomMesh = (file) => this.uploadCustomMesh(file);
    window.applyDeformation = (sliderId, value, type, axis) => 
      this.applyDeformation(sliderId, value, type, axis);
    window.resetAllDeformations = () => this.resetAllDeformations();
    window.exportCharacter = () => this.exportCharacter();
    window.importCharacter = (data) => this.importCharacter(data);
    window.savePreset = (name) => this.savePreset(name);
    window.loadPreset = (name) => this.loadPreset(name);
  }

  /**
   * Initialize preset system
   */
  initializePresetSystem() {
    // Create preset UI
    const presetHTML = `
      <div id="preset-panel" class="preset-panel">
        <div class="preset-header">
          <h3>Character Presets</h3>
          <button class="preset-toggle" onclick="togglePresetPanel()">▼</button>
        </div>
        <div class="preset-content" id="preset-content">
          <div class="preset-actions">
            <input 
              type="text" 
              id="preset-name-input" 
              placeholder="Preset name..." 
              class="preset-input"
            />
            <button onclick="window.savePreset()" class="preset-btn save-btn">
              💾 Save
            </button>
          </div>
          <div class="preset-list" id="preset-list">
            <!-- Presets will be loaded here -->
          </div>
        </div>
      </div>
    `;
    
    const presetCSS = `
      .preset-panel {
        position: fixed;
        right: 16px;
        bottom: 16px;
        width: 280px;
        background: rgba(20, 20, 30, 0.95);
        border: 1px solid rgba(88, 185, 237, 0.3);
        border-radius: 12px;
        z-index: 1500;
        color: #e8f6ff;
        font-family: 'Inter', 'Roboto', sans-serif;
      }

      .preset-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        background: rgba(88, 185, 237, 0.1);
        border-radius: 12px 12px 0 0;
        cursor: pointer;
      }

      .preset-header h3 {
        margin: 0;
        font-size: 14px;
        font-weight: 600;
        color: #58b9ed;
      }

      .preset-toggle {
        background: none;
        border: none;
        color: #58b9ed;
        font-size: 16px;
        cursor: pointer;
        transition: transform 0.3s ease;
      }

      .preset-toggle.collapsed {
        transform: rotate(-90deg);
      }

      .preset-content {
        padding: 16px;
        max-height: 400px;
        overflow-y: auto;
      }

      .preset-content.collapsed {
        display: none;
      }

      .preset-actions {
        display: flex;
        gap: 8px;
        margin-bottom: 16px;
      }

      .preset-input {
        flex: 1;
        padding: 8px 12px;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(88, 185, 237, 0.3);
        border-radius: 6px;
        color: #e8f6ff;
        font-size: 13px;
      }

      .preset-input:focus {
        outline: none;
        border-color: #58b9ed;
      }

      .preset-btn {
        padding: 8px 12px;
        background: rgba(88, 185, 237, 0.2);
        border: 1px solid rgba(88, 185, 237, 0.4);
        border-radius: 6px;
        color: #58b9ed;
        font-size: 12px;
        cursor: pointer;
        transition: all 0.2s ease;
        white-space: nowrap;
      }

      .preset-btn:hover {
        background: rgba(88, 185, 237, 0.3);
        border-color: #58b9ed;
      }

      .preset-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .preset-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 12px;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(88, 185, 237, 0.2);
        border-radius: 6px;
        transition: all 0.2s ease;
      }

      .preset-item:hover {
        background: rgba(255, 255, 255, 0.08);
        border-color: rgba(88, 185, 237, 0.4);
      }

      .preset-name {
        font-size: 13px;
        color: #e8f6ff;
      }

      .preset-item-actions {
        display: flex;
        gap: 4px;
      }

      .preset-item-btn {
        padding: 4px 8px;
        background: none;
        border: none;
        color: #58b9ed;
        font-size: 11px;
        cursor: pointer;
        border-radius: 4px;
        transition: background 0.2s ease;
      }

      .preset-item-btn:hover {
        background: rgba(88, 185, 237, 0.2);
      }

      .preset-content::-webkit-scrollbar {
        width: 6px;
      }

      .preset-content::-webkit-scrollbar-track {
        background: rgba(0, 0, 0, 0.2);
        border-radius: 3px;
      }

      .preset-content::-webkit-scrollbar-thumb {
        background: rgba(88, 185, 237, 0.4);
        border-radius: 3px;
      }
    `;
    
    // Inject preset UI
    const container = document.createElement('div');
    container.innerHTML = presetHTML;
    document.body.appendChild(container);
    
    // Inject preset CSS
    const styleElement = document.createElement('style');
    styleElement.textContent = presetCSS;
    document.head.appendChild(styleElement);
    
    // Load saved presets
    this.loadPresetList();
    
    // Set up global functions
    window.togglePresetPanel = () => this.togglePresetPanel();
  }

  /**
   * Toggle preset panel
   */
  togglePresetPanel() {
    const content = document.getElementById('preset-content');
    const toggle = document.querySelector('.preset-toggle');
    
    if (content && toggle) {
      content.classList.toggle('collapsed');
      toggle.classList.toggle('collapsed');
    }
  }

  /**
   * Upload custom mesh
   */
  async uploadCustomMesh(file) {
    return await this.fileUploadHandler.handleFileSelection(file);
  }

  /**
   * Apply deformation
   */
  applyDeformation(sliderId, value, type, axis) {
    // Find the zone for this slider
    const slider = this.sliderGenerator.sliders.find(s => s.id === sliderId);
    if (!slider) {
      console.warn('Slider not found:', sliderId);
      return;
    }
    
    this.meshDeformer.applyDeformation(sliderId, value, type, axis, slider.zone);
  }

  /**
   * Reset all deformations
   */
  resetAllDeformations() {
    this.meshDeformer.resetAllDeformations();
    
    // Reset all slider values
    const sliders = document.querySelectorAll('.slider-input');
    sliders.forEach(slider => {
      const defaultValue = slider.getAttribute('data-default') || slider.min;
      slider.value = defaultValue;
      
      const valueDisplay = document.getElementById(slider.id + '-value');
      if (valueDisplay) {
        valueDisplay.textContent = parseFloat(defaultValue).toFixed(2);
      }
    });
  }

  /**
   * Export character configuration
   */
  exportCharacter() {
    const characterData = {
      version: '1.0',
      timestamp: new Date().toISOString(),
      mesh: {
        format: this.fileUploadHandler.currentFile ? 
          this.fileUploadHandler.currentFile.name.split('.').pop() : null,
        analysis: this.fileUploadHandler.currentAnalysis
      },
      sliders: this.sliderGenerator.sliders.map(slider => ({
        id: slider.id,
        label: slider.label,
        category: slider.category,
        value: document.getElementById(slider.id)?.value || slider.default
      })),
      deformations: this.meshDeformer.exportDeformationState()
    };
    
    return JSON.stringify(characterData, null, 2);
  }

  /**
   * Import character configuration
   */
  async importCharacter(dataJSON) {
    try {
      const data = JSON.parse(dataJSON);
      
      // Import deformations
      if (data.deformations) {
        this.meshDeformer.importDeformationState(data.deformations);
      }
      
      // Apply slider values
      if (data.sliders) {
        data.sliders.forEach(slider => {
          const sliderElement = document.getElementById(slider.id);
          if (sliderElement) {
            sliderElement.value = slider.value;
            
            const valueDisplay = document.getElementById(slider.id + '-value');
            if (valueDisplay) {
              valueDisplay.textContent = parseFloat(slider.value).toFixed(2);
            }
            
            // Apply deformation
            const sliderConfig = this.sliderGenerator.sliders.find(s => s.id === slider.id);
            if (sliderConfig) {
              this.applyDeformation(
                slider.id,
                slider.value,
                sliderConfig.deformationType,
                sliderConfig.axis
              );
            }
          }
        });
      }
      
      console.log('Character imported successfully');
    } catch (error) {
      console.error('Failed to import character:', error);
      throw error;
    }
  }

  /**
   * Save preset
   */
  savePreset(name) {
    const presetNameInput = document.getElementById('preset-name-input');
    const presetName = name || presetNameInput?.value;
    
    if (!presetName) {
      alert('Please enter a preset name');
      return;
    }
    
    // Get current character configuration
    const characterData = this.exportCharacter();
    
    // Save to localStorage
    const presets = this.loadPresetsFromStorage();
    presets[presetName] = {
      name: presetName,
      data: characterData,
      timestamp: new Date().toISOString()
    };
    
    localStorage.setItem('characterPresets', JSON.stringify(presets));
    
    // Clear input
    if (presetNameInput) {
      presetNameInput.value = '';
    }
    
    // Refresh preset list
    this.loadPresetList();
    
    alert(`Preset "${presetName}" saved successfully!`);
  }

  /**
   * Load preset
   */
  async loadPreset(name) {
    const presets = this.loadPresetsFromStorage();
    const preset = presets[name];
    
    if (!preset) {
      alert('Preset not found');
      return;
    }
    
    try {
      await this.importCharacter(preset.data);
      alert(`Preset "${name}" loaded successfully!`);
    } catch (error) {
      alert('Failed to load preset: ' + error.message);
    }
  }

  /**
   * Delete preset
   */
  deletePreset(name) {
    if (!confirm(`Delete preset "${name}"?`)) {
      return;
    }
    
    const presets = this.loadPresetsFromStorage();
    delete presets[name];
    
    localStorage.setItem('characterPresets', JSON.stringify(presets));
    
    // Refresh preset list
    this.loadPresetList();
  }

  /**
   * Load presets from localStorage
   */
  loadPresetsFromStorage() {
    try {
      const presetsJSON = localStorage.getItem('characterPresets');
      return presetsJSON ? JSON.parse(presetsJSON) : {};
    } catch (error) {
      console.error('Failed to load presets:', error);
      return {};
    }
  }

  /**
   * Load preset list UI
   */
  loadPresetList() {
    const presetList = document.getElementById('preset-list');
    if (!presetList) return;
    
    const presets = this.loadPresetsFromStorage();
    const presetNames = Object.keys(presets);
    
    if (presetNames.length === 0) {
      presetList.innerHTML = '<p style="color: rgba(232, 246, 255, 0.5); font-size: 12px; text-align: center;">No saved presets</p>';
      return;
    }
    
    presetList.innerHTML = presetNames.map(name => {
      const preset = presets[name];
      const date = new Date(preset.timestamp);
      const dateStr = date.toLocaleDateString();
      
      return `
        <div class="preset-item">
          <div>
            <div class="preset-name">${name}</div>
            <div style="font-size: 11px; color: rgba(232, 246, 255, 0.5); margin-top: 2px;">${dateStr}</div>
          </div>
          <div class="preset-item-actions">
            <button class="preset-item-btn" onclick="window.loadPreset('${name}')">Load</button>
            <button class="preset-item-btn" onclick="window.AdvancedCharacterStudio.deletePreset('${name}')">Delete</button>
          </div>
        </div>
      `;
    }).join('');
  }

  /**
   * Create example presets
   */
  createExamplePresets() {
    const examples = {
      'Muscular Hero': {
        sliders: [
          { name: 'biceps', value: 1.8 },
          { name: 'chest', value: 1.6 },
          { name: 'shoulders', value: 1.7 },
          { name: 'abs', value: 1.5 }
        ]
      },
      'Werewolf': {
        sliders: [
          { name: 'snoutLength', value: 1.5 },
          { name: 'earSize', value: 1.8 },
          { name: 'jawWidth', value: 1.4 },
          { name: 'hairVolume', value: 1.6 }
        ]
      },
      'Slim Athlete': {
        sliders: [
          { name: 'biceps', value: 1.2 },
          { name: 'chest', value: 1.1 },
          { name: 'abs', value: 1.3 },
          { name: 'calves', value: 1.2 }
        ]
      }
    };
    
    // This would create example presets in the system
    console.log('Example presets available:', Object.keys(examples));
  }
}

// Auto-initialize when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    // Wait for Character Studio to be ready
    if (window.CharacterStudio) {
      const advancedStudio = new AdvancedCharacterStudio();
      advancedStudio.initialize(window.CharacterStudio.scene);
    } else {
      // Listen for Character Studio ready event
      window.addEventListener('CharacterStudioReady', (event) => {
        const advancedStudio = new AdvancedCharacterStudio();
        advancedStudio.initialize(event.detail.scene);
      });
    }
  });
} else {
  // DOM already loaded
  if (window.CharacterStudio) {
    const advancedStudio = new AdvancedCharacterStudio();
    advancedStudio.initialize(window.CharacterStudio.scene);
  }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = AdvancedCharacterStudio;
}