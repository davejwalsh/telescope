

  function fetchAvailableDevices() {
      fetch('/device/getAvailableDevices')
          .then(response => response.json())
          .then(data => {
              const deviceList = document.getElementById('deviceList');
              const deviceSelect = document.getElementById('currentDeviceId');

              deviceList.innerHTML = '';
              deviceSelect.innerHTML = '';

              data.forEach(deviceId => {
                  const listItem = document.createElement('li');
                  listItem.textContent = `Device ID: ${deviceId}`;
                  deviceList.appendChild(listItem);
                  const option = document.createElement('option');
                  option.value = deviceId;
                  option.textContent = `Device ID: ${deviceId}`;
                  deviceSelect.appendChild(option);
              });

              if (data.length > 0) {
                  selectedDeviceId = data[0];
                  deviceSelect.value = selectedDeviceId;
              }
          })
          .catch(error => addMessageToTop('Error fetching devices: ' + error));
  }

  function startDevice() {
      fetch(`/device/startDevice`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => addMessageToTop('Error starting device:' + error));
  }

  function stopDevice() {
      fetch(`/device/stopDevice`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => addMessageToTop('Error stopping device:' + error));
  }

  function onDeviceSelect() {
      const deviceId = document.getElementById('currentDeviceId').value;
      selectedDeviceId = deviceId;
      fetch(`/device/setSelectedDevice?deviceId=${deviceId}`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => console.error('Error setting coordinates:', error));
      const deviceIdBox = document.getElementById('selectedDeviceId').textContent = selectedDeviceId.toString();
  }

  function initaliseSelectedDevice() {
      selectedDeviceId = 0;
      fetch(`/device/setSelectedDevice?deviceId=${selectedDeviceId}`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => console.error('Error setting coordinates:', error));
      const deviceIdBox = document.getElementById('selectedDeviceId').textContent = selectedDeviceId.toString();
  }



  function addNewDevice() {
      fetch('/device/addNewDevice', {
              method: 'POST',
              headers: {
                  'Content-Type': 'application/json'
              },
              body: JSON.stringify({ /* NOOP */ })
          })
          .then(response => addMessageToTop(response.json()))
          .then(data => {
              console.log('New device added:', data);
              fetchAvailableDevices();
          })
          .catch(error => addMessageToTop('Error adding device:' + error));
  }

    function setShutterTime() {
        const shutterTime = parseFloat(document.getElementById('shutter-time').value);
        fetch(`/device/setShutterTime?shutterTime=${shutterTime}`, {
                method: 'POST'
            })
            .then(response => response.text())
            .then(data => addMessageToTop(data))
            .catch(error => addMessageToTop('Error setting shutter time:' + error));
    }

     function setMovementRate() {
            const movementRate = parseFloat(document.getElementById('movement-rate').value);
            fetch(`/device/setMovementRate?movementRate=${movementRate}`, {
                    method: 'POST'
                })
                .then(response => response.text())
                .then(data => addMessageToTop(data))
                .catch(error => addMessageToTop('Error setting movement rate:' + error));
        }

    function induceMotorError() {
        fetch(`/device/induceMotorError`, {
                method: 'POST'
            })
            .then(response => response.text())
            .then(data => addMessageToTop(data))
            .catch(error => addMessageToTop('Error inducing motor error:' + error));
    }

    function induceReceiverError() {
        fetch(`/device/induceReceiverError`, {
                method: 'POST'
            })
            .then(response => response.text())
            .then(data => addMessageToTop(data))
            .catch(error => addMessageToTop('Error inducing motor error:' + error));
    }

  function setTargetCoordinates() {
      const xCoord = parseFloat(document.getElementById('targetX').value);
      const yCoord = parseFloat(document.getElementById('targetY').value);
      const zCoord = parseFloat(document.getElementById('targetZ').value);
      fetch(`/device/setTargetCoordinates?xCoord=${xCoord}&yCoord=${yCoord}&zCoord=${zCoord}`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => addMessageToTop('Error setting coordinates:' + error));
  }

  function moveToTargetCoordinates() {
      const xCoord = parseFloat(document.getElementById('targetX').value);
      const yCoord = parseFloat(document.getElementById('targetY').value);
      const zCoord = parseFloat(document.getElementById('targetZ').value);
      fetch(`/device/moveToTargetCoordinates`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => addMessageToTop('Error moving to coordinates:' + error));
  }

  function pauseMovement() {
      fetch(`/device/pauseMovement`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => addMessageToTop('Error pausing movement:' + error));
  }



  function startReceiving() {
      fetch(`/device/startReceiving`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => addMessageToTop('Error starting device:' + error));
  }

  function stopReceiving() {
      fetch(`/device/stopReceiving`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => addMessageToTop('Error stopping device:' + error));
  }

  function resetDevice() {
      fetch(`/device/resetDevice`, {
              method: 'POST'
          })
          .then(response => response.text())
          .then(data => addMessageToTop(data))
          .catch(error => addMessageToTop('Error resetting device:' + error));
  }