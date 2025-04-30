  let stompClient = null;

  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function(frame) {
      console.log('Connected: ' + frame);
      fetchAvailableDevices();
      initaliseSelectedDevice();
      subscribeToDeviceData();
  });

  function subscribeToDeviceData() {
      stompClient.subscribe(`/telescope/deviceState`, function(message) {
          const deviceState = JSON.parse(message.body);
          document.getElementById('deviceStatus').textContent = `${deviceState.toString()}`;
          const raw = JSON.parse(message.body);
          formattedMessage = JSON.stringify({
              message: `Device State: ${raw}`,
              status: raw.toUpperCase()
          });
          addMessageToTop(formattedMessage);
          updateStatusColor('device-status-box', deviceState.toString());
      });

      stompClient.subscribe(`/telescope/motorPosition`, function(message) {
          const position = JSON.parse(message.body);
          document.getElementById('motorPositionX').textContent = position.x.toFixed(2);
          document.getElementById('motorPositionY').textContent = position.y.toFixed(2);
          document.getElementById('motorPositionZ').textContent = position.z.toFixed(2);
      });

      stompClient.subscribe(`/telescope/targetPosition`, function(message) {
          const position = JSON.parse(message.body);
          document.getElementById('currentTargetX').textContent = position.x.toFixed(2);
          document.getElementById('currentTargetY').textContent = position.y.toFixed(2);
          document.getElementById('currentTargetZ').textContent = position.z.toFixed(2);
      });

      stompClient.subscribe(`/telescope/motorState`, function(message) {
          const motorState = JSON.parse(message.body);
          document.getElementById('motorStatus').textContent = `Motor: ${motorState.toString()}`;
          const raw = JSON.parse(message.body);
          formattedMessage = JSON.stringify({
              message: `Motor State: ${raw}`,
              status: raw.toUpperCase()
          });
          addMessageToTop(formattedMessage);

          updateStatusColor('motor-status-box', motorState.toString());
      });

      stompClient.subscribe(`/telescope/powerMeasurement`, function(message) {
          const power = JSON.parse(message.body);
          document.getElementById('powerReading').textContent = `${power.measurement.toFixed(2)} dBm`;
      });

      stompClient.subscribe(`/telescope/receiverState`, function(message) {
          try {
              let raw = JSON.parse(message.body);
              let formattedMessage;
              if (typeof raw === 'string') {
                  formattedMessage = JSON.stringify({
                      message: `Receiver State: ${raw}`,
                      status: raw.toUpperCase()
                  });
                  if (raw != "RECEIVING") addMessageToTop(formattedMessage);
              }
              document.getElementById('receiverStatus').textContent = `Receiver: ${raw}`;
              updateStatusColor('receiver-status-box', raw);
          } catch (err) {
              console.error('Error parsing receiverState:', err);
              addMessageToTop(JSON.stringify({
                  message: 'Receiver State Parse Error: ' + err.message,
                  status: 'ERROR'
              }));
          }
      });

      stompClient.subscribe(`/telescope/overpowered`, function(message) {
          const isOverpowered = JSON.parse(message.body); // this gives you a boolean
          const el = document.getElementById('overpowered');

          if (isOverpowered === true) {
              el.style.visibility = 'visible';
              el.style.display = 'block';
          } else {
              el.style.visibility = 'hidden';
              el.style.display = 'none';
          }
      });

  }