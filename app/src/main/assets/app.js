// PUZZLE NOVA - COSMIC WEB ENGINE

// --- STATE MANAGEMENT ---
const STATE = {
    // Progress & Resources
    coins: 0,
    gems: 0,
    highscore: 0,
    activeCampaignLevel: 1,
    
    // Config Flags
    soundEnabled: true,
    musicEnabled: true,
    hapticEnabled: true,
    kidsModeEnabled: false,

    // Session Data
    currentScore: 0,
    gameMode: 'infinite', // 'infinite' or 'campaign'
    grid: [],             // 8x8 matrix (0 = empty, colored hex code = block cell)
    nextPieces: [null, null, null],
    selectedPieceIndex: -1,
    selectedPieceOffset: { x: 0, y: 0 },
    dragPosition: { x: 0, y: 0 },
    isDragging: false,
    campaignObjectives: {
        1: { targetScore: 600, desc: "Synthesize 600 energy units in the Orion Lattice." },
        2: { targetScore: 1200, desc: "Synthesize 1200 energy units in the Perseus Nebula." },
        3: { targetScore: 1800, desc: "Synthesize 1800 energy units in the Andromeda Ring." },
        4: { targetScore: 2500, desc: "Synthesize 2500 energy units in the Cassiopeia Gate." },
        5: { targetScore: 4000, desc: "Synthesize 4000 energy units in the Nova Core Matrix." }
    }
};

// --- AUDIO GENERATIVE SYNTHESIZER ---
class CosmicSynth {
    constructor() {
        this.ctx = null;
        this.masterVolume = null;
        this.musicOsc1 = null;
        this.musicOsc2 = null;
        this.musicInterval = null;
    }

    init() {
        if (this.ctx) return;
        try {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            this.ctx = new AudioContext();
            this.masterVolume = this.ctx.createGain();
            this.masterVolume.connect(this.ctx.destination);
            this.masterVolume.gain.value = 0.5;

            if (STATE.musicEnabled) {
                this.startAmbientMusic();
            }
        } catch (e) {
            console.warn("AudioContext init failed", e);
        }
    }

    playClick() {
        if (!STATE.soundEnabled || !this.ctx) return;
        this.init();
        try {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(880, this.ctx.currentTime);
            osc.frequency.exponentialRampToValueAtTime(1320, this.ctx.currentTime + 0.08);
            gain.gain.setValueAtTime(0.2, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.01, this.ctx.currentTime + 0.08);
            osc.connect(gain);
            gain.connect(this.masterVolume);
            osc.start();
            osc.stop(this.ctx.currentTime + 0.08);
        } catch (e) {}
    }

    playDrop() {
        if (!STATE.soundEnabled || !this.ctx) return;
        try {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(440, this.ctx.currentTime);
            osc.frequency.exponentialRampToValueAtTime(110, this.ctx.currentTime + 0.15);
            gain.gain.setValueAtTime(0.3, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.01, this.ctx.currentTime + 0.15);
            osc.connect(gain);
            gain.connect(this.masterVolume);
            osc.start();
            osc.stop(this.ctx.currentTime + 0.15);
        } catch (e) {}
    }

    playClear() {
        if (!STATE.soundEnabled || !this.ctx) return;
        try {
            const now = this.ctx.currentTime;
            const freqs = [523.25, 659.25, 783.99, 1046.5]; // C5, E5, G5, C6 arpeggio
            freqs.forEach((freq, idx) => {
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();
                osc.type = 'sine';
                osc.frequency.setValueAtTime(freq, now + idx * 0.06);
                gain.gain.setValueAtTime(0.18, now + idx * 0.06);
                gain.gain.exponentialRampToValueAtTime(0.005, now + idx * 0.06 + 0.25);
                osc.connect(gain);
                gain.connect(this.masterVolume);
                osc.start(now + idx * 0.06);
                osc.stop(now + idx * 0.06 + 0.25);
            });
        } catch (e) {}
    }

    playGameOver() {
        if (!STATE.soundEnabled || !this.ctx) return;
        try {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'sawtooth';
            osc.frequency.setValueAtTime(220, this.ctx.currentTime);
            osc.frequency.exponentialRampToValueAtTime(55, this.ctx.currentTime + 0.8);
            gain.gain.setValueAtTime(0.25, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.01, this.ctx.currentTime + 0.8);
            osc.connect(gain);
            gain.connect(this.masterVolume);
            osc.start();
            osc.stop(this.ctx.currentTime + 0.8);
        } catch (e) {}
    }

