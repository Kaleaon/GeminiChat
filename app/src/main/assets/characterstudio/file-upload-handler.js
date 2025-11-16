/**
 * File Upload Handler
 * Handles mesh file uploads and processing
 */

class FileUploadHandler {
  constructor(meshProcessor, sliderGenerator, meshDeformer) {
    this.meshProcessor = meshProcessor;
    this.sliderGenerator = sliderGenerator;
    this.meshDeformer = meshDeformer;
    this.supportedFormats = ['glb', 'gltf', 'fbx', 'obj', 'vrm'];
    this.maxFileSize = 50 * 1024 * 1024; // 50MB
  }

  /**
   * Initialize file upload UI
   */
  initializeUploadUI() {
    const uploadHTML = this.generateUploadHTML();
    const uploadCSS = this.generateUploadCSS();
    
    // Inject CSS
    const styleElement = document.createElement('style');
    styleElement.textContent = uploadCSS;
    document.head.appendChild(styleElement);
    
    // Create upload container
    const container = document.createElement('div');
    container.innerHTML = uploadHTML;
    document.body.appendChild(container);
    
    // Attach event listeners
    this.attachEventListeners();
  }

  /**
   * Generate upload UI HTML
   */
  generateUploadHTML() {
    return `
      <div id="mesh-upload-container" class="mesh-upload-container">
        <button id="upload-trigger-btn" class="upload-trigger-btn">
          📁 Upload Custom Mesh
        </button>
        
        <div id="upload-modal" class="upload-modal hidden">
          <div class="upload-modal-content">
            <div class="upload-modal-header">
              <h2>Upload Custom Character Mesh</h2>
              <button class="close-modal-btn" onclick="closeUploadModal()">✕</button>
            </div>
            
            <div class="upload-modal-body">
              <div class="upload-dropzone" id="upload-dropzone">
                <div class="dropzone-content">
                  <div class="dropzone-icon">📦</div>
                  <p class="dropzone-text">Drag & drop your mesh file here</p>
                  <p class="dropzone-subtext">or click to browse</p>
                  <p class="dropzone-formats">Supported: GLB, GLTF, FBX, OBJ, VRM</p>
                  <input 
                    type="file" 
                    id="file-input" 
                    class="file-input" 
                    accept=".glb,.gltf,.fbx,.obj,.vrm"
                  />
                </div>
              </div>
              
              <div id="upload-progress" class="upload-progress hidden">
                <div class="progress-bar-container">
                  <div class="progress-bar" id="progress-bar"></div>
                </div>
                <p class="progress-text" id="progress-text">Processing...</p>
              </div>
              
              <div id="upload-options" class="upload-options hidden">
                <h3>Mesh Processing Options</h3>
                
                <div class="option-group">
                  <label>
                    <input type="checkbox" id="auto-rig" checked />
                    Auto-detect rigging
                  </label>
                </div>
                
                <div class="option-group">
                  <label>
                    <input type="checkbox" id="detect-hair" checked />
                    Detect hair systems
                  </label>
                </div>
                
                <div class="option-group">
                  <label>
                    <input type="checkbox" id="detect-facial-hair" checked />
                    Detect facial hair
                  </label>
                </div>
                
                <div class="option-group">
                  <label>
                    <input type="checkbox" id="detect-animal-features" checked />
                    Detect animal features
                  </label>
                </div>
                
                <div class="option-group">
                  <label>
                    <input type="checkbox" id="generate-muscle-zones" checked />
                    Generate muscle definition zones
                  </label>
                </div>
                
                <div class="option-group">
                  <label>
                    <input type="checkbox" id="optimize-mesh" checked />
                    Optimize mesh geometry
                  </label>
                </div>
                
                <button class="process-btn" id="process-btn">
                  Process Mesh & Generate Sliders
                </button>
              </div>
              
              <div id="upload-results" class="upload-results hidden">
                <h3>Processing Complete!</h3>
                <div class="results-summary" id="results-summary"></div>
                <button class="apply-btn" id="apply-btn">
                  Apply to Character
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  /**
   * Generate upload UI CSS
   */
  generateUploadCSS() {
    return `
      .mesh-upload-container {
        position: fixed;
        top: 80px;
        right: 16px;
        z-index: 1500;
      }

      .upload-trigger-btn {
        padding: 12px 20px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        border-radius: 8px;
        color: white;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        transition: all 0.3s ease;
      }

      .upload-trigger-btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.6);
      }

      .upload-modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.8);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 2000;
        backdrop-filter: blur(4px);
      }

      .upload-modal.hidden {
        display: none;
      }

      .upload-modal-content {
        background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
        border-radius: 16px;
        width: 90%;
        max-width: 600px;
        max-height: 90vh;
        overflow-y: auto;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
        border: 1px solid rgba(88, 185, 237, 0.3);
      }

      .upload-modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 24px;
        border-bottom: 1px solid rgba(88, 185, 237, 0.2);
      }

      .upload-modal-header h2 {
        margin: 0;
        color: #58b9ed;
        font-size: 20px;
        font-weight: 600;
      }

      .close-modal-btn {
        background: none;
        border: none;
        color: #58b9ed;
        font-size: 24px;
        cursor: pointer;
        padding: 0;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 4px;
        transition: background 0.2s ease;
      }

      .close-modal-btn:hover {
        background: rgba(88, 185, 237, 0.2);
      }

      .upload-modal-body {
        padding: 24px;
      }

      .upload-dropzone {
        border: 2px dashed rgba(88, 185, 237, 0.4);
        border-radius: 12px;
        padding: 40px 20px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;
        position: relative;
      }

      .upload-dropzone:hover {
        border-color: #58b9ed;
        background: rgba(88, 185, 237, 0.05);
      }

      .upload-dropzone.dragover {
        border-color: #58b9ed;
        background: rgba(88, 185, 237, 0.1);
        transform: scale(1.02);
      }

      .dropzone-content {
        pointer-events: none;
      }

      .dropzone-icon {
        font-size: 48px;
        margin-bottom: 16px;
      }

      .dropzone-text {
        color: #e8f6ff;
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 8px 0;
      }

      .dropzone-subtext {
        color: rgba(232, 246, 255, 0.7);
        font-size: 14px;
        margin: 0 0 16px 0;
      }

      .dropzone-formats {
        color: rgba(232, 246, 255, 0.5);
        font-size: 12px;
        margin: 0;
      }

      .file-input {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        opacity: 0;
        cursor: pointer;
      }

      .upload-progress {
        margin-top: 24px;
      }

      .upload-progress.hidden {
        display: none;
      }

      .progress-bar-container {
        width: 100%;
        height: 8px;
        background: rgba(88, 185, 237, 0.2);
        border-radius: 4px;
        overflow: hidden;
        margin-bottom: 12px;
      }

      .progress-bar {
        height: 100%;
        background: linear-gradient(90deg, #58b9ed 0%, #667eea 100%);
        border-radius: 4px;
        transition: width 0.3s ease;
        width: 0%;
      }

      .progress-text {
        color: #58b9ed;
        font-size: 14px;
        text-align: center;
        margin: 0;
      }

      .upload-options {
        margin-top: 24px;
      }

      .upload-options.hidden {
        display: none;
      }

      .upload-options h3 {
        color: #58b9ed;
        font-size: 16px;
        margin: 0 0 16px 0;
      }

      .option-group {
        margin-bottom: 12px;
      }

      .option-group label {
        display: flex;
        align-items: center;
        color: #e8f6ff;
        font-size: 14px;
        cursor: pointer;
      }

      .option-group input[type="checkbox"] {
        margin-right: 8px;
        width: 18px;
        height: 18px;
        cursor: pointer;
      }

      .process-btn,
      .apply-btn {
        width: 100%;
        padding: 14px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        border-radius: 8px;
        color: white;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        margin-top: 20px;
        transition: all 0.3s ease;
      }

      .process-btn:hover,
      .apply-btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.6);
      }

      .process-btn:disabled,
      .apply-btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
        transform: none;
      }

      .upload-results {
        margin-top: 24px;
      }

      .upload-results.hidden {
        display: none;
      }

      .upload-results h3 {
        color: #58b9ed;
        font-size: 18px;
        margin: 0 0 16px 0;
        text-align: center;
      }

      .results-summary {
        background: rgba(88, 185, 237, 0.1);
        border: 1px solid rgba(88, 185, 237, 0.3);
        border-radius: 8px;
        padding: 16px;
        color: #e8f6ff;
        font-size: 14px;
        line-height: 1.6;
      }

      .results-summary p {
        margin: 8px 0;
      }

      .results-summary strong {
        color: #ffd479;
      }
    `;
  }

  /**
   * Attach event listeners
   */
  attachEventListeners() {
    // Upload trigger button
    const triggerBtn = document.getElementById('upload-trigger-btn');
    if (triggerBtn) {
      triggerBtn.addEventListener('click', () => this.openUploadModal());
    }

    // Dropzone
    const dropzone = document.getElementById('upload-dropzone');
    const fileInput = document.getElementById('file-input');
    
    if (dropzone && fileInput) {
      dropzone.addEventListener('click', () => fileInput.click());
      
      dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('dragover');
      });
      
      dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('dragover');
      });
      
      dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('dragover');
        const files = e.dataTransfer.files;
        if (files.length > 0) {
          this.handleFileSelection(files[0]);
        }
      });
      
      fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
          this.handleFileSelection(e.target.files[0]);
        }
      });
    }

    // Process button
    const processBtn = document.getElementById('process-btn');
    if (processBtn) {
      processBtn.addEventListener('click', () => this.processMesh());
    }

    // Apply button
    const applyBtn = document.getElementById('apply-btn');
    if (applyBtn) {
      applyBtn.addEventListener('click', () => this.applyMeshToCharacter());
    }

    // Make close function global
    window.closeUploadModal = () => this.closeUploadModal();
  }

  /**
   * Open upload modal
   */
  openUploadModal() {
    const modal = document.getElementById('upload-modal');
    if (modal) {
      modal.classList.remove('hidden');
    }
  }

  /**
   * Close upload modal
   */
  closeUploadModal() {
    const modal = document.getElementById('upload-modal');
    if (modal) {
      modal.classList.add('hidden');
    }
    
    // Reset UI
    this.resetUploadUI();
  }

  /**
   * Reset upload UI
   */
  resetUploadUI() {
    const dropzone = document.getElementById('upload-dropzone');
    const progress = document.getElementById('upload-progress');
    const options = document.getElementById('upload-options');
    const results = document.getElementById('upload-results');
    
    if (dropzone) dropzone.classList.remove('hidden');
    if (progress) progress.classList.add('hidden');
    if (options) options.classList.add('hidden');
    if (results) results.classList.add('hidden');
    
    const fileInput = document.getElementById('file-input');
    if (fileInput) fileInput.value = '';
    
    this.currentFile = null;
    this.currentMeshData = null;
    this.currentAnalysis = null;
  }

  /**
   * Handle file selection
   */
  async handleFileSelection(file) {
    // Validate file
    const validation = this.validateFile(file);
    if (!validation.valid) {
      alert(validation.error);
      return;
    }

    this.currentFile = file;

    // Show options
    const dropzone = document.getElementById('upload-dropzone');
    const options = document.getElementById('upload-options');
    
    if (dropzone) dropzone.classList.add('hidden');
    if (options) options.classList.remove('hidden');
  }

  /**
   * Validate uploaded file
   */
  validateFile(file) {
    // Check file size
    if (file.size > this.maxFileSize) {
      return {
        valid: false,
        error: `File size exceeds maximum of ${this.maxFileSize / 1024 / 1024}MB`
      };
    }

    // Check file format
    const extension = file.name.split('.').pop().toLowerCase();
    if (!this.supportedFormats.includes(extension)) {
      return {
        valid: false,
        error: `Unsupported file format. Supported formats: ${this.supportedFormats.join(', ')}`
      };
    }

    return { valid: true };
  }

  /**
   * Process uploaded mesh
   */
  async processMesh() {
    if (!this.currentFile) return;

    // Get processing options
    const options = {
      autoRig: document.getElementById('auto-rig')?.checked || false,
      detectHair: document.getElementById('detect-hair')?.checked || false,
      detectFacialHair: document.getElementById('detect-facial-hair')?.checked || false,
      detectAnimalFeatures: document.getElementById('detect-animal-features')?.checked || false,
      generateMuscleZones: document.getElementById('generate-muscle-zones')?.checked || false,
      optimizeMesh: document.getElementById('optimize-mesh')?.checked || false
    };

    // Show progress
    const optionsDiv = document.getElementById('upload-options');
    const progress = document.getElementById('upload-progress');
    
    if (optionsDiv) optionsDiv.classList.add('hidden');
    if (progress) progress.classList.remove('hidden');

    try {
      // Update progress
      this.updateProgress(10, 'Reading file...');

      // Read file
      const meshData = await this.readFile(this.currentFile);
      this.currentMeshData = meshData;

      // Update progress
      this.updateProgress(30, 'Analyzing mesh structure...');

      // Analyze mesh
      const format = this.currentFile.name.split('.').pop().toLowerCase();
      const analysis = await this.meshProcessor.analyzeMesh(meshData, format);
      this.currentAnalysis = analysis;

      // Update progress
      this.updateProgress(60, 'Generating deformation zones...');

      // Apply processing options
      if (!options.detectHair) {
        analysis.hairGroups = [];
      }
      if (!options.detectFacialHair) {
        analysis.facialHairGroups = [];
      }
      if (!options.generateMuscleZones) {
        analysis.deformationZones = analysis.deformationZones.filter(
          zone => zone.type !== 'muscle'
        );
      }
      if (!options.detectAnimalFeatures) {
        analysis.deformationZones = analysis.deformationZones.filter(
          zone => zone.type !== 'animalFeature'
        );
      }

      // Update progress
      this.updateProgress(80, 'Generating sliders...');

      // Generate sliders
      const sliders = this.sliderGenerator.generateSliders(
        analysis.deformationZones,
        analysis
      );

      // Update progress
      this.updateProgress(100, 'Complete!');

      // Show results
      setTimeout(() => {
        this.showResults(analysis, sliders);
      }, 500);

    } catch (error) {
      console.error('Mesh processing failed:', error);
      alert('Failed to process mesh: ' + error.message);
      this.resetUploadUI();
    }
  }

  /**
   * Read file as ArrayBuffer
   */
  readFile(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      
      reader.onload = (e) => {
        resolve(e.target.result);
      };
      
      reader.onerror = () => {
        reject(new Error('Failed to read file'));
      };
      
      reader.readAsArrayBuffer(file);
    });
  }

  /**
   * Update progress bar
   */
  updateProgress(percent, text) {
    const progressBar = document.getElementById('progress-bar');
    const progressText = document.getElementById('progress-text');
    
    if (progressBar) {
      progressBar.style.width = percent + '%';
    }
    
    if (progressText) {
      progressText.textContent = text;
    }
  }

  /**
   * Show processing results
   */
  showResults(analysis, sliders) {
    const progress = document.getElementById('upload-progress');
    const results = document.getElementById('upload-results');
    const summary = document.getElementById('results-summary');
    
    if (progress) progress.classList.add('hidden');
    if (results) results.classList.remove('hidden');
    
    if (summary) {
      const categories = new Map();
      sliders.forEach(slider => {
        if (!categories.has(slider.category)) {
          categories.set(slider.category, 0);
        }
        categories.set(slider.category, categories.get(slider.category) + 1);
      });
      
      let summaryHTML = `
        <p><strong>Mesh Analysis Complete!</strong></p>
        <p>📊 Total Vertices: ${analysis.vertices.length.toLocaleString()}</p>
        <p>🦴 Bones Detected: ${analysis.bones.length}</p>
        <p>🎭 Morph Targets: ${analysis.morphTargets.length}</p>
        <p>💇 Hair Groups: ${analysis.hairGroups.length}</p>
        <p>🧔 Facial Hair Groups: ${analysis.facialHairGroups.length}</p>
        <p>🎚️ <strong>Generated ${sliders.length} Dynamic Sliders</strong></p>
        <div style="margin-top: 12px; padding-top: 12px; border-top: 1px solid rgba(88, 185, 237, 0.3);">
          <p style="margin-bottom: 8px;"><strong>Slider Categories:</strong></p>
      `;
      
      categories.forEach((count, category) => {
        summaryHTML += `<p style="margin: 4px 0;">• ${category}: ${count} sliders</p>`;
      });
      
      summaryHTML += '</div>';
      summary.innerHTML = summaryHTML;
    }
  }

  /**
   * Apply mesh to character
   */
  applyMeshToCharacter() {
    if (!this.currentMeshData || !this.currentAnalysis) {
      alert('No mesh data available');
      return;
    }

    // Load mesh into scene
    // This would integrate with the Three.js scene
    console.log('Applying mesh to character...');
    
    // Generate and inject slider UI
    const sliders = this.sliderGenerator.sliders;
    const sliderHTML = this.sliderGenerator.generateSliderUI(sliders);
    const sliderCSS = this.sliderGenerator.generateSliderCSS();
    const sliderJS = this.sliderGenerator.generateSliderJS();
    
    // Inject slider UI
    let sliderContainer = document.getElementById('dynamic-sliders-panel');
    if (!sliderContainer) {
      sliderContainer = document.createElement('div');
      sliderContainer.id = 'dynamic-sliders-panel';
      sliderContainer.style.position = 'fixed';
      sliderContainer.style.left = '16px';
      sliderContainer.style.top = '80px';
      sliderContainer.style.zIndex = '1500';
      document.body.appendChild(sliderContainer);
    }
    
    sliderContainer.innerHTML = sliderHTML;
    
    // Inject CSS if not already present
    if (!document.getElementById('dynamic-sliders-css')) {
      const styleElement = document.createElement('style');
      styleElement.id = 'dynamic-sliders-css';
      styleElement.textContent = sliderCSS;
      document.head.appendChild(styleElement);
    }
    
    // Inject JS if not already present
    if (!document.getElementById('dynamic-sliders-js')) {
      const scriptElement = document.createElement('script');
      scriptElement.id = 'dynamic-sliders-js';
      scriptElement.textContent = sliderJS;
      document.body.appendChild(scriptElement);
    }
    
    // Close modal
    this.closeUploadModal();
    
    // Show success message
    alert('Mesh applied successfully! Use the new sliders on the left to customize your character.');
  }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = FileUploadHandler;
}