const $ = (selector) => document.querySelector(selector);
const mainMenuButton = $("#mainMenuBtn");
const mainMenu = $("#mainMenu");
const lensMenuButton = $("#lensMenuBtn");
const lensMenu = $("#lensMenu");
const lensOverlay = $("#lensOverlay");
const lensCloseButton = $("#lensClose");
const cameraVideo = $("#cameraVideo");
const cameraPlaceholder = $("#cameraPlaceholder");
const captureCanvas = $("#captureCanvas");
const statusMessage = $("#statusMessage");
const toast = $("#toast");
const galleryInput = $("#galleryInput");
const speechOverlay = $("#speechOverlay");
const speechClose = $("#speechClose");
const speechMenuBtn = $("#speechMenuBtn");
const speechSideMenu = $("#speechSideMenu");
const speechMenuOverlay = $("#speechMenuOverlay");
const sourceLanguage = $("#sourceLanguage");
const targetLanguage = $("#targetLanguage");
const inputTitle = $("#inputTitle");
const outputTitle = $("#outputTitle");
const recordBtn = $("#recordBtn");
const inputWave = $("#inputWave");
const recordTimer = $("#recordTimer");
const recordStatus = $("#recordStatus");
const recognizedText = $("#recognizedText");
const speechConvertBtn = $("#speechConvertBtn");
const translatedText = $("#translatedText");
const playBtn = $("#playBtn");
const outputTimer = $("#outputTimer");
const historyBtn = $("#historyBtn");
const converterOverlay = $("#converterOverlay");
const converterClose = $("#converterClose");
const converterTitle = $("#converterTitle");
const addFile = $("#addFile");
const fileInput = $("#fileInput");
const fileList = $("#fileList");
const uploadBtn = $("#uploadBtn");
const convertBtn = $("#convertBtn");
const textConverterOverlay = $("#textConverterOverlay");
const textConverterClose = $("#textConverterClose");
const textMenuButton = $("#textMenuButton");
const textConverterMenu = $("#textConverterMenu");
const textMenuOverlay = $("#textMenuOverlay");
const textSourceLanguage = $("#textSourceLanguage");
const textTargetLanguage = $("#textTargetLanguage");
const textSwapButton = $("#textSwapButton");
const textLanguageInfo = $("#textLanguageInfo");
const textSourceName = $("#textSourceName");
const textTargetName = $("#textTargetName");
const textSourceText = $("#textSourceText");
const textCharacterCount = $("#textCharacterCount");
const textResult = $("#textResult");
const textTranslationStatus = $("#textTranslationStatus");
const textConvertButton = $("#textConvertButton");
let cameraStream = null;
let speechRecognition = null;
let isRecording = false;
let recordingSeconds = 0;
let recordingInterval = null;
let outputSeconds = 0;
let outputInterval = null;
let finalTranscript = "";
let currentTranslation = "";
let toastTimer;
let selectedImageUrl = null;
let selectedFiles = [];
let viewerObjectUrl = null;
function showToast(message) {
  toast.textContent = message;
  toast.classList.add("visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.classList.remove("visible");
  }, 3000);
}
function setStatus(message) {
  statusMessage.textContent = message;
}
function toggleMenu(button, menu) {
  const isOpen = !menu.hidden;
  menu.hidden = isOpen;
  button.setAttribute("aria-expanded", String(!isOpen));
}
function closeMenu(button, menu) {
  menu.hidden = true;
  button.setAttribute("aria-expanded", "false");
}
function handleMenuAction(event) {
  const action = event.currentTarget.dataset.menuAction;
  closeMenu(mainMenuButton, mainMenu);
  closeMenu(lensMenuButton, lensMenu);
  showToast(`${action} selected.`);
}
function toggleFeatures() {
  const container = $(".node-container");
  const centerNode = $("#centerNode");
  const isOpen = container.classList.toggle("open");
  centerNode.setAttribute("aria-expanded", String(isOpen));
  centerNode.setAttribute(
    "aria-label",
    isOpen ? "Hide PALASH AI features" : "Show PALASH AI features"
  );
}
const languageNames = { hi: "Hindi", sat: "Santali", ho: "Ho" };
const speechCodes = { hi: "hi-IN", sat: "sat", ho: "hoc" };
function languageName(code) {
  return languageNames[code] || code;
}
function updateSpeechLabels() {
  inputTitle.textContent = `Speak in ${languageName(sourceLanguage.value)}`;
  outputTitle.textContent = `${languageName(targetLanguage.value)} Speech`;
}
function resetSpeechContent() {
  recognizedText.textContent = "";
  translatedText.textContent = "";
  finalTranscript = "";
  currentTranslation = "";
  resetRecordingTimer();
  resetOutputTimer();
}
function updateRecordingTimer() {
  const minutes = Math.floor(recordingSeconds / 60);
  const seconds = recordingSeconds % 60;
  recordTimer.textContent = `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}
function startRecordingTimer() {
  stopRecordingTimer();
  recordingSeconds = 0;
  updateRecordingTimer();
  recordingInterval = setInterval(() => {
    recordingSeconds += 1;
    updateRecordingTimer();
  }, 1000);
}
function stopRecordingTimer() {
  clearInterval(recordingInterval);
  recordingInterval = null;
}
function resetRecordingTimer() {
  stopRecordingTimer();
  recordingSeconds = 0;
  updateRecordingTimer();
}
function updateOutputTimer() {
  const minutes = Math.floor(outputSeconds / 60);
  const seconds = outputSeconds % 60;
  outputTimer.textContent = `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}