    startAmbientMusic() {
        if (!this.ctx) return;
        this.stopAmbientMusic();
        
        try {
            const chords = [
                [220, 261.63, 329.63], // Am
                [174.61, 220, 261.63], // F
                [196, 246.94, 293.66], // G
                [261.63, 329.63, 392]  // C
            ];
            let chordIdx = 0;

            const playNextChord = () => {
                if (!STATE.musicEnabled || !this.ctx) return;
                const now = this.ctx.currentTime;
                const chord = chords[chordIdx];
                chordIdx = (chordIdx + 1) % chords.length;

                chord.forEach((freq) => {
                    const osc = this.ctx.createOscillator();
                    const gain = this.ctx.createGain();
                    osc.type = 'sine';
                    osc.frequency.value = freq;
                    gain.gain.setValueAtTime(0, now);
                    gain.gain.linearRampToValueAtTime(0.04, now + 1.5);
                    gain.gain.exponentialRampToValueAtTime(0.001, now + 5.8);
                    
                    osc.connect(gain);
                    gain.connect(this.masterVolume);
                    osc.start(now);
                    osc.stop(now + 6);
                });
            };

            playNextChord();
            this.musicInterval = setInterval(playNextChord, 6000);
        } catch (e) {}
    }

    stopAmbientMusic() {
        if (this.musicInterval) {
            clearInterval(this.musicInterval);
            this.musicInterval = null;
        }
    }

    updateSettings() {
        if (this.masterVolume) {
            this.masterVolume.gain.value = STATE.soundEnabled ? 0.5 : 0;
        }
        if (STATE.musicEnabled) {
            this.startAmbientMusic();
        } else {
            this.stopAmbientMusic();
        }
    }
}

const SYNTH = new CosmicSynth();

// --- STARFIELD INTERACTIVE ANIMATION ---
class Starfield {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        this.stars = [];
        this.maxStars = 80;
        this.resize();
        window.addEventListener('resize', () => this.resize());
        this.initStars();
    }

    resize() {
        this.canvas.width = window.innerWidth;
        this.canvas.height = window.innerHeight;
    }

    initStars() {
        this.stars = [];
        for (let i = 0; i < this.maxStars; i++) {
            this.stars.push({
                x: Math.random() * this.canvas.width,
                y: Math.random() * this.canvas.height,
                size: Math.random() * 1.5 + 0.5,
                color: Math.random() > 0.4 ? '#ffffff' : (Math.random() > 0.5 ? '#818cf8' : '#d946ef'),
                alpha: Math.random(),
                speed: Math.random() * 0.015 + 0.002
            });
        }
    }

    animate() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.stars.forEach(star => {
            star.alpha += star.speed;
            if (star.alpha > 1 || star.alpha < 0) {
                star.speed = -star.speed;
            }
            this.ctx.globalAlpha = Math.max(0.1, Math.min(1, star.alpha));
            this.ctx.fillStyle = star.color;
            this.ctx.beginPath();
            this.ctx.arc(star.x, star.y, star.size, 0, Math.PI * 2);
            this.ctx.fill();
        });
        this.ctx.globalAlpha = 1;
        requestAnimationFrame(() => this.animate());
    }
}

// --- PARTICLE BURSTS SYSTEM ---
class ParticleSystem {
    constructor() {
        this.particles = [];
    }

    spawnBurst(cellX, cellY, color) {
        const particleCount = 12;
        for (let i = 0; i < particleCount; i++) {
            const angle = Math.random() * Math.PI * 2;
            const velocity = Math.random() * 3 + 1.5;
            this.particles.push({
                x: cellX,
                y: cellY,
                vx: Math.cos(angle) * velocity,
                vy: Math.sin(angle) * velocity,
                alpha: 1,
                size: Math.random() * 4 + 2,
                color: color,
                decay: Math.random() * 0.03 + 0.015
            });
        }
    }

    updateAndDraw(ctx) {
        for (let i = this.particles.length - 1; i >= 0; i--) {
            const p = this.particles[i];
            p.x += p.vx;
            p.y += p.vy;
            p.alpha -= p.decay;
            if (p.alpha <= 0) {
                this.particles.splice(i, 1);
                continue;
            }
            ctx.globalAlpha = p.alpha;
            ctx.fillStyle = p.color;
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
            ctx.fill();
        }
        ctx.globalAlpha = 1.0;
    }
}

const PARTICLES = new ParticleSystem();

