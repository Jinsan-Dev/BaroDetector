var SAAgent,
    SASocket,
    SAMessageObj,
    gFileTransfer,
    SAPeerAgent = null;

var agentCallback = {
    onconnect: function(socket) {
        SASocket = socket;
        console.log("Entering the agentCallback, SASocket: " + SASocket);
        SASocket.setDataReceiveListener(onreceive);
        console.log("connected to the Android agent");
        setConnectionStatusHTML(true);

        SASocket.setSocketStatusListener(function(reason) {
            console.log("Connection with androidAgent lost. Reason : [" + reason + "]");
            SASocket.close();
            SASocket = null;
            setConnectionStatusHTML(false);
        });
    },
    onerror: onerror
};

var peerAgentFindCallback = {
    onpeeragentfound: function(peerAgent) {
        try {
            SAAgent.setServiceConnectionListener(agentCallback);
            SAAgent.requestServiceConnection(peerAgent);
        } catch (err) {
            console.log("Failed to request service connection [" + err.name + "] msg[" + err.message + "]");
        }
    },
    onerror: onerror
};


var filesendcallback = {
    oncomplete: function() {
        console.log('send Completed!!');
    },
    onerror: function(errCode, id) {
        console.log('Failed to send File. : ' + ' errorCode :' + errCode);
    }
};

const CHANNELID = 110;
// new var
var statusText1, statusText2;
var recording = false;
var documentsDir;
var pressureSensor = tizen.sensorservice.getDefaultSensor("PRESSURE");
var linearAccelerationSensor = tizen.sensorservice.getDefaultSensor("LINEAR_ACCELERATION");
var timeStamp = Date.now();
var baroFilename = timeStamp + 'baro.csv',
    baroFilestream = {
        value: null
    };
var accelerometerFilename = timeStamp + 'accelerometer.csv',
    accelerometerFilestream = {
        value: null
    };
var current_status = "NA";
/* Make Provider application running in background */
tizen.application.getCurrentApplication().hide();

function printStatus(status) {
    console.log("status:" + status);
}

function createHTML(log_string) {
    console.log("log string : " + log_string);
    var content = document.getElementById("toast-content");
    content.innerHTML = log_string;
    //tau.openPopup("#toast");
}

function onreceive(channelId, data) {
    //submitFilesToAndroidAgent();
    console.log("Process comes to onreceive");
    submitBaro();
}

function messageReceivedCallback(peerAgent, data) {
    createHTML("Msg recevied from the [" + peerAgent.peerId + "] : " + data);
    SAPeerAgent = peerAgent;
    console.log("SAPeerAgent: ", SAPeerAgent);
    if (recording === false) {
        recording = true;
        setRecordingStatusHTML(recording);
        startBarometerCollection();
        startLinearAccelerationCollection();
    } else if (recording == true && data == "outdoor") {
        current_status = "outdoor";
    } else if (recording == true && data == "indoor") {
        current_status = "indoor";
    } else if (recording == true && data == "semi") {
        current_status = "passing";
    } else {
        recording = false;
        setRecordingStatusHTML(recording);
        stopBarometerCollection();
        stopAccCollection();
        submitBaro();
        submitAcc();
        clearBaro();
        clearAcc();
    }
}

//when pairing succed
function requestOnSuccess(agents) {
    var i = 0;
    for (i; i < agents.length; i += 1) {
        if (agents[i].role === "PROVIDER") {
            createHTML("Service Provider found!<br />" +
                "Name: " + agents[i].name);
            SAAgent = agents[i];
            break;
        }
    }

    SAMessageObj = SAAgent.getSAMessage();
    gFileTransfer = SAAgent.getSAFileTransfer();
    gFileTransfer.setFileSendListener(filesendcallback);
    if (SAMessageObj) {
        printStatus("Connect...");
        setConnectionStatusHTML(true);
        SAMessageObj.setMessageReceiveListener(messageReceivedCallback);
    } else {
        printStatus("Disconnect...");
        setConnectionStatusHTML(false);
    }
}

//when pairing failed
function requestOnError(e) {
    setConnectionStatusHTML(false);
    createHTML("requestSAAgent Error" +
        "Error name : " + e.name + "<br />" +
        "Error message : " + e.message);
}

// end of previous func

//start of new func

function startBarometerCollection() {
    pressureSensor.start(function() {
        pressureSensor.getPressureSensorData(function onGetSuccessCB(sensorData) {
            var timestamp = new Date().getTime();
            saveBaroSamples(timestamp + "," + sensorData.pressure + "," + current_status);
        }, function onerrorCB(error) {
            console.log("Error occurred: " + error);
        });
        pressureSensor.setChangeListener(function(sensorData) {
            var timestamp = new Date().getTime();
            saveBaroSamples(timestamp + "," + sensorData.pressure + "," + current_status);
        }, 100); //The second determines the amount of time (in milliseconds) passing between 2 consecutive events. Valid values are integers from 10 to 1000, inclusively. For example, the value 100 results in approximately 10 events being send every second.
    });
}

function startLinearAccelerationCollection() {
    linearAccelerationSensor.start(function() {
        linearAccelerationSensor.getLinearAccelerationSensorData(function(AccData) {
            var timestamp = new Date().getTime();
            saveAccelerometerSample(timestamp + "," + AccData.x + "," + AccData.y + "," + AccData.z + "," + current_status);
        }, function(error) {
            console.log("error occurred:" + error);
        });
        linearAccelerationSensor.setChangeListener(function(AccData) {
            var timestamp = new Date().getTime();
            saveAccelerometerSample(timestamp + "," + AccData.x + "," + AccData.y + "," + AccData.z + "," + current_status);
        }, 100);
    });
    console.log('Linear acc collection started');
}

