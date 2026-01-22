/* ===================== GET URL PARAMS ===================== */
const params = new URLSearchParams(window.location.search);
const category = params.get("cat");
const difficulty = params.get("diff");

/* ===================== VALIDATION ===================== */
if (!category || !difficulty || !quizData[category] || !quizData[category][difficulty]) {
  alert("Invalid quiz configuration");
  window.location.href = "index.html";
}

/* ===================== RANDOM QUESTION SELECTION ===================== */
function getRandomQuestions(allQuestions, count = 10) {
  const shuffled = [...allQuestions].sort(() => Math.random() - 0.5);
  return shuffled.slice(0, count);
}

/* ===================== LOAD QUESTIONS ===================== */
const questions = getRandomQuestions(
  quizData[category][difficulty],
  10
);

/* ===================== STATE VARIABLES ===================== */
let currentIndex = 0;
let score = 0;
let timer = null;
let timeLeft = 30;

let userAnswers = [];
let timeSpent = [];

/* ===================== LOAD QUESTION ===================== */
function loadQuestion() {
  clearInterval(timer);

  if (currentIndex >= questions.length) {
    finishQuiz();
    return;
  }

  const q = questions[currentIndex];

  // Question text
  document.getElementById("question").innerText = q.q;

  // Options
  const optionsDiv = document.getElementById("options");
  optionsDiv.innerHTML = "";

  q.options.forEach(option => {
    const label = document.createElement("label");
    label.innerHTML = `
      <input type="radio" name="option" value="${option}">
      <span>${option}</span>
    `;
    optionsDiv.appendChild(label);
  });

  updateProgressBar();
  startTimer();
}

/* ===================== TIMER (30s PER QUESTION) ===================== */
function startTimer() {
  timeLeft = 30;
  document.getElementById("timer").innerText = `Time: ${timeLeft}s`;

  timer = setInterval(() => {
    timeLeft--;
    document.getElementById("timer").innerText = `Time: ${timeLeft}s`;

    if (timeLeft <= 0) {
      clearInterval(timer);
      submitAnswer(null); // auto submit
    }
  }, 1000);
}

/* ===================== SUBMIT ANSWER ===================== */
function submitAnswer(selectedValue) {
  clearInterval(timer);

  const correctAnswer = questions[currentIndex].answer;

  userAnswers.push(selectedValue);
  timeSpent.push(30 - timeLeft);

  if (selectedValue && selectedValue === correctAnswer) {
    score++;
  }

  currentIndex++;
  loadQuestion();
}

/* ===================== NEXT BUTTON HANDLER ===================== */
function nextQuestion() {
  const selected = document.querySelector('input[name="option"]:checked');
  submitAnswer(selected ? selected.value : null);
}

/* ===================== PROGRESS BAR ===================== */
function updateProgressBar() {
  const progress = (currentIndex / questions.length) * 100;
  document.getElementById("progress-bar").style.width = `${progress}%`;
}

/* ===================== FINISH QUIZ ===================== */
function finishQuiz() {
  clearInterval(timer);

  localStorage.setItem("score", score);
  localStorage.setItem("total", questions.length);
  localStorage.setItem("timeSpent", JSON.stringify(timeSpent));
  localStorage.setItem("answers", JSON.stringify(userAnswers));
  localStorage.setItem("questions", JSON.stringify(questions));
  localStorage.setItem("category", category);
  localStorage.setItem("difficulty", difficulty);

  window.location.href = "result.html";
}

/* ===================== INIT ===================== */
document.addEventListener("DOMContentLoaded", loadQuestion);