// --- THE PIECE FRAGMENTS LIBRARY ---
const PIECES_REPERTOIRE = [
    { matrix: [[1]], color: "#00e5ff" }, // 1x1 Cyan
    { matrix: [[1, 1]], color: "#ffd700" }, // 1x2 Gold
    { matrix: [[1], [1]], color: "#ffd700" }, // 2x1 Gold
    { matrix: [[1, 1, 1]], color: "#d946ef" }, // 1x3 Purple
    { matrix: [[1], [1], [1]], color: "#d946ef" }, // 3x1 Purple
    { matrix: [[1, 1], [1, 1]], color: "#818cf8" }, // 2x2 Indigo
    { matrix: [[1, 1, 1], [0, 1, 0]], color: "#e040fb" }, // T-shape Magenta
    { matrix: [[1, 0], [1, 0], [1, 1]], color: "#38bdf8" }, // L-shape Sky Blue
    { matrix: [[1, 1, 1], [1, 0, 0]], color: "#38bdf8" }, // Corner L
];

const KIDS_REPERTOIRE = [
    { matrix: [[1]], color: "#00e5ff" },
    { matrix: [[1, 1]], color: "#ffd700" },
    { matrix: [[1], [1]], color: "#ffd700" }
];

// --- CORE GAME CONTROLLER ---
class GameController {
    constructor() {
        this.gridCanvas = document.getElementById('game-grid-canvas');
        this.gridCtx = this.gridCanvas.getContext('2d');
        this.cellSize = 0;
        this.cellPadding = 3;
        this.activeOverlay = null;
        
        this.initLocalData();
        this.initDOM();
        this.setupGridDimensions();
        this.resetGrid();
    }

    initLocalData() {
        STATE.coins = parseInt(localStorage.getItem('nov_coins')) || 100;
        STATE.gems = parseInt(localStorage.getItem('nov_gems')) || 10;
        STATE.highscore = parseInt(localStorage.getItem('nov_highscore')) || 0;
        STATE.activeCampaignLevel = parseInt(localStorage.getItem('nov_level')) || 1;
        
        STATE.soundEnabled = localStorage.getItem('nov_snd') !== 'false';
        STATE.musicEnabled = localStorage.getItem('nov_msc') !== 'false';
        STATE.hapticEnabled = localStorage.getItem('nov_hpt') !== 'false';
        STATE.kidsModeEnabled = localStorage.getItem('nov_kids') === 'true';

        this.syncPills();
    }

    saveLocalData() {
        localStorage.setItem('nov_coins', STATE.coins);
        localStorage.setItem('nov_gems', STATE.gems);
        localStorage.setItem('nov_highscore', STATE.highscore);
        localStorage.setItem('nov_level', STATE.activeCampaignLevel);
        localStorage.setItem('nov_snd', STATE.soundEnabled);
        localStorage.setItem('nov_msc', STATE.musicEnabled);
        localStorage.setItem('nov_hpt', STATE.hapticEnabled);
        localStorage.setItem('nov_kids', STATE.kidsModeEnabled);
    }

    syncPills() {
        document.getElementById('menu-coins').innerText = STATE.coins;
        document.getElementById('menu-gems').innerText = STATE.gems;
        document.getElementById('menu-highscore').innerText = STATE.highscore;
        
        document.getElementById('hud-coins').innerText = STATE.coins;
        document.getElementById('hud-gems').innerText = STATE.gems;
        document.getElementById('hud-score').innerText = String(STATE.currentScore).padStart(5, '0');
        document.getElementById('hud-best-score').innerText = String(STATE.highscore).padStart(5, '0');

        document.getElementById('toggle-sound').checked = STATE.soundEnabled;
        document.getElementById('toggle-music').checked = STATE.musicEnabled;
        document.getElementById('toggle-haptic').checked = STATE.hapticEnabled;
        document.getElementById('toggle-kids').checked = STATE.kidsModeEnabled;
    }