function startOutputTimer() {
  clearInterval(outputInterval);
  outputSeconds = 0;
  updateOutputTimer();
  outputInterval = setInterval(() => {
    outputSeconds += 1;
    updateOutputTimer();
  }, 1000);
}
function resetOutputTimer() {
  clearInterval(outputInterval);
  outputInterval = null;
  outputSeconds = 0;
  updateOutputTimer();
}
function stopSpeechRecognition() {
  if (speechRecognition && isRecording) speechRecognition.stop();
  isRecording = false;
  stopRecordingTimer();
  recordBtn.classList.remove("recording");
  inputWave.classList.remove("active");
}
function closeSpeechMenu() {
  speechSideMenu.hidden = true;
  speechMenuOverlay.hidden = true;
  speechSideMenu.classList.remove("open");
  speechMenuOverlay.classList.remove("active");
  speechMenuBtn.setAttribute("aria-expanded", "false");
}
function toggleSpeechRecording() {
  const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!Recognition) {
    recordStatus.textContent = "Speech recognition is not supported in this browser.";
    return;
  }
  if (isRecording) {
    speechRecognition.stop();
    return;
  }
  speechRecognition = new Recognition();
  speechRecognition.lang = speechCodes[sourceLanguage.value];
  speechRecognition.continuous = false;
  speechRecognition.interimResults = true;
  speechRecognition.onstart = () => {
    isRecording = true;
    recordBtn.classList.add("recording");
    inputWave.classList.add("active");
    recordStatus.textContent = "Listening...";
    startRecordingTimer();
  };
  speechRecognition.onresult = (event) => {
    let interimText = "";
    let finalText = "";
    for (let index = event.resultIndex; index < event.results.length; index += 1) {
      const transcript = event.results[index][0].transcript;
      if (event.results[index].isFinal) finalText += transcript;
      else interimText += transcript;
    }
    if (finalText.trim()) finalTranscript += finalText;
    recognizedText.textContent = finalTranscript + interimText;
  };
  speechRecognition.onerror = (event) => {
    stopSpeechRecognition();
    recordStatus.textContent = event.error === "not-allowed"
      ? "Microphone permission denied"
      : event.error === "no-speech"
        ? "No speech detected"
        : "Speech recognition error";
    speechRecognition = null;
  };
  speechRecognition.onend = () => {
    const captured = recognizedText.textContent.trim();
    stopSpeechRecognition();
    recordStatus.textContent = captured
      ? "Speech captured successfully"
      : "No speech detected";
    speechRecognition = null;
  };
  try {
    speechRecognition.start();
  } catch (error) {
    recordStatus.textContent = "Unable to start the microphone.";
    speechRecognition = null;
  }
}
function openSpeechConverter() {
  speechOverlay.hidden = false;
  updateSpeechLabels();
  speechClose.focus();
}
function closeSpeechConverter() {
  stopSpeechRecognition();
  speechRecognition = null;
  speechOverlay.hidden = true;
  closeSpeechMenu();
}
function getDemoTranslation(text, source, target) {
  return `Demo: ${languageName(source)} → ${languageName(target)}\n\n${text}`;
}
function saveTranslationHistory(text, translation, source, target) {
  const history = JSON.parse(localStorage.getItem("translationHistory") || "[]");
  history.unshift({
    sourceLanguage: languageName(source),
    targetLanguage: languageName(target),
    sourceText: text,
    translation,
    date: new Date().toLocaleString()
  });
  localStorage.setItem("translationHistory", JSON.stringify(history.slice(0, 20)));
}
async function convertSpeech() {
  const text = recognizedText.textContent.trim();
  if (!text) {
    showToast("Record some speech first.");
    return;
  }
  const source = sourceLanguage.value;
  const target = targetLanguage.value;
  speechConvertBtn.classList.add("loading");
  speechConvertBtn.textContent = "•••";
  translatedText.textContent = "Translating...";
  try {
    const response = await fetch("http://localhost:8000/translate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text, source_language: source, target_language: target })
    });
    if (!response.ok) throw new Error("Translation server error");
    const data = await response.json();
    currentTranslation = data.translation || getDemoTranslation(text, source, target);
  } catch (error) {
    currentTranslation = getDemoTranslation(text, source, target);
  }
  translatedText.textContent = currentTranslation;
  startOutputTimer();
  saveTranslationHistory(text, currentTranslation, source, target);
  speechConvertBtn.classList.remove("loading");
  speechConvertBtn.textContent = "Convert";
}
function playTranslatedSpeech() {
  const text = translatedText.textContent.trim();
  if (!text || text === "Translating...") {
    showToast("Convert speech first.");
    return;
  }
  if (!("speechSynthesis" in window)) {
    showToast("Text-to-speech is not supported in this browser.");
    return;
  }
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = speechCodes[targetLanguage.value];
  utterance.rate = 0.9;
  speechSynthesis.cancel();
  speechSynthesis.speak(utterance);
}
function showTranslationHistory() {
  const history = JSON.parse(localStorage.getItem("translationHistory") || "[]");
  if (!history.length) {
    showToast("No translation history found.");
    return;
  }
  showToast(`${history.length} translation(s) saved in history.`);
}
function openConverter() {
  converterTitle.textContent = "File Converter";
  converterOverlay.hidden = false;
  converterClose.focus();
}
function closeConverter() {
  converterOverlay.hidden = true;
}
const textLanguageNames = {
  hindi: "Hindi",
  santali: "Santali",
  ho: "Ho",
  mundari: "Mundari"
};
const textSpeechLanguages = {
  hindi: "hi-IN",
  santali: "sat-IN",
  ho: "hi-IN",
  mundari: "hi-IN"
};
const textTranslationDatabase = {
  hindiToSantali: {
    "नमस्ते": "[Demo Santali translation]",
    "आप कैसे हैं?": "[Demo Santali translation]",
    "धन्यवाद": "[Demo Santali translation]",
    "शुभ प्रभात": "[Demo Santali translation]",
    "मेरा नाम सोहेल है।": "[Demo Santali translation]"
  },
  santaliToHindi: {
    "[Demo Santali translation]": "नमस्ते"
  },
  hindiToHo: {
    "नमस्ते": "[Demo Ho translation]",
    "आप कैसे हैं?": "[Demo Ho translation]",
    "धन्यवाद": "[Demo Ho translation]",
    "शुभ प्रभात": "[Demo Ho translation]",
    "मेरा नाम सोहेल है।": "[Demo Ho translation]"
  },
  hoToHindi: {
    "[Demo Ho translation]": "नमस्ते"
  },
  hindiToMundari: {
    "नमस्ते": "[Demo Mundari translation]",
    "आप कैसे हैं?": "[Demo Mundari translation]",
    "धन्यवाद": "[Demo Mundari translation]",
    "शुभ प्रभात": "[Demo Mundari translation]",
    "मेरा नाम सोहेल है।": "[Demo Mundari translation]"
  },
  mundariToHindi: {
    "[Demo Mundari translation]": "नमस्ते"
  }
};
function getTextTranslationPair(source, target) {
  const pair = `${source}To${target[0].toUpperCase()}${target.slice(1)}`;
  return {
    hindiToSantali: textTranslationDatabase.hindiToSantali,
    santaliToHindi: textTranslationDatabase.santaliToHindi,
    hindiToHo: textTranslationDatabase.hindiToHo,
    hoToHindi: textTranslationDatabase.hoToHindi,
    hindiToMundari: textTranslationDatabase.hindiToMundari,
    mundariToHindi: textTranslationDatabase.mundariToHindi
  }[pair] || null;
}
function updateTextTargetLanguage() {
  const source = textSourceLanguage.value;
  if (source === "hindi") {
    textTargetLanguage.disabled = false;
    if (textTargetLanguage.value === "hindi") {
      textTargetLanguage.value = "santali";
    }
  } else {
    textTargetLanguage.value = "hindi";
    textTargetLanguage.disabled = true;
  }
}
function updateTextLanguageUI() {
  updateTextTargetLanguage();
  const source = textLanguageNames[textSourceLanguage.value];
  const target = textLanguageNames[textTargetLanguage.value];
  textSourceName.textContent = source;
  textTargetName.textContent = target;
  textLanguageInfo.textContent = `${source} → ${target}`;
  textSourceText.placeholder = `Type ${source} text here...`;
}
function resetTextResult() {
  textResult.innerHTML = "<span>Your translated text will appear here...</span>";
  textTranslationStatus.textContent = "Ready to translate";
}
function openTextConverter() {
  textConverterOverlay.hidden = false;
  updateTextLanguageUI();
  textSourceText.focus();
}
function closeTextConverter() {
  textConverterOverlay.hidden = true;
  textConverterMenu.hidden = true;
  textMenuOverlay.hidden = true;
  textMenuButton.setAttribute("aria-expanded", "false");
}
async function translateText(text, source, target) {
  if (source === target) return text;
  const dictionary = getTextTranslationPair(source, target);
  if (!dictionary) {
    return "This translator currently supports Hindi ↔ Santali, Hindi ↔ Ho and Hindi ↔ Mundari only.";
  }
  if (Object.prototype.hasOwnProperty.call(dictionary, text)) {
    return dictionary[text];
  }
  return "Demo translation unavailable for this phrase.\n\nPlease connect a verified translation API or linguistic database for complete translation.";
}
async function convertText() {
  const text = textSourceText.value.trim();
  if (!text) {
    showToast("Enter text to translate first.");
    textSourceText.focus();
    return;
  }
  const source = textSourceLanguage.value;
  const target = textTargetLanguage.value;
  textConvertButton.disabled = true;
  textConvertButton.textContent = "•••";
  textResult.textContent = "Translating...";
  textTranslationStatus.textContent = "Translating...";
  try {
    const translation = await translateText(text, source, target);
    textResult.textContent = translation;
    textTranslationStatus.textContent = "Translation complete";
  } catch (error) {
    textResult.textContent = "Something went wrong. Please try again.";
    textTranslationStatus.textContent = "Translation failed";
  } finally {
    textConvertButton.disabled = false;
    textConvertButton.textContent = "⇄ CONVERT";
  }
}
function speakText(text, language) {
  if (!text || text.includes("Your translated text")) {
    showToast("There is no text to read.");
    return;
  }
  if (!("speechSynthesis" in window)) {
    showToast("Text-to-speech is not supported in this browser.");
    return;
  }
  const codes = { hindi: "hi-IN", santali: "sat", ho: "hoc", mundari: "un" };
  speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = codes[language] || "en-US";
  utterance.rate = 0.9;
  speechSynthesis.speak(utterance);
}
function selectFile(selectedFile) {
  fileList.querySelectorAll(".file").forEach((file) => {
    file.classList.remove("selected");
  });
  selectedFile.classList.add("selected");
}
function renderFiles() {
  fileList.replaceChildren();
  if (!selectedFiles.length) {
    const emptyFile = document.createElement("div");
    emptyFile.className = "file";
    emptyFile.textContent = "No files selected";
    fileList.appendChild(emptyFile);
    return;
  }
  selectedFiles.forEach((file, index) => {
    const fileElement = document.createElement("div");
    fileElement.className = `file${index === 0 ? " selected" : ""}`;
    fileElement.textContent = `• ${file.name}`;
    fileElement.title = file.name;
    fileElement.addEventListener("click", () => {
      selectFile(fileElement);
    });
    fileList.appendChild(fileElement);
  });
}
function selectFiles(event) {
  const newFiles = [...event.target.files];
  if (!newFiles.length) {
    return;
  }
  selectedFiles = [...selectedFiles, ...newFiles];
  renderFiles();
  showToast(`${newFiles.length} file(s) added.`);
  event.target.value = "";
}
function uploadFiles() {
  if (!selectedFiles.length) {
    showToast("Please select a file first.");
    return;
  }
  showToast(`${selectedFiles.length} file(s) selected successfully!`);
}
function getSelectedFile() {
  const selectedElement = fileList.querySelector(".file.selected");
  if (!selectedElement) {
    return null;
  }
  const fileIndex = [...fileList.children].indexOf(selectedElement);
  return selectedFiles[fileIndex] || null;
}
function convertFiles() {
  const selectedFile = getSelectedFile();
  if (!selectedFile) {
    showToast("Please select a file to convert.");
    return;
  }
  showToast(`Converting ${selectedFile.name} from Santali to Hindi...`);
}
function addViewerStyles() {
  const style = document.createElement("style");
  style.textContent = `
    .file-viewer {
      position: fixed;
      inset: 0;
      z-index: 40;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      background: #111c;
      backdrop-filter: blur(8px);
    }
    .file-viewer[hidden] {
      display: none;
    }
    .file-viewer-card {
      position: relative;
      width: min(100%, 900px);
      height: min(90vh, 720px);
      padding: 28px;
      overflow: hidden;
      border: 4px solid #111;
      border-radius: 25px;
      background: #fff;
      box-shadow: 10px 12px 0 #111;
      font-family: Arial, sans-serif;
    }
    .file-viewer-title {
      margin: 0 50px 18px 0;
      overflow: hidden;
      color: #7134e8;
      font-size: 26px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .file-viewer-close {
      position: absolute;
      top: 12px;
      right: 12px;
      width: 42px;
      height: 42px;
      border: 3px solid #111;
      border-radius: 50%;
      color: #fff;
      font-size: 28px;
      cursor: pointer;
      background: #df1e39;
      box-shadow: 3px 3px 0 #111;
    }
    .file-viewer-content {
      width: 100%;
      height: calc(100% - 65px);
      display: grid;
      place-items: center;
      overflow: auto;
      border: 3px dashed #111;
      border-radius: 15px;
      padding: 15px;
      background: #f1eaff;
    }
    .file-viewer-content img,
    .file-viewer-content video {
      max-width: 100%;
      max-height: 100%;
    }
    .file-viewer-content iframe {
      width: 100%;
      height: 100%;
      border: 0;
    }
    .file-viewer-content audio {
      width: min(100%, 500px);
    }
    .file-viewer-text {
      width: 100%;
      height: 100%;
      margin: 0;
      padding: 15px;
      overflow: auto;
      color: #111;
      white-space: pre-wrap;
      word-break: break-word;
      background: #fff;
    }
    .view-file-btn {
      background: #ffdc5e !important;
    }
    @media (max-width: 600px) {
      .file-viewer-card {
        height: 85vh;
        padding: 18px 12px;
      }
      .file-viewer-title {
        font-size: 21px;
      }
    }
  `;
  document.head.appendChild(style);
}
function createFileViewer() {
  const viewer = document.createElement("section");
  viewer.className = "file-viewer";
  viewer.hidden = true;
  viewer.setAttribute("role", "dialog");
  viewer.setAttribute("aria-modal", "true");
  viewer.setAttribute("aria-labelledby", "fileViewerTitle");
  viewer.id = "fileViewer";
  viewer.innerHTML = `
    <div class="file-viewer-card">
      <button class="file-viewer-close" type="button"
          aria-label="Close file viewer">×</button>
      <h2 class="file-viewer-title" id="fileViewerTitle"></h2>
      <div class="file-viewer-content"></div>
    </div>
  `;
  document.body.appendChild(viewer);
  const closeButton = viewer.querySelector(".file-viewer-close");
  closeButton.addEventListener("click", closeFileViewer);
  viewer.addEventListener("click", (event) => {
    if (event.target === viewer) {
      closeFileViewer();
    }
  });
  return viewer;
}
function closeFileViewer() {
  const viewer = $("#fileViewer");
  if (!viewer) {
    return;
  }
  viewer.hidden = true;
  viewer.querySelector(".file-viewer-content").replaceChildren();
  if (viewerObjectUrl) {
    URL.revokeObjectURL(viewerObjectUrl);
    viewerObjectUrl = null;
  }
}
function viewSelectedFile() {
  const file = getSelectedFile();
  if (!file) {
    showToast("Please select a file to view.");
    return;
  }
  const viewer = $("#fileViewer");
  const title = viewer.querySelector(".file-viewer-title");
  const content = viewer.querySelector(".file-viewer-content");
  title.textContent = file.name;
  content.replaceChildren();
  if (viewerObjectUrl) {
    URL.revokeObjectURL(viewerObjectUrl);
    viewerObjectUrl = null;
  }
  viewerObjectUrl = URL.createObjectURL(file);
  if (file.type.startsWith("image/")) {
    const image = document.createElement("img");
    image.src = viewerObjectUrl;
    image.alt = file.name;
    content.appendChild(image);
  } else if (file.type === "application/pdf") {
    const frame = document.createElement("iframe");
    frame.src = viewerObjectUrl;
    frame.title = `Preview of ${file.name}`;
    content.appendChild(frame);
  } else if (file.type.startsWith("audio/")) {
    const audio = document.createElement("audio");
    audio.src = viewerObjectUrl;
    audio.controls = true;
    content.appendChild(audio);
  } else if (file.type.startsWith("video/")) {
    const video = document.createElement("video");
    video.src = viewerObjectUrl;
    video.controls = true;
    video.playsInline = true;
    content.appendChild(video);
  } else if (
    file.type.startsWith("text/") ||
    /\.(txt|csv|json|html|css|js|xml|md)$/i.test(file.name)
  ) {
    const reader = new FileReader();
    reader.onload = () => {
      const text = document.createElement("pre");
      text.className = "file-viewer-text";
      text.textContent = reader.result;
      content.appendChild(text);
    };
    reader.readAsText(file);
  } else {
    const message = document.createElement("p");
    message.textContent = "Preview is not available for this file type.";
    content.appendChild(message);
  }
  viewer.hidden = false;
  viewer.querySelector(".file-viewer-close").focus();
}
function stopCamera() {
  if (cameraStream) {
    cameraStream.getTracks().forEach((track) => track.stop());
    cameraStream = null;
  }
  cameraVideo.srcObject = null;
}
async function requestCameraAccess() {
  if (!window.isSecureContext) {
    setStatus("Camera access requires HTTPS or localhost.");
    cameraPlaceholder.hidden = false;
    cameraPlaceholder.textContent = "Use HTTPS or localhost for camera access.";
    return false;
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    setStatus("Camera access is not supported by this browser.");
    cameraPlaceholder.hidden = false;
    cameraPlaceholder.textContent = "Camera access is not supported.";
    return false;
  }
  stopCamera();
  setStatus("Please allow camera access when prompted.");
  cameraPlaceholder.hidden = false;
  cameraPlaceholder.textContent = "Waiting for camera permission…";
  try {
    cameraStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: { ideal: "environment" } },
      audio: false
    });
    cameraVideo.srcObject = cameraStream;
    cameraPlaceholder.hidden = true;
    setStatus("Camera access granted. Camera is ready.");
    return true;
  } catch (error) {
    stopCamera();
    cameraPlaceholder.hidden = false;
    cameraPlaceholder.textContent = "Camera access failed.";
    if (error.name === "NotAllowedError") {
      setStatus("Camera permission was denied.");
      cameraPlaceholder.textContent = "Allow camera access and reopen AI Lens.";
    } else if (error.name === "NotFoundError") {
      setStatus("No camera was found.");
      cameraPlaceholder.textContent = "No camera was found.";
    } else {
      setStatus("Unable to access the camera.");
    }
    return false;
  }
}
async function openLens() {
  lensOverlay.hidden = false;
  lensOverlay.setAttribute("aria-hidden", "false");
  lensCloseButton.focus();
  await requestCameraAccess();
}
function closeLens() {
  stopCamera();
  if (selectedImageUrl) {
    URL.revokeObjectURL(selectedImageUrl);
    selectedImageUrl = null;
  }
  cameraVideo.removeAttribute("src");
  cameraVideo.load();
  lensOverlay.hidden = true;
  lensOverlay.setAttribute("aria-hidden", "true");
  closeMenu(lensMenuButton, lensMenu);
}
function captureImage() {
  if (!cameraStream || !cameraVideo.videoWidth) {
    setStatus("Camera is not ready yet.");
    return;
  }
  captureCanvas.width = cameraVideo.videoWidth;
  captureCanvas.height = cameraVideo.videoHeight;
  const context = captureCanvas.getContext("2d");
  if (!context) {
    setStatus("Unable to capture the image.");
    return;
  }
  context.drawImage(
    cameraVideo,
    0,
    0,
    captureCanvas.width,
    captureCanvas.height
  );
  captureCanvas.toBlob((blob) => {
    if (!blob) {
      setStatus("Unable to create the captured image.");
      return;
    }
    const imageUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = imageUrl;
    link.download = `palash-ai-${Date.now()}.jpg`;
    link.click();
    URL.revokeObjectURL(imageUrl);
    setStatus("Image captured and downloaded.");
  }, "image/jpeg", 0.9);
}
function handleGallerySelection(event) {
  const file = event.target.files[0];
  if (!file) {
    return;
  }
  if (selectedImageUrl) {
    URL.revokeObjectURL(selectedImageUrl);
  }
  selectedImageUrl = URL.createObjectURL(file);
  stopCamera();
  cameraVideo.src = selectedImageUrl;
  cameraVideo.load();
  cameraPlaceholder.hidden = true;
  setStatus(`Selected ${file.name}.`);
  event.target.value = "";
}
mainMenuButton.addEventListener("click", () => {
  toggleMenu(mainMenuButton, mainMenu);
});
lensMenuButton.addEventListener("click", () => {
  toggleMenu(lensMenuButton, lensMenu);
});
$("#loginBtn").addEventListener("click", () => {
  showToast("Login portal coming soon.");
});
$("#centerNode").addEventListener("click", toggleFeatures);
document.querySelectorAll(".satellite").forEach((button) => {
  button.addEventListener("click", () => {
    const featureName = button.dataset.feature;
    if (featureName === "AI Lens") {
      openLens();
    } else if (featureName === "Add file") {
      openConverter();
    } else if (featureName === "Text converter") {
      openTextConverter();
    } else if (featureName === "Speech converter") {
      openSpeechConverter();
    } else {
      showToast(`${featureName} is coming soon.`);
    }
  });
});
document.querySelectorAll("[data-menu-action]").forEach((button) => {
  button.addEventListener("click", handleMenuAction);
});
sourceLanguage.addEventListener("change", () => {
  if (sourceLanguage.value === targetLanguage.value) {
    targetLanguage.value = sourceLanguage.value === "hi" ? "sat" : "hi";
  }
  updateSpeechLabels();
  resetSpeechContent();
});
targetLanguage.addEventListener("change", () => {
  if (sourceLanguage.value === targetLanguage.value) {
    sourceLanguage.value = targetLanguage.value === "sat" ? "hi" : "sat";
  }
  updateSpeechLabels();
  resetSpeechContent();
});
recordBtn.addEventListener("click", toggleSpeechRecording);
speechConvertBtn.addEventListener("click", convertSpeech);
playBtn.addEventListener("click", playTranslatedSpeech);
speechClose.addEventListener("click", closeSpeechConverter);
speechMenuBtn.addEventListener("click", () => {
  const isOpen = speechSideMenu.classList.toggle("open");
  speechSideMenu.hidden = !isOpen;
  speechMenuOverlay.hidden = !isOpen;
  speechMenuOverlay.classList.toggle("active", isOpen);
  speechMenuBtn.setAttribute("aria-expanded", String(isOpen));
});
speechMenuOverlay.addEventListener("click", closeSpeechMenu);
document.querySelectorAll("[data-speech-menu]").forEach((button) => {
  button.addEventListener("click", () => {
    closeSpeechMenu();
    if (button.dataset.speechMenu === "History") {
      showTranslationHistory();
    } else {
      showToast(`${button.dataset.speechMenu} selected.`);
    }
  });
});
historyBtn.addEventListener("click", showTranslationHistory);
speechOverlay.addEventListener("click", (event) => {
  if (event.target === speechOverlay) closeSpeechConverter();
});
addFile.addEventListener("click", () => fileInput.click());
uploadBtn.addEventListener("click", uploadFiles);
textSourceLanguage.addEventListener("change", () => {
  updateTextTargetLanguage();
  updateTextLanguageUI();
  resetTextResult();
});
textTargetLanguage.addEventListener("change", () => {
  if (textSourceLanguage.value !== "hindi") {
    textTargetLanguage.value = "hindi";
    showToast("Regional languages translate to Hindi.");
  }
  updateTextLanguageUI();
  resetTextResult();
});
textSwapButton.addEventListener("click", () => {
  const source = textSourceLanguage.value;
  const target = textTargetLanguage.value;
  const input = textSourceText.value;
  const output = textResult.textContent.trim();
  textSourceLanguage.value = target;
  textTargetLanguage.value = source;
  updateTextTargetLanguage();
  updateTextLanguageUI();
  textSourceText.value = output && !output.includes("Your translated text")
    ? output
    : "";
  textResult.textContent = input.trim() ? input : "";
  textCharacterCount.textContent = `${textSourceText.value.length} / 1000`;
  textTranslationStatus.textContent = "Ready to translate";
});
textSourceText.addEventListener("input", () => {
  textCharacterCount.textContent = `${textSourceText.value.length} / 1000`;
});
$("#textClearButton").addEventListener("click", () => {
  textSourceText.value = "";
  textCharacterCount.textContent = "0 / 1000";
  textResult.innerHTML = "<span>Your translated text will appear here...</span>";
  textTranslationStatus.textContent = "Ready to translate";
});
$("#textInputSpeakButton").addEventListener("click", () => {
  speakText(textSourceText.value.trim(), textSourceLanguage.value);
});
$("#textOutputSpeakButton").addEventListener("click", () => {
  speakText(textResult.textContent.trim(), textTargetLanguage.value);
});
$("#textCopyButton").addEventListener("click", async () => {
  const text = textResult.textContent.trim();
  if (!text || text.includes("Your translated text")) {
    showToast("Convert text first.");
    return;
  }
  try {
    await navigator.clipboard.writeText(text);
    showToast("Translation copied.");
  } catch (error) {
    showToast("Unable to copy translation.");
  }
});
textConvertButton.addEventListener("click", convertText);
textConverterClose.addEventListener("click", closeTextConverter);
textMenuButton.addEventListener("click", () => {
  const isOpen = textConverterMenu.hidden;
  textConverterMenu.hidden = !isOpen;
  textMenuOverlay.hidden = !isOpen;
  textMenuButton.setAttribute("aria-expanded", String(isOpen));
});
textMenuOverlay.addEventListener("click", closeTextConverter);
document.querySelectorAll("[data-text-menu]").forEach((button) => {
  button.addEventListener("click", () => {
    closeTextConverter();
    if (button.dataset.textMenu === "History") showTranslationHistory();
    else showToast(`${button.dataset.textMenu} selected.`);
  });
});
textConverterOverlay.addEventListener("click", (event) => {
  if (event.target === textConverterOverlay) closeTextConverter();
});
convertBtn.addEventListener("click", convertFiles);
fileInput.addEventListener("change", selectFiles);
converterClose.addEventListener("click", closeConverter);
lensCloseButton.addEventListener("click", closeLens);
$("#galleryBtn").addEventListener("click", () => galleryInput.click());
$("#shutterBtn").addEventListener("click", captureImage);
galleryInput.addEventListener("change", handleGallerySelection);
addViewerStyles();
const viewFileButton = document.createElement("button");
viewFileButton.type = "button";
viewFileButton.className = "view-file-btn";
viewFileButton.textContent = "View Selected File";
viewFileButton.addEventListener("click", viewSelectedFile);
convertBtn.parentElement.appendChild(viewFileButton);
createFileViewer();
document.addEventListener("keydown", (event) => {
  if (event.key !== "Escape") {
    return;
  }
  closeMenu(mainMenuButton, mainMenu);
  closeMenu(lensMenuButton, lensMenu);
  closeSpeechMenu();
  stopRecordingTimer();
  clearInterval(outputInterval);
  if (!lensOverlay.hidden) {
    closeLens();
  }
  if (!converterOverlay.hidden) {
    closeConverter();
  }
  if (!speechOverlay.hidden) {
    closeSpeechConverter();
  }
  if (!textConverterOverlay.hidden) {
    closeTextConverter();
  }
  const viewer = $("#fileViewer");
  if (viewer && !viewer.hidden) {
    closeFileViewer();
  }
});
lensOverlay.addEventListener("click", (event) => {
  if (event.target === lensOverlay) {
    closeLens();
  }
});
converterOverlay.addEventListener("click", (event) => {
  if (event.target === converterOverlay) {
    closeConverter();
  }
});
window.addEventListener("pagehide", () => {
  stopCamera();
  if (viewerObjectUrl) {
    URL.revokeObjectURL(viewerObjectUrl);
  }
});