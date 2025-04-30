  function updateStatusColor(elementId, status) {
      const statusBox = document.getElementById(elementId);
      switch (status) {
          case 'ERROR':
              statusBox.style.backgroundColor = 'red';
              break;
          case 'READY':
              statusBox.style.backgroundColor = 'green';
              break;
          case 'RECEIVING':
              statusBox.style.backgroundColor = 'yellow';
              break;
          case 'MOVING':
              statusBox.style.backgroundColor = 'blue';
              break;
          default:
              statusBox.style.backgroundColor = 'transparent';
      }
  }

  function addMessageToTop(message) {
      const messageList = document.getElementById('messageList');
      const newMessage = document.createElement('li');

      let messageObj;
      try {
          messageObj = JSON.parse(message);
      } catch (e) {
          messageObj = {
              message: message
          };
      }

      newMessage.textContent = messageObj.message || message;

      if (messageObj.status === "SUCCESS") {
          newMessage.style.backgroundColor = "green";
          newMessage.style.color = "white";
      } else if (messageObj.status === "ERROR") {
          newMessage.style.backgroundColor = "red";
          newMessage.style.color = "white";
      } else if (messageObj.status === "FAIL") {
          newMessage.style.backgroundColor = "yellow";
          newMessage.style.color = "black";
      } else if (messageObj.status === "MOVING") {
          newMessage.style.backgroundColor = "blue";
          newMessage.style.color = "black";
      } else if (messageObj.status === "IDLE") {
          newMessage.style.backgroundColor = "lightgreen";
          newMessage.style.color = "black";
      } else if (messageObj.status === "OFF") {
          newMessage.style.backgroundColor = "lightgray";
          newMessage.style.color = "black";
      }
      newMessage.style.padding = "5px";
      newMessage.style.marginBottom = "5px";
      messageList.insertBefore(newMessage, messageList.firstChild);
  }