    initDOM() {
        // Navigation Buttons
        const instantPlayBtn = document.getElementById('btn-menu-instant-play');
        if (instantPlayBtn) {
            instantPlayBtn.addEventListener('click', () => {
                SYNTH.init();
                this.startLoading('infinite');
            });
        }
        document.getElementById('btn-play-campaign').addEventListener('click', () => {
            SYNTH.init();
            this.startLoading('campaign');
        });
        document.getElementById('btn-play-infinite').addEventListener('click', () => {
            SYNTH.init();
            this.startLoading('infinite');
        });
        document.getElementById('btn-settings').addEventListener('click', () => this.switchScreen('screen-settings'));
        document.getElementById('btn-instructions').addEventListener('click', () => this.switchScreen('screen-instructions'));
        
        document.getElementById('btn-settings-back').addEventListener('click', () => this.switchScreen('screen-menu'));
        document.getElementById('btn-instructions-back').addEventListener('click', () => this.switchScreen('screen-menu'));
        document.getElementById('btn-game-home').addEventListener('click', () => {
            SYNTH.playClick();
            this.switchScreen('screen-menu');
        });

        // Config Controls
        document.getElementById('toggle-sound').addEventListener('change', (e) => {
            STATE.soundEnabled = e.target.checked;
            SYNTH.playClick();
            this.saveLocalData();
        });
        document.getElementById('toggle-music').addEventListener('change', (e) => {
            STATE.musicEnabled = e.target.checked;
            SYNTH.updateSettings();
            SYNTH.playClick();
            this.saveLocalData();
        });
        document.getElementById('toggle-haptic').addEventListener('change', (e) => {
            STATE.hapticEnabled = e.target.checked;
            SYNTH.playClick();
            this.saveLocalData();
        });
        document.getElementById('toggle-kids').addEventListener('change', (e) => {
            STATE.kidsModeEnabled = e.target.checked;
            SYNTH.playClick();
            this.saveLocalData();
        });

        document.getElementById('btn-reset-data').addEventListener('click', () => {
            if (confirm("Reset all cosmic progression? This is permanent.")) {
                localStorage.clear();
                this.initLocalData();
                SYNTH.playClick();
                this.switchScreen('screen-menu');
            }
        });

        // Dialogue Actions
        document.getElementById('btn-go-restart').addEventListener('click', () => {
            document.getElementById('dialog-gameover').classList.remove('active');
            this.startLoading(STATE.gameMode);
        });
        document.getElementById('btn-go-menu').addEventListener('click', () => {
            document.getElementById('dialog-gameover').classList.remove('active');
            this.switchScreen('screen-menu');
        });
        document.getElementById('btn-win-next').addEventListener('click', () => {
            document.getElementById('dialog-win').classList.remove('active');
            this.startLoading('campaign');
        });
        document.getElementById('btn-win-menu').addEventListener('click', () => {
            document.getElementById('dialog-win').classList.remove('active');
            this.switchScreen('screen-menu');
        });

        // Setup Interaction Events for Grid and Pieces Touch Dragging
        this.setupDraggingEvents();
    }

    switchScreen(screenId) {
        SYNTH.playClick();
        document.querySelectorAll('.app-screen').forEach(s => s.classList.remove('active'));
        document.getElementById(screenId).classList.add('active');
    }

    startLoading(gameMode) {
        SYNTH.playClick();
        const overlay = document.getElementById('loading-overlay');
        overlay.classList.remove('loading-overlay-hidden');
        
        setTimeout(() => {
            overlay.classList.add('loading-overlay-hidden');
            this.initGameplay(gameMode);
        }, 1200); // Atmospheric cosmic assembly delay
    }

    initGameplay(mode) {
        STATE.gameMode = mode;
        STATE.currentScore = 0;
        this.resetGrid();
        
        const objBar = document.getElementById('game-objective-container');
        if (mode === 'campaign') {
            const levelInfo = STATE.campaignObjectives[STATE.activeCampaignLevel] || STATE.campaignObjectives[5];
            document.getElementById('game-objective-text').innerText = `Target: Reach ${levelInfo.targetScore} energy units.`;
            document.getElementById('game-objective-progress').innerText = `0 / ${levelInfo.targetScore}`;
            objBar.style.display = 'flex';
        } else {
            objBar.style.display = 'none';
        }

        this.generateNewPieces();
        this.syncPills();
        this.switchScreen('screen-gameplay');
        
        // Begin Canvas Paint Tick Loop
        this.tick();
    }

    resetGrid() {
        STATE.grid = [];
        for (let r = 0; r < 8; r++) {
            STATE.grid.push(new Array(8).fill(0));
        }
    }

    setupGridDimensions() {
        const dpr = window.devicePixelRatio || 1;
        const rect = this.gridCanvas.getBoundingClientRect();
        this.gridCanvas.width = rect.width * dpr;
        this.gridCanvas.height = rect.height * dpr;
        this.gridCtx.scale(dpr, dpr);
        
        this.cellSize = (rect.width - (this.cellPadding * 9)) / 8;
    }

