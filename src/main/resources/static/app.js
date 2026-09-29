// ExamCraft Frontend Application Logic
const API_BASE = '/api';

let currentTab = 'dashboard';
let allUnits = [];
let allQuestions = [];
let allTestPapers = [];
let activeExamPaper = null;

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
    loadDashboard();
    loadUnits();
    loadQuestions();
    loadTestPapers();
    loadUsageFrequency();
    loadAttempts();
    updateDistributionCounts();
});

// Toast notification helper
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
    toast.innerHTML = `<span>${icon}</span> <div>${message}</div>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Navigation Handling
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const tabId = item.getAttribute('data-tab');
            switchTab(tabId);
        });
    });
}

function switchTab(tabId) {
    currentTab = tabId;

    // Update active nav button
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
    });

    // Update active tab section
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.toggle('active', tab.id === `tab-${tabId}`);
    });

    // Update header title
    const titles = {
        'dashboard': { title: 'System Overview', sub: 'Real-time statistics and automated question-paper management' },
        'generator': { title: 'Question Paper Generator', sub: 'Generate randomized papers matching requested difficulty distribution' },
        'questions': { title: 'Question Bank', sub: 'Manage syllabus questions tagged by Unit, Topic, and Difficulty' },
        'units': { title: 'Course Units', sub: 'Configure course modules and syllabus structure' },
        'test-papers': { title: 'Generated Test Papers', sub: 'Archive of all auto-generated, duplicate-free exam papers' },
        'exam-room': { title: 'Student Exam Room', sub: 'Simulate student examination with real-time objective auto-grading' },
        'usage-analytics': { title: 'Question Usage Frequency', sub: 'Audit how often questions appear across generated test papers' },
        'attempts': { title: 'Student Attempts & Scores', sub: 'Review completed submissions, scores, and evaluation summaries' }
    };

    if (titles[tabId]) {
        document.getElementById('pageTitle').textContent = titles[tabId].title;
        document.getElementById('pageSubtitle').textContent = titles[tabId].sub;
    }

    refreshCurrentTab();
}

function refreshCurrentTab() {
    if (currentTab === 'dashboard') loadDashboard();
    else if (currentTab === 'questions') loadQuestions();
    else if (currentTab === 'units') loadUnits();
    else if (currentTab === 'test-papers') loadTestPapers();
    else if (currentTab === 'usage-analytics') loadUsageFrequency();
    else if (currentTab === 'attempts') loadAttempts();
    else if (currentTab === 'exam-room') populateExamPaperDropdown();
}

// 1. Dashboard Data
async function loadDashboard() {
    try {
        const res = await fetch(`${API_BASE}/dashboard/stats`);
        if (!res.ok) throw new Error('Failed to load stats');
        const stats = await res.json();

        document.getElementById('statQuestions').textContent = stats.totalQuestions || 0;
        document.getElementById('statUnits').textContent = stats.totalUnits || 0;
        document.getElementById('statPapers').textContent = stats.totalTestPapers || 0;
        document.getElementById('statAttempts').textContent = stats.totalAttempts || 0;

        document.getElementById('statDifficultySplit').textContent =
            `${stats.easyCount || 0} Easy • ${stats.mediumCount || 0} Med • ${stats.hardCount || 0} Hard`;
        document.getElementById('statAvgScore').textContent = `Avg Score: ${stats.averageScore || 0}%`;

        loadRecentPapers();
    } catch (err) {
        console.error('Error loading dashboard stats:', err);
    }
}

async function loadRecentPapers() {
    try {
        const res = await fetch(`${API_BASE}/test-papers`);
        if (!res.ok) return;
        const papers = await res.json();
        const listContainer = document.getElementById('recentPapersList');

        if (!papers || papers.length === 0) {
            listContainer.innerHTML = '<p class="text-muted" style="color: var(--text-muted); font-size: 0.9rem;">No test papers generated yet. Click "Generate Test Paper" to create one!</p>';
            return;
        }

        const recent = papers.slice(0, 4);
        listContainer.innerHTML = recent.map(p => `
            <div class="paper-item-mini">
                <div>
                    <h4>${escapeHtml(p.title)}</h4>
                    <p>${p.totalQuestions} Questions • Easy: ${p.easyCount || 0}, Med: ${p.mediumCount || 0}, Hard: ${p.hardCount || 0}</p>
                </div>
                <button class="btn btn-sm btn-outline" onclick="viewPaperDetails(${p.id})">Inspect</button>
            </div>
        `).join('');
    } catch (err) {
        console.error('Error loading recent papers:', err);
    }
}

// 2. Units Management
async function loadUnits() {
    try {
        const res = await fetch(`${API_BASE}/units`);
        if (!res.ok) throw new Error('Failed to fetch units');
        allUnits = await res.json();

        // Render Unit Cards
        const grid = document.getElementById('unitsGrid');
        if (grid) {
            grid.innerHTML = allUnits.map(u => `
                <div class="unit-card">
                    <div>
                        <h4>${escapeHtml(u.name)}</h4>
                        <p>${escapeHtml(u.description || 'No description provided.')}</p>
                    </div>
                    <div class="unit-card-footer">
                        <span>Unit ID: #${u.id}</span>
                        <button class="btn btn-sm btn-outline" onclick="filterByUnit(${u.id})">View Questions</button>
                    </div>
                </div>
            `).join('');
        }

        // Populate unit dropdowns
        populateUnitDropdowns();
    } catch (err) {
        console.error('Error loading units:', err);
    }
}

function populateUnitDropdowns() {
    const qUnitSelect = document.getElementById('qUnit');
    const filterUnitSelect = document.getElementById('filterQuestionUnit');
    const genUnitSelect = document.getElementById('genUnit');

    const optionsHtml = allUnits.map(u => `<option value="${u.id}">${escapeHtml(u.name)}</option>`).join('');

    if (qUnitSelect) {
        qUnitSelect.innerHTML = '<option value="">-- Select Unit --</option>' + optionsHtml;
    }
    if (filterUnitSelect) {
        const currentVal = filterUnitSelect.value;
        filterUnitSelect.innerHTML = '<option value="">All Units</option>' + optionsHtml;
        filterUnitSelect.value = currentVal;
    }
    if (genUnitSelect) {
        const currentVal = genUnitSelect.value;
        genUnitSelect.innerHTML = '<option value="">All Units (Full Syllabus)</option>' + optionsHtml;
        genUnitSelect.value = currentVal;
    }
}

async function handleAddUnit(e) {
    e.preventDefault();
    const name = document.getElementById('unitName').value.trim();
    const description = document.getElementById('unitDesc').value.trim();

    try {
        const res = await fetch(`${API_BASE}/units`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, description })
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to create unit');
        }

        showToast(`Unit '${name}' created successfully!`, 'success');
        closeModal('addUnitModal');
        document.getElementById('addUnitForm').reset();
        loadUnits();
        loadDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function filterByUnit(unitId) {
    switchTab('questions');
    const select = document.getElementById('filterQuestionUnit');
    if (select) {
        select.value = unitId;
        loadQuestions();
    }
}

// 3. Question Bank Management
async function loadQuestions() {
    const unitId = document.getElementById('filterQuestionUnit')?.value;
    const diff = document.getElementById('filterQuestionDiff')?.value;

    let url = `${API_BASE}/questions`;
    const params = [];
    if (unitId) params.push(`unitId=${unitId}`);
    if (diff) params.push(`difficulty=${diff}`);
    if (params.length > 0) url += `?${params.join('&')}`;

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error('Failed to fetch questions');
        allQuestions = await res.json();
        renderQuestionsList(allQuestions);
    } catch (err) {
        console.error('Error loading questions:', err);
    }
}

function renderQuestionsList(questions) {
    const container = document.getElementById('questionsContainer');
    if (!container) return;

    if (!questions || questions.length === 0) {
        container.innerHTML = '<div class="card" style="text-align: center; color: var(--text-muted); padding: 40px;">No questions found matching your filter criteria.</div>';
        return;
    }

    container.innerHTML = questions.map(q => {
        const diffClass = q.difficulty ? q.difficulty.toLowerCase() : 'medium';
        const unitName = q.unit ? q.unit.name : 'Unassigned';

        return `
            <div class="question-card" data-qid="${q.id}">
                <div class="question-meta">
                    <div class="tags-row">
                        <span class="diff-tag ${diffClass}">${q.difficulty}</span>
                        <span class="unit-tag">${escapeHtml(unitName)}</span>
                        <span class="topic-tag">${escapeHtml(q.topic || 'General')}</span>
                    </div>
                    <button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="deleteQuestion(${q.id})">Delete</button>
                </div>
                <div class="question-text">${escapeHtml(q.questionText)}</div>
                <div class="options-preview-grid">
                    <div class="opt-box ${q.correctAnswer === 'A' ? 'correct' : ''}">
                        <strong>A:</strong> ${escapeHtml(q.optionA)}
                    </div>
                    <div class="opt-box ${q.correctAnswer === 'B' ? 'correct' : ''}">
                        <strong>B:</strong> ${escapeHtml(q.optionB)}
                    </div>
                    <div class="opt-box ${q.correctAnswer === 'C' ? 'correct' : ''}">
                        <strong>C:</strong> ${escapeHtml(q.optionC)}
                    </div>
                    <div class="opt-box ${q.correctAnswer === 'D' ? 'correct' : ''}">
                        <strong>D:</strong> ${escapeHtml(q.optionD)}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function filterQuestionsClient() {
    const term = document.getElementById('searchQuestionText').value.toLowerCase();
    const filtered = allQuestions.filter(q =>
        q.questionText.toLowerCase().includes(term) ||
        (q.topic && q.topic.toLowerCase().includes(term))
    );
    renderQuestionsList(filtered);
}

