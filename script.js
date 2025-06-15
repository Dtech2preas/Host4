// Data files would be in the same directory const QUESTIONS_URL = 'questions.json'; const QUOTES_URL = 'quotes.json'; const VIDEOS_URL = 'videos.json';

// DOM Elements const stage1 = document.getElementById('stage1'); const stage2 = document.getElementById('stage2'); const stage3 = document.getElementById('stage3'); const quizForm = document.getElementById('quiz-form'); const quizQuestion = document.getElementById('quiz-question'); const quizOptions = document.getElementById('quiz-options'); const quoteText = document.getElementById('quote-text'); const quoteAuthor = document.getElementById('quote-author'); const nextBtn = document.getElementById('next-btn'); const videoBtn = document.getElementById('video-btn'); const videoTitle = document.getElementById('video-title'); const videoModal = document.getElementById('video-modal'); const videoEmbed = document.getElementById('video-embed'); const closeBtn = document.querySelector('.close-btn'); const restartBtn = document.getElementById('restart-btn');

// Ad URL and retry duration const AD_URL = 'https://cafewarriors.com/spymr251ew?key=ce76edf7e5c6e4907177e712dc143365'; const RETRY_MS = 10000;

// App state let currentStage = 1; let questions = []; let quotes = []; let videos = []; let dayIndex = 0; let firstVisitDate = null;

// Initialize the app async function init() { setupRetryOverlay();

// Load first visit date
const storedDate = localStorage.getItem('firstVisitDate');
if (!storedDate) {
    firstVisitDate = Date.now();
    localStorage.setItem('firstVisitDate', firstVisitDate.toString());
} else {
    firstVisitDate = parseInt(storedDate, 10);
}

// Calculate day index
const today = new Date();
const timeDiff = today - new Date(firstVisitDate);
const dayDiff = Math.floor(timeDiff / (1000 * 60 * 60 * 24));

try {
    const responses = await Promise.all([
        fetch(QUESTIONS_URL).then(res => res.json()),
        fetch(QUOTES_URL).then(res => res.json()),
        fetch(VIDEOS_URL).then(res => res.json())
    ]);

    [questions, quotes, videos] = responses;
    dayIndex = dayDiff % Math.min(questions.length, quotes.length, videos.length);
    loadStage1();
} catch (error) {
    console.error('Error loading data:', error);
    quizQuestion.textContent = 'Error loading content. Please try again later.';
}

}

// Retry overlay setup and logic function setupRetryOverlay() { const overlay = document.createElement('div'); overlay.id = 'retry-overlay'; overlay.style.position = 'fixed'; overlay.style.top = '0'; overlay.style.left = '0'; overlay.style.width = '100%'; overlay.style.height = '100%'; overlay.style.background = 'rgba(0,0,0,0.8)'; overlay.style.color = '#fff'; overlay.style.display = 'flex'; overlay.style.flexDirection = 'column'; overlay.style.justifyContent = 'center'; overlay.style.alignItems = 'center'; overlay.style.fontSize = '2rem'; overlay.style.zIndex = '1000'; overlay.style.visibility = 'hidden'; document.body.appendChild(overlay);

const message = document.createElement('div');
message.id = 'retry-message';
overlay.appendChild(message);

checkRetry();

}

function checkRetry() { const retryEnd = parseInt(localStorage.getItem('retryEnd'), 10); if (retryEnd && Date.now() < retryEnd) { showRetryCountdown(retryEnd); } else { clearRetry(); } }

function showRetryCountdown(endTime) { const overlay = document.getElementById('retry-overlay'); const message = document.getElementById('retry-message'); overlay.style.visibility = 'visible'; quizForm.querySelector('button[type="submit"]').disabled = true;

const interval = setInterval(() => {
    const remaining = Math.ceil((endTime - Date.now()) / 1000);
    if (remaining > 0) {
        message.textContent = `Incorrect! Try again in ${remaining}s...`;
    } else {
        clearInterval(interval);
        overlay.style.visibility = 'hidden';
        quizForm.querySelector('button[type="submit"]').disabled = false;
        localStorage.removeItem('retryEnd');
        // Open ad
        window.open(AD_URL, '_blank');
    }
}, 500);

}

function clearRetry() { const overlay = document.getElementById('retry-overlay'); overlay.style.visibility = 'hidden'; quizForm.querySelector('button[type="submit"]').disabled = false; localStorage.removeItem('retryEnd'); }

// Quiz submission handler quizForm.addEventListener('submit', function(e) { e.preventDefault();

// Prevent submission during retry
if (localStorage.getItem('retryEnd')) return;

const selectedOption = document.querySelector('input[name="quiz-option"]:checked');
if (!selectedOption) return;

const question = questions[dayIndex];
const isCorrect = parseInt(selectedOption.value, 10) === question.answer;

if (isCorrect) {
    document.body.classList.add('correct');
    setTimeout(() => document.body.classList.remove('correct'), 500);

    setTimeout(() => {
        stage1.style.animation = 'slideOutLeft 0.5s forwards';
        setTimeout(() => {
            stage1.style.animation = '';
            loadStage2();
        }, 500);
    }, 2000);
} else {
    document.body.classList.add('incorrect');
    setTimeout(() => document.body.classList.remove('incorrect'), 500);
    const endTime = Date.now() + RETRY_MS;
    localStorage.setItem('retryEnd', endTime.toString());
    showRetryCountdown(endTime);
}

});

// Stage loaders and other handlers remain unchanged... function loadStage1() { /* unchanged / } function loadStage2() { / unchanged / } function loadStage3() { / unchanged / } function openVideoModal(url) { / unchanged / } function closeVideoModal() { / unchanged / } function extractVideoId(url) { / unchanged / } function setActiveStage(stage) { / unchanged */ }

// Next button nextBtn.addEventListener('click', () => { stage2.style.animation = 'slideOutLeft 0.5s forwards'; setTimeout(() => { stage2.style.animation = ''; loadStage3(); }, 500); });

// Modal controls closeBtn.addEventListener('click', closeVideoModal); restartBtn.addEventListener('click', () => { closeVideoModal(); loadStage1(); }); window.addEventListener('click', (e) => { if (e.target === videoModal) closeVideoModal(); });

// Initialize init();