    generateNewPieces() {
        const repo = STATE.kidsModeEnabled ? KIDS_REPERTOIRE : PIECES_REPERTOIRE;
        for (let i = 0; i < 3; i++) {
            const randomPick = repo[Math.floor(Math.random() * repo.length)];
            STATE.nextPieces[i] = JSON.parse(JSON.stringify(randomPick)); // Deep clone
        }
        this.renderPiecesDock();
    }

    renderPiecesDock() {
        const slots = [
            document.getElementById('slot-0'),
            document.getElementById('slot-1'),
            document.getElementById('slot-2')
        ];

        slots.forEach((slot, idx) => {
            slot.innerHTML = '';
            const piece = STATE.nextPieces[idx];
            if (!piece) {
                slot.classList.add('empty');
                return;
            }
            slot.classList.remove('empty');

            // Draw miniature preview canvas of the block fragment inside slot
            const canvas = document.createElement('canvas');
            canvas.width = 72;
            canvas.height = 72;
            slot.appendChild(canvas);

            const ctx = canvas.getContext('2d');
            const rows = piece.matrix.length;
            const cols = piece.matrix[0].length;
            const miniCellSize = Math.min(60 / cols, 60 / rows, 18);
            const startX = (72 - (cols * miniCellSize)) / 2;
            const startY = (72 - (rows * miniCellSize)) / 2;

            for (let r = 0; r < rows; r++) {
                for (let c = 0; c < cols; c++) {
                    if (piece.matrix[r][c] === 1) {
                        ctx.fillStyle = piece.color;
                        ctx.strokeStyle = "rgba(255,255,255,0.4)";
                        ctx.lineWidth = 1;
                        
                        // Drawing cell
                        ctx.beginPath();
                        ctx.roundRect(
                            startX + c * miniCellSize, 
                            startY + r * miniCellSize, 
                            miniCellSize - 1, 
                            miniCellSize - 1, 
                            3
                        );
                        ctx.fill();
                        ctx.stroke();
                    }
                }
            }
        });
    }

    setupDraggingEvents() {
        const slots = document.querySelectorAll('.dock-slot');
        
        const handleDragStart = (e, index) => {
            if (!STATE.nextPieces[index]) return;
            SYNTH.init();
            
            const piece = STATE.nextPieces[index];
            STATE.selectedPieceIndex = index;
            STATE.isDragging = true;

            const rect = e.currentTarget.getBoundingClientRect();
            let clientX, clientY;
            if (e.touches) {
                clientX = e.touches[0].clientX;
                clientY = e.touches[0].clientY;
            } else {
                clientX = e.clientX;
                clientY = e.clientY;
            }

            STATE.selectedPieceOffset = {
                x: clientX - rect.left,
                y: clientY - rect.top
            };
            STATE.dragPosition = { x: clientX, y: clientY };
            
            if (STATE.hapticEnabled && window.navigator.vibrate) {
                window.navigator.vibrate(15);
            }
        };

        slots.forEach(slot => {
            const index = parseInt(slot.dataset.index);
            slot.addEventListener('mousedown', (e) => handleDragStart(e, index));
            slot.addEventListener('touchstart', (e) => handleDragStart(e, index), { passive: true });
        });

        // Global move & drop events
        const handleDragMove = (e) => {
            if (!STATE.isDragging) return;
            let clientX, clientY;
            if (e.touches) {
                clientX = e.touches[0].clientX;
                clientY = e.touches[0].clientY;
            } else {
                clientX = e.clientX;
                clientY = e.clientY;
            }
            STATE.dragPosition = { x: clientX, y: clientY };
        };

        const handleDragEnd = () => {
            if (!STATE.isDragging) return;
            STATE.isDragging = false;
            
            // Try matching drop position to grid
            const success = this.attemptPiecePlacement();
            if (success) {
                SYNTH.playDrop();
                STATE.nextPieces[STATE.selectedPieceIndex] = null;
                this.renderPiecesDock();
                
                // If all 3 slots cleared, generate next round
                if (STATE.nextPieces.every(p => p === null)) {
                    this.generateNewPieces();
                }

                this.checkPostPlacementMatrices();
            } else {
                // Failed, snap back
                if (STATE.hapticEnabled && window.navigator.vibrate) {
                    window.navigator.vibrate(20);
                }
            }
            
            STATE.selectedPieceIndex = -1;
        };

        window.addEventListener('mousemove', handleDragMove);
        window.addEventListener('touchmove', handleDragMove, { passive: true });
        window.addEventListener('mouseup', handleDragEnd);
        window.addEventListener('touchend', handleDragEnd);
    }