async function handleAddQuestion(e) {
    e.preventDefault();
    const unitId = document.getElementById('qUnit').value;
    const difficulty = document.getElementById('qDifficulty').value;
    const topic = document.getElementById('qTopic').value.trim();
    const questionText = document.getElementById('qText').value.trim();
    const optionA = document.getElementById('qOptionA').value.trim();
    const optionB = document.getElementById('qOptionB').value.trim();
    const optionC = document.getElementById('qOptionC').value.trim();
    const optionD = document.getElementById('qOptionD').value.trim();
    const correctAnswer = document.getElementById('qCorrectAnswer').value;

    const payload = {
        unitId: Number(unitId),
        difficulty,
        topic,
        questionText,
        optionA,
        optionB,
        optionC,
        optionD,
        correctAnswer
    };

    try {
        const res = await fetch(`${API_BASE}/questions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to add question');
        }

        showToast('Question successfully added to question bank!', 'success');
        closeModal('addQuestionModal');
        document.getElementById('addQuestionForm').reset();
        loadQuestions();
        loadDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteQuestion(id) {
    if (!confirm('Are you sure you want to delete this question?')) return;
    try {
        const res = await fetch(`${API_BASE}/questions/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete question');
        showToast('Question deleted', 'info');
        loadQuestions();
        loadDashboard();
        loadUsageFrequency();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// 4. Test Paper Generator Logic & Slider Sync
function syncSliders(source) {
    const easyInput = document.getElementById('genEasyPct');
    const medInput = document.getElementById('genMedPct');
    const hardInput = document.getElementById('genHardPct');

    let easy = parseInt(easyInput.value) || 0;
    let med = parseInt(medInput.value) || 0;
    let hard = parseInt(hardInput.value) || 0;

    let total = easy + med + hard;
    const badge = document.getElementById('distroSumBadge');

    if (total === 100) {
        badge.className = 'badge badge-success';
        badge.textContent = 'Sum: 100% (Valid)';
    } else {
        badge.className = 'badge badge-warning';
        badge.textContent = `Sum: ${total}% (Must be 100%)`;
    }

    document.getElementById('easyPctLabel').textContent = `${easy}%`;
    document.getElementById('medPctLabel').textContent = `${med}%`;
    document.getElementById('hardPctLabel').textContent = `${hard}%`;

    updateDistributionCounts();
}

function updateDistributionCounts() {
    const totalQs = parseInt(document.getElementById('genTotalQuestions').value) || 10;
    const easy = parseInt(document.getElementById('genEasyPct').value) || 40;
    const med = parseInt(document.getElementById('genMedPct').value) || 40;
    const hard = parseInt(document.getElementById('genHardPct').value) || 20;

    const estEasy = Math.round((easy * totalQs) / 100);
    const estMed = Math.round((med * totalQs) / 100);
    const estHard = Math.max(0, totalQs - estEasy - estMed);

    document.getElementById('easyCountEst').textContent = estEasy;
    document.getElementById('medCountEst').textContent = estMed;
    document.getElementById('hardCountEst').textContent = estHard;
}

async function handleGeneratePaper(e) {
    e.preventDefault();

    const title = document.getElementById('genTitle').value.trim();
    const totalQuestions = parseInt(document.getElementById('genTotalQuestions').value);
    const unitId = document.getElementById('genUnit').value;
    const description = document.getElementById('genDescription').value.trim();
    const easyPercentage = parseInt(document.getElementById('genEasyPct').value);
    const mediumPercentage = parseInt(document.getElementById('genMedPct').value);
    const hardPercentage = parseInt(document.getElementById('genHardPct').value);

    if (easyPercentage + mediumPercentage + hardPercentage !== 100) {
        showToast(`Percentages must sum to 100%! Current sum: ${easyPercentage + mediumPercentage + hardPercentage}%`, 'error');
        return;
    }

    const payload = {
        title,
        totalQuestions,
        easyPercentage,
        mediumPercentage,
        hardPercentage,
        unitId: unitId ? Number(unitId) : null,
        description
    };

    const submitBtn = document.getElementById('btnGenerateSubmit');
    submitBtn.disabled = true;
    submitBtn.textContent = '🎲 Randomizing & Generating...';

    try {
        const res = await fetch(`${API_BASE}/test-papers/generate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Generation failed');
        }

        const generatedPaper = await res.json();
        showToast(`🎉 Test Paper '${generatedPaper.title}' generated successfully!`, 'success');

        renderGeneratedPreview(generatedPaper);
        loadTestPapers();
        loadDashboard();
        loadUsageFrequency();
        populateExamPaperDropdown();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = '⚡ Generate Test Paper Now';
    }
}

function renderGeneratedPreview(paper) {
    const previewDiv = document.getElementById('generatedPaperPreview');
    previewDiv.classList.remove('hidden');

    previewDiv.innerHTML = `
        <div class="card-header">
            <div>
                <h3>✅ Generated Paper: ${escapeHtml(paper.title)}</h3>
                <p class="card-desc">Total: ${paper.totalQuestions} Questions • Distribution: Easy: ${paper.easyCount}, Med: ${paper.mediumCount}, Hard: ${paper.hardCount}</p>
            </div>
            <div style="display: flex; gap: 8px;">
                <button class="btn btn-outline btn-sm" onclick="printTestPaper()">🖨️ Print Paper</button>
                <button class="btn btn-primary btn-sm" onclick="startExamWithPaper(${paper.id})">Take This Exam ✍️</button>
            </div>
        </div>
        <div class="questions-list">
            ${paper.questions.map((q, idx) => `
                <div class="question-card" style="border-left: 4px solid var(--primary);">
                    <div class="question-meta">
                        <div class="tags-row">
                            <span class="badge badge-info">Q${idx + 1}</span>
                            <span class="diff-tag ${q.difficulty.toLowerCase()}">${q.difficulty}</span>
                            <span class="unit-tag">${escapeHtml(q.unit?.name || '')}</span>
                            <span class="topic-tag">${escapeHtml(q.topic)}</span>
                        </div>
                    </div>
                    <div class="question-text">${escapeHtml(q.questionText)}</div>
                    <div class="options-preview-grid">
                        <div class="opt-box"><strong>A:</strong> ${escapeHtml(q.optionA)}</div>
                        <div class="opt-box"><strong>B:</strong> ${escapeHtml(q.optionB)}</div>
                        <div class="opt-box"><strong>C:</strong> ${escapeHtml(q.optionC)}</div>
                        <div class="opt-box"><strong>D:</strong> ${escapeHtml(q.optionD)}</div>
                    </div>
                </div>
            `).join('')}
        </div>
    `;

    previewDiv.scrollIntoView({ behavior: 'smooth' });
}

// 5. Test Papers List & Details
async function loadTestPapers() {
    try {
        const res = await fetch(`${API_BASE}/test-papers`);
        if (!res.ok) throw new Error('Failed to fetch test papers');
        allTestPapers = await res.json();

        const container = document.getElementById('testPapersContainer');
        if (!container) return;

        if (!allTestPapers || allTestPapers.length === 0) {
            container.innerHTML = '<div class="card" style="text-align: center; color: var(--text-muted); padding: 40px; grid-column: 1 / -1;">No test papers generated yet. Click "+ Generate New Paper" above!</div>';
            return;
        }

        container.innerHTML = allTestPapers.map(p => `
            <div class="test-paper-card">
                <div>
                    <div class="test-paper-header">
                        <h3>${escapeHtml(p.title)}</h3>
                        <p>${escapeHtml(p.description || 'Auto-generated assessment')}</p>
                    </div>
                    <div class="paper-distribution-pills">
                        <span class="diff-tag easy">Easy: ${p.easyCount || 0}</span>
                        <span class="diff-tag medium">Med: ${p.mediumCount || 0}</span>
                        <span class="diff-tag hard">Hard: ${p.hardCount || 0}</span>
                        <span class="pill">${p.totalQuestions} Qs</span>
                    </div>
                </div>
                <div class="test-paper-actions">
                    <button class="btn btn-outline btn-sm flex-1" onclick="viewPaperDetails(${p.id})">Inspect</button>
                    <button class="btn btn-primary btn-sm flex-1" onclick="startExamWithPaper(${p.id})">Take Test ✍️</button>
                    <button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="deleteTestPaper(${p.id})">🗑️</button>
                </div>
            </div>
        `).join('');

        populateExamPaperDropdown();
    } catch (err) {
        console.error('Error loading test papers:', err);
    }
}

async function viewPaperDetails(paperId) {
    try {
        const res = await fetch(`${API_BASE}/test-papers/${paperId}`);
        if (!res.ok) throw new Error('Failed to load test paper details');
        const paper = await res.json();

        document.getElementById('viewPaperModalTitle').textContent = paper.title;
        const content = document.getElementById('viewPaperModalContent');

        content.innerHTML = `
            <div style="margin-bottom: 20px;">
                <p style="color: var(--text-muted); font-size: 0.9rem;">${escapeHtml(paper.description || '')}</p>
                <div style="display: flex; gap: 10px; margin-top: 8px;">
                    <span class="diff-tag easy">Easy: ${paper.easyCount}</span>
                    <span class="diff-tag medium">Medium: ${paper.mediumCount}</span>
                    <span class="diff-tag hard">Hard: ${paper.hardCount}</span>
                    <span class="pill">Total: ${paper.totalQuestions} Questions</span>
                </div>
            </div>
            <div class="questions-list">
                ${paper.questions.map((q, idx) => `
                    <div class="question-card">
                        <div class="question-meta">
                            <span class="badge badge-info">Question ${idx + 1}</span>
                            <span class="diff-tag ${q.difficulty.toLowerCase()}">${q.difficulty}</span>
                        </div>
                        <div class="question-text">${escapeHtml(q.questionText)}</div>
                        <div class="options-preview-grid">
                            <div class="opt-box ${q.correctAnswer === 'A' ? 'correct' : ''}"><strong>A:</strong> ${escapeHtml(q.optionA)}</div>
                            <div class="opt-box ${q.correctAnswer === 'B' ? 'correct' : ''}"><strong>B:</strong> ${escapeHtml(q.optionB)}</div>
                            <div class="opt-box ${q.correctAnswer === 'C' ? 'correct' : ''}"><strong>C:</strong> ${escapeHtml(q.optionC)}</div>
                            <div class="opt-box ${q.correctAnswer === 'D' ? 'correct' : ''}"><strong>D:</strong> ${escapeHtml(q.optionD)}</div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;

        openModal('viewPaperModal');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteTestPaper(id) {
    if (!confirm('Are you sure you want to delete this test paper?')) return;
    try {
        const res = await fetch(`${API_BASE}/test-papers/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete test paper');
        showToast('Test Paper deleted', 'info');
        loadTestPapers();
        loadDashboard();
        loadUsageFrequency();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// 6. Student Exam Room & Auto-Grading Engine
function populateExamPaperDropdown() {
    const select = document.getElementById('examPaperSelect');
    if (!select) return;

    const currentVal = select.value;
    select.innerHTML = '<option value="">-- Choose a Test Paper --</option>' +
        allTestPapers.map(p => `<option value="${p.id}">${escapeHtml(p.title)} (${p.totalQuestions} Qs)</option>`).join('');

    if (currentVal) select.value = currentVal;
}

function startExamWithPaper(paperId) {
    switchTab('exam-room');
    const select = document.getElementById('examPaperSelect');
    if (select) {
        select.value = paperId;
    }
    const nameInput = document.getElementById('studentNameInput');
    if (!nameInput.value) {
        nameInput.value = 'Student ' + Math.floor(Math.random() * 900 + 100);
    }
    startExam();
}

async function startExam() {
    const paperId = document.getElementById('examPaperSelect').value;
    const studentName = document.getElementById('studentNameInput').value.trim();

    if (!paperId) {
        showToast('Please select a test paper to start the exam.', 'error');
        return;
    }
    if (!studentName) {
        showToast('Please enter your student name.', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/test-papers/${paperId}`);
        if (!res.ok) throw new Error('Failed to load test questions');
        activeExamPaper = await res.json();

        // Hide setup card, hide result card, show active exam
        document.getElementById('examSetupCard').classList.add('hidden');
        document.getElementById('examResultCard').classList.add('hidden');
        const activeContainer = document.getElementById('activeExamContainer');
        activeContainer.classList.remove('hidden');

        document.getElementById('activeExamTitle').textContent = activeExamPaper.title;
        document.getElementById('activeExamStudent').textContent = `Student: ${studentName}`;
        document.getElementById('activeExamTotalQs').textContent = `${activeExamPaper.questions.length} Questions`;

        const questionsList = document.getElementById('examQuestionsList');
        questionsList.innerHTML = activeExamPaper.questions.map((q, idx) => `
            <div class="exam-question-item">
                <div class="exam-q-header">
                    <span class="badge badge-info">Question ${idx + 1} of ${activeExamPaper.questions.length}</span>
                    <span class="diff-tag ${q.difficulty.toLowerCase()}">${q.difficulty}</span>
                </div>
                <div class="exam-q-text">${escapeHtml(q.questionText)}</div>
                <div class="exam-options-stack">
                    <label class="exam-option-label">
                        <input type="radio" name="answer_${q.id}" value="A">
                        <span><strong>A:</strong> ${escapeHtml(q.optionA)}</span>
                    </label>
                    <label class="exam-option-label">
                        <input type="radio" name="answer_${q.id}" value="B">
                        <span><strong>B:</strong> ${escapeHtml(q.optionB)}</span>
                    </label>
                    <label class="exam-option-label">
                        <input type="radio" name="answer_${q.id}" value="C">
                        <span><strong>C:</strong> ${escapeHtml(q.optionC)}</span>
                    </label>
                    <label class="exam-option-label">
                        <input type="radio" name="answer_${q.id}" value="D">
                        <span><strong>D:</strong> ${escapeHtml(q.optionD)}</span>
                    </label>
                </div>
            </div>
        `).join('');

        activeContainer.scrollIntoView({ behavior: 'smooth' });
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function cancelExam() {
    if (!confirm('Are you sure you want to cancel this test attempt?')) return;
    document.getElementById('activeExamContainer').classList.add('hidden');
    document.getElementById('examSetupCard').classList.remove('hidden');
    activeExamPaper = null;
}

async function handleExamSubmit(e) {
    e.preventDefault();

    if (!activeExamPaper) return;
    const studentName = document.getElementById('studentNameInput').value.trim();
    const studentEmail = document.getElementById('studentEmailInput').value.trim();

    const answersMap = {};
    activeExamPaper.questions.forEach(q => {
        const selected = document.querySelector(`input[name="answer_${q.id}"]:checked`);
        if (selected) {
            answersMap[q.id] = selected.value;
        }
    });

    const payload = {
        testPaperId: activeExamPaper.id,
        studentName,
        studentEmail: studentEmail || null,
        answers: answersMap
    };

    const submitBtn = document.getElementById('btnSubmitExam');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Auto-Grading in Progress... ⚡';

    try {
        const res = await fetch(`${API_BASE}/attempts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Submission grading failed');
        }

        const attemptResult = await res.json();
        showToast('Test auto-graded successfully!', 'success');

        displayExamResult(attemptResult);
        loadAttempts();
        loadDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = '✅ Submit Test & Auto-Grade Now';
    }
}

function displayExamResult(result) {
    document.getElementById('activeExamContainer').classList.add('hidden');
    const resultCard = document.getElementById('examResultCard');
    resultCard.classList.remove('hidden');

    const passed = result.percentage >= 50.0;
    const circleClass = passed ? '' : 'fail';
    const badgeClass = passed ? 'badge-success' : 'badge-danger';
    const statusText = passed ? 'Passed 🌟' : 'Needs Improvement ⚠️';

    resultCard.innerHTML = `
        <div class="result-score-circle ${circleClass}">
            <div class="score-num">${result.score}</div>
            <div class="score-total">out of ${result.totalQuestions}</div>
        </div>
        <h2>${result.percentage}% • <span class="badge ${badgeClass}">${statusText}</span></h2>
        <p style="color: var(--text-muted); margin-top: 6px;">
            Submitted by <strong>${escapeHtml(result.studentName)}</strong> on ${new Date(result.submittedAt).toLocaleString()}
        </p>

        <div class="breakdown-table">
            <h3 style="margin-bottom: 16px;">Question-by-Question Evaluation Breakdown:</h3>
            <div class="questions-list">
                ${result.answers.map((ans, idx) => `
                    <div class="question-card" style="border-left: 4px solid ${ans.correct ? 'var(--success)' : 'var(--danger)'}">
                        <div class="question-meta">
                            <span class="badge badge-info">Q${idx + 1}</span>
                            <span class="badge ${ans.correct ? 'badge-success' : 'badge-danger'}">
                                ${ans.correct ? 'CORRECT (+1)' : 'INCORRECT (0)'}
                            </span>
                        </div>
                        <div class="question-text">${escapeHtml(ans.questionText)}</div>
                        <div style="font-size: 0.9rem; margin-top: 8px;">
                            <span style="color: ${ans.correct ? 'var(--success)' : 'var(--danger)'}">
                                Your Answer: <strong>Option ${escapeHtml(ans.selectedOption || 'Unanswered')}</strong>
                            </span>
                            ${!ans.correct ? `<span style="color: var(--success); margin-left: 20px;">Correct Answer: <strong>Option ${escapeHtml(ans.correctOption)}</strong></span>` : ''}
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>

        <div style="margin-top: 30px; display: flex; justify-content: center; gap: 16px;">
            <button class="btn btn-secondary" onclick="switchTab('attempts')">View All Attempts</button>
            <button class="btn btn-primary" onclick="resetExamRoom()">Take Another Test</button>
        </div>
    `;

    resultCard.scrollIntoView({ behavior: 'smooth' });
}

function resetExamRoom() {
    document.getElementById('examResultCard').classList.add('hidden');
    document.getElementById('examSetupCard').classList.remove('hidden');
    document.getElementById('studentNameInput').value = '';
}

// 7. Question Usage Frequency Analytics (Feature #5)
async function loadUsageFrequency() {
    try {
        const res = await fetch(`${API_BASE}/questions/usage-frequency`);
        if (!res.ok) throw new Error('Failed to fetch usage frequency');
        const list = await res.json();

        const tbody = document.getElementById('usageTableBody');
        if (!tbody) return;

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 30px;">No questions in database.</td></tr>';
            return;
        }

        // Find max usage for progress bar scaling
        const maxUsage = Math.max(...list.map(q => q.usageCount), 1);

        tbody.innerHTML = list.map(q => {
            const pct = Math.round((q.usageCount / maxUsage) * 100);
            const papersStr = q.testPaperTitles && q.testPaperTitles.length > 0
                ? q.testPaperTitles.map(t => `<span class="pill" style="font-size: 0.75rem; margin: 2px;">${escapeHtml(t)}</span>`).join('')
                : '<span style="color: var(--text-muted); font-size: 0.8rem;">Not yet included</span>';

            return `
                <tr>
                    <td>#${q.questionId}</td>
                    <td><strong>${escapeHtml(q.questionText)}</strong></td>
                    <td><span class="unit-tag">${escapeHtml(q.unitName)}</span></td>
                    <td><span class="topic-tag">${escapeHtml(q.topic)}</span></td>
                    <td><span class="diff-tag ${q.difficulty.toLowerCase()}">${q.difficulty}</span></td>
                    <td>
                        <div class="usage-meter-box">
                            <strong>${q.usageCount}</strong>
                            <div class="usage-meter-bar">
                                <div class="usage-fill" style="width: ${pct}%;"></div>
                            </div>
                        </div>
                    </td>
                    <td>${papersStr}</td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error('Error loading usage frequency:', err);
    }
}

// 8. Attempts History
async function loadAttempts() {
    try {
        const res = await fetch(`${API_BASE}/attempts`);
        if (!res.ok) throw new Error('Failed to fetch attempts');
        const attempts = await res.json();

        const tbody = document.getElementById('attemptsTableBody');
        if (!tbody) return;

        if (!attempts || attempts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 30px;">No student attempts recorded yet. Take an exam in the Exam Room tab!</td></tr>';
            return;
        }

        tbody.innerHTML = attempts.map(a => {
            const passed = a.percentage >= 50;
            const statusClass = passed ? 'badge-success' : 'badge-danger';
            const statusText = passed ? 'PASSED' : 'FAILED';

            return `
                <tr>
                    <td>#${a.id}</td>
                    <td><strong>${escapeHtml(a.studentName)}</strong></td>
                    <td>${escapeHtml(a.testPaper?.title || 'Unknown Paper')}</td>
                    <td>${a.score} / ${a.totalQuestions}</td>
                    <td><strong>${a.percentage}%</strong></td>
                    <td><span class="badge ${statusClass}">${statusText}</span></td>
                    <td>${new Date(a.submittedAt).toLocaleString()}</td>
                    <td>
                        <button class="btn btn-sm btn-outline" onclick="viewAttemptBreakdown(${a.id})">Review</button>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error('Error loading attempts:', err);
    }
}

async function viewAttemptBreakdown(attemptId) {
    try {
        const res = await fetch(`${API_BASE}/attempts/${attemptId}`);
        if (!res.ok) throw new Error('Failed to load attempt details');
        const a = await res.json();

        document.getElementById('viewAttemptModalTitle').textContent = `Attempt #${a.id} - ${a.studentName}`;
        const content = document.getElementById('viewAttemptModalContent');

        content.innerHTML = `
            <div style="margin-bottom: 20px;">
                <p><strong>Test Paper:</strong> ${escapeHtml(a.testPaper?.title || '')}</p>
                <p><strong>Final Score:</strong> ${a.score}/${a.totalQuestions} (${a.percentage}%)</p>
                <p><strong>Submitted At:</strong> ${new Date(a.submittedAt).toLocaleString()}</p>
            </div>
            <div class="questions-list">
                ${a.answers.map((ans, idx) => `
                    <div class="question-card" style="border-left: 4px solid ${ans.correct ? 'var(--success)' : 'var(--danger)'}">
                        <div class="question-meta">
                            <span class="badge badge-info">Question ${idx + 1}</span>
                            <span class="badge ${ans.correct ? 'badge-success' : 'badge-danger'}">
                                ${ans.correct ? 'CORRECT (+1)' : 'INCORRECT (0)'}
                            </span>
                        </div>
                        <div class="question-text">${escapeHtml(ans.questionText)}</div>
                        <div style="font-size: 0.9rem; margin-top: 8px;">
                            <span>Selected: <strong>Option ${escapeHtml(ans.selectedOption)}</strong></span>
                            ${!ans.correct ? `<span style="color: var(--success); margin-left: 20px;">Correct Answer: <strong>Option ${escapeHtml(ans.correctOption)}</strong></span>` : ''}
                        </div>
                    </div>
                `).join('')}
            </div>
        `;

        openModal('viewAttemptModal');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Modal helper functions
function openModal(id) {
    document.getElementById(id)?.classList.remove('hidden');
}

function closeModal(id) {
    document.getElementById(id)?.classList.add('hidden');
}

// Print / PDF helper
function printTestPaper() {
    window.print();
}

// Utility: escape HTML to prevent XSS
function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