function saveBaroSamples(sample) {
    if (recording) {
        baroFilestream.value.write(sample + '\n');
    }
}

function saveAccelerometerSample(sample) {
    if (recording) {
        accelerometerFilestream.value.write(sample + '\n');
    }
}

//binding file
function bindFile(onSuccess) {
    tizen.filesystem.resolve('documents/' + baroFilename, function(file) {
        file.openStream('w', function(fs) {
            baroFilestream.value = fs;
        }, function(e) {
            console.log('failed to bind to an existing file : ' + baroFilename + ', error : ' + e.message);
        });
    }, function(error) {
        var file = documentsDir.createFile(baroFilename);
        if (file === null) {
            console.log('failed to create a new file : ' + baroFilename);
        } else {
            file.openStream('w', function(fs) {
                baroFilestream.value = fs;
            }, function(e) {
                console.log('failed to bind to a new file : ' + baroFilename + ', error : ' + e.message);
            });
        }
    });
}

function bindAccelerometer(onSuccess) {
    tizen.filesystem.resolve('documents/' + accelerometerFilename, function(file) {
        // accelerometer file exists
        file.openStream('w', function(fs) {
            accelerometerFilestream.value = fs;
        }, function(e) {
            console.log('failed to bind to an existing file : ' + accelerometerFilename + ', error : ' + e.message);
        });
    }, function(error) {
        // accelerometer file is missing
        var file = documentsDir.createFile(accelerometerFilename);
        if (file === null) {
            console.log('failed to create a new file : ' + accelerometerFilename);
        } else {
            file.openStream('w', function(fs) {
                accelerometerFilestream.value = fs;
            }, function(e) {
                console.log('failed to bind to a new file : ' + accelerometerFilename + ', error : ' + e.message);
            });
        }
    });
}

function submitBaro() {
    ftSend('documents/' + baroFilename, function() {
        console.log('Succeed to send file');
    }, function(err) {
        console.log('Failed to send File ' + err);
    });
    baroFilestream.value.close();
}

function clearBaro() {
    documentsDir.deleteFile('documents/' + baroFilename, function() {
        console.log(baroFilename + " is deleted");
    }, function(e) {
        console.log('failed to delete ' + baroFilename + ', error : ' + e.message);
    });
}

function submitAcc() {
    ftSend('documents/' + accelerometerFilename, function() {
        console.log('Succeed to send file');
    }, function(err) {
        console.log('Failed to send File ' + err);
    });
    accelerometerFilestream.value.close();
}

function clearAcc() {
    documentsDir.deleteFile('documents/' + accelerometerFilename, function() {
        console.log(accelerometerFilename + " is deleted");
    }, function(e) {
        console.log('failed to delete ' + accelerometerFilename + ', error : ' + e.message);
    });
}

function ftSend(path, successCb, errorCb) {
    try {
        var transferId = gFileTransfer.sendFile(SAPeerAgent, path);
        successCb(transferId);
    } catch (err) {
        console.log('sendFile exception <' + err.name + '> : ' + err.message);
        window.setTimeout(function() {
            errorCb({
                name: 'RequestFailedError',
                message: 'send request failed'
            });
        }, 0);
    }
}

function stopBarometerCollection() {
    try {
        pressureSensor.stop();
    } catch (err) {
        console.log(err.name + ': ' + err.message);
    }
}

function stopAccCollection() {
    try {
        linearAccelerationSensor.stop();
    } catch (err) {
        console.log(err.name + ': ' + err.message);
    }
}

function setConnectionStatusHTML(connectionStatus) {
    if (connectionStatus) {
        statusText1.style.color = '#2ecc71';
        statusText1.innerHTML = 'CONNECTED';
    } else {
        statusText1.style.color = 'red';
        statusText1.innerHTML = 'DISCONNECTED';
    }
}

function setRecordingStatusHTML(recording) {
    if (recording) {
        statusText2.style.color = '#2ecc71';
        statusText2.innerHTML = 'Now recording...';
    } else {
        statusText2.style.color = 'red';
        statusText2.innerHTML = 'Not recorded now';
    }
}


//end of new func
/* Requests the SAAgent specified in the Accessory Service Profile */
webapis.sa.requestSAAgent(requestOnSuccess, requestOnError);

(function() {
    /* Basic Gear gesture & buttons handler */
    window.addEventListener('tizenhwkey', function(ev) {
        var page,
            pageid;

        if (ev.keyName === "back") {
            page = document.getElementsByClassName('ui-page-active')[0];
            pageid = page ? page.id : "";
            if (pageid === "main") {
                try {
                    tizen.application.getCurrentApplication().exit();
                } catch (ignore) {}
            } else {
                window.history.back();
            }
        }
    });

    statusText1 = document.getElementById("connect_status");
    statusText2 = document.getElementById("recording_status");
    tizen.ppm.requestPermission("http://tizen.org/privilege/mediastorage", function() {
        tizen.ppm.requestPermission("http://tizen.org/privilege/healthinfo", function() {
            tizen.filesystem.resolve("documents", function(dir) {
                documentsDir = dir;
                bindFile();
                bindAccelerometer();
            }, function(error) {
                console.log('resolve error : ' + error.message);
            }, "rw");
        }, function(error) {
            console.log('resolve permission error : ' + error.message);
        });
    }, function(error) {
        console.log('resolve permission error : ' + error.message);
    });


}());

(function(tau) {
    var toastPopup = document.getElementById('toast');

    toastPopup.addEventListener('popupshow', function(ev) {
        setTimeout(function() {
            tau.closePopup();
        }, 2000);
    }, false);
})(window.tau);