    attemptPiecePlacement() {
        const piece = STATE.nextPieces[STATE.selectedPieceIndex];
        if (!piece) return false;

        const gridRect = this.gridCanvas.getBoundingClientRect();
        // Shift mouse center cursor tracking slightly higher on mobile touch to avoid finger occlusion
        const trackingOffsetY = STATE.selectedPieceOffset.y + 35;
        
        const dropX = STATE.dragPosition.x - gridRect.left - STATE.selectedPieceOffset.x;
        const dropY = STATE.dragPosition.y - gridRect.top - trackingOffsetY;

        // Map drop position coordinate to grid cells
        const dropCol = Math.round(dropX / (this.cellSize + this.cellPadding));
        const dropRow = Math.round(dropY / (this.cellSize + this.cellPadding));

        // Placement checks
        if (this.canPlacePiece(piece, dropRow, dropCol)) {
            const rows = piece.matrix.length;
            const cols = piece.matrix[0].length;

            for (let r = 0; r < rows; r++) {
                for (let c = 0; c < cols; c++) {
                    if (piece.matrix[r][c] === 1) {
                        STATE.grid[dropRow + r][dropCol + c] = piece.color;
                    }
                }
            }
            
            // Add initial placement energy points
            const squareCount = piece.matrix.flat().filter(v => v === 1).length;
            STATE.currentScore += squareCount * 10;
            return true;
        }

        return false;
    }

