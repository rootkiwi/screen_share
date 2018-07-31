var numberOfRetries = 0;
var h264Player;
var fpsMbitsIntervalId = null;
var fps = 0;
var timeLastStatsUpdate = 0;
var bytesTransferred = 0;
var totalBytesTransferredLastCheck = 0;
var imgWidth;
var imgHeight;
var lastImgWidth;
var lastImgHeight;
var scaleToWindow = true;

var fpsValue = select("#fpsValue");
var bitsPerSecondValue = select("#bitsPerSecondValue");
var bitsPerSecondLabel = select("#bitsPerSecondLabel");
var megabytesTransferredValue = select("#megabytesTransferredValue");
var resolutionWidthValue = select("#resolutionWidthValue");
var resolutionHeightValue = select("#resolutionHeightValue");
var canvasContainer = select("#canvasContainer");
var clippedCanvasContainer = select("#clippedCanvasContainer");
var messageContainer = select("#messageContainer");
var message = select("#message");

function setupH264CanvasPlayer() {
    h264Player = new Player({
        useWorker: true,
        workerFile: "js/broadway/Decoder.min.js",
        webgl: "auto"
    });
    var canvas = h264Player.canvas;
    canvas.width = 0;
    canvas.height = 0;
    clippedCanvasContainer.appendChild(canvas);
}

function connectWebSocket() {
    try {
        var proto = location.protocol === "https:" ? "wss://" : "ws://";
        var port = location.port ? ':'+location.port : '';
        var ws = new WebSocket(proto + location.hostname + port + "/ws/");
        ws.binaryType = "arraybuffer";
        ws.addEventListener("message", function(frame) {
            handleNewFrame(frame);
        });
        ws.addEventListener("close", function() {
            exitFullscreen();
            hideCanvasContainer();
            showConnectingMessage();
            retryConnect();
            clearStats();
        });
        ws.addEventListener("open", function() {
            hideConnectingMessage();
            showCanvasContainer();
            numberOfRetries = 0;
            megabytesTransferredValue.textContent = "0.00";
            fpsMbitsIntervalId = window.setInterval(updateStats, 1000);
        });
    } catch (e) {
        retryConnect();
    }
}

function updateStats() {
    var timeElapsedSeconds = (performance.now()-timeLastStatsUpdate) / 1000;
    fpsValue.textContent = Math.floor(fps / timeElapsedSeconds);
    megabytesTransferredValue.textContent = (bytesTransferred / 1024 / 1024).toFixed(2);

    if (timeLastStatsUpdate !== 0) {
        var bitsTransferredSinceLastTime = (bytesTransferred-totalBytesTransferredLastCheck) * 8;
        var bitsPerSecond = bitsTransferredSinceLastTime / timeElapsedSeconds;
        if (bitsPerSecond < 1024*1024) {
            bitsPerSecondLabel.textContent = "kbit/s";
            bitsPerSecondValue.textContent = (bitsPerSecond / 1024).toFixed(2);
        } else {
            bitsPerSecondLabel.textContent = "mbit/s";
            bitsPerSecondValue.textContent = (bitsPerSecond / 1024 / 1024).toFixed(2);
        }
    }
    timeLastStatsUpdate = performance.now();
    fps = 0;
    totalBytesTransferredLastCheck = bytesTransferred;
    resolutionWidthValue.textContent = imgWidth;
    resolutionHeightValue.textContent = imgHeight;
}

function retryConnect() {
    if (numberOfRetries < 10) {
        setTimeout(function(){connectWebSocket()}, 2000);
        if (numberOfRetries > 0) {
            message.textContent = "retrying... (" + numberOfRetries + ")";
        }
    } else {
        message.innerHTML = "failed to connect<br>refresh to retry";
    }
    numberOfRetries++;
}

function clearStats() {
    clearInterval(fpsMbitsIntervalId);
    fpsValue.textContent = "0";
    bitsPerSecondValue.textContent = "0.00";
    resolutionWidthValue.textContent = "0";
    resolutionHeightValue.textContent = "0";
    fps = 0;
    bytesTransferred = 0;
    timeLastStatsUpdate = 0;
}

function showConnectingMessage() {
    messageContainer.classList.add("show");
    message.textContent = "connecting..."
}

function hideConnectingMessage() {
    messageContainer.classList.remove("show");
}

function showCanvasContainer() {
    canvasContainer.classList.add("show");
}

function hideCanvasContainer() {
    canvasContainer.classList.remove("show");
}

function handleNewFrame(frame) {
    var data = frame.data;
    var imgResolution = new DataView(data, 0, 4);
    imgWidth = imgResolution.getUint16(0);
    imgHeight = imgResolution.getUint16(2);
    if (lastImgWidth !== imgWidth || lastImgHeight !== imgHeight) {
        clippedCanvasContainer.style.width = imgWidth + "px";
        clippedCanvasContainer.style.height = imgHeight + "px";
        updateCanvasScale();
    }
    lastImgWidth = imgWidth;
    lastImgHeight = imgHeight;
    fps++;
    bytesTransferred += data.byteLength;
    h264Player.decode(new Uint8Array(data, 4));
}

function updateCanvasScale() {
    if (imgWidth === null || imgHeight === null) {
        return;
    }
    if (scaleToWindow) {
        var topBarHeight = 38;
        var scaleX = window.innerWidth / imgWidth;
        var scaleY = (window.innerHeight - topBarHeight) / imgHeight;
        var scaleToFit = Math.min(scaleX, scaleY);
        clippedCanvasContainer.style.transform = "scale(" + scaleToFit + ")" + "translate(-50%, -50%)";
        canvasContainer.classList.remove("actual");
        clippedCanvasContainer.classList.remove("actual");
        canvasContainer.classList.add("scale");
        clippedCanvasContainer.classList.add("scale");
    } else {
        clippedCanvasContainer.style.transform = "none";
        canvasContainer.classList.remove("scale");
        clippedCanvasContainer.classList.remove("scale");
        canvasContainer.classList.add("actual");
        clippedCanvasContainer.classList.add("actual");
    }
}

function select(selection) {
    return document.querySelector(selection);
}

function requestFullscreen() {
    if (document.body.requestFullscreen) {
        document.body.requestFullscreen();
    } else if (document.body.webkitRequestFullscreen) {
        document.body.webkitRequestFullscreen();
    } else if (document.body.mozRequestFullScreen) {
        document.body.mozRequestFullScreen();
    } else if (document.body.msRequestFullscreen) {
        document.body.msRequestFullscreen();
    }
}

function exitFullscreen() {
    if (document.exitFullscreen) {
        document.exitFullscreen();
    } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
    } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
    } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
    }
}

function init() {
    setupH264CanvasPlayer();
    showConnectingMessage();
    connectWebSocket();
    clippedCanvasContainer.addEventListener("dblclick", function() {
        var fullScreenEnabled = document.fullscreenEnabled ||
            document.webkitFullscreenEnabled ||
            document.mozFullScreenEnabled ||
            document.msFullscreenEnabled;
        if (fullScreenEnabled) {
            var isFullscreen = document.fullscreenElement ||
                document.webkitFullscreenElement ||
                document.mozFullScreenElement ||
                document.msFullscreenElement;
            if (isFullscreen) {
                exitFullscreen();
            } else {
                requestFullscreen();
            }
        }
    });
    var selectElement = select("#topBarRight select");
    selectElement.onchange = function() {
        scaleToWindow = selectElement.value === "scale";
        updateCanvasScale();
    };
    window.addEventListener("resize", updateCanvasScale);
}

window.addEventListener("DOMContentLoaded", init);