    canPlacePiece(piece, startRow, startCol) {
        const rows = piece.matrix.length;
        const cols = piece.matrix[0].length;

        if (startRow < 0 || startCol < 0 || startRow + rows > 8 || startCol + cols > 8) {
            return false;
        }

        for (let r = 0; r < rows; r++) {
            for (let c = 0; c < cols; c++) {
                if (piece.matrix[r][c] === 1) {
                    if (STATE.grid[startRow + r][startCol + c] !== 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    checkPostPlacementMatrices() {
        let fullRows = [];
        let fullCols = [];

        // Identify complete rows
        for (let r = 0; r < 8; r++) {
            if (STATE.grid[r].every(cell => cell !== 0)) {
                fullRows.push(r);
            }
        }

        // Identify complete columns
        for (let c = 0; c < 8; c++) {
            let full = true;
            for (let r = 0; r < 8; r++) {
                if (STATE.grid[r][c] === 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                fullCols.push(c);
            }
        }

        // Dissolve lattices & trigger particle bursts
        if (fullRows.length > 0 || fullCols.length > 0) {
            SYNTH.playClear();
            
            // Spawn bursts on cell centers
            const gridRect = this.gridCanvas.getBoundingClientRect();
            
            fullRows.forEach(r => {
                for (let c = 0; c < 8; c++) {
                    const color = STATE.grid[r][c];
                    const cx = c * (this.cellSize + this.cellPadding) + this.cellSize/2 + this.cellPadding;
                    const cy = r * (this.cellSize + this.cellPadding) + this.cellSize/2 + this.cellPadding;
                    PARTICLES.spawnBurst(cx, cy, color);
                    STATE.grid[r][c] = 0;
                }
            });

            fullCols.forEach(c => {
                for (let r = 0; r < 8; r++) {
                    const color = STATE.grid[r][c];
                    if (color !== 0) { // Might have already been cleared by rows
                        const cx = c * (this.cellSize + this.cellPadding) + this.cellSize/2 + this.cellPadding;
                        const cy = r * (this.cellSize + this.cellPadding) + this.cellSize/2 + this.cellPadding;
                        PARTICLES.spawnBurst(cx, cy, color);
                        STATE.grid[r][c] = 0;
                    }
                }
            });

            // Reward multipliers for multiple cleared lines
            const linesCleared = fullRows.length + fullCols.length;
            const pointsReward = linesCleared * 120 + (linesCleared > 1 ? linesCleared * 60 : 0);
            STATE.currentScore += pointsReward;
            
            // Synthesize resources
            STATE.coins += linesCleared * 5;
            if (linesCleared > 1) {
                STATE.gems += linesCleared - 1;
            }
            
            if (STATE.hapticEnabled && window.navigator.vibrate) {
                window.navigator.vibrate([40, 30, 60]);
            }
        }

        // Sync local storage and best highscores
        if (STATE.currentScore > STATE.highscore) {
            STATE.highscore = STATE.currentScore;
        }
        
        this.saveLocalData();
        this.syncPills();

        // Check Campaign Target Success Conditions
        if (STATE.gameMode === 'campaign') {
            const levelTarget = STATE.campaignObjectives[STATE.activeCampaignLevel] || STATE.campaignObjectives[5];
            document.getElementById('game-objective-progress').innerText = `${STATE.currentScore} / ${levelTarget.targetScore}`;
            
            if (STATE.currentScore >= levelTarget.targetScore) {
                this.triggerCampaignWin();
                return;
            }
        }

        // Verify remaining playable combinations
        if (!this.anyValidMovesRemaining()) {
            this.triggerGameOver();
        }
    }

    anyValidMovesRemaining() {
        for (let idx = 0; idx < 3; idx++) {
            const piece = STATE.nextPieces[idx];
            if (!piece) continue;

            // Search whole 8x8 matrix for at least one fit
            for (let r = 0; r < 8; r++) {
                for (let c = 0; c < 8; c++) {
                    if (this.canPlacePiece(piece, r, c)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    triggerCampaignWin() {
        STATE.coins += 50;
        STATE.gems += 5;
        if (STATE.activeCampaignLevel < 5) {
            STATE.activeCampaignLevel += 1;
        }
        this.saveLocalData();
        
        setTimeout(() => {
            document.getElementById('dialog-win').classList.add('active');
        }, 800);
    }

    triggerGameOver() {
        SYNTH.playGameOver();
        // Final coin dividend
        const awardedCoins = Math.floor(STATE.currentScore / 15);
        const awardedGems = Math.floor(STATE.currentScore / 250);
        STATE.coins += awardedCoins;
        STATE.gems += awardedGems;
        this.saveLocalData();

        document.getElementById('go-score').innerText = STATE.currentScore;
        document.getElementById('go-coins').innerText = awardedCoins;
        document.getElementById('go-gems').innerText = awardedGems;

        setTimeout(() => {
            document.getElementById('dialog-gameover').classList.add('active');
        }, 800);
    }

    tick() {
        // Stop animating if gameplay screen not active
        if (!document.getElementById('screen-gameplay').classList.contains('active')) return;

        this.drawGrid();
        PARTICLES.updateAndDraw(this.gridCtx);

        // Render dragging placeholder ghost
        if (STATE.isDragging && STATE.selectedPieceIndex !== -1) {
            this.drawDraggingGhost();
        }

        requestAnimationFrame(() => this.tick());
    }

    drawGrid() {
        this.gridCtx.clearRect(0, 0, this.gridCanvas.width, this.gridCanvas.height);
        
        for (let r = 0; r < 8; r++) {
            for (let c = 0; c < 8; c++) {
                const cellVal = STATE.grid[r][c];
                const x = c * (this.cellSize + this.cellPadding) + this.cellPadding;
                const y = r * (this.cellSize + this.cellPadding) + this.cellPadding;

                if (cellVal !== 0) {
                    this.gridCtx.fillStyle = cellVal;
                    this.gridCtx.strokeStyle = "rgba(255,255,255,0.25)";
                    this.gridCtx.lineWidth = 1;
                    this.gridCtx.beginPath();
                    this.gridCtx.roundRect(x, y, this.cellSize, this.cellSize, 6);
                    this.gridCtx.fill();
                    this.gridCtx.stroke();
                } else {
                    // Draw subtle vacant grid matrix node
                    this.gridCtx.fillStyle = "rgba(22, 19, 44, 0.4)";
                    this.gridCtx.strokeStyle = "rgba(255, 255, 255, 0.04)";
                    this.gridCtx.lineWidth = 1;
                    this.gridCtx.beginPath();
                    this.gridCtx.roundRect(x, y, this.cellSize, this.cellSize, 6);
                    this.gridCtx.fill();
                    this.gridCtx.stroke();
                }
            }
        }
    }

    drawDraggingGhost() {
        const piece = STATE.nextPieces[STATE.selectedPieceIndex];
        if (!piece) return;

        const gridRect = this.gridCanvas.getBoundingClientRect();
        const trackingOffsetY = STATE.selectedPieceOffset.y + 35; // Lift up finger preview
        
        const dragX = STATE.dragPosition.x - gridRect.left - STATE.selectedPieceOffset.x;
        const dragY = STATE.dragPosition.y - gridRect.top - trackingOffsetY;

        const rows = piece.matrix.length;
        const cols = piece.matrix[0].length;

        // Grid snapped position projection
        const dropCol = Math.round(dragX / (this.cellSize + this.cellPadding));
        const dropRow = Math.round(dragY / (this.cellSize + this.cellPadding));
        const canFit = this.canPlacePiece(piece, dropRow, dropCol);

        // 1. Draw glowing snapping projection block on the actual grid
        if (canFit) {
            for (let r = 0; r < rows; r++) {
                for (let c = 0; c < cols; c++) {
                    if (piece.matrix[r][c] === 1) {
                        const px = (dropCol + c) * (this.cellSize + this.cellPadding) + this.cellPadding;
                        const py = (dropRow + r) * (this.cellSize + this.cellPadding) + this.cellPadding;
                        
                        this.gridCtx.fillStyle = piece.color;
                        this.gridCtx.globalAlpha = 0.4;
                        this.gridCtx.beginPath();
                        this.gridCtx.roundRect(px, py, this.cellSize, this.cellSize, 6);
                        this.gridCtx.fill();
                        
                        // Golden aura projection trim
                        this.gridCtx.strokeStyle = "rgba(255, 215, 0, 0.6)";
                        this.gridCtx.lineWidth = 1.5;
                        this.gridCtx.stroke();
                    }
                }
            }
            this.gridCtx.globalAlpha = 1.0;
        }

        // 2. Draw floating cursor block under finger
        for (let r = 0; r < rows; r++) {
            for (let c = 0; c < cols; c++) {
                if (piece.matrix[r][c] === 1) {
                    const cx = dragX + c * (this.cellSize + this.cellPadding);
                    const cy = dragY + r * (this.cellSize + this.cellPadding);
                    
                    this.gridCtx.fillStyle = piece.color;
                    this.gridCtx.strokeStyle = "rgba(255, 255, 255, 0.4)";
                    this.gridCtx.lineWidth = 1.2;
                    this.gridCtx.shadowColor = piece.color;
                    this.gridCtx.shadowBlur = 10;
                    
                    this.gridCtx.beginPath();
                    this.gridCtx.roundRect(cx, cy, this.cellSize, this.cellSize, 6);
                    this.gridCtx.fill();
                    this.gridCtx.stroke();
                    
                    // Clear shadow metrics
                    this.gridCtx.shadowColor = "transparent";
                    this.gridCtx.shadowBlur = 0;
                }
            }
        }
    }
}

// --- INIT APP ON WINDOW LOAD ---
let deferredPrompt = null;

// PWA Chrome Installation Interceptor
window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPrompt = e;
    
    const splashInstallBtn = document.getElementById('btn-pwa-install-splash');
    const menuInstallBtn = document.getElementById('btn-pwa-install-menu');
    if (splashInstallBtn) splashInstallBtn.classList.remove('hidden');
    if (menuInstallBtn) menuInstallBtn.classList.remove('hidden');
});

const triggerPwaInstall = async () => {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    console.log('User PWA install choice:', outcome);
    deferredPrompt = null;
    const splashInstallBtn = document.getElementById('btn-pwa-install-splash');
    const menuInstallBtn = document.getElementById('btn-pwa-install-menu');
    if (splashInstallBtn) splashInstallBtn.classList.add('hidden');
    if (menuInstallBtn) menuInstallBtn.classList.add('hidden');
};

window.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Starfield Animation Background
    const stars = new Starfield('starfield-canvas');
    stars.animate();

    // 2. Initialize Game Coordinator
    const game = new GameController();

    // 3. PWA Install Event Listeners
    document.getElementById('btn-pwa-install-splash')?.addEventListener('click', triggerPwaInstall);
    document.getElementById('btn-pwa-install-menu')?.addEventListener('click', triggerPwaInstall);

    // 4. Play Now Launch Handler from Splash Screen
    const btnSplashPlay = document.getElementById('btn-splash-play');
    const splashScreen = document.getElementById('screen-splash');
    const menuScreen = document.getElementById('screen-menu');

    const launchGameFromSplash = () => {
        SYNTH.init();
        if (splashScreen) splashScreen.classList.remove('active');
        if (menuScreen) menuScreen.classList.add('active');
    };

    if (btnSplashPlay) {
        btnSplashPlay.addEventListener('click', launchGameFromSplash);
    }

    // 5. Simulated Splash Loader bar progression
    let progress = 0;
    const splashProgress = document.getElementById('splash-progress');
    const splashStatus = document.getElementById('splash-status');

    const progressInterval = setInterval(() => {
        progress += Math.random() * 20 + 10;
        if (progress >= 100) {
            progress = 100;
            clearInterval(progressInterval);
            if (splashStatus) splashStatus.innerText = "Stellar Lattice Ready! Tap PLAY NOW to start.";
        }
        if (splashProgress) splashProgress.style.width = `${progress}%`;
    }, 120);
